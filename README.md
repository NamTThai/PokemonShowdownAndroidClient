PokemonShowdown
===============

## Requirements

* AndroidStudio stable version

## Setup

* Get Debug .apk file (for development)

`./gradlew assembleDebug`

* Get Release .apk file 

`./gradlew assembleRelease`

* Install Debug .apk onto devices/emulators

`./gradlew installDebug`

* Install and start PokemonShowdown with launcher activity:

`./gradlew appStart` (customized Gradle task)

* Launch a non-launcher activity (Install .apk fist and make sure adb is in the $PATH variable)

`adb shell am start -n com.pokemonshowdown.app/.ActivityNameHere`
