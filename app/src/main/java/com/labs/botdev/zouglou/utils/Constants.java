package com.labs.botdev.zouglou.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Environment;

public class Constants {
    public static final String BASE_URL = "http://www.berakatravel.com/zouglou/public/";
    public static final String UPLOAD_URL = "http://www.berakatravel.com/zouglou/uploads/";
    public static final String EVENTS_PICTURES_DIR= Environment.getExternalStorageDirectory().getAbsolutePath()+"/pictures/"+AppController.getInstance().getPackageName()+"/events/";

    //Check if Internet is available
    public static final boolean isNetworkConnected(Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }
}
