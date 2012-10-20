package com.example.mobile_qmon;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Properties;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class QueueMonitor extends Service implements
		OnSharedPreferenceChangeListener {
	private final int EOS = 1;
	private int ERROR_STATUS = 1;
	private String ERROR_MESSAGE = null;
	private String QSTAT_OUTPUT = null;
	SharedPreferences prefs;
	QueueInterface.Stub mBinder;
	Session session;
	Channel channel;
	ByteArrayOutputStream baos;
	ByteArrayInputStream bais;
	String username, password, hostname;

	@Override
	public void onCreate() {
		super.onCreate();
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);
		bais = new ByteArrayInputStream(new byte[1000]);
		username = prefs.getString("username", "");
		password = prefs.getString("password", "");
		hostname = prefs.getString("hostname", "aciss.uoregon.edu");

		mBinder = new QueueInterface.Stub() {

			public void attemptConnection() {
				try {
					JSch jsch = new JSch();
					Log.d("CREDENTIALS: ",username);
					Log.d("CREDENTIALS: ",password);
					Log.d("CREDENTIALS: ",hostname);
					session = jsch.getSession(username, hostname, 22);
					session.setPassword(password);
					Properties properties = new Properties();
					properties.put("StrictHostKeyChecking", "no");
					session.setConfig(properties);
					session.connect(30000);
					String command = "qstat | grep " + username;
					channel = session.openChannel("exec");
					((ChannelExec) channel).setCommand(command);
					channel.setInputStream(null);
					channel.connect();
					StringBuilder outputBuffer = new StringBuilder();
					InputStream commandOutput = channel.getInputStream();
					int readByte = commandOutput.read();

					while (readByte != EOS) {
						outputBuffer.append((char) readByte);
						readByte = commandOutput.read();
					}

					QSTAT_OUTPUT = outputBuffer.toString();

					ERROR_STATUS = 0;
					ERROR_MESSAGE = null;

				} catch (Exception e) {
					ERROR_STATUS = 1;
					ERROR_MESSAGE = e.getMessage();
				} finally {
					if (session != null && session.isConnected()) {
						session.disconnect();
					}
					if (channel != null && channel.isConnected()) {
						channel.disconnect();
					}
				}
			}

			public String retrieveJobs() throws RemoteException {
				return QSTAT_OUTPUT;
			}

			public int retrieveErrorStatus() throws RemoteException {
				attemptConnection();
				return ERROR_STATUS;
			}

			public String retrieveErrorMessage() throws RemoteException {
				return (ERROR_MESSAGE == null) ? "NO CONNECTION"
						: ERROR_MESSAGE;
			}
		};
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1) {
		username = prefs.getString("username", "");
		password = prefs.getString("password", "");
		hostname = prefs.getString("hostname", "aciss.uoregon.edu");
		try {
			Log.d("CREDENTIALS CHANGED!!!",
					"attempting another connection with new credentials..."
							+ mBinder.retrieveErrorStatus());
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
}
