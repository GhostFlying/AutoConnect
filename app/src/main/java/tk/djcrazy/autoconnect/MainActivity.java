package tk.djcrazy.autoconnect;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
public class MainActivity extends ActionBarActivity {

	public static final String USER_INFO = "user_info";
	public static final String USER_NAME = "user_name";
	public static final String PASSWORD = "password";
	public static final String LAST_LOGIN_TIME = "last_login_time";


	private EditText userName;
	private EditText password;
	private Button sure;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        userName = (EditText)findViewById(R.id.username);
        password = (EditText)findViewById(R.id.password);
        sure = (Button)findViewById(R.id.sure);
        SharedPreferences userInfo = getSharedPreferences("user_info", MODE_PRIVATE);  
        userName.setText( userInfo.getString("user_name", ""));
        password.setText( userInfo.getString("password", ""));
        sure.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String name =  userName.getText().toString().trim();
				String pwd = password.getText().toString().trim();
				if (name.length()<1|| pwd.length()<1) {
					Toast.makeText(MainActivity.this, getString(R.string.input_empty), Toast.LENGTH_SHORT).show();
				} else {
			        Editor editor = getSharedPreferences("user_info", MODE_PRIVATE).edit();
			        editor.putString(USER_NAME, name);
			        editor.putString(PASSWORD, pwd);
			        editor.commit();
					Toast.makeText(MainActivity.this, getString(R.string.input_saved), Toast.LENGTH_SHORT).show();
					finish();
				}
			}
		});
	}
}
