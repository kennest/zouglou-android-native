package com.labs.botdev.zouglou.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import com.labs.botdev.zouglou.R;
import com.labs.botdev.zouglou.adapters.DrawerListAdapter;
import com.labs.botdev.zouglou.adapters.EventPagerAdapter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.objectbox.Box;
import nl.psdcompany.duonavigationdrawer.views.DuoDrawerLayout;
import nl.psdcompany.duonavigationdrawer.views.DuoMenuView;
import nl.psdcompany.duonavigationdrawer.widgets.DuoDrawerToggle;

public class ListEventsActivity extends Activity {
    Toolbar toolbar;
    LayoutInflater inflater;
    FloatingActionButton mapBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_events);

        inflater = LayoutInflater.from(getApplicationContext());
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mapBtn=findViewById(R.id.mapView);
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
                Intent map=new Intent(ListEventsActivity.this,MapActivity.class);
                map.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                startActivity(map);
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
        mMenuOptions.add("Liste artistes");
        mMenuOptions.add("Termes et conditions");
        mMenuOptions.add("A propos");
        mMenuOptions.add("Quitter");

        drawerLayout.setDrawerListener(drawerToggle);
        drawerToggle.syncState();
        DuoMenuView duoMenuView = (DuoMenuView) findViewById(R.id.sidemenu);
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

                        break;
                    case 1:


                        break;
                    case 2:


                        break;
                    case 3:

                        break;
                    case 4:

                        break;
                    case 5:
                        finishAffinity();
                }
            }
        });
    }

}
