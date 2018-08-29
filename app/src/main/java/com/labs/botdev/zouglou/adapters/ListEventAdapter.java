package com.labs.botdev.zouglou.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.labs.botdev.zouglou.objectbox.Event;

import java.util.List;

public class ListEventAdapter extends BaseAdapter {

    private List<Event> eventList;
    private Context context;
    @Override
    public int getCount() {
        return eventList.size();
    }

    @Override
    public Event getItem(int position) {
        return eventList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }
}
