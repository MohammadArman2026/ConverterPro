cmake_minimum_required(VERSION 3.22.1)

project("converterpro")

add_library(native-lib SHARED
        native-lib.cpp)

# This CMake file lives in app/src/main/cpp; FFmpeg headers and shared objects
# are siblings of the cpp directory under app/src/main.
set(FFMPEG_DIR ${CMAKE_CURRENT_LIST_DIR}/..)

include_directories(
        ${FFMPEG_DIR}/cpp/include
)

add_library(avcodec SHARED IMPORTED)
set_target_properties(avcodec PROPERTIES
        IMPORTED_LOCATION
        ${FFMPEG_DIR}/jniLibs/${ANDROID_ABI}/libavcodec.so)

add_library(avformat SHARED IMPORTED)
set_target_properties(avformat PROPERTIES
        IMPORTED_LOCATION
        ${FFMPEG_DIR}/jniLibs/${ANDROID_ABI}/libavformat.so)

add_library(avutil SHARED IMPORTED)
set_target_properties(avutil PROPERTIES
        IMPORTED_LOCATION
        ${FFMPEG_DIR}/jniLibs/${ANDROID_ABI}/libavutil.so)

add_library(swresample SHARED IMPORTED)
set_target_properties(swresample PROPERTIES
        IMPORTED_LOCATION
        ${FFMPEG_DIR}/jniLibs/${ANDROID_ABI}/libswresample.so)

add_library(swscale SHARED IMPORTED)
set_target_properties(swscale PROPERTIES
        IMPORTED_LOCATION
        ${FFMPEG_DIR}/jniLibs/${ANDROID_ABI}/libswscale.so)

add_library(avfilter SHARED IMPORTED)
set_target_properties(avfilter PROPERTIES
        IMPORTED_LOCATION
        ${FFMPEG_DIR}/jniLibs/${ANDROID_ABI}/libavfilter.so)

add_library(avdevice SHARED IMPORTED)
set_target_properties(avdevice PROPERTIES
        IMPORTED_LOCATION
        ${FFMPEG_DIR}/jniLibs/${ANDROID_ABI}/libavdevice.so)

target_link_libraries(
        native-lib
        avcodec
        avformat
        avutil
        avfilter
        avdevice
        swresample
        swscale
        log
)
