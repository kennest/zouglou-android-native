package com.labs.botdev.zouglou.activities;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.jean.jcplayer.model.JcAudio;
import com.example.jean.jcplayer.view.JcPlayerView;
import com.labs.botdev.zouglou.R;
import com.labs.botdev.zouglou.objectbox.Address;
import com.labs.botdev.zouglou.objectbox.Address_;
import com.labs.botdev.zouglou.objectbox.Artist;
import com.labs.botdev.zouglou.objectbox.Artist_;
import com.labs.botdev.zouglou.objectbox.Event;
import com.labs.botdev.zouglou.objectbox.Event_;
import com.labs.botdev.zouglou.objectbox.Place;
import com.labs.botdev.zouglou.objectbox.Place_;
import com.labs.botdev.zouglou.tasks.Player;
import com.labs.botdev.zouglou.utils.AppController;
import com.labs.botdev.zouglou.utils.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import io.objectbox.Box;

public class DetailsEventActivity extends AppCompatActivity {
    ImageView event_picture;
    Toolbar toolbar;
    TextView description,date;
    FloatingActionButton navigation_top,navigation_bottom;
    Box<Place> placeBox=AppController.boxStore.boxFor(Place.class);
    Box<Address> addressBox=AppController.boxStore.boxFor(Address.class);
    Box<Artist> artistBox=AppController.boxStore.boxFor(Artist.class);
     boolean playPause;
     MediaPlayer mediaPlayer;
     boolean initialStage = true;
     List<Artist> artists=new ArrayList<>();
     LinearLayout artists_layout;
     JcPlayerView jcplayerView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        event_picture = findViewById(R.id.header);
        toolbar = findViewById(R.id.anim_toolbar);
        description=findViewById(R.id.event_description);
        date=findViewById(R.id.event_date);
        navigation_top=findViewById(R.id.navigation_top);
        navigation_bottom=findViewById(R.id.navigation_bottom);
        artists_layout=findViewById(R.id.artists_layout);
        jcplayerView = (JcPlayerView) findViewById(R.id.jcplayer);

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        Intent intent = getIntent();
        int id = intent.getIntExtra("event_id", 0);
        Box<Event> eventBox = AppController.boxStore.boxFor(Event.class);
        Event e = eventBox.query().equal(Event_.raw_id, id).build().findFirst();
        Glide
                .with(getApplicationContext())
                .applyDefaultRequestOptions(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                .load(Constants.UPLOAD_URL + e.getPicture())
                .into(event_picture);

        Place p=placeBox.query().equal(Place_.raw_id,e.getPlace_id()).build().findFirst();
        Address a=addressBox.query().equal(Address_.place_id,p.getRaw_id()).build().findFirst();



        collapsingToolbarLayout.setTitle(e.getTitle()+"/"+p.getTitle());

        toolbar.setTitle(e.getBegin());
        description.setText(e.getDescription());
        date.setText(String.format("%s /%s", e.getBegin(), e.getEnd()));

        navigation_top.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowOnMap(a.getLatitude(),a.getLongitude());
            }
        });

        navigation_bottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowOnMap(a.getLatitude(),a.getLongitude());
            }
        });
        extractRecipient(e.getArtists_id());
//        Toast.makeText(getApplicationContext(),"Artists IDS"+e.getArtists_id(),Toast.LENGTH_LONG).show();
        loadArtists(artists);

        //Toast.makeText(getApplicationContext(),"Event picture:"+Constants.UPLOAD_URL+e.getPicture(),Toast.LENGTH_LONG).show();
        InitAppBar();
    }

    private void InitAppBar() {
        AppBarLayout mAppBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                //Toast.makeText(getApplicationContext(),String.valueOf(appBarLayout.getHeight()),Toast.LENGTH_LONG).show();
                if (Math.abs(verticalOffset) == appBarLayout.getTotalScrollRange()) {
                    //Toast.makeText(getApplicationContext(),"collapsed",Toast.LENGTH_LONG).show();
                    navigation_bottom.setVisibility(View.VISIBLE);
                } else if (verticalOffset == 0) {
                    //Toast.makeText(getApplicationContext(),"expanded",Toast.LENGTH_LONG).show();
                    navigation_bottom.setVisibility(View.GONE);
                } else {
                    //Toast.makeText(getApplicationContext(),"scrolling",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    protected void ShowOnMap(Double lat, Double lon) {
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + lat + "," + lon + "");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }
    }


    protected void playSample(String url,ImageView play,ImageView pause){
        ArrayList<JcAudio> jcAudios = new ArrayList<>();
        jcAudios.add(JcAudio.createFromURL(Constants.UPLOAD_URL+url));
        jcplayerView.initPlaylist(jcAudios, null);
        jcplayerView.setVisibility(View.VISIBLE);
    }

    private void loadArtists(List<Artist> artists){
        for(Artist a:artists) {
            Toast.makeText(getApplicationContext(),"Artist added"+a.getName(),Toast.LENGTH_LONG).show();
            LinearLayout parent= new LinearLayout(getApplicationContext());
            parent= (LinearLayout) getLayoutInflater().inflate(R.layout.artist_item, null);
            CircleImageView avatar = parent.findViewById(R.id.avatar);
            ImageView pause = parent.findViewById(R.id.pause);
            ImageView play = parent.findViewById(R.id.play);
            Glide
                    .with(getApplicationContext())
                    .applyDefaultRequestOptions(new RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.ALL))
                    .load(Constants.UPLOAD_URL + a.getAvatar())
                    .into(avatar);
            parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playSample(Constants.UPLOAD_URL+a.getSample(),play,pause);
                }
            });
            parent.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            artists_layout.addView(parent);
        }
    }

    private void extractRecipient(String s){
        Toast.makeText(getApplicationContext(),"ID: "+s,Toast.LENGTH_LONG).show();
        Box<Artist> recipientBox=AppController.boxStore.boxFor(Artist.class);
        String replace = s.replace("[","");
        System.out.println(replace);
        String replace1 = replace.replace("]","");
        System.out.println(replace1);

        List<String> IDlists = new ArrayList<String>(Arrays.asList(replace1.split(",")));

        for(String n:IDlists){
            int i=Integer.parseInt(n.trim());
            Artist x=artistBox.query().equal(Artist_.raw_id,i).build().findFirst();
            if(x!=null)
                artists.add(x);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public boolean onNavigateUp() {
        onBackPressed();
        return true;
    }
}

