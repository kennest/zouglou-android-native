package com.labs.botdev.zouglou.services.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Place {

    @SerializedName("address")
    @Expose
    public Address address;

    @SerializedName("events")
    @Expose
    public ArrayList<Event> events;

    @SerializedName("id")
    @Expose
    protected int id;

    @SerializedName("title")
    @Expose
    protected String title;

    @SerializedName("picture")
    @Expose
    protected String picture;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public ArrayList<Event> getEvents() {
        return events;
    }

    public void setEvents(ArrayList<Event> events) {
        this.events = events;
    }
}
