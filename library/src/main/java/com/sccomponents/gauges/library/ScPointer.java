package com.sccomponents.gauges.library;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Draw a pointer on the path at certain distance from the path start.

 * @author Samuele Carassai
 * @version 3.5.0
 * @since 2016-05-26
 */
public class ScPointer extends ScNotches {

    // ***************************************************************************************
    // Constants

    private static final float DEFAULT_HALO_WIDTH = 10.0f;
    private static final int DEFAULT_HALO_ALPHA = 128;


    /****************************************************************************************
     * Private variables
     */

    private float mDistance;
    private boolean mPressed;

    private float mHaloWidth;
    private int mHaloAlpha;
    private Paint mHaloPaint;
    private PointerInfo mRepetitionInfo;


    /****************************************************************************************
     * Constructor
     */

    @SuppressWarnings("unused")
    public ScPointer() {
        // Super
        super();

        // Init
        super.setRepetitions(1);
        super.setLastRepetitionOnPathEnd(false);
        super.setType(NotchTypes.OVAL_FILLED);
        super.setDoubleBuffering(false);

        this.mHaloWidth = ScPointer.DEFAULT_HALO_WIDTH;
        this.mHaloAlpha = ScPointer.DEFAULT_HALO_ALPHA;
        this.mHaloPaint = new Paint();
        this.mHaloPaint.setStyle(Paint.Style.STROKE);
        this.mRepetitionInfo = new PointerInfo();
    }


    // ***************************************************************************************
    // Overrides

    /**
     * Get the current repetition drawing info.
     * This methods must be overridden for create custom drawing info for inherited
     * classes.
     * @param repetition    the repetition index
     * @return              the repetition drawing info
     */
    @SuppressWarnings("unused")
    @Override
    protected PointerInfo getRepetitionInfo(int contour, int repetition) {
        this.mRepetitionInfo.reset(this, contour, repetition);
        return this.mRepetitionInfo;
    }

    /**
     * Draw a line.
     * @param canvas the canvas to draw
     * @param info   the notch info
     */
    @Override
    protected void drawLine(Canvas canvas, NotchInfo info, Paint paint) {
        super.drawLine(canvas, info, paint);
        super.drawLine(canvas, info, this.mHaloPaint);
    }

    /**
     * Draw a rectangle.
     * @param canvas the canvas to draw
     * @param info   the notch info
     */
    protected void drawRectangle(Canvas canvas, NotchInfo info, Paint paint) {
        super.drawRectangle(canvas, info, paint);
        super.drawRectangle(canvas, info, this.mHaloPaint);
    }

    /**
     * Draw a oval.
     * @param canvas the canvas to draw
     * @param info   the notch info
     */
    protected void drawOval(Canvas canvas, NotchInfo info, Paint paint) {
        super.drawOval(canvas, info, paint);
        super.drawOval(canvas, info, this.mHaloPaint);
    }

    /**
     * Draw a triangle.
     * @param canvas the canvas to draw
     * @param info   the notch info
     */
    protected void drawTriangle(Canvas canvas, NotchInfo info, Paint paint) {
        super.drawTriangle(canvas, info, paint);
        super.drawTriangle(canvas, info, this.mHaloPaint);
    }

    /**
     * Draw method
     * @param canvas where to draw
     * @hide
     */
    @Override
    protected void onDraw(Canvas canvas, RepetitionInfo info) {
        // Set the painters
        PointerInfo pointerInfo = (PointerInfo) info;
        this.mHaloPaint.setStrokeWidth(this.mHaloWidth);
        this.mHaloPaint.setColor(pointerInfo.color);
        this.mHaloPaint.setAlpha(pointerInfo.pressed ? 255: this.mHaloAlpha);

        Paint paint = this.getPainter();
        paint.setAlpha(pointerInfo.pressed ? this.mHaloAlpha: 255);

        // Call the super method
        super.onDraw(canvas, info);
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

    /**
     * Disable this method.
     * @param value the repetitions number
     * @hide
     */
    @SuppressWarnings("unused")
    @Override
    public void setRepetitions(int value) {
        // Do nothing
    }

    /**
     * Disable this method.
     * @param value the new setting
     * @hide
     */
    @SuppressWarnings("unused")
    @Override
    public void setLastRepetitionOnPathEnd(boolean value) {
        // Do nothing
    }

    /**
     * Disable this method.
     * @param value the new space between repetition value
     * @hide
     */
    @SuppressWarnings("unused")
    @Override
    public void setSpaceBetweenRepetitions(float value) {
        // Do nothing
    }

    /**
     * Disable this method
     * @param value the status
     */
    @SuppressWarnings("unused")
    @Override
    public void setDoubleBuffering(boolean value) {
        // Do nothing
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
        destination.setDistance(this.mDistance);
        destination.setPressed(this.mPressed);

        destination.setHaloAlpha(this.mHaloAlpha);
        destination.setHaloWidth(this.mHaloWidth);
    }

    /**
     * Get the pointer max dimension.
     * Note than this could be depend on the current position on the path.
     * @return   the max dimension
     */
    @SuppressWarnings({"unused", "WeakerAccess"})
    public float getMaxDimension() {
        // Find the max dimension
        float width = this.getWidth(this.mDistance);
        float height = this.getHeight(this.mDistance);
        float max = width > height ? width: height;

        // Add the halo dimension
        return max + this.mHaloWidth;
    }


    // ***************************************************************************************
    // Public properties

    /**
     * Set the distance of pointer in percentage respect to the path start.
     * @param value the position in percentage
     */
    @SuppressWarnings("unused")
    public void setDistance(float value) {
        // Check the limits
        if (value < 0.0f) value = 0.0f;
        if (value > 100.0f) value = 100.0f;

        // Store the value
        if (this.mDistance != value) {
            this.mDistance = value;
            this.onPropertyChange("distance", value);
        }
    }

    /**
     * Get the position of pointer in percentage respect to the path start.
     * @return the position in percentage
     */
    @SuppressWarnings("unused")
    public float getDistance() {
        return this.mDistance;
    }


    /**
     * Set the halo width in pixel
     * @param value the width
     */
    @SuppressWarnings({"unused"})
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
    @SuppressWarnings({"unused"})
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
    @SuppressWarnings({"unused", "WeakerAccess"})
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
    @SuppressWarnings({"unused", "WeakerAccess"})
    public boolean getPressed() {
        return this.mPressed;
    }


    // ***************************************************************************************
    // Public classes and methods

    /**
     * This is a structure to hold the feature information before draw it
     */
    @SuppressWarnings({"unused", "WeakerAccess"})
    public class PointerInfo extends NotchInfo {

        // ***************************************************************************************
        // Properties

        private float[] mGenericPoint;

        public ScPointer source;
        public boolean pressed;

        // ***************************************************************************************
        // Constructor

        public PointerInfo() {
            this.mGenericPoint = new float[2];
        }

        // ***************************************************************************************
        // Public methods

        public void reset(ScPointer feature, int contour, int repetition) {
            // Super
            super.reset(feature, contour, repetition);

            // Holder
            float percentage = feature.getDistance();
            float distance = feature.getDistance(percentage);

            // Reset
            this.source = feature;
            this.pressed = feature.getPressed();
            this.distance = distance;

            this.height = feature.getHeight(distance);
            this.width = feature.getWidth(distance);
            this.tangent = feature.getPointAndAngle(distance, this.mGenericPoint);
            this.color = feature.getGradientColor(distance);

            // Find the center as the point on path
            this.point[0] = this.mGenericPoint[0];
            this.point[1] = this.mGenericPoint[1];
        }

    }

}
