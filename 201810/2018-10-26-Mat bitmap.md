# Mat bitmap

```cmake
# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_VERBOSE_MAKEFILE on)
set(OpenCV_DIR $ENV{OPENCV_HOME})
set(OpenCV_LIBS "${OpenCV_DIR}/sdk/native/libs")
include_directories(${OpenCV_DIR}/sdk/native/jni/include)

add_library(libopencv_java3 SHARED IMPORTED )
set_target_properties(libopencv_java3 PROPERTIES
                      IMPORTED_LOCATION "${OpenCV_LIBS}/${ANDROID_ABI}/libopencv_java3.so")

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
             depth_map_transformation

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             src/main/cpp/depth_map_transformation.cpp )
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

target_link_libraries( # Specifies the target library.
                       depth_map_transformation android log libopencv_java3

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )
```





Bitmap to mat java  ( 仅仅当做参考，下面代码没有跑通)

```c++
//
// Created by colby on 18-10-17.
//

#include "depth_map_1transformation.h"

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_myntai_sdk_usbcamera_CameraMainActivity_depth_1map_1transformation(JNIEnv *env,
                                                                            jobject instance,
                                                                            jintArray pixels_,
                                                                            jint w, jint h) {
    jint *pixels = env->GetIntArrayElements(pixels_, NULL);
    jint *cbuf;
    jboolean ptfalse = false;
    jbyteArray gray_byte_array = env->NewByteArray(2 * w * h);
    unsigned char *byte_array = new unsigned char[2 * h * w];

    cbuf = env->GetIntArrayElements(pixels_, &ptfalse);
    if (cbuf == NULL) {
        (*env).SetByteArrayRegion(gray_byte_array, 0, 2 * h * w, (jbyte *) byte_array);
        return gray_byte_array;
    }

    Mat depth(h, w * 2, CV_16SC1);

    Convert_Buff2Mat(cbuf, depth, h, w);
    depth_normalize(depth);
    get_grayscale_map(depth);
    mat_to_byte_array(depth, byte_array, h, w);

    (*env).SetByteArrayRegion(gray_byte_array, 0, 2 * h * w, (jbyte *) byte_array);
    env->ReleaseIntArrayElements(pixels_, pixels, 0);
    LOGI("return");
    return gray_byte_array;
}

void depth_normalize(Mat &depth) {
    normalize(depth, depth, 0, 254, NORM_MINMAX);
}

void Convert_Buff2Mat(jint *cbuf, Mat &depth, int h, int w) {
    int kum = 0;
    for (int i = 0; i < h; i++) {
        short *depth_ptr = depth.ptr<short>(i);
        int width_temp = (int) 2 * w;
        for (int j = 0; j < width_temp; j++) {
            depth_ptr[j] = ushort(cbuf[kum]);
            kum++;
        }
    }
}

void get_grayscale_map(Mat &depth) {
    Mat depth_copy;
    depth.copyTo(depth_copy);
    Mat depth_copy_inv = 255 - depth_copy;
    depth_copy_inv.convertTo(depth, CV_8UC1, 1, 0);
}

void mat_to_byte_array(Mat &depth, unsigned char *byte_array, int h, int w) {
    int kum = 0;
    int width_temp = 2 * w;
    for (int i = 0; i < h; i++) {
        char *depth_ptr = depth.ptr<char>(i);
        for (int j = 0; j < width_temp; j++) {
            byte_array[kum] = uchar(depth_ptr[j]);
            kum++;
        }
    }
}


void bitmap_to_mat2(JNIEnv *env, jobject &bitmap, Mat &mat, jboolean needUnPremultiplyAlpha) {
    AndroidBitmapInfo info;
    void *pixels = 0;
    Mat &dst = mat;
    try {
        LOGD("nBitmapToMat");
        CV_Assert(AndroidBitmap_getInfo(env, bitmap, &info) >= 0);
        CV_Assert(info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 ||
                  info.format == ANDROID_BITMAP_FORMAT_RGB_565);
        CV_Assert(AndroidBitmap_lockPixels(env, bitmap, &pixels) >= 0);
        CV_Assert(pixels);
        dst.create(info.height, info.width, CV_8UC4);
        if (info.format == ANDROID_BITMAP_FORMAT_RGBA_8888) {
            LOGD("nBitmapToMat: RGBA_8888 -> CV_8UC4");
            Mat tmp(info.height, info.width, CV_8UC4, pixels);
            if (needUnPremultiplyAlpha) cvtColor(tmp, dst, COLOR_mRGBA2RGBA); else tmp.copyTo(dst);
        } else { // info.format == ANDROID_BITMAP_FORMAT_RGB_565
            LOGD("nBitmapToMat: RGB_565 -> CV_8UC4");
            Mat tmp(info.height, info.width, CV_8UC2, pixels);
            cvtColor(tmp, dst, COLOR_BGR5652RGBA);
        }
        AndroidBitmap_unlockPixels(env, bitmap);
        return;
    } catch (const cv::Exception &e) {
        AndroidBitmap_unlockPixels(env, bitmap);
        LOGE("nBitmapToMat catched cv::Exception: %s", e.what());
        jclass je = env->FindClass("org/opencv/core/CvException");
        if (!je) je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, e.what());
        return;
    } catch (...) {
        AndroidBitmap_unlockPixels(env, bitmap);
        LOGE("nBitmapToMat catched unknown exception (...)");
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, "Unknown exception in JNI code {nBitmapToMat}");
        return;
    }
}

void bit_map_to_mat(JNIEnv *env, jobject &bitmap, Mat &mat) {
    bitmap_to_mat2(env, bitmap, mat, false);
}

void mat_to_bitmap2(JNIEnv *env, Mat &mat, jobject &bitmap, jboolean needPremultiplyAlpha) {
    AndroidBitmapInfo info;
    void *pixels = 0;
    Mat &src = mat;
    try {
        LOGD("nMatToBitmap");
        CV_Assert(AndroidBitmap_getInfo(env, bitmap, &info) >= 0);
        CV_Assert(info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 ||
                  info.format == ANDROID_BITMAP_FORMAT_RGB_565);
        CV_Assert(src.dims == 2 && info.height == (uint32_t) src.rows &&
                  info.width == (uint32_t) src.cols);
        CV_Assert(src.type() == CV_8UC1 || src.type() == CV_8UC3 || src.type() == CV_8UC4);
        CV_Assert(AndroidBitmap_lockPixels(env, bitmap, &pixels) >= 0);
        CV_Assert(pixels);
        if (info.format == ANDROID_BITMAP_FORMAT_RGBA_8888) {
            Mat tmp(info.height, info.width, CV_8UC4, pixels);
            if (src.type() == CV_8UC1) {
                LOGD("nMatToBitmap: CV_8UC1 -> RGBA_8888");
                cvtColor(src, tmp, COLOR_GRAY2RGBA);
            } else if (src.type() == CV_8UC3) {
                LOGD("nMatToBitmap: CV_8UC3 -> RGBA_8888");
                cvtColor(src, tmp, COLOR_RGB2RGBA);
            } else if (src.type() == CV_8UC4) {
                LOGD("nMatToBitmap: CV_8UC4 -> RGBA_8888");
                if (needPremultiplyAlpha) cvtColor(src, tmp, COLOR_RGBA2mRGBA);
                else
                    src.copyTo(tmp);
            }
        } else { // info.format == ANDROID_BITMAP_FORMAT_RGB_565
            Mat tmp(info.height, info.width, CV_8UC2, pixels);
            if (src.type() == CV_8UC1) {
                LOGD("nMatToBitmap: CV_8UC1 -> RGB_565");
                cvtColor(src, tmp, COLOR_GRAY2BGR565);
            } else if (src.type() == CV_8UC3) {
                LOGD("nMatToBitmap: CV_8UC3 -> RGB_565");
                cvtColor(src, tmp, COLOR_RGB2BGR565);
            } else if (src.type() == CV_8UC4) {
                LOGD("nMatToBitmap: CV_8UC4 -> RGB_565");
                cvtColor(src, tmp, COLOR_RGBA2BGR565);
            }
        }
        AndroidBitmap_unlockPixels(env, bitmap);
        return;
    } catch (const cv::Exception &e) {
        AndroidBitmap_unlockPixels(env, bitmap);
        LOGE("nMatToBitmap catched cv::Exception: %s", e.what());
        jclass je = env->FindClass("org/opencv/core/CvException");
        if (!je) je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, e.what());
        return;
    } catch (...) {
        AndroidBitmap_unlockPixels(env, bitmap);
        LOGE("nMatToBitmap catched unknown exception (...)");
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, "Unknown exception in JNI code {nMatToBitmap}");
        return;
    }
}

void mat_to_bitmap(JNIEnv *env, Mat &mat, jobject &bitmap) {
    mat_to_bitmap2(env, mat, bitmap, false);
}
```

