package com.nemoq.nqentityv8.print;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;

import android_serialport_api.SerialPort;

/**
 * Created by Martin Backudd on 2015-08-17. Takes the parsed bytes that receiptparser made and prints them with the V8's print API
 */

public class PrintInterface {




    private static PrintInterface printInterface;

    private SerialPort serialPort;
    public OutputStream outputStream;
    public InputStream inputStream;


    public void writeData(byte[] bytes) throws IOException {

        serialPort = this.getSerialPort();

        OutputStream outputStream =  serialPort.getOutputStream();
        outputStream.write(bytes);
        this.closeSerialPort();


    }


    public static synchronized PrintInterface getInstance(Context ctx){


        if (printInterface == null){


            printInterface = new PrintInterface(ctx);
        }
        return printInterface;

    }

    private PrintInterface(Context ctx){


            try {

                serialPort = this.getSerialPort();


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

