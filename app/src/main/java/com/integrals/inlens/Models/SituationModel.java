package com.integrals.inlens.Models;

public class SituationModel {

    private String Createdby;
    private String Time;
    private String Title;
    private String SituationID;

    public SituationModel() {
    }

    public SituationModel(String createdby, String time, String title, String situationID) {
        Createdby = createdby;
        Time = time;
        Title = title;
        SituationID = situationID;
    }

    public String getCreatedby() {
        return Createdby;
    }

    public void setCreatedby(String createdby) {
        Createdby = createdby;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getSituationID() {
        return SituationID;
    }

    public void setSituationID(String situationID) {
        SituationID = situationID;
    }
}
