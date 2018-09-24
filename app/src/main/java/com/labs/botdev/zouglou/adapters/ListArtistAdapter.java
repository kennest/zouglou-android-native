package com.labs.botdev.zouglou.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.fxn.stash.Stash;
import com.labs.botdev.zouglou.R;
import com.labs.botdev.zouglou.activities.DetailsArtistActivity;
import com.labs.botdev.zouglou.services.models.Artist;
import com.labs.botdev.zouglou.services.models.Event;
import com.labs.botdev.zouglou.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ListArtistAdapter extends BaseAdapter implements Filterable {
    private Context context;
    List<com.labs.botdev.zouglou.services.models.Artist> filterArtists;
    private List<Artist> artists;
    private LayoutInflater inflater;
    private ListArtistAdapter.ValueFilter filter;

    public ListArtistAdapter(Context ctx, List<Artist> list) {
        this.context = ctx;
        this.artists = list;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return artists.size();
    }

    @Override
    public Artist getItem(int position) {
        return artists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = inflater.inflate(R.layout.artist_list_item, null);
        CircleImageView picture = v.findViewById(R.id.picture);
        TextView name = v.findViewById(R.id.name);

        Artist a = artists.get(position);

        Glide
                .with(context)
                .applyDefaultRequestOptions(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                .load(Constants.UPLOAD_URL + a.getAvatar())
                .into(picture);
        name.setText(a.getName());
        v.setTag(a.getId());
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent details = new Intent(context, DetailsArtistActivity.class);
                details.putExtra("artist_id", (int) v.getTag());
                context.startActivity(details);
            }
        });
        return v;
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new ListArtistAdapter.ValueFilter(artists, this);
        }
        return filter;
    }

    public class ValueFilter extends Filter {
        List<com.labs.botdev.zouglou.services.models.Artist> filterArtists;
        ListArtistAdapter adapter;

        ValueFilter(List<com.labs.botdev.zouglou.services.models.Artist> filterArtists, ListArtistAdapter adapter) {
            this.filterArtists = filterArtists;
            this.adapter = adapter;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            FilterResults results = new FilterResults();

            if (constraint != null && constraint.length() > 0) {

                ArrayList<Artist> filterList = new ArrayList<>();

                for (int i = 0; i < filterArtists.size(); i++) {
                    if ((filterArtists.get(i).getName().toUpperCase()).contains(constraint.toString().toUpperCase())) {
                        filterList.add(filterArtists.get(i));
                    }
                }
                results.count = filterList.size();
                results.values = filterList;
            } else {
                results.count = filterArtists.size();
                results.values = filterArtists;
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            adapter.artists = (List<com.labs.botdev.zouglou.services.models.Artist>) results.values;
            Stash.put("filter_artists",adapter.artists);
            Toast.makeText(adapter.context,"Filtering artists "+adapter.artists.size(),Toast.LENGTH_LONG).show();
            adapter.notifyDataSetChanged();
        }
    }
}
