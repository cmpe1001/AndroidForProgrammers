// MainActivity.java
// Manages your favourite Twitter searches for easy
// access and display in the device's web browser
package me.seet.twittersearches;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.view.View.OnClickListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.TextView;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;


public class MainActivity extends ListActivity {

    // name of SharedPreferences XML file that stores the saved searches
    private static final String SEARCHES = "searches";
    
    private EditText mQueryEditText;             // EditText where user enters a query
    private EditText mTagEditText;               // EditText where user tags a query
    private SharedPreferences mSavedSearches;    // user's favorite searches
    private ArrayList<String> mTags;             // list of tags for saved searches
    private ArrayAdapter<String> mAdapter;       // binds tags to ListView

    // saveButtonListener saves a tag-query pair into SharedPreferences
    private OnClickListener saveButtonListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            // create tag if neither queryEditText nor tagEditText is empty
            if(mQueryEditText.getText().length() > 0 && mTagEditText.getText().length() > 0) {
                addTaggedSearch(mQueryEditText.getText().toString(), mTagEditText.getText().toString());
                mQueryEditText.setText(""); // clear queryEditText
                mTagEditText.setText(""); // clear tagEditText

                ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromInputMethod(mTagEditText.getWindowToken(), 0);
            }
            else // display message asking user to provide a query and a tag
            {
                // create a new AlertDialog Builder
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(MainActivity.this);

                // set dialog's title and message to display
                builder.setMessage(R.string.missingMessage);

                // provide an OK button that simply dismisses the dialog
                builder.setPositiveButton(R.string.OK, null);

                // create AlertDialog from the AlertDialog.Builder
                AlertDialog errorDialog = builder.create();
                errorDialog.show(); // display the modal dialog
            }
        }
    };

    // itemClickListener launches web browser to display search results
    private OnItemClickListener itemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // get query string and create a URL representing the search
            String tag = ((TextView)view).getText().toString();
            String urlString = getString(R.string.searchURL) + Uri.encode(mSavedSearches.getString(tag, ""), "UTF-8");

            // create an Intent to launch a web browser
            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlString));

            startActivity(webIntent);   // launches web browser to view results
        }
    };


    private OnItemLongClickListener itemLongClickListener = new OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            // get the tag that the user long touched
            final String tag = ((TextView)view).getText().toString();

            // create a new AlertDialog
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

            // set the AlertDialog's title
            builder.setTitle(getString(R.string.sharedEditDeleteTitle, tag));

            // set list of items to display in dialog
            builder.setItems(R.array.dialog_items, new DialogInterface.OnClickListener() {
                // responds to user touch by sharing, editing or deleting a saved search
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    switch (which) {
                        case 0: // share
                            shareSearch(tag);
                            break;
                        case 1: // edit
                            // set EditTexts to match chosen tag and query
                            mTagEditText.setText(tag);
                            mQueryEditText.setText(mSavedSearches.getString(tag, ""));
                            break;
                        case 2: // delete
                            deleteSearch(tag);
                            break;
                    }
                }
            });

            builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                // called when the "Cancel" button is clicked
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });

            builder.create().show();

            return true;
        }
    };

    // deletes a search after the user confirms the delete operation
    private void deleteSearch(final String tag) {
        // create a new AlertDialog
        AlertDialog.Builder confirmBuilder = new AlertDialog.Builder(this);

        // set the AlertDialog's message
        confirmBuilder.setMessage(getString(R.string.confirmMessage, tag));

        // set the AlertDialog's negative Button
        confirmBuilder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            // called when "Cancel" Button is clicked
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        // set the AlertDialog's positive Button
        confirmBuilder.setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                mTags.remove(tag); // remove tag from tags

                // get SharedPreferences.Editor to remove saved search
                SharedPreferences.Editor preferencesEditor =
                        mSavedSearches.edit();
                preferencesEditor.remove(tag);  // remove search
                preferencesEditor.apply();      // saves the changes

                // rebind tags ArrayList to ListView to show updated list
                mAdapter.notifyDataSetChanged();
            }
        });

        confirmBuilder.create().show(); // display AlertDialog
    }

    // allows user to choose an app for sharing a saved search's URL
    private void shareSearch(String tag) {
        // create the URL representing the search
        String urlString = getString(R.string.searchURL) + Uri.encode(mSavedSearches.getString(tag,""), "UTF-8");
        // create Intent to share urlString
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.shareSubject));
        shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.shareMessage, urlString));
        shareIntent.setType("text/plain");

        // display apps that can share text
        startActivity(Intent.createChooser(shareIntent, getString(R.string.shareSearch)));
    }


    // add new search to the save file, then refresh all buttons
    private void addTaggedSearch(String query, String tag) {
        // get a SharedPreferences.Editor to store new tag/ query pair
        SharedPreferences.Editor preferencesEditor = mSavedSearches.edit();
        preferencesEditor.putString(tag, query);
        preferencesEditor.apply();

        // if tag is new, add to and sort tags, then display updated list
        if(!mTags.contains(tag)) {
            mTags.add(tag); // add new tag
            Collections.sort(mTags, String.CASE_INSENSITIVE_ORDER);
            mAdapter.notifyDataSetChanged();    // rebind tags to ListView
        }
    }


    // called when MainActivity is first created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get references to the EditTexts
        mQueryEditText = (EditText)findViewById(R.id.queryEditText);
        mTagEditText = (EditText)findViewById(R.id.tagEditText);
        
        // get the SharedPreferences containing the user's saved searches
        mSavedSearches = getSharedPreferences(SEARCHES, MODE_PRIVATE);
        
        // store the saved tags in an ArrayList then sort them
        mTags = new ArrayList<String>(mSavedSearches.getAll().keySet());
        Collections.sort(mTags, String.CASE_INSENSITIVE_ORDER);
        
        // create ArrayAdapter and use it to bind tags to the ListView
        mAdapter = new ArrayAdapter<String>(this, R.layout.list_item, mTags);
        setListAdapter(mAdapter);
        
        // register listener to save a new or edited search
        ImageButton saveButton = (ImageButton)findViewById(R.id.saveButton);
        saveButton.setOnClickListener(saveButtonListener);

        // register listener that searches Twitter when user touches a tag
        getListView().setOnItemClickListener(itemClickListener);

        // set listener that allows user to delete or edit a search
        getListView().setOnItemLongClickListener(itemLongClickListener);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
