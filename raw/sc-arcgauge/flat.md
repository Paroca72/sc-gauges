# ScArcGauge examples
Following some example of the [ScArcGauge](../sc-arcgauge/ScArcGauge.md) application.

Is simple to understand that inheriting from the [ScGauge](../sc-gauge/ScGauge.md) class the possibilities are infinite.
So this example as been only a demonstration of the most used configurations.

> **ATTENTION**<br />
> Please keep in mind that you can enable the user input (`setInputEnabled`) to allow the user to drive the gauge values.<br />
> Also you can use the animator (`getHighValueAnimator` or `getLowValueAnimator`) to animate the value changing.

<br />
<br />

---
## Example 1


<img align="right" src="https://github.com/Paroca72/sc-widgets/blob/master/raw/sc-arcgauge/f-01.jpg">

```xml
    <FrameLayout
        android:layout_width="200dp"
        android:layout_height="wrap_content"
        android:background="#f5f5f5" >

        <com.sccomponents.widgets.ScArcGauge
            xmlns:sc="http://schemas.android.com/apk/res-auto"
            android:id="@+id/gauge"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            sc:scc_angle_start="135"
            sc:scc_angle_sweep="270"
            sc:scc_progress_color="#ffff00ff"
            sc:scc_progress_size="10dp"
            sc:scc_stroke_color="#009688"
            sc:scc_stroke_size="10dp"
            android:paddingBottom="20dp"
            android:paddingLeft="10dp"
            android:paddingTop="10dp"
            android:paddingRight="10dp"/>

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="center"
            android:text="Percentage"
            android:textColor="#009688"
            android:textSize="24dp"/>

        <TextView
            android:id="@+id/counter"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="bottom|center_horizontal"
            android:text="0%"
            android:textColor="#009688"
            android:textSize="38dp"/>

    </FrameLayout>
```

```java
    // Find the components
    final TextView counter = (TextView) MainActivity.this.findViewById(R.id.counter);
    ScArcGauge gauge = (ScArcGauge) this.findViewById(R.id.gauge);

    // Set the features stroke cap style to rounded
    gauge.findFeature(ScArcGauge.BASE_IDENTIFIER)
            .getPainter().setStrokeCap(Paint.Cap.ROUND);
    gauge.findFeature(ScArcGauge.PROGRESS_IDENTIFIER)
            .getPainter().setStrokeCap(Paint.Cap.ROUND);

    // If you set the value from the xml that not produce an event so I will change the
    // value from code.
    gauge.setHighValue(60);

    // Each time I will change the value I must write it inside the counter text.
    gauge.setOnEventListener(new ScGauge.OnEventListener() {
        @Override
        public void onValueChange(float lowValue, float highValue) {
            counter.setText((int) highValue + "%");
        }
    });
```


---
## Example 2

<img align="right" src="https://github.com/Paroca72/sc-widgets/blob/master/raw/sc-arcgauge/f-02.jpg">

```xml
    <FrameLayout
        android:layout_width="200dp"
        android:layout_height="wrap_content"
        android:background="#f5f5f5" >

        <com.sccomponents.widgets.ScArcGauge
            xmlns:sc="http://schemas.android.com/apk/res-auto"
            android:id="@+id/gauge"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:padding="10dp"
            sc:scc_angle_start="-90"
            sc:scc_progress_color="#288636"
            sc:scc_progress_size="10dp"
            sc:scc_stroke_color="#585258"
            sc:scc_stroke_size="2dp" />

        <TextView
            android:id="@+id/counter"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="center"
            android:text="0%"
            android:textColor="#288636"
            android:textSize="42dp"/>

    </FrameLayout>
```

```java
    // Find the components
    final TextView counter = (TextView) MainActivity.this.findViewById(R.id.counter);
    ScArcGauge gauge = (ScArcGauge) this.findViewById(R.id.gauge);

    // If you set the value from the xml that not produce an event so I will change the
    // value from code.
    gauge.setHighValue(60);

    // Each time I will change the value I must write it inside the counter text.
    gauge.setOnEventListener(new ScGauge.OnEventListener() {
        @Override
        public void onValueChange(float lowValue, float highValue) {
            counter.setText((int) highValue + "%");
        }
    });
```


---
## Example 3

<img align="right" src="https://github.com/Paroca72/sc-widgets/blob/master/raw/sc-arcgauge/f-03.jpg">

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
            sc:scc_angle_start="-90"
            sc:scc_stroke_size="10dp"
            sc:scc_stroke_color="#e9e6e6"
            sc:scc_pointer_radius="5dp"
            sc:scc_pointer_color="#a4abaa"
            sc:scc_progress_size="20dp"
            sc:scc_progress_color="#a4abaa"
            sc:scc_value="30"/>

    </FrameLayout>
```

```java
    // Find the components
    final ScArcGauge gauge = (ScArcGauge) this.findViewById(R.id.gauge);
    assert gauge != null;

    // Low pointer visibility
    gauge.setPointerLowVisibility(true);

    // Clear the pointers halo
    gauge.setPointerHaloWidth(0.0f);
```


---
## Example 4
Note that the `scc_path_touchable` is enable so you can drag the pointers.

<img align="right" src="https://github.com/Paroca72/sc-widgets/blob/master/raw/sc-arcgauge/f-04.jpg">

```
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
            sc:scc_angle_start="-90"
            sc:scc_stroke_size="4dp"
            sc:scc_stroke_color="#e9e6e6"
            sc:scc_pointer_radius="10dp"
            sc:scc_pointer_color="#a4abaa"
            sc:scc_progress_size="4dp"
            sc:scc_progress_color="#a4abaa"
            sc:scc_path_touchable="true"
            sc:scc_notches="1"
            sc:scc_notches_size="4dp"
            sc:scc_notches_length="10dp"
            sc:scc_notches_color="#e9e6e6"/>

        <LinearLayout
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:layout_gravity="center"
            android:gravity="center">

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="LOW"
                android:textColor="#dddada"/>

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:textColor="#a4abaa"
                android:id="@+id/lowValue"
                android:textSize="20dp"/>

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="HIGHT"
                android:textColor="#dddada"/>

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:textColor="#a4abaa"
                android:id="@+id/highValue"
                android:textSize="20dp"/>

        </LinearLayout>

    </FrameLayout>
```

```java
    // Find the components
    final ScArcGauge gauge = (ScArcGauge) this.findViewById(R.id.gauge);
    assert gauge != null;

    final TextView txtLowValue = (TextView) this.findViewById(R.id.lowValue);
    assert txtLowValue != null;

    final TextView txtHighValue = (TextView) this.findViewById(R.id.highValue);
    assert txtHighValue != null;

    // Clear the pointers halo and make visible the low one
    gauge.setPointerHaloWidth(0.0f);
    gauge.setPointerLowVisibility(true);

    // Set the values.
    // Note that the low cannot be over the high so you must set always for second because
    // the initial high is, by default, equal to 0.
    gauge.setHighValue(59);
    gauge.setLowValue(12);

    // Each time I will change the value I must write it inside the counter text.
    gauge.setOnEventListener(new ScGauge.OnEventListener() {
        @Override
        public void onValueChange(float lowValue, float highValue) {
            txtLowValue.setText((int) lowValue + "°");
            txtHighValue.setText((int) highValue + "°");
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