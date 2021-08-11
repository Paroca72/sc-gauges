package com.sccomponents.gauges.library;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.util.Arrays;


/**
 * Create a series of notches that follow a path.
 * <p>
 * You can choose between some predefined shape (line, rectangle and oval) or by drawing a
 * passed bitmap.
 * All the notch are customizable before drawing it using the proper event.
 *
 * @author Samuele Carassai
 * @version 3.6.0
 * @since 2016-05-30
 */
@SuppressWarnings("FieldMayBeFinal")
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
        TRIANGLE,
        TRIANGLE_FILLED,
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

    private float[] mWidths;
    private WidthsMode mWidthsMode;
    private float[] mHeights;
    private HeightsMode mHeightsMode;

    private NotchTypes mType;
    private Bitmap mBitmap;
    private Drawable mDrawable;

    private int mLastColor;
    private float[] mFirstPoint;
    private float[] mSecondPoint;
    private RectF mGenericRect;
    private Path mGenericPath;
    private Paint mGenericPaint;
    private Canvas mGenericCanvas;

    private NotchInfo mRepetitionInfo;


    // ***************************************************************************************
    // Constructor

    @SuppressWarnings({"unused", "WeakerAccess"})
    public ScNotches() {
        // Super
        super();

        // Init
        this.mWidths = new float[]{0.0f};
        this.mWidthsMode = WidthsMode.SMOOTH;
        this.mHeights = new float[]{0.0f};
        this.mHeightsMode = HeightsMode.SMOOTH;

        this.mType = NotchTypes.LINE;
        this.mRepetitionInfo = new NotchInfo();

        this.mLastColor = Color.TRANSPARENT;
        this.mFirstPoint = new float[2];
        this.mSecondPoint = new float[2];
        this.mGenericRect = new RectF();
        this.mGenericPath = new Path();
        this.mGenericPaint = new Paint();
        this.mGenericCanvas = new Canvas();
    }


    // ***************************************************************************************
    // Utils methods

    /**
     * Convert a drawable to a bitmap
     * @param drawable source
     * @return the new bitmap
     */
    private Bitmap drawableToBitmap(Drawable drawable) {
        // Check
        if (drawable == null)
            return null;

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0)
            return null;

        // Check for BitmapDrawable since have already the bitmap representation inside
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null)
                return bitmapDrawable.getBitmap();
        }

        // Check the dimension
        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888
        );

        // Create the bitmap
        this.mGenericCanvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, this.mGenericCanvas.getWidth(), this.mGenericCanvas.getHeight());
        drawable.draw(this.mGenericCanvas);

        // Return
        return bitmap;
    }

    /**
     * Change the color of given bitmap
     * @param sourceBitmap source
     * @param color new color
     * @return new bitmap
     */
    private Bitmap changeBitmapColor(Bitmap sourceBitmap, int color) {
        // Define the painter
        ColorFilter filter = new LightingColorFilter(color, 1);
        this.mGenericPaint.setColorFilter(filter);

        // Copy the fixed color bitmap
        Bitmap resultBitmap = sourceBitmap.copy(sourceBitmap.getConfig(), true);
        this.mGenericCanvas.setBitmap(resultBitmap);
        this.mGenericCanvas.drawBitmap(resultBitmap, 0, 0, this.mGenericPaint);

        // Return
        return resultBitmap;
    }


    // ***************************************************************************************
    // Private methods

    /**
     * Draw on canvas a bitmap centered in the passed point.
     * Change the color of the bitmap could be very expensive in term of performance.
     * @param canvas where to draw
     * @param info   the pointer info
     */
    private void drawBitmap(Canvas canvas, NotchInfo info, Paint paint) {
        // Check for empty values
        if (info.bitmap == null)
            return;

        // Scale the original bitmap
        Bitmap scaled = info.bitmap;
        if (info.width != 0 && info.height != 0)
            scaled = Bitmap.createScaledBitmap(
                    info.bitmap,
                    (int) info.width,
                    (int) info.height,
                    false);

        // Adjust the first point
        this.mFirstPoint[0] -= scaled.getWidth() / 2.0f;
        this.mFirstPoint[1] -= scaled.getHeight() / 2.0f;

        // Change the bitmap color only if needs
        int currentColor = paint.getColor();
        if (this.mLastColor != currentColor) {
            // Save the last color
            this.mLastColor = currentColor;

            // Redraw the bitmap with the new color
            scaled = this.changeBitmapColor(scaled, currentColor);
        }

        // Print the bitmap centered respect the point
        canvas.drawBitmap(scaled, this.mFirstPoint[0], this.mFirstPoint[1], null);
    }

    /**
     * Draw a line.
     * @param canvas the canvas to draw
     * @param info   the notch info
     */
    protected void drawLine(Canvas canvas, NotchInfo info, Paint paint) {
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
     * Draw a triangle.
     * @param canvas the canvas to draw
     * @param info   the notch info
     */
    protected void drawTriangle(Canvas canvas, NotchInfo info, Paint paint) {
        // Holder
        float halfWidth = info.width / 2;
        float halfHeight = info.height / 2;

        float x = this.mFirstPoint[0];
        float y = this.mFirstPoint[1];

        // Create the shape path
        this.mGenericPath.reset();

        switch (info.position) {
            case INSIDE:
                this.mGenericPath.moveTo(x, y);
                this.mGenericPath.lineTo(x + halfWidth, y - info.height);
                this.mGenericPath.lineTo(x - halfWidth, y - info.height);
                this.mGenericPath.lineTo(x, y);
                break;

            case MIDDLE:
                this.mGenericPath.moveTo(x + halfWidth, y);
                this.mGenericPath.lineTo(x - halfWidth, y - halfHeight);
                this.mGenericPath.lineTo(x - halfWidth, y + halfHeight);
                this.mGenericPath.lineTo(x, y);
                break;

            case OUTSIDE:
                this.mGenericPath.moveTo(x, y);
                this.mGenericPath.lineTo(x + halfWidth, y + info.height);
                this.mGenericPath.lineTo(x - halfWidth, y + info.height);
                this.mGenericPath.lineTo(x, y);
                break;
        }

        // Draw
        canvas.drawPath(this.mGenericPath, paint);
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
                        info.type == NotchTypes.RECTANGLE_FILLED ||
                        info.type == NotchTypes.TRIANGLE_FILLED;

        Paint painter = this.getPainter();
        painter.setStyle(
                isFilled ? Paint.Style.FILL : Paint.Style.STROKE);

        // Set the stroke width
        painter.setStrokeWidth(info.width);

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
                this.drawBitmap(canvas, info, painter);
                break;

            // Draw a line
            case LINE:
                this.drawLine(canvas, info, painter);
                break;

            // Draw a circle
            case OVAL:
            case OVAL_FILLED:
                this.drawOval(canvas, info, painter);
                break;

            // Draw a square
            case RECTANGLE:
            case RECTANGLE_FILLED:
                this.drawRectangle(canvas, info, painter);
                break;

            // Draw a triangle
            case TRIANGLE:
            case TRIANGLE_FILLED:
                this.drawTriangle(canvas, info, painter);
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
     * Get the current width dependently from the distance from the starting of path and the
     * widths array. If the widths are not defined will be returned the current width of painter.
     * @param distance from the path start
     * @param length force the length of the path
     * @return the width
     */
    @SuppressWarnings({"unused", "WeakerAccess"})
    public float getWidth(float distance, float length) {
        return this.getValue(
                this.mWidths,
                distance / length,
                this.mWidthsMode == WidthsMode.SMOOTH,
                0.0f
        );
    }

    /**
     * Get the current width dependently from the distance from the starting of path,
     * the colors array and the mode to draw. If the width are not defined will be returned
     * the current width of painter.
     * @param distance from the path start
     * @return the color
     */
    @SuppressWarnings({"unused", "WeakerAccess"})
    public float getWidth(float distance) {
        return this.getWidth(distance, this.getMeasure().getLength());
    }

    /**
     * Get the current height dependently from the distance from the starting of path and the
     * widths array. If the height are not defined will be returned the current height of painter.
     * @param distance the distance
     * @param length force the length of the path
     * @return the height
     */
    @SuppressWarnings("unused")
    public float getHeight(float distance, float length) {
        return this.getValue(
                this.mHeights,
                distance / this.getMeasure().getLength(),
                this.mHeightsMode == HeightsMode.SMOOTH,
                0.0f
        );
    }

    /**
     * Get the current height dependently from the distance from the starting of path and the
     * widths array. If the height are not defined will be returned the current height of painter.
     * @param distance the distance
     * @return the height
     */
    @SuppressWarnings({"unused", "WeakerAccess"})
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
        if (this.mWidths != null)
            destination.setWidths(this.mWidths.clone());
        if (this.mHeights != null)
            destination.setHeights(this.mHeights.clone());

        destination.setWidthsMode(this.mWidthsMode);
        destination.setHeightsMode(this.mHeightsMode);
        destination.setType(this.mType);
        destination.setBitmap(this.mBitmap);
        destination.setDrawable(this.mDrawable);
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

    /**
     * Round the value near the closed notch.
     * @param value the value to round
     * @return      a rounded to notch value
     */
    @SuppressWarnings({"unused", "WeakerAccess"})
    public float snapToNotches(float value) {
        // Holders
        int notches = this.getRepetitions();
        float distance = notches == 0 ? value: Float.MAX_VALUE;

        // Check all points
        for (int index = 1; index <= notches + 1; index ++) {
            // Get the current notch value
            float current = this.getDistance(index);

            // Calculate the deltas
            float deltaDistance =  Math.abs(distance - value);
            float deltaCurrent = Math.abs(current - value);

            // Check
            if (deltaCurrent < deltaDistance)
                distance = current;
            if (deltaCurrent > deltaDistance)
                break;
        }

        return distance;
    }


    // ***************************************************************************************
    // Public properties

    /**
     * Set the current drawable than will displayed as notch.
     * This drawable will loaded as bitmap and can find the bitmap result calling
     * <code>getBitmap</code> function.
     * @param drawable the new resource
     */
    @SuppressWarnings({"unused", "WeakerAccess"})
    public void setDrawable(Drawable drawable) {
        // Reload only if the resource is changed
        if (this.mDrawable != drawable) {
            // Assign the resource
            this.mDrawable = drawable;

            // Set bitmap
            Bitmap bitmap = this.drawableToBitmap(drawable);
            this.setBitmap(bitmap);

            // Event
            this.onPropertyChange("resource", drawable);
        }
    }

    /**
     * Get the current bitmap
     * @return the current bitmap
     */
    @SuppressWarnings({"unused"})
    public Drawable getResource() {
        return this.mDrawable;
    }


    /**
     * Set the current bitmap.
     * @param value the new bitmap
     */
    @SuppressWarnings({"unused", "WeakerAccess"})
    public void setBitmap(Bitmap value) {
        this.mBitmap = value;
        this.onPropertyChange("bitmap", value);
    }

    /**
     * Get the current bitmap
     * @return the current bitmap
     */
    @SuppressWarnings({"unused", "WeakerAccess"})
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
    @SuppressWarnings({"unused", "WeakerAccess"})
    public float[] getWidths() {
        return this.mWidths;
    }


    /**
     * Set the widths filling mode.
     * You can have two way for manage the width of the path: SMOOTH or ROUGH.
     * @param value the new width filling mode
     */
    @SuppressWarnings({"unused", "WeakerAccess"})
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
    @SuppressWarnings({"unused", "WeakerAccess"})
    public float[] getHeights() {
        return this.mHeights;
    }


    /**
     * Set the lengths calculation mode.
     * You can have two way for calculate the lengths of the path: SMOOTH or ROUGH.
     * @param value the new height calculation mode
     */
    @SuppressWarnings({"unused", "WeakerAccess"})
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
    @SuppressWarnings({"unused", "WeakerAccess"})
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
    @SuppressWarnings({"unused", "WeakerAccess"})
    public class NotchInfo extends RepetitionInfo {

        // ***************************************************************************************
        // Properties

        public ScNotches source;
        public float width;
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
            this.width = feature.getWidth(this.distance);
            this.height = feature.getHeight(this.distance);
            this.type = feature.mType;
            this.bitmap = feature.getBitmap();
        }

    }

}
