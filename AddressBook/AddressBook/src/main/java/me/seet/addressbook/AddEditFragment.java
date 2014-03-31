// AddEditFragment.java
// Allows user to add a new contact or edit an existing one
package me.seet.addressbook;

import android.app.*;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

public class AddEditFragment extends Fragment {
    private AddEditFragmentListener mListener;

    private long mRowID;    // database row ID of the contact
    private Bundle mContactInfoBundle;  // arguments for editing a contact

    // EditTexts for contact information
    private EditText mNameEditText;
    private EditText mPhoneEditText;
    private EditText mEmailEditText;
    private EditText mStreetEditText;
    private EditText mCityEditText;
    private EditText mStateEditText;
    private EditText mZipEditText;
    // responds to event generated when user saves a contact
    private View.OnClickListener mSaveContactButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(mNameEditText.getText().toString().trim().length() != 0) {
                // AsyncTask to save contact, then notify listener
                AsyncTask<Object, Object, Object>saveContactTask = new AsyncTask<Object, Object, Object>() {
                    @Override
                    protected Object doInBackground(Object... objects) {
                        saveContact();  // save contact to the database
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Object result) {
                        // hide soft keyboard
                        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
                        mListener.onAddEditCompleted(mRowID);
                    }
                };

                // save the contact to the database using a separate thread
                saveContactTask.execute((Object[])null);
            }
            else // required contact name is blank, so display error dialog
            {
                DialogFragment errorSaving = new DialogFragment() {
                    @Override
                    public Dialog onCreateDialog(Bundle savedInstanceState) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage(R.string.error_message);
                        builder.setPositiveButton(R.string.ok, null);

                        return builder.create();
                    }
                };

                errorSaving.show(getFragmentManager(), "error saving contact");
            }
        }
    };

    // saves contact information to the database
    private void saveContact() {
        // get DatabaseConnector to interact with the SQLite database
        DatabaseConnector databaseConnector = new DatabaseConnector(getActivity());
        if(mContactInfoBundle == null) {
            // insert the contact information into the database
            mRowID = databaseConnector.insertContact(
                    mNameEditText.getText().toString(),
                    mPhoneEditText.getText().toString(),
                    mEmailEditText.getText().toString(),
                    mStreetEditText.getText().toString(),
                    mCityEditText.getText().toString(),
                    mStateEditText.getText().toString(),
                    mZipEditText.getText().toString()
            );
        } else {
            databaseConnector.updateContact(mRowID,
                mNameEditText.getText().toString(),
                mPhoneEditText.getText().toString(),
                mEmailEditText.getText().toString(),
                mStreetEditText.getText().toString(),
                mCityEditText.getText().toString(),
                mStateEditText.getText().toString(),
                mZipEditText.getText().toString()
            );
        }
    }

    public AddEditFragment() {
        // Required empty public constructor
    }

    // called when Fragment's view needs to be created
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setRetainInstance(true);    // save fragment across config changes
        setHasOptionsMenu(true);    // fragment has menu items to display

        // inflate GUI and get references to EditTexts
        View view = inflater.inflate(R.layout.fragment_add_edit, container, false);
        mNameEditText = (EditText)view.findViewById(R.id.nameEditText);
        mPhoneEditText = (EditText)view.findViewById(R.id.phoneEditText);
        mEmailEditText = (EditText)view.findViewById(R.id.emailEditText);
        mStreetEditText = (EditText)view.findViewById(R.id.streetEditText);
        mCityEditText = (EditText)view.findViewById(R.id.cityEditText);
        mStateEditText = (EditText)view.findViewById(R.id.stateEditText);
        mZipEditText = (EditText)view.findViewById(R.id.zipEditText);

        mContactInfoBundle = getArguments();    // null if creating new contact

        if(mContactInfoBundle != null)
        {
            mRowID = mContactInfoBundle.getLong(MainActivity.ROW_ID);
            mNameEditText.setText(mContactInfoBundle.getString("name"));
            mPhoneEditText.setText(mContactInfoBundle.getString("phone"));
            mEmailEditText.setText(mContactInfoBundle.getString("email"));
            mStreetEditText.setText(mContactInfoBundle.getString("street"));
            mCityEditText.setText(mContactInfoBundle.getString("city"));
            mStateEditText.setText(mContactInfoBundle.getString("state"));
            mZipEditText.setText(mContactInfoBundle.getString("zip"));
        }

        // set Save Contact Button's event listener
        Button saveContactButton = (Button)view.findViewById(R.id.saveContactButton);
        saveContactButton.setOnClickListener(mSaveContactButtonClicked);
        return view;
    }

    // set AddEditFragmentListener when Fragment attached
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (AddEditFragmentListener)activity;
    }

    // remove AddEditFragmentListener when Fragment detached
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    // callback method implemented by MainActivity
    public interface AddEditFragmentListener {
        // called after edit completed so contact can be redisplayed
        void onAddEditCompleted(long rowID);
    }
}
