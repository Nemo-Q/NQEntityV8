package com.nemoq.nqpossysv8.print;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;

import android_serialport_api.SerialPort;

/**
 * Created by Martin Backudd on 2015-08-14.
 */
public class PrintApplication extends android.app.Application {
    private SerialPort mSerialPort = null;

    public SerialPort getSerialPort() throws SecurityException, IOException, InvalidParameterException
    {
        if (mSerialPort == null)
        {
            String path = "/dev/ttyS1";
            int baudrate = 115200;
            boolean flagCon = true;

            File myFile = new File(path);

			/* Open the serial port */
            mSerialPort = new SerialPort(myFile,baudrate,0,flagCon);
        }
        return mSerialPort;
    }

    public void closeSerialPort()
    {
        if (mSerialPort != null)
        {
            mSerialPort.close();
            mSerialPort = null;
        }
    }
}