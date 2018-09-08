package com.labs.botdev.zouglou.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SearchView;

import com.appizona.yehiahd.fastsave.FastSave;
import com.gmail.samehadar.iosdialog.IOSDialog;
import com.labs.botdev.zouglou.R;
import com.labs.botdev.zouglou.activities.MapActivity;
import com.labs.botdev.zouglou.services.models.Event;
import com.labs.botdev.zouglou.utils.AppController;

import java.util.List;

import io.objectbox.Box;

public class EventPagerAdapter extends PagerAdapter {
    private List<View> fragmentList;
    private Context context;
    ListView list;
    ListEventAdapter adapter;
    IOSDialog dialog;

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
                //Current Events
                dialog = LoaderProgress("Un instant", "Nous chargons les données");
                dialog.show();
               List<Event> events= FastSave.getInstance().getObjectsList("events", Event.class);
                adapter = new ListEventAdapter(events, context);
                view=fragmentList.get(0);
                list=view.findViewById(R.id.curent_events);
                list.setAdapter(adapter);

                SearchView mSearchView =view.findViewById(R.id.search_view);
                mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        //Toast.makeText(context,"Query Text "+newText,Toast.LENGTH_LONG).show();
                        adapter.getFilter().filter(newText);
                        adapter.notifyDataSetChanged();
                        return false;
                    }
                });
                dialog.dismiss();
                break;
            case 1:
                //Passed Events
                view=fragmentList.get(1);
                break;
        }
        container.addView(view);
        return view;
    }

    public IOSDialog LoaderProgress(String title, String content) {
        final IOSDialog dialog = new IOSDialog.Builder(context)
                .setTitle(title)
                .setMessageContent(content)
                .setSpinnerColorRes(R.color.colorPrimary)
                .setCancelable(false)
                .setTitleColorRes(R.color.white)
                .setMessageContentGravity(Gravity.END)
                .build();
        return dialog;
    }

}
