package com.sccomponents.gauges.library;

import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

/**
 * Extend the PathMeasure because the original class not consider the contours in its
 * totality.
 *
 * @author Samuele Carassai
 * @version 3.1.0
 * @since 2016-05-26
 */
public class ScPathMeasure extends PathMeasure {

    // ***************************************************************************************
    // Private

    private Path mPath;
    private boolean mForceClosed;

    private RectF mBounds;
    private float mLength;
    private Path[] mPaths;

    // For internal calculation
    private float[] mGenericPoint;
    private Path mGenericPath;
    private PathMeasure mGenericMeasure;


    // ***************************************************************************************
    // Constructor

    @SuppressWarnings({"unused", "WeakerAccess"})
    public ScPathMeasure() {
        // Super
        super();

        // Init
        this.mGenericPoint = new float[2];
        this.mGenericMeasure = new PathMeasure();
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public ScPathMeasure(Path path, boolean forceClosed) {
        // Super
        super(path, forceClosed);

        // Init
        this.mPath = path;
        this.mForceClosed = forceClosed;
        this.mGenericPoint = new float[2];
        this.mGenericPath = new Path();
        this.mGenericMeasure = new PathMeasure();

        // Get the path info
        this.divideContours();
        this.getPathInfo();
    }


    // ***************************************************************************************
    // Private methods

    /**
     * Divide all contours in single paths
     */
    private void divideContours() {
        // Reset
        this.mPaths = new Path[] {};

        // Check for empty values
        if (this.mPath == null || this.mPath.isEmpty())
            return;

        // Reset the path
        this.mGenericMeasure.setPath(this.mPath, this.mForceClosed);
        List<Path> list = new ArrayList<>();

        // Cycle all paths
        do {
            // Find the height of the path
            float len = this.mGenericMeasure.getLength();

            // Consider only the path with height major of zero
            if (len > 0.0f) {
                // Extract the current path segment and store it in list
                Path segment = new Path();
                this.mGenericMeasure.getSegment(0, len, segment, true);

                // On KITKAT and earlier releases, the resulting path may not display on a
                // hardware-accelerated Canvas. A simple workaround is to add a single
                // operation to this path segment.
                segment.rLineTo(0, 0);
                list.add(segment);
            }
        } while (this.mGenericMeasure.nextContour());

        // Hold the paths as array
        Path[] buffer = new Path[list.size()];
        this.mPaths = list.toArray(buffer);
    }

    /**
     * Internal method to get the path contours info.
     */
    private void getPathInfo() {
        // Reset
        this.mBounds = new RectF();
        this.mLength = 0.0f;

        // Check for empty values
        if (this.mPath == null || this.mPath.isEmpty())
            return;

        // Reset the path and the bounds
        this.mGenericMeasure.setPath(this.mPath, this.mForceClosed);
        this.mBounds.set(Float.MAX_VALUE, Float.MAX_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);

        // Cycle all paths
        do {
            // Find the height of the path
            float len = this.mGenericMeasure.getLength();
            int distance = 0;

            // Add the current height to the global height
            this.mLength += len;

            // Cycle all the point of the path using an arbitrary increment
            while (distance < len) {
                // Define the point holder and get the point
                float[] point = new float[2];
                this.mGenericMeasure.getPosTan(distance, point, null);

                // Check the position of the current point and update the bounds
                if (this.mBounds.left > point[0]) this.mBounds.left = point[0];
                if (this.mBounds.right < point[0]) this.mBounds.right = point[0];

                if (this.mBounds.top > point[1]) this.mBounds.top = point[1];
                if (this.mBounds.bottom < point[1]) this.mBounds.bottom = point[1];

                // Next point
                distance++;
            }

        } while (this.mGenericMeasure.nextContour());
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

        // Get the path info
        this.divideContours();
        this.getPathInfo();
    }

    /**
     * Get the height of a path considering all the contours.
     * If the path changed you must recall a setPath to update this value.
     * @return the path height
     */
    @Override
    public float getLength() {
        return this.mLength;
    }

    /**
     * Pins distance to 0 <= distance <= getHeight(), and then computes the corresponding position
     * and tangent. Returns false if there is no path, or a zero-height path was specified, in
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
        // Holders
        float currentDistance = 0.0f;
        float lastDistance = 0.0f;
        boolean found = false;

        // Reset the path
        this.mGenericMeasure.setPath(this.mPath, this.mForceClosed);

        // Cycle all contours
        do {
            // add the current contour height to the current distance
            currentDistance += this.mGenericMeasure.getLength();

            // Check if are on the right contour
            if (distance <= currentDistance) {
                // Get the point and tangent and exit
                found = this.mGenericMeasure
                        .getPosTan(distance - lastDistance, pos, tan);
                break;
            }

            // Save the current distance
            lastDistance = currentDistance;

        } while (this.mGenericMeasure.nextContour());

        // Return if found
        return found;
    }

    /**
     * Given a start and stop distance, return in dst the intervening segment(s).
     * If the segment is zero-height, return false, else return true.
     * startD and stopD are pinned to legal values (0..getHeight()).
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
        if (dst == null)
            return false;
        else
            dst.reset();

        if (this.mPath == null || startD > stopD)
            return false;

        // Reset the path
        this.mGenericMeasure.setPath(this.mPath, this.mForceClosed);
        float currentDistance = 0.0f;

        // Cycle all contours
        do {
            // The current contour lengths
            float contourLen = this.mGenericMeasure.getLength();
            float contourStart = currentDistance;
            float contourEnd = contourStart + contourLen;

            // Check if are on the right contour
            if (startD <= contourEnd) {
                // The current start and end segment to take
                float currStart = 0.0f;
                float currEnd = 0.0f;

                // Limit the values
                if (contourEnd >= startD) {
                    currStart = startD - contourStart;
                    if (currStart < 0.0f) currStart = 0.0f;

                    currEnd = stopD - contourStart;
                    if (currEnd > contourLen) currEnd = contourLen;
                }

                // If need take the segment
                if (currStart < currEnd) {
                    // Extract the segment
                    this.mGenericPath.reset();
                    this.mGenericMeasure.getSegment(currStart, currEnd, this.mGenericPath, startWithMoveTo);

                    // On KITKAT and earlier releases, the resulting path may not display on a
                    // hardware-accelerated Canvas. A simple workaround is to add a single
                    // operation to this path segment.
                    this.mGenericPath.rLineTo(0, 0);

                    // Add the extracted segment to the destination path
                    dst.addPath(this.mGenericPath);
                }
            }

            // Update the global distance
            currentDistance = contourEnd;

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
        // Check domain
        if (distance < 0.0f)
            return null;

        // Cycle all the paths (contours) summing the height.
        // When the global height is over return the current path.
        float global = 0.0f;
        for (Path path : this.mPaths) {
            // Get the path measure
            this.mGenericMeasure.setPath(path, false);
            global += this.mGenericMeasure.getLength();

            // Check the distance
            if (distance < global)
                return path;
        }

        // Not found
        return null;
    }

    /**
     * Get the real distance from the own contour.
     * @param distance  from the start
     * @return          the distance from the contour start
     */
    @SuppressWarnings("unused")
    public float getContourDistance(float distance) {
        // Cycle all the paths (contours) summing the height.
        // When the global height is over return the current path.
        float global = 0.0f;
        for (Path path : this.mPaths) {
            // Get the path measure
            this.mGenericMeasure.setPath(path, false);
            float length = this.mGenericMeasure.getLength();

            // Check the distance
            if (distance < global + length)
                return distance - global;

            // Increase the global distance
            global += length;
        }

        // Not found
        return global;
    }

    /**
     * Divide the current path in an array of contours.
     * @return an array of Path
     */
    @SuppressWarnings({"unused", "WeakerAccess"})
    public Path[] getPaths() {
        return this.mPaths;
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
    @SuppressWarnings({"unused", "WeakerAccess"})
    public int getDistance(float x, float y, float threshold) {
        // Find the rectangle around the point
        float left = x - threshold;
        float top = y - threshold;
        float right = x + threshold;
        float bottom = y + threshold;

        // Define the points holder
        float[] nearest = null;
        int nearestDistance = -1;
        int globalDistance = 0;

        // Reset the path
        this.mGenericMeasure.setPath(this.mPath, this.mForceClosed);

        // Cycle all contours
        do {
            // Get the current contour height
            double len = Math.ceil(this.mGenericMeasure.getLength());

            // Cycle all the point of the path using an arbitrary increment
            int distance = 0;
            while (distance <= len) {
                // Get the points position on the path
                this.mGenericMeasure
                        .getPosTan(distance, this.mGenericPoint, null);

                // Check if the threshold is infinite or the point is contained within the
                // referenced rectangle
                if (left <= this.mGenericPoint[0] && right >= this.mGenericPoint[0] &&
                        top <= this.mGenericPoint[1] && bottom >= this.mGenericPoint[1]) {
                    // Trigger to assign the nearest value
                    boolean toAssign = nearest == null;
                    if (!toAssign) {
                        // Calculate the distances from found point
                        float currentPointsDistance = (float) Math
                                .sqrt(Math.pow(x - this.mGenericPoint[0], 2) + Math.pow(y - this.mGenericPoint[1], 2));
                        float nearestPointsDistance = (float) Math
                                .sqrt(Math.pow(x - nearest[0], 2) + Math.pow(y - nearest[1], 2));

                        // If the current distance is less than the nearest point distance the
                        // nearest point must be reassigned
                        toAssign = currentPointsDistance < nearestPointsDistance;
                    }

                    // Check if must be assigned
                    if (toAssign) {
                        // Initialize
                        if (nearest == null)
                            nearest = new float[2];

                        // Assign the current point to the nearest
                        nearest[0] = this.mGenericPoint[0];
                        nearest[1] = this.mGenericPoint[1];
                        nearestDistance = globalDistance;
                    }
                }

                // Next point
                globalDistance++;
                distance++;
            }
        } while (this.mGenericMeasure.nextContour());

        // Return the distance
        return nearestDistance;
    }

    /**
     * Find the distance (from path start) of point nearest to the passed one.
     * Noted that this method consider all contours.
     * @param x the x of point
     * @param y the y of point
     * @return  the calculated distance
     */
    @SuppressWarnings("unused")
    public float getDistance(float x, float y) {
        return this.getDistance(x, y, 0.0f);
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
        return this.getDistance(x, y, threshold) != -1;
    }

}
