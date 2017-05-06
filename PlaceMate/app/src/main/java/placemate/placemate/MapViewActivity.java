package placemate.placemate;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
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
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import static android.provider.UserDictionary.Words.APP_ID;

public class MapViewActivity extends AppCompatActivity{
    private Button getLocationBtn;
    private TextView locationTxtView;
    private LocationManager locationManager;
    private LocationListener listener;
    private final int permissionNumber = 10;
    private double longitude;
    private double latitude;
    private JSONObject placeDetails;


    private Button getApiBtn;
    private String result;



    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_map_view);

        locationTxtView = (TextView) findViewById(R.id.longitudeTxt);
        getLocationBtn = (Button) findViewById(R.id.getLocationBtn);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);


        listener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                longitude = location.getLongitude();
                latitude = location.getLatitude();
                locationTxtView.append("\n " + longitude + " " + latitude);
                GetNearAPIData asyncTask = new GetNearAPIData();
                asyncTask.execute();
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

        configure_button();




    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case permissionNumber:
                configure_button();
                break;
            default:
                break;
        }
    }


    void configure_button(){
        // first check for permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.INTERNET}
                        ,permissionNumber);
            }
            return;
        }
        // this code won't execute IF permissions are not allowed, because in the line above there is return statement.
        getLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //noinspection MissingPermission
                locationManager.requestLocationUpdates("gps", 5000, 0, listener);
            }
        });
    }

    private class GetNearAPIData extends AsyncTask<Void, String, String> {

        @Override
        protected String doInBackground(Void... voids) {

            //create a URI
            //final String FORECAST_BASE_URL="https://api.foursquare.com/v2/venues/51d145718bbd51c5fe0f3132?client_id=YBO033ISFIBQBHR0RJ3O3RWTRMMS4GGDFTLUDYEMWYZQZWYO&client_secret=KMXY35UEU1VV53RQ2OVHFD3ZPQNWSX2YSK2LQOAHAK4ETTXZ&ll=51.513144,-0.124396&radius=2520&section=drinks&time=any&v=20150409&m=foursquare&limit=50&sortByDistance=1&offset=0";
            final String FORECAST_BASE_URL = "https://api.foursquare.com/v2/venues/search?ll=" + latitude +"," + longitude + "&client_id=YBO033ISFIBQBHR0RJ3O3RWTRMMS4GGDFTLUDYEMWYZQZWYO&client_secret=KMXY35UEU1VV53RQ2OVHFD3ZPQNWSX2YSK2LQOAHAK4ETTXZ&v=20150409";

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
                String msg = "No network connection";
                publishProgress(msg);
            }

            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
            try {
                placeDetails = new JSONObject(s);
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

}