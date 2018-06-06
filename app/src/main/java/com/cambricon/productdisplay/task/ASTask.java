package com.cambricon.productdisplay.task;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.huawei.hiai.vision.image.detector.AestheticsScoreDetector;
import com.huawei.hiai.vision.visionkit.common.Frame;
import com.huawei.hiai.vision.visionkit.image.detector.AestheticsScore;

import org.json.JSONObject;

/**
 * Created by xiaoxiao on 18-6-5.
 */

public class ASTask extends AsyncTask<Bitmap,Void,Float> {
    private static final String LOG_TAG = "aesthetics_score_demo";
    private AestheticsScoreListener listener;
    private long startTime;
    private long endTime;

    AestheticsScoreDetector aestheticsScoreDetector;
    public ASTask(AestheticsScoreListener listener) {
        this.listener = listener;
    }

    @Override
    protected Float doInBackground(Bitmap... bitmaps) {
        Log.i(LOG_TAG, "init AestheticsScoreDetector");
        aestheticsScoreDetector = new AestheticsScoreDetector((Context) listener);
        Log.i(LOG_TAG, "start to get score");
        startTime = System.currentTimeMillis();
        float result_score = getScore(bitmaps[0]);
        endTime = System.currentTimeMillis();
        Log.i(LOG_TAG, String.format("aesthetics detect whole time: %d ms", endTime - startTime));
        //release engine after detect finished
        aestheticsScoreDetector.release();
        return result_score;
    }

    @Override
    protected void onPostExecute(Float aFloat) {
        listener.onTaskCompleted(aFloat);
        super.onPostExecute(aFloat);
    }

    public  float getScore(Bitmap bitmap) {
        if (bitmap == null) {
            Log.e(LOG_TAG,"bitmap is null ");
            return -1;
        }
        Frame frame = new Frame();
        frame.setBitmap(bitmap);
        Log.d(LOG_TAG,"runVisionService " + "start get score");
        JSONObject jsonObject = aestheticsScoreDetector.detect(frame,null);
        Log.d(LOG_TAG,"json result is " + jsonObject.toString());
        AestheticsScore aestheticsScores = aestheticsScoreDetector.convertResult(jsonObject);
        if (null == aestheticsScores) {
            Log.e(LOG_TAG,"aestheticsScores is null ");
            return -1;
        }
        float score = aestheticsScores.getScore();
        return score;
    }
}
