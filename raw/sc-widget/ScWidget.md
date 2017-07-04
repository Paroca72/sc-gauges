# ScWidget
The base class for all widget component of this series.<br />
Contains just utility methods for facilitate the building of a component.
<br />
<br />

## ScWidget class details
This is an abstract class and extend the <code>View</code> class.<br />
This class cannot instanced directly but must be inherited.
<br />
<br />

#### Static methods

- **float valueRangeLimit(float value, float startValue, float endValue)**<br />
**int valueRangeLimit(int value, int startValue, int endValue)**<br />
Limit number within a range.<br />
This method not consider the sign and the upper and lower values limit order.

- **boolean withinRange(float value, float startValue, float endValue)**<br />
Check if number is within a values range.<br />
This method not consider the sign and the upper and lower values limit order.

- **float findMaxValue(float... values)**<br />
Find the max given a series of values.

- **RectF inflateRect(RectF source, float value, boolean holdOrigin)**<br />
**RectF inflateRect(RectF source, float value)**<br />
Inflate a rectangle by the passed value.<br />
The method return a new inflated rectangle and can alter the origin too.

- **RectF resetRectToOrigin(RectF rect)**<br />
Reset the rectangle to its origin.

- **void swapArrayPosition(T[] source, int first, int second)**<br />
**void swapArrayPosition(int[] source, int first, int second)**<br />
Swap two array elements position.
<br />
<br />

#### Public methods

- **float dipToPixel(float dip)**<br />
Convert Dip to Pixel.
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