package com.labs.botdev.zouglou.activities;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.appizona.yehiahd.fastsave.FastSave;
import com.bumptech.glide.Glide;
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork;
import com.gmail.samehadar.iosdialog.IOSDialog;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsRequest;
import com.jetradarmobile.rxlocationsettings.RxLocationSettings;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.labs.botdev.zouglou.R;
import com.labs.botdev.zouglou.services.APIClient;
import com.labs.botdev.zouglou.services.APIService;
import com.labs.botdev.zouglou.services.TrackGPS;
import com.labs.botdev.zouglou.services.models.Artist;
import com.labs.botdev.zouglou.services.models.ArtistsResponse;
import com.labs.botdev.zouglou.services.models.Event;
import com.labs.botdev.zouglou.services.models.EventsResponse;
import com.labs.botdev.zouglou.services.models.PlacesResponse;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigation;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
import rx.functions.Action1;

public class MapActivity extends AppCompatActivity {
    private MapView mapView;
    private MapboxNavigation navigation;
    TextView title, snippet, detail_sheet_title;
    View bottomsheet;
    ImageView picture;
    FloatingActionButton menu;
    private MapboxMap map;
    private TrackGPS gps;
    double longitude;
    double latitude;
    private BottomSheetBehavior mbottomSheetBehavior;
    IOSDialog dialog;
    List<Event> events = new ArrayList<>();
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
        dialog = LoaderProgress("Un instant", "Nous chargons les données");
        bottomsheet = findViewById(R.id.details_sheet);
        mbottomSheetBehavior = BottomSheetBehavior.from(bottomsheet);

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
        ReactiveNetwork.checkInternetConnectivity()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        if (aBoolean) {
                            RemoteSyncData();
                        } else {
                            OfflineSyncData();
                        }
                    }
                });

        events=FastSave.getInstance().getObjectsList("events",Event.class);
        //getLocation();
        ensureLocationSettings();
    }

    private void getLocation(){
        LocationEngine locationEngine = new LocationEngineProvider(this).obtainBestLocationEngineAvailable();
        locationEngine.activate();
        locationEngine.addLocationEngineListener(new LocationEngineListener() {
            @Override
            public void onConnected() {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                locationEngine.requestLocationUpdates();
            }

            @Override
            public void onLocationChanged(Location location) {
                MarkerOptions markerOptions = new MarkerOptions();
                LatLng me = new LatLng();
                IconFactory iconFactory = IconFactory.getInstance(MapActivity.this);
                Icon icon = iconFactory.fromResource(R.drawable.ic_map_marker_radius_black_48dp);

                me.setLatitude(location.getLatitude());
                me.setLongitude(location.getLongitude());
                FastSave.getInstance().saveObject("my_position", me);
                markerOptions.setPosition(me);
                markerOptions.setSnippet("Je suis Ici");
                markerOptions.setTitle(String.valueOf(0));
                markerOptions.setIcon(icon);
                PlaceMarker(markerOptions);
            }
        });
    }

    //Check if Lacation is enabled and launch teask
    private void ensureLocationSettings() {
        LocationSettingsRequest locationSettingsRequest = new LocationSettingsRequest.Builder()
                .addLocationRequest(LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY))
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
            FastSave.getInstance().saveObject("my_position", me);
            markerOptions.setPosition(me);
            markerOptions.setSnippet("Je suis Ici");
            markerOptions.setTitle(String.valueOf(0));
            markerOptions.setIcon(icon);
            PlaceMarker(markerOptions);

            //Toast.makeText(getApplicationContext(), "Longitude:" + Double.toString(longitude) + "\nLatitude:" + Double.toString(latitude), Toast.LENGTH_SHORT).show();
        } else {
            gps.showSettingsAlert();
        }
    }

    protected void PlaceMarker(MarkerOptions markerOptions) {
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                mapboxMap.addMarker(markerOptions);
            }
        });
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

                mapboxMap.setOnMarkerClickListener(new MapboxMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(@NonNull Marker marker) {
                        if(Integer.parseInt(marker.getTitle())==0){
                            Toast.makeText(MapActivity.this,marker.getSnippet(),Toast.LENGTH_LONG).show();
                            return false;
                        }else {
                            showDetails(Integer.parseInt(marker.getTitle()));
                            if (mbottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                                mbottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                            } else {
                                mbottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                            }
                            return true;
                        }
                    }

                });
            }
        });
    }

   public void showDetails(int id){
       String base_url="http://www.berakatravel.com/zouglou/public/uploads/";
       LinearLayout view = findViewById(R.id.bottom_sheet);
       com.labs.botdev.zouglou.services.models.Event e=new Event();

       for(Event n:events){
           if(n.getId()==id){
               e=n;
           }
       }

       ImageView picture=view.findViewById(R.id.picture);
       TextView title=view.findViewById(R.id.title);
       TextView artists=view.findViewById(R.id.artists);
       TextView place=view.findViewById(R.id.place);
       AppCompatButton show_details=view.findViewById(R.id.showDetails);
       AppCompatButton show_navigation=view.findViewById(R.id.showNavigation);
       //TextView date=view.findViewById(R.id.date);

       show_details.setTag(e.getId());

       view.setTag(e);
       String url =   base_url+e.getPicture();
       Glide
               .with(getApplicationContext())
               .load(url)
               .into(picture);

       title.setText(e.getTitle());
       place.setText(Objects.requireNonNull(e.place.getTitle()));

       String artist_str="";
       for(Artist a:e.artists){
           artist_str = a.getName() + ",";
       }

       artists.setText(artist_str);

       show_details.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               Intent details=new Intent(MapActivity.this, DetailsEventActivity.class);
               details.putExtra("event_id", (int) v.getTag());
               startActivity(details);
           }
       });

       Event finalE = e;
       show_navigation.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               ShowOnMap(Double.parseDouble(finalE.place.address.getLatitude()),Double.parseDouble(finalE.place.address.getLongitude()));
           }
       });
   }

    protected void ShowOnMap(Double lat, Double lon) {
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + lat + "," + lon + "");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }else{
            Intent navigation=new Intent(MapActivity.this, DoNavigationActivity.class);
            String destination = lat+":"+lon;
            navigation.putExtra("destination",destination);
            startActivity(navigation);
        }
    }

    private void RemoteSyncData() {
        loadArtistsRx();
    }

    private void OfflineSyncData() {
        placeEventsMarker();
    }

    private void loadEventsRx() {

        Observer mObserver = new Observer<EventsResponse>() {
            @Override
            public void onSubscribe(Disposable disposable) {
                //Toast.makeText(getApplicationContext(), getLocalClassName() + " Data load Init", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNext(EventsResponse response) {
                FastSave.getInstance().saveObjectsList("events", response.getEvents());
            }

            @Override
            public void onError(Throwable e) {
                Toast.makeText(getApplicationContext(), "Data load Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("Data load error: ", e.getMessage());
            }

            @Override
            public void onComplete() {
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

    private void loadArtistsRx() {
        Observer mObserver = new Observer<ArtistsResponse>() {
            @Override
            public void onSubscribe(Disposable disposable) {
                dialog.show();
                //Toast.makeText(getApplicationContext(), getLocalClassName() + " Data load Init", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNext(ArtistsResponse artistsResponse) {
                FastSave.getInstance().saveObjectsList("artists", artistsResponse.getArtists());
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
                FastSave.getInstance().saveObjectsList("places", response.getPlaces());
            }

            @Override
            public void onError(Throwable e) {
                Toast.makeText(getApplicationContext(), "Data load Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("Data load error: ", e.getMessage());
            }

            @Override
            public void onComplete() {
                loadEventsRx();
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
        List<Event> events = FastSave.getInstance().getObjectsList("events", Event.class);
        for (Event e : events) {
            addMarker(Double.parseDouble(e.place.address.getLatitude()), Double.parseDouble(e.place.address.getLongitude()), e.getId(), e.getDescription());
        }
        dialog.dismiss();
    }

    private void checkPermissions() {
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {/* ... */}

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {/* ... */}
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
