package com.letv.statistics;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;

public class PhoneInfo {

	/**
	 * 获取设备MAC地址 注意：如果没有MAC地址，则获取IMEI号，如果没有IMEI号，则获取ANDROID_ID，如果再没有则获取一个随机串
	 * 
	 * @return
	 */
	TelephonyManager telephonyManager;
	private Context context;

	public PhoneInfo(Context context) {
		this.context = context;
		telephonyManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
	}

	public String getDeviceMac() {

		WifiManager wifi = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);

		WifiInfo info = wifi.getConnectionInfo();
		if (info != null) {
			String mac = info.getMacAddress();
			if (mac != null) {
				return mac.replaceAll(":", "");
			}
		}
		return "";

	}

	public String getVersionName() {
		String version = null;
		// 获取packagemanager的实例
		PackageManager packageManager = context.getPackageManager();
		// getPackageName()是你当前类的包名，0代表是获取版本信息
		PackageInfo packInfo;
		try {
			packInfo = packageManager.getPackageInfo(context.getPackageName(),
					0);

			version = packInfo.versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return version;
	}

	// public String getDeviceMac() {
	// // 获取MAC地址
	// WifiManager wifi = (WifiManager)
	// context.getSystemService(Context.WIFI_SERVICE);
	// if (wifi != null
	// && wifi.getWifiState() != WifiManager.WIFI_STATE_UNKNOWN) {
	// WifiInfo info = wifi.getConnectionInfo();
	// if (info != null) {
	// String mac = info.getMacAddress();
	// if (mac != null) {
	// return mac.replaceAll(":", "");
	// }
	// }
	// }
	//
	// // 获取IMEI号
	// TelephonyManager tm = (TelephonyManager)
	// getSystemService(TELEPHONY_SERVICE);
	// String deviceID = tm.getDeviceId();
	// if (deviceID != null && !deviceID.startsWith("000000000000000")) { //
	// 全零为模拟器，过滤掉
	// return deviceID;
	// }
	//
	// // 获取ANDROID_ID
	// String androidID = Secure.getString(getContentResolver(),
	// Secure.ANDROID_ID);
	// if (androidID != null)
	// return androidID;
	// else
	// return UUID.randomUUID().toString().replaceAll("-", "");
	// }

	public String getDevicePhoneNum() {
		String NativePhoneNumber = "";
		if (telephonyManager != null)
			NativePhoneNumber = telephonyManager.getLine1Number();
		return NativePhoneNumber;
	}

	public String getVersion() {
		String version;
		try {
			PackageManager manager = context.getPackageManager();
			PackageInfo info = manager.getPackageInfo(context.getPackageName(),
					0);
			version = info.versionName;
			return version;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	public String getDeviceType() {
		String model = Build.MODEL;
		return model;
	}

	public String getDeviceOS() {
		// Build bd = new Build();
		// String model = bd.MODEL;
		// return android.os.Build.MODEL;
		return "android";
	}

	public String getDeviceOSVersion() {
		return android.os.Build.VERSION.RELEASE;
	}

	public String getUdid() {
		TelephonyManager telephonyManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		String deviceIMEI = "";
		if (telephonyManager != null)
			deviceIMEI = telephonyManager.getDeviceId();
		return deviceIMEI;
	}

}
