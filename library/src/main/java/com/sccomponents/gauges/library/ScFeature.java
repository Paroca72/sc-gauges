package com.sccomponents.gauges.library;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.Log;

import java.util.Arrays;


/**
 * Create a base feature for draw on a given path.
 * <p>
 * The feature is independent and can be used with any path. Is enough to instantiate it passing
 * the path object and call the draw function passing the canvas where draw.
 * The original design of this class was for link it with the ScDrawer to have a base drawer and
 * many features applicable to it.
 * The "feature" base class essentially do nothing. For draw something, hence for specialize the
 * feature, you need to override the onDraw method.
 * The base class provides only a common set of methods to display something on the path as the
 * color manager, visibility, limits, ecc. that is useful to inherit it and create a specialized
 * class.
 * This class allow to consider the path as whole or each contours inside the master path. If
 * enable to consider the contours the method onDraw will called for each contour within the path.
 * Also many method (eg: getPointAndAngle) will related to the current contour (and not the
 * global path) when called inside the drawing period.
 * @author Samuele Carassai
 * @version 3.5.0
 * @since 2016-05-26
 */
@SuppressWarnings({"FieldMayBeFinal"})
public abstract class ScFeature {

    // ***************************************************************************************
    // Enumerators

    /**
     * The mode to building the painter shader.
     */
    @SuppressWarnings("unuse")
    public enum ColorsMode {
        SOLID,
        GRADIENT
    }

    /**
     * The mode to building the painter width.
     */
    @SuppressWarnings("unuse")
    public enum WidthsMode {
        ROUGH,
        SMOOTH
    }

    /**
     * Define the position respect path
     */
    @SuppressWarnings("unused")
    public enum Positions {
        INSIDE,
        MIDDLE,
        OUTSIDE
    }


    // ***************************************************************************************
    // Private variable

    private ScPathMeasure mPathMeasure;
    private String mTag;
    private Paint mPaint;
    private int[] mColors;
    private Positions mPosition;
    private ColorsMode mColorsMode;
    private boolean mConsiderContours;
    private boolean mVisible;
    private float mStartPercentage;
    private float mEndPercentage;
    private int mContourIndex;
    private boolean mIsDrawing;
    private ContourInfo mContourInfo;
    private Bitmap mBuffer;
    private Canvas mCanvas;
    private boolean mDoubleBuffering;

    // Listeners
    private OnDrawContourListener mOnDrawListener;
    private OnPropertyChangedListener mOnPropertyChangedListener;

    // Generic holder
    private float[] mGenericTangent;


    /****************************************************************************************
     * Constructor
     */

    @SuppressWarnings({"unused"})
    public ScFeature() {
        // Init
        this.mColorsMode = ColorsMode.GRADIENT;
        this.mPosition = Positions.MIDDLE;
        this.mContourInfo = new ContourInfo();

        this.mVisible = true;
        this.mIsDrawing = false;
        this.mStartPercentage = 0.0f;
        this.mEndPercentage = 100.0f;

        this.mGenericTangent = new float[2];

        this.mContourIndex = 1;
        this.mConsiderContours = false;

        this.mBuffer = null;
        this.mCanvas = new Canvas();
        this.mPathMeasure = new ScPathMeasure();
        this.mDoubleBuffering = true;

        // Create the painter
        this.mPaint = new Paint();
        this.mPaint.setStrokeCap(Paint.Cap.BUTT);
        this.mPaint.setStyle(Paint.Style.STROKE);
        this.mPaint.setColor(Color.BLACK);
        this.mPaint.setAntiAlias(true);
    }


    // ***************************************************************************************
    // Methods to override

    /**
     * Get the current contour drawing info.
     * This methods must be overridden for create custom drawing info for inherited
     * classes.
     * @param contour   the contour index
     * @return          the contour drawing info
     */
    @SuppressWarnings({"unused"})
    protected ContourInfo getContourInfo(int contour) {
        this.mContourInfo.reset(this, contour);
        return this.mContourInfo;
    }

    /**
     * The draw method to override in the inherited classes.
     * @param canvas    where draw
     * @param info      the contour info
     */
    @SuppressWarnings({"unused"})
    protected abstract void onDraw(Canvas canvas, ContourInfo info);


    // ***************************************************************************************
    // Private methods

    /**
     * Check if two strings are equal considering the null too.
     * @param a first
     * @param b second
     * @return  true is equals
     */
    @SuppressWarnings("all")
    protected boolean equals(String a, String b) {
        if (a == null) return b == null;
        return a.equals(b);
    }

    /**
     * Limit the value within the passed range of percentage value range.
     * Note that if the start value is over the end one will swapped in the right order.
     * @param value the value to limit
     * @return      the limited value
     */
    @SuppressWarnings({"unused"})
    protected float range(float value) {
        // Check the limit
        if (Float.compare(value, 0.0f) == -1) return 0.0f;
        if (Float.compare(value, 100.0f) == 1) return 100.0f;
        return value;
    }

    /**
     * Create a clone of a point
     * @param source      the source point to clone
     * @param destination the destination point
     */
    @SuppressWarnings({"unused"})
    protected void clonePoint(float[] source, float[] destination) {
        destination[0] = source[0];
        destination[1] = source[1];
    }

    /**
     * Given an array of colors calculate the right color by a ratio.
     * The color can be smooth or rough.
     * @param colors    the source
     * @param ratio     the ratio
     * @param isSmooth  the type of calculation
     * @return          the color
     */
    @SuppressWarnings({"unused"})
    protected int getColor(int[] colors, float ratio, boolean isSmooth) {
        // Check
        if (colors == null)
            return this.getPainter().getColor();

        if (ratio <= 0 || colors.length == 1)
            return colors[0];
        if (ratio >= 1)
            return colors[colors.length - 1];

        // Smooth value
        if (isSmooth) {
            // Calc the sector
            float position = ((colors.length - 1) * ratio);
            int sector = (int) position;
            ratio = position - sector;

            // Get the color to mix
            int sColor = colors[sector];
            int eColor = colors[sector + 1];

            // Manage the transparent case
            if (sColor == Color.TRANSPARENT)
                sColor = Color.argb(0, Color.red(eColor), Color.green(eColor), Color.blue(eColor));
            if (eColor == Color.TRANSPARENT)
                eColor = Color.argb(0, Color.red(sColor), Color.green(sColor), Color.blue(sColor));

            // Calculate the result color
            int alpha = (int) (Color.alpha(eColor) * ratio + Color.alpha(sColor) * (1 - ratio));
            int red = (int) (Color.red(eColor) * ratio + Color.red(sColor) * (1 - ratio));
            int green = (int) (Color.green(eColor) * ratio + Color.green(sColor) * (1 - ratio));
            int blue = (int) (Color.blue(eColor) * ratio + Color.blue(sColor) * (1 - ratio));

            // Get the color
            return Color.argb(alpha, red, green, blue);

        } else {
            // Rough value
            int sector = (int) (colors.length * ratio);
            return colors[sector];
        }
    }

    /**
     * Given an array of values calculate the right value by a ratio.
     * The value can be smooth or rough.
     * @param values        the source
     * @param ratio         the ratio
     * @param isSmooth      the type of calculation
     * @param defaultValue  the default value
     * @return              the value
     */
    @SuppressWarnings("SameParameterValue")
    protected float getValue(float[] values, float ratio, boolean isSmooth, float defaultValue) {
        // Check
        if (values == null)
            return defaultValue;

        if (ratio <= 0 || values.length == 1)
            return values[0];
        if (ratio >= 1)
            return values[values.length - 1];

        // Smooth value
        if (isSmooth) {
            // Calc the sector
            float position = ((values.length - 1) * ratio);
            int sector = (int) position;
            ratio = position - sector;

            // Get the color to mix
            float sValue = values[sector];
            float eValue = values[sector + 1];

            // Get the color
            return eValue * ratio + sValue * (1 - ratio);

        } else {
            // Rough value
            int sector = (int) (values.length * ratio);
            return values[sector];
        }
    }

    /**
     * Given an array of strings calculate the right value by a ratio.
     * @param values        the source
     * @param ratio         the ratio
     * @return              the value
     */
    @SuppressWarnings({"unused"})
    protected String getString(String[] values, float ratio) {
        // Check
        if (values == null)
            return null;

        if (ratio <= 0 || values.length == 1)
            return values[0];
        if (ratio >= 1)
            return values[values.length - 1];

        // Rough value
        int sector = (int) (values.length * ratio);
        return values[sector];
    }

    /**
     * Draw a single contour.
     * This method is implemented just for give the possibility to override it for some
     * future application.
     * @param canvas    where to draw
     * @param info      the current contour info
     * @hide
     */
    protected void drawContour(Canvas canvas, ContourInfo info) {
        // Rotate, translate and scale
        RectF bounds = this.getMeasure().getBounds();
        canvas.rotate(info.angle, bounds.centerX(), bounds.centerY());
        canvas.translate(info.offset[0], info.offset[1]);
        canvas.scale(info.scale[0], info.scale[1], bounds.centerX(), bounds.centerY());

        // Draw
        this.onDraw(canvas, info);
    }

    /**
     * Draw all contours.
     * @param canvas where to draw
     */
    private void drawContours(Canvas canvas) {
        // Cycle all contours
        int contour = 1;
        do {
            // Save the current contour index as need to have it globally.
            // The current contour will used to get the current path measurer in case we treat
            // the path in separate contours.
            this.mContourIndex = contour;

            // Prepare the info objects
            ContourInfo info = this.getContourInfo(contour);

            // Call the base listener
            if (this.mOnDrawListener != null)
                this.mOnDrawListener.onDrawContour(this, info);

            // Check for visibility
            if (!info.visible)
                continue;

            // Call the draw for the single contour
            canvas.save();
            this.drawContour(canvas, info);
            canvas.restore();

        } while (this.mPathMeasure.nextContour());

        // Reset the contour index
        this.mContourIndex = 1;
    }

    /**
     * Free the double buffering bitmap memory and all the related objects.
     */
    @SuppressWarnings({"unused"})
    protected void freeBitmapMemory() {
        // Free memory
        if (this.mBuffer != null)
            this.mBuffer.recycle();

        this.mBuffer = null;
    }

    /**
     * Proxy for call the property change event
     * @param name  the property name
     * @param value the property value
     * @hide
     */
    protected void onPropertyChange(String name, Object value) {
        // Need to redraw the bitmap
        this.freeBitmapMemory();

        // Listener
        if (this.mOnPropertyChangedListener != null)
            this.mOnPropertyChangedListener.onPropertyChanged(this, name, value);
    }

    /**
     * Get the index path measurer and if not exists create and store it.
     * In this case we will use the PathMeasurer class as we need to treat just
     * one unique path.
     * @param contour the current contour
     * @return the measurer
     */
    @SuppressWarnings({"unused"})
    protected ScPathMeasure getMeasure(int contour) {
        // Move the contour and check the limit
        float result = this.mPathMeasure.moveToContour(contour);
        if (result == -1)
            throw new IndexOutOfBoundsException();

        // Return the contour measurer
        return this.mPathMeasure;
    }

    /**
     * Get back the current path measurer.
     * If not drawing and not consider the contours by class settings the method will back
     * the global measurer otherwise will back the measurer related at the current contour.
     * @return the measurer
     */
    @SuppressWarnings({"unused"})
    protected ScPathMeasure getMeasure() {
        // Select the case
        if (this.mIsDrawing && this.mConsiderContours)
            // Back the current path measurer
            return this.getMeasure(this.mContourIndex);
        else
            // Back the global measurer
            return this.mPathMeasure;
    }

    /**
     * Get the trimmed current path based on the current start and end limit.
     * @param path the path trimmed
     */
    @SuppressWarnings("unused")
    protected void getTrimmedPath(Path path) {
        // Convert the percentage values in distance referred to the current path height.
        float startDistance = this.getStartAtDistance();
        float endDistance = this.getEndToDistance();

        // Trim a new segment and save it inside the path
        ScPathMeasure measurer = this.getMeasure();
        if (measurer != null)
            measurer.getSegment(startDistance, endDistance, path, true);
    }

    /**
     * Try to allocate a bitmap
     */
    @SuppressWarnings("unused")
    private Bitmap createBitmap(Canvas canvas) {
        try {
            return Bitmap.createBitmap(
                canvas.getWidth(),
                canvas.getHeight(),
                Bitmap.Config.ARGB_8888
            );

        } catch (Exception ex) {
            Log.d("ScFeature", "createBitmap: no memory");
            return null;
        }
    }


    // ***************************************************************************************
    // Public and static methods

    /**
     * Implement a copy of this object
     * @param destination the destination object
     */
    @SuppressWarnings("unused")
    public void copy(ScFeature destination) {
        // Check for empty values
        if (destination == null)
            return;

        // Set
        destination.setTag(this.mTag);
        destination.getPainter().set(this.mPaint);

        if (this.mColors != null)
            destination.setColors(this.mColors.clone());

        destination.setPosition(this.mPosition);
        destination.setColorsMode(this.mColorsMode);
        destination.setConsiderContours(this.mConsiderContours);
        destination.setVisible(this.mVisible);
        destination.setStartAt(this.mStartPercentage);
        destination.setEndTo(this.mEndPercentage);
    }

    /**
     * Draw something on the canvas.
     * @param canvas where draw
     * @param matrix to apply at canvas
     */
    @SuppressWarnings("unused")
    public void draw(Canvas canvas, Path path, Matrix matrix) {
        // Check the domain
        if (canvas == null || canvas.getWidth() == 0 || canvas.getHeight() == 0 ||
                !this.mVisible || path == null)
            return;

        // Is drawing
        this.mIsDrawing = true;

        // Redraw only if request
        if (this.mBuffer == null) {
            // Reset measurer
            // Reset
            this.mPathMeasure.setPath(path, false);

            // Try to create the bitmap for double buffering
            if (this.mDoubleBuffering)
                this.mBuffer = this.createBitmap(canvas);

            // Define the bitmap canvas if able to use double buffering
            if (this.mBuffer != null)
                this.mCanvas.setBitmap(this.mBuffer);
            else
                // Is impossible to use the double buffering so will write
                // directly on the master canvas.
                this.mCanvas = canvas;

            // Apply the matrix
            if (matrix != null)
                this.mCanvas.setMatrix(matrix);

            // If the have only one color inside the colors array set it directly on the painter
            if (this.mColors != null && this.mColors.length == 1)
                this.mPaint.setColor(this.mColors[0]);

            // Draw the contours
            this.drawContours(this.mCanvas);
        }

        // Draw the buffer on the canvas only if exists
        if (this.mBuffer != null)
            canvas.drawBitmap(this.mBuffer, 0, 0, null);

        // Not drawing
        this.mIsDrawing = false;
    }

    /**
     * Draw something on the canvas.
     * @param canvas where draw
     */
    @SuppressWarnings("unused")
    public void draw(Canvas canvas, Path path) {
        this.draw(canvas, path, null);
    }

    /**
     * Refresh the feature measure.
     */
    @SuppressWarnings("unused")
    public void refresh() {
        // Force redraw
        this.freeBitmapMemory();

        // Need to refresh the measure
        this.mPathMeasure = new ScPathMeasure();
    }

    /**
     * Return a path point coordinates and tangent angle given the distance from the path start.
     * @param distance the point distance from path start
     * @param point    the array where will save the point coordinates
     * @return the tangent angle in degrees
     */
    @SuppressWarnings("unused")
    public float getPointAndAngle(float distance, float[] point) {
        // Find the tangent
        this.getMeasure().getPosTan(distance, point, this.mGenericTangent);

        // Convert calculated angle to degrees and return
        float angle = this.mGenericTangent == null ?
                0.0f : (float) Math.atan2(this.mGenericTangent[1], this.mGenericTangent[0]);
        return (float) Math.toDegrees(angle);
    }

    /**
     * Return a path point coordinates given the distance from the path start.
     * @param distance the point distance from path start
     * @param point    the array where will save the point coordinates
     */
    @SuppressWarnings("unused")
    public void getPoint(float distance, float[] point) {
        this.getPointAndAngle(distance, point);
    }

    /**
     * Get the tangent angle in degrees of the tangent to a point on the path given the distance
     * from the start of path.
     * @param distance the distance
     * @return the tangent angle in degrees
     */
    @SuppressWarnings("unused")
    public float getAngle(float distance) {
        return this.getPointAndAngle(distance, null);
    }

    /**
     * Given a percentage return the relative distance from the path start.
     * @param percentage the percentage of the path
     * @return the distance
     */
    @SuppressWarnings({"unused"})
    public float getDistance(float percentage) {
        // Holders
        float length = this.getMeasure().getLength();
        percentage = this.range(percentage);

        // Calc the distance
        if (percentage == 0) return 0.0f;
        if (percentage == 100) return length;
        return length * percentage / 100.0f;
    }

    /**
     * Get the current gradient color dependently from the distance from the starting of path,
     * the colors array and the mode to draw. If the colors are not defined will be returned
     * the current color of painter.
     * @param distance from the path start
     * @param length   force the height of the path
     * @return the color
     */
    @SuppressWarnings({"unused"})
    public int getGradientColor(float distance, float length) {
        return this.getColor(
                this.mColors,
                distance / length,
                this.mColorsMode == ColorsMode.GRADIENT
        );
    }

    /**
     * Get the current gradient color dependently from the distance from the starting of path,
     * the colors array and the mode to draw. If the colors are not defined will be returned
     * the current color of painter.
     * @param distance from the starting path
     * @return the color
     */
    @SuppressWarnings({"unused"})
    public int getGradientColor(float distance) {
        return this.getGradientColor(distance, this.getMeasure().getLength());
    }

    /**
     * Drawing time meaning while the class will finish to call the onDraw method.
     * If not drawing the getMeasure() will return always the global path measurer.
     * @return true if drawing
     */
    @SuppressWarnings("unused")
    public boolean isDrawing() {
        return this.mIsDrawing;
    }

    /**
     * Get the start limit distance from the path start
     * @return the distance
     */
    @SuppressWarnings({"unused"})
    public float getStartAtDistance() {
        return this.getDistance(this.mStartPercentage);
    }

    /**
     * Get the end limit distance from the path start
     * @return the distance
     */
    @SuppressWarnings({"unused"})
    public float getEndToDistance() {
        return this.getDistance(this.mEndPercentage);
    }


    // ***************************************************************************************
    // Getter and setter

    /**
     * Set the painter
     * @param value the painter
     */
    @SuppressWarnings("unused")
    public void setPainter(Paint value) {
        this.mPaint = value;
        this.onPropertyChange("paint", value);
    }

    /**
     * Get the painter
     * @return the painter
     */
    @SuppressWarnings({"unused"})
    public Paint getPainter() {
        return this.mPaint;
    }


    /**
     * Set the tag. The tag is useful to find the features inside, for example, a ScGauge.
     * @param value the tag
     */
    @SuppressWarnings({"unused"})
    public void setTag(String value) {
        if (!this.equals(this.mTag, value)) {
            this.mTag = value;
            this.onPropertyChange("tag", value);
        }
    }

    /**
     * Get the tag.
     * @return the tag
     */
    @SuppressWarnings({"unused"})
    public String getTag() {
        return this.mTag;
    }


    /**
     * Set the double buffering status.
     * <p>
     * If true the feature will store a bitmap of the draw for increase the performance.
     * This bitmap will lost if some feature properties will change or calling the refresh method.
     * <p>
     * This could be very expensive in term of memory when are using many drawer in the same
     * activity. In these kind of cases its recommended to disable the double buffering.
     * @param value the status
     */
    @SuppressWarnings({"unused"})
    public void setDoubleBuffering(boolean value) {
        if (this.mDoubleBuffering != value) {
            // Set
            this.mDoubleBuffering = value;

            // If false free the memory
            if (!this.mDoubleBuffering)
                this.freeBitmapMemory();

            // Event
            this.onPropertyChange("doubleBuffering", value);
        }
    }

    /**
     * Get the double buffering status.
     * <p>
     * If true the feature will store a bitmap of the draw for increase the performance.
     * This bitmap will lost if some feature properties will change or calling the refresh method.
     * <p>
     * This could be very expensive in term of memory when are using many drawer in the same
     * activity. In these kind of cases its recommended to disable the double buffering.
     * @return the status
     */
    @SuppressWarnings("unused")
    public boolean getDoubleBuffering() {
        return this.mDoubleBuffering;
    }


    /**
     * Set the visibility
     * @param value the visibility
     */
    @SuppressWarnings({"unused"})
    public void setVisible(boolean value) {
        if (this.mVisible != value) {
            this.mVisible = value;
            this.onPropertyChange("visible", value);
        }
    }

    /**
     * Get the visibility
     * @return the visibility
     */
    @SuppressWarnings({"unused"})
    public boolean getVisible() {
        return this.mVisible;
    }


    /**
     * Set the current stroke colors
     * @param values the new stroke colors
     */
    @SuppressWarnings({"unused"})
    public void setColors(int... values) {
        if (!Arrays.equals(this.mColors, values)) {
            this.mColors = values;
            this.onPropertyChange("colors", values);
        }
    }

    /**
     * Get the current stroke colors
     * @return the current stroke colors
     */
    @SuppressWarnings({"unused"})
    public int[] getColors() {
        return this.mColors;
    }


    /**
     * Set the colors filling mode.
     * You can have two way for draw the colors of the path: SOLID or GRADIENT.
     * @param value the new color filling mode
     */
    @SuppressWarnings({"unused"})
    public void setColorsMode(ColorsMode value) {
        if (this.mColorsMode != value) {
            this.mColorsMode = value;
            this.onPropertyChange("colorsMode", value);
        }
    }

    /**
     * Get the colors filling mode.
     * @return the color filling mode
     */
    @SuppressWarnings("unused")
    public ColorsMode getColorsMode() {
        return this.mColorsMode;
    }


    /**
     * Set the start percentage of the path height.
     * The point before this percentage should not be considered.
     * @param percentage the percentage
     */
    @SuppressWarnings({"unused"})
    public void setStartAt(float percentage) {
        if (this.mStartPercentage != percentage) {
            this.mStartPercentage = this.range(percentage);
            this.onPropertyChange("startAt", percentage);
        }
    }

    /**
     * Get the start percentage of the path height.
     * @return the start limit in percentage
     */
    @SuppressWarnings("unused")
    public float getStartAt() {
        return this.mStartPercentage;
    }


    /**
     * Set the end percentage of the path height.
     * The point after this percentage should not be considered.
     * @param percentage the percentage
     */
    @SuppressWarnings({"unused"})
    public void setEndTo(float percentage) {
        if (this.mEndPercentage != percentage) {
            this.mEndPercentage = this.range(percentage);
            this.onPropertyChange("endTo", percentage);
        }
    }

    /**
     * Get the end percentage of the path height.
     * @return the end limit in percentage
     */
    @SuppressWarnings("unused")
    public float getEndTo() {
        return this.mEndPercentage;
    }


    /**
     * If false consider the whole path and call onDraw just one time.
     * True for divide the path in sub-path (contours). In this case the onDraw method
     * will called for the number of the contours in path.
     * @param value default true
     */
    @SuppressWarnings({"unused"})
    public void setConsiderContours(boolean value) {
        if (this.mConsiderContours != value) {
            this.mConsiderContours = value;
            this.onPropertyChange("considerContours", value);
        }
    }

    /**
     * If false consider the whole path and call onDraw just one time.
     * True for divide the path in sub-path (contours). In this case the onDraw method
     * will called for the number of the contours in path.
     * @return true if consider the whole path
     */
    @SuppressWarnings({"unused"})
    public boolean getConsiderContours() {
        return this.mConsiderContours;
    }


    /**
     * Set the generic alignment respect the path.
     * @param value the notches alignment
     */
    @SuppressWarnings("unused")
    public void setPosition(Positions value) {
        if (this.mPosition != value) {
            this.mPosition = value;
            this.onPropertyChange("position", value);
        }
    }

    /**
     * Get the generic alignment respect the path.
     * @return the notches alignment
     */
    @SuppressWarnings("unused")
    public Positions getPosition() {
        return this.mPosition;
    }


    // ***************************************************************************************
    // Listeners and Interfaces

    /**
     * Define the draw listener interface
     */
    @SuppressWarnings("unused")
    public interface OnDrawContourListener {

        /**
         * Called before draw the contour.
         * @param feature the source object
         * @param info the feature info
         */
        void onDrawContour(ScFeature feature, ContourInfo info);

    }

    /**
     * Set the draw listener to call.
     * @param listener the linked method to call
     */
    @SuppressWarnings({"unused"})
    public void setOnDrawContourListener(OnDrawContourListener listener) {
        this.mOnDrawListener = listener;
    }


    /**
     * Define the property change listener interface
     */
    @SuppressWarnings("unused")
    public interface OnPropertyChangedListener {

        /**
         * Called before draw the path.
         * @param feature the source object
         * @param name  the property name
         * @param value the property value
         */
        void onPropertyChanged(ScFeature feature, String name, Object value);

    }

    /**
     * Set the property change listener to call.
     * @param listener the linked method to call
     */
    @SuppressWarnings({"unused"})
    public void setOnPropertyChangedListener(OnPropertyChangedListener listener) {
        this.mOnPropertyChangedListener = listener;
    }


    // ***************************************************************************************
    // Drawing info class

    /**
     * This is a structure to hold the feature information before draw a contour
     */
    @SuppressWarnings("InnerClassMayBeStatic")
    public class ContourInfo {

        // ***************************************************************************************
        // Properties

        public ScFeature source;
        public int contour;
        public ScFeature.Positions position;
        public boolean visible = true;

        public float angle;
        public float[] scale;
        public float[] offset;


        // ***************************************************************************************
        // Constructor

        public ContourInfo() {
            this.scale = new float[2];
            this.offset = new float[2];
        }

        // ***************************************************************************************
        // Public methods

        public void reset(ScFeature feature, int contour) {
            // Set the drawing info
            this.source = feature;
            this.contour = contour;

            this.angle = 0.0f;
            this.position = feature.getPosition();
            this.visible = feature.getVisible();

            this.scale[0] = 1.0f;
            this.scale[1] = 1.0f;

            this.offset[0] = 0.0f;
            this.offset[1] = 0.0f;
        }

    }


}
