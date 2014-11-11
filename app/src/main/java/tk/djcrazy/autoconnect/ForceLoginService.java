package tk.djcrazy.autoconnect;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.github.kevinsawicki.http.HttpRequest;

/**
 * Created by ghostflying on 11/2/14.
 */
public class ForceLoginService extends ConnectService {
    private String TAG = this.getClass().getSimpleName();

    @Override
    protected void doLogin(SharedPreferences sharedPreferences, String name, String pwd){
        logOut(name, pwd);
        super.doLogin(sharedPreferences, name, pwd);
        cancelNotification();
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

}
