package com.labs.botdev.zouglou.utils;

import android.app.Application;

public class AppController extends Application {
    private static AppController mInstance;
    public static synchronized AppController getInstance() {
        return mInstance;
    }
}
