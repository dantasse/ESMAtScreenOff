package com.aware.plugin.esmatscreenoff;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;

import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.utils.Aware_Sensor;

public class Plugin extends Aware_Sensor {
    
    private BroadcastReceiver mReceiver = new ScreenReceiver();

    private List<Date> esmDateTimes = new ArrayList<Date>();
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // listen to screen off events
        registerReceiver(mReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));

        //Activate sensors, and apply
        Aware.setSetting(getContentResolver(), Aware_Preferences.STATUS_ESM, true);
        Intent applySettings = new Intent(Aware.ACTION_AWARE_REFRESH);
        sendBroadcast(applySettings);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
        
        //Deactivate sensors, and apply
        Aware.setSetting(getContentResolver(), Aware_Preferences.STATUS_ESM, false);
        Intent applySettings = new Intent(Aware.ACTION_AWARE_REFRESH);
        sendBroadcast(applySettings);
    }
    
//    boolean areEsmDateTimesOkay() {
//        
//    }
}