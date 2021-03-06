package tk.djcrazy.autoconnect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class ConnectionChangeReceiver extends BroadcastReceiver {

	private static final String TAG = ConnectionChangeReceiver.class.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "onReceive:"+intent);
 		ConnectivityManager connManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		State state = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
		Log.d(TAG, "onReceive:"+ state.toString());
		if (State.CONNECTED == state) {
	        WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);  
	        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
	        if ("ZJUWLAN".equals(wifiInfo.getSSID().replace("\"", ""))) {
	 	 		context.startService(new Intent(context, ConnectService.class));
			}
		}
  	}
}