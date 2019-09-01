package com.sccomponents.gauges.library;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Build;

import java.util.Arrays;

/**
 * Create a custom drawn copy of a given path.
 * <p>
 * This class draw a series of points on the given path with colors and dimensions taken by the
 * class settings on a bitmap and will print this bitmap on the given canvas.
 * The bitmap will redraw every time of a properties will changed so can be very expensive for
 * the global application performance.
 *
 * @author Samuele Carassai
 * @version 3.5.0
 * @since 2016-05-26
 */
@SuppressWarnings({"WeakerAccess"})
public class ScCopier extends ScFeature {

    // ***************************************************************************************
    // Private and protected variables

    private float[] mWidths;
    private WidthsMode mWidthsMode;

    private boolean mIsVisible;
    private Path mAreaPath;

    private BitmapShader mShader;
    private Paint mGenericPaint;
    private float[] mGenericPoint;
    private RectF mGenericRect;
    private Canvas mGenericCanvas;

    private boolean mNeedToGetPathInfo;
    private boolean mNeedToRedrawShader;
    private boolean mNeedToRedrawCover;

    private float[][] mPointsOutside;
    private float[][] mPointsInside;


    // ***************************************************************************************
    // Constructor

    @SuppressWarnings("unused")
    public ScCopier() {
        // Super
        super();

        // Init
        super.setDoubleBuffering(false);
        this.getPainter().setStyle(Paint.Style.FILL);

        this.mWidths = new float[]{0.0f};
        this.mWidthsMode = WidthsMode.SMOOTH;
        this.mAreaPath = new Path();
        this.mIsVisible = false;

        this.mGenericPoint = new float[2];
        this.mGenericRect = new RectF();
        this.mGenericCanvas = new Canvas();

        this.mNeedToGetPathInfo = true;
        this.mNeedToRedrawCover = true;
        this.mNeedToRedrawShader = true;

        // Painter
        this.mGenericPaint = new Paint();
        this.mGenericPaint.set(this.getPainter());
        this.mGenericPaint.setStyle(Paint.Style.FILL);
        this.mGenericPaint.setStrokeCap(Paint.Cap.SQUARE);
        this.mGenericPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        this.mGenericPaint.setStrokeWidth(2);
    }


    // ***************************************************************************************
    // Private methods

    /**
     * Check if the path is visible and must be draw
     * @return visibility
     */
    private boolean isVisible() {
        if (this.mWidths != null)
            for (float mWidth : this.mWidths)
                if (mWidth > 0.0f)
                    return true;

        // Not visible
        return false;
    }

    /**
     * Move a point considering an angle
     * @param point    the point to move
     * @param distance the distance
     * @param angle    the angle
     */
    private void movePoint(float[] point, float distance, float angle) {
        double radiant = Math.toRadians(angle);
        point[0] += distance * Math.cos(radiant);
        point[1] += distance * Math.sin(radiant);
    }

    /**
     * Fix the point position considering the position on path
     * @param point    the point to move
     * @param distance the distance
     * @param angle    the angle
     */
    private void fixPoint(float[] point, float distance, float angle) {
        // Adjust the y considering the point width
        float halfWidth = this.getWidth(distance) / 2;
        switch (this.getPosition()) {
            case INSIDE:
                movePoint(point, halfWidth, angle + 90);
                break;
            case OUTSIDE:
                movePoint(point, halfWidth, angle + 270);
                break;
        }
    }

    /**
     * Calculate all the points of the path.
     * NOTE than this use an approximation about the path points.
     */
    private void calculatePoints() {
        // Init
        float length = this.getMeasure().getLength();
        float[][] points = this.getMeasure().getApproximation();

        this.mPointsOutside = new float[points.length][2];
        this.mPointsInside = new float[points.length][2];

        // Cycle all points
        for (int index = 0; index < points.length; index ++) {
            // Holder
            float[] outside = this.mPointsOutside[index];
            float[] center = points[index];
            float[] inside = this.mPointsInside[index];

            // Check the position and get the point info
            float distance = index > length ? length: index;
            float angle = center[2];
            float halfWidth = this.getWidth(distance) / 2;

            // Fix the center
            this.fixPoint(center, distance, angle);

            // Other points
            this.clonePoint(center, outside);
            this.movePoint(outside, halfWidth, angle - 90);

            this.clonePoint(center, inside);
            this.movePoint(inside, halfWidth, angle + 90);
        }
    }

    /**
     * Get the border point
     * @param distance from the path start
     * @param isFirstOrLast if first point or the last
     * @param isReturn the direction
     * @return the point
     */
    private float[] getBorderPoint(float distance, boolean isFirstOrLast, boolean isReturn) {
        // If first of last point must take the current path point and not the rounded one
        if (isFirstOrLast) {
            // Holders
            float halfWidth = this.getWidth(distance) / 2;

            // Get the point on the path
            float angle = this.getPointAndAngle(distance, this.mGenericPoint);
            int multiplier = isReturn ? 1: -1;

            // Adjust the point
            this.movePoint(this.mGenericPoint, halfWidth, angle + (90 * multiplier));
            return this.mGenericPoint;

        } else
            // Take the point from the calculated points
            return isReturn ?
                    this.mPointsInside[(int) distance]:
                    this.mPointsOutside[(int) distance];
    }

    /**
     * Connection the path point with an arc.
     * @param source   the path
     * @param isReturn the direction
     */
    private void addArcToPath(Path source, boolean isReturn, float distance) {
        // Holders
        float multiplier = isReturn ? 1 : -1;
        float radius = this.getWidth(distance) / 2;

        // Get the center point and calc the rectangle area
        float angle = this.getPointAndAngle(distance, this.mGenericPoint);
        this.mGenericRect.set(
                this.mGenericPoint[0] - radius,
                this.mGenericPoint[1] - radius,
                this.mGenericPoint[0] + radius,
                this.mGenericPoint[1] + radius
        );

        // Draw the arc
        angle += 90 * multiplier;
        source.arcTo(this.mGenericRect, angle, 180);
    }

    /**
     * Draw the border line on the path
     * @param startFrom start distance
     * @param endTo end distance
     */
    private void drawBorderPath(float startFrom, float endTo, boolean isRounded) {
        // Holders
        boolean isReturn = startFrom > endTo;
        int fixedStart = (int)(isReturn ? Math.ceil(startFrom): Math.floor(startFrom));
        int fixedEnd = (int)(isReturn ? Math.floor(endTo): Math.ceil(endTo));

        float distance = fixedStart;
        int increment = isReturn ? -1: +1;

        // Cycle all points
        while ((!isReturn && distance <= fixedEnd) || (isReturn && distance >= fixedEnd)) {
            // Fix the distance
            boolean isFirst = isReturn ? distance >= startFrom: distance <= startFrom;
            boolean isLast = !isReturn ? distance >= endTo: distance <= endTo;

            float fixedDistance = distance;
            if (isFirst) fixedDistance = startFrom;
            if (isLast) fixedDistance = endTo;

            // Get the point
            float[] point = this.getBorderPoint(fixedDistance, isFirst || isLast, isReturn);

            // Add to path
            if (this.mAreaPath.isEmpty())
                this.mAreaPath.moveTo(point[0], point[1]);
            else
                this.mAreaPath.lineTo(point[0], point[1]);

            // Update the distance
            distance += increment;
        }

        // Close with an arc if is rounded
        if (isRounded)
            this.addArcToPath(this.mAreaPath, isReturn, endTo);
    }

    /**
     * Fill the path than will cover the shader bitmap
     */
    private void fillCoverPath() {
        // Find the effective limits
        float startFrom = this.getStartAtDistance();
        float endTo = this.getEndToDistance();

        // If rounded must fix the limits
        boolean isRounded = this.getPainter().getStrokeCap() == Paint.Cap.ROUND;
        if (isRounded) {
            // Get the start and end point dimension
            float startWidth = this.getWidth(startFrom) / 2;
            float endWidth = this.getWidth(endTo) / 2;

            // Fix it
            startFrom += startWidth;
            endTo -= endWidth;

            // The distance must be enough to draw something.
            if (startFrom > endTo)
                endTo = startFrom;
        }

        // Reset the old path
        this.mAreaPath.reset();

        // Create the path
        if (startFrom < endTo) {
            // Go and back
            this.drawBorderPath(startFrom, endTo, isRounded);
            this.drawBorderPath(endTo, startFrom, isRounded);

            // Close the path
            this.mAreaPath.close();
        }
    }

    /**
     * Create a colored bitmap following the path.
     * @param canvasWidth  the width
     * @param canvasHeight the height
     * @return the bitmap
     */
    private Bitmap createBitmap(int canvasWidth, int canvasHeight) {
        // Holders
        float[][] points = this.getMeasure().getApproximation();
        float length = points.length;

        // If the path is closed could happen than the first point is equal to the last.
        // Need to avoid this situation.
        if (this.getMeasure().isClosed())
            length -= 1;

        // Create the bitmap using the path boundaries and retrieve the canvas where draw
        Bitmap bitmap = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888);
        this.mGenericCanvas.setBitmap(bitmap);

        // Cycle all points of the path
        for (int index = 0; index < length; index++) {
            // Set the current painter color
            int color = this.getGradientColor(index);
            this.mGenericPaint.setColor(color);

            // Draw a line between the points
            this.mGenericCanvas.drawLine(
                    this.mPointsInside[index][0], this.mPointsInside[index][1],
                    this.mPointsOutside[index][0], this.mPointsOutside[index][1],
                    this.mGenericPaint
            );
        }

        // Return the new bitmap
        return bitmap;
    }

    /**
     * Create the shader to apply at path.
     * @param canvasWidth  the width
     * @param canvasHeight the height
     */
    private BitmapShader createShader(int canvasWidth, int canvasHeight) {
        // Check for empty values
        if (canvasWidth > 0 && canvasHeight > 0) {
            // Create the shader and recycle the bitmap
            Bitmap bitmap = this.createBitmap(canvasWidth, canvasHeight);
            return new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        }
        // Else
        return null;
    }

    /**
     * Draw a copy of the source path on the canvas.
     * @param canvas the destination canvas
     */
    @SuppressWarnings({"unused"})
    private void internalDraw(Canvas canvas, ContourInfo info) {
        // Check for visibility
        if (!this.mIsVisible)
            return ;

        // Need to get path information
        if (this.mNeedToGetPathInfo) {
            this.mNeedToGetPathInfo = false;
            this.calculatePoints();
        }

        // Check for re-path
        if (this.mNeedToRedrawCover) {
            // Calculate all the path points and create the path
            this.mNeedToRedrawCover = false;
            this.fillCoverPath();
        }

        // Check if needs to redraw the bitmap
        Paint painter = this.getPainter();
        Paint clone;

        // Before Lollipop version must assigned the original paint
        // to a clone due have displaying issue.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            clone = new Paint(painter);
        else
            clone = painter;

        // Check if needs to redraw the shader
        if (this.mNeedToRedrawShader) {
            // Trigger and shader
            this.mNeedToRedrawShader = false;

            // Create the shader and apply to the painter
            this.mShader = this.createShader(canvas.getWidth(), canvas.getHeight());
        }

        if (this.mShader != null)
            clone.setShader(this.mShader);

        // Draw the masked path
        canvas.drawPath(this.mAreaPath, clone);
    }


    // ***************************************************************************************
    // Public

    /**
     * Get the current width dependently from the distance from the starting of path and the
     * widths array. If the widths are not defined will be returned the current width of painter.
     * @param distance from the path start
     * @param length force the length of the path
     * @return the width
     */
    @SuppressWarnings({"unused"})
    public float getWidth(float distance, float length) {
        return this.getValue(
                this.mWidths,
                distance / length,
                this.mWidthsMode == WidthsMode.SMOOTH,
                0.0f
        );
    }

    /**
     * Get the current width dependently from the distance from the starting of path,
     * the colors array and the mode to draw. If the width are not defined will be returned
     * the current width of painter.
     * @param distance from the path start
     * @return the color
     */
    @SuppressWarnings({"unused"})
    public float getWidth(float distance) {
        return this.getWidth(distance, this.getMeasure().getLength());
    }


    // ***************************************************************************************
    // Public properties

    /**
     * Set the current stroke widths
     * @param values the new stroke widths
     */
    @SuppressWarnings({"unused"})
    public void setWidths(float... values) {
        if (!Arrays.equals(this.mWidths, values)) {
            this.mWidths = values;
            this.onPropertyChange("widths", values);
        }
    }

    /**
     * Get the current stroke widths
     * @return the current stroke widths
     */
    @SuppressWarnings({"unused"})
    public float[] getWidths() {
        return this.mWidths;
    }


    /**
     * Set the widths filling mode.
     * You can have two way for manage the width of the path: SMOOTH or ROUGH.
     * @param value the new width filling mode
     */
    @SuppressWarnings({"unused"})
    public void setWidthsMode(WidthsMode value) {
        if (this.mWidthsMode != value) {
            this.mWidthsMode = value;
            this.onPropertyChange("widthsMode", value);
        }
    }

    /**
     * Get the widths filling mode.
     * @return the width filling mode
     */
    @SuppressWarnings("unused")
    public WidthsMode getWidthsMode() {
        return this.mWidthsMode;
    }


    // ***************************************************************************************
    // Overrides

    /**
     * Find the point and the angle and adjust it to be at center of the line considering
     * the line position respect the origin.
     * @param distance the point distance from path start
     * @param point    the array where will save the point coordinates
     * @return the angle
     */
    @Override
    public float getPointAndAngle(float distance, float[] point) {
        // Adjust the point considering the point width and position on path
        float angle = super.getPointAndAngle(distance, point);
        this.fixPoint(point, distance, angle);

        // Return the angle
        return angle;
    }

    /**
     * Find the point and adjust it to be at center of the line considering
     * the line position respect the origin.
     * @param distance the point distance from path start
     * @param point    the array where will save the point coordinates
     */
    @Override
    public void getPoint(float distance, float[] point) {
        this.getPointAndAngle(distance, point);
    }

    /**
     * The draw method to override in the inherited classes.
     * @param canvas where draw
     * @param info   the contour info
     */
    @Override
    protected void onDraw(Canvas canvas, ContourInfo info) {
        this.internalDraw(canvas, info);
    }

    /**
     * Implement a copy of this object
     * @param destination the destination object
     * @hide
     */
    @SuppressWarnings("unused")
    public void copy(ScCopier destination) {
        // Super
        super.copy(destination);

        // This object
        if (this.mWidths != null)
            destination.setWidths(this.mWidths.clone());

        destination.setWidthsMode(this.mWidthsMode);
    }

    /**
     * For every changes force to redraw the bitmap.
     * If no have changes the class will use the last bitmap calculated.
     * @param name  the property name
     * @param value the property value
     * @hide
     */
    @Override
    protected void onPropertyChange(String name, Object value) {
        // Consider to redraw the cover
        String[] coverProps = new String[] {
                "paint", "position", "considerContours",
                "widths", "widthsMode",
                "startAt", "endTo"
        };
        if (Arrays.asList(coverProps).contains(name))
            this.mNeedToRedrawCover = true;

        // Consider to redraw the shader
        String[] shaderProps = new String[] {
                "paint", "position", "considerContours",
                "colors", "colorsMode",
                "widths", "widthsMode"
        };
        if (Arrays.asList(shaderProps).contains(name))
            this.mNeedToRedrawShader = true;

        // Check the visibility
        if ("widths".equals(name))
            this.mIsVisible = this.isVisible();

        // Super
        super.onPropertyChange(name, value);
    }

    /**
     * Refresh the feature measure.
     */
    @Override
    @SuppressWarnings("unused")
    public void refresh() {
        this.mNeedToGetPathInfo = true;
        this.mNeedToRedrawShader = true;
        this.mNeedToRedrawCover = true;
        super.refresh();
    }

    /**
     * Disable this method
     * @param value the status
     */
    @Override
    public void setDoubleBuffering(boolean value) {
        // Do nothing
    }

}
