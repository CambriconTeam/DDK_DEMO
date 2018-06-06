package com.cambricon.productdisplay.task;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.huawei.hiai.vision.image.detector.LabelDetector;
import com.huawei.hiai.vision.visionkit.common.Frame;
import com.huawei.hiai.vision.visionkit.image.detector.Label;

import org.json.JSONObject;

/**
 * Created by xiaoxiao on 18-6-1.
 */

public class LabelDetectTask extends AsyncTask<Bitmap,Void,Label>{
    private static final String LOG_TAG = "label_detect";
    private LabelDetectListener listener;
    private long startTime;
    private long endTime;

    LabelDetector labelDetector;


    public LabelDetectTask(LabelDetectListener listener) {
        this.listener = listener;
    }

    @Override
    protected Label doInBackground(Bitmap... bitmaps) {
        Log.i(LOG_TAG, "init LabelDetector");
        labelDetector = new LabelDetector((Context)listener);

        Log.i(LOG_TAG, "start to get label");
        startTime = System.currentTimeMillis();
        Label result_label = getLabel(bitmaps[0]);
        endTime = System.currentTimeMillis();
        Log.i(LOG_TAG, String.format("labeldetect whole time: %d ms", endTime - startTime));
        //release engine after detect finished
        labelDetector.release();
        return result_label;
    }


    @Override
    protected void onPostExecute(Label label) {
        listener.onTaskCompleted(label);
        super.onPostExecute(label);
    }

    private Label getLabel(Bitmap bitmap) {
        if (bitmap == null) {
            Log.e(LOG_TAG, "bitmap is null");
            return null;
        }
        Frame frame = new Frame();
        frame.setBitmap(bitmap);
        Log.d(LOG_TAG, "runVisionService " + "start get label");
        JSONObject jsonObject = labelDetector.detect(frame,null);
        Log.e(LOG_TAG,"JSONObject:"+jsonObject);
        Label label = labelDetector.convertResult(jsonObject);
        Log.e(LOG_TAG,"label"+label);

        if (null == label) {
            Log.e(LOG_TAG, "label is null ");
            return null;
        }
        return label;
    }
}






