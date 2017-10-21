package tomatojuice.sakura.ne.jp.vocalocharactercompass;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class CompassPreference extends AppCompatActivity{
	private ListView listView;
	private int prefInt;
	SharedPreferences pref;
	Editor editor;
	Intent intent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTheme();

		String[] themes = getResources().getStringArray(R.array.theme_array);

		listView = (ListView)findViewById(R.id.listview);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, themes);
		listView.setAdapter(adapter);

		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
				Log.i("arg2の値", "arg2の値： " + arg2);
//				pref = getSharedPreferences("pref",MODE_PRIVATE);
				editor = pref.edit();
				editor.putInt("key", arg2);
				editor.commit();

				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){ //Android 6.0以上
					intent = new Intent(getApplicationContext(), CompassMain.class);
				}
				else{
					intent = new Intent(getApplicationContext(), CompassMainforGingerbread.class);
				}

				startActivity(intent);
				finish();
			} // Listener
		});

	} // onCreate

	private void setTheme(){
		pref = getSharedPreferences("pref", MODE_PRIVATE);
		prefInt = pref.getInt("key", 0);
		Log.i("prefInt", "prefIntの値 : "+prefInt);
		switch(prefInt){
			case 0:
				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.miku_1));
					getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.miku_1));
				}
					setTheme(R.style.MikuTheme);
					setContentView(R.layout.miku_pref);
				break;
			case 1:
				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.gumi_4));
					getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.gumi_4));
				}
					setTheme(R.style.GumiTheme);
					setContentView(R.layout.gumi_pref);
				break;
			default:
				break;
		} // switch
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode != KeyEvent.KEYCODE_BACK){
			// Back Keyでなければ何もしない
		}
		else{
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){ //Android 6.0以上
				intent = new Intent(getApplicationContext(), CompassMain.class);
			}
			else{
				intent = new Intent(getApplicationContext(), CompassMainforGingerbread.class);
			}

			startActivity(intent);
			finish();
		}
		return super.onKeyDown(keyCode, event);
	} // onKeyDown

}

