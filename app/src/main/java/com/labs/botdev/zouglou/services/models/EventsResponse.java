package com.labs.botdev.zouglou.services.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class EventsResponse {
    @SerializedName("events")
    @Expose
    public ArrayList<Event> events;

    @SerializedName("artists")
    @Expose
    public ArrayList<Artist> artists;

    @SerializedName("places")
    @Expose
    public Place place;

    public ArrayList<Event> getEvents() {
        return events;
    }

    public void setEvents(ArrayList<Event> events) {
        this.events = events;
    }

    public ArrayList<Artist> getArtists() {
        return artists;
    }

    public void setArtists(ArrayList<Artist> artists) {
        this.artists = artists;
    }

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
    }
}
