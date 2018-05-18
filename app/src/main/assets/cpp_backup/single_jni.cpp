#include <jni.h>
#include <android/log.h>

#include <gflags/gflags.h>
#include <glog/logging.h>

#include <cstring>
#include <map>
#include <string>
#include <vector>
#include <map>

#include "boost/algorithm/string.hpp"
#include "caffe/caffe.hpp"
#include "caffe/proto/caffe.pb.h"

#ifdef __cplusplus
extern "C" {
#endif

using caffe::Blob ;
using caffe::Caffe ;
using caffe::Net ;
using caffe::Layer ;
using caffe::Solver ;
using caffe::shared_ptr ;
using caffe::string ;
using caffe::Timer ;
using caffe::vector ;
using std::ostringstream ;

int FLAGS_iterations = 5;

string jstring2string(JNIEnv *env, jstring jstr) {
    const char *cstr = env->GetStringUTFChars(jstr, 0);
    string str(cstr);
    env->ReleaseStringUTFChars(jstr, cstr);
    return str;
}

extern "C"
JNIEXPORT jdoubleArray JNICALL
Java_com_cambricon_productdisplay_caffenative_SingleNetDetection_SingleNetTime(JNIEnv *env,
                                                                               jclass type,
                                                                               jstring model_file_,
                                                                               jstring trained_file_) {
    const char *model_file = env->GetStringUTFChars(model_file_, 0);
    const char *trained_file = env->GetStringUTFChars(trained_file_, 0);

    LOG(INFO) << "Use CPU.";
    Caffe::set_mode(Caffe::CPU);

    Net<float> caffe_net(jstring2string(env, model_file_), caffe::TEST);
    caffe_net.CopyTrainedLayersFrom(jstring2string(env, trained_file_));

    LOG(INFO) << "Performing Forward";

    float initial_loss;

    caffe_net.Forward(&initial_loss);
    LOG(INFO) << "Initial loss: " << initial_loss;

    const vector<shared_ptr<Layer<float> > >& layers = caffe_net.layers();
    const vector<vector<Blob<float> *> >& bottom_vecs = caffe_net.bottom_vecs();
    const vector<vector<Blob<float> *> >& top_vecs = caffe_net.top_vecs();

    int size = layers.size();

    LOG(INFO) << "*** Benchmark begins ***";
    LOG(INFO) << "Testing for " << FLAGS_iterations << " iterations.";

    struct timeval totalbegin, totalend,
            iterBegin, iterEnd,
            forwardBegin, forwardEnd,
            timerBegin, timerEnd;
    double totalTime_use = 0.0, forward_time = 0.0;

    gettimeofday(&totalbegin, NULL);

    jdoubleArray result = env->NewDoubleArray(size);
    jdouble *array = new jdouble[size];


    std::vector<double> forward_time_per_layer(layers.size(), 0.0);

    for (int j = 0; j < FLAGS_iterations; ++j) {
        gettimeofday(&iterBegin, NULL);
        gettimeofday(&forwardBegin, NULL);
        for (int i = 0; i < layers.size(); ++i) {
            gettimeofday(&timerBegin, NULL);
            layers[i]->Forward(bottom_vecs[i], top_vecs[i]);
            gettimeofday(&timerEnd, NULL);
            forward_time_per_layer[i] += 1000000 * (timerEnd.tv_sec - timerBegin.tv_sec)+timerEnd.tv_usec - timerBegin.tv_usec;
        }
        gettimeofday(&forwardEnd, NULL);
        forward_time += 1000000 * (forwardEnd.tv_sec - forwardBegin.tv_sec)+forwardEnd.tv_usec - forwardBegin.tv_usec;
        gettimeofday(&iterEnd, NULL);
        LOG(INFO) << "Iteration: " << j + 1 << " forward time: "
                  << 1000000 * (iterEnd.tv_sec - iterBegin.tv_sec)+iterEnd.tv_usec-iterBegin.tv_usec << " us.";
    }
    LOG(INFO) << "Average time per layer: ";

    for (int i = 1; i < layers.size(); ++i) {
        const caffe::string &layername = layers[i]->layer_param().name();
        LOG(INFO) << std::setfill(' ') << std::setw(10) << i << "th " << layername <<
                  "\tforward: " << forward_time_per_layer[i] /
                                   FLAGS_iterations << " ms.";

        array[i-1] = (forward_time_per_layer[i] / FLAGS_iterations);
    }
    gettimeofday(&totalend, NULL);

    totalTime_use = 1000000 * (totalend.tv_sec - totalbegin.tv_sec)+ totalend.tv_usec- totalend.tv_usec;

    env->SetDoubleArrayRegion(result, 0, size, array);

    env->ReleaseStringUTFChars(model_file_, model_file);
    env->ReleaseStringUTFChars(trained_file_, trained_file);

    return result;
}

#ifdef __cplusplus
}
#endif

