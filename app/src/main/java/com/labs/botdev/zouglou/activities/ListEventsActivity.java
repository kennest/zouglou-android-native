package com.labs.botdev.zouglou.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.fxn.stash.Stash;
import com.labs.botdev.zouglou.R;
import com.labs.botdev.zouglou.adapters.DrawerListAdapter;
import com.labs.botdev.zouglou.adapters.EventPagerAdapter;
import com.labs.botdev.zouglou.models.User;
import com.labs.botdev.zouglou.utils.AppController;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nl.psdcompany.duonavigationdrawer.views.DuoDrawerLayout;
import nl.psdcompany.duonavigationdrawer.views.DuoMenuView;
import nl.psdcompany.duonavigationdrawer.widgets.DuoDrawerToggle;

public class ListEventsActivity extends Activity {
    Toolbar toolbar;
    LayoutInflater inflater;
    FloatingActionButton mapBtn;
    TextView user_name,user_email;
    ImageView user_picture;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_events);

        inflater = LayoutInflater.from(getApplicationContext());
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mapBtn = findViewById(R.id.mapView);
        tabLayout.addTab(tabLayout.newTab().setText("Courants"));
        tabLayout.addTab(tabLayout.newTab().setText("Passés"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        final ViewPager viewPager = (ViewPager) findViewById(R.id.alertpager);

        View current = inflater.inflate(R.layout.fragment_events_current, null);
        View passed = inflater.inflate(R.layout.fragment_events_passed, null);

        List<View> fragmentList = new ArrayList<>();

        fragmentList.add(current);
        fragmentList.add(passed);

        final EventPagerAdapter adapter = new EventPagerAdapter(fragmentList, ListEventsActivity.this);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMap();
            }
        });

        InitSideMenu();
    }

    private void InitSideMenu() {
        toolbar = findViewById(R.id.toolbar);
        String timeStamp = new SimpleDateFormat("dd.MM.yy").format(new Date());
        toolbar.setTitle("Salut,aujourd'hui est " + timeStamp);
        DuoDrawerLayout drawerLayout = (DuoDrawerLayout) findViewById(R.id.drawerlayout);
        DuoDrawerToggle drawerToggle = new DuoDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);

        ArrayList<String> mMenuOptions = new ArrayList<>();
        mMenuOptions.add("Voir carte");
        mMenuOptions.add("Personnages Zouglou");
        mMenuOptions.add("Les coins chauds");
        mMenuOptions.add("Partager l'application");
        mMenuOptions.add("Termes et conditions");
        mMenuOptions.add("A propos");
        mMenuOptions.add("Quitter");

        drawerLayout.setDrawerListener(drawerToggle);
        drawerToggle.syncState();
        DuoMenuView duoMenuView = (DuoMenuView) findViewById(R.id.sidemenu);

        View headerView= duoMenuView.getHeaderView();
        user_name=headerView.findViewById(R.id.duo_header_title);
        user_email=headerView.findViewById(R.id.duo_header_sub_title);
        user_picture=headerView.findViewById(R.id.picture);

        User user= (User) Stash.getObject("facebook_user", User.class);

        Glide.with(getApplicationContext())
                .load(user.getPicture())
                .into(user_picture);
        user_name.setText(user.getName());
        user_email.setText(user.getEmail());

        DrawerListAdapter menuAdapter = new DrawerListAdapter(mMenuOptions);
        duoMenuView.setAdapter(menuAdapter);

        duoMenuView.setOnMenuClickListener(new DuoMenuView.OnMenuClickListener() {
            @Override
            public void onFooterClicked() {

            }

            @Override
            public void onHeaderClicked() {

            }

            @Override
            public void onOptionClicked(int position, Object objectClicked) {

                switch (position) {
                    case 0:
                        showMap();
                        break;
                    case 1:
                        Intent list_artists = new Intent(ListEventsActivity.this, ListArtistsActivity.class);
                        list_artists.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(list_artists);
                        break;
                    case 2:

                        break;
                    case 3:
                        ShareApp();
                        break;
                    case 4:

                        break;
                    case 5:

                        break;
                    case 6:
                        quitApp();
                        break;
                }
            }
        });
    }

    private void showMap() {
        Intent map = new Intent(ListEventsActivity.this, MapActivity.class);
        map.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(map);
    }

    private void ShareApp() {
        try {
            PackageManager pm = getPackageManager();
            ApplicationInfo ai = pm.getApplicationInfo(getPackageName(), 0);
            File srcFile = new File(ai.publicSourceDir);
            Log.e("ShareApp", ai.publicSourceDir);
            Intent share = new Intent();
            share.setAction(Intent.ACTION_SEND);
            share.setType("application/vnd.android.package-archive");
            share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(srcFile));
            startActivity(Intent.createChooser(share, "PersianCoders"));
        } catch (Exception e) {
            Log.e("ShareApp", e.getMessage());
        }
    }

    private void ShowArtistList() {

    }

    public void quitApp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            finishAffinity();
        }
        System.exit(0);
    }

}
