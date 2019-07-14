package com.sccomponents.gauges.library;

import android.graphics.Paint;

import java.text.DecimalFormat;
import java.util.Arrays;


/**
 * Draw a label on the path at certain distance from the path start.
 *
 * @author Samuele Carassai
 * @version 3.5.0
 * @since 2018-08-13
 */
public class ScLabeler extends ScWriter {

    // ***************************************************************************************
    // Constants


    /****************************************************************************************
     * Private variables
     */

    private float mDistance;
    private boolean mLinkedToProgress;
    private String mFormat;
    private String[] mTokens;

    private LabelInfo mRepetitionInfo;


    /****************************************************************************************
     * Constructor
     */

    @SuppressWarnings("unused")
    public ScLabeler() {
        // Super
        super();

        // Init
        super.setTokens("ITEM");
        super.setLastRepetitionOnPathEnd(false);

        this.mLinkedToProgress = true;
        this.mFormat = null;
        this.mRepetitionInfo = new LabelInfo();

        this.getPainter().setTextAlign(Paint.Align.CENTER);
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
    protected LabelInfo getRepetitionInfo(int contour, int repetition) {
        // Super
        this.mRepetitionInfo.reset(this, contour, repetition);
        return this.mRepetitionInfo;
    }

    /**
     * Implement a copy of this object
     * @param destination the destination object
     * @hide
     */
    @SuppressWarnings("unused")
    @Override
    public void copy(ScFeature destination) {
        if (destination instanceof ScLabeler)
            this.copy((ScLabeler) destination);
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
     * Return the string tokens.
     * @return the tokens list
     */
    @SuppressWarnings("unused")
    @Override
    public String[] getTokens() {
        return this.mTokens;
    }

    /**
     * Set the string tokens to draw on path.
     * @param values the tokens list
     */
    @Override
    public void setTokens(String... values) {
        if (!Arrays.equals(this.mTokens, values)) {
            this.mTokens = values;
            this.onPropertyChange("tokens", values);
        }
    }


    // ***************************************************************************************
    // Public methods

    /**
     * Implement a copy of this object
     * @param destination the destination object
     */
    @SuppressWarnings("unused")
    public void copy(ScLabeler destination) {
        // Super
        super.copy(destination);

        // Set
        if (this.mTokens != null)
            destination.setTokens(this.mTokens.clone());

        destination.setDistance(this.mDistance);
        destination.setFormat(this.mFormat);
        destination.setLinkedToProgress(this.mLinkedToProgress);
    }

    /**
     * Get a formatted value using the format patter defined in settings.
     * @param value the value to format
     * @return the formatted value
     */
    @SuppressWarnings({"unused", "WeakerAccess"})
    public String getFormattedNumber(float value) {
        // Check the pattern and format
        if (this.mFormat == null || this.mFormat.isEmpty())
            return Float.toString(value);

        // Else
        DecimalFormat formatter = new DecimalFormat(this.mFormat);
        return formatter.format(value);
    }


    // ***************************************************************************************
    // Public properties

    /**
     * Set the distance of the label in percentage respect to the path start.
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
     * Get the position of the label in percentage respect to the path start.
     * @return the position in percentage
     */
    @SuppressWarnings("unused")
    public float getDistance() {
        return this.mDistance;
    }


    /**
     * Set the text formatter pattern.
     * The format pattern will applied using the android Formatter.
     * @param value the format pattern
     */
    @SuppressWarnings({"unused", "WeakerAccess"})
    public void setFormat(String value) {
        // Store the value
        if (!this.equals(this.mFormat, value)) {
            this.mFormat = value;
            this.onPropertyChange("format", value);
        }
    }

    /**
     * Get the text formatter pattern.
     * The format pattern will applied using the android Formatter.
     * @return the format pattern
     */
    @SuppressWarnings("unused")
    public String getFormat() {
        return this.mFormat;
    }


    /**
     * Set linking status respect the progress bar.
     * If true when the progress will change this label position will change too and moved
     * at the same position of the progress end.
     * @param value the status
     */
    @SuppressWarnings({"unused", "WeakerAccess"})
    public void setLinkedToProgress(boolean value) {
        // Store the value
        if (this.mLinkedToProgress != value) {
            this.mLinkedToProgress = value;
            this.onPropertyChange("linked", value);
        }
    }

    /**
     * Get linking status respect the progress bar.
     * If true when the progress will change this label position will change too and moved
     * at the same position of the progress end.
     * @return the status
     */
    @SuppressWarnings({"unused", "WeakerAccess"})
    public boolean getLinkedToProgress() {
        return this.mLinkedToProgress;
    }


    // ***************************************************************************************
    // Public classes and methods

    /**
     * This is a structure to hold the feature information before draw it
     */
    @SuppressWarnings("unused")
    public class LabelInfo extends ScWriter.TokenInfo {

        // ***************************************************************************************
        // Properties

        private float[] mGenericPoint;
        public ScLabeler source;


        // ***************************************************************************************
        // Constructor

        @SuppressWarnings({"WeakerAccess"})
        public LabelInfo() {
            this.mGenericPoint = new float[2];
        }


        // ***************************************************************************************
        // Public methods

        public void reset(ScLabeler feature, int contour, int repetition) {
            // Super
            super.reset(feature, contour, repetition);

            // Holder
            float percentage = feature.getDistance();
            float distance = feature.getDistance(percentage);

            // Reset
            this.source = feature;
            this.distance = distance;

            this.tangent = feature.getPointAndAngle(distance, this.mGenericPoint) - 90;
            this.color = feature.getGradientColor(distance);
            this.text = feature.getString(feature.getTokens(), percentage / 100);

            // If linked to progress
            if (feature.getLinkedToProgress())
                this.text = feature.getFormattedNumber(percentage);

            // Find the center as the point on path
            this.point[0] = this.mGenericPoint[0];
            this.point[1] = this.mGenericPoint[1];
        }

    }

}
