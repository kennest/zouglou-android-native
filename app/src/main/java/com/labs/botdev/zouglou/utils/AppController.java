package com.labs.botdev.zouglou.utils;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import com.labs.botdev.zouglou.objectbox.Address;
import com.labs.botdev.zouglou.objectbox.Artist;
import com.labs.botdev.zouglou.objectbox.Event;
import com.labs.botdev.zouglou.objectbox.MyObjectBox;
import com.labs.botdev.zouglou.objectbox.Place;
import com.labs.botdev.zouglou.services.APIClient;
import com.labs.botdev.zouglou.services.APIService;
import com.labs.botdev.zouglou.services.models.EventsResponse;
import java.util.ArrayList;
import java.util.List;
import io.objectbox.Box;
import io.objectbox.BoxStore;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class AppController extends Application {
    private static AppController mInstance;
    public static BoxStore boxStore;

    public static synchronized AppController getInstance() {
        return mInstance;
    }

    static APIService service;

    @Override
    public void onCreate() {
        super.onCreate();
        service = APIClient.getClient().create(APIService.class);
        boxStore = MyObjectBox.builder().androidContext(this).build();
        SyncData();
    }

    private void SyncData() {
        SyncEvents();
    }

    private void SyncEvents() {
        Box<Event> eventBox = AppController.boxStore.boxFor(com.labs.botdev.zouglou.objectbox.Event.class);
        Box<Place> placeBox=AppController.boxStore.boxFor(Place.class);
        Box<Address> addressBox=AppController.boxStore.boxFor(Address.class);
        Box<Artist> artistBox=AppController.boxStore.boxFor(Artist.class);
        Observer mObserver = new Observer<EventsResponse>() {

            @Override
            public void onSubscribe(Disposable disposable) {
                eventBox.removeAll();
                placeBox.removeAll();
                addressBox.removeAll();
                artistBox.removeAll();
                //Toast.makeText(getApplicationContext(), "events load Init", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNext(EventsResponse response) {
                for (com.labs.botdev.zouglou.services.models.Event e : response.getEvents()) {
                    Toast.makeText(getApplicationContext(), "Event title: " + e.getTitle(), Toast.LENGTH_LONG).show();
                    com.labs.botdev.zouglou.objectbox.Event eb = new com.labs.botdev.zouglou.objectbox.Event();
                    Place place=new Place();
                    Address address=new Address();
                    List<Artist> artists=new ArrayList<>();
                    Artist a=new Artist();

                    eb.setRaw_id(e.getId());
                    eb.setTitle(e.getTitle());
                    eb.setDescription(e.getDescription());
                    eb.setBegin(e.getBegin());
                    eb.setEnd(e.getEnd());
                    eb.setPicture(e.getPicture());

                    place.setRaw_id(e.place.getId());
                    place.setPicture(e.place.getPicture());
                    place.setTitle(e.place.getTitle());

                    eb.setPlace_id(place.getRaw_id());

                    placeBox.put(place);

                    address.setLatitude(Double.parseDouble(e.place.address.getLatitude()));
                    address.setLongitude(Double.parseDouble(e.place.address.getLongitude()));
                    address.setRaw_id(e.place.address.getId());
                    address.setCommune(e.place.address.getCommune());
                    address.setQuartier(e.place.address.getQuartier());
                    address.setPlace_id(place.getRaw_id());
                    addressBox.put(address);

                    eventBox.put(eb);
                }
            }

            @Override
            public void onError(Throwable e) {
                //Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("OBS error: ", e.getMessage());
            }

            @Override
            public void onComplete() {
                Toast.makeText(getApplicationContext(), "App Event total: " + eventBox.count(), Toast.LENGTH_LONG).show();
                Toast.makeText(getApplicationContext(), "App Place total: " + placeBox.count(), Toast.LENGTH_LONG).show();
                Toast.makeText(getApplicationContext(), "App Address total: " + addressBox.count(), Toast.LENGTH_LONG).show();
            }
        };
        Observable<EventsResponse> observable = service.getEventsList();
        observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(mObserver);
    }

}
