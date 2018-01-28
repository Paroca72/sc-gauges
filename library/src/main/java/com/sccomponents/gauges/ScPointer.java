package com.sccomponents.gauges;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * Draw a pointer on the path at certain distance from the path start.

 * @author Samuele Carassai
 * @version 3.0.0
 * @since 2016-05-26
 */
public class ScPointer extends ScFeature {

    // ***************************************************************************************
    // Constants

    private static final float DEFAULT_HALO_WIDTH = 10.0f;
    private static final int DEFAULT_HALO_ALPHA = 128;


    /****************************************************************************************
     * Private variables
     */

    private float mPointerRadius;
    private float mPointerPosition;
    private boolean mPressed;
    private Bitmap mBitmap;

    private float mHaloWidth;
    private int mHaloAlpha;
    private Paint mHaloPaint;

    private float[] mGenericPoint;
    private ScPointer.DrawingInfo mGenericInfo;
    private OnCustomDrawListener mOnCustomDrawListener;


    /****************************************************************************************
     * Constructor
     */

    @SuppressWarnings("unused")
    public ScPointer(Path path) {
        // Super
        super(path);

        // Init
        this.setTransformCanvas(false);

        this.mPointerRadius = 0.0f;
        this.mHaloWidth = ScPointer.DEFAULT_HALO_WIDTH;
        this.mHaloAlpha = ScPointer.DEFAULT_HALO_ALPHA;

        this.mGenericPoint = new float[2];
        this.mGenericInfo = new ScPointer.DrawingInfo();

        // Painters
        Paint paint = this.getPainter();
        paint.setStrokeWidth(1.0f);
        paint.setStyle(Paint.Style.FILL);

        this.mHaloPaint = new Paint();
    }


    // ***************************************************************************************
    // Draw methods

    /**
     * Default drawing the circles that representing the pointer.
     * Note when draw a circle the angle not take effect on the final drawing.
     * @param canvas where to draw
     * @param info   the pointer info
     */
    private void drawCircles(Canvas canvas, ScPointer.DrawingInfo info) {
        // Set the halo painter
        Paint painter = this.getPainter();
        this.mHaloPaint.set(painter);
        this.mHaloPaint.setAlpha(info.pressed ? 255 : this.mHaloAlpha);
        this.mHaloPaint.setStyle(Paint.Style.STROKE);
        this.mHaloPaint.setStrokeWidth(this.mHaloWidth);

        // Check the pointer radius
        if (this.mPointerRadius > 0) {
            // Draw the halo and the pointer
            canvas.drawCircle(
                    this.mGenericPoint[0], this.mGenericPoint[1],
                    this.mPointerRadius, this.mHaloPaint);
            canvas.drawCircle(
                    this.mGenericPoint[0], this.mGenericPoint[1],
                    this.mPointerRadius, painter);
        }
    }

    /**
     * Draw on canvas a bitmap centered in the passed point.
     * @param canvas where to draw
     * @param info   the pointer info
     */
    private void drawBitmap(Canvas canvas, ScPointer.DrawingInfo info) {
        // Apply the transformation as the default is off
        canvas.save();
        canvas.rotate(info.angle, this.mGenericPoint[0], this.mGenericPoint[1]);
        canvas.translate(info.offsetX, info.offsetY);
        canvas.scale(info.scaleX, info.scaleY);

        // Print the bitmap centered respect the point
        canvas.drawBitmap(
                info.bitmap,
                this.mGenericPoint[0] - info.bitmap.getWidth() / 2,
                this.mGenericPoint[1] - info.bitmap.getHeight() / 2,
                null
        );
        canvas.restore();
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
    protected ScPointer.DrawingInfo setDrawingInfo(int contour) {
        // Reset and fill with the base values
        this.mGenericInfo.reset(this, contour);

        // Fill the missing data
        this.mGenericInfo.angle = this.getAngle(this.getPointer());
        this.mGenericInfo.bitmap = this.mBitmap;
        this.mGenericInfo.pressed = this.mPressed;

        // Return
        return this.mGenericInfo;
    }

    /**
     * Draw the pointer on the canvas.
     * @param canvas the canvas where draw
     * @hide
     */
    @Override
    protected void onDraw(Canvas canvas, ScFeature.DrawingInfo info) {
        // Set the pointer painter
        ScPointer.DrawingInfo pointerInfo = (ScPointer.DrawingInfo) info;
        this.getPainter()
                .setAlpha(pointerInfo.pressed ? this.mHaloAlpha : 255);

        // Custom draw
        if (this.mOnCustomDrawListener != null) {
            this.mOnCustomDrawListener.onCustomDraw(canvas, pointerInfo);
            return;
        }

        // Find the point on the path
        float distance = this.getDistance(this.mPointerPosition);
        this.getPoint(distance, this.mGenericPoint);

        // Check if the bitmap is not null
        if (pointerInfo.bitmap != null)
            // Draw a bitmap
            this.drawBitmap(canvas, pointerInfo);
        else
            // Draw the circles
            this.drawCircles(canvas, pointerInfo);
    }


    // ***************************************************************************************
    // Public methods

    /**
     * Implement a copy of this object
     * @param destination the destination object
     */
    @SuppressWarnings("unused")
    public void copy(ScPointer destination) {
        // Super
        super.copy(destination);

        // Set
        destination.setRadius(this.mPointerRadius);
        destination.setPointer(this.mPointerPosition);
        destination.setPressed(this.mPressed);
        destination.setBitmap(this.mBitmap);

        destination.setHaloAlpha(this.mHaloAlpha);
        destination.setHaloWidth(this.mHaloWidth);
    }

    /**
     * Implement a copy of this object
     * @param destination the destination object
     * @hide
     */
    @SuppressWarnings("unused")
    @Override
    public void copy(ScFeature destination) {
        if (destination instanceof ScPointer)
            this.copy((ScPointer) destination);
        else
            super.copy(destination);
    }


    // ***************************************************************************************
    // Public properties

    /**
     * Set the position of pointer in percentage respect to the path length.
     * @param value the position in percentage
     */
    @SuppressWarnings("unused")
    public void setPointer(float value) {
        // Check the limits
        if (value < 0.0f) value = 0.0f;
        if (value > 100.0f) value = 100.0f;

        // Store the value
        if (this.mPointerPosition != value) {
            this.mPointerPosition = value;
            this.onPropertyChange("pointer", value);
        }
    }

    /**
     * Get the position of pointer in percentage respect to the path length.
     * @return the position in percentage
     */
    @SuppressWarnings("unused")
    public float getPointer() {
        return this.mPointerPosition;
    }


    /**
     * Set the pointer radius in pixel.
     * @param value the radius
     */
    @SuppressWarnings("unused")
    public void setRadius(float value) {
        value = value < 0.0f ? 0.0f : value;
        if (this.mPointerRadius != value) {
            this.mPointerRadius = value;
            this.onPropertyChange("radius", value);
        }
    }

    /**
     * Get the pointer radius in pixel.
     * @return the radius
     */
    @SuppressWarnings("unused")
    public float getRadius() {
        return this.mPointerRadius;
    }


    /**
     * Set the halo width in pixel
     * @param value the width
     */
    @SuppressWarnings("unused")
    public void setHaloWidth(float value) {
        value = value < 0.0f ? 0.0f : value;
        if (this.mHaloWidth != value) {
            this.mHaloWidth = value;
            this.onPropertyChange("haloWidth", value);
        }
    }

    /**
     * Get the halo width in pixel
     * @return the width
     */
    @SuppressWarnings("unused")
    public float getHaloWidth() {
        return this.mHaloWidth;
    }


    /**
     * Set the halo alpha
     * @param value the new alpha value
     */
    @SuppressWarnings("unused")
    public void setHaloAlpha(int value) {
        // Check the limits
        if (value < 0) value = 0;
        if (value > 255) value = 255;

        // Store the value
        if (this.mHaloAlpha != value) {
            this.mHaloAlpha = value;
            this.onPropertyChange("haloAlpha", value);
        }
    }

    /**
     * Get the halo alpha
     * @return the alpha
     */
    @SuppressWarnings("unused")
    public int getHaloAlpha() {
        return this.mHaloAlpha;
    }


    /**
     * Set the pointer status.
     * @param value the new status
     */
    @SuppressWarnings("unused")
    public void setPressed(boolean value) {
        if (this.mPressed != value) {
            this.mPressed = value;
            this.onPropertyChange("pressed", value);
        }
    }

    /**
     * Get the pointer status
     * @return the current status
     */
    @SuppressWarnings("unused")
    public boolean getPressed() {
        return this.mPressed;
    }


    /**
     * Set the current bitmap.
     * @param value the new bitmap
     */
    @SuppressWarnings("unused")
    public void setBitmap(Bitmap value) {
        this.mBitmap = value;
        this.onPropertyChange("bitmap", value);
    }

    /**
     * Get the current bitmap
     * @return the current bitmap
     */
    @SuppressWarnings("unused")
    public Bitmap getBitmap() {
        return this.mBitmap;
    }



    // ***************************************************************************************
    // Public classes and methods

    /**
     * This is a structure to hold the feature information before draw it
     */
    @SuppressWarnings("unused")
    public class DrawingInfo extends ScFeature.DrawingInfo {

        public Bitmap bitmap;
        public boolean pressed;

    }


    // ***************************************************************************************
    // Listeners and Interfaces

    /**
     * Define the draw listener interface
     */
    @SuppressWarnings("unused")
    public interface OnCustomDrawListener {

        /**
         * Called before draw the path copy.
         * @param info the copier info
         */
        void onCustomDraw(Canvas canvas, DrawingInfo info);

    }

    /**
     * Set the draw listener to call.
     * @param listener the linked method to call
     */
    @SuppressWarnings("unused")
    public void setOnDrawListener(OnCustomDrawListener listener) {
        this.mOnCustomDrawListener = listener;
    }

}
