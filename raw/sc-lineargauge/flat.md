# ScLinearGauge examples
Following some example of the [ScLinearGauge](../sc-lineargauge/ScLinearGauge.md) application.

Is simple to understand that inheriting from the [ScGauge](../sc-gauge/ScGauge.md) class the possibilities are infinite.
So this example as been only a demonstration of the most used configurations.

> **ATTENTION**<br />
> Please keep in mind that you can enable the user input (`setInputEnabled`) to allow the user to drive the gauge values.<br />
> Also you can use the animator (`getHighValueAnimator` or `getLowValueAnimator`) to animate the value changing.

<br />
<br />

---
## Example 1

<img align="right" src="https://github.com/Paroca72/sc-widgets/blob/master/raw/sc-lineargauge/f-01.jpg">

```xml
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:background="#ECECEA"
        android:padding="10dp"
        android:orientation="vertical">

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Progress"
            android:textColor="#5B5B5A"
            android:textSize="24dp" />

        <com.sccomponents.widgets.ScLinearGauge 
            xmlns:sc="http://schemas.android.com/apk/res-auto"
            android:id="@+id/line"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:paddingBottom="10dp"
            android:paddingTop="10dp"
            sc:scc_progress_colors="#3589F6"
            sc:scc_progress_size="6dp"
            sc:scc_stroke_color="#ABCDED"
            sc:scc_stroke_size="6dp"
            sc:scc_value="75" />

    </LinearLayout>
```

---
## Example 2

<img align="right" src="https://github.com/Paroca72/sc-widgets/blob/master/raw/sc-lineargauge/f-02.jpg">

```xml
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:background="#ECECEA"
        android:padding="10dp"
        android:orientation="vertical">

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:id="@+id/counter"
            android:textColor="#5B5B5A"
            android:textSize="24dp" />

        <com.sccomponents.widgets.ScLinearGauge
            xmlns:sc="http://schemas.android.com/apk/res-auto"
            android:id="@+id/line"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:paddingBottom="20dp"
            android:paddingTop="20dp"
            sc:scc_progress_colors="#3589F6"
            sc:scc_progress_size="6dp"
            sc:scc_stroke_color="#ABCDED"
            sc:scc_stroke_size="6dp"
            sc:scc_pointer_radius="10dp"
            sc:scc_pointer_colors="#3589F6"
            sc:scc_path_touchable="true" />

    </LinearLayout>
```

```java
    // Find the components
    final ScLinearGauge gauge = (ScLinearGauge) this.findViewById(R.id.line);
    assert gauge != null;

    final TextView counter = (TextView) this.findViewById(R.id.counter);
    assert counter != null;

    // Set the value
    gauge.setHighValue(75);

    // Each time I will change the value I must write it inside the counter text.
    gauge.setOnEventListener(new ScGauge.OnEventListener() {
        @Override
        public void onValueChange(float lowValue, float highValue) {
            counter.setText("Value: " + (int) highValue + "%");
        }
    });
```

---
## Example 3

<img align="right" src="https://github.com/Paroca72/sc-widgets/blob/master/raw/sc-lineargauge/n-02.jpg">

```xml
    <LinearLayout
        android:layout_width="wrap_content"
        android:layout_height="300dp"
        android:background="#f5f5f5"
        android:orientation="vertical"
        android:padding="10dp">

        <com.sccomponents.widgets.ScLinearGauge xmlns:sc="http://schemas.android.com/apk/res-auto"
            android:id="@+id/line"
            android:layout_width="wrap_content"
            android:layout_height="match_parent"
            android:paddingBottom="10dp"
            android:paddingLeft="30dp"
            android:paddingRight="30dp"
            android:paddingTop="10dp"
            sc:scc_orientation="vertical"
            sc:scc_stroke_size="10dp"
            sc:scc_stroke_color="#dcdcdc"
            sc:scc_progress_size="10dp"
            sc:scc_progress_colors="#67ce5c|#E23D3D" />

    </LinearLayout>
```

```java
    // Find the components
    final ScLinearGauge gauge = (ScLinearGauge) this.findViewById(R.id.line);
    assert gauge != null;

    // Remove all features
    gauge.removeAllFeatures();

    // Take in mind that when you tagged a feature after this feature inherit the principal
    // characteristic of the identifier.
    // For example in the case of the BASE_IDENTIFIER the feature notches (always) will be
    // settle as the color and stroke size settle for the base (in xml or via code).

    // Create the base notches.
    ScNotches base = (ScNotches) gauge.addFeature(ScNotches.class);
    base.setTag(ScGauge.BASE_IDENTIFIER);
    base.setCount(20);
    base.setLength(gauge.dipToPixel(18));

    // Note that I will create two progress because to one will add the blur and to the other
    // will be add the emboss effect.

    // Create the progress notches.
    ScNotches progressBlur = (ScNotches) gauge.addFeature(ScNotches.class);
    progressBlur.setTag(ScGauge.PROGRESS_IDENTIFIER);
    progressBlur.setCount(20);
    progressBlur.setLength(gauge.dipToPixel(18));

    // Create the progress notches.
    ScNotches progressEmboss = (ScNotches) gauge.addFeature(ScNotches.class);
    progressEmboss.setTag(ScGauge.PROGRESS_IDENTIFIER);
    progressEmboss.setCount(20);
    progressEmboss.setLength(gauge.dipToPixel(18));

    // Blur filter
    BlurMaskFilter blur = new BlurMaskFilter(5.0f, BlurMaskFilter.Blur.SOLID);
    progressBlur.getPainter().setMaskFilter(blur);

    // Emboss filter
    EmbossMaskFilter emboss = new EmbossMaskFilter(new float[]{0.0f, 1.0f, 0.5f}, 0.8f, 3.0f, 0.5f);
    progressEmboss.getPainter().setMaskFilter(emboss);

    // Set the value
    gauge.setHighValue(75);
```


---
## Example 4

<img align="right" src="https://github.com/Paroca72/sc-widgets/blob/master/raw/sc-lineargauge/n-01.jpg">

```xml
    <LinearLayout
        android:layout_width="wrap_content"
        android:layout_height="300dp"
        android:background="#ECECEA"
        android:orientation="vertical"
        android:padding="10dp">

        <com.sccomponents.widgets.ScLinearGauge xmlns:sc="http://schemas.android.com/apk/res-auto"
            android:id="@+id/line"
            android:layout_width="wrap_content"
            android:layout_height="match_parent"
            android:paddingBottom="10dp"
            android:paddingLeft="50dp"
            android:paddingRight="20dp"
            android:paddingTop="10dp"
            sc:scc_orientation="vertical"
            sc:scc_notches="8"
            sc:scc_notches_color="#313131"
            sc:scc_progress_colors="#3589F6"
            sc:scc_progress_size="6dp"
            sc:scc_stroke_size="0dp"
            sc:scc_text_align="left"
            sc:scc_text_position="outside"
            sc:scc_text_tokens="0|50|100"
            sc:scc_text_unbend="true" />

    </LinearLayout>
```

```java
    // Find the components
    final ScLinearGauge gauge = (ScLinearGauge) this.findViewById(R.id.line);
    assert gauge != null;

    // Set the last token on the end of path
    final ScWriter writer = (ScWriter) gauge.findFeature(ScGauge.WRITER_IDENTIFIER);
    writer.setLastTokenOnEnd(true);
    
    // Set the value
    gauge.setHighValue(25);

    // Before draw
    gauge.setOnDrawListener(new ScGauge.OnDrawListener() {
        @Override
        public void onBeforeDrawCopy(ScCopier.CopyInfo info) {
            // NOP
        }

        @Override
        public void onBeforeDrawNotch(ScNotches.NotchInfo info) {
            // The notch length
            info.length = gauge.dipToPixel(info.index % 4 == 0 ? 20 : 10);
        }

        @Override
        public void onBeforeDrawPointer(ScPointer.PointerInfo info) {
            // NOP
        }

        @Override
        public void onBeforeDrawToken(ScWriter.TokenInfo info) {
            // Get the text bounds
            Rect bounds = new Rect();
            info.source.getPainter().getTextBounds(info.text, 0, info.text.length(), bounds);

            // Zero angle
            info.angle = 0.0f;
            info.offset.x = -50 - bounds.width();
            info.offset.y = bounds.height() / 2;
        }
    });
```

---
## Example 5

<img align="right" src="https://github.com/Paroca72/sc-widgets/blob/master/raw/sc-lineargauge/n-03.jpg">

```xml
    <FrameLayout
        android:layout_width="wrap_content"
        android:layout_height="300dp"
        android:background="#ECECEA"
        android:padding="10dp">

        <com.sccomponents.widgets.ScLinearGauge xmlns:sc="http://schemas.android.com/apk/res-auto"
            android:id="@+id/line"
            android:layout_width="wrap_content"
            android:layout_height="match_parent"
            android:paddingBottom="20dp"
            android:paddingLeft="20dp"
            android:paddingRight="50dp"
            android:paddingTop="20dp"
            sc:scc_notches="300"
            sc:scc_notches_colors="#67ce5c|#f2f200|#e23d3d"
            sc:scc_notches_length="10dp"
            sc:scc_notches_position="inside"
            sc:scc_notches_size="2dp"
            sc:scc_orientation="vertical"
            sc:scc_text_tokens="100%"
            sc:scc_text_unbend="true"
            sc:scc_text_align="left"
            sc:scc_path_touchable="true" />

    </FrameLayout>
```

```java
    // Find the components
    final ScLinearGauge gauge = (ScLinearGauge) this.findViewById(R.id.line);
    assert gauge != null;

    // Create a drawable
    final Bitmap indicator = BitmapFactory.decodeResource(this.getResources(), R.drawable.indicator);

    // Set the values.
    gauge.setHighValue(75);
    gauge.setPathTouchThreshold(40);

    // Event
    gauge.setOnDrawListener(new ScGauge.OnDrawListener() {
        @Override
        public void onBeforeDrawCopy(ScCopier.CopyInfo info) {
            // NOP
        }

        @Override
        public void onBeforeDrawNotch(ScNotches.NotchInfo info) {
            // Calculate the length
            float min = 10.0f;
            float max = 40.0f;
            float current = min + (max - min) * (info.index / (float) gauge.getNotches());

            // Apply
            info.length = gauge.dipToPixel(current);
        }

        @Override
        public void onBeforeDrawPointer(ScPointer.PointerInfo info) {
            // Check if the pointer if the high pointer
            if (info.source.getTag() == ScGauge.HIGH_POINTER_IDENTIFIER) {
                // Adjust the offset
                info.offset.x = -indicator.getWidth() / 2;
                info.offset.y = -indicator.getHeight() / 2 - gauge.getStrokeSize();
                // Assign the bitmap to the pointer info structure
                info.bitmap = indicator;
            }
        }

        @Override
        public void onBeforeDrawToken(ScWriter.TokenInfo info) {
            // Set angle and text
            info.angle = 0.0f;
            info.text = Math.round(gauge.getHighValue()) + "%";

            // Set the position
            float distance = info.source.getDistance(gauge.getHighValue());
            info.offset.x = 20;
            info.offset.y = -distance + 20;
        }
    });
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