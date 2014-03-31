package tk.djcrazy.autoconnect;

import java.util.HashMap;
import java.util.Map;

import com.github.kevinsawicki.http.HttpRequest;

import roboguice.service.RoboIntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

public class ConnectService extends RoboIntentService {
	private static final String TAG = "ConnectService";
	private Handler handler = new Handler();

	public ConnectService() {
		super("ConnectService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(TAG, "onHandleIntent");
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
	private void doLogin(SharedPreferences sharedPreferences, String name, String pwd) {
 		Map<String, String> data = new HashMap<String, String>();
		data.put("action", "login");
		data.put("username", name);
		data.put("password", pwd);
		data.put("ac_id", "5");
		data.put("is_ldap", "1");
		data.put("type", "2");
		data.put("local_auth", "1");

		String res = HttpRequest.post("http://net.zju.edu.cn/rad_online.php")
				.form("action", "auto_dm").form("uid", -1).form("username", name)
				.form("password", pwd).body();

		if ("ok".equalsIgnoreCase(res)) {
			String body = HttpRequest.post("http://net.zju.edu.cn/cgi-bin/srun_portal").form(data)
					.body();
			if (body.contains("action=login_ok")) {
				Log.i(TAG, "Login success");
				showToastMessage(getString(R.string.login_success));
 				sharedPreferences.edit()
						.putLong(MainActivity.LAST_LOGIN_TIME, System.currentTimeMillis()).commit();
			} else if ("password_error".equalsIgnoreCase(body)) {
				Log.d(TAG, "login failed:" + res);
				showToastMessage(getString(R.string.password_error));
 			} else if ("username_error".equalsIgnoreCase(body)) {
				Log.d(TAG, "login failed:" + res);
				showToastMessage(getString(R.string.username_error));
 			} else {
				Log.d(TAG, "login failed:" + res);
				showToastMessage(getString(R.string.other_error) + body);
 			}
		} else {
			Log.d(TAG, "rad_online failed:" + res);
			showToastMessage(getString(R.string.other_error) + res);
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
}
