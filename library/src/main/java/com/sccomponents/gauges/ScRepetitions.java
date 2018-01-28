package com.sccomponents.gauges;

import android.graphics.Canvas;
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

    private DrawingInfo mGenericInfo;


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
        this.mGenericInfo = new DrawingInfo();
    }


    // ***************************************************************************************
    // Methods to override

    /**
     * Prepare the info object to send before drawing.
     * Need to override this method if you want have a custom info.
     * @param contour       the current contour
     * @param repetition    the current repetition
     * @return              the drawing info
     * @hide
     */
    @SuppressWarnings("unused")
    protected DrawingInfo setDrawingInfo(int contour, int repetition) {
        // Reset and Return
        this.mGenericInfo.reset(this, contour, repetition);
        return this.mGenericInfo;
    }

    /**
     * The draw method to override in the inherited classes.
     * @param canvas where draw
     * @hide
     */
    @SuppressWarnings("unused")
    protected abstract void onDraw(Canvas canvas, DrawingInfo info);


    // ***************************************************************************************
    // Private methods

    /**
     * Calculate the number of repetition considering the space between them.
     * @return the repetitions
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
     * Override the super method to draw all the repetitions too.
     * @param canvas where to draw
     * @param contour the current contour index
     * @hide
     */
    @Override
    protected void drawContour(Canvas canvas, int contour) {
        // Check for auto-calculate the repetitions number
        if (this.mSpaceBetween > 0) {
            float[] repetitions = this.calculateRepetitions();
            this.mRepetitions = repetitions.length;
        }

        // Cycle all repetition
        for (int repetition = 1; repetition <= this.mRepetitions; repetition++) {
            // Prepare the info objects
            DrawingInfo info = this.setDrawingInfo(contour, repetition);
            this.draw(canvas, info);
        }
    }


    // ***************************************************************************************
    // Public and static methods

    /**
     * Draw method
     * @param canvas where to draw
     */
    @Override
    protected void onDraw(Canvas canvas, ScFeature.DrawingInfo info) {
        // Draw the notch
        this.onDraw(canvas, (ScRepetitions.DrawingInfo) info);
    }

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
     * If true the last repetition distance from path start will be equal to the path length.
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
     * If true the last repetition distance from path start will be equal to the path length.
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
    // Drawing info class

    /**
     * This is a structure to hold the feature information before draw it
     */
    @SuppressWarnings("unused")
    public class DrawingInfo extends ScFeature.DrawingInfo {

        // ***************************************************************************************
        // Properties

        public ScRepetitions source = null;
        private float[] mGenericPoint;
        public int repetition = 0;
        public float distance = 0.0f;


        // Constructor
        public DrawingInfo() {
            this.mGenericPoint = new float[2];
        }


        // ***************************************************************************************
        // Public methods

        public void reset(ScRepetitions feature, int contour, int repetition) {
            // Super
            super.reset(feature, contour);

            // Holder
            this.source = feature;

            float distance = feature.getDistance(repetition);
            float angle = feature.getAngle(distance);
            int color = feature.getGradientColor(distance);
            float width = feature.getWidth(distance);
            boolean isOverLimits = feature.isOverLimits(repetition);

            // Find the center as the point on path
            feature.getPoint(distance, this.mGenericPoint);
            this.centerX = this.mGenericPoint[0];
            this.centerY = this.mGenericPoint[1];

            // Set the drawing info
            this.repetition = repetition;
            this.distance = distance;

            this.width = width;
            this.angle = angle;
            this.color = color;

            this.isVisible = !isOverLimits;
        }

    }


}
