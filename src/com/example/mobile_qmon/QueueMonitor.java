package com.example.mobile_qmon;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.Random;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

public class QueueMonitor extends Service {
	QueueInterface.Stub mBinder;
	Session session;
	Channel channel;
	ByteArrayOutputStream baos;
	ByteArrayInputStream bais;

	@Override
	public void onCreate() {
		super.onCreate();
		bais = new ByteArrayInputStream(new byte[1000]);

		mBinder = new QueueInterface.Stub() {
			public String retrieveJobs() throws RemoteException {
				try {
					String username = "xxxx";
					String password = "xxxx";
					String host = "xxxx"; // sample ip address
					JSch jsch = new JSch();
					session = jsch.getSession(username, host, 22);
					session.setPassword(password);
					Properties properties = new Properties();
					properties.put("StrictHostKeyChecking", "no");
					session.setConfig(properties);
					session.connect(30000);
					String command = "qstat | grep "+ username;
					channel = session.openChannel("exec");
					((ChannelExec) channel).setCommand(command);
					channel.setInputStream(null);
					channel.connect();
					String qstatOutput = "<initial status>";
					StringBuilder outputBuffer = new StringBuilder();
					InputStream  commandOutput = channel.getInputStream();
					int readByte = commandOutput.read();
					
					while(readByte != 0xffffffff){
						outputBuffer.append((char)readByte);
						readByte = commandOutput.read();
					}
					
					qstatOutput += outputBuffer.toString();
					
					channel.disconnect();
					session.disconnect();

					return qstatOutput;
				} catch (Exception e) {
					return "<output unavailable>";
				}
			}
		};
		// Toast.makeText(this, "Service Created!!!", Toast.LENGTH_LONG).show();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}
