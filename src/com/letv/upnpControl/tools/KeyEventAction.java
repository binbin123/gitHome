package com.letv.upnpControl.tools;

import org.json.JSONException;
import org.json.JSONObject;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;
import android.view.MotionEvent.PointerProperties;
import com.letv.dmr.DmrInterfaceManage;
import com.letv.dmr.MediaplayerConstants;
import com.letv.upnpControl.entity.MouseData;
import com.letv.upnpControl.service.BackgroundService;
import com.letv.upnpControl.ui.ShowApkInstallProgress;
import android.os.SystemClock;

/**
 * @title:
 * @description:
 * @company: 娑旀劘顬呯純鎴滀繆閹垱濡ч張顖ょ礄閸栨ぞ鍚敍澶庡亗娴犺姤婀侀梽鎰彆閸欙拷 * @author 閸欐彃顒熷锟�* @version
 *           1.0
 * @created 2012-2-7 娑撳宕�0:02:06
 * @changeRecord
 */
public class KeyEventAction {

	private static final String LETV_ACTION = "com.letv.external.launch.playleso";

	/**
	 * 濡剝瀚欓幐澶愭暛
	 * 
	 * @param KeyCode
	 */
	public static void simulateKeystroke(final int KeyCode) {
		if (LetvUtils.haveSystemSigned()) {
			injectKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyCode));
			injectKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyCode));
		} else {
			Message msg = Message.obtain();
			msg.what = BackgroundService.KEY_ACTION;
			msg.arg1 = KeyCode;
			if (BackgroundService.handler != null) {
				BackgroundService.handler.sendMessage(msg);
				// try{
				// Runtime rt = Runtime.getRuntime();
				// rt.exec("adb -s 127.0.0.1:5555 shell input keyevent " +
				// KeyCode);
				//
				// }catch (Throwable t) {
				// t.printStackTrace();
				// }
			}
		}
	}

	private static void injectKeyEvent(KeyEvent kEvent) {
		// try {
		//
		// IWindowManager windowManager = IWindowManager.Stub
		// .asInterface(ServiceManager.getService("window"));
		// windowManager.injectKeyEvent(kEvent, true);
		// } catch (Exception e) {
		// LetvLog.d("KeyEventAction", "injectKeyEvent" + e);
		// e.printStackTrace();
		// }
		try {
			Instrumentation inst = new Instrumentation();
			inst.sendKeySync(kEvent);
		} catch (SecurityException ie) {

		}

	}

	public static void PowerOff(Context context) {
		if (context == null) {
			LetvLog.e("KeyEventAction", "SettingEvent context = null");
			return;
		}
		// letv 2.3 or 3.0
		if (LetvUtils.getTvProductName().equals("letv")) {

			Intent powerOffIntent = new Intent(
					"android.intent.action.ACTION_REQUEST_SHUTDOWN");
			powerOffIntent.putExtra("android.intent.extra.AUTOSLEEP", false);
			powerOffIntent.putExtra("android.intent.extra.KEY_CONFIRM", false);
			powerOffIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(powerOffIntent);
		} else {// ui1.5 or others

			Instrumentation inst = new Instrumentation();
			try {
				inst.sendKeySync(new KeyEvent(KeyEvent.ACTION_DOWN,
						KeyEvent.KEYCODE_POWER));
			} catch (SecurityException ie) {

			}
			if (BackgroundService.handler != null) {
				Message msg = Message.obtain();
				msg.what = BackgroundService.KEY_POWER;
				msg.arg1 = KeyEvent.KEYCODE_POWER;
				BackgroundService.handler.sendMessageDelayed(msg, 500);
			}
		}
	}

	public static void SettingEvent(Context context) {
		if (context == null) {
			LetvLog.e("KeyEventAction", "SettingEvent context = null");
			return;
		}
		LetvLog.d("KeyEventAction", "SettingEvent");
		Intent closeIntent = new Intent();
		closeIntent.setAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
		closeIntent.putExtra("reason", "settingkey");
		context.sendBroadcast(closeIntent);

		Intent settingIntent = new Intent("com.letv.action.setting");
		context.sendBroadcast(settingIntent);

	}

	/* 姒х姵鐖ｅ姘崇枂婢跺嫮鎮婃稉濠佺瑓濠婃艾濮� */
	public static void MouseWheelEvent(Context context,
			final MouseData mMouseData) {

		if (context == null) {
			LetvLog.e("KeyEventAction", "MouseWheelEvent context = null");
			return;
		}

		float value = Float.parseFloat(mMouseData.y);
		if (LetvUtils.haveSystemSigned()) {
			if (value > 1.0f) {
				value = 1.0f;

			} else if (value < -1.0f) {
				value = -1.0f;
			}
			DisplayMetrics dm = new DisplayMetrics();
			dm = context.getApplicationContext().getResources()
					.getDisplayMetrics();
			int screenWidth = dm.widthPixels;
			int screenHeight = dm.heightPixels;

			int x = BackgroundService.x + screenWidth / 2;
			int y = BackgroundService.y + screenHeight / 2;
			LetvLog.d("shixq", "value =  " + value + "x= " + x + "y= " + y);
			/* 鐠佸墽鐤嗗姘崇枂娴滃娆㈢仦鐐达拷閸婏拷 */
			int[] pointerIds = new int[1];
			pointerIds[0] = 0;

			PointerProperties[] pointerProperties = new PointerProperties[1];
			pointerProperties[0] = new PointerProperties();
			pointerProperties[0].clear();
			pointerProperties[0].id = 0;
			pointerProperties[0].toolType = MotionEvent.TOOL_TYPE_MOUSE;

			PointerCoords[] pointerCoords = new PointerCoords[1];
			pointerCoords[0] = new PointerCoords();
			pointerCoords[0].clear();
			pointerCoords[0].size = 1.0f;
			pointerCoords[0].x = x;
			pointerCoords[0].y = y;
			pointerCoords[0].setAxisValue(MotionEvent.AXIS_VSCROLL, value);
			pointerCoords[0].setAxisValue(MotionEvent.AXIS_HSCROLL, 0);

			/* 閸掓稑缂撴稉锟介嚋濠婃俺鐤嗘禍瀣╂ */
			MotionEvent ponter_scroll = MotionEvent.obtain(
					SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
					MotionEvent.ACTION_SCROLL, 1, pointerProperties,
					pointerCoords, 0, 0, 1.0f, 1.0f, 0, 0,
					InputDevice.SOURCE_CLASS_POINTER, 0);

			Instrumentation inst = new Instrumentation();
			try {
				inst.sendPointerSync(ponter_scroll);
			} catch (SecurityException ie) {

			}
			// } else if (LetvUtils.getTvManufacturer() == LetvUtils.LETV) {
			// /* 濠婃俺鐤嗘稉濠佺瑓閺佹澘锟介懠鍐ㄦ纯閺勶拷1.0f 閸掞拷1.0f */
			// if (value > 1.0f) {
			// value = 1.0f;
			//
			// } else if (value < -1.0f) {
			// value = -1.0f;
			// }
			//
			// /* 閼惧嘲绶辫ぐ鎾冲姒х姵鐖ｉ崗澶嬬垼閻ㄥ嫪缍呯純锟� */
			//
			// IWindowManager windowManager = IWindowManager.Stub
			// .asInterface(ServiceManager.getService("window"));
			// float newX = 0;
			// float newY = 0;
			// try {
			//
			// // 閼惧嘲褰囪ぐ鎾冲姒х姵鐖ｉ崗澶嬬垼閹碉拷婀惃鍕秴缂冿拷
			// String tempStr = windowManager.getMousePos();
			// String[] oldStr = tempStr.split(",");
			// if (oldStr.length >= 2) {
			// newX = Float.parseFloat(oldStr[0]);
			// newY = Float.parseFloat(oldStr[1]);
			// }
			//
			// DisplayMetrics dm = new DisplayMetrics();
			// dm = context.getApplicationContext().getResources()
			// .getDisplayMetrics();
			//
			// int screenWidth = dm.widthPixels;
			// int screenHeight = dm.heightPixels;
			//
			// // 閼煎啫娲块崚銈嗘焽
			// if (newX < 0) {
			// newX = 0;
			// }
			// if (newY < 0) {
			// newY = 0;
			// }
			// if (newX > screenWidth) {
			// newX = screenWidth - 10;
			// }
			// if (newY > screenHeight) {
			// newY = screenHeight - 20;
			// }
			// ;
			// } catch (RemoteException e) {
			// e.printStackTrace();
			// }
			// /* 鐠佸墽鐤嗗姘崇枂娴滃娆㈢仦鐐达拷閸婏拷 */
			// int[] pointerIds = new int[1];
			// pointerIds[0] = 0;
			//
			// PointerProperties[] pointerProperties = new PointerProperties[1];
			// pointerProperties[0] = new PointerProperties();
			// pointerProperties[0].clear();
			// pointerProperties[0].id = 0;
			// pointerProperties[0].toolType = MotionEvent.TOOL_TYPE_MOUSE;
			//
			// PointerCoords[] pointerCoords = new PointerCoords[1];
			// pointerCoords[0] = new PointerCoords();
			// pointerCoords[0].clear();
			// pointerCoords[0].size = 1.0f;
			// pointerCoords[0].x = newX;
			// pointerCoords[0].y = newY;
			// pointerCoords[0].setAxisValue(MotionEvent.AXIS_VSCROLL, value);
			// pointerCoords[0].setAxisValue(MotionEvent.AXIS_HSCROLL, 0);
			//
			// /* 閸掓稑缂撴稉锟介嚋濠婃俺鐤嗘禍瀣╂ */
			// MotionEvent ponter_scroll = MotionEvent.obtain(
			// SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
			// MotionEvent.ACTION_SCROLL, 1, pointerProperties,
			// pointerCoords, 0, 0, 1.0f, 1.0f, 0, 0,
			// InputDevice.SOURCE_CLASS_POINTER, 0);
			//
			// Instrumentation inst = new Instrumentation();
			// try{
			// inst.sendPointerSync(ponter_scroll);
			// }catch(SecurityException ie){
			//
			// }
		} else {

			int scroll_y = 0;

			if (value == 1.0f) {
				scroll_y = -10;

			} else if (value == -1.0f) {
				scroll_y = 10;
			}
			DisplayMetrics dm = new DisplayMetrics();
			dm = context.getApplicationContext().getResources()
					.getDisplayMetrics();
			int screenWidth = dm.widthPixels;
			int screenHeight = dm.heightPixels;
			LetvLog.d("shixq", "device width " + screenWidth + " "
					+ screenHeight);
			int x = BackgroundService.x + screenWidth / 2;

			Message msg = Message.obtain();
			msg.what = BackgroundService.WHEEL_SCROLL;
			msg.arg1 = x;
			msg.arg2 = scroll_y;
			if (BackgroundService.handler != null)
				BackgroundService.handler.sendMessage(msg);
		}
	}

	/**
	 * 姒х姵鐖ｉ悙鐟板毊
	 */
	public static void touchScreen() {
		if (LetvUtils.haveSystemSigned()) {

			DisplayMetrics dm = new DisplayMetrics();
			dm = Engine.getInstance().getContext().getApplicationContext()
					.getResources().getDisplayMetrics();

			int screenWidth = dm.widthPixels;
			int screenHeight = dm.heightPixels;
			LetvLog.d("shixq", "device width " + screenWidth + " "
					+ screenHeight);
			int x = BackgroundService.x + screenWidth / 2;
			int y = BackgroundService.y + screenHeight / 2;
			LetvLog.d("shixq", "touch x = " + x + "touch y = " + y);
			Instrumentation inst = new Instrumentation();
			try {
				inst.sendPointerSync(MotionEvent.obtain(
						SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
						MotionEvent.ACTION_DOWN, x, y, 0));
				inst.sendPointerSync(MotionEvent.obtain(
						SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
						MotionEvent.ACTION_UP, x, y, 0));
			} catch (SecurityException ie) {

			}

			// }else if (LetvUtils.getTvManufacturer() == LetvUtils.LETV) {
			// IWindowManager windowManager = IWindowManager.Stub
			// .asInterface(ServiceManager.getService("window"));
			//
			// try {
			// float oldX = 0;
			// float oldY = 0;
			// // 閼惧嘲褰囪ぐ鎾冲姒х姵鐖ｉ崗澶嬬垼閹碉拷婀惃鍕秴缂冿拷
			// String tempStr = windowManager.getMousePos();
			// String[] oldStr = tempStr.split(",");
			// if (oldStr.length >= 2) {
			// oldX = Float.parseFloat(oldStr[0]);
			// oldY = Float.parseFloat(oldStr[1]);
			// }
			//
			// Instrumentation inst = new Instrumentation();
			// try{
			// inst.sendPointerSync(MotionEvent.obtain(
			// SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
			// MotionEvent.ACTION_DOWN, oldX, oldY, 0));
			// inst.sendPointerSync(MotionEvent.obtain(
			// SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
			// MotionEvent.ACTION_UP, oldX, oldY, 0));
			// }catch(SecurityException ie){
			//
			// }
			//
			// } catch (RemoteException e) {
			// e.printStackTrace();
			// }
		} else {
			// if (Build.VERSION.SDK_INT < 16) //16 = ANDROID4.1
			DisplayMetrics dm = new DisplayMetrics();
			dm = Engine.getInstance().getContext().getApplicationContext()
					.getResources().getDisplayMetrics();

			int screenWidth = dm.widthPixels;
			int screenHeight = dm.heightPixels;
			LetvLog.d("shixq", "device width " + screenWidth + " "
					+ screenHeight);
			int x = BackgroundService.x + screenWidth / 2;
			int y = BackgroundService.y + screenHeight / 2;

			Message msg = Message.obtain();
			msg.what = BackgroundService.MOUSE_PRESS;
			msg.arg1 = x;
			msg.arg2 = y;
			if (BackgroundService.handler != null)
				BackgroundService.handler.sendMessage(msg);
		}
	}

	/**
	 * 缁夎濮╂Η鐘崇垼
	 * 
	 * @param mMouseData
	 *            姒х姵鐖ｉ崸鎰垼閻ㄥ嫬浜哥粔锟�
	 */
	public static void moveMouseEvent(Context context, MouseData mMouseData) {

		/*
		 * if (LetvUtils.getTvManufacturer() == LetvUtils.LETV) {
		 * 
		 * IWindowManager windowManager = IWindowManager.Stub
		 * .asInterface(ServiceManager.getService("window")); if (context ==
		 * null) { LetvLog.e("KeyEventAction", "moveMouseEvent context = null");
		 * return; } try { float oldX = 0; float oldY = 0; //
		 * 閼惧嘲褰囪ぐ鎾冲姒х姵鐖ｉ崗澶嬬垼閹碉拷婀惃鍕秴缂冿拷 String tempStr =
		 * windowManager.getMousePos(); if (tempStr != null) { String[] oldStr =
		 * tempStr.split(",");
		 * 
		 * if (oldStr != null && oldStr.length >= 2) { oldX =
		 * Float.parseFloat(oldStr[0]); oldY = Float.parseFloat(oldStr[1]); } }
		 * 
		 * // 鐠侊紕鐣绘Η鐘崇垼閸忓鐖ｉ弬鎵畱娴ｅ秶鐤� float newX =
		 * Float.parseFloat(mMouseData.x) + oldX; float newY =
		 * Float.parseFloat(mMouseData.y) + oldY;
		 * 
		 * DisplayMetrics dm = new DisplayMetrics();
		 * 
		 * dm = context.getApplicationContext().getResources()
		 * .getDisplayMetrics(); LetvLog.e("KeyEventAction", " dm.ydpi = " +
		 * dm.ydpi + "dm.density = " + dm.density + "" + dm.densityDpi + "" +
		 * dm.densityDpi); int screenWidth = dm.widthPixels; int screenHeight =
		 * dm.heightPixels; try { screenWidth = Integer.valueOf(SystemProperties
		 * .get("const.window.w")); screenHeight =
		 * Integer.valueOf(SystemProperties .get("const.window.h")); } catch
		 * (NumberFormatException e) {
		 * 
		 * } LetvLog.e("KeyEventAction", "screenWidth = " + screenWidth +
		 * "screenHeight = " + screenHeight); LetvLog.e("KeyEventAction",
		 * "newX = " + newX + "newY = " + newY); // 閼煎啫娲块崚銈嗘焽 if (newX < 0) {
		 * newX = 0; } if (newY < 0) { newY = 0; } if (newX > screenWidth) {
		 * newX = screenWidth; // - 10; } if (newY > screenHeight) { newY =
		 * screenHeight; // - 20; }
		 * 
		 * // 鐠佸墽鐤嗘Η鐘崇垼閸忓鐖ｉ弬鎵畱娴ｅ秶鐤� windowManager.setMousePos(newX, newY);
		 * 
		 * } catch (RemoteException e) { e.printStackTrace(); } } else
		 */{
			float newX = Float.parseFloat(mMouseData.x);
			float newY = Float.parseFloat(mMouseData.y);

			Message msg = Message.obtain();
			msg.what = BackgroundService.DISPLAY_MOUSE;
			msg.arg1 = (int) newX;
			msg.arg2 = (int) newY;
			if (BackgroundService.handler != null)
				BackgroundService.handler.sendMessage(msg);
		}

	}

	/* 閸愬懎鐡ㄥ〒鍛倞 */
	public static void ClearMemory(Context context) {
		if (context == null) {
			LetvLog.e("KeyEventAction", "ClearMemory context = null");
			return;
		}
		Intent intent = new Intent();
		intent.setAction("com.letv.clearMemoryFromPhone");
		intent.putExtra("isFromPhone", true);
		context.sendBroadcast(intent);
	}

	public static void sendControlActionSeekToDmr(String seektime) {

		DmrInterfaceManage.getInstance().setSeek(seektime);
	}

	public static void sendControlActionToDmr(String cmd) {

		if (cmd.startsWith("SetVolume")) {
			int len = "SetVolume".length();
			String volume = cmd.substring(len + 1);
			DmrInterfaceManage.getInstance().setVolume(Integer.valueOf(volume));

		} else if (cmd.startsWith("Play") || cmd.startsWith("Pause")) {
			LetvLog.e("KeyEventAction",
					"sendControlActionToDmr = pause or play");
			DmrInterfaceManage.getInstance().setAction(
					MediaplayerConstants.PLAYORPAUSE);

		} else if (cmd.startsWith("SetMute")) {
			DmrInterfaceManage.getInstance().setAction(
					MediaplayerConstants.SETMUTE);

		} else if (cmd.startsWith("Stop")) {
			DmrInterfaceManage.getInstance().setAction(
					MediaplayerConstants.STOP);
		}
	}

	public static int PlayVideoByUrl(String url) {

		int ret = DmrInterfaceManage.getInstance().setUrl(url, 0, 1);
		return ret;
	}

	static private class TvVideoParams {
		String vId = "";// 瑜拌京澧杋d
		String streamCode = "";// 閻焦绁﹂惍锟�
		String streamName = "";// 閻焦绁﹂崥宥囆�
		String videoTitle = "";// 娑撴捁绶崥宥囆�
		String series = "";// 缁楊剙鍤戦梿锟�
		String seriesSize = ""; // 閹娉﹂弫锟�
		String duration = ""; // 瑜拌京澧栭幐浣虹敾閺冨爼妫�
		String category = "";// 缁鍩唅d

		TvVideoParams(String vid, String streamCode, String streamName,
				String videoTitle, String series, String seriesSize,
				String duration, String category) {
			this.vId = vid;
			this.streamCode = streamCode;
			this.streamName = streamName;
			this.videoTitle = videoTitle;
			this.series = series;
			this.seriesSize = seriesSize;
			this.duration = duration;
			this.category = category;
		}

	}

	public static int SendRecommendedVideo(Context context, String info) {

		if (context == null) {
			LetvLog.e("KeyEventAction", "SendRecommendedVideo context = null");
			return -1;
		}
		/* 鐠囷附鍎忔い闈涘棘閺侊拷 */
		String channelId = "";
		String channelCode = "";
		String iptvalbumid = "";
		String vrsalbumid = "";
		String broadcastId = "";
		/* 閻╁瓨甯寸憴鍡涱暥閹绢厽鏂侀崣鍌涙殶 */
		String vId = "";
		String streamCode = "";
		String streamName = "";
		String videoTitle = "";
		String series = "";
		String seriesSize = "";
		String duration = "";
		String category = "";

		LetvLog.e("KeyEventAction", "jsonstr = " + info);
		try {

			JSONObject jo = new JSONObject(info);

			for (int i = 0; i < jo.length(); i++) {

				/* 鐟欏棝顣剁拠锔剧矎妞ょ敻娼伴崣鍌涙殶鐟欙絾鐎� */
				if (jo.has("iptvAlbumId")) {
					iptvalbumid = jo.getString("iptvAlbumId");
				}
				if (jo.has("channelCode")) {
					channelCode = jo.getString("channelCode");
				}

				if (jo.has("broadcastId")) {
					broadcastId = jo.getString("broadcastId");
				}
				/* 鐟欏棝顣堕幘顓熸杹閸欏倹鏆熺憴锝嗙� */
				if (jo.has("PLAY_LESO_VID")) {
					vId = jo.getString("PLAY_LESO_VID");
				}
				if (jo.has("PLAY_LESO_STREAM_CODE")) {
					streamCode = jo.getString("PLAY_LESO_STREAM_CODE");
				}
				if (jo.has("PLAY_LESO_STREAM_NAME")) {
					streamName = jo.getString("PLAY_LESO_STREAM_NAME");
				}
				if (jo.has("PLAY_LESO_ALBUMNAME")) {
					videoTitle = jo.getString("PLAY_LESO_ALBUMNAME");
				}
				if (jo.has("PLAY_LESO_VIDEO_SERIESNUM")) {
					series = jo.getString("PLAY_LESO_VIDEO_SERIESNUM");
				}
				if (jo.has("PLAY_LESO_VIDEO_SERIESSUM")) {
					seriesSize = jo.getString("PLAY_LESO_VIDEO_SERIESSUM");
				}
				if (jo.has("PLAY_LESO_VIDEO_DURATION")) {
					duration = jo.getString("PLAY_LESO_VIDEO_DURATION");
				}
				if (jo.has("PLAY_LESO_VIDEO_CATEGORYID")) {
					category = jo.getString("PLAY_LESO_VIDEO_CATEGORYID");
				}

			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		LetvLog.e("KeyEventAction", "vId = " + vId + "streamCode = "
				+ streamCode + "streamName = " + streamName + "videoTitle ="
				+ videoTitle + "series = " + series + "seriesSize = "
				+ seriesSize + "category = " + category + "duration = "
				+ duration);
		if (!series.isEmpty()) {
			openLetvVideo(context, new TvVideoParams(vId, streamCode,
					streamName, videoTitle, series, seriesSize, duration,
					category));
		} else {
			openLetvVideo(context, channelId, channelCode, iptvalbumid,
					vrsalbumid, broadcastId);
		}
		return 1;
	}

	public static void openLetvVideo(Context context, TvVideoParams videoParams) {

		Intent intent = new Intent(LETV_ACTION);
		Bundle extras = new Bundle();

		extras.putString("PLAY_LESO_VID", videoParams.vId);
		extras.putString("PLAY_LESO_STREAM_CODE", videoParams.streamCode);
		extras.putString("PLAY_LESO_STREAM_NAME", videoParams.streamName);
		extras.putString("PLAY_LESO_ALBUMNAME", videoParams.videoTitle);
		extras.putString("PLAY_LESO_VIDEO_NAME", videoParams.videoTitle + " 缁�"
				+ videoParams.series + "");// 瑜版挸澧犻梿鍡楁倳缁�
		extras.putString("PLAY_LESO_VIDEO_SERIESNUM", videoParams.series);
		extras.putString("PLAY_LESO_VIDEO_SERIESSUM", videoParams.seriesSize);
		extras.putString("PLAY_LESO_VIDEO_DURATION", videoParams.duration);
		extras.putString("PLAY_LESO_CATEGORYID", videoParams.category);

		intent.putExtras(extras);
		context.sendBroadcast(intent);
	}

	/**
	 * @param context
	 * @param albumId
	 * @param channelCode
	 * @param broadcastId
	 *            楠炴寧鎸遍惃鍒焎tion閿涳拷com.letv.external.launch.channeldetail
	 *            楠炴寧鎸遍幍锟芥付閻ㄥ嫬鐡у▓纰夌礉婵″倷绗呴敍锟�*
	 *            娑撴捁绶獻D閿涙ptvalbumid閿涘苯绻�繅顐礉Long閸ㄥ绱濇稉宥夋付鐟曚焦妞傛导锟�
	 *            妫版垿浜綢D閿涙瓭hannelid閿涘苯绻�繅顐礉Int閸ㄥ绱濇稉宥夋付鐟曚焦妞傛导锟�
	 *            妫版垿浜綜ODE閿涙瓭hannelcode閿涘苯绻
	 *            �繅顐礉鐎涙顑佹稉璇х礉娑撳秹娓剁憰浣规娴肩垔ull閹存牑锟介垾锟�*
	 *            濞夘煉绱版潻娑樺弳鐠囷附鍎忔い鐢告付鐟曚椒绗撴潏鎱朌閿涘矁绻橀崗銉╊暥闁捇銆夐棁锟筋渽妫版垿浜綢D閸滃矂顣堕柆鎻匫DE
	 */
	public static void openLetvVideo(Context context, String channelId,
			String channelCode, String iptvalbumid, String vrsalbumid,
			String broadcastId) {

		LetvLog.d("openLetvVideo(iptvalbumid=" + iptvalbumid + ", vrsalbumid="
				+ vrsalbumid + ", channelCode=" + channelCode + ", channelId="
				+ channelId + ", broadcastId=" + broadcastId);

		Intent intent = new Intent("com.letv.external.launch.channeldetail");
		if (TextUtils.isEmpty(iptvalbumid)) {
			intent.putExtra("iptvalbumid", -1L);
		} else {
			try {
				intent.putExtra("iptvalbumid", Long.parseLong(iptvalbumid));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (TextUtils.isEmpty(vrsalbumid)) {
			intent.putExtra("vrsalbumid", -1L);
		} else {
			try {
				intent.putExtra("vrsalbumid", Long.parseLong(vrsalbumid));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		intent.putExtra("channelcode", channelCode);
		intent.putExtra("broadcastId", broadcastId);

		context.sendBroadcast(intent);
	}

	public static void addTextToEditText(Context context, String data) {
		// 鐠嬪啰鏁ら弴瀛樻暭鏉╁洨娈戞潏鎾冲弳濞夋毜锟�
		if (context == null) {
			LetvLog.e("KeyEventAction", "SettingEvent context = null");
			return;
		}
		Intent intent = new Intent();
		intent.setAction("com.letv.TEXT_RECEIVE");
		intent.putExtra("curentText", data);
		context.sendBroadcast(intent);
	}

	/* 鏉╂粎鈻肩�澶庮棖 */
	public static int InstallPackage(Context context, String packageId) {

		if (context == null) {
			LetvLog.e("KeyEventAction", "SettingEvent context = null");
			return 0;
		}

		Intent intent = new Intent();

		LetvLog.e("KeyEventAction", "install apk info = " + packageId);
		JSONObject jsonObject = null;
		String url = null;
		String fileName = null;
		String packageName = null;
		try {
			jsonObject = new JSONObject(packageId);
			fileName = jsonObject.getString("app_name");
			url = jsonObject.getString("apk_url");
			packageName = jsonObject.getString("apk_package");
		} catch (JSONException e) {
			e.printStackTrace();

			intent.setAction("com.letv.appstore.c2tv");
			intent.putExtra("cmd", "install");
			intent.putExtra("type", "apk");
			try {
				intent.putExtra("appID", Long.valueOf(packageId));
			} catch (NumberFormatException e1) {

			}

			// LetvLog.e("KeyEventAction", "appID = " +
			// Long.valueOf(packageId));
			context.sendBroadcast(intent);
			return 1;
		}
		intent.setClass(context, ShowApkInstallProgress.class);
		intent.putExtra("url", url);
		intent.putExtra("fileName", fileName);
		intent.putExtra("packageName", packageName);

		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
		context.startActivity(intent);
		return 1;

	}

	/* UDP dlna push */
	public static void SetDmrInfo(String url, String mediaData) {

		DmrInterfaceManage.getInstance().actionControlReceivedByUDP(url,
				mediaData);
	}

	/*start lunch app*/
	public void srartApp(Context context, String packageName) {
		Intent intent = context.getPackageManager().getLaunchIntentForPackage(
				packageName);
		if (intent != null) {
			context.startActivity(intent);
		}
	}

}
