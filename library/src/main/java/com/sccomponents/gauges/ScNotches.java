package com.sccomponents.gauges;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;


/**
 * Create a series of notches that follow a path
 *
 * @author Samuele Carassai
 * @version 2.0.1
 * @since 2016-05-30
 */
public class ScNotches extends ScFeature {

    // ***************************************************************************************
    // Enumerators

    /**
     * Define the types of notches can be draw
     */
    @SuppressWarnings("unused")
    public enum NotchTypes {
        LINE,
        CIRCLE,
        CIRCLE_FILLED
    }

    /**
     * Define the notches position respect path
     */
    @SuppressWarnings("unused")
    public enum NotchPositions {
        INSIDE,
        MIDDLE,
        OUTSIDE
    }


    // ***************************************************************************************
    // Private and protected variables

    private Paint mPaintClone;

    private int mNotchesCount;
    private float mNotchesLen;

    private NotchTypes mNotchType;
    private NotchPositions mNotchPosition;

    private boolean mDividePathInContours;
    private OnDrawListener mOnDrawListener;


    // ***************************************************************************************
    // Constructor

    @SuppressWarnings("unused")
    public ScNotches(Path path) {
        // Super
        super(path);

        // Init
        this.mNotchesCount = 0;
        this.mNotchesLen = 0.0f;
        this.mNotchType = NotchTypes.LINE;
        this.mNotchPosition = NotchPositions.MIDDLE;
        this.mDividePathInContours = true;

        this.mPaintClone = new Paint(this.mPaint);
    }

    // ***************************************************************************************
    // Draw methods
    //
    // ATTENTION!
    // In these methods I used to instantiate new objects and is preferable NOT do it for improve
    // the performance of the component drawing.
    // In case of low performance the first solution must be to move the new object creation in
    // the global scope for do it once.
    //

    /**
     * Draw a line.
     * I could use the Point class for a better coding but is always not instantiate classes
     * inside the draw method for a speed improvement.
     *
     * @param canvas the canvas to draw
     * @param info   the notch info
     */
    private void drawLine(Canvas canvas, NotchInfo info) {
        // Check the point
        if (info.point == null) return;

        // Global offset
        float globalOffset = info.offset;
        if (info.align == NotchPositions.MIDDLE) globalOffset -= info.length / 2;
        if (info.align == NotchPositions.OUTSIDE) info.angle += 180;

        // Find the start and end point to draw the line
        PointF first = new PointF(info.point.x, info.point.y);
        ScNotches.translatePoint(first, globalOffset, info.angle);

        PointF second = new PointF(first.x, first.y);
        ScNotches.translatePoint(second, info.length, info.angle);

        // Draw the line if the canvas is not null
        if (canvas != null) {
            canvas.drawLine(first.x, first.y, second.x, second.y, this.mPaintClone);
        }
    }

    /**
     * Draw a circle.
     * I could use the Point class for a better coding but is always not instantiate classes
     * inside the draw method for a speed improvement.
     *
     * @param canvas the canvas to draw
     * @param info   the notch info
     */
    private void drawCircle(Canvas canvas, NotchInfo info) {
        // Check the point
        if (info.point == null) return;

        // Global offset
        float radius = info.length / 2;
        float globalOffset = info.offset;

        if (info.align == NotchPositions.INSIDE) globalOffset += radius;
        if (info.align == NotchPositions.OUTSIDE) globalOffset -= radius;

        // Apply the point offset
        ScNotches.translatePoint(info.point, globalOffset, info.angle);

        // Draw the circle if the canvas is not null
        if (canvas != null) {
            canvas.drawCircle(info.point.x, info.point.y, radius, this.mPaintClone);
        }
    }

    /**
     * Draw a single notch.
     *
     * @param canvas where to draw
     * @param info   the notch info
     */
    private void drawNotch(Canvas canvas, NotchInfo info) {
        // Apply the current info settings to the painter
        this.mPaintClone.setStrokeWidth(info.size);
        this.mPaintClone.setStyle(
                info.type == NotchTypes.CIRCLE_FILLED ? Paint.Style.FILL_AND_STROKE : Paint.Style.STROKE);
        this.mPaintClone.setColor(info.color);

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
        }
    }

    /**
     * Draw all notches on the path.
     *
     * @param canvas  where to draw
     * @param path    the current contour path
     * @param contour the contour index
     */
    private void drawNotches(Canvas canvas, Path path, int contour) {
        // Create the path measure and calculate the step
        ScPathMeasure measure = new ScPathMeasure(path, false);
        float step = measure.getLength() / this.mNotchesCount;

        // Define the notch info.
        // I use to create the object here for avoid to create they n times after.
        // Always inside the draw method is better not instantiate too much classes.
        NotchInfo info = new NotchInfo();
        info.source = this;

        // Convert the limits from percentages in distances
        float startLimit = (measure.getLength() * this.mStartPercentage) / 100.0f;
        float endLimit = (measure.getLength() * this.mEndPercentage) / 100.0f;

        // If the path is not closed add one notch to the beginning of path.
        int count = this.mNotchesCount + (measure.isClosed() ? 0 : 1);
        if (startLimit == 0 && endLimit == 0) count = 0;

        // Cycle all notches.
        for (int index = 0; index < count; index++) {
            // Get the point on the path
            float distance = index * step;
            float[] point = measure.getPosTan(distance);

            // Define the notch info structure and fill with the local settings
            info.point = null;
            info.size = this.mPaint.getStrokeWidth();
            info.length = this.mNotchesLen;
            info.offset = 0.0f;
            info.angle = 0.0f;
            info.type = this.mNotchType;
            info.align = this.mNotchPosition;
            info.contour = contour;
            info.index = index;
            info.distance = distance;
            info.visible = (this.mStartPercentage == 0.0f || info.distance >= startLimit) &&
                    (this.mEndPercentage == 100.0f || info.distance <= endLimit);
            info.color = this.getGradientColor(distance, measure.getLength());

            // Check if the point exists
            if (point != null) {
                info.point = ScNotches.toPoint(point);
                info.angle = (float) Math.toDegrees(point[3]) + 90.0f;
            }

            // Check if have a liked listener
            if (this.mOnDrawListener != null) {
                this.mOnDrawListener.onBeforeDrawNotch(info);
            }

            // Draw the single notch if visible
            if (info.visible) {
                this.mPaintClone.set(this.mPaint);
                this.drawNotch(canvas, info);
            }
        }
    }

    /**
     * Draw all contours
     *
     * @param canvas where to draw
     */
    private void drawContours(Canvas canvas) {
        // Holder
        Path[] contours =
                this.mDividePathInContours ? this.mPathMeasure.getPaths() : new Path[]{this.mPath};

        // Cycle all contours
        for (int index = 0; index < contours.length; index++) {
            // Draw the notches on the path
            this.drawNotches(canvas, contours[index], index);
        }
    }


    // ***************************************************************************************
    // Overrides

    /**
     * Draw method
     *
     * @param canvas where to draw
     */
    @Override
    protected void onDraw(Canvas canvas) {
        // Check for empty value
        if (this.mNotchesCount == 0 || this.mPath == null)
            return;

        // Draw all notches
        this.drawContours(canvas);
    }


    // ***************************************************************************************
    // Public methods

    /**
     * Return the point on path by the notch index.
     * Note that this method return always the point on the first contour of the path.
     *
     * @return the point on the path
     */
    @SuppressWarnings("unused")
    public PointF getPointOnPath(int index) {
        // Check the index limit
        if (index < 0 || index > this.mNotchesCount) return new PointF();

        // Get the path len and the step
        float step = this.mPathLength / this.mNotchesCount;

        // Find the points of path
        float[] point = this.mPathMeasure.getPosTan(step * index);
        return point == null ? new PointF() : new PointF(point[0], point[1]);
    }


    // ***************************************************************************************
    // Public classes and methods

    /**
     * This is a structure to hold the notch information before draw it.
     * Note that the "point" represent the point from will start to draw.
     * Note that the "distance" is the distance from the oath starting.
     * Note that the "angle" is in degrees.
     */
    @SuppressWarnings("unused")
    public class NotchInfo {

        public ScNotches source;
        public PointF point;
        public float size;
        public float length;
        public int color;
        public int contour;
        public int index;
        public float angle;
        public float offset;
        public float distance;
        public boolean visible;
        public NotchTypes type;
        public NotchPositions align;

    }

    /**
     * Round the value near the closed notch.
     *
     * @param value the value to round
     * @return a rounded to notch value
     */
    @SuppressWarnings("unused")
    public float snapToNotches(float value) {
        // Check for empty values
        if (this.mNotchesCount == 0) return value;

        // Calc the delta angle and round at notches value
        float deltaAngle = this.mPathLength / this.mNotchesCount;
        return Math.round(value / deltaAngle) * deltaAngle;
    }

    /**
     * By default the class will draw the n notches on each contours that compose the current
     * path. If settle on false the class will consider the path as a unique path.
     *
     * @param value default true
     */
    @SuppressWarnings("unused")
    public void setDividePathInContours(boolean value) {
        this.mDividePathInContours = value;
    }


    // ***************************************************************************************
    // Public properties

    /**
     * Return the notches count.
     *
     * @return the notches count
     */
    @SuppressWarnings("unused")
    public float getCount() {
        return this.mNotchesCount;
    }

    /**
     * Set the notches count.
     *
     * @param value the notches count
     */
    @SuppressWarnings("unused")
    public void setCount(int value) {
        if (value < 0) value = 0;
        this.mNotchesCount = value;
    }

    /**
     * Return the notches length.
     *
     * @return the notches count
     */
    @SuppressWarnings("unused")
    public float getLength() {
        return this.mNotchesLen;
    }

    /**
     * Set the notches length.
     *
     * @param value the notches count
     */
    @SuppressWarnings("unused")
    public void setLength(float value) {
        if (value < 0) value = 0;
        this.mNotchesLen = value;
    }

    /**
     * Return the notches type.
     *
     * @return the notches type
     */
    @SuppressWarnings("unused")
    public NotchTypes getType() {
        return this.mNotchType;
    }

    /**
     * Set the notches type.
     *
     * @param value the notches type
     */
    @SuppressWarnings("unused")
    public void setType(NotchTypes value) {
        this.mNotchType = value;
    }

    /**
     * Return the notches alignment respect the path.
     *
     * @return the notches alignment
     */
    @SuppressWarnings("unused")
    public NotchPositions getPosition() {
        return this.mNotchPosition;
    }

    /**
     * Set the notches alignment respect the path.
     *
     * @param value the notches alignment
     */
    @SuppressWarnings("unused")
    public void setPosition(NotchPositions value) {
        this.mNotchPosition = value;
    }


    // ***************************************************************************************
    // Listeners and Interfaces

    /**
     * Define the draw listener interface
     */
    @SuppressWarnings("unused")
    public interface OnDrawListener {

        /**
         * Called before draw the single notch.
         *
         * @param info the notch info
         */
        void onBeforeDrawNotch(NotchInfo info);

    }

    /**
     * Set the draw listener to call.
     *
     * @param listener the linked method to call
     */
    @SuppressWarnings("unused")
    public void setOnDrawListener(OnDrawListener listener) {
        this.mOnDrawListener = listener;
    }


}
