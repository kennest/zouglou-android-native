package com.labs.botdev.zouglou.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
    private Menu menu;
    TextView description,date;
    FloatingActionButton navigation_top,navigation_bottom;

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
        collapsingToolbarLayout.setTitle(e.getTitle());
        toolbar.setTitle(e.getBegin());
        description.setText(e.getDescription());
        date.setText(String.format("%s /%s", e.getBegin(), e.getEnd()));

        //Toast.makeText(getApplicationContext(),"Event picture:"+Constants.UPLOAD_URL+e.getPicture(),Toast.LENGTH_LONG).show();
        InitAppBar();
    }

    private void InitAppBar() {
        AppBarLayout mAppBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                Toast.makeText(getApplicationContext(),String.valueOf(appBarLayout.getHeight()),Toast.LENGTH_LONG).show();
                if (Math.abs(verticalOffset) == appBarLayout.getTotalScrollRange()) {
                    Toast.makeText(getApplicationContext(),"collapsed",Toast.LENGTH_LONG).show();
                    navigation_bottom.setVisibility(View.VISIBLE);
                } else if (verticalOffset == 0) {
                    Toast.makeText(getApplicationContext(),"expanded",Toast.LENGTH_LONG).show();
                    navigation_bottom.setVisibility(View.GONE);
                } else {
                    Toast.makeText(getApplicationContext(),"scrolling",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.menu = menu;
        getMenuInflater().inflate(R.menu.scrolling_menu, menu);
        hideOption(R.id.action_info);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_info) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void hideOption(int id) {
        MenuItem item = menu.findItem(id);
        item.setVisible(false);
    }

    private void showOption(int id) {
        MenuItem item = menu.findItem(id);
        item.setVisible(true);
    }
}

