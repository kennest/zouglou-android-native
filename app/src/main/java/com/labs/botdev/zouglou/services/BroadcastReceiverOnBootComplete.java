package com.labs.botdev.zouglou.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class BroadcastReceiverOnBootComplete extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
            Toast.makeText(context,"Zouglou Event Services Started...",Toast.LENGTH_LONG).show();
            Intent serviceIntent = new Intent(context, PusherEventService.class);
            context.startService(serviceIntent);
        }
    }
}
