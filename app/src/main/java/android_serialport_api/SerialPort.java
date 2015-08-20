package android_serialport_api;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SerialPort
{
	/*
	 * Do not remove or rename the field mFd: it is used by native method
	 * close()    ;
	 */
	private FileDescriptor mFd;
	private FileOutputStream mFileOutputStream;
	private FileInputStream mFileInputStream;

	public SerialPort(File device, int baudrate, int flags, boolean flowCon)
			throws SecurityException, IOException
	{
		// Check access permission
		if (!device.canRead() || !device.canWrite())
		{
			try
			{
				// Missing read/write permission, trying to chmod the file 
				Process su;
				su = Runtime.getRuntime().exec("system/xbin/su");
				String cmd = "chmod 666 " + device.getAbsolutePath() + "\n"
						+ "exit\n";
				su.getOutputStream().write(cmd.getBytes());
				if ((su.waitFor() != 0) || !device.canRead()
						|| !device.canWrite())
				{
					throw new SecurityException();
				}
			} catch (Exception e)
			{
				e.printStackTrace();
				throw new SecurityException();
			}
		}

		mFd = open(device.getAbsolutePath(), baudrate, flags, flowCon);
		if (mFd == null)
		{
			throw new IOException();
		}
		mFileOutputStream = new FileOutputStream(mFd);
		mFileInputStream = new FileInputStream(mFd);

	}
	
	// JNI
	static
	{
		//���ؿ��ļ�
		System.loadLibrary("serial_port");
	}
	//����ԭ������
	private native static FileDescriptor open(String path, int baudrate, int flags, boolean flowCon);
	public native void close();

	public OutputStream getOutputStream() {return mFileOutputStream;}
	public InputStream getInputStream()
	{
		return mFileInputStream;
	}
}
