package tomatojuice.sakura.ne.jp.vocalocharactercompass;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import java.util.Locale;

public class AboutMikuCompass extends AppCompatActivity {
	private AdView adView;
	private String url;
	private TextView links,sensorText;
	private Toast toast;
	private LinearLayout layout;
	private int prefInt;
	private Toolbar toolbar;
	private Intent intent;
	private CharSequence sequence;
	SharedPreferences pref;

	private Camera mCamera;
	private Camera.Parameters mParameters;
	private CameraManager mCameraManager;
	private String mCameraId;
	private Boolean isTorchOn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTheme();

		toolbar = (Toolbar)findViewById(R.id.toolbar);
		toolbar.setTitle("");
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		getSupportActionBar().setDisplayShowHomeEnabled(false);

		links = (TextView)findViewById(R.id.weblink);
		Log.i("ロケール",Locale.getDefault().toString());

		if(Locale.CHINA.equals(Locale.getDefault())){ // 中国語
			url = "QQ空间：　   Go to <a href=\"http://64668838.qzone.qq.com/\">TomatoJuice的QQ空间</a>";
		}
		else{ // その他の言語
			url = "Google+ ：　　<a href=\"https://plus.google.com/112157917158095099005/\">Miku Compass</a>";
			Log.i("リンク","その他OK");
		}

		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.N){ // Android 7.0以下
			sequence = Html.fromHtml(url);
		}
		else{
			sequence = Html.fromHtml(url,Html.FROM_HTML_MODE_COMPACT);
		}
		MovementMethod movement = LinkMovementMethod.getInstance();
		links.setMovementMethod(movement);
		links.setText(sequence);
		sensorText = (TextView)findViewById(R.id.sensor);
		sensorText.setText(CompassMain.STR); // CompassMainで取得したセンサー一覧を出力

        isTorchOn = false;

		createAdMob();

	} // onCreate()

	private void setTheme(){
		pref = getSharedPreferences("pref", MODE_PRIVATE);
		prefInt = pref.getInt("key", 0);
		Log.i("prefInt", "prefIntの値 : "+prefInt);
		switch(prefInt){
		case 0:
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) { // Android 5.0以上
				getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.miku_9));
				getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.miku_9));
			}
			setTheme(R.style.MikuTheme);
			setContentView(R.layout.miku_about);
			break;
		case 1:
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { // Android 5.0以上
                getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.gumi_4));
                getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.gumi_4));
            }
			setTheme(R.style.GumiTheme);
			setContentView(R.layout.gumi_about);
			break;
		default:
			break;
		} // switch
	} // setTheme

	private void createAdMob(){

		// ネットワークの接続情報を取得
		ConnectivityManager cm = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();

		/** ネットに接続していたらAdmobを表示する **/
		if(ni!=null){
			adView = new AdView(this);
			adView.setAdUnitId("ca-app-pub-6891896770413839/5816991332");
			adView.setAdSize(AdSize.SMART_BANNER);

			layout = (LinearLayout)findViewById(R.id.admodlinear);
			layout.addView(adView);
			AdRequest adRequest = new AdRequest.Builder().build();
			adView.loadAd(adRequest);
			Log.i("NetWorkInfo", "接続済みです");
		}
		else{
			Log.i("NetWorkInfo","未接続です");
		}
	} // createAdMob()

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i("onDestroy()","onDestroy()が呼ばれました");
		if(adView!=null){
			adView.destroy();
		}
		if(mCamera!=null){ // カメラが利用中なら開放する
			mCamera.release();
			mCamera = null;
		}
		if(isTorchOn){
			turnOffFlashLight();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if(adView!=null){
			adView.pause();
		}
        if(mCamera!=null){ // カメラが利用中なら開放する
            mCamera.release();
            mCamera = null;
        }
        if(isTorchOn){
            turnOffFlashLight();
        }
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(adView!=null){
			adView.resume();
		}
        // カメラオープン
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) { // Android5.0未満
            try {
                mCamera = Camera.open();
				mCamera.startPreview();
                Log.i("onResume", "CameraのOpenに成功");
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("onResume", "CameraのOpenに失敗");
            }
        }
        else { // Android 6.0以上はこちら
            mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            try{
                mCameraId = mCameraManager.getCameraIdList()[0];
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        if(isTorchOn){
            turnOnFlashLight();
        }
	}

	@Override
	protected void onStop() {
		super.onStop();
        if(isTorchOn){
            turnOffFlashLight();
        }
		finish();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode != KeyEvent.KEYCODE_BACK){
			// Back Keyでなければ何もしない
		}
		else{
			intent = new Intent(getApplicationContext(), CompassMain.class);
			startActivity(intent);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			finish();
		}
		return super.onKeyDown(keyCode, event);
	} // onKeyDown

    private void turnOnFlashLight() {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            try {
                mCameraManager.setTorchMode(mCameraId,true);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        else if(mCamera!=null){ //カメラが起動している場合
            mParameters = mCamera.getParameters();
            //[FLASH_MODE_TORCH)]常に点灯
            mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            mCamera.setParameters(mParameters);
        }
    } // turnOnFlashLight()

    private void turnOffFlashLight() {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                mCameraManager.setTorchMode(mCameraId,false);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
        else if(mCamera!=null){ //カメラが起動している場合
            mParameters = mCamera.getParameters();
            //[FLASH_MODE_OFF]消灯
            mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(mParameters);
        }
    } // turnOffFlashLight()


	/** メニューの作成 **/
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.menu,menu);
		return super.onCreateOptionsMenu(menu);
	} // onCreateOptionsMenu

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		Log.i("itemId", "iTemIdの値: " + String.valueOf(itemId));
    	switch(itemId){
			case R.id.theme:
				Intent intent_pref = new Intent(this,CompassPreference.class);
				startActivity(intent_pref);
				intent_pref.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				finish();
				break;
			case R.id.about:
	    		intent = new Intent(this,AboutMikuCompass.class);
	    		startActivity(intent);
	    		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	    		finish();
				break;
			case R.id.lightonoff:
				Log.i("アイコン押下時のisTorchOn",isTorchOn.toString());
				try {
					if (isTorchOn) {
						turnOffFlashLight();
						isTorchOn = false;
					} else {
						turnOnFlashLight();
						isTorchOn = true;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
//				if(mCamera == null){
//					toast =  new Toast(this);
//					toast.setView(makeToast(R.string.toast_text));
//					toast.setGravity(Gravity.CENTER, 0, 0); // 第一引数で中央表示、第二引数はx,第三引数はy
//					toast.setDuration(Toast.LENGTH_SHORT);
//					toast.show();
//				}
				Log.i("アイコン押下後のisTorchOn",isTorchOn.toString());
				break;
			default:
				break;
    	} // switch
		return super.onOptionsItemSelected(item);
	} // onOptionsItemSelected

	/** Toastの背景色と文字色を変えるためのメソッド **/
	private View makeToast(int toasterr){
		TextView text = new TextView(this);
		
		switch(prefInt){
		case 0:
			text.setBackgroundColor(ContextCompat.getColor(this,R.color.miku_7));
			text.setTextColor(ContextCompat.getColor(this,R.color.miku_3));
			break;
		case 1:
			text.setBackgroundColor(ContextCompat.getColor(this,R.color.gumi_6));
			text.setTextColor(ContextCompat.getColor(this,R.color.gumi_2));
			break;
		}

		text.setText(toasterr);
		text.setPadding(7, 7, 7, 7);
		return text;
	} // makeToast

}
