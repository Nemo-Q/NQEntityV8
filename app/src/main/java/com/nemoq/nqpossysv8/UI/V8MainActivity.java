
package com.nemoq.nqpossysv8.UI;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.nemoq.nqpossysv8.PinCodeManager;
import com.nemoq.nqpossysv8.R;
import com.nemoq.nqpossysv8.print.PrintApplication;
import com.nemoq.nqpossysv8.print.PrintInterface;
import com.nemoq.nqpossysv8.print.ReceiptParser;


import java.io.IOException;
import java.io.OutputStream;

import android_serialport_api.SerialPort;


/**
 * Created by Martin Backudd on 2015-08-14. Main Activity for the app.
 */


public class V8MainActivity extends Activity{

    private static V8MainActivity v8MainActivity;
    protected PrintApplication mApplication;
    protected SerialPort mSerialPort;
    protected OutputStream mOutputStream;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_ui);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(getString(R.string.broadcast_close_activity));
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(getResources().getString(R.string.broadcast_close_activity))) {

                    finish();
                }

            }
        }, intentFilter);

        final Button xmlPrint = (Button) findViewById(R.id.button);
        xmlPrint.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                /*ReceiptParser rp = new ReceiptParser(getApplicationContext());
                BitmapTranslator bt = new BitmapTranslator();
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.c);*/

                    AsyncTask asyncTask = new AsyncTask() {
                        @Override
                        protected Object doInBackground(Object[] params) {
                            byte[] bytes = ReceiptParser.getInstance(getApplicationContext()).testJson(getResources().openRawResource(R.raw.ticketjson));
                            PrintInterface printInterface = new PrintInterface();
                            try {
                                printInterface.writeData(bytes);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }
                    };
                    asyncTask.execute(new Object[]{0x0});


                    /*
                    printInterface.writeData(rp.setPrintWidth(580));
                    byte[] bit = bt.getPortableBitmap(bitmap, false,false);
                    printInterface.writeData(bit);*/



            }
            });



    }


    public void showSettings(){


        Intent intent = new Intent(this, SettingsActivity.class);

        startActivityForResult(intent, 0);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.sendBroadcast(new Intent("prefs_updated"));

    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    protected void onDestroy()
    {
        //mApplication.closeSerialPort();
       // mSerialPort = null;

        super.onDestroy();
    }


}
