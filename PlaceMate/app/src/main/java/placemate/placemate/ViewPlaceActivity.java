package placemate.placemate;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ViewPlaceActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private Toolbar mToolbar;
    private NavigationView mNavigationView;
    private JSONObject placeDetails;
    private String venueName;
    private String venuePhoneNumber;
    private String venueRating;
    private String venueAddress;
    private TextView addressTxtView;
    private TextView placeNameTxtView;
    private Button getApiBtn;
    private String result;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_place);
        mToolbar = (Toolbar) findViewById(R.id.nav_action);
        setSupportActionBar(mToolbar);
        //get draw layout
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
        getApiBtn = (Button)findViewById(R.id.getApiBtn);
        addressTxtView = (TextView)findViewById(R.id.addressTxtView);
        placeNameTxtView = (TextView)findViewById(R.id.placeNameTxtView);
        //add toggle option to layout
        //mDrawerLayout.addDrawerListener(mToggle);
        //mToggle.syncState();
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        NavigationView nv = (NavigationView)findViewById(R.id.nav_view);
        nv.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case (R.id.nav_my_places):
                        Intent changeToLogin = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(changeToLogin);
                    case (R.id.nav_map_view):
                        Intent changeToMap = new Intent(getApplicationContext(), MapViewActivity.class);
                        startActivity(changeToMap);
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
    }

    //makes icon bar actually work
    public boolean onOptionsItemSelected(MenuItem item){
        if(mToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class GetAPIData extends AsyncTask<Void, String, String> {

        @Override
        protected String doInBackground(Void... voids) {

            //create a URI
            final String FORECAST_BASE_URL="https://api.foursquare.com/v2/venues/51d145718bbd51c5fe0f3132?client_id=YBO033ISFIBQBHR0RJ3O3RWTRMMS4GGDFTLUDYEMWYZQZWYO&client_secret=KMXY35UEU1VV53RQ2OVHFD3ZPQNWSX2YSK2LQOAHAK4ETTXZ&ll=51.513144,-0.124396&radius=2520&section=drinks&time=any&v=20150409&m=foursquare&limit=50&sortByDistance=1&offset=0";

            //check connectivity
            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo !=null && networkInfo.isConnected()) {
                //if the device is connected to a network, fetch data

                //call the helper function to get data from the uri
                result = GET(FORECAST_BASE_URL);
                //Log.v(LOG_TAG, result);


            }
            else {
                //otherwise call publishProgress() to display the error message in a Toast
                String msg = "No network connection";
                publishProgress(msg);

            }

            //remove the following return statements after the extra mile to return an ArrayList of information instead of a raw json string
            //return result;

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
                venueAddress = venueDetails.getJSONObject("location").getString("address");
                venuePhoneNumber = venueDetails.getJSONObject("contact").getString("phone");
                //venueRating = venueDetails.getJSONObject("response").getString("rating");
                //placeNameTxtView.setText(venueName);
                setFields();
                String testString = venueName + " " + venueAddress + " " + venueRating;
            } catch (JSONException e) {
                e.printStackTrace();
            }
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
