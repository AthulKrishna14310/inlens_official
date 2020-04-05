package com.integrals.inlens.Notification;

public class InlensImageModel {

    private int count;
    private String uri;
    private String lastModified;

    public InlensImageModel(int count, String uri, String lastModified) {
        this.count = count;
        this.uri = uri;
        this.lastModified = lastModified;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }
}
