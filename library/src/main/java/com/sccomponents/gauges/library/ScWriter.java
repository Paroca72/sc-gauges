package com.sccomponents.gauges.library;

import android.graphics.Canvas;
import android.graphics.Color;
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
 * @version 3.5.0
 * @since 2016-05-26
 */
public class ScWriter extends ScRepetitions {

    // ***************************************************************************************
    // Private variables

    private String[] mTokens;
    private boolean mBending;
    private boolean mConsiderFontMetrics;
    private int mBackground;
    private int mPadding;
    private float mInterline;
    private float mLetterSpacing;

    private float[] mFirstPoint;
    private float[] mGenericPoint;
    private float[] mLastPoint;

    private Rect mGenericRect;
    private TokenInfo mRepetitionInfo;
    private Paint mBackgroundPaint;


    /****************************************************************************************
     * Constructor
     */

    @SuppressWarnings({"unused", "WeakerAccess"})
    public ScWriter() {
        // Super
        super();

        // Init
        this.mConsiderFontMetrics = true;
        this.mBending = false;
        this.mBackground = Color.TRANSPARENT;
        this.mPadding = 0;
        this.mInterline = 1.0f;
        this.mLetterSpacing = 0.3f;

        this.mRepetitionInfo = new TokenInfo();
        this.mFirstPoint = new float[2];
        this.mGenericPoint = new float[2];
        this.mLastPoint = new float[2];
        this.mGenericRect = new Rect();

        // Update the painter
        Paint painter = this.getPainter();
        painter.setStrokeWidth(0.0f);
        painter.setTextSize(0.0f);
        painter.setStyle(Paint.Style.FILL);

        this.mBackgroundPaint = new Paint();
        this.mBackgroundPaint.setStyle(Paint.Style.FILL);
        this.mBackgroundPaint.setColor(this.mBackground);
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
            return singleRowHeight - bounds.height() / 2.0f;

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
     * Get the text bounds using the current painter
     * @param text  the source
     * @param start the position to start
     * @param end   the position where finish
     * @return the bounds
     */
    private Rect getTextBounds(String text, int start, int end) {
        // Holders
        Paint paint = this.getPainter();
        Rect rect = this.mGenericRect;

        // Get the measure of the text
        paint.getTextBounds(text, start, end, rect);
        return rect;
    }

    /**
     * Get the text with using the current painter
     * @param text the source
     * @return the width
     */
    private int getTextWidth(String text) {
        return this.getTextBounds(text, 0, text.length()).width();
    }

    /**
     * Passed the font size calculate the letters spacing
     * @return the letter spacing
     */
    private float getInternalLetterSpacing() {
        float letterSpacing = this.getPainter().getTextSize() * this.mLetterSpacing;
        return letterSpacing < 1.0f ? 1.0f: letterSpacing;
    }

    /**
     * Get the horizontal offset where start to draw the text considering the current
     * painter alignment.
     * @param text the source text
     * @return the start position
     */
    private float getHorizontalOffset(String text) {
        // Holder
        Paint.Align align = this.getPainter().getTextAlign();

        // Check for default value
        if (align == Paint.Align.LEFT)
            return 0.0f;

        // Correct for bending
        float width = this.getTextWidth(text);
        if (this.getBending() && text.length() > 1)
            width += this.getInternalLetterSpacing() * (text.length() - 2);

        // Calculate the start position considering the painter text align
        switch (align) {
            case CENTER:
                return -width / 2.0f;

            case RIGHT:
                return -width;
        }
        return 0.0f;
    }


    // ***************************************************************************************
    // Draw methods

    /**
     * Draw a rectangle as a background
     * @param canvas where to draw
     * @param bounds the rectangle to draw
     */
    private void drawBackground(Canvas canvas, Rect bounds, float letterSpacing) {
        // Draw the background just if needs
        if (this.mBackgroundPaint.getColor() != Color.TRANSPARENT) {
            float offsetY = bounds.height();
            canvas.drawRect(
                    bounds.left - (letterSpacing / 2 + this.mPadding),
                    bounds.top - offsetY - this.mPadding,
                    bounds.right + (letterSpacing / 2 + this.mPadding),
                    bounds.bottom - offsetY + this.mPadding,
                    this.mBackgroundPaint
            );
        }
    }

    /**
     * Draw raw text on canvas
     * @param canvas where to draw
     * @param token  what to draw
     * @param bounds the drawing position
     */
    private void drawText(Canvas canvas, char token, Rect bounds) {
        // Draw the text
        Paint painter = this.getPainter();
        canvas.drawText(
                Character.toString(token),
                bounds.left,
                bounds.top,
                painter
        );
    }

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
    private void drawTextOnPath(Canvas canvas, String token, float distance,
                                float offsetY, float angle, boolean drawBackground) {
        // The text align must fixed to left and restore at the end of this procedure
        Paint painter = this.getPainter();
        Paint.Align oldAlign = painter.getTextAlign();
        painter.setTextAlign(Paint.Align.LEFT);

        // Holders
        float currentPos = distance;
        int textHeight = this.getTextBounds(token, 0, token.length()).height();
        float letterSpacing = this.getInternalLetterSpacing();

        // Get the last point info of the whole path
        float pathLength = this.getMeasure().getLength();
        float lastPointAngle = this.getPointAndAngle(pathLength, this.mLastPoint);

        // Draw chars per chars
        for (int index = 0, len = token.length(); index < len; index++) {
            // Save the canvas status
            canvas.save();

            // Holder
            char currentChar = token.charAt(index);
            Rect textBounds = this.getTextBounds(token, index, index + 1);
            textBounds.top = textBounds.bottom - textHeight;

            // Draw before the paths
            if (currentPos < 0) {
                // Fix the bounds
                int x = (int)(this.mFirstPoint[0] + currentPos);
                int y = (int)(this.mFirstPoint[1] + offsetY);
                textBounds.offset(x, y);

            } else {
                // Draw on path
                Path path = this.getMeasure().getPath(currentPos);
                if (path != null) {
                    // Rotate on the original point
                    canvas.rotate(-angle, this.mFirstPoint[0], this.mFirstPoint[1]);

                    // Rotate on the current point
                    float currentAngle = this.getPointAndAngle(currentPos, this.mGenericPoint);
                    canvas.rotate(currentAngle, this.mGenericPoint[0], this.mGenericPoint[1]);

                    // Fix the bounds offset
                    int x = (int)(this.mGenericPoint[0]);
                    int y = (int)(this.mGenericPoint[1] + offsetY);
                    textBounds.offset(x, y);

                } else {
                    // Draw after path
                    int x = (int)(this.mLastPoint[0] + currentPos - pathLength);
                    int y = (int)(this.mLastPoint[1] + offsetY);
                    textBounds.offset(x, y);

                    // Adjust rotation
                    canvas.rotate(lastPointAngle, this.mLastPoint[0], this.mLastPoint[1]);
                }
            }

            // Draw
            if (drawBackground)
                this.drawBackground(canvas, textBounds, letterSpacing);
            else
                this.drawText(canvas, currentChar, textBounds);

            // Increase the current position
            currentPos += textBounds.width() + letterSpacing;

            // Restore the previous canvas state
            canvas.restore();
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
    private void drawToken(Canvas canvas, String token, float x, float y,
                           float angle, boolean drawBackground) {
        // Holders
        Rect bounds = this.getBounds(token);
        int offsetY = bounds.centerY();

        // Save the state and rotate
        canvas.save();
        canvas.rotate(angle, x, y - offsetY);

        // Draw
        if (drawBackground) {
            // Fix the offset
            x += this.getHorizontalOffset(token);
            y += this.getPainter().getTextAlign() == Paint.Align.LEFT ? 0.0f: offsetY * 2;
            bounds.offset((int)x, (int)y);

            // Draw
            this.drawBackground(canvas, bounds, 0);
        } else
            canvas.drawText(token, x, y, this.getPainter());

        // Restore the canvas status
        canvas.restore();
    }

    /**
     * Draw the single token on canvas.
     * @param canvas the canvas where draw
     * @param info   the token info
     */
    private void drawToken(Canvas canvas, TokenInfo info, boolean drawBackground) {
        // Get the current point and save the current canvas status
        this.getPoint(info.distance, this.mFirstPoint);

        // Holders
        Rect bounds = this.getBounds(info.text);
        String[] rows = this.getTextRows(info.text);

        float singleRowHeight = ((float) bounds.height()) / rows.length;
        float offsetY = this.getVerticalOffset(info) - this.getFontMetricsOffset(info);

        // Font size
        Paint painter = this.getPainter();
        painter.setTextSize(info.size);

        // Interline for multi-rows text
        singleRowHeight += info.size * this.mInterline - info.size;

        // Background
        this.mBackgroundPaint.setColor(this.mBackground);

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
                        offsetY + singleRowHeight,
                        info.tangent,
                        drawBackground
                );
            } else
                // Unbending
                this.drawToken(
                        canvas,
                        token,
                        this.mFirstPoint[0],
                        this.mFirstPoint[1] + offsetY,
                        info.angle,
                        drawBackground
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

        if (text != null && text.length() > 0) {
            this.drawToken(canvas, tokenInfo, true);
            this.drawToken(canvas, tokenInfo, false);
        }
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
        destination.setBackground(this.mBackground);
        destination.setPadding(this.mPadding);
        destination.setInterline(this.mInterline);
        destination.setLetterSpacing(this.mLetterSpacing);
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
    @SuppressWarnings({"unused"})
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
    @SuppressWarnings({"unused", "WeakerAccess"})
    public boolean getBending() {
        return this.mBending;
    }

    /**
     * Set true to have a bending text.
     * @param value the bending status
     */
    @SuppressWarnings({"unused", "WeakerAccess"})
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
    @SuppressWarnings({"unused", "WeakerAccess"})
    public void setConsiderFontMetrics(boolean value) {
        if (this.mConsiderFontMetrics != value) {
            this.mConsiderFontMetrics = value;
            this.onPropertyChange("considerFontMetrics", value);
        }
    }


    /**
     * Return the color of the background.
     * @return the background color
     */
    @SuppressWarnings({"unused", "WeakerAccess"})
    public int getBackground() {
        return this.mBackground;
    }

    /**
     * Set the color of the background.
     * @param value the background color
     */
    @SuppressWarnings({"unused", "WeakerAccess"})
    public void setBackground(int value) {
        if (this.mBackground != value) {
            this.mBackground = value;
            this.onPropertyChange("background", value);
        }
    }

    /**
     * Return the padding value than will apply to the background.
     * @return the padding value
     */
    @SuppressWarnings({"unused", "WeakerAccess"})
    public int getPadding() {
        return this.mPadding;
    }

    /**
     * Set the padding value than will apply to the background.
     * @param value the padding value
     */
    @SuppressWarnings({"unused", "WeakerAccess"})
    public void setPadding(int value) {
        if (this.mPadding != value) {
            this.mPadding = value;
            this.onPropertyChange("padding", value);
        }
    }


    /**
     * Return the interline value between the rows.
     * Will apply only in multi rows texts.
     * The value is expressed in ratio respect the font size.
     * The default value is 1.0
     * @return the interline value
     */
    @SuppressWarnings("unused")
    public float getInterline() {
        return this.mInterline;
    }

    /**
     * Return the interline value between the rows.
     * Will apply only in multi rows texts.
     * The value is expressed in ratio respect the font size.
     * The default value is 1.0
     * @param value the interline value
     */
    @SuppressWarnings({"unused", "WeakerAccess"})
    public void setInterline(float value) {
        if (this.mInterline != value) {
            this.mInterline = value;
            this.onPropertyChange("interline", value);
        }
    }


    /**
     * Return the spacing value between the text letters.
     * The value is expressed in ratio respect the font size.
     * Note that this setting have effect only when <code>bending</code> is <code>true</code>.
     * The default value is 0.3
     * @return the spacing value
     */
    @SuppressWarnings("unused")
    public float getLetterSpacing() {
        return this.mLetterSpacing;
    }

    /**
     * Set the spacing value between the text letters.
     * The value is expressed in ratio respect the font size.
     * Note that this setting have effect only when <code>bending</code> is <code>true</code>.
     * The default value is 0.3
     * @param value the spacing value
     */
    @SuppressWarnings({"unused", "WeakerAccess"})
    public void setLetterSpacing(float value) {
        if (this.mLetterSpacing != value) {
            this.mLetterSpacing = value;
            this.onPropertyChange("letterSpacing", value);
        }
    }


    // ***************************************************************************************
    // Public classes and methods

    /**
     * This is a structure to hold the feature information before draw it
     */
    @SuppressWarnings({"unused", "WeakerAccess"})
    public class TokenInfo extends RepetitionInfo {

        // ***************************************************************************************
        // Properties

        public ScWriter source;
        public String text;
        public float size;
        public boolean bending;
        public int background;
        public int padding;

        // ***************************************************************************************
        // Public methods

        public void reset(ScWriter feature, int contour, int repetition) {
            // Super
            super.reset(feature, contour, repetition);

            // Reset
            this.source = feature;
            this.bending = feature.getBending();
            this.background = feature.getBackground();
            this.padding = feature.getPadding();

            Paint painter = feature.getPainter();
            this.size = painter.getTextSize();
            this.text = null;
            this.background = Color.TRANSPARENT;

            String[] tokens = feature.getTokens();
            if (tokens != null && repetition > 0 && repetition <= tokens.length)
                this.text = tokens[repetition - 1];
        }

    }

}
