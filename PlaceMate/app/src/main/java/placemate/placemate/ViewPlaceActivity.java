package placemate.placemate;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.nearby.messages.Strategy;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import static com.loopj.android.http.AsyncHttpClient.LOG_TAG;

public class ViewPlaceActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private Toolbar mToolbar;
    private NavigationView mNavigationView;
    private JSONObject placeDetails;
    private String venueName;
    private String phoneNumber;
    private String rating;
    private String addressOne;
    private String addressTwo;
    private String city;
    private String postcode;
    private String longitude;
    private String latitude;
    private String placeType;
    private String website;
    private String venueAddress;
    private TextView addressTxtView;
    private TextView placeNameTxtView;
    private Button getApiBtn;
    private String result;
    private String clientId;
    private String clientSecret;
    private String BASE_URL;
    private String venueId;
    private Button btnSavePlace;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_place);
        btnSavePlace = (Button) findViewById(R.id.btnSavePlace);

        //used to pull intent through from map view

        Intent intent = getIntent();
        venueId = intent.getStringExtra("venueID");
        //venueId = "51d145718bbd51c5fe0f3132";
        Log.v("venID", venueId);

        //get variables from layouts and strings file
        clientSecret = getResources().getString(R.string.client_secret);
        clientId = getResources().getString(R.string.client_id);
        mToolbar = (Toolbar) findViewById(R.id.nav_action);
        BASE_URL = "https://api.foursquare.com/v2/venues/"+ venueId +"?client_id="+ clientId +"&client_secret="+ clientSecret +"&v=20170101";




        //get draw layout (side bar layout)
        setSupportActionBar(mToolbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
        getApiBtn = (Button)findViewById(R.id.getApiBtn);
        addressTxtView = (TextView)findViewById(R.id.addressTxtView);
        placeNameTxtView = (TextView)findViewById(R.id.placeNameTxtView);


        //navigation switch for drawer menu
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
    protected void onStart() {
        super.onStart();
        GetAPIData asyncTask = new GetAPIData();
        asyncTask.execute();

        btnSavePlace.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){
                addData();
            }
        });
    }

    //makes icon bar actually work
    public boolean onOptionsItemSelected(MenuItem item){
        if(mToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void addData(){

        ContentValues values = new ContentValues();

        values.put(PlaceProvider.name, venueName);
        values.put(PlaceProvider.venueId, venueId);
        values.put(PlaceProvider.addressOne, addressOne);
        values.put(PlaceProvider.addressTwo, addressTwo);
        values.put(PlaceProvider.city, city);
        values.put(PlaceProvider.postcode, postcode);
        values.put(PlaceProvider.phoneNumber, phoneNumber);
        values.put(PlaceProvider.rating, rating);
        values.put(PlaceProvider.longitude, longitude);
        values.put(PlaceProvider.latitude, latitude);
        values.put(PlaceProvider.website, website);
        values.put(PlaceProvider.placeType, placeType);

        Uri uri = getContentResolver().insert(PlaceProvider.CONTENT_URL, values);

        toastMessage("New Place Added");
    }

    //makes toast with customisable message
    private void toastMessage(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private class GetAPIData extends AsyncTask<Void, String, String> {

        @Override
        protected String doInBackground(Void... voids) {

            //create a URI
            //final String FORECAST_BASE_URL="https://api.foursquare.com/v2/venues/51d145718bbd51c5fe0f3132?client_id=YBO033ISFIBQBHR0RJ3O3RWTRMMS4GGDFTLUDYEMWYZQZWYO&client_secret=KMXY35UEU1VV53RQ2OVHFD3ZPQNWSX2YSK2LQOAHAK4ETTXZ&ll=51.513144,-0.124396&radius=2520&section=drinks&time=any&v=20150409&m=foursquare&limit=50&sortByDistance=1&offset=0";
            Log.d(LOG_TAG, BASE_URL);
            //check connectivity
            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo !=null && networkInfo.isConnected()) {
                //if the device is connected to a network, fetch data

                //call the helper function to get data from the uri
                result = GET(BASE_URL);
                //Log.v(LOG_TAG, result);


            }
            else {
                //otherwise call publishProgress() to display the error message in a Toast
                String msg = "No network connection";
                publishProgress(msg);

            }

            //remove the following return statements after the extra mile to return an ArrayList of information instead of a raw json string
            //return result;
            Log.d("RESULT ", result);
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                 placeDetails = new JSONObject(s);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                JSONObject venueDetails = placeDetails.getJSONObject("response").getJSONObject("venue");
                venueName = venueDetails.getString("name");
                Log.v("test", venueName);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                JSONObject venueDetails = placeDetails.getJSONObject("response").getJSONObject("venue");
                venueAddress = venueDetails.getJSONObject("location").getString("address");

            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                JSONObject venueDetails = placeDetails.getJSONObject("response").getJSONObject("venue");
                phoneNumber = venueDetails.getJSONObject("contact").getString("phone");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                JSONObject venueDetails = placeDetails.getJSONObject("response").getJSONObject("venue");
                rating = venueDetails.getString("rating");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                JSONObject venueDetails = placeDetails.getJSONObject("response").getJSONObject("venue");
                latitude = venueDetails.getJSONObject("location").getString("lat");

            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                JSONObject venueDetails = placeDetails.getJSONObject("response").getJSONObject("venue");
                longitude = venueDetails.getJSONObject("location").getString("lng");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                JSONObject venueDetails = placeDetails.getJSONObject("response").getJSONObject("venue");
                placeType = venueDetails.getJSONArray("categories").getJSONObject(0).getString("shortName");
                //james
                website = "www.google.com";
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                JSONObject venueDetails = placeDetails.getJSONObject("response").getJSONObject("venue");
                //different length address'
                int length = venueDetails.getJSONObject("location").getJSONArray("formattedAddress").length();
                addressOne = venueDetails.getJSONObject("location").getJSONArray("formattedAddress").getString(0);
                if (length > 5) {
                    addressTwo = venueDetails.getJSONObject("location").getJSONArray("formattedAddress").getString(1);
                    city = venueDetails.getJSONObject("location").getJSONArray("formattedAddress").getString(2);
                    postcode = venueDetails.getJSONObject("location").getJSONArray("formattedAddress").getString(4);
                } else {
                    city = venueDetails.getJSONObject("location").getJSONArray("formattedAddress").getString(1);
                    postcode = venueDetails.getJSONObject("location").getJSONArray("formattedAddress").getString(3);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            setFields();
        }

        private void setFields(){
            placeNameTxtView.setText(venueName);
            placeNameTxtView.setText(venueName);
            addressTxtView.setText(venueAddress);
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
}
