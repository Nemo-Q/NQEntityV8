package com.nemoq.nqpossysv8.NetworkHandling;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Handler;

import com.nemoq.nqpossysv8.print.ReceiptParser;

import org.apache.http.protocol.HTTP;
import org.xmlpull.v1.XmlPullParserException;


/**
 * Created by Martin Backudd on 2015-09-01. Accepts incoming connections ex. http
 */
public class SocketThreadClass implements Runnable{

    private boolean acceptConnections;
    private boolean running;
    public String ipAddress;
    private Handler messageHandler;
    private Context context;


    private static int LISTEN_STARTED = 0;
    private static int RECEIVED_DATA = 1;
    private static int LISTEN_STOPPED = 2;
    private static int WEIRD_DATA = 3;
    private static int ACCEPTED_CONNECTION = 4;

    private int serverPort;

    private static long TIMEOUT_INTERVAL = 30000;


    private static SocketThreadClass socketThreadClass;

    private ArrayList<Socket> connectionList;


    private SocketThreadClass(Context ctx,Handler handler){

        messageHandler = handler;
        running = true;
        acceptConnections = true;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        serverPort = Integer.parseInt(sharedPreferences.getString("listen_port","8080"));


        context = ctx;
    }

    public void stopListener(){


        running = false;
        socketThreadClass = null;

    }


     public static synchronized SocketThreadClass getInstance(Context ctx,Handler handler){


        if (socketThreadClass == null){


            socketThreadClass = new SocketThreadClass(ctx,handler);

        }

         return socketThreadClass;

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


    //The method for the runnable
    @Override
    public void run(){

        listIp();

        try {


            ServerSocket serverSocket = new ServerSocket(serverPort);
            sendMessage(LISTEN_STARTED,"Listening to port:"+serverPort);

            while (running) {

                    //Listen for connection

                    Socket socket = serverSocket.accept();

                    sendMessage(ACCEPTED_CONNECTION, socket.getPort());


                    //Retrieve the data into a hash-map

                    Map<String, String> table = receiveData(socket);
                    byte[] printBytes = null;

                    // Make printer bytes


                    if (table != null && Integer.parseInt(table.get("Content-Length")) > 0) {
                        if (table.get("Content-Type").equals("application/json"))
                            printBytes = ReceiptParser.getInstance(context).JSONToPrinterCommand(table.get("Body"));
                        else
                            printBytes = ReceiptParser.getInstance(context).xmlStringToPrinterCommand(table.get("Body"));


                        //How did it go?
                        if ((!table.get("Content-Type").equals("qticket"))) {
                            sendMessage(WEIRD_DATA, "Wrong content type");
                        } else if (table.get("Body").length() == 0) {

                            sendMessage(WEIRD_DATA, "No body");

                        } else if (printBytes == null) {
                            //Bad formatting from the receipt parser
                            sendMessage(WEIRD_DATA, "Bad Format");
                            sendResponse(socket, "Bad formatting");
                        } else {
                            //All good lets print
                            sendMessage(RECEIVED_DATA, printBytes);
                            sendResponse(socket, "Printing");
                        }
                    }else {

                        sendResponse(socket, "No Data");



                    }

                    socket.close();


                }

        } catch (UnknownHostException e) {
            Log.e("Connection error:", e.toString());
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("I/O Exception:", e.toString());
            e.printStackTrace();
        }

        sendMessage(LISTEN_STOPPED,"Stopped listening for connection");
    }



    //Send Http Response.

    private void sendResponse(Socket socket,String message) throws IOException {



        OutputStream os = socket.getOutputStream();
        String response = HttpParser.makeHttpResponse(message);
        os.write(response.getBytes(Charset.forName("UTF-8")));

    }


    //Handle the connection, read the stream, parse the http message.
    private Map<String, String> receiveData(Socket socket){

        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        Map<String,String> httpTable;


        try {

            DataInputStream  inputStream = new DataInputStream(socket.getInputStream());

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            if (inputStream != null){



                int bytesAvailable;
                byte[] buffer = new byte[1024];
                HttpParser httpParser = new HttpParser();

                int bytesRead;
                byte[] bytes;
                long startTime = System.currentTimeMillis();

                while ((bytesRead = inputStream.read(buffer)) != -1) {


                    //timeout
                    if ((System.currentTimeMillis() - startTime) > TIMEOUT_INTERVAL){
                        break;

                    }
                    bytes = new byte[bytesRead];


                    System.arraycopy(buffer, 0, bytes, 0, bytesRead);


                    if (httpParser.parse(bytes))
                        break;



                }


                httpTable = httpParser.getTable();

                return httpTable;


            }


        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (Exception e){
            Log.e("SocketThreadClass e:", e.toString());
        }

        return null;
    }


    private void sendMessage(int type,Object obj){


            Message message = Message.obtain();
            message.what = type;
            message.obj = obj;
            message.setTarget(messageHandler);
            message.sendToTarget();

    }


}
