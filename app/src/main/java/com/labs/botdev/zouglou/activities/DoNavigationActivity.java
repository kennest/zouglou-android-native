package com.labs.botdev.zouglou.activities;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.view.View;

import com.fxn.stash.Stash;
import com.labs.botdev.zouglou.R;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.services.android.navigation.ui.v5.NavigationView;
import com.mapbox.services.android.navigation.ui.v5.NavigationViewOptions;
import com.mapbox.services.android.navigation.ui.v5.OnNavigationReadyCallback;
import com.mapbox.services.android.navigation.ui.v5.listeners.InstructionListListener;
import com.mapbox.services.android.navigation.ui.v5.listeners.NavigationListener;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.mapbox.services.android.navigation.v5.routeprogress.ProgressChangeListener;
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// classes needed to initialize map
// classes needed to add location layer
// classes to calculate a route
// classes needed to launch navigation UI

public class DoNavigationActivity extends AppCompatActivity implements OnNavigationReadyCallback,
        NavigationListener, ProgressChangeListener, InstructionListListener {

    // variables for calculating and drawing a route
    private static final String TAG = "DirectionsActivity";
    private NavigationView navigationView;
    private boolean bottomSheetVisible = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_AppCompat_Light_NoActionBar);
        Mapbox.getInstance(this, getResources().getString(R.string.mapbox_access_token));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigation_activity);
        navigationView = findViewById(R.id.navigationView);
        navigationView.onCreate(savedInstanceState);
        navigationView.initialize(this);

        String extra = getIntent().getStringExtra("destination");
        String[] point = extra.split(":");
        LatLng store_destination = new LatLng(Double.parseDouble(point[0]), Double.parseDouble(point[1]));

        LatLng me = (LatLng) Stash.getObject("my_position", LatLng.class);

        Point origin = Point.fromLngLat(me.getLongitude(), me.getLatitude());
        Point destination = Point.fromLngLat(store_destination.getLongitude(), store_destination.getLatitude());
        fetchRoutePoint(origin, destination);
    }

    private void fetchRoutePoint(Point ORIGIN, Point DESTINATION) {
        NavigationRoute.builder(this)
                .accessToken(Mapbox.getAccessToken())
                .origin(ORIGIN)
                .destination(DESTINATION)
                .language(Locale.FRANCE)
                .alternatives(true)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        DirectionsRoute directionsRoute = response.body().routes().get(0);
                        startNavigation(directionsRoute);
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable t) {

                    }
                });
    }

    private void setBottomSheetCallback(NavigationViewOptions.Builder options) {
        options.bottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        bottomSheetVisible = false;
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        bottomSheetVisible = true;
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        if (!bottomSheetVisible) {
// View needs to be anchored to the bottom sheet before it is finished expanding
// because of the animation
                        }
                        break;
                    default:
                        return;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });
    }

    private void startNavigation(DirectionsRoute directionsRoute) {
        NavigationViewOptions.Builder options =
                NavigationViewOptions.builder()
                        .directionsRoute(directionsRoute)
                        .shouldSimulateRoute(true);
        setBottomSheetCallback(options);
        navigationView.startNavigation(options.build());
    }


    @Override
    @SuppressWarnings({"MissingPermission"})
    protected void onStart() {
        super.onStart();
        navigationView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        navigationView.onResume();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        navigationView.onLowMemory();
    }

    @Override
    public void onBackPressed() {
// If the navigation view didn't need to do anything, call super
        if (!navigationView.onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        navigationView.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        navigationView.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
        navigationView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        navigationView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        navigationView.onDestroy();
    }


    @Override
    public void onNavigationReady(boolean isRunning) {

    }

    @Override
    public void onInstructionListVisibilityChanged(boolean visible) {

    }

    @Override
    public void onCancelNavigation() {

    }

    @Override
    public void onNavigationFinished() {

    }

    @Override
    public void onNavigationRunning() {

    }

    @Override
    public void onProgressChange(Location location, RouteProgress routeProgress) {
        setSpeed(location);
    }

    private void setSpeed(Location location) {
        String string = String.format("%d\nMPH", (int) (location.getSpeed() * 2.2369));
        int mphTextSize = getResources().getDimensionPixelSize(R.dimen.mph_text_size);
        int speedTextSize = getResources().getDimensionPixelSize(R.dimen.speed_text_size);

        SpannableString spannableString = new SpannableString(string);
        spannableString.setSpan(new AbsoluteSizeSpan(mphTextSize),
                string.length() - 4, string.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        spannableString.setSpan(new AbsoluteSizeSpan(speedTextSize),
                0, string.length() - 3, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
    }
}

