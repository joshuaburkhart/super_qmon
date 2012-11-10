package com.burkhart.super_qmon;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class QueueMonitorService extends Service {
	private final int EOS = -1;
	public static final String BCAST_QJA = "Queued_Job_Announcement";
	private long REFRESH_INTVL = 60000L;
	private boolean credentials_valid;
	private SharedPreferences prefs;
	private Session session;
	private Channel channel;
	private String username, password, hostname;
	private Timer updateTimer;
	private TimerTask doRefresh;

	public void refreshQueuedJobs() {
		String qStatOutput = null;
		String eMessage = null;
		int eStatus = 0;
		try {
			JSch jsch = new JSch();
			session = jsch.getSession(username, hostname, 22);
			session.setPassword(password);
			Properties properties = new Properties();
			properties.put("StrictHostKeyChecking", "no");
			session.setConfig(properties);
			session.connect(30000);
			String command = "qstat | grep " + username + " | tr -s [:blank:]";
			channel = session.openChannel("exec");
			((ChannelExec) channel).setCommand(command);
			channel.setInputStream(null);
			channel.connect();
			StringBuilder outputBuffer = new StringBuilder();
			InputStream commandOutput = channel.getInputStream();
			int readByte;
			while ((readByte = commandOutput.read()) != EOS) {
				outputBuffer.append((char) readByte);
			}
			qStatOutput = outputBuffer.toString();
		} catch (NullPointerException e) {
			eStatus = 1;
			eMessage = e.getMessage();
		} catch (IOException e) {
			eStatus = 1;
			eMessage = e.getMessage();
		} catch (JSchException e) {
			credentials_valid = false;
			eStatus = 1;
			eMessage = e.getMessage();
		} finally {
			if (session != null && session.isConnected()) {
				session.disconnect();
			}
			if (channel != null && channel.isConnected()) {
				channel.disconnect();
			}
			announceQueuedJobs(qStatOutput, eStatus, eMessage);
		}
	}

	private void announceQueuedJobs(String output, int eStatus, String eMessage) {
		Intent intent = new Intent(BCAST_QJA);
		intent.putExtra("output", output);
		intent.putExtra("eStatus", eStatus);
		intent.putExtra("eMessage", eMessage);
		sendBroadcast(intent);
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		username = prefs.getString("username", "");
		password = prefs.getString("password", "");
		hostname = prefs.getString("hostname", "aciss.uoregon.edu");
		credentials_valid = true;
		if (doRefresh != null) {
			doRefresh.cancel();
			doRefresh = null;
		}
		if (updateTimer != null) {
			updateTimer.cancel();
			updateTimer = null;
		}
		updateTimer = new Timer("queueMonitorUpdates");
		doRefresh = new TimerTask() {
			public void run() {
				if (credentials_valid) {
					refreshQueuedJobs();
				} else {
				}
			}
		};
		updateTimer.scheduleAtFixedRate(doRefresh, 0, REFRESH_INTVL);
	}
}
