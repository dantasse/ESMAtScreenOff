package com.aware.plugin.esmatscreenoff;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;

import com.aware.ESM;

public class ScreenReceiver extends BroadcastReceiver {
    
    // see: http://thinkandroid.wordpress.com/2010/01/24/handling-screen-off-and-screen-on-intents/
    public static boolean wasScreenOn = true;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            Log.d("EsmAtScreenOff", "turning screen off");

            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "EsmAtScreenOff");
            wl.acquire(1000);
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(500);
            
            Intent i = new Intent();
            i.setAction(ESM.ACTION_AWARE_QUEUE_ESM);
            String esmStr = "[{'esm': { 'esm_type': 1, 'esm_title': 'ESM Freetext', 'esm_instructions': 'The user can answer an open ended question.', 'esm_submit': 'Next', 'esm_expiration_threashold': 60, 'esm_trigger': 'AWARE Tester' }}]";
            i.putExtra(ESM.EXTRA_ESM, esmStr);
            context.sendBroadcast(i);
            
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            Log.d("EsmAtScreenOff", "turning screen on");
        }
    }
}