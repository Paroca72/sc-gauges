package com.sccomponents.gauges;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.ViewGroup;

/**
 * Draw an arc
 *
 * @author Samuele Carassai
 * @version 2.0.0
 * @since 2016-05-26
 */
public class ScArcGauge extends ScGauge {

    /****************************************************************************************
     * Constants
     */

    public static final float DEFAULT_ANGLE_START = 0.0f;
    public static final float DEFAULT_ANGLE_SWEEP = 360.0f;


    /****************************************************************************************
     * Private attributes
     */

    protected float mAngleStart;
    protected float mAngleSweep;


    // ***************************************************************************************
    // Constructors

    public ScArcGauge(Context context) {
        super(context);
        this.init(context, null, 0);
    }

    public ScArcGauge(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(context, attrs, 0);
    }

    public ScArcGauge(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init(context, attrs, defStyleAttr);
    }


    // ***************************************************************************************
    // Privates methods

    /**
     * Init the component.
     * Retrieve all attributes with the default values if needed.
     * Check the values for internal use and create the painters.
     *
     * @param context  the owner context
     * @param attrs    the attribute set
     * @param defStyle the style
     */
    private void init(Context context, AttributeSet attrs, int defStyle) {
        //--------------------------------------------------
        // ATTRIBUTES

        // Get the attributes list
        final TypedArray attrArray = context
                .obtainStyledAttributes(attrs, R.styleable.ScGauges, defStyle, 0);

        // Read all attributes from xml and assign the value to linked variables
        this.mAngleStart = attrArray.getFloat(
                R.styleable.ScGauges_angleStart, ScArcGauge.DEFAULT_ANGLE_START);
        this.mAngleSweep = attrArray.getFloat(
                R.styleable.ScGauges_angleSweep, ScArcGauge.DEFAULT_ANGLE_SWEEP);

        // Recycle
        attrArray.recycle();
    }


    // ***************************************************************************************
    // Overrides

    /**
     * Create the path to draw.
     * This is fundamental to draw something on the canvas.
     *
     * @return the path
     */
    @Override
    @SuppressWarnings("all")
    protected Path createPath(int width, int height) {
        // If have any wrap dimensions to apply to the content we want to have a perfect circle
        // so we'll update the dimensions for have equal.
        if (this.getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT) width = height;
        if (this.getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT) height = width;

        // the new path and the area
        Path path = new Path();
        RectF area = new RectF(0.0f, 0.0f, width, height);

        // Draw the arc
        path.addArc(area, this.mAngleStart, this.mAngleSweep);

        // If the sweep angle if 360° must resolve a issue with addArc than lost the starting
        // angle. This issue happen only in version less than API 26.
        if ((this.mAngleSweep == 360.0f || this.mAngleSweep == -360.0f) &&
                android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            // Create a transform and rotate the path on the starting angle
            Matrix matrix = new Matrix();
            matrix.postRotate(this.mAngleStart);
            path.transform(matrix);
        }

        // Return the path
        return path;
    }


    // ***************************************************************************************
    // Instance state

    /**
     * Save the current instance state
     *
     * @return the state
     */
    @Override
    protected Parcelable onSaveInstanceState() {
        // Call the super and get the parent state
        Parcelable superState = super.onSaveInstanceState();

        // Create a new bundle for store all the variables
        Bundle state = new Bundle();
        // Save all starting from the parent state
        state.putParcelable("PARENT", superState);
        state.putFloat("mAngleStart", this.mAngleStart);
        state.putFloat("mAngleSweep", this.mAngleSweep);

        // Return the new state
        return state;
    }

    /**
     * Restore the current instance state
     *
     * @param state the state
     */
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        // Implicit conversion in a bundle
        Bundle savedState = (Bundle) state;

        // Recover the parent class state and restore it
        Parcelable superState = savedState.getParcelable("PARENT");
        super.onRestoreInstanceState(superState);

        // Now can restore all the saved variables values
        this.mAngleStart = savedState.getFloat("mAngleStart");
        this.mAngleSweep = savedState.getFloat("mAngleSweep");
    }


    // ***************************************************************************************
    // Public methods

    /**
     * Convert a percentage value in a angle (in degrees) value respect the start and sweep angles.
     *
     * @param percentage the starting value
     * @return the angle value
     */
    @SuppressWarnings("unused")
    public float percentageToAngle(float percentage) {
        return (this.mAngleSweep * (percentage / 100)) + this.mAngleStart;
    }


    // ***************************************************************************************
    // Public properties

    /**
     * Return the start angle
     *
     * @return the start angle in degrees
     */
    @SuppressWarnings("unused")
    public float getAngleStart() {
        return this.mAngleStart;
    }

    /**
     * Set the start angle
     *
     * @param value the start angle in degrees
     */
    @SuppressWarnings("unused")
    public void setAngleStart(float value) {
        // Check if value is changed
        if (this.mAngleStart != value) {
            // Store the new value
            this.mAngleStart = value;
            this.requestLayout();
        }
    }

    /**
     * Return the sweep angle
     *
     * @return the sweep angle in degrees
     */
    @SuppressWarnings("unused")
    public float getAngleSweep() {
        return this.mAngleSweep;
    }

    /**
     * Set the sweep angle
     *
     * @param value the sweep angle in degrees
     */
    @SuppressWarnings("unused")
    public void setAngleSweep(float value) {
        // Normalize
        if (value <= -360.0f) value = -360.0f;
        if (value >= 360.0f) value = 360.0f;

        // Check if value is changed
        if (this.mAngleSweep != value) {
            // Store the new value
            this.mAngleSweep = value;
            this.requestLayout();
        }
    }

}