// DetailsFragment.java
// Displays one contact's details
package me.seet.addressbook;

import android.app.*;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.*;
import android.widget.TextView;

import java.util.Objects;

public class DetailsFragment extends Fragment {
    private DetailsFragmentListener mListener;
    private long mRowID = -1;    // selected contact's rowID
    private TextView mNameTextView; // displays contact's name
    private TextView mPhoneTextView; // displays contact's phone
    private TextView mEmailTextView; // displays contact's email
    private TextView mStreetTextView; // displays contact's street
    private TextView mCityTextView; // displays contact's city
    private TextView mStateTextView; // displays contact's state
    private TextView mZipTextView; // displays contact's zip
    // DialogFragment to confirm deletion of contact
    private DialogFragment mConfirmDelete = new DialogFragment() {
        // create an AlertDialog and return it
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // create an AlertDialog and return it
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.confirm_title);
            builder.setMessage(R.string.confirm_message);

            // provide an OK button that simply dismisses the dialog
            builder.setPositiveButton(R.string.button_delete,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            final DatabaseConnector databaseConnector = new DatabaseConnector(getActivity());
                            // AsyncTask delete contact and notifies listener
                            AsyncTask<Long, Object, Object> deleteTask =
                                    new AsyncTask<Long, Object, Object>() {
                                        @Override
                                        protected Object doInBackground(Long... params) {
                                            databaseConnector.deleteContact(params[0]);
                                            return null;
                                        }

                                        @Override
                                        protected void onPostExecute(Object o) {
                                            mListener.onContactDeleted();
                                        }
                                    };
                            deleteTask.execute(new Long[] { mRowID });
                        }
                    });
            builder.setNegativeButton(R.string.button_cancel, null);
            return builder.create();
        }
    };

    public DetailsFragment() {
        // Required empty public constructor
    }

    // called when DetailsFragmentListener's view needs to be created
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setRetainInstance(true);    // save fragment across config changes
        // if Details Fragment is being restored, get saved row ID
        if(savedInstanceState != null)
            mRowID = savedInstanceState.getLong(MainActivity.ROW_ID);
        else
        {
            // get Bundle of arguments then extract the contact's row ID
            Bundle arguments = getArguments();

            if(arguments != null)
                mRowID = arguments.getLong(MainActivity.ROW_ID);
        }

        // inflate DetailsFragment's layout
        View view =  inflater.inflate(R.layout.fragment_details, container, false);
        setHasOptionsMenu(true);

        // get the EditTexts
        mNameTextView = (TextView)view.findViewById(R.id.nameTextView );
        mPhoneTextView = (TextView)view.findViewById(R.id.phoneTextView );
        mEmailTextView = (TextView)view.findViewById(R.id.emailTextView );
        mStreetTextView = (TextView)view.findViewById(R.id.streetTextView );
        mCityTextView = (TextView)view.findViewById(R.id.cityTextView);
        mStateTextView = (TextView)view.findViewById(R.id.stateTextView);
        mZipTextView = (TextView)view.findViewById(R.id.zipTextView);
        return  view;
    }

    // called when the DetailsFragment resumes
    @Override
    public void onResume() {
        super.onResume();
        new LoadContactTask().execute(mRowID);  // load contact at rowID
    }

    // save currently displayed contact's row ID
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(MainActivity.ROW_ID, mRowID);
    }

    // display this fragment's menu items
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_details_menu, menu);
    }

    // handle menu item selections
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                // create Bundle containing contact data to edit
                Bundle arguments = new Bundle();
                arguments.putLong(MainActivity.ROW_ID, mRowID);;
                arguments.putCharSequence("name", mNameTextView.getText());
                arguments.putCharSequence("phone", mPhoneTextView.getText());
                arguments.putCharSequence("email", mEmailTextView.getText());
                arguments.putCharSequence("street", mStreetTextView.getText());
                arguments.putCharSequence("city", mCityTextView.getText());
                arguments.putCharSequence("state", mStateTextView.getText());
                arguments.putCharSequence("zip", mZipTextView.getText());
                mListener.onEditContact(arguments); // pass Bundle to listener
                return  true;
            case R.id.action_delete:
                deleteContact();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // delete a contact
    private void deleteContact() {
        // use FragmentManager to display the confirmDelete DialogFragment
        mConfirmDelete.show(getFragmentManager(), "confirm delete");
    }

    // set DetailsFragmentListener when fragment attached
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (DetailsFragmentListener)activity;
    }

    // remove DetailsFragmentListener when fragment detached
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    // callback methods implemented by MainActivity
    public interface DetailsFragmentListener {
        // called when a contact is deleted
        void onContactDeleted();
        // called to pass Bundle of contact's info for editing
        void onEditContact(Bundle arguments);
    }

    // performs data query outside GUI thread
    private class LoadContactTask extends AsyncTask<Long, Object, Cursor> {
        DatabaseConnector mDatabaseConnector = new DatabaseConnector(getActivity());

        // open database & get Cursor representing specified contact's data
        @Override
        protected Cursor doInBackground(Long... params) {
            mDatabaseConnector.open();;
            return mDatabaseConnector.getOneContact(params[0]);
        }

        // use the Cursor returned from the doInBackground method
        @Override
        protected void onPostExecute(Cursor result) {
            super.onPostExecute(result);
            result.moveToFirst();

            // get the column index for each data item
            int nameIndex = result.getColumnIndex("name");
            int phoneIndex = result.getColumnIndex("phone");
            int emailIndex = result.getColumnIndex("email");
            int streetIndex = result.getColumnIndex("street");
            int cityIndex = result.getColumnIndex("city");
            int stateIndex = result.getColumnIndex("state");
            int zipIndex = result.getColumnIndex("zip");

            // fill TextViews with the retrieved data
            mNameTextView.setText(result.getString(nameIndex));
            mPhoneTextView.setText(result.getString(phoneIndex));
            mEmailTextView.setText(result.getString(emailIndex));
            mStreetTextView.setText(result.getString(streetIndex));
            mCityTextView.setText(result.getString(cityIndex));
            mStateTextView.setText(result.getString(stateIndex));
            mZipTextView.setText(result.getString(zipIndex));

            result.close(); // close the result cursor
            mDatabaseConnector.close(); // close database connection
        }
    }
}
