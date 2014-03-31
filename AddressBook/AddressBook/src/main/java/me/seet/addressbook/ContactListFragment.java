// ContactListFragment.java
// Displays the list of contact names
package me.seet.addressbook;

import android.app.Activity;
import android.app.ListFragment;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.util.Objects;

public class ContactListFragment extends ListFragment {
    // callback methods implemented by MainActivity
    public interface ContactListFragmentListener {
        // called when user selects a contact
        void onContactSelected(long rowID);

        // called when user decides to add a contact
        void onAddContact();
    }

    private ContactListFragmentListener mListener;

    private ListView mContactListView;  // the ListActivity's ListView
    private CursorAdapter mContactAdapter;  // adapter for ListView
    // responds to the user touching a contact's name in the ListView
    private AdapterView.OnItemClickListener mViewContactListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            mListener.onContactSelected(id);    // pass selection to MainActivity
        }
    };

    // set ContactListFragmentListener when fragment attached
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (ContactListFragmentListener)activity;
    }

    // remove ContactListFragmentListener when Fragment detached
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    // called after View is created
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setRetainInstance(true);    // save fragment across config changes
        setHasOptionsMenu(true);    // this fragment has menu items to display

        // set text to display when there are no contacts
        setEmptyText(getResources().getString(R.string.no_contacts));

        // get ListView reference and configure ListView
        mContactListView = getListView();
        mContactListView.setOnItemClickListener(mViewContactListener);
        mContactListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        // map each contact's name to a TextView in the ListView layout
        String[] from = new String[] { "name" };
        int[] to = new int[] { android.R.id.text1 };
        mContactAdapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_1, null, from, to, 0);
        setListAdapter(mContactAdapter);    // set adapter that supplies data
    }

    // when fragment resumes, use a GetsContactsTask to load contacts
    @Override
    public void onResume() {
        super.onResume();
        new GetContactsTask().execute((Object[])null);
    }

    // when fragment stops, close Cursor and remove from contactAdapter
    @Override
    public void onStop() {
        Cursor cursor = mContactAdapter.getCursor();    // get current Cursor
        mContactAdapter.changeCursor(null);             // adapter now has no Cursor

        if(cursor != null)
            cursor.close(); // release the Cursor's resources

        super.onStop();
    }

    // display this fragment's menu items
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_contact_list_menu, menu);
    }

    // handle choice from options menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                mListener.onAddContact();
                return true;
        }
        return super.onOptionsItemSelected(item);   // call super's method
    }

    // update data set
    public void updateContactList() {
        new GetContactsTask().execute((Object[])null);
    }

    // performs database query outside GUI thread
    private class GetContactsTask extends AsyncTask<Object, Object, Cursor> {
        DatabaseConnector mDatabaseConnector = new DatabaseConnector(getActivity());

        // open database and return Cursor for all contacts
        @Override
        protected Cursor doInBackground(Object... objects) {
            mDatabaseConnector.open();
            return mDatabaseConnector.getAllContacts();
        }

        // use the Cursor returned from the doInBackground method

        @Override
        protected void onPostExecute(Cursor result) {
            mContactAdapter.changeCursor(result);   // set the adapter's Cursor
            mDatabaseConnector.close();
        }
    }
}
