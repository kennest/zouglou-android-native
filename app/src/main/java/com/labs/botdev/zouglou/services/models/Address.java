package com.labs.botdev.zouglou.services.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Address {
    @SerializedName("id")
    @Expose
    private int id;

    @SerializedName("commune")
    @Expose
    private String commune;

    @SerializedName("quartier")
    @Expose
    private String quartier;

    @SerializedName("lat")
    @Expose
    private String latitude;

    @SerializedName("long")
    @Expose
    private String longitude;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCommune() {
        return commune;
    }

    public void setCommune(String commune) {
        this.commune = commune;
    }

    public String getQuartier() {
        return quartier;
    }

    public void setQuartier(String quartier) {
        this.quartier = quartier;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
