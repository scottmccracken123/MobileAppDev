package placemate.placemate;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.view.MenuItem;
import java.util.ArrayList;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import android.widget.ListView;
import android.widget.Toast;

public class MyPlacesActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {


    //intialise variables for custom loaders
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

        //navigation variables - display burger icon top right
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //handle notifications, setup after N amount of time
        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setNotification("Want to view your favourite places?", "Click on me to find them!");
            }
        }, 10000);

        //setNotification("It's almost dinner time", "Click on me to find somewhere to eat!");
        //add toggle option to layout

        //navigation view
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
                    case(R.id.nav_user_guide):
                        Intent changeMap = new Intent(getApplicationContext(), UserGuideActivity.class);
                        startActivity(changeMap);
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

    //signout method for menu
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



    //setup notification to be ran afer set amount of time
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
        String[] columns = {"_id", "name", "placeType", "placeBestImg"};
        return new CursorLoader(this, Uri.parse("content://placemate.placemate.PlaceProvider/cpplace"), columns, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        //lists will store data from DB
        ArrayList<byte[]> imgs = new ArrayList<byte[]>();
        final ArrayList<String> names = new ArrayList<String>();
        final ArrayList<String> types = new ArrayList<String>();

        //loop through DB, adding data. - fields are nullable
        if (data.moveToFirst()) {
            do {
                String currentName = data.getString(1);
                String currentType = data.getString(2);
                byte[] currentImg = data.getBlob(3);

                //add to arrays
                imgs.add(currentImg);
                types.add(currentType);
                names.add(currentName);
            } while (data.moveToNext());
        }


        //custom imagecursoradapter
        ImageCursorAdapter adapter = new ImageCursorAdapter(this, names, types, imgs);
        ListView list = (ListView) findViewById(R.id.savedPlacesList);
        list.setAdapter(adapter);

        //if list item is clicked on, go to view saved place
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getBaseContext(), ViewSavedPlaceActivity.class);
                intent.putExtra("PLACE_ID", String.valueOf(id));
                startActivity(intent);
            }
        });
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    //makes toast with customisable message
    private void toastMessage(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    };

}
