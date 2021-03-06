package com.labs.botdev.zouglou.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;

import com.fxn.stash.Stash;
import com.labs.botdev.zouglou.R;
import com.labs.botdev.zouglou.adapters.ListArtistAdapter;
import com.labs.botdev.zouglou.models.Artist;

import java.util.ArrayList;
import java.util.List;

public class ListArtistsActivity extends AppCompatActivity {
    ListView lv_artists;
    List<Artist> artists=new ArrayList<>();
    ListArtistAdapter artistAdapter;
    Toolbar toolbar;
    SearchView searchView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_artist);
        lv_artists = findViewById(R.id.lv_artists);
        toolbar = findViewById(R.id.toolbar);
        searchView=findViewById(R.id.searchview);
        artists = Stash.getArrayList("artists", Artist.class);
        artistAdapter = new ListArtistAdapter(this, artists);
        searchView.setQueryHint("Nom de l'artiste...");
        searchView.setIconified(true);
        lv_artists.setAdapter(artistAdapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            //Rechercher sur la carte(Not finished yet!!)
            @Override
            public boolean onQueryTextChange(String newText) {
                    if (artistAdapter != null) {
                        artistAdapter.getFilter().filter(newText);
                        artistAdapter.notifyDataSetChanged();
                        artists = Stash.getArrayList("filter_artists", Artist.class);
                        Stash.put("artists", artists);
                    }
                return false;
            }
        });

        toolbar.setTitle("Mes gars sûrs");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);
//        getSupportActionBar().setHomeButtonEnabled(true);
    }


    @Override
    public void onBackPressed() {
        Intent main=new Intent(ListArtistsActivity.this,ListEventsActivity.class);
        main.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        startActivity(main);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
