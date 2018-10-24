package com.labs.botdev.zouglou.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.fxn.stash.Stash;
import com.google.android.gms.ads.MobileAds;
import com.labs.botdev.zouglou.R;
import com.labs.botdev.zouglou.models.Event;
import com.labs.botdev.zouglou.models.Place;
import com.labs.botdev.zouglou.utils.AppController;
import com.labs.botdev.zouglou.utils.Constants;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class DetailsPlaceActivity extends AppCompatActivity {
    List<Place> places=new ArrayList<>();
    Place p=new Place();
    ImageView place_picture;
    Toolbar toolbar;
    MapView mapView;
    TextView commune,quartier;
    FloatingActionButton navigation_top, navigation_bottom,share_top,share_bottom;
    LinearLayout events_layout, top_actions, bottom_actions;
    private MapboxNavigation navigation;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_details);
        Mapbox.getInstance(this, getResources().getString(R.string.mapbox_access_token));
        navigation = new MapboxNavigation(this, getResources().getString(R.string.mapbox_access_token));
        MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");
        place_picture = findViewById(R.id.header);
        toolbar = findViewById(R.id.anim_toolbar);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        navigation_top = findViewById(R.id.navigation_top);
        navigation_bottom = findViewById(R.id.navigation_bottom);
        share_top=findViewById(R.id.share_top);
        share_bottom=findViewById(R.id.share_bottom);
        top_actions = findViewById(R.id.top_actions);
        bottom_actions = findViewById(R.id.bottom_actions);
        events_layout = findViewById(R.id.events_layout);
        commune=findViewById(R.id.commune);
        quartier=findViewById(R.id.quartier);
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        Intent intent = getIntent();
        int id = intent.getIntExtra("place_id", 0);
        places = Stash.getArrayList("places", Place.class);
        for (Place n : places) {
            if (n.getId() == id) {
                p = n;
            }
        }

        Glide
                .with(getApplicationContext())
                .applyDefaultRequestOptions(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                .load(p.getPicture())
                .into(place_picture);
        collapsingToolbarLayout.setTitle(p.getTitle());

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        commune.setText(p.address.getCommune());
        quartier.setText(p.address.getQuartier());

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(new LatLng(Double.parseDouble(p.address.getLatitude()),Double.parseDouble(p.address.getLongitude())))
                        .setTitle(p.address.getCommune())
                        .setSnippet(p.getTitle());
                mapboxMap.addMarker(markerOptions);

                LatLng me = (new LatLng(Double.parseDouble(p.address.getLatitude()),Double.parseDouble(p.address.getLongitude())));
                CameraPosition position = new CameraPosition.Builder()
                        .target(me) // Sets the new camera position
                        .zoom(10) // Sets the zoom
                        .bearing(25) // Rotate the camera
                        .tilt(30) // Set the camera tilt
                        .build();
                mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 5000);

                mapboxMap.getUiSettings().setZoomGesturesEnabled(true);
                mapboxMap.getUiSettings().setCompassEnabled(true);
                mapboxMap.getUiSettings().setScrollGesturesEnabled(true);
            }
        });

        navigation_top.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowOnMap(Double.parseDouble(p.address.getLatitude()), Double.parseDouble(p.address.getLongitude()));
            }
        });

        navigation_bottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowOnMap(Double.parseDouble(p.address.getLatitude()), Double.parseDouble(p.address.getLongitude()));
            }
        });

        share_top.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareOnSocialNetwork(p);
                //whatsappShare();
            }
        });

        share_bottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareOnSocialNetwork(p);
            }
        });


        InitAppBar();
        try {
            loadEvents(p.events);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void InitAppBar() {
        AppBarLayout mAppBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                //Toast.makeText(getApplicationContext(),String.valueOf(appBarLayout.getHeight()),Toast.LENGTH_LONG).show();
                if (Math.abs(verticalOffset) == appBarLayout.getTotalScrollRange()) {
                    //Toast.makeText(getApplicationContext(),"collapsed",Toast.LENGTH_LONG).show();
                    top_actions.setVisibility(View.GONE);
                    bottom_actions.setVisibility(View.VISIBLE);
                } else if (verticalOffset == 0) {
                    //Toast.makeText(getApplicationContext(),"expanded",Toast.LENGTH_LONG).show();
                    bottom_actions.setVisibility(View.GONE);
                    top_actions.setVisibility(View.VISIBLE);
                } else {
                    //Toast.makeText(getApplicationContext(),"scrolling",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void loadEvents(List<Event> events) throws ParseException {
//        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
//        Date date = new Date();
        if(events.size()>0){
        for (Event a : events) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date strDate = sdf.parse(a.getEnd());
            Log.e("Event date:", strDate.toString());
            if (new Date().before(strDate)) {
                //Toast.makeText(getApplicationContext(), "Artist added" + a.getAvatar(), Toast.LENGTH_LONG).show();
                LinearLayout parent = new LinearLayout(getApplicationContext());
                parent = (LinearLayout) getLayoutInflater().inflate(R.layout.artist_item, null);
                CircleImageView avatar = parent.findViewById(R.id.avatar);
                ImageView pause = parent.findViewById(R.id.pause);
                ImageView play = parent.findViewById(R.id.play);
                play.setVisibility(View.GONE);
                TextView artist_name = parent.findViewById(R.id.artist_name);
                Glide
                        .with(getApplicationContext())
                        .applyDefaultRequestOptions(new RequestOptions()
                                .diskCacheStrategy(DiskCacheStrategy.ALL))
                        .load(Constants.UPLOAD_URL + a.getPicture())
                        .into(avatar);
                parent.setTag(a.getId());
                parent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent details = new Intent(getApplicationContext(), DetailsEventActivity.class);
                        details.putExtra("event_id", (int) v.getTag());
                        startActivity(details);
                    }
                });
                artist_name.setText(a.getTitle());
                parent.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                events_layout.addView(parent);
            } else {
                LinearLayout parent = new LinearLayout(getApplicationContext());
                parent = (LinearLayout) getLayoutInflater().inflate(R.layout.artist_item, null);
                CircleImageView avatar = parent.findViewById(R.id.avatar);
                ImageView pause = parent.findViewById(R.id.pause);
                ImageView play = parent.findViewById(R.id.play);
                play.setVisibility(View.GONE);
                TextView artist_name = parent.findViewById(R.id.artist_name);
                Glide
                        .with(getApplicationContext())
                        .applyDefaultRequestOptions(new RequestOptions()
                                .diskCacheStrategy(DiskCacheStrategy.ALL))
                        .load(Constants.UPLOAD_URL + a.getPicture())
                        .into(avatar);
                parent.setTag(a.getId());
                parent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent details = new Intent(getApplicationContext(), DetailsPassedEventActivity.class);
                        details.putExtra("event_id", (int) v.getTag());
                        startActivity(details);
                    }
                });
                artist_name.setText(a.getTitle());
                parent.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                events_layout.addView(parent);
            }
        }
        }else{
            TextView emptytxt=new TextView(DetailsPlaceActivity.this);
            emptytxt.setText("Aucun Evenement!");
            LinearLayout parent = new LinearLayout(getApplicationContext());
            parent.addView(emptytxt);
            events_layout.addView(parent);
        }
    }

    protected void ShowOnMap(Double lat, Double lon) {
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + lat + "," + lon + "");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Intent navigation = new Intent(DetailsPlaceActivity.this, DoNavigationActivity.class);
            String destination = lat + ":" + lon;
            navigation.putExtra("destination", destination);
            startActivity(navigation);
        }
    }

    private void shareOnSocialNetwork(Place e){
        String pic_url= AppController.getInstance().downloadedPicture(Constants.UPLOAD_URL+e.getPicture());
        Uri pictureUri = Uri.parse(pic_url);
        String geoUri = "http://maps.google.com/maps?q=loc:" + e.address.getLatitude() + "," +
                e.address.getLongitude() ;
        String text=new StringBuilder()
                .append("\n"+e.getTitle().toUpperCase())
                .append("\n"+"Position GPS:\n"+geoUri)
                .toString();

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        shareIntent.putExtra(Intent.EXTRA_STREAM, pictureUri);
        //shareIntent.setType("image/*");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, e.getTitle());
        startActivity(Intent.createChooser(shareIntent, "Partager sur les réseaux sociaux..."));
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
