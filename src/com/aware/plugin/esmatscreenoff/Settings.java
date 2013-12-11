package com.aware.plugin.esmatscreenoff;

import android.app.Activity;
import android.os.Bundle;

/** This would be the Settings page for this plugin. Currently disabled because there's nothing
 * for the user to set. */
public class Settings extends Activity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }
}