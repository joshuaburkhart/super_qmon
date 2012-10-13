package com.example.mobile_qmon;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.view.Menu;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

public class ActiveJobs extends Activity {
	private LinearLayout rootView;
	Messenger mService = null;
	final Messenger mMessenger = new Messenger(new IncomingHandler());
	boolean mIsBound;
	@SuppressLint("HandlerLeak")
	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == QueueMonitor.MSG_SET_STRING_VALUE) {
				String qstatOutput = msg.getData().getString("str1");
				Job[] jobList = parseRawQstatOutput(qstatOutput);
				updateActiveJobView(jobList);
			} else {
				super.handleMessage(msg);
			}
		}
	}
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
	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mService = new Messenger(service);
			try {
				Message msg = Message.obtain(null,
						QueueMonitor.MSG_REGISTER_CLIENT);
				msg.replyTo = mMessenger;
				mService.send(msg);
			} catch (RemoteException e) {
				// In this case the service has crashed before we could even do
				// anything with it
			}
		}

		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the service has been
			// unexpectedly disconnected - process crashed.
			mService = null;
		}
	};
	
	void doBindService() {
		bindService(new Intent(this, QueueMonitor.class), mConnection,
				Context.BIND_AUTO_CREATE);
		mIsBound = true;
	}

	void doUnbindService() {
		if (mIsBound) {
			// If we have received the service, and hence registered with it,
			// then now is the time to unregister.
			if (mService != null) {
				try {
					Message msg = Message.obtain(null,
							QueueMonitor.MSG_UNREGISTER_CLIENT);
					msg.replyTo = mMessenger;
					mService.send(msg);
				} catch (RemoteException e) {
					// There is nothing special we need to do if the service has
					// crashed.
				}
			}
			// Detach our existing connection.
			unbindService(mConnection);
			mIsBound = false;
		}
	}

	private void sendMessageToService(int intvaluetosend) {
		if (mIsBound) {
			if (mService != null) {
				try {
					Message msg = Message.obtain(null,
							QueueMonitor.MSG_SET_INT_VALUE, intvaluetosend, 0);
					msg.replyTo = mMessenger;
					mService.send(msg);
				} catch (RemoteException e) {
				}
			}
		}
	}

	private void CheckIfServiceIsRunning() {
		// If the service is running when the activity starts, we want to
		// automatically bind to it.
		if (QueueMonitor.isPaused()) {
			doBindService();
		}
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
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_active_jobs, menu);
		return true;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		rootView = new LinearLayout(this);
		rootView.setOrientation(LinearLayout.VERTICAL);
		rootView.setBackgroundColor(Color.WHITE);
		rootView.setLayoutParams(new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT, 0.0F));

		setContentView(rootView);

		Thread t = new Thread() {
			public void run() {
				startService(new Intent(ActiveJobs.this, QueueMonitor.class));
			}
		};
		t.start();
		
	    CheckIfServiceIsRunning();

	}
	
	@Override
	protected void onDestroy() {
		try {
			doUnbindService();
		} catch (Throwable t) {
		}

		stopService(new Intent(ActiveJobs.this, QueueMonitor.class));
		super.onDestroy();
	}
}
