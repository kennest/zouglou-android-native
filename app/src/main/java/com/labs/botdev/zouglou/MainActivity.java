package com.labs.botdev.zouglou;

import android.annotation.SuppressLint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.appizona.yehiahd.fastsave.FastSave;
import com.github.florent37.rxgps.RxGps;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsRequest;
import com.jetradarmobile.rxlocationsettings.RxLocationSettings;
import com.labs.botdev.zouglou.services.APIClient;
import com.labs.botdev.zouglou.services.APIService;
import com.labs.botdev.zouglou.services.models.Event;
import com.labs.botdev.zouglou.services.models.EventsResponse;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigation;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private MapView mapView;
    private MapboxNavigation navigation;
    private static String access_token = "pk.eyJ1IjoiYnVtYmxlYmVlNDciLCJhIjoiY2phdjA0Ym11MHFodjJ6bjAxbnF2NXdtayJ9.WW82rcFdL6_o4pVs1itgcQ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        navigation = new MapboxNavigation(this, access_token);
        Mapbox.getInstance(this, access_token);
        setContentView(R.layout.activity_main);
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        getLocation();

        loadEventsRx();

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                // One way to add a marker view
                mapboxMap.addMarker(new MarkerOptions()
                        .position(new LatLng(Double.parseDouble(FastSave.getInstance().getString("lat")), Double.parseDouble(FastSave.getInstance().getString("long"))))
                        .title("Moi")
                        .snippet("Ma position")
                );
                mapboxMap.addMarker(new MarkerOptions()
                        .position(new LatLng(41.885, -87.679))
                        .title("Chicago")
                        .snippet("Illinois")
                );
            }
        });

        Point origin = Point.fromLngLat(-77.03613, 38.90992);
        Point destination = Point.fromLngLat(-77.0365, 38.8977);

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

//        LocationEngine location = new LocationEngineProvider(this).obtainBestLocationEngineAvailable();
//        navigation.setLocationEngine(location);
    }

    private void getLocation() {
        LocationSettingsRequest locationSettingsRequest = new LocationSettingsRequest.Builder()
                .addLocationRequest(LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY))
                .build();
        RxLocationSettings.with(this).ensure(locationSettingsRequest)
                .subscribe(enabled -> {
                    Toast.makeText(getApplicationContext(), "GPS ENABLED", Toast.LENGTH_LONG).show();
                    startLocationService();
                });
    }

    @SuppressLint("CheckResult")
    protected void startLocationService() {
        new RxGps(this).locationLowPower()
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribe(location -> {
                    FastSave.getInstance().saveString("lat", String.valueOf(location.getLatitude()));
                    FastSave.getInstance().saveString("long", String.valueOf(location.getLongitude()));
                    Log.v(getLocalClassName(), "LONG:" + location.getLongitude() + "/" + "LAT:" + location.getLatitude());
                    addMarker(location.getLatitude(), location.getLongitude(), "My position", "Im here!");
                    //you've got the location
                }, throwable -> {
                    if (throwable instanceof RxGps.PermissionException) {
                        //the user does not allow the permission
                    } else if (throwable instanceof RxGps.PlayServicesNotAvailableException) {
                        //the user do not have play services
                    }
                });
    }

    private void addMarker(Double lat, Double lon, String title, String snippet) {
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                // One way to add a marker view
                mapboxMap.addMarker(new MarkerOptions()
                        .position(new LatLng(lat, lon))
                        .title(title)
                        .snippet(snippet)
                );
            }
        });
    }

    private void loadEventsRx() {
        Observer mObserver = new Observer<EventsResponse>() {

            @Override
            public void onSubscribe(Disposable disposable) {
                Toast.makeText(getApplicationContext(), "events load Init", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNext(EventsResponse response) {
                for (Event e : response.getEvents()) {
                    Toast.makeText(getApplicationContext(), "Event title: " + e.getTitle(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(Throwable e) {
                Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("OBS error: ", e.getMessage());
            }

            @Override
            public void onComplete() {
                Toast.makeText(getApplicationContext(), "Load finished total", Toast.LENGTH_LONG).show();
            }
        };

        APIService service = APIClient.getClient().create(APIService.class);
        Observable<EventsResponse> observable = service.getEventsList();
        observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(mObserver);
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
