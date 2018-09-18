package com.labs.botdev.zouglou.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;

import com.arlib.floatingsearchview.util.view.SearchInputView;
import com.fxn.stash.Stash;
import com.labs.botdev.zouglou.R;
import com.labs.botdev.zouglou.adapters.ListPlaceAdapter;
import com.labs.botdev.zouglou.services.models.Place;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ListPlacesActivity extends AppCompatActivity {
ListView list;
List<Place> places=new ArrayList<>();
ListPlaceAdapter adapter;
SearchView searchview;
Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_places);
        list=findViewById(R.id.places_list);
        searchview=findViewById(R.id.searchview);
        toolbar=findViewById(R.id.toolbar);
        toolbar.setTitle("Les Coins Chauds");

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        places= Stash.getArrayList("places",Place.class);
        adapter=new ListPlaceAdapter(places,this);
        list.setAdapter(adapter);
        searchview.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                adapter.notifyDataSetChanged();
                return false;
            }
        });
    }

//    @Override
//    public void onBackPressed() {
//        Intent events=new Intent(ListPlacesActivity.this,ListEventsActivity.class);
//        events.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
//        startActivity(events);
//    }

    @Override
    public boolean onNavigateUp() {
        onBackPressed();
        return false;
    }
}