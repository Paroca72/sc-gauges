package com.sccomponents.gauges.library;

import android.content.Context;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

/**
 * The base class.
 * <p>
 * This is class no have a direct utility.
 * This is a simpler collection of methods used from the inherited classes as utility.
 *
 * @author Samuele Carassai
 * @version 3.5.1
 * @since 2016-05-26
 */
public abstract class ScBase extends View {

    // ***************************************************************************************
    // Constructors

    public ScBase(Context context) {
        super(context);
    }

    public ScBase(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScBase(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    // ***************************************************************************************
    // Privates methods

    /**
     * Get the display metric.
     * This method is used for screen measure conversion.
     * @param context   the current context
     * @return          the display metrics
     */
    private DisplayMetrics getDisplayMetrics(Context context) {
        // Get the window manager from the window service
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        assert wm != null;

        // Create the variable holder and inject the values
        DisplayMetrics displayMetrics = new DisplayMetrics();
        Display display = wm.getDefaultDisplay();
        display.getMetrics(displayMetrics);

        // Return
        return displayMetrics;
    }


    // ***************************************************************************************
    // Public methods

    /**
     * Convert Dip to Pixel using the current display metrics.
     * @param dip   the start value in Dip
     * @return      the correspondent value in Pixels
     */
    @SuppressWarnings("unused")
    public float dipToPixel(float dip) {
        // Get the display metrics
        DisplayMetrics metrics = this.getDisplayMetrics(this.getContext());
        // Calc the conversion by the screen density
        return dip * metrics.density;
    }


    // ***************************************************************************************
    // Static methods

    /**
     * Limit number within a values range.
     * This method not consider the sign and the upper and lower values limit order.
     * @param value         the value to limit
     * @param startValue    the start limit
     * @param endValue      the end value
     * @return              the normalized value
     */
    @SuppressWarnings("unused")
    public static float valueRangeLimit(float value, float startValue, float endValue) {
        // Holders
        float min = Math.min(startValue, endValue);
        float max = Math.max(startValue, endValue);

        // If is over the limit return the normalized value
        if (value < min) return min;
        if (value > max) return max;

        // Else return the original value
        return value;
    }

    /**
     * Limit number within a values range.
     * This method not consider the sign and the upper and lower values limit order.
     * @param value         the value to limit
     * @param startValue    the start limit
     * @param endValue      the end value
     * @return              the normalized value
     */
    public static int valueRangeLimit(int value, int startValue, int endValue) {
        return (int) ScBase.valueRangeLimit((float) value, (float) startValue, (float) endValue);
    }

    /**
     * Check if number is within a values range.
     * This method not consider the sign and the upper and lower values limit order.
     * @param value         the value to check
     * @param startValue    the start value
     * @param endValue      the end value
     * @return              true if within
     */
    @SuppressWarnings("unused")
    public static boolean withinRange(float value, float startValue, float endValue) {
        return value == ScBase.valueRangeLimit(value, startValue, endValue);
    }

    /**
     * Find the max given a series of values.
     * @param values    values to compare
     * @return          the maximum
     */
    @SuppressWarnings("unused")
    public static float findMaxValue(float... values) {
        // Check for null values
        if (values == null || values.length == 0) return 0;

        // Cycle all other values
        float max = Float.MIN_VALUE;
        for (float value : values) {
            // Find the max
            if (max < value) max = value;
        }
        // Return
        return max;
    }

    /**
     * Inflate a rectangle by the passed value.
     * The method return a new inflated rectangle and can alter the origin too.
     * @param source        the source rectangle
     * @param value         the inflate value
     * @param holdOrigin    if false reset the rectangle on its origin
     * @return              the new inflated rectangle
     */
    public static RectF inflateRect(RectF source, float value, boolean holdOrigin) {
        // Create a copy of the rect
        RectF dest = new RectF(source);
        // Reduce the width and the height
        dest.right -= value * 2;
        dest.bottom -= value * 2;
        // Translate if needed
        if (!holdOrigin) {
            dest.offset(value, value);
        }
        // Return
        return dest;
    }

    /**
     * Inflate a rectangle by the passed value.
     * The method return a new inflated rectangle and can alter the origin too.
     * @param source    the source rectangle
     * @param value     the inflate value
     * @return          the new inflated rectangle
     */
    @SuppressWarnings("unused")
    public static RectF inflateRect(RectF source, float value) {
        return ScBase.inflateRect(source, value, false);
    }

    /**
     * Reset the rectangle to its origin
     * @param rect  the original rectangle
     * @return      a new resettled rectangle
     */
    @SuppressWarnings("unused")
    public static RectF resetRectToOrigin(RectF rect) {
        // Create a new rect
        RectF newRect = new RectF(rect);
        // Reset to origin and return it
        newRect.offset(-newRect.left, -newRect.top);
        return newRect;
    }

    /**
     * Swap two array elements position.
     * @param source    a generic array source
     * @param first     the first position
     * @param second    the second position
     */
    @SuppressWarnings("unused")
    public static <T> void swapArrayPosition(T[] source, int first, int second) {
        T temp = source[first];
        source[first] = source[second];
        source[second] = temp;
    }

    /**
     * Swap two array elements position.
     * @param source    the array source
     * @param first     the first position
     * @param second    the second position
     */
    @SuppressWarnings("unused")
    public static void swapArrayPosition(int[] source, int first, int second) {
        int temp = source[first];
        source[first] = source[second];
        source[second] = temp;
    }

}
