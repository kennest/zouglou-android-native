package com.labs.botdev.zouglou.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.fxn.stash.Stash;
import com.gmail.samehadar.iosdialog.IOSDialog;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsRequest;
import com.jetradarmobile.rxlocationsettings.RxLocationSettings;
import com.labs.botdev.zouglou.R;
import com.labs.botdev.zouglou.adapters.ListEventAdapter;
import com.labs.botdev.zouglou.services.APIClient;
import com.labs.botdev.zouglou.services.APIService;
import com.labs.botdev.zouglou.services.PusherEventService;
import com.labs.botdev.zouglou.services.TrackGPS;
import com.labs.botdev.zouglou.services.models.Artist;
import com.labs.botdev.zouglou.services.models.ArtistsResponse;
import com.labs.botdev.zouglou.services.models.Event;
import com.labs.botdev.zouglou.services.models.EventsResponse;
import com.labs.botdev.zouglou.services.models.PlacesResponse;
import com.labs.botdev.zouglou.utils.Constants;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.MarkerView;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode;
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import rx.functions.Action1;

public class MapActivity extends AppCompatActivity {
    TextView title, snippet, detail_sheet_title;
    ImageView picture;
    FloatingActionButton menu;
    double longitude;
    double latitude;
    IOSDialog dialog;
    List<Event> events = new ArrayList<>();
    List<Event> tmpEvents = new ArrayList<>();
    private MapView mapView;
    private MapboxNavigation navigation;
    private TrackGPS gps;
    View bottomsheet;
    private BottomSheetBehavior mbottomSheetBehavior;
    private LocationLayerPlugin locationPlugin;
    SearchView searchView;
    ListEventAdapter adapter;

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        startPusherServiceEvent();
        navigation = new MapboxNavigation(this, getResources().getString(R.string.mapbox_access_token));
        Mapbox.getInstance(this, getResources().getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_map);
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        searchView=findViewById(R.id.searchview);
        dialog = LoaderProgress("Un instant", "Nous chargons les données");
        bottomsheet = findViewById(R.id.details_sheet);
        mbottomSheetBehavior = BottomSheetBehavior.from(bottomsheet);
        searchView.setQueryHint("Nom artiste,maquis...");
        searchView.setIconified(true);

        mbottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_EXPANDED:
                        menu.setVisibility(View.GONE);
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        menu.setVisibility(View.GONE);
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        menu.setVisibility(View.VISIBLE);
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        menu.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        menu = findViewById(R.id.menu);
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MapActivity.this, ListEventsActivity.class));
            }
        });

        if (Constants.isNetworkConnected(getApplicationContext())) {
            RemoteSyncData();
        } else {
            OfflineSyncData();
        }

        //events = FastSave.getInstance().getObjectsList("events", Event.class);


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            //Rechercher sur la carte(Not finished yet!!)
            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText.matches("")){
                    Stash.put("events", tmpEvents);
                    clearMap();
                    placeEventsMarker();
                }else {
                    if (adapter != null) {
                        adapter.getFilter().filter(newText);
                        adapter.notifyDataSetChanged();
                        events = Stash.getArrayList("filter_events", Event.class);
                        Stash.put("events", events);
                        clearMap();
                        placeEventsMarker();
                    }
                }
                return false;
            }
        });

        searchView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    Stash.put("events", tmpEvents);
                    clearMap();
                    placeEventsMarker();
                }
            }
        });

        //getLocation();
        ensureLocationSettings();
        //facebookLogin();
    }

    //Check if Lacation is enabled and launch teask
    private void ensureLocationSettings() {
        LocationSettingsRequest locationSettingsRequest = new LocationSettingsRequest.Builder()
                .addLocationRequest(LocationRequest.create().setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY))
                .build();
        RxLocationSettings.with(MapActivity.this).ensure(locationSettingsRequest)
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean enabled) {
                        Toast.makeText(MapActivity.this, enabled ? "Enabled" : "Failed", Toast.LENGTH_LONG).show();
                        if (enabled) {
                            startLocationService();
                        }
                    }
                });
    }

    @SuppressLint("CheckResult")
    protected void startLocationService() {
        gps = new TrackGPS(MapActivity.this);

        if (gps.canGetLocation()) {
            longitude = gps.getLongitude();
            latitude = gps.getLatitude();

            MarkerOptions markerOptions = new MarkerOptions();
            LatLng me = new LatLng();
            IconFactory iconFactory = IconFactory.getInstance(MapActivity.this);
            Icon icon = iconFactory.fromResource(R.drawable.ic_account_location_black_48dp);

            me.setLatitude(latitude);
            me.setLongitude(longitude);
            Stash.put("my_position", me);
            markerOptions.setPosition(me);
            markerOptions.setSnippet("Je suis Ici");
            markerOptions.setTitle(String.valueOf(0));
            markerOptions.setIcon(icon);

            PlaceMe();

            //Toast.makeText(getApplicationContext(), "Longitude:" + Double.toString(longitude) + "\nLatitude:" + Double.toString(latitude), Toast.LENGTH_SHORT).show();
        } else {
            gps.showSettingsAlert();
        }
    }

    protected void PlaceMe() {
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                locationPlugin = new LocationLayerPlugin(mapView, mapboxMap);
                locationPlugin.setRenderMode(RenderMode.COMPASS);
                getLifecycle().addObserver(locationPlugin);
                //mapboxMap.addMarker(markerOptions);
            }
        });
    }

    private void clearMap(){
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                mapboxMap.deselectMarkers();
                mapboxMap.clear();
            }
        });
    }

    private void startPusherServiceEvent(){
        Intent intent = new Intent(getApplicationContext(), PusherEventService.class);
        // manager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,calendar.getTimeInMillis(),manager.INTERVAL_HALF_HOUR,intent);
        stopService(intent);
        startService(intent);
    }

    protected void addMarker(Double lat, Double lon, int id, String fsnippet) {
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(new LatLng(lat, lon))
                        .setTitle(String.valueOf(id))
                        .setSnippet(fsnippet);
                mapboxMap.addMarker(markerOptions);
                //mapboxMap.clear();

                mapboxMap.getUiSettings().setZoomControlsEnabled(true);
                mapboxMap.getUiSettings().setZoomGesturesEnabled(true);
                mapboxMap.getUiSettings().setCompassEnabled(true);
                mapboxMap.getUiSettings().setScrollGesturesEnabled(true);

                mapboxMap.setOnMarkerClickListener(new MapboxMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(@NonNull Marker marker) {
                        if (Integer.parseInt(marker.getTitle()) == 0) {
                            Toast.makeText(MapActivity.this, marker.getSnippet(), Toast.LENGTH_LONG).show();
                            return false;
                        } else {
                            showDetails(Integer.parseInt(marker.getTitle()));
                            if (mbottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                                mbottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                            } else {
                                mbottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                                mbottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                            }
                            return true;
                        }
                    }

                });
            }
        });
    }

    public void showDetails(int id) {
        String base_url = Constants.UPLOAD_URL;
        LinearLayout view = findViewById(R.id.bottom_sheet);
        com.labs.botdev.zouglou.services.models.Event e = new Event();

        for (Event n : events) {
            if (n.getId() == id) {
                e = n;
            }
        }

        ImageView picture = view.findViewById(R.id.picture);
        TextView title = view.findViewById(R.id.title);
        TextView artists = view.findViewById(R.id.artists);
        TextView place = view.findViewById(R.id.place);
        AppCompatButton show_details = view.findViewById(R.id.showDetails);
        AppCompatButton show_navigation = view.findViewById(R.id.showNavigation);
        //TextView date=view.findViewById(R.id.date);
        show_details.setTag(e.getId());

        view.setTag(e);
        String url = base_url + e.getPicture();
        Glide
                .with(getApplicationContext())
                .load(url)
                .into(picture);

        title.setText(e.getTitle());
        place.setText(Objects.requireNonNull(e.place.getTitle()));

        String artist_str = "";
        for (Artist a : e.artists) {
            artist_str = a.getName() + ",";
        }

        artists.setText(artist_str);

        show_details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent details = new Intent(MapActivity.this, DetailsEventActivity.class);
                details.putExtra("event_id", (int) v.getTag());
                startActivity(details);
            }
        });

        Event finalE = e;
        show_navigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowOnMap(Double.parseDouble(finalE.place.address.getLatitude()), Double.parseDouble(finalE.place.address.getLongitude()));
            }
        });
    }

    protected void ShowOnMap(Double lat, Double lon) {
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + lat + "," + lon + "");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Intent navigation = new Intent(MapActivity.this, DoNavigationActivity.class);
            String destination = lat + ":" + lon;
            navigation.putExtra("destination", destination);
            startActivity(navigation);
        }
    }

    private void RemoteSyncData() {
        loadEventsRx();
    }

    private void OfflineSyncData() {
        placeEventsMarker();
    }

    private void loadEventsRx() {
        Observer mObserver = new Observer<EventsResponse>() {
            @Override
            public void onSubscribe(Disposable disposable) {
                dialog.show();
                //Toast.makeText(getApplicationContext(), getLocalClassName() + " Data load Init", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNext(EventsResponse response) {
                Stash.put("events",response.getEvents());
                //FastSave.getInstance().saveObjectsList("events", response.getEvents());
            }

            @Override
            public void onError(Throwable e) {
                Toast.makeText(getApplicationContext(), "Data load Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                dialog.dismiss();
                Log.e("Data load error: ", e.getMessage());
            }

            @Override
            public void onComplete() {
                loadArtistsRx();
                if (Stash.getArrayList("events",Event.class) != null)
                    events = Stash.getArrayList("events", Event.class);
                    tmpEvents = Stash.getArrayList("events", Event.class);
                    adapter=new ListEventAdapter(events,MapActivity.this);
                    placeEventsMarker();
                    dialog.dismiss();
            }
        };

        APIService service = APIClient.getClient().create(APIService.class);
        Observable<EventsResponse> observable = service.getEventsList();
        observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(mObserver);
    }

    private void loadArtistsRx() {

        Observer mObserver = new Observer<ArtistsResponse>() {
            @Override
            public void onSubscribe(Disposable disposable) {
                //Toast.makeText(getApplicationContext(), getLocalClassName() + " Data load Init", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNext(ArtistsResponse response) {
                Stash.put("artists",response.getArtists());
                //FastSave.getInstance().saveObjectsList("artists", response.getArtists());
            }

            @Override
            public void onError(Throwable e) {
                Toast.makeText(getApplicationContext(), "Data load Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("Data load error: ", e.getMessage());
            }

            @Override
            public void onComplete() {
                loadPlaceRx();
            }
        };
        APIService service = APIClient.getClient().create(APIService.class);
        Observable<ArtistsResponse> observable = service.getArtistsList();
        observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(mObserver);
    }

    private void loadPlaceRx() {
        Observer mObserver = new Observer<PlacesResponse>() {
            @Override
            public void onSubscribe(Disposable disposable) {

            }

            @Override
            public void onNext(PlacesResponse response) {
                Stash.put("places",response.getPlaces());
                //FastSave.getInstance().saveObjectsList("places", response.getPlaces());
            }

            @Override
            public void onError(Throwable e) {
                Toast.makeText(getApplicationContext(), "Data load Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("Data load error: ", e.getMessage());
            }

            @Override
            public void onComplete() {

            }
        };

        APIService service = APIClient.getClient().create(APIService.class);
        Observable<PlacesResponse> observable = service.getPlaces();
        observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(mObserver);
    }

    private void placeEventsMarker() {
        //List<Event> events = FastSave.getInstance().getObjectsList("events", Event.class);
        List<Event> events = Stash.getArrayList("events", Event.class);
        for (Event e : events) {
            addMarker(Double.parseDouble(e.place.address.getLatitude()), Double.parseDouble(e.place.address.getLongitude()), e.getId(), e.getDescription());
        }
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

    public IOSDialog LoaderProgress(String title, String content) {
        final IOSDialog dialog = new IOSDialog.Builder(MapActivity.this)
                .setTitle(title)
                .setMessageContent(content)
                .setSpinnerColorRes(R.color.colorPrimary)
                .setCancelable(false)
                .setTitleColorRes(R.color.white)
                .setMessageContentGravity(Gravity.END)
                .build();
        return dialog;
    }
}
