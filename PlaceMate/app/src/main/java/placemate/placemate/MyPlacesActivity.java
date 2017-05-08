package placemate.placemate;

import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class MyPlacesActivity extends AppCompatActivity {

    DatabaseHelper databaseHelper;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private Toolbar mToolbar;
    private NavigationView mNavigationView;
    private JSONObject placeDetails;
    private String json = "{ \"response\": { \"venue\": { \"id\": \"51d145718bbd51c5fe0f3132\", name\": \"PETER\" }}}";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_places);
        mToolbar = (Toolbar) findViewById(R.id.nav_action);
        setSupportActionBar(mToolbar);
        //get draw layout
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);

        //add toggle option to layout
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //database helper
        databaseHelper = new DatabaseHelper(this);



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

        try {
            placeDetails = new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            JSONObject venueDetails = placeDetails.getJSONObject("response").getJSONObject("venue");
            String venueName = venueDetails.getString("name");
            toastMessage(venueName);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    //makes icon bar actually work
    public boolean onOptionsItemSelected(MenuItem item){
        if(mToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void AddData(String newEntry){

        boolean insertedData = databaseHelper.addData(newEntry);
        if(insertedData) {
            toastMessage("Data added successfully");
        } else {
            toastMessage("Something went wrong");
        }
    }

    //makes toast with customisable message
    private void toastMessage(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
