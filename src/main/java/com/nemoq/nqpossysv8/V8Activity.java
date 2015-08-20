package com.nemoq.nqpossysv8;

import android.app.Application;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.io.OutputStream;

import android_serialport_api.SerialPort;

public class V8Activity extends AppCompatActivity {


    protected Application mApplication;
    protected SerialPort mSerialPort;
    protected OutputStream mOutputStream;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onDestroy()
    {
        mApplication.closeSerialPort();
        mSerialPort = null;
        super.onDestroy();
    }

}
