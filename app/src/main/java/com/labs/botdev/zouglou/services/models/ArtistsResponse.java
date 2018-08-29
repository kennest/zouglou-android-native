package com.labs.botdev.zouglou.services.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class ArtistsResponse{
    @SerializedName("artists")
    @Expose
    private ArrayList<Artist> artists;

    public ArrayList<Artist> getArtists() {
        return artists;
    }

    public void setArtists(ArrayList<Artist> artists) {
        this.artists = artists;
    }
}
