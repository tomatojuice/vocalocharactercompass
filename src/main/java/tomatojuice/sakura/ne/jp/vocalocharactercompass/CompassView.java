package tomatojuice.sakura.ne.jp.vocalocharactercompass;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CompassView extends SurfaceView implements SurfaceHolder.Callback , Runnable {

	private SurfaceHolder mSurfaceHolder; // ホルダー
	private Thread mMainLoop; // スレッド
	private final Resources aRes = this.getContext().getResources();
	private Bitmap arrowBitmap;
	private Paint pArrow, pCircle, pCircleStroke;
	private static float fArrow; // 矢印の方向
	private int[] scCenter = { 0, 0 };
	private int aWidth, aHeight, dWidth, dHeight;
	private String scom[] = new String[8];
	private Canvas dCanvas , rCanvas;
	private Context cContext;
 
    public CompassView(Context context, SurfaceView surfaceview) {
    	super(context);
    	setConst();
		cContext = context;
		mSurfaceHolder = surfaceview.getHolder(); // ホルダーの取得
		mSurfaceHolder.addCallback(this); // コールバックを登録
    } // コンストラクタ

    public void setConst(){ // コンストラクタ内で実行

//		dWidth = CompassMain.getWidth() / 2; // 未使用
//		dHeight = CompassMain.getHeight() / 2; // 未使用
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){ // Android 6.0以上
			dWidth = CompassMain.getWidth();
			dHeight = CompassMain.getHeight();
		}
		else{
			dWidth = CompassMainforGingerbread.getWidth();
			dHeight = CompassMainforGingerbread.getHeight();
		}

		int bitmapsize = (int) (dWidth*0.9/900*900); // コンパスの画像を画面の約90パーセントの値を代入
		Log.i("dWidth","dWidthの値：　" + dWidth);
		Log.i("bitmapsize","bitmapsizeの値：　" + bitmapsize);

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // Android 6.0以上
			switch (CompassMain.PREF_INT) {
				case 0: // Miku
					arrowBitmap = BitmapFactory.decodeResource(aRes, R.drawable.miku_arrow);
					arrowBitmap = Bitmap.createScaledBitmap(arrowBitmap, bitmapsize, bitmapsize, false);
					break;
				case 1: // Gumi
					arrowBitmap = BitmapFactory.decodeResource(aRes, R.drawable.gumi_arrow);
					arrowBitmap = Bitmap.createScaledBitmap(arrowBitmap, bitmapsize, bitmapsize, false);
					break;
				default:
					break;
			}
		} // if
		else{
			switch (CompassMainforGingerbread.PREF_INT) {
				case 0: // Miku
					arrowBitmap = BitmapFactory.decodeResource(aRes, R.drawable.miku_arrow);
					arrowBitmap = Bitmap.createScaledBitmap(arrowBitmap, bitmapsize, bitmapsize, false);
					break;
				case 1: // Gumi
					arrowBitmap = BitmapFactory.decodeResource(aRes, R.drawable.gumi_arrow);
					arrowBitmap = Bitmap.createScaledBitmap(arrowBitmap, bitmapsize, bitmapsize, false);
					break;
				default:
					break;
			}
		} // else

		pArrow = new Paint();
		pCircle = new Paint();
		pCircleStroke = new Paint();

		// 方位の文字をセット
		scom[0] = getResources().getString(R.string.north);
		scom[1] = getResources().getString(R.string.north_east);
		scom[2] = getResources().getString(R.string.east);
		scom[3] = getResources().getString(R.string.south_east);
		scom[4] = getResources().getString(R.string.south);
		scom[5] = getResources().getString(R.string.south_west);
		scom[6] = getResources().getString(R.string.west);
		scom[7] = getResources().getString(R.string.north_west);
//		Log.i("コンストラクタ内のdWidthの値", dWidth +"です");
//		Log.i("コンストラクタ内のdHeightの値", dHeight +"です");

    } // setConst()

	public static void setArrowDir(float dir) { // 角度のセット
		fArrow = -dir;
//		Log.i("fArrowの値", fArrow +"です");
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) { // サーフェイス作成時に呼ばれる
		mMainLoop = new Thread(this);
		mMainLoop.start();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) { // サーフェイスに変更があった際に呼ばれる
		scCenter[0] = width / 2;
		scCenter[1] = height / 2;
		aHeight = arrowBitmap.getHeight();
		aWidth = arrowBitmap.getWidth();
		Log.i("コンストラクタ内のaWidthの値", aWidth +"です");
		Log.i("コンストラクタ内のaHeightの値", aHeight +"です");
		Log.i("サーフィスチェンジのscCenter[0]", Integer.toString(scCenter[0]));
		Log.i("サーフィスチェンジのscCenter[1]", Integer.toString(scCenter[1]));
	}


	@Override
	public void surfaceDestroyed(SurfaceHolder holder) { // サーフェイス破棄時に呼ばれる
		mMainLoop = null;
	}

	public void onDraw() {
		dCanvas = mSurfaceHolder.lockCanvas();
		draw(dCanvas);
		mSurfaceHolder.unlockCanvasAndPost(dCanvas);
	}

	@Override
	public void draw(final Canvas canvas) { // 方位磁針と羅針盤を表示
		canvas.save();

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){ // Android 6.0以上
			switch(CompassMain.PREF_INT){
				case 0:
					// Miku
					pCircle.setColor(ContextCompat.getColor(cContext,R.color.miku_8));
					pCircle.setAntiAlias(true);
					pCircleStroke.setColor(ContextCompat.getColor(cContext,R.color.miku_10));
					pCircleStroke.setAntiAlias(true);
					pCircleStroke.setStyle(Paint.Style.STROKE);
//			pCircleStroke.setPathEffect(new DashPathEffect(new float[]{10.0f,10.0f}, 0));
					pCircleStroke.setStrokeWidth(5);
					canvas.drawCircle(scCenter[0], scCenter[1],(float) (scCenter[0]/1.1) , pCircle);
					canvas.drawCircle(scCenter[0], scCenter[1],(float) (scCenter[0]/1.1) , pCircleStroke);
					canvas.rotate(fArrow, scCenter[0], scCenter[1]);
					canvas.drawBitmap(arrowBitmap, scCenter[0] -aWidth/2, scCenter[1]-aHeight/2, pArrow);
					break;
				case 1:
					// Gumi
					pCircle.setColor(ContextCompat.getColor(cContext,R.color.gumi_5));
					pCircle.setAntiAlias(true);
					pCircleStroke.setColor(ContextCompat.getColor(cContext,R.color.gumi_7));
					pCircleStroke.setAntiAlias(true);
					pCircleStroke.setStyle(Paint.Style.STROKE);
//			pCircleStroke.setPathEffect(new DashPathEffect(new float[]{10.0f,10.0f}, 0));
					pCircleStroke.setStrokeWidth(5);
					canvas.drawCircle(scCenter[0], scCenter[1],(float) (scCenter[0]/1.1) , pCircle);
					canvas.drawCircle(scCenter[0], scCenter[1],(float) (scCenter[0]/1.1) , pCircleStroke);
					canvas.rotate(fArrow, scCenter[0], scCenter[1]);
					canvas.drawBitmap(arrowBitmap, scCenter[0] -aWidth/2, scCenter[1]-aHeight/2, pArrow);
					break;
				default:
					super.draw(canvas);
					break;
			}
		} // if
		else{
			switch(CompassMainforGingerbread.PREF_INT){
				case 0:
					// Miku
					pCircle.setColor(ContextCompat.getColor(cContext,R.color.miku_8));
					pCircle.setAntiAlias(true);
					pCircleStroke.setColor(ContextCompat.getColor(cContext,R.color.miku_10));
					pCircleStroke.setAntiAlias(true);
					pCircleStroke.setStyle(Paint.Style.STROKE);
//			pCircleStroke.setPathEffect(new DashPathEffect(new float[]{10.0f,10.0f}, 0));
					pCircleStroke.setStrokeWidth(5);
					canvas.drawCircle(scCenter[0], scCenter[1],(float) (scCenter[0]/1.1) , pCircle);
					canvas.drawCircle(scCenter[0], scCenter[1],(float) (scCenter[0]/1.1) , pCircleStroke);
					canvas.rotate(fArrow, scCenter[0], scCenter[1]);
					canvas.drawBitmap(arrowBitmap, scCenter[0] -aWidth/2, scCenter[1]-aHeight/2, pArrow);
					break;
				case 1:
					// Gumi
					pCircle.setColor(ContextCompat.getColor(cContext,R.color.gumi_5));
					pCircle.setAntiAlias(true);
					pCircleStroke.setColor(ContextCompat.getColor(cContext,R.color.gumi_7));
					pCircleStroke.setAntiAlias(true);
					pCircleStroke.setStyle(Paint.Style.STROKE);
//			pCircleStroke.setPathEffect(new DashPathEffect(new float[]{10.0f,10.0f}, 0));
					pCircleStroke.setStrokeWidth(5);
					canvas.drawCircle(scCenter[0], scCenter[1],(float) (scCenter[0]/1.1) , pCircle);
					canvas.drawCircle(scCenter[0], scCenter[1],(float) (scCenter[0]/1.1) , pCircleStroke);
					canvas.rotate(fArrow, scCenter[0], scCenter[1]);
					canvas.drawBitmap(arrowBitmap, scCenter[0] -aWidth/2, scCenter[1]-aHeight/2, pArrow);
					break;
				default:
					super.draw(canvas);
					break;
			}
		} // else

		canvas.restore();
	} // Draw

	@Override
	public void run() { // 常に実行されるメソッド

		while (this.mMainLoop != null) {
			rCanvas = null;
			try {
				synchronized (mSurfaceHolder) {
					rCanvas = mSurfaceHolder.lockCanvas();
					if (rCanvas == null){ // canvasがnullの時はエラーになるのでcontinueでループ終了
						continue;
					} // if

					if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // Android 6.0以上
						switch(CompassMain.PREF_INT){
							case 0: // Miku
								rCanvas.drawColor(ContextCompat.getColor(cContext,R.color.miku_5));
								break;
							case 1: // Gumi
								rCanvas.drawColor(ContextCompat.getColor(cContext,R.color.gumi_3));
								break;
							default:
								break;
						} // switchここまで
					} // if
					else{
						switch(CompassMainforGingerbread.PREF_INT){
							case 0: // Miku
								rCanvas.drawColor(ContextCompat.getColor(cContext,R.color.miku_5));
								break;
							case 1: // Gumi
								rCanvas.drawColor(ContextCompat.getColor(cContext,R.color.gumi_3));
								break;
							default:
								break;
						} // switchここまで
					}

					rCanvas.save();
					draw(rCanvas);
					rCanvas.restore();
				} // synchronizedここまで
			}
			catch(Exception e){
				// エラー処理
			}
			finally {
				if (rCanvas != null) { // キャンバスの解放
					mSurfaceHolder.unlockCanvasAndPost(rCanvas);
				}
			}
		} // while
	} // run

}
