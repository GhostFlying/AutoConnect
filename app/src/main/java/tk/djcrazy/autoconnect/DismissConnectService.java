package tk.djcrazy.autoconnect;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.IBinder;

/**
 * Created by ghostflying on 11/14/14.
 */
public class DismissConnectService extends ConnectService {

    @Override
    protected void onHandleIntent(Intent intent){
        WifiManager mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        mWifiManager.setWifiEnabled(false);
        cancelNotification();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
