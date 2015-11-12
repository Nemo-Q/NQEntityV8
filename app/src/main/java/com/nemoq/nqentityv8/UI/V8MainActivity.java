
package com.nemoq.nqentityv8.UI;

import android.animation.Animator;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.nemoq.nqentityv8.NetworkHandling.NetworkService;
import com.nemoq.nqentityv8.PinCodeManager;
import com.nemoq.nqentityv8.R;
import com.nemoq.nqentityv8.print.PrintApplication;
import com.nemoq.nqentityv8.print.PrintInterface;
import com.nemoq.nqentityv8.print.ReceiptParser;

import java.io.IOException;
import java.io.OutputStream;

import android_serialport_api.SerialPort;


/**
 * Created by Martin Backudd on 2015-08-14. Main Activity for the app.
 */


public class V8MainActivity extends Activity implements View.OnClickListener{

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


        final ImageView leftArrow = (ImageView)findViewById(R.id.leftArrow);
        leftArrow.setAlpha(1.0f);
        Animation fadeInArrow = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.abc_fade_in);
        fadeInArrow.setStartOffset(250);
        leftArrow.setAnimation(fadeInArrow);
        leftArrow.startAnimation(fadeInArrow);

        final Animation arrowCycle = AnimationUtils.loadAnimation(this,R.anim.arrow_cycle);
        arrowCycle.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                    leftArrow.startAnimation(arrowCycle);


            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        leftArrow.setAnimation(arrowCycle);
        leftArrow.startAnimation(arrowCycle);





        final ImageButton settingsButton = (ImageButton) findViewById(R.id.settingsButton);

            settingsButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View settingsButtonView) {

                    showSettings();

                }


            });




    }

    @Override
    protected void onResume() {
        super.onResume();
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

        Intent intent = new Intent(getBaseContext().getApplicationContext(), NetworkService.class);
        intent.setAction(getResources().getString(R.string.broadcast_preferences_changed));
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(getBaseContext());
        broadcastManager.sendBroadcast(intent);

    }


    float lastY;
    float firstY;

    boolean slideView = false;
    boolean animating = false;
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        final View targetView = (View)findViewById(R.id.draggableLayout);
        final ImageView dragTarget = (ImageView)findViewById(R.id.dragTarget);
        float deltaY = event.getRawY() - lastY;

        Rect targetBounds = new Rect();
        targetView.getHitRect(targetBounds);

        if (event.getAction() == MotionEvent.ACTION_DOWN && targetBounds.contains((int)event.getRawX(),(int)event.getRawY())) {
            if ((event.getRawY() < targetView.getBottom()) || event.getRawY() > targetView.getTop()) {
                dragTarget.setImageResource(R.drawable.white1);
                firstY = event.getRawY();
                slideView = true;

            }
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            View mainView = (View) targetView.getParent();
            if (slideView && !animating) {

                targetView.setY(targetView.getY() + deltaY);
                float targetBottomPos = targetView.getY() + targetView.getHeight();
                ImageView leftArrow = (ImageView) findViewById(R.id.leftArrow);
                float distanceToArrow = leftArrow.getY() - targetBottomPos;
                if (distanceToArrow < 100) {
                    float alphaPercentage = distanceToArrow * 0.01f;
                    leftArrow.setAlpha(alphaPercentage);

                } else {
                    leftArrow.setAlpha(1f);
                }

                if (targetBottomPos > mainView.getMeasuredHeight()) {
                    testPrint();
                    slideOutTargetView();
                }
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP && slideView) {

            dragTarget.setImageResource(R.drawable.white0);
            if ((event.getRawY() - firstY) > 100 && !animating) {


                testPrint();


                targetView.animate()
                        .translationY(targetView.getHeight())
                        .setInterpolator(new DecelerateInterpolator())
                        .setListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {

                                slideOutTargetView();
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {
                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        });

            } else {


                targetView.animate()
                        .translationY(0.0f)
                        .setInterpolator(new DecelerateInterpolator())
                        .setListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {


                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        });
            }
            firstY = 0;
            slideView = false;


        }


        lastY = event.getRawY();

        return super.onTouchEvent(event);
    }



    private void slideOutTargetView(){
        animating = true;
        final View targetView = findViewById(R.id.draggableLayout);
        Animation slideOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.linear_slide_out);
        slideOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {


                targetView.setVisibility(View.INVISIBLE);
                targetView.setTranslationY(0);
                Animation fadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.abc_fade_in);
                fadeIn.setStartOffset(500);
                fadeIn.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        targetView.setVisibility(View.VISIBLE);

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        animating = false;
                        ImageView leftArrow = (ImageView)findViewById(R.id.leftArrow);
                        leftArrow.setAlpha(1.0f);
                        Animation arrowCycle = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.arrow_cycle);
                        leftArrow.setAnimation(arrowCycle);
                        leftArrow.startAnimation(arrowCycle);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                targetView.setAnimation(fadeIn);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        targetView.startAnimation(slideOut);

    }


    private void testPrint(){

        AsyncTask asyncTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {


                byte[] bytes = ReceiptParser.getInstance(getApplicationContext()).testJson(getResources().openRawResource(R.raw.ticketjson));

                if (bytes.length != 0){

                    PrintInterface printInterface = new PrintInterface();

                    try {
                        printInterface.writeData(bytes);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                return null;
            }
        };
        asyncTask.execute(new Object[] {0x0});



    }


    private void showPinDialog(){

        FrameLayout frameLayout = (FrameLayout)findViewById(android.R.id.content);
        View.inflate(this, R.layout.pin_dialog, frameLayout);


        EditText pinField = (EditText) findViewById(R.id.pinTextField);
        ViewGroup pinFieldParentView = (ViewGroup)pinField.getParent();
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.abc_fade_in);
        pinFieldParentView.startAnimation(fadeIn);

        for (int i = 0;i<10;i++){

            int buttonId = getResources().getIdentifier("button" + String.valueOf(i), "id", "com.nemoq.nqentityv8");

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

  /*  private void hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        View dView = (View)findViewById(R.id.fullscreen_content_controls);
        dView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    // This snippet shows the system bars. It does this by removing all the flags
// except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        View dView = (View)findViewById(R.id.fullscreen_content_controls);
        dView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }
*/


}
