package com.letv.memory.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import java.util.TreeMap;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import android.util.Log;

public class AppMeminfo {

	public static final int FILTER_ALL_APP = 0; // all app
	public static final int FILTER_SYSTEM_APP = 1; // system app
	public static final int FILTER_THIRD_APP = 2; // the third app
	public static final int FILTER_SDCARD_APP = 3; // SDCard app
	public int rate = 0;

	private static String TAG = "appmeminfo";
	private Context context;
	private int filter = FILTER_ALL_APP;
	private List<AppInfo> mlistAppInfo;
	private PackageManager pm;

	private static final String APPSELF = "com.letv.memorycleaner";

	private Map pidMap;

	private static int process = 100;

	private String specialApp[] = { "com.letv.tv.playe", "com.letv.tv" };

	public AppMeminfo(Context context) {
		this.context = context;
	}

	public Map getThirdAppinfo() {

		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		// MemoryInfo memoryInfo = ActivityManager.MemoryInfo();
		ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
		int type = -1;

		// ActivityManager.
		activityManager.getMemoryInfo(mi);
		ApplicationInfo appInfo;
		Log.e(TAG, " memoryInfo.availMem " + mi.availMem + "n");
		Log.i(TAG, " memoryInfo.lowMemory " + mi.lowMemory + "n");
		Log.i(TAG, " memoryInfo.threshold " + mi.threshold + "n");
		List<RunningAppProcessInfo> runningAppProcesses = activityManager
				.getRunningAppProcesses();
		pidMap = new TreeMap();
		pm = context.getPackageManager();
		for (RunningAppProcessInfo runningAppProcessInfo : runningAppProcesses) {
			try {
				appInfo = pm.getApplicationInfo(
						runningAppProcessInfo.processName, 0);
				type = filterAPP(appInfo);

				if (FILTER_SYSTEM_APP != type) {
					pidMap.put(runningAppProcessInfo.pid,
							runningAppProcessInfo.processName);
					// count++;

				}

			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		/*
		 * Collection<Integer> keys = pidMap.keySet();
		 * 
		 * for(int key:keys) { int pids[] = new int[1]; pids[0] = key;
		 * android.os.Debug.MemoryInfo[] memoryInfoArray =
		 * activityManager.getProcessMemoryInfo(pids);
		 * for(android.os.Debug.MemoryInfo pidMemoryInfo: memoryInfoArray) {
		 * Log.i(TAG,
		 * String.format("** MEMINFO in pid %d [%s] **n",pids[0],pidMap
		 * .get(pids[0]))); Log.i(TAG, " pidMemoryInfo.getTotalPrivateDirty(): "
		 * + pidMemoryInfo.getTotalPrivateDirty() + "n"); Log.i(TAG,
		 * " pidMemoryInfo.getTotalPss(): " + pidMemoryInfo.getTotalPss() +
		 * "n"); Log.i(TAG, " pidMemoryInfo.getTotalSharedDirty(): " +
		 * pidMemoryInfo.getTotalSharedDirty() + "n"); } }
		 */

		return pidMap;

	}

	/* get Avail Memory */
	public float getAvailableMemory() {
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
		;
		am.getMemoryInfo(memInfo);
		return memInfo.availMem / (1024 * 1024);
	}

	public float getTotalMemory() {
		String str1 = "/proc/meminfo";
		String str2;
		String[] arrayOfString;
		int initial_memory = 0;
		try {
			FileReader localFileReader = new FileReader(str1);
			BufferedReader localBufferedReader = new BufferedReader(
					localFileReader, 8192);
			str2 = localBufferedReader.readLine();
			arrayOfString = str2.split("\\s+");
			for (String num : arrayOfString) {
				Log.i(str2, num + "\t");
			}
			initial_memory = Integer.valueOf(arrayOfString[1]).intValue() * 1024;// 鑾峰緱绯荤粺鎬诲唴瀛橈紝鍗曚綅鏄疜B锛屼箻浠�024杞崲涓築yte
			localBufferedReader.close();
		} catch (IOException e) {
		}
		// return Formatter.formatFileSize(context, initial_memory);
		return initial_memory / (1024 * 1024);
	}

	public float getRatio() {
		float radio = 0.0f;
		radio = getAvailableMemory() / getTotalMemory();
		return radio;
	}

	/* kill special APP by package name independent */
	private void killSpecialApp() {

		ActivityManager activityManger = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		for (int i = 0; i < specialApp.length; i++)
			activityManger.killBackgroundProcesses(specialApp[i]);

	}

	/* clean memory */
	public void cleanMemoryByKillThirdApp(boolean isFromPhone) {
		ActivityManager activityManger = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningAppProcessInfo> list = activityManger
				.getRunningAppProcesses();
		pidMap = getThirdAppinfo();
		Collection<Integer> keys = pidMap.keySet();
		rate = keys.size();
		if (isFromPhone == false)
			killSpecialApp();

		for (int key : keys) {
			int pids[] = new int[1];
			pids[0] = key;
			if (!"com.letv.memorycleaner".equals(pidMap.get(pids[0]))) {
				Log.e("libao", "@@@@@@@@third count=" + rate + "package = "
						+ pidMap.get(pids[0]));

				activityManger.killBackgroundProcesses(pidMap.get(pids[0])
						.toString());

				rate--;
				// activityManger.forceStopPackage(pkgList[j]);
			}

		}

	}

	/* clean memory by app type */
	public void cleanMemoryByAppType() {
		ActivityManager activityManger = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningAppProcessInfo> runningAppProcesslist = activityManger
				.getRunningAppProcesses();
		ApplicationInfo appInfo;
		int flag = -1;
		pm = context.getPackageManager();
		if (runningAppProcesslist != null)
			for (int i = 0; i < runningAppProcesslist.size(); i++) {
				ActivityManager.RunningAppProcessInfo apinfo = runningAppProcesslist
						.get(i);

				String[] pkgList = apinfo.pkgList;
				for (int j = 0; j < pkgList.length; j++) {

					try {
						appInfo = pm.getApplicationInfo(pkgList[j], 0);
						flag = filterAPP(appInfo);

						if (!APPSELF.equals(pkgList[j])) {
							if (FILTER_SYSTEM_APP != flag) {
								Log.e("libao1", "########the third pkgList"
										+ "[" + i + " " + j + "]" + "= "
										+ pkgList[j] + "size ="
										+ pkgList.length);
								activityManger
										.killBackgroundProcesses(pkgList[j]);
								// activityManger.forceStopPackage(pkgList[j]);
							}

						}

					} catch (NameNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}

			}
	}

	private int filterAPP(ApplicationInfo appInfo) {
		int ret = -1;
		// system app
		if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
			// Log.e("libao", "#####system" + appInfo.packageName);
			ret = FILTER_SYSTEM_APP;
		}
		// the third app
		else if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
			ret = FILTER_THIRD_APP;
			// Log.e("libao", "#####third " + appInfo.packageName);
		}
		// the third app
		else if ((appInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
			// Log.e("libao", "#####third " + appInfo.packageName);
			ret = FILTER_THIRD_APP;
		}
		// SDCard app
		else if ((appInfo.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0) {
			// Log.e("libao", "#####sd card " + appInfo.packageName);
			ret = FILTER_SDCARD_APP;
		}
		return ret;
	}

	/* filter app */
	public List<AppInfo> filterAPP(int filter) {
		pm = context.getPackageManager();
		// 鏌ヨ鎵�湁宸茬粡瀹夎鐨勫簲鐢ㄧ▼搴�
		List<ApplicationInfo> listAppcations = pm
				.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
		Collections.sort(listAppcations,
				new ApplicationInfo.DisplayNameComparator(pm));// 鎺掑簭
		List<AppInfo> appInfos = new ArrayList<AppInfo>(); // 淇濆瓨杩囨护鏌ュ埌鐨凙ppInfo

		// 鏍规嵁鏉′欢鏉ヨ繃婊�
		switch (filter) {
		case FILTER_ALL_APP: // 鎵�湁搴旂敤绋嬪簭
			appInfos.clear();
			for (ApplicationInfo app : listAppcations) {
				appInfos.add(getAppInfo(app));
			}
			return appInfos;
		case FILTER_SYSTEM_APP: // 绯荤粺绋嬪簭
			appInfos.clear();
			for (ApplicationInfo app : listAppcations) {
				if ((app.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
					appInfos.add(getAppInfo(app));
				}
			}
			return appInfos;
		case FILTER_THIRD_APP: // 绗笁鏂瑰簲鐢ㄧ▼搴�
			appInfos.clear();
			for (ApplicationInfo app : listAppcations) {
				// 闈炵郴缁熺▼搴�
				if ((app.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {
					appInfos.add(getAppInfo(app));
				}
				// 鏈潵鏄郴缁熺▼搴忥紝琚敤鎴锋墜鍔ㄦ洿鏂板悗锛岃绯荤粺绋嬪簭涔熸垚涓虹涓夋柟搴旂敤绋嬪簭浜�
				else if ((app.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
					appInfos.add(getAppInfo(app));
				}
			}
			break;
		case FILTER_SDCARD_APP: // 瀹夎鍦⊿DCard鐨勫簲鐢ㄧ▼搴�
			appInfos.clear();
			for (ApplicationInfo app : listAppcations) {
				if ((app.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0) {
					appInfos.add(getAppInfo(app));
				}
			}
			return appInfos;
		default:
			return null;
		}
		return appInfos;

	}

	// 鏋勯�涓�釜AppInfo瀵硅薄 锛屽苟璧嬪�
	private AppInfo getAppInfo(ApplicationInfo app) {
		AppInfo appInfo = new AppInfo();
		appInfo.setAppLabel((String) app.loadLabel(pm));
		appInfo.setAppIcon(app.loadIcon(pm));
		appInfo.setPkgName(app.packageName);
		return appInfo;
	}
}
