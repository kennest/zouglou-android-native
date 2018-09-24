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
import android.view.Gravity;
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
import com.gmail.samehadar.iosdialog.IOSDialog;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.labs.botdev.zouglou.R;
import com.labs.botdev.zouglou.services.models.Artist;
import com.labs.botdev.zouglou.services.models.Event;
import com.labs.botdev.zouglou.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class DetailsEventActivity extends AppCompatActivity implements Player.EventListener {
    ImageView event_picture;
    Toolbar toolbar;
    TextView description, place, begin, end;
    FloatingActionButton navigation_top, navigation_bottom;
    List<Event> events = new ArrayList<>();
    Event e = new Event();
    LinearLayout artists_layout, top_actions, bottom_actions;
    PlayerView playerView;
    SimpleExoPlayer player;
    IOSDialog loader;
    ImageView place_picture;
    CollapsingToolbarLayout collapsingToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        event_picture = findViewById(R.id.header);
        toolbar = findViewById(R.id.anim_toolbar);
        description = findViewById(R.id.event_description);
        begin = findViewById(R.id.event_begin);
        end = findViewById(R.id.event_end);
        place = findViewById(R.id.event_place);
        place_picture = findViewById(R.id.place_picture);
        navigation_top = findViewById(R.id.navigation_top);
        navigation_bottom = findViewById(R.id.navigation_bottom);
        artists_layout = findViewById(R.id.artists_layout);
        playerView = findViewById(R.id.playerView);
        top_actions = findViewById(R.id.top_actions);
        bottom_actions = findViewById(R.id.bottom_actions);
        loader = LoaderProgress("Un instant", "Nous chargons les données");
        loader.show();
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Intent intent = getIntent();
        int id = intent.getIntExtra("event_id", 0);

        events = Stash.getArrayList("events", Event.class);
        for (Event n : events) {
            if (n.getId() == id) {
                e = n;
            }
        }

        Glide
                .with(getApplicationContext())
                .applyDefaultRequestOptions(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                .load(Constants.UPLOAD_URL + e.getPicture())
                .into(event_picture);

        Glide
                .with(getApplicationContext())
                .applyDefaultRequestOptions(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                .load(Constants.UPLOAD_URL + e.place.getPicture())
                .into(place_picture);

        description.setText(e.getDescription());
        place.setText(e.place.getTitle());
        begin.setText(e.getBegin());
        end.setText(e.getEnd());

        collapsingToolbarLayout.setTitle(e.getTitle());

        toolbar.setTitle(e.getBegin());
        loadArtists(e.artists);

        InitAppBar();
        //date.setText(String.format("%s /%s", e.getBegin(), e.getEnd()));

        navigation_top.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowOnMap(Double.parseDouble(e.place.address.getLatitude()), Double.parseDouble(e.place.address.getLongitude()));
            }
        });

        navigation_bottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowOnMap(Double.parseDouble(e.place.address.getLatitude()), Double.parseDouble(e.place.address.getLongitude()));
            }
        });

        loader.dismiss();
//        Toast.makeText(getApplicationContext(),"Artists IDS"+e.getArtists_id(),Toast.LENGTH_LONG).show();
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

    protected void ShowOnMap(Double lat, Double lon) {
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + lat + "," + lon + "");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Intent navigation = new Intent(DetailsEventActivity.this, DoNavigationActivity.class);
            String destination = lat + ":" + lon;
            navigation.putExtra("destination", destination);
            startActivity(navigation);
        }
    }

    protected void playSample(String url, ImageView play, ImageView pause) {
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

// 2. Create the player
        player = ExoPlayerFactory.newSimpleInstance(getApplicationContext(), trackSelector);

        playerView.setPlayer(player);
        playerView.setVisibility(View.VISIBLE);

// Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(getApplicationContext(), Util.getUserAgent(getApplicationContext(), "yourApplicationName"));
// This is the MediaSource representing the media to be played.
        MediaSource audioSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.parse(url));
// Prepare the player with the source.
        player.prepare(audioSource);
        player.addListener(this);

        player.setPlayWhenReady(true);
    }

    private void loadArtists(List<Artist> artists) {
        for (Artist a : artists) {
            //Toast.makeText(getApplicationContext(), "Artist added" + a.getAvatar(), Toast.LENGTH_LONG).show();
            LinearLayout parent = new LinearLayout(getApplicationContext());
            parent = (LinearLayout) getLayoutInflater().inflate(R.layout.artist_item, null);
            CircleImageView avatar = parent.findViewById(R.id.avatar);
            ImageView pause = parent.findViewById(R.id.pause);
            ImageView play = parent.findViewById(R.id.play);
            TextView artist_name = parent.findViewById(R.id.artist_name);
            Glide
                    .with(getApplicationContext())
                    .applyDefaultRequestOptions(new RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.ALL))
                    .load(Constants.UPLOAD_URL + a.getAvatar())
                    .into(avatar);
            parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playSample(Constants.UPLOAD_URL + a.getSample(), play, pause);
                }
            });
            artist_name.setText(a.getName());
            parent.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            artists_layout.addView(parent);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

        switch (playbackState) {

            case Player.STATE_BUFFERING:

                break;

            case Player.STATE_ENDED:
                playerView.setVisibility(View.INVISIBLE);
                break;

            case Player.STATE_IDLE:

                break;

            case Player.STATE_READY:

                break;

            default:

                break;

        }

    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onPositionDiscontinuity(int reason) {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    @Override
    public void onSeekProcessed() {

    }

    public IOSDialog LoaderProgress(String title, String content) {
        final IOSDialog dialog = new IOSDialog.Builder(DetailsEventActivity.this)
                .setTitle(title)
                .setMessageContent(content)
                .setSpinnerColorRes(R.color.colorPrimary)
                .setCancelable(false)
                .setTitleColorRes(R.color.white)
                .setMessageContentGravity(Gravity.END)
                .build();
        return dialog;
    }

    @Override
    protected void onDestroy() {
        if (player != null)
            player.release();
        super.onDestroy();
    }
}

