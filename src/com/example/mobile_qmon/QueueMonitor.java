package com.example.mobile_qmon;

import java.util.Random;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

public class QueueMonitor extends Service {

	QueueInterface.Stub mBinder;

	@Override
	public void onCreate() {
		super.onCreate();
		mBinder = new QueueInterface.Stub() {
			public String retrieveJobs() throws RemoteException{

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
				return qstatOutput;
			}
		};
		//Toast.makeText(this, "Service Created!!!", Toast.LENGTH_LONG).show();
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
