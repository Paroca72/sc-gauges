package com.sccomponents.gauges.library;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * This class extends the ScFeature give to it the possibility to manage the repetitions.
 *
 * @author Samuele Carassai
 * @version 3.1.0
 * @since 2016-05-26
 */
public abstract class ScRepetitions extends ScFeature {

    // ***************************************************************************************
    // Private variable

    private int mRepetitions;
    private float mSpaceBetween;
    private boolean mSpaceBetweenAsPercentage;
    private float mRepetitionOffset;
    private boolean mLastRepetitionOnPathEnd;
    private RepetitionInfo mRepetitionInfo;
    private Positions mEdges;

    // Listener
    private OnDrawRepetitionListener mOnDrawListener;


    /****************************************************************************************
     * Constructor
     */

    @SuppressWarnings({"unused", "WeakerAccess"})
    public ScRepetitions() {
        // Init
        super();

        // Repetitions
        this.mRepetitions = 0;
        this.mSpaceBetween = 0.0f;
        this.mSpaceBetweenAsPercentage = true;
        this.mRepetitionOffset = 0;
        this.mLastRepetitionOnPathEnd = true;
        this.mEdges = Positions.MIDDLE;

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
     * return a float with one digits precision
     * @param value number
     * @return rounded float
     */
    private float toRoundedFloat(float value) {
        return new BigDecimal(value)
                .setScale(2, RoundingMode.FLOOR)
                .floatValue();
    }

    /**
     * Convert the space between always in pixel since could be treat as percentage.
     * This is always calculate in every cases: set repetitions ot set space between.
     * @return the pixels
     */
    private float getSpaceBetweenInPixels() {
        // Check for space between
        if (this.mSpaceBetween > 0.0f) {
            // Treat as percentage
            if (this.mSpaceBetweenAsPercentage)
                return (this.getMeasure().getLength() * this.mSpaceBetween) / 100.0f;

            // Treat as pixels
            return this.mSpaceBetween;
        }

        // Check the repetitions
        if (this.mRepetitions == 1)
            return this.getMeasure().getLength();

        if (this.mRepetitions > 1) {
            // Fix the repetition count.
            int repetitions = this.mRepetitions -
                    (this.mLastRepetitionOnPathEnd && !this.getMeasure().isClosed() ? 1: 0);

            // Calculate the percentage of the path length
            return this.getMeasure().getLength() / (float) repetitions;
        }

        // Not found
        return 0.0f;
    }

    /**
     * Draw a single repetition.
     * This method is implemented just for give the possibility to override it for some
     * future application.
     * @param canvas    where to draw
     * @param info      the current repetition info
     * @hide
     */
    @SuppressWarnings({"unused", "WeakerAccess"})
    protected void drawRepetition(Canvas canvas, RepetitionInfo info) {
        // Rotate, translate and scale
        canvas.rotate(info.tangent, info.point[0], info.point[1]);
        canvas.translate(info.offset[0], info.offset[1]);
        canvas.rotate(info.angle, info.point[0], info.point[1]);
        canvas.scale(info.scale[0], info.scale[1], info.point[0], info.point[1]);

        // Set the color
        Paint paint = this.getPainter();
        paint.setColor(info.color);

        // Draw
        this.onDraw(canvas, info);
    }

    /**
     * Draw all the repetitions
     * @param canvas    where to draw
     * @param contour   the current contour index
     * @hide
     */
    @SuppressWarnings({"unused", "WeakerAccess"})
    protected void drawRepetitions(Canvas canvas, int contour) {
        // Holders
        float spaceBetween = this.getSpaceBetweenInPixels();
        float len = this.getMeasure().getLength();
        float distance = this.mRepetitionOffset;
        int repetition = 1;

        // All the path length
        while (spaceBetween > 0 &&
                this.toRoundedFloat(distance) <= this.toRoundedFloat(len)) {
            // Get the drawing info
            RepetitionInfo info = this.getRepetitionInfo(contour, repetition);

            // Call the base listener
            if (this.mOnDrawListener != null)
                this.mOnDrawListener.onDrawRepetition(this, info);

            // Check for visibility
            if (info.visible) {
                // Call the draw for the single repetition
                canvas.save();
                this.drawRepetition(canvas, info);
                canvas.restore();
            }

            // Increase all triggers
            distance += spaceBetween;
            repetition += 1;
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
        // Rotate, translate and scale
        RectF bounds = this.getMeasure().getBounds();
        canvas.rotate(info.angle, bounds.centerX(), bounds.centerY());
        canvas.translate(info.offset[0], info.offset[1]);
        canvas.scale(info.scale[0], info.scale[1], bounds.centerX(), bounds.centerY());

        // Draw the repetition
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
    public void draw(Canvas canvas, Path path, Matrix matrix) {
        // Check for repetition
        if (this.mRepetitions == 0 && this.mSpaceBetween == 0)
            return ;

        // Call the super
        super.draw(canvas, path, matrix);
    }

    /**
     * Given a repetition return the relative distance from the path start.
     * @param repetition    the percentage of the path
     * @return              the distance
     */
    @SuppressWarnings({"unused", "WeakerAccess"})
    public float getDistance(int repetition) {
        return this.getSpaceBetweenInPixels() * (repetition - 1);
    }

    /**
     * Check if a repetition if over the limits
     * @param   distance the current distance from the path start
     * @return  if over the global limits
     */
    @SuppressWarnings({"unused", "WeakerAccess"})
    public boolean isOverLimits(float distance) {
        // Limit precision
        float position = this.toRoundedFloat(distance);
        float start = this.toRoundedFloat(this.getStartAtDistance());
        float end = this.toRoundedFloat(this.getEndToDistance());

        // Compare
        return position < start || position > end;
    }

    /**
     * Check if a repetition if over the limits
     * @param   repetition the current repetition
     * @return  if over the global limits
     */
    @SuppressWarnings({"unused"})
    public boolean isOverLimits(int repetition) {
        float distance = this.getDistance(repetition);
        return this.isOverLimits(distance);
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
        destination.setRepetitionOffset(this.mRepetitionOffset);
        destination.setEdges(this.mEdges);
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
     * Set the edges type.
     * Can be: INSIDE, MIDDLE and OUTSIDE.
     * @param value the edges type
     */
    @SuppressWarnings({"unused", "WeakerAccess"})
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
    @SuppressWarnings({"unused", "WeakerAccess"})
    public Positions getEdges() { return this.mEdges; }


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
     * If space between is different by zero this always be zero.
     * @return get the repetitions number
     */
    @SuppressWarnings({"unused", "WeakerAccess"})
    public int getRepetitions() {
        return this.mRepetitions;
    }


    /**
     * If true the last repetition distance from path start will be equal to the path length.
     * NOTE than this property have effect only if the space between is zero.
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
     * If true the last repetition distance from path start will be equal to the path legth.
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
     * NOTE than this property exclude last repetition on path end property.
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


    /**
     * If true treat the space between repetition as percentage of the global path len.
     * If false the space between will consider as pixels length.
     * The reason of this property is the needs to keep the proportionality of the space between
     * repetition also after scale the drawing canvas.
     * @param value the new setting
     */
    @SuppressWarnings("unused")
    public void setSpaceBetweenAsPercentage(boolean value) {
        if (this.mSpaceBetweenAsPercentage != value) {
            this.mSpaceBetweenAsPercentage = value;
            this.onPropertyChange("spaceBetweenAsPercentage", value);
        }
    }

    /**
     * If true treat the space between repetition as percentage of the global path len.
     * If false the space between will consider as pixels length.
     * The reason of this property is the needs to keep the proportionality of the space between
     * repetition also after scale the drawing canvas.
     * @return the status
     */
    @SuppressWarnings("unused")
    public boolean getSpaceBetweenAsPercentage() {
        return this.mSpaceBetweenAsPercentage;
    }


    /**
     * Set the global repetition offset.
     * This property will affect on the value return by the getPositionOnPath method.
     * @param value the new setting
     */
    @SuppressWarnings({"unused", "WeakerAccess"})
    public void setRepetitionOffset(float value) {
        if (this.mRepetitionOffset != value) {
            this.mRepetitionOffset = value;
            this.onPropertyChange("repetitionOffset", value);
        }
    }

    /**
     * Get the global repetition offset.
     * This property will affect on the value return by the getPositionOnPath method.
     * @return true if the last repetition is on the path end
     */
    @SuppressWarnings("unused")
    public float getRepetitionOffset() {
        return this.mRepetitionOffset;
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
         * @param repetition the source object
         * @param info the feature info
         */
        void onDrawRepetition(ScRepetitions repetition, RepetitionInfo info);

    }

    /**
     * Set the draw listener to call.
     * @param listener the linked method to call
     */
    @SuppressWarnings({"unused", "WeakerAccess"})
    public void setOnDrawRepetitionListener(OnDrawRepetitionListener listener) {
        this.mOnDrawListener = listener;
    }


    // ***************************************************************************************
    // Drawing info class

    /**
     * This is a structure to hold the feature information before draw it
     */
    @SuppressWarnings({"unused", "WeakerAccess"})
    public class RepetitionInfo {

        // ***************************************************************************************
        // Properties

        private float[] mGenericPoint;

        public ScRepetitions source;
        public int repetition;

        public float distance;
        public float angle;
        public float tangent;
        public int color;
        public ScFeature.Positions position;
        public boolean visible;

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
            this.tangent = tangent;
            this.angle = 0.0f;
            this.position = feature.getPosition();
            this.color = feature.getGradientColor(distance);
            this.visible = !feature.isOverLimits(distance) && feature.getVisible();
        }

    }


}
