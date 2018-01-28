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

import java.lang.reflect.Field;
import java.util.List;

/**
 * Manage a generic gauge.
 * <p>
 * This class is studied to be an "helper class" and facilitate the user to create a gauge.
 * The path is generic and the class start with a standard configuration of features.
 * One base (inherited from the ScDrawer), one notches, one writer, one copier to create the
 * progress effect and two pointer for manage the user touch input.
 * <p>
 * You can drive each features calling back the wanted feature and using the belonging methods or
 * many setting can be drive directly by the XML composer.
 *
 * @author Samuele Carassai
 * @version 3.0.0
 * @since 2016-05-26
 * -----------------------------------------------------------------------------------------------
 */
public abstract class ScGauge extends ScDrawer implements
        ValueAnimator.AnimatorUpdateListener,
        ScFeature.OnDrawListener {

    // ***************************************************************************************
    // Constants

    private static final float DEFAULT_STROKE_SIZE = 3.0f;
    private static final int DEFAULT_STROKE_COLOR = Color.BLACK;
    private static final float DEFAULT_HALO_SIZE = 10.0f;
    private static final int DEFAULT_HALO_ALPHA = 128;

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

    private boolean mSnapToNotches;
    private PointerSelectMode mPointerSelectMode;

    private ScCopier mBase;
    private ScNotches mNotches;
    private ScCopier mProgress;
    private ScWriter mWriter;
    private ScPointer mHighPointer;
    private ScPointer mLowPointer;


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
    // Init methods


    /**
     * Get the attribute Id by a given name reference
     * @param prefix    name prefix
     * @param name      attribute name
     * @return          the Id
     */
    private int getAttributeId(String prefix, String name) {
        // Holders
        Field[] allFields = R.styleable.class.getFields();
        String resourceName = "ScGauge_" + prefix + name;

        // Cycle all styleable resource fields
        for (Field field : allFields) {
            // Get the field name and compare with that searching
            String fieldName = field.getName();
            if (resourceName.equals(fieldName)) {
                try {
                    // Try to convert into an int
                    String value = field.get(R.styleable.class).toString();
                    return Integer.parseInt(value);

                } catch (IllegalAccessException ignore) {}
            }
        }
        return 0;
    }

    /**
     * Apply the default attributes to a generic feature
     * @param attrArray the attribute array
     * @param feature   the destination feature
     * @param prefix    the attribute prefix
     */
    private void applyDefaultAttribute(TypedArray attrArray, ScFeature feature, String prefix) {
        // Find the width
        float[] widths = this
                .splitToWidths(attrArray.getString(this.getAttributeId(prefix, "Widths")));
        if (widths == null) {
            float width = attrArray.getDimension(
                    this.getAttributeId(prefix, "Size"),
                    0.0f
            );

            if (width != 0.0)
                widths = new float[] { width };
            else {
                if (feature.getWidths() == null)
                    widths = new float[] { ScGauge.DEFAULT_STROKE_SIZE };
                else
                    widths = feature.getWidths();
            }
        }

        ScFeature.WidthsMode widthMode = ScFeature.WidthsMode.values()[
            attrArray.getInt(
                    this.getAttributeId(prefix, "WidthsMode"),
                    ScFeature.WidthsMode.SMOOTH.ordinal()
            )
        ];

        // Find the colors
        int[] colors = this
                .splitToColors(attrArray.getString(this.getAttributeId(prefix, "Colors")));
        if (colors == null) {
            int color = attrArray.getColor(
                    this.getAttributeId(prefix, "Color"),
                    ScGauge.DEFAULT_STROKE_COLOR
            );

            if (color != 0.0)
                colors = new int[] { color };
            else {
                if (feature.getColors() == null)
                    colors = new int[] { ScGauge.DEFAULT_STROKE_COLOR };
                else
                    colors = feature.getColors();
            }
        }

        ScFeature.ColorsMode colorsMode = ScFeature.ColorsMode.values()[
            attrArray.getInt(
                    this.getAttributeId(prefix, "ColorsMode"),
                    ScFeature.ColorsMode.GRADIENT.ordinal()
            )
        ];

        // Position
        int index = attrArray.getInt(this.getAttributeId(prefix, "Position"), -1);
        if (index == -1)
            index = feature.getPosition().ordinal();
        ScFeature.Positions position = ScFeature.Positions.values()[index];

        // Rounded cap
        boolean roundedCap =
                attrArray.getBoolean(this.getAttributeId(prefix, "RoundedCap"), false);
        if (roundedCap)
            feature.getPainter().setStrokeCap(Paint.Cap.ROUND);

        // Apply
        feature.setWidths(widths);
        feature.setWidthsMode(widthMode);
        feature.setColors(colors);
        feature.setColorsMode(colorsMode);
        feature.setPosition(position);
    }

    /**
     * Apply the attributes to the base feature.
     * @param attrArray the attributes array
     * @param feature   the feature
     */
    private void applyAttributesToBase(TypedArray attrArray, ScCopier feature) {
        this.applyDefaultAttribute(attrArray, feature, "stroke");
    }

    /**
     * Apply the attributes to the progress feature.
     * @param attrArray the attributes array
     * @param feature   the feature
     */
    private void applyAttributesToProgress(TypedArray attrArray, ScCopier feature) {
        this.applyDefaultAttribute(attrArray, feature, "progress");
    }

    /**
     * Apply the attributes to the notches feature.
     * @param attrArray the attributes array
     * @param feature   the feature
     */
    private void applyAttributesToNotches(TypedArray attrArray, ScNotches feature) {
        // Apply the default attributes
        this.applyDefaultAttribute(attrArray, feature, "notches");

        // Get the notches count
        int count = attrArray.getInt(
                this.getAttributeId("", "notches"), 0);
        feature.setRepetitions(count);

        // Find the length
        float[] lengths = this
                .splitToWidths(attrArray.getString(R.styleable.ScGauge_notchesLengths));
        if (lengths == null) {
            float length = attrArray.getDimension(
                    R.styleable.ScGauge_notchesLength,
                    this.dipToPixel(ScGauge.DEFAULT_STROKE_SIZE)
            );
            lengths = new float[] { length };
        }
        feature.setLengths(lengths);

        ScNotches.LengthsMode lengthsMode = ScNotches.LengthsMode.values()[
            attrArray.getInt(
                    R.styleable.ScGauge_notchesLengthsMode,
                    ScNotches.LengthsMode.SMOOTH.ordinal()
            )
        ];
        feature.setLengthsMode(lengthsMode);
    }

    /**
     * Apply the attributes to the text writer feature.
     * @param attrArray the attributes array
     * @param feature   the feature
     */
    private void applyAttributesToWriter(TypedArray attrArray, ScWriter feature) {
        // Apply the default attributes
        this.applyDefaultAttribute(attrArray, feature, "text");

        // Get tokens
        String stringTokens = attrArray
                .getString(R.styleable.ScGauge_textTokens);
        String[] tokens = stringTokens != null ? stringTokens.split("\\|") : null;

        // Get the text alignment
        Paint.Align textAlign = Paint.Align.values()[
            attrArray.getInt(
                    R.styleable.ScGauge_textAlign, Paint.Align.LEFT.ordinal())
        ];

        // Bending
        boolean bending = attrArray.getBoolean(
                R.styleable.ScGauge_textBending, false);

        // Assign
        feature.setTokens(tokens);
        feature.getPainter().setTextAlign(textAlign);
        feature.setBending(bending);
    }

    /**
     * Apply the attributes to the pointer feature.
     * @param attrArray the attributes array
     * @param feature   the feature
     */
    private void applyAttributesToPointer(TypedArray attrArray, ScPointer feature) {
        // Apply the default attributes
        this.applyDefaultAttribute(attrArray, feature, "pointer");

        // Radius
        float radius = attrArray.getDimension(
                R.styleable.ScGauge_pointerRadius, 0.0f);

        // Halo
        float haloWidth= attrArray.getDimension(
                R.styleable.ScGauge_pointerHaloSize,
                this.dipToPixel(ScGauge.DEFAULT_HALO_SIZE)
        );
        int haloAlpha = attrArray.getInt(
                R.styleable.ScGauge_pointerHaloAlpha,
                ScGauge.DEFAULT_HALO_ALPHA
        );

        // Assign
        feature.setRadius(radius);
        feature.setHaloWidth(haloWidth);
        feature.setHaloAlpha(haloAlpha);
    }

    /**
      * Init the component.
      * Retrieve all attributes with the default values if needed.
      * Check the values for internal use and create the painters.
      * @param context  the owner context
      * @param attrs    the attribute set
      * @param defStyle the style
      */
    private void init(Context context, AttributeSet attrs, int defStyle) {
        //--------------------------------------------------
        // ATTRIBUTES

        // Get the attributes list
        final TypedArray attrArray = context
                .obtainStyledAttributes(attrs, R.styleable.ScGauge, defStyle, 0);

        // Features
        this.mBase = (ScCopier) this.addFeature(ScCopier.class);
        this.mBase.setTag(ScGauge.BASE_IDENTIFIER);
        this.applyAttributesToBase(attrArray, this.mBase);

        this.mNotches = (ScNotches) this.addFeature(ScNotches.class);
        this.mNotches.setTag(ScGauge.NOTCHES_IDENTIFIER);
        this.applyAttributesToNotches(attrArray, this.mNotches);

        this.mProgress = (ScCopier) this.addFeature(ScCopier.class);
        this.mProgress.setTag(ScGauge.PROGRESS_IDENTIFIER);
        this.applyAttributesToProgress(attrArray, this.mProgress);

        this.mWriter = (ScWriter) this.addFeature(ScWriter.class);
        this.mWriter.setTag(ScGauge.WRITER_IDENTIFIER);
        this.applyAttributesToWriter(attrArray, this.mWriter);

        this.mHighPointer = (ScPointer) this.addFeature(ScPointer.class);
        this.mHighPointer.setTag(ScGauge.HIGH_POINTER_IDENTIFIER);
        this.applyAttributesToPointer(attrArray, this.mHighPointer);

        this.mLowPointer = (ScPointer) this.addFeature(ScPointer.class);
        this.mLowPointer.setTag(ScGauge.LOW_POINTER_IDENTIFIER);
        this.mLowPointer.setVisible(false);
        this.mLowPointer.setOnDrawListener(this);

        // Common
        this.mHighValue = attrArray.getFloat(
                R.styleable.ScGauge_value, 0.0f);
        this.mLowValue = attrArray.getFloat(
                R.styleable.ScGauge_lowValue, 0.0f);

        if (this.mHighValue == 0.0f)
            this.mHighValue = attrArray.getFloat(
                    R.styleable.ScGauge_highValue, 0.0f);

        this.mSnapToNotches = attrArray.getBoolean(
                R.styleable.ScGauge_snapToNotches, false);

        this.mPointerSelectMode = PointerSelectMode.values()[
            attrArray.getInt(
                    R.styleable.ScGauge_pointerSelectMode, PointerSelectMode.NEAREST.ordinal())
        ];

        // Recycle
        attrArray.recycle();

        //--------------------------------------------------
        // ANIMATORS

        this.mHighValueAnimator = new ValueAnimator();
        this.mHighValueAnimator.setDuration(0);
        this.mHighValueAnimator.setInterpolator(new DecelerateInterpolator());
        this.mHighValueAnimator.addUpdateListener(this);

        this.mLowValueAnimator = new ValueAnimator();
        this.mLowValueAnimator.setDuration(0);
        this.mLowValueAnimator.setInterpolator(new DecelerateInterpolator());
        this.mLowValueAnimator.addUpdateListener(this);

        //--------------------------------------------------
        // INTERNAL

        // Disable the hardware acceleration as have problem with the shader
        this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        // Check for snap to notches the new degrees value
        if (this.mSnapToNotches && this.mNotches != null) {
            // Get the current value and round at the closed notches value
            this.mHighValue = this.mNotches.snapToRepetitions(this.mHighValue);
            this.mLowValue = this.mNotches.snapToRepetitions(this.mLowValue);
        }

        // Define the touch threshold
        this.fixTouchOnPathThreshold();
    }


    // ***************************************************************************************
    // Privates methods

    /**
     * Define the threshold for the touch on path recognize.
     */
    private void fixTouchOnPathThreshold() {
        // Check
        if (this.mHighPointer == null || this.mLowPointer == null)
            return ;

        // Fix the max
        float radius = this.mHighPointer.getRadius() > this.mLowPointer.getRadius() ?
                this.mHighPointer.getRadius() : this.mLowPointer.getRadius();
        float halo = this.mHighPointer.getHaloWidth() > this.mLowPointer.getHaloWidth() ?
                this.mHighPointer.getHaloWidth() : this.mLowPointer.getHaloWidth();

        // Define the touch threshold
        float sum = radius + halo;
        if (sum > 0)
            this.setPathTouchThreshold(sum);
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
     * Split a string in a series of width.
     */
    private float[] splitToWidths(String source) {
        // Check for empty values
        if (source == null || source.isEmpty()) return null;

        // Split the string and create the colors holder
        String[] tokens = source.split("\\|");
        float[] widths = new float[tokens.length];

        // Cycle all token
        for (int index = 0; index < tokens.length; index++) {
            // Try to convert
            float value = Float.valueOf(tokens[index]);
            widths[index] = this.dipToPixel(value);
        }

        // return
        return widths;
    }

    /**
     * Find the value percentage respect range of values.
     * @param value         the value
     * @param startRange    the start range value
     * @param endRange      the end range value
     * @return              the percentage
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
     * @param value         the new value
     * @param treatLowValue consider the low or the high value
     */
    private void setGenericValue(float value, boolean treatLowValue) {
        // Check the limits
        value = ScGauge.valueRangeLimit(value, 0, 100);

        // Check for snap to notches the new degrees value.
        if (this.mSnapToNotches && this.mNotches != null) {
            // Round at the closed notches value
            value = this.mNotches.snapToRepetitions(value);
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
     * @param percentage    from the path start
     * @return              the nearest pointer
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
            float normalDistance = Math.abs(percentage - current.getPointer());
            float inverseDistance = Math.abs(100 - percentage + current.getPointer());

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
     * @param distance  from the path start
     * @return          the over pointer
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
            float percentage = current.getPointer();
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
        if (this.mSnapToNotches && this.mNotches != null)
            value = this.mNotches.snapToRepetitions(value);

        // If here mean that the pointer is untagged.
        // I will move the pointer to the new position but I will not change no values.
        pointer.setPointer(value);
        this.invalidate();
    }

    /**
     * Attach the feature to the right listener only if the class listener is defined.
     * @param feature the source
     */
    private void attachFeatureToListener(ScFeature feature) {
        // Attach the listener by the class type
        if (this.mOnDrawListener != null)
            feature.setOnDrawListener(this);
    }


    // ***************************************************************************************
    // Instance state

    /**
     * Save the current instance state
     * @return the state
     * @hide
     */
    @Override
    protected Parcelable onSaveInstanceState() {
        // Call the super and get the parent state
        Parcelable superState = super.onSaveInstanceState();

        // Create a new bundle for store all the variables
        Bundle state = new Bundle();
        // Save all starting from the parent state
        state.putParcelable("PARENT", superState);
        state.putFloat("mHighValue", this.mHighValue);
        state.putFloat("mLowValue", this.mLowValue);
        state.putBoolean("mSnapToNotches", this.mSnapToNotches);
        state.putInt("mPointerSelectMode", this.mPointerSelectMode.ordinal());

        // Return the new state
        return state;
    }

    /**
     * Restore the current instance state
     * @param state the state
     * @hide
     */
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        // Implicit conversion in a bundle
        Bundle savedState = (Bundle) state;

        // Recover the parent class state and restore it
        Parcelable superState = savedState.getParcelable("PARENT");
        super.onRestoreInstanceState(superState);

        // Now can restore all the saved variables values
        this.mHighValue = savedState.getFloat("mHighValue");
        this.mLowValue = savedState.getFloat("mLowValue");
        this.mSnapToNotches = savedState.getBoolean("mSnapToNotches");
        this.mPointerSelectMode = PointerSelectMode
                .values()[savedState.getInt("mPointerSelectMode")];
    }


    // ***************************************************************************************
    // Overrides

    /**
     * Setting the features and call the ScDrawer base draw method.
     * @param canvas the view canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        // Check if have a selected pointer
        if (this.mSelectedPointer != null) {
            // Set the current status
            this.mSelectedPointer.setPressed(this.isPressed());
        }

        // Set the connected progress features properties
        List<ScFeature> progresses = this.findFeatures(null, ScGauge.PROGRESS_IDENTIFIER);
        for (ScFeature progress : progresses) {
            progress.setEndTo(this.mHighValue);
            progress.setStartAt(this.mLowValue);
        }

        // Set the connected pointers features properties
        List<ScFeature> pointers = this.findFeatures(ScPointer.class, null);
        for (ScFeature pointer : pointers) {
            // Cast to right class
            ScPointer casted = (ScPointer) pointer;
            // Select
            switch (pointer.getTag()) {
                case ScGauge.HIGH_POINTER_IDENTIFIER:
                    casted.setPointer(this.mHighValue);
                    break;

                case ScGauge.LOW_POINTER_IDENTIFIER:
                    casted.setPointer(this.mLowValue);
                    break;
            }
        }

        // Call the base drawing method
        super.onDraw(canvas);
    }

    /**
     * Override the on animation update method
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
     * The passed class reference must implement the ScFeature interface and will be filled
     * with the setting default params of this object by the type.
     * For example if instance a ScNotches the notches count will be auto settle to the defined
     * getNotchesCount method.
     * The new feature instantiate will linked to the gauge on draw listener.
     * If you will create the feature with another method you must manage the on draw listener by
     * yourself or attach it to the gauge at a later time using the proper method.
     * @param classRef  the class reference to instantiate
     * @return          the new feature object
     */
    @Override
    @SuppressWarnings("unused")
    public ScFeature addFeature(Class<?> classRef) {
        // Holders
        ScFeature already = this.findFeature(classRef);
        ScFeature feature;

        // Create a new features
        feature = super.addFeature(classRef);

        // If exists already a feature of this kind copy it
        if (already != null)
            already.copy(feature);

        // Attach the feature the listener if needed.
        this.attachFeatureToListener(feature);

        // Return the new feature
        return feature;
    }

    /**
     * Called when the path is touched.
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
     * Called before draw a feature.
     * @param info the copier info
     */
    @Override
    public void onBeforeDraw(ScFeature.DrawingInfo info) {
        // Forward the calling on local listener
        if (this.mOnDrawListener != null) {
            // Call the right event
            if (info instanceof ScCopier.DrawingInfo)
                this.mOnDrawListener.onBeforeDrawCopy((ScCopier.DrawingInfo) info);

            if (info instanceof ScNotches.DrawingInfo)
                this.mOnDrawListener.onBeforeDrawNotch((ScNotches.DrawingInfo) info);

            if (info instanceof ScPointer.DrawingInfo)
                this.mOnDrawListener.onBeforeDrawPointer((ScPointer.DrawingInfo) info);

            if (info instanceof ScWriter.DrawingInfo)
                this.mOnDrawListener.onBeforeDrawToken((ScWriter.DrawingInfo) info);
        }
    }


    // ***************************************************************************************
    // Public methods

    /**
     * Get the high value animator.
     * Note that the initial value duration of the animation is zero equal to "no animation".
     * @return the animator
     */
    @SuppressWarnings("unused")
    public Animator getHighValueAnimator() {
        return this.mHighValueAnimator;
    }

    /**
     * Get the low value animator.
     * Note that the initial value duration of the animation is zero equal to "no animation".
     * @return the animator
     */
    @SuppressWarnings("unused")
    public Animator getLowValueAnimator() {
        return this.mLowValueAnimator;
    }

    /**
     * Convert a percentage in a value within the passed range of values.
     * @param percentage    the percentage
     * @param startValue    the range starting value
     * @param endValue      the range ending value
     * @return              the value
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

    /**
     * Get the base feature.
     * @return the feature
     */
    @SuppressWarnings("unused")
    public ScCopier getBase() {
        return this.mBase;
    }

    /**
     * Get the progress feature.
     * @return the feature
     */
    @SuppressWarnings("unused")
    public ScCopier getProgress() {
        return this.mProgress;
    }

    /**
     * Get the notches feature.
     * @return the feature
     */
    @SuppressWarnings("unused")
    public ScNotches getNotches() {
        return this.mNotches;
    }

    /**
     * Get the text writer feature.
     * @return the feature
     */
    @SuppressWarnings("unused")
    public ScWriter getWriter() {
        return this.mWriter;
    }

    /**
     * Get the high pointer feature.
     * @return the feature
     */
    @SuppressWarnings("unused")
    public ScPointer getHighPointer() {
        return this.mHighPointer;
    }

    /**
     * Get the low pointer feature.
     * @return the feature
     */
    @SuppressWarnings("unused")
    public ScPointer getLowPointer() {
        return this.mLowPointer;
    }


    // ***************************************************************************************
    // Public properties

    /**
     * Set the current progress high value in percentage from the path start
     * @param percentage the new value in percentage
     */
    @SuppressWarnings("unused")
    public void setHighValue(float percentage) {
        this.setGenericValue(percentage, false);
    }

    /**
     * Get the current progress high value in percentage from the path start
     * @return the current value in percentage
     */
    @SuppressWarnings("unused")
    public float getHighValue() {
        return this.mHighValue;
    }


    /**
     * Set the progress high value but based on a values range.
     * @param value         the value to convert
     * @param startRange    the start value
     * @param endRange      the end value
     */
    @SuppressWarnings("unused")
    public void setHighValue(float value, float startRange, float endRange) {
        // Find the relative percentage
        float percentage = this.findPercentage(value, startRange, endRange);
        // Call the base method
        this.setGenericValue(percentage, false);
    }

    /**
     * Get the progress high value but based on a values range.
     * @param startRange    the start value
     * @param endRange      the end value
     * @return              the translated value
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
     * Set the current progress low value in percentage from the path start
     * @param percentage the new value in percentage
     */
    @SuppressWarnings("unused")
    public void setLowValue(float percentage) {
        this.setGenericValue(percentage, true);
    }

    /**
     * Get the current progress low value in percentage from the path start
     * @return the current value in percentage
     */
    @SuppressWarnings("unused")
    public float getLowValue() {
        return this.mLowValue;
    }


    /**
     * Set the progress low value but based on a values range.
     * @param value         the value to convert
     * @param startRange    the start value
     * @param endRange      the end value
     */
    @SuppressWarnings("unused")
    public void setLowValue(float value, float startRange, float endRange) {
        // Find the relative percentage
        float percentage = this.findPercentage(value, startRange, endRange);
        // Call the base method
        this.setGenericValue(percentage, true);
    }

    /**
     * Get the progress low value but based on a values range.
     * @param startRange    the start value
     * @param endRange      the end value
     * @return              the translated value
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
     * Set if the progress value must rounded to the closed notch.
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
     * Get if the progress value must rounded to the closed notch.
     * @return the status
     */
    @SuppressWarnings("unused")
    public boolean getSnapToNotches() {
        return this.mSnapToNotches;
    }


    /**
     * Set the pointer selection mode.
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

    /**
     * Get the pointer selection mode.
     * @return the selection mode
     */
    @SuppressWarnings("unused")
    public PointerSelectMode getPointerSelectMode() {
        return this.mPointerSelectMode;
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
         * @param lowValue  the current low value
         * @param highValue the current high value
         */
        void onValueChange(float lowValue, float highValue);

    }

    /**
     * Set the generic event listener
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
         * @param info the copier info
         */
        void onBeforeDrawCopy(ScCopier.DrawingInfo info);


        /**
         * Called before draw the single notch.
         * @param info the notch info
         */
        void onBeforeDrawNotch(ScNotches.DrawingInfo info);

        /**
         * Called before draw the pointer.
         * If the method set the bitmap inside the info object the default drawing will be bypassed
         * and the new bitmap will be draw on the canvas following the other setting.
         * @param info the pointer info
         */
        void onBeforeDrawPointer(ScPointer.DrawingInfo info);

        /**
         * Called before draw the single token
         * @param info the token info
         */
        void onBeforeDrawToken(ScWriter.DrawingInfo info);

    }

    /**
     * Set the draw listener to call.
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
