package com.example.mobile_qmon;

import java.util.Timer;
import java.util.TimerTask;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class ActiveJobs extends Activity{
	private LinearLayout rootView;
	private long REFRESH_INTVL = 60000L;
	private int REFRESH_SIGNL = 0;
	QueueInterface service;
	qServiceConnection connection;
	Timer tmr = new Timer();
	TimerTask refresher = new TimerTask() {
		public void run() {
			ActiveJobs.this.mHandler.sendEmptyMessage(REFRESH_SIGNL);
		};
	};
	
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg){
			if (service != null) {
				try {
					String qstatOutput = service.retrieveJobs();
					Job[] jobList = parseRawQstatOutput(qstatOutput);
					updateActiveJobView(jobList);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};
	
	class Job {
		private String name, ID;
		private int processingTime, wallTime, queueTimeLimit;

		public Job(String ID, String name) {
			this.name = name;
			this.ID = ID;
		}

		String getID() {
			return ID;
		}

		String getName() {
			return name;
		}

		int getTimeEfficiency() {
			return (processingTime / wallTime) * 100;
		}

		int getTimeLeftInQueue() {
			return queueTimeLimit - wallTime;
		}
	};

	class qServiceConnection implements ServiceConnection {
		public void onServiceConnected(ComponentName name, IBinder boundService) {
			service = QueueInterface.Stub.asInterface((IBinder) boundService);
			Toast.makeText(ActiveJobs.this, "Service Connected!",
					Toast.LENGTH_SHORT).show();
		}

		public void onServiceDisconnected(ComponentName name) {
			service = null;
			Toast.makeText(ActiveJobs.this, "Service Disconnected!",
					Toast.LENGTH_SHORT).show();
		}
	}
	
	private void initService() {
		connection = new qServiceConnection();
		Intent i = new Intent();
		i.setClassName("com.example.mobile_qmon",
				com.example.mobile_qmon.QueueMonitor.class.getName());
		if (!bindService(i, connection, Context.BIND_AUTO_CREATE)) {
			Toast.makeText(ActiveJobs.this, "Bind Service Failed!",
					Toast.LENGTH_LONG).show();
		}
	}

	private void releaseService() {
		unbindService(connection);
		connection = null;
	}

	private void updateActiveJobView(Job[] jobList) {
		rootView.removeAllViews();
		for (Job j : jobList) {
			Button butt = new Button(this);
			String activeJobDisplayName = j.getID() + "   " + j.getName();
			butt.setText(activeJobDisplayName);
			butt.setTextColor(Color.BLACK);
			butt.setLayoutParams(new LinearLayout.LayoutParams(
					ViewGroup.LayoutParams.FILL_PARENT,
					ViewGroup.LayoutParams.FILL_PARENT, 1.0F));
			rootView.addView(butt);
		}
	}

	private Job[] parseRawQstatOutput(String qstatOutput) {
		String[] formattedOutput = qstatOutput.split("\n");
		Job[] jobList = new Job[formattedOutput.length];
		for (int i = 0; i < formattedOutput.length; i++) {
			Job j = new Job("<jobID>", formattedOutput[i]);
			jobList[i] = j;
		}
		return jobList;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		rootView = new LinearLayout(this);
		rootView.setOrientation(LinearLayout.VERTICAL);
		rootView.setBackgroundColor(Color.GREEN);
		rootView.setLayoutParams(new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT, 0.0F));

		setContentView(rootView);

		initService();

		tmr.scheduleAtFixedRate(refresher, 0, REFRESH_INTVL);
	}

	@Override
	protected void onDestroy() {
		releaseService();
		super.onDestroy();
	}

	public void run() {
		// TODO Auto-generated method stub
		
	}
}
