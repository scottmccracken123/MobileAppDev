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
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.widget.TextView;

import org.w3c.dom.Text;


public class MyPlacesActivity extends AppCompatActivity {

    DatabaseHelper databaseHelper;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private Toolbar mToolbar;
    private NavigationView mNavigationView;
  
    String result;

    private String users_name;
    private String users_email;
    private String users_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent previousIntent = getIntent();

        users_name = (String)previousIntent.getSerializableExtra("firstName");
        users_email = (String)previousIntent.getSerializableExtra("email");
        users_id = (String)previousIntent.getSerializableExtra("ID");


        setContentView(R.layout.activity_my_places);
        mToolbar = (Toolbar) findViewById(R.id.nav_action);
        setSupportActionBar(mToolbar);
        //get draw layout
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
        final Button viewPlaceBtn = (Button)findViewById(R.id.viewPlaceBtn);
        //add toggle option to layout
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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



        viewPlaceBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Intent changeToPlace = new Intent(getApplicationContext(), ViewPlaceActivity.class);
                //add in place to change from map? id? url?
                //changeToPlace.putExtra("weatherdetail", "PLACE");
                startActivity(changeToPlace);
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
