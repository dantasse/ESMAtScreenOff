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
        Log.d("EsmAtScreenOff", "Re-setting ESM times");
        if (!intent.getAction().equals(Plugin.ACTION_RESET_ESM_TIMES)) {
            Log.d("EsmAtScreenOff",
                    "NewDateReceiver got an event that's not about resetting ESM times. Why?");
            return;
        }
        plugin.setEsmTimes();
    }
}
