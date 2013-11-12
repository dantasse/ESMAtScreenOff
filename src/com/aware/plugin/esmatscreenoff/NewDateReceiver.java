package com.aware.plugin.esmatscreenoff;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NewDateReceiver extends BroadcastReceiver {

    private Plugin plugin;
    public NewDateReceiver(Plugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("EsmAtScreenOff", "got a new date event");
        if (!intent.getAction().equals(Intent.ACTION_DATE_CHANGED)) {
            Log.d("EsmAtScreenOff", "NewDateReceiver got an event that's not a new date. Why?");
            return;
        }
        plugin.setEsmTimes();
    }
}
