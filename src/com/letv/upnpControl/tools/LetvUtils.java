package com.letv.upnpControl.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import org.json.JSONObject;
import com.letv.update.UpdateService;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.SystemProperties;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceControl;
import android.view.WindowManager;
import android.widget.Toast;

/*
 c1s签名版本和大众版本：
 1：修改haveSystemSigned（）函数
 2：修改manifest中的android:sharedUserId="android.uid.system"
 * */
public class LetvUtils extends Engine {

	public static final String TAG = "LetvUtils";
	/**
	 * 鑾峰彇Letv鏅鸿兘鏈虹殑绯荤粺甯愬彿淇℃伅
	 * 
	 * @return 鐢ㄦ埛鍚嶅拰鏈�悗鐨勭櫥褰曟椂闂�
	 */
	public static final int LETV = 0;
	public static final int XIAOMI = 1;
	public static final int C1S = 2;
	public static final int NEWC1S = 3;
	public static final int S250 = 4;
	public static final int S240 = 5;
	public static final int OTHERS = -1;

	public static String[] getSystemUserInfo() {
		String[] result = new String[3];

		// result[0] = SystemProperties.get("persist.letv.username");
		// result[1] = SystemProperties.get("persist.letv.loginTime");
		// result[2] = SystemProperties.get("persist.letv.password");

		// 鏉烆剚宕查幋鎰毈閸愶拷
		if (result[0] != null) {
			result[0] = result[0].toLowerCase();
		}

		LetvLog.d("TAG", "get system user name = " + result[0]
				+ "   loginTime = " + result[1] + "  password = " + result[2]);
		return result;
	}

	public static int getTvManufacturer() {

		// /* add begin for unsigned system */
		// if (Build.USER.contains("alexwang")) {
		// return C1S;
		// }
		// /* add end for unsigned system */
		if (Build.USER.contains("letv") || Build.HOST.contains("letv")
				|| Build.USER.contains("alexwang")) {
			// LetvLog.d("LetvUtils", "isLetvTv");
			return LETV;
		} else if (Build.MODEL.contains("MiBOX1S")) {
			return XIAOMI;
		}

		return OTHERS;
	}

	public static boolean haveSystemSigned() {

		return true;// LETV product
		// return false; //thirty common version
	}

	public static boolean isCloseAirplay() {
		if (Build.USER.contains("alexwang")) {
			return true;
		} else if (Build.USER.contains("letv") || Build.HOST.contains("letv")) {

			return false;
		}
		return true;
	}

	public static String getTvProductName() {
		if (Build.USER.contains("alexwang")) {
			return "c1s";
		} else if (Build.USER.contains("letv") || Build.HOST.contains("letv")) {
			return "letv";
		}
		return "other";
	}

	public static boolean isLetvUi3Version() {
		String version = SystemProperties.get("ro.letv.ui");
		if (version.contains("3.0")) {
			return true;
		}
		return false;

	}

	public static boolean isDmrOnly() {
		return true;
		// return false;
	}

	public static boolean isSupportMiracast() {
		String product = SystemProperties.get("ro.letv.product.name");
		if (product.contains("S2-50") || product.contains("S250")
				|| product.contains("S240")) {
			return true;
		}
		return false;

	}

	public static int isLetvUI() {

		if (Build.USER.contains("alexwang")) {
			return C1S;

		} else if (Build.USER.contains("letv") || Build.HOST.contains("letv")
				|| Build.USER.contains("0radix")) {
			// LetvLog.d("LetvUtils", "isLetvTv");
			return LETV;
		}

		return OTHERS;
	}

	public static String getProductNameMac(Context context) {
		String name_mac = getMac(context);
		if (isSupportMiracast()) {
			name_mac = "s250" + name_mac;
		} else if (isLetvUI() == LetvUtils.C1S) {
			name_mac = "c1s" + name_mac;
		} else if (isLetvUI() == LetvUtils.OTHERS) {
			name_mac = "other" + name_mac;
		}
		return name_mac;
	}

	public static boolean isHideNameFunc() {
		if (Build.USER.contains("alexwang")) {
			return false;
		} else if (Build.USER.contains("letv") || Build.HOST.contains("letv")) {
			// LetvLog.d("LetvUtils", "isLetvTv");
			return true;
		}

		return false;
	}

	public static boolean isInstalledIE() {
		if (Build.USER.contains("alexwang")) {
			return false; // C1S ui1.0 version

		} else if (Build.MODEL.contains("AMLOGIC8726MX")) {
			return false; // NEWC1S ui2.3 version
		} else if (Build.USER.contains("letv") || Build.HOST.contains("letv")) {
			// LetvLog.d("LetvUtils", "isLetvTv");
			return true; // LETV ui3.0 or ui2.3 version, for tv
		}
		return false; // other tv or box
	}

	/**
	 * 閼惧嘲褰囬張顒�勾閻ㄥ嚘P閸︽澘娼� * @return
	 */
	public static String getLocalIpAddress() {
		LetvLog.d("getLocalIpAddress  getLocalIpAddress");
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				if (intf != null) {
					for (Enumeration<InetAddress> enumIpAddr = intf
							.getInetAddresses(); enumIpAddr.hasMoreElements();) {
						InetAddress inetAddress = enumIpAddr.nextElement();
						if (inetAddress != null
								&& !inetAddress.isLoopbackAddress()
								&& inetAddress.getHostAddress() != null) {
							String tempAddress = inetAddress.getHostAddress()
									.toString();
							if (tempAddress.contains(".")) {// 閸欙拷0.10.70.49缁鎶�弽鐓庣础閻拷
															// return
															// tempAddress;
							}
							// return inetAddress.getHostAddress().toString();
						}
					}
				}
			}
		} catch (SocketException ex) {
			ex.printStackTrace();
			LetvLog.e("TAG", ex.toString());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return "";
	}

	/**
	 * 濡拷鐓＄純鎴犵捕閺勵垰鎯佹潻鐐村复
	 * 
	 * @param ctx
	 * @return 缂冩垹绮跺锝呯埗鏉╃偞甯�true
	 */
	public static boolean isCanConnected(Context ctx) {
		ConnectivityManager cm = (ConnectivityManager) ctx
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		return ni != null && ni.isConnected();
	}

	/**
	 * 濡拷鐓＄�妤冾儊娑撳弶妲搁崥锔芥Цjson 閺佺増宓� * @param json
	 * 
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
	 * 閺勫墽銇氭潻娑樺鐎电鐦藉锟� * @param context
	 * 
	 * @param title
	 * @param msg
	 */
	public static void showProgressDialog(final Context context, String msg,
			boolean back_flag) {
		try {
			if (pd != null) {
				pd.dismiss();
				pd.cancel();
				pd = null;
			}

			pd = new ProgressDialog(context);

			pd.setMessage(msg);
			pd.setCancelable(back_flag);// 鏉╂柨娲栭柨顔荤瑝閼冲�绻戦崶锟�
			pd.show();

			// 濞ｈ濮為崣鏍ㄧХ閻╂垵鎯�
			pd.setOnDismissListener(new DialogInterface.OnDismissListener() {
				public void onDismiss(DialogInterface dialog) {
				}
			});
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 閸欐牗绉烽弰鍓с仛鏉╂稑瀹崇�纭呯樈濡楋拷
	 */
	public static void dismissProgressDialog() {
		if (pd != null) {
			pd.dismiss();
			pd.cancel();
			pd = null;
		}
	}

	/**
	 * 閼奉亜鐣炬稊濉梠ast閹绘劗銇� * @param duration 閺勫墽銇氶弮鍫曟毐
	 * 
	 * @param content
	 *            閺勫墽銇氶崘鍛啇
	 * @param isSingleLine
	 *            閺勵垰鎯侀弰鍓с仛娑擄拷顢戦敍瀹紃ue=閺勵垽绱漟alse=閸氾拷
	 */
	public static void showToast(int duration, String content, Context context) {
		Toast.makeText(context, content, duration).show();
	}

	/**
	 * 鐟欙絽鍠呴柊宥囩枂閺傚洣娆㈡稉顓濊厬閺傚洤鐡х粭锔胯础閻胶娈戦梻顕�暯
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

	private static String getEthernetMacAddressByName() {
		int i = 0;
		try {
			if (NetworkInterface.getByName("eth0") != null) {
				byte[] arrayOfByte = NetworkInterface.getByName("eth0")
						.getHardwareAddress();

				if (arrayOfByte == null)
					return null;
				StringBuilder localStringBuilder = new StringBuilder();
				int j = arrayOfByte.length;
				for (i = 0; i < j; i++) {

					byte b = arrayOfByte[i];
					Object[] arrayOfObject = new Object[1];
					arrayOfObject[0] = Byte.valueOf(b);
					localStringBuilder.append(String.format("%02x",
							arrayOfObject));
				}
				LetvLog.d(TAG, "getEthernetMacAddressByName mac addr: "
						+ localStringBuilder.toString());
				return localStringBuilder.toString();
			}

		} catch (Exception localException) {
			localException.printStackTrace();
		}

		return null;
	}

	private static String loadFileAsString(String paramString)
			throws IOException {
		StringBuffer localStringBuffer = new StringBuffer(1000);
		BufferedReader localBufferedReader = new BufferedReader(new FileReader(
				paramString));
		char[] arrayOfChar = new char[1024];
		while (true) {
			int i = localBufferedReader.read(arrayOfChar);
			if (i == -1) {
				localBufferedReader.close();
				return localStringBuffer.toString();
			}
			localStringBuffer.append(String.valueOf(arrayOfChar, 0, i));
		}
	}

	private static String getEthernetMacAddressFromFile() {
		try {
			String str = loadFileAsString("/sys/class/net/eth0/address")
					.toUpperCase().substring(0, 17);
			return str;
		} catch (Exception localException) {
			localException.printStackTrace();
		}
		return null;
	}

	private static String getEthernetMacaddr() {
		String str = getEthernetMacAddressFromFile();
		if ((str == null) || (str.length() <= 0))
			str = getEthernetMacAddressByName();
		return str;
	}

	public static String getMacAddress(Context context) {

		WifiInfo localWifiInfo = ((WifiManager) context
				.getSystemService("wifi")).getConnectionInfo();

		ConnectivityManager localConnectivityManager = (ConnectivityManager) context
				.getSystemService("connectivity");
		NetworkInfo localNetworkInfo = localConnectivityManager
				.getActiveNetworkInfo();
		if (localNetworkInfo == null)
			return null;

		if (localNetworkInfo.getTypeName().equalsIgnoreCase("WIFI")) {
			LetvLog.i(TAG, "use  wifi mac addr");
			return localWifiInfo.getMacAddress();
		}

		// if ((localWifiInfo == null) || (localWifiInfo.getMacAddress() ==
		// null)
		// || (localWifiInfo.getMacAddress().length() <= 0)) {
		// LetvLog.i(TAG, "get ethernet macaddr");
		// return getEthernetMacaddr();
		// }
		// return localWifiInfo.getMacAddress();
		LetvLog.i(TAG, "use  ethernet mac addr");
		return getEthernetMacaddr();
	}

	public static String getMac(Context context) {
		String mac = getMacAddress(context);
		if ((mac != null) && (mac != "")) {
			String[] tmp = mac.split(":");
			int len = tmp.length;
			if (len == 6) {
				String device_mac = tmp[0] + tmp[1] + tmp[2] + tmp[3] + tmp[4]
						+ tmp[5];
				return device_mac;
			}
		}

		return "";
		// return mac;
	}

	public static void saveFileToSDCar(String name, String content) {
		if (getTvProductName().equals("c1s")) {
			if (Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)) {
				FileOutputStream fos = null;

				try {

					File file = new File(
							Environment.getExternalStorageDirectory(), name);

					if (file.length() > 2 * 1024 * 1024) {
						file.delete();
						file = new File(
								Environment.getExternalStorageDirectory(), name);
					}
					fos = new FileOutputStream(file, true);

					fos.write(content.getBytes());
				} catch (Exception e) {

					e.printStackTrace();

				} finally {

					try {
						if (fos != null)
							fos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} else {
				// Toast.makeText(context, "no sdcard",
				// Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	   public static void checkUpdate(Context context) {
	        // 开始检测升级
	        UpdateService.isAutoUpdate = true;
	        Intent intent = new Intent(context, UpdateService.class);
	        context.stopService(intent);
	        context.startService(intent);

	    }
	   public static String getSDPath() {
			File sdDir = null;
			boolean sdCardExist = Environment.getExternalStorageState().equals(
					android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
			if (sdCardExist) {
				sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
			}
			LetvLog.w(TAG,"getSDPath = " + sdDir.toString());
			return "/storage/emulated/legacy/";

		}     
	   
	   public boolean takeScreenShot(String imagePath,Context context){
           
           if(imagePath.equals("" )){
                    imagePath = Environment.getExternalStorageDirectory()+File. separator+"Screenshot.png" ;
           }
                   
        Bitmap mScreenBitmap = null;
        WindowManager mWindowManager;
        DisplayMetrics mDisplayMetrics;
        Display mDisplay;
                
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mDisplay = mWindowManager.getDefaultDisplay();
        mDisplayMetrics = new DisplayMetrics();
        mDisplay.getRealMetrics(mDisplayMetrics);
                               
        float[] dims = {mDisplayMetrics.widthPixels , mDisplayMetrics.heightPixels };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH 
        		&& Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1 ){
        	try{
                Class<?> surface = Class.forName("android.view.Surface");
                Method screenshot=  surface.getMethod("screenshot",int.class,int.class);
                if(screenshot != null){
                	
                	mScreenBitmap = (Bitmap)screenshot.invoke(surface,(int) dims[0], ( int) dims[1]);
                }
               
            }catch (Exception e){
                e.printStackTrace();
                return false;
            }
        	//mScreenBitmap = Surface.screenshot((int) dims[0], ( int) dims[1]);
        }else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2){
        	mScreenBitmap = SurfaceControl.screenshot((int) dims[0], ( int) dims[1]);
        }
                   
        if (mScreenBitmap == null) {  
               return false ;
        }
                
     try {
        FileOutputStream out = new FileOutputStream(imagePath);
        mScreenBitmap.compress(Bitmap.CompressFormat. PNG, 100, out);
           
      } catch (Exception e) {
              
              
        return false ;
      }  
     return true;
	}

}
