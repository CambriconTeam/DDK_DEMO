package com.cambricon.productdisplay.bean;

/**
 * 资讯类
 */

public class News {

    //编号
    private int id;
    //图片编号
    private int imageNum;
    //资讯标题
    private String title;
    //资讯日期
    private String date;
    //资讯详情路径
    private String url;

    public News() {
    }

    public News(int id, int imageNum, String title, String date, String url) {
        this.id = id;
        this.imageNum = imageNum;
        this.title = title;
        this.date = date;
        this.url = url;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getImageNum() {
        return imageNum;
    }

    public void setImageNum(int imageNum) {
        this.imageNum = imageNum;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
