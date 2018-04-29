package com.sccomponents.gauges.library;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import java.util.Arrays;


/**
 * Create a series of notches that follow a path.
 * <p>
 * You can choose between some predefined shape (line, rectangle and oval) or by drawing a
 * passed bitmap.
 * All the notch are customizable before drawing it using the proper event.
 *
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
        BITMAP,
        LINE,
        OVAL,
        OVAL_FILLED,
        RECTANGLE,
        RECTANGLE_FILLED,
    }

    /**
     * The mode to calculate the current height.
     */
    @SuppressWarnings("unuse")
    public enum HeightsMode {
        ROUGH,
        SMOOTH
    }


    // ***************************************************************************************
    // Private and protected variables

    private float[] mHeights;
    private HeightsMode mHeightsMode;
    private NotchTypes mType;
    private Bitmap mBitmap;

    private float[] mFirstPoint;
    private float[] mSecondPoint;
    private RectF mGenericRect;
    private NotchInfo mRepetitionInfo;


    // ***************************************************************************************
    // Constructor

    @SuppressWarnings("unused")
    public ScNotches() {
        // Super
        super();

        // Init
        this.mHeights = new float[]{0.0f};
        this.mHeightsMode = HeightsMode.SMOOTH;
        this.mType = NotchTypes.LINE;
        this.mRepetitionInfo = new NotchInfo();

        this.mFirstPoint = new float[2];
        this.mSecondPoint = new float[2];
        this.mGenericRect = new RectF();
    }


    // ***************************************************************************************
    // Private methods

    /**
     * Draw on canvas a bitmap centered in the passed point.
     * @param canvas where to draw
     * @param info   the pointer info
     */
    private void drawBitmap(Canvas canvas, NotchInfo info) {
        // Check for empty values
        if (info.bitmap == null)
            return;

        // Adjust the first point
        this.mFirstPoint[0] -= info.bitmap.getWidth() / 2;
        this.mFirstPoint[1] -= info.bitmap.getHeight() / 2;

        // Scale the original bitmap
        Bitmap scaled = info.bitmap;
        if (info.width != 0 && info.height != 0)
            scaled = Bitmap.createScaledBitmap(
                    info.bitmap,
                    (int) info.width,
                    (int) info.height,
                    false);

        // Print the bitmap centered respect the point
        canvas.drawBitmap(scaled, this.mFirstPoint[0], this.mFirstPoint[1], null);
    }

    /**
     * Draw a line.
     * @param canvas the canvas to draw
     * @param info   the notch info
     */
    protected void drawLine(Canvas canvas, NotchInfo info, Paint paint) {
        // Set the stroke width
        paint.setStrokeWidth(info.width);

        // Adjust the first point
        this.mFirstPoint[1] -= info.height / 2;

        // Find the second point
        this.clonePoint(this.mFirstPoint, this.mSecondPoint);
        this.mSecondPoint[1] += info.height;

        // Draw the line from the first to second point
        canvas.drawLine(
                this.mFirstPoint[0], this.mFirstPoint[1],
                this.mSecondPoint[0], this.mSecondPoint[1],
                this.getPainter()
        );
    }

    /**
     * Draw a rectangle.
     * @param canvas the canvas to draw
     * @param info   the notch info
     */
    protected void drawRectangle(Canvas canvas, NotchInfo info, Paint paint) {
        // Holder
        float halfWidth = info.width / 2;
        float left = this.mFirstPoint[0] - halfWidth;
        float right = this.mFirstPoint[0] + halfWidth;

        float halfHeight = info.height / 2;
        float top = this.mFirstPoint[1] - halfHeight;
        float bottom = this.mFirstPoint[1] + halfHeight;

        // Draw
        canvas.drawRect(left, top, right, bottom, paint);
    }

    /**
     * Draw a oval.
     * @param canvas the canvas to draw
     * @param info   the notch info
     */
    protected void drawOval(Canvas canvas, NotchInfo info, Paint paint) {
        // Holder
        float halfWidth = info.width / 2;
        float left = this.mFirstPoint[0] - halfWidth;
        float right = this.mFirstPoint[0] + halfWidth;

        float halfHeight = info.height / 2;
        float top = this.mFirstPoint[1] - halfHeight;
        float bottom = this.mFirstPoint[1] + halfHeight;

        // Draw
        this.mGenericRect.set(left, top, right, bottom);
        canvas.drawOval(this.mGenericRect, paint);
    }

    /**
     * Adjust the point based on the edges management
     * @param point    the point to adjust
     * @param distance the distance of the point from the path start
     */
    private void adjustPointByEdges(float[] point, float distance) {
        // Check for domain
        if (this.getEdges() == Positions.MIDDLE)
            return;

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
            switch (this.getEdges()) {
                case INSIDE:
                    point[0] += increment;
                    break;
                case OUTSIDE:
                    point[0] -= increment;
                    break;
            }
        }
    }

    /**
     * Draw a single notch.
     * @param canvas where to draw
     * @param info   the notch info
     */
    private void drawNotch(Canvas canvas, NotchInfo info) {
        // Apply the current info settings to the painter
        boolean isFilled =
                info.type == NotchTypes.OVAL_FILLED ||
                        info.type == NotchTypes.RECTANGLE_FILLED;

        Paint painter = this.getPainter();
        painter.setStyle(
                isFilled ? Paint.Style.FILL : Paint.Style.STROKE);

        // Get the point by the distance
        this.getPoint(info.distance, this.mFirstPoint);
        this.adjustPointByEdges(this.mFirstPoint, info.distance);

        // Get and fix the y point position
        switch (this.getPosition()) {
            case INSIDE:
                this.mFirstPoint[1] += info.height / 2;
                break;
            case OUTSIDE:
                this.mFirstPoint[1] -= info.height / 2;
                break;
        }

        // Draw the notches by the case
        switch (info.type) {
            // Draw a bitmap
            case BITMAP:
                this.drawBitmap(canvas, info);
                break;

            // Draw a line
            case LINE:
                this.drawLine(canvas, info, this.getPainter());
                break;

            // Draw a circle
            case OVAL:
            case OVAL_FILLED:
                this.drawOval(canvas, info, this.getPainter());
                break;

            // Draw a square
            case RECTANGLE:
            case RECTANGLE_FILLED:
                this.drawRectangle(canvas, info, this.getPainter());
                break;
        }
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
    protected NotchInfo getRepetitionInfo(int contour, int repetition) {
        this.mRepetitionInfo.reset(this, contour, repetition);
        return this.mRepetitionInfo;
    }

    /**
     * Draw method
     * @param canvas where to draw
     * @hide
     */
    @Override
    protected void onDraw(Canvas canvas, RepetitionInfo info) {
        // Draw the notch
        this.drawNotch(canvas, (NotchInfo) info);
    }


    // ***************************************************************************************
    // Public Methods

    /**
     * Get the notches height given a distance from the path start.
     * @param distance  the distance
     * @return          the height
     */
    public float getHeight(float distance) {
        return this.getValue(
                this.mHeights,
                distance / this.getMeasure().getLength(),
                this.mHeightsMode == HeightsMode.SMOOTH,
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
        if (this.mHeights != null)
            destination.setHeights(this.mHeights.clone());

        destination.setHeightsMode(this.mHeightsMode);
        destination.setType(this.mType);
        destination.setBitmap(this.mBitmap);
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
     * Set the current bitmap.
     * @param value the new bitmap
     */
    @SuppressWarnings("unused")
    public void setBitmap(Bitmap value) {
        this.mBitmap = value;
        this.onPropertyChange("bitmap", value);
    }

    /**
     * Get the current bitmap
     * @return the current bitmap
     */
    @SuppressWarnings("unused")
    public Bitmap getBitmap() {
        return this.mBitmap;
    }


    /**
     * Set the notches count.
     * @param value the notches count
     * @deprecated use setRepetition instead.
     */
    @SuppressWarnings("unused")
    @Deprecated
    public void setCount(int value) {
        this.setRepetitions(value);
    }

    /**
     * Get the notches count.
     * @return the notches count
     * @deprecated use getRepetition instead.
     */
    @SuppressWarnings("unused")
    @Deprecated
    public float getCount() {
        return this.getRepetitions();
    }


    /**
     * Set the notches height.
     * @param values the notches height
     */
    @SuppressWarnings("unused")
    public void setHeights(float... values) {
        if (!Arrays.equals(this.mHeights, values)) {
            this.mHeights = values;
            this.onPropertyChange("lengths", values);
        }
    }

    /**
     * @return the notches height
     */
    @SuppressWarnings("unused")
    public float[] getHeights() {
        return this.mHeights;
    }


    /**
     * Set the lengths calculation mode.
     * You can have two way for calculate the lengths of the path: SMOOTH or ROUGH.
     * @param value the new height calculation mode
     */
    @SuppressWarnings("unused")
    public void setHeightsMode(HeightsMode value) {
        if (this.mHeightsMode != value) {
            this.mHeightsMode = value;
            this.onPropertyChange("lengthMode", value);
        }
    }

    /**
     * Get the lengths calculation mode.
     * @return the height calculation mode
     */
    @SuppressWarnings("unused")
    public HeightsMode getHeightsMode() {
        return this.mHeightsMode;
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


    // ***************************************************************************************
    // Public classes and methods

    /**
     * This is a structure to hold the feature information before draw it
     */
    @SuppressWarnings("unused")
    public class NotchInfo extends RepetitionInfo {

        // ***************************************************************************************
        // Properties

        public ScNotches source;
        public float height;
        public NotchTypes type;
        public Bitmap bitmap;

        // ***************************************************************************************
        // Constructor

        public NotchInfo() {
            this.type = NotchTypes.LINE;
        }

        // ***************************************************************************************
        // Public methods

        public void reset(ScNotches feature, int contour, int repetition) {
            // Super
            super.reset(feature, contour, repetition);

            // Reset
            this.source = feature;
            this.height = feature.getHeight(this.distance);
            this.type = feature.mType;
            this.bitmap = feature.getBitmap();
        }

    }

}
