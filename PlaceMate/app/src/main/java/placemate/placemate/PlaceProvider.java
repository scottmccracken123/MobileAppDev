package placemate.placemate;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by jsnap on 13/05/2017.
 */

public class PlaceProvider extends ContentProvider{

    // The Java namespace for the Content Provider
    static final String PROVIDER_NAME = "placemate.placemate.PlaceProvider";
    static final String CP_NAME = "cpplace";

    // Assigned to a content provider so any application can access it
    // cpcontacts is the virtual directory in the provider
    static final String URL = "content://" + PROVIDER_NAME + "/" + CP_NAME;
    static final Uri CONTENT_URL = Uri.parse(URL);

    static final String id = "id";
    static final String name = "name";
    static final String venueId = "venueId";
    static final String addressOne = "addressOne";
    static final String addressTwo = "addressTwo";
    static final String city = "city";
    static final String postcode = "postcode";
    static final String phoneNumber = "phoneNumber";
    static final String longitude = "longitude";
    static final String latitude = "latitude";
    static final String rating = "rating";
    static final String placeType = "placeType";
    static final String website = "website";
    static final int uriCode = 1;

    //maps key value pairs
    private static HashMap<String, String> values;

    // Used to match uris with Content Providers
    static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, CP_NAME, uriCode);
    }

    private SQLiteDatabase sqlDB;
    static final String DATABASE_NAME = "Placemate";
    static final String TABLE_NAME = "Places";
    static final int DATABASE_VERSION = 1;
    static final String CREATE_DB_TABLE = " CREATE TABLE " + TABLE_NAME +
            " (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            " name TEXT NOT NULL," +
            " venueId TEXT NOT NULL," +
            " addressOne TEXT NOT NULL," +
            " addressTwo TEXT," +
            " city TEXT NOT NULL," +
            " postcode TEXT NOT NULL," +
            " phoneNumber TEXT NOT NULL," +
            " longitude TEXT NOT NULL," +
            " latitude TEXT NOT NULL," +
            " rating TEXT NOT NULL," +
            " placeType TEXT NOT NULL," +
            " website TEXT NOT NULL" +
            ");";

    @Override
    public boolean onCreate() {
        DatabaseHelper dbHelper = new DatabaseHelper(getContext());
        sqlDB = dbHelper.getWritableDatabase();
        if (sqlDB != null) {
            return true;
        }
        return false;
    }

    // Returns a cursor that provides read and write access to the results of the query
    // Uri : Links to the table in the provider (The From part of a query)
    // projection : an array of columns to retrieve with each row
    // selection : The where part of the query selection
    // selectionArgs : The argument part of the where (where id = 1)
    // sortOrder : The order by part of the query
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        // Used to create a SQL query
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        // Set table to query
        queryBuilder.setTables(TABLE_NAME);

        // Used to match uris with Content Providers
        switch (uriMatcher.match(uri)) {
            case uriCode:

                // A projection map maps from passed column names to database column names
                queryBuilder.setProjectionMap(values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // Cursor provides read and write access to the database
        Cursor cursor = queryBuilder.query(sqlDB, projection, selection, selectionArgs, null,
                null, sortOrder);

        // Register to watch for URI changes
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    // Handles requests for the MIME type (Type of Data) of the data at the URI
    @Override
    public String getType(Uri uri) {

        // Used to match uris with Content Providers
        switch (uriMatcher.match(uri)) {

            // vnd.android.cursor.dir/cpcontacts states that we expect multiple pieces of data
            case uriCode:
                return "vnd.android.cursor.dir/" + CP_NAME;

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    // Used to insert a new row into the provider
    // Receives the URI (Uniform Resource Identifier) for the Content Provider and a set of values
    @Override
    public Uri insert(Uri uri, ContentValues values) {

        // Gets the row id after inserting a map with the keys representing the the column
        // names and their values. The second attribute is used when you try to insert
        // an empty row
        long rowID = sqlDB.insert(TABLE_NAME, null, values);

        // Verify a row has been added
        if (rowID > 0) {

            // Append the given id to the path and return a Builder used to manipulate URI
            // references
            Uri _uri = ContentUris.withAppendedId(CONTENT_URL, rowID);

            // getContentResolver provides access to the content model
            // notifyChange notifies all observers that a row was updated
            getContext().getContentResolver().notifyChange(_uri, null);

            // Return the Builder used to manipulate the URI
            return _uri;
        }
        Toast.makeText(getContext(), "Row Insert Failed", Toast.LENGTH_LONG).show();
        return null;
    }

    // Deletes a row or a selection of rows
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int rowsDeleted = 0;

        // Used to match uris with Content Providers
        switch (uriMatcher.match(uri)) {
            case uriCode:
                rowsDeleted = sqlDB.delete(TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // getContentResolver provides access to the content model
        // notifyChange notifies all observers that a row was updated
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    // Used to update a row or a selection of rows
    // Returns to number of rows updated
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int rowsUpdated = 0;

        // Used to match uris with Content Providers
        switch (uriMatcher.match(uri)) {
            case uriCode:

                // Update the row or rows of data
                rowsUpdated = sqlDB.update(TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // getContentResolver provides access to the content model
        // notifyChange notifies all observers that a row was updated
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    // Creates and manages our database
    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqlDB) {
            sqlDB.execSQL(CREATE_DB_TABLE);
        }

        // Recreates the table when the database needs to be upgraded
        @Override
        public void onUpgrade(SQLiteDatabase sqlDB, int oldVersion, int newVersion) {
            sqlDB.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(sqlDB);
        }
    }

    /*public class DatabaseHelper extends SQLiteOpenHelper {

        private static final String TAG = "DatabaseHelper";

        //private static final String TABLE_NAME = "saved_places_table";
        private static final String COL0 = "ID";
        private static final String COL1 = "PlaceName";
        private static final String COL2 = "Address";
        private static final String COL3 = "PhoneNumber";
        private static final String COL4 = "Rating";


        public DatabaseHelper(Context context) {
            super(context, TABLE_NAME, null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String createTable = "CREATE TABLE " + TABLE_NAME + " (" + COL0 + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COL1 +" TEXT, " + COL2 + " TEXT)";

            db.execSQL(createTable);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP IF TABLE EXISTS " + TABLE_NAME);
            onCreate(db);
        }


        public boolean addData(ArrayList<String> items) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();

            contentValues.put(COL1, items.get(0).toString());
            Log.d(TAG, "addData: Adding " + items.get(0).toString() + " to " + TABLE_NAME);

            contentValues.put(COL2, items.get(1).toString());
            Log.d(TAG, "addData: Adding " + items.get(1).toString() + " to " + TABLE_NAME);

            //contentValues.put(COL3, items.get(2));
            //Log.d(TAG, "addData: Adding " + items.get(2) + " to " + TABLE_NAME);

            //contentValues.put(COL4, items.get(3));
            //Log.d(TAG, "addData: Adding " + items.get(3) + " to " + TABLE_NAME);

            long result = db.insert(TABLE_NAME, null, contentValues);

            //if data is inserted incorrectly result will return -1
            if(result == -1) {
                return false;
            } else {
                return true;
            }
        }

        public Cursor getData(){
            SQLiteDatabase db = this.getWritableDatabase();
            String query = "SELECT * FROM " + TABLE_NAME;
            Cursor data = db.rawQuery(query, null);
            return data;
        }
    }*/

}
