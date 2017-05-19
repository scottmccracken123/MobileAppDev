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
import android.widget.Toast;
import java.util.HashMap;

/**
 Class used to create content provider
 */

public class PlaceProvider extends ContentProvider{

    //  Java namespace for the Content Provider
    static final String PROVIDER_NAME = "placemate.placemate.PlaceProvider";
    static final String CP_NAME = "cpplace";
    // cpplace is the virtual directory in the provider
    static final String URL = "content://" + PROVIDER_NAME + "/" + CP_NAME;
    static final Uri CONTENT_URL = Uri.parse(URL);

    //database fields to be set externally
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
    static final String placeBestImg = "placeBestImg";
    static final String placeImg = "placeImg";
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
            " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            " name TEXT," +
            " venueId TEXT," +
            " addressOne TEXT," +
            " addressTwo TEXT," +
            " city TEXT," +
            " postcode TEXT," +
            " phoneNumber TEXT," +
            " longitude TEXT," +
            " latitude TEXT," +
            " rating TEXT," +
            " placeType TEXT," +
            " website TEXT," +
            " placeImg BLOB," +
            " placeBestImg BLOB" +
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

    //returns a cursor providing queries to the db
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        // create SQL query
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        // table to query
        queryBuilder.setTables(TABLE_NAME);

        // match URI with content provider
        switch (uriMatcher.match(uri)) {
            case uriCode:

                //  maps from passed column names to database column names
                queryBuilder.setProjectionMap(values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        //  provides read and write access to the database
        Cursor cursor = queryBuilder.query(sqlDB, projection, selection, selectionArgs, null,
                null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {

        // used to match uris with Content Providers
        switch (uriMatcher.match(uri)) {

            // states that multiple peices of data are expected
            case uriCode:
                return "vnd.android.cursor.dir/" + CP_NAME;

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    // insert new row using content provider
    @Override
    public Uri insert(Uri uri, ContentValues values) {

        // insert into db
        long rowID = sqlDB.insert(TABLE_NAME, null, values);

        // check row has been added
        if (rowID > 0) {

            // append the id to path and return builder
            Uri _uri = ContentUris.withAppendedId(CONTENT_URL, rowID);

            // notify watchers
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

                // update rows
                rowsUpdated = sqlDB.update(TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // notify change notifies all listeners that a row was updated
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    // create and manages our database
    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqlDB) {
            sqlDB.execSQL(CREATE_DB_TABLE);
        }

        // Recreates the table if needs to be upgraded
        @Override
        public void onUpgrade(SQLiteDatabase sqlDB, int oldVersion, int newVersion) {
            sqlDB.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(sqlDB);
        }
    }


}
