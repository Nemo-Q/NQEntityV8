package com.nemoq.nqpossysv8;

import android.app.Application;
import android.content.Intent;

import com.nemoq.nqpossysv8.NetworkHandling.NetworkService;

/**
 * Created by Martin Backudd on 2015-09-10. App start, runs service for listening and printing.
 */
public class V8Application extends Application {


    @Override
    public void onCreate() {


        Intent networkServiceIntent = new Intent(getApplicationContext(), NetworkService.class);
        startService(networkServiceIntent);

        super.onCreate();
    }



}


