package com.sccomponents.gauges.library;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;

import java.util.Arrays;


/**
 * Write a series of texts following the path.
 * <p>
 * Can draw on multi contours and before and after the path. If before of after it will
 * follow a straight line along the angle of the related first or last point of the path.
 *
 * @author Samuele Carassai
 * @version 3.1.0
 * @since 2016-05-26
 */
public class ScWriter extends ScRepetitions {

    // ***************************************************************************************
    // Private variables

    private String[] mTokens;
    private boolean mBending;
    private boolean mConsiderFontMetrics;

    private float[] mGenericPoint;
    private Rect mGenericRect;
    private TokenInfo mRepetitionInfo;


    /****************************************************************************************
     * Constructor
     */

    @SuppressWarnings("unused")
    public ScWriter() {
        // Super
        super();

        // Init
        this.mConsiderFontMetrics = true;
        this.mBending = false;
        this.mRepetitionInfo = new TokenInfo();

        this.mGenericPoint = new float[2];
        this.mGenericRect = new Rect();

        // Update the painter
        Paint painter = this.getPainter();
        painter.setStrokeWidth(0.0f);
        painter.setTextSize(30.0f);
        painter.setStyle(Paint.Style.FILL);
    }


    // ***************************************************************************************
    // Private methods

    /**
     * Get the text boundaries.
     * As we can have a multiline string we must find the max boundaries calculated on all
     * the rows in the string.
     * @param text the text to check
     * @return the bounds in rect
     */
    private Rect getBounds(String text) {
        // Holder
        String[] rows = this.getTextRows(text);
        Paint painter = this.getPainter();

        int width = 0;
        int height = 0;

        // Cycle each rows within the string
        for (String row : rows) {
            // Get the current row dimensions
            painter.getTextBounds(row, 0, row.length(), this.mGenericRect);

            // Fix the current values
            if (width < this.mGenericRect.width())
                width = this.mGenericRect.width();
            height += this.mGenericRect.height();
        }

        // Fix the new values and return
        this.mGenericRect.set(0, 0, width, height);
        return this.mGenericRect;
    }

    /**
     * Divide the text in tokens (rows) by the carriage return "\n"
     * @param text The passed value
     * @return the tokens
     */
    private String[] getTextRows(String text) {
        if (text == null || text.length() == 0)
            return new String[]{};
        else
            return text.split("\\n");
    }

    /**
     * In case of multiline get back the the number of rows.
     * NOTE: the separator is "\n"
     * @param text the passed text
     * @return the number of rows
     */
    private int getTextRowsCount(String text) {
        return this.getTextRows(text).length;
    }

    /**
     * Calculate the extra vertical offset by the text position respect to the path.
     * This method consider multiline text also.
     * @param info the token info
     * @return the extra vertical offset
     */
    private float getVerticalOffset(TokenInfo info) {
        // Return the calculated offset considering the text rows number
        int rows = this.getTextRowsCount(info.text);
        Rect bounds = this.getBounds(info.text);
        int singleRowHeight = bounds.height() / rows;

        if (info.position == Positions.MIDDLE)
            return singleRowHeight - bounds.height() / 2;

        if (info.position == Positions.OUTSIDE)
            return singleRowHeight - bounds.height();

        return 0.0f;
    }

    /**
     * Calculate the extra vertical offset by the font metrics dimension.
     * @param info the token info
     * @return the extra vertical offset
     */
    private float getFontMetricsOffset(TokenInfo info) {
        // Check if need to calculate the offset
        if (!this.mConsiderFontMetrics)
            return 0.0f;

        // Return the calculated offset
        Paint painter = this.getPainter();
        if (info.position == Positions.OUTSIDE)
            return painter.getFontMetrics().bottom;

        if (info.position == Positions.INSIDE)
            return painter.getFontMetrics().top;

        return 0.0f;
    }

    /**
     * Get the text with using the current painter
     * @param text  the source
     * @param start the position to start
     * @param end   the position where finish
     * @return the width
     */
    private int getTextWidth(String text, int start, int end) {
        // Holders
        Paint paint = this.getPainter();
        Rect rect = this.mGenericRect;

        // Get the measure of the text
        paint.getTextBounds(text, start, end, rect);
        return rect.width();
    }

    /**
     * Get the text with using the current painter
     * @param text the source
     * @return the width
     */
    private int getTextWidth(String text) {
        return this.getTextWidth(text, 0, text.length());
    }

    /**
     * Get the horizontal offset where start to draw the text considering the current
     * painter alignment.
     * @param text the source text
     * @return the start position
     */
    private float getHorizontalOffset(String text) {
        // Calculate the start position considering the painter text align
        switch (this.getPainter().getTextAlign()) {
            case CENTER:
                return -this.getTextWidth(text) / 2.0f;

            case RIGHT:
                return -this.getTextWidth(text);
        }
        return 0.0f;
    }


    // ***************************************************************************************
    // Draw methods

    /**
     * Draw some text on the passed path.
     * Can draw on multi contours and before and after the path. If before of after it will
     * follow a straight line along the angle of the related first or last point of the path.
     * This method draw each characters of the string one by one and this will have effect on
     * the method performance.
     * @param canvas   where to draw
     * @param token    the text to draw
     * @param distance the start distance
     * @param angle    the start angle
     * @param offsetY  the vertical offset
     */
    private void drawTextOnPath(Canvas canvas, String token,
                                float distance, float angle, float offsetY) {
        // The text align must fixed to left and restore at the end of this procedure
        Paint painter = this.getPainter();
        Paint.Align oldAlign = painter.getTextAlign();
        painter.setTextAlign(Paint.Align.LEFT);

        // Holders
        float currentPos = distance;
        float letterSpacing = 3.0f;

        // Get the last point info of the whole path
        float[] lastPoint = new float[2];
        float pathLength = this.getMeasure().getLength();
        float lastPointAngle = this.getPointAndAngle(pathLength, lastPoint);

        // Draw chars per chars
        for (int index = 0, len = token.length(); index < len; index++) {
            // Draw before the paths
            if (currentPos < 0) {
                canvas.drawText(
                        token,
                        index,
                        index + 1,
                        this.mGenericPoint[0] + currentPos,
                        this.mGenericPoint[1] + offsetY,
                        painter
                );

            } else {
                // Reset the canvas rotation
                canvas.save();
                canvas.rotate(-angle, this.mGenericPoint[0], this.mGenericPoint[1]);

                // Draw on path
                Path path = this.getMeasure().getPath(currentPos);
                if (path != null) {
                    float offsetX = this.getMeasure().getContourDistance(currentPos);
                    canvas.drawTextOnPath(
                            "" + token.charAt(index),
                            path,
                            offsetX,
                            offsetY,
                            painter);
                } else {
                    // Draw after path
                    canvas.rotate(lastPointAngle, lastPoint[0], lastPoint[1]);
                    canvas.drawText(
                            token,
                            index,
                            index + 1,
                            lastPoint[0] + currentPos - pathLength,
                            lastPoint[1] + offsetY,
                            painter
                    );

                }

                // Restore the previous canvas state
                canvas.restore();
            }

            // Increase the current position
            currentPos += this.getTextWidth(token, index, index + 1) + letterSpacing;
        }

        // Restore the alignment
        painter.setTextAlign(oldAlign);
    }

    /**
     * Draw the single token on the path
     * @param canvas where to draw
     * @param token  the text to draw
     * @param x      the point
     * @param y      the point
     * @param angle  the angle
     */
    private void drawToken(Canvas canvas, String token, float x, float y, float angle) {
        // Holders
        Rect bounds = this.getBounds(token);

        // Save the state and rotate
        canvas.save();
        canvas.rotate(angle, x, y - bounds.centerY());

        // Draw and restore
        canvas.drawText(token, x, y, this.getPainter());
        canvas.restore();
    }

    /**
     * Draw the single token on canvas.
     * @param canvas the canvas where draw
     * @param info   the token info
     */
    private void drawToken(Canvas canvas, TokenInfo info) {
        // Get the current point and save the current canvas status
        this.getPoint(info.distance, this.mGenericPoint);

        // Holders
        Rect bounds = this.getBounds(info.text);
        String[] rows = this.getTextRows(info.text);

        float singleRowHeight = bounds.height() / rows.length;
        float offsetY = this.getVerticalOffset(info) - this.getFontMetricsOffset(info);

        // Font size
        Paint painter = this.getPainter();
        painter.setTextSize(info.size);

        // Draw one line per time
        for (String token : rows) {
            // Draw
            if (info.bending) {
                // Bending
                float distance = info.distance + this.getHorizontalOffset(token);
                this.drawTextOnPath(
                        canvas,
                        token,
                        distance,
                        info.tangent,
                        offsetY
                );
            } else
                // Unbending
                this.drawToken(
                        canvas,
                        token,
                        this.mGenericPoint[0],
                        this.mGenericPoint[1] + offsetY,
                        info.angle
                );

            // Adjust vertical offset
            offsetY += singleRowHeight;
        }
    }


    // ***************************************************************************************
    // Overrides

    /**
     * Get the current repetition drawing info.
     * This methods must be overridden for create custom drawing info for inherited
     * classes.
     * @param repetition the repetition index
     * @return the repetition drawing info
     */
    @SuppressWarnings("unused")
    @Override
    protected TokenInfo getRepetitionInfo(int contour, int repetition) {
        this.mRepetitionInfo.reset(this, contour, repetition);
        return this.mRepetitionInfo;
    }

    /**
     * Draw method
     * @param canvas where to draw
     */
    @Override
    public void onDraw(Canvas canvas, RepetitionInfo info) {
        // Check if have something to draw
        TokenInfo tokenInfo = (TokenInfo) info;
        String text = tokenInfo.text;

        if (text != null && text.length() > 0)
            this.drawToken(canvas, tokenInfo);
    }

    /**
     * Hide this property use to the user.
     * @param value the repetitions number
     * @hide
     */
    @SuppressWarnings("unused")
    @Override
    public void setRepetitions(int value) {
        // Do nothing
    }


    // ***************************************************************************************
    // Public methods

    /**
     * Implement a copy of this object
     * @param destination the destination object
     */
    @SuppressWarnings("unused")
    public void copy(ScWriter destination) {
        // Super
        super.copy(destination);

        // This object
        if (this.mTokens != null)
            destination.setTokens(this.mTokens.clone());

        destination.setBending(this.mBending);
        destination.setConsiderFontMetrics(this.mConsiderFontMetrics);
    }

    /**
     * Implement a copy of this object
     * @param destination the destination object
     */
    @Override
    public void copy(ScRepetitions destination) {
        if (destination instanceof ScWriter)
            this.copy((ScWriter) destination);
        else
            super.copy(destination);
    }


    // ***************************************************************************************
    // Public properties

    /**
     * Return the string tokens.
     * @return the tokens list
     */
    @SuppressWarnings("unused")
    public String[] getTokens() {
        return this.mTokens;
    }

    /**
     * Set the string tokens to draw on path.
     * @param values the tokens list
     */
    @SuppressWarnings("all")
    public void setTokens(String... values) {
        if (!Arrays.equals(this.mTokens, values)) {
            this.mTokens = values;
            if (this.mTokens != null)
                super.setRepetitions(this.mTokens.length);
            else
                super.setRepetitions(0);
            this.onPropertyChange("tokens", values);
        }
    }

    /**
     * Return true if the text is bending.
     * @return the bending status
     */
    @SuppressWarnings("unused")
    public boolean getBending() {
        return this.mBending;
    }

    /**
     * Set true to have a bending text.
     * @param value the bending status
     */
    @SuppressWarnings("unused")
    public void setBending(boolean value) {
        if (this.mBending != value) {
            this.mBending = value;
            this.onPropertyChange("bending", value);
        }
    }

    /**
     * Return true if the offset calculation consider the font metrics too.
     * @return the current status
     */
    @SuppressWarnings("unused")
    public boolean getConsiderFontMetrics() {
        return this.mConsiderFontMetrics;
    }

    /**
     * Set true if want that the offset calculation consider the font metrics too.
     * @param value the current status
     */
    @SuppressWarnings("unused")
    public void setConsiderFontMetrics(boolean value) {
        if (this.mConsiderFontMetrics != value) {
            this.mConsiderFontMetrics = value;
            this.onPropertyChange("considerFontMetrics", value);
        }
    }


    // ***************************************************************************************
    // Public classes and methods

    /**
     * This is a structure to hold the feature information before draw it
     */
    public class TokenInfo extends RepetitionInfo {

        // ***************************************************************************************
        // Properties

        public ScWriter source;
        public String text;
        public float size;
        public boolean bending;

        // ***************************************************************************************
        // Public methods

        public void reset(ScWriter feature, int contour, int repetition) {
            // Super
            super.reset(feature, contour, repetition);

            // Reset
            this.source = feature;
            this.bending = feature.getBending();

            Paint painter = feature.getPainter();
            this.size = painter.getTextSize();
            this.text = null;

            String[] tokens = feature.getTokens();
            if (tokens != null && repetition > 0 && repetition <= tokens.length)
                this.text = tokens[repetition - 1];
        }

    }

}
