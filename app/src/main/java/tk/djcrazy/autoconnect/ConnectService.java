package tk.djcrazy.autoconnect;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

import com.github.kevinsawicki.http.HttpRequest;

import java.util.HashMap;
import java.util.Map;

import roboguice.service.RoboIntentService;

public class ConnectService extends RoboIntentService {
	private static final String TAG = "ConnectService";
	private static final String FORCE_LOGIN = "tk.djcrazy.autoconnect.FORCELOGIN";
    protected static final String LOGIN_URL_PREFIX = "http://10.50.200.245/";
    protected static final String NETWORK_TEST_URL = "http://10.202.42.20/";
	private Handler handler = new Handler();

	public ConnectService() {
		super("ConnectService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(TAG, "onHandleIntent" + intent);
		SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.USER_INFO,
				MODE_PRIVATE);
		String name = sharedPreferences.getString(MainActivity.USER_NAME, "");
		String pwd = sharedPreferences.getString(MainActivity.PASSWORD, "");

		long lastLogin = sharedPreferences.getLong(MainActivity.LAST_LOGIN_TIME, 0L);
		if (System.currentTimeMillis() - lastLogin < 10000) {
			return;
		}
		if (name.length() < 1 || pwd.length() < 1) {
			showToastMessage(getString(R.string.user_not_configured));
			return;
		}
		try {
			doLogin(sharedPreferences, name, pwd);
		} catch (Exception e) {
			showToastMessage(getString(R.string.error_unknown));
 		}
	}

	/**
	 * @param sharedPreferences
	 * @param name
	 * @param pwd
	 */
	protected void doLogin(SharedPreferences sharedPreferences, String name, String pwd) {
 		Map<String, String> data = new HashMap<String, String>();
		data.put("action", "login");
		data.put("username", name);
		data.put("password", pwd);
		data.put("ac_id", "5");
		data.put("is_ldap", "1");
		data.put("type", "2");
		data.put("local_auth", "1");


        String networkTest = HttpRequest.post(NETWORK_TEST_URL).body();

		if (!networkTest.contains("net.zju.edu.cn")){
			Log.i(TAG, "Login success ever.");
            updateLastLoginTime(sharedPreferences);
			return;
		}
		else {
			Log.i(TAG, "Have not log in.");
		}
		String body = HttpRequest.post(LOGIN_URL_PREFIX + "cgi-bin/srun_portal").form(data)
				.body();
		if (body.contains("action=login_ok")){
			Log.i(TAG, "Login success");
			showToastMessage(getString(R.string.login_success));
            updateLastLoginTime(sharedPreferences);
        }
		else if (body.equals("online_num_error")){
			showToastMessage(getString(R.string.alreay_login));
			showNotification(getString(R.string.alreay_login), "");
		}
		else if (body.equals("password_error")){
			showToastMessage(getString(R.string.password_error));
			showNotification(getString(R.string.password_error), getString(R.string.press_to_check));
		}
		else if (body.equals("username_error")){
			showToastMessage(getString(R.string.username_error));
			showNotification(getString(R.string.username_error), getString(R.string.press_to_check));
		}
		else {
			Log.d(TAG, "login failed:" + body);
			showToastMessage(getString(R.string.other_error) + body);
		}
	}

    private void updateLastLoginTime(SharedPreferences sharedPreferences) {
        sharedPreferences.edit()
            .putLong(MainActivity.LAST_LOGIN_TIME, System.currentTimeMillis()).apply();
    }

    private void showToastMessage(final String msg) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(ConnectService.this, msg, Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	public IBinder onBind(Intent intent){
		Log.d(TAG, "onBind");
		return null;		
	}
	
	private void showNotification(String title, String text){
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(this)
		        .setSmallIcon(R.drawable.ic_warning_white_24dp)
		        .setAutoCancel(true)
		        .setContentTitle(title)
		        .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(new long[]{0, 200})
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
		Intent resultIntent = new Intent(this, MainActivity.class);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addParentStack(MainActivity.class);
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent =
		        stackBuilder.getPendingIntent(
		            0,
		            PendingIntent.FLAG_UPDATE_CURRENT
		        );
		mBuilder.setContentIntent(resultPendingIntent);		
		if (title == getString(R.string.alreay_login)){
            Intent serviceIntent = new Intent(this, ForceLoginService.class);
			PendingIntent mpending = PendingIntent.getService(this, 1, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            Intent dismissServiceIntent = new Intent(this, DismissConnectService.class);
            PendingIntent dismissPending = PendingIntent.getService(this, 2, dismissServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.addAction(R.drawable.ic_clear_white_18dp, getString(R.string.press_to_dismiss), dismissPending);
			mBuilder.addAction(R.drawable.ic_check_white_18dp, getString(R.string.press_to_login), mpending);
            mBuilder.setAutoCancel(true);
		}
		NotificationManager mNotificationManager =
		    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(10, mBuilder.build());
	}


    protected void cancelNotification(){
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(10);
    }
}
