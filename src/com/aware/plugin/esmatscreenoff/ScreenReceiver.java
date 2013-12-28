package com.aware.plugin.esmatscreenoff;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.Vibrator;
import android.text.format.Time;
import android.util.Log;

import com.aware.Aware;
import com.aware.ESM;

public class ScreenReceiver extends BroadcastReceiver {
    
    // see: http://thinkandroid.wordpress.com/2010/01/24/handling-screen-off-and-screen-on-intents/

    private Plugin plugin;
    public ScreenReceiver(Plugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            Log.d("EsmAtScreenOff", "Got an event that's not a screen off. Why?");
            return;
        }
        if (plugin.esmTimes.isEmpty()) {
            return; // no more ESMs left to do today
        }
        Time now = new Time();
        now.setToNow();
        if (now.before(plugin.esmTimes.get(0))) {
            return; // not time to do an ESM yet
        }
        while(!plugin.esmTimes.isEmpty() && now.after(plugin.esmTimes.get(0))) {
            plugin.esmTimes.remove(0); // if a few have queued, just do one
        }
        
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "EsmAtScreenOff");
        wl.acquire(1000);
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(500);

        Intent i = new Intent();
        i.setAction(ESM.ACTION_AWARE_QUEUE_ESM);
        String esmTitle = Aware.getSetting(plugin.getContentResolver(), Plugin.ESM_TITLE_KEY);
        String esmInstructions = Aware.getSetting(plugin.getContentResolver(), Plugin.ESM_INSTRUCTIONS_KEY);
        String esmStr = "[" +
        		"{'esm': {" +
        		"'esm_type': 1, " +
        		"'esm_title': '" + esmTitle + "', " +
        		"'esm_instructions': '" + esmInstructions + "', " +
        		"'esm_submit': 'Done', " +
        		"'esm_expiration_threashold': 60, " +
        		"'esm_trigger': 'EsmAtScreenOff_Main' }}," +
                "{'esm': {" +
                "'esm_type': 5, " +
                "'esm_title': 'How is your mood right now?', " +
                "'esm_instructions': ''," +
                "'esm_quick_answers': " +
                    "['3 (very good)','2','1','0 (neither good nor bad)', '-1', '-2','-3 (very bad)']," +
                "'esm_expiration_threashold': 60, " +
                "'esm_trigger': 'EsmAtScreenOff_Mood' }}," +
                "{'esm': { " +
                "'esm_type': 5, " +
                "'esm_title': 'How is your energy right now?', " +
                "'esm_instructions': ''," +
                "'esm_quick_answers': ['3 (very active)','2','1','0 (neither active nor passive)', '-1', '-2','-3 (very passive)']," +
                "'esm_expiration_threashold': 60, " +
                "'esm_trigger': 'EsmAtScreenOff_Energy' }}" +
                "]";
        i.putExtra(ESM.EXTRA_ESM, esmStr);
        context.sendBroadcast(i);
    }
}
