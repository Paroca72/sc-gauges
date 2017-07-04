# ScFeature
Create a feature to draw on a given path.

The feature is independent and can be used with any path.
Is enough to instantiate it passing the path object and call the draw function passing the canvas where draw.
The original design of this class was for link it with the ScDrawer to have a base drawer (the ScDrawer linked) and many features applicable to it.
The "feature" base class essentially do nothing.

For draw something, hence for specialize the feature, you need to override the onDraw method.
The base class provides only a common set of methods to display something on the path as the color manager, visibility, limits, ecc. that is useful to inherit it and create a specialized class.

One of most important characteristic of this class is the possibility to create a colors gradient and get the current color based on the distance from the path starting.
Have available two ways to calculate the current color: **GRADIENT** or **SOLID**.

The **GRADIENT** one is simple to understand while the **SOLID** choice will divide the path in sectors equal to the number of colors and return the sector referenced color.
Note that this method was created to be a generic and it based on the distance from the starting of the path so in some cases may be better to create a custom shader to attach directly to the painter.
For example if you build a filled circle might be better to create a radial gradient.
<br />
<br />

## ScFeature class details
This class expose a `draw` method to call to draw something on the passed canvas.<br />
When you use the colors properties to create a shader it will create as a BitmapShader and settle in the painter object.

> **IMPORTANT**<br />
> The `draw` method of this class do nothing. <br />
> Is important to understand that you need to override the `void onDraw()` protected method for specialize this class.
> So it is a not sense to use this class directly.

> **NOTE**<br />
> When you use a series of color the class will product a shader and will apply it on the class Painter.<br />
> If you need to have a custom shader you can assign directly a new shader to the painter calling the `getPainter` method but you must to reset the colors if you have (`setColors(null)`);

<br />
<br />

#### Public methods

- **void draw(Canvas canvas)**<br />
Draw something on the canvas.

- **void refresh()**<br />
Refresh the feature measure.

- **static PointF toPoint(float[] point)**<br />
Convert a point represented by an array to an modern object.<br />
Supposed that the 0 array position correspond to the x coordinate and on the 1 array position correspond the y coordinate.

- **static void translatePoint(PointF point, float offset, float angle)**<br />
Translate a point considering the angle (in degrees) and the offset.<br />
Move the pointer on the tangent defined by the angle.

- **static void translatePoint(PointF point, PointF offset, float angle)**<br />
Translate a point considering the angle (in degrees) and the offset (x, y).<br />
Move the pointer on the tangent defined by the angle by the x value and move the pointer on the perpendicular defined by the angle by the y value.

- **float getDistance(float percentage)**<br />
Given a percentage return back the relative distance from the path start.

- **void setLimits(float start, float end)**<br />
Set the drawing limits (in percentage).<br />
The assignment will do only if the values is different from infinity.

- **PointF getPoint(float distance)**<br />
Return a point on path given the distance from the path start.

- **float getTangentAngle(float distance)**<br />
Get the angle in degrees of the tangent to a point on the path given the distance from the start of path.

- **int getGradientColor(float distance, float length)**<br />
**int getGradientColor(float distance)**<br />
Get the current gradient color by a ratio dependently about the distance from the starting of path, the colors array and the mode to draw.
The algorithm to calculate the color will vary by the current `ColorsMode` setting.
If the colors are not defined will be returned the current color of painter.
<br />
<br />

#### Getter and Setter

- **get/setPainter**  -> `Paint` value<br />
Get or set the current painter.

- **get/setTag**  -> `String` value, default `null`<br />
Get or set the feature tag.<br />
This can useful when you must find a particular feature inside many others.

- **get/setVisible**  -> `boolean` value, default `true`<br />
Get or set the feature visibility.

- **get/setColors**  -> `int[]` value, default `null`<br />
Get or set the filling colors.<br />
When this properties is settle a shader will be created and assigned to the painter.

- **get/setColorsMode**  -> `ColorsMode` value, default `ColorsMode.GRADIENT`<br />
Define the way to fill the feature with the colors defined above.<br />
Possibly values by enum: `SOLID`, `GRADIENT`<br />
<br />
<br />

# License
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
