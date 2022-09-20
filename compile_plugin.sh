#!/bin/bash

if [ -f lib_plugin/build ]; then
    rm -rf lib_plugin/build
fi

./gradlew assemble -p lib_plugin

find lib_plugin/build | grep \\.apk
find lib_plugin/build | grep \\.aar

cp lib_plugin/build/outputs/apk/debug/lib_plugin-debug.apk MainApp/src/main/assets/plugin.apk