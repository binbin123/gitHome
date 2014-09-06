package com.letv.upnpControl.service;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import android.app.Instrumentation;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.ImageView;
import com.letv.smartControl.R;
import com.letv.upnpControl.tools.Engine;
import com.letv.upnpControl.tools.LetvLog;
import com.letv.upnpControl.tools.LetvUtils;

/**
 * @title:
 * @changeRecord
 */
public class BackgroundService extends Service {
	private static final String TAG = "BackgroundService";
	// private static LetvLoginManager llm;
	private static boolean isLogin = false;
	private Timer mTimer = null;
	private TimerTask mTimerTask = null;
	public static Handler handler;
	private static WindowManager mWM = null;
	private static ImageView ivCursor = null;
	private static WindowManager.LayoutParams mParams;
	public static int x = 200;
	public static int y = 200;
	private boolean mDrawMouse = false;
	public static Handler mCancelMousehandler;
	private Object mObject = new Object();
	public static final int DISPLAY_MOUSE = 0;
	public static final int REMOVE_MOUSE = 1;
	public static final int WHEEL_SCROLL = 2;
	public static final int KEY_ACTION = 3;
	public static final int MOUSE_PRESS = 4;
	public static final int KEY_POWER = 5;
	private static boolean mInit = false;

	/* intel upnp lib begin */
	public static native final int TvDeviceStart();

	public static native final int TvDeviceStop();

	public static native int setDeviceNameAndMac(String name_mac,
			String user_name);

	public static native int setDeviceName(String device_name);

//	static {
//		System.loadLibrary("ixml");
//		System.loadLibrary("threadutil");
//		System.loadLibrary("upnp");
//		System.loadLibrary("upnp_device");
//	}

	/* intel upnp lib end */
	public IBinder onBind(Intent intent) {
		LetvLog.i(TAG, "Service onBind--->");
		return null;
	}

	public void onCreate() {
		LetvLog.i(TAG, "backgroundService onCreate--->");
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		onStart(intent, startId);
		return Service.START_STICKY;
	}

	public void onStart(Intent intent, int startId) {
		LetvLog.i(TAG, "Service onStart--->");
		// LetvUtils.saveFileToSDCar("kankan_log", "background service onstart "
		// + "\n");
		init();
	}

	public void onDestroy() {
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}
		if (mTimerTask != null) {
			mTimerTask.cancel();
			mTimerTask = null;
		}
		LetvLog.i(TAG, "Service onDestroy--->");
	}

	public boolean onUnbind(Intent intent) {
		LetvLog.i(TAG, "Service onUnbind--->");
		return super.onUnbind(intent);
	}

	private void createMessageHandleThread() {

		new Thread() {

			public void run() {

				Looper.prepare();

				handler = new Handler() {

					public void handleMessage(Message msg) {
						LetvLog.i(TAG, "handleMessage msg =" + msg);
						// process incoming messages here
						switch (msg.what) {
						case DISPLAY_MOUSE: {
							handler.removeMessages(WHEEL_SCROLL);
							handler.removeMessages(MOUSE_PRESS);
							handler.removeMessages(KEY_ACTION);
							handler.removeMessages(REMOVE_MOUSE);
							handler.sendEmptyMessageDelayed(REMOVE_MOUSE,
									10 * 1000);
							int x = msg.arg1;
							int y = msg.arg2;
							synchronized (mObject) {
								drawMouse(Engine.getInstance().getContext(), x,
										y);
							}
							break;
						}
						case KEY_POWER: {
							int KeyCode = msg.arg1;
							try {
								Instrumentation inst = new Instrumentation();
								inst.sendKeySync(new KeyEvent(
										KeyEvent.ACTION_UP, KeyCode));
							} catch (SecurityException ie) {

							}
							break;
						}
						case REMOVE_MOUSE:

							mWM.removeView(ivCursor);
							mDrawMouse = false;

							break;
						case KEY_ACTION: {
							handler.removeMessages(WHEEL_SCROLL);
							handler.removeMessages(DISPLAY_MOUSE);
							handler.removeMessages(MOUSE_PRESS);

							int KeyCode = msg.arg1;
							LetvLog.i(TAG, "input keyevent code =" + KeyCode);
							// Runtime runtime = Runtime.getRuntime();
							// if (mInit == false)
							// {
							// try
							// {
							//
							// // Process p = runtime.exec("adb tcpip 5555");
							//
							// LetvLog.i(TAG, "adb connect 127.0.0.1");
							// Process p =
							// runtime.exec("adb connect 127.0.0.1");
							//
							//
							// } catch (IOException e)
							// {
							// LetvLog.e(TAG, e.toString());
							// }
							// mInit = true;
							// return;
							// }

							// try
							// {
							// mDoCmd = true;
							// Runtime run = Runtime.getRuntime();
							// Process p1 =
							// run.exec("adb -s 127.0.0.1:5555 shell input keyevent "
							// +
							// KeyCode);
							// int value1 = p1.waitFor();
							// LetvLog.i(TAG, "adb shell input keyevent = "
							// +value1);
							//
							// } catch (IOException e)
							// {
							// LetvLog.e(TAG, e.toString());
							// }catch(InterruptedException e){
							// Log.e(TAG, e.toString());
							// }
							try {
								Runtime rt = Runtime.getRuntime();
								Process proc = rt
										.exec("adb -s 127.0.0.1:5555 shell input keyevent "
												+ KeyCode);
								// InputStream stderr = proc.getErrorStream();
								// InputStreamReader isr = new
								// InputStreamReader(stderr);
								// BufferedReader br = new BufferedReader(isr);
								// String line = null;
								// System.out.println("<error></error>");
								// while ((line = br.readLine()) != null)
								// System.out.println(line);
								// System.out.println("");
								// int exitVal = proc.waitFor();
								// System.out.println("Process exitValue: " +
								// exitVal);
							} catch (Throwable t) {
								t.printStackTrace();
							}
						}
							break;
						case WHEEL_SCROLL: {
							int x = msg.arg1;
							int y = msg.arg2;
							handler.removeMessages(WHEEL_SCROLL);
							handler.removeMessages(DISPLAY_MOUSE);
							handler.removeMessages(MOUSE_PRESS);
							handler.removeMessages(KEY_ACTION);
							try {
								Runtime runtime = Runtime.getRuntime();
								LetvLog.d(TAG, "adb shell input trackball y = "
										+ y);
								runtime.exec("adb shell input trackball roll "
										+ x + " " + y);
							} catch (IOException e) {
								LetvLog.e(TAG, e.toString());
							}
							break;
						}
						case MOUSE_PRESS:
							try {
								handler.removeMessages(WHEEL_SCROLL);
								handler.removeMessages(DISPLAY_MOUSE);
								handler.removeMessages(KEY_ACTION);
								int x = msg.arg1;
								int y = msg.arg2;
								Runtime runtime = Runtime.getRuntime();
								LetvLog.d(TAG,
										"adb shell input touchscreen tap " + x
												+ " " + y);
								runtime.exec("adb -s 127.0.0.1:5555 shell input tap "
										+ x + " " + y);
							} catch (IOException e) {
								LetvLog.e(TAG, e.toString());
							}
							break;
						}

					}

				};

				Looper.loop();

			}

		}.start();
		if (!LetvUtils.haveSystemSigned()) {
			Runtime runtime = Runtime.getRuntime();
			if (mInit == false) {
				try {

					// Process p = runtime.exec("adb tcpip 5555");
					LetvLog.i(TAG, "adb connect 127.0.0.1");
					runtime.exec("adb connect 127.0.0.1");

				} catch (IOException e) {
					LetvLog.e(TAG, e.toString());
				}
				mInit = true;
			}
		}
	}

	public void drawMouse(Context context, int temp_x, int temp_y) {

		mWM = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		if (ivCursor == null) {
			ivCursor = new ImageView(context);

			ivCursor.setImageResource(R.drawable.pointer_arrow);
		}
		mParams = new WindowManager.LayoutParams();
		mParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
		mParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		mParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
				| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
				| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
		mParams.format = PixelFormat.TRANSLUCENT;

		mParams.type = WindowManager.LayoutParams.TYPE_TOAST;

		x += temp_x;

		y += temp_y;

		mParams.x = x;// 閻╃顕禍搴＄潌楠炴洖甯悙鍦畱x鏉炵绐涚粋锟�

		mParams.y = y;// 閻╃顕禍搴＄潌楠炴洖甯悙鍦畱y鏉炵绐涚粋锟�LetvLog.d("shixq",
						// "draw mouse position  " + x + " " + y);
		if (mDrawMouse) {
			mWM.updateViewLayout(ivCursor, mParams);
		} else {
			mWM.addView(ivCursor, mParams);
			mDrawMouse = true;
		}

	}

	private void init() {
		if (!LetvUtils.isCanConnected(this)) {// 濡拷鐓￠幍瀣簚閺勵垰鎯佸鑼病鏉╃偞甯寸純鎴犵捕
			LetvLog.d("TAG", "network error!");
			// sendBroadcast(Constants.LoginStatus.NET_ERROR);
			LetvUtils.saveFileToSDCar("kankan_log",
					"background service network error return " + "\n");
			// return;
		}
		// if (LetvUtils.isLetvUI() == LetvUtils.LETV) {
		// return;
		// }
		if (LetvUtils.isDmrOnly() == false) {
			int ret = 0;
			ret = TvDeviceStop();
			if (ret == 0) {// 0 success
				Intent intent1 = new Intent();
				intent1.setAction("com.letv.dlna.threescreen.stop");
				// sendBroadcast(intent1);
			}

			String device_name = "KanKan";
			if (LetvUtils.isHideNameFunc()) {
				device_name = SystemProperties.get("net.hostname");

			} else {
				SharedPreferences sp = getSharedPreferences("DeviceName",
						Context.MODE_PRIVATE);

				device_name = sp.getString("device_name", "客厅电视1");
			}

			String name_mac = LetvUtils.getProductNameMac(this);

			LetvLog.d("backgroundService", " start tv device name_mac = "
					+ name_mac);
			setDeviceNameAndMac(name_mac, device_name);
			ret = TvDeviceStart();
			LetvUtils.saveFileToSDCar("kankan_log",
					"background service TvDeviceStart ret = " + ret + "\n");
			if (ret == 0) {// 0 success
				Intent intent1 = new Intent();
				intent1.setAction("com.letv.dlna.threescreen.start");
				// sendBroadcast(intent1);
			}
		}
		Engine.getInstance().setContext(this);

		createMessageHandleThread();
	}
}
