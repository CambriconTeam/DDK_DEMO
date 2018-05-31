package com.cambricon.productdisplay.task;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.huawei.hiai.vision.face.FaceDetector;
import com.huawei.hiai.vision.visionkit.common.Frame;

import org.json.JSONObject;

import java.util.List;

/**
 * Created by dell on 18-5-30.
 */

public class FaceDetectTask extends AsyncTask<Bitmap, Void, List> {
    private static final String LOG_TAG = "face_detect";
    private MMListener listener;
    private long startTime;
    private long endTime;
    public static int forwardTime=0;

    FaceDetector faceDetector;
    public FaceDetectTask(MMListener listener) {
        this.listener = listener;
    }

    @Override
    protected List doInBackground(Bitmap... bmp) {
        Log.i(LOG_TAG, "init FaceDetector");
        faceDetector = new FaceDetector((Context)listener);

        Log.i(LOG_TAG, "start to get face");
        startTime = System.currentTimeMillis();
        List result_face = getFace(bmp[0]);
        endTime = System.currentTimeMillis();
        forwardTime=(int)(endTime - startTime);
        Log.i(LOG_TAG, String.format("face detect whole time: %d ms", endTime - startTime));
        //release engine after detect finished
        faceDetector.release();
        return result_face;
    }

    @Override
    protected void onPostExecute(List result) {
        listener.onTaskCompleted(result);
        super.onPostExecute(result);
    }

    public  List getFace(Bitmap bitmap) {
        if (bitmap == null) {
            Log.e(LOG_TAG,"bitmap is null ");
            return null;
        }
        Frame frame = new Frame();
        frame.setBitmap(bitmap);
        Log.d(LOG_TAG,"runVisionService " + "start get face");
        JSONObject jsonObject = faceDetector.detect(frame,null);
        Log.d(LOG_TAG,"jsonObject " + jsonObject);
        List faces = faceDetector.convertResult(jsonObject);
        if (null == faces) {
            Log.e(LOG_TAG,"face is null ");
            return null;
        }
        return faces;
    }
}
