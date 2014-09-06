package com.letv.upnpControl.receiver;

import java.io.File;
import java.util.HashMap;
import com.letv.smartControl.R;
import com.letv.upnpControl.tools.Engine;
import com.letv.upnpControl.tools.LetvLog;
import com.letv.upnpControl.tools.LetvUtils;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
//import android.content.SharedPreferences;
//import android.content.pm.ApplicationInfo;
//import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
//import android.database.Cursor;
import android.net.Uri;
//import android.widget.Toast;
//import android.app.DownloadManager;

public class InstallQuietReceiver extends BroadcastReceiver {
	public static String INSTALL_PACKAGE_QUIET = "com.letv.smartControl.INSTALL_PACKAGE_QUIET";
	public static String PACKAGE_NAME = "INSTALL_PACKAGE_NAME";
	public static String APP_NAME = "INSTALL_APP_NAME";
	public static String CLIENT_NAME = "CLIENT_NAME";

	public static final String UPDATING_APP_NAME_KEY = "updating_app_name_key";
	public static final String UPDATING_APP_DB_ID_KEY = "updating_app_id_key";
	public static final String UPDATING_APP_ID_KEY = "updating_app_id_key";
	public static final String REASON = "storage_error_reason";

	public static final String APPID = "appId";
	public static final String APPName = "appName";
	public static final String STATE = "state";
	public static final String SUCCESS = "Success";

	public static final int PAY_SUCCESS = 1;
	public static final int PAY_OVER_TIME = -1;
	public static final int PAY_FAILED = -2;

	public static final String APP_INSTALL_FAILED = "com.letv.smartControl.APP_INSTALL_FAILED";
	public static final String APP_INSTALL_SUCCESS = "com.letv.smartControl.APP_INSTALL_SUCCESS";

	private static String mAppName;
	public static final String TAG = "InstallQuietReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		LetvLog.d("InstallQuietReceiver", "onReceive " + intent.getAction());
		if (INSTALL_PACKAGE_QUIET.equals(intent.getAction())) {
			Uri packageUri = intent.getData();
			String pkgName = intent.getStringExtra(PACKAGE_NAME);
			LetvLog.d("InstallQuietReceiver", "onReceive pkgName = " + pkgName);
			mAppName = intent.getStringExtra(APP_NAME);
			String clientName = intent.getStringExtra(CLIENT_NAME);
			if (packageUri == null || pkgName == null)
				return;
			if (LetvUtils.haveSystemSigned()) {

				installPackage(context, packageUri, pkgName, mAppName,
						clientName);
			} else {
				LetvLog.d("LetvUtils", "isLetvTv");
				install_app(context);
			}
		} else if ("android.intent.action.PACKAGE_ADDED".equals(intent
				.getAction())) {

			String fileUrl = Engine.getInstance().getFilePath() + mAppName + ".apk";
			LetvLog.d("InstallQuietReceiver", "******************fileUrl = "
					+ fileUrl);
			File appFile = new File(fileUrl);
			if (appFile.exists()) {
				appFile.delete();
			}
		}
	}

	private void install_app(Context context) {

		String fileName = Engine.getInstance().getFilePath() + mAppName + ".apk";

		Uri uri = Uri.fromFile(new File(fileName));
		LetvLog.d(TAG, "install_app installPackage fileName = " + fileName);
		Intent intent = new Intent(Intent.ACTION_VIEW);

		intent.setDataAndType(uri, "application/vnd.android.package-archive");
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		context.startActivity(intent);

	}

	private void installPackage(Context context, Uri packageUri,
			String pkgName, String title, String client) {

		PackageManager pm = context.getPackageManager();
		PackageInstallObserver observer = new PackageInstallObserver(context,
				title);
		/*
		 * check if there is an old version that has been installed.
		 */
		int installFlags = 0;
		try {
			PackageInfo pi = pm.getPackageInfo(pkgName,
					PackageManager.GET_UNINSTALLED_PACKAGES);
			if (pi != null) {
				installFlags |= PackageManager.INSTALL_REPLACE_EXISTING;
			}
		} catch (NameNotFoundException e) {
		}

		// enable apk request permissions
		PackageInfo pi = pm.getPackageArchiveInfo(packageUri.getPath(),
				PackageManager.GET_PERMISSIONS);
		HashMap<String, Integer> permsEnableMap = new HashMap<String, Integer>();
		if (pi != null && pi.requestedPermissions != null) {
			for (String perm : pi.requestedPermissions) {
				permsEnableMap.put(perm, 3);
			}
		}
		LetvLog.d("InstallQuietReceiver", "onReceive installPackage begin");
		try{
			pm.installPackage(packageUri, observer, installFlags, client);
		}catch(SecurityException ie){
			LetvLog.d("InstallQuietReceiver", "SecurityException");
		}
	}

	public static class PackageInstallObserver extends
			IPackageInstallObserver.Stub {
		boolean finished;
		int result;
		String mTitle;
		Context mContext;

		public PackageInstallObserver(Context context, String title) {
			mContext = context;
			mTitle = title;
		}

		public void packageInstalled(String pkgName, int status) {
			synchronized (this) {
				LetvLog.d("InstallQuietReceiver",
						"PackageInstallObserver ******************" + pkgName
								+ " :" + status);

				if (status != PackageManager.INSTALL_SUCCEEDED) {
					// TODO: install failed, toast to notify the user fail
					// reason
					if (status == PackageManager.INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES) {
						//mCertifiErrorApkName = pkgName;
					} else {
						broadcastInstallStatus(status);
					}
				} else {
					Intent intent = new Intent(APP_INSTALL_SUCCESS);

					String downloadcomplete = mContext
							.getString(R.string.installed_complete);

					if (mTitle != null)
						downloadcomplete = mTitle + downloadcomplete;
					intent.putExtra(REASON, downloadcomplete);
					mContext.sendBroadcast(intent);
				}
				deleteAppFile();
				finished = true;
				result = status;
				notifyAll();
			}
		}

		private void deleteAppFile() {
			String fileUrl = Engine.getInstance().getFilePath() + mAppName + ".apk";
			LetvLog.d("InstallQuietReceiver", "******************fileUrl = "
					+ fileUrl);
			File appFile = new File(fileUrl);
			if (appFile.exists()) {
				appFile.delete();
			}
		}

		private void broadcastInstallStatus(int status) {
			Intent intent = new Intent(APP_INSTALL_FAILED);
			// switch (status) {
			// case PackageManager.INSTALL_FAILED_ALREADY_EXISTS: {
			// intent.putExtra(
			// REASON,
			// mContext.getText(R.string.install_failed_already_exists));
			// break;
			// }
			// case PackageManager.INSTALL_FAILED_CONFLICTING_PROVIDER: {
			// intent.putExtra(
			// REASON,
			// mContext.getText(R.string.install_failed_conflicting_provider));
			// break;
			// }
			// case PackageManager.INSTALL_FAILED_CONTAINER_ERROR: {
			// intent.putExtra(
			// REASON,
			// mContext.getText(R.string.install_failed_container_error));
			// break;
			// }
			// case PackageManager.INSTALL_FAILED_CPU_ABI_INCOMPATIBLE: {
			// intent.putExtra(
			// REASON,
			// mContext.getText(R.string.install_failed_cpu_abi_incompatible));
			// break;
			// }
			// case PackageManager.INSTALL_FAILED_DEXOPT: {
			// intent.putExtra(
			// REASON,
			// mContext.getText(R.string.install_failed_dexopt));
			// break;
			// }
			// case PackageManager.INSTALL_FAILED_DUPLICATE_PACKAGE: {
			// intent.putExtra(
			// REASON,
			// mContext.getText(R.string.install_failed_duplicate_package));
			// break;
			// }
			// case PackageManager.INSTALL_FAILED_INSUFFICIENT_STORAGE: {
			// intent.putExtra(
			// REASON,
			// mContext.getText(R.string.install_failed_insufficient_storage));
			// break;
			// }
			// case PackageManager.INSTALL_FAILED_INTERNAL_ERROR: {
			// intent.putExtra(
			// REASON,
			// mContext.getText(R.string.install_failed_internal_error));
			// break;
			// }
			// case PackageManager.INSTALL_FAILED_INVALID_APK: {
			// intent.putExtra(
			// REASON,
			// mContext.getText(R.string.install_failed_invalid_apk));
			// break;
			// }
			// case PackageManager.INSTALL_FAILED_INVALID_INSTALL_LOCATION: {
			// intent.putExtra(
			// REASON,
			// mContext.getText(R.string.install_failed_invalid_install_location));
			// break;
			// }
			// case PackageManager.INSTALL_FAILED_INVALID_URI: {
			// intent.putExtra(
			// REASON,
			// mContext.getText(R.string.install_failed_invalid_uri));
			// break;
			// }
			// case PackageManager.INSTALL_FAILED_MEDIA_UNAVAILABLE: {
			// intent.putExtra(
			// REASON,
			// mContext.getText(R.string.install_failed_media_unavailable));
			// break;
			// }
			// case PackageManager.INSTALL_FAILED_MISSING_FEATURE: {
			// intent.putExtra(
			// REASON,
			// mContext.getText(R.string.install_failed_missing_feature));
			// break;
			// }
			// case PackageManager.INSTALL_FAILED_MISSING_SHARED_LIBRARY: {
			// intent.putExtra(
			// REASON,
			// mContext.getText(R.string.install_failed_missing_shared_library));
			// break;
			// }
			// case PackageManager.INSTALL_FAILED_NEWER_SDK: {
			// intent.putExtra(
			// REASON,
			// mContext.getText(R.string.install_failed_newer_sdk));
			// break;
			// }
			// case PackageManager.INSTALL_FAILED_NO_SHARED_USER: {
			// intent.putExtra(
			// REASON,
			// mContext.getText(R.string.install_failed_no_shared_user));
			// break;
			// }
			// case PackageManager.INSTALL_FAILED_OLDER_SDK: {
			// intent.putExtra(
			// REASON,
			// mContext.getText(R.string.install_failed_older_sdk));
			// break;
			// }
			// case PackageManager.INSTALL_FAILED_REPLACE_COULDNT_DELETE: {
			// intent.putExtra(
			// REASON,
			// mContext.getText(R.string.install_failed_replace_couldnt_delete));
			// break;
			// }
			// case PackageManager.INSTALL_FAILED_SHARED_USER_INCOMPATIBLE: {
			// intent.putExtra(
			// REASON,
			// mContext.getText(R.string.install_failed_shared_user_incompatible));
			// break;
			// }
			// case PackageManager.INSTALL_FAILED_TEST_ONLY: {
			// intent.putExtra(
			// REASON,
			// mContext.getText(R.string.install_failed_test_only));
			// break;
			// }
			// case PackageManager.INSTALL_FAILED_UPDATE_INCOMPATIBLE: {
			// intent.putExtra(
			// REASON,
			// mContext.getText(R.string.install_failed_update_incompatible));
			// break;
			// }
			// default: {
			// intent.putExtra(
			// REASON,
			// mContext.getText(R.string.install_failed));
			// break;
			// }
			// }
			intent.putExtra(REASON, mContext.getText(R.string.install_failed));
			mContext.sendBroadcast(intent);
		}
	}
}