PokemonShowdown Android Client
===============

## Requirements

* AndroidStudio stable release (I'm using Android Studio 0.5.2)
* When you finish installing, go to SDK Manager and download all supporting stuffs. It doesn't hurt. 
* Android SDK
* Java JDK 

## Setup

In AndroidStudio, choose Open Project and select the correct directory. From there, go to File > Project Structures and specify the correct path to Android SDK and Java JDK

* Get Debug .apk file (for development)

`./gradlew assembleDebug`

* Get Release .apk file 

`./gradlew assembleRelease`

* Install Debug .apk onto devices/emulators

`./gradlew installDebug`

* Install and start PokemonShowdown with launcher activity:

`./gradlew appStart` (customized Gradle task)

* Launch a non-launcher activity (Install .apk first and make sure adb is in the environment variable)

`adb shell am start -n com.pokemonshowdown.app/.ActivityNameHere`
