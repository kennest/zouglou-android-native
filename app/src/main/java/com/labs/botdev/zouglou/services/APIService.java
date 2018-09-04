package com.labs.botdev.zouglou.services;

import com.labs.botdev.zouglou.services.models.Artist;
import com.labs.botdev.zouglou.services.models.ArtistsResponse;
import com.labs.botdev.zouglou.services.models.EventsResponse;
import com.labs.botdev.zouglou.services.models.Place;
import com.labs.botdev.zouglou.services.models.PlacesResponse;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface APIService {

    @GET("api/activeevents")
    Observable<EventsResponse> getEventsList();

    @GET("api/artists")
    Observable<ArtistsResponse> getArtistsList();

    @GET("api/artist/{id}")
    Observable<Artist> getArtist(@Path("id") String id);

    @GET("api/places/{id}")
    Observable<Place> getPlace(@Path("id") String id);

    @GET("api/places")
    Observable<PlacesResponse> getPlaces();

    @GET("api/placeshistory")
    Observable<PlacesResponse> getPlacesHistory();

}
