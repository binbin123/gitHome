package org.cybergarage.upnp;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import com.letv.dmr.utils.IfcUtil;
import com.letv.upnpControl.tools.Engine;
import com.letv.upnpControl.tools.LetvLog;

/**
 * 
 * @author youbin
 * 
 */
public class AppInfoSentPhone {
	
	public static final String TAG = AppInfoSentPhone.class.getSimpleName();
	
	public static final int FILTER_ALL_APP = 0; // 所有应用程序

	public static final int FILTER_SYSTEM_APP = 1; // 系统程序

	public static final int FILTER_THIRD_APP = 2; // 第三方应用程序

	public static final int FILTER_SDCARD_APP = 3; // 安装在SDCard的应用程序

	private String AppListJson = null;

	private int port = 0;

	private String ip;


	public AppInfoSentPhone(int port) {

		this.port = port;
		ip = IfcUtil.getIpAddress(Engine.getInstance().getContext());
	}

	private AppInfo getAppInfo(ApplicationInfo app) {
		AppInfo appInfo = new AppInfo();
		// appInfo.setAppLabel((String)app.loadLabel(pm));
		// appInfo.setAppIcon(app.loadIcon(pm));
		appInfo.setPkgName(app.packageName);

		return appInfo;
	}

	public String queryAppInfo(int filter) {
		List<AppInfo> mlistAppInfo = new ArrayList<AppInfo>();
		PackageInfo packinfo = null;
		PackageManager pm = Engine.getInstance().getContext()
				.getPackageManager();
		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		List<ResolveInfo> resolveInfos = pm.queryIntentActivities(mainIntent,
				PackageManager.GET_UNINSTALLED_PACKAGES);
		Collections.sort(resolveInfos,
				new ResolveInfo.DisplayNameComparator(pm));

		// 查询所有已经安装的应用程序
		List<ApplicationInfo> listAppcations = pm
				.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
		Collections.sort(listAppcations,
				new ApplicationInfo.DisplayNameComparator(pm));// 排序
		List<AppInfo> appInfos = new ArrayList<AppInfo>(); // 保存过滤查到的AppInfo

		switch (filter) {
		case FILTER_ALL_APP:

			if (mlistAppInfo != null) {
				appInfos.clear();
				mlistAppInfo.clear();
				for (ResolveInfo reInfo : resolveInfos) {
					String activityName = reInfo.activityInfo.name; // 获得该应用程序的启动Activity的name
					String pkgName = reInfo.activityInfo.packageName; // 获得应用程序的包名
					String appLabel = (String) reInfo.loadLabel(pm); // 获得应用程序的Label
					Drawable icon = reInfo.loadIcon(pm); // 获得应用程序图标
					try {
						packinfo = pm.getPackageInfo(pkgName, 0);
					} catch (NameNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					String versionName = packinfo.versionName;
					int versionCode = packinfo.versionCode;
					// 为应用程序的启动Activity的Intent
					Intent launchIntent = new Intent();
					launchIntent.setComponent(new ComponentName(pkgName,
							activityName));
					AppInfo appInfo = new AppInfo();
					appInfo.setAppLabel(appLabel);
					appInfo.setPkgName(pkgName);
					appInfo.setAppIcon(icon);
					appInfo.setAppversion(versionName);
					appInfo.setVersionCode(versionCode);
					appInfo.setIntent(launchIntent);
					mlistAppInfo.add(appInfo); // 添加至列表中
					AppListJson = onCreateJson(mlistAppInfo, FILTER_ALL_APP);
				}
			}
			break;

		case FILTER_THIRD_APP: // 第三方应用程序
			mlistAppInfo.clear();
			appInfos.clear();
			for (ApplicationInfo app : listAppcations) {
				if ((app.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {

					appInfos.add(getAppInfo(app));
					AppListJson = onCreateJson(appInfos, FILTER_THIRD_APP);
				}
			}
			break;

		default:
			break;
		}

		return AppListJson;
	}

	public String onCreateJson(List<AppInfo> mlistAppInfo, int filter) {

		JSONArray jsonArray = new JSONArray();
		JSONObject jsonObject = null;

		String uri = "http://" + ip + ":" + port + "/" + "pkg=";

		for (int i = 0; i < mlistAppInfo.size(); i++) {
			jsonObject = new JSONObject();
			try {
				jsonObject.put("appname", mlistAppInfo.get(i).getAppLabel());
				jsonObject.put("pkgname", mlistAppInfo.get(i).getPkgName());
				jsonObject.put("versionName", mlistAppInfo.get(i)
						.getAppversion());
				jsonObject.put("versionCode", mlistAppInfo.get(i)
						.getVersionCode());
				jsonObject.put("startintent", mlistAppInfo.get(i).getIntent());
				if (filter == FILTER_ALL_APP) {
					jsonObject.put("icn_url", uri
							+ mlistAppInfo.get(i).getPkgName());
				}

				jsonArray.put(jsonObject);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		String AppListJson = jsonArray.toString();

		LetvLog.e(TAG,"AppListJson========" +  AppListJson);
		return AppListJson;

	}

	public String getArg(String str) {
		if (null == str) {
			return null;
		}

		String[] txt = str.split("pkg=");
		if (null == txt) {
			return null;
		}

		int seq = txt.length - 1;
		if (seq < 0) {
			return null;
		}

		return txt[seq];
	}

	public byte[] postIcn(String pag) {
		// 404
		AppInfoMethod info = new AppInfoMethod(Engine.getInstance()
				.getContext());

		Drawable drawable = info.getAppIcon(pag);
		if (null == drawable) {
			LetvLog.e(TAG, "drawable===null");
			return null;
		}
		Bitmap bitmap = drawableToBitmap(drawable);

		return Bitmap2Bytes(bitmap);

	}

	public byte[] Bitmap2Bytes(Bitmap bm) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}

	public Bitmap drawableToBitmap(Drawable drawable) {

		Bitmap bitmap = Bitmap.createBitmap(

		drawable.getIntrinsicWidth(),

		drawable.getIntrinsicHeight(),

		drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
				: Bitmap.Config.RGB_565);

		Canvas canvas = new Canvas(bitmap);

		// canvas.setBitmap(bitmap);

		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
				drawable.getIntrinsicHeight());

		drawable.draw(canvas);

		return bitmap;

	}

}
