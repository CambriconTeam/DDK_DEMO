package com.cambricon.productdisplay.bean;

public class SpeechItem {

    private String resourse;
    private String translate;
    private String voiceUrl;

    public SpeechItem(){

    }
    public SpeechItem(String resourse, String translate){
        this.resourse = resourse;
        this.translate = translate;
    }

    public String getResourse() {
        return resourse;
    }

    public void setResourse(String resourse) {
        this.resourse = resourse;
    }

    public String getTranslate() {
        return translate;
    }

    public void setTranslate(String translate) {
        this.translate = translate;
    }

    public String getVoiceUrl() {
        return voiceUrl;
    }

    public void setVoiceUrl(String voiceUrl) {
        this.voiceUrl = voiceUrl;
    }
}
