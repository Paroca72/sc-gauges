# ScArcGauge examples
Following some example of the [ScArcGauge](../sc-arcgauge/ScArcGauge.md) application.

Is simple to understand that inheriting from the [ScGauge](../sc-gauge/ScGauge.md) class the possibilities are infinite.
These example as been only a demonstration of the most used configurations and are building by the case (**not responsive**).

> **ATTENTION**<br />
> Please keep in mind that you can enable the user input (`setInputEnabled`) to allow the user to drive the gauge values.<br />
> Also you can use the animator (`getHighValueAnimator` or `getLowValueAnimator`) to animate the value changing.

<br />
<br />


---
## Example 1
You can download the indicator image used below from [**HERE**](indicator-06.png).

<img align="right" src="https://github.com/Paroca72/sc-widgets/blob/master/raw/sc-arcgauge/n-01.jpg">

```xml
    <FrameLayout
        android:layout_width="300dp"
        android:layout_height="wrap_content"
        android:background="#f5f5f5">

        <com.sccomponents.widgets.ScArcGauge
            android:id="@+id/gauge"
            xmlns:sc="http://schemas.android.com/apk/res-auto"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_gravity="center_horizontal"
            android:padding="50dp"
            sc:scc_angle_start="180"
            sc:scc_angle_sweep="180"
            sc:scc_stroke_size="40dp"
            sc:scc_stroke_colors="#03A409|#FFF506|#EB0100"
            sc:scc_notches="10"
            sc:scc_notches_size="1dp"
            sc:scc_notches_length="20dp"
            sc:scc_notches_color="#4e4e4e"
            sc:scc_text_align="center"/>

        <ImageView
            android:id="@+id/indicator"
            android:layout_width="120dp"
            android:layout_height="wrap_content"
            android:layout_gravity="bottom|center_horizontal"
            android:src="@drawable/indicator"
            android:layout_marginLeft="40dp"
            android:layout_marginBottom="9dp"/>

    </FrameLayout>
```

```java
    // Find the components
    final ScArcGauge gauge = (ScArcGauge) this.findViewById(R.id.gauge);
    assert gauge != null;

    final ImageView indicator = (ImageView) this.findViewById(R.id.indicator);
    assert indicator != null;

    // Set the center pivot for a right rotation
    indicator.setPivotX(39);
    indicator.setPivotY(88);

    // Set the values.
    gauge.setHighValue(55);

    // Set the filter of the base
    ScFeature base = gauge.findFeature(ScGauge.BASE_IDENTIFIER);
    BlurMaskFilter filter = new BlurMaskFilter(10, BlurMaskFilter.Blur.INNER);
    base.getPainter().setMaskFilter(filter);

    // Writer
    String[] tokens = new String[10];
    for (int index = 0; index < 10; index++) {
        tokens[index] = Integer.toString((index + 1) * 10);
    }
    gauge.setTextTokens(tokens);

    ScWriter writer = (ScWriter) gauge.findFeature(ScGauge.WRITER_IDENTIFIER);
    writer.setTokenOffset(0.0f, -40.0f);

    // Each time I will change the value I must write it inside the counter text.
    gauge.setOnEventListener(new ScGauge.OnEventListener() {
        @Override
        public void onValueChange(float lowValue, float highValue) {
            // Convert the percentage value in an angle
            float angle = gauge.percentageToAngle(highValue);
            indicator.setRotation(angle);
        }
    });

    gauge.setOnDrawListener(new ScGauge.OnDrawListener() {
        @Override
        public void onBeforeDrawCopy(ScCopier.CopyInfo info) {
            // Do nothing
        }

        @Override
        public void onBeforeDrawNotch(ScNotches.NotchInfo info) {
            // Hide the first and the last
            info.visible = info.index > 0 && info.index < info.source.getCount();
        }

        @Override
        public void onBeforeDrawPointer(ScPointer.PointerInfo info) {
            // Do nothing
        }

        @Override
        public void onBeforeDrawToken(ScWriter.TokenInfo info) {
            // Highlight
            int sector = (int) (gauge.getHighValue() / 10);
            info.color = sector == info.index ? Color.BLACK : Color.parseColor("#cccccc");
        }
    });
```


---
## Example 2
You can download the indicator image used below from [**HERE**](indicator-07.png).

<img align="right" src="https://github.com/Paroca72/sc-widgets/blob/master/raw/sc-arcgauge/n-02.jpg">

```xml
    <FrameLayout
        android:layout_width="300dp"
        android:layout_height="wrap_content"
        android:background="#f5f5f5">

        <com.sccomponents.widgets.ScArcGauge
            android:id="@+id/gauge"
            xmlns:sc="http://schemas.android.com/apk/res-auto"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_gravity="center_horizontal"
            android:padding="30dp"
            sc:scc_angle_start="135"
            sc:scc_angle_sweep="270"
            sc:scc_stroke_size="10dp"
            sc:scc_notches="80"
            sc:scc_notches_size="1dp"
            sc:scc_notches_length="10dp"
            sc:scc_notches_color="#000000"
            sc:scc_notches_position="inside"
            sc:scc_progress_size="3dp"
            sc:scc_text_position="inside"
            sc:scc_path_touchable="true"/>

        <TextView
            android:layout_width="100dp"
            android:layout_height="wrap_content"
            android:id="@+id/counter"
            android:layout_gravity="center"
            android:textColor="#424242"
            android:textSize="32dp"
            android:textStyle="bold"
            android:text="0.0°"
            android:layout_marginRight="2dp"
            android:background="#d8dee0"
            android:gravity="center"
            android:paddingLeft="10dp"/>

    </FrameLayout>
```

<br />
<br />
<br />
<br />

```java
    // Find the components
    final ScArcGauge gauge = (ScArcGauge) this.findViewById(R.id.gauge);
    assert gauge != null;

    final TextView counter = (TextView) this.findViewById(R.id.counter);
    assert counter != null;

    // Create a drawable
    final Bitmap indicator = BitmapFactory.decodeResource(this.getResources(), R.drawable.indicator);

    // Set the values.
    gauge.setHighValue(14, -10, 30);
    gauge.setPathTouchThreshold(40);

    // Set colors of the base
    gauge.setColors(
            Color.parseColor("#15B7FF"), Color.parseColor("#15B7FF"),
            Color.parseColor("#98CA06"), Color.parseColor("#98CA06"),
            Color.parseColor("#98CA06"), Color.parseColor("#98CA06"),
            Color.parseColor("#98CA06"), Color.parseColor("#DC1E10")
    );
    gauge.setStrokeColorsMode(ScFeature.ColorsMode.SOLID);

    // Writer
    String[] tokens = new String[9];
    for (int index = 0; index < 9; index++) {
        tokens[index] = Integer.toString(index * 5 - 10);
    }
    gauge.setTextTokens(tokens);

    ScWriter writer = (ScWriter) gauge.findFeature(ScGauge.WRITER_IDENTIFIER);
    writer.setLastTokenOnEnd(true);

    // Each time I will change the value I must write it inside the counter text.
    gauge.setOnEventListener(new ScGauge.OnEventListener() {
        @Override
        public void onValueChange(float lowValue, float highValue) {
            // Write the value
            highValue = ScGauge.percentageToValue(highValue, -10, 30);
            float round = (Math.round(highValue * 10.0f)) / 10.0f;
            counter.setText(Float.toString(round) + "°");
        }
    });

    gauge.setOnDrawListener(new ScGauge.OnDrawListener() {
        @Override
        public void onBeforeDrawCopy(ScCopier.CopyInfo info) {
            // Check if is the progress
            if (info.source.getTag() == ScGauge.PROGRESS_IDENTIFIER) {
                // Scale and adjust the offset
                info.scale = new PointF(1.1f, 1.1f);
                info.offset = new PointF(-28, -28);

                // Adjust the color
                int color = base.getGradientColor(gauge.getHighValue(), 100);
                info.source.getPainter().setColor(color);
            }
        }

        @Override
        public void onBeforeDrawNotch(ScNotches.NotchInfo info) {
            // Move the offset
            info.offset = -info.length / 2;

            // Check for module highlight the notches than have module 5 and 10
            if (info.index % 10 == 0) {
                info.size = 6;
            } else if (info.index % 5 != 0) {
                info.length -= 5;
            }
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
            // Center on the notches
            Rect bounds = new Rect();
            info.source.getPainter().getTextBounds(info.text, 0, info.text.length(), bounds);
            info.offset.x = - (bounds.width() / 2) - 1;
        }
    });
```


---
## Example 3

<img align="right" src="https://github.com/Paroca72/sc-widgets/blob/master/raw/sc-arcgauge/n-03.jpg">

```xml
    <FrameLayout
        android:layout_width="300dp"
        android:layout_height="wrap_content"
        android:background="#f5f5f5">

        <com.sccomponents.widgets.ScArcGauge
            android:id="@+id/gauge"
            xmlns:sc="http://schemas.android.com/apk/res-auto"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_gravity="center_horizontal"
            android:padding="30dp"
            sc:scc_angle_start="-90"
            sc:scc_stroke_size="10dp"
            sc:scc_stroke_color="#dbdfe6"
            sc:scc_progress_size="10dp"
            sc:scc_progress_color="#6184be"
            />

        <LinearLayout
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:gravity="center_horizontal"
            android:layout_gravity="center">

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:textColor="#b6c2d5"
            android:textSize="30dp"
            android:text="Loading"
            android:gravity="center"
            />

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:id="@+id/counter"
            android:textColor="#6184be"
            android:textSize="44dp"
            android:textStyle="bold"
            android:text="0%"
            android:gravity="center"
            android:paddingLeft="10dp"/>

        </LinearLayout>

    </FrameLayout>
```

```java
    // Find the components
    final ScArcGauge gauge = (ScArcGauge) this.findViewById(R.id.gauge);
    assert gauge != null;

    final TextView counter = (TextView) this.findViewById(R.id.counter);
    assert counter != null;

    // Clear all default features from the gauge
    gauge.removeAllFeatures();

    // Take in mind that when you tagged a feature after this feature inherit the principal
    // characteristic of the identifier.
    // For example in the case of the BASE_IDENTIFIER the feature notches (always) will be
    // settle as the color and stroke size settle for the base (in xml or via code).

    // Create the base notches.
    ScNotches base = (ScNotches) gauge.addFeature(ScNotches.class);
    base.setTag(ScGauge.BASE_IDENTIFIER);
    base.setCount(40);
    base.setLength(gauge.dipToPixel(18));

    // Note that I will create two progress because to one will add the blur and to the other
    // will be add the emboss effect.

    // Create the progress notches.
    ScNotches progressBlur = (ScNotches) gauge.addFeature(ScNotches.class);
    progressBlur.setTag(ScGauge.PROGRESS_IDENTIFIER);
    progressBlur.setCount(40);
    progressBlur.setLength(gauge.dipToPixel(18));

    // Create the progress notches.
    ScNotches progressEmboss = (ScNotches) gauge.addFeature(ScNotches.class);
    progressEmboss.setTag(ScGauge.PROGRESS_IDENTIFIER);
    progressEmboss.setCount(40);
    progressEmboss.setLength(gauge.dipToPixel(18));

    // Blur filter
    BlurMaskFilter blur = new BlurMaskFilter(5.0f, BlurMaskFilter.Blur.SOLID);
    progressBlur.getPainter().setMaskFilter(blur);

    // Emboss filter
    EmbossMaskFilter emboss = new EmbossMaskFilter(new float[]{0.0f, 1.0f, 0.5f}, 0.8f, 3.0f, 0.5f);
    progressEmboss.getPainter().setMaskFilter(emboss);

    // Set the value
    gauge.setHighValue(60);

    // Each time I will change the value I must write it inside the counter text.
    gauge.setOnEventListener(new ScGauge.OnEventListener() {
        @Override
        public void onValueChange(float lowValue, float highValue) {
            // Write the value
            counter.setText((int) highValue + "%");
        }
    });    
```


---
## Example 4

<img align="right" src="https://github.com/Paroca72/sc-widgets/blob/master/raw/sc-arcgauge/n-04.jpg">

```xml
    <FrameLayout
        android:layout_width="300dp"
        android:layout_height="wrap_content"
        android:background="#f5f5f5">

        <com.sccomponents.widgets.ScArcGauge
            android:id="@+id/gauge"
            xmlns:sc="http://schemas.android.com/apk/res-auto"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_gravity="center_horizontal"
            android:padding="10dp"
            sc:scc_angle_start="-180"
            sc:scc_angle_sweep="90"
            sc:scc_stroke_size="4dp"
            sc:scc_stroke_color="#dbdfe6"
            sc:scc_progress_size="4dp"
            sc:scc_progress_colors="#0BA60A|#FEF301|#EA0C01"
            />

        <LinearLayout
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:gravity="right"
            android:layout_gravity="bottom|right"
            android:layout_marginRight="10dp">

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:textColor="#b6c2d5"
            android:textSize="20dp"
            android:text="speedometer"
            android:gravity="center"
            />

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:id="@+id/counter"
            android:textColor="#787b80"
            android:textSize="40dp"
            android:textStyle="bold"
            android:text="0"
            android:gravity="center"
            android:paddingLeft="10dp"/>

        </LinearLayout>

    </FrameLayout>
```

```java
    // Find the components
    final ScArcGauge gauge = (ScArcGauge) this.findViewById(R.id.gauge);
    assert gauge != null;

    final TextView counter = (TextView) this.findViewById(R.id.counter);
    assert counter != null;

    // Clear all default features from the gauge
    gauge.removeAllFeatures();

    // Take in mind that when you tagged a feature after this feature inherit the principal
    // characteristic of the identifier.
    // For example in the case of the BASE_IDENTIFIER the feature notches (always) will be
    // settle as the color and stroke size settle for the base (in xml or via code).

    // Create the base notches.
    ScNotches base = (ScNotches) gauge.addFeature(ScNotches.class);
    base.setTag(ScGauge.BASE_IDENTIFIER);
    base.setCount(40);
    base.setPosition(ScNotches.NotchPositions.INSIDE);

    // Note that I will create two progress because to one will add the blur and to the other
    // will be add the emboss effect.

    // Create the progress notches.
    ScNotches progress = (ScNotches) gauge.addFeature(ScNotches.class);
    progress.setTag(ScGauge.PROGRESS_IDENTIFIER);
    progress.setCount(40);
    progress.setPosition(ScNotches.NotchPositions.INSIDE);

    // Set the value
    gauge.setHighValue(12000, 0, 13000);

    // Each time I will change the value I must write it inside the counter text.
    gauge.setOnEventListener(new ScGauge.OnEventListener() {
        @Override
        public void onValueChange(float lowValue, float highValue) {
            // Write the value
            int value = (int) ScGauge.percentageToValue(highValue, 0, 13000);
            counter.setText(Integer.toString(value));
        }
    });

    // Before draw
    gauge.setOnDrawListener(new ScGauge.OnDrawListener() {
        @Override
        public void onBeforeDrawCopy(ScCopier.CopyInfo info) {
            // Do nothing
        }

        @Override
        public void onBeforeDrawNotch(ScNotches.NotchInfo info) {
            // Set the length of the notch
            info.source.setLength(gauge.dipToPixel(info.index + 5));
        }

        @Override
        public void onBeforeDrawPointer(ScPointer.PointerInfo info) {
            // Do nothing
        }

        @Override
        public void onBeforeDrawToken(ScWriter.TokenInfo info) {
            // Do nothing
        }
    });
```


---
## Example 5

<img align="right" src="https://github.com/Paroca72/sc-widgets/blob/master/raw/sc-arcgauge/n-05.jpg">

```xml
    <FrameLayout
        android:layout_width="280dp"
        android:layout_height="wrap_content"
        android:background="#000000">

        <com.sccomponents.widgets.ScArcGauge
            android:id="@+id/gauge"
            xmlns:sc="http://schemas.android.com/apk/res-auto"
            android:layout_width="240dp"
            android:layout_height="150dp"
            android:padding="10dp"
            sc:scc_angle_start="-180"
            sc:scc_angle_sweep="135"
            sc:scc_stroke_size="2dp"
            sc:scc_stroke_colors="white|white|white|white|white|white|red|red"
            sc:scc_stroke_colors_mode="solid"
            sc:scc_progress_size="15dp"
            sc:scc_notches="8"
            sc:scc_notches_size="2dp"
            sc:scc_notches_color="#ffffff"
            sc:scc_notches_position="inside"
            sc:scc_text_tokens="0|40|80|120|160|200|240|280"
            sc:scc_text_color="#ffffff"
            sc:scc_text_size="12dp"
            sc:scc_text_position="inside"
            sc:scc_text_unbend="true"
          />

        <LinearLayout
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:gravity="right"
            android:layout_gravity="bottom|right"
            android:layout_marginRight="10dp"
            android:padding="10dp">

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:id="@+id/counter"
                android:textColor="#ffffff"
                android:textSize="44dp"
                android:textStyle="bold"
                android:text="0"
                android:gravity="center"
                />

            <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:textColor="#ffffff"
            android:textSize="20dp"
            android:text="km/h"
            android:gravity="center"
            />

        </LinearLayout>

    </FrameLayout>
```

```java
    // Find the components
    final ScArcGauge gauge = (ScArcGauge) this.findViewById(R.id.gauge);
    assert gauge != null;

    final TextView counter = (TextView) this.findViewById(R.id.counter);
    assert counter != null;

    // Bring on top
    gauge.bringOnTop(ScGauge.BASE_IDENTIFIER);
    gauge.bringOnTop(ScGauge.NOTCHES_IDENTIFIER);

    // Set the value
    gauge.setHighValue(180, 0, 320);

    // Each time I will change the value I must write it inside the counter text.
    gauge.setOnEventListener(new ScGauge.OnEventListener() {
        @Override
        public void onValueChange(float lowValue, float highValue) {
            // Write the value
            int value = (int) ScGauge.percentageToValue(highValue, 0, 320);
            counter.setText(Integer.toString(value));
        }
    });

    // Before draw
    gauge.setOnDrawListener(new ScGauge.OnDrawListener() {
        @Override
        public void onBeforeDrawCopy(ScCopier.CopyInfo info) {
            // Check for the progress
            if (info.source.getTag() == ScGauge.PROGRESS_IDENTIFIER) {
                // Scale
                info.scale = new PointF(0.95f, 0.95f);
                info.offset = new PointF(14.0f, 14.0f);
            }
        }

        @Override
        public void onBeforeDrawNotch(ScNotches.NotchInfo info) {
            // Set the length of the notch
            info.length = info.index == 0 || info.index == info.source.getCount() ?
                    gauge.dipToPixel(15) : gauge.dipToPixel(5);
            info.color = info.index > 6 ? Color.RED : Color.WHITE;
        }

        @Override
        public void onBeforeDrawPointer(ScPointer.PointerInfo info) {
            // Do nothing
        }

        @Override
        public void onBeforeDrawToken(ScWriter.TokenInfo info) {
            // Get the text bounds
            Rect bounds = new Rect();
            info.source.getPainter().getTextBounds(info.text, 0, info.text.length(), bounds);

            // Hide the first
            info.visible = info.index != 0;

            // Reset the angle and the offset
            info.angle = 0.0f;
            info.offset.y = bounds.height() / 2 + 5;
            info.offset.x = -bounds.width() * ((float) info.index / (float) info.source.getTokens().length);
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