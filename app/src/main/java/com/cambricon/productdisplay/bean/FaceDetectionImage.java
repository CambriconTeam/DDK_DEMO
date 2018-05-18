package com.cambricon.productdisplay.bean;

/**
 * 人脸检测结果
 */

public class FaceDetectionImage {

    private String name;
    private String fps;
    private String time;

    public FaceDetectionImage() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFps() {
        return fps;
    }

    public void setFps(String fps) {
        this.fps = fps;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return name + ";" + fps + ";" + time;
    }
}
