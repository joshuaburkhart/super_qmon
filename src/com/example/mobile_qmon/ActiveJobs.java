package com.example.mobile_qmon;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class ActiveJobs extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_jobs);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_active_jobs, menu);
        return true;
    }
}
