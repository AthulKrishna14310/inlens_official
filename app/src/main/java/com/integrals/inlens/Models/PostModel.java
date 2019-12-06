package com.integrals.inlens.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class PostModel implements Parcelable {

    private String PoskKey;
    private String Uri;
    private String Time;
    private String PostBy;

    public PostModel(String poskKey, String uri, String time, String postBy) {
        PoskKey = poskKey;
        Uri = uri;
        Time = time;
        PostBy = postBy;
    }

    protected PostModel(Parcel in) {
        PoskKey = in.readString();
        Uri = in.readString();
        Time = in.readString();
        PostBy = in.readString();
    }

    public static final Creator<PostModel> CREATOR = new Creator<PostModel>() {
        @Override
        public PostModel createFromParcel(Parcel in) {
            return new PostModel(in);
        }

        @Override
        public PostModel[] newArray(int size) {
            return new PostModel[size];
        }
    };

    public String getPoskKey() {
        return PoskKey;
    }

    public void setPoskKey(String poskKey) {
        PoskKey = poskKey;
    }

    public String getUri() {
        return Uri;
    }

    public void setUri(String uri) {
        Uri = uri;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }

    public String getPostBy() {
        return PostBy;
    }

    public void setPostBy(String postBy) {
        PostBy = postBy;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

        parcel.writeString(PoskKey);
        parcel.writeString(Uri);
        parcel.writeString(Time);
        parcel.writeString(PostBy);

    }

}
