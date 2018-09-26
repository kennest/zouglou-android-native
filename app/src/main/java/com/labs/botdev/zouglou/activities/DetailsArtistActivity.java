package com.labs.botdev.zouglou.activities;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.fxn.stash.Stash;
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
import com.labs.botdev.zouglou.models.Artist;
import com.labs.botdev.zouglou.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class DetailsArtistActivity extends AppCompatActivity implements Player.EventListener {
    List<Artist> artists = new ArrayList<>();
    Artist artist = new Artist();
    ImageView header;
    Toolbar toolbar;
    TextView name, description;
    FloatingActionButton play_sample, navigation_bottom;
    LinearLayout events_layout, top_actions;
    PlayerView playerView;
    SimpleExoPlayer player;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.artist_details);
        header = findViewById(R.id.header);
        top_actions = findViewById(R.id.top_actions);
        toolbar = findViewById(R.id.anim_toolbar);
        name = findViewById(R.id.artist_name);
        description = findViewById(R.id.artist_description);
        playerView = findViewById(R.id.playerView);
        play_sample = findViewById(R.id.play_sample);

        int id = getIntent().getIntExtra("artist_id", 0);
        artists = Stash.getArrayList("artists", Artist.class);
        for (Artist a : artists) {
            if (a.getId() == id) {
                artist = a;
            }
        }

        name.setText(artist.getName());

        toolbar.setTitle(artist.getName());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ActionBar actionBar = getSupportActionBar();
//Permet d'afficher le bouton de navigation up sur l'application
        actionBar.setDisplayHomeAsUpEnabled(true);

        Glide
                .with(getApplicationContext())
                .applyDefaultRequestOptions(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                .load(Constants.UPLOAD_URL + artist.getAvatar())
                .into(header);
        //Toast.makeText(getApplicationContext(),"Artist: "+artist.getName(),Toast.LENGTH_LONG).show();
        InitAppBar();

        play_sample.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSample(Constants.UPLOAD_URL + artist.getSample());
            }
        });
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
                } else if (verticalOffset == 0) {
                    //Toast.makeText(getApplicationContext(),"expanded",Toast.LENGTH_LONG).show();
                    top_actions.setVisibility(View.VISIBLE);
                } else {
                    //Toast.makeText(getApplicationContext(),"scrolling",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    protected void playSample(String url) {
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

    @Override
    public boolean onNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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

    @Override
    protected void onDestroy() {
        if (player != null)
            player.release();
        super.onDestroy();
    }
}
