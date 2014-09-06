package com.letv.accountLogin.netty;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.KeyEvent;
import com.letv.accountLogin.protobuf.ControlDataProto.Parcel;
import com.letv.dmr.DmrInterfaceManage;
import com.letv.upnpControl.dlna.jni_interface;
import com.letv.upnpControl.entity.MouseData;
import com.letv.upnpControl.service.ListenNetWorkService;
import com.letv.upnpControl.tools.Constants;
import com.letv.upnpControl.tools.Engine;
import com.letv.upnpControl.tools.JsonParser;
import com.letv.upnpControl.tools.KeyEventAction;
import com.letv.upnpControl.tools.LetvLog;


@SuppressLint("DefaultLocale")
public class OperationCtrlData {
	private static Context mContext;
	private static MouseData mMouseData;
	private static int NET_VIDEO_PUSH_REPLAY = 0;
	// private static int NET_VIDEO_PUSH_PAUSE = 1;
	private static int NET_VIDEO_PUSH_STOP = 2;
	private static int NET_VIDEO_PUAH_PLAY_OR_PAUSE = 8;
	public static String ip;
	public static final String TAG = OperationCtrlData.class.getSimpleName();

	public static void operate(Context context, Parcel parcel) {
		// to = parcel.getFrom();
		if (jni_interface.TvGetUpnpDeviceStatus() == false) {
			return;
		}
		Engine.setTo(parcel.getFrom());
		mContext = context;
		LetvLog.d(TAG, "Receivedparcel.getMsgInfo() = " + parcel.getMsgInfo());
		if (parcel.getMsgInfo().equals(Constants.CtrlType.CONTROL)) {
			int keyCode = parcel.getKeyCode();
			LetvLog.d(TAG, "Receivedparcel.keyCode = " + keyCode);
			switch (keyCode) {
			case Constants.CtrlKeyCode.UP:
				KeyEventAction.simulateKeystroke(KeyEvent.KEYCODE_DPAD_UP);
				break;
			case Constants.CtrlKeyCode.DOWN:
				KeyEventAction.simulateKeystroke(KeyEvent.KEYCODE_DPAD_DOWN);
				break;
			case Constants.CtrlKeyCode.LEFT:
				KeyEventAction.simulateKeystroke(KeyEvent.KEYCODE_DPAD_LEFT);
				break;
			case Constants.CtrlKeyCode.RIGHT:
				KeyEventAction.simulateKeystroke(KeyEvent.KEYCODE_DPAD_RIGHT);
				break;
			case Constants.CtrlKeyCode.OK:
				KeyEventAction.simulateKeystroke(KeyEvent.KEYCODE_DPAD_CENTER);
				break;
			case Constants.CtrlKeyCode.RETURN:
				KeyEventAction.simulateKeystroke(KeyEvent.KEYCODE_BACK);
				break;
			case Constants.CtrlKeyCode.HOME:
				KeyEventAction.simulateKeystroke(KeyEvent.KEYCODE_HOME);
				break;
			case Constants.CtrlKeyCode.MENU:
				KeyEventAction.simulateKeystroke(KeyEvent.KEYCODE_MENU);
				break;
			case Constants.CtrlKeyCode.NUM_0:
				KeyEventAction.simulateKeystroke(KeyEvent.KEYCODE_0);
				break;
			case Constants.CtrlKeyCode.NUM_1:
				KeyEventAction.simulateKeystroke(KeyEvent.KEYCODE_1);
				break;
			case Constants.CtrlKeyCode.NUM_2:
				KeyEventAction.simulateKeystroke(KeyEvent.KEYCODE_2);
				break;
			case Constants.CtrlKeyCode.NUM_3:
				KeyEventAction.simulateKeystroke(KeyEvent.KEYCODE_3);
				break;
			case Constants.CtrlKeyCode.NUM_4:
				KeyEventAction.simulateKeystroke(KeyEvent.KEYCODE_4);
				break;
			case Constants.CtrlKeyCode.NUM_5:
				KeyEventAction.simulateKeystroke(KeyEvent.KEYCODE_5);
				break;
			case Constants.CtrlKeyCode.NUM_6:
				KeyEventAction.simulateKeystroke(KeyEvent.KEYCODE_6);
				break;
			case Constants.CtrlKeyCode.NUM_7:
				KeyEventAction.simulateKeystroke(KeyEvent.KEYCODE_7);
				break;
			case Constants.CtrlKeyCode.NUM_8:
				KeyEventAction.simulateKeystroke(KeyEvent.KEYCODE_8);
				break;
			case Constants.CtrlKeyCode.NUM_9:
				KeyEventAction.simulateKeystroke(KeyEvent.KEYCODE_9);
				break;
			case Constants.CtrlKeyCode.VOLUME_DOWN:
				KeyEventAction.simulateKeystroke(KeyEvent.KEYCODE_VOLUME_DOWN);
				break;
			case Constants.CtrlKeyCode.VOLUME_UP:
				KeyEventAction.simulateKeystroke(KeyEvent.KEYCODE_VOLUME_UP);
				break;
			case Constants.CtrlKeyCode.CHANNEL_DOWN:
				KeyEventAction.simulateKeystroke(KeyEvent.KEYCODE_CHANNEL_DOWN);
				break;
			case Constants.CtrlKeyCode.CHANNEL_UP:
				KeyEventAction.simulateKeystroke(KeyEvent.KEYCODE_CHANNEL_UP);
				break;
			case Constants.CtrlKeyCode.MUTE:
				KeyEventAction.simulateKeystroke(KeyEvent.KEYCODE_VOLUME_MUTE);
				break;
			case Constants.CtrlKeyCode.SETTING:
				KeyEventAction.SettingEvent(Engine.getInstance().getContext());
				break;
			case Constants.CtrlKeyCode.MEMORY_CLEAR:
				KeyEventAction.ClearMemory(Engine.getInstance().getContext());
				break;
			case Constants.CtrlKeyCode.POWER:
				KeyEventAction.PowerOff(Engine.getInstance().getContext());
				break;
			}
		} else {
			parseMsg(parcel.getMsgInfo());
		}

	}

	private static void parseMsg(String str) {
		String type;
		int len;
		LetvLog.d(TAG, "str:" + str);
		if (str.startsWith(Constants.CtrlType.INPUT_TEXT)) {
			type = Constants.CtrlType.INPUT_TEXT;
			len = type.length();
			String value = str.substring(len + 1);
			KeyEventAction.addTextToEditText(mContext, value);
		} else if (str.startsWith(Constants.CtrlType.MOUSE_MOVE)) {
			type = Constants.CtrlType.MOUSE_MOVE;
			len = type.length();
			mMouseData = JsonParser.parseMouseData(str.substring(len + 1));
			KeyEventAction.moveMouseEvent(mContext, mMouseData);
		} else if (str.startsWith(Constants.CtrlType.MOUSE_PRESS)) {
			type = Constants.CtrlType.MOUSE_PRESS;
			len = type.length();
			KeyEventAction.touchScreen();
		} else if (str.startsWith(Constants.CtrlType.WHEEL_DOWN)) {
			type = Constants.CtrlType.WHEEL_DOWN;
			len = type.length();
			MouseData mMouseData = new MouseData();
			mMouseData.x = String.valueOf(0);
			mMouseData.y = String.valueOf(-1.0);
			KeyEventAction.MouseWheelEvent(Engine.getInstance().getContext(),
					mMouseData);
		} else if (str.startsWith(Constants.CtrlType.WHEEL_UP)) {
			type = Constants.CtrlType.WHEEL_UP;
			len = type.length();
			MouseData mMouseData = new MouseData();
			mMouseData.x = String.valueOf(0);
			mMouseData.y = String.valueOf(1.0);
			KeyEventAction.MouseWheelEvent(Engine.getInstance().getContext(),
					mMouseData);
		} else if (str.startsWith(Constants.NET_VIDEO_PUSH_START)) {
			type = Constants.NET_VIDEO_PUSH_START;
			len = type.length();
			String url = str.substring(len + 1);
			LetvLog.d(TAG, "--URL is --" + url + "--");
	
  		   DmrInterfaceManage.getInstance().setUrl(url, 0, 0);

		} else if (str.startsWith(Constants.NET_VIDEO_PUSH_STOP)) {

			DmrInterfaceManage.getInstance().setAction(NET_VIDEO_PUSH_STOP);

		} else if (str.startsWith(Constants.NET_VIDEO_PUSH_PAUSE_OR_PLAY)) {

			DmrInterfaceManage.getInstance().setAction(
					NET_VIDEO_PUAH_PLAY_OR_PAUSE);

		} else if (str.startsWith(Constants.NET_VIDEO_PUSH_SET_MUTE)) {

			DmrInterfaceManage.getInstance().Mute();

		} else if (str.startsWith(Constants.NET_VIDEO_PUSH_REPLAY)) {

			DmrInterfaceManage.getInstance().setAction(NET_VIDEO_PUSH_REPLAY);

		} else if (str.startsWith(Constants.NET_VIDEO_PUSH_SET_VOLUME)) {
			type = Constants.NET_VIDEO_PUSH_SET_VOLUME;
			len = type.length();
			int volumeProgress = Integer.parseInt(str.substring(len + 1));

			DmrInterfaceManage.getInstance().setVolume(volumeProgress);

		} else if (str.startsWith(Constants.NET_VIDEO_PUSH_SEEK)) {

			type = Constants.NET_VIDEO_PUSH_SEEK;
			len = type.length();
			int progress = Integer.parseInt(str.substring(len + 1));
			int totalTime = DmrInterfaceManage.getInstance().getTotalTime() / 1000;
			int seekTime = totalTime * progress / 100;
			DmrInterfaceManage.getInstance().setSeek(toTimeFormat(seekTime));

		} else if (str.startsWith(Constants.NET_VIDEO_PUSH_SEEK30S)) {
			type = Constants.NET_VIDEO_PUSH_SEEK30S;
			len = type.length();
			int flag = Integer.parseInt(str.substring(len + 1));

			int totalTime = 0;
			int currentTime = 0;
			int seekTime = 0;

			totalTime = DmrInterfaceManage.getInstance().getTotalTime() / 1000;;
			currentTime = DmrInterfaceManage.getInstance().getCurrentPosition() / 1000;
			if (flag == 0) {// seek foward
				seekTime = currentTime + 30;
				if (seekTime > totalTime) {
					seekTime = totalTime;
				}
			} else if (flag == 1) {// seek back
				seekTime = currentTime - 30;
				if (seekTime < 0) {
					seekTime = 0;
				}
			}

			DmrInterfaceManage.getInstance().setSeek(toTimeFormat(seekTime));

		} else if (str.startsWith(Constants.NET_VIDEO_PUSH_GET_DMR_STATE)) {
	
			int total_time = 0;
			int pos = 0;
			String state = null;
			int volume = 0;
			int muteState = 0;

			total_time = DmrInterfaceManage.getInstance().getTotalTime();
			pos = DmrInterfaceManage.getInstance().getCurrentPosition();
			state = DmrInterfaceManage.getInstance().getCurrentTransportState();
			volume = DmrInterfaceManage.getInstance().getVolume();
			if (DmrInterfaceManage.getInstance().getMute()) {
				muteState = 1;
			} else {
				muteState = 0;
			}

			if (state == null) {
				state = "";
			}
			String msg = "get_dmr_state&" + "total_time:" + total_time + "&"
					+ "current_time:" + pos + "&" + "state:" + state + "&"
					+ "volume:" + volume + "&" + "mute:" + muteState;
			TvMessageClient.sendDataByNetty(Constants.SEND_DATA_TYPE, Engine
					.getInstance().getFrom(), Engine.getInstance().getToken(),
					msg, mContext, Engine.getInstance().getSignature());
		} else if (str.startsWith(Constants.CtrlType.PHONE_ONLINE)) {
			type = Constants.CtrlType.PHONE_ONLINE;
			len = type.length();
			ip = str.substring(len + 1);
			if (ip != null) {
				LetvLog.d(TAG, "phone_online ip is--" + ip);
				ListenNetWorkService.sendImageOnline();
			}
		} else if (str.startsWith(Constants.NET_VIDEO_SEARCH)) {
			type = Constants.NET_VIDEO_SEARCH;
			len = type.length();
			String url = str.substring(len + 1);
			if (url != null && url.length() > 0) {
				KeyEventAction.PlayVideoByUrl(url);

			}
		} else if (str.startsWith(Constants.NET_VIDEO_RECOMMEND)) {
			type = Constants.NET_VIDEO_RECOMMEND;
			len = type.length();
			String url = str.substring(len + 1);
			if (url != null && url.length() > 0) {
				KeyEventAction.SendRecommendedVideo(mContext, url);
			}

		} else if (str.startsWith(Constants.NET_APP_RECOMMEND)) {

			type = Constants.NET_APP_RECOMMEND;
			len = type.length();
			String appId = str.substring(len + 1);
			if (appId != null && appId.length() > 0) {

				KeyEventAction.InstallPackage(mContext, appId);
			}
		}
	}

	private static String toTimeFormat(int time) {

		int minute = time / 60;
		int hour = minute / 60;
		int second = time % 60;
		minute %= 60;
		return String.format("%02d:%02d:%02d", hour, minute, second);
	}
}
