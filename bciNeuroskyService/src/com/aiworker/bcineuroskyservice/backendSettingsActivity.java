package com.aiworker.bcineuroskyservice;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class backendSettingsActivity extends PreferenceActivity {
    @SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.backendsettings);
    }
}