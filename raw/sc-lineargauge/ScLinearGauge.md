# ScLinearGauge
This class is a specialized to create a linear gauge.

The line coordinates start (left, top) and end (right, bottom) is always intended as percentage of the container. 
By default start is (0%, 0%) and end (100%, 100%).
Values major of 0% will be normalized to 0% and the same for value major of 100% will be normalized to 100%.

Please consider that one horizontal line in a path have height equal to 0, same for the vertical.
So if you using the `wrap_content` layout mode you will see the line only playing with the padding.

This class extend the [ScGauge](../sc-gauge/ScGauge.md) class.<br />
This class inherit all its properties from the [ScGauge](../sc-feature/ScGauge.md) so please take a look to the related documentation.
<br />
<br />

#### Getter and Setter

- **get/setLeftBounds**  -> `float` value, default `0`<br />
The left bounds in percentage.

- **get/setTopBounds**  -> `float` value, default `100`<br />
The top bounds in percentage.

- **get/setRightBounds**  -> `float` value, default `0`<br />
The right bounds in percentage.

- **get/setBottomBounds**  -> `float` value, default `100`<br />
The bottom bounds in percentage.
<br />
<br />

---
###### XML using
```xml
    <com.sccomponents.widgets.ScLinearGauge
        android:layout_width="200dp"
        android:layout_height="wrap_content"
        android:padding="10dp"
    />
```


###### XML Properties
```xml
    <declare-styleable name="ScComponents">
        ...
        <attr name="scc_left" format="float" />
        <attr name="scc_top" format="float" />
        <attr name="scc_right" format="float" />
        <attr name="scc_bottom" format="float" />
    </declare-styleable>
```


---
###### Let's play

- **Basic**
<img src="https://github.com/Paroca72/sc-widgets/blob/master/raw/sc-lineargauge/1.jpg" align="right" />

```xml
    <com.sccomponents.widgets.ScLinearGauge
        android:layout_width="300dp"
        android:layout_height="300dp"
        android:padding="10dp"
        android:background="#f5f5f5"/>
```

- **All feature in basic mode**
<img src="https://github.com/Paroca72/sc-widgets/blob/master/raw/sc-lineargauge/2.jpg" align="right" />

```xml
    <com.sccomponents.widgets.ScLinearGauge
        android:layout_width="300dp"
        android:layout_height="300dp"
        android:padding="30dp"
        android:background="#f5f5f5"
        sc:scc_stroke_size="6dp"
        sc:scc_progress_size="4dp"
        sc:scc_value="45"
        sc:scc_notches="8"
        sc:scc_notches_length="10dp"
        sc:scc_text_tokens="01|02|03|04|05|06|07|08"
        sc:scc_pointer_radius="10dp" />
```
<br />
<br />

---
## Examples

Press on the picture linked below to see the demonstration.

[![image](https://github.com/Paroca72/sc-widgets/blob/master/raw/sc-lineargauge/f-01.jpg)](flat.md)
[![image](https://github.com/Paroca72/sc-widgets/blob/master/raw/sc-lineargauge/f-02.jpg)](flat.md)
[![image](https://github.com/Paroca72/sc-widgets/blob/master/raw/sc-lineargauge/n-01.jpg)](flat.md)
[![image](https://github.com/Paroca72/sc-widgets/blob/master/raw/sc-lineargauge/n-02.jpg)](flat.md)
[![image](https://github.com/Paroca72/sc-widgets/blob/master/raw/sc-lineargauge/n-03.jpg)](flat.md)

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