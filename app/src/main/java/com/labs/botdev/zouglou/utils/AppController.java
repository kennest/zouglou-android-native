package com.labs.botdev.zouglou.utils;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.fxn.stash.Stash;
import com.google.android.gms.ads.MobileAds;
import com.labs.botdev.zouglou.R;
import com.labs.botdev.zouglou.services.APIClient;
import com.labs.botdev.zouglou.services.APIService;

import java.io.File;

public class AppController extends MultiDexApplication {
    static APIService service;
    private static AppController mInstance;
    MediaPlayer mp;
    FetchDownloader fetch=new FetchDownloader();

    public static synchronized AppController getInstance() {
        return mInstance;
    }

    @SuppressLint("CheckResult")
    @Override
    public void onCreate() {
        super.onCreate();
        Stash.init(this);
        mInstance = this;
        //FastSave.init(getApplicationContext());
        service = APIClient.getClient().create(APIService.class);
        //boxStore = MyObjectBox.builder().androidContext(this).build();
        //InitFacebook();
    }

    public void quitApp() {
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }

    public void playSound(String fileName) {
        mp = new MediaPlayer();
        try {
            AssetFileDescriptor afd = getApplicationContext().getAssets().openFd(fileName);
            mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
            mp.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mp.start();
    }

    public String downloadedPicture(String url){
        String pic="";
        String outputfile=url.substring(url.lastIndexOf("/") + 1);
        File imgFile = new  File(Constants.EVENTS_PICTURES_DIR + outputfile);
        if(imgFile.exists()){
            pic=Constants.EVENTS_PICTURES_DIR + outputfile;
        }else {
            pic = fetch.downloadFile(this, url, Constants.EVENTS_PICTURES_DIR + outputfile);
            Log.e("Activity Dir",Constants.EVENTS_PICTURES_DIR + outputfile);
        }
        return pic;
    }
}
