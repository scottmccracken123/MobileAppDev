package placemate.placemate;

import android.app.DownloadManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ShareCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.nearby.messages.Strategy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import cz.msebera.android.httpclient.util.ByteArrayBuffer;

import static com.loopj.android.http.AsyncHttpClient.LOG_TAG;

public class ViewPlaceActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private Toolbar mToolbar;
    private ShareActionProvider mShareActionProvider;
    private NavigationView mNavigationView;
    private JSONObject placeDetails;
    private TextView placeNameTxtView;
    ImageView mImg;
    private String[] result = new String[2];
    private String clientId;
    private String clientSecret;
    private String BASE_URL;
    private String PHOTO_URL;
    private String venueId;
    private String imgSize = "500x300";
    private Button btnSavePlace;
    private String iconPrefix;
    private String iconSuffix;
    private String iconUrl;
    private Bitmap downloadedImg;
    private byte[] downloadImgByteArray;
    private byte[] downloadedBestImgArray;
    HashMap<String, String> resultsH = new HashMap<String, String>();
    private String[] fields = {"venueName", "venueId", "longitude", "latitude", "rating", "phone", "url"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_place);
        btnSavePlace = (Button) findViewById(R.id.btnSavePlace);
        mImg = (ImageView) findViewById(R.id.placeImgView);



        GetAPIData asyncTask = new GetAPIData();
        asyncTask.execute();

        //used to pull intent through from map view
        Intent intent = getIntent();
        venueId = intent.getStringExtra("venueID");
        Log.v("venID", venueId);

        //get variables from layouts and strings file
        clientSecret = getResources().getString(R.string.client_secret);
        clientId = getResources().getString(R.string.client_id);

        mToolbar = (Toolbar) findViewById(R.id.nav_action);
        BASE_URL = "https://api.foursquare.com/v2/venues/"+ venueId +"?client_id="+ clientId +"&client_secret="+ clientSecret +"&v=20170101";
        PHOTO_URL = "https://api.foursquare.com/v2/venues/"+ venueId +"?client_id="+clientId+"&client_secret="+clientSecret+"&v=20150409&m=foursquare&limit=3/photos/";

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);


        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);





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



        btnSavePlace.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){
                Log.d("HERE SCOTT", downloadImgByteArray.toString());
                addData();
            }
        });
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navigation_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);
        mShareActionProvider = new ShareActionProvider(this);
        MenuItemCompat.setActionProvider(menuItem, mShareActionProvider);
        onShareAction();
        return super.onCreateOptionsMenu(menu);
    }



    private void onShareAction(){
        // Create the share Intent
        String playStoreLink = "https://play.google.com/store/apps/details?id=" + getPackageName();
        String yourShareText = "Visiting my favourite place!" + resultsH.get("venueName");
        Intent shareIntent = ShareCompat.IntentBuilder.from(this).setType("text/plain").setText(yourShareText).getIntent();
        // Set the share Intent
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    //makes icon bar actually work
    @Override
    public boolean onOptionsItemSelected(MenuItem item){



        // Fetch and store ShareActionProvide
        if(item.getItemId() == R.id.action_share){
            onShareAction();
        }

        if(mToggle.onOptionsItemSelected(item)){
            onShareAction();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void addData(){

        ContentValues values = new ContentValues();
        values.put(PlaceProvider.name, resultsH.get("venueName"));
        values.put(PlaceProvider.venueId, resultsH.get("venueId"));
        values.put(PlaceProvider.city, resultsH.get("city"));
        values.put(PlaceProvider.postcode, resultsH.get("postcode"));
        values.put(PlaceProvider.phoneNumber, resultsH.get("phoneNumber"));
        values.put(PlaceProvider.rating, resultsH.get("rating"));
        values.put(PlaceProvider.longitude, resultsH.get("longitude"));
        values.put(PlaceProvider.latitude, resultsH.get("latitude"));
        values.put(PlaceProvider.website, resultsH.get("url"));
        values.put(PlaceProvider.placeType, resultsH.get("placeType"));
        //add image as blob to db
        values.put(PlaceProvider.placeImg, downloadImgByteArray);
        values.put(PlaceProvider.placeBestImg, downloadedBestImgArray);

        Uri uri = getContentResolver().insert(PlaceProvider.CONTENT_URL, values);

        toastMessage("New Place Added");
    }

    //makes toast with customisable message
    private void toastMessage(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    };

    private class GetAPIData extends AsyncTask<Void, String, String[]> {

        @Override
        protected String[] doInBackground(Void... voids) {

            //create a URI
            //Log.d(LOG_TAG, BASE_URL);
            //check connectivity
            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo !=null && networkInfo.isConnected()) {
                //if the device is connected to a network, fetch data
                //call the helper function to get data from the uri
                Log.d("BASE URL",BASE_URL);
                Log.d("PHOTO URL", PHOTO_URL);
                result[0] = GET(BASE_URL);
                result[1] = GET(PHOTO_URL);
            }
            else {
                //otherwise call publishProgress() to display the error message in a Toast
                String msg = "No network connection";
                publishProgress(msg);

            }

            //remove the following return statements after the extra mile to return an ArrayList of information instead of a raw json string
            //return result;
            Log.d("JSON INFO ", result[0]);
            Log.d("PHOTO JSON", result[1]);
            return result;
        }

        @Override
        protected void onPostExecute(String[] s) {
            super.onPostExecute(s);
            //get initial JSON Object for venue details
            try {

                 placeDetails = new JSONObject(s[0]);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            //get initial JSON Object for photos
            JSONObject photoDetails = null;
            try {
                Log.d("JSON DEBUG", s[1]);
                photoDetails = new JSONObject(s[1]);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            List<String[]> fieldsToGet = new ArrayList<String[]>();


            //values to look at -- depth based on json
            String nameArr[] = {"response", "venue", "name"};
            String idArr[] = {"response", "venue", "id"};
            String latArr[] = {"response", "venue", "location", "lat"};
            String lngArr[] = {"response", "venue", "location", "lng"};
            String ratingArr[] = {"response", "venue", "rating"};
            String contactArr[] = {"response", "venue", "contact", "phone"};
            String urlArr[] = {"response", "venue", "url"};

            //add fields to loop for get objects/strings from api
            fieldsToGet.add(nameArr);
            fieldsToGet.add(idArr);
            fieldsToGet.add(latArr);
            fieldsToGet.add(lngArr);
            fieldsToGet.add(ratingArr);
            fieldsToGet.add(contactArr);
            fieldsToGet.add(urlArr);

            for(int i = 0; i<fieldsToGet.size(); i++){
                String item[] = fieldsToGet.get(i);
                JSONObject tmpObject = null;
                for(int j = 0; j<item.length; j++){
                    try{

                        Log.d("CURRENT OBJECT ", item[j]);
                        if(j == 0) {
                            tmpObject = placeDetails.getJSONObject(item[j]);
                            Log.d("I = 0 ", item[j]);
                        }  else if (j == item.length-1){
                            String outString = tmpObject.getString(item[j]);
                            resultsH.put(fields[i], outString);
                            Log.d("OUT STRING ", outString);
                        } else {
                            tmpObject = tmpObject.getJSONObject(item[j]);
                        }
                    } catch(JSONException e){
                        resultsH.put(fields[i], "");
                        e.printStackTrace();
                    }
                }
            }

            //JSON NESTED ARRAYS DONE SEPERATELY

            //Get ICONS URL FROM API
            try{
                JSONObject venueDetails = placeDetails.getJSONObject("response").getJSONObject("venue");
                JSONObject icon = venueDetails.getJSONArray("categories").getJSONObject(0).getJSONObject("icon");
                iconPrefix = icon.getString("prefix");
                iconSuffix = icon.getString("suffix");
                int imgSize = 88;
                iconUrl = iconPrefix + imgSize + iconSuffix;

                Log.d("TOP IMG URL", iconUrl);



            }catch(JSONException e){
                e.printStackTrace();
            }

            //Get Category from JSON Array
            try {
                JSONObject venueDetails = placeDetails.getJSONObject("response").getJSONObject("venue");
                resultsH.put("placeType", venueDetails.getJSONArray("categories").getJSONObject(0).getString("shortName"));
                //james
            } catch (JSONException e) {
                e.printStackTrace();
            }


            //CODE FOR GETTING FULL ADDRESS
            try {
                JSONObject venueDetails = placeDetails.getJSONObject("response").getJSONObject("venue");
                //different length address'
                JSONArray addressArr = venueDetails.getJSONObject("location").getJSONArray("formattedAddress");
                int length = addressArr.length();
                resultsH.put("addressOne", addressArr.getString(0));
                if (length > 5) {
                    resultsH.put("addressTwo",  addressArr.getString(1));
                    resultsH.put("city",  addressArr.getString(2));
                    resultsH.put("postcode",  addressArr.getString(4));
                } else {
                    resultsH.put("city",  addressArr.getString(1));
                    resultsH.put("postcode",  addressArr.getString(3));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            String bestImgCompleteURL = "";
            try{
                JSONObject bestImg = photoDetails.getJSONObject("response").getJSONObject("venue").getJSONObject("bestPhoto");
                String bestImgPrefix = bestImg.getString("prefix");
                String bestImgSuffix = bestImg.getString("suffix");
                bestImgCompleteURL = bestImgPrefix + imgSize + bestImgSuffix;
                Log.d("Img Prefix!", bestImgCompleteURL);
            } catch (JSONException e){
                Log.d("NO IMAGE", "NO IMAGE");
                e.printStackTrace();
            }

            String[] getImages = {iconUrl, bestImgCompleteURL};
            GetImageClass imgClass = new GetImageClass();
            imgClass.execute(getImages);

            setFields();
            //used for share -- gets place name (stops null value error when sharing a place)
            invalidateOptionsMenu();
        }

        private void setFields(){
            Log.d("HEREEE", resultsH.get("venueName"));

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


    //GET IMAGE ON BACKGROUND THREAD
    private class GetImageClass extends AsyncTask<String[], String, ArrayList<byte[]>>{

        @Override
        protected ArrayList<byte[]> doInBackground(String[]... params) {
            String[] passedImgUrl = params[0];
            String imgIcon = passedImgUrl[0];
            String imgBest = passedImgUrl[1];
            Log.d("ICON IMG", imgIcon);
            Log.d("BEST IMG", "IMAGE BEST" + imgBest);

            byte[] imgByte = getLogoImage(imgIcon);
            byte[] bestImgByte = null;
            if(imgBest != ""){
                bestImgByte = getLogoImage(imgBest);
                Log.d("Best Image", bestImgByte.toString());
            } else{
                Log.d("IMAGE NULL", "NO BEST IMAGE");
            }

            ArrayList<byte[]> returnImages = new ArrayList<>();
            returnImages.add(0, bestImgByte);
            returnImages.add(1, imgByte);

            return returnImages;
        }

        @Override
        protected void onPostExecute(ArrayList<byte[]> result) {
            super.onPostExecute(result);

            downloadedBestImgArray = result.get(0);
            downloadImgByteArray = result.get(1);
            //ByteArrayInputStream imgStream = new ByteArrayInputStream(downloadImgByteArray);
            //Bitmap downloadedImg = BitmapFactory.decodeStream(imgStream);
            //mImg.setImageBitmap(downloadedImg);

            Log.d("ICON", Arrays.toString(downloadImgByteArray));
            Log.d("BEST IMAGE", Arrays.toString(downloadedBestImgArray));
        }

        private byte[] getLogoImage(String url){
            try {
                URL imageUrl = new URL(url);
                URLConnection ucon = imageUrl.openConnection();

                InputStream is = ucon.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(is);

                ByteArrayBuffer baf = new ByteArrayBuffer(500);
                int current = 0;
                while ((current = bis.read()) != -1) {
                    baf.append((byte) current);
                }

                return baf.toByteArray();
            } catch (Exception e) {
                Log.d("ImageManager", "Error: " + e.toString());
                return null;
            }
        }

    }


}
