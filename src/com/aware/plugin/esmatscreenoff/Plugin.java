package com.aware.plugin.esmatscreenoff;

import com.aware.utils.Aware_Sensor;

public class Plugin extends Aware_Sensor {
    @Override
    public void onCreate() {
        super.onCreate();
        // Code here when add-on is turned on.
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Code here when add-on is turned off.
    }
}