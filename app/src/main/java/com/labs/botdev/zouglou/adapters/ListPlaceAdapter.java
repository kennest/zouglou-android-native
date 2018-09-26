package com.labs.botdev.zouglou.adapters;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.fxn.stash.Stash;
import com.labs.botdev.zouglou.R;
import com.labs.botdev.zouglou.activities.DoNavigationActivity;
import com.labs.botdev.zouglou.services.models.Event;
import com.labs.botdev.zouglou.services.models.Place;
import com.labs.botdev.zouglou.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class ListPlaceAdapter extends BaseAdapter implements Filterable {
    private List<Place> placeList;
    private Activity activity;
    private LayoutInflater inflater;
    private List<Place> filterPlaces;
    private ListPlaceAdapter.ValueFilter filter;

    public ListPlaceAdapter(List<Place> placeList, Activity activity) {
        this.placeList = placeList;
        this.activity = activity;
        this.filterPlaces=placeList;
        this.inflater = LayoutInflater.from(activity);
    }

    @Override
    public int getCount() {
        return placeList.size();
    }

    @Override
    public Object getItem(int position) {
        return placeList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView=inflater.inflate(R.layout.place_item,null);
        TextView placename,details;
        ImageView picture=convertView.findViewById(R.id.picture);
        placename=convertView.findViewById(R.id.placename);
        details=convertView.findViewById(R.id.details);
        FloatingActionButton navigation=convertView.findViewById(R.id.navigation);
        Place p=placeList.get(position);

        Glide.with(activity)
                .applyDefaultRequestOptions(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                .load(Constants.UPLOAD_URL+p.getPicture())
                .into(picture);

        placename.setText(p.getTitle());
        details.setText(p.address.getCommune()+" ," +p.events.size() +" evenement(s)");

        navigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowOnMap(Double.parseDouble(p.address.getLatitude()),Double.parseDouble(p.address.getLongitude()));
            }
        });
        return convertView;
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new ValueFilter(filterPlaces, this);
        }
        return filter;
    }

    public class ValueFilter extends Filter {
        List<com.labs.botdev.zouglou.services.models.Place> filterPlaces;
        ListPlaceAdapter adapter;

        ValueFilter(List<com.labs.botdev.zouglou.services.models.Place> filterPlaces, ListPlaceAdapter adapter) {
            this.filterPlaces = filterPlaces;
            this.adapter = adapter;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            FilterResults results = new FilterResults();

            if (constraint != null && constraint.length() > 0) {

                ArrayList<Place> filterList = new ArrayList<>();

                for (int i = 0; i < filterPlaces.size(); i++) {
                    if ((filterPlaces.get(i).getTitle().toUpperCase()).contains(constraint.toString().toUpperCase())) {
                        filterList.add(filterPlaces.get(i));
                    }
                }
                results.count = filterList.size();
                results.values = filterList;
            } else {
                results.count = filterPlaces.size();
                results.values = filterPlaces;
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            adapter.placeList = (List<Place>) results.values;
            adapter.notifyDataSetChanged();
        }
    }

    protected void ShowOnMap(Double lat, Double lon) {
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + lat + "," + lon + "");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(mapIntent);
        } else {
            Intent navigation = new Intent(activity, DoNavigationActivity.class);
            String destination = lat + ":" + lon;
            navigation.putExtra("destination", destination);
            activity.startActivity(navigation);
        }
    }
}
