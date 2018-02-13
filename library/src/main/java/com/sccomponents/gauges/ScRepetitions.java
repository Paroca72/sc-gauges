package com.sccomponents.gauges;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import java.util.ArrayList;
import java.util.List;

/**
 * This class extends the ScFeature give to it the possibility to manage the repetitions.
 *
 * @author Samuele Carassai
 * @version 3.0.0
 * @since 2016-05-26
 */
public abstract class ScRepetitions extends ScFeature {

    // ***************************************************************************************
    // Private variable

    private boolean mLastRepetitionOnPathEnd;
    private int mRepetitions;
    private float mSpaceBetween;
    private RepetitionInfo mRepetitionInfo;

    // Listener
    private OnDrawRepetitionListener mOnDrawListener;


    /****************************************************************************************
     * Constructor
     */

    @SuppressWarnings("unused")
    public ScRepetitions(Path path) {
        // Init
        super(path);

        // Repetitions
        this.mRepetitions = 0;
        this.mSpaceBetween = 0.0f;
        this.mLastRepetitionOnPathEnd = true;

        // Generic
        this.mRepetitionInfo = new RepetitionInfo();
    }


    // ***************************************************************************************
    // Methods to override

    /**
     * The draw method to override in the inherited classes.
     * @param canvas    where draw
     * @param info      the contour info
     * @hide
     */
    @Override
    protected void onDraw(Canvas canvas, ContourInfo info) {
        // Do nothing
    }

    /**
     * Get the current repetition drawing info.
     * This methods must be overridden for create custom drawing info for inherited
     * classes.
     * @param repetition    the repetition index
     * @return              the repetition drawing info
     */
    @SuppressWarnings("unused")
    protected RepetitionInfo getRepetitionInfo(int contour, int repetition) {
        this.mRepetitionInfo.reset(this, contour, repetition);
        return this.mRepetitionInfo;
    }

    /**
     * The draw method to override in the inherited classes.
     * @param canvas where draw
     */
    @SuppressWarnings("unused")
    protected abstract void onDraw(Canvas canvas, RepetitionInfo info);


    // ***************************************************************************************
    // Private methods

    /**
     * Calculate the number of repetition considering the space between them.
     * @return the repetitions
     * @hide
     */
    protected float[] calculateRepetitions() {
        // If no value return the default value
        if (this.mSpaceBetween <= 0.0f)
            return new float[] {};

        // Holders
        float length = this.getMeasure().getLength();
        float currentPosition = 0.0f;
        List<Float> repetitions = new ArrayList<>();

        // Start calculate
        while (currentPosition < length - this.mSpaceBetween) {
            // Adjust the current position
            float width = this.getWidth(currentPosition, length);
            currentPosition += width + this.mSpaceBetween;

            // Store
            repetitions.add(currentPosition);
        }

        // Check for last
        if (this.mLastRepetitionOnPathEnd)
            repetitions.add(length);

        // Convert to primitive
        float[] primitive = new float[repetitions.size()];
        for(int index = 0, len = repetitions.size(); index < len; index ++)
            primitive[index] = repetitions.get(index);

        // Return
        return primitive;
    }

    /**
     * Draw a single repetition.
     * This method is implemented just for give the possibility to override it for some
     * future application.
     * @param canvas    where to draw
     * @param info      the current repetition info
     * @hide
     */
    protected void drawRepetition(Canvas canvas, RepetitionInfo info) {
        // Draw
        this.onDraw(canvas, info);
    }

    /**
     * Draw all the repetitions
     * @param canvas    where to draw
     * @param contour   the current contour index
     * @hide
     */
    protected void drawRepetitions(Canvas canvas, int contour) {
        // Check for auto-calculate the repetitions number
        if (this.mSpaceBetween > 0) {
            float[] repetitions = this.calculateRepetitions();
            this.mRepetitions = repetitions.length;
        }

        // Cycle all repetition
        for (int repetition = 1; repetition <= this.mRepetitions; repetition++) {
            // Get the drawing info
            RepetitionInfo info = this.getRepetitionInfo(contour, repetition);

            // Call the base listener
            if (this.mOnDrawListener != null)
                this.mOnDrawListener.onDrawRepetition(info);

            // Check for visibility
            if (!info.isVisible)
                continue;

            // Rotate, translate and scale
            canvas.save();
            canvas.rotate(info.tangent, info.point[0], info.point[1]);
            canvas.translate(info.offset[0], info.offset[1]);
            canvas.rotate(info.angle, info.point[0], info.point[1]);
            canvas.scale(info.scale[0], info.scale[1], info.point[0], info.point[1]);

            // Call the draw for the single repetition
            this.drawRepetition(canvas, info);
            canvas.restore();
        }
    }

    /**
     * Override the super method to draw all the repetitions too.
     * @param canvas    where to draw
     * @param info      the current contour info
     * @hide
     */
    @Override
    protected void drawContour(Canvas canvas, ScFeature.ContourInfo info) {
        this.drawRepetitions(canvas, info.contour);
    }


    // ***************************************************************************************
    // Public and static methods

    /**
     * Draw method
     * @param canvas where to draw
     * @hide
     */
    @Override
    public void draw(Canvas canvas) {
        // Check for repetition
        if (this.mRepetitions == 0)
            return ;

        // Call the super
        super.draw(canvas);
    }

    /**
     * Given a repetition return the relative distance from the path start.
     * @param repetition    the percentage of the path
     * @return              the distance
     */
    @SuppressWarnings("unused")
    public float getDistance(int repetition) {
        // Check for zero value
        if (this.mRepetitions == 0 || repetition == 0)
            return 0.0f;

        // Check for auto-calculated repetitions.
        // This is need to execute only the have more than one widths.
        if (this.mSpaceBetween > 0.0f &&
                (this.getWidths() != null && this.getWidths().length > 1)) {
            float[] repetitions = this.calculateRepetitions();
            return repetitions[repetition - 1];
        }

        // Correct the repetition number if needs
        repetition -= 1;
        int repetitions = this.mRepetitions -
                (this.mLastRepetitionOnPathEnd && !this.getMeasure().isClosed() ? 1: 0);

        // Convert the repetition in a percentage
        float percentage = repetitions <= 0?
                0.0f: this.range(100.0f / repetitions * repetition);
        return this.getDistance(percentage);
    }

    /**
     * Check if a repetition if over the limits
     * @param   repetition the current repetition
     * @return  if over the global limits
     */
    @SuppressWarnings("unused")
    public boolean isOverLimits(int repetition) {
        float distance = this.getDistance(repetition);
        return distance < this.getStartAtDistance() || distance > this.getEndToDistance();
    }

    /**
     * Round the value near the closed notch.
     * @param value the value to round
     * @return      a rounded to notch value
     */
    @SuppressWarnings("unused")
    public float snapToRepetitions(float value) {
        // Check for empty values
        if (this.mRepetitions == 0)
            return value;

        // Calc the delta angle and round at notches value
        float length = this.getMeasure().getLength();
        float deltaAngle = length / this.mRepetitions;
        float position = Math.round(value / deltaAngle) * deltaAngle;

        // Check and return the value
        return position > length ? length : position;
    }

    /**
     * Implement a copy of this object
     * @param destination the destination object
     */
    @SuppressWarnings("unused")
    public void copy(ScRepetitions destination) {
        // Super
        super.copy(destination);

        // Set
        destination.setLastRepetitionOnPathEnd(this.mLastRepetitionOnPathEnd);
        destination.setRepetitions(this.mRepetitions);
        destination.setSpaceBetweenRepetitions(this.mSpaceBetween);
    }

    /**
     * Implement a copy of this object
     * @param destination the destination object
     * @hide
     */
    @SuppressWarnings("unused")
    @Override
    public void copy(ScFeature destination) {
        if (destination instanceof ScRepetitions)
            this.copy((ScRepetitions) destination);
        else
            super.copy(destination);
    }


    // ***************************************************************************************
    // Getter and setter

    /**
     * Set the repetitions count.
     * This method reset the space between value.
     * @param value the repetitions number
     */
    @SuppressWarnings("unused")
    public void setRepetitions(int value) {
        value = value < 0 ? 0: value;
        if (this.mRepetitions != value) {
            this.mSpaceBetween = 0.0f;
            this.mRepetitions = value;
            this.onPropertyChange("repetitions", value);
        }
    }

    /**
     * Get the repetitions count.
     * @return get the repetitions number
     */
    @SuppressWarnings("unused")
    public int getRepetitions() {
        return this.mRepetitions;
    }


    /**
     * If true the last repetition distance from path start will be equal to the path height.
     * @param value the new setting
     */
    @SuppressWarnings("unused")
    public void setLastRepetitionOnPathEnd(boolean value) {
        if (this.mLastRepetitionOnPathEnd != value) {
            this.mLastRepetitionOnPathEnd = value;
            this.onPropertyChange("lastRepetitionOnPathEnd", value);
        }
    }

    /**
     * If true the last repetition distance from path start will be equal to the path height.
     * @return true if the last repetition is on the path end
     */
    @SuppressWarnings("unused")
    public boolean getLastRepetitionOnPathEnd() {
        return this.mLastRepetitionOnPathEnd;
    }


    /**
     * Define the space between the repetitions.
     * Will considered only value over zero in other case will not consider as set. If set
     * the repetitions count will recalculate every time before draw.
     * @param value the new space between repetition value
     */
    @SuppressWarnings("unused")
    public void setSpaceBetweenRepetitions(float value) {
        if (this.mSpaceBetween != value) {
            this.mSpaceBetween = value;
            this.onPropertyChange("spaceBetweenRepetitions", value);
        }
    }

    /**
     * Get the space between the repetitions.
     * @return the space between repetition value
     */
    @SuppressWarnings("unused")
    public float getSpaceBetweenRepetitions() {
        return this.mSpaceBetween;
    }


    // ***************************************************************************************
    // Listeners and Interfaces

    /**
     * Define the draw listener interface
     */
    @SuppressWarnings("unused")
    public interface OnDrawRepetitionListener {

        /**
         * Called before draw the repetition.
         * @param info the feature info
         */
        void onDrawRepetition(RepetitionInfo info);

    }

    /**
     * Set the draw listener to call.
     * @param listener the linked method to call
     */
    @SuppressWarnings("unused")
    public void setOnDrawRepetitionListener(OnDrawRepetitionListener listener) {
        this.mOnDrawListener = listener;
    }


    // ***************************************************************************************
    // Drawing info class

    /**
     * This is a structure to hold the feature information before draw it
     */
    @SuppressWarnings("unused")
    public class RepetitionInfo {

        // ***************************************************************************************
        // Properties

        private float[] mGenericPoint;

        public ScRepetitions source;
        public int repetition;

        public float distance;
        public float angle;
        public float tangent;
        public float width;
        public int color;
        public ScFeature.Positions position;
        private boolean isVisible;

        public float[] offset;
        public float[] point;
        public float[] scale;

        // ***************************************************************************************
        // Constructor

        public RepetitionInfo() {
            this.mGenericPoint = new float[2];
            this.offset = new float[2];
            this.point = new float[2];
            this.scale = new float[2];
        }

        // ***************************************************************************************
        // Public methods

        public void reset(ScRepetitions feature, int contour, int repetition) {
            // Holder
            float distance = feature.getDistance(repetition);
            float tangent = feature.getPointAndAngle(distance, this.mGenericPoint);

            // Find the center as the point on path
            this.point[0] = this.mGenericPoint[0];
            this.point[1] = this.mGenericPoint[1];

            // Reset the offset and the scale
            this.offset[0] = 0.0f;
            this.offset[1] = 0.0f;
            this.scale[0] = 1.0f;
            this.scale[1] = 1.0f;

            // Reset the drawing info
            this.source = feature;
            this.repetition = repetition;
            this.distance = distance;
            this.width = feature.getWidth(distance);
            this.tangent = tangent;
            this.angle = 0.0f;
            this.position = feature.getPosition();
            this.color = feature.getGradientColor(distance);
            this.isVisible = !feature.isOverLimits(repetition) && feature.getVisible();
        }

    }


}
