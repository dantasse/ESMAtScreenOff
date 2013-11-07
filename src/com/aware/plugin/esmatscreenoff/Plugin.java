package com.aware.plugin.esmatscreenoff;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.aware.utils.Aware_Sensor;

public class Plugin extends Aware_Sensor {
    
    private BroadcastReceiver mReceiver = new ScreenReceiver();

    
    @Override
    public void onCreate() {
        Log.d("EsmAtScreenOff", "starting up!");
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mReceiver, filter);

        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Log.d("EsmAtScreenOff", "destroyin'!");
        unregisterReceiver(mReceiver);
        super.onDestroy();
        // Code here when add-on is turned off.
    }
}