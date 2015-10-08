package com.nemoq.nqpossysv8;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.nemoq.nqpossysv8.NetworkHandling.NetworkService;
import com.nemoq.nqpossysv8.print.BitmapTranslator;
import com.nemoq.nqpossysv8.print.PrintApplication;
import com.nemoq.nqpossysv8.print.PrintInterface;
import com.nemoq.nqpossysv8.print.ReceiptParser;


import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.SortedMap;

import android_serialport_api.SerialPort;


public class V8Activity extends Activity {



    protected PrintApplication mApplication;
    protected SerialPort mSerialPort;
    protected OutputStream mOutputStream;
    public PrintInterface printInterface;
    public ReceiptParser receiptParser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_ui);


        printInterface = new PrintInterface();
        receiptParser = new ReceiptParser(this);

        hideSystemUI();
        try {



        } catch (Exception e) {
            e.printStackTrace();
        }



        final Button xmlPrint = (Button) findViewById(R.id.button);
        xmlPrint.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                InputStream fs = getResources().openRawResource(R.raw.abd);



                try {



                    byte[] bytes = receiptParser.testXml(fs);

                    printInterface.writeData(bytes);

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                }

            }
            });



    }


    private void hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.


        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    // This snippet shows the system bars. It does this by removing all the flags
// except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        hideSystemUI();
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    protected void onDestroy()
    {
        mApplication.closeSerialPort();
        mSerialPort = null;

        super.onDestroy();
    }

}
