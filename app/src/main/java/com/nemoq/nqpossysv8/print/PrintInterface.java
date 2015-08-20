package com.nemoq.nqpossysv8.print;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;
import java.util.Calendar;

import android_serialport_api.SerialPort;

/**
 * Created by Temporär on 2015-08-17.
 */

public class PrintInterface {



    private SerialPort serialPort;
    public OutputStream outputStream;
    public InputStream inputStream;
    public byte[] dataToWrite;

    private class SendData extends AsyncTask<byte[],Integer,Boolean> {


        @Override
        protected Boolean doInBackground(byte[]... byteData) {



            try {



                outputStream.write(dataToWrite);
                return true;




            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

        }
    }





    public PrintInterface(){


            try {

                serialPort = this.getSerialPort();
                outputStream = serialPort.getOutputStream();
                inputStream = serialPort.getInputStream();

            }
            catch (SecurityException e) {
                Log.e("Access:.", e.toString());
            }
            catch (IOException e) {
                Log.e("Serialport cannot open","");
            }
            catch (InvalidParameterException e) {
                Log.e("Error:","Configure");
            }


    }


    public byte[] writeData(byte[] bytes){


            dataToWrite = bytes;

            SendData sendData = new SendData();
            sendData.execute(bytes);

            byte[]  bytesRead = new byte[8];
            try {
                inputStream.read(bytesRead);
            }
            catch (Exception e){

                Log.e("InputStream error:", e.toString());

            }
            return bytesRead;


    }






    public SerialPort getSerialPort() throws SecurityException, IOException, InvalidParameterException
    {
        if (serialPort == null)
        {
            String path = "/dev/ttyS1";
            int baudrate = 115200;
            boolean flagCon = true;

            File myFile = new File(path);

			/* Open the serial port */
            serialPort = new SerialPort(myFile,baudrate,0,flagCon);
        }
        return serialPort;
    }

    public void closeSerialPort()
    {
        if (serialPort != null)
        {
            serialPort.close();
            serialPort = null;
        }
    }


}

