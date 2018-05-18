#ifndef CAFFE_DETECTION_HPP_
#define CAFFE_DETECTION_HPP_

#include <caffe/caffe.hpp>
#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <algorithm>
#include <iosfwd>
#include <memory>
#include <string>
#include <utility>
#include <vector>
#include <iostream>
using std::clock;
using std::clock_t;
using std::string;
using std::vector;

using caffe::Blob;
using caffe::Caffe;
using caffe::Datum;
using caffe::Net;

namespace caffe{
typedef std::vector<float> detection;
typedef unsigned char byte;

const string CLASSES[21] = {"background",
                            "aeroplane", "bicycle", "bird", "boat",
                            "bottle", "bus", "car", "cat", "chair",
                            "cow", "diningtable", "dog", "horse",
                            "motorbike", "person", "pottedplant"
                            "sheep", "sofa", "train", "tvmonitor"};

const cv::Scalar COLOR[21] = {{0,0,0},
                              {255,0,0}, {255,0,255}, {226,43,138}, {140,199,0},
                              {0,255,0}, {34,139,34}, {255,255,0}, {255,0,255},
                              {0,0,255}, {0,255,255}, {0,128,255}, {0,215,255},
                              {255,255,255}, {192,192,192}, {184,169,238}, {86,87,88}};
//header end
class Detection {
  public:
    ~Detection();
  
    cv::Mat show(const cv::Mat& img);

    static Detection *Get();
    static Detection *Get(const string &model_path, const string &weights_path, const bool &mode);
    void SetScale(const float scale);

    void SetMean(const string& mean_file);
    void SetMean(const vector<float> &mean_values);

    Detection(const string &model_path, const string &weights_path,const bool &mode);
    void im_detect(const cv::Mat& img, float scores[], float boxes[]);

  private:
    //attributes
    static Detection *caffe_detection_;
    static string model_path_;
    static string weights_path_;
    shared_ptr<Net<float> > net_;
    int num_channels_;
    cv::Size input_geometry_;
    int im_shape[3];
    cv::Mat mean_;
    float scales;
    static bool mode_;

    //methods
    
    void WrapInputLayer(std::vector<cv::Mat>* input_channels);

    void Preprocess(const cv::Mat& img,
                    std::vector<cv::Mat>* input_channels);

    void bbox_transform_inv(const float boxes[], 
                            const float deltas[],
                            float pred_boxes[]);
    void clip_boxes(float boxes[]);

    void copy(const float input[], float scores[], int num);

    void update(const detection &member, detection &means);

    void drawRec(const cv::Mat& img, const std::vector<detection> &nmsed_dets);

    void cpp_nms(float dets[][5],float thresh,std::vector<detection> &nmsed_dets, float cls);

    void nms(const float boxes[], const float scores[], std::vector<detection> &v);

    int count_pos(float score[],int n);

    int ind_start(float score[],int ind[],int n);

    void del(float x1[],float y1[],float x2[],float y2[],float score[],float areas[],int key);
    
};
}//namespace
#endif
