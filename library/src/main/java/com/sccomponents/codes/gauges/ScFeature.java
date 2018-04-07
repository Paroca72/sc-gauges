package com.sccomponents.codes.gauges;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
 * @version 3.0.0
 * @since 2016-05-26
 */
public abstract class ScFeature {

    // ***************************************************************************************
    // Constants

    private static final int DEFAULT_PRECISION = 2;


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
    // Protected variable

    /** @hide */
    protected Path mPath;


    // ***************************************************************************************
    // Private variable

    private ScPathMeasure mPathMeasure;
    private String mTag;
    private Paint mPaint;
    private int[] mColors;
    private float[] mWidths;
    private Positions mPosition;
    private Positions mEdges;
    private ColorsMode mColorsMode;
    private WidthsMode mWidthsMode;
    private boolean mConsiderContours;
    private boolean mVisible;
    private float mStartPercentage;
    private float mEndPercentage;
    private int mContourIndex;
    private boolean mIsDrawing;
    private ContourInfo mContourInfo;
    private int mFloatComparisonPrecision;
    private boolean mNeedToRedraw;
    private Bitmap mBuffer;
    private Matrix mMatrix;

    // Listeners
    private OnDrawContourListener mOnDrawListener;
    private OnPropertyChangedListener mOnPropertyChangedListener;

    // Generic holder
    private ScPathMeasure[] mContoursMeasurer;
    private float[] mGenericTangent;


    /****************************************************************************************
     * Constructor
     */

    @SuppressWarnings("unused")
    public ScFeature() {
        // Init
        this.mColorsMode = ColorsMode.GRADIENT;
        this.mWidthsMode = WidthsMode.SMOOTH;
        this.mPosition = Positions.MIDDLE;
        this.mEdges = Positions.MIDDLE;
        this.mContourInfo = new ContourInfo();

        this.mVisible = true;
        this.mIsDrawing = false;
        this.mStartPercentage = 0.0f;
        this.mEndPercentage = 100.0f;

        this.mGenericTangent = new float[2];

        this.mContourIndex = 1;
        this.mConsiderContours = false;
        this.mContoursMeasurer = null;

        this.mFloatComparisonPrecision = DEFAULT_PRECISION;
        this.mNeedToRedraw = true;
        this.mBuffer = null;

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
    @SuppressWarnings("unused")
    protected ContourInfo getContourInfo(int contour) {
        this.mContourInfo.reset(this, contour);
        return this.mContourInfo;
    }

    /**
     * The draw method to override in the inherited classes.
     * @param canvas    where draw
     * @param info      the contour info
     */
    @SuppressWarnings("unused")
    protected abstract void onDraw(Canvas canvas, ContourInfo info);


    // ***************************************************************************************
    // Private methods

    /**
     * Compare two float given the decimal precision
     * @param first first number
     * @param second second number
     * @param precision precision
     * @return the comparison result
     */
    protected int compare(float first, float second, int precision) {
        BigDecimal firstDecimal = new BigDecimal(first).setScale(precision, RoundingMode.FLOOR);
        BigDecimal secondDecimal = new BigDecimal(second).setScale(precision, RoundingMode.FLOOR);
        return firstDecimal.compareTo(secondDecimal);
    }

    /**
     * @param first first number
     * @param second second number
     * @return the comparison result
     */
    protected int compare(float first, float second) {
        return this.compare(first, second, this.mFloatComparisonPrecision);
    }

    /**
     * Check if two strings are equal considering the null too.
     * @param a first
     * @param b second
     * @return  true is equals
     */
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
    protected float range(float value) {
        // Check the limit
        if (this.compare(value, 0.0f) == -1) return 0.0f;
        if (this.compare(value, 100.0f) == 1) return 100.0f;
        return value;
    }

    /**
     * Create a clone of a point
     * @param source      the source point to clone
     * @param destination the destination point
     */
    @SuppressWarnings("unused")
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

            // Calculate the result color
            int red = (int) (Color.red(eColor) * ratio + Color.red(sColor) * (1 - ratio));
            int green = (int) (Color.green(eColor) * ratio + Color.green(sColor) * (1 - ratio));
            int blue = (int) (Color.blue(eColor) * ratio + Color.blue(sColor) * (1 - ratio));

            // Get the color
            return Color.rgb(red, green, blue);

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
    @SuppressWarnings("all")
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
     * @param canvas   where to draw
     * @param contours the contours list
     */
    private void drawContours(Canvas canvas, Path[] contours) {
        // Cycle all contours
        for (int contour = 1; contour <= contours.length; contour++) {
            // Save the current contour index as I need to have it globally.
            // The current contour will used to get the current path measurer in case we treat
            // the path in separate contours.
            this.mContourIndex = contour;

            // Prepare the info objects
            ContourInfo info = this.getContourInfo(contour);

            // Call the base listener
            if (this.mOnDrawListener != null)
                this.mOnDrawListener.onDrawContour(info);

            // Check for visibility
            if (!info.isVisible)
                continue;

            // Call the draw for the single contour
            canvas.save();
            this.drawContour(canvas, info);
            canvas.restore();
        }

        // Reset the contour index
        this.mContourIndex = 1;
    }

    /**
     * Proxy for call the property change event
     * @param name  the property name
     * @param value the property value
     * @hide
     */
    protected void onPropertyChange(String name, Object value) {
        // Force redraw
        this.mNeedToRedraw = true;

        // Listener
        if (this.mOnPropertyChangedListener != null)
            this.mOnPropertyChangedListener.onPropertyChanged(name, value);
    }

    /**
     * Get the index path measurer and if not exists create and store it.
     * In this case we will use the PathMeasurer class as we need to treat just
     * one unique path.
     * @param contour the current contour
     * @return the measurer
     */
    @SuppressWarnings("unused")
    protected ScPathMeasure getMeasure(int contour) {
        // Check the limit
        int len = this.mPathMeasure.getPaths().length;
        if (contour < 0 || contour > len)
            throw new IndexOutOfBoundsException();

        // Fix the base 0
        contour--;

        // Create the holder if not already created
        if (this.mContoursMeasurer == null)
            this.mContoursMeasurer = new ScPathMeasure[len];

        // Check if already exists
        if (this.mContoursMeasurer[contour] == null) {
            // Store the new measurer
            Path path = this.mPathMeasure.getPaths()[contour];
            ScPathMeasure measurer = new ScPathMeasure(path, false);
            this.mContoursMeasurer[contour] = measurer;
        }

        // Return the contour measurer
        return this.mContoursMeasurer[contour];
    }

    /**
     * Get back the current path measurer.
     * If not drawing and not consider the contours by class settings the method will back
     * the global measurer otherwise will back the measurer related at the current contour.
     * @return the measurer
     */
    @SuppressWarnings("unused")
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
        if (this.mWidths != null)
            destination.setWidths(this.mWidths.clone());

        destination.setPosition(this.mPosition);
        destination.setEdges(this.mEdges);
        destination.setColorsMode(this.mColorsMode);
        destination.setWidthsMode(this.mWidthsMode);
        destination.setConsiderContours(this.mConsiderContours);
        destination.setVisible(this.mVisible);
        destination.setStartAt(this.mStartPercentage);
        destination.setEndTo(this.mEndPercentage);
    }

    /**
     * Apply a matrix to canvas before draw on it
     * @param matrix the matrix
     */
    public void applyMatrixToCanvas(Matrix matrix) {
        this.mMatrix = matrix;
    }

    /**
     * Draw something on the canvas.
     * @param canvas where draw
     */
    @SuppressWarnings("unused")
    public void draw(Canvas canvas) {
        // Check the domain
        if (canvas == null || !this.mVisible || this.mPath == null)
            return;

        // Is drawing
        this.mIsDrawing = true;

        // Redraw only if request
        if (this.mNeedToRedraw) {
            // Define the bitmap canvas
            this.mBuffer = Bitmap.createBitmap(
                    canvas.getWidth(),
                    canvas.getHeight(),
                    Bitmap.Config.ARGB_8888
            );
            Canvas desk = new Canvas(this.mBuffer);
            desk.setMatrix(this.mMatrix);

            // If the have only one color inside the colors array set it directly on the painter
            if (this.mColors != null && this.mColors.length == 1)
                this.mPaint.setColor(this.mColors[0]);

            // Draw the contours
            this.mPathMeasure.setPath(this.mPath, false);
            Path[] contours = this.mConsiderContours ?
                    this.mPathMeasure.getPaths() : new Path[]{this.mPath};
            this.drawContours(desk, contours);
        }

        // Apply
        canvas.drawBitmap(this.mBuffer, 0, 0, null);

        // Not drawing
        this.mIsDrawing = false;
        this.mNeedToRedraw = false;
    }

    /**
     * Refresh the feature measure.
     */
    @SuppressWarnings("unused")
    public void refresh() {
        this.mContoursMeasurer = null;
        this.mNeedToRedraw = true;
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
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
    public int getGradientColor(float distance) {
        return this.getGradientColor(distance, this.getMeasure().getLength());
    }

    /**
     * Get the current width dependently from the distance from the starting of path and the
     * widths array. If the widths are not defined will be returned the current width of painter.
     * @param distance from the path start
     * @param length   force the height of the path
     * @return the width
     */
    @SuppressWarnings("unused")
    public float getWidth(float distance, float length) {
        return this.getValue(
                this.mWidths,
                distance / length,
                this.mWidthsMode == WidthsMode.SMOOTH,
                0.0f
        );
    }

    /**
     * Get the current gradient color dependently from the distance from the starting of path,
     * the colors array and the mode to draw. If the colors are not defined will be returned
     * the current color of painter.
     * @param distance from the path start
     * @return the color
     */
    @SuppressWarnings("unused")
    public float getWidth(float distance) {
        return this.getWidth(distance, this.getMeasure().getLength());
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
    @SuppressWarnings("unused")
    public float getStartAtDistance() {
        return this.getDistance(this.mStartPercentage);
    }

    /**
     * Get the end limit distance from the path start
     * @return the distance
     */
    @SuppressWarnings("unused")
    public float getEndToDistance() {
        return this.getDistance(this.mEndPercentage);
    }

    /**
     * Set the path
     * @param value the painter
     */
    @SuppressWarnings("unused")
    public void setPath(Path value) {
        this.mPath = value;
        this.mPathMeasure = new ScPathMeasure(this.mPath, false);
        this.onPropertyChange("path", value);
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
    @SuppressWarnings("unused")
    public Paint getPainter() {
        return this.mPaint;
    }


    /**
     * Set the tag. The tag is useful to find the features inside, for example, a ScGauge.
     * @param value the tag
     */
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
    public String getTag() {
        return this.mTag;
    }


    /**
     * Set the visibility
     * @param value the visibility
     */
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
    public boolean getVisible() {
        return this.mVisible;
    }


    /**
     * Set the current stroke colors
     * @param values the new stroke colors
     */
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
    public int[] getColors() {
        return this.mColors;
    }


    /**
     * Set the colors filling mode.
     * You can have two way for draw the colors of the path: SOLID or GRADIENT.
     * @param value the new color filling mode
     */
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
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

    /**
     * If false consider the whole path and call onDraw just one time.
     * True for divide the path in sub-path (contours). In this case the onDraw method
     * will called for the number of the contours in path.
     * @param value default true
     */
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
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


    /**
     * Set the current stroke widths
     * @param values the new stroke widths
     */
    @SuppressWarnings("unused")
    public void setWidths(float... values) {
        if (!Arrays.equals(this.mWidths, values)) {
            this.mWidths = values;
            this.onPropertyChange("widths", values);
        }
    }

    /**
     * Get the current stroke widths
     * @return the current stroke widths
     */
    @SuppressWarnings("unused")
    public float[] getWidths() {
        return this.mWidths;
    }


    /**
     * Set the widths filling mode.
     * You can have two way for manage the width of the path: SMOOTH or ROUGH.
     * @param value the new width filling mode
     */
    @SuppressWarnings("unused")
    public void setWidthsMode(WidthsMode value) {
        if (this.mWidthsMode != value) {
            this.mWidthsMode = value;
            this.onPropertyChange("widthsMode", value);
        }
    }

    /**
     * Get the widths filling mode.
     * @return the width filling mode
     */
    @SuppressWarnings("unused")
    public WidthsMode getWidthsMode() {
        return this.mWidthsMode;
    }

    /**
     * Set the float comparison precision.
     * As default is 2 decimal.
     * @param value the new precision
     */
    @SuppressWarnings("unused")
    public void setFloatComparisonPrecision(int value) {
        if (this.mFloatComparisonPrecision != value) {
            this.mFloatComparisonPrecision = value;
            this.onPropertyChange("floatComparisonPrecision", value);
        }
    }

    /**
     * Get the float comparison precision.
     * @return the current precision
     */
    @SuppressWarnings("unused")
    public int getFloatComparisonPrecision() {
        return this.mFloatComparisonPrecision;
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
         * @param info the feature info
         */
        void onDrawContour(ContourInfo info);

    }

    /**
     * Set the draw listener to call.
     * @param listener the linked method to call
     */
    @SuppressWarnings("unused")
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
         * @param name  the property name
         * @param value the property value
         */
        void onPropertyChanged(String name, Object value);

    }

    /**
     * Set the property change listener to call.
     * @param listener the linked method to call
     */
    @SuppressWarnings("unused")
    public void setOnPropertyChangedListener(OnPropertyChangedListener listener) {
        this.mOnPropertyChangedListener = listener;
    }


    // ***************************************************************************************
    // Drawing info class

    /**
     * This is a structure to hold the feature information before draw a contour
     */
    @SuppressWarnings("unused")
    public class ContourInfo {

        // ***************************************************************************************
        // Properties

        public ScFeature source;
        public int contour;
        public ScFeature.Positions position;
        public boolean isVisible = true;

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
            this.isVisible = feature.getVisible();

            this.scale[0] = 1.0f;
            this.scale[1] = 1.0f;

            this.offset[0] = 0.0f;
            this.offset[1] = 0.0f;
        }

    }


}
