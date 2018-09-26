package com.labs.botdev.zouglou.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class PlacesResponse {

    @SerializedName("places")
    @Expose
    public ArrayList<Place> places;

    public ArrayList<Place> getPlaces() {
        return places;
    }

    public void setPlaces(ArrayList<Place> places) {
        this.places = places;
    }
}
