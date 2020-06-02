package com.integrals.inlens.Models;

import android.support.annotation.NonNull;

import com.google.firebase.database.DatabaseReference;

public class CommunityModel implements Comparable<CommunityModel> {

    private String Title;
    private String Description;
    private String Status;
    private String StartTime;
    private String EndTime;
    private String Type;
    private String CoverImage;
    private String Admin;
    private String CommunityID;
    private boolean isReported;





    public CommunityModel() {
    }

    public CommunityModel(String title, String description, String status, String startTime, String endTime, String type, String coverImage, String admin, String communityID, boolean isReported) {
        Title = title;
        Description = description;
        Status = status;
        StartTime = startTime;
        EndTime = endTime;
        Type = type;
        CoverImage = coverImage;
        Admin = admin;
        CommunityID = communityID;
        this.isReported = isReported;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getStartTime() {
        return StartTime;
    }

    public void setStartTime(String startTime) {
        StartTime = startTime;
    }

    public String getEndTime() {
        return EndTime;
    }

    public void setEndTime(String endTime) {
        EndTime = endTime;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public String getCoverImage() {
        return CoverImage;
    }

    public void setCoverImage(String coverImage) {
        CoverImage = coverImage;
    }

    public String getAdmin() {
        return Admin;
    }

    public void setAdmin(String admin) {
        Admin = admin;
    }

    public String getCommunityID() {
        return CommunityID;
    }

    public void setCommunityID(String communityID) {
        CommunityID = communityID;
    }

    public boolean isReported() {
        return isReported;
    }

    public void setReported(boolean reported) {
        isReported = reported;
    }

    @Override
    public int compareTo(@NonNull CommunityModel communityModel) {
        return this.getCommunityID().compareTo(communityModel.getCommunityID());
    }
}
