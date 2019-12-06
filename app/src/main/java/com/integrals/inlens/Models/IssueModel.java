package com.integrals.inlens.Models;

public class IssueModel {

    private String Title;
    private String Desc;
    private String Status;
    private String By;

    public IssueModel(String title, String desc, String status, String by) {
        Title = title;
        Desc = desc;
        Status = status;
        By = by;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getDesc() {
        return Desc;
    }

    public void setDesc(String desc) {
        Desc = desc;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getBy() {
        return By;
    }

    public void setBy(String by) {
        By = by;
    }
}
