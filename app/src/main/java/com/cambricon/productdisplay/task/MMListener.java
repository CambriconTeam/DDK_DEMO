package com.cambricon.productdisplay.task;

import com.huawei.hiai.vision.visionkit.face.Face;

import java.util.List;

/**
 * Created by dell on 18-5-30.
 */

public interface MMListener {
    void onTaskCompleted(List<Face> faces);
}
