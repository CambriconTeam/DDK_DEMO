# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
             native-lib

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             src/main/cpp/native-lib.cpp )

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
              log-lib
              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.
#huangyaling add 2018.01.31 begin
add_library(
    caffe_jni
    SHARED
    IMPORTED
    )
set_target_properties(
    caffe_jni
    PROPERTIES IMPORTED_LOCATION
    ${CMAKE_CURRENT_LIST_DIR}/src/main/jniLibs/${ANDROID_ABI}/libcaffe_jni.so
    )
add_library(
    detecte_jni
    SHARED
    IMPORTED
    )
set_target_properties(
    detecte_jni
    PROPERTIES IMPORTED_LOCATION
    ${CMAKE_CURRENT_LIST_DIR}/src/main/jniLibs/${ANDROID_ABI}/libdetecte_jni.so
    )
add_library(
    offclassify_jni
    SHARED
    IMPORTED
    )
set_target_properties(
    offclassify_jni
    PROPERTIES IMPORTED_LOCATION
    ${CMAKE_CURRENT_LIST_DIR}/src/main/jniLibs/${ANDROID_ABI}/liboffclassify_jni.so
    )

add_library(
    hiaiddk
    SHARED
    IMPORTED
    )
set_target_properties(
    hiaiddk
    PROPERTIES IMPORTED_LOCATION
    ${CMAKE_CURRENT_LIST_DIR}/src/main/jniLibs/${ANDROID_ABI}/libhiaiddk.so
    )

add_library(
    hiaidetecte
    SHARED
    IMPORTED
    )
set_target_properties(
    hiaidetecte
    PROPERTIES IMPORTED_LOCATION
    ${CMAKE_CURRENT_LIST_DIR}/src/main/jniLibs/${ANDROID_ABI}/libhiaidetecte.so
    )
#huangyaling add 2018.01.31 end

add_library(
    face_jni
    SHARED
    IMPORTED
    )
set_target_properties(
    face_jni
    PROPERTIES IMPORTED_LOCATION
    ${CMAKE_CURRENT_LIST_DIR}/src/main/jniLibs/${ANDROID_ABI}/libface_jni.so
    )
#add by Chengyu Yang 2018.03.12

add_library(
    single_jni
    SHARED
    IMPORTED
    )
set_target_properties(
    single_jni
    PROPERTIES IMPORTED_LOCATION
    ${CMAKE_CURRENT_LIST_DIR}/src/main/jniLibs/${ANDROID_ABI}/libsingle_jni.so
    )
#add by Chengyu Yang 2018.04.23

target_link_libraries( # Specifies the target library.
                       native-lib
                       hiaiddk
                       hiaidetecte
                       caffe_jni
                       detecte_jni
                       face_jni
                       single_jni
                       offclassify_jni
                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )