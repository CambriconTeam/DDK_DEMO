package com.cambricon.productdisplay.task;

import com.huawei.hiai.vision.visionkit.image.detector.Label;

/**
 * Created by xiaoxiao on 18-6-4.
 */

public interface LabelDetectListener {
    void onTaskCompleted(Label label);
}
