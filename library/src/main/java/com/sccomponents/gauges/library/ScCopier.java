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
 * @version 3.0.0
 * @since 2016-05-26
 */
public class ScCopier extends ScFeature {

    // ***************************************************************************************
    // Private and protected variables

    private Path mAreaPath;
    private Paint mGenericPaint;
    private Canvas mGenericCanvas;

    private float[] mFirstPoint;
    private float[] mSecondPoint;
    private RectF mRectangle;
    private BitmapShader mShader;


    // ***************************************************************************************
    // Constructor

    @SuppressWarnings("unused")
    public ScCopier() {
        // Super
        super();

        // Init
        this.getPainter().setStyle(Paint.Style.FILL);

        this.mAreaPath = new Path();
        this.mGenericCanvas = new Canvas();

        this.mFirstPoint = new float[2];
        this.mSecondPoint = new float[2];
        this.mRectangle = new RectF();

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
     * Get the point on the path given the distance from the start and apply the offset
     * by the line position respect to the path.
     */
    private void getFixedPoint(float distance, float[] point, boolean isReturn) {
        // Holders
        float halfWidth = this.getWidth(distance) / 2;
        int multiplier = isReturn ? 1: -1;

        // Get the center and calc the rectangle area
        float angle = this.getPointAndAngle(distance, point);
        this.movePoint(point, halfWidth, angle + 90 * multiplier);
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
     * Connection the path point with an arc.
     * @param source   the path
     * @param isReturn the direction
     */
    private void addArcToPath(Path source, boolean isReturn,
                              float startDistance, float endDistance) {
        // Holders
        float distance = isReturn ? startDistance : endDistance;
        float multiplier = isReturn ? 1 : -1;
        float radius = this.getWidth(distance) / 2;

        // Get the center point and calc the rectangle area
        float angle = this.getPointAndAngle(distance, this.mFirstPoint);
        this.mRectangle.set(
                this.mFirstPoint[0] - radius,
                this.mFirstPoint[1] - radius,
                this.mFirstPoint[0] + radius,
                this.mFirstPoint[1] + radius
        );

        // Draw the arc
        angle += 90 * multiplier;
        source.arcTo(this.mRectangle, angle, 180);
    }

    /**
     * Clone a path creating a series of point and translate the path by the related point width.
     * The method consider the direction to calculate the offset.
     * @param path      the destination path
     * @param startFrom start distance
     * @param endTo     end distance
     */
    private void cloneSourcePath(Path path, float startFrom, float endTo) {
        // Holders
        boolean isReturn = startFrom > endTo;

        // Find the limits
        float fixedStart = startFrom < endTo ? startFrom : endTo;
        float fixedEnd = endTo > startFrom ? endTo : startFrom;

        // If rounded must fix the limits
        boolean isRounded = this.getPainter().getStrokeCap() == Paint.Cap.ROUND;
        if (isRounded) {
            // Get the start and end point dimension
            float startWidth = this.getWidth(fixedStart) / 2;
            float endWidth = this.getWidth(fixedEnd) / 2;

            // Fix it
            fixedStart += startWidth;
            fixedEnd -= endWidth;

            // The distance must be enough to draw something.
            if (fixedStart > fixedEnd)
                fixedEnd = fixedStart;
        }

        // Clone the source path adjusting the y position
        for (float distance = fixedStart; distance < fixedEnd; distance++) {
            // Fix the distance as the return way check is different and
            // calculate the right point position on the path.
            float fixedDistance = isReturn ? startFrom - distance : distance;
            this.getFixedPoint(fixedDistance, this.mFirstPoint, isReturn);

            // Check if empty
            if (path.isEmpty())
                // Just move the pointer on the first point
                path.moveTo(this.mFirstPoint[0], this.mFirstPoint[1]);
            else
                // Draw a line
                path.lineTo(this.mFirstPoint[0], this.mFirstPoint[1]);
        }

        // Conjunction with arc
        if (isRounded) {
            // Draw the arc
            this.addArcToPath(path, isReturn, fixedStart, fixedEnd);
        }
    }

    /**
     * Create a cloned path to cover the drawing area.
     * @return the area
     */
    private Path coverDrawingArea() {
        // Find the effective limits
        float startFrom = this.getStartAtDistance();
        float endTo = this.getEndToDistance();

        // Reset the old path
        this.mAreaPath.reset();

        // Create the path area
        if (startFrom != endTo) {
            this.cloneSourcePath(this.mAreaPath, startFrom, endTo);
            this.cloneSourcePath(this.mAreaPath, endTo, startFrom);
        }

        // Return the path
        this.mAreaPath.close();
        return this.mAreaPath;
    }

    /**
     * Create a colored bitmap following the path.
     * @param canvasWidth  the width
     * @param canvasHeight the height
     * @return the bitmap
     */
    private Bitmap createBitmap(int canvasWidth, int canvasHeight) {
        // Create the bitmap using the path boundaries and retrieve the canvas where draw
        Bitmap bitmap = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = this.mGenericCanvas;
        canvas.setBitmap(bitmap);

        // Cycle all points of the path
        float length = this.getMeasure().getLength();
        for (float distance = 0; distance <= length; distance++) {
            // Get the point and the angle
            float angle = this.getPointAndAngle(distance, this.mFirstPoint);
            float halfWidth = this.getWidth(distance) / 2;

            // Set the current painter color
            int color = this.getGradientColor(distance);
            this.mGenericPaint.setColor(color);

            // Adjust the x position
            this.clonePoint(this.mFirstPoint, this.mSecondPoint);
            this.movePoint(this.mSecondPoint, halfWidth, angle - 90);
            this.movePoint(this.mFirstPoint, halfWidth, angle + 90);

            // Draw a line between the points
            canvas.drawLine(
                    this.mFirstPoint[0], this.mFirstPoint[1],
                    this.mSecondPoint[0], this.mSecondPoint[1],
                    this.mGenericPaint
            );
        }

        // Return the new bitmap
        return bitmap;
    }

    /**
     * Draw a copy of the source path on the canvas.
     * @param canvas the destination canvas
     */
    @SuppressWarnings("all")
    private void drawCopy(Canvas canvas, ContourInfo info) {
        // Check if needs to redraw the bitmap
        if (this.mShader == null) {
            Bitmap bitmap = this.createBitmap(canvas.getWidth(), canvas.getHeight());
            this.mShader = new BitmapShader(
                    bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        }

        // Create a paint clone and set the shader
        Paint clone = new Paint(this.getPainter());
        clone.setShader(this.mShader);

        // Check the area path
        if (this.mAreaPath.isEmpty() || this.getConsiderContours())
            this.mAreaPath = this.coverDrawingArea();

        // Draw
        canvas.drawPath(this.mAreaPath, clone);
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
        // Base method
        float angle = super.getPointAndAngle(distance, point);

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
     * Set the path
     * @param value the painter
     */
    @Override
    public void setPath(Path value) {
        super.setPath(value);
        this.onPropertyChange("path", value);
    }

    /**
     * The draw method to override in the inherited classes.
     * @param canvas where draw
     * @param info   the contour info
     */
    @Override
    protected void onDraw(Canvas canvas, ContourInfo info) {
        this.drawCopy(canvas, info);
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
        // Consider to redraw the shader
        String[] properties = new String[] {
                "path", "paint", "colors", "colorsMode", "considerContours",
                "position", "widths", "widthsMode"
        };
        boolean contains = Arrays.asList(properties).contains(name);
        if (contains)
            this.mShader = null;

        // Rebuild the path
        if (this.mAreaPath != null)
            this.mAreaPath.reset();

        // Super
        super.onPropertyChange(name, value);
    }

    /**
     * Refresh the feature measure.
     */
    @Override
    @SuppressWarnings("unused")
    public void refresh() {
        if (this.mAreaPath != null) this.mAreaPath.reset();
        this.mShader = null;
        super.refresh();
    }

}
