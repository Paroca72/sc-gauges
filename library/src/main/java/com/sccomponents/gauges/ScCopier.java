package com.sccomponents.gauges;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

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

    private Bitmap mBitmap;

    private Paint mGenericPaint;
    private Canvas mGenericCanvas;
    private float[] mGenericPoint;
    private ScCopier.DrawingInfo mGenericInfo;


    // ***************************************************************************************
    // Constructor

    @SuppressWarnings("unused")
    public ScCopier(Path path) {
        // Super
        super(path);

        // Init
        this.mBitmap = null;

        this.mGenericPaint = new Paint();
        this.mGenericCanvas = new Canvas();
        this.mGenericPoint = new float[2];
        this.mGenericInfo = new ScCopier.DrawingInfo();
    }


    // ***************************************************************************************
    // Private methods

    /**
     * Retrieve the first width.
     * If no have widths return the painter stroke width.
     * @return the first width
     */
    private float getFirstWidth() {
        // If empty return the painter stroke width
        if (this.getWidths() == null || this.getWidths().length == 0)
            return this.getPainter().getStrokeWidth();

        // Else find the max
        return this.getWidths()[0];
    }

    /**
     * Retrieve the last width.
     * If no have widths return the painter stroke width.
     * @return the first width
     */
    private float getLastWidth() {
        // If empty return the painter stroke width
        if (this.getWidths() == null || this.getWidths().length == 0)
            return this.getPainter().getStrokeWidth();

        // Else find the max
        return this.getWidths()[this.getWidths().length - 1];
    }

    /**
     * Create a colored bitmap following the path.
     * @param canvasWidth   the width
     * @param canvasHeight  the height
     * @return              the bitmap
     */
    private Bitmap createBitmap(int canvasWidth, int canvasHeight) {
        // Create the bitmap using the path boundaries and retrieve the canvas where draw
        Bitmap bitmap = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = this.mGenericCanvas;
        canvas.setBitmap(bitmap);

        // Set a clone of the original painter
        Paint painter = this.mGenericPaint;
        painter.set(this.getPainter());

        // Fix the start and end
        float startFrom = this.getStartAtDistance();
        float endTo = this.getEndToDistance();

        if (painter.getStrokeCap() == Paint.Cap.BUTT || this.getMeasure().isClosed()) {
            startFrom += this.getFirstWidth();
            endTo -= this.getLastWidth();
        }

        // Cycle all points of the path
        for (float distance = startFrom; distance < endTo; distance++) {
            // Get and check the width for empty values
            float width = this.getWidth(distance);
            if (width <= 0) continue;

            // Get the point and the angle
            float angle = this.getPointAndAngle(distance, this.mGenericPoint);
            int color = this.getGradientColor(distance);

            // Set the current painter
            painter.setColor(color);
            painter.setStrokeWidth(width);

            // Adjust the point
            float x = this.mGenericPoint[0];
            float y = this.mGenericPoint[1];

            float adjustY = y;
            switch (this.getPosition()) {
                case INSIDE: adjustY += width / 2; break;
                case OUTSIDE: adjustY -= width / 2; break;
            }

            // If the round stroke is not settled the point have a square shape.
            // This can create a visual issue when the path follow a curve.
            // To avoid this issue the point (square) will be rotate of the tangent angle
            // before to write it on the canvas.
            canvas.save();
            canvas.rotate(angle, x, y);
            canvas.drawPoint(x, adjustY, painter);
            canvas.restore();
        }

        // Return the new bitmap
        return bitmap;
    }

    /**
     * Draw a copy of the source path on the canvas.
     * @param canvas the destination canvas
     */
    private void drawCopy(Canvas canvas, DrawingInfo info) {
        // Check if needs to redraw the bitmap
        if (this.mBitmap == null)
            this.mBitmap = this.createBitmap(canvas.getWidth(), canvas.getHeight());

        // Draw the bitmap on the canvas
        canvas.drawBitmap(this.mBitmap, 0, 0, this.getPainter());
    }


    // ***************************************************************************************
    // Overrides

    /**
     * Prepare the info object to send before drawing.
     * Need to override this method if you want have a custom info.
     * @param contour   the current contour
     * @return          the drawing info
     * @hide
     */
    @Override
    protected ScCopier.DrawingInfo setDrawingInfo(int contour) {
        // Reset and fill with the base values
        this.mGenericInfo.reset(this, contour);
        this.mGenericInfo.source = this;

        // Return
        return this.mGenericInfo;
    }

    /**
     * Draw method
     * @param canvas where draw
     * @hide
     */
    @Override
    protected void onDraw(Canvas canvas, ScFeature.DrawingInfo info) {
        // Draw a copy
        this.drawCopy(canvas, (ScCopier.DrawingInfo) info);
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
     * @param name      the property name
     * @param value     the property value
     * @hide
     */
    @Override
    protected void onPropertyChange(String name, Object value) {
        this.mBitmap = null;
        super.onPropertyChange(name, value);
    }



    // ***************************************************************************************
    // Public classes and methods

    /**
     * This is a structure to hold the feature information before draw it
     */
    @SuppressWarnings("unused")
    public class DrawingInfo extends ScFeature.DrawingInfo {

        public ScCopier source = null;

    }

}
