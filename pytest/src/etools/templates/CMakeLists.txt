# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

set(CURRENT_DIR ${CMAKE_SOURCE_DIR})

include(../../config.cmake)

#设置生成的so动态库最后输出的路径
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${CURRENT_DIR}/libs/${ANDROID_ABI})

#这里指定一下咱的头文件目录，openssl头文件，以及咱封装的ssl_hash.h头文件
include_directories(${CURRENT_DIR}/src/main/jni/)

link_directories(${ROOT_PROJECT}/Component-AI/AlgoBase/BaseLib-FastCV/libs/${ANDROID_ABI}
        ${ROOT_PROJECT}/Component-AI/AlgoBase/BaseLib-Bridge/libs/${ANDROID_ABI}
        ${ROOT_PROJECT}/Component-AI/AlgoBase/commonAI/libs/${ANDROID_ABI})

#这里引用静态.a,
add_library(XYAIMImageRestore STATIC IMPORTED)

# 这里加载，并且找到相应的 libxxx.a
set_target_properties( # Specifies the target library.
        {algo-lib-name}

        # Specifies the parameter you want to define.
        PROPERTIES IMPORTED_LOCATION

        # Provides the path to the library you want to import.
        ${CURRENT_DIR}/src/main/jni/${ANDROID_ABI}/lib{algo-lib-name}.a)

# 获取路径下所有的.cpp/.c/.cc文件，并赋值给变量中
#aux_source_directory(src/main/jni/)

add_library( # Sets the name of the library.
        XY{module-name}

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        ${CURRENT_DIR}/src/main/jni/{lower-name}_jni.cpp)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

#最后一步连接，
target_link_libraries( # Specifies the target library.
        XY{module-name}

        # Links the target library to the log library
        # included in the NDK.
        XYFastCV
        XYAIBridge
        XYAlgoStruct
        {algo-lib-name}
        ${log-lib})
