package com.nemoq.nqentityv8.NetworkHandling;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.*;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.nemoq.nqentityv8.R;
import com.nemoq.nqentityv8.UI.V8MainActivity;
import com.nemoq.nqentityv8.print.PrintInterface;

import java.io.IOException;

import java.net.SocketException;
import java.net.UnknownHostException;
public class NetworkService extends Service {

    private NotificationManager notificationManager;

    private UDPBroadcastAdapter udpBroadcastAdapter;
    private SocketThreadClass socketThreadClass;
    private Thread socketThreadInstance;
    //Starts service to listen for incoming http posts.

    public boolean serviceRunning;


    static final int  LISTEN_STARTED = 0;
    static final int  RECEIVED_DATA = 1;
    static final int  LISTEN_STOPPED = 2;
    static final int  WRONG_FORMAT = 3;

    private Handler messageHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what){
                case LISTEN_STARTED:
                    //Started listening
                    Log.i("NetworkService:",(String)msg.obj);
                    break;
                case RECEIVED_DATA:
                    //Received data from HTTP post
                    byte[] printBytes = (byte[])msg.obj;
                    Log.i("NetWorkService:", "Printing");


                    try {
                        print(printBytes);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case LISTEN_STOPPED:
                    //Stopped listening
                    Log.i("NetworkService:",(String)msg.obj);
                    break;
                case WRONG_FORMAT:
                    //Weird http body
                    Log.i(" NetworkService: ",(String)msg.obj);
                    break;

            }

            super.handleMessage(msg);
        }
    };



    public NetworkService() {


    }


    public class LocalBinder extends Binder {
        NetworkService getService() {
            return NetworkService.this;
        }
    }


    @Override
    public void onCreate() {
        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);


        // Display a notification about us starting.  We put an icon in the status bar.
        showNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);
        Toast.makeText(this, "Nemo-Q service started", Toast.LENGTH_SHORT).show();

        serviceRunning = true;

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(getApplicationContext().getString(R.string.broadcast_app_shutdown));
        intentFilter.addAction(getApplicationContext().getString(R.string.broadcast_restart_listen));
        intentFilter.addAction(getApplicationContext().getString(R.string.broadcast_service_toggle));
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("ACTION_SHUTDOWN"))
                    stopSelf();
                else if (intent.getAction().equals(getApplicationContext().getString(R.string.broadcast_restart_listen)))
                    restartListen();



            }
        }, intentFilter);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean receiveLan = sharedPreferences.getBoolean(getResources().getString(R.string.pref_key_receive), true);


        //starts a thread to listen for  http.
        if (checkConnectivity() && receiveLan){
            startListen();
        }
        else {
            Toast.makeText(this,"No Connectivity",Toast.LENGTH_SHORT).show();
        }



        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        notificationManager.cancel(R.integer.notification_id);

        udpBroadcastAdapter.stopBroadcast();
        serviceRunning = false;
        // Tell the user we stopped.
        Toast.makeText(this,"Service stopped", Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();

    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = "Service started";

        // The PendingIntent to launch our activity if the user selects this notification
        Intent activityIntent = new Intent(getBaseContext(),V8MainActivity.class);
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                activityIntent, 0);


        // Set the info for the views that show in the notification panel.
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.logo)  // the status icon
                .setTicker(text)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle("Nemo-Q")  // the label of the entry
                .setContentText(text)  // the contents of the entry
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .build();

        // Send the notification.
        notificationManager.notify(R.integer.notification_id, notification);
    }


    private boolean checkConnectivity(){

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null)
        {
            Toast.makeText(this,"No Internet Connection",Toast.LENGTH_SHORT).show();
            return networkInfo.isConnected();
        }
        else
            return false;

    }


    //Restart the socket that listens to connections
    private void restartListen(){


        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean receiveLan = sharedPreferences.getBoolean(getResources().getString(R.string.pref_key_receive), false);
        stopListen();

        if (checkConnectivity() && receiveLan) {

            startListen();
        }

    }


    //Starting the thread
    public void startListen(){

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String listenPortPreference = sharedPreferences.getString(getApplicationContext().getString(R.string.pref_key_listen_port), "8080");
        broadCastConnection(Integer.parseInt(listenPortPreference));
        socketThreadClass = SocketThreadClass.getInstance(this.getApplicationContext(), messageHandler);
        socketThreadInstance = new Thread(socketThreadClass);

        socketThreadInstance.start();



    }

    public void stopListen(){


        socketThreadClass.stopListener();
        udpBroadcastAdapter.stopBroadcast();
        socketThreadInstance.interrupt();


    }


    private void broadCastConnection(int port){


        try {
            udpBroadcastAdapter= new UDPBroadcastAdapter(port,this.getApplicationContext());
        } catch (SocketException e) {
            Log.e("UDP error:", e.toString());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        udpBroadcastAdapter.startBroadcast();





    }



    private void print(byte[] printerBytes) throws IOException {

        Object[] paramObject = new Object[]{printerBytes};
        AsyncTask asyncTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {


                byte[] bytes = (byte[])params[0];
                PrintInterface printInterface = PrintInterface.getInstance(getBaseContext());
                try {
                    printInterface.writeData(bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }

        };
        asyncTask.execute(paramObject);



    }




}
