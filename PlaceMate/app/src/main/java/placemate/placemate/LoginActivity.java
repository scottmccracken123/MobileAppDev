package placemate.placemate;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

        GoogleApiClient mGoogleApiClient;
        private static final int RC_SIGN_IN = 1000;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_login);

                //Listener
                findViewById(R.id.sign_in_button).setOnClickListener(this);

                GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .build();

                mGoogleApiClient = new GoogleApiClient.Builder(this)
                        //.enableAutoManage(this, (GoogleApiClient.OnConnectionFailedListener) this)
                        .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                        .build();
        }
        @Override
        public void onClick(View v) {
                switch(v.getId()) {
                        case R.id.sign_in_button:
                                signIn();
                                break;
                }
        }
        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
                super.onActivityResult(requestCode, resultCode, data);
                if(requestCode == RC_SIGN_IN){
                        GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                        handleSignInResult(result);
                }
        }
        private void handleSignInResult(GoogleSignInResult result) {
               if (result.isSuccess()) {
                       // go to nice new page?
                       GoogleSignInAccount account = result.getSignInAccount();

                       Intent toMyPlaces = new Intent(LoginActivity.this, MyPlacesActivity.class);
                       toMyPlaces.putExtra("firstName", account.getGivenName());
                       toMyPlaces.putExtra("email", account.getEmail());
                        toMyPlaces.putExtra("ID", account.getId());
                       startActivity(toMyPlaces);

               }else{
                       // go to nasty page
               }
        }

        private void signIn() {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);
        }
}

