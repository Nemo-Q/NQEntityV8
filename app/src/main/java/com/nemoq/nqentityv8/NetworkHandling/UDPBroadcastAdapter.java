package com.nemoq.nqentityv8.NetworkHandling;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Martin Backudd on 2015-09-29. //Sending udp broadcast to ease the connection between ticket editor.
 */
public class UDPBroadcastAdapter  {


    private  ScheduledExecutorService threadPoolExecutor;

    private Timer broadcastTimer;

    String connection;
    int port;
    private int servport;
    Context context;
    DatagramSocket datagramSocket;
    InetAddress ipAddresses;


    public UDPBroadcastAdapter(int p,Context cont) throws SocketException, UnknownHostException {
        context = cont;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);


        port = Integer.parseInt(sharedPreferences.getString("udp_port","8000"));
        servport = p;


        String name = sharedPreferences.getString("device_name","Entity V8");
        connection = name + "," +  listIp() + ":" + Integer.toString(servport);

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
