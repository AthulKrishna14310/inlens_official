package com.integrals.inlens.Models;

public class PariticpantModel {

    private String MemberUID;
    private String MemberName;
    private String MemberIDinCommunity;

    public PariticpantModel(String memberUID, String memberName, String memberIDinCommunity) {
        MemberUID = memberUID;
        MemberName = memberName;
        MemberIDinCommunity = memberIDinCommunity;
    }

    public String getMemberUID() {
        return MemberUID;
    }

    public void setMemberUID(String memberUID) {
        MemberUID = memberUID;
    }

    public String getMemberName() {
        return MemberName;
    }

    public void setMemberName(String memberName) {
        MemberName = memberName;
    }

    public String getMemberIDinCommunity() {
        return MemberIDinCommunity;
    }

    public void setMemberIDinCommunity(String memberIDinCommunity) {
        MemberIDinCommunity = memberIDinCommunity;
    }
}
