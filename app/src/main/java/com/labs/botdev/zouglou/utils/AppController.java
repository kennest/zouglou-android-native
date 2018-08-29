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
import com.labs.botdev.zouglou.services.models.ArtistsResponse;
import com.labs.botdev.zouglou.services.models.EventsResponse;
import com.labs.botdev.zouglou.services.models.PlacesResponse;

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
        SyncArtist();
        SyncPlaces();
        SyncEvents();
    }

    private void SyncEvents() {
        Box<Event> eventBox = AppController.boxStore.boxFor(com.labs.botdev.zouglou.objectbox.Event.class);
        Observer mObserver = new Observer<EventsResponse>() {

            @Override
            public void onSubscribe(Disposable disposable) {
                eventBox.removeAll();
                Toast.makeText(getApplicationContext(), "events load Init", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNext(EventsResponse response) {
                for (com.labs.botdev.zouglou.services.models.Event e : response.getEvents()) {
                    Toast.makeText(getApplicationContext(), "Event title: " + e.getTitle(), Toast.LENGTH_LONG).show();
                    com.labs.botdev.zouglou.objectbox.Event eb = new com.labs.botdev.zouglou.objectbox.Event();
                    eb.setRaw_id(e.getId());
                    eb.setTitle(e.getTitle());
                    eb.setDescription(e.getDescription());
                    eb.setBegin(e.getBegin());
                    eb.setEnd(e.getEnd());
                    eb.setPicture(e.getPicture());
                    eventBox.put(eb);
                }
            }

            @Override
            public void onError(Throwable e) {
                Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("OBS error: ", e.getMessage());
            }

            @Override
            public void onComplete() {
                Toast.makeText(getApplicationContext(), "Event total: " + eventBox.count(), Toast.LENGTH_LONG).show();
            }
        };
        Observable<EventsResponse> observable = service.getEventsList();
        observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(mObserver);
    }

    private void SyncPlaces() {
        Box<Place> placeBox = AppController.boxStore.boxFor(Place.class);
        Box<Address> addressBox = AppController.boxStore.boxFor(Address.class);
        Observer mObserver = new Observer<PlacesResponse>() {
            @Override
            public void onSubscribe(Disposable disposable) {
                placeBox.removeAll();
                addressBox.removeAll();
                Toast.makeText(getApplicationContext(), "places load Init", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNext(PlacesResponse response) {

                for(com.labs.botdev.zouglou.services.models.Place n:response.getPlaces()) {
                    Place p = new Place();
                    Address a = new Address();

                    p.setTitle(n.getTitle());
                    p.setPicture(n.getPicture());
                    p.setRaw_id(n.getId());
                    a.setCommune(n.getAddress().getCommune());
                    a.setRaw_id(n.getAddress().getId());
                    a.setQuartier(n.getAddress().getQuartier());
                    a.setLatitude(Double.parseDouble(n.getAddress().getLatitude()));
                    a.setLongitude(Double.parseDouble(n.getAddress().getLongitude()));

                    p.address.setTarget(a);

                    addressBox.put(a);
                    placeBox.put(p);
                }
            }

            @Override
            public void onError(Throwable e) {
                Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("OBS error: ", e.getMessage());
            }

            @Override
            public void onComplete() {
                Toast.makeText(getApplicationContext(), "Place total: " + placeBox.count(), Toast.LENGTH_LONG).show();
            }
        };

        Observable<PlacesResponse> observable = service.getPlaces();
        observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(mObserver);
    }

    private void SyncArtist() {
        Box<Artist> artistBox = AppController.boxStore.boxFor(Artist.class);
        Observer mObserver = new Observer<ArtistsResponse>() {

            @Override
            public void onSubscribe(Disposable disposable) {
                artistBox.removeAll();
                Toast.makeText(getApplicationContext(), "artist load Init", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNext(ArtistsResponse r) {
                for(com.labs.botdev.zouglou.services.models.Artist n:r.getArtists()) {
                    Artist a = new Artist();
                    a.setAvatar(n.getAvatar());
                    a.setRaw_id(n.getId());
                    a.setName(n.getName());
                    a.setSample(n.getSample());
                    artistBox.put(a);
                }
            }

            @Override
            public void onError(Throwable e) {
                Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("OBS error: ", e.getMessage());
            }

            @Override
            public void onComplete() {
                Toast.makeText(getApplicationContext(), "Artist total: " + artistBox.count(), Toast.LENGTH_LONG).show();
            }
        };
        Observable<ArtistsResponse> Eventsobservable = service.getArtistsList();
        Eventsobservable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(mObserver);
    }

}
