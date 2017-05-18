package placemate.placemate;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;

public class UserGuideActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_guide);
        WebView myWebView = (WebView) findViewById(R.id.webview);
        myWebView.loadUrl("http://176.74.17.181/userGuide.html");
    }
}
