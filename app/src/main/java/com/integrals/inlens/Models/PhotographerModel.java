package com.integrals.inlens.Models;

public class PhotographerModel {
    private String name,id,imgUrl;

    public PhotographerModel(String name, String id, String imgUrl) {
        this.name = name;
        this.id = id;
        this.imgUrl = imgUrl;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getImgUrl() {
        return imgUrl;
    }
}
