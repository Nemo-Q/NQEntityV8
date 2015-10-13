
package com.nemoq.nqpossysv8.UI;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;

import com.android.volley.AuthFailureError;
import com.nemoq.nqpossysv8.NetworkHandling.RequestAdapter;
import com.nemoq.nqpossysv8.NetworkHandling.SOAPWriter;
import com.nemoq.nqpossysv8.R;
import com.nemoq.nqpossysv8.print.PrintApplication;
import com.nemoq.nqpossysv8.print.PrintInterface;
import com.nemoq.nqpossysv8.print.ReceiptParser;


import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android_serialport_api.SerialPort;


/**
 * Created by Martin Backudd on 2015-08-14. Main Activity for the app.
 */

public class V8MainActivity extends Activity {



    protected PrintApplication mApplication;
    protected SerialPort mSerialPort;
    protected OutputStream mOutputStream;
    public PrintInterface printInterface;
    public ReceiptParser receiptParser;
    private RequestAdapter requestAdapter;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_ui);


        printInterface = new PrintInterface();
        receiptParser = new ReceiptParser(this);

        requestAdapter = RequestAdapter.getInstance(this);


        hideSystemUI();
        try {



        } catch (Exception e) {
            e.printStackTrace();
        }



        final Button xmlPrint = (Button) findViewById(R.id.button);
        xmlPrint.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                InputStream fs = getResources().openRawResource(R.raw.test);


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


    float historicalX;
    @Override
    public boolean onTouchEvent(MotionEvent event) {


        if(event.getAction() == MotionEvent.ACTION_DOWN){
            historicalX = event.getX();

        }
        else if(event.getAction() == MotionEvent.ACTION_UP){
            historicalX = 0;


        }

        else {
            ImageButton settingsButton = (ImageButton) findViewById(R.id.settingsButton);

            if (historicalX >= settingsButton.getX()) {

                settingsButton.setOnClickListener(new View.OnClickListener() {
                                                      public void onClick(View v) {
                                                          showSettings();


                                                      }
                                                  });


                Animation slideIn = AnimationUtils.loadAnimation(this, R.anim.slidein);

                settingsButton.startAnimation(slideIn);
                settingsButton.setVisibility(View.VISIBLE);
            }
        }

        return super.onTouchEvent(event);
    }


    private void showSettings(){


        Intent intent = new Intent(this, SettingsActivity.class);

        startActivityForResult(intent, 0);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.sendBroadcast(new Intent("prefs_updated"));


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
