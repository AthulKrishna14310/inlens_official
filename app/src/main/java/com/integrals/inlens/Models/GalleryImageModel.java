package com.integrals.inlens.Models;

public class GalleryImageModel {

    private String ImageUri;
    private boolean isSelected,isQueued;
    private String CreatedTime;

    public GalleryImageModel(String imageUri, boolean isSelected, boolean isQueued, String createdTime) {
        ImageUri = imageUri;
        this.isSelected = isSelected;
        this.isQueued = isQueued;
        CreatedTime = createdTime;
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

    public boolean isQueued() {
        return isQueued;
    }

    public void setQueued(boolean queued) {
        isQueued = queued;
    }
    public String getCreatedTime() {
        return CreatedTime;
    }

    public void setCreatedTime(String createdTime) {
        CreatedTime = createdTime;
    }
}
