# ScPointer
Create a feature that draw a pointer on the given path.

By default the pointer is building as a circle with halo around and you can modify the pointer settings directly using the class properties.
Also this feature allow to define a custom bitmap to stamp on the path.
This custom bitmap will following the angle rotation by the tangent of the position on path.

The position will defined by the distance of the point from the path starting.<br />
This class inherit all its properties from the [ScFeature](../sc-feature/ScFeature.md) so please take a look to the related documentation.
<br />
<br />

#### Public methods

- **float getDistance()**<br />
Get the distance of the pointer from the start of path.

- **void setOnDrawListener(OnDrawListener listener)**<br />
Link the listener.
<br />
<br />

#### Getter and Setter

- **get/getPosition**  -> `float` value, default `0`<br />
Return the position of pointer in percentage respect to the path length.

- **get/setRadius**  -> `float` value, default `0`<br />
Set the pointer radius in pixel.

- **get/setHaloWidth**  -> `float` value, default `DEFAULT_HALO_WIDTH`<br />
Set the halo width in pixel.

- **get/setHaloAlpha**  -> `int` value `(0..255)`, default `DEFAULT_HALO_ALPHA`<br />
Set the halo alpha .

- **get/setPressed**  -> `boolean` value, default `false`<br />
Set the pointer status.
<br />
<br />

#### Interfaces

- **OnDrawListener**<br />
**void onBeforeDrawPointer(CopyInfo info)**<br />
Called before draw pointer on the path.
Note that changing the `info` properties you will change the pointer drawing.<br />
Properties list: `bitmap`, `point`, `offset`, `angle`, `color`, `pressed`.<br />
If assign a bitmap the default drawing will be bypassed and the new bitmap will be draw on the canvas following the **some** other setting.
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

- **Normal and pressed**

<img src="https://github.com/Paroca72/sc-widgets/blob/master/raw/sc-pointer/1.jpg" align="right" />

```java
    // Dimensions
    int padding = 24;
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
    ScPointer pointer = new ScPointer(path);
    pointer.setPosition(30);
    pointer.setRadius(20);
    pointer.draw(canvas);

    pointer.setPosition(70);
    pointer.setPressed(true);
    pointer.draw(canvas);

    // Add the bitmap to the container
    imageContainer.setImageBitmap(bitmap);
```
<br />
<br />

- **Play with colors.**
<img src="https://github.com/Paroca72/sc-widgets/blob/master/raw/sc-pointer/2.jpg" align="right" />

If you define a colors sequence the pointer will assume the gradient color by its position respect the path.
```java
    ...
    // Feature
    ScPointer pointer = new ScPointer(path);
    pointer.setRadius(16);
    pointer.setColors(Color.BLUE, Color.RED);

    pointer.setPosition(0);
    pointer.draw(canvas);

    pointer.setPosition(30);
    pointer.draw(canvas);

    pointer.setPosition(70);
    pointer.draw(canvas);

    pointer.setPosition(100);
    pointer.draw(canvas);
    ...
```
<br />
<br />

- **Custom bitmap.**

<img src="https://github.com/Paroca72/sc-widgets/blob/master/raw/sc-pointer/3.jpg" align="right" />

```java
    ...
    // Preload the bitmap
    final Bitmap custom = BitmapFactory.decodeResource(this.getResources(), R.drawable.arrow);

    // Feature
    ScPointer pointer = new ScPointer(path);
    pointer.setOnDrawListener(new ScPointer.OnDrawListener() {
        @Override
        public void onBeforeDrawPointer(ScPointer.PointerInfo info) {
            info.bitmap = custom;
            info.offset = new PointF(-16, -16);
            // Uncomment the following line if you not want bitmap rotation
            // info.angle = 0;
        }
    });

    // Draw 10 arrows
    for (int position = 0; position <= 100; position = position + 10) {
        pointer.setPosition(position);
        pointer.draw(canvas);
    }
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