package me.seet.flagquiz;

import android.os.Bundle;
import android.preference.PreferenceFragment;

// SettingsFragment.java
// Subclass of PreferenceFragment for managing app settings
public class SettingsFragment extends PreferenceFragment {
    // creates preferences GUI from preferences.xml file in res/xml

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);  // load from XML
    }
}
