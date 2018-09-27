package com.labs.botdev.zouglou.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SearchView;

import com.fxn.stash.Stash;
import com.gmail.samehadar.iosdialog.IOSDialog;
import com.labs.botdev.zouglou.R;
import com.labs.botdev.zouglou.models.Event;

import java.util.List;

public class EventPagerAdapter extends PagerAdapter {
    ListView list,list2;
    ListEventAdapter adapter;
    ListPassedEventAdapter adapter2;
    IOSDialog dialog,dialog2;
    private List<View> fragmentList;
    private Context context;

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
        SearchView mSearchView=null;
        SearchView mSearchView2=null;
        switch (position) {
            case 0:
                //Current Events
                dialog = LoaderProgress("Un instant", "Nous chargons les données");
                dialog.show();
                List<Event> events = Stash.getArrayList("events", Event.class);
                adapter = new ListEventAdapter(events, context);
                view = fragmentList.get(0);
                list = view.findViewById(R.id.curent_events);
                list.setAdapter(adapter);
                View empty=view.findViewById(R.id.empty_list);
                if(events.size()==0){
                    empty.setVisibility(View.VISIBLE);
                }
                 mSearchView = view.findViewById(R.id.searchview);
                 mSearchView.setQueryHint("Nom artiste,maquis...");
                 mSearchView.setIconified(true);
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
                dialog = LoaderProgress("Un instant", "Nous chargons les données");
                dialog.show();
                view = fragmentList.get(1);
                List<Event> events2 = Stash.getArrayList("passed_events", Event.class);
                adapter2 = new ListPassedEventAdapter(events2, context);
                View empty2=view.findViewById(R.id.empty_list);
                if(events2.size()==0){
                    empty2.setVisibility(View.VISIBLE);
                }
                list2 = view.findViewById(R.id.passed_events);
                list2.setAdapter(adapter2);

                mSearchView2 = view.findViewById(R.id.searchview);
                mSearchView2.setQueryHint("Nom artiste,maquis...");
                mSearchView2.setIconified(true);
                mSearchView2.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        //Toast.makeText(context,"Query Text "+newText,Toast.LENGTH_LONG).show();
                        adapter2.getFilter().filter(newText);
                        adapter2.notifyDataSetChanged();
                        return false;
                    }
                });
                dialog.dismiss();
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
