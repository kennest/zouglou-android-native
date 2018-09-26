package com.labs.botdev.zouglou.services;

import com.google.gson.JsonObject;
import com.labs.botdev.zouglou.models.Customer;
import com.labs.botdev.zouglou.models.ArtistsResponse;
import com.labs.botdev.zouglou.models.CustomerResponse;
import com.labs.botdev.zouglou.models.EventsResponse;
import com.labs.botdev.zouglou.models.FavoriteArtist;
import com.labs.botdev.zouglou.models.FavoritePlace;
import com.labs.botdev.zouglou.models.PlacesResponse;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface APIService {

    @GET("api/activeevents")
    Observable<EventsResponse> getEventsList();

    @GET("api/inactiveevents")
    Observable<EventsResponse> getPassedEventsList();

    @GET("api/artists")
    Observable<ArtistsResponse> getArtistsList();

//    @GET("api/artist/{id}")
//    Observable<Artist> getArtist(@Path("id") String id);
//
//    @GET("api/places/{id}")
//    Observable<Place> getPlace(@Path("id") String id);

    @GET("api/places")
    Observable<PlacesResponse> getPlaces();

    @GET("api/placeshistory")
    Observable<PlacesResponse> getPlacesHistory();

    @GET("api/customer/{fb_id}")
    Call<Customer> getCustomerInfo(@Path("fb_id") String fb_id);

    @Headers({"Content-Type: application/json","Accept:application/json"})
    @POST("api/favoriteartist")
    Call<JsonObject> setFavoriteArtist(@Body FavoriteArtist request);

    @Headers({"Content-Type: application/json","Accept:application/json"})
    @POST("api/favoriteplace")
    Call<JsonObject> setFavoritePlace(@Body FavoritePlace request);

    @Headers({"Content-Type: application/json","Accept:application/json"})
    @POST("api/addcustomer")
    Call<JsonObject> addCustomer(@Body Customer request);

}
