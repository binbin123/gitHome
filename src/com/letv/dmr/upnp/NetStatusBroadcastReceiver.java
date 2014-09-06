package com.letv.dmr.upnp;

import java.util.Timer;
import java.util.TimerTask;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.SystemProperties;
import android.widget.Toast;
import com.letv.dmr.upnp.DMRService;
import org.cybergarage.util.Debug;
import com.letv.dmr.utils.IfcUtil;
import com.letv.smartControl.R;
import com.letv.statistics.LogPostService;
import com.letv.upnpControl.service.BackgroundService;
import com.letv.upnpControl.service.ListenNetWorkService;
import com.letv.upnpControl.tools.AccountUtils;
import com.letv.upnpControl.tools.Constants;
import com.letv.upnpControl.tools.LetvLog;
import com.letv.upnpControl.tools.LetvUtils;

public class NetStatusBroadcastReceiver extends BroadcastReceiver {
	private static String mIp = "";
	private static int mNetType = -1;
	private static String mWifiSSID = "";
	private Timer mTimer = null;
	private static TimerTask mTimerTask = null;
	private static Toast mToast = null;
	private Context mcontext;
	public static final String TAG = "KanKanNetStatusBroadcastReceiver";

	public void onReceive(Context paramContext, Intent paramIntent) {
		Debug.d(TAG, ">>>>>onReceive :" + paramIntent.getAction());

		mcontext = paramContext;

		if (ListenNetWorkService.ACTION_LOGIN_EVENT.equals(paramIntent
				.getAction())) {

			if (ListenNetWorkService.mHandler != null
					&& ListenNetWorkService.mThread != null
					&& ListenNetWorkService.mThread.isAlive()) {
				Debug.d("LPF", "account log in");
				ListenNetWorkService.sendOnline();
			}
		} else if (ListenNetWorkService.ACTION_LOGOUT_EVENT.equals(paramIntent
				.getAction())) {
			if (ListenNetWorkService.mHandler != null
					&& ListenNetWorkService.mThread != null
					&& ListenNetWorkService.mThread.isAlive()) {
				Debug.d("LPF", "account log out");
				ListenNetWorkService.sendOffline();
			}
		} else if ("android.net.conn.CONNECTIVITY_CHANGE".equals(paramIntent
				.getAction())
				|| "android.net.ethernet.ETH_STATE_CHANGED".equals(paramIntent
						.getAction())) {

			ConnectivityManager localConnectivityManager = (ConnectivityManager) paramContext
					.getSystemService("connectivity");
			NetworkInfo localNetworkInfo = localConnectivityManager
					.getActiveNetworkInfo();
			if (localNetworkInfo == null)
				return;

			int netType = localNetworkInfo.getType();
			String ip = IfcUtil.getIpAddress(paramContext);
			// LetvUtils.saveFileToSDCar("kankan_log",
			// "ip =" + ip + "\n");
			Debug.d(TAG, ip + "net type =" + netType);
			String wifiSSID = "";
			if (localNetworkInfo.getTypeName().equalsIgnoreCase("WIFI")) {
				WifiManager localWifiManager = (WifiManager) paramContext
						.getSystemService(Context.WIFI_SERVICE);
				wifiSSID = localWifiManager.getConnectionInfo().getSSID();
			}

			if ((localConnectivityManager.getNetworkInfo(1).getDetailedState() == NetworkInfo.DetailedState.DISCONNECTED)
					&& (localConnectivityManager.getNetworkInfo(9)
							.getDetailedState() == NetworkInfo.DetailedState.DISCONNECTED)) {
				paramContext.stopService(new Intent(paramContext,
						DMRService.class));
				Debug.d(TAG, ">>>>>onReceive :network disconnected");
				setNetInfo(-1, "", "");
			}
			if ((localNetworkInfo != null)
					&& (localNetworkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED)) {
				Debug.d(TAG, ">>>>>onReceive :network CONNECTED");
				Debug.d("NetStatusBroadcastReceiver ", ">>>>> mNetType ="
						+ mNetType + "mIp = " + mIp);
				if (netType != mNetType || !ip.equals(mIp)) {
					Debug.d(TAG, ">>>>>start dmr service");
					paramContext.stopService(new Intent(paramContext,
							DMRService.class));
					paramContext.startService(new Intent(paramContext,
							DMRService.class));

					/*
					 * if(localNetworkInfo.getTypeName().equalsIgnoreCase("WIFI")
					 * ){ WifiManager localWifiManager =
					 * (WifiManager)paramContext
					 * .getSystemService(Context.WIFI_SERVICE); wifiSSID =
					 * localWifiManager.getConnectionInfo().getSSID(); }
					 */

					LetvLog.d(TAG,
							"netWork connected ok, start threeScreen service.");
					if (ListenNetWorkService.mHandler != null
							&& ListenNetWorkService.mThread != null
							&& ListenNetWorkService.mThread.isAlive()
							&& AccountUtils.isLetvAccountLogin(paramContext)) {
						ListenNetWorkService.sendOnline();
					}
					Intent intentL = new Intent();
					// 鐠佸墽鐤咰lass鐏炵偞鈧拷
					intentL.setClass(paramContext, BackgroundService.class);
					// 閸氼垰濮╃拠顧檈rvice
					paramContext.startService(intentL);

				} else if (localNetworkInfo.getTypeName().equalsIgnoreCase(
						"WIFI")) {
					if (wifiSSID != null && !wifiSSID.equals(this.mWifiSSID)) {
						Debug.d(TAG, ">>>>>start dmr service by wifi network");
						paramContext.stopService(new Intent(paramContext,
								DMRService.class));
						paramContext.startService(new Intent(paramContext,
								DMRService.class));

						LetvLog.d(TAG,
								"netWork connected ok, start threeScreen service.");
						if (ListenNetWorkService.mHandler != null
								&& ListenNetWorkService.mThread != null
								&& ListenNetWorkService.mThread.isAlive()
								&& AccountUtils
										.isLetvAccountLogin(paramContext)) {
							ListenNetWorkService.sendOnline();
						}
						Intent intentL = new Intent();
						intentL.setClass(paramContext, BackgroundService.class);
						paramContext.startService(intentL);

					}
				}
				setNetInfo(netType, ip, wifiSSID);
			}
		} else if (("android.net.wifi.WIFI_AP_STATE_CHANGED".equals(paramIntent
				.getAction()))
				&& (WifiManager.WIFI_AP_STATE_ENABLED == paramIntent
						.getIntExtra("wifi_state",
								WifiManager.WIFI_AP_STATE_FAILED))) {
			Debug.d(TAG, ">>>>>onReceive :WIFI_AP_STATE_CHANGED");
			paramContext
					.stopService(new Intent(paramContext, DMRService.class));
			paramContext
					.startService(new Intent(paramContext, DMRService.class));
		} else if ("com.letv.dmr.service.PUSH_NET_VIDEO".equals(paramIntent
				.getAction())) {
			Debug.d(TAG, ">>>>>com.letv.dmr.service.PUSH_NET_VIDEO");
			String url = paramIntent.getStringExtra("media_uri");
			Intent videoIntent = new Intent("com.letv.UPNP_PLAY_ACTION");
			videoIntent.putExtra("media_uri", url);
			videoIntent.putExtra("media_type", "video/*");
			if (DMRService.dmrDev != null) {
				DMRService.dmrDev.notifyDMR(videoIntent);
			}
		} else if ("com.letv.dmr.service.PUSH_LOCAL_IMAGE".equals(paramIntent
				.getAction())) {
			Debug.d(TAG, ">>>>>com.letv.dmr.service.PUSH_LOCAL_IMAGE");
			String url = paramIntent.getStringExtra("media_uri");
			Intent imageIntent = new Intent("com.letv.UPNP_PLAY_ACTION");
			imageIntent.putExtra("media_uri", url);
			imageIntent.putExtra("media_type", "image/*");
			imageIntent.putExtra("download_type", 0);

			if (DMRService.dmrDev != null) {
				DMRService.dmrDev.notifyDMR(imageIntent);
			}
		} else if ("android.intent.action.BOOT_COMPLETED".equals(paramIntent
				.getAction())) {
			LetvLog.d(TAG, "receive BOOT_COMPLETED event");

			if (mTimer == null) {
				mTimer = new Timer();
			}

			if (mTimerTask == null) {
				mTimerTask = new TimerTask() {
					public void run() {
						if (DMRService.dmrDev != null) {
							DMRService.dmrDev.setDmrDeviceName(true);
						}
						if (AccountUtils.isLetvAccountLogin(mcontext)
								&& ListenNetWorkService.mHandler != null
								&& ListenNetWorkService.mThread != null
								&& ListenNetWorkService.mThread.isAlive()) {
							LetvLog.d("NetStatusBroadCast",
									"BOOT_COMPLETED sendOnline after 10 seconds");
							ListenNetWorkService.sendOnline();
						}
						if (mTimer != null) {
							mTimer.cancel();
							mTimer = null;
						}

						if (mTimerTask != null) {
							mTimerTask.cancel();
							mTimerTask = null;
						}
					}
				};
			}

			if (mTimer != null && mTimerTask != null)
				mTimer.schedule(mTimerTask, 10000);
			Intent i = new Intent(paramContext, ListenNetWorkService.class);
			i.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
			paramContext.startService(i);
			/* 开启数据统计服务 */
			paramContext.startService(new Intent(paramContext,
					LogPostService.class));

			/* 开启DMR服务 */
			paramContext
					.startService(new Intent(paramContext, DMRService.class));
			/* 开启UPNP服务 */
			paramContext.startService(new Intent(paramContext,
					BackgroundService.class));
			/* C1S同步多屏看看设备名称到系统设置-》设备名称 */
			if (LetvUtils.isLetvUI() == LetvUtils.C1S) {
				SharedPreferences sp = paramContext.getSharedPreferences(
						"DeviceName", Context.MODE_PRIVATE);
				String device_name = sp.getString("device_name", "客厅盒子1");
				SystemProperties.set(Constants.DEVEICE_NAME, device_name);
			}

		} else if ("com.letv.t2.globalsetting.multiscreenstatechange"
				.equals(paramIntent.getAction())) {

			Debug.d(TAG, ">>>>>onReceive :multiscreenstatechange");
			if (DMRService.dmrDev != null) {
				boolean isOn = paramIntent.getBooleanExtra("MultiScreenIsOn",
						true);
				DMRService.dmrDev.setDmrDeviceStatus(isOn);
			}
		} else if (paramIntent.getAction().equals("com.letv.action.changeName")) {
			String device_name = SystemProperties.get("net.hostname", "Letv");

			LetvLog.d(TAG, "changeName = " + device_name);
			if (DMRService.dmrDev != null) {
				DMRService.dmrDev.setDmrDeviceName(true);
			}

		} else if (paramIntent.getAction().equals(
				"com.letv.action.displayPhoneNum")) {
			
			int value = paramIntent.getIntExtra("PHONENUM", 0);
			Debug.v(TAG, ">>>>>displayPhoneNum ="  + value);
			if (value > 0) {

				String text = String.format(paramContext.getResources()
						.getString(R.string.main_connectnum_toast), value);
				if (mToast != null) {
					mToast.setText(text);
				} else {
					mToast = Toast.makeText(paramContext, text,
							Toast.LENGTH_SHORT);
				}
				if (mToast != null)
					mToast.show();
			}
		}

	}

	private void setNetInfo(int netType, String ip, String wifiSSID) {
		Debug.d(TAG, ">>>>>set net info netType =" + netType + "ip = " + ip);
		mNetType = netType;
		if (ip == null)
			ip = "";
		mIp = ip;
		if (wifiSSID == null)
			wifiSSID = "";
		mWifiSSID = wifiSSID;
	}

}