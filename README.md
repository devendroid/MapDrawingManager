# Map Drawing Manager
[![](https://jitpack.io/v/devendroid/MapDrawingManager.svg)](https://jitpack.io/#devendroid/MapDrawingManager)
[![Android Arsenal]( https://img.shields.io/badge/Android%20Arsenal-Map%20Drawing%20Manager-green.svg?style=flat )]( https://android-arsenal.com/details/1/7980 )

MDM is a library with the help of we can draw many different shapes like polygon, polyline and many more on the google map with editable mode.

## Key Features:
- Draw any shape by touch on map
- Resize any shape by drag
- Remove any individual or all shapes from map
- Auto calculate the size of shapes drawn on map
- Customize shapes properties like color, stroke etc
- Easy callbacks for shapes draw, update and remove

## Sample app using MDM:
![MapDrawingManager](/assets/mdm1.0.0.gif)

## Dependency
- Add it in your root build.gradle at the end of repositories
```gradle
   allprojects {
       repositories {
    	...
    	maven { url 'https://jitpack.io' }
    	}
    }
```
- Add the dependency
```gradle
    dependencies {
         implementation 'com.github.devendroid:MapDrawingManager:1.0.0'
     }

```

## Usage
```java

// Simple Initialization
supportMapFragment.getMapAsync { googleMap ->
     val mapDrawingManager = MDMBuilder(baseContext).withMap(googleMap).build()
     mapDrawingManager?.removeListener = this //OnShapeRemoveListener
     mapDrawingManager?.drawListener = this //OnShapeDrawListener
     mapDrawingManager?.shapeType = ShapeType.POLYGON
 }

```


## License
```
Copyright 2018 Deven Singh

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
