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
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class ActiveJobs extends Activity {
	private LinearLayout rootView;
	private long REFRESH_INTVL = 60000L;
	private int REFRESH_SIGNL = 0;
	private LayoutParams buttonLayout = new LinearLayout.LayoutParams(
			ViewGroup.LayoutParams.FILL_PARENT,
			ViewGroup.LayoutParams.FILL_PARENT, 1.0F);
	QueueInterface service;
	qServiceConnection connection;
	Timer tmr = new Timer();
	TimerTask refresher = new TimerTask() {
		public void run() {
			ActiveJobs.this.mHandler.sendEmptyMessage(REFRESH_SIGNL);
		};
	};

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (service != null) {
				try {
					if (service.retrieveErrorStatus() == 0) {
						String qstatOutput = service.retrieveJobs();
						Log.d("PrintStatement", "qstatOutput RAW: '"
								+ qstatOutput + "'");
						Job[] jobList = parseRawQstatOutput(qstatOutput);
						updateActiveJobView(jobList);
					} else if (service.retrieveErrorStatus() == 1) {
						String errorMessage = service.retrieveErrorMessage();
						Log.d("PrintStatement", "error RAW: '" + errorMessage
								+ "'");
						setErrorView(errorMessage);
						//Toast.makeText(ActiveJobs.this, "Error Detected", Toast.LENGTH_LONG).show();
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
	};

	class Job {
		private String name, ID;
		private int processingTime, wallTime, queueTimeLimit;
		private boolean errorStatus = true;

		public Job(String ID, String name) {
			this.name = name;
			this.ID = ID;
		}

		String getID() {
			return this.ID;
		}

		String getName() {
			return this.name;
		}

		int getTimeEfficiency() {
			return (this.processingTime / this.wallTime) * 100;
		}

		int getTimeLeftInQueue() {
			return this.queueTimeLimit - this.wallTime;
		}

		void setErrorStatus(boolean errorStatus) {
			this.errorStatus = errorStatus;
		}

		boolean getErrorStatus() {
			return this.errorStatus;
		}
	};

	class qServiceConnection implements ServiceConnection {
		public void onServiceConnected(ComponentName name, IBinder boundService) {
			service = QueueInterface.Stub.asInterface((IBinder) boundService);
			//Toast.makeText(ActiveJobs.this, "Service Connected!", Toast.LENGTH_SHORT).show();
		}

		public void onServiceDisconnected(ComponentName name) {
			service = null;
			//Toast.makeText(ActiveJobs.this, "Service Disconnected!", Toast.LENGTH_SHORT).show();
		}
	}

	private void initService() {
		connection = new qServiceConnection();
		Intent i = new Intent();
		i.setClassName("com.example.mobile_qmon",
				com.example.mobile_qmon.QueueMonitor.class.getName());
		if (!bindService(i, connection, Context.BIND_AUTO_CREATE)) {
			//Toast.makeText(ActiveJobs.this, "Bind Service Failed!", Toast.LENGTH_LONG).show();
		}
	}

	private void releaseService() {
		unbindService(connection);
		connection = null;
	}

	private void setErrorView(String errorMessage) {
		rootView.removeAllViews();
		Button errorButt = new Button(this);
		errorButt.setText(errorMessage);
		errorButt.setTextColor(Color.RED);
		errorButt.setLayoutParams(buttonLayout);
		rootView.addView(errorButt);
	}

	private void updateActiveJobView(Job[] jobList) {
		rootView.removeAllViews();
		if (jobList != null) {
			for (Job j : jobList) {
				Button butt = new Button(this);
				String activeJobDisplayName = j.getID() + "   " + j.getName();
				butt.setText(activeJobDisplayName);
				butt.setTextColor(Color.BLACK);
				butt.setLayoutParams(buttonLayout);
				rootView.addView(butt);
			}
		} else {
			Button emptyButt = new Button(this);
			String status = "NO JOBS DETECTED";
			emptyButt.setText(status);
			emptyButt.setTextColor(Color.BLACK);
			emptyButt.setLayoutParams(buttonLayout);
			rootView.addView(emptyButt);
		}
	}

	private Job[] parseRawQstatOutput(String qstatOutput) {
		Job[] jobList = null;
		if (qstatOutput != null && qstatOutput.length() > 0) {
			int numJobs;
			String[] formattedOutput;
			formattedOutput = qstatOutput.split("\n");
			numJobs = formattedOutput.length;
			jobList = new Job[numJobs];
			for (int i = 0; i < numJobs; i++) {
				Log.d("PrintStatement", "job name: '" + formattedOutput[i]
						+ "'");
				String line = formattedOutput[i];
				line = line.replaceAll("\\s+", ",");
				Job j;
				if (line.split(",").length >= 6) {
					String jobId = line.split(",")[0];
					String jobName = line.split(",")[1];
					String jobUser = line.split(",")[2];
					String jobTime = line.split(",")[3];
					String jobStatus = line.split(",")[4];
					String jobQueue = line.split(",")[5];
					j = new Job(jobId, jobName + " " + jobTime + " "
							+ jobStatus + " " + jobQueue);
					j.setErrorStatus(false);
				} else {
					j = new Job("-1", line);
				}
				jobList[i] = j;
			}
			Log.d("PrintStatement", "job length: " + numJobs);
		}
		return jobList;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		rootView = new LinearLayout(this);
		rootView.setOrientation(LinearLayout.VERTICAL);
		rootView.setBackgroundColor(0xFF14400B);
		rootView.setLayoutParams(new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT, 0.0F));

		setContentView(rootView);

		initService();

		tmr.scheduleAtFixedRate(refresher, 9000, REFRESH_INTVL);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
		case R.id.item_prefs:
			startActivity(new Intent(this,QMPreferences.class));
			break;
		}
		return true;
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
