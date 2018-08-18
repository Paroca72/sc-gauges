# sc-gauges
This is a library of gauges.<br />

**For see the documentation please refer to the link below**
<br />
[DOCS](https://paroca72.github.io/sc-gauges)
<br />
<br />

> **IMPORTANT**<br />
> The 3.x version it is NOT compatible with the previous versions.
<br />
<br />

# Some implementation
[Gauges Reality](http://www.sccomponents.com/)
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
    compile 'com.github.paroca72:sc-gauges:3.1.0'
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
