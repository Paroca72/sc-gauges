# sc-gauges
This is a library of gauges.<br />
The 2.x version change completely the way to draw using the [ScDrawer](..\sc-drawer\ScDrawer.md) as base for create the [ScGauge](..\sc-gauge\ScGauge.md) and all classes inherited from it.
This using a path to follow and applying some features to draw extra on the path.
This way to think leaves a lot of freedom to the users to create particular components limited only by his imagination. 

> **IMPORTANT**<br />
> The 2.x version it is NOT compatible with the previous versions.
<br />

- **[ScArcGauge](raw/sc-arcgauge/ScArcGauge.md)**<br />
This class is a specialized to create an arc gauge.<br />
This class extend the [ScGauge](raw/sc-gauge/ScGauge.md) class.

- **[ScLinearGauge](raw/sc-lineargauge/ScLinearGauge.md)**<br />
This class is a specialized to create a linear gauge.<br />
This class extend the [ScGauge](raw/sc-gauge/ScGauge.md) class.

- **[ScBase](raw/sc-widget/ScBase.md)**<br />
The base class for all gauge components of this series.<br />
Contains just utility methods for facilitate the building of a component.

- **[ScGauge](raw/sc-gauge/ScGauge.md)**<br />
Manage a generic gauge.<br />
This class is studied to be an "helper class" to facilitate the user to create a gauge.
This class extend the [ScDrawer](raw/sc-drawer/ScDrawer.md) class.

- **[ScCopier](raw/sc-copier/ScCopier.md)**<br />
You can define the line characteristic by setting the inner painter.<br />
This class inherit all its properties from the [ScFeature](raw/sc-feature/ScFeature.md).

- **[ScDrawer](raw/sc-drawer/ScDrawer.md)**<br />
This is a small class to design the future components using the "path following" way.<br />
The duty of this class is divided in two main: define settings where draw the path and provide the possibility to add some "features" for drawing it.
Whereas the "[features](raw/sc-feature/ScFeature.md)" are independent from this class but are necessary to draw the path on the canvas.

- **[ScFeature](raw/sc-feature/ScFeature.md)**<br />
Create a feature to draw on a given path.<br />
The feature is independent and can be used with any path.

- **[ScNotches](raw/sc-notches/ScNotches.md)**<br />
Create a feature that draw a series of notches following the base path.<br />
This class inherit all its properties from the [ScFeature](raw/sc-feature/ScFeature.md).

- **[ScPointer](raw/sc-pointer/ScPointer.md)**<br />
Create a feature that draw a pointer on the given path.<br />
This class inherit all its properties from the [ScFeature](raw/sc-feature/ScFeature.md).

- **[ScWriter](raw/sc-writer/ScWriter.md)**<br />
Create a feature that draw a series of texts on the given path.<br />
This class inherit all its properties from the [ScFeature](raw/sc-feature/ScFeature.md).

- **[ScPathMeasure](raw/sc-pathmeasure/ScPathMeasure.md)**<br />
Extend the PathMeasure because the original class not consider the contours in its totality.

<br />
<br />


# Some examples from **[ScArcGauge](raw/sc-arcgauge/ScArcGauge.md)**

<img src="https://github.com/Paroca72/sc-gauges/blob/master/raw/sc-arcgauge/f-01.jpg" height="120px" /> <img src="https://github.com/Paroca72/sc-gauges/blob/master/raw/sc-arcgauge/f-02.jpg" height="120px" /> <img src="https://github.com/Paroca72/sc-gauges/blob/master/raw/sc-arcgauge/f-03.jpg" height="120px" /> <img src="https://github.com/Paroca72/sc-gauges/blob/master/raw/sc-arcgauge/f-04.jpg" height="120px" />
<br />
<img src="https://github.com/Paroca72/sc-gauges/blob/master/raw/sc-arcgauge/i-01.jpg" height="120px" /> <img src="https://github.com/Paroca72/sc-gauges/blob/master/raw/sc-arcgauge/i-02.jpg" height="120px" /> <img src="https://github.com/Paroca72/sc-gauges/blob/master/raw/sc-arcgauge/i-03.jpg" height="120px" />
<br />
<img src="https://github.com/Paroca72/sc-gauges/blob/master/raw/sc-arcgauge/i-04.jpg" height="120px" /> <img src="https://github.com/Paroca72/sc-gauges/blob/master/raw/sc-arcgauge/i-05.jpg" height="120px" /> <img src="https://github.com/Paroca72/sc-gauges/blob/master/raw/sc-arcgauge/n-01.jpg" height="120px" />
<br />
<img src="https://github.com/Paroca72/sc-gauges/blob/master/raw/sc-arcgauge/n-02single.jpg" height="120px" /> <img src="https://github.com/Paroca72/sc-gauges/blob/master/raw/sc-arcgauge/n-03.jpg" height="120px" /> <img src="https://github.com/Paroca72/sc-gauges/blob/master/raw/sc-arcgauge/n-04.jpg" height="120px" /> <img src="https://github.com/Paroca72/sc-gauges/blob/master/raw/sc-arcgauge/n-05.jpg" height="120px" />
<br />
<br />

# Some examples from **[ScLinearGauge](raw/sc-lineargauge/ScLinearGauge.md)**

<img src="https://github.com/Paroca72/sc-gauges/blob/master/raw/sc-lineargauge/f-01.jpg" width="300px" /> <img src="https://github.com/Paroca72/sc-gauges/blob/master/raw/sc-lineargauge/f-02.jpg" width="300px" />
<br />
<img src="https://github.com/Paroca72/sc-gauges/blob/master/raw/sc-lineargauge/n-01.jpg" height="200px" /> <img src="https://github.com/Paroca72/sc-gauges/blob/master/raw/sc-lineargauge/n-02.jpg" height="200px" /> <img src="https://github.com/Paroca72/sc-gauges/blob/master/raw/sc-lineargauge/n-03.jpg" height="200px" />
<br />
<br />


# The idea

The idea was to create a base (solid) class that help the user to design more quickly every type of gauges.
Now the base is, still raw and improvable, for this every **fork** or help is welcome.

I would like to have some help to produce some examples of vary design of gauges, using the existing ScArcGauge and ScLinearGauge, or creating new classes specialized.
If you start to use this framework you will note that the possibility is very infinite and can be funny to create and publishing new gauge design.
If we can increase the number of the example the final users could be use it without the effort to create it from zero and customizing the example by the case.

**Every suggestions are welcome.**
<br />
<br />


# Usage

via Gradle:
<br />
Add it in your root build.gradle at the end of repositories:
```java
allprojects {
	repositories {
		...
		maven { url "https://jitpack.io" }
	}
}
```

Add the dependency
```java
dependencies {
    ...
    compile 'com.github.paroca72:sc-gauges:2.5.1'
}
```
<br />
<br />


#License
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
