package com.nemoq.nqentityv8;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.nemoq.nqentityv8.NetworkHandling.NetworkService;
import com.nemoq.nqentityv8.UI.V8MainActivity;

/**
 * Created by Martin Backudd on 2015-10-27.
 * Receives broadcast from the os that the device booted and launches the application
 */
public class BootBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {


        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean launchOnBoot = sharedPreferences.getBoolean(context.getResources().getString(R.string.pref_key_boot), false);


        if (launchOnBoot) {
            Intent activityIntent = new Intent(context, V8MainActivity.class);
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(activityIntent);

            Intent networkServiceIntent = new Intent(context, NetworkService.class);
            context.startService(networkServiceIntent);
        }

    }
}
