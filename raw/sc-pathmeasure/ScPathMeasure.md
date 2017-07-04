# ScPathMeasure
Extend the PathMeasure because the original class not consider the contours in its totality.
Also add some new method as a helper class.
Please take a look to the original class documentation before use this.


#### Overrides

- **void setPath(Path path, boolean forceClosed)**<br />
Set the current path.

- **float getLength()**<br />
Get the length of a path considering all the contours.
If the path changed you must recall a setPath to update this value.

- **boolean getPosTan(float distance, float[] pos, float[] tan)**<br />
Pins distance to 0 <= distance <= getLength(), and then computes the corresponding position and tangent. 
Returns false if there is no path, or a zero-length path was specified, in which case position and tangent are unchanged.
Noted that this override method consider all contours.

- **boolean getSegment(float startD, float stopD, Path dst, boolean startWithMoveTo)**<br />
Given a start and stop distance, return in dst the intervening segment(s).
If the segment is zero-length, return false, else return true.
startD and stopD are pinned to legal values (0..getLength()).
If startD <= stopD then return false (and leave dst untouched).
Begin the segment with a moveTo if startWithMoveTo is true.
Noted that this override method consider all contours.


#### Methods

- **Path[] getPaths()**<br />
Divide the current path in an array of contours.

- **int getCount()**<br />
Get the contours count.
If the path changed you must recall a setPath to update this value.

- **RectF getBounds()**<br />
Get the path bounds.
Noted that this method consider all contours.
If the path changed you must recall a setPath to update this value.
As the computeBounds of the path object seem not work proper I must cycle point by point of path for find the right path boundaries.

- **float[] getPosTan(float distance)**<br />
Get the point and its tangent on the path considering all the contours.

- **float[] findNearestPoint(float x, float y, float threshold)**<br />
Find the point nearest to the one passed.
Considering only the points inside the area defined by the threshold parameter.
Noted that this method consider all contours.
Return a structure with the following position values:
0 - x point coordinate
1 - y point coordinate
2 - point distance from the path starting
3 - measure in radiant of the tangent angle

- **boolean contains(float x, float y, float threshold)**<br />
Check if the passed point is on the path.
The threshold parameter define the checking tolerance.
Noted that this method take valid all contours.

- **float getDistance(float x, float y, float threshold)**<br />
**float getDistance(float x, float y)**<br />
Get the point distance from the path start.
Noted that this method take valid all contours.
Instead using this method you can also use findNearestPoint and control the distance value inserted in the returned array structure.

- **float[] getFirstPoint()**<br />
Get the first point of the path.
Note that this method consider all contours so get the first point of the first contour.

- **float[] getLastPoint()**<br />
Get the last point on path.
Note that this method consider all contours so get the last point of the last contour.


#License
<pre>
 Copyright 2015 Samuele Carassai

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in  writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,  either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
</pre>
