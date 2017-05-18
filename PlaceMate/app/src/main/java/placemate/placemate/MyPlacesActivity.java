package placemate.placemate;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import android.view.MenuItem;

import java.util.List;

import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.vision.text.Text;

import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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


        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setNotification("It's almost dinner time", "Click on me to find somewhere to eat!");
            }
        }, 10000);

        //setNotification("It's almost dinner time", "Click on me to find somewhere to eat!");
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

        //initialise resolver
        resolver = getContentResolver();
        //initialising custom loader
        getSupportLoaderManager().initLoader(LOADER_ID, null, this);

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

    public void setNotification(String title, String content) {

        NotificationCompat.Builder notBuilder = new NotificationCompat.Builder(this);
        notBuilder.setContentTitle(title);
        notBuilder.setContentText(content);
        notBuilder.setSmallIcon(android.R.drawable.ic_menu_compass);
        notBuilder.setLights(Color.MAGENTA, 2000, 4000);
        notBuilder.setAutoCancel(true);

        // Remove if we're not notifying them of saved places.

        Intent moveThemFromNot = new Intent(this, MapViewActivity.class);
        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);
        taskStackBuilder.addParentStack(MapViewActivity.class);
        taskStackBuilder.addNextIntent(moveThemFromNot);
        PendingIntent movingThePendingIntent = taskStackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        notBuilder.setContentIntent(movingThePendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notBuilder.build());

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //pulling the data using custom loader from content provider
        return new CursorLoader(this, Uri.parse("content://placemate.placemate.PlaceProvider/cpplace"), null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if(data.getCount() == 0) {
            TextView empty = (TextView) findViewById(R.id.place_empty);
            empty.setText("You currently have no saved places.");
        } else {
            String[] fromColumns = new String[] {"name", "placeType", "_id"};
            int[] toViews = {R.id.placeName, R.id.placeType};



            SimpleCursorAdapter myCursorAdapter;
            myCursorAdapter = new SimpleCursorAdapter(getBaseContext(),R.layout.list_row, data, fromColumns, toViews, 0);

            /*while(data.moveToNext())
            {
                if(data.getBlob(0) != null){
                    Log.d("BLOB TEST", "worked?");
                } else {
                    Log.d("BLOB NULL", "not worked");
                }

            }*/


            //images upload?
            /*SimpleCursorAdapter.ViewBinder viewBinder = new SimpleCursorAdapter.ViewBinder() {

                public boolean setViewValue(View view, Cursor cursor,
                                            int columnIndex) {
                    ImageView image = (ImageView) view;
                    byte[] byteArr = cursor.getBlob(columnIndex);
                    image.setImageBitmap(BitmapFactory.decodeByteArray(byteArr, 0, byteArr.length));
                    return true;
                }
            };
            ImageView image = (ImageView) findViewById(R.id.list_image);
            viewBinder.setViewValue(image, data, data.getColumnIndex("placeImg"));
            myCursorAdapter.setViewBinder(viewBinder);*/

            //create list adapter and set this
            ListView listView = (ListView) findViewById(R.id.savedPlacesList);
            listView.setAdapter(myCursorAdapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(getBaseContext(), ViewSavedPlaceActivity.class);
                    intent.putExtra("PLACE_ID", String.valueOf(id));
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    //makes toast with customisable message
    private void toastMessage(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    };

}
