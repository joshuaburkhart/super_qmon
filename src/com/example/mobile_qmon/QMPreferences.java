package com.example.mobile_qmon;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class QMPreferences extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);
    }
}
