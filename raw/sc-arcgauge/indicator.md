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
You can download the indicator image used below from [**HERE**](indicator-01.png).

<img align="right" src="https://github.com/Paroca72/sc-widgets/blob/master/raw/sc-arcgauge/i-01.jpg">

```xml
    <FrameLayout
        android:layout_width="260dp"
        android:layout_height="150dp"
        android:background="#cccccc">

        <com.sccomponents.widgets.ScArcGauge
            xmlns:sc="http://schemas.android.com/apk/res-auto"
            android:layout_width="220dp"
            android:layout_height="wrap_content"
            android:id="@+id/gauge"
            sc:scc_angle_start="-180"
            sc:scc_angle_sweep="180"
            sc:scc_progress_color="#ff35c44a"
            sc:scc_progress_size="50dp"
            sc:scc_stroke_color="#f5f5f5"
            sc:scc_stroke_size="50dp"
            android:layout_gravity="center_horizontal"
            android:padding="25dp"
            android:layout_marginTop="20dp"/>

        <ImageView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="bottom|center_horizontal"
            android:id="@+id/indicator"
            android:src="@drawable/indicator"
            android:layout_marginLeft="50dp"
            android:layout_marginBottom="5dp"/>

    </FrameLayout>
```

```java
    // Find the components
    final ScArcGauge gauge = (ScArcGauge) this.findViewById(R.id.gauge);

    // Get the indicator image and set the center pivot for a right rotation
    final ImageView indicator = (ImageView) this.findViewById(R.id.indicator);
    indicator.setPivotX(35f);
    indicator.setPivotY(35f);

    // If you set the value from the xml that not produce an event so I will change the
    // value from code.
    gauge.setHighValue(60);

    // Each time I will change the value I must write it inside the counter text.
    gauge.setOnEventListener(new ScGauge.OnEventListener() {
        @Override
        public void onValueChange(float lowValue, float highValue) {
            // Convert the percentage value in an angle
            float angle = gauge.percentageToAngle(highValue);
            // Apply the angle to indicator
            indicator.setRotation(angle);
        }
    });
```
<br />
<br />

---
## Example 2
You can download the indicator image used below from [**HERE**](indicator-02.png).

<img align="right" src="https://github.com/Paroca72/sc-widgets/blob/master/raw/sc-arcgauge/i-02.jpg">

```xml
    <FrameLayout
        android:layout_width="260dp"
        android:layout_height="162dp"
        android:background="#354051">

        <com.sccomponents.widgets.ScArcGauge
            android:id="@+id/gauge"
            xmlns:sc="http://schemas.android.com/apk/res-auto"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_gravity="center_horizontal"
            android:padding="30dp"
            sc:scc_angle_start="-180"
            sc:scc_angle_sweep="180"
            sc:scc_progress_color="#00B4FF"
            sc:scc_progress_size="30dp"
            sc:scc_stroke_color="#ffffff"
            sc:scc_stroke_size="30dp"
            sc:scc_notches="3"
            sc:scc_notches_length="32dp"
            sc:scc_notches_size="6dp"
            sc:scc_notches_color="#354051"
            sc:scc_text_tokens="1000|2000|3000"
            sc:scc_text_align="left"
            sc:scc_text_position="middle"
            sc:scc_text_color="#354051"/>

        <ImageView
            android:id="@+id/indicator"
            android:layout_width="64dp"
            android:layout_height="wrap_content"
            android:layout_gravity="bottom|center_horizontal"
            android:src="@drawable/indicator"
            android:layout_marginLeft="18dp"
            android:layout_marginBottom="29dp"/>

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="0000"
            android:layout_marginBottom="2dp"
            android:id="@+id/counter"
            android:layout_gravity="bottom|center_horizontal"
            android:textColor="#ffffff"
            android:textSize="20dp"/>

    </FrameLayout>
```

```java
    // Find the components
    final ScArcGauge gauge = (ScArcGauge) this.findViewById(R.id.gauge);
    assert gauge != null;

    final ImageView indicator = (ImageView) this.findViewById(R.id.indicator);
    assert indicator != null;

    final TextView counter = (TextView) this.findViewById(R.id.counter);
    assert counter != null;

    // Set the center pivot for a right rotation
    indicator.setPivotX(30f);
    indicator.setPivotY(30f);

    // As the progress feature by default the last to be draw I must bring the notches feature
    // on top.
    gauge.bringOnTop(ScGauge.NOTCHES_IDENTIFIER);

    // If you set the value from the xml that not produce an event so I will change the
    // value from code.
    gauge.setHighValue(60);

    // Each time I will change the value I must write it inside the counter text.
    gauge.setOnEventListener(new ScGauge.OnEventListener() {
        @Override
        public void onValueChange(float lowValue, float highValue) {
            // Convert the percentage value in an angle
            float angle = gauge.percentageToAngle(highValue);
            indicator.setRotation(angle);

            // Write the value
            int value = (int) ScGauge.percentageToValue(highValue, 0.0f, 3000.0f);
            counter.setText(value + "");
        }
    });
```
<br />
<br />

---
## Example 3
You can download the indicator image used below from [**HERE**](indicator-02.png).

<img align="right" src="https://github.com/Paroca72/sc-widgets/blob/master/raw/sc-arcgauge/i-03.jpg">

```xml
    <FrameLayout
        android:layout_width="260dp"
        android:layout_height="162dp"
        android:background="#354051">

        <com.sccomponents.widgets.ScArcGauge
            android:id="@+id/gauge"
            xmlns:sc="http://schemas.android.com/apk/res-auto"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_gravity="center_horizontal"
            android:padding="30dp"
            sc:scc_angle_start="-180"
            sc:scc_angle_sweep="180"
            sc:scc_stroke_colors="#EC4949|#EC4949|#F7AD36|#F7AD36|#F7AD36|#F7AD36|#8BBE28"
            sc:scc_stroke_colors_mode="solid"
            sc:scc_stroke_size="30dp"/>

        <ImageView
            android:id="@+id/indicator"
            android:layout_width="64dp"
            android:layout_height="wrap_content"
            android:layout_gravity="bottom|center_horizontal"
            android:layout_marginBottom="29dp"
            android:layout_marginLeft="18dp"
            android:src="@drawable/indicator"/>

    </FrameLayout>
```

```java
    // Find the components
    final ScArcGauge gauge = (ScArcGauge) this.findViewById(R.id.gauge);
    assert gauge != null;

    final ImageView indicator = (ImageView) this.findViewById(R.id.indicator);
    assert indicator != null;

    // Set the center pivot for a right rotation
    indicator.setPivotX(30f);
    indicator.setPivotY(30f);

    // If you set the value from the xml that not produce an event so I will change the
    // value from code.
    gauge.setHighValue(60);

    // Each time I will change the value I must write it inside the counter text.
    gauge.setOnEventListener(new ScGauge.OnEventListener() {
        @Override
        public void onValueChange(float lowValue, float highValue) {
            // Convert the percentage value in an angle
            float angle = gauge.percentageToAngle(highValue);
            indicator.setRotation(angle);
        }
    });
```
<br />
<br />

---
## Example 4
You can download the indicator image used below from [**HERE**](indicator-04.png).

<img align="right" src="https://github.com/Paroca72/sc-widgets/blob/master/raw/sc-arcgauge/i-04.jpg">

```xml
    <FrameLayout
        android:layout_width="180dp"
        android:layout_height="180dp"
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
            sc:scc_stroke_size="40dp"/>

        <ImageView
            android:id="@+id/indicator"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="center"
            android:src="@drawable/indicator"
            />

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:id="@+id/counter"
            android:layout_gravity="center"
            android:textColor="#f5f5f5"
            android:textSize="40dp"
            android:textStyle="bold"
            android:text="0"
            android:layout_marginRight="2dp"/>

    </FrameLayout>
```

```java
    // Find the components
    final ScArcGauge gauge = (ScArcGauge) this.findViewById(R.id.gauge);
    assert gauge != null;

    final ImageView indicator = (ImageView) this.findViewById(R.id.indicator);
    assert indicator != null;

    final TextView counter = (TextView) this.findViewById(R.id.counter);
    assert counter != null;

    // If you set the value from the xml that not produce an event so I will change the
    // value from code.
    gauge.setHighValue(60);

    // Set the base colors feature
    gauge.setStrokeColors(new int[] {
            Color.parseColor("#97B329"), Color.parseColor("#A9CB2A"),
            Color.parseColor("#D4E935"), Color.parseColor("#F1DD31"),
            Color.parseColor("#FBCB2E"), Color.parseColor("#F3A328"),
            Color.parseColor("#F18C23"), Color.parseColor("#F3341E"),
            Color.parseColor("#F51319")}
    );
    gauge.setStrokeColorsMode(ScFeature.ColorsMode.SOLID);

    // Each time I will change the value I must write it inside the counter text.
    gauge.setOnEventListener(new ScGauge.OnEventListener() {
        @Override
        public void onValueChange(float lowValue, float highValue) {
            // Convert the percentage value in an angle
            float angle = gauge.percentageToAngle(highValue);
            indicator.setRotation(angle);

            // Write the value
            counter.setText((int) highValue + "");
        }
    });
```
<br />
<br />

---
## Example 5
This is another way to proceed.<br />
You can download the indicator image used below from [**HERE**](indicator-05.png).

<img align="right" src="https://github.com/Paroca72/sc-widgets/blob/master/raw/sc-arcgauge/i-05.jpg">

```xml
    <FrameLayout
        android:layout_width="230dp"
        android:layout_height="wrap_content"
        android:background="#f5f5f5">

        <com.sccomponents.widgets.ScArcGauge
            android:id="@+id/gauge"
            xmlns:sc="http://schemas.android.com/apk/res-auto"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_gravity="center_horizontal"
            android:padding="30dp"
            sc:scc_angle_start="180"
            sc:scc_angle_sweep="180"
            sc:scc_stroke_size="50dp"/>

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:id="@+id/counter"
            android:layout_gravity="bottom|center"
            android:textColor="#f5f5f5"
            android:textSize="40dp"
            android:textStyle="bold"
            android:text="0"
            android:layout_marginLeft="5dp"/>

    </FrameLayout>
```

```java
    // Find the components
    final ScArcGauge gauge = (ScArcGauge) this.findViewById(R.id.gauge);
    assert gauge != null;

    final TextView counter = (TextView) this.findViewById(R.id.counter);
    assert counter != null;

    // Create a drawable
    final Bitmap indicator = BitmapFactory.decodeResource(this.getResources(), R.drawable.indicator);

    // If you set the value from the xml that not produce an event so I will change the
    // value from code.
    gauge.setHighValue(30);

    // Get the base feature
    gauge.setStrokeColors(
            Color.parseColor("#079900"), Color.parseColor("#079900"),
            Color.parseColor("#F0F501"), Color.parseColor("#F0F501"),
            Color.parseColor("#F6C713"), Color.parseColor("#F6C713"),
            Color.parseColor("#F36300"), Color.parseColor("#F36300"),
            Color.parseColor("#BD0000"), Color.parseColor("#BD0000")
    );

    // Each time I will change the value I must write it inside the counter text.
    gauge.setOnEventListener(new ScGauge.OnEventListener() {
        @Override
        public void onValueChange(float lowValue, float highValue) {
            // Write the value
            counter.setTextColor(base.getGradientColor(highValue, 100));
            counter.setText((int) highValue + "°");
        }
    });

    gauge.setOnDrawListener(new ScGauge.OnDrawListener() {
        @Override
        public void onBeforeDrawCopy(ScCopier.CopyInfo info) {
            // Do nothing
        }

        @Override
        public void onBeforeDrawNotch(ScNotches.NotchInfo info) {
            // Do nothing
        }

        @Override
        public void onBeforeDrawPointer(ScPointer.PointerInfo info) {
            // Check if the pointer if the high pointer
            if (info.source.getTag() == ScGauge.HIGH_POINTER_IDENTIFIER) {
                // Adjust the offset
                info.offset.x = -indicator.getWidth() / 2;
                info.offset.y = -indicator.getHeight() / 2;
                // Assign the bitmap to the pointer info structure
                info.bitmap = indicator;
            }
        }

        @Override
        public void onBeforeDrawToken(ScWriter.TokenInfo info) {
            // Do nothing
        }
    });
}
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
