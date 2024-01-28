package it.uniba.dib.sms23248;







import static java.security.AccessController.getContext;




import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.widget.Toast;

public class NetworkChangeReceiver extends BroadcastReceiver {



    @Override
    public void onReceive(Context context, Intent intent) {
        String connessione=context.getString(R.string.connessione);

        if (NetworkUtils.isNetworkAvailable(context)) {
            // Internet connection is available
        } else {
            Toast.makeText(context,connessione, Toast.LENGTH_LONG).show();
        }
    }
}
