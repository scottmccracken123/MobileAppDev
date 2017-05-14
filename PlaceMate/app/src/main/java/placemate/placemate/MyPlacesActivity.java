package placemate.placemate;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.widget.TextView;

import org.w3c.dom.Text;


public class MyPlacesActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "MyPlacesActivity";
    private static final int LOADER_ID = 0x01;

    private ListView listView;
    private Button viewPlaceBtn;

    ContentResolver resolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_places);

        listView = (ListView) findViewById(R.id.savedPlacesList);
        /*viewPlaceBtn = (Button) findViewById(R.id.viewPlaceBtn);*/

        //initialise resolver
        resolver = getContentResolver();

        //initialising custom loader
        getSupportLoaderManager().initLoader(LOADER_ID, null, this);

        /*viewPlaceBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent changeToViewPlace = new Intent(getApplicationContext(), ViewPlaceActivity.class);
                startActivity(changeToViewPlace);
            }
        });*/

        /////////////////////////////////////////////////////////////////////

        NavigationView nv = (NavigationView) findViewById(R.id.nav_view);
        nv.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case (R.id.nav_my_places):
                        Intent changeToMyPlaces = new Intent(getApplicationContext(), MyPlacesActivity.class);
                        startActivity(changeToMyPlaces);
                    case (R.id.nav_map_view):
                        Intent changeToMap = new Intent(getApplicationContext(), MapViewActivity.class);
                        startActivity(changeToMap);

                }
                return true;
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        //pulling the data using custom loader from content provider
        return new CursorLoader(this, Uri.parse("content://placemate.placemate.PlaceProvider/cpplace"), null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {


        String[] fromColumns = new String[] {"_id", "name",};
        int[] toViews = {R.id.placeName, R.id.placeType};

        SimpleCursorAdapter myCursorAdapter;
        myCursorAdapter = new SimpleCursorAdapter(getBaseContext(),R.layout.list_row, data, fromColumns, toViews, 0);

        /*ArrayList<ArrayList<String>> lists = new ArrayList<ArrayList<String>>();
        while(data.moveToNext()){

            ArrayList<String> listData = new ArrayList<>();
            //add data pulled from db to list
            listData.add(data.getString(0));
            listData.add(data.getString(1));

            lists.add(listData);
        }*/

        //create list adapter and set this
        ListView listView2 = (ListView) findViewById(R.id.savedPlacesList);
        listView.setAdapter(myCursorAdapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }


    //makes icon bar actually work
    //public boolean onOptionsItemSelected(MenuItem item){
     //   if(mToggle.onOptionsItemSelected(item)){
     //       return true;
    //    }
    //    return super.onOptionsItemSelected(item);
    //}
}
