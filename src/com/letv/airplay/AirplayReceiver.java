package com.letv.airplay;

import com.letv.upnpControl.tools.LetvLog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 开机自启动类
 * @author 李振
 *
 */
public class AirplayReceiver extends BroadcastReceiver {
	static final String ACTION = "android.intent.action.BOOT_COMPLETED";
	private final String TAG = AirplayReceiver.class.getSimpleName();
	@Override
	public void onReceive(Context context, Intent intent) {
		
		LetvLog.d(TAG, "Airplay BroadcastReceiver : " + intent.getAction());
		
		if (intent.getAction().equals(ACTION)) {
			Intent i = new Intent(context, AirplayService.class);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
			i.setAction("startService");
			context.startService(i);
		}
	}
}
