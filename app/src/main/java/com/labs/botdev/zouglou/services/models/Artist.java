package com.labs.botdev.zouglou.services.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Artist {
    @SerializedName("events")
    @Expose
    public ArrayList<Event> events;
    @SerializedName("id")
    @Expose
    protected int id;
    @SerializedName("name")
    @Expose
    protected String name;
    @SerializedName("avatar")
    @Expose
    protected String avatar;
    @SerializedName("urlsample")
    @Expose
    protected String sample;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getSample() {
        return sample;
    }

    public void setSample(String sample) {
        this.sample = sample;
    }

    public ArrayList<Event> getEvents() {
        return events;
    }

    public void setEvents(ArrayList<Event> events) {
        this.events = events;
    }
}
