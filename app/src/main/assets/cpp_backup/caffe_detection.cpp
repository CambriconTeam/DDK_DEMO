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
#include "caffe_detection.hpp"
using std::clock;
using std::clock_t;
using std::string;
using std::vector;

using caffe::Blob;
using caffe::Caffe;
using caffe::Datum;
using caffe::Net;

using namespace cv;

namespace caffe{
//header end
Detection *Detection::caffe_detection_ = 0;
string Detection::model_path_ = "";
string Detection::weights_path_ = "";
bool Detection::mode_=1;



Detection *Detection::Get() {
  CHECK(caffe_detection_);
  return caffe_detection_;
}

Detection *Detection::Get(const string &model_path,
                              const string &weights_path, const bool &mode) {
  if (!caffe_detection_ || model_path != model_path_ ||
      weights_path != weights_path_|| mode!= mode_) {
    caffe_detection_ = new Detection(model_path, weights_path, mode);
    model_path_ = model_path;
    weights_path_ = weights_path;
  }
  return caffe_detection_;
}
Detection::~Detection() { net_.reset(); }
Detection::Detection(const string &model_path, const string &weights_path,const bool &mode) {
  CHECK_GT(model_path.size(), 0) << "Need a model definition to score.";
  CHECK_GT(weights_path.size(), 0) << "Need model weights to score.";
  LOG(INFO) << "mode";
  LOG(INFO) << "mode" <<mode;
  
  //Caffe::set_mode(Caffe::CPU);
  if(mode){
	 LOG(ERROR)<<"is cpu mode";
	 Caffe::set_mode(Caffe::CPU);
  }else{
     LOG(ERROR)<<"is ipu mode";
     Caffe::set_mode(Caffe::CPU);
  }
  
  scales=0.0;
  
  clock_t t_start = clock();
  LOG(INFO) << "model load begin";
  net_.reset(new Net<float>(model_path, caffe::TEST));
  LOG(INFO) << "model load end and weights load begin";
  net_->CopyTrainedLayersFrom(weights_path);
  LOG(INFO) << "weights load end";
  clock_t t_end = clock();
  LOG(INFO) << "Loading time: " << 1000.0 * (t_end - t_start) / CLOCKS_PER_SEC
            << " ms.";
  //CHECK_EQ(net_->num_inputs(), 2) << "Network should have exactly one input.";
  //CHECK_EQ(net_->num_outputs(), 1) << "Network should have exactly one output.";

  Blob<float>* input_layer = net_->input_blobs()[0];
  num_channels_ = input_layer->channels();
  CHECK(num_channels_ == 3 || num_channels_ == 1)
    << "Input layer should have 1 or 3 channels.";
  input_geometry_ = cv::Size(input_layer->width(), input_layer->height());
}

void Detection::bbox_transform_inv(const float boxes[], 
                                   const float deltas[],
                                   float pred_boxes[]) {
  int proposal_count = 300;
  for (int i =0; i < proposal_count; i++){
    int box = i * 4;
    int delta = i * 8;
    int pred = i * 8;
    float widths = boxes[box + 2] - boxes[box + 0] + 1;
    float heights = boxes[box + 3] - boxes[box + 1] + 1;

    float pred_w0 = exp(deltas[delta + 2]) * widths;
    float pred_w1 = exp(deltas[delta + 6]) * widths;
    float pred_h0 = exp(deltas[delta + 3]) * heights;
    float pred_h1 = exp(deltas[delta + 7]) * heights;
  
    pred_boxes[pred] = (deltas[delta] + 0.5) * widths + boxes[box] - 0.5 * pred_w0;
    pred_boxes[pred + 4] = (deltas[delta + 4] + 0.5) * widths + boxes[box] - 0.5 * pred_w1;
    pred_boxes[pred + 1] = (deltas[delta + 1] + 0.5) * heights + boxes[box + 1] - 0.5 * pred_h0;
    pred_boxes[pred + 5] = (deltas[delta + 5] + 0.5) * heights + boxes[box + 1] - 0.5 * pred_h1;
    pred_boxes[pred + 2] = (deltas[delta] + 0.5) * widths + boxes[box] + 0.5 * pred_w0;
    pred_boxes[pred + 6] = (deltas[delta + 4] + 0.5) * widths + boxes[box] + 0.5 * pred_w1;
    pred_boxes[pred + 3] = (deltas[delta + 1] + 0.5) * heights + boxes[box + 1] + 0.5 * pred_h0;
    pred_boxes[pred + 7] = (deltas[delta + 5] + 0.5) * heights + boxes[box + 1] + 0.5 * pred_h1;
  }
}

void Detection::copy(const float input[], float scores[], int num) {
  for (int i = 0; i < num; ++i) 
    scores[i] = input[i];
}
//huangyaling
void Detection::SetScale(const float scale_) {
  CHECK_GT(scale_, 0);
  scales= scale_;
}
void Detection::SetMean(const vector<float> &mean_values) {
  CHECK_EQ(mean_values.size(), num_channels_)
      << "Number of mean values doesn't match channels of input layer.";

  cv::Scalar channel_mean(0);
  double *ptr = &channel_mean[0];
  for (int i = 0; i < num_channels_; ++i) {
    ptr[i] = mean_values[i];
  }
  mean_ = cv::Mat(input_geometry_, (num_channels_ == 3 ? CV_32FC3 : CV_32FC1),
                  channel_mean);
}
//huangyaling

void Detection::SetMean(const string& mean_file) {
  BlobProto blob_proto;
  ReadProtoFromBinaryFileOrDie(mean_file.c_str(), &blob_proto);

  /* Convert from BlobProto to Blob<float> */
  Blob<float> mean_blob;
  mean_blob.FromProto(blob_proto);
  CHECK_EQ(mean_blob.channels(), num_channels_)
    << "Number of channels of mean file doesn't match input layer.";

  /* The format of the mean file is planar 32-bit float BGR or grayscale. */
  std::vector<cv::Mat> channels;
  float* data = mean_blob.mutable_cpu_data();
  for (int i = 0; i < num_channels_; ++i) {
    /* Extract an individual channel. */
    cv::Mat channel(mean_blob.height(), mean_blob.width(), CV_32FC1, data);
    channels.push_back(channel);
    data += mean_blob.height() * mean_blob.width();
  }

  /* Merge the separate channels into a single image. */
  cv::Mat mean;
  cv::merge(channels, mean);

  /* Compute the global mean pixel value and create a mean image
   * filled with this value. */
  cv::Scalar channel_mean = cv::mean(mean);
  mean_ = cv::Mat(input_geometry_, mean.type(), channel_mean);
}

void Detection::clip_boxes(float boxes[]) {
  int proposal_count = 300;
  for(int i = 0; i < proposal_count; i++) {
    int box_index = i * 8;
    for(int j = 0; j < 8; j++ ) {
      int k;
      k = (j % 2 == 0) ? 1 : 0;
      if (boxes[box_index+j] < 0)
        boxes[box_index+j] = 0;
      if (boxes[box_index+j] > (im_shape[k]-1) * scales)
        boxes[box_index+j] = (im_shape[k]-1) * scales;
    }
  }
}

void Detection::WrapInputLayer(std::vector<cv::Mat>* input_channels) {
  Blob<float>* input_layer = net_->input_blobs()[0];

  int width = input_layer->width();
  int height = input_layer->height();
  float* input_data = input_layer->mutable_cpu_data();
  for (int i = 0; i < input_layer->channels(); ++i) {
    cv::Mat channel(height, width, CV_32FC1, input_data);
    input_channels->push_back(channel);
    input_data += width * height;
  }
}

void Detection::Preprocess(const cv::Mat& img,
                           std::vector<cv::Mat>* input_channels) {
  /* Convert the input image to the input image format of the network. */
  cv::Mat sample;
  if (img.channels() == 3 && num_channels_ == 1)
    cv::cvtColor(img, sample, cv::COLOR_BGR2GRAY);
  else if (img.channels() == 4 && num_channels_ == 1)
    cv::cvtColor(img, sample, cv::COLOR_BGRA2GRAY);
  else if (img.channels() == 4 && num_channels_ == 3)
    cv::cvtColor(img, sample, cv::COLOR_BGRA2BGR);
  else if (img.channels() == 1 && num_channels_ == 3)
    cv::cvtColor(img, sample, cv::COLOR_GRAY2BGR);
  else
    sample = img;

  cv::Mat sample_resized;
  if (sample.size() != input_geometry_) {
    cv::resize(sample, sample_resized, input_geometry_);}
  else
    sample_resized = sample;

  cv::Mat sample_float;
  if (num_channels_ == 3)
    sample_resized.convertTo(sample_float, CV_32FC3);
  else
    sample_resized.convertTo(sample_float, CV_32FC1);

  cv::Mat sample_normalized;
  cv::subtract(sample_float, mean_, sample_normalized);

  /* This operation will write the separate BGR planes directly to the
   * input layer of the network because it is wrapped by the cv::Mat
   * objects in input_channels. */
  cv::split(sample_normalized, *input_channels);

  CHECK(reinterpret_cast<float*>(input_channels->at(0).data)
        == net_->input_blobs()[0]->cpu_data())
    << "Input channels are not wrapping the input layer of the network.";
}

void Detection::nms(const float boxes[], const float scores[], std::vector<detection> &v) {
	 LOG(INFO)<<"nms";
  float nmsArr[300][5];
  const float THRESH = 0.8;
  for (int i = 1; i < 21; ++i) {
    for (int j = 0; j < 300; ++j) {
      int box = j * 8;
      int score = j * 21;
      nmsArr[j][0] = boxes[box + 4];
      nmsArr[j][1] = boxes[box + 5];
      nmsArr[j][2] = boxes[box + 6];
      nmsArr[j][3] = boxes[box + 7];
      nmsArr[j][4] = scores[score + i];
    }
    cpp_nms(nmsArr, THRESH, v, i);
  }
}

void Detection::cpp_nms(float dets[][5],float thresh,std::vector<detection> &nmsed_dets, float cls){
    float x1[300], x2[300], y1[300], y2[300], score[300], areas[300];
    float key_x1, key_x2, key_y1, key_y2, key_areas;
    float xx1[300], xx2[300], yy1[300], yy2[300], width[300], height[300], inter[300], ovr[300];
    int ind[300], state[300]={0};
    float temp_score;
    int temp_ind, key;
    detection dect;
    for(int i = 0; i < 300; i++){
      x1[i] = dets[i][0];
      y1[i] = dets[i][1];
      x2[i] = dets[i][2];
      y2[i] = dets[i][3];
      xx1[i] = x1[i];
      yy1[i] = y1[i];
      xx2[i] = x2[i];
      yy2[i] = y2[i];
      score[i] = dets[i][4];
      areas[i] = (x2[i] - x1[i] + 1) * (y2[i] - y1[i] + 1);
      ind[i] = i;
    }
    for(int i = 0; i < 299; i++){
      for(int j = 0; j< 299-i; j++){
        if(score[j] <= score[j+1]){
          temp_score = score[j];
          score[j] = score[j+1];
          score[j+1] = temp_score;
          temp_ind = ind[j];
          ind[j] = ind[j+1];
          ind[j+1] = temp_ind;
        }
      }
    }

    int haha = 0;
    int order_size = count_pos(score,300);
    while (order_size > 0){
      haha ++;
      key = ind_start(score,ind,300);
      if (key == -1) {break;}
      if (state[key] == 0) {
        dect.push_back(cls);
        for(int i = 0; i < 5; i++)
          dect.push_back (dets[key][i]);
        nmsed_dets.push_back(dect);
        dect.clear();
      }
      key_x1 = x1[key];
      key_x2 = x2[key];
      key_y1 = y1[key];
      key_y2 = y2[key];
      key_areas = areas[key];
      state[key] = 2;
      del(x1,y1,x2,y2,score,areas,key);

      for(int i = 0; i < 300; i++) {
        xx1[i] = (x1[i] > key_x1) ? x1[i] : key_x1;
        yy1[i] = (y1[i] > key_y1) ? y1[i] : key_y1;
        xx2[i] = (x2[i] < key_x2) ? x2[i] : key_x2;
        yy2[i] = (y2[i] < key_y2) ? y2[i] : key_y2;
        width[i] = ((xx2[i] - xx1[i] + 1) > 0) ? (xx2[i] - xx1[i] + 1) : 0;
        height[i] = ((yy2[i] - yy1[i] + 1) > 0) ?  (yy2[i] - yy1[i] + 1) : 0;
        inter[i] = width[i] * height[i];
        if(state[i] == 0)
            ovr[i] = inter[i] / (key_areas - inter[i] + areas[i]);
        else
            ovr[i] = 1;
        if (ovr[i] > thresh) {
          state[i] = 1;
          del(x1,y1,x2,y2,score,areas,i);
        }
      }
      order_size = count_pos(score,300);
    }
}

int Detection::count_pos(float score[],int n){
  int count=0;
  for(int i = 0; i < n; i++){
    if(score[i] > -1)
      count++;
  }
  return count;
}

int Detection::ind_start(float score[],int ind[],int n){
  int pos ;
  for( int i = 0; i < n; i++){
    if(score[ind[i]] > -1){
      pos = ind[i];
      return pos;
    }
  }
  return -1;
}

void Detection::del(float x1[],float y1[],float x2[],float y2[],float score[],float areas[],int key){
  x1[key] = 0;
  y1[key] = 0;
  x2[key] = 1;
  y2[key] = 1;
  score[key] = -1;
  areas[key] = 100000;
}

void Detection::update(const detection &member, detection &means) {
  CHECK(member[0] == means[0]) << "class error" << member[0];
  means[1] = means[1] > member[1] ? member[1] : means[1];
  means[2] = means[2] > member[2] ? member[2] : means[2];
  means[3] = means[3] < member[3] ? member[3] : means[3];
  means[4] = means[4] < member[4] ? member[4] : means[4];
  means[5] = means[5] < member[5] ? member[5] : means[5];
}

void Detection::drawRec(const cv::Mat& img, const std::vector<detection> &nmsed_dets) {
	 LOG(INFO)<<"drawRec";
  std::vector<vector<float> > group;
  std::vector<detection> rects;
  const float thr = 100;
  for (int i = 0; i < nmsed_dets.size(); ++i) {
    detection temp = nmsed_dets[i];
    if (temp[5] <= 0.9){continue;}
    std::vector<float> rect;
    rect.push_back(i);
    rect.push_back(temp[0]);
    float x = (temp[1] + temp[3]) / 2;
    float y = (temp[2] + temp[4]) / 2;
    rect.push_back(x);
    rect.push_back(y);
    rect.push_back(0);
    group.push_back(rect);
    rect.clear();
  }
  for (int i = 0; i < group.size(); ++i) {
    std::vector<float> v;
    v = group[i];
    detection Rect = nmsed_dets[int(v[0])];
    if(v[4] == 0) {
      for (int j = i + 1; j < group.size(); ++j) {
        std::vector<float> vec;
        vec = group[j];
        if (vec[1] != v[1])continue;
        if (vec[4] != 0)continue;
        if (abs(v[2] - vec[2]) > thr)continue;
        if (abs(v[3] - vec[3]) > thr)continue;
        update(nmsed_dets[int(vec[0])], Rect);
        group[j][4] = 1;
      }
      
      rects.push_back(Rect);
      Rect.clear();
    }
  }
  for (int i = 0; i < rects.size(); ++i) {
    float pos_param_1;
    float pos_param_2;
	
	
	//LOG(INFO)<<"rects[1]:"<<rects[1];
	
    detection dects = rects[i];
    if (dects[5] <= 0.9) {continue;}
    CvPoint pt1 = cvPoint(dects[1] / scales, dects[2] / scales);
    CvPoint pt2 = cvPoint(dects[3] / scales, dects[4] / scales);
    
    LOG(INFO)<<"scales:"<<scales;
   
    LOG(INFO)<<"dects[1] :"<<dects[1];
    LOG(INFO)<<"dects[2] :"<<dects[2];
    LOG(INFO)<<"dects[3] :"<<dects[3];
    LOG(INFO)<<"dects[4] :"<<dects[4];
    LOG(INFO)<<"dects[1] / scales:"<<dects[1] / scales;
    LOG(INFO)<<"dects[2] / scales:"<<dects[2] / scales;
    LOG(INFO)<<"dects[3] / scales:"<<dects[3] / scales;
    LOG(INFO)<<"dects[4] / scales:"<<dects[4] / scales;
    
    
    //Point
   
    
    pos_param_1 = dects[1] / scales;
    pos_param_2 = dects[2] / scales;

    int font_face = cv::FONT_HERSHEY_SIMPLEX;
    int baseline;
    cv::Scalar color = {255,0,0};
    cv::rectangle(img, pt1, pt2, color); 
    
    //Test
    //cv::rectangle(img,Point(350,100),Point(400,300),Scalar(0,0,255),3,4,0);
    
    cv::rectangle(img,Point(dects[1] / scales, dects[2] / scales),Point(dects[3] / scales, dects[4] / scales),Scalar(0,0,255),1,1,0);
    
    //string text = CLASSES[int(dects[0])] + " " + std::to_string(dects[5]);
    string text = CLASSES[int(dects[0])];
    cv::Size text_size = cv::getTextSize(text, font_face, 0.5, 1, &baseline);

    if(pos_param_2 > img.rows)pos_param_2 = img.rows;
    if(pos_param_2 - text_size.height < 0)pos_param_2 = text_size.height;
    if(pos_param_1 + text_size.width > img.cols)pos_param_1 = img.cols - text_size.width;
    if(pos_param_1 < 0)pos_param_1 = 0;

    CvPoint title = cvPoint(pos_param_1, pos_param_2);
    cv::putText(img, text, title, font_face, 0.5, Scalar(0,0,255));//cv::FONT_HERSHEY_SIMPLEX
  }
}

void Detection::im_detect(const cv::Mat& img, float scores[], float boxes[]) {
	 LOG(INFO)<<"im_detect";
  Blob<float>* input_layer = net_->input_blobs()[0];
  input_layer->Reshape(1, num_channels_,
                       input_geometry_.height, 
                       input_geometry_.width);
  boost::shared_ptr<Blob<float> > im_info = net_->blob_by_name("im_info");
  im_info->mutable_cpu_data()[0] = 600;
  im_info->mutable_cpu_data()[1] = 800;
  im_info->mutable_cpu_data()[2] = scales;

  net_->Reshape();

  std::vector<cv::Mat> input_channels;
  WrapInputLayer(&input_channels);
  Preprocess(img, &input_channels);

  net_->Forward();

  Blob<float>* output_layer_box = net_->output_blobs()[0];
  Blob<float>* output_layer_score = net_->output_blobs()[1];
  boost::shared_ptr<Blob<float> > rois_blob = net_->blob_by_name("rois");

  float roisArr[1200];
  int j = 0;

  for (int i = 0; i < rois_blob->count(); ++i) {
    if (i % 5 == 0) {continue;}
    roisArr[j++] = rois_blob->cpu_data()[i];
  }

  bbox_transform_inv(roisArr, output_layer_box->cpu_data(), boxes);

  clip_boxes(boxes);

  copy(output_layer_score->cpu_data(), scores, 6300);
}

cv::Mat Detection::show(const cv::Mat &img) {
//	cv::Mat resultImg;

  im_shape[0] = img.rows;
  im_shape[1] = img.cols;
  im_shape[2] = img.channels();
  int im_size_min = img.rows < img.cols ? img.rows : img.cols;
  
  //scales = 600 / im_size_min;
  scales = 0.42;

  std::vector<detection> detections;
  float scores[6300];
  float boxes[2400];
  im_detect(img, scores, boxes);
  nms(boxes, scores, detections);
  drawRec(img, detections);
  LOG(INFO)<<"huangyaling detecte";
  
  //cv::rectangle(img,Point(250,100),Point(300,300),Scalar(0,0,255),3,4,0); 
  
  
  
  return img;
  //cv::imshow("test", img);
  //cvWaitKey(0);
}

}  // NOLINT(build/namespaces)




