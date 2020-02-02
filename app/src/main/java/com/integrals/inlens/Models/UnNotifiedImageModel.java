package com.integrals.inlens.Models;

public class UnNotifiedImageModel {
    private String Uri;
    private String CreatedTime;


    public UnNotifiedImageModel(String uri, String createdTime) {
        Uri = uri;
        CreatedTime = createdTime;
    }

    public String getUri() {
        return Uri;
    }

    public String getCreatedTime() {
        return CreatedTime;
    }

    public void setUri(String uri) {
        Uri = uri;
    }

    public void setCreatedTime(String createdTime) {
        CreatedTime = createdTime;
    }
}
