package com.nemoq.nqpossysv8;

import android.app.Activity;
import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.nemoq.nqpossysv8.print.BitmapTranslator;
import com.nemoq.nqpossysv8.print.PrintApplication;
import com.nemoq.nqpossysv8.print.PrintInterface;

import java.io.OutputStream;

import android_serialport_api.SerialPort;


public class V8Activity extends Activity {


    protected PrintApplication mApplication;
    protected SerialPort mSerialPort;
    protected OutputStream mOutputStream;
    public PrintInterface printInterface;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_ui);
/*
        printInterface = new PrintInterface();
        byte[] downloadbytes = {0x1D,0x2A,0x1,0x1,0x44};

        byte[] scanmode = {0x12,0x49,0x1};
        printInterface.writeData(scanmode);

        printInterface.writeData(downloadbytes);

        */
        byte[] send = { 0x12, 0x2A, 0x35};



        printInterface.writeData(send);


        final Button dummy = (Button) findViewById(R.id.dummy_button);
        dummy.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {

                byte[] lineFeedcut = {0x1B,0x4A,0x2,0x1D,0x56,0x0};



                printInterface.writeData(lineFeedcut);



            }
        });




    }


    @Override
    protected void onDestroy()
    {
        //mApplication.closeSerialPort();
        //mSerialPort = null;
        super.onDestroy();
    }

}
