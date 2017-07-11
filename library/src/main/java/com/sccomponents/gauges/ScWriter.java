package com.sccomponents.gauges;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;


/**
 * Write some token texts on the linked path.
 *
 * @author Samuele Carassai
 * @version 1.0.1
 * @since 2016-05-26
 */
public class ScWriter extends ScFeature {

    // ***************************************************************************************
    // Enumerators

    /**
     * Define the text position respect path
     */
    @SuppressWarnings("unused")
    public enum TokenPositions {
        INSIDE,
        MIDDLE,
        OUTSIDE
    }

    /**
     * Define the text alignment respect the owner path segment
     */
    @SuppressWarnings("unused")
    public enum TokenAlignments {
        CENTER,
        LEFT,
        RIGHT
    }


    // ***************************************************************************************
    // Private variables

    private Paint mPaintClone;

    private String[] mTokens;
    private TokenPositions mTokenPosition;
    private PointF mTokenOffset;

    private boolean mUnbend;
    private boolean mConsiderFontMetrics;
    private boolean mLastTokenOnEnd;

    private ScPathMeasure mSegmentMeasure;
    private OnDrawListener mOnDrawListener;


    /****************************************************************************************
     * Constructor
     */

    @SuppressWarnings("unused")
    public ScWriter(Path path) {
        // Super
        super(path);

        // Init
        this.mConsiderFontMetrics = true;
        this.mTokenPosition = TokenPositions.OUTSIDE;
        this.mTokenOffset = new PointF();
        this.mSegmentMeasure = new ScPathMeasure();

        // Update the painter
        this.mPaint.setStrokeWidth(0.0f);
        this.mPaint.setTextSize(16.0f);
        this.mPaint.setStyle(Paint.Style.FILL);

        this.mPaintClone = new Paint(this.mPaint);
    }


    // ***************************************************************************************
    // Private methods

    /**
     * Calculate the extra vertical offset by the text position respect to the path.
     *
     * @param info the token info
     * @return the extra vertical offset
     */
    private float getVerticalOffsetByPosition(TokenInfo info) {
        // Calc the text boundaries
        Rect bounds = new Rect();
        this.getPainter().getTextBounds(info.text, 0, info.text.length(), bounds);

        // Return the calculated offset
        switch (info.position) {
            case MIDDLE:
                return bounds.height() / 2;

            case INSIDE:
                return bounds.height();

            default:
                return 0.0f;
        }
    }

    /**
     * Calculate the extra vertical offset by the font metrics dimension.
     *
     * @param info the token info
     * @return the extra vertical offset
     */
    private float getVerticalOffsetByFontMetrics(TokenInfo info) {
        // Check if need to calculate the offset
        if (!this.mConsiderFontMetrics) return 0.0f;

        // Return the calculated offset
        switch (info.position) {
            case OUTSIDE:
                return this.mPaintClone.getFontMetrics().bottom;

            case INSIDE:
                return this.mPaintClone.getFontMetrics().top;

            default:
                return 0.0f;
        }
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
     * Draw a single unbend string token.
     *
     * @param canvas where to draw
     * @param info   the token info
     */
    private void drawUnbend(Canvas canvas, TokenInfo info, float originalAngle) {
        // Check for null value
        if (canvas == null) return;

        // Fix the vertical offset considering the position of the text on the path and the
        // font metrics offset.
        float extraVerticalOffset = this.getVerticalOffsetByPosition(info) -
                this.getVerticalOffsetByFontMetrics(info);
        ScFeature.translatePoint(info.point, 0.0f, extraVerticalOffset, originalAngle);

        // Save the canvas status and rotate by the calculated tangent angle
        canvas.save();

        // Draw the straight text
        canvas.rotate(info.angle, info.point.x, info.point.y);
        canvas.drawText(
                info.text,
                info.point.x + info.offset.x, info.point.y + info.offset.y,
                this.mPaintClone
        );

        // Restore the canvas status
        canvas.restore();
    }

    /**
     * Draw the bend token on canvas following a segment extracted from the original path.
     *
     * @param canvas where to draw
     * @param info   the token info
     * @param step   the length of segment
     */
    private void drawBend(Canvas canvas, TokenInfo info, float step) {
        // Check for null value
        if (canvas == null) return;

        // Holder
        Path segment = new Path();

        // Extract the path segment
        this.mSegmentMeasure.setPath(this.mPath, false);
        this.mSegmentMeasure
                .getSegment(info.distance, info.distance + step, segment, true);

        // Check for the angle
        if (info.angle != 0) {
            // Get the matrix and rotate it
            Matrix matrix = new Matrix();
            matrix.postRotate(info.angle);
            // Apply the new matrix to the segment
            segment.transform(matrix);
        }

        // Fix the vertical offset considering the position of the text on the path and the
        // font metrics offset.
        float extraVerticalOffset = this.getVerticalOffsetByPosition(info) -
                this.getVerticalOffsetByFontMetrics(info);

        // Draw the text on the path
        canvas.drawTextOnPath(
                info.text,
                segment,
                info.offset.x, info.offset.y + extraVerticalOffset,
                this.mPaint
        );
    }

    /**
     * Draw the single token on canvas.
     *
     * @param canvas the canvas where draw
     * @param info   the token info
     */
    private void drawToken(Canvas canvas, TokenInfo info, float step) {
        // Define the point holder
        float[] point;

        // Check if the last token must be on the last path point
        if (this.mLastTokenOnEnd && info.index == this.mTokens.length - 1) {
            // Get the last point on the original path
            point = this.mPathMeasure.getPosTan(this.mPathLength);

        } else {
            // Local distance
            float local = info.distance;

            // Check the alignment
            if (this.mPaint.getTextAlign() == Paint.Align.CENTER) local += step / 2;
            if (this.mPaint.getTextAlign() == Paint.Align.RIGHT) local += step;

            // Get the point on the path by the current distance
            point = this.mPathMeasure.getPosTan(local);
        }

        // Check for empty values
        if (point == null) return;

        // Define the properties.
        info.angle = this.mUnbend ? (float) Math.toDegrees(point[3]) : 0.0f;
        info.point = ScWriter.toPoint(point);

        // Check if have a liked listener
        if (this.mOnDrawListener != null) {
            this.mOnDrawListener.onBeforeDrawToken(info);
        }

        // Check for empty values
        if (!info.visible || info.point == null || info.offset == null || info.text == null)
            return;

        // Apply the current settings to the painter
        this.mPaintClone.set(this.mPaint);
        this.mPaintClone.setColor(info.color);

        // Draw by the case
        if (info.unbend) {
            // Unbend
            this.drawUnbend(canvas, info, (float) Math.toDegrees(point[3]));

        } else {
            // Bend
            this.drawBend(canvas, info, step);
        }
    }

    /**
     * Draw all string token on the path.
     *
     * @param canvas where to draw
     */
    private void drawTokens(Canvas canvas) {
        // Check for empty value
        if (this.mTokens == null || this.mPath == null) return;

        // Get the step distance to cover all path
        int count = this.mTokens.length + (this.mLastTokenOnEnd ? -1 : 0);
        float step = this.mPathLength / (count > 0 ? count : 1);

        // Define the token info.
        // I use to create the object here for avoid to create they n times after.
        // Always inside the draw method is better not instantiate too much classes.
        TokenInfo info = new TokenInfo();
        info.source = this;

        // Convert the limits from percentages in distances
        float startLimit = (this.mPathLength * this.mStartPercentage) / 100.0f;
        float endLimit = (this.mPathLength * this.mEndPercentage) / 100.0f;

        // Cycle all token.
        for (int index = 0; index < this.mTokens.length; index++) {
            // Helper for last position
            boolean isLast = index == this.mTokens.length - 1;

            // Define the notch info structure and fill with the local settings
            info.point = null;
            info.offset = new PointF(this.mTokenOffset.x, this.mTokenOffset.y);
            info.position = this.mTokenPosition;
            info.unbend = this.mUnbend;
            info.text = this.mTokens[index];
            info.index = index;
            info.distance = isLast && this.mLastTokenOnEnd ? this.mPathLength : index * step;
            info.visible = info.distance >= startLimit && info.distance <= endLimit;
            info.color = this.getGradientColor(info.distance);

            // Draw the single token
            this.drawToken(canvas, info, step);
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
    public void onDraw(Canvas canvas) {
        // Internal drawing
        this.drawTokens(canvas);
    }


    // ***************************************************************************************
    // Public classes and methods

    /**
     * This is a structure to hold the token information before draw it
     * Note that the "point" represent the point from will start to draw.
     * Note that the "distance" is the distance from the path starting.
     * Note that the "angle" is in degrees.
     */
    public class TokenInfo {

        public ScWriter source;
        public PointF point;
        public int index;
        public String text;
        public float distance;
        public float angle;
        public boolean unbend;
        public int color;
        public boolean visible;
        public PointF offset;
        public TokenPositions position;

    }

    /**
     * Set the global tokens offset.
     *
     * @param horizontal the horizontal offset
     * @param vertical   the vertical offset
     */
    @SuppressWarnings("unused")
    public void setTokenOffset(float horizontal, float vertical) {
        this.mTokenOffset.x = horizontal;
        this.mTokenOffset.y = vertical;
    }


    // ***************************************************************************************
    // Public properties

    /**
     * Return the string tokens.
     *
     * @return the tokens list
     */
    @SuppressWarnings("unused")
    public String[] getTokens() {
        return this.mTokens;
    }

    /**
     * Set the string tokens to draw on path.
     *
     * @param value the tokens list
     */
    @SuppressWarnings("unused")
    public void setTokens(String... value) {
        this.mTokens = value;
    }

    /**
     * Return the string tokens alignment respect the path.
     *
     * @return the notches alignment
     */
    @SuppressWarnings("unused")
    public TokenPositions getPosition() {
        return this.mTokenPosition;
    }

    /**
     * Set the string tokens alignment respect the path.
     *
     * @param value the notches alignment
     */
    @SuppressWarnings("unused")
    public void setPosition(TokenPositions value) {
        this.mTokenPosition = value;
    }

    /**
     * Return true if the text is unbend.
     *
     * @return the unbend status
     */
    @SuppressWarnings("unused")
    public boolean getUnbend() {
        return this.mUnbend;
    }

    /**
     * Set true to have a unbend text.
     *
     * @param value the unbend status
     */
    @SuppressWarnings("unused")
    public void setUnbend(boolean value) {
        this.mUnbend = value;
    }

    /**
     * Return true if the offset calculation consider the font metrics too.
     *
     * @return the current status
     */
    @SuppressWarnings("unused")
    public boolean getConsiderFontMetrics() {
        return this.mConsiderFontMetrics;
    }

    /**
     * Set true if want that the offset calculation consider the font metrics too.
     *
     * @param value the current status
     */
    @SuppressWarnings("unused")
    public void setConsiderFontMetrics(boolean value) {
        this.mConsiderFontMetrics = value;
    }

    /**
     * Return true if force to draw the last token on the end of the path.
     *
     * @return the current status
     */
    @SuppressWarnings("unused")
    public boolean getLastTokenOnEnd() {
        return this.mLastTokenOnEnd;
    }

    /**
     * Set true if want that the last token is forced to draw to the end of the path.
     * Note that the last token on the last point of path cannot work proper with the bending text
     * enable. So, if value is true, this method will forced to disable the bending.
     *
     * @param value the current status
     */
    @SuppressWarnings("unused")
    public void setLastTokenOnEnd(boolean value) {
        this.mLastTokenOnEnd = value;
        if (value) this.mUnbend = true;
    }


    // ***************************************************************************************
    // Listeners and Interfaces

    /**
     * Define the draw listener interface
     */
    @SuppressWarnings("unused")
    public interface OnDrawListener {

        /**
         * Called before draw the single token
         *
         * @param info the token info
         */
        void onBeforeDrawToken(TokenInfo info);

    }

    /**
     * Set the draw listener to call
     *
     * @param listener the linked method to call
     */
    @SuppressWarnings("unused")
    public void setOnDrawListener(OnDrawListener listener) {
        this.mOnDrawListener = listener;
    }

}

