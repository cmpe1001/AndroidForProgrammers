package me.seet.flagquiz;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.Set;


public class MainActivity extends Activity {

    // keys for reading data from SharedPreferences
    public static final String CHOICES = "pref_numberOfChoices";
    public static final String REGIONS = "pref_regionsToInclude";

    private boolean mPhoneDevice = true; // used to force portrait mode
    private boolean mPreferencesChanged = true;  // did preferences change?

    // listener for changes to the app's SharedPreferences
    private SharedPreferences.OnSharedPreferenceChangeListener mPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            mPreferencesChanged = true; // user changed app settings
            QuizFragment quizFragment = (QuizFragment)
                    getFragmentManager().findFragmentById(R.id.quizFragment);
            if(key.equals(CHOICES)) // # of choices to display changed
            {
                quizFragment.updateRegions(sharedPreferences);
                quizFragment.resetQuiz();
            }
            else if(key.equals(REGIONS)) // regions to include changed
            {
                Set<String> regions = sharedPreferences.getStringSet(REGIONS, null);
                if(regions != null && regions.size() > 0) {
                    quizFragment.updateGuessRows(sharedPreferences);
                    quizFragment.resetQuiz();
                }
                else // must select one region--set North America as default
                {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    regions.add(getResources().getString(R.string.default_region));
                    editor.putStringSet(REGIONS,regions);
                    editor.commit();
                    Toast.makeText(MainActivity.this, R.string.default_region_message, Toast.LENGTH_SHORT).show();
                }
            }

            Toast.makeText(MainActivity.this, R.string.restarting_quiz, Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set default values in the app's SharedPreferences
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // register listener for SharedPreferences changes
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(mPreferenceChangeListener);

        // determine screen size
        int screenSize = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;

        // if device is a table, set mPhoneDevice to false
        if(screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE || screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE)
            mPhoneDevice = false;   // not a phone-sized device

        // if running on phone-sized device, allow portrait orientation
        if(mPhoneDevice)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    // called after onCreate completes execution
    @Override
    protected void onStart() {
        super.onStart();
        if(mPreferencesChanged) {
            // now that the default preferences have been set,
            // initialize QuizFragment and start the quiz
            QuizFragment quizFragment = (QuizFragment)
                    getFragmentManager().findFragmentById(R.id.quizFragment);
            quizFragment.updateGuessRows(PreferenceManager.getDefaultSharedPreferences(this));
            quizFragment.updateRegions(PreferenceManager.getDefaultSharedPreferences(this));
            quizFragment.resetQuiz();
            mPreferencesChanged = false;
        }
    }

    // show menu if app is running on a phone or a portrait-oriented tablet
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // get the default Display object representing the screen
        Display display = ((WindowManager)getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
        Point screenSize = new Point();
        display.getRealSize(screenSize);    // store size in screenSize

        // display the app's menu only in portrait orientation
        if(screenSize.x < screenSize.y) // x is width, y is height
        {
            getMenuInflater().inflate(R.menu.main, menu);   // inflate the menu
            return true;
        }
        else
        {
            return false;
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent preferencesIntent = new Intent(this, SettingsActivity.class);
        startActivity(preferencesIntent);
        return super.onOptionsItemSelected(item);
    }

}
