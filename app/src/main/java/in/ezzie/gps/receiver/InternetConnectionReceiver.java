package in.ezzie.gps.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.widget.Toast;

import in.ezzie.gps.app.Config;

/**
 * Created by parminder on 8/2/16.
 */
public class InternetConnectionReceiver  extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Make sure it's an event we're listening for ...
        if (!intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION) &&
                !intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION) &&
                !intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))
        {
            return;
        }
        if(Config.isConnected(context)) {
            Toast.makeText(context, "Connected to Network", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Disconnected From Network", Toast.LENGTH_SHORT).show();
        }

    }
}
