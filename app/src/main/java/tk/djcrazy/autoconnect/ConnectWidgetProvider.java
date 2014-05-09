package tk.djcrazy.autoconnect;

import java.util.List;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

public class ConnectWidgetProvider extends AppWidgetProvider {
	
	final String TAG = this.getClass().getName();

	
	@Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(TAG, "onUpdate(): appWidgetIds.length="+appWidgetIds.length);
        RemoteViews remoteView = new RemoteViews(context.getPackageName(), R.layout.connect_widget_layout);
        remoteView.setOnClickPendingIntent(R.id.connect_button, getPendingIntent(context));
        for (int id: appWidgetIds) {
        	appWidgetManager.updateAppWidget(id, remoteView);
        }
    }
	
    @Override  
    public void onReceive(Context context, Intent intent) {  
        super.onReceive(context, intent);  
        Log.d(TAG, "onReceive start." + intent);
        if (intent.hasCategory(Intent.CATEGORY_ALTERNATIVE)) {
        	 WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);         	 
        	 
        	 WifiConfiguration zjuwlan = new WifiConfiguration();
        	 zjuwlan.SSID = "\"ZJUWLAN\"";
        	 zjuwlan.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        	 while (!mWifiManager.isWifiEnabled()) {
        		 mWifiManager.setWifiEnabled(true);
        	 }
        	 mWifiManager.disconnect();
        	 int netid = mWifiManager.addNetwork(zjuwlan);
        	 mWifiManager.enableNetwork(netid, true);
        	 mWifiManager.reconnect();
        	 List<WifiConfiguration> wifiList = mWifiManager.getConfiguredNetworks();
        	 Log.d(TAG, "total count :" + wifiList.size());
        	 /*List<WifiConfiguration> wifiList = mWifiManager.getConfiguredNetworks();
        	 Log.d(TAG, "total count :" + wifiList.size());
        	 mWifiManager.disconnect();
        	 for (WifiConfiguration each :wifiList) {      
        		 Log.d(TAG, "SSID :" + each.SSID);
        		 if (each.SSID.replace("\"", "").equals("ZJUWLAN")) {
        			 mWifiManager.enableNetwork(each.networkId, true);
        			 mWifiManager.reconnect();
        			 return;
        		 }
        	 }
        	 Toast.makeText(context, context.getString(R.string.please_connect_once), Toast.LENGTH_SHORT).show();
        	 Log.e(TAG, "ZJUWLAN is not stored");        	 */
        }
    }  
	
	private PendingIntent getPendingIntent(Context context) {
		Intent intent = new Intent(context, ConnectWidgetProvider.class);
		intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
		PendingIntent mpending = PendingIntent.getBroadcast(context, 0, intent, 0);
        return mpending;
    }

}
