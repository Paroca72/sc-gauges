# ScWriter

Create a feature that draw a series of texts on the given path.

By default each text token will follow the path and will be distributed with equity on all length of the path. 
Also this feature allow to define the position and rotation of the text respect to the path.
This class inherit all its properties from the [ScFeature](../sc-feature/ScFeature.md) so please take a look to the related documentation.

> **IMPORTANT**<br />
> When you use the bending to write the text on the path will separate the path in segments.
> Each text token will be write on each corresponding path segment.
>
> If the you move the token offset on the x coordinate and the token will be over the segment limit the token will be clipped.
> The better way to avoid this issue it disable the bending (`setUnbend`).

<br />
<br />

#### Public methods

- **void setOnDrawListener(OnDrawListener listener)**<br />
Link the listener.
<br />
<br />

#### Getter and Setter

- **get/setTokens**  -> `String...` value, default `null`<br />
Set the string tokens to draw on path.

- **get/setPosition**  -> `TokenPositions` value, default `TokenPositions.OUTSIDE`<br />
Set the string tokens alignment respect the path.<br />
Possibly values by enum: `INSIDE`, `MIDDLE`, `OUTSIDE`

- **get/setUnbend**  -> `boolean` value, default `false`<br />
When unbend the text not follow the curve of the path but will follow the tangent to the starting point related to the path.

- **get/setConsiderFontMetrics**  -> `boolean` value, default `true`<br />
Set true if want that the offset calculation consider the font metrics too.

- **get/setLastTokenOnEnd**  -> `boolean` value, default `false`<br />
Set true if want that the last token is forced to draw to the end of the path.<br />
Note that the last token on the last point of path cannot work proper with the bending text enable. 
So, if value is true, this method will forced to disable the bending.
<br />
<br />

#### Interfaces

- **OnDrawListener**<br />
**void onBeforeDrawToken(TokenInfo info)**<br />
Called before draw string token on the path.<br />
Note that changing the `info` properties you will change the drawing.<br />
Properties list: `point`, `index`, `text`, `distance`, `angle`, `unbend`, `color`, `visible`, `offset`, `position`.
<br />
<br />

---
###### Let's play

- **Common xml configuration**
```xml
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:sc="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:gravity="center_vertical|center_horizontal"
    android:orientation="vertical">

    <ImageView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:id="@+id/image" />

</LinearLayout>
```
<br />
<br />

- **Normal flow**

<img src="https://github.com/Paroca72/sc-widgets/blob/master/raw/sc-writer/1.jpg" align="right" />

```java
    // Dimensions
    int padding = 30;
    final Rect drawArea = new Rect(padding, padding, 500 - padding, 300 - padding);

    // Get the main layout
    ImageView imageContainer = (ImageView) this.findViewById(R.id.image);
    assert imageContainer != null;

    // Create a bitmap and link a canvas
    Bitmap bitmap = Bitmap.createBitmap(
            drawArea.width() + padding * 2, drawArea.height() + padding * 2,
            Bitmap.Config.ARGB_8888
    );
    Canvas canvas = new Canvas(bitmap);
    canvas.drawColor(Color.parseColor("#f5f5f5"));

    // Create the path building a bezier curve from the left-top to the right-bottom angles of
    // the drawing area.
    Path path = new Path();
    path.moveTo(drawArea.left, drawArea.top);
    path.quadTo(drawArea.centerX(), drawArea.top, drawArea.centerX(), drawArea.centerY());
    path.quadTo(drawArea.centerX(), drawArea.bottom, drawArea.right, drawArea.bottom);

    // Draw the path only for have a reference
    Paint temp = new Paint();
    temp.setStyle(Paint.Style.STROKE);
    temp.setStrokeWidth(2);
    canvas.drawPath(path, temp);

    // Feature
    ScWriter writer = new ScWriter (path);
    writer.setTokens("FIRST", "SECOND", "THIRD");
    writer.draw(canvas);

    // Add the bitmap to the container
    imageContainer.setImageBitmap(bitmap);
```
<br />
<br />

- **Before drawing the token**

<img src="https://github.com/Paroca72/sc-widgets/blob/master/raw/sc-writer/2.jpg" align="right" />

```java
    ...
    // Create the tokens
    int count = 14;
    String[] tokens = new String[count];
    for (int index = 0; index < count; index ++) {
        tokens[index] = (index < 9 ? "0" : "") + (index + 1);
    }

    // Feature
    ScWriter writer = new ScWriter (path);
    writer.setTokens(tokens);
    writer.setUnbend(true);
    writer.setLastTokenOnEnd(true);
    writer.setColors(Color.RED, Color.BLUE, Color.GREEN, Color.CYAN);
    writer.setOnDrawListener(new ScWriter.OnDrawListener() {
        @Override
        public void onBeforeDrawToken(ScWriter.TokenInfo info) {
            info.angle -= 90;
            info.offset = new PointF(5, 10);
        }
    });
    writer.draw(canvas);
    ...
```
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