package com.sccomponents.gauges;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import java.util.Arrays;
import java.util.List;

/**
 * Manage a generic gauge.
 * <p/>
 * This class is studied to be an "helper class" to facilitate the user to create a gauge.
 * The path is generic and the class start with a standard configuration of features.
 * One base (inherited from the ScDrawer), one notches manager, one writer manager, one copier to
 * create the progress effect and two pointer for manage the user touch input.
 * <p/>
 * Here are exposed many methods to drive the common feature from the code or directly by the XML.
 * The features are recognized from the class by its tag so changing, for example, the color of
 * notches you will change the color of all notches tagged.
 * This is useful when you have a custom features configuration that use one more of feature per
 * type. All the custom added features not tagged should be managed by the user by himself.
 *
 * @author Samuele Carassai
 * @version 1.0.3
 * @since 2016-05-26
 */
public abstract class ScGauge extends ScDrawer implements
        ValueAnimator.AnimatorUpdateListener,
        ScCopier.OnDrawListener,
        ScPointer.OnDrawListener,
        ScNotches.OnDrawListener,
        ScWriter.OnDrawListener {

    // ***************************************************************************************
    // Constants

    public static final float DEFAULT_STROKE_SIZE = 3.0f;
    public static final int DEFAULT_STROKE_COLOR = Color.BLACK;

    public static final float DEFAULT_PROGRESS_SIZE = 0.0f;
    public static final int DEFAULT_PROGRESS_COLOR = Color.GRAY;

    public static final float DEFAULT_TEXT_SIZE = 16.0f;
    public static final float DEFAULT_HALO_SIZE = 10.0f;

    public static final String BASE_IDENTIFIER = "ScGauge_Base";
    public static final String NOTCHES_IDENTIFIER = "ScGauge_Notches";
    public static final String WRITER_IDENTIFIER = "ScGauge_Writer";
    public static final String PROGRESS_IDENTIFIER = "ScGauge_Progress";
    public static final String HIGH_POINTER_IDENTIFIER = "ScGauge_Pointer_High";
    public static final String LOW_POINTER_IDENTIFIER = "ScGauge_Pointer_Low";


    // ***************************************************************************************
    // Enumerators

    /**
     * The mode to select a pointer.
     */
    @SuppressWarnings("unuse")
    public enum PointerSelectMode {
        NEAREST,
        OVER
    }


    // ***************************************************************************************
    // Privates attribute

    private float mStrokeSize;
    private int[] mStrokeColors;
    private ScFeature.ColorsMode mStrokeColorsMode;

    private float mProgressSize;
    private int[] mProgressColors;
    private ScFeature.ColorsMode mProgressColorsMode;

    private float mNotchesSize;
    private int[] mNotchesColors;
    private ScFeature.ColorsMode mNotchesColorsMode;
    private int mNotchesCount;
    private float mNotchesLength;
    private ScNotches.NotchPositions mNotchesPosition;
    private boolean mSnapToNotches;

    private String[] mTextTokens;
    private float mTextSize;
    private int[] mTextColors;
    private ScFeature.ColorsMode mTextColorsMode;
    private ScWriter.TokenPositions mTextPosition;
    private ScWriter.TokenAlignments mTextAlignment;
    private boolean mTextUnbend;

    private float mPointerRadius;
    private int[] mPointerColors;
    private ScFeature.ColorsMode mPointerColorsMode;
    private float mPointerHaloWidth;
    private boolean mPointerLowVisible;
    private boolean mPointerHighVisible;
    private PointerSelectMode mPointerSelectMode;

    private Boolean mRoundedLineCap;


    // ***************************************************************************************
    // Privates variable

    private float mHighValue;
    private float mLowValue;

    private ValueAnimator mHighValueAnimator;
    private ValueAnimator mLowValueAnimator;

    private ScPointer mSelectedPointer;

    private OnEventListener mOnEventListener;
    private OnDrawListener mOnDrawListener;


    // ***************************************************************************************
    // Constructors

    public ScGauge(Context context) {
        super(context);
        this.init(context, null, 0);
    }

    public ScGauge(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(context, attrs, 0);
    }

    public ScGauge(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init(context, attrs, defStyleAttr);
    }


    // ***************************************************************************************
    // Privates methods

    /**
     * Set a feature with the default setting values by its type.
     *
     * @param feature the feature to settle
     */
    private void featureSetter(ScFeature feature) {
        // Check for empty value
        if (feature == null || feature.getTag() == null) return;

        // Hold the tag
        String tag = feature.getTag();

        // Check for rounded cap line style
        if (this.mRoundedLineCap != null) {
            feature.getPainter()
                    .setStrokeCap(this.mRoundedLineCap ? Paint.Cap.ROUND : Paint.Cap.BUTT);
        }

        // Base
        if (tag.equalsIgnoreCase(ScGauge.BASE_IDENTIFIER)) {
            // fill
            feature.getPainter().setStrokeWidth(this.mStrokeSize);
            feature.setColors(this.mStrokeColors);
            feature.setColorsMode(this.mStrokeColorsMode);
        }

        // Progress
        if (tag.equalsIgnoreCase(ScGauge.PROGRESS_IDENTIFIER)) {
            // fill
            feature.setLimits(this.mLowValue, this.mHighValue);
            feature.getPainter().setStrokeWidth(this.mProgressSize);
            feature.setColors(this.mProgressColors);
            feature.setColorsMode(this.mProgressColorsMode);
        }

        // Notches
        if (feature instanceof ScNotches &&
                tag.equalsIgnoreCase(ScGauge.NOTCHES_IDENTIFIER)) {
            // Cast and fill
            ScNotches notches = (ScNotches) feature;
            notches.setLength(this.mNotchesLength);
            notches.setCount(this.mNotchesCount);
            notches.getPainter().setStrokeWidth(this.mNotchesSize);
            notches.setColors(this.mNotchesColors);
            notches.setColorsMode(this.mNotchesColorsMode);
            notches.setPosition(this.mNotchesPosition);
        }

        // Writer
        if (feature instanceof ScWriter &&
                tag.equalsIgnoreCase(ScGauge.WRITER_IDENTIFIER)) {
            // Cast and fill
            ScWriter writer = (ScWriter) feature;
            writer.getPainter().setTextSize(this.mTextSize);
            writer.setColors(this.mTextColors);
            writer.setColorsMode(this.mTextColorsMode);
            writer.setPosition(this.mTextPosition);
            writer.setUnbend(this.mTextUnbend);
            writer.getPainter().setTextAlign(
                    Paint.Align.values()[this.mTextAlignment.ordinal()]);

            if (this.mTextTokens != null) {
                writer.setTokens(this.mTextTokens);
            }
        }

        // Pointers
        boolean isHigh = tag.equalsIgnoreCase(ScGauge.HIGH_POINTER_IDENTIFIER);
        boolean isLow = tag.equalsIgnoreCase(ScGauge.LOW_POINTER_IDENTIFIER);

        if (feature instanceof ScPointer && (isHigh || isLow)) {
            // Cast and fill
            ScPointer pointer = (ScPointer) feature;
            pointer.setRadius(this.mPointerRadius);
            pointer.setHaloWidth(this.mPointerHaloWidth);
            pointer.setColors(this.mPointerColors);
            pointer.setColorsMode(this.mPointerColorsMode);

            // Switch the case
            if (isHigh) {
                pointer.setPosition(this.mHighValue);
                pointer.setVisible(this.mPointerHighVisible);
            }
            if (isLow) {
                pointer.setPosition(this.mLowValue);
                pointer.setVisible(this.mPointerLowVisible);
            }
        }
    }

    /**
     * Round the value near the closed notch.
     *
     * @param percentage the start percentage value
     * @return the percentage value close the notch
     */
    private float snapToNotches(float percentage) {
        // Check for empty value
        if (this.mNotchesCount == 0)
            return 0.0f;

        // Calc the percentage step delta and return the closed value
        float step = 100.0f / this.mNotchesCount;
        float value = Math.round(percentage / step) * step;

        if (this.mPathMeasure != null && this.mPathMeasure.isClosed() &&
                value >= 100)
            return 0;
        else
            return value;
    }

    /**
     * Define the threshold for the touch on path recognize.
     */
    private void fixTouchOnPathThreshold() {
        // Define the touch threshold
        if (this.mPointerRadius > 0) {
            this.setPathTouchThreshold(mPointerRadius + this.mPointerHaloWidth);
        }
    }

    /**
     * Split a string in a series of colors.
     */
    private int[] splitToColors(String source) {
        // Check for empty values
        if (source == null || source.isEmpty()) return null;

        // Split the string and create the colors holder
        String[] tokens = source.split("\\|");
        int[] colors = new int[tokens.length];

        // Cycle all token
        for (int index = 0; index < tokens.length; index++) {
            // Try to convert
            colors[index] = Color.parseColor(tokens[index]);
        }

        // return
        return colors;
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

        //--------------------------------------------------
        // BASE

        this.mStrokeSize = attrArray.getDimension(
                R.styleable.ScGauges_strokeSize, this.dipToPixel(ScGauge.DEFAULT_STROKE_SIZE));
        this.mStrokeColors = this
                .splitToColors(attrArray.getString(R.styleable.ScGauges_strokeColors));

        int strokeColor = attrArray.getColor(
                R.styleable.ScGauges_strokeColor, ScGauge.DEFAULT_STROKE_COLOR);
        if (this.mStrokeColors == null) {
            this.mStrokeColors = new int[]{strokeColor};
        }

        int strokeColorsMode = attrArray.getInt(
                R.styleable.ScGauges_strokeColorsMode, ScFeature.ColorsMode.GRADIENT.ordinal());
        this.mStrokeColorsMode = ScFeature.ColorsMode.values()[strokeColorsMode];

        //--------------------------------------------------
        // PROGRESS

        this.mProgressSize = attrArray.getDimension(
                R.styleable.ScGauges_progressSize, this.dipToPixel(ScGauge.DEFAULT_PROGRESS_SIZE));
        this.mProgressColors = this
                .splitToColors(attrArray.getString(R.styleable.ScGauges_progressColors));
        this.mHighValue = attrArray.getFloat(
                R.styleable.ScGauges_value, 0.0f);

        int progressColor = attrArray.getColor(
                R.styleable.ScGauges_progressColor, ScGauge.DEFAULT_PROGRESS_COLOR);
        if (this.mProgressColors == null) {
            this.mProgressColors = new int[]{progressColor};
        }

        int progressColorsMode = attrArray.getInt(
                R.styleable.ScGauges_progressColorsMode, ScFeature.ColorsMode.GRADIENT.ordinal());
        this.mProgressColorsMode = ScFeature.ColorsMode.values()[progressColorsMode];

        //--------------------------------------------------
        // NOTCHES

        this.mNotchesSize = attrArray.getDimension(
                R.styleable.ScGauges_notchesSize, this.dipToPixel(ScGauge.DEFAULT_STROKE_SIZE));
        this.mNotchesCount = attrArray.getInt(
                R.styleable.ScGauges_notches, 0);
        this.mNotchesLength = attrArray.getDimension(
                R.styleable.ScGauges_notchesLength, this.mStrokeSize * 2);
        this.mSnapToNotches = attrArray.getBoolean(
                R.styleable.ScGauges_snapToNotches, false);
        this.mNotchesColors = this
                .splitToColors(attrArray.getString(R.styleable.ScGauges_notchesColors));

        int notchesColor = attrArray.getColor(
                R.styleable.ScGauges_notchesColor, ScGauge.DEFAULT_STROKE_COLOR);
        if (this.mNotchesColors == null) {
            this.mNotchesColors = new int[]{notchesColor};
        }

        int notchesColorsMode = attrArray.getInt(
                R.styleable.ScGauges_notchesColorsMode, ScFeature.ColorsMode.GRADIENT.ordinal());
        this.mNotchesColorsMode = ScFeature.ColorsMode.values()[notchesColorsMode];

        int notchesPosition = attrArray.getInt(
                R.styleable.ScGauges_notchesPosition, ScNotches.NotchPositions.MIDDLE.ordinal());
        this.mNotchesPosition = ScNotches.NotchPositions.values()[notchesPosition];

        //--------------------------------------------------
        // TEXT

        this.mTextSize = attrArray.getDimension(
                R.styleable.ScGauges_textSize, this.dipToPixel(ScGauge.DEFAULT_TEXT_SIZE));
        this.mTextColors = this
                .splitToColors(attrArray.getString(R.styleable.ScGauges_textColors));
        this.mTextUnbend = attrArray.getBoolean(
                R.styleable.ScGauges_textUnbend, false);

        String stringTokens = attrArray.getString(R.styleable.ScGauges_textTokens);
        this.mTextTokens = stringTokens != null ? stringTokens.split("\\|") : null;

        int textColor = attrArray.getColor(
                R.styleable.ScGauges_textColor, ScGauge.DEFAULT_STROKE_COLOR);
        if (this.mTextColors == null) {
            this.mTextColors = new int[]{textColor};
        }

        int textColorsMode = attrArray.getInt(
                R.styleable.ScGauges_textColorsMode, ScFeature.ColorsMode.GRADIENT.ordinal());
        this.mTextColorsMode = ScFeature.ColorsMode.values()[textColorsMode];

        int textPosition = attrArray.getInt(
                R.styleable.ScGauges_textPosition, ScWriter.TokenPositions.MIDDLE.ordinal());
        this.mTextPosition = ScWriter.TokenPositions.values()[textPosition];
        int textAlign = attrArray.getInt(
                R.styleable.ScGauges_textAlign, ScWriter.TokenAlignments.LEFT.ordinal());
        this.mTextAlignment = ScWriter.TokenAlignments.values()[textAlign];

        //--------------------------------------------------
        // POINTER

        this.mPointerRadius = attrArray.getDimension(
                R.styleable.ScGauges_pointerRadius, 0.0f);
        this.mPointerColors = this
                .splitToColors(attrArray.getString(R.styleable.ScGauges_pointerColors));
        this.mPointerHaloWidth = attrArray.getDimension(
                R.styleable.ScGauges_haloSize, ScGauge.DEFAULT_HALO_SIZE);

        int pointerColor = attrArray.getColor(
                R.styleable.ScGauges_pointerColor, ScGauge.DEFAULT_STROKE_COLOR);
        if (this.mPointerColors == null) {
            this.mPointerColors = new int[]{pointerColor};
        }

        int pointerColorsMode = attrArray.getInt(
                R.styleable.ScGauges_pointerColorsMode, ScFeature.ColorsMode.GRADIENT.ordinal());
        this.mPointerColorsMode = ScFeature.ColorsMode.values()[pointerColorsMode];

        int pointerSelectMode = attrArray.getInt(
                R.styleable.ScGauges_pointerSelectMode, PointerSelectMode.NEAREST.ordinal());
        this.mPointerSelectMode = PointerSelectMode.values()[pointerSelectMode];

        //--------------------------------------------------
        // COMMON

        // Rounded line cap style
        if (attrArray.hasValue(R.styleable.ScGauges_roundedLine)) {
            this.mRoundedLineCap = attrArray.getBoolean(R.styleable.ScGauges_roundedLine, false);
        } else {
            this.mRoundedLineCap = null;
        }

        //--------------------------------------------------
        // INTERNAL

        // Recycle
        attrArray.recycle();

        // Disable the hardware acceleration as have problem with the shader
        this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        // Check for snap to notches the new degrees value
        if (this.mSnapToNotches) {
            // Get the current value and round at the closed notches value
            this.mHighValue = this.snapToNotches(this.mHighValue);
            this.mLowValue = this.snapToNotches(this.mLowValue);
        }

        // Define the touch threshold
        this.fixTouchOnPathThreshold();

        // Pointer visibility
        this.mPointerHighVisible = true;
        this.mPointerLowVisible = false;

        //--------------------------------------------------
        // FEATURES

        ScCopier base = (ScCopier) this.addFeature(ScCopier.class);
        base.setTag(ScGauge.BASE_IDENTIFIER);
        this.featureSetter(base);

        ScNotches notches = (ScNotches) this.addFeature(ScNotches.class);
        notches.setTag(ScGauge.NOTCHES_IDENTIFIER);
        this.featureSetter(notches);

        ScCopier progress = (ScCopier) this.addFeature(ScCopier.class);
        progress.setTag(ScGauge.PROGRESS_IDENTIFIER);
        this.featureSetter(progress);

        ScWriter writer = (ScWriter) this.addFeature(ScWriter.class);
        writer.setTag(ScGauge.WRITER_IDENTIFIER);
        this.featureSetter(writer);

        ScPointer highPointer = (ScPointer) this.addFeature(ScPointer.class);
        highPointer.setTag(ScGauge.HIGH_POINTER_IDENTIFIER);
        this.featureSetter(highPointer);

        ScPointer lowPointer = (ScPointer) this.addFeature(ScPointer.class);
        lowPointer.setTag(ScGauge.LOW_POINTER_IDENTIFIER);
        lowPointer.setOnDrawListener(this);
        this.featureSetter(lowPointer);

        //--------------------------------------------------
        // ANIMATOR

        this.mHighValueAnimator = new ValueAnimator();
        this.mHighValueAnimator.setDuration(0);
        this.mHighValueAnimator.setInterpolator(new DecelerateInterpolator());
        this.mHighValueAnimator.addUpdateListener(this);

        this.mLowValueAnimator = new ValueAnimator();
        this.mLowValueAnimator.setDuration(0);
        this.mLowValueAnimator.setInterpolator(new DecelerateInterpolator());
        this.mLowValueAnimator.addUpdateListener(this);
    }

    /**
     * Find the value percentage respect range of values.
     *
     * @param value      the value
     * @param startRange the start range value
     * @param endRange   the end range value
     * @return the percentage
     */
    private float findPercentage(float value, float startRange, float endRange) {
        // Limit the value within the range
        value = ScGauge.valueRangeLimit(value, startRange, endRange);
        // Check the domain
        if (endRange - startRange == 0.0f) {
            // Return zero
            return 0.0f;

        } else {
            // return the calculated percentage
            return ((value - startRange) / (endRange - startRange)) * 100.0f;
        }
    }

    /**
     * Set the current progress value in percentage from the path start.
     *
     * @param value         the new value
     * @param treatLowValue consider the low or the high value
     */
    private void setGenericValue(float value, boolean treatLowValue) {
        // Check the limits
        value = ScGauge.valueRangeLimit(value, 0, 100);

        // Check for snap to notches the new degrees value.
        if (this.mSnapToNotches) {
            // Round at the closed notches value
            value = this.snapToNotches(value);
        }

        // Choice the value and the animation
        float currValue = treatLowValue ? this.mLowValue : this.mHighValue;
        ValueAnimator animator = treatLowValue ? this.mLowValueAnimator : this.mHighValueAnimator;

        // Limits
        if (treatLowValue && value > this.mHighValue) value = this.mHighValue;
        if (!treatLowValue && value < this.mLowValue) value = this.mLowValue;

        // Check if value is changed
        if (currValue != value) {
            // Set and start animation
            animator.setFloatValues(currValue, value);
            animator.start();
        }
    }

    /**
     * Get the nearest pointer considering the passed distance from the path start.
     *
     * @param percentage from the path start
     * @return the nearest pointer
     */
    private ScPointer findNearestPointer(float percentage) {
        // Get all pointers
        List<ScFeature> pointers = this.findFeatures(ScPointer.class, null);
        ScPointer nearestPointer = null;
        float nearestValue = Float.MAX_VALUE;

        // Cycle all pointers found
        for (ScFeature pointer : pointers) {
            // Cast to current pointer
            ScPointer current = (ScPointer) pointer;
            if (!current.getVisible() || current.getRadius() == 0.0f) continue;

            // Find the distance from the current pointer and the pressed point
            float normalDistance = Math.abs(percentage - current.getPosition());
            float inverseDistance = Math.abs(100 - percentage + current.getPosition());

            // Check in the normal way
            if (normalDistance < nearestValue) {
                nearestValue = normalDistance;
                nearestPointer = current;
            }

            // If the path is closed try to search in negative
            if (this.mPathMeasure.isClosed())
                if (inverseDistance < nearestValue) {
                    nearestValue = inverseDistance;
                    nearestPointer = current;
                }
        }
        // Return the nearest pointer if found
        return nearestPointer;
    }

    /**
     * Find the pointer positioned over this distance
     *
     * @param distance from the path start
     * @return the over pointer
     */
    private ScPointer findOverPointer(float distance) {
        // Get all pointers
        List<ScFeature> pointers = this.findFeatures(ScPointer.class, null);

        // Cycle all pointers found
        for (ScFeature pointer : pointers) {
            // Cast to current pointer
            ScPointer current = (ScPointer) pointer;
            if (!current.getVisible()) continue;

            // Transform a distance of the current pointer from percentage to pixel
            float percentage = current.getPosition();
            float currentDistance = ScGauge
                    .percentageToValue(percentage, 0, this.mPathMeasure.getLength());

            // If the nearest is null assign the first pointer to it
            if (currentDistance >= distance - current.getRadius() &&
                    currentDistance <= distance + current.getRadius())
                return current;

            // If the path is closed try to search in negative
            if (this.mPathMeasure.isClosed()) {
                float negative = distance - this.mPathMeasure.getLength();
                if (currentDistance >= negative - current.getRadius() &&
                        currentDistance <= negative + current.getRadius())
                    return current;
            }
        }
        // Return null if not found
        return null;
    }

    /**
     * Set the value (high or low) considering the near pointer.
     *
     * @param value   the new value
     * @param pointer the pointer near
     */
    private void setValueByPointer(float value, ScPointer pointer) {
        // Check for the low value
        if (pointer != null && pointer.getTag() != null &&
                pointer.getTag().equalsIgnoreCase(ScGauge.LOW_POINTER_IDENTIFIER)) {
            // Set and exit
            this.invalidate();
            this.setGenericValue(value, true);
            return;
        }

        // Check for the high value
        if (pointer == null ||
                (pointer.getTag() != null && pointer.getTag().equalsIgnoreCase(ScGauge.HIGH_POINTER_IDENTIFIER))) {
            // Set and exit
            this.invalidate();
            this.setGenericValue(value, false);
            return;
        }

        // Check for snap to notches the new degrees value.
        if (this.mSnapToNotches)
            value = this.snapToNotches(value);

        // If here mean that the pointer is untagged.
        // I will move the pointer to the new position but I will not change no values.
        pointer.setPosition(value);
        this.invalidate();
    }

    /**
     * Attach the feature to the right listener only if the class listener is defined.
     *
     * @param feature the source
     */
    private void attachFeatureToListener(ScFeature feature) {
        // Attach the listener by the class type
        if (this.mOnDrawListener != null) {
            if (feature instanceof ScCopier) ((ScCopier) feature).setOnDrawListener(this);
            if (feature instanceof ScPointer) ((ScPointer) feature).setOnDrawListener(this);
            if (feature instanceof ScNotches) ((ScNotches) feature).setOnDrawListener(this);
            if (feature instanceof ScWriter) ((ScWriter) feature).setOnDrawListener(this);
        }
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
        state.putFloat("mStrokeSize", this.mStrokeSize);
        state.putIntArray("mStrokeColors", this.mStrokeColors);
        state.putInt("mStrokeColorsMode", this.mStrokeColorsMode.ordinal());
        state.putFloat("mHighValue", this.mHighValue);
        state.putFloat("mLowValue", this.mLowValue);
        state.putFloat("mProgressSize", this.mProgressSize);
        state.putIntArray("mProgressColors", this.mProgressColors);
        state.putInt("mProgressColorsMode", this.mProgressColorsMode.ordinal());
        state.putFloat("mNotchesSize", this.mNotchesSize);
        state.putIntArray("mNotchesColors", this.mNotchesColors);
        state.putInt("mNotchesColorsMode", this.mNotchesColorsMode.ordinal());
        state.putInt("mNotchesCount", this.mNotchesCount);
        state.putFloat("mNotchesLength", this.mNotchesLength);
        state.putBoolean("mSnapToNotches", this.mSnapToNotches);
        state.putStringArray("mTextTokens", this.mTextTokens);
        state.putFloat("mTextSize", this.mTextSize);
        state.putIntArray("mTextColors", this.mTextColors);
        state.putInt("mTextColorsMode", this.mTextColorsMode.ordinal());
        state.putFloat("mPointerRadius", this.mPointerRadius);
        state.putIntArray("mPointerColors", this.mPointerColors);
        state.putInt("mPointerColorsMode", this.mPointerColorsMode.ordinal());
        state.putFloat("mPointerHaloWidth", this.mPointerHaloWidth);

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
        this.mStrokeSize = savedState.getFloat("mStrokeSize");
        this.mStrokeColors = savedState.getIntArray("mStrokeColors");
        this.mStrokeColorsMode = ScFeature.ColorsMode.values()[savedState.getInt("mStrokeColorsMode")];
        this.mHighValue = savedState.getFloat("mHighValue");
        this.mLowValue = savedState.getFloat("mLowValue");
        this.mProgressSize = savedState.getFloat("mProgressSize");
        this.mProgressColors = savedState.getIntArray("mProgressColors");
        this.mProgressColorsMode = ScFeature.ColorsMode.values()[savedState.getInt("mProgressColorsMode")];
        this.mNotchesSize = savedState.getFloat("mNotchesSize");
        this.mNotchesColors = savedState.getIntArray("mNotchesColors");
        this.mNotchesColorsMode = ScFeature.ColorsMode.values()[savedState.getInt("mNotchesColorsMode")];
        this.mNotchesCount = savedState.getInt("mNotchesCount");
        this.mNotchesLength = savedState.getFloat("mNotchesLength");
        this.mSnapToNotches = savedState.getBoolean("mSnapToNotches");
        this.mTextTokens = savedState.getStringArray("mTextTokens");
        this.mTextSize = savedState.getFloat("mTextSize");
        this.mTextColors = savedState.getIntArray("mTextColors");
        this.mTextColorsMode = ScFeature.ColorsMode.values()[savedState.getInt("mTextColorsMode")];
        this.mPointerRadius = savedState.getFloat("mPointerRadius");
        this.mPointerColors = savedState.getIntArray("mPointerColors");
        this.mPointerColorsMode = ScFeature.ColorsMode.values()[savedState.getInt("mPointerColorsMode")];
        this.mPointerHaloWidth = savedState.getFloat("mPointerHaloWidth");
    }


    // ***************************************************************************************
    // Overrides

    /**
     * Setting the features and call the ScDrawer base draw method.
     *
     * @param canvas the view canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        // Cycle all features
        for (ScFeature feature : this.findFeatures(null, null)) {
            // Setter
            this.featureSetter(feature);
        }

        // Check if have a selected pointer
        if (this.mSelectedPointer != null) {
            // Set the current status
            this.mSelectedPointer.setPressed(this.isPressed());
        }

        // Call the base drawing method
        super.onDraw(canvas);
    }

    /**
     * Override the on animation update method
     *
     * @param animation the animator
     */
    @Override
    @SuppressWarnings("unused")
    public void onAnimationUpdate(ValueAnimator animation) {
        // Get the current value
        if (animation.equals(this.mHighValueAnimator))
            this.mHighValue = (float) animation.getAnimatedValue();
        if (animation.equals(this.mLowValueAnimator))
            this.mLowValue = (float) animation.getAnimatedValue();

        // Refresh
        this.invalidate();

        // Manage the listener
        if (this.mOnEventListener != null) {
            this.mOnEventListener.onValueChange(this.mLowValue, this.mHighValue);
        }
    }

    /**
     * Add one feature to this drawer.
     * This particular overload instantiate a new object from the class reference passed.
     * <p/>
     * The passed class reference must implement the ScFeature interface and will be filled
     * with the setting default params of this object by the type.
     * For example if instance a ScNotches the notches count will be auto settle to the defined
     * getNotchesCount method.
     * <p/>
     * The new feature instantiate will linked to the gauge on draw listener.
     * If you will create the feature with another method you must manage the on draw listener by
     * yourself or attach it to the gauge at a later time using the proper method.
     *
     * @param classRef the class reference to instantiate
     * @return the new feature object
     */
    @Override
    @SuppressWarnings("unused")
    public ScFeature addFeature(Class<?> classRef) {
        // Instance calling the base method
        ScFeature feature = super.addFeature(classRef);

        // Call the feature setter here is useless but we want to have the right setting from
        // the first creation in case the user looking inside this object.
        this.featureSetter(feature);
        // Attach the feature the listener if needed.
        this.attachFeatureToListener(feature);

        // Return the new feature
        return feature;
    }

    /**
     * Called when the path is touched.
     *
     * @param distance the distance from the path start
     */
    @Override
    protected void onPathTouch(float distance) {
        // Select the nearest pointer and set the value
        float percentage = this.findPercentage(distance, 0, this.mPathMeasure.getLength());
        this.mSelectedPointer = null;

        if (this.mPointerSelectMode == PointerSelectMode.NEAREST)
            this.mSelectedPointer = this.findNearestPointer(percentage);

        if (this.mPointerSelectMode == PointerSelectMode.OVER)
            this.mSelectedPointer = this.findOverPointer(distance);

        this.setValueByPointer(percentage, this.mSelectedPointer);

        // Super
        super.onPathTouch(distance);
    }

    /**
     * Called when the user release the touch after than he touched the path.
     */
    @Override
    protected void onPathRelease() {
        // Super and refresh
        super.onPathRelease();
        this.invalidate();
    }

    /**
     * Called when, after a path touch, the user move the finger on the component.
     *
     * @param distance the distance from the path start
     */
    @Override
    protected void onPathSlide(float distance) {
        // Move the pointer and the value
        float percentage = this.findPercentage(distance, 0, this.mPathMeasure.getLength());
        this.setValueByPointer(percentage, this.mSelectedPointer);

        // Super
        super.onPathSlide(distance);
    }

    /**
     * Called before draw the path copy.
     *
     * @param info the copier info
     */
    @Override
    public void onBeforeDrawCopy(ScCopier.CopyInfo info) {
        // Forward the calling on local listener
        if (this.mOnDrawListener != null) {
            this.mOnDrawListener.onBeforeDrawCopy(info);
        }
    }

    /**
     * Called before draw the pointer.
     * If the method set the bitmap inside the info object the default drawing will be bypassed
     * and the new bitmap will be draw on the canvas following the other setting.
     *
     * @param info the pointer info
     */
    @Override
    public void onBeforeDrawPointer(ScPointer.PointerInfo info) {
        // Forward the calling on local listener
        if (this.mOnDrawListener != null) {
            this.mOnDrawListener.onBeforeDrawPointer(info);
        }
    }

    /**
     * Called before draw the single notch.
     *
     * @param info the notch info
     */
    @Override
    public void onBeforeDrawNotch(ScNotches.NotchInfo info) {
        // Forward the calling on local listener
        if (this.mOnDrawListener != null) {
            this.mOnDrawListener.onBeforeDrawNotch(info);
        }
    }

    /**
     * Called before draw the single token
     *
     * @param info the token info
     */
    @Override
    public void onBeforeDrawToken(ScWriter.TokenInfo info) {
        // Forward the calling on local listener
        if (this.mOnDrawListener != null) {
            this.mOnDrawListener.onBeforeDrawToken(info);
        }
    }


    // ***************************************************************************************
    // Public methods

    /**
     * Get the high value animator.
     * Note that the initial value duration of the animation is zero equal to "no animation".
     *
     * @return the animator
     */
    @SuppressWarnings("unused")
    public Animator getHighValueAnimator() {
        return this.mHighValueAnimator;
    }

    /**
     * Get the low value animator.
     * Note that the initial value duration of the animation is zero equal to "no animation".
     *
     * @return the animator
     */
    @SuppressWarnings("unused")
    public Animator getLowValueAnimator() {
        return this.mLowValueAnimator;
    }

    /**
     * Convert a percentage in a value within the passed range of values.
     *
     * @param percentage the percentage
     * @param startValue the range starting value
     * @param endValue   the range ending value
     * @return the value
     */
    @SuppressWarnings("unused")
    public static float percentageToValue(float percentage, float startValue, float endValue) {
        // Calculate the delta range
        float min = Math.min(startValue, endValue);
        float max = Math.max(startValue, endValue);
        float delta = max - min;
        // Return the value
        return (delta * (percentage / 100)) + min;
    }


    // ***************************************************************************************
    // Common

    /**
     * Return if the line style cap is set on rounded or not
     *
     * @return if rounded or not
     */
    @SuppressWarnings("unused")
    public boolean getRoundedLine() {
        return this.mRoundedLineCap != null && this.mRoundedLineCap;
    }

    /**
     * Set if the line style cap is set on rounded or not.
     * Please note than once set all the features, old and new, will be with this property settle
     * by the passed value.
     *
     * @param value if rounded or not
     */
    @SuppressWarnings("unused")
    public void setRoundedLine(boolean value) {
        // Check for changed value
        if (this.mRoundedLineCap == null || this.mRoundedLineCap != value) {
            // Set the new value and refresh
            this.mRoundedLineCap = value;
            this.invalidate();
        }
    }


    // ***************************************************************************************
    // Base

    /**
     * Return the stroke size
     *
     * @return the current stroke size in pixel
     */
    @SuppressWarnings("unused")
    public float getStrokeSize() {
        return this.mStrokeSize;
    }

    /**
     * Set the stroke size
     *
     * @param value the new stroke size in pixel
     */
    @SuppressWarnings("unused")
    public void setStrokeSize(float value) {
        // Check if value is changed
        if (this.mStrokeSize != value) {
            // Store the new value, check it and refresh the component
            this.mStrokeSize = value;
            this.requestLayout();
        }
    }

    /**
     * Return the current stroke colors
     *
     * @return the current stroke colors
     */
    @SuppressWarnings("unused")
    public int[] getStrokeColors() {
        return this.mStrokeColors;
    }

    /**
     * Set the current stroke colors
     *
     * @param value the new stroke colors
     */
    @SuppressWarnings("unused")
    public void setStrokeColors(int[] value) {
        // Check is values has changed
        if (!Arrays.equals(this.mStrokeColors, value)) {
            // Save the new value and refresh
            this.mStrokeColors = value;
            this.requestLayout();
        }
    }

    /**
     * Return the current stroke filling colors mode.
     *
     * @return the current mode
     */
    @SuppressWarnings("unused")
    public ScFeature.ColorsMode getStrokeColorsMode() {
        return this.mStrokeColorsMode;
    }

    /**
     * Set the current stroke filling colors mode.
     *
     * @param value the new mode
     */
    @SuppressWarnings("unused")
    public void setStrokeColorsMode(ScFeature.ColorsMode value) {
        // Check is values has changed
        if (this.mStrokeColorsMode != value) {
            // Save the new value and refresh
            this.mStrokeColorsMode = value;
            this.requestLayout();
        }
    }


    // ***************************************************************************************
    // Progress

    /**
     * Return the progress stroke size
     *
     * @return the size in pixel
     */
    @SuppressWarnings("unused")
    public float getProgressSize() {
        return this.mProgressSize;
    }

    /**
     * Set the progress stroke size
     *
     * @param value the value in pixel
     */
    @SuppressWarnings("unused")
    public void setProgressSize(float value) {
        // Check if value is changed
        if (this.mProgressSize != value) {
            // Store the new value
            this.mProgressSize = value;
            this.invalidate();
        }
    }

    /**
     * Return the progress stroke colors
     *
     * @return the colors
     */
    @SuppressWarnings("unused")
    public int[] getProgressColors() {
        return this.mProgressColors;
    }

    /**
     * Set the progress colors
     *
     * @param value the new colors
     */
    @SuppressWarnings("unused")
    public void setProgressColors(int[] value) {
        // Check if value is changed
        if (!Arrays.equals(this.mProgressColors, value)) {
            // Store the new value and refresh the component
            this.mProgressColors = value;
            this.invalidate();
        }
    }

    /**
     * Return the current progress filling colors mode.
     *
     * @return the current mode
     */
    @SuppressWarnings("unused")
    public ScFeature.ColorsMode getProgressColorsMode() {
        return this.mProgressColorsMode;
    }

    /**
     * Set the current progress filling colors mode.
     *
     * @param value the new mode
     */
    @SuppressWarnings("unused")
    public void setProgressColorsMode(ScFeature.ColorsMode value) {
        // Check is values has changed
        if (this.mProgressColorsMode != value) {
            // Save the new value and refresh
            this.mProgressColorsMode = value;
            this.requestLayout();
        }
    }

    /**
     * Return the high current progress value in percentage
     *
     * @return the current value in percentage
     */
    @SuppressWarnings("unused")
    public float getHighValue() {
        return this.mHighValue;
    }

    /**
     * Set the current progress high value in percentage from the path start
     *
     * @param percentage the new value in percentage
     */
    @SuppressWarnings("unused")
    public void setHighValue(float percentage) {
        this.setGenericValue(percentage, false);
    }

    /**
     * Return the high progress value but based on a values range.
     *
     * @param startRange the start value
     * @param endRange   the end value
     * @return the translated value
     */
    @SuppressWarnings("unused")
    public float getHighValue(float startRange, float endRange) {
        // Check the domain
        if (this.mHighValue == 0) {
            return 0.0f;

        } else {
            // Calculate the value relative
            return ((endRange - startRange) * this.mHighValue) / 100.0f;
        }
    }

    /**
     * Set the progress high value but based on a values range.
     *
     * @param value      the value to convert
     * @param startRange the start value
     * @param endRange   the end value
     */
    @SuppressWarnings("unused")
    public void setHighValue(float value, float startRange, float endRange) {
        // Find the relative percentage
        float percentage = this.findPercentage(value, startRange, endRange);
        // Call the base method
        this.setGenericValue(percentage, false);
    }

    /**
     * Return the low current progress value in percentage
     *
     * @return the current value in percentage
     */
    @SuppressWarnings("unused")
    public float getLowValue() {
        return this.mLowValue;
    }

    /**
     * Set the current progress low value in percentage from the path start
     *
     * @param percentage the new value in percentage
     */
    @SuppressWarnings("unused")
    public void setLowValue(float percentage) {
        this.setGenericValue(percentage, true);
    }

    /**
     * Return the low progress value but based on a values range.
     *
     * @param startRange the start value
     * @param endRange   the end value
     * @return the translated value
     */
    @SuppressWarnings("unused")
    public float getLowValue(float startRange, float endRange) {
        // Check the domain
        if (this.mHighValue == 0) {
            return 0.0f;

        } else {
            // Calculate the value relative
            return ((endRange - startRange) * this.mLowValue) / 100.0f;
        }
    }

    /**
     * Set the progress low value but based on a values range.
     *
     * @param value      the value to convert
     * @param startRange the start value
     * @param endRange   the end value
     */
    @SuppressWarnings("unused")
    public void setLowValue(float value, float startRange, float endRange) {
        // Find the relative percentage
        float percentage = this.findPercentage(value, startRange, endRange);
        // Call the base method
        this.setGenericValue(percentage, true);
    }


    // ***************************************************************************************
    // Notches

    /**
     * Return the progress notch size
     *
     * @return the size in pixel
     */
    @SuppressWarnings("unused")
    public float getNotchesSize() {
        return this.mNotchesSize;
    }

    /**
     * Set the progress notch size
     *
     * @param value the new size in pixel
     */
    @SuppressWarnings("unused")
    public void setNotchesSize(float value) {
        // Check if value is changed
        if (this.mNotchesSize != value) {
            // Store the new value and refresh the component
            this.mNotchesSize = value;
            this.invalidate();
        }
    }

    /**
     * Return the notches colors
     *
     * @return the colors
     */
    @SuppressWarnings("unused")
    public int[] getNotchesColors() {
        return this.mNotchesColors;
    }

    /**
     * Set the notches colors
     *
     * @param value the new colors
     */
    @SuppressWarnings("unused")
    public void setNotchesColors(int[] value) {
        // Check is values has changed
        if (!Arrays.equals(this.mNotchesColors, value)) {
            // Store the new value and refresh the component
            this.mNotchesColors = value;
            this.invalidate();
        }
    }

    /**
     * Return the current notches filling colors mode.
     *
     * @return the current mode
     */
    @SuppressWarnings("unused")
    public ScFeature.ColorsMode getNotchesColorsMode() {
        return this.mNotchesColorsMode;
    }

    /**
     * Set the current notches filling colors mode.
     *
     * @param value the new mode
     */
    @SuppressWarnings("unused")
    public void setNotchesColorsMode(ScFeature.ColorsMode value) {
        // Check is values has changed
        if (this.mNotchesColorsMode != value) {
            // Save the new value and refresh
            this.mNotchesColorsMode = value;
            this.requestLayout();
        }
    }

    /**
     * Return the notches count
     *
     * @return the count
     */
    @SuppressWarnings("unused")
    public int getNotches() {
        return this.mNotchesCount;
    }

    /**
     * Set the notches count
     *
     * @param value the new value
     */
    @SuppressWarnings("unused")
    public void setNotches(int value) {
        // Check if value is changed
        if (this.mNotchesCount != value) {
            // Fix the new value
            this.mNotchesCount = value;
            this.invalidate();
        }
    }

    /**
     * Return the notches length
     *
     * @return the length
     */
    @SuppressWarnings("unused")
    public float getNotchesLength() {
        return this.mNotchesLength;
    }

    /**
     * Set the notches length
     *
     * @param value the new value in pixel
     */
    @SuppressWarnings("unused")
    public void setNotchesLength(float value) {
        // Check if value is changed
        if (this.mNotchesLength != value) {
            // Fix the new value
            this.mNotchesLength = value;
            this.invalidate();
        }
    }

    /**
     * Return if the progress value is rounded to the closed notch.
     *
     * @return the status
     */
    @SuppressWarnings("unused")
    public boolean getSnapToNotches() {
        return this.mSnapToNotches;
    }

    /**
     * Set if the progress value must rounded to the closed notch.
     *
     * @param value the status
     */
    @SuppressWarnings("unused")
    public void setSnapToNotches(boolean value) {
        // Check if the value is changed
        if (this.mSnapToNotches != value) {
            // Fix the trigger
            this.mSnapToNotches = value;

            // Recall the set value method for apply the new setting
            this.setHighValue(this.getHighValue());
            this.setLowValue(this.getLowValue());
        }
    }

    /**
     * Return the notches position respect the path.
     *
     * @return the position
     */
    @SuppressWarnings("unused")
    public ScNotches.NotchPositions getNotchesPosition() {
        return this.mNotchesPosition;
    }

    /**
     * Set the notches position respect the path.
     *
     * @param value the position
     */
    @SuppressWarnings("unused")
    public void setNotchesPosition(ScNotches.NotchPositions value) {
        // Check if the value is changed
        if (this.mNotchesPosition != value) {
            // Fix the trigger
            this.mNotchesPosition = value;
            this.requestLayout();
        }
    }


    // ***************************************************************************************
    // Texts

    /**
     * Return the text tokens to write on the path
     *
     * @return the tokens
     */
    @SuppressWarnings("unused")
    public String[] getTextTokens() {
        return this.mTextTokens;
    }

    /**
     * Set the text token to write on the path
     *
     * @param value the status
     */
    @SuppressWarnings("unused")
    public void setTextTokens(String[] value) {
        // Fix the trigger
        this.mTextTokens = value;
        this.invalidate();
    }

    /**
     * Return the text size in pixel
     *
     * @return the size in pixel
     */
    @SuppressWarnings("unused")
    public float getTextSize() {
        return this.mTextSize;
    }

    /**
     * Set the text size in pixel
     *
     * @param value the status
     */
    @SuppressWarnings("unused")
    public void setTextSize(float value) {
        // Check if value is changed
        if (this.mTextSize != value) {
            // Fix the trigger
            this.mTextSize = value;
            this.invalidate();
        }
    }

    /**
     * Return the text colors
     *
     * @return the colors
     */
    @SuppressWarnings("unused")
    public int[] getTextColors() {
        return this.mTextColors;
    }

    /**
     * Set the text colors
     *
     * @param value the colors
     */
    @SuppressWarnings("unused")
    public void setTextColors(int[] value) {
        // Check is values has changed
        if (!Arrays.equals(this.mTextColors, value)) {
            // Fix the trigger
            this.mTextColors = value;
            this.invalidate();
        }
    }

    /**
     * Return the current text filling colors mode.
     *
     * @return the current mode
     */
    @SuppressWarnings("unused")
    public ScFeature.ColorsMode getTextColorsMode() {
        return this.mTextColorsMode;
    }

    /**
     * Set the current text filling colors mode.
     *
     * @param value the new mode
     */
    @SuppressWarnings("unused")
    public void setTextColorsMode(ScFeature.ColorsMode value) {
        // Check is values has changed
        if (this.mTextColorsMode != value) {
            // Save the new value and refresh
            this.mTextColorsMode = value;
            this.requestLayout();
        }
    }

    /**
     * Return the text position respect the path.
     *
     * @return the position
     */
    @SuppressWarnings("unused")
    public ScWriter.TokenPositions getTextPosition() {
        return this.mTextPosition;
    }

    /**
     * Set the text position respect the path.
     *
     * @param value the position
     */
    @SuppressWarnings("unused")
    public void setTextPosition(ScWriter.TokenPositions value) {
        // Check if the value is changed
        if (this.mTextPosition != value) {
            // Fix the trigger
            this.mTextPosition = value;
            this.requestLayout();
        }
    }

    /**
     * Return the text alignment respect the path owner segment.
     *
     * @return the alignment
     */
    @SuppressWarnings("unused")
    public ScWriter.TokenAlignments getTextAlign() {
        return this.mTextAlignment;
    }

    /**
     * Set the text alignment respect the path owner segment.
     *
     * @param value the alignment
     */
    @SuppressWarnings("unused")
    public void setTextAlign(ScWriter.TokenAlignments value) {
        // Check if the value is changed
        if (this.mTextAlignment != value) {
            // Fix the trigger
            this.mTextAlignment = value;
            this.requestLayout();
        }
    }

    /**
     * Return true if the text is unbend.
     *
     * @return the status
     */
    @SuppressWarnings("unused")
    public boolean getTextUnbend() {
        return this.mTextUnbend;
    }

    /**
     * Set true to have a unbend text.
     *
     * @param value the status
     */
    @SuppressWarnings("unused")
    public void setTextUnbend(boolean value) {
        // Check if the value is changed
        if (this.mTextUnbend != value) {
            // Fix the trigger
            this.mTextUnbend = value;
            this.requestLayout();
        }
    }


    // ***************************************************************************************
    // Pointers

    /**
     * Return the pointers radius in pixel.
     * Note that in the standard configuration the pointers are two: high and low.
     *
     * @return the radius in pixel
     */
    @SuppressWarnings("unused")
    public float getPointerRadius() {
        return this.mPointerRadius;
    }

    /**
     * Set all pointers radius in pixel.
     * Note that in the standard configuration the pointers are two: high and low.
     *
     * @param value the new radius
     */
    @SuppressWarnings("unused")
    public void setPointerRadius(float value) {
        // Check if value is changed
        if (this.mPointerRadius != value) {
            // Fix the trigger
            this.mPointerRadius = value;
            this.fixTouchOnPathThreshold();
            this.invalidate();
        }
    }

    /**
     * Return the pointers colors.
     * Note that in the standard configuration the pointers are two: high and low.
     *
     * @return the colors
     */
    @SuppressWarnings("unused")
    public int[] getPointersColors() {
        return this.mPointerColors;
    }

    /**
     * Set all pointers colors.
     * Note that in the standard configuration the pointers are two: high and low.
     *
     * @param value the colors
     */
    @SuppressWarnings("unused")
    public void setPointersColors(int[] value) {
        // Check is values has changed
        if (!Arrays.equals(this.mPointerColors, value)) {
            // Fix the trigger
            this.mPointerColors = value;
            this.invalidate();
        }
    }

    /**
     * Return the current pointer filling colors mode.
     * Note that in the standard configuration the pointers are two: high and low.
     *
     * @return the current mode
     */
    @SuppressWarnings("unused")
    public ScFeature.ColorsMode getPointerColorsMode() {
        return this.mPointerColorsMode;
    }

    /**
     * Set the current pointer filling colors mode.
     * Note that in the standard configuration the pointers are two: high and low.
     *
     * @param value the new mode
     */
    @SuppressWarnings("unused")
    public void setPointerColorsMode(ScFeature.ColorsMode value) {
        // Check is values has changed
        if (this.mPointerColorsMode != value) {
            // Save the new value and refresh
            this.mPointerColorsMode = value;
            this.requestLayout();
        }
    }

    /**
     * Return the pointers halo width in pixel.
     * Note that in the standard configuration the pointers are two: high and low.
     *
     * @return the halo size
     */
    @SuppressWarnings("unused")
    public float getPointerHaloWidth() {
        return this.mPointerHaloWidth;
    }

    /**
     * Set all pointers halo width in pixel.
     * Note that in the standard configuration the pointers are two: high and low.
     *
     * @param value the new halo size
     */
    @SuppressWarnings("unused")
    public void setPointerHaloWidth(float value) {
        // Check if value is changed
        if (this.mPointerHaloWidth != value) {
            // Fix the trigger
            this.mPointerHaloWidth = value;
            this.fixTouchOnPathThreshold();
            this.invalidate();
        }
    }

    /**
     * Return the low pointers visibility.
     * By default is false.
     *
     * @return the pointer visibility
     */
    @SuppressWarnings("unused")
    public boolean getPointerLowVisibility() {
        return this.mPointerLowVisible;
    }

    /**
     * Set the low pointer visibility..
     * By default is false.
     *
     * @param value the pointer visibility
     */
    @SuppressWarnings("unused")
    public void setPointerLowVisibility(boolean value) {
        // Check if value is changed
        if (this.mPointerLowVisible != value) {
            // Fix the trigger
            this.mPointerLowVisible = value;
            this.invalidate();
        }
    }

    /**
     * Return the high pointer visibility.
     * By default is false.
     *
     * @return the pointer visibility
     */
    @SuppressWarnings("unused")
    public boolean getPointerHighVisibility() {
        return this.mPointerHighVisible;
    }

    /**
     * Set the high pointer visibility.
     * By default is true.
     *
     * @param value the pointer visibility
     */
    @SuppressWarnings("unused")
    public void setPointerHighVisibility(boolean value) {
        // Check if value is changed
        if (this.mPointerHighVisible != value) {
            // Fix the trigger
            this.mPointerHighVisible = value;
            this.invalidate();
        }
    }

    /**
     * Get the pointer selection mode.
     *
     * @return the selection mode
     */
    @SuppressWarnings("unused")
    public PointerSelectMode getPointerSelectMode() {
        return this.mPointerSelectMode;
    }

    /**
     * Set the pointer selection mode.
     *
     * @param value the selection mode
     */
    @SuppressWarnings("unused")
    public void setPointerSelectMode(PointerSelectMode value) {
        // Check if value is changed
        if (this.mPointerSelectMode != value) {
            // Fix the trigger
            this.mPointerSelectMode = value;
        }
    }


    // ***************************************************************************************
    // Public listener and interface

    /**
     * Generic event listener
     */
    @SuppressWarnings("unused")
    public interface OnEventListener {

        /**
         * Called when the high or the low value changed.
         *
         * @param lowValue  the current low value
         * @param highValue the current high value
         */
        void onValueChange(float lowValue, float highValue);

    }

    /**
     * Set the generic event listener
     *
     * @param listener the listener
     */
    @SuppressWarnings("unused")
    public void setOnEventListener(OnEventListener listener) {
        this.mOnEventListener = listener;
    }

    /**
     * Define the draw listener interface
     */
    @SuppressWarnings("unused")
    public interface OnDrawListener {

        /**
         * Called before draw the path copy.
         *
         * @param info the copier info
         */
        void onBeforeDrawCopy(ScCopier.CopyInfo info);


        /**
         * Called before draw the single notch.
         *
         * @param info the notch info
         */
        void onBeforeDrawNotch(ScNotches.NotchInfo info);

        /**
         * Called before draw the pointer.
         * If the method set the bitmap inside the info object the default drawing will be bypassed
         * and the new bitmap will be draw on the canvas following the other setting.
         *
         * @param info the pointer info
         */
        void onBeforeDrawPointer(ScPointer.PointerInfo info);

        /**
         * Called before draw the single token
         *
         * @param info the token info
         */
        void onBeforeDrawToken(ScWriter.TokenInfo info);

    }

    /**
     * Set the draw listener to call.
     *
     * @param listener the linked method to call
     */
    @SuppressWarnings("unused")
    public void setOnDrawListener(OnDrawListener listener) {
        // Hold the reference
        this.mOnDrawListener = listener;

        // Attach the listener to all features
        for (ScFeature feature : this.mFeatures) {
            this.attachFeatureToListener(feature);
        }
    }

}
