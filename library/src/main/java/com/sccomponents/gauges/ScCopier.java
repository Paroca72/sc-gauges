package com.sccomponents.gauges;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Shader;

import java.util.Arrays;

/**
 * Create a copy of the path.
 * The most important of this feature is that can decide the path segment to draw.
 *
 * @author Samuele Carassai
 * @version 1.0.2
 * @since 2016-05-26
 */
public class ScCopier extends ScFeature {

    // ***************************************************************************************
    // Private and protected variables

    private Path mSegment;
    private BitmapShader mShader;
    private boolean mForceCreateShader;

    private OnDrawListener mOnDrawListener;


    // ***************************************************************************************
    // Constructor

    @SuppressWarnings("unused")
    public ScCopier(Path path) {
        // Super
        super(path);

        // Init
        this.mSegment = new Path();
        this.mForceCreateShader = true;
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
     * Create a colored bitmap following the path.
     * Note that the bitmap will be created on the whole path and not on the extracted segment.
     * This bitmap is rough and must clipped before draw it on the destination canvas.
     *
     * @return the bitmap
     */
    public Bitmap createColoredBitmap() {
        // Create the bitmap using the path boundaries and retrieve the canvas where draw
        RectF bounds = this.mPathMeasure.getBounds();
        Bitmap bitmap = Bitmap.createBitmap(
                (int) (bounds.right + this.mPaint.getStrokeWidth()),
                (int) (bounds.bottom + this.mPaint.getStrokeWidth()),
                Bitmap.Config.ARGB_8888
        );
        Canvas canvas = new Canvas(bitmap);

        // Hold if a rounded stroke and the point holder
        boolean isRoundedStroke = this.mPaint.getStrokeCap() == Paint.Cap.ROUND;
        float halfStroke = this.mPaint.getStrokeWidth() / 2;
        float[] point;

        // Cycle all points of the path
        for (int distance = 0; distance < this.mPathLength; distance++) {
            // Get the point and adjust the for the stroke size
            point = this.mPathMeasure.getPosTan(distance);

            // Trigger for index position and get the color
            boolean isFirstOrLast = distance == 0 || distance == this.mPathLength - 1;
            int color = this.getGradientColor(distance);

            // Set the current painter color and stroke
            this.mPaint.setColor(color);
            this.mPaint.setStrokeCap(
                    isRoundedStroke && isFirstOrLast ? Paint.Cap.ROUND : Paint.Cap.BUTT);

            // If the round stroke is not settled the point have a square shape.
            // This can create a visual issue when the path follow a curve.
            // To avoid this issue the point (square) will be rotate of the tangent angle
            // before to write it on the canvas.
            canvas.save();
            canvas.rotate((float) Math.toDegrees(point[3]), point[0], point[1]);
            canvas.drawPoint(point[0] + halfStroke, point[1], this.mPaint);
            canvas.restore();
        }

        // Check for rounded stroke
        if (isRoundedStroke)
            this.mPaint.setStrokeCap(Paint.Cap.ROUND);

        // Return the new bitmap
        return bitmap;
    }

    /**
     * Draw a copy of the source path on the canvas.
     *
     * @param canvas the destination canvas
     */
    private void drawCopy(Canvas canvas) {
        // Convert the percentage values in distance referred to the current path length.
        float startDistance = (this.mPathLength * this.mStartPercentage) / 100.0f;
        float endDistance = (this.mPathLength * this.mEndPercentage) / 100.0f;

        // Extract the segment to draw
        this.mSegment.reset();
        this.mPathMeasure.getSegment(startDistance, endDistance, this.mSegment, true);

        // Check the listener
        if (this.mOnDrawListener != null) {
            // Define the copy info
            CopyInfo info = new CopyInfo();
            info.source = this;
            info.scale = new PointF(1.0f, 1.0f);
            info.offset = new PointF(0.0f, 0.0f);

            // Call the listener method
            this.mOnDrawListener.onBeforeDrawCopy(info);

            // Find the center
            float xCenter = this.mPathMeasure.getBounds().centerX();
            float yCenter = this.mPathMeasure.getBounds().centerY();

            // Define the matrix to transform the path and the shader
            Matrix matrix = new Matrix();
            matrix.postScale(info.scale.x, info.scale.y);
            matrix.postTranslate(info.offset.x, info.offset.y);
            matrix.postRotate(info.rotate, xCenter, yCenter);

            // Apply the matrix on the shader
            if (this.mShader != null)
                this.mShader.setLocalMatrix(matrix);

            // Apply the matrix on the segment
            this.mSegment.transform(matrix);
        }

        // Draw only a path segment if the canvas is not null and the painter allow to draw
        if (canvas != null && this.mPaint != null &&
                (this.mPaint.getStyle() != Paint.Style.STROKE || this.mPaint.getStrokeWidth() > 0)) {
            // Create a paint clone and set the shader
            Paint clone = new Paint(this.mPaint);
            clone.setShader(this.mShader);

            // Draw the segment on the canvas
            canvas.drawPath(this.mSegment, clone);
        }
    }


    // ***************************************************************************************
    // Overrides

    /**
     * Draw method
     *
     * @param canvas where draw
     */
    @Override
    protected void onDraw(Canvas canvas) {
        //Check the domain
        if (this.mPath == null || this.mStartPercentage == this.mEndPercentage)
            return;

        // Check the number of colors for create the shader if requested
        if (this.mColors != null && this.mColors.length > 1) {
            // Check if need to create the shader bitmap
            if (this.mForceCreateShader) {
                // Create the bitmap
                this.mForceCreateShader = false;
                this.mShader = new BitmapShader(
                        this.createColoredBitmap(),
                        Shader.TileMode.CLAMP,
                        Shader.TileMode.CLAMP
                );
            }

        } else
            // Reset the shader
            this.mShader = null;

        // Draw a copy
        this.drawCopy(canvas);
    }


    /**
     * Refresh the feature measure.
     */
    @Override
    public void refresh() {
        super.refresh();
        this.mForceCreateShader = true;
    }

    /**
     * Set the current stroke colors
     *
     * @param value the new stroke colors
     */
    @Override
    public void setColors(int... value) {
        // Check if value is changed
        if (!Arrays.equals(this.mColors, value)) {
            this.mForceCreateShader = true;
            super.setColors(value);
        }
    }

    /**
     * Set the colors filling mode.
     * You can have to way for draw the colors of the path: SOLID or GRADIENT.
     *
     * @param value the new color filling mode
     */
    @Override
    public void setColorsMode(ColorsMode value) {
        // Check if value is changed
        if (this.mColorsMode != value) {
            this.mForceCreateShader = true;
            super.setColorsMode(value);
        }
    }


    // ***************************************************************************************
    // Public classes and methods

    /**
     * This is a structure to hold the notch information before draw it.
     * Note that the "rotate" angle is in degrees.
     */
    @SuppressWarnings("unused")
    public class CopyInfo {

        public ScCopier source;
        public PointF scale;
        public PointF offset;
        public float rotate;

    }


    // ***************************************************************************************
    // Listeners and Interfaces

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
        void onBeforeDrawCopy(CopyInfo info);

    }

    /**
     * Set the draw listener to call.
     *
     * @param listener the linked method to call
     */
    @SuppressWarnings("unused")
    public void setOnDrawListener(OnDrawListener listener) {
        this.mOnDrawListener = listener;
    }

}
