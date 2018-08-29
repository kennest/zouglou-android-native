package com.labs.botdev.zouglou.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.labs.botdev.zouglou.R;
import com.labs.botdev.zouglou.objectbox.Event;
import com.labs.botdev.zouglou.utils.AppController;

import java.util.List;

import io.objectbox.Box;

public class EventPagerAdapter extends PagerAdapter {
    private List<View> fragmentList;
    private Context context;
    ListView list;
    Box<Event> eventBox= AppController.boxStore.boxFor(Event.class);

    public EventPagerAdapter(List<View> fragmentList, Context ctx) {
        this.fragmentList = fragmentList;
        this.context = ctx;
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = null;
        switch (position) {
            case 0:
                view=fragmentList.get(0);
                list=view.findViewById(R.id.curent_events);
                list.setAdapter(new ListEventAdapter(eventBox.getAll(),context));
                break;
            case 1:
                view=fragmentList.get(1);
                break;
        }
        container.addView(view);
        return view;
    }
}
