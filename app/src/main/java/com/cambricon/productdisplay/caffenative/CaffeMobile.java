package com.cambricon.productdisplay.caffenative;

import java.nio.charset.StandardCharsets;

public class CaffeMobile {
    static {
        System.loadLibrary("caffe_jni");
    }
    private static byte[] stringToBytes(String s) {
        return s.getBytes(StandardCharsets.US_ASCII);
    }

    public native void setNumThreads(int numThreads);

    //public native int loadModel(String modelPath, String weightsPath,String label,boolean mode);  // required
    public native int loadModel(String modelPath, String weightsPath,boolean mode);  // required

    private native void setMeanWithMeanFile(String meanFile);

    private native void setMeanWithMeanValues(float[] meanValues);

    public native void setScale(float scale);

    public native int[] predictImage(byte[] data, int width, int height, int k);

    public int[] predictImage(String imgPath, int k) {
        return predictImage(stringToBytes(imgPath), 0, 0, k);
    }

    public int[] predictImage(String imgPath) {
        return predictImage(imgPath, 3);
    }

    public void setMean(float[] meanValues) {
        setMeanWithMeanValues(meanValues);
    }

    public void setMean(String meanFile) {
        setMeanWithMeanFile(meanFile);
    }
}
