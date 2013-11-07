package com.aware.plugin.esmatscreenoff;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.utils.Aware_Sensor;

public class Plugin extends Aware_Sensor {
    
    private BroadcastReceiver mReceiver = new ScreenReceiver();

    
    @Override
    public void onCreate() {
        super.onCreate();

        Log.d("EsmAtScreenOff", "starting up!");
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mReceiver, filter);

        //Activate sensors
        Aware.setSetting(getContentResolver(), Aware_Preferences.STATUS_ESM, true);
        //Apply settings
        Intent applySettings = new Intent(Aware.ACTION_AWARE_REFRESH);
        sendBroadcast(applySettings);
    }

    @Override
    public void onDestroy() {
        Log.d("EsmAtScreenOff", "destroyin'!");
        unregisterReceiver(mReceiver);
        super.onDestroy();
        
        //Deactivate sensors
        Aware.setSetting(getContentResolver(), Aware_Preferences.STATUS_ESM, false);
        //Apply settings
        Intent applySettings = new Intent(Aware.ACTION_AWARE_REFRESH);
        sendBroadcast(applySettings);
    }
}