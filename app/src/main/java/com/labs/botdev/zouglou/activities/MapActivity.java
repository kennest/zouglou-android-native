package com.labs.botdev.zouglou.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.appizona.yehiahd.fastsave.FastSave;
import com.bumptech.glide.Glide;
import com.github.florent37.rxgps.RxGps;
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsRequest;
import com.jetradarmobile.rxlocationsettings.RxLocationSettings;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.labs.botdev.zouglou.R;
import com.labs.botdev.zouglou.objectbox.Address;
import com.labs.botdev.zouglou.objectbox.Address_;
import com.labs.botdev.zouglou.objectbox.Artist;
import com.labs.botdev.zouglou.objectbox.Place;
import com.labs.botdev.zouglou.objectbox.Place_;
import com.labs.botdev.zouglou.services.APIClient;
import com.labs.botdev.zouglou.services.APIService;
import com.labs.botdev.zouglou.services.models.Event;
import com.labs.botdev.zouglou.services.models.EventsResponse;
import com.labs.botdev.zouglou.utils.AppController;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigation;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import java.util.List;

import io.objectbox.Box;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class MapActivity extends AppCompatActivity {
    private MapView mapView;
    private MapboxNavigation navigation;
    TextView title, snippet;
    ImageView picture;
    ViewGroup infowindow;
    FloatingActionButton menu;
    //private static String access_token = "pk.eyJ1IjoiYnVtYmxlYmVlNDciLCJhIjoiY2phdjA0Ym11MHFodjJ6bjAxbnF2NXdtayJ9.WW82rcFdL6_o4pVs1itgcQ";

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        checkPermissions();
        navigation = new MapboxNavigation(this, getResources().getString(R.string.mapbox_access_token));
        Mapbox.getInstance(this, getResources().getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_map);
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        infowindow = (ViewGroup) getLayoutInflater().inflate(R.layout.place_info_window, null);
        menu = findViewById(R.id.menu);

        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MapActivity.this, ListEventsActivity.class));
            }
        });

        getLocation();

        ReactiveNetwork.checkInternetConnectivity()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        if (aBoolean) {
                            loadEventsRx();
                        }else{
                            placeEventsMarker();
                        }
                    }
                });

    }

    private void getLocation() {
        LocationSettingsRequest locationSettingsRequest = new LocationSettingsRequest.Builder()
                .addLocationRequest(LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY))
                .build();
        RxLocationSettings.with(this).ensure(locationSettingsRequest)
                .subscribe(enabled -> {
                    if (enabled) {
                        Toast.makeText(getApplicationContext(), "GPS ENABLED", Toast.LENGTH_LONG).show();
                        startLocationService();
                    }
                });
    }

    @SuppressLint("CheckResult")
    protected void startLocationService() {
        new RxGps(MapActivity.this).locationLowPower()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(location -> {
                    FastSave.getInstance().saveString("lat", String.valueOf(location.getLatitude()));
                    FastSave.getInstance().saveString("long", String.valueOf(location.getLongitude()));
                    Timber.tag(getLocalClassName()).v("LONG:" + location.getLongitude() + "/" + "LAT:" + location.getLatitude());
                    Toast.makeText(getApplicationContext(), "LAT:" + location.getLatitude() + "/" + "LONG:" + location.getLongitude(), Toast.LENGTH_LONG).show();
                    addMarker(location.getLatitude(), location.getLongitude(), "My position", "Im here!", null);
                    //you've got the location
                }, throwable -> {
                    if (throwable instanceof RxGps.PermissionException) {
                        //the user does not allow the permission
                        Toast.makeText(getApplicationContext(), "Error" + throwable.getMessage(), Toast.LENGTH_LONG).show();
                    } else if (throwable instanceof RxGps.PlayServicesNotAvailableException) {
                        //the user do not have play services
                        Toast.makeText(getApplicationContext(), "Error" + throwable.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    protected void addMarker(Double lat, Double lon, String ftitle, String fsnippet, String fpicture) {
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {

                MarkerOptions markerOptions = new MarkerOptions()
                        .position(new LatLng(lat, lon))
                        .title(ftitle)
                        .setSnippet(fsnippet);
                mapboxMap.addMarker(markerOptions);

                mapboxMap.setInfoWindowAdapter(new MapboxMap.InfoWindowAdapter() {
                    @Nullable
                    @Override
                    public View getInfoWindow(@NonNull Marker marker) {

                        // The info window layout is created dynamically, parent is the info window
                        // container
                        RelativeLayout parent = new RelativeLayout(MapActivity.this);
                        parent = (RelativeLayout) infowindow;
                        picture = parent.findViewById(R.id.picture);
                        title = parent.findViewById(R.id.info_title);
                        snippet = parent.findViewById(R.id.snippet);
                        parent.setLayoutParams(new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        // parent.setOrientation(LinearLayout.VERTICAL);
                        parent.setBackgroundColor(Color.parseColor("#FFFFFF"));

                        // Depending on the marker title, the correct image source is used. If you
                        // have many markers using different images, extending Marker and
                        // baseMarkerOptions, adding additional options such as the image, might be
                        // a better choice.
                        //ImageView countryFlagImage = new ImageView(MapActivity.this);
                        title.setText(marker.getTitle());
                        snippet.setText(marker.getSnippet());

                        Log.e("url",fpicture);
                        Glide
                                .with(getApplicationContext())
                                .load(fpicture)
                                .into(picture);

                        return parent;
                    }
                });
            }
        });
    }

    protected void traceRoute(Point origin, Point destination) {
        //Point origin = Point.fromLngLat(-77.03613, 38.90992);
        //Point destination = Point.fromLngLat(-77.0365, 38.8977);

        NavigationRoute.builder(getApplicationContext())
                .accessToken(Mapbox.getAccessToken())
                .origin(origin)
                .destination(destination)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {

                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable t) {

                    }
                });

        LocationEngine location = new LocationEngineProvider(this).obtainBestLocationEngineAvailable();
        navigation.setLocationEngine(location);
    }

    private void loadEventsRx() {
        Box<com.labs.botdev.zouglou.objectbox.Event> eventBox = AppController.boxStore.boxFor(com.labs.botdev.zouglou.objectbox.Event.class);
        Box<Artist> artistBox = AppController.boxStore.boxFor(Artist.class);
        Box<Place> placeBox = AppController.boxStore.boxFor(Place.class);
        Box<Address> addressBox = AppController.boxStore.boxFor(Address.class);

        Observer mObserver = new Observer<EventsResponse>() {
            @Override
            public void onSubscribe(Disposable disposable) {
                eventBox.removeAll();
                placeBox.removeAll();
                addressBox.removeAll();
                artistBox.removeAll();
                //Toast.makeText(getApplicationContext(), getLocalClassName() + " Data load Init", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNext(EventsResponse response) {
                for (Event e : response.getEvents()) {
                    //Toast.makeText(getApplicationContext(), getLocalClassName() + " Place title: " + e.place.getTitle(), Toast.LENGTH_LONG).show();
                    com.labs.botdev.zouglou.objectbox.Event eb = new com.labs.botdev.zouglou.objectbox.Event();
                    Place place = new Place();
                    Address address = new Address();

                    eb.setRaw_id(e.getId());
                    eb.setTitle(e.getTitle());
                    eb.setDescription(e.getDescription());
                    eb.setBegin(e.getBegin());
                    eb.setEnd(e.getEnd());
                    eb.setPicture(e.getPicture());

                    place.setRaw_id(e.place.getId());
                    place.setPicture(e.place.getPicture());
                    place.setTitle(e.place.getTitle());
                    placeBox.put(place);

                    eb.setPlace_id(place.getRaw_id());

                    address.setLatitude(Double.parseDouble(e.place.address.getLatitude()));
                    address.setLongitude(Double.parseDouble(e.place.address.getLongitude()));
                    address.setRaw_id(e.place.address.getId());
                    address.setCommune(e.place.address.getCommune());
                    address.setQuartier(e.place.address.getQuartier());
                    address.setPlace_id(place.getRaw_id());
                    addressBox.put(address);

                    for(com.labs.botdev.zouglou.services.models.Artist a:e.getArtists()){
                        Artist artist=new Artist();
                        artist.setRaw_id(a.getId());
                        artist.setName(a.getName());
                        artist.setSample(a.getSample());
                        artist.setAvatar(a.getAvatar());
                        eb.artists.add(artist);
                    }

                    eventBox.put(eb);
                }
            }

            @Override
            public void onError(Throwable e) {
                Toast.makeText(getApplicationContext(), "Data load Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("Data load error: ", e.getMessage());
            }

            @Override
            public void onComplete() {
                //Toast.makeText(getApplicationContext(), getLocalClassName() + " Event total: " + eventBox.count(), Toast.LENGTH_LONG).show();
                //Toast.makeText(getApplicationContext(), getLocalClassName() + " Address total: " + addressBox.count(), Toast.LENGTH_LONG).show();
                //Toast.makeText(getApplicationContext(), getLocalClassName() + " Place total: " + placeBox.count(), Toast.LENGTH_LONG).show();
                //Toast.makeText(getApplicationContext(), getLocalClassName() + " Artist total: " + artistBox.count(), Toast.LENGTH_LONG).show();
                placeEventsMarker();
            }
        };

        APIService service = APIClient.getClient().create(APIService.class);
        Observable<EventsResponse> observable = service.getEventsList();
        observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(mObserver);
    }

    private void placeEventsMarker() {
        Box<com.labs.botdev.zouglou.objectbox.Event> eventBox = AppController.boxStore.boxFor(com.labs.botdev.zouglou.objectbox.Event.class);
        Box<Place> placeBox = AppController.boxStore.boxFor(Place.class);
        Box<Address> addressBox = AppController.boxStore.boxFor(Address.class);
        for (com.labs.botdev.zouglou.objectbox.Event e : eventBox.getAll()) {
            String upload_url="http://www.berakatravel.com/zouglou/public/uploads/";
            String picture_url=upload_url+e.getPicture();
            Place p = placeBox.query().equal(Place_.raw_id, e.getPlace_id()).build().findFirst();
                if (p != null) {
                    Address a = addressBox.query().equal(Address_.place_id, p.getRaw_id()).build().findFirst();
                    Toast.makeText(getApplicationContext(), "Place latitude: " + a.getLatitude(), Toast.LENGTH_LONG).show();
                    addMarker(a.getLatitude(), a.getLongitude(), e.getTitle()+"/"+p.getTitle(), e.getDescription(), picture_url);
                } else {
                    Toast.makeText(getApplicationContext(), "Place latitude: NULL", Toast.LENGTH_LONG).show();
                }
        }
    }

    private void checkPermissions(){
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
            @Override public void onPermissionsChecked(MultiplePermissionsReport report) {/* ... */}
            @Override public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {/* ... */}
        }).check();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        navigation.stopNavigation();
        navigation.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
