# ScDrawer
This is a small class to design the future components using the "path following" way.

The duty of this class is divided in two main: define settings where draw the path and provide the possibility to add some "features" for drawing it.
Whereas the "[features](..\sc-feature\ScFeature.md)" are independent from this class but are necessary to draw the path on the canvas.

There are two ways to settle the drawing area:
- **DRAW**: will simply draw the path on the component canvas using the proper methods.
- **STRETCH**: before to draw the path stretch the canvas.

Note that the stretch methods will stretch also the stroke creating a singular effect.
Always you can decide witch dimensions to fill: none, both dimensions, vertical or horizontal.
This for give to the user many possibilities to render the path on the drawing area.

For better understand this important mode to use the settings will propose **some examples** at the end of this document.
<br />
<br />

## ScDrawer class details
This class extend the [ScWidget](..\sc-widget\ScWidget.md) class.<br />
This is an abstract class and cannot be instantiate directly but only inherited from another class.
Note that this class no have path properties exposed to modify it directly but you need to override the `createPath()` method.
If you decide to expose some property for manage the path you can use the **protected** property named `mPath`.

> **NOTE**
> In this version the class not implement any methods to auto-size the path within the drawing area.<br />
> For example the area calculation not consider the width of the stroke so if the stroke size is noticeable and the component padding zero could be a clipping drawing.
> This can be simply solved playing with the component padding.
<br />
<br />

#### Public methods

- **void addFeature(ScFeature feature)**<br />
**ScFeature addFeature(Class<?> classRef)**<br />
Add one feature to this drawer.<br />
The second overload instantiate a new object from the class reference passed.
The passed class reference must implement the ScFeature interface.

- **boolean removeFeature(ScFeature feature)**<br />
Remove a feature from this drawer.

- **void removeAllFeatures()**<br />
Remove all feature from this drawer.

- **List<ScFeature> findFeatures(Class<?> classRef, String tag)**<br />
Find all features that corresponds to a class and tag reference.<br />
If the class reference is null the class will be not consider. 
Same behavior for the tag param.

- **ScFeature findFeature(String tag)**<br />
**ScFeature findFeature(Class<?> classRef)**<br />
Find the feature searching by tag or the class reference.<br />
If found something return the first element found.
If the param is null return the first feature found avoid the comparison check.

- **void bringOnTop(String tag)**<br />
**void bringOnTop(Class<?> classRef)**<br />
**void bringOnTop(ScFeature feature)**<br />
Find all feature that are tagged or inherit from class param and move they at the end of the list so will draw for last (on top).

- **Paint getPainter()**<br />
Get the arc painter.

- **void setOnPathTouchListener(OnPathTouchListener listener)**<br />
Link the listener.

- **boolean isPressed()**<br />
Return true is the path is pressed.
<br />
<br />

#### Getter and Setter

- **get/setMaxWidth**  -> `int` value, default `Int.MAX_VALUE`<br />
The value must be passed in pixel.

- **get/setMaxHeight**  -> `int` value, default `Int.MAX_VALUE`<br />
The value must be passed in pixel.

- **get/setFillingArea**  -> `FillingArea` value, default `FillingArea.BOTH`<br />
Possibly values by enum: `NONE`, `BOTH`, `HORIZONTAL`, `VERTICAL`<br />
This indicate what kind of dimension will filled.

- **get/setFillingMode**  -> `FillingMode` value, default `FillingMode.DRAW`<br />
Possibly values by enum: `DRAW`, `STRETCH`<br />
Please look above for a short explain of this feature.

- **get/setRecognizePathTouch**  -> `boolean` value, default `false`<br />
Define if the input is enabled.<br />
When enable and the user touch on the path will throw an event with the point details of pressure.

- **get/setPathTouchThreshold** -> `float` value, default `0`<br />
The recognize threshold for find the point on path.
<br />
<br />

#### Interfaces

**OnEventListener**
- **void onTouch(float distance)**<br />
Called when the path is touched.
- **void onRelease()**<br />
Called when the path is released.
- **void onSlide(float distance)**<br />
Called when the user move the pressure on the screen.<br />
This called only if before had a onTouch event.
<br />
<br />

---
###### XML Properties
```xml
    <declare-styleable name="ScComponents">
        <attr name="scc_max_width" format="dimension" />
        <attr name="scc_max_height" format="dimension" />
        <attr name="scc_fill_area" format="enum" />
        <attr name="scc_fill_mode" format="enum" />
        <attr name="scc_input_enabled" format="boolean" />
    </declare-styleable>
```
<br />
<br />

###### Understanding the canvas and area filling

![image](https://github.com/Paroca72/sc-widgets/blob/master/raw/sc-drawer/1.jpg)
![image](https://github.com/Paroca72/sc-widgets/blob/master/raw/sc-drawer/2.jpg)

![image](https://github.com/Paroca72/sc-widgets/blob/master/raw/sc-drawer/3.jpg)
![image](https://github.com/Paroca72/sc-widgets/blob/master/raw/sc-drawer/4.jpg)
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