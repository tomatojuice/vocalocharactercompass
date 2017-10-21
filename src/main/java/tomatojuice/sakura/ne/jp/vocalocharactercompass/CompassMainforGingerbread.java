package tomatojuice.sakura.ne.jp.vocalocharactercompass;

import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class CompassMainforGingerbread extends AppCompatActivity implements SensorEventListener{

	private SensorManager mSensorManager; // センサマネージャ
	private Sensor mAccelerometer , mMagneticField; // 加速度センサ、磁気センサ
	private List<Sensor> list;
	private boolean mValidMagneticFiled = false; // 磁気センサの更新判定
	private float[]	mAccelerometerValue = new float[3]; // 加速度センサの値
	private float[] mMagneticFieldValue = new float[3]; // 磁気センサの値
	private float[] rotate = new float[16]; // 傾斜(傾き)行列
	private float[] inclination = new float[16]; // 回転行列
	private float[] orientation = new float[3]; // 方位行列
	private Display disp;
	private DisplayMetrics metrics;
	private Intent aboutIntent;
	private int dispDir , iSensors;
	private SurfaceView surfaceview;
	private CompassView compassview;
	private Toolbar toolbar;
	private TextView textazimuth,textcompass;
	private Toast toast;
	private String numSensors;
	private Camera mCamera;
	private Camera.Parameters mParameters;
	private Boolean isTorchOn;

	public static String STR = new String();
	public static int WIDTH,HEIGHT,PREF_INT;
	SharedPreferences pref;

	public static int getWidth(){
		return  WIDTH;
	}

	public static int getHeight(){
		return HEIGHT;
	}

	public void setWidth(int width){
		WIDTH = width;
	}

	public void setHeight(int height){
		HEIGHT = height;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // 画面を常時点灯
		super.onCreate(savedInstanceState);

		// 画面サイズの取得
		metrics = new DisplayMetrics();
		disp = getWindowManager().getDefaultDisplay();
		disp.getMetrics(metrics);
		setWidth(metrics.widthPixels);
		setHeight(metrics.heightPixels);
		Log.i("onCreate、WIDTHの値", WIDTH+"です");
		Log.i("onCreate、HEIGHTの値", HEIGHT+"です");

		// センサーを取り出す
		mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

		list = mSensorManager.getSensorList(Sensor.TYPE_ALL ); // 端末で利用出来るセンサー一覧取得
		iSensors = 1; // センサー数を数える準備
		numSensors = ""; // センサー数の一時格納場所
		for(Sensor s :list){

			Log.i("センサーの名前",s.getName());
			numSensors = String.valueOf(iSensors);
			STR += numSensors + ":" + s.getName()+ "\n";

			iSensors ++;
		}

		Log.i("地磁気センサーの値",String.valueOf(mMagneticField));
		Log.i("加速度センサーの値",String.valueOf(mAccelerometer));

		setTheme();

		toolbar = (Toolbar)findViewById(R.id.toolbar);
		toolbar.setTitle("");
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		getSupportActionBar().setDisplayShowHomeEnabled(false);

		textazimuth = (TextView)findViewById(R.id.text_azimuth);
		textcompass = (TextView)findViewById(R.id.text_compass);

		isTorchOn = false;

		if(mMagneticField == null || mAccelerometer == null){
			toast =  new Toast(this);
			toast.setView(makeToast(R.string.toast_text_No_Senser));
			toast.setGravity(Gravity.CENTER, 0, 0); // 第一引数で中央表示、第二引数はx,第三引数はy
			toast.setDuration(Toast.LENGTH_LONG);
			toast.show();
		}

	} // onCreate

	private void setTheme(){
		pref = getSharedPreferences("pref", MODE_PRIVATE);
		PREF_INT = pref.getInt("key", 0);
		Log.i("PREF_INT", "PREF_INTの値 : "+PREF_INT);
		switch(PREF_INT){
			case 0:
				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.miku_9));
					getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.miku_9));
				}
					setTheme(R.style.MikuTheme);
					setContentView(R.layout.miku_main);
				break;
			case 1:
				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.gumi_4));
					getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.gumi_4));
				}
					setTheme(R.style.GumiTheme);
					setContentView(R.layout.gumi_main);
				break;
			default:
				break;
		} // switch

		surfaceview = (SurfaceView)findViewById(R.id.compassView);
		compassview = new CompassView(this,surfaceview);

	} // setTheme

	@Override
	protected void onPause() { // 一時停止
		super.onPause();
		Log.i("onPause", "onPauseの呼び出し");
		mSensorManager.unregisterListener(this); // センサー解除

		if(mCamera!=null){ // カメラが利用中なら開放する
			mCamera.release();
			mCamera = null;
		}

		if(isTorchOn){
			turnOffFlashLight();
		}
	}

	@Override
	protected void onResume() { // 再開時
		super.onResume();
		Log.i("onResume", "onResumeの呼び出し");
		// メーターと磁気センサーを再登録
		mSensorManager.registerListener(this,mAccelerometer,SensorManager.SENSOR_DELAY_UI);
		mSensorManager.registerListener(this,mMagneticField,SensorManager.SENSOR_DELAY_UI);

		// カメラオープン
            try {
                mCamera = Camera.open();
				mCamera.startPreview();
                Log.i("onResume", "CameraのOpenに成功");
            } catch (Exception e) {
				e.printStackTrace();
                Log.e("onResume", "CameraのOpenに失敗");
            }

		if(isTorchOn){
			turnOnFlashLight();
		}
	} // onResume

	@Override
	protected void onStop() { // 停止時
		super.onStop();
		mSensorManager.unregisterListener(this);
		if(isTorchOn){
			turnOffFlashLight();
		}
	}

	@Override
	protected void onDestroy() { // 破棄時
		super.onDestroy();
        Log.i("onDestroy()","onDestroy()が呼ばれました");
		mSensorManager.unregisterListener(this); // センサー解除
		CompassMainforGingerbread.STR = "";

        if(mCamera!=null){ // カメラが利用中なら開放する
            mCamera.release();
            mCamera = null;
        }

        if(isTorchOn){
            turnOffFlashLight();
        }

	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) { // センサーの精度変更
		// 今回は未使用
	}

	@Override
	public void onSensorChanged(SensorEvent event) { // センサーの値変更時の処理

		switch (event.sensor.getType()) { // センサー毎の処理
			case Sensor.TYPE_ACCELEROMETER:
				mAccelerometerValue = event.values.clone();
				break;
			case Sensor.TYPE_MAGNETIC_FIELD:
				mMagneticFieldValue = event.values.clone();
				mValidMagneticFiled = true;
				break;
		} // switch

		if (mValidMagneticFiled) { // 角度を出す準備
			// 回転行列を生成。 第1引数=「回転行列」、第2引数=「傾き」、第3引数=「加速度」、第4引数=「地磁気」
			SensorManager.getRotationMatrix(rotate, inclination,mAccelerometerValue,mMagneticFieldValue);
			getOrientation(rotate, orientation); // 方向を求める

			float degreeDir = (float)Math.toDegrees(orientation[0]); // デグリー角に変換
//			Log.i("onSensorChanged", "角度:" + degreeDir);

			if(degreeDir < 0){ // 180°以降は-1～-179の表示になるので、0～360°で方位角を出す
				textazimuth.setText(String.valueOf(360 + (int)degreeDir) + "°");
			}else{
				textazimuth.setText(String.valueOf((int)degreeDir) + "°");
			}

			// ここはfArrowの値によって東西南北の表示を行う
			if(((int)degreeDir >= 0 && (int)degreeDir <= 15) || ((int)degreeDir <= -1 && (int)degreeDir >= -15)){
				textcompass.setText(R.string.north); // 北
			}
			else if((int)degreeDir <= -16 && (int)degreeDir >= -75){
				textcompass.setText(R.string.north_west); // 北西
			}
			else if((int)degreeDir <= -76 && (int)degreeDir >= -105){
				textcompass.setText(R.string.west); // 西
			}
			else if((int)degreeDir <= -106 && (int)degreeDir >= -165){
				textcompass.setText(R.string.south_west); // 南西
			}
			else if(((int)degreeDir <= -166 && (int)degreeDir >= -179) || ((int)degreeDir >=166 && (int)degreeDir <= 179)){
				textcompass.setText(R.string.south); // 南
			}
			else if((int)degreeDir >= 106 && (int)degreeDir <= 165){
				textcompass.setText(R.string.south_east); // 南東
			}
			else if((int)degreeDir >= 76 && (int)degreeDir <= 105){
				textcompass.setText(R.string.east);// 東
			}
			else if((int)degreeDir >= 16 && (int)degreeDir <= 75){
				textcompass.setText(R.string.north_east); // 北東
			}

			CompassView.setArrowDir(degreeDir); // 方位磁針を回転描画
		} // if文ここまで
	} // onSensorChanged

	public void getOrientation(float[] rotate, float[] out) { // 画面回転中の方位取得メソッ

		float[] outR = new float[16];
		float[] outR2 = new float[16];

		dispDir = disp.getRotation(); // 自然な"方向からの画面の回転を返す。戻り値=ROTATION_0,90,180,270

		switch(dispDir){ // 回転の値によって処理
			case Surface.ROTATION_0:
				SensorManager.getOrientation(rotate, out);
				break;
			case Surface.ROTATION_90:
				SensorManager.remapCoordinateSystem(rotate, SensorManager.AXIS_Y,SensorManager.AXIS_MINUS_X, outR);
				break;
			case Surface.ROTATION_180:
				SensorManager.remapCoordinateSystem(rotate, SensorManager.AXIS_Y,SensorManager.AXIS_MINUS_X, outR2);
				SensorManager.remapCoordinateSystem(outR2, SensorManager.AXIS_Y,SensorManager.AXIS_MINUS_X, outR);
				break;
			case Surface.ROTATION_270:
				SensorManager.remapCoordinateSystem(outR, SensorManager.AXIS_MINUS_Y,SensorManager.AXIS_MINUS_X, outR);
				break;
		}

		if(dispDir != Surface.ROTATION_0){
			SensorManager.getOrientation(outR, out);
		}

	} // getOrientation


	private void turnOnFlashLight() {

		if(mCamera!=null){ //カメラが起動している場合
			mParameters = mCamera.getParameters();
			//[FLASH_MODE_TORCH)]常に点灯
			mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
			mCamera.setParameters(mParameters);
		}
	} // turnOnFlashLight()

	private void turnOffFlashLight() {

		if(mCamera!=null){ //カメラが起動している場合
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
				aboutIntent = new Intent(this,AboutMikuCompassforGingerbread.class);
				startActivity(aboutIntent);
				aboutIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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

		switch(PREF_INT){
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