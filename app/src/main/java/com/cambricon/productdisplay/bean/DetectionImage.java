package com.cambricon.productdisplay.bean;

/**
 * 目标检测结果
 */

public class DetectionImage {
    private  String name;
    private  String fps;
    private  String time;
    private  String result;
    private  String netType;

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

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getNetType() {
        return netType;
    }

    public void setNetType(String netType) {
        this.netType = netType;
    }

    @Override
    public String toString() {
        return name+";"+fps+";"+time+";"+netType;
    }
}
