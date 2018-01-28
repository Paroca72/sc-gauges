package com.sccomponents.gauges;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

import java.util.Arrays;


/**
 * Create a series of notches that follow a path.

 * @author Samuele Carassai
 * @version 3.0.0
 * @since 2016-05-30
 */
public class ScNotches extends ScRepetitions {

    // ***************************************************************************************
    // Enumerators

    /**
     * Define the types of notches can be draw
     */
    @SuppressWarnings("unused")
    public enum NotchTypes {
        LINE,
        CIRCLE,
        CIRCLE_FILLED,
        SQUARE,
        SQUARE_FILLED,
    }

    /**
     * The mode to calculate the current length.
     */
    @SuppressWarnings("unuse")
    public enum LengthsMode {
        ROUGH,
        SMOOTH
    }


    // ***************************************************************************************
    // Private and protected variables

    private float[] mLengths;
    private LengthsMode mLengthsMode;
    private NotchTypes mType;
    private Positions mEdges;

    private float[] mFirstPoint;
    private float[] mSecondPoint;
    private ScNotches.DrawingInfo mGenericInfo;
    private OnCustomDrawListener mOnCustomDrawListener;


    // ***************************************************************************************
    // Constructor

    @SuppressWarnings("unused")
    public ScNotches(Path path) {
        // Super
        super(path);

        // Init
        this.mLengths = new float[] { 0.0f };
        this.mLengthsMode = LengthsMode.SMOOTH;
        this.mType = NotchTypes.LINE;
        this.mEdges = Positions.MIDDLE;

        this.mFirstPoint = new float[2];
        this.mSecondPoint = new float[2];

        this.mGenericInfo = new ScNotches.DrawingInfo();
    }


    // ***************************************************************************************
    // Private methods

    /**
     * Draw a line.
     * @param canvas the canvas to draw
     * @param info   the notch info
     */
    private void drawLine(Canvas canvas, ScNotches.DrawingInfo info) {
        // Adjust the first point
        this.mFirstPoint[1] -= info.length / 2;

        // Find the second point
        this.clonePoint(this.mFirstPoint, this.mSecondPoint);
        this.mSecondPoint[1] += info.length;

        // Draw the line from the first to second point
        canvas.drawLine(
                this.mFirstPoint[0], this.mFirstPoint[1],
                this.mSecondPoint[0], this.mSecondPoint[1],
                this.getPainter()
        );
    }

    /**
     * Draw a square.
     * @param canvas the canvas to draw
     * @param info   the notch info
     */
    private void drawSquare(Canvas canvas, ScNotches.DrawingInfo info) {
        // Holder
        float half = info.length / 2;
        float left = this.mFirstPoint[0] - half;
        float top = this.mFirstPoint[1] - half;
        float right = this.mFirstPoint[0] + half;
        float bottom = this.mFirstPoint[1] + half;

        // Draw
        canvas.drawRect(left, top, right, bottom, this.getPainter());
    }

    /**
     * Draw a circle.
     * @param canvas the canvas to draw
     * @param info   the notch info
     */
    private void drawCircle(Canvas canvas, ScNotches.DrawingInfo info) {
        canvas.drawCircle(
                this.mFirstPoint[0],
                this.mFirstPoint[1],
                info.length / 2,
                this.getPainter()
        );
    }

    /**
     * Adjust the point based on the edges management
     * @param point the point to adjust
     * @param distance the distance of the point from the path start
     */
    private void adjustPointByEdges(float[] point, float distance) {
        // Check for domain
        if (this.mEdges == Positions.MIDDLE)
            return ;

        // If in the middle skip
        float middle = this.getMeasure().getLength() / 2;
        if (distance != middle) {
            // Holders
            float multiplier = 0.0f;
            float halfWidth = this.getPainter().getStrokeWidth() / 2;

            // If the distance is before the middle path calculate the modifier respect at
            // the first middle else on the second.
            if (distance < middle)
                multiplier = (middle - distance) / middle;
            if (distance > middle)
                multiplier = -(distance - middle) / middle;

            // Calculate the increment and add to the x point coordinate
            float increment = halfWidth * multiplier;
            switch (this.mEdges) {
                case INSIDE: point[0] += increment; break;
                case OUTSIDE: point[0] -= increment; break;
            }
        }
    }

    /**
     * Draw a single notch.
     * @param canvas where to draw
     * @param info   the notch info
     */
    private void drawNotch(Canvas canvas, ScNotches.DrawingInfo info) {
        // Apply the current info settings to the painter
        boolean isFilled =
                info.type == NotchTypes.CIRCLE_FILLED ||
                info.type == NotchTypes.SQUARE_FILLED;

        Paint painter = this.getPainter();
        painter.setStyle(
                 isFilled ? Paint.Style.FILL_AND_STROKE : Paint.Style.STROKE);

        // Get the point by the distance
        this.getPoint(info.distance, this.mFirstPoint);
        this.adjustPointByEdges(this.mFirstPoint, info.distance);

        // Get and fix the y point position
        switch (this.getPosition()) {
            case INSIDE: this.mFirstPoint[1] += info.length / 2; break;
            case OUTSIDE: this.mFirstPoint[1] -= info.length / 2; break;
        }

        // Custom draw
        if (this.mOnCustomDrawListener != null) {
            this.mOnCustomDrawListener.onCustomDraw(canvas, info);
            return;
        }

        // Draw the notches by the case
        switch (info.type) {
            // Draw a line
            case LINE:
                this.drawLine(canvas, info);
                break;

            // Draw a circle
            case CIRCLE:
            case CIRCLE_FILLED:
                this.drawCircle(canvas, info);
                break;

            // Draw a square
            case SQUARE:
            case SQUARE_FILLED:
                this.drawSquare(canvas, info);
                break;
        }
    }


    // ***************************************************************************************
    // Overrides

    /**
     * Prepare the info object to send before drawing.
     * Need to override this method if you want have a custom info.
     * @param contour       the current contour
     * @param repetition    the current repetition
     * @return              the drawing info
     * @hide
     */
    @Override
    protected ScNotches.DrawingInfo setDrawingInfo(int contour, int repetition) {
        // Reset and fill with the base values
        this.mGenericInfo.reset(this, contour, repetition);

        // Fill the missing data
        this.mGenericInfo.source = this;
        this.mGenericInfo.length = this.getLength(mGenericInfo.distance);
        this.mGenericInfo.type = this.mType;

        // Return
        return this.mGenericInfo;
    }

    /**
     * Draw method
     * @param canvas where to draw
     * @hide
     */
    @Override
    protected void onDraw(Canvas canvas, ScRepetitions.DrawingInfo info) {
        // Draw the notch
        this.drawNotch(canvas, (ScNotches.DrawingInfo) info);
    }


    // ***************************************************************************************
    // Public Methods

    /**
     * Get the notches length gived a distance from the path start.
     * @param distance  the distance
     * @return          the length
     */
    public float getLength(float distance) {
        return this.getValue(
                this.mLengths,
                distance / this.getMeasure().getLength(),
                this.mLengthsMode == LengthsMode.SMOOTH,
                0.0f
        );
    }

    /**
     * Implement a copy of this object
     * @param destination the destination object
     */
    @SuppressWarnings("unused")
    public void copy(ScNotches destination) {
        // Super
        super.copy(destination);

        // This object
        if (this.mLengths != null)
            destination.setLengths(this.mLengths.clone());

        destination.setLengthsMode(this.mLengthsMode);
        destination.setType(this.mType);
        destination.setEdges(this.mEdges);
    }

    /**
     * Implement a copy of this object
     * @param destination the destination object
     * @hide
     */
    @SuppressWarnings("unused")
    @Override
    public void copy(ScRepetitions destination) {
        if (destination instanceof ScNotches)
            this.copy((ScNotches) destination);
        else
            super.copy(destination);
    }


    // ***************************************************************************************
    // Public properties

    /**
     * Set the notches count.
     * @param value the notches count
     * @deprecated  use setRepetition instead.
     */
    @SuppressWarnings("unused")
    @Deprecated
    public void setCount(int value) {
        this.setRepetitions(value);
    }

    /**
     * Get the notches count.
     * @return      the notches count
     * @deprecated  use getRepetition instead.
     */
    @SuppressWarnings("unused")
    @Deprecated
    public float getCount() {
        return this.getRepetitions();
    }


    /**
     * Set the notches length.
     * @param values the notches length
     */
    @SuppressWarnings("unused")
    public void setLengths(float... values) {
        if (!Arrays.equals(this.mLengths, values)) {
            this.mLengths = values;
            this.onPropertyChange("lengths", values);
        }
    }

    /**
     * @return the notches length
     */
    @SuppressWarnings("unused")
    public float[] getLengths() {
        return this.mLengths;
    }


    /**
     * Set the lengths calculation mode.
     * You can have two way for calculate the lengths of the path: SMOOTH or ROUGH.
     * @param value the new length calculation mode
     */
    @SuppressWarnings("unused")
    public void setLengthsMode(LengthsMode value) {
        if (this.mLengthsMode != value) {
            this.mLengthsMode = value;
            this.onPropertyChange("lengthMode", value);
        }
    }

    /**
     * Get the lengths calculation mode.
     * @return the length calculation mode
     */
    @SuppressWarnings("unused")
    public LengthsMode getLengthsMode() {
        return this.mLengthsMode;
    }


    /**
     * Set the notches type.
     * @param value the notches type
     */
    @SuppressWarnings("unused")
    public void setType(NotchTypes value) {
        if (this.mType != value) {
            this.mType = value;
            this.onPropertyChange("type", value);
        }
    }

    /**
     * Get the notches type.
     * @return the notches type
     */
    @SuppressWarnings("unused")
    public NotchTypes getType() {
        return this.mType;
    }


    /**
     * Set the edges type.
     * Can be: INSIDE, MIDDLE and OUTSIDE.
     * @param value the edges type
     */
    @SuppressWarnings("unused")
    public void setEdges(Positions value) {
        if (this.mEdges != value) {
            this.mEdges = value;
            this.onPropertyChange("edges", value);
        }
    }

    /**
     * Get the edges type.
     * @return the edges type
     */
    @SuppressWarnings("unused")
    public Positions getEdges() { return this.mEdges; }


    // ***************************************************************************************
    // Public classes and methods

    /**
     * This is a structure to hold the feature information before draw it
     */
    @SuppressWarnings("unused")
    public class DrawingInfo extends ScRepetitions.DrawingInfo {

        public ScNotches source = null;
        float length = 0.0f;
        NotchTypes type = NotchTypes.LINE;

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
