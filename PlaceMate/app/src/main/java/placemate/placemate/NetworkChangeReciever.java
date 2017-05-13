/*

Script checks network activity
If down - notify user with toast

 */

package placemate.placemate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

public class NetworkChangeReciever extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        //get connection properties
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        Log.v("network", "work");

        //if not connected to the internet
        if (activeNetwork == null) {
            Log.v("network", "toast");
            Toast.makeText(context, "NOT CONNECTED TO THE INTERNET!!!! ", Toast.LENGTH_LONG).show();
            //possibly disable map view?
        }



    }
}
