package com.pg.lockapp.presentation.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.pg.lockapp.presentation.services.AutoStartService;

public class RestartServiceReceiver extends BroadcastReceiver {

    private static final String TAG = "RestartServiceReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "RestartServiceReceiver triggered");
        System.out.println("RestartServiceReceiver triggered");
        Intent serviceIntent = new Intent(context, AutoStartService.class);
        context.startService(serviceIntent);
    }
}

