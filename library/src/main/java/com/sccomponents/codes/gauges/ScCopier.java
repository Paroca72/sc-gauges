package com.sccomponents.codes.gauges;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;

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

    private BitmapShader mShader;
    private Path mAreaPath;

    private Paint mGenericPaint;
    private Canvas mGenericCanvas;

    private float[] mFirstPoint;
    private float[] mSecondPoint;
    private float[] mThirdPoint;


    // ***************************************************************************************
    // Constructor

    @SuppressWarnings("unused")
    public ScCopier(Path path) {
        // Super
        super(path);

        // Init
        this.setEdges(this.getMeasure().isClosed() ? Positions.INSIDE: Positions.OUTSIDE);
        this.getPainter().setStyle(Paint.Style.FILL);

        this.mShader = null;
        this.mAreaPath = new Path();

        this.mGenericPaint = new Paint();
        this.mGenericCanvas = new Canvas();

        this.mFirstPoint = new float[2];
        this.mSecondPoint = new float[2];
        this.mThirdPoint = new float[2];
    }


    // ***************************************************************************************
    // Private methods

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
        float angle = this.getAngle(distance) + (isReturn ? 180 : 0);
        float halfWidth = this.getWidth(distance) / 2;

        // Get the points
        this.calcPoint(this.mFirstPoint, distance, isReturn);
        this.calcPoint(this.mSecondPoint, distance, !isReturn);
        this.clonePoint(mSecondPoint, this.mThirdPoint);

        this.movePoint(this.mFirstPoint, halfWidth, angle);
        this.movePoint(this.mSecondPoint, halfWidth, angle);

        // Draw
        source.quadTo(
                this.mFirstPoint[0],
                this.mFirstPoint[1],
                (this.mSecondPoint[0] + this.mFirstPoint[0]) / 2,
                (this.mSecondPoint[1] + this.mFirstPoint[1]) / 2
        );
        source.quadTo(
                this.mSecondPoint[0], this.mSecondPoint[1],
                this.mThirdPoint[0], this.mThirdPoint[1]
        );
    }

    /**
     * Calculate the point on path considering the direction and the position respect the path.
     * @param distance from path start
     * @param isReturn the direction
     */
    private void calcPoint(float[] point, float distance, boolean isReturn) {
        // Adjust the point
        float halfWidth = this.getWidth(distance) / 2;
        float toMove = 0.0f;

        switch (this.getPosition()) {
            case INSIDE:
                toMove = (isReturn ? 2 : 0) * halfWidth;
                break;
            case MIDDLE:
                toMove = (isReturn ? -1 : 1) * halfWidth;
                break;
            case OUTSIDE:
                toMove = -(isReturn ? 2 : 0) * halfWidth;
                break;
        }

        float angle = this.getPointAndAngle(distance, point);
        this.movePoint(point, toMove, angle + 90);
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

        // Adjust the limits
        switch (this.getEdges()) {
            case INSIDE:
                fixedStart += this.getWidth(fixedStart) / 2;
                fixedEnd -= this.getWidth(fixedEnd) / 2;
                break;
            case MIDDLE:
                fixedStart += this.getWidth(fixedStart) / 4;
                fixedEnd -= this.getWidth(fixedEnd) / 4;
        }

        // Clone the source path adjusting the y position
        for (float distance = fixedStart; distance < fixedEnd; distance++) {
            // Fix the distance as the return way check is different and
            // calculate the right point position on the path.
            float fixedDistance = isReturn ? startFrom - distance : distance;
            this.calcPoint(this.mFirstPoint, fixedDistance, isReturn);

            // Check if empty
            if (path.isEmpty())
                // Just move the pointer on the first point
                path.moveTo(this.mFirstPoint[0], this.mFirstPoint[1]);
            else
                // Draw a line
                path.lineTo(this.mFirstPoint[0], this.mFirstPoint[1]);
        }

        // Conjunction with arc
        boolean isRounded = this.getPainter().getStrokeCap() == Paint.Cap.ROUND;
        if (isRounded)
            this.addArcToPath(path, isReturn, fixedStart, fixedEnd);
    }

    /**
     * Create a cloned path to cover the drawing area.
     * @return the area
     */
    private Path coverDrawingArea() {
        // Holders
        this.mAreaPath.reset();

        // Get the start and end
        float startFrom = this.getStartAtDistance();
        float endTo = this.getEndToDistance();

        // Create the path area
        if (startFrom != endTo) {
            this.cloneSourcePath(this.mAreaPath, startFrom, endTo);
            this.cloneSourcePath(this.mAreaPath, endTo, startFrom);
        }

        // Return the path
        return mAreaPath;
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

        // Set a clone of the original painter
        Paint painter = this.mGenericPaint;
        painter.set(this.getPainter());
        painter.setStrokeCap(Paint.Cap.SQUARE);

        // Fix the start and end
        float length = this.getMeasure().getLength();

        // Cycle all points of the path
        for (float distance = 0; distance < length; distance++) {
            // Get and check the width for empty values
            float width = this.getWidth(distance);
            if (width <= 0)
                continue;

            // Get the point and the angle
            float angle = this.getPointAndAngle(distance, this.mFirstPoint);
            int color = this.getGradientColor(distance);

            // Set the current painter
            painter.setColor(color);
            painter.setStrokeWidth(width);

            // Adjust the point
            float halfWidth = width / 2;
            float adjustX = this.mFirstPoint[0] + halfWidth;
            float adjustY = this.mFirstPoint[1];

            // Adjust the y
            switch (this.getPosition()) {
                case INSIDE:
                    adjustY = this.mFirstPoint[1] + halfWidth;
                    break;
                case OUTSIDE:
                    adjustY = this.mFirstPoint[1] - halfWidth;
                    break;
            }

            // If the round stroke is not settled the point have a square shape.
            // This can create a visual issue when the path follow a curve.
            // To avoid this issue the point (square) will be rotate of the tangent angle
            // before to write it on the canvas.
            canvas.save();
            canvas.rotate(angle, this.mFirstPoint[0], this.mFirstPoint[1]);

            // if is first
            if (distance == 0)
                // Draw a point in front of the path
                canvas.drawPoint(adjustX - halfWidth * 2, adjustY, painter);

            // Draw the common point
            canvas.drawPoint(adjustX, adjustY, painter);

            // Restore the canvas status as previous
            canvas.restore();
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
        if (this.mShader == null || this.getConsiderContours()) {
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
        this.mShader = null;
        if (this.mAreaPath != null) this.mAreaPath.reset();
        super.onPropertyChange(name, value);
    }

}
