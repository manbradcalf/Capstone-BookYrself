package com.bookyrself.bookyrself.data.serverModels.SearchResponseEvents;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import javax.annotation.Generated;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class _source {

    @SerializedName("citystate")
    private String mCitystate;
    @SerializedName("date")
    private String mDate;
    @SerializedName("eventname")
    private String mEventname;
    @SerializedName("host")
    private Host mHost;
    @SerializedName("picture")
    private String mPicture;
    @SerializedName("tags")
    private List<String> mTags;
    @SerializedName("users")
    private List<User> mUsers;

    public String getCitystate() {
        return mCitystate;
    }

    public void setCitystate(String citystate) {
        mCitystate = citystate;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public String getEventname() {
        return mEventname;
    }

    public void setEventname(String eventname) {
        mEventname = eventname;
    }

    public Host getHost() {
        return mHost;
    }

    public void setHost(Host host) {
        mHost = host;
    }

    public String getPicture() {
        return mPicture;
    }

    public void setPicture(String picture) {
        mPicture = picture;
    }

    public List<String> getTags() {
        return mTags;
    }

    public void setTags(List<String> tags) {
        mTags = tags;
    }

    public List<User> getUsers() {
        return mUsers;
    }

    public void setUsers(List<User> users) {
        mUsers = users;
    }

}
