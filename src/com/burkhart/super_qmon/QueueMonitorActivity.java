package com.burkhart.super_qmon;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import com.burkhart.super_qmon.R;

public class QueueMonitorActivity extends Activity {
	private LinearLayout rootView;
	private ComponentName service;
	private SharedPreferences prefs;
	private LayoutParams buttonLayout = new LinearLayout.LayoutParams(
			ViewGroup.LayoutParams.FILL_PARENT,
			ViewGroup.LayoutParams.FILL_PARENT, 1.0F);
	QueuedJobAnnouncementReceiver receiver;

	public class QueuedJobAnnouncementReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			int eStatus = 0;
			String output = "";
			String eMessage = "";
			if (intent == null) {
				Log.d("tag!!!", "received a null intent...");
			} else if (intent.getExtras() == null) {
				Log.d("tag!!!", "intent.getExtras is null...");
			} else {
				Log.d("tag!!!", intent.getExtras().toString());
				output = intent.getExtras().getString("output");
				Log.d("tag!!!", "output: " + output);
				eStatus = intent.getExtras().getInt("eStatus");
				Log.d("tag!!!", "eStatus: " + eStatus + "");
				eMessage = intent.getExtras().getString("eMessage");
				Log.d("tag!!!", "eMessage: " + eMessage);
			}
			try {
				if (eStatus == 0) {
					String qstatOutput = output;
					Log.d("PrintStatement", "qstatOutput RAW: '" + qstatOutput
							+ "'");
					Job[] jobList = parseRawQstatOutput(qstatOutput);
					updateActiveJobView(jobList);
				} else if (eStatus == 1) {
					String errorMessage = eMessage;
					Log.d("PrintStatement", "error RAW: '" + errorMessage + "'");
					setErrorView(errorMessage);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

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
					// String jobUser = line.split(",")[2];
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
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
	}

	@Override
	protected void onResume() {
		String username = prefs.getString("username", "");
		if (username.equals("")) {
			Log.d("tag!!!", "username is blank!");
			startActivity(new Intent(this, QueueMonitorPreferenceActivity.class));
		} else {
			Log.d("tag!!!", "resuming main activity... starting service");
			service = startService(new Intent(this, QueueMonitorService.class));
			IntentFilter filter = new IntentFilter(
					QueueMonitorService.BCAST_QJA);
			receiver = new QueuedJobAnnouncementReceiver();
			registerReceiver(receiver, filter);
		}
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.item_prefs:
			Log.d("tag!!!", "onOptionsItemSelected called, stopping service...");
			stopService(new Intent(this, service.getClass()));
			startActivity(new Intent(this, QueueMonitorPreferenceActivity.class));
			break;
		}
		return true;
	}

	@Override
	protected void onPause() {
		if (receiver != null) {
			unregisterReceiver(receiver);
		}
		super.onPause();
	}

}
