package com.labs.botdev.zouglou.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.labs.botdev.zouglou.utils.Constants;
import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.SubscriptionEventListener;

public class PusherEventService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Constants.isNetworkConnected(getApplicationContext())) {
            PusherOptions options = new PusherOptions();
            options.setCluster("eu");
            Pusher pusher = new Pusher("414e2fd5843af1c2865d", options);
            Channel channel = pusher.subscribe("zouglou");

            channel.bind("event-added", new SubscriptionEventListener() {
                @Override
                public void onEvent(String channelName, String eventName, final String data) {
                    Toast.makeText(getApplicationContext(), "Zouglou Event added...", Toast.LENGTH_LONG).show();
                    System.out.println(data);
                }
            });

            pusher.connect();
        }
    }
}
