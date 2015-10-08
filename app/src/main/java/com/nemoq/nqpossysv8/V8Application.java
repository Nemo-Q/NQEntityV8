package com.nemoq.nqpossysv8;

import android.app.Application;
import android.content.Intent;

import com.nemoq.nqpossysv8.NetworkHandling.NetworkService;

/**
 * Created by Martin on 2015-09-10.
 */
public class V8Application extends Application {

    private Intent networkServiceIntent;

    @Override
    public void onCreate() {


        networkServiceIntent = new Intent(getApplicationContext(), NetworkService.class);
        startService(networkServiceIntent);

        super.onCreate();
    }



}


