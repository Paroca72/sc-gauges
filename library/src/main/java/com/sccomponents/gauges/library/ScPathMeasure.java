package com.sccomponents.gauges.library;

import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.os.Build;

import java.util.Arrays;


/**
 * Extend the PathMeasure because the original class not consider the contours in its
 * totality.
 * NOTE: About nearest found point this class work mostly on an points approximation of 1 pixel
 * of the given path.
 *
 * @author Samuele Carassai
 * @version 3.5.0
 * @since 2016-05-26
 */
@SuppressWarnings({"WeakerAccess"})
public class ScPathMeasure extends PathMeasure {

    // ***************************************************************************************
    // Private

    private Path mPath;
    private boolean mForceClosed;

    private RectF mBounds;
    private float mLength;
    private float[][] mPathPoints;

    // For internal calculation
    private PathMeasure mGenericMeasure;


    // ***************************************************************************************
    // Constructor

    @SuppressWarnings({"unused"})
    public ScPathMeasure(Path path, boolean forceClosed) {
        // Super
        super(path, forceClosed);

        // Init
        this.init(path, forceClosed);
    }

    @SuppressWarnings({"unused"})
    public ScPathMeasure() {
        // Super
        super();

        // Init
        this.init(null, false);
    }


    // ***************************************************************************************
    // Private methods

    /**
     * Get an approximation of 1 pixel of all the points inside the path.
     * @return a points list: x, y, angle (degrees).
     */
    private float[][] getPathPoints() {
        // Check for empty value
        if (this.mPath == null || this.mPath.isEmpty())
            return new float[][]{};

        // Reset the path measurer
        this.mGenericMeasure.setPath(this.mPath, this.mForceClosed);

        // Get the right length
        float length = this.mLength;
        int fixedLength = (int) Math.ceil(length);
        float increment = length / fixedLength;

        // Get the array capacity and create an empty one
        float[][] points = new float[fixedLength][3];
        float[] point = new float[2];
        float[] tangent = new float[2];

        // Holders
        float distance = 0;

        // Cycle all the point of the path using an arbitrary increment
        for (int index = 0; index < fixedLength; index ++) {
            // Check for exit
            if (Float.compare(distance, length) == 1)
                distance = length;

            // Find the tangent
            this.mGenericMeasure.getPosTan(distance, point, tangent);

            // Convert calculated angle to degrees
            float angle = (float) Math.atan2(tangent[1], tangent[0]);
            float degrees = (float) Math.toDegrees(angle);

            // Assign
            points[index][0] = point[0];
            points[index][1] = point[1];
            points[index][2] = degrees;

            // Next point
            distance += increment;
        }

        // Return all the array
        return points;
    }

    /**
     * Get the length of a path considering all the contours.
     * @return the path length
     */
    private float getGlobalLength() {
        // Check for empty value
        if (this.mPath == null || this.mPath.isEmpty())
            return 0.0f;

        // Reset
        this.mGenericMeasure.setPath(this.mPath, this.mForceClosed);

        // Cycle all the contours
        float globalLen = 0.0f;
        do {
            // Increase the global length
            globalLen += this.mGenericMeasure.getLength();

        } while (this.mGenericMeasure.nextContour());

        // Return the global length
        return globalLen;
    }

    /**
     * Get a segment from the current contour
     * @param start the start position
     * @param end the end position
     * @param offset the contour offset
     * @return the new path
     */
    private Path getOffsetSegment(float start, float end, float offset) {
        // Store the path as new
        Path segment = new Path();
        this.mGenericMeasure.getSegment(start - offset, end - offset, segment, true);

        // On KITKAT and earlier releases, the resulting path may not display on a
        // hardware-accelerated Canvas. A simple workaround is to add a single
        // operation to this path segment.
        segment.rLineTo(0, 0);

        // Return the new object
        return segment;
    }

    /**
     * Get the path bounds considering all the contours.
     * @return the path boundaries
     */
    private RectF getGlobalBounds() {
        // Check for empty value
        if (this.mPath == null || this.mPath.isEmpty())
            return new RectF();

        // Reset
        this.mGenericMeasure.setPath(this.mPath, this.mForceClosed);
        RectF bounds = new RectF(
                Float.MAX_VALUE, Float.MAX_VALUE,
                Float.MIN_VALUE, Float.MIN_VALUE
        );

        // Cycle all the points in path
        for (float[] point : this.mPathPoints) {
            // Check the position of the current point and update the bounds
            if (bounds.left > point[0]) bounds.left = point[0];
            if (bounds.right < point[0]) bounds.right = point[0];

            if (bounds.top > point[1]) bounds.top = point[1];
            if (bounds.bottom < point[1]) bounds.bottom = point[1];
        }

        // Else
        return bounds;
    }

    /**
     * Get the distance between two point on a 2D plane
     * @param first point
     * @param second point
     * @return distance
     */
    private float getPointsDistance(float[] first, float[] second) {
        return (float) Math.sqrt(
                Math.pow(first[0] - second[0], 2) + Math.pow(first[1] - second[1], 2));
    }

    /**
     * Init all the class properties
     */
    private void init(Path path, boolean forceClosed) {
        // Keep the values
        this.mPath = path;
        this.mForceClosed = forceClosed;

        // Init
        this.mPathPoints = null;
        this.mGenericMeasure = new PathMeasure();
        this.mLength = this.getGlobalLength();
        this.mPathPoints = this.getPathPoints();
        this.mBounds = this.getGlobalBounds();
    }


    // ***************************************************************************************
    // Overrides

    /**
     * Set the current path.
     * @param path          the source path
     * @param forceClosed   force to close
     */
    @Override
    public void setPath(Path path, boolean forceClosed) {
        // Super
        super.setPath(path, forceClosed);

        // Init
        if (this.mPath == null || !this.mPath.equals(path))
            this.init(path, forceClosed);
    }

    /**
     * Get the length of a path considering all the contours.
     * If the path changed you must recall a setPath to update this value.
     * @return the path length
     */
    @Override
    public float getLength() {
        return this.mLength;
    }

    /**
     * Pins distance to 0 <= distance <= getLength(), and then computes the corresponding position
     * and tangent. Returns false if there is no path, or a zero-length path was specified, in
     * which case position and tangent are unchanged.
     * Noted that this override method consider all contours.
     *
     * @param distance  The distance along the current contour to sample
     * @param pos       If not null, returns the sampled position (x==[0], y==[1])
     * @param tan       If not null, returns the sampled tangent (x==[0], y==[1])
     * @return          false if there was no path associated with this measure object
     */
    @Override
    public boolean getPosTan(float distance, float[] pos, float[] tan) {
        // Move the measurer on the contour where the distance fall
        float contourDistance = this.moveToContour(distance);

        // If not found exit
        if (contourDistance == -1)
            return false;

        // Get the info
        return this.mGenericMeasure.getPosTan(distance - contourDistance, pos, tan);
    }

    /**
     * Given a start and stop distance, return in dst the intervening segment(s).
     * If the segment is zero-length, return false, else return true.
     * startD and stopD are pinned to legal values (0..getLength()).
     * If startD <= stopD then return false (and leave dst untouched).
     * Begin the segment with a moveTo if startWithMoveTo is true.
     * On {@link Build.VERSION_CODES#KITKAT} and earlier
     * releases, the resulting path may not display on a hardware-accelerated Canvas.
     * A simple workaround is to add a single operation to this path, such as
     * <code>dst.rLineTo(0, 0)</code>.
     * Noted that this override method consider all contours.
     *
     * @param startD            the start distance
     * @param stopD             the end distance
     * @param dst               the destination path
     * @param startWithMoveTo   start with a moveTo calling
     */
    @Override
    public boolean getSegment(float startD, float stopD, Path dst, boolean startWithMoveTo) {
        // Check for proper values
        if (dst == null || this.mPath == null || startD > stopD)
            return false;

        // Reset all
        this.mGenericMeasure.setPath(this.mPath, this.mForceClosed);
        dst.reset();

        // Cycle all contours
        float globalLen = 0.0f;
        do {
            // Get the segment
            Path segment = this.getOffsetSegment(startD, stopD, globalLen);
            if (!segment.isEmpty())
                // Add the extracted segment to the destination path
                dst.addPath(segment);

            // Update the global distance
            globalLen += this.mGenericMeasure.getLength();

        } while (this.mGenericMeasure.nextContour());

        // Return the result
        return !dst.isEmpty();
    }


    // ***************************************************************************************
    // Public methods

    /**
     * Get the source path.
     * @return a Path
     */
    @SuppressWarnings("unused")
    public Path getPath() {
        return this.mPath;
    }

    /**
     * Get the source path, between all contours, where the distance fall.
     * @return a Path
     */
    @SuppressWarnings({"unused"})
    public Path getPath(float distance) {
        // Move on right contour
        float offset = this.moveToContour(distance);

        // Check
        if (offset == -1)
            return null;

        // Get the segment
        float len = this.mGenericMeasure.getLength();
        return this.getOffsetSegment(0.0f, len, 0.0f);
    }

    /**
     * Get the source path given an index.
     * @return a Path
     */
    @SuppressWarnings({"unused"})
    public Path getPath(int index) {
        // Move on right contour
        float offset = this.moveToContour(index);

        // Check
        if (offset == -1)
            return null;


        // Get the segment
        float len = this.mGenericMeasure.getLength();
        return this.getOffsetSegment(0.0f, len, 0.0f);
    }

    /**
     * Get the contours count
     * @return the count
     */
    @SuppressWarnings({"unused"})
    public int countContours() {
        // Check for empty value
        if (this.mPath == null)
            return 0;

        // Reset
        this.mGenericMeasure.setPath(this.mPath, this.mForceClosed);

        // Cycle all the contours
        int index = 0;
        do {
            // Next
            index ++;

        } while (this.mGenericMeasure.nextContour());

        // Else
        return index;
    }

    /**
     * Move the global measurer on the indexed contour
     * @param index the index to find
     * @return the contour global distance from the path start
     */
    @SuppressWarnings({"unused"})
    public float moveToContour(int index) {
        // Reset
        this.mGenericMeasure.setPath(this.mPath, this.mForceClosed);

        // Holders
        int temp = 0;
        float globalLen = 0.0f;

        // Cycle all the contours
        do {
            // Get the current contour length
            float currentLength = this.mGenericMeasure.getLength();

            // If the current contour is the index return found
            if ((temp ++) == index)
                return globalLen;

            // Update the global distance from the path start
            globalLen += currentLength;

        } while (this.mGenericMeasure.nextContour());

        // Else not found
        return -1;
    }

    /**
     * Move the global measurer on contour where the distance fall
     * @param distance the distance from path start
     * @return the contour global distance from the path start
     */
    @SuppressWarnings({"unused"})
    public float moveToContour(float distance) {
        // Reset
        this.mGenericMeasure.setPath(this.mPath, this.mForceClosed);

        // Cycle all the contours
        float globalLen = 0.0f;
        do {
            // Get the current contour length
            float currentLen = this.mGenericMeasure.getLength();
            // If the distance fall in the global length return found
            if (distance <= globalLen + currentLen)
                return globalLen;

            // Increase the global length
            globalLen += currentLen;

        } while (this.mGenericMeasure.nextContour());

        // Else not found
        return -1;
    }

    /**
     * Get the path bounds.
     * Noted that this method consider all contours.
     * If the path changed you must recall a setPath to update this value.
     * As the computeBounds of the path object seem not work proper I must cycle point by point
     * of path for find the right path boundaries.
     * @return the path boundaries
     */
    @SuppressWarnings({"unused"})
    public RectF getBounds() {
        return this.mBounds;
    }

    /**
     * Find the distance (from path start) of point nearest to the passed one considering
     * only the area defined by the threshold parameter. Noted that this method consider all
     * contours.
     * @param x         the x of point
     * @param y         the y of point
     * @param threshold the threshold to define the checking area
     * @return          return -1 if the point is not on the path else the distance of the point from start
     */
    @SuppressWarnings({"unused"})
    public float getPositionOnPath(float x, float y, float threshold) {
        // Store the source point
        float[] sourcePoint = new float[2];
        sourcePoint[0] = x;
        sourcePoint[1] = y;

        // Find the rectangle around the point
        RectF area = new RectF(
                x - threshold, y - threshold,
                x + threshold, y + threshold
        );

        // Holders
        float nearest = 0.0f;
        float position = -1;

        // Cycle all points on path
        for (int index = 0; index < this.mPathPoints.length; index ++) {
            // Holders
            float[] point = this.mPathPoints[index];

            // Check the point is contained within the referenced rectangle
            if (area.contains(point[0], point[1])) {
                // Calculate the distances from found point
                float distance = this.getPointsDistance(sourcePoint, point);

                // Check if must be assigned.
                // If the distance is less save the current distance from path and the position
                // on the path from the start.
                if (position == -1 || nearest > distance) {
                    nearest = distance;
                    position = index;
                }
            }
        }

        // Return
        int fixedEnd = ((int) Math.ceil(this.mLength)) - 1;
        return position >= fixedEnd ? this.mLength: position;
    }

    /**
     * Find the distance (from path start) of point nearest to the passed one.
     * Noted that this method consider all contours.
     * @param x the x of point
     * @param y the y of point
     * @return  the calculated distance
     */
    @SuppressWarnings("unused")
    public float getPositionOnPath(float x, float y) {
        return this.getPositionOnPath(x, y, 0.0f);
    }

    /**
     * Check if the passed point is on the path the threshold parameter define the checking
     * tolerance. Noted that this method take valid all contours.
     * @param x         the x of point
     * @param y         the y of point
     * @param threshold the threshold to define the checking area
     * @return          true if contain
     */
    @SuppressWarnings("unused")
    public boolean contains(float x, float y, float threshold) {
        // Check if have
        return this.getPositionOnPath(x, y, threshold) != -1;
    }

    /**
     * Get a path points approximation of 1 pixel.
     * @return x, y and angle in degrees.
     */
    @SuppressWarnings({"unused"})
    public float[][] getApproximation() {
        // Return a copy
        return Arrays.copyOf(this.mPathPoints, this.mPathPoints.length);
    }
}
