package com.labs.botdev.zouglou.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.fxn.stash.Stash;
import com.gmail.samehadar.iosdialog.IOSDialog;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsRequest;
import com.jetradarmobile.rxlocationsettings.RxLocationSettings;
import com.labs.botdev.zouglou.R;
import com.labs.botdev.zouglou.adapters.ListEventAdapter;
import com.labs.botdev.zouglou.services.APIClient;
import com.labs.botdev.zouglou.services.APIService;
import com.labs.botdev.zouglou.services.PusherEventService;
import com.labs.botdev.zouglou.services.TrackGPS;
import com.labs.botdev.zouglou.models.Artist;
import com.labs.botdev.zouglou.models.ArtistsResponse;
import com.labs.botdev.zouglou.models.Event;
import com.labs.botdev.zouglou.models.EventsResponse;
import com.labs.botdev.zouglou.models.PlacesResponse;
import com.labs.botdev.zouglou.utils.Constants;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.offline.OfflineTilePyramidRegionDefinition;
import com.mapbox.mapboxsdk.plugins.building.BuildingPlugin;
import com.mapbox.mapboxsdk.plugins.cluster.MarkerManager;
import com.mapbox.mapboxsdk.plugins.cluster.clustering.ClusterManagerPlugin;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode;
import com.mapbox.mapboxsdk.plugins.offline.model.NotificationOptions;
import com.mapbox.mapboxsdk.plugins.offline.model.OfflineDownloadOptions;
import com.mapbox.mapboxsdk.plugins.offline.offline.OfflinePlugin;
import com.mapbox.mapboxsdk.plugins.offline.utils.OfflineUtils;
import com.mapbox.mapboxsdk.plugins.traffic.TrafficPlugin;
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigation;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import rx.functions.Action1;

public class MapActivity extends AppCompatActivity {
    FloatingActionButton menu, traffic;
    double longitude;
    double latitude;
    IOSDialog dialog;
    List<Event> events = new ArrayList<>();
    List<Event> passed_events = new ArrayList<>();
    List<Event> tmpEvents = new ArrayList<>();
    List<Event> tmpEvents2 = new ArrayList<>();
    private MapView mapView;
    private MapboxMap map;
    private MapboxNavigation navigation;
    private TrackGPS gps;
    private LocationLayerPlugin locationPlugin;
    SearchView searchView;
    ListEventAdapter adapter,adapter2;
    private BuildingPlugin buildingPlugin;
    private TrafficPlugin trafficPlugin;
    ClusterManagerPlugin clusterManagerPlugin;
    MarkerManager markerManager;
    BottomSheetDialog bottomdialog;
    View view;
    FrameLayout suggestion;
    TextView suggestionTxt;
    View suggestion_item;
    private static long sayBackPress;
    MediaPlayer mp;


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
        searchView = findViewById(R.id.searchview);
        traffic = findViewById(R.id.traffic);

        suggestion=findViewById(R.id.suggestion);
        suggestion_item=getLayoutInflater().inflate(R.layout.suggestion_item,null);
        suggestionTxt=suggestion_item.findViewById(R.id.suggestionTxt);

        dialog = LoaderProgress("Un instant", "Nous chargons les données");
       // mbottomSheetBehavior = BottomSheetBehavior.from(bottomsheet);
        searchView.setQueryHint("Nom artiste,maquis...");
        searchView.setIconified(true);

        menu = findViewById(R.id.menu);
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Stash.put("events", tmpEvents);
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
                if (newText.matches("")) {
                    Stash.put("events", tmpEvents);
                    clearMap();
                    placeEventsMarker();
                    filterZoomIn(tmpEvents);
                    suggestion.removeAllViews();
                    suggestion.setVisibility(View.GONE);
                } else {
                    if (adapter != null) {
                        adapter.getFilter().filter(newText);
                        adapter.notifyDataSetChanged();
                        events = Stash.getArrayList("filter_events", Event.class);
                        Stash.put("events", events);
                        clearMap();
                        placeEventsMarker();
                        filterZoomIn(events);
                        suggestion.removeAllViews();
                        suggestionTxt.setText("");
                        if(events.size()>1) {
                            suggestionTxt.setText(String.format(Locale.FRENCH, "%d endroits trouvés pour [%s]", events.size(), newText));
                        }else{
                            suggestionTxt.setText(String.format(Locale.FRENCH, "%d endroit trouvé pour [%s]", events.size(), newText));
                        }
                        suggestion.addView(suggestion_item);
                        suggestion.setVisibility(View.VISIBLE);
                    }
                }
                return false;
            }
        });

        searchView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    Stash.put("events", tmpEvents);
                    clearMap();
                    placeEventsMarker();
                    filterZoomIn(tmpEvents);
                    suggestion.removeAllViews();
                    suggestion.setVisibility(View.GONE);
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

            LatLng me = new LatLng();
            me.setLatitude(latitude);
            me.setLongitude(longitude);
            Stash.put("my_position", me);

            PlaceMe();
            zoomIn();
            //Toast.makeText(getApplicationContext(), "Longitude:" + Double.toString(longitude) + "\nLatitude:" + Double.toString(latitude), Toast.LENGTH_SHORT).show();
        } else {
            gps.showSettingsAlert();
        }
    }

    protected void PlaceMe() {
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                map = mapboxMap;
                locationPlugin = new LocationLayerPlugin(mapView, mapboxMap);
                locationPlugin.setRenderMode(RenderMode.COMPASS);
                getLifecycle().addObserver(locationPlugin);
                //mapboxMap.addMarker(markerOptions);

                buildingPlugin = new BuildingPlugin(mapView, mapboxMap);
                buildingPlugin.setVisibility(true);

                trafficPlugin = new TrafficPlugin(mapView, mapboxMap);
                trafficPlugin.setVisibility(true);
                clusterManagerPlugin = new ClusterManagerPlugin(MapActivity.this, mapboxMap);
            }
        });

        traffic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (map != null) {
                    trafficPlugin.setVisibility(!trafficPlugin.isVisible());
                }
            }
        });
    }

    private void zoomIn() {
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                LatLng me = (LatLng) Stash.getObject("my_position", LatLng.class);
                CameraPosition position = new CameraPosition.Builder()
                        .target(me) // Sets the new camera position
                        .zoom(10) // Sets the zoom
                        .bearing(25) // Rotate the camera
                        .tilt(30) // Set the camera tilt
                        .build();
                mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 5000);
            }
        });
    }

    private void filterZoomIn(List<Event> events) {
        List<LatLng> latLngs = new ArrayList<>();
        LatLng me = (LatLng) Stash.getObject("my_position", LatLng.class);
        latLngs.add(me);
        for (Event e : events) {
            LatLng p = new LatLng(Double.parseDouble(e.place.address.getLatitude()), Double.parseDouble(e.place.address.getLongitude()));
            latLngs.add(p);
        }

            mapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(MapboxMap mapboxMap) {
                    if(latLngs.size()>1) {
                        mapboxMap.animateCamera(CameraUpdateFactory.newLatLngBounds(new LatLngBounds.Builder()
                                .includes(latLngs)
                                .build(), 50), 7000);
                    }
                }
            });
    }

    private void clearMap() {
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                mapboxMap.deselectMarkers();
                mapboxMap.clear();
            }
        });
    }

    private void startPusherServiceEvent() {
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

                //mapboxMap.getUiSettings().setZoomControlsEnabled(true);
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
                                playSound("touch.mp3");
                                showDetails(Integer.parseInt(marker.getTitle()));
                                return true;
                            }
                    }

                });
            }
        });
    }

    public void showDetails(int id) {
        String base_url = Constants.UPLOAD_URL;
        view = getLayoutInflater().inflate(R.layout.fragment_bottom_sheet_details,null);
        Event e = new Event();
        events=Stash.getArrayList("events",Event.class);
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
                .applyDefaultRequestOptions(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                .load(url)
                .into(picture);

        title.setText(e.getTitle());
        place.setText(e.place.getTitle());

        String artist_str = "";
        for (Artist a : e.artists) {
            if (artist_str.equals("")) {
                artist_str = a.getName();
            } else {
                artist_str = artist_str + "," + a.getName();
            }
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

        bottomdialog=null;
        bottomdialog = new BottomSheetDialog(MapActivity.this);
        bottomdialog.setContentView(view);
        bottomdialog.show();
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
        loadPassedEventsRx();
    }

    private void OfflineSyncData() {
        dialog.show();
        events = Stash.getArrayList("events", Event.class);
        tmpEvents = Stash.getArrayList("events", Event.class);
        adapter = new ListEventAdapter(events, MapActivity.this);
        placeEventsMarker();
        dialog.dismiss();
        if(!events.isEmpty())
            filterZoomIn(events);
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
                Stash.put("events", response.getEvents());
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
                if (Stash.getArrayList("events", Event.class) != null)
                    events = Stash.getArrayList("events", Event.class);
                    tmpEvents = Stash.getArrayList("events", Event.class);
                    adapter = new ListEventAdapter(events, MapActivity.this);
                    placeEventsMarker();
                    dialog.dismiss();
                    filterZoomIn(events);
                    //zoomIn();
                //offlineMap();
                loadArtistsRx();
            }
        };

        APIService service = APIClient.getClient().create(APIService.class);
        Observable<EventsResponse> observable = service.getEventsList();
        observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.single())
                .subscribe(mObserver);
    }

    private void loadPassedEventsRx() {
        Observer mObserver = new Observer<EventsResponse>() {
            @Override
            public void onSubscribe(Disposable disposable) {
                dialog.show();
                //Toast.makeText(getApplicationContext(), getLocalClassName() + " Data load Init", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNext(EventsResponse response) {
                Stash.put("passed_events", response.getEvents());
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
                if (Stash.getArrayList("passed_events", Event.class) != null)
                    passed_events = Stash.getArrayList("passed_events", Event.class);
                    tmpEvents2 = Stash.getArrayList("passed_events", Event.class);
                    adapter2 = new ListEventAdapter(passed_events, MapActivity.this);
                    dialog.dismiss();
                //zoomIn();
                //offlineMap();
            }
        };

        APIService service = APIClient.getClient().create(APIService.class);
        Observable<EventsResponse> observable = service.getPassedEventsList();
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
                Stash.put("artists", response.getArtists());
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
                Stash.put("places", response.getPlaces());
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

    private void offlineMap() {
        OfflineTilePyramidRegionDefinition definition = new OfflineTilePyramidRegionDefinition(
                "https://api.mapbox.com/styles/v1/bumblebee47/cjmhpisnl3eep2so8gt0a4fsz.html?fresh=true&title=true&access_token=pk.eyJ1IjoiYnVtYmxlYmVlNDciLCJhIjoiY2phdjA0Ym11MHFodjJ6bjAxbnF2NXdtayJ9.WW82rcFdL6_o4pVs1itgcQ#12.0/48.866500/2.317600/0",
                new LatLngBounds.Builder()
                        .include(new LatLng(8.21174, -5.68668))
                        .include(new LatLng(8.21174, -5.68668))
                        .build(),
                5,
                8,
                getResources().getDisplayMetrics().density
        );

        NotificationOptions notificationOptions = NotificationOptions.builder(this)
                .smallIconRes(R.drawable.mapbox_logo_icon)
                .returnActivity(MapActivity.class.getName())
                .build();

        OfflinePlugin.getInstance(this).startDownload(
                OfflineDownloadOptions.builder()
                        .definition(definition)
                        .metadata(OfflineUtils.convertRegionName("Ivory-Coast"))
                        .notificationOptions(notificationOptions)
                        .build()
        );
    }

    private void playSound(String fileName) {
        mp = new MediaPlayer();
        try {
            AssetFileDescriptor afd = getApplicationContext().getAssets().openFd(fileName);
            mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
            mp.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mp.start();
    }

    @Override
    public void onBackPressed() {
        if (sayBackPress + 2000 > System.currentTimeMillis()){
            super.onBackPressed();
        }
        else{
            Toast.makeText(MapActivity.this, "Appuyer sur retour encore pour sortir", Toast.LENGTH_SHORT).show();
            sayBackPress = System.currentTimeMillis();
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
