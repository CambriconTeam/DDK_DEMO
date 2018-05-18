package com.cambricon.productdisplay.caffenative;

public class SingleNetDetection {

    static {
        System.loadLibrary("single_jni");
    }


    /**
     * 获取CPU处理每一层的时间，BenchMark Time
     *
     * @param model_file   deploy.prototxt的地址
     * @param trained_file caffemodel的地址
     * @return 时间数组
     */
    public static native double[] SingleNetTime(String model_file, String trained_file);


}
