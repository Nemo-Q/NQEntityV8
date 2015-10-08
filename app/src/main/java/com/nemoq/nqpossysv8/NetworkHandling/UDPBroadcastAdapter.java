package com.nemoq.nqpossysv8.NetworkHandling;

import android.app.Application;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Timer;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by Martin on 2015-09-29.
 */
public class UDPBroadcastAdapter  {



    private  ScheduledExecutorService threadPoolExecutor;

    private Timer broadcastTimer;

    String connection;
    int port;
    int SERVER_PORT = 8080;
    Context context;
    DatagramSocket datagramSocket;
    InetAddress ipAddresses;


    public UDPBroadcastAdapter(int p,Context cont) throws SocketException, UnknownHostException {


        context = cont;
        connection = "Nemo-Q Entity 8," +  listIp() + ":" + Integer.toString(8080);

        port = p;

        broadcastTimer = new Timer();

        WifiManager wifi =  (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        // handle null somehow

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);

        ipAddresses = InetAddress.getByAddress(quads);

        datagramSocket = new DatagramSocket(port);

        datagramSocket.setBroadcast(true);


    }




    public void startBroadcast(){


        threadPoolExecutor = Executors.newScheduledThreadPool(5);



        threadPoolExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
           public void run(){


                try {
                    sendUDP(port,connection.getBytes());
                    Log.d("UDP BROADCAST:", "Sending: " + connection);
                } catch (IOException e) {
                    Log.e("UDP BROADCAST:", "ERROR" + e.toString());
                }


            }},0,15,TimeUnit.SECONDS);



    }


    public void stopBroadcast(){





        threadPoolExecutor.shutdown();






    }


    private String listIp(){

        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress() && inetAddress.isSiteLocalAddress()) {
                        Log.i("Device ip:", inetAddress.getHostAddress().toString());
                        return inetAddress.getHostAddress().toString();
                    }

                }
            }

        } catch (SocketException ex) {
            Log.e("LOG_TAG", ex.toString());
            return "";
        }

        return "";

    }





    private void sendUDP(int port,byte[] bytes) throws IOException {






        DatagramPacket datagramPacket = new DatagramPacket(bytes,bytes.length,ipAddresses,port);

        datagramSocket.send(datagramPacket);








    }







}
