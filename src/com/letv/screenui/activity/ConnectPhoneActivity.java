package com.letv.screenui.activity;

import java.io.FileInputStream;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import com.letv.screenui.activity.tools.Constants;
import com.letv.screenui.activity.tools.DownloadImageTask;
import com.letv.screenui.activity.tools.HttpUtil;
import com.letv.screenui.activity.tools.LetvUtils;
import com.letv.smartControl.R;
import com.letv.upnpControl.tools.LetvLog;

public class ConnectPhoneActivity extends Activity implements OnClickListener {
	private ImageView iv_iphone, iv_android;
	private String iphone_url = null;
	private String android_url = null;
	private String ANDROID_VERSION = Constants.A_VERSION;
	private String IPHONE_VERSION = Constants.I_VERSION;
	private Bitmap iphone_bm, android_bm;
	private String jsonString;
	private boolean flag = true;
	private static final int TIME = 5000;
	private static final int IPHONE = 1;
	private static final int ANDROID = 2;
	private static final int CONNECT = 3;
	private static final int SHOW = 4;
	private static final int INIT = 5;
	private WorkHandler mwHandler;
	private HandlerThread mwThread;
	private UIHandler mUiHandler;

	class UIHandler extends Handler {
		public UIHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case IPHONE:
				iv_iphone.setImageBitmap(iphone_bm);
				break;
			case ANDROID:
				iv_android.setImageBitmap(android_bm);
				break;
			case INIT:
				initOperate();
				break;
			default:
				break;
			}
		}

	}

	public String getApkqrpicUrl() {
		String apkqrpicUrl = "";
		String host_addr = SystemProperties.get(Constants.APIADDRGROP);
		if (host_addr == null || "".equals(host_addr)) {
			host_addr = Constants.APIADDRDEFAULT;
		}
		apkqrpicUrl = "http://" + host_addr + "/iptv/api/apk/getapkqrpic";
		return apkqrpicUrl;
	}

	class WorkHandler extends Handler {
		public WorkHandler(Looper looper) {
			super(looper);
		}

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case CONNECT:
				String mac = LetvUtils.getMac();

				String host_addr = SystemProperties.get(Constants.APIADDRGROP);
				if (host_addr == null || "".equals(host_addr)) {
					host_addr = Constants.APIADDRDEFAULT;
				}
				String apkqrpicUrl = getApkqrpicUrl();
				jsonString = HttpUtil.doGet(apkqrpicUrl + "?appkey="
						+ Constants.APPKEY + "&mac=" + mac, Constants.CHARSET,
						TIME);
				if (jsonString != null) {
					parseJson(jsonString);
				}
				break;
			case SHOW:
				try {
					FileInputStream iphone_in = ConnectPhoneActivity.this
							.openFileInput("iphone.jpg");
					FileInputStream android_in = ConnectPhoneActivity.this
							.openFileInput("android.jpg");
					iphone_bm = BitmapFactory.decodeStream(iphone_in);
					android_bm = BitmapFactory.decodeStream(android_in);
					mUiHandler.sendEmptyMessage(IPHONE);
					mUiHandler.sendEmptyMessage(ANDROID);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			default:
				break;
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.connect_activity_main);
		iv_iphone = (ImageView) findViewById(R.id.iv_iphone);
		iv_android = (ImageView) findViewById(R.id.iv_android);
		mwThread = new HandlerThread("LetvQrcode_WorkThread");
		mwThread.start();
		mUiHandler = new UIHandler(getMainLooper());
		mwHandler = new WorkHandler(mwThread.getLooper());
		mwHandler.sendEmptyMessage(CONNECT);
		mwHandler.sendEmptyMessage(SHOW);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.yh_back:
			finish();
			break;
		default:
			break;
		}
	}

	private void initOperate() {
		DownloadImageTask dit_iphone = new DownloadImageTask(iv_iphone,
				iphone_url, this, "iphone");
		dit_iphone.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,iphone_url);
		DownloadImageTask dit_android = new DownloadImageTask(iv_android,
				android_url, this, "android");
		dit_android.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,android_url);
	}

	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		finish();

	}

	@Override
	protected void onDestroy() {
		if (mwHandler != null) {
			mwHandler.removeMessages(CONNECT);
			mwHandler.removeMessages(SHOW);
			mwHandler = null;
			if (mwThread != null) {
				mwThread.getLooper().quit();
				try {
					mwThread.join(500);
				} catch (Exception e) {
					e.printStackTrace();
				}
				mwThread = null;
			}
		}
		if (mUiHandler != null) {
			mUiHandler.removeMessages(IPHONE);
			mUiHandler.removeMessages(ANDROID);
		}
		super.onDestroy();
	}

	private void parseJson(String str) {
		JSONObject jsonObject = null;
		JSONArray jsonArray = null;
		try {
			jsonObject = new JSONObject(str);
			jsonArray = jsonObject.getJSONArray("appQRs");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject json = (JSONObject) jsonArray.opt(i);
			try {
				if ("Android".equals(json.getString("title"))) {
					android_url = json.getString("picUrl");
					if (!ANDROID_VERSION.equals(json.getString("picTime"))) {
						Constants.A_VERSION = json.getString("picTime");
						flag = true;
					}
					LetvLog.d("LPF", "--" + android_url + "--");
				} else {
					iphone_url = json.getString("picUrl");
					if (!IPHONE_VERSION.equals(json.getString("picTime"))) {
						Constants.I_VERSION = json.getString("picTime");
						flag = true;
					}
					LetvLog.d("LPF", "--iphone:-" + iphone_url + "--");
				}
				
				LetvLog.d("LPF", "--android:" + Constants.A_VERSION + "--");
				LetvLog.d("LPF", "--iphone:" + Constants.I_VERSION + "--");
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}
		if (flag) {
			mUiHandler.sendEmptyMessage(INIT);
		}
	}

	// private void dealTextView(int phoneNums) {
	// String text = String.format(
	// getResources().getString(R.string.mutils_connectnum_tip),
	// phoneNums);
	//
	// SpannableStringBuilder style = new SpannableStringBuilder(text);
	// style.setSpan(new ForegroundColorSpan(Color.rgb(38, 153, 255)), 2, 3,
	// Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
	// connect_tips.setText(style);
	// }
}
