package com.cambricon.productdisplay.bean;

/**
 * 目标分类结果
 */

public class ClassificationImage {
    private String name;
    private String fps;
    private String time;
    private String result;

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

    public String toString(){
        return "["+name+";"+fps+";"+time+";"+result+"]";
    }
}
