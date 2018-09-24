package com.labs.botdev.zouglou.utils;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.multidex.MultiDexApplication;
import com.fxn.stash.Stash;
import com.labs.botdev.zouglou.services.APIClient;
import com.labs.botdev.zouglou.services.APIService;

public class AppController extends MultiDexApplication {
    static APIService service;
    private static AppController mInstance;

    public static synchronized AppController getInstance() {
        return mInstance;
    }

    @SuppressLint("CheckResult")
    @Override
    public void onCreate() {
        super.onCreate();
        Stash.init(this);
        //FastSave.init(getApplicationContext());
        service = APIClient.getClient().create(APIService.class);
        //boxStore = MyObjectBox.builder().androidContext(this).build();
        //InitFacebook();

        if (Constants.isNetworkConnected(getApplicationContext())) {
            SyncData();
        }
    }

    private void SyncData() {
        //SyncEvents();
    }

    public void quitApp() {
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }
}
