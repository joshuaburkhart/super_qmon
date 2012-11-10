package com.burkhart.super_qmon;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import com.burkhart.super_qmon.R;

public class QueueMonitorPreferenceActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);
    }
}
