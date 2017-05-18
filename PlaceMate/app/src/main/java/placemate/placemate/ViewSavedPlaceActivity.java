package placemate.placemate;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.design.widget.NavigationView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.vision.text.Text;

public class ViewSavedPlaceActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "SavedPlaceActivity";
    private static final int LOADER_ID = 0x01;
    public String placeId;
    public byte[] placeName;
    public String placeType;
    public String placeRating;
    ContentResolver resolver;
    Button deleteButton;

    //navigation layout variables
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;

    //google sign in variables
    GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_saved_place);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        //navigation switch for drawer menu
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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

        //selected ID passed through via intent
        placeId = getIntent().getStringExtra("PLACE_ID");
        //initialise resolver
        resolver = getContentResolver();
        //initialising custom loader
        getSupportLoaderManager().initLoader(LOADER_ID, null, this);

        deleteButton = (Button) findViewById(R.id.delete_place);

        deleteButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){
                deletePlace();
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] columns = new String[]{"placeBestImg"};

        //pulling the data using custom loader from content provider
        //if id = 1 query should say
        //SELECT name, placeType FROM PLACES WHERE (_ID = 1)
        return new CursorLoader(this, Uri.parse("content://placemate.placemate.PlaceProvider/cpplace"), columns, "_id = " + placeId, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //data for the selected place has been pulled from db
        while(data.moveToNext()) {
            placeName = data.getBlob(0);

        }

        ImageView mainImg = (ImageView) findViewById(R.id.place_image);
        //Log.d("BITMAP FACTORY", BitmapFactory.decodeByteArray(placeName,0,placeName.length).toString());
        mainImg.setImageBitmap(BitmapFactory.decodeByteArray( placeName,
                0,placeName.length));

        TextView name = (TextView) findViewById(R.id.place_name);

        name.setText(placeName.toString());

        TextView type = (TextView) findViewById(R.id.place_type);
        type.setText(placeType);

        TextView rating = (TextView) findViewById(R.id.place_rating);
        rating.setText(placeRating);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void deletePlace() {
       int deletedRows = getContentResolver().delete(PlaceProvider.CONTENT_URL, "_id = " + placeId, null);

        if(deletedRows > 0){
            toastMessage("Place successfully deleted");
            Intent intent = new Intent(getBaseContext(), MyPlacesActivity.class);
            startActivity(intent);
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

    //makes toast with customisable message
    private void toastMessage(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    };


}
