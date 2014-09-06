package com.letv.dmr;

import java.util.Timer;
import java.util.TimerTask;
import org.cybergarage.util.Debug;
import com.letv.dmr.asynctask.DownloadImageTask;
import com.letv.dmr.asynctask.NavigateLocalImageTask;
import com.letv.smartControl.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;

/**
 * 
 * @title Picture-show
 * 
 */
@SuppressLint("ShowToast")
public class PictureShowActivity extends Activity implements AnimationListener {
	private static final String TAG = "PictureShowActivity";

	public static int URL_LOCAL = 0;

	public static int URL_HTTP = 1;

	public static final int SHOW_LOADING = 2;

	public static final int SHOW_IMAGE = 3;

	public static final int SHOW_IMAGE_DISMISS = 4;

	public static final int SHOW_LOADING_TWO = 5;

	public static final int SHOW_IMAGE_TWO = 6;

	public static final int SHOW_IMAGE_DISMISS_TEO = 7;

	public static final int STOP = 2;

	private ImageView iv;

	public boolean isStopped = true;

	private DownloadImageTask downurlphoto = null;

	private ImageFromUrlReceiver imageFromUrlReceiver = null;

	private RelativeLayout image_loading, loadbar_center, main_bc = null;

	public UIhandler mUiHandler = null;

	private PowerManager.WakeLock mWakeLock = null;

	private Animation myAnimationTranslate1;

	private Animation mScaleAnimation;

	private ImageView paly_btn_move;

	private ProgressBar loadingProgressBar;

	private RelativeLayout play_start;

	private Animation myAnimationScale_big;

	private Animation mAnimation;

	private AlphaAnimation animation;

	private TextView mPlay_loading_title;

	private TextView mPic_loading_title;

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Debug.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_picture);
		initview();
		MediaplayerBase.getInstance().setPictureShow(this);
		mUiHandler = new UIhandler();
		initoperate(false);
		imageFromUrlReceiver = new ImageFromUrlReceiver();
		IntentFilter localIntentFilter = new IntentFilter();
		localIntentFilter.addAction("com.letv.UPNP_STOP_ACTION");
		localIntentFilter.addAction("com.letv.accountLogin.receiveImage");

		registerReceiver(this.imageFromUrlReceiver, localIntentFilter);
		isStopped = false;
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
				| PowerManager.ON_AFTER_RELEASE,
				PictureShowActivity.class.getName());
		mWakeLock.setReferenceCounted(false);
		mWakeLock.acquire();
	}

	public void initview() {

		main_bc = (RelativeLayout) findViewById(R.id.main_bc);
		iv = (ImageView) findViewById(R.id.imageView);
		paly_btn_move = (ImageView) findViewById(R.id.paly_btn_picture);
		play_start = (RelativeLayout) findViewById(R.id.play_start);
		loadingProgressBar = (ProgressBar) findViewById(R.id.loadingProgressBar);
		image_loading = (RelativeLayout) findViewById(R.id.play_now_loading);
		mPlay_loading_title = (TextView) findViewById(R.id.play_loading_title);
		loadbar_center = (RelativeLayout) findViewById(R.id.loadbar_center);
		mPic_loading_title = (TextView) findViewById(R.id.pic_loading_title);

		mScaleAnimation = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		myAnimationTranslate1 = AnimationUtils.loadAnimation(this,
				R.anim.translate_top);
		AnimationUtils.loadAnimation(this, R.anim.scalelitte);
		myAnimationScale_big = AnimationUtils.loadAnimation(this,
				R.anim.scalelitte_big);
		mAnimation = AnimationUtils.loadAnimation(this, R.anim.alpha);
		animation = new AlphaAnimation(1, 0);
		animation.setDuration(2000);// 设置动画持续时间
		animation.setFillAfter(true);
		animation.setAnimationListener(this);
		myAnimationTranslate1.setAnimationListener(this);
		mScaleAnimation.setAnimationListener(this);
		mAnimation.setAnimationListener(this);
		mScaleAnimation.setDuration(2000);
		mScaleAnimation.setFillAfter(true);

	}

	@SuppressLint("HandlerLeak")
	public class UIhandler extends Handler {

		public void handleMessage(Message msg) {

			switch (msg.what) {
			case SHOW_LOADING:
				loadingProgressBar.setVisibility(View.VISIBLE);
				play_start.setVisibility(View.INVISIBLE);
				image_loading.setVisibility(View.VISIBLE);
				iv.setImageDrawable(null);
				break;
			case SHOW_IMAGE:
				image_loading.startAnimation(mAnimation);
				play_start.setVisibility(View.VISIBLE);
				AnimationSet animationSet = new AnimationSet(true);
				animationSet.addAnimation(myAnimationScale_big);
				animationSet.addAnimation(myAnimationTranslate1);
				paly_btn_move.startAnimation(animationSet);
				mPlay_loading_title.setText(getString(R.string.pic_loading_txt));
				break;
			case SHOW_IMAGE_DISMISS:

				loadingProgressBar.setVisibility(View.INVISIBLE);

				break;
			case SHOW_LOADING_TWO:
				image_loading.setVisibility(View.INVISIBLE);
				loadbar_center.setVisibility(View.VISIBLE);

				break;
			case SHOW_IMAGE_TWO:

				loadbar_center.setVisibility(View.INVISIBLE);
				mPic_loading_title.setText(getString(R.string.pic_loading_txt));
				iv.startAnimation(mScaleAnimation);

				if (downurlphoto.bitmap != null) {
					main_bc.setBackgroundColor(Color.BLACK);
					iv.setImageBitmap(downurlphoto.bitmap);

				}

				break;
			default:
				break;
			}

		}
	}

	public void initoperate(Boolean isContinue) {

		if (mUiHandler != null
				&& MediaplayerBase.gDlnaPictureUrlType == URL_HTTP) {
			if (isContinue == false) {
				mUiHandler.sendEmptyMessage(SHOW_LOADING);
			} else {
				mUiHandler.sendEmptyMessage(SHOW_LOADING_TWO);
			}
		}

		if (MediaplayerBase.gDlnaPictureUrlType == URL_LOCAL) {
			play_start.setVisibility(View.INVISIBLE);
			iv.setVisibility(View.VISIBLE);
		}
		if (downurlphoto != null && !downurlphoto.isCancelled()) {
			downurlphoto.cancel(true);
			if (downurlphoto.conn != null)
				downurlphoto.conn.disconnect();
		}

		if (MediaplayerBase.gDlnaPictureUrlType == URL_HTTP) {
			
			if (isContinue) {
				downurlphoto = new DownloadImageTask(isContinue, iv,
						image_loading, mPic_loading_title,
						MediaplayerBase.gDlnaPictureShowURL,
						PictureShowActivity.this);
			} else {
				downurlphoto = new DownloadImageTask(isContinue, iv,
						image_loading, mPlay_loading_title,
						MediaplayerBase.gDlnaPictureShowURL,
						PictureShowActivity.this);
			}
			downurlphoto.execute(MediaplayerBase.gDlnaPictureShowURL);

		} else if (MediaplayerBase.gDlnaPictureUrlType == URL_LOCAL) {
			NavigateLocalImageTask navigatelocalphoto = new NavigateLocalImageTask(
					MediaplayerBase.gDlnaPictureShowURL, iv, loadbar_center,
					main_bc, PictureShowActivity.this);
			navigatelocalphoto.execute(MediaplayerBase.gDlnaPictureShowURL);

		}

	}

	class ImageFromUrlReceiver extends BroadcastReceiver {
		ImageFromUrlReceiver() {

		}

		public void onReceive(Context paramContext, Intent paramIntent) {
			String str = paramIntent.getAction();

			if (!"image/*".equals(paramIntent.getStringExtra("media_type")))
				return;
			if (str.equals("com.letv.UPNP_STOP_ACTION")) {
				Debug.d(TAG, "UPNP_STOP_ACTION");
				if (downurlphoto != null && !downurlphoto.isCancelled()) {
					downurlphoto.cancel(true);
				}

				MediaplayerBase.getInstance().stopPictureShow();
			}
			if (str.equals("com.letv.accountLogin.receiveImage")) {
				Debug.d(TAG, "com.letv.accountLogin.receiveImage");

				mUiHandler.sendEmptyMessage(SHOW_LOADING_TWO);
			}
		}
	}

	protected void onNewIntent(Intent intent) {
		Debug.d(TAG, "onNewIntent" + intent);

	}

	public void onPause() {
		Debug.d(TAG, "onPause");
		super.onPause();
		if (mWakeLock != null) {
			mWakeLock.release();
			mWakeLock = null;
		}
		MediaplayerBase.getInstance().stopPictureShow();
	}

	@Override
	protected void onStop() {
		Debug.d(TAG, "onStop begin");
		if (downurlphoto != null) {
			downurlphoto.cancel(true);
			if (downurlphoto.conn != null)
				downurlphoto.conn.disconnect();
		}
		if (downurlphoto != null && downurlphoto.bitmap != null
				&& !downurlphoto.bitmap.isRecycled()) {
			Debug.d(TAG, "bitmap do recycle");
			downurlphoto.bitmap.recycle();
			downurlphoto.bitmap = null;
		}
		super.onStop();
		Debug.d(TAG, "onStop end");
	}

	protected void onDestroy() {
		super.onDestroy();
		isStopped = true;
		if (imageFromUrlReceiver != null) {
			unregisterReceiver(imageFromUrlReceiver);
			imageFromUrlReceiver = null;
		}
		System.gc();
		Debug.d(TAG, "onDestroy");
	}

	@Override
	public void onAnimationEnd(Animation arg0) {
		// TODO Auto-generated method stub
		if (arg0.hashCode() == myAnimationTranslate1.hashCode()) {

			iv.startAnimation(mScaleAnimation);

			play_start.startAnimation(animation);

			if (downurlphoto.bitmap != null) {
				main_bc.setBackgroundColor(Color.BLACK);
				iv.setImageBitmap(downurlphoto.bitmap);

			}

		}

	}

	@Override
	public void onAnimationRepeat(Animation arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAnimationStart(Animation arg0) {
		// TODO Auto-generated method stub
		if (arg0.hashCode() == myAnimationTranslate1.hashCode()) {

			Timer mTimer = new Timer();
			mTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					mUiHandler.sendEmptyMessage(SHOW_IMAGE_DISMISS);

				}
			}, 600, 600);
			iv.clearColorFilter();
		}
		if (arg0.hashCode() == mScaleAnimation.hashCode()) {

			paly_btn_move.setVisibility(View.VISIBLE);

		}
	}
}
