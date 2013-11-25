package com.aware.plugin.esmatscreenoff;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.format.Time;
import android.util.Log;

import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.utils.Aware_Sensor;

public class Plugin extends Aware_Sensor {
    
    private static final int START_HOUR = 10; // nothing earlier than 10am
    private static final int END_HOUR = 21; // nothing later than 9pm
    private static final int NUM_ESMS = 8; // this many ESMs per day
    private Random random = new Random();
    
    private BroadcastReceiver screenReceiver = new ScreenReceiver(this);
    private BroadcastReceiver newDateReceiver = new NewDateReceiver(this);
    private AlarmManager alarmManager;
    private PendingIntent setTimesPendingIntent;
    
    List<Time> esmTimes = new ArrayList<Time>();
    
    static final String ACTION_RESET_ESM_TIMES = "com.aware.plugin.esmatscreenoff.NewDateReceiver";

    @Override
    public void onCreate() {
        super.onCreate();
        
        // listen to screen off events
        registerReceiver(screenReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
        registerReceiver(newDateReceiver, new IntentFilter(ACTION_RESET_ESM_TIMES));

        // set the alarm to schedule future ESM date resets
        // (could I have just used an alarm manager to schedule everything? hmm.)
        Calendar tomorrowMidnightish = new GregorianCalendar();
        tomorrowMidnightish.add(Calendar.DATE, 1); // will roll over at the end of month, I think
        tomorrowMidnightish.set(Calendar.HOUR, 0); // so it's between 12-1AM
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        
        setTimesPendingIntent = PendingIntent.getBroadcast(getBaseContext(),
                0, /* ??? */
                new Intent(ACTION_RESET_ESM_TIMES),
                PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setInexactRepeating(AlarmManager.RTC,
                tomorrowMidnightish.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY /* 1 day, ignore DST, being 1hr off is fine */,
                setTimesPendingIntent);

        setEsmTimes(); // do it once now to set the times for the rest of the day
        
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
        alarmManager.cancel(setTimesPendingIntent);
        
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
        
        // also set times for tomorrow. this is for a few reasons:
        // 1. the setInexactRepeating might not occur until a full interval after the first time.
        // so if you set it for 12:01am tomorrow morning, it might not happen until 11:59pm
        // tomorrow night, missing all of study day 2.
        // 2. setInexactRepeating is, well, inexact. So if it's supposed to go off on 12:01am
        // on Tuesday, it might instead go off on 11:59pm Monday instead, so it'll set a bunch of
        // dates on Monday, which is not correct.
        // 3. it doesn't hurt to have some times set for tomorrow. if we get to tomorrow morning
        // and we already have some dates set, they'll be cleared and we'll reset a new bunch.

        Calendar tomorrowCal = new GregorianCalendar(); // java dates are terrible.
        tomorrowCal.add(Calendar.DATE, 1); // doing this b/c Calendar.add handles end of month well.
        Time tomorrowTime = new Time();
        tomorrowTime.set(0 /*sec*/, 0 /*min*/, START_HOUR,
                tomorrowCal.get(Calendar.DAY_OF_MONTH), tomorrowCal.get(Calendar.MONTH),
                tomorrowCal.get(Calendar.YEAR));
        for (int i = 0; i < NUM_ESMS; i++) {
            long nextTimeMillis = tomorrowTime.toMillis(true);
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