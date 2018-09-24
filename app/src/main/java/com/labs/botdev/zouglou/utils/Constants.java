package com.labs.botdev.zouglou.utils;

import android.content.Context;
import android.net.ConnectivityManager;

public class Constants {
    public static final String BASE_URL = "http://www.berakatravel.com/zouglou/public/";
    public static final String UPLOAD_URL = "http://www.berakatravel.com/zouglou/uploads/";

    //Check if Internet is available
    public static final boolean isNetworkConnected(Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }
}
