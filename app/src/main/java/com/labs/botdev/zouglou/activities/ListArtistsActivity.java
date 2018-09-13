package com.labs.botdev.zouglou.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;

import com.fxn.stash.Stash;
import com.labs.botdev.zouglou.R;
import com.labs.botdev.zouglou.adapters.ListArtistAdapter;
import com.labs.botdev.zouglou.services.models.Artist;

import java.util.List;
import java.util.Objects;

public class ListArtistsActivity extends AppCompatActivity {
    ListView lv_artists;
    Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_artist);
        lv_artists = findViewById(R.id.lv_artists);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Liste des Personnages du Zouglou");
        List<Artist> artists = Stash.getArrayList("artists", Artist.class);
        ListArtistAdapter artistAdapter = new ListArtistAdapter(this, artists);
        lv_artists.setAdapter(artistAdapter);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onNavigateUp() {
        onBackPressed();
        return super.onNavigateUp();
    }
}
