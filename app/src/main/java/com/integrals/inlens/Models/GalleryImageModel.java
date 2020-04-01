package com.integrals.inlens.Models;

public class GalleryImageModel {

    private String ImageUri;
    private boolean isSelected,isQueued;
    private String CreatedTime;

    public GalleryImageModel(String imageUri, boolean isSelected, String createdTime) {
        ImageUri = imageUri;
        this.isSelected = isSelected;
        CreatedTime = createdTime;
        isQueued=false;
    }

    public String getImageUri() {
        return ImageUri;
    }

    public void setImageUri(String imageUri) {
        ImageUri = imageUri;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getCreatedTime() {
        return CreatedTime;
    }

    public void setCreatedTime(String createdTime) {
        CreatedTime = createdTime;
    }

    public boolean isQueued() {
        return isQueued;
    }

    public void setQueued(boolean queued) {
        isQueued = queued;
    }
}
