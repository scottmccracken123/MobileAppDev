package placemate.placemate;


import android.Manifest;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


import static android.provider.UserDictionary.Words.APP_ID;
import static com.loopj.android.http.AsyncHttpClient.LOG_TAG;
import static java.lang.Double.parseDouble;

public class MapViewActivity extends AppCompatActivity implements OnMapReadyCallback, OnMarkerClickListener {
    private Button getLocationBtn;
    private TextView locationTxtView;
    private LocationManager locationManager;
    private LocationListener listener;
    private final int permissionNumber = 10;
    private double longitude;
    private double latitude;
    private JSONObject placeDetails;
    private String clientId;
    private String clientSecret;
    private Button getApiBtn;
    private String result;
    private String BASE_URL;
    private GoogleMap map;
    //nav variable intialisation
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private IntentFilter mIntentFilter;

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);

        //navigation switch for drawer menu

        //navigation switch for drawer menu


        // Adams stuff

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // get variables form layout and strings
//        locationTxtView = (TextView) findViewById(R.id.longitudeTxt);
//        getLocationBtn = (Button) findViewById(R.id.getLocationBtn);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        clientSecret = getResources().getString(R.string.client_secret);
        clientId = getResources().getString(R.string.client_id);

        listener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);
            }
        };

        getLocation();
        //navigation switch for drawer menu

        //navigation switch for drawer menu

        // get variables form layout and strings
//        locationTxtView = (TextView) findViewById(R.id.longitudeTxt);
//        getLocationBtn = (Button) findViewById(R.id.getLocationBtn);


        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


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
                        //code for actually logging out needs to be implemented
                        Intent changeToLogout = new Intent(getApplicationContext(), LoginActivity.class);
                        break;
                }
                return true;
            }
        });


    }

    @Override
    protected void onResume() {
        registerReceiver(broadcastReceiver, mIntentFilter);
        super.onResume();
    }

    @Override
    protected void onPause(){
        unregisterReceiver(broadcastReceiver);
        super.onPause();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        googleMap.setOnMarkerClickListener(this);
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        try {
            int venueArrayId = (int) marker.getTag();
            Intent toViewPlace = new Intent(this, ViewPlaceActivity.class);
            toViewPlace.putExtra("venueID", placeDetails.getJSONObject("response").getJSONArray("venues").getJSONObject(venueArrayId).getString("id"));
            startActivity(toViewPlace);
        } catch(JSONException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case permissionNumber:
                break;
            default:
                break;
        }
    }


    void getLocation(){
        // first check for permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.INTERNET}
                        ,permissionNumber);
            }
            return;
        }
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        longitude = location.getLongitude();
        latitude = location.getLatitude();

        //API URL
        BASE_URL = "https://api.foursquare.com/v2/venues/search?ll=" + latitude +"," + longitude + "&categoryId=4d4b7105d754a06374d81259,4d4b7105d754a06376d81259&radius=1000&client_id=" + clientId + "&client_secret=" +clientSecret+ "&v=20170101";
        //locationTxtView.append("\n " + longitude + " " + latitude);
        GetNearAPIData asyncTask = new GetNearAPIData();
        asyncTask.execute();
        //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 50, listener);
    }

    private void displayMarkers(JSONObject placeDetails) {
        try {
            JSONArray venues = placeDetails.getJSONObject("response").getJSONArray("venues");
            if(venues.length() > 0) {
                for (int i = 0; i < venues.length(); i++) {
                    //Log.v("log", venues.getJSONObject(i).toString());
                    double lat = parseDouble(venues.getJSONObject(i).getJSONObject("location").getString("lat"));
                    double lng = parseDouble(venues.getJSONObject(i).getJSONObject("location").getString("lng"));
                    LatLng location = new LatLng(lat, lng);

                    Marker marker = map.addMarker(new MarkerOptions().position(location)
                            .title(venues.getJSONObject(i).getString("name")));
                    marker.setTag(i);
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private class GetNearAPIData extends AsyncTask<Void, String, String> {

        @Override
        protected String doInBackground(Void... voids) {

            //create a URI
            //final String FORECAST_BASE_URL="https://api.foursquare.com/v2/venues/51d145718bbd51c5fe0f3132?client_id=YBO033ISFIBQBHR0RJ3O3RWTRMMS4GGDFTLUDYEMWYZQZWYO&client_secret=KMXY35UEU1VV53RQ2OVHFD3ZPQNWSX2YSK2LQOAHAK4ETTXZ&ll=51.513144,-0.124396&radius=2520&section=drinks&time=any&v=20150409&m=foursquare&limit=50&sortByDistance=1&offset=0";
            //final String FORECAST_BASE_URL = "https://api.foursquare.com/v2/venues/search?ll=" + latitude +"," + longitude + "&client_id=YBO033ISFIBQBHR0RJ3O3RWTRMMS4GGDFTLUDYEMWYZQZWYO&client_secret=KMXY35UEU1VV53RQ2OVHFD3ZPQNWSX2YSK2LQOAHAK4ETTXZ&v=20150409";

            //check connectivity
            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo !=null && networkInfo.isConnected()) {
                //if the device is connected to a network, fetch data

                //call the helper function to get data from the uri
                Log.d(LOG_TAG, BASE_URL);
                result = GET(BASE_URL);
                //Log.v(LOG_TAG, result);


            }
            else {
                String msg = "No network connection";
                publishProgress(msg);
            }

            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
            try {
                placeDetails = new JSONObject(s);
                displayMarkers(placeDetails);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        //The helper function that makes an HTTP GET request using the url passed in as the parameter
        private String GET(String url){
            InputStream is;
            String result="";
            URL request = null;
            //create a URL object from the string url passed in
            try {
                request = new URL(url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            //create an HttpURLConnection object
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) request.openConnection();
                //Connect to the remote object
                conn.connect();
                //Read InputStream
                is = conn.getInputStream();
                if (is!=null)
                    result = convertInputStreamToString(is);
                else
                    result = "Did not work!";
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                //Disconnect
                conn.disconnect();
            }
            return result;
        }

        //The helper function that converts the input stream to String
        private String convertInputStreamToString(InputStream is) throws IOException{
            //initialise a BufferedReader
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
            String line;
            String result = "";
            //Read out the input stream buffer line by line until it's empty
            while((line=bufferedReader.readLine())!=null)
                result += line;
            //close the input stream and return
            is.close();
            return result;
        }

    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

            if (networkInfo == null) {
                Toast.makeText(context, "Map View feature requires an internet connection...", Toast.LENGTH_LONG).show();
                Intent nextIntent = new Intent(context, MyPlacesActivity.class);
                nextIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(nextIntent);
            }else if(networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                Toast.makeText(context, "You're using your mobile data, we recommend a wifi connection.", Toast.LENGTH_LONG).show();

            }
        }
    };
}