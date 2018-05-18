#include <jni.h>
#include <string>

extern "C"
JNIEXPORT jstring

JNICALL
Java_com_cambricon_productdisplay_activity_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
extern "C"
JNIEXPORT jint JNICALL
Java_com_cambricon_productdisplay_caffenative_CaffeDetection_loadModel(JNIEnv *env,
                                                                       jobject instance,
                                                                       jstring modelPath_,
                                                                       jstring weightsPath_,
                                                                       jboolean mode) {
    const char *modelPath = env->GetStringUTFChars(modelPath_, 0);
    const char *weightsPath = env->GetStringUTFChars(weightsPath_, 0);

    // TODO

    env->ReleaseStringUTFChars(modelPath_, modelPath);
    env->ReleaseStringUTFChars(weightsPath_, weightsPath);
}