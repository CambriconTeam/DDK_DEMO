#include <string.h>
#include <jni.h>
#include <cstdlib>
#include "caffe/caffe.hpp"
// Created by dell on 18-5-9.
//

string jstring2string(JNIEnv *env, jstring jstr) {
    const char *cstr = env->GetStringUTFChars(jstr, 0);
    string str(cstr);
    env->ReleaseStringUTFChars(jstr, cstr);
    return str;
}
extern "C"
JNIEXPORT void JNICALL
Java_com_cambricon_productdisplay_caffenative_OffLineCaffeClassification_offClassification(
        JNIEnv *env, jclass type, jstring offmode_, jstring listfile_, jstring label_,
jstring mean_) {
/*const char *offmode = env->GetStringUTFChars(offmode_, 0);
const char *listfile = env->GetStringUTFChars(listfile_, 0);
const char *label = env->GetStringUTFChars(label_, 0);
const char *mean = env->GetStringUTFChars(mean_, 0);*/
    LOG(INFO)<<"Usage:offline.cambricon,image_list_file,labels.txt,image_mean_file";

    //huangyaling
    names = jstring2string(env,label_);
    ipuStatus_t ret;

    ipuLibInit();
    ipuAddIteration();

    ipuModelDesc_t modelDesc;
    ipuCreateModelDescriptor(&modelDesc);
    ipuSetModelDescriptorFromFile(modelDesc, jstring2string(env,offmode_));

    ipuMallocOfflineModel(modelDesc);
    ret = ipuModelInit(modelDesc);
    if(ret == IPU_STATUS_FAIL){
        cout << "error ! ipuModelInit failed" <<endl;
        ipuDestroyModelDescriptor(modelDesc);
        exit(-1);
    }

    ipuNetInfo_t input_netinfo = NULL, output_netinfo = NULL;
    ret = ipuGetModelInfo(modelDesc, input_netinfo, output_netinfo);
    if(ret == IPU_STATUS_FAIL){
        cout << "error ! ipuGetModelInfo failed" << endl;
        ipuDestroyModelDescriptor(modelDesc);
        exit(-1);
    }

    in_n = ipuGetShape(input_netinfo,0,0)[0];
    in_c = ipuGetShape(input_netinfo,0,0)[1];
    in_h = ipuGetShape(input_netinfo,0,0)[2];
    in_w = ipuGetShape(input_netinfo,0,0)[3];

    out_n = ipuGetShape(output_netinfo,0,0)[0];
    out_c = ipuGetShape(output_netinfo,0,0)[1];
    out_h = ipuGetShape(output_netinfo,0,0)[2];
    out_w = ipuGetShape(output_netinfo,0,0)[3];

    ret = ipuCreateTensorDescriptor(&tensor_input);
    if(ret == IPU_STATUS_FAIL){
        cout << "error ! ipuCreateTensorDescriptor of input failed" << endl;
        ipuDestroyModelDescriptor(modelDesc);
        exit(-1);
    }
    ret = ipuSetTensor4dDescriptor(tensor_input,
                                   CPU_DATA_FLOAT32, CPU_TENSOR_NCHW,
                                   IPU_DATA_FLOAT16, IPU_TENSOR_NEURON,
                                   modelDesc,
                                   input_netinfo,
                                   0, 0, HOST_TO_DEVICE);

    if(ret == IPU_STATUS_FAIL){
        cout << "error ! ipuSetTensor4dDescriptor of input failed" << endl;
        ipuDestroyTensorDescriptor(tensor_input);
        exit(-1);
    }
    ret = ipuCreateTensorDescriptor(&tensor_output);
    ret = ipuSetTensor4dDescriptor(tensor_output,
                                   CPU_DATA_FLOAT32, CPU_TENSOR_NCHW,
                                   IPU_DATA_FLOAT16, IPU_TENSOR_NEURON,
                                   modelDesc,
                                   output_netinfo,
                                   0, 0, DEVICE_TO_HOST);
    if(ret == IPU_STATUS_FAIL){
        cout << "error ! ipuSetTensor4dDescriptor of output failed" << endl;
        ipuDestroyTensorDescriptor(tensor_input);
        ipuDestroyTensorDescriptor(tensor_output);
        exit(-1);
    }

    means = (float*) malloc(sizeof(float) * 3);
    ifstream fin;
    fin.open(jstring2string(env,mean_));
    for(int i = 0; i < 3; i++)
        fin >> means[i];
    fin.close();

    int outputSize = out_n * out_c * out_h * out_w;
    dataBuffer = (float*) malloc(sizeof(float) * in_n * in_c * in_h * in_w);
    output_cpu = (float*) malloc(outputSize * sizeof(float));
    file_name = jstring2string(env,listfile_);

    long inputaddr, outputaddr;
    inputaddr = ipuGetAddr(input_netinfo,0,0,0);
    outputaddr = ipuGetAddr(output_netinfo,0,0,0);

    void *s=NULL;
    int count = 1;
    float time_use;
    struct timeval tpstart, tpend;
    gettimeofday(&tpstart, NULL);

    while(!file_clear) {
        if(count > 0) {
            get_img();
            if(!file_clear){
                ipuMemcpyOfflineModel(dataBuffer, (void *)inputaddr, tensor_input, modelDesc, HOST_TO_DEVICE);
                ipuInvoke(modelDesc, 0);
                ipuDeviceSync();
                cout<<"after ipuDeviceSync"<<endl;
                ipuMemcpyOfflineModel((void *)outputaddr, output_cpu, tensor_output, modelDesc, DEVICE_TO_HOST);
                softmax_print(s);

                count++;
            }
        }
    }
    gettimeofday(&tpend, NULL);
    time_use = 1000000 * (tpend.tv_sec - tpstart.tv_sec) + tpend.tv_usec - tpstart.tv_usec;
    std::cout <<"total use: " << time_use << " us" << std::endl;
    ipuPipelineDrain();

    ipuFreeOfflineModel(modelDesc);
    ipuDestroyModelDescriptor(modelDesc);
    ipuDestroyTensorDescriptor(tensor_input);
    ipuDestroyTensorDescriptor(tensor_output);

    free(means);
    free(dataBuffer);
    free(output_cpu);
    ipuLibExit();
    //huangyaling

// TODO

env->ReleaseStringUTFChars(offmode_, offmode);
env->ReleaseStringUTFChars(listfile_, listfile);
env->ReleaseStringUTFChars(label_, label);
env->ReleaseStringUTFChars(mean_, mean);
}

