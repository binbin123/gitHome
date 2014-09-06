package com.letv.upnpControl.tools;

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


public class PhoneCleanMemoryAppinfo {

	public static final int FILTER_ALL_APP = 0; // all app
	public static final int FILTER_SYSTEM_APP = 1; // system app
	public static final int FILTER_THIRD_APP = 2; // the third app
	public static final int FILTER_SDCARD_APP = 3; // SDCard app
	public boolean finish = false;
	private static String TAG = "youbin======================appmeminfo";
	private Context context;
	private PackageManager pm;
	private static final String APPSELF = "com.letv.smartControl";
	@SuppressWarnings("rawtypes")
	private Map pidMap;
	private String specialApp[] = { "com.letv.tv.player", "com.letv.tv" };

	public PhoneCleanMemoryAppinfo(Context context) {
		this.context = context;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map getThirdAppinfo() {

		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
		int type = -1;

		activityManager.getMemoryInfo(mi);
		ApplicationInfo appInfo;
		LetvLog.e(TAG, " memoryInfo.availMem " + mi.availMem);
		LetvLog.i(TAG, " memoryInfo.lowMemory " + mi.lowMemory);
		LetvLog.i(TAG, " memoryInfo.threshold " + mi.threshold);
		List<RunningAppProcessInfo> runningAppProcesses = activityManager.getRunningAppProcesses();
		pidMap = new TreeMap();
		pm = context.getPackageManager();
		for (RunningAppProcessInfo runningAppProcessInfo : runningAppProcesses) {
			try {
				appInfo = pm.getApplicationInfo(
						runningAppProcessInfo.processName, 0);
				type = filterAPP(appInfo);

				if (FILTER_SYSTEM_APP != type) {
					pidMap.put(runningAppProcessInfo.pid,runningAppProcessInfo.processName);

				}

			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return pidMap;

	}

	/* get Avail Memory */
	public float getAvailableMemory() {
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
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
				LetvLog.i(str2, num + "\t");
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

		ActivityManager activityManger = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

		for (int i=0; i < specialApp.length; i++)
			activityManger.killBackgroundProcesses(specialApp[i]);
		

	}

	/* clean memory */
	public void cleanMemoryByKillThirdApp(boolean isFromPhone) {
		ActivityManager activityManger = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		//List<ActivityManager.RunningAppProcessInfo> list = activityManger.getRunningAppProcesses();
		pidMap = getThirdAppinfo();
		Collection<Integer> keys = pidMap.keySet();
		if (isFromPhone == false)
			killSpecialApp();

		for (int key : keys) {
			LetvLog.e(TAG, " kill background process= " + pidMap.get(key).toString());
			activityManger.killBackgroundProcesses(pidMap.get(key).toString());

		}
		finish = true;
	}

	/* clean memory by app type */
	public void cleanMemoryByAppType() {
		ActivityManager activityManger = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningAppProcessInfo> runningAppProcesslist = activityManger.getRunningAppProcesses();
		ApplicationInfo appInfo;
		int flag = -1;
		pm = context.getPackageManager();
		if (runningAppProcesslist != null)
			for (int i = 0; i < runningAppProcesslist.size(); i++) {
				ActivityManager.RunningAppProcessInfo apinfo = runningAppProcesslist.get(i);

				String[] pkgList = apinfo.pkgList;
				for (int j = 0; j < pkgList.length; j++) {

					try {
						appInfo = pm.getApplicationInfo(pkgList[j], 0);
						flag = filterAPP(appInfo);

						if (!APPSELF.equals(pkgList[j])) {
							if (FILTER_SYSTEM_APP != flag) {
								LetvLog.e("libao1", "########the third pkgList"
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
			ret = FILTER_SYSTEM_APP;
		}
		// the third app
		else if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
			ret = FILTER_THIRD_APP;
		}
		// the third app
		else if ((appInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
			ret = FILTER_THIRD_APP;
		}
		// SDCard app
		else if ((appInfo.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0) {
			ret = FILTER_SDCARD_APP;
		}
		return ret;
	}

	/* filter app */
	public List<PhoneCleanMemoryInfo> filterAPP(int filter) {
		pm = context.getPackageManager();
		List<ApplicationInfo> listAppcations = pm.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
		Collections.sort(listAppcations,new ApplicationInfo.DisplayNameComparator(pm));
		List<PhoneCleanMemoryInfo> appInfos = new ArrayList<PhoneCleanMemoryInfo>();
		switch (filter) {
		case FILTER_ALL_APP: 
			appInfos.clear();
			for (ApplicationInfo app : listAppcations) {
				appInfos.add(getAppInfo(app));
			}
			return appInfos;
		case FILTER_SYSTEM_APP:
			appInfos.clear();
			for (ApplicationInfo app : listAppcations) {
				if ((app.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
					appInfos.add(getAppInfo(app));
				}
			}
			return appInfos;
		case FILTER_THIRD_APP:
			appInfos.clear();
			for (ApplicationInfo app : listAppcations) {
				if ((app.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {
					appInfos.add(getAppInfo(app));
				}
				else if ((app.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
					appInfos.add(getAppInfo(app));
				}
			}
			break;
		case FILTER_SDCARD_APP: 
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

	private PhoneCleanMemoryInfo getAppInfo(ApplicationInfo app) {
		PhoneCleanMemoryInfo appInfo = new PhoneCleanMemoryInfo();
		appInfo.setAppLabel((String) app.loadLabel(pm));
		appInfo.setAppIcon(app.loadIcon(pm));
		appInfo.setPkgName(app.packageName);
		return appInfo;
	}
}
