
package com.nemoq.nqpossysv8.UI;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
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

import com.nemoq.nqpossysv8.NetworkHandling.NetworkService;
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


public class V8MainActivity extends Activity implements View.OnClickListener {

    private static V8MainActivity v8MainActivity;
    protected PrintApplication mApplication;
    protected SerialPort mSerialPort;
    protected OutputStream mOutputStream;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setDefaultPreferences();
        setContentView(R.layout.activity_main_ui);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(getString(R.string.broadcast_close_activity));
        intentFilter.addAction(getString(R.string.broadcast_address_changed));
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


            }
        });



    }

    private void setDefaultPreferences(){
        PreferenceManager.setDefaultValues(this, R.xml.pref_connection, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
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


    float historicalX;
    boolean slidingButton = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        //Intercept touch events
        if (event.getY() < this.getWindow().getDecorView().getMeasuredHeight()/8) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                historicalX = event.getX();

            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                historicalX = 0;


            } else {
                //Settings button logic
                if (!slidingButton) {
                    final ImageButton settingsButton = (ImageButton) findViewById(R.id.settingsButton);

                    if (historicalX >= settingsButton.getX()) {

                        settingsButton.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View settingsButtonView) {

                                showPinDialog();
                                settingsButtonView.setEnabled(false);


                            }


                        });


                        Animation slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in);

                        slideIn.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                                slidingButton = true;
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                settingsButton.setVisibility(View.INVISIBLE);
                                slidingButton = false;
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });

                        settingsButton.startAnimation(slideIn);
                        settingsButton.setVisibility(View.VISIBLE);

                    }
                }
            }
        }
        return super.onTouchEvent(event);
    }


    private void showPinDialog(){

        FrameLayout frameLayout = (FrameLayout)findViewById(android.R.id.content);
        View.inflate(this, R.layout.pin_dialog, frameLayout);


        EditText pinField = (EditText) findViewById(R.id.pinTextField);
        ViewGroup pinFieldParentView = (ViewGroup)pinField.getParent();
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.abc_fade_in);
        pinFieldParentView.startAnimation(fadeIn);

        for (int i = 0;i<10;i++){

            int buttonId = getResources().getIdentifier("button" + String.valueOf(i), "id", "com.nemoq.nqpossysv8");

            final Button button = (Button) findViewById(buttonId);
            button.setOnClickListener(this);

        }


        final ImageButton dismissButton = (ImageButton) findViewById(R.id.buttonDismissPin);
        dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogFinish(false);
            }
        });

        final ImageButton eraseButton = (ImageButton) findViewById(R.id.eraseButton);
        eraseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText pinField = (EditText) findViewById(R.id.pinTextField);
                pinField.getText().delete(pinField.length() - 1, pinField.length());
            }
        });





    }
    private void dialogFinish(final boolean pinOk){

        final EditText pinField = (EditText) findViewById(R.id.pinTextField);

        Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.abc_fade_out);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                ViewGroup viewGroupDialog = (ViewGroup) pinField.getParent();
                viewGroupDialog.removeAllViews();

                if (pinOk)
                    showSettings();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        View parent = (ViewGroup)pinField.getParent();

        parent.startAnimation(fadeOut);
        ImageButton settingsButton = (ImageButton)findViewById(R.id.settingsButton);
        settingsButton.setEnabled(true);



    }

    @Override
    public void onClick(View v) {


        EditText pinField = (EditText) findViewById(R.id.pinTextField);
        pinField.append(v.getTag().toString());

        if (pinField.getText().toString().length() > 4){
            pinField.setText(v.getTag().toString());



        }
        else if(pinField.getText().toString().length() == 4) {

            if (PinCodeManager.getInstance(this).checkPin(pinField.getText().toString())) {

                dialogFinish(true);

            }
            else {

                Animation pinShake = AnimationUtils.loadAnimation(this, R.anim.pin_shake);
                pinField.startAnimation(pinShake);

            }

        }


    }




    @Override
    protected void onDestroy()
    {
        //mApplication.closeSerialPort();
       // mSerialPort = null;

        super.onDestroy();
    }



}
