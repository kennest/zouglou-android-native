package com.labs.botdev.zouglou.services;

import android.os.Environment;

import com.labs.botdev.zouglou.utils.Constants;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class APIClient {
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        int cacheSize = 40 * 1024 * 1024; // 40 MiB
        File cacheDirectory=new File(Environment.getDataDirectory().getAbsolutePath()+"/zouglou_okhttp_cache/");
        Cache cache = new Cache(cacheDirectory, cacheSize);

        OkHttpClient client = new OkHttpClient.Builder()
                .cache(cache)
                .connectTimeout(500,TimeUnit.SECONDS)
                .writeTimeout(500, TimeUnit.SECONDS)
                .readTimeout(500, TimeUnit.SECONDS)
                .addInterceptor(interceptor)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        return retrofit;
    }
}
