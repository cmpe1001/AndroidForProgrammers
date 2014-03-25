package me.seet.flagquiz;

import android.app.Activity;
import android.os.Bundle;

// SettingsActivity.java
// Activity to display SettingsFragment on a phone
public class SettingsActivity extends Activity {
    // use FragmentManager to display SettingsFragment
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }
}
