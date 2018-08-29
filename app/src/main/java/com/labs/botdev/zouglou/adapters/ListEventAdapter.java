package com.labs.botdev.zouglou.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.labs.botdev.zouglou.R;
import com.labs.botdev.zouglou.objectbox.Artist;
import com.labs.botdev.zouglou.objectbox.Event;
import com.labs.botdev.zouglou.objectbox.Place;
import com.labs.botdev.zouglou.objectbox.Place_;
import com.labs.botdev.zouglou.utils.AppController;

import java.util.List;

import io.objectbox.Box;

public class ListEventAdapter extends BaseAdapter {

    private List<Event> eventList;
    private Context context;
    private LayoutInflater inflater;
    Box<Event> eventBox= AppController.boxStore.boxFor(Event.class);
    Box<Artist> artistBox=AppController.boxStore.boxFor(Artist.class);
    Box<Place> placeBox=AppController.boxStore.boxFor(Place.class);

    public ListEventAdapter(List<Event> eventList, Context context) {
        this.eventList = eventList;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

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
    public View getView(int position, View view, ViewGroup parent) {
        String base_url="http://www.berakatravel.com/zouglou/public/uploads/";
        view = inflater.inflate(R.layout.event_item, parent, false);

        Event e=eventList.get(position);
        Place p=placeBox.query().equal(Place_.raw_id,e.getPlace_id()).build().findFirst();

        ImageView picture=view.findViewById(R.id.picture);
        TextView title=view.findViewById(R.id.title);
        TextView artists=view.findViewById(R.id.artists);
        TextView place=view.findViewById(R.id.place);
        TextView description=view.findViewById(R.id.description);
        TextView date=view.findViewById(R.id.date);

        String url =   base_url+e.getPicture();
        Glide
                .with(context)
                .load(url)
                .into(picture);

        title.setText(e.getTitle());
        description.setText(e.getDescription());
        place.setText(p.getTitle());
        date.setText(String.format("%s/%s", e.getBegin(), e.getEnd()));
        return view;
    }
}
