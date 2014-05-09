package tk.djcrazy.autoconnect;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ForceLoginReceiver extends BroadcastReceiver {
	
	private static final String TAG = ForceLoginReceiver.class.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onReceive:"+intent);
		Intent serviceIntent = new Intent(context, ConnectService.class);
		serviceIntent.putExtra("log", true);
		context.startService(serviceIntent);
		
		NotificationManager mNotificationManager =
			    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(10);
	}	


}
