package com.letv.update;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.xmlpull.v1.XmlPullParser;
import com.letv.smartControl.R;
import com.letv.upnpControl.tools.LetvLog;
import com.letv.upnpControl.tools.LetvUtils;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Xml;
import android.view.WindowManager;
import android.widget.Toast;

/**
 * 升级服务（包含数据上报） 为了让数据上报和检测升级的过程不影响用户操作软件，将其作为服务
 * 
 * @author
 */
public class UpdateService extends Service {

	/**
	 * 升级地址
	 */
	private static final String UPDATE_URL = "http://api.hdtv.letv.com/iptv/api/apk/getUpgradeProfile";
	// private static final String UPDATE_URL =
	// "http://115.182.94.28/iptv/api/apk/getUpgradeProfile"; // 测试地址

	/**
	 * 不升级
	 */
	private static final int UPDATE_NOT = 0;

	/**
	 * 可选升级
	 */
	private static final int UPDATE_OPTIONAL = 1;

	/**
	 * 强制升级
	 */
	private static final int UPDATE_FORCE = 2;

	private static final int STOP_SERVICE = 3;
	private final static int UPDATE_FAIL_SERVER = 4;
	private final static int UPDATE_FAIL_CLIENT = 5;
	private final static int UPDATE_PREPARE = 6;

	private static final int CONNECTION_TIMEOUT = 20000;
	private static final String ENCODEING = "utf-8";

	private String packageName;
	private String packageVersion;
	private String deviceType;
	private String deviceModel;
	private String deviceResolution;
	private String deviceMac;
	private UpdateData updateInfo;
	public static boolean isAutoUpdate = true;
    public static String TAG = UpdateService.class.getSimpleName();
    
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case UPDATE_PREPARE:
				if (!isAutoUpdate) {
					// Toast.makeText(UpdateService.this,
					// R.string.update_check_now, Toast.LENGTH_LONG).show();
				}
				break;
			case UPDATE_NOT:
				if (!isAutoUpdate) {
					// Toast.makeText(UpdateService.this,
					// R.string.update_last_version, Toast.LENGTH_SHORT).show();
				}
				break;
			case STOP_SERVICE:
				stopSelf();
				break;
			case UPDATE_FAIL_CLIENT:
				if (!isAutoUpdate) {
					Toast.makeText(UpdateService.this,
							R.string.update_fail_client, Toast.LENGTH_SHORT)
							.show();
				}
				break;
			case UPDATE_FAIL_SERVER:
				if (!isAutoUpdate) {
					Toast.makeText(UpdateService.this,
							R.string.update_fail_server, Toast.LENGTH_SHORT)
							.show();
				}
				break;
			}
			super.handleMessage(msg);
		}

	};

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		new Thread() {
			public void run() {

				if (!isNetworkConnected()) {
					handler.sendEmptyMessage(STOP_SERVICE);
					return;
				}

				// 获取设备信息
				getDeviceInfo();

				// handler.sendEmptyMessage(UPDATE_PREPARE);

				// 获取服务器升级信息
				byte[] data = getUpdateMessage();
				if (data == null) {
					LetvLog.d(TAG, "update data is null");
					handler.sendEmptyMessage(UPDATE_FAIL_CLIENT);
					handler.sendEmptyMessage(STOP_SERVICE);
					return;
				}

				// 如果JSON解析失败，尝试XML解析
				updateInfo = analysisJSON(data);
				if (updateInfo == null) {
					updateInfo = analysisXML(data);
				}
				LetvLog.d(TAG, "--" + updateInfo + "--");
				// System.out.println(updateInfo);

				// 判断升级信息是否异常
				if (updateInfo == null
						|| (updateInfo.getCommand() != UPDATE_NOT && !updateInfo
								.getUrl().endsWith(".apk"))) {
					LetvLog.d(TAG, "update error");
					handler.sendEmptyMessage(UPDATE_FAIL_SERVER);
					handler.sendEmptyMessage(STOP_SERVICE);
					return;
				}

				// 判断是否升级
				if (UPDATE_NOT == updateInfo.getCommand()) {
					LetvLog.d(TAG, "not update");

					handler.sendEmptyMessage(UPDATE_NOT);
					handler.sendEmptyMessage(STOP_SERVICE);
					return;
				} else {
					showUpdateDiaLetvLog();
				}
			}
		}.start();
		return START_NOT_STICKY;
	}

	/**
	 * 获取设备信息
	 */
	private void getDeviceInfo() {
		packageName = getPackageName();
		packageVersion = getPackageVersion();
		deviceType = getDeviceType();
		deviceModel = getDeviceModel();
		deviceResolution = getDeviceResolution();
		deviceMac = getDeviceMac();
	}

	/**
	 * 向服务器发出GET请求，获取升级信息JSON串
	 * 
	 * @return
	 */
	private byte[] getUpdateMessage() {
		if (!isNetworkConnected()) {
			return null;
		}

		// 设置请求参数
		Map<String, String> params = new HashMap<String, String>();
		params.put("package", packageName);
		params.put("version", packageVersion);
		params.put("type", deviceType);
		params.put("model", deviceModel);
		params.put("resolution", deviceResolution);
		params.put("mac", deviceMac);
		String url = getUrlWithParams(UPDATE_URL, params);
		LetvLog.d(TAG, "--update url " + url + "--");
		HttpGet request = new HttpGet(url);

		HttpResponse response = null;
		byte[] data = null;
		try {
			HttpClient client = new DefaultHttpClient();
			client.getParams()
					.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,
							CONNECTION_TIMEOUT);
			response = client.execute(request);
			if (response != null
					&& response.getStatusLine().getStatusCode() == 200) {
				HttpEntity entity = response.getEntity();
				int length = (int) entity.getContentLength();
				InputStream is = entity.getContent();
				if (length > 0) {
					data = new byte[length];
					is.read(data);
				} else {
					ByteArrayOutputStream os = new ByteArrayOutputStream(2048);
					byte[] buffer = new byte[1024];
					int len = 0;
					while ((len = is.read(buffer, 0, buffer.length)) != -1) {
						os.write(buffer, 0, len);
					}
					os.flush();
					is.close();
					data = os.toByteArray();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}

	/**
	 * 解析JSON升级信息
	 * 
	 * @param data
	 * @return
	 */
	private UpdateData analysisJSON(byte[] data) {
		UpdateData info = null;
		String json = null;
		try {
			json = new String(data, "utf-8");

			JSONTokener jsonParser = new JSONTokener(json);
			JSONObject jsonObject = (JSONObject) jsonParser.nextValue();

			info = new UpdateData();
			info.setCommand(jsonObject.getInt("update"));
			info.setVersion(jsonObject.getString("version"));
			info.setNote(jsonObject.getString("note"));
			info.setUrl(jsonObject.getString("url"));

		} catch (Exception e) {
			e.printStackTrace();
		}
		return info;
	}

	/**
	 * 解析XML升级信息
	 * 
	 * @param data
	 * @return
	 */
	private UpdateData analysisXML(byte[] data) {
		UpdateData info = null;
		InputStream stream = new ByteArrayInputStream(data);
		String s = null;
		try {
			s = new String(data, "utf-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		XmlPullParser parser = Xml.newPullParser();
		try {
			parser.setInput(stream, ENCODEING);
			int eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
				case XmlPullParser.START_DOCUMENT:
					break;
				case XmlPullParser.START_TAG:
					String name = parser.getName().toLowerCase();
					if (name.equals("apkupgraderesponse")) {
						info = new UpdateData();
					} else {
						String text = parser.nextText();
						if (text != null) {
							text = text.trim();
							if (name.equals("update")) {
								info.setCommand(Integer.parseInt(text));
							} else if (name.equals("version")) {
								info.setVersion(parser.nextText());
							} else if (name.equals("note")) {
								info.setNote(parser.nextText());
							} else if (name.equals("url")) {
								info.setUrl(parser.nextText());
							}
						}
					}
					break;
				case XmlPullParser.END_TAG:
					break;
				}
				eventType = parser.next();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return info;
	}

	/**
	 * 显示升级日志对话框
	 */
	private void showUpdateDiaLetvLog() {
		Intent intent = new Intent(this, UpdateDialogActivity.class);
		intent.putExtra("updateInfo", updateInfo);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

	/**
	 * 获取本应用的包名
	 * 
	 * @return
	 */
	private String getPackageVersion() {
		String versionName = null;
		try {
			PackageInfo info = this.getPackageManager().getPackageInfo(
					getPackageName(), 0);
			versionName = info.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		int index = versionName.indexOf(".");
		versionName = versionName.substring(0, index + 2);
		LetvLog.d(TAG, "versionName = " + versionName);
		return versionName;
	}

	/**
	 * 获设备的类型
	 * 
	 * @return phone pad tv 3rd(第三方)
	 */
	private String getDeviceType() {
		if (LetvUtils.getTvManufacturer() == LetvUtils.LETV) {
			return "tv";
		} else {
			return "3rd";
		}
	}

	/**
	 * 获取设备型号
	 * 
	 * @return
	 */
	private String getDeviceModel() {
		if (LetvUtils.getTvManufacturer() == LetvUtils.LETV) {
		    return "X60";
		}else{
			return "";
		}
	}

	/**
	 * 获取设备分辨率
	 * 
	 * @return
	 */

	private String getDeviceResolution() {
		DisplayMetrics dm = new DisplayMetrics();
		((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay()
				.getMetrics(dm);
		int width = dm.widthPixels;
		int height = dm.heightPixels;
		return width + "x" + height;
	}

	/**
	 * 获取设备MAC地址 注意：如果没有MAC地址，则获取IMEI号，如果没有IMEI号，则获取ANDROID_ID，如果再没有则获取一个随机串
	 * 
	 * @return
	 */
	private String getDeviceMac() {
		// 获取MAC地址
		WifiManager wifi = (WifiManager) getSystemService(WIFI_SERVICE);
		if (wifi != null&& wifi.getWifiState() != WifiManager.WIFI_STATE_UNKNOWN) {
			WifiInfo info = wifi.getConnectionInfo();
			if (info != null) {
				String mac = info.getMacAddress();
				if (mac != null) {
					return mac.replaceAll(":", "");
				}
			}
		}

		// 获取IMEI号
		TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		String deviceID = tm.getDeviceId();
		if (deviceID != null && !deviceID.startsWith("000000000000000")) { // 全零为模拟器，过滤掉
			return deviceID;
		}

		// 获取ANDROID_ID
		String androidID = Secure.getString(getContentResolver(),
				Secure.ANDROID_ID);
		if (androidID != null)
			return androidID;
		else
			return UUID.randomUUID().toString().replaceAll("-", "");
	}

	/**
	 * 判断网络是否处于正常连接状态（包括WIFI和3G）
	 * 
	 * @return
	 */
	private boolean isNetworkConnected() {
		ConnectivityManager mConnectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
		if (mNetworkInfo != null) {
			return mNetworkInfo.isAvailable();
		}
		return false;
	}

	/**
	 * 根据给定的MAP自动生成标准的URL地址
	 * 
	 * @param url
	 * @param params
	 * @return
	 */
	private String getUrlWithParams(String url, Map<String, String> params) {
		StringBuilder builder = new StringBuilder(url);
		try {
			builder.append('?');
			Set<Entry<String, String>> entry = params.entrySet();
			for (Entry<String, String> param : entry) {
				builder.append(URLEncoder.encode(param.getKey(), ENCODEING));
				builder.append('=');
				builder.append(URLEncoder.encode(param.getValue(), ENCODEING));
				builder.append('&');
			}
			builder.deleteCharAt(builder.length() - 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return builder.toString();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
