package com.cambricon.productdisplay.bean;

public class Participle {

    private int code;
    private String message;
    private String[] words;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String[] getWords() {
        return words;
    }

    public void setWords(String[] words) {
        this.words = words;
    }

    @Override
    public String toString() {
        return "[" + code + "," + message + "," + words + "]";
    }
}
