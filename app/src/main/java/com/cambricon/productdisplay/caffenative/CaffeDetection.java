package com.cambricon.productdisplay.caffenative;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Created by dell on 18-2-3.
 */

public class CaffeDetection implements Serializable{
    static {
        System.loadLibrary("detecte_jni");
    }
    private static byte[] stringToBytes(String s) {
        return s.getBytes(StandardCharsets.US_ASCII);
    }

    public native void setNumThreads(int numThreads);

    public native void enableLog(boolean enabled);  // currently nonfunctional

    public native int loadModel(String modelPath, String weightsPath, boolean mode);  // required

    private native void setMeanWithMeanFile(String meanFile);

    private native void setMeanWithMeanValues(float[] meanValues);

    public native void setScale(float scale);
    //huangyaling
    public native byte[] detectImage(byte[] data,int width,int height);
    public byte[] detectImage(String imgPath){
        return detectImage(stringToBytes(imgPath),0,0);
    }
    public native int getWidth();
    public native int getHeight();
    //huangyaling

    public void setMean(float[] meanValues) {
        setMeanWithMeanValues(meanValues);
    }

    public void setMean(String meanFile) {
        setMeanWithMeanFile(meanFile);
    }

    public native int[] grayPoc(int[] pixels,int w,int h);
}
