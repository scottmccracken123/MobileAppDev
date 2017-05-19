package placemate.placemate;

/*
For viewing a saved place
Pulls data from the database
 */

import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import java.util.List;

public class ViewSavedPlaceActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    //variables needed throughout
    private static final String TAG = "SavedPlaceActivity";
    private static final int LOADER_ID = 0x01;
    public String placeId, placeName, placeType, placeRating, addressOne, addressTwo, city, postcode,phoneNumber, website, longitude, latitude, venueId;
    private byte[] bestImg;
    public String[] columns;
    private final int permissionNumber = 10;
    private double cLongitude;
    private double cLatitude;
    ContentResolver resolver;
    Button mapButton;
    ImageView deleteButton, shareButton;

    //navigation layout variables
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private LocationManager locationManager;

    //google sign in variables
    GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //sets layout of activity
        setContentView(R.layout.activity_view_saved_place);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        //navigation switch for drawer menu
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();

        //sets burger icon
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //navigation menu
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

        //selected ID passed through via intent
        placeId = getIntent().getStringExtra("PLACE_ID");
        //initialise resolver
        resolver = getContentResolver();
        //initialising custom loader
        getSupportLoaderManager().initLoader(LOADER_ID, null, this);

        //initialise map button
        //used for map directions
        mapButton = (Button) findViewById(R.id.place_get_directions);
        mapButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){
                directions(v);
            }
        });

        //initialises the delete image
        //seen as a heart, used to delete saved place from db
        deleteButton = (ImageView) findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){
                deletePlace();
            }
        });

        //initialises facebook image
        //used to share place to facebook
        shareButton = (ImageView) findViewById(R.id.facebook_button);
        shareButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){
                shareLink(website);
            }
        });

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        //columns that are pull from the db
        String[] columns = new String[]{"name", "placeType", "rating", "addressOne", "addressTwo", "city", "postcode", "phoneNumber", "website", "longitude", "latitude", "placeBestImg", "venueId"};

        //pulling the data using custom loader from content provider
        //if id = 1 query should say
        //SELECT name, placeType FROM PLACES WHERE (_ID = 1)
        return new CursorLoader(this, Uri.parse("content://placemate.placemate.PlaceProvider/cpplace"), columns, "_id = " + placeId, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //data for the selected place has been pulled from db
        //assign data to variables creating previously
        while(data.moveToNext()) {

            placeName = data.getString(0);
            placeType = data.getString(1);
            placeRating = data.getString(2);
            addressOne = data.getString(3);
            addressTwo = data.getString(4);
            city = data.getString(5);
            postcode = data.getString(6);
            phoneNumber = data.getString(7);
            website = data.getString(8);
            longitude = data.getString(9);
            latitude = data.getString(10);
            bestImg = data.getBlob(11);
            venueId = data.getString(12);
        }

        //set place name in xml
        TextView name = (TextView) findViewById(R.id.place_name);
        if(placeName != null  && !placeName.isEmpty()) {
            name.setText(placeName);
        } else {
            name.setVisibility(View.GONE);
        }

        //set place type in xml
        TextView type = (TextView) findViewById(R.id.place_type);
        if(placeType != null && !placeType.isEmpty()) {
            type.setText(placeType);
        } else {
            type.setVisibility(View.GONE);
        }

        //set rating bar in xml
        RatingBar rating = (RatingBar) findViewById(R.id.place_rating);
        if(placeRating != null && !placeRating.isEmpty()) {
            Float newRating = Float.parseFloat(placeRating);
            rating.setRating(newRating/2);
        } else {
            rating.setVisibility(View.GONE);
        }

        //set address 1 in xml
        TextView add1 = (TextView) findViewById(R.id.place_address_1);
        if(addressOne != null && !addressOne.isEmpty()) {
            add1.setText(addressOne);
        } else {
            add1.setVisibility(View.GONE);
        }

        //set address 2 in xml
        TextView add2 = (TextView) findViewById(R.id.place_address_2);
        if(addressTwo != null && !addressTwo.isEmpty()) {
            add2.setText(addressTwo);
        } else {
            add2.setVisibility(View.GONE);
        }

        //set city in xml
        TextView addCity = (TextView) findViewById(R.id.place_address_city);
        if(city != null && !city.isEmpty()) {
            addCity.setText(city);
        } else {
            addCity.setVisibility(View.GONE);
        }

        //set postcode in xml
        TextView addPost = (TextView) findViewById(R.id.place_address_postcode);
        if(postcode != null && !postcode.isEmpty()) {
            addPost.setText(postcode);
        } else {
            addPost.setVisibility(View.GONE);
        }

        //set phone number in xml
        TextView addTel = (TextView) findViewById(R.id.place_address_telephone);
        if(phoneNumber != null && !phoneNumber.isEmpty()) {
            addTel.setText(phoneNumber);
        } else {
            addTel.setVisibility(View.GONE);
        }

        //set website url in xml
        TextView placeWeb = (TextView) findViewById(R.id.place_website);
        if(website != null && !website.isEmpty()) {
            placeWeb.setText(website);
        } else {
            placeWeb.setVisibility(View.GONE);
        }

        //set image in xml
        ImageView imageView = (ImageView) findViewById(R.id.place_image);
        if(bestImg != null){

            Bitmap imgBitMap = BitmapFactory.decodeByteArray(bestImg, 0, bestImg.length);
            imageView.setImageBitmap(imgBitMap);

        } else {
            imageView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    //ensure navigation drawer works on burger icon
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mToggle.onOptionsItemSelected(item)){
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //function to delete place
    public void deletePlace() {
        //delete place and return number of deleted rows
        int deletedRows = getContentResolver().delete(PlaceProvider.CONTENT_URL, "_id = " + placeId, null);

        //if deletion is successful
        //rows should be greater than 0
        //show toast and take user to view unsaved place (heart changes state)
        if(deletedRows > 0){
            toastMessage("Place Unsaved");
            Intent toViewPlace = new Intent(getApplicationContext(), ViewPlaceActivity.class);
            toViewPlace.putExtra("venueID", venueId);
            startActivity(toViewPlace);
        } else {
            toastMessage("Something went wrong, please try again.");
        }

    }

    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        Intent loginPage = new Intent(ViewSavedPlaceActivity.this, LoginActivity.class);
                        startActivity(loginPage);

                    }
                }
        );
    }

    //function to enable google maps directions
    public void directions(View view){
        try {
            //see if location permission is enabled
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.INTERNET}
                            ,permissionNumber);
                }
                return;
            }
            //get location longitude and latitude
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            cLongitude = location.getLongitude();
            cLatitude = location.getLatitude();

            //start intent to go to google maps
            //pass in current long and lat (saddr)
            //passin place long and lat (daddr)
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("http://maps.google.com/maps?saddr="+cLatitude+","+cLongitude+"&daddr="+longitude+","+latitude));
            startActivity(intent);

        }catch(NullPointerException exception) {
            Toast.makeText(this, "The direction feature requires your devices location to be enabled.", Toast.LENGTH_LONG).show();
        }
    }

    //function to share to facebook
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

    //makes toast with customisable message
    private void toastMessage(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    };


}
