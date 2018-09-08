package com.labs.botdev.zouglou.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.labs.botdev.zouglou.R;
import com.labs.botdev.zouglou.activities.DetailsEventActivity;
import com.labs.botdev.zouglou.objectbox.Artist_;
import com.labs.botdev.zouglou.objectbox.Event;
import com.labs.botdev.zouglou.objectbox.Place;
import com.labs.botdev.zouglou.objectbox.Place_;
import com.labs.botdev.zouglou.services.models.Artist;
import com.labs.botdev.zouglou.utils.AppController;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.objectbox.Box;

public class ListEventAdapter extends BaseAdapter implements Filterable{

    private List<com.labs.botdev.zouglou.services.models.Event> eventList;
    private Context context;
    private LayoutInflater inflater;
    List<com.labs.botdev.zouglou.services.models.Event> filterEvents;
    private ValueFilter filter;

    public ListEventAdapter(List<com.labs.botdev.zouglou.services.models.Event> eventList, Context context) {
        this.eventList = eventList;
        this.context = context;
        this.filterEvents=eventList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return eventList.size();
    }

    @Override
    public com.labs.botdev.zouglou.services.models.Event getItem(int position) {
        return eventList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        String base_url="http://www.berakatravel.com/zouglou/public/uploads/";
        view = inflater.inflate(R.layout.event_item, parent, false);
        com.labs.botdev.zouglou.services.models.Event e=eventList.get(position);

        ImageView picture=view.findViewById(R.id.picture);
        TextView title=view.findViewById(R.id.title);
        TextView artists=view.findViewById(R.id.artists);
        TextView place=view.findViewById(R.id.place);
        //TextView date=view.findViewById(R.id.date);

        view.setTag(e.getId());

        String url =   base_url+e.getPicture();
        Glide
                .with(context)
                .load(url)
                .into(picture);

        title.setText(e.getTitle());
        place.setText(Objects.requireNonNull(e.place.getTitle()));

        String artist_str="";
        for(Artist a:e.artists){
            artist_str = a.getName() + ",";
        }

        artists.setText(artist_str);
        //date.setText(String.format("%s/%s", e.getBegin(), e.getEnd()));
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent details=new Intent(context, DetailsEventActivity.class);
                details.putExtra("event_id", (int) v.getTag());
                context.startActivity(details);
            }
        });
        return view;
    }

    @Override
    public Filter getFilter() {
        if(filter==null)
        {
            filter=new ValueFilter(filterEvents,this);
        }
        return filter;
    }

    public class ValueFilter extends Filter {
        List<com.labs.botdev.zouglou.services.models.Event> filterEvents;
        ListEventAdapter adapter;

        ValueFilter(List<com.labs.botdev.zouglou.services.models.Event> filterEvents, ListEventAdapter adapter) {
            this.filterEvents = filterEvents;
            this.adapter = adapter;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            FilterResults results = new FilterResults();

            if (constraint != null && constraint.length() > 0) {

                ArrayList<com.labs.botdev.zouglou.services.models.Event> filterList = new ArrayList<>();

                for (int i = 0; i < filterEvents.size(); i++) {
                    if ((filterEvents.get(i).getTitle().toUpperCase()).contains(constraint.toString().toUpperCase())) {
                        filterList.add(filterEvents.get(i));
                    }
                }
                results.count = filterList.size();
                results.values = filterList;
            } else {
                results.count = filterEvents.size();
                results.values = filterEvents;
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            adapter.eventList = (List<com.labs.botdev.zouglou.services.models.Event>) results.values;
            adapter.notifyDataSetChanged();
        }
    }
}
