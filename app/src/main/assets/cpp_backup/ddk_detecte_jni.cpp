
#define LOG_TAG "aiserver_test"

#include <stdlib.h>
#include <time.h>
#include <string.h>
#include <limits.h>
#include <sys/mman.h>
#include <string>
#include <fstream>
#include <android/log.h>
#include <cmath>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <vector>

#include <nativehelper/jni.h>
#include <nativehelper/JNIHelp.h>
#include <utils/Mutex.h>
#include "securec.h"
#include "AiModelManager.h"

using namespace android;
using namespace ai;
using namespace std;

using ::vendor::huawei::hardware::ai::V1_0::AiDataType;
using ::vendor::huawei::hardware::ai::V1_0::AiFramework;
using ::vendor::huawei::hardware::ai::V1_0::AiModelEncrypt;

using ::vendor::huawei::hardware::ai::V1_0::ModelInfo;
using ::vendor::huawei::hardware::ai::V1_0::ModelDescription;
using ::vendor::huawei::hardware::ai::V1_0::TensorDescription;
using ::vendor::huawei::hardware::ai::V1_0::AiDevPerf;

#define IMG_H 227
#define IMG_W 227
#define IMG_C 3
#define MAX_DATA_SIZE IMG_H*IMG_W*IMG_C

#define LOG_TAG1 "Ai_DDK_Msg"

#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG1, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG1, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG1, __VA_ARGS__)

static char raw_data[MAX_DATA_SIZE];
static float input_data[MAX_DATA_SIZE];

static std::vector<std::string> synset_words;

int g_status = 0;

float time_use;
struct timeval tpstart, tpend;

std::ostringstream stringStream;

enum ModelMngrStatus{
    STATUS_START_DONE = 1,
    STATUS_RUN_DONE = 2,
    STATUS_STOP_DONE = 3,
    STATUS_ERROR = 4,
    STATUS_TIMEOUT = 5,
    STATUS_SERVICE_DIED = 6,
};
class MMTaskWaiter
{
public:
    MMTaskWaiter()
        : count(0)
    {
        pthread_mutex_init(&mMutex, NULL);
        pthread_cond_init(&mCond, NULL);
    }

    ~MMTaskWaiter()
    {
        pthread_cond_destroy(&mCond);
        pthread_mutex_destroy(&mMutex);
    }

    int32_t increase(int32_t ret, int32_t addCnt=1)
    {
        pthread_mutex_lock(&mMutex);
        if(0 == ret)
        {
            count += addCnt;
            LOGD("MMTaskWaiter: add task, task count[%d]\n", count);
        }
        pthread_mutex_unlock(&mMutex);
        return ret;
    }

    void decrease(int32_t decCnt=1)
    {
        pthread_mutex_lock(&mMutex);
        count -= decCnt;
        LOGD("MMTaskWaiter: dec task, task count[%d]\n", count);
        pthread_mutex_unlock(&mMutex);

        LOGD("MMTaskWaiter: signal once\n");
        pthread_cond_signal(&mCond);
    }

    void cancel()
    {
        pthread_mutex_lock(&mMutex);
        count = 0;
        LOGD("MMTaskWaiter: cancel, task count reset to 0\n");
        pthread_mutex_unlock(&mMutex);

        LOGD("MMTaskWaiter: signal once\n");
        pthread_cond_signal(&mCond);
    }

    void wait()
    {
        pthread_mutex_lock(&mMutex);

        while (count != 0)
        {
            LOGD("MMTaskWaiter: wait...  task count[%d]\n", count);
            pthread_cond_wait(&mCond, &mMutex);
        }
        pthread_mutex_unlock(&mMutex);
    }

private:
    int count;
    pthread_mutex_t mMutex;
    pthread_cond_t mCond;
};

sp<AiModelManagerListener> sp_mmListener = NULL;
sp<AiModelManager> sp_aiModelMngrClient = NULL;
MMTaskWaiter waiter;
class MMListener: public AiModelManagerListener
{
public:
    MMListener(MMTaskWaiter* waiter) {
        mWaiter = waiter;
    }

    ~MMListener() {
    }

    void onStartDone() override
    {
        LOGI("MMListener get onStartDone!\n");
        g_status = STATUS_START_DONE;
        mWaiter->decrease();
    }

    void onRunDone(vector<native_handle_t *> destDataVec, vector<TensorDescription> destTensorVec)
    {
        LOGI("MMListener get onRunDone!\n");
        mWaiter->decrease();
    }

    void onStopDone() override
    {
        LOGI("MMListener get onStopDone!\n");
        g_status = STATUS_STOP_DONE;
        mWaiter->decrease();
    }

    void onError(int32_t errCode) override
    {
        LOGI("MMListener get onError! ErrCode=%d!\n", errCode);
        g_status = STATUS_ERROR;
        mWaiter->decrease();
    }

    void onTimeout(vector<native_handle_t *> srcDataVec)
    {
        LOGI("MMListener get onTimeout!\n");
        g_status = STATUS_TIMEOUT;
        mWaiter->decrease();
    }

    void onServiceDied() override
    {
        LOGI("MMListener get onServiceDied!\n");
        g_status = STATUS_SERVICE_DIED;
        mWaiter->cancel();
    }

private:
    MMTaskWaiter* mWaiter;
};


#ifdef __cplusplus
extern "C" {
#endif
JNIEXPORT jint JNICALL
Java_com_cambricon_productdisplay_caffenative_OfflineDetecte_loadModelSyncFromSdcard(JNIEnv *env,
                                                                                       jclass type) {

    // TODO
   if(sp_aiModelMngrClient == NULL){
        LOGE("you should invoke createModelClient first!");
        return STATUS_ERROR;
    }

    vector<ModelBuffer> modelBufferVec;
    sp<ModelBufferWrapper> wrap = ModelBufferWrapper::createFromModelFile("model1", "/sdcard/caffe_mobile/ipu/fasterrcnn.cambricon", AiDevPerf::DEV_HIGH_PROFILE);
    
    ModelBuffer modelBuffer = wrap->getModelBuf();
    
    jint ret = -1;
    ret = (jint)sp_aiModelMngrClient->startModel(modelBuffer);
    LOGD("chenfuduo startModel from sdcard %d\n", ret);
    
    return ret;

}

JNIEXPORT void JNICALL
Java_com_cambricon_productdisplay_caffenative_OfflineDetecte_createModelClient(JNIEnv *env,
                                                                                 jobject instance,
                                                                                 jint isSync) {

    // TODO
    LOGD("create client issync value %d", isSync);
    
    if(isSync == 1){
        if (sp_aiModelMngrClient == NULL){
            LOGI("create sync client.");
            sp_aiModelMngrClient = new AiModelManager();
        }else{
            LOGE("client existed.");
            return;
        }
    }
    else{
        if (sp_mmListener == NULL){
            sp_mmListener = new MMListener(&waiter);
        }else{
            LOGE("listener existed.");
        }
        
        if (sp_aiModelMngrClient == NULL && sp_mmListener != NULL){
            sp_aiModelMngrClient = new AiModelManager(sp_mmListener);
        }else{
            LOGE("create async client.");
        }
    }
    return;

}

/* stop model sync */
JNIEXPORT jint Java_com_cambricon_productdisplay_caffenative_OfflineDetecte_stopModelSync(JNIEnv *env, jobject)
{
    if(sp_aiModelMngrClient == NULL){
        sp_aiModelMngrClient = new AiModelManager();
    }
    
    jint ret = (jint)sp_aiModelMngrClient->stopModel();
    
    return ret;
}

JNIEXPORT void JNICALL Java_com_cambricon_productdisplay_caffenative_OfflineDetecte_destroyModelClient(
        JNIEnv *env,
        jobject /* this */,
        jint isSync){
    
    LOGD("destroy client issync value %d", isSync);
    
    if (isSync == 1){
        if (sp_aiModelMngrClient != NULL){
            LOGI("destroy sync client.");
            sp_aiModelMngrClient = NULL;
        }
    }else{
        LOGI("destroy async client.");
        if (sp_mmListener != NULL){
            sp_mmListener = NULL;
        }
        if (sp_aiModelMngrClient != NULL){
            sp_aiModelMngrClient = NULL;
        }
    }

    return;
}
#ifdef __cplusplus
}
#endif
