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
    
    private static final int FALLBACK_START_HOUR = 9; // nothing earlier than 9am
    private static final int FALLBACK_END_HOUR = 21; // nothing later than 9pm
    private static final int FALLBACK_NUM_ESMS = 8; // this many ESMs per day (if not set otherwise)
    private static final String FALLBACK_ESM_TITLE = "What were you doing just now?";
    private static final String FALLBACK_ESM_INSTRUCTIONS = "What did you mean to accomplish when you took out your phone just now?";
    private Random random = new Random();
    
    private BroadcastReceiver screenReceiver = new ScreenReceiver(this);
    private BroadcastReceiver newDateReceiver = new NewDateReceiver(this);
    private AlarmManager alarmManager;
    private PendingIntent setTimesPendingIntent;
    
    List<Time> esmTimes = new ArrayList<Time>();
    
    static final String ACTION_RESET_ESM_TIMES = "com.aware.plugin.esmatscreenoff.NewDateReceiver";

    // these are the keys you can use to set preferences via the Aware dashboard. Go to
    // http://(servername)/aware/index.php/aware_ws/dashboard (as of 12/2013; will probably change)
    // and select the right client, enter the Topic="configuration", and Message=
    // "com.aware.plugin.esmatscreenoff.NUM_ESMS=8" (or whatever. omit quotes.)
    static final String NUM_ESMS_KEY = "com.aware.plugin.esmatscreenoff.NUM_ESMS";
    static final String START_HOUR_KEY = "com.aware.plugin.esmatscreenoff.START_HOUR";
    static final String END_HOUR_KEY = "com.aware.plugin.esmatscreenoff.END_HOUR";
    static final String ESM_TITLE_KEY = "com.aware.plugin.esmatscreenoff.ESM_TITLE";
    static final String ESM_INSTRUCTIONS_KEY = "com.aware.plugin.esmatscreenoff.ESM_INSTRUCTIONS";
    
    static final String LOG_TAG = "EsmAtScreenOff";
    
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

        // Set all the EsmAtScreenOff-specific settings
        Aware.setSetting(getContentResolver(), NUM_ESMS_KEY, FALLBACK_NUM_ESMS);
        Aware.setSetting(getContentResolver(), START_HOUR_KEY, FALLBACK_START_HOUR);
        Aware.setSetting(getContentResolver(), END_HOUR_KEY, FALLBACK_END_HOUR);
        Aware.setSetting(getContentResolver(), ESM_TITLE_KEY, FALLBACK_ESM_TITLE);
        Aware.setSetting(getContentResolver(), ESM_INSTRUCTIONS_KEY, FALLBACK_ESM_INSTRUCTIONS);
        
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
        try {
            startTime.hour = Integer.parseInt(Aware.getSetting(getContentResolver(), START_HOUR_KEY));
        } catch (NumberFormatException nfe) {
            Log.d(LOG_TAG, "Illegal setting for start hour: " +
                    Aware.getSetting(getContentResolver(), START_HOUR_KEY) +
                    ", using fallback: " + FALLBACK_START_HOUR);
            startTime.hour = FALLBACK_START_HOUR;
        }
        Time endTime = new Time();
        try {
            endTime.hour = Integer.parseInt(Aware.getSetting(getContentResolver(), END_HOUR_KEY));
        } catch (NumberFormatException nfe) {
            Log.d(LOG_TAG, "Illegal setting for end hour: " +
                    Aware.getSetting(getContentResolver(), END_HOUR_KEY) +
                    ", using fallback: " + FALLBACK_END_HOUR);
            endTime.hour = FALLBACK_END_HOUR;
        }
        int numEsms;
        try {
            numEsms = Integer.parseInt(Aware.getSetting(getContentResolver(), NUM_ESMS_KEY));
        } catch (NumberFormatException nfe) {
            Log.d(LOG_TAG, "Illegal setting for number of ESMs: " +
                    Aware.getSetting(getContentResolver(), NUM_ESMS_KEY) +
                    ", using fallback: " + FALLBACK_NUM_ESMS);
            numEsms = FALLBACK_NUM_ESMS;
        }
        
        // divide into N intervals, pick a random time in each
        int timeBetween = (int) (endTime.toMillis(true) - startTime.toMillis(true));
        int intervalLength = timeBetween / numEsms;
        
        esmTimes.clear();

        Time todayStart = new Time();
        todayStart.setToNow();
        todayStart.hour = startTime.hour;
        todayStart.minute = todayStart.second = 0;
        for (int i = 0; i < numEsms; i++) {
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
        tomorrowTime.set(0 /*sec*/, 0 /*min*/, startTime.hour,
                tomorrowCal.get(Calendar.DAY_OF_MONTH), tomorrowCal.get(Calendar.MONTH),
                tomorrowCal.get(Calendar.YEAR));
        for (int i = 0; i < numEsms; i++) {
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