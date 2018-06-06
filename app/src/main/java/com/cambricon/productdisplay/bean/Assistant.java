package com.cambricon.productdisplay.bean;

public class Assistant {

    private int code;
    private String message;
    private Intention[] intentions;

    public class Intention{
        private String name;
        private int confidence;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getConfidence() {
            return confidence;
        }

        public void setConfidence(int confidence) {
            this.confidence = confidence;
        }
    }

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

    public Intention[] getIntentions() {
        return intentions;
    }

    public void setIntentions(Intention[] intentions) {
        this.intentions = intentions;
    }
}


