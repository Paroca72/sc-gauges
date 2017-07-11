package com.sccomponents.gauges;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;

/**
 * Create a feature to draw on a given path.
 * <p>
 * The feature is independent and can be used with any path.
 * Is enough to instantiate it passing the path object and call the draw function passing the
 * canvas where draw.
 * The original design of this class was for link it with the ScDrawer to have a base drawer (the
 * ScDrawer linked) and many features applicable to it.
 * <p>
 * The "feature" base class essentially do nothing.
 * For draw something, hence for specialize the feature, you need to override the onDraw method.
 * The base class provides only a common set of methods to display something on the path as the
 * color manager, visibility, limits, ecc. that is useful to inherit it and create a specialized
 * class.
 *
 * @author Samuele Carassai
 * @version 1.0.1
 * @since 2016-05-26
 */
public class ScFeature {

    // ***************************************************************************************
    // Enumerators

    /**
     * The mode to building the painter shader.
     */
    @SuppressWarnings("unuse")
    public enum ColorsMode {
        SOLID,
        GRADIENT
    }


    // ***************************************************************************************
    // Privates and protected variable

    protected Path mPath;
    protected ScPathMeasure mPathMeasure;
    protected float mPathLength;

    protected Paint mPaint;
    protected int[] mColors;
    protected ColorsMode mColorsMode;

    protected String mTag;
    protected boolean mVisible;

    protected float mStartPercentage;
    protected float mEndPercentage;


    /****************************************************************************************
     * Constructor
     */

    @SuppressWarnings("unused")
    public ScFeature(Path path) {
        // Init
        this.mColorsMode = ColorsMode.GRADIENT;
        this.mVisible = true;
        this.mStartPercentage = 0.0f;
        this.mEndPercentage = 100.0f;

        // Path
        this.mPath = path;
        this.mPathMeasure = new ScPathMeasure(this.mPath, false);
        this.mPathLength = this.mPathMeasure.getLength();

        // Create the painter
        this.mPaint = new Paint();
        this.mPaint.setStrokeCap(Paint.Cap.BUTT);
        this.mPaint.setStrokeWidth(0.0f);
        this.mPaint.setStyle(Paint.Style.STROKE);
        this.mPaint.setColor(Color.BLACK);
        this.mPaint.setAntiAlias(true);
    }


    // ***************************************************************************************
    // Protected methods

    /**
     * The draw method to override in the inherited classes.
     *
     * @param canvas where draw
     */
    @SuppressWarnings("unused")
    protected void onDraw(Canvas canvas) {
        // To implement
    }


    // ***************************************************************************************
    // Public and static methods

    /**
     * Draw something on the canvas.
     *
     * @param canvas where draw
     */
    @SuppressWarnings("unused")
    public void draw(Canvas canvas) {
        // Check for the visibility
        if (!this.mVisible || this.mPath == null) return;

        // If the have only one color inside the colors array set it directly on the painter
        if (this.mColors != null && this.mColors.length == 1) {
            this.mPaint.setColor(this.mColors[0]);
        }

        // Call the base onDraw method
        this.onDraw(canvas);
    }

    /**
     * Refresh the feature measure.
     */
    @SuppressWarnings("unused")
    public void refresh() {
        this.mPathMeasure.setPath(this.mPath, false);
        this.mPathLength = this.mPathMeasure.getLength();
    }

    /**
     * Convert a point represented by an array to an modern object.
     * Supposed that the 0 array position correspond to the x coordinate and on the 1 array
     * position correspond the y coordinate.
     *
     * @param point the array
     * @return the point
     */
    @SuppressWarnings("unused")
    public static PointF toPoint(float[] point) {
        // Check the passed point
        if (point == null) return null;
        if (point.length < 2) throw new IndexOutOfBoundsException();

        // Do a conversion
        return new PointF(point[0], point[1]);
    }

    /**
     * Translate a point considering the angle (in degrees) and the offset.
     * Move the pointer on the tangent defined by the angle.
     *
     * @param point  the point to translate
     * @param offset the point offset
     * @param angle  the angle reference in degrees
     */
    @SuppressWarnings("unused")
    public static void translatePoint(PointF point, float offset, float angle) {
        // Convert to radiant
        float radiant = (float) Math.toRadians(angle);
        // Transpose
        point.x += (float) (Math.cos(radiant) * offset);
        point.y += (float) (Math.sin(radiant) * offset);
    }

    /**
     * Translate a point considering the angle (in degrees) and the offset (x, y).
     * Move the pointer on the tangent defined by the angle by the x value and move the pointer
     * on the perpendicular defined by the angle by the y value.
     *
     * @param point   the point to translate
     * @param offsetX the point offset
     * @param offsetY the point offset
     * @param angle   the angle reference in degrees
     */
    @SuppressWarnings("unused")
    public static void translatePoint(PointF point, float offsetX, float offsetY, float angle) {
        // Translate on the x
        ScFeature.translatePoint(point, offsetX, angle);
        // Translate on the y
        ScFeature.translatePoint(point, offsetY, angle + 90.0f);
    }

    /**
     * Return a point on path given the distance from the path start.
     *
     * @param distance the distance
     * @return the point on path
     */
    @SuppressWarnings("unused")
    public PointF getPoint(float distance) {
        // Get the point
        float[] point = this.mPathMeasure.getPosTan(distance);
        // Check and return
        return point == null ? null : ScFeature.toPoint(point);
    }

    /**
     * Given a percentage return back the relative distance from the path start.
     *
     * @param percentage the percentage
     * @return the distance
     */
    @SuppressWarnings("unused")
    public float getDistance(float percentage) {
        // Check percentage
        if (percentage > 100.0f) percentage = 100.0f;
        if (percentage < 0.0f) percentage = 0.0f;
        // Return
        return this.mPathMeasure.getLength() * percentage / 100.0f;
    }

    /**
     * Get the angle in degrees of the tangent to a point on the path given the distance from
     * the start of path.
     *
     * @param distance the distance
     * @return the angle in degrees
     */
    @SuppressWarnings("unused")
    public float getTangentAngle(float distance) {
        // Get the point
        float[] point = this.mPathMeasure.getPosTan(distance);
        // Check and return
        return point == null ? 0.0f : (float) Math.toDegrees(point[3]);
    }

    /**
     * Set the drawing limits (in percentage).
     * The assignment happen only if the value is different from infinity.
     *
     * @param start the start value
     * @param end   the end value
     */
    @SuppressWarnings("unused")
    public void setLimits(float start, float end) {
        // Store the new values
        if (!Float.isInfinite(start)) this.mStartPercentage = start;
        if (!Float.isInfinite(end)) this.mEndPercentage = end;
    }

    /**
     * Get the current gradient color by a ratio dependently about the distance from the
     * starting of path, the colors array and the mode to draw.
     * If the colors are not defined will be returned the current color of painter.
     *
     * @param distance from the starting path
     * @param length   force the length of the path
     * @return the color
     */
    @SuppressWarnings("unused")
    public int getGradientColor(float distance, float length) {
        // Check color constraints
        if (this.mColors == null || this.mColors.length == 0 || length <= 0)
            return this.mPaint.getColor();

        if (this.mColors.length == 1 || distance <= 0)
            return this.mColors[0];

        if (distance >= length)
            return this.mColors[this.mColors.length - 1];

        // Holder
        int sector = (int) (distance / (length / this.mColors.length));

        // Check sector limits
        if (sector < 0) sector = 0;
        if (sector > this.mColors.length - 1) sector = this.mColors.length - 1;

        // Check the case
        switch (this.mColorsMode) {
            case SOLID:
                return this.mColors[sector];

            case GRADIENT:
                // Calculation of the right ratio by the current sector
                float sectorLen = length / (this.mColors.length - 1);
                sector = (int) (distance / sectorLen);
                float normalized = distance - (sectorLen * sector);
                float ratio = normalized / sectorLen;

                // Get the color to mix
                int startColor = this.mColors[sector];
                int endColor = this.mColors[sector + 1];

                // Calculate the result color
                int red = (int) (Color.red(endColor) * ratio + Color.red(startColor) * (1 - ratio));
                int green = (int) (Color.green(endColor) * ratio + Color.green(startColor) * (1 - ratio));
                int blue = (int) (Color.blue(endColor) * ratio + Color.blue(startColor) * (1 - ratio));

                // Get the color
                return Color.rgb(red, green, blue);

            default:
                return Color.BLACK;
        }
    }

    /**
     * Get the current gradient color by a ratio dependently about the distance from the
     * starting of path, the colors array and the mode to draw.
     * If the colors are not defined will be returned the current color of painter.
     *
     * @param distance from the starting path
     * @return the color
     */
    public int getGradientColor(float distance) {
        return this.getGradientColor(distance, this.mPathLength);
    }


    // ***************************************************************************************
    // Public properties

    /**
     * Return the painter
     *
     * @return the painter
     */
    @SuppressWarnings("unused")
    public Paint getPainter() {
        return this.mPaint;
    }

    /**
     * Set the painter
     *
     * @param value the painter
     */
    @SuppressWarnings("unused")
    public void setPainter(Paint value) {
        this.mPaint = value;
    }

    /**
     * Get the tag
     *
     * @return the tag
     */
    @SuppressWarnings("unused")
    public String getTag() {
        return this.mTag;
    }

    /**
     * Set the tag
     *
     * @param value the tag
     */
    @SuppressWarnings("unused")
    public void setTag(String value) {
        this.mTag = value;
    }

    /**
     * Get the visibility
     *
     * @return the tag
     */
    @SuppressWarnings("unused")
    public boolean getVisible() {
        return this.mVisible;
    }

    /**
     * Set the visibility
     *
     * @param value the tag
     */
    @SuppressWarnings("unused")
    public void setVisible(boolean value) {
        this.mVisible = value;
    }

    /**
     * Return the current stroke colors
     *
     * @return the current stroke colors
     */
    @SuppressWarnings("unused")
    public int[] getColors() {
        return this.mColors;
    }

    /**
     * Set the current stroke colors
     *
     * @param value the new stroke colors
     */
    @SuppressWarnings("unused")
    public void setColors(int... value) {
        this.mColors = value;
    }

    /**
     * Return the colors filling mode.
     * You can have to way for draw the colors of the path: SOLID or GRADIENT.
     *
     * @return the color filling mode
     */
    @SuppressWarnings("unused")
    public ColorsMode getColorsMode() {
        return this.mColorsMode;
    }

    /**
     * Set the colors filling mode.
     * You can have to way for draw the colors of the path: SOLID or GRADIENT.
     *
     * @param value the new color filling mode
     */
    @SuppressWarnings("unused")
    public void setColorsMode(ColorsMode value) {
        // Store the new value and refresh the component
        this.mColorsMode = value;
    }

}
