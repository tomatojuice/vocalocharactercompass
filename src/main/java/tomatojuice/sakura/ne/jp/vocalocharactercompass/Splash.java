package tomatojuice.sakura.ne.jp.vocalocharactercompass;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class Splash extends AppCompatActivity{

	private int prefInt;
	SharedPreferences pref;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTheme();

		// Handlerで1秒後にCompassMainクラスを呼び出す
		Handler splashHandler = new Handler();
		splashHandler.postDelayed(new SplashHandler(), 1000);

	} // onCreateはここまで

	private void setTheme(){
		pref = getSharedPreferences("pref", MODE_PRIVATE);
		prefInt = pref.getInt("key", 0);
		Log.i("prefInt", "prefIntの値 : "+prefInt);
		switch(prefInt){
		case 0:
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.miku_9));
				getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.miku_9));
			}
				setTheme(R.style.MikuTheme);
				setContentView(R.layout.splash_miku);
			break;
		case 1:
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.gumi_4));
				getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.gumi_4));
			}
				setTheme(R.style.GumiTheme);
				setContentView(R.layout.splash_gumi);
			break;
		default:
			break;
		} // switch
	}

	/** ハンドラーで数秒起動が面を停止させた後に実行 **/
	class SplashHandler implements Runnable{
		@Override
		public void run(){
			Intent intent;

			/*
			* Camera2 APIをimportするだけでAndroid2.3.3はクラッシュするので、仕方なく2つのMainとAboutクラス分けた。
			* 因みにCamera APIは5.0から非推奨だけど動くけど、6.0だと動作しないので、6.0以上と6.0未満で振り分けている。
			* */

			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
				intent = new Intent(getApplication(),CompassMain.class);
			}
			else{
				intent = new Intent(getApplication(),CompassMainforGingerbread.class);
			}
			startActivity(intent);
			Splash.this.finish(); // Activityを終わらせる
		}
	} // SplashHandlerはここまで
}
