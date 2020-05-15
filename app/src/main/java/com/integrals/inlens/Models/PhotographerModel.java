package com.integrals.inlens.Models;

public class PhotographerModel {
    private String name,id,imgUrl,email;

    public PhotographerModel(String name, String id, String imgUrl, String email) {
        this.name = name;
        this.id = id;
        this.imgUrl = imgUrl;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
