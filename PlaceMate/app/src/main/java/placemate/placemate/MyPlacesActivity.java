package placemate.placemate;


import android.content.Intent;
import android.content.pm.ResolveInfo;


import android.content.ContentResolver;
import android.database.Cursor;

import android.net.Uri;
import android.support.design.widget.NavigationView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import java.util.List;
import android.view.View;
import android.widget.Button;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import java.util.ArrayList;


public class MyPlacesActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {



    private static final String TAG = "MyPlacesActivity";
    private static final int LOADER_ID = 0x01;
    private ListView listView;
    private Button viewPlaceBtn;
    ContentResolver resolver;

    //navigation layout variables
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;

    GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_places);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        //navigation switch for drawer menu
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        //add toggle option to layout


        NavigationView nv = (NavigationView) findViewById(R.id.nav_view);
        nv.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case (R.id.nav_my_places):
                        Intent changeToMyPlaces = new Intent(getApplicationContext(), MyPlacesActivity.class);
                        startActivity(changeToMyPlaces);
                        break;
                    case (R.id.nav_map_view):
                        Intent changeToMap = new Intent(getApplicationContext(), MapViewActivity.class);
                        startActivity(changeToMap);
                        break;
                    case (R.id.nav_logout):
                        signOut();
                        break;
                }
                return true;
            }
        });


        listView = (ListView) findViewById(R.id.savedPlacesList);
        viewPlaceBtn = (Button) findViewById(R.id.viewPlaceBtn);


        //initialise resolver
        resolver = getContentResolver();

        //initialising custom loader
        getSupportLoaderManager().initLoader(LOADER_ID, null, this);

        //view place button - to be removed
        viewPlaceBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent changeToViewPlace = new Intent(getApplicationContext(), ViewPlaceActivity.class);
                startActivity(changeToViewPlace);
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mToggle.onOptionsItemSelected(item)){
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        Intent loginPage = new Intent(MyPlacesActivity.this, LoginActivity.class);
                        startActivity(loginPage);

                    }
                }
        );
    }

    private void shareLink(String urlToShare) {

        Intent intent = new Intent(Intent.ACTION_SEND); // change action  
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, urlToShare);

        boolean facebookAppFound = false;

        List<ResolveInfo> matches = getPackageManager().queryIntentActivities(intent, 0);
            for (ResolveInfo info:matches) {
                if (info.activityInfo.packageName.toLowerCase().startsWith("com.facebook.katana")) {
                    intent.setPackage(info.activityInfo.packageName);
                    facebookAppFound = true;
                    break;
                }
            }
        if (!facebookAppFound) {
            String shareUrl = "https://www.facebook.com/sharer/sharer.php?u=" + urlToShare;
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(shareUrl));
        }
        startActivity(intent);


    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {


        String[] anything = {"placeImg"};


        //pulling the data using custom loader from content provider
        return new CursorLoader(this, Uri.parse("content://placemate.placemate.PlaceProvider/cpplace"), anything, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {


        while(data.moveToNext()){
            //add data pulled from db to list

            if(data.getBlob(0) != null){
                Log.d("BLOB TEST", "worked?");
            } else {
                Log.d("BLOB NULL", "not worked");
            }

        }


    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

}
