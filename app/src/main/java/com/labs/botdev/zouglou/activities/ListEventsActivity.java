package com.labs.botdev.zouglou.activities;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.fxn.stash.Stash;
import com.labs.botdev.zouglou.R;
import com.labs.botdev.zouglou.adapters.DrawerListAdapter;
import com.labs.botdev.zouglou.adapters.EventPagerAdapter;
import com.labs.botdev.zouglou.models.Customer;

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
    TextView user_name, user_stats;
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

        View headerView = duoMenuView.getHeaderView();
        user_name = headerView.findViewById(R.id.duo_header_title);
        user_stats = headerView.findViewById(R.id.stats);
        user_picture = headerView.findViewById(R.id.picture);

        Customer customer = (Customer) Stash.getObject("facebook_user", Customer.class);

        Glide.with(getApplicationContext())
                .load(customer.getPicture())
                .into(user_picture);
        user_name.setText(customer.getName());
        user_stats.setText(customer.getEmail());

        DrawerListAdapter menuAdapter = new DrawerListAdapter(mMenuOptions);
        duoMenuView.setAdapter(menuAdapter);

        duoMenuView.setOnMenuClickListener(new DuoMenuView.OnMenuClickListener() {
            @Override
            public void onFooterClicked() {

                AlertDialog.Builder builder = new AlertDialog.Builder(ListEventsActivity.this);
                builder.setMessage("Vous êtes sur le point de vous déconnecter et de remettre à zero les données de l'application?")
                        .setCancelable(false)
                        .setPositiveButton("Oui,je le veux", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // FIRE ZE MISSILES!
                                clearAppData();
                            }
                        })
                        .setNegativeButton("Non,je refuse", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Customer cancelled the dialog
                                dialog.dismiss();
                            }
                        });
                // Create the AlertDialog object and return it
                AlertDialog dialog = builder.create();
                dialog.show();

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
                        Intent list_places = new Intent(ListEventsActivity.this, ListPlacesActivity.class);
                        list_places.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(list_places);
                        break;
                    case 3:
                        ShareApp();
                        break;
                    case 4:

                        break;
                    case 5:

                        break;
                    case 6:
                        AlertDialog.Builder builder = new AlertDialog.Builder(ListEventsActivity.this);
                        builder.setMessage("Voulez-vous quitter l'application?")
                                .setCancelable(false)
                                .setPositiveButton("Oui,je le veux", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // FIRE ZE MISSILES!
                                        quitApp();
                                    }
                                })
                                .setNegativeButton("Non,je refuse", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // Customer cancelled the dialog
                                        dialog.dismiss();
                                    }
                                });
                        // Create the AlertDialog object and return it
                        AlertDialog dialog = builder.create();
                        dialog.show();

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

    private void clearAppData() {
        try {
            // clearing app data
            if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) {
                ((ActivityManager) getSystemService(ACTIVITY_SERVICE)).clearApplicationUserData(); // note: it has a return value!
            } else {
                String packageName = getApplicationContext().getPackageName();
                Runtime runtime = Runtime.getRuntime();
                runtime.exec("pm clear " + packageName);
            }

        } catch (Exception e) {
            e.printStackTrace();
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
