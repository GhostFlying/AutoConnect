package tk.djcrazy.autoconnect;

import java.util.HashMap;
import java.util.Map;

import com.github.kevinsawicki.http.HttpRequest;

import roboguice.service.RoboIntentService;
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

public class ConnectService extends RoboIntentService {
	private static final String TAG = "ConnectService";
	private static final String FORCE_LOGIN = "tk.djcrazy.autoconnect.FORCELOGIN";
    private static final String LOGIN_URL_PREFIX = "http://10.50.200.245/";
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
		if (intent.hasExtra("log")){
			logOut(name,pwd);
		}		
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
	private void doLogin(SharedPreferences sharedPreferences, String name, String pwd) {
 		Map<String, String> data = new HashMap<String, String>();
		data.put("action", "login");
		data.put("username", name);
		data.put("password", pwd);
		data.put("ac_id", "5");
		data.put("is_ldap", "1");
		data.put("type", "2");
		data.put("local_auth", "1");

        String networkTest = "";
        try{
            networkTest = HttpRequest.post("http://zuits.zju.edu.cn/").body();
        }
        catch (Exception e){

        }
		if (!networkTest.contains("net.zju.edu.cn")){
			Log.i(TAG, "Login success ever.");
			return;
		}
		else {
			Log.i(TAG, "Have not log in.");
		}
		String body = HttpRequest.post(LOGIN_URL_PREFIX + "cgi-bin/srun_portal").form(data)
				.body();
		Log.d(TAG, body);
		if (body.contains("action=login_ok")){
			Log.i(TAG, "Login success");
			showToastMessage(getString(R.string.login_success));
			sharedPreferences.edit()
				.putLong(MainActivity.LAST_LOGIN_TIME, System.currentTimeMillis()).commit();
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
	
	private void logOut (String username, String pwd){
		String res = HttpRequest.post(LOGIN_URL_PREFIX + "rad_online.php")
				.form("action", "auto_dm").form("uid", -1).form("username", username)
				.form("password", pwd).body();
		Log.d(TAG, res);
		if ("ok".equalsIgnoreCase(res)){
			Log.i(TAG, "Log out successfully.");
		}
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
		        .setSmallIcon(R.drawable.ic_launcher)
		        .setAutoCancel(true)
		        .setContentTitle(title)
		        .setContentText(text);
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
			Intent loginIntent = new Intent(this, ForceLoginReceiver.class);
			PendingIntent mpending = PendingIntent.getBroadcast(this, 0, loginIntent, 0);
			mBuilder.addAction(R.drawable.ic_launcher, getString(R.string.press_to_login), mpending);
		}
		NotificationManager mNotificationManager =
		    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(10, mBuilder.build());
	}
}
