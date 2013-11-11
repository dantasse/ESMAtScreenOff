package com.aware.plugin.esmatscreenoff;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.format.Time;
import android.util.Log;

import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.utils.Aware_Sensor;

public class Plugin extends Aware_Sensor {
    
    private static final int START_HOUR = 10;
    private static final int END_HOUR = 21; // 9pm
    private static final int NUM_ESMS = 8;
    private Random random = new Random();
    
    private BroadcastReceiver screenReceiver = new ScreenReceiver(this);
    private BroadcastReceiver newDateReceiver = new NewDateReceiver(this);

    List<Time> esmTimes = new ArrayList<Time>();

    // TODO somehow run a cron job to reset the times every day!
    @Override
    public void onCreate() {
        super.onCreate();
        setEsmTimes();
        
        // listen to screen off events
        registerReceiver(screenReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
        registerReceiver(newDateReceiver, new IntentFilter(Intent.ACTION_DATE_CHANGED));

        //Activate sensors, and apply
        Aware.setSetting(getContentResolver(), Aware_Preferences.STATUS_ESM, true);
        Intent applySettings = new Intent(Aware.ACTION_AWARE_REFRESH);
        sendBroadcast(applySettings);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(screenReceiver);
        unregisterReceiver(newDateReceiver);
        
        //Deactivate sensors, and apply
        Aware.setSetting(getContentResolver(), Aware_Preferences.STATUS_ESM, false);
        Intent applySettings = new Intent(Aware.ACTION_AWARE_REFRESH);
        sendBroadcast(applySettings);
    }
    
    void setEsmTimes() {
        Time startTime = new Time();
        startTime.hour = START_HOUR;        
        Time endTime = new Time();
        endTime.hour = END_HOUR;
        
        // divide into N intervals, pick a random time in each
        int timeBetween = (int) (endTime.toMillis(true) - startTime.toMillis(true));
        int intervalLength = timeBetween / NUM_ESMS;
        
        esmTimes.clear();
        Time todayStart = new Time();
        todayStart.setToNow();
        todayStart.hour = START_HOUR;
        todayStart.minute = todayStart.second = 0;
        for (int i = 0; i < NUM_ESMS; i++) {
            long nextTimeMillis = todayStart.toMillis(true);
            nextTimeMillis += i * intervalLength + random.nextInt(intervalLength);
            Time nextTime = new Time();
            nextTime.set(nextTimeMillis);
            esmTimes.add(nextTime);
        }
        
        Log.d("EsmAtScreenOff", "set ESM times to:");
        for (Time time : esmTimes) {
            Log.d("EsmAtScreenOff", time.format3339(false));
        }
    }
}