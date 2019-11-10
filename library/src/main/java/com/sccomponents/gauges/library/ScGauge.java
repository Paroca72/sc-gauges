package com.sccomponents.gauges.library;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
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
 * </p><p>
 * You can drive each features calling back the wanted feature and using the belonging methods or
 * many setting can be drive directly by the XML composer.
 * </p><br /><p>
 * <strong>XML attributes</strong><br />
 * See inherited from class {@link ScDrawer}
 * <li>duration: int - {@link #setDuration(int)}</li>
 * <li>value: float - {@link #setHighValue(float)}</li>
 * <li>highValue: float - {@link #setHighValue(float)}</li>
 * <li>lowValue: float - {@link #setLowValue(float)}</li>
 *
 * <li>strokeColor: color - {@link ScCopier#setColors(int...)}</li>
 * <li>strokeColors: string - example of 3 colors, #0000ff|#00ff00|#ff0000</li>
 * <li>strokeColorsMode: enum - {@link ScCopier#setColorsMode(ScFeature.ColorsMode)}</li>
 * <li>strokeWidth: float - {@link ScCopier#setWidths(float...)}</li>
 * <li>strokeWidths: string - example of 3 widths in Dip, 10|20|10</li>
 * <li>strokeWidthsMode: enum - {@link ScCopier#setWidthsMode(ScFeature.WidthsMode)}</li>
 * <li>strokePosition: enum - {@link ScCopier#setPosition(ScFeature.Positions)}</li>
 * <li>strokeRoundedCap: boolean</li>
 *
 * <li>progressColor: color - {@link ScCopier#setColors(int...)}</li>
 * <li>progressColors: string - example of 3 colors, #0000ff|#00ff00|#ff0000</li>
 * <li>progressColorsMode: enum - {@link ScCopier#setColorsMode(ScFeature.ColorsMode)}</li>
 * <li>progressWidth: float - {@link ScCopier#setWidths(float...)}</li>
 * <li>progressWidths: string - example of 3 widths in Dip, 10|20|10</li>
 * <li>progressWidthsMode: enum - {@link ScCopier#setWidthsMode(ScFeature.WidthsMode)}</li>
 * <li>progressPosition: enum - {@link ScCopier#setPosition(ScFeature.Positions)}</li>
 * <li>progressRoundedCap: boolean</li>
 *
 * <li>pointerSelectMode: enum - {@link #setPointerSelectMode(PointerSelectMode)}</li>
 * <li>pointerColor: color - {@link ScPointer#setColors(int...)}</li>
 * <li>pointerColors: string - example of 3 colors, #0000ff|#00ff00|#ff0000</li>
 * <li>pointerColorsMode: enum - {@link ScPointer#setColorsMode(ScFeature.ColorsMode)}</li>
 * <li>pointerWidth: float - {@link ScPointer#setWidths(float...)}</li>
 * <li>pointerWidths: string - example of 3 widths in Dip, 10|20|10</li>
 * <li>pointerWidthsMode: enum - {@link ScPointer#setWidthsMode(ScFeature.WidthsMode)}</li>
 * <li>pointerHeight: float - {@link ScPointer#setHeights(float...)}</li>
 * <li>pointerHeights: string - example of 3 heights in Dip, 10|20|10</li>
 * <li>pointerHeightsMode: enum - {@link ScPointer#setHeightsMode(ScNotches.HeightsMode)}</li>
 * <li>pointerPosition: enum - {@link ScPointer#setPosition(ScFeature.Positions)}</li>
 * <li>pointerHaloSize: dimension - {@link ScPointer#setHaloWidth(float)}</li>
 * <li>pointerHaloAlpha: dimension - {@link ScPointer#setHaloAlpha(int)}}</li>
 * <li>pointerRoundedCap: boolean</li>
 *
 * <li>notches: integer - {@link ScNotches#setRepetitions(int)}</li>
 * <li>notchesColor: color - {@link ScNotches#setColors(int...)}</li>
 * <li>notchesColors: string - example of 3 colors, #0000ff|#00ff00|#ff0000</li>
 * <li>notchesColorsMode: enum - {@link ScNotches#setColorsMode(ScFeature.ColorsMode)}</li>
 * <li>notchesWidth: float - {@link ScNotches#setWidths(float...)}</li>
 * <li>notchesWidths: string - example of 3 widths in Dip, 10|20|10</li>
 * <li>notchesWidthsMode: enum - {@link ScNotches#setWidthsMode(ScFeature.WidthsMode)}</li>
 * <li>notchesHeight: float - {@link ScNotches#setHeights(float...)}</li>
 * <li>notchesHeights: string - example of 3 heights in Dip, 10|20|10</li>
 * <li>notchesHeightsMode: enum - {@link ScNotches#setHeightsMode(ScNotches.HeightsMode)}</li>
 * <li>notchesPosition: enum - {@link ScNotches#setPosition(ScFeature.Positions)}</li>
 * <li>notchesRoundedCap: boolean</li>
 * <li>snapToNotches: boolean - {@link #setSnapToNotches(boolean)}</li>
 *
 * <li>textTokens: string - example, 1|2|3|4 {@link ScWriter#setTokens(String...)}</li>
 * <li>textColor: color - {@link ScWriter#setColors(int...)}</li>
 * <li>textColors: string - example of 3 colors, #0000ff|#00ff00|#ff0000</li>
 * <li>textColorsMode: enum - {@link ScWriter#setColorsMode(ScFeature.ColorsMode)}</li>
 * <li>textPosition: enum {@link ScWriter#setPosition(ScFeature.Positions)}</li>
 * <li>textAlign: enum</li>
 * <li>textBending: boolean {@link ScWriter#setBending(boolean)}</li>
 *
 * <li>labelTokens: string - example, 1|2|3|4 {@link ScLabeler#setTokens(String...)}</li>
 * <li>labelColor: color - {@link ScLabeler#setColors(int...)}</li>
 * <li>labelColors: string - example of 3 colors, #0000ff|#00ff00|#ff0000</li>
 * <li>labelColorsMode: enum - {@link ScLabeler#setColorsMode(ScFeature.ColorsMode)}</li>
 * <li>labelPosition: enum {@link ScLabeler#setPosition(ScFeature.Positions)}</li>
 * <li>labelAlign: enum</li>
 * <li>labelBending: boolean {@link ScLabeler#setBending(boolean)}</li>
 * <li>labelFormat: String {@link ScLabeler#setFormat(String)}</li>
 * </p>
 *
 * @author Samuele Carassai
 * @version 3.5.0
 * @since 2016-05-26
 * -----------------------------------------------------------------------------------------------
 */
public abstract class ScGauge extends ScDrawer
        implements ScFeature.OnPropertyChangedListener {

    // ***************************************************************************************
    // Constants

    private static final float DEFAULT_STROKE_SIZE = 3.0f;
    private static final int DEFAULT_STROKE_COLOR = Color.BLACK;
    private static final float DEFAULT_HALO_SIZE = 10.0f;
    private static final int DEFAULT_HALO_ALPHA = 128;

    /** Tag identifier of this feature */
    public static final String BASE_IDENTIFIER = "ScGauge_Base";
    /** Tag identifier of this feature */
    public static final String NOTCHES_IDENTIFIER = "ScGauge_Notches";
    /** Tag identifier of this feature */
    public static final String WRITER_IDENTIFIER = "ScGauge_Writer";
    /** Tag identifier of this feature */
    public static final String PROGRESS_IDENTIFIER = "ScGauge_Progress";
    /** Tag identifier of this feature */
    public static final String HIGH_POINTER_IDENTIFIER = "ScGauge_Pointer_High";
    /** Tag identifier of this feature */
    public static final String LOW_POINTER_IDENTIFIER = "ScGauge_Pointer_Low";
    /** Tag identifier of this feature */
    public static final String LABELER_IDENTIFIER = "ScGauge_Labeler";


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
    private ScLabeler mLabeler;


    // ***************************************************************************************
    // Privates variable

    private float mHighValue;
    private float mHighValueAnimated;
    private float mLowValue;
    private float mLowValueAnimated;
    private int mDuration;

    private ValueAnimator mHighValueAnimator;
    private ValueAnimator mLowValueAnimator;
    private AnimationStarter mAnimatorStarter;

    private ScPointer mSelectedPointer;

    private OnEventListener mOnEventListener;
    private OnDrawListener mOnDrawListener;

    // Events proxies
    private ScFeature.OnDrawContourListener proxyFeatureDrawListener =
            new ScFeature.OnDrawContourListener() {
                @Override
                public void onDrawContour(ScFeature source, ScFeature.ContourInfo info) {
                    callOnDrawContourEvent(info);
                }
            };

    // Events proxies
    private ScRepetitions.OnDrawRepetitionListener proxyRepetitionsDrawListener =
            new ScRepetitions.OnDrawRepetitionListener() {
                @Override
                public void onDrawRepetition(ScRepetitions source, ScRepetitions.RepetitionInfo info) {
                    callOnDrawRepetitionEvent(info);
                }
            };

    // Events proxies
    private ValueAnimator.AnimatorUpdateListener proxyAnimatorUpdateListener =
            new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    callOnAnimationUpdate(valueAnimator);
                }
            };


    // ***************************************************************************************
    // Classes

    class AnimationStarter implements Runnable {
        private ValueAnimator animator;
        private float lastValue;
        private float nextValue;

        public void set(ValueAnimator animator, float lastValue, float nextValue) {
            this.animator = animator;
            this.lastValue = lastValue;
            this.nextValue = nextValue;
        }

        public void run() {
            // Check for empty values
            if (this.animator == null)
                return ;

            // Update the value to reach
            this.animator.setFloatValues(this.lastValue, this.nextValue);

            // Start if not already running
            if (!this.animator.isRunning())
                this.animator.start();
        }
    }


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
    @SuppressWarnings("all")
    private int getAttributeId(String prefix, String name) {
        // Holders
        Field[] allFields = R.styleable.class.getFields();
        String resourceName = "ScGauge_scc" + prefix + name;

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
     * Retrieve a color array from the attributes
     * @param attrArray the attribute array
     * @param prefix    the attribute prefix
     * @return          the values
     */
    private int[] getColorsAttributes(TypedArray attrArray, String prefix) {
        // Find the colors
        int[] colors = this
                .splitToColors(attrArray.getString(this.getAttributeId(prefix, "Colors")));
        if (colors == null) {
            int color = attrArray.getColor(
                    this.getAttributeId(prefix, "Color"),
                    ScGauge.DEFAULT_STROKE_COLOR
            );
            return color != 0.0 ? new int[] { color }: null;
        } else
            return colors;
    }

    /**
     * Retrieve a float array from an attributes name
     * @param attrArray the attribute array
     * @param prefix    the attribute prefix
     * @param name      the name of the attribute
     * @return          the values
     */
    private float[] getFloatsAttributes(TypedArray attrArray, String prefix, String name) {
        float[] results = this
                .splitToWidths(attrArray.getString(this.getAttributeId(prefix, name)));
        if (results == null) {
            String shortName = name.substring(0, name.length() - 1);
            float value = attrArray.getDimension(
                    this.getAttributeId(prefix, shortName),
                    0.0f
            );
            return value != 0.0 ? new float[] { value }: null;
        } else
            return results;
    }

    /**
     * Apply the default attributes to a generic feature
     * @param attrArray the attribute array
     * @param feature   the destination feature
     * @param prefix    the attribute prefix
     */
    private void applyDefaultAttribute(TypedArray attrArray, ScFeature feature, String prefix) {
        // Find the colors
        int[] colors = this.getColorsAttributes(attrArray, prefix);
        if (colors == null) {
            if (feature.getColors() == null)
                colors = new int[] { ScGauge.DEFAULT_STROKE_COLOR };
            else
                colors = feature.getColors();
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

        // Apply
        feature.setColors(colors);
        feature.setColorsMode(colorsMode);
        feature.setPosition(position);
    }

    /**
     * Apply the default attributes to a copier feature
     * @param attrArray the attribute array
     * @param feature   the destination feature
     * @param prefix    the attribute prefix
     */
    private void applyAttributeToCopier(TypedArray attrArray, ScCopier feature, String prefix) {
        // Get the default
        this.applyDefaultAttribute(attrArray, feature, prefix);

        // Find the width
        float[] widths = this.getFloatsAttributes(attrArray, prefix, "Widths");
        if (widths == null) {
            if (feature.getWidths() == null)
                widths = new float[] { ScGauge.DEFAULT_STROKE_SIZE };
            else
                widths = feature.getWidths();
        }

        ScFeature.WidthsMode widthMode = ScFeature.WidthsMode.values()[
            attrArray.getInt(
                    this.getAttributeId(prefix, "WidthsMode"),
                    ScFeature.WidthsMode.SMOOTH.ordinal()
            )
        ];

        // Rounded cap
        boolean roundedCap =
                attrArray.getBoolean(this.getAttributeId(prefix, "RoundedCap"), false);
        if (roundedCap)
            feature.getPainter().setStrokeCap(Paint.Cap.ROUND);

        // Apply
        feature.setWidths(widths);
        feature.setWidthsMode(widthMode);
    }

    /**
      * Apply the attributes to the base feature.
     * @param attrArray the attributes array
     * @param feature   the feature
     */
    @SuppressWarnings("")
    private void applyAttributesToBase(TypedArray attrArray, ScCopier feature) {
        this.applyAttributeToCopier(attrArray, feature, "Stroke");
    }

    /**
     * Apply the attributes to the progress feature.
     * @param attrArray the attributes array
     * @param feature   the feature
     */
    private void applyAttributesToProgress(TypedArray attrArray, ScCopier feature) {
        this.applyAttributeToCopier(attrArray, feature, "Progress");
    }

    /**
     * Apply the attributes to the notches feature.
     * @param attrArray the attributes array
     * @param feature   the feature
     * @param prefix    the attribute prefix
     */
    private void applyAttributesToNotches(TypedArray attrArray, ScNotches feature, String prefix) {
        // Apply the default attributes
        this.applyDefaultAttribute(attrArray, feature, prefix);

        // Get the notches count
        int count = attrArray.getInt(
                this.getAttributeId("", prefix), 0);

        // Find the width
        float[] widths = this.getFloatsAttributes(attrArray, prefix, "Widths");
        if (widths == null) {
            if (feature.getWidths() == null)
                widths = new float[] { ScGauge.DEFAULT_STROKE_SIZE };
            else
                widths = feature.getWidths();
        }

        ScFeature.WidthsMode widthMode = ScFeature.WidthsMode.values()[
            attrArray.getInt(
                    this.getAttributeId(prefix, "WidthsMode"),
                    ScFeature.WidthsMode.SMOOTH.ordinal()
            )
        ];

        // Find the height
        float[] heights = this.getFloatsAttributes(attrArray, prefix, "Heights");
        if (heights == null) {
            if (feature.getHeights() == null)
                heights = new float[] { ScGauge.DEFAULT_STROKE_SIZE };
            else
                heights = feature.getHeights();
        }

        ScNotches.HeightsMode heightsMode = ScNotches.HeightsMode.values()[
            attrArray.getInt(
                    this.getAttributeId(prefix, "HeightsMode"),
                    ScNotches.HeightsMode.SMOOTH.ordinal()
            )
        ];

        // Rounded cap
        boolean roundedCap =
                attrArray.getBoolean(this.getAttributeId(prefix, "RoundedCap"), false);
        if (roundedCap)
            feature.getPainter().setStrokeCap(Paint.Cap.ROUND);

        // Apply
        feature.setRepetitions(count);
        feature.setWidths(widths);
        feature.setWidthsMode(widthMode);
        feature.setHeights(heights);
        feature.setHeightsMode(heightsMode);
    }

    /**
     * Apply the attributes to the text writer feature.
     * @param attrArray the attributes array
     * @param feature   the feature
     * @param prefix    the attribute prefix
     */
    private void applyAttributesToWriter(TypedArray attrArray, ScWriter feature, String prefix) {
        // Apply the default attributes
        this.applyDefaultAttribute(attrArray, feature, prefix);

        // Get tokens
        String stringTokens = attrArray
                .getString(this.getAttributeId(prefix, "Tokens"));
        String[] tokens = stringTokens != null ?
                stringTokens.split("\\|") : feature.getTokens();

        // Get the text alignment
        Paint.Align align = feature.getPainter().getTextAlign();
        Paint.Align textAlign = Paint.Align.values()[
            attrArray.getInt(
                    this.getAttributeId(prefix, "Align"), align.ordinal())
        ];

        // Bending
        boolean bending = attrArray.getBoolean(
                this.getAttributeId(prefix, "Bending"), feature.getBending());

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
        String prefix = "Pointer";
        this.applyAttributesToNotches(attrArray, feature, prefix);

        // Halo
        float haloWidth= attrArray.getDimension(
                this.getAttributeId(prefix, "HaloSize"),
                this.dipToPixel(ScGauge.DEFAULT_HALO_SIZE)
        );
        int haloAlpha = attrArray.getInt(
                this.getAttributeId(prefix, "HaloAlpha"),
                ScGauge.DEFAULT_HALO_ALPHA
        );

        // Assign
        feature.setHaloWidth(haloWidth);
        feature.setHaloAlpha(haloAlpha);
    }

    /**
     * Apply the attributes to the label writer feature.
     * @param attrArray the attributes array
     * @param feature   the feature
     */
    private void applyAttributesToLabeler(TypedArray attrArray, ScLabeler feature) {
        // Apply the default attributes
        String prefix = "Label";
        this.applyAttributesToWriter(attrArray, feature, prefix);

        // Other
        String format = attrArray.getString(this.getAttributeId(prefix, "Format"));
        boolean linked = attrArray.getBoolean(
                this.getAttributeId(prefix, "Linked"), feature.getLinkedToProgress());

        // Assign
        feature.setFormat(format);
        feature.setLinkedToProgress(linked);
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
        this.mBase.setOnPropertyChangedListener(this);
        this.applyAttributesToBase(attrArray, this.mBase);

        this.mNotches = (ScNotches) this.addFeature(ScNotches.class);
        this.mNotches.setTag(ScGauge.NOTCHES_IDENTIFIER);
        this.mNotches.setOnPropertyChangedListener(this);
        this.applyAttributesToNotches(attrArray, this.mNotches, "Notches");

        this.mProgress = (ScCopier) this.addFeature(ScCopier.class);
        this.mProgress.setTag(ScGauge.PROGRESS_IDENTIFIER);
        this.mProgress.setOnPropertyChangedListener(this);
        this.applyAttributesToProgress(attrArray, this.mProgress);

        this.mWriter = (ScWriter) this.addFeature(ScWriter.class);
        this.mWriter.setTag(ScGauge.WRITER_IDENTIFIER);
        this.mWriter.setOnPropertyChangedListener(this);
        this.applyAttributesToWriter(attrArray, this.mWriter, "Text");

        this.mHighPointer = (ScPointer) this.addFeature(ScPointer.class);
        this.mHighPointer.setTag(ScGauge.HIGH_POINTER_IDENTIFIER);
        this.mHighPointer.setOnPropertyChangedListener(this);
        this.mHighPointer.setVisible(false);
        this.applyAttributesToPointer(attrArray, this.mHighPointer);

        this.mLowPointer = (ScPointer) this.addFeature(ScPointer.class);
        this.mLowPointer.setTag(ScGauge.LOW_POINTER_IDENTIFIER);
        this.mLowPointer.setVisible(false);
        this.mLowPointer.setOnPropertyChangedListener(this);

        this.mLabeler = (ScLabeler) this.addFeature(ScLabeler.class);
        this.mLabeler.setTag(ScGauge.LABELER_IDENTIFIER);
        this.mLabeler.setVisible(false);
        this.mLabeler.setOnPropertyChangedListener(this);
        this.applyAttributesToLabeler(attrArray, this.mLabeler);

        // Common
        this.mHighValue = attrArray.getFloat(R.styleable.ScGauge_sccValue, 0.0f);
        this.mLowValue = attrArray.getFloat(R.styleable.ScGauge_sccLowValue, 0.0f);

        if (this.mHighValue == 0.0f)
            this.mHighValue = attrArray.getFloat(R.styleable.ScGauge_sccHighValue, 0.0f);

        this.mSnapToNotches = attrArray.getBoolean(
                R.styleable.ScGauge_sccSnapToNotches, false);

        this.mPointerSelectMode = PointerSelectMode.values()[
            attrArray.getInt(
                    R.styleable.ScGauge_sccPointerSelectMode, PointerSelectMode.NEAREST.ordinal())
        ];

        this.mDuration = attrArray.getInt(R.styleable.ScGauge_sccDuration, 0);

        // Recycle
        attrArray.recycle();

        //--------------------------------------------------
        // ANIMATORS

        this.mHighValueAnimator = new ValueAnimator();
        this.mHighValueAnimator.setDuration(this.mDuration);
        this.mHighValueAnimator.setInterpolator(new DecelerateInterpolator());
        this.mHighValueAnimator.addUpdateListener(this.proxyAnimatorUpdateListener);

        this.mLowValueAnimator = new ValueAnimator();
        this.mLowValueAnimator.setDuration(this.mDuration);
        this.mLowValueAnimator.setInterpolator(new DecelerateInterpolator());
        this.mLowValueAnimator.addUpdateListener(this.proxyAnimatorUpdateListener);

        this.mAnimatorStarter = new AnimationStarter();

        //--------------------------------------------------
        // INTERNAL

        // Before Lollipop needs to disable the hardware acceleration as have
        // problem with shader effect.
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        // Check for snap to notches the new degrees value
        if (this.mSnapToNotches && this.mNotches != null) {
            // Get the current value and round at the closed notches value
            this.mHighValue = this.snapToNotches(this.mHighValue);
            this.mLowValue = this.snapToNotches(this.mLowValue);
        }

        this.mHighValueAnimated = this.mHighValue;
        this.mLowValueAnimated = this.mLowValue;

        // Define the touch threshold
        this.fixTouchOnPathThreshold();
    }


    // ***************************************************************************************
    // Privates methods

    /**
     * Define the threshold for the touch on path recognize.
     */
    private void fixTouchOnPathThreshold() {
        // Find the max comparing every pointers
        float max = 0.0f;
        List<ScFeature> pointers = this.findFeatures(ScPointer.class, null);
        for (ScFeature pointer : pointers)
            if (pointer.getVisible()){
                float current = ((ScPointer) pointer).getMaxDimension();
                if (max < current)
                    max = current;
            }

        // Define the touch threshold
        if (max > 0)
            this.setPathTouchThreshold(max);
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
            value = this.snapToNotches(value);
        }

        // Set the duration
        if (this.mDuration >= 0) {
            this.mHighValueAnimator.setDuration(this.mDuration);
            this.mLowValueAnimator.setDuration(this.mDuration);
        }

        // Choice the value and the animation
        float currValue = treatLowValue ? this.mLowValueAnimated : this.mHighValueAnimated;
        ValueAnimator animator = treatLowValue ? this.mLowValueAnimator : this.mHighValueAnimator;

        // Limits
        if (treatLowValue && value > this.mHighValue) value = this.mHighValueAnimated;
        if (!treatLowValue && value < this.mLowValue) value = this.mLowValueAnimated;

        // Check if value is changed
        if (currValue != value) {
            // The animator should be started on a different thread to be sure to start
            // when the gauges will finished to draw.
            this.mAnimatorStarter.set(animator, currValue, value);
            this.post(this.mAnimatorStarter);
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
            if (!current.getVisible()) continue;

            // Find the distance from the current pointer and the pressed point
            float normalDistance = Math.abs(percentage - current.getDistance());
            float inverseDistance = Math.abs(100 - percentage + current.getDistance());

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
            float percentage = current.getDistance();
            float currentDistance = ScGauge
                    .percentageToValue(percentage, 0, this.mPathMeasure.getLength());

            // If the nearest is null assign the first pointer to it
            float dimension = current.getMaxDimension();
            if (currentDistance >= distance - dimension &&
                    currentDistance <= distance + dimension)
                return current;

            // If the path is closed try to search in negative
            if (this.mPathMeasure.isClosed()) {
                float negative = distance - this.mPathMeasure.getLength();
                if (currentDistance >= negative - dimension &&
                        currentDistance <= negative + dimension)
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
            this.mLowValue = value;
            this.setGenericValue(value, true);
            return;
        }

        // Check for the high value
        if (pointer == null ||
                (pointer.getTag() != null && pointer.getTag().equalsIgnoreCase(ScGauge.HIGH_POINTER_IDENTIFIER))) {
            // Set and exit
            this.mHighValue = value;
            this.setGenericValue(value, false);
            return;
        }

        // Check for snap to notches the new degrees value.
        if (this.mSnapToNotches && this.mNotches != null)
            value = this.snapToNotches(value);

        // If here mean that the pointer is untagged.
        // I will move the pointer to the new position but I will not change no values.
        pointer.setDistance(value);
        this.invalidate();
    }

    /**
     * Attach the feature to the right listener only if the class listener is defined.
     * @param feature the source
     */
    private void attachFeatureToListener(ScFeature feature) {
        // Check for empty values
        if (feature == null)
            return ;

        // Attach the listener by the class type
        feature.setOnDrawContourListener(this.proxyFeatureDrawListener);
        if (feature instanceof ScRepetitions) {
            ScRepetitions repetitions = (ScRepetitions) feature;
            repetitions.setOnDrawRepetitionListener(this.proxyRepetitionsDrawListener);
        }
    }

    /**
     * Call the before draw contour event
     * @param info the contour info
     */
    private void callOnDrawContourEvent(ScFeature.ContourInfo info) {
        if (this.mOnDrawListener != null)
            this.mOnDrawListener.onDrawContour(this, info);
    }

    /**
     * Call the before draw repetition event
     * @param info the repetition info
     */
    private void callOnDrawRepetitionEvent(ScRepetitions.RepetitionInfo info) {
        if (this.mOnDrawListener != null)
            this.mOnDrawListener.onDrawRepetition(this, info);
    }

    /**
     * Call the on animation update method
     * @param animation the animator
     */
    private void callOnAnimationUpdate(ValueAnimator animation) {
        // Holders
        boolean needToUpdate = false;
        float currentValue = (float) animation.getAnimatedValue();

        // Get the current value
        if (animation.equals(this.mHighValueAnimator))
            // If changed
            if (currentValue != this.mHighValueAnimated) {
                needToUpdate = true;
                this.mHighValueAnimated = currentValue;
            }

        if (animation.equals(this.mLowValueAnimator))
            // If changed
            if (currentValue != this.mLowValueAnimated) {
                needToUpdate = true;
                this.mLowValueAnimated = currentValue;
            }

        // Check
        if (!needToUpdate)
            return;

        // Refresh
        this.invalidate();

        // Manage the listener
        if (this.mOnEventListener != null) {
            this.mOnEventListener.onValueChange(
                    this,
                    this.mLowValueAnimated,
                    this.mHighValueAnimated,
                    animation.isRunning()
            );
        }
    }

    /**
     * Round the value (as percentage) near the closed notch.
     * @param value the value to round
     * @return      a rounded to notch value
     */
    @SuppressWarnings("unused")
    public float snapToNotches(float value) {
        // Convert the percentage to a distance
        float length = this.getPathMeasure().getLength();
        float distance = ScGauge.percentageToValue(value, 0, length);

        // Check for null values
        if (length == 0)
            return value;

        // Get the current notches and round the value
        ScNotches notches = this.getNotches();
        float fixed = notches.snapToNotches(distance);

        // Return a percentage
        return ScGauge.valueToPercentage(fixed, 0, length);
    }

    /**
     * Call the on property changed method
     * @param name the property name
     * @param value the new value
     */
    @Override
    public void onPropertyChanged(ScFeature feature, String name, Object value) {
        this.invalidate();
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
            progress.setEndTo(this.mHighValueAnimated);
            progress.setStartAt(this.mLowValueAnimated);
        }

        // Set the connected pointers features properties
        List<ScFeature> pointers = this.findFeatures(ScPointer.class, null);
        for (ScFeature pointer : pointers) {
            // Cast to right class
            ScPointer casted = (ScPointer) pointer;
            // Select
            switch (pointer.getTag()) {
                case ScGauge.HIGH_POINTER_IDENTIFIER:
                    casted.setDistance(this.mHighValueAnimated);
                    break;

                case ScGauge.LOW_POINTER_IDENTIFIER:
                    casted.setDistance(this.mLowValueAnimated);
                    break;
            }
        }

        // Set the connected labeler features properties
        List<ScFeature> labelers = this.findFeatures(ScLabeler.class, null);
        for (ScFeature labeler : labelers) {
            // Cast to right class
            ScLabeler casted = (ScLabeler) labeler;
            // Set the distance only if this labeler still linked with the progress
            if (casted.getLinkedToProgress())
                casted.setDistance(this.mHighValueAnimated);
        }

        // Call the base drawing method
        super.onDraw(canvas);
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
        float percentage = ScGauge.valueToPercentage(distance, 0, this.mPathMeasure.getLength());
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
        float percentage = ScGauge.valueToPercentage(distance, 0, this.mPathMeasure.getLength());
        this.setValueByPointer(percentage, this.mSelectedPointer);

        // Super
        super.onPathSlide(distance);
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
    public static float percentageToValue(float percentage, float startValue, float endValue) {
        // Calculate the delta range
        float min = Math.min(startValue, endValue);
        float max = Math.max(startValue, endValue);
        float delta = max - min;

        // Check limits
        if (percentage <= 0) return min;
        if (percentage >= 100) return max;

        // Return the value
        return (delta * (percentage / 100)) + min;
    }

    /**
     * Find the percentage respect a range of values.
     * @param value         the value
     * @param startRange    the start range value
     * @param endRange      the end range value
     * @return              the percentage
     */
    public static float valueToPercentage(float value, float startRange, float endRange) {
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

    /**
     * Get the labeler feature.
     * @return the feature
     */
    @SuppressWarnings("unused")
    public ScLabeler getLabeler() {
        return this.mLabeler;
    }


    // ***************************************************************************************
    // Public properties

    /**
     * Set the current progress high value in percentage from the path start
     * @param percentage the new value in percentage
     */
    @SuppressWarnings("unused")
    public void setHighValue(float percentage) {
        this.mHighValue = percentage;
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
        float percentage = ScGauge.valueToPercentage(value, startRange, endRange);
        // Call the base method
        this.setHighValue(percentage);
    }

    /**
     * Get the progress high value but based on a values range.
     * @param startRange    the start value
     * @param endRange      the end value
     * @return              the translated value
     */
    @SuppressWarnings("unused")
    public float getHighValue(float startRange, float endRange) {
        return ScGauge.percentageToValue(this.mHighValue, startRange, endRange);
    }


    /**
     * Set the current progress low value in percentage from the path start
     * @param percentage the new value in percentage
     */
    @SuppressWarnings("unused")
    public void setLowValue(float percentage) {
        this.mLowValue = percentage;
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
        float percentage = ScGauge.valueToPercentage(value, startRange, endRange);
        // Call the base method
        this.setLowValue(percentage);
    }

    /**
     * Get the progress low value but based on a values range.
     * @param startRange    the start value
     * @param endRange      the end value
     * @return              the translated value
     */
    @SuppressWarnings("unused")
    public float getLowValue(float startRange, float endRange) {
        return ScGauge.percentageToValue(this.mLowValue, startRange, endRange);
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


    /**
     * Set the animation duration.
     * As exists two animator (low and high) for have a major granular control of the animations
     * you can access to them using {@link #getHighValueAnimator} and {@link #getLowValueAnimator}.
     * If the duration minor than zero will not set and will considered the animator duration.
     * @param value the duration in milliseconds.
     */
    @SuppressWarnings("unused")
    public void setDuration(int value) {
        // Check if value is changed
        if (this.mDuration != value)
            this.mDuration = value;
    }

    /**
     * Get the animation duration.
     * @return the duration in milliseconds
     */
    @SuppressWarnings("unused")
    public int getDuration() {
        return this.mDuration;
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
        void onValueChange(ScGauge gauge, float lowValue, float highValue, boolean isRunning);

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
         * Called before draw the contour.
         * @param gauge the source object
         * @param info the feature info
         */
        void onDrawContour(ScGauge gauge, ScFeature.ContourInfo info);

        /**
         * Called before draw the repetition.
         * @param gauge the source object
         * @param info the feature info
         */
        void onDrawRepetition(ScGauge gauge, ScRepetitions.RepetitionInfo info);

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
