package tk.djcrazy.autoconnect;



import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.RemoteViews;

public class ConnectWidgetProvider extends AppWidgetProvider {
	
	final String TAG = this.getClass().getName();
    static int[] ids = null;

	
	@Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(TAG, "onUpdate(): appWidgetIds.length="+appWidgetIds.length);
        RemoteViews remoteView = new RemoteViews(context.getPackageName(), R.layout.connect_widget_layout);
        remoteView.setOnClickPendingIntent(R.id.imageButton, getPendingIntent(context));
        WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (mWifiManager.isWifiEnabled() && mWifiManager.getConnectionInfo().getSSID().equalsIgnoreCase("\"ZJUWLAN\"")) {
            remoteView.setImageViewResource(R.id.imageButton, R.drawable.open);
        }
        else {
            remoteView.setImageViewResource(R.id.imageButton, R.drawable.close);
        }
        ids = appWidgetIds;
        appWidgetManager.updateAppWidget(appWidgetIds, remoteView);
    }
	
    @Override  
    public void onReceive(Context context, Intent intent) {  
        super.onReceive(context, intent);  
        Log.d(TAG, "onReceive start." + intent);
        if (intent.hasCategory(Intent.CATEGORY_ALTERNATIVE)) {
            RemoteViews remoteView = new RemoteViews(context.getPackageName(), R.layout.connect_widget_layout);
            remoteView.setOnClickPendingIntent(R.id.imageButton, getPendingIntent(context));
        	WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (mWifiManager.isWifiEnabled() && mWifiManager.getConnectionInfo().getSSID().equalsIgnoreCase("\"ZJUWLAN\"")) {
                mWifiManager.setWifiEnabled(false);
                remoteView.setImageViewResource(R.id.imageButton, R.drawable.close);
            }
            else {
                WifiConfiguration zjuwlan = new WifiConfiguration();
                zjuwlan.SSID = "\"ZJUWLAN\"";
                zjuwlan.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                while (!mWifiManager.isWifiEnabled()) {
                    mWifiManager.setWifiEnabled(true);
                }
                mWifiManager.disconnect();
                int netId = mWifiManager.addNetwork(zjuwlan);
                mWifiManager.enableNetwork(netId, true);
                mWifiManager.reconnect();
                remoteView.setImageViewResource(R.id.imageButton, R.drawable.open);
            }
            AppWidgetManager mAppWidget= AppWidgetManager.getInstance(context);
            mAppWidget.updateAppWidget(ids, remoteView);
        }
    }  
	
	private PendingIntent getPendingIntent(Context context) {
		Intent intent = new Intent(context, ConnectWidgetProvider.class);
		intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
		PendingIntent mpending = PendingIntent.getBroadcast(context, 0, intent, 0);
        return mpending;
    }

}
