package com.sccomponents.codes.gauges;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * Draw a pointer on the path at certain distance from the path start.

 * @author Samuele Carassai
 * @version 3.0.0
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

    private float mValue;
    private boolean mPressed;

    private float mHaloWidth;
    private int mHaloAlpha;
    private Paint mHaloPaint;
    private PointerInfo mRepetitionInfo;


    /****************************************************************************************
     * Constructor
     */

    @SuppressWarnings("unused")
    public ScPointer(Path path) {
        // Super
        super(path);

        // Init
        this.setRepetitions(1);
        this.setLastRepetitionOnPathEnd(false);

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
     * Draw method
     * @param canvas where to draw
     * @hide
     */
    @Override
    protected void onDraw(Canvas canvas, RepetitionInfo info) {
        // Set the painters
        PointerInfo pointerInfo = (PointerInfo) info;
        this.mHaloPaint.setAlpha(pointerInfo.pressed ? 255: this.mHaloAlpha);
        this.mHaloPaint.setStrokeWidth(this.mHaloWidth);

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
        destination.setValue(this.mValue);
        destination.setPressed(this.mPressed);

        destination.setHaloAlpha(this.mHaloAlpha);
        destination.setHaloWidth(this.mHaloWidth);
    }

    /**
     * Get the pointer mac dimension.
     * Note than this could be depend on the current position on the path.
     * @return   the max dimension
     */
    @SuppressWarnings("unused")
    public float getMaxDimension() {
        // Find the max dimension
        float width = this.getWidth(this.mValue);
        float height = this.getHeight(this.mValue);
        float max = width > height ? width: height;

        // Add the halo dimension
        return max + this.mHaloWidth;
    }


    // ***************************************************************************************
    // Public properties

    /**
     * Set the position of pointer in percentage respect to the path height.
     * @param value the position in percentage
     */
    @SuppressWarnings("unused")
    public void setValue(float value) {
        // Check the limits
        if (value < 0.0f) value = 0.0f;
        if (value > 100.0f) value = 100.0f;

        // Store the value
        if (this.mValue != value) {
            this.mValue = value;
            this.onPropertyChange("pointer", value);
        }
    }

    /**
     * Get the position of pointer in percentage respect to the path height.
     * @return the position in percentage
     */
    @SuppressWarnings("unused")
    public float getValue() {
        return this.mValue;
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


    // ***************************************************************************************
    // Public classes and methods

    /**
     * This is a structure to hold the feature information before draw it
     */
    @SuppressWarnings("unused")
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

            // Reset
            this.source = feature;
            this.pressed = feature.getPressed();
            this.distance = feature.getValue();

            this.height = feature.getHeight(this.distance);
            this.width = feature.getWidth(this.distance);
            this.tangent = feature.getPointAndAngle(this.distance, this.mGenericPoint);
            this.color = feature.getGradientColor(this.distance);
        }

    }

}
