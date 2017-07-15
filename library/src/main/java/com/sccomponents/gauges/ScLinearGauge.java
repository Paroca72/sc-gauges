package com.sccomponents.gauges;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;

/**
 * Draw a line
 *
 * @author Samuele Carassai
 * @version 1.1.0
 * @since 2016-08-16
 */
@SuppressWarnings("unused")
public class ScLinearGauge extends ScGauge {

    // ***************************************************************************************
    // Enumerators

    /**
     * The mode to building the painter shader.
     */
    @SuppressWarnings("unuse")
    public enum Orientation {
        CUSTOM,
        HORIZONTAL,
        VERTICAL
    }


    // ***************************************************************************************
    // Private attributes

    protected RectF mBounds;


    // ***************************************************************************************
    // Constructors

    public ScLinearGauge(Context context) {
        super(context);
        this.init(context, null, 0);
    }

    public ScLinearGauge(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(context, attrs, 0);
    }

    public ScLinearGauge(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init(context, attrs, defStyleAttr);
    }


    // ***************************************************************************************
    // Privates methods

    /**
     * Reflect the current orientation on the path bounds limit.
     */
    private void internalSetOrientation(Orientation orientation) {
        // Check the case
        switch (orientation) {
            case HORIZONTAL:
                this.mBounds = new RectF(0.0f, 0.0f, 100.0f, 0.0f);
                break;

            case VERTICAL:
                this.mBounds = new RectF(0.0f, 100.0f, 0.0f, 0.0f);
                break;
        }
    }

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
        float left = attrArray.getFloat(R.styleable.ScGauges_left, 0.0f);
        float top = attrArray.getFloat(R.styleable.ScGauges_top, 0.0f);
        float right = attrArray.getFloat(R.styleable.ScGauges_right, 100.0f);
        float bottom = attrArray.getFloat(R.styleable.ScGauges_bottom, 0.0f);

        // Check the value
        left = ScBase.valueRangeLimit(left, 0.0f, 100.0f);
        top = ScBase.valueRangeLimit(top, 0.0f, 100.0f);
        right = ScBase.valueRangeLimit(right, 0.0f, 100.0f);
        bottom = ScBase.valueRangeLimit(bottom, 0.0f, 100.0f);

        // Create the boundaries in percentage
        this.mBounds = new RectF(left, top, right, bottom);

        // Predefined orientation
        int orientation = attrArray.getInt(
                R.styleable.ScGauges_orientation, Orientation.CUSTOM.ordinal());
        this.internalSetOrientation(Orientation.values()[orientation]);

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
    protected Path createPath(int width, int height) {
        // Calculate the left, top and right, bottom location by percentage
        float left = (this.mBounds.left / 100.0f) * (float) width;
        float top = (this.mBounds.top / 100.0f) * (float) height;
        float right = (this.mBounds.right / 100.0f) * (float) width;
        float bottom = (this.mBounds.bottom / 100.0f) * (float) height;

        // Create the path
        Path path = new Path();
        path.moveTo(left, top);
        path.lineTo(right, bottom);

        // Return
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
        state.putFloat("mBoundsLeft", this.mBounds.left);
        state.putFloat("mBoundsTop", this.mBounds.top);
        state.putFloat("mBoundsRight", this.mBounds.right);
        state.putFloat("mBoundsBottom", this.mBounds.bottom);

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
        this.mBounds.left = savedState.getFloat("mBoundsLeft");
        this.mBounds.top = savedState.getFloat("mBoundsTop");
        this.mBounds.right = savedState.getFloat("mBoundsRight");
        this.mBounds.bottom = savedState.getFloat("mBoundsBottom");
    }


    // ***************************************************************************************
    // Public properties

    /**
     * Return the left bounds in percentage
     *
     * @return the left bounds percentage
     */
    @SuppressWarnings("unused")
    public float getLeftBounds() {
        return this.mBounds.left;
    }

    /**
     * Set the left position in percentage
     *
     * @param value of the left bounds in percentage
     */
    @SuppressWarnings("unused")
    public void setLeftBounds(float value) {
        // Limit the percentage
        value = ScBase.valueRangeLimit(value, 0.0f, 100.0f);

        // Check if value is changed
        if (this.mBounds.left != value) {
            // Store the new value
            this.mBounds.left = value;
            this.requestLayout();
        }
    }

    /**
     * Return the top bounds in percentage
     *
     * @return the top bounds percentage
     */
    @SuppressWarnings("unused")
    public float getTopBounds() {
        return this.mBounds.top;
    }

    /**
     * Set the top position in percentage
     *
     * @param value of the top bounds in percentage
     */
    @SuppressWarnings("unused")
    public void setTopBounds(float value) {
        // Limit the percentage
        value = ScBase.valueRangeLimit(value, 0.0f, 100.0f);

        // Check if value is changed
        if (this.mBounds.top != value) {
            // Store the new value
            this.mBounds.top = value;
            this.requestLayout();
        }
    }

    /**
     * Return the right bounds in percentage
     *
     * @return the right bounds percentage
     */
    @SuppressWarnings("unused")
    public float getRightBounds() {
        return this.mBounds.right;
    }

    /**
     * Set the right position in percentage
     *
     * @param value of the right bounds in percentage
     */
    @SuppressWarnings("unused")
    public void setRightBounds(float value) {
        // Limit the percentage
        value = ScBase.valueRangeLimit(value, 0.0f, 100.0f);

        // Check if value is changed
        if (this.mBounds.right != value) {
            // Store the new value
            this.mBounds.right = value;
            this.requestLayout();
        }
    }

    /**
     * Return the bottom bounds in percentage
     *
     * @return the bottom bounds percentage
     */
    @SuppressWarnings("unused")
    public float getBottomBounds() {
        return this.mBounds.bottom;
    }

    /**
     * Set the bottom position in percentage
     *
     * @param value of the bottom bounds in percentage
     */
    @SuppressWarnings("unused")
    public void setBottomBounds(float value) {
        // Limit the percentage
        value = ScBase.valueRangeLimit(value, 0.0f, 100.0f);

        // Check if value is changed
        if (this.mBounds.bottom != value) {
            // Store the new value
            this.mBounds.bottom = value;
            this.requestLayout();
        }
    }

    /**
     * Return the current orientation
     *
     * @return the orientation
     */
    @SuppressWarnings("unused")
    public Orientation getOrientation() {
        // Check for horizontal
        if (this.mBounds.top == this.mBounds.bottom) return Orientation.HORIZONTAL;
        // Check for vertical
        if (this.mBounds.left == this.mBounds.right) return Orientation.VERTICAL;
        // Custom
        return Orientation.CUSTOM;
    }

    /**
     * Set the current orientation.
     * Set the value to "horizontal" it the same to set top and bottom at the same value.
     * Set the value to "vertical" it the same to set left and right at the same value.
     * Set the value to "custom" no have visible effect.
     *
     * @param value of the bottom bounds in percentage
     */
    @SuppressWarnings("unused")
    public void setOrientation(Orientation value) {
        // Check if value is changed
        if (this.getOrientation() != value) {
            // Store the new value
            this.internalSetOrientation(value);
            this.requestLayout();
        }
    }


}
