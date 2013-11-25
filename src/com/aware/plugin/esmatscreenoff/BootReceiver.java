package com.aware.plugin.esmatscreenoff;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

    // this is configured to receive boot broadcasts in the AndroidManifest.
    // the others are configured to receive them in Plugin.java. I'm not sure if one way or the
    // other is better.
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            context.startService(new Intent(context, Plugin.class));
        }
    }

}
