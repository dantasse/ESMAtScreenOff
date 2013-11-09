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
    
    private BroadcastReceiver mReceiver = new ScreenReceiver(this);

    List<Time> esmTimes = new ArrayList<Time>();

    // TODO somehow run a cron job to reset the times every day!
    @Override
    public void onCreate() {
        super.onCreate();
        setEsmTimes();
        
        // listen to screen off events
        registerReceiver(mReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));

        //Activate sensors, and apply
        Aware.setSetting(getContentResolver(), Aware_Preferences.STATUS_ESM, true);
        Intent applySettings = new Intent(Aware.ACTION_AWARE_REFRESH);
        sendBroadcast(applySettings);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mReceiver);
        
        //Deactivate sensors, and apply
        Aware.setSetting(getContentResolver(), Aware_Preferences.STATUS_ESM, false);
        Intent applySettings = new Intent(Aware.ACTION_AWARE_REFRESH);
        sendBroadcast(applySettings);
    }
    
    // return true if all the ESMs lined up to go are today; false if they're old or
    // there aren't any ESM times set.
//    boolean areEsmDateTimesOkay() {
//        Time now = new Time();
//        now.setToNow();
//        for (Time time : esmTimes) {
//            if (time.monthDay == now.monthDay) {
//                return true;
//            }
//        }
//        return false;
//    }
    
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
        
        String timesString = "";
        Log.d("EsmAtScreenOff", "set ESM times to:");
        for (Time time : esmTimes) {
            Log.d("EsmAtScreenOff", time.format3339(false));
        }
    }
}