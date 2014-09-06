package com.letv.upnpControl.receiver;

import java.util.List;
import org.cybergarage.upnp.Device;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.letv.dmr.upnp.DMRService;
import com.letv.upnpControl.http.HttpUtil;
import com.letv.upnpControl.tools.LetvLog;

public class TvSendPhoneWordsReceiver extends BroadcastReceiver {
	public String TAG = getClass().getSimpleName();

	@Override
	public void onReceive(final Context context, final Intent intent) {
		// TODO Auto-generated method stub
		LetvLog.d(TAG, "TvSendPhoneWordsReceiver action= " + intent.getAction());
		if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")
				|| intent.getAction().equals(
						"android.intent.action.PACKAGE_REMOVED")
				|| intent.getAction().equals(
						"android.intent.action.PACKAGE_REPLACED")) {

			new Thread(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					try {
						if (Device.mPhoneServerIp.length() > 0) {
							JSONObject message = new JSONObject();
							message.put("intent_text", intent != null ? intent.toUri(0) : "");
							JSONObject js = new JSONObject();
							js.put("input_text",message.toString());
							js.put("device_id", Device.mUuid);
							// for ANDROID
							
							String url = "http://" + Device.mPhoneServerIp
									+ ":" + Device.mPhoneServerPort
									+ "/inputintent";
							LetvLog.d(TAG, "TvSendPhoneWordsReceiver url= " + url);
							HttpUtil.doPost(url, js.toString(), "utf-8");

						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}).start();
		} else if (intent.getAction().equals("com.letv.samescreen.input.send")) {
			final String sendJson = intent.getStringExtra("message");
			if (sendJson == null
					|| (sendJson != null && sendJson.length() <= 0)) {
				return;
			}

			boolean isRuning = isServiceRunning(context,
					"com.letv.dmr.upnp.DMRService");
			if (isRuning == true) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						try {
							if (DMRService.dmrDev != null
									&& Device.mPhoneServerIp.length() > 0) {
								JSONObject js = new JSONObject();
								js.put("input_text", sendJson);
								js.put("device_id", Device.mUuid);
								// for IOS
								DMRService.dmrDev.SendToIos(js.toString());
								// for ANDROID
								HttpUtil.doPost("http://"
										+ Device.mPhoneServerIp + ":"
										+ Device.mPhoneServerPort
										+ "/inputvalues", js.toString(),
										"utf-8");

							}
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}).start();
			}
		}
	}

	/**
	 * 用来判断服务是否运行.
	 * 
	 * @param context
	 * @param className
	 *            判断的服务名字
	 * @return true 在运行 false 不在运行
	 */
	private boolean isServiceRunning(Context mContext, String className) {
		boolean isRunning = false;
		ActivityManager activityManager = (ActivityManager) mContext
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo> serviceList = activityManager
				.getRunningServices(30);
		if (!(serviceList.size() > 0)) {
			return false;
		}
		for (int i = 0; i < serviceList.size(); i++) {
			if (serviceList.get(i).service.getClassName().equals(className) == true) {
				isRunning = true;
				break;
			}
		}
		return isRunning;
	}
}
