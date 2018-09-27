package com.labs.botdev.zouglou.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.fxn.stash.Stash;
import com.google.gson.JsonObject;
import com.labs.botdev.zouglou.R;
import com.labs.botdev.zouglou.activities.DetailsPlaceActivity;
import com.labs.botdev.zouglou.activities.DoNavigationActivity;
import com.labs.botdev.zouglou.models.Customer;
import com.labs.botdev.zouglou.services.APIClient;
import com.labs.botdev.zouglou.services.APIService;
import com.labs.botdev.zouglou.models.FavoritePlace;
import com.labs.botdev.zouglou.models.Place;
import com.labs.botdev.zouglou.utils.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListPlaceAdapter extends BaseAdapter implements Filterable {
    private List<Place> placeList;
    private Activity activity;
    private LayoutInflater inflater;
    private List<Place> filterPlaces;
    private ListPlaceAdapter.ValueFilter filter;
    Set<String> user_places = new HashSet<>();
    MediaPlayer mp;

    public ListPlaceAdapter(List<Place> placeList, Activity activity) {
        this.placeList = placeList;
        this.activity = activity;
        this.filterPlaces = placeList;
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

    @SuppressLint("RestrictedApi")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.place_item, null);
        TextView placename, details;
        ImageView picture = convertView.findViewById(R.id.picture);
        placename = convertView.findViewById(R.id.placename);
        details = convertView.findViewById(R.id.details);
        ImageView followed = convertView.findViewById(R.id.followed);
        FloatingActionButton navigation = convertView.findViewById(R.id.navigation);
        FloatingActionButton bookmark = convertView.findViewById(R.id.bookmark);


        Place p = placeList.get(position);

        if (Stash.getStringSet("user_places", new HashSet<>()) != null) {
            Set<String> favoritePlaces = Stash.getStringSet("user_places", new HashSet<>());
            for (String x : favoritePlaces) {
                if (p.getId() == Integer.parseInt(x)) {
                    bookmark.setEnabled(false);
                    bookmark.setClickable(false);
                    bookmark.setFocusable(false);
                    bookmark.setVisibility(View.GONE);
                    followed.setVisibility(View.VISIBLE);
                }
            }
        }

        Glide.with(activity)
                .applyDefaultRequestOptions(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                .load(Constants.UPLOAD_URL + p.getPicture())
                .into(picture);

        placename.setText(p.getTitle());
        details.setText(p.address.getCommune() + " ," + p.events.size() + " evenement(s)");

        navigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowOnMap(Double.parseDouble(p.address.getLatitude()), Double.parseDouble(p.address.getLongitude()));
            }
        });
        bookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Constants.isNetworkConnected(activity)) {
                    APIService service = APIClient.getClient().create(APIService.class);
                    Customer u = (Customer) Stash.getObject("facebook_user", Customer.class);
                    FavoritePlace favoritePlace = new FavoritePlace();
                    favoritePlace.setCustomer_id(u.getId());
                    favoritePlace.setPlace_id(p.getId());
                    Call<JsonObject> call = service.setFavoritePlace(favoritePlace);
                    call.enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                            if (response.code() == 200 && response.body().get("response") != null && response.body().get("response").getAsString() != "error") {
                                String place = response.body().get("response").getAsString();
                                Log.e("Response place ID:", "" + place);
                                if (Stash.getStringSet("user_places", new HashSet<>()) != null) {
                                    user_places = Stash.getStringSet("user_places", new HashSet<>());
                                }
                                user_places.add(place);
                                Stash.put("user_places", user_places);
                                playSound("store.mp3");
                                notifyDataSetChanged();
                            } else {
                                try {
                                    call.execute();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<JsonObject> call, Throwable t) {
                            Toast.makeText(activity, "Error:" + t.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    Toast.makeText(activity, "Vous devez être connecté à internet pour cela.", Toast.LENGTH_LONG).show();
                }
            }
        });

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent details = new Intent(activity, DetailsPlaceActivity.class);
                details.putExtra("place_id", p.getId());
                activity.startActivity(details);
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

    public class ValueFilter extends Filter {
        List<Place> filterPlaces;
        ListPlaceAdapter adapter;

        ValueFilter(List<Place> filterPlaces, ListPlaceAdapter adapter) {
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

    private void playSound(String fileName) {
        mp = new MediaPlayer();
        try {
            AssetFileDescriptor afd = activity.getAssets().openFd(fileName);
            mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
            mp.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mp.start();
    }
}
