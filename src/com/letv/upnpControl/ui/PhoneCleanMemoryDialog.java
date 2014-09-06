package com.letv.upnpControl.ui;

import java.util.Timer;
import java.util.TimerTask;
import org.cybergarage.util.Debug;
import com.letv.smartControl.R;
import com.letv.upnpControl.tools.LetvLog;
import com.letv.upnpControl.tools.PhoneCleanMemoryAppinfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.app.Activity;
import android.content.Intent;

public class PhoneCleanMemoryDialog extends Activity {
	/**
	 * youbin
	 */
	private boolean isFromPhone = false;
	private ProgressBar progressBar = null;
	private PhoneCleanMemoryAppinfo appmeminfo = null;
	private final static int WARNING = 8000; // 10000*80%
	private final static int NORMAL = 6000; // 10000*60%
	private final static int CLEAN_MSG = 1;
	private final static int CLEAN_OK = 2;
	private String TAG = PhoneCleanMemoryDialog.class.getSimpleName();
	private Button clean_ico = null;
	private TextView clean_state = null;
	private TextView clean_rate = null;
	private boolean flag = false;
	private int initLevel;
	private int initRadio;
	private static int STEP = 150;
	private Timer mTimer = null;
	private TimerTask mTimerTask = null;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.phone_clean_memory);
		Intent intent = getIntent();
		appmeminfo = new PhoneCleanMemoryAppinfo(getApplicationContext());
		isFromPhone = intent.getBooleanExtra("isFromPhone", false);
		initView();
	}

	private void initView() {

		progressBar = (ProgressBar) findViewById(R.id.progress_memory);
		clean_ico = (Button) findViewById(R.id.clean_ico);
		clean_state = (TextView) findViewById(R.id.clean_state);
		clean_rate = (TextView) findViewById(R.id.clean_rate);
		initRadio = 100 - (int) (appmeminfo.getRatio() * 100);
		initLevel = (int) ((1 - (appmeminfo.getRatio())) * 10000);
		clean_rate.setText(String.valueOf(initRadio) + "%");
		LetvLog.e(TAG, "initView initLevel=" + initLevel + "initRadio = "
				+ initRadio);
		showLevel(initLevel);
		progressBar.setProgress(initRadio);
		flag = false;
		startClean();

	}

	/**
	 * 开始清理
	 */
	private void startClean() {

		appmeminfo.cleanMemoryByKillThirdApp(isFromPhone);
		mTimer = new Timer();
		mTimerTask = new TimerTask() {
			public void run() {
				Message msg = new Message();
				msg.what = CLEAN_MSG;
				handler.sendMessage(msg);

			}
		};
		mTimer.schedule(mTimerTask, 0, 50);

	}

	private void showLevel(int level) {

		if (level >= WARNING) {
			// clean_state.setText(R.string.clean_high);
			progressBar.setIndeterminateDrawable(getResources().getDrawable(
					R.drawable.progressbar_red_ico));
			progressBar.setProgressDrawable(getResources().getDrawable(
					R.drawable.progressbar_red_ico));
		} else if (level >= NORMAL) {

			// clean_state.setText(R.string.clean_middle);
			progressBar.setIndeterminateDrawable(getResources().getDrawable(
					R.drawable.progressbar_orange_ico));
			progressBar.setProgressDrawable(getResources().getDrawable(
					R.drawable.progressbar_orange_ico));
		} else {
			progressBar.setIndeterminateDrawable(getResources().getDrawable(
					R.drawable.progressbar_green_ico));
			progressBar.setProgressDrawable(getResources().getDrawable(
					R.drawable.progressbar_green_ico));
			// clean_state.setText(R.string.clean_low);

		}

	}

	private void showSpeed(int initLevel) {
		float f = (float) ((float) initLevel / 10000);
		int speed = (int) (f * 100);
		progressBar.setProgress(speed);
		clean_rate.setText(String.valueOf(speed) + "%");

	}

	final Handler handler = new Handler() {

		public void handleMessage(Message msg) {
			if (msg.what == CLEAN_OK) {
				LetvLog.e(TAG, "clear done finish");
				finish();
			} else if (msg.what == CLEAN_MSG) {

				int currentLevel = (int) ((1 - appmeminfo.getRatio()) * 10000);

				synchronized (PhoneCleanMemoryDialog.class) {
					if (appmeminfo.finish) {
						currentLevel = (int) ((1 - appmeminfo.getRatio()) * 10000);

						LetvLog.e(TAG, "currentLevel= " + currentLevel);
						if (false == flag) {
							LetvLog.e(TAG, "initLevel= " + initLevel);
							if (initLevel > 0) {
								initLevel -= STEP;
								if (initLevel < 0)
									initLevel = 0;

							} else {
								flag = true;
							}

						} else {
							LetvLog.e(TAG, "initLevel2= " + initLevel);
							if (initLevel < currentLevel) {
								initLevel += STEP;
							} else {
								LetvLog.e(TAG, "clear done");
								// 完成清理处理
								flag = false;
								clean_state.setText(R.string.cleaned);
								clean_ico
										.setBackgroundResource(R.drawable.ic_playbar_ok);
								if (mTimer != null) {
									if (mTimerTask != null)
										mTimerTask.cancel();
									mTimer.cancel();
								}
								showLevel(currentLevel);
								showSpeed(currentLevel);
								new Thread(new MyThread()).start();
							}

						}
						showLevel(initLevel);
						showSpeed(initLevel);

					}
				}
			}

		}
	};

	public class MyThread implements Runnable {
		@Override
		public void run() {
			try {
				Thread.sleep(2000);
				Message message = new Message();
				message.what = CLEAN_OK;
				handler.sendMessage(message);

			} catch (Exception e) {
			}
		}
	}

	protected void onDestroy() {
		super.onDestroy();

		Debug.d(TAG, "onDestroy");
	}

	public void onResume() {
		super.onResume();
		Debug.d(TAG, "onResume");
	}
}
