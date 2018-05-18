#include <jni.h>
#include <vector>
#include <string>
#include <iostream>
#include <opencv2/opencv.hpp>
#include "face_detector.hpp"
#include "helpers.hpp"

using namespace std;
using namespace cv;

string jstring2string(JNIEnv *env, jstring jstr) {
    const char *cstr = env->GetStringUTFChars(jstr, 0);
    string str(cstr);
    env->ReleaseStringUTFChars(jstr, cstr);
    return str;
}

/**
 * 人脸检测JNI方法
 *
 * 通过从Java层传入resultPixel获得图像的数组,这里需要将其从jintArray转换为jint
 * 再将图像完整的放入OpenCV Mat中,在OpenCV的框架下对图像进行操作
 *
 * 由于转换后对同一Mat操作后都将记录在resultPixel中,故不需要特意设置转换的值,
 * 且即使设置了将Mat再转回jint数组并没有在Java层中操作方便.
 *
 * Chengyu Yang 2018-3-12
 */

extern "C"
JNIEXPORT jintArray JNICALL
Java_com_cambricon_productdisplay_caffenative_FaceDetection_doFaceDetector(JNIEnv *env, jclass type,
                                                                           jstring modelDir_,
                                                                           jint w, jint h,
                                                                           jintArray resultPixel_) {
    const char *modelDir = env->GetStringUTFChars(modelDir_, 0);
    jint *resultPixel = env->GetIntArrayElements(resultPixel_, NULL);

    mtcnn::FaceDetector fd(jstring2string(env, modelDir_), 0.6f, 0.7f, 0.7f, true, false, 0);
    cv::Mat img(h, w, CV_8UC4, (unsigned char *) resultPixel);
    std::vector<mtcnn::Face> faces = fd.detect(img, 40.f, 0.709f);
    std::vector<cv::Point> pts;
    std::cout << "pts:" << pts.size() << std::endl;


    //Draw Faces
    cv::Rect r;
    img.convertTo(img, CV_8UC3);
    for (size_t i = 0; i < faces.size(); ++i) {
        std::cout << faces[i].bbox.getRect() << std::endl;
        r = faces[i].bbox.getRect();
        cv::rectangle(img, r, cv::Scalar(0, 255, 0),4,8,0);
        for (size_t i = 0; i < pts.size(); ++i) {
            cv::circle(img, pts[i], 3, cv::Scalar(0, 255, 0));
        }
    }

    int size = w * h;
    jintArray result = env->NewIntArray(size);
    env->SetIntArrayRegion(result, 0, size, resultPixel);
    env->ReleaseStringUTFChars(modelDir_, modelDir);
    env->ReleaseIntArrayElements(resultPixel_, resultPixel, 0);
    return result;
}