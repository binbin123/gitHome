package com.letv.screenui.activity.tools;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import org.json.JSONObject;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class LetvUtils {

	/**
	 * 鑾峰彇鏈�?��鐨処P鍦板�?
	 * 
	 * @return
	 */
	public static String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			ex.printStackTrace();
		}
		return "";
	}

	/**
	 * 鑾峰彇鏈�?��鐨凪AC鍦板�?
	 * 
	 * @return
	 */
	public static String getMac() {
		String macAdress = null;
		String str = "";
		try {
			Process pp = Runtime.getRuntime().exec(
					"cat /sys/class/net/wlan0/address ");
			InputStreamReader ir = new InputStreamReader(pp.getInputStream());
			LineNumberReader input = new LineNumberReader(ir);
			for (; null != str;) {
				str = input.readLine();
				if (str != null) {
					macAdress = str.trim();
					break;
				}
			}
			ir.close();
			input.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return macAdress;
	}

	/**
	 * 妫€鏌ョ綉缁滄槸鍚﹁繛鎺�?
	 * 
	 * @param ctx
	 * @return 缃戠粶姝ｅ父杩炴�?true
	 */
	public static boolean isCanConnected(Context ctx) {
		ConnectivityManager cm = (ConnectivityManager) ctx
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		return ni != null && ni.isConnected();
	}

	/**
	 * 妫€鏌ュ瓧绗︿覆鏄惁鏄痡son 鏁版�?
	 * 
	 * @param json
	 * @return
	 */
	public static boolean checkIsJson(String json) {
		if (json == null) {
			return false;
		}
		if (json.trim().length() == 0) {
			return false;
		}

		try {
			new JSONObject(json);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	private static ProgressDialog pd = null;

	/**
	 * 鏄剧ず杩涘害瀵硅瘽妗�?
	 * 
	 * @param context
	 * @param title
	 * @param msg
	 */
	public static void showProgressDialog(final Context context, String msg,
			boolean back_flag) {
		if (pd != null) {
			pd.dismiss();
			pd.cancel();
			pd = null;
		}

		pd = new ProgressDialog(context);

		pd.setMessage(msg);
		pd.setCancelable(back_flag);// 杩斿洖閿笉鑳�?繑鍥�?
		pd.show();

		// 娣诲姞鍙栨秷鐩戝�?
		pd.setOnDismissListener(new DialogInterface.OnDismissListener() {
			public void onDismiss(DialogInterface dialog) {
			}
		});
	}

	/**
	 * 鍙栨秷鏄剧ず杩涘害�?硅瘽妗�
	 */
	public static void dismissProgressDialog() {
		if (pd != null) {
			pd.dismiss();
			pd.cancel();
			pd = null;
		}
	}

	/**
	 * 鑷畾涔塗oast鎻愮�?
	 * 
	 * @param duration
	 *            鏄剧ず鏃堕暱
	 * @param content
	 *            鏄剧ず鍐呭
	 * @param isSingleLine
	 *            鏄惁鏄剧ず涓€琛岋紝true=鏄紝false=鍚�
	 */
	public static void showToast(int duration, String content, Context context) {
		Toast.makeText(context, content, duration).show();
	}

	/**
	 * 瑙ｅ喅閰嶇疆鏂囦欢涓腑鏂囧瓧绗︿贡鐮佺殑闂�?��
	 * 
	 * @param str
	 * @return
	 */
	public static String getTranscodingString(String str) {
		try {
			str = (new String(str.getBytes("ISO-8859-1"), "utf8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return str;
	}

}
