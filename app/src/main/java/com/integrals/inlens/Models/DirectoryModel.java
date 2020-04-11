package com.integrals.inlens.Models;

public class DirectoryModel {

    private String dirPath;
    private boolean isSelected;

    public DirectoryModel(String dirPath, boolean isSelected) {
        this.dirPath = dirPath;
        this.isSelected = isSelected;
    }

    public String getDirPath() {
        return dirPath;
    }

    public void setDirPath(String dirPath) {
        this.dirPath = dirPath;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
