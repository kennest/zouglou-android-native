package com.labs.botdev.zouglou.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.labs.botdev.zouglou.R;
import com.labs.botdev.zouglou.objectbox.Event;
import com.labs.botdev.zouglou.objectbox.Event_;
import com.labs.botdev.zouglou.utils.AppController;
import com.labs.botdev.zouglou.utils.Constants;

import io.objectbox.Box;

public class DetailsEventActivity extends AppCompatActivity {
    ImageView event_picture;
    Toolbar toolbar;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        event_picture=findViewById(R.id.header);
        toolbar=findViewById(R.id.anim_toolbar);
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent intent=getIntent();
        int id = intent.getIntExtra("event_id", 0);
        Box<Event> eventBox= AppController.boxStore.boxFor(Event.class);
        Event e=eventBox.query().equal(Event_.raw_id,id).build().findFirst();
        Glide
                .with(getApplicationContext())
                .applyDefaultRequestOptions(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                .load(Constants.UPLOAD_URL+e.getPicture())
                .into(event_picture);
        collapsingToolbarLayout.setTitle(e.getTitle());

        //Toast.makeText(getApplicationContext(),"Event picture:"+Constants.UPLOAD_URL+e.getPicture(),Toast.LENGTH_LONG).show();
    }
}
