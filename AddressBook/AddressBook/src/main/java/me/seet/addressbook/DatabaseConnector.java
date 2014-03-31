package me.seet.addressbook;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by darren on 3/31/14.
 */
public class DatabaseConnector {
    // database name
    private static final String DATABASE_NAME = "UserContacts";

    private SQLiteDatabase mDatabase;  // for interacting with the database
    private DatabaseOpenHelper mDatabaseOpenHelper; // creates the database

    // public constructor for DatabaseConnector
    public DatabaseConnector(Context context) {
        // create a new DatabaseOpenHelper
        mDatabaseOpenHelper = new DatabaseOpenHelper(context, DATABASE_NAME, null, 1);
    }

    // open the database connection
    public void open() throws SQLException {
        // create or open a database for reading/ writing
        mDatabase = mDatabaseOpenHelper.getWritableDatabase();
    }

    // close the database connection
    public void close() {
        if(mDatabase != null)
            mDatabase.close();  // close the database connection
    }

    // inserts a new contact in the database
    public long insertContact(String name, String phone, String email, String street, String city, String state, String zip) {
        ContentValues newContact = new ContentValues();
        newContact.put("name", name);
        newContact.put("phone", phone);
        newContact.put("email", email);
        newContact.put("street", street);
        newContact.put("city", city);
        newContact.put("state", state);
        newContact.put("zip", zip);

        open(); // open the database
        long rowID = mDatabase.insert("contacts", null, newContact);
        close(); // close the database
        return rowID;
    }

    // updates an existing contact in the database
    public void updateContact(Long id, String name, String phone, String email, String street, String city, String state, String zip) {
        ContentValues editContact = new ContentValues();
        editContact.put("name", name);
        editContact.put("phone", phone);
        editContact.put("email", email);
        editContact.put("street", street);
        editContact.put("city", city);
        editContact.put("state", state);
        editContact.put("zip", zip);

        open(); // open the database
        mDatabase.update("contacts", editContact, "_id=" + id, null);
        close(); // close the database
    }

    // return a Cursor with all contact names in the database
    public Cursor getAllContacts() {
        return mDatabase.query("contacts", new String[] { "_id", "name" }, null, null, null, null, "name");
    }

    // return a Cursor containing specified contact's information
    public Cursor getOneContact(long id) {
        return mDatabase.query("contacts", null, "_id=" + id, null, null, null, "name");
    }

    // delete the contact specified by the given String name
    public void deleteContact(long id) {
        open(); // open the database
        mDatabase.delete("contacts", "_id=" + id, null);
        close(); // close the database
    }

    private class DatabaseOpenHelper extends SQLiteOpenHelper {
        // constructor
        public DatabaseOpenHelper(Context context, String name, CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        // creates the contacts table when the database is created
        @Override
        public void onCreate(SQLiteDatabase db) {
            // query to create a new table named contacts
            String createQuery = "CREATE TABLE contacts"
                    + "(_id integer primary key autoincrement,"
                    + "name TEXT, phone TEXT, email TEXT"
                    + ", street TEXT, city TEXT, state TEXT, zip TEXT);";

            db.execSQL(createQuery);    // execute query to create the database
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {

        }
    }
}
