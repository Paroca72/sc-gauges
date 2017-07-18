package com.sccomponents.gauges;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Define the way to draw a path on a View's canvas
 *
 * @author Samuele Carassai
 * @version 1.0.3
 * @since 2016-05-26
 */
public abstract class ScDrawer extends ScBase {

    // ***************************************************************************************
    // Enumerators

    /**
     * The area filling types.
     */
    @SuppressWarnings("unuse")
    public enum FillingArea {
        NONE,
        BOTH,
        HORIZONTAL,
        VERTICAL
    }

    /**
     * The area filling mode.
     */
    @SuppressWarnings("unuse")
    public enum FillingMode {
        STRETCH,
        DRAW
    }


    // ***************************************************************************************
    // Private and protected attributes

    protected Path mPath;
    protected ScPathMeasure mPathMeasure;

    protected RectF mDrawArea;
    protected RectF mVirtualArea;
    protected PointF mAreaScale;

    protected List<ScFeature> mFeatures;
    private boolean mFeaturesMustBeRefresh;

    private FillingArea mFillingArea;
    private FillingMode mFillingMode;

    private int mMaximumWidth;
    private int mMaximumHeight;

    private Path mCopyPath;

    private boolean mRecognizePathTouch;
    private float mPathTouchThreshold;
    private boolean mPathIsTouched;

    private OnPathTouchListener mOnPathTouchListener;


    // ***************************************************************************************
    // Constructors

    // Constructor
    public ScDrawer(Context context) {
        super(context);
        this.init(context, null, 0);
    }

    // Constructor
    public ScDrawer(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(context, attrs, 0);
    }

    // Constructor
    public ScDrawer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init(context, attrs, defStyleAttr);
    }


    // ***************************************************************************************
    // Abstract methods

    /**
     * Create the path to draw.
     * This method need to draw something on the canvas. Note that the ScDrawer class not expose
     * other methods or public properties to manage the path. To work on path you can use the
     * protected properties: mPath and mPathMeasurer.
     * When you create the path you should be not take care of the view padding because is already
     * considered in the width and height params.
     *
     * @param width  the drawing area width
     * @param height the drawing area height
     * @return return the new path
     */
    @SuppressWarnings("unused")
    protected abstract Path createPath(int width, int height);


    // ***************************************************************************************
    // Privates methods

    /**
     * Check all input values if over the limits
     */
    private void checkValues() {
        // Dimensions
        if (this.mMaximumWidth < 0) this.mMaximumWidth = 0;
        if (this.mMaximumHeight < 0) this.mMaximumHeight = 0;
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
        final TypedArray attrArray = context.obtainStyledAttributes(attrs, R.styleable.ScGauges, defStyle, 0);

        // Read all attributes from xml and assign the value to linked variables
        this.mMaximumWidth = attrArray.getDimensionPixelSize(
                R.styleable.ScGauges_maxWidth, Integer.MAX_VALUE);
        this.mMaximumHeight = attrArray.getDimensionPixelSize(
                R.styleable.ScGauges_maxHeight, Integer.MAX_VALUE);

        int fillingArea = attrArray.getInt(
                R.styleable.ScGauges_fillArea, FillingArea.BOTH.ordinal());
        this.mFillingArea = FillingArea.values()[fillingArea];

        int fillingMode = attrArray.getInt(
                R.styleable.ScGauges_fillMode, FillingMode.DRAW.ordinal());
        this.mFillingMode = FillingMode.values()[fillingMode];

        // Input
        this.mRecognizePathTouch = attrArray.getBoolean(
                R.styleable.ScGauges_pathTouchable, false);

        // Recycle
        attrArray.recycle();

        //--------------------------------------------------
        // INTERNAL

        this.checkValues();
        this.mPathMeasure = new ScPathMeasure();
        this.mCopyPath = new Path();
        this.mFeaturesMustBeRefresh = true;
    }

    /**
     * Get the drawable area.
     *
     * @param width  the reference width
     * @param height the reference height
     * @return a rectangle that represent the area
     */
    private RectF getDrawableArea(int width, int height) {
        // Create the area and transpose it by the component padding
        RectF area = new RectF(0, 0, width, height);
        area.offset(this.getPaddingLeft(), this.getPaddingTop());
        // Return the calculated area
        return area;
    }

    /**
     * Calculate the virtual drawing area.
     * This area is calculated starting from the trimmed path area and expanded proportionally
     * by the stretch setting to cover the component drawing area.
     *
     * @param width  the reference width
     * @param height the reference height
     * @return a rectangle that represent the area
     */
    private RectF getVirtualArea(int width, int height) {
        // Check for empty values
        RectF pathBounds = this.mPathMeasure.getBounds();
        if (pathBounds == null) return new RectF();

        // Create the starting area
        RectF area = new RectF(0, 0, width, height);

        if (this.getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT ||
                (this.mFillingArea == FillingArea.BOTH || this.mFillingArea == FillingArea.HORIZONTAL)) {
            // Center the area
            area.offset(-pathBounds.left, 0);

            // Find the horizontal scale and apply it
            float xScale = pathBounds.width() <= 0.01f ? 0.0f : (float) width / pathBounds.width();
            area.left *= xScale;
            area.right *= xScale;
        }

        if (this.getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT ||
                (this.mFillingArea == FillingArea.BOTH || this.mFillingArea == FillingArea.VERTICAL)) {
            // Center the area
            area.offset(0, -pathBounds.top);

            // Find the vertical scale and apply it
            float yScale = pathBounds.height() <= 0.01f ? 0.0f : (float) height / pathBounds.height();
            area.top *= yScale;
            area.bottom *= yScale;
        }

        return area;
    }

    /**
     * Given a source rectangle and a destination one calculate the scale.
     *
     * @param source      the source rectangle
     * @param destination the destination rectangle
     * @return the scale
     */
    private PointF getScale(RectF source, RectF destination) {
        // Check for empty values
        if (source == null || destination == null) return new PointF();

        // Calculate the scale
        float scaleX = destination.width() == 0.0f ? 0.0f : source.width() / destination.width();
        float scaleY = destination.height() == 0.0f ? 0.0f : source.height() / destination.height();

        // Calculate the scale
        return new PointF(scaleX, scaleY);
    }

    /**
     * Fix the scales of the path by filling mode settings.
     *
     * @param source the source path
     * @param xScale the horizontal scale
     * @param yScale the vertical scale
     */
    private void scalePath(Path source, float xScale, float yScale) {
        // Check for empty value
        if (source == null) return;

        // Create a matrix and apply the new scale
        Matrix matrix = new Matrix();
        matrix.postScale(xScale, yScale);

        // Apply the scale
        source.transform(matrix);
    }

    /**
     * Calculate an threshold for convenience considering all the pointer on the path.
     *
     * @return The auto calculate threshold
     */
    private float getAutoPathTouchThreshold() {
        // Get all the pointers on the path
        List<ScFeature> pointers = this.findFeatures(ScPointer.class, null);
        float threshold = 0;

        // Cycle all the pointer and get the max radius
        for (ScFeature pointer : pointers) {
            // Get the current radius and compare
            float radius = ((ScPointer) pointer).getRadius();
            if (radius > threshold)
                threshold = radius;
        }
        return threshold;
    }


    // ***************************************************************************************
    // Draw methods

    /**
     * Draw all the features
     *
     * @param canvas the canvas where draw
     */
    private void drawFeatures(Canvas canvas) {
        // Check for empty values
        if (this.mFeatures != null) {
            // Cycle all features
            for (ScFeature feature : this.mFeatures) {
                // Check for empty value
                if (feature != null) {
                    // Check if need to refresh
                    if (this.mFeaturesMustBeRefresh) {
                        feature.refresh();
                    }
                    //Call the draw methods.
                    feature.draw(canvas);
                }
            }
            // Trigger
            this.mFeaturesMustBeRefresh = false;
        }
    }

    /**
     * Scale and transpose the path and after draw the features on the canvas
     *
     * @param canvas  the canvas where draw
     * @param xOffset the horizontal offset
     * @param yOffset the vertical offset
     */
    private void setForDraw(Canvas canvas, float xOffset, float yOffset) {
        // Create a copy of the original path because need to move the offset or scale the
        // path and not want lost the original one values.
        this.mCopyPath.set(this.mPath);
        this.scalePath(this.mCopyPath, this.mAreaScale.x, this.mAreaScale.y);
        this.mCopyPath.offset(
                xOffset + this.getPaddingLeft(),
                yOffset + this.getPaddingTop()
        );

        // Draw the features
        this.drawFeatures(canvas);
    }

    /**
     * Scale and transpose the canvas and after draw the features on the canvas
     *
     * @param canvas the canvas where draw
     */
    private void setForStretch(Canvas canvas) {
        // Save the current canvas status
        canvas.save();

        // Translate and scale the canvas
        canvas.translate(this.getPaddingLeft(), this.getPaddingTop());
        canvas.scale(this.mAreaScale.x, this.mAreaScale.y);

        // Create a copy of the original path because need to move the offset or scale the
        // path and not want lost the original one values.
        this.mCopyPath.set(this.mPath);
        this.mCopyPath.offset(
                -this.mPathMeasure.getBounds().left,
                -this.mPathMeasure.getBounds().top
        );

        // Draw all features
        this.drawFeatures(canvas);

        // Restore the last saved canvas status
        canvas.restore();
    }


    // ***************************************************************************************
    // Overrides

    /**
     * This method is used to calc the areas and filling it by call/set the right draw plan.
     * Are to consider two type of draw:
     * - DRAW: Scale and transpose the path and after draw it on canvas
     * - STRETCH: Scale and transpose the canvas and after draw the path on it
     *
     * @param canvas the view canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        // Check for empty values
        if (this.mPath == null || this.mDrawArea == null) return;

        // Select the drawing mode by the case
        switch (this.mFillingMode) {
            // Draw
            case DRAW:
                this.setForDraw(canvas, this.mVirtualArea.left, this.mVirtualArea.top);
                break;

            // Stretch
            case STRETCH:
                this.setForStretch(canvas);
                break;
        }
    }

    /**
     * On measure
     *
     * @param widthMeasureSpec  the reference width
     * @param heightMeasureSpec the reference height
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Find the global padding
        int widthGlobalPadding = this.getPaddingLeft() + this.getPaddingRight();
        int heightGlobalPadding = this.getPaddingTop() + this.getPaddingBottom();

        // Get suggested dimensions
        int width = View.getDefaultSize(this.getSuggestedMinimumWidth(), widthMeasureSpec);
        int height = View.getDefaultSize(this.getSuggestedMinimumHeight(), heightMeasureSpec);

        // Force to re-create the path passing the real dimensions to draw and get the measurer
        this.mPath = this.createPath(width - widthGlobalPadding, height - heightGlobalPadding);
        this.mPathMeasure.setPath(this.mPath, false);

        // The path could be changed so I must force the feattures to refresh the path info.
        this.mFeaturesMustBeRefresh = true;

        // If have some dimension to wrap will use the path boundaries for have the right
        // dimension summed to the global padding.
        if (this.getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT) {
            RectF rect = this.mPathMeasure.getBounds();
            width = (int) (rect != null ? rect.width() : 0) + widthGlobalPadding;
        }
        if (this.getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            RectF rect = this.mPathMeasure.getBounds();
            height = (int) (rect != null ? rect.height() : 0) + heightGlobalPadding;
        }

        // Get all area info that we need to hold
        this.mDrawArea = this
                .getDrawableArea(width - widthGlobalPadding, height - heightGlobalPadding);
        this.mVirtualArea = this
                .getVirtualArea(width - widthGlobalPadding, height - heightGlobalPadding);
        this.mAreaScale = this.getScale(this.mVirtualArea, this.mDrawArea);

        // Fix the component dimensions limits
        width = ScDrawer.valueRangeLimit(width, 0, this.mMaximumWidth);
        height = ScDrawer.valueRangeLimit(height, 0, this.mMaximumHeight);

        // Set the calculated dimensions
        this.setMeasuredDimension(width, height);
    }

    /**
     * On touch management
     *
     * @param event the touch event
     * @return Event propagation
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Check if the input is enabled
        if (!this.mRecognizePathTouch) {
            // Return false mean that on touch event will not capture all the event propagation
            // after the touch event.
            return false;
        }

        // Adjust the point
        // TODO: on stretch wrong the point
        float x = 0;
        float y = 0;

        if (this.mAreaScale.x != 0)
            x = (event.getX() - this.getPaddingLeft() - this.mVirtualArea.left) / this.mAreaScale.x;
        if (this.mAreaScale.y != 0)
            y = (event.getY() - this.getPaddingTop() - this.mVirtualArea.top) / this.mAreaScale.y;

        // Fix the threshold if not defined by the user
        if (this.mPathTouchThreshold == 0)
            this.mPathTouchThreshold = this.getAutoPathTouchThreshold();

        // Get the nearest point on the path from the touch of the user and calculate the distance
        // from the path start. Note that if the path is already pressed the threshold will be
        // infinite.
        float threshold = this.mPathIsTouched ? Float.POSITIVE_INFINITY : this.mPathTouchThreshold;
        float distance = this.mPathMeasure.getDistance(x, y, threshold);

        // Select case by action type
        switch (event.getAction()) {
            // Press
            case MotionEvent.ACTION_DOWN:
                // If the point belong to the arc set the current value and the pressed trigger.
                if (distance != -1.0f) {
                    // Hold the trigger and call the method
                    this.mPathIsTouched = true;
                    this.onPathTouch(distance);
                }
                break;

            // Release
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // Trigger is released and call the methos
                this.mPathIsTouched = false;
                this.onPathRelease();
                break;

            // Move
            case MotionEvent.ACTION_MOVE:
                // Check if the point belong to the path and if pressed.
                if (distance != -1.0f && this.mPathIsTouched) {
                    // Call method
                    this.onPathSlide(distance);
                }
                break;
        }

        // Event propagation.
        // Return true so this method will capture events after the pressure.
        return true;
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
        state.putInt("mMaximumWidth", this.mMaximumWidth);
        state.putInt("mMaximumHeight", this.mMaximumHeight);
        state.putInt("mFillingArea", this.mFillingArea.ordinal());
        state.putInt("mFillingMode", this.mFillingMode.ordinal());

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
        this.mMaximumWidth = savedState.getInt("mMaximumWidth");
        this.mMaximumHeight = savedState.getInt("mMaximumHeight");
        this.mFillingArea = FillingArea.values()[savedState.getInt("mFillingArea")];
        this.mFillingMode = FillingMode.values()[savedState.getInt("mFillingMode")];
    }


    // ***************************************************************************************
    // Public

    /**
     * Return true is the path is touched.
     *
     * @return the pressure status
     */
    @SuppressWarnings("unused")
    public boolean isPressed() {
        return this.mPathIsTouched;
    }


    // ***************************************************************************************
    // Features

    /**
     * Add one feature to this drawer.
     *
     * @param feature the new feature to add to the drawer
     */
    @SuppressWarnings("unused")
    public void addFeature(ScFeature feature) {
        // Check for null value
        if (feature == null) return;

        // Check if the holder is null
        if (this.mFeatures == null) {
            // Create an empty list
            this.mFeatures = new ArrayList<>();
        }

        // Check if already in
        if (!this.mFeatures.contains(feature)) {
            // Add the feature and refresh the component
            this.mFeatures.add(feature);
            this.forceLayout();
            this.invalidate();
        }
    }

    /**
     * Add one feature to this drawer.
     * This particular overload instantiate a new object from the class reference passed.
     * The passed class reference must implement the ScFeature interface.
     *
     * @param classRef the class reference to instantiate
     * @return the new feature object
     */
    @SuppressWarnings("unused")
    public ScFeature addFeature(Class<?> classRef) {
        // Manage the possible exception
        try {
            // Try to instantiate a new class
            ScFeature feature = (ScFeature) classRef
                    .getDeclaredConstructor(Path.class)
                    .newInstance(this.mCopyPath);

            // Call the base method and return the new object
            this.addFeature(feature);
            return feature;

        } catch (Exception ex) {
            // If the passed class reference not inherit from the ScFeature return null.
            // Maybe better to thrown a exception but this mean manage the exception on the
            // method declaration.
            return null;
        }
    }

    /**
     * Remove a feature from this drawer.
     *
     * @param feature the feature to remove
     * @return true if removed
     */
    @SuppressWarnings("unused")
    public boolean removeFeature(ScFeature feature) {
        // Check if the feature list contain this
        if (this.mFeatures != null && this.mFeatures.contains(feature)) {
            // Remove and return true
            boolean result = this.mFeatures.remove(feature);
            this.forceLayout();
            this.invalidate();
            return result;
        }
        // Else return false
        return false;
    }

    /**
     * Remove all feature from this drawer.
     */
    @SuppressWarnings("unused")
    public void removeAllFeatures() {
        // Check if the feature list contain this
        if (this.mFeatures != null) {
            // Remove all and refresh the component
            this.mFeatures.clear();
            this.forceLayout();
            this.invalidate();
        }
    }

    /**
     * Find all features that corresponds to a class and tag reference.
     * If the class reference is null the class will be not consider.
     * Same behavior for the tag param.
     *
     * @param classRef the class reference to compare
     * @param tag      the tag reference to compare
     * @return the features found
     */
    @SuppressWarnings("unused")
    public List<ScFeature> findFeatures(Class<?> classRef, String tag) {
        // Holder
        List<ScFeature> founds = new ArrayList<>();

        // Check for empty value
        if (this.mFeatures != null) {
            // Cycle all features
            for (ScFeature feature : this.mFeatures) {
                // Check the instance or add all features if the class reference is null
                if ((classRef == null || feature.getClass().isAssignableFrom(classRef)) &&
                        tag == null || feature.getTag().equalsIgnoreCase(tag)) {
                    // Add the feature to the list
                    founds.add(feature);
                }
            }
        }
        // Return the founds list
        return founds;
    }

    /**
     * Find the feature searching by tag.
     * If found something return the first element found.
     * If the tag param is null return the first feature found avoid the comparison check.
     *
     * @param tag the tag reference
     * @return the found feature
     */
    @SuppressWarnings("unused")
    public ScFeature findFeature(String tag) {
        // Get all the features of this class
        List<ScFeature> features = this.findFeatures(null, tag);
        // If here mean not find correspondence with tag
        return features.size() > 0 ? features.get(0) : null;
    }

    /**
     * Find the feature searching by the class.
     * If found something return the first element found.
     * If the class param is null return the first feature found avoid the comparison check.
     *
     * @param classRef the class reference
     * @return the found feature
     */
    @SuppressWarnings("unused")
    public ScFeature findFeature(Class<?> classRef) {
        // Get all the features of this class
        List<ScFeature> features = this.findFeatures(classRef, null);
        // If here mean not find correspondence with tag
        return features.size() > 0 ? features.get(0) : null;
    }

    /**
     * Find all feature tagged as param and move they at the end of the list so will draw for
     * last (on top).
     *
     * @param tag the tag reference
     */
    @SuppressWarnings("unused")
    public void bringOnTop(String tag) {
        // Find all features
        List<ScFeature> features = this.findFeatures(null, tag);
        // If exists and have at least one
        if (features != null && features.size() > 0) {
            // Remove all features from list
            this.mFeatures.removeAll(features);
            // Add all features at the end of the list
            this.mFeatures.addAll(features);
        }
    }

    /**
     * Find all feature that inherit from class param and move they at the end of the list so will
     * draw for last (on top).
     *
     * @param classRef the class reference
     */
    @SuppressWarnings("unused")
    public void bringOnTop(Class<?> classRef) {
        // Find all features
        List<ScFeature> features = this.findFeatures(classRef, null);
        // If exists and have at least one
        if (features != null && features.size() > 0) {
            // Remove all features from list
            this.mFeatures.removeAll(features);
            // Add all features at the end of the list
            this.mFeatures.addAll(features);
        }
    }

    /**
     * Find the feature and move it at the end of the list so will draw for last (on top).
     *
     * @param feature the feature
     */
    @SuppressWarnings("unused")
    public void bringOnTop(ScFeature feature) {
        // Check for empty value
        if (feature != null && this.mFeatures.contains(feature)) {
            // Remove the feature
            this.mFeatures.remove(feature);
            this.mFeatures.add(feature);
        }
    }


    // ***************************************************************************************
    // User input interface

    /**
     * Called when the path is touched.
     *
     * @param distance the distance from the path start
     */
    protected void onPathTouch(float distance) {
        // Disable the parent touch
        this.getParent().requestDisallowInterceptTouchEvent(true);

        // Call the event if need
        if (this.mOnPathTouchListener != null) {
            this.mOnPathTouchListener.onTouch(distance);
        }
    }

    /**
     * Called when the user release the touch after than he touched the path.
     */
    protected void onPathRelease() {
        // Enable the parent touch
        this.getParent().requestDisallowInterceptTouchEvent(false);

        // Call the event if need
        if (this.mOnPathTouchListener != null) {
            this.mOnPathTouchListener.onRelease();
        }
    }

    /**
     * Called when, after a path touch, the user move the finger on the component.
     *
     * @param distance the distance from the path start
     */
    protected void onPathSlide(float distance) {
        // Call the event if need
        if (this.mOnPathTouchListener != null) {
            this.mOnPathTouchListener.onSlide(distance);
        }
    }


    // ***************************************************************************************
    // Public properties

    /**
     * Return the current area filling type.
     * This setting decide how the path will fit the drawing area.
     * If the setting is different from FillingArea.NONE the path will stretched to fit the
     * dimension specified.
     *
     * @return the filling area type
     */
    @SuppressWarnings("unused")
    public FillingArea getFillingArea() {
        return this.mFillingArea;
    }

    /**
     * Set the current area filling type.
     * This setting decide how the path will fit the drawing area.
     * If the setting is different from FillingArea.NONE the path will stretched to fit the
     * dimension specified.
     *
     * @param value set filling area type
     */
    @SuppressWarnings("unused")
    public void setFillingArea(FillingArea value) {
        // Check if value is changed
        if (this.mFillingArea != value) {
            // Store the new value and refresh the component
            this.mFillingArea = value;
            this.requestLayout();
        }
    }

    /**
     * Get the current area filling mode.
     * This setting tell to the class how drawing the path on canvas:
     * - DRAW: Scale and transpose the path and after draw it on canvas
     * - STRETCH: Scale and transpose the canvas and after draw the path on it.
     *
     * @return get the current filling mode
     */
    @SuppressWarnings("unused")
    public FillingMode getFillingMode() {
        return this.mFillingMode;
    }

    /**
     * Set the current area filling mode.
     * This setting tell to the class how drawing the path on canvas:
     * - DRAW: Scale and transpose the path and after draw it on canvas
     * - STRETCH: Scale and transpose the canvas and after draw the path on it.
     *
     * @param value the new filling mode
     */
    @SuppressWarnings("unused")
    public void setFillingMode(FillingMode value) {
        // Check if value is changed
        if (this.mFillingMode != value) {
            // Store the new value and refresh the component
            this.mFillingMode = value;
            this.requestLayout();
        }
    }

    /**
     * Return the maximum width of the component
     *
     * @return the actual maximum value
     */
    @SuppressWarnings("unused")
    public int getMaximumWidth() {
        return this.mMaximumWidth;
    }

    /**
     * Set the maximum width of the component
     *
     * @param value the new maximum value in pixel
     */
    @SuppressWarnings("unused")
    public void setMaximumWidth(int value) {
        // Check if value is changed
        if (this.mMaximumWidth != value) {
            // Store the new value
            this.mMaximumWidth = value;
            // Check and refresh the component
            this.checkValues();
            this.requestLayout();
        }
    }

    /**
     * Return the maximum height of the component
     *
     * @return the new maximum value in pixel
     */
    @SuppressWarnings("unused")
    public int getMaximumHeight() {
        return this.mMaximumHeight;
    }

    /**
     * Set the maximum height of the component
     *
     * @param value the new maximum value in pixel
     */
    @SuppressWarnings("unused")
    public void setMaximumHeight(int value) {
        // Check if value is changed
        if (this.mMaximumHeight != value) {
            // Store the new value
            this.mMaximumHeight = value;
            // Check and refresh the component
            this.checkValues();
            this.requestLayout();
        }
    }

    /**
     * Return if the input is enabled.
     *
     * @return the current input status
     */
    @SuppressWarnings("unused")
    public boolean getRecognizePathTouch() {
        return this.mRecognizePathTouch;
    }

    /**
     * Set the input status
     *
     * @param value the new input status
     */
    @SuppressWarnings("unused")
    public void setRecognizePathTouch(boolean value) {
        // Check if value is changed
        if (this.mRecognizePathTouch != value) {
            this.mRecognizePathTouch = value;
            this.invalidate();
        }
    }

    /**
     * Return the recognize threshold for find the point on path.
     *
     * @return the threshold value
     */
    @SuppressWarnings("unused")
    public float getPathTouchThreshold() {
        return this.mPathTouchThreshold;
    }

    /**
     * Set the recognize threshold for find the point on path.
     *
     * @param value the threshold value
     */
    @SuppressWarnings("unused")
    public void setPathTouchThreshold(float value) {
        this.mPathTouchThreshold = value;
    }


    // ***************************************************************************************
    // Public listener and interface

    /**
     * Generic event listener
     */
    @SuppressWarnings("unused")
    public interface OnPathTouchListener {

        /**
         * Called when the path is touched.
         *
         * @param distance the distance from the path start
         */
        void onTouch(float distance);

        /**
         * Called when the user release the pressure.
         */
        void onRelease();

        /**
         * Called when the user move the pressure on the screen.
         * This called only if before had a onTouch event.
         *
         * @param distance the distance from the path start
         */
        void onSlide(float distance);
    }

    /**
     * Set the generic event listener
     *
     * @param listener the listener
     */
    @SuppressWarnings("unused")
    public void setOnPathTouchListener(OnPathTouchListener listener) {
        this.mOnPathTouchListener = listener;
    }

}
