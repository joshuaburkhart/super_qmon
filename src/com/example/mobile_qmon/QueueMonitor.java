package com.example.mobile_qmon;

import java.util.ArrayList;
import java.util.Random;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.widget.Toast;

public class QueueMonitor extends Service {
	private final long ONE_MINUTE = 60000;
	private String[] qstatOutput;
	private static boolean paused = false;
	static final int MSG_SET_STRING_VALUE = 4;
	static final int MSG_UNREGISTER_CLIENT = 2;
	static final int MSG_SET_INT_VALUE = 3;
	static final int MSG_REGISTER_CLIENT = 1;
	ArrayList<Messenger> mClients = new ArrayList<Messenger>(); // Keeps track
																// of all
																// current
																// registered
																// clients.
	final Messenger mMessenger = new Messenger(new IncomingHandler()); // Target
																		// we
																		// publish
																		// for
																		// clients
																		// to
																		// send
																		// messages
																		// to
																		// IncomingHandler.

	public QueueMonitor() {
		super();
	}

	protected void retrieveJobs() {
		Toast.makeText(this, "Service retreiving jobs!!!", Toast.LENGTH_LONG)
				.show();

		// get UN / password
		// get servername
		// ssh into servername
		// execute command
		// retreive command output
		// split output into lines
		// count lines
		// initialize qstatOutput
		// fill qstatOutput

		Random r = new Random();
		int numOutputLines = r.nextInt(5);
		String qstatOutput = "";
		for (int i = 0; i < numOutputLines; i++) {
			qstatOutput += i + "   process XXX -- " + "\n";
		}

		sendMessageToUI(qstatOutput);

		try {
			System.out.print("Trying to sleep!!!!!!");
			Thread.sleep(ONE_MINUTE);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			System.out.println("wtf stack trace!!!");
			e.printStackTrace();
		}
	}

	private void sendMessageToUI(String val2send) {
		for (Object m : mClients.toArray()) {
			try {

				Messenger msgr = (Messenger) m;
				
				// Send data as a String
				Bundle b = new Bundle();
				b.putString("str1", val2send);
				Message msg = Message.obtain(null, MSG_SET_STRING_VALUE);
				msg.setData(b);
				msgr.send(msg);

			} catch (RemoteException e) {
				// The client is dead. Remove it from the list; we are going
				// through the list from back to front so this is safe to do
				// inside the loop.
				mClients.remove(m);
			}
		}

	}

	@SuppressLint("HandlerLeak")
	class IncomingHandler extends Handler { // Handler of incoming messages from
											// clients.
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_REGISTER_CLIENT:
				mClients.add(msg.replyTo);
				break;
			case MSG_UNREGISTER_CLIENT:
				mClients.remove(msg.replyTo);
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mMessenger.getBinder();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Toast.makeText(this, "Service Created!!!", Toast.LENGTH_LONG).show();
	}

	public static boolean isPaused() {
		return paused;
	}
	
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		Toast.makeText(this, "Service Started!!!", Toast.LENGTH_LONG).show();

		while (!paused) {
			retrieveJobs();
		}

		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		paused = true;
		super.onDestroy();
	}
}
