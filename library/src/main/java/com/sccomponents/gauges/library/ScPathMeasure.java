package com.sccomponents.gauges.library;

import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.os.Build;

/**
 * Extend the PathMeasure because the original class not consider the contours in its
 * totality.
 *
 * @author Samuele Carassai
 * @version 4.0.0
 * @since 2016-05-26
 */
public class ScPathMeasure extends PathMeasure {

    // ***************************************************************************************
    // Private

    private Path mPath;
    private boolean mForceClosed;

    // For internal calculation
    private PathMeasure mGenericMeasure;


    // ***************************************************************************************
    // Constructor

    @SuppressWarnings({"unused"})
    public ScPathMeasure(Path path, boolean forceClosed) {
        // Super
        super(path, forceClosed);

        // Init
        this.mPath = path;
        this.mForceClosed = forceClosed;
        this.mGenericMeasure = new PathMeasure();
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public ScPathMeasure() {
        // Super
        super();

        // Init
        this.mGenericMeasure = new PathMeasure();
    }


    // ***************************************************************************************
    // Private methods

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
     * Internal method to get the path contours info.
     * @return the bounds area
     */
    private RectF getContourBounds() {
        // Check for empty values
        if (this.mPath == null || this.mPath.isEmpty())
            return new RectF();

        // Holders
        RectF bounds = new RectF();
        float[] point = new float[2];
        boolean running = true;

        float len = this.mGenericMeasure.getLength();
        float distance = 0.0f;

        // Reset the path and the bounds
        this.mGenericMeasure.setPath(this.mPath, this.mForceClosed);
        bounds.set(Float.MAX_VALUE, Float.MAX_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);

        // Cycle all the point of the path using an arbitrary increment
        while (running) {
            // Check for exit
            if (Float.compare(distance, len) == 1) {
                distance = len;
                running = false;
            }

            // Define the point holder and get the point
            this.mGenericMeasure.getPosTan(distance, point, null);

            // Check the position of the current point and update the bounds
            if (bounds.left > point[0]) bounds.left = point[0];
            if (bounds.right < point[0]) bounds.right = point[0];

            if (bounds.top > point[1]) bounds.top = point[1];
            if (bounds.bottom < point[1]) bounds.bottom = point[1];

            // Next point
            distance += 1.0f;
        }

        // Return
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
     * Find the distance (from contour start) of point nearest to the passed one considering
     * only the area defined by the threshold parameter. Noted that this method consider just
     * the current contours.
     * @param x         the x of point
     * @param y         the y of point
     * @param threshold the threshold to define the checking area
     * @return          return [distanceFromPath, positionOnPath]
     */
    private float[] getContourDistance(float x, float y, float threshold) {
        // Init
        float[] info = new float[2];
        info[0] = -1;
        info[1] = -1;

        float[] sourcePoint = new float[2];
        sourcePoint[0] = x;
        sourcePoint[1] = y;

        // Find the rectangle around the point
        RectF area = new RectF(
                x - threshold, y - threshold,
                x + threshold, y + threshold
        );

        // Reset the path
        this.mGenericMeasure.setPath(this.mPath, this.mForceClosed);

        // Holders
        float[] currentPoint = new float[2];
        float len = this.mGenericMeasure.getLength();
        float currentPosition = 0;

        // Cycle all the point of the path using an arbitrary increment
        boolean running = true;
        while (running) {
            // Check for exit
            if (currentPosition > len) {
                currentPosition = len;
                running = false;
            }

            // Get the points position on the path
            this.mGenericMeasure.getPosTan(currentPosition, currentPoint, null);

            // Check the point is contained within the referenced rectangle
            if (area.contains(currentPoint[0], currentPoint[1])) {
                // Calculate the distances from found point
                float distance = this.getPointsDistance(sourcePoint, currentPoint);

                // Check if must be assigned.
                // If the distance is less save the current distance from path and the position
                // on the path from the start.
                if (info[0] == -1 || info[0] > distance) {
                    info[0] = distance;
                    info[1] = currentPosition;
                }
            }

            // Next point
            currentPosition += 1.0f;
        }

        // Return the distance
        return info;
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
        this.mPath = path;
        this.mForceClosed = forceClosed;
    }

    /**
     * Get the length of a path considering all the contours.
     * If the path changed you must recall a setPath to update this value.
     * @return the path length
     */
    @Override
    public float getLength() {
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
    @SuppressWarnings({"unused", "WeakerAccess"})
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
    @SuppressWarnings({"unused", "WeakerAccess"})
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
    @SuppressWarnings({"unused", "WeakerAccess"})
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
    @SuppressWarnings({"unused", "WeakerAccess"})
    public RectF getBounds() {
        // Holders
        RectF globalBounds = new RectF();

        // Reset
        this.mGenericMeasure.setPath(this.mPath, this.mForceClosed);

        // Cycle all the contours
        do {
            // Get the current contour bounds and sum to the global
            RectF currentBounds = this.getContourBounds();
            globalBounds.union(currentBounds);

        } while (this.mGenericMeasure.nextContour());

        // Else
        return globalBounds;
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
    @SuppressWarnings({"unused", "WeakerAccess"})
    public float getPositionOnPath(float x, float y, float threshold) {
        // Reset
        this.mGenericMeasure.setPath(this.mPath, this.mForceClosed);

        // Holders
        float distance = -1;
        float nearest = -1;
        float globalLen = 0.0f;

        // Cycle all the contours
        do {
            // Get the current contour nearest point to the passed coordinates
            float[] info = this.getContourDistance(x, y, threshold);

            // Compare.
            // The info position is relative to the contour so need to be globalized.
            // 0 - Distance from path
            // 1 - Position on path
            if (nearest == -1 || info[0] > nearest) {
                nearest = info[0];
                distance = info[1] + globalLen;
            }

            // Update the length
            globalLen += this.mGenericMeasure.getLength();

        } while (this.mGenericMeasure.nextContour());

        // Return the distance from the path start
        return distance;
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

}
