package com.labs.botdev.zouglou.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.labs.botdev.zouglou.R;
import com.labs.botdev.zouglou.activities.DetailsArtistActivity;
import com.labs.botdev.zouglou.services.models.Artist;
import com.labs.botdev.zouglou.utils.Constants;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ListArtistAdapter extends BaseAdapter {
    private Context context;
    private List<Artist> artists;
    private LayoutInflater inflater;

    public ListArtistAdapter(Context ctx, List<Artist> list) {
        this.context = ctx;
        this.artists = list;
        this.inflater=LayoutInflater.from(context);
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
        View v=inflater.inflate(R.layout.artist_list_item,null);
        CircleImageView picture=v.findViewById(R.id.picture);
        TextView name=v.findViewById(R.id.name);

        Artist a=artists.get(position);

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
                Intent details=new Intent(context, DetailsArtistActivity.class);
                details.putExtra("artist_id", (int) v.getTag());
                context.startActivity(details);
            }
        });
        return v;
    }
}
