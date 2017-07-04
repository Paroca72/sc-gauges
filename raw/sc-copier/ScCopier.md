# ScCopier
Create a feature that draw a line copy of the given path.

You can define the line characteristic by setting the inner painter.<br />
This class inherit all its properties from the [ScFeature](../sc-feature/ScFeature.md) so please take a look to the related documentation.
The class is a basic class and not expose only one proprietary method and all the methods inherited from [ScFeature](../sc-feature/ScFeature.md).
<br />
<br />

> **KNOWN ISSUES**<br />
> When you using multiple colors the class will produce a **bitmap shader** and apply it on the painter.<br />
> If you will use to scale the path before draw (`onBeforeDrawCopy`), being the shader a `Bitmap`, the stroke width will scaled too.<br />
> This issue can be solved drawing the colors gradient directly on the canvas but in this case will lost the possibility to override the shader in the future.
>
> Generally the shader have problem to work proper with the hardware-accelerate enable.<br />
> So maybe better to disable it when you use more that one colors otherwise you could be have a unexpected visual result:
> `this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);`<br />
> Please note that the `ScGauge` **disable it by default**.

<br />
<br />
 
#### Public Methods

- **void setOnDrawListener(OnDrawListener listener)**<br />
Link the listener.
<br />
<br />

#### Interfaces

- **OnDrawListener**<br />
**void onBeforeDrawCopy(CopyInfo info)**<br />
Called before draw the path copy.<br />
Note that changing the `info` properties you will change the copy drawing.<br />
Properties list: `scale`, `offset`, `rotate`.
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

- **Create a bezier line and colorize it**

<img src="https://github.com/Paroca72/sc-widgets/blob/master/raw/sc-copier/1.jpg" align="right" />

```java
    // Dimensions
    int padding = 24;
    Rect drawArea = new Rect(padding, padding, 500 - padding, 300 - padding);

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

    // Feature
    ScCopier copier = new ScCopier(path);
    copier.getPainter().setStrokeWidth(8);
    copier.setColors(Color.RED, Color.GREEN, Color.BLUE);
    copier.setColorsMode(ScCopier.ColorsMode.SOLID);
    copier.draw(canvas);

    // Add the bitmap to the container
    imageContainer.setImageBitmap(bitmap);
```
<br />
<br />

- **Using `onBeforeDrawCopy` listener method.**
Refer to known issue listed above.

<img src="https://github.com/Paroca72/sc-widgets/blob/master/raw/sc-copier/2.jpg" align="right" />

```java
    ...
    // Feature
    ScCopier copier = new ScCopier(path);
    copier.getPainter().setStrokeWidth(8);
    copier.setOnDrawListener(new ScCopier.OnDrawListener() {
        @Override
        public void onBeforeDrawCopy(ScCopier.CopyInfo info) {
            info.scale = new PointF(0.5f, 1.0f);
            info.offset = new PointF(125.0f, 0.0f);
            info.rotate = -45;
        }
    });
    copier.draw(canvas);
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