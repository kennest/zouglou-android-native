package com.labs.botdev.zouglou.utils;

import android.annotation.SuppressLint;
import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import com.appizona.yehiahd.fastsave.FastSave;
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork;
import com.labs.botdev.zouglou.services.APIClient;
import com.labs.botdev.zouglou.services.APIService;
import com.labs.botdev.zouglou.services.models.EventsResponse;
import java.util.ArrayList;
import io.objectbox.BoxStore;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class AppController extends Application {
    private static AppController mInstance;
    public static BoxStore boxStore;

    public static synchronized AppController getInstance() {
        return mInstance;
    }

    static APIService service;

    @SuppressLint("CheckResult")
    @Override
    public void onCreate() {
        super.onCreate();
        service = APIClient.getClient().create(APIService.class);
        FastSave.init(getApplicationContext());
        //boxStore = MyObjectBox.builder().androidContext(this).build();

        ReactiveNetwork.checkInternetConnectivity()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        if (aBoolean) {
                            SyncData();
                        }
                    }
                });
    }

    private void SyncData() {
        //SyncEvents();
    }

}
