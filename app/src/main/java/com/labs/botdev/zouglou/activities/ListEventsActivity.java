package com.labs.botdev.zouglou.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.fxn.stash.Stash;
import com.labs.botdev.zouglou.R;
import com.labs.botdev.zouglou.adapters.DrawerListAdapter;
import com.labs.botdev.zouglou.adapters.EventPagerAdapter;
import com.labs.botdev.zouglou.adapters.ListArtistAdapter;
import com.labs.botdev.zouglou.adapters.ListPlaceAdapter;
import com.labs.botdev.zouglou.models.Artist;
import com.labs.botdev.zouglou.models.Customer;
import com.labs.botdev.zouglou.models.Event;
import com.labs.botdev.zouglou.models.Place;
import com.labs.botdev.zouglou.utils.AppController;
import com.labs.botdev.zouglou.utils.Constants;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

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
        downloadEventsPictures();
        downloadPlacesPictures();
    }

    private void InitSideMenu() {
        toolbar = findViewById(R.id.toolbar);
        //String timeStamp = new SimpleDateFormat("dd.MM.yy").format(new Date());
        toolbar.setTitle("Liste des evenements");
        DuoDrawerLayout drawerLayout = (DuoDrawerLayout) findViewById(R.id.drawerlayout);
        DuoDrawerToggle drawerToggle = new DuoDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);

        ArrayList<String> mMenuOptions = new ArrayList<>();
        mMenuOptions.add("Voir carte");
        mMenuOptions.add("Mes gars sûrs");
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
        user_picture = headerView.findViewById(R.id.picture);
        Button favPlace=headerView.findViewById(R.id.placetxt);
        Button favArtist=headerView.findViewById(R.id.artistxt);

        Set<String> user_places=Stash.getStringSet("user_places");
        Set<String> user_artists=Stash.getStringSet("user_artists");
        if(user_places!=null) {
            favPlace.setText(String.format(Locale.FRENCH, "%d coins chauds", user_places.size()));
        }

        if(user_artists!=null) {
            favArtist.setText(String.format(Locale.FRENCH, "%d gars sûrs", user_artists.size()));
        }

        favArtist.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(View v) {
                if (Stash.getStringSet("user_artists") != null) {
                    View view = getLayoutInflater().inflate(R.layout.activity_list_artist, null);
                    ListView list = view.findViewById(R.id.lv_artists);
                    SearchView searchView = view.findViewById(R.id.searchview);
                    Toolbar toolbar = view.findViewById(R.id.toolbar);
                    toolbar.setVisibility(View.GONE);
                    List<Artist> artists = new ArrayList<>(Stash.getArrayList("artists", Artist.class));
                    List<Artist> tmp = new ArrayList<>();
                    for (Artist a : artists) {
                        for (String s : user_artists) {
                            if (a.getId() == Integer.parseInt(s)) {
                                tmp.add(a);
                            }
                        }
                    }
                    ListArtistAdapter artistAdapter = new ListArtistAdapter(ListEventsActivity.this, tmp);
                    searchView.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
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
//                            artists = Stash.getArrayList("filter_artists", Artist.class);
//                            Stash.put("artists", artists);
                            }
                            return false;
                        }
                    });
                    list.setAdapter(artistAdapter);

                    AlertDialog.Builder builder = new AlertDialog.Builder(ListEventsActivity.this);
                    builder.setTitle("Artistes suivis")
                            .setView(view)
                            .setCancelable(false)
                            .setNegativeButton("Fermer", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // Customer cancelled the dialog
                                    dialog.dismiss();
                                }
                            });
                    // Create the AlertDialog object and return it
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });

        favPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Stash.getStringSet("user_places")!=null) {
                    View view = getLayoutInflater().inflate(R.layout.activity_list_places, null);
                    ListView list = view.findViewById(R.id.places_list);
                    SearchView searchView = view.findViewById(R.id.searchview);
                    Toolbar toolbar = view.findViewById(R.id.toolbar);
                    toolbar.setVisibility(View.GONE);
                    List<Place> places = new ArrayList<>(Stash.getArrayList("places", Place.class));
                    List<Place> tmp = new ArrayList<>();
                    for (Place a : places) {
                        for (String s : user_places) {
                            if (a.getId() == Integer.parseInt(s)) {
                                tmp.add(a);
                            }
                        }
                    }
                    ListPlaceAdapter placeAdapter = new ListPlaceAdapter(tmp, ListEventsActivity.this);
                    searchView.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextSubmit(String query) {
                            return false;
                        }

                        //Rechercher sur la carte(Not finished yet!!)
                        @Override
                        public boolean onQueryTextChange(String newText) {
                            if (placeAdapter != null) {
                                placeAdapter.getFilter().filter(newText);
                                placeAdapter.notifyDataSetChanged();
//                            artists = Stash.getArrayList("filter_artists", Artist.class);
//                            Stash.put("artists", artists);
                            }
                            return false;
                        }
                    });
                    list.setAdapter(placeAdapter);

                    AlertDialog.Builder builder = new AlertDialog.Builder(ListEventsActivity.this);
                    builder.setTitle("Places suivis")
                            .setView(view)
                            .setCancelable(false)
                            .setNegativeButton("Fermer", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // Customer cancelled the dialog
                                    dialog.dismiss();
                                }
                            });
                    // Create the AlertDialog object and return it
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });

        Customer customer = (Customer) Stash.getObject("facebook_user", Customer.class);

        Glide.with(getApplicationContext())
                .load(customer.getPicture())
                .into(user_picture);
        user_name.setText(customer.getName());

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

    private void downloadEventsPictures() {
    List<Event> events=Stash.getArrayList("events",Event.class);
        for(Event e:events){
            String url=Constants.UPLOAD_URL+e.getPicture();
            String outputfile =url.substring(url.lastIndexOf("/") + 1);
            File f = new File(Constants.EVENTS_PICTURES_DIR + outputfile);
            if(f.exists()){
                e.setPicture(Constants.EVENTS_PICTURES_DIR + outputfile);
            }else{
                String s=AppController.getInstance().downloadedPicture(Constants.UPLOAD_URL+e.getPicture());
                e.setPicture(s);
            }

        }
        Stash.put("events", events);
    }

    private void downloadPlacesPictures() {
        List<Place> places=Stash.getArrayList("places",Place.class);
        for(Place e:places){
            String url=Constants.UPLOAD_URL+e.getPicture();
            String outputfile =url.substring(url.lastIndexOf("/") + 1);
            File f = new File(Constants.EVENTS_PICTURES_DIR + outputfile);
            if(f.exists()){
                e.setPicture(Constants.EVENTS_PICTURES_DIR + outputfile);
            }else{
                String s=AppController.getInstance().downloadedPicture(Constants.UPLOAD_URL+e.getPicture());
                e.setPicture(s);
            }
        }

        Stash.put("places",places);
    }


    public void quitApp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            finishAffinity();
        }
        System.exit(0);
    }

}
