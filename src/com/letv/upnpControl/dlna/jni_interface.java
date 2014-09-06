package com.letv.upnpControl.dlna;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.KeyEvent;
import com.letv.upnpControl.tools.Constants;
import com.letv.upnpControl.tools.Engine;
import com.letv.upnpControl.tools.KeyEventAction;
import com.letv.upnpControl.tools.LetvLog;
import com.letv.upnpControl.tools.LetvUtils;
import com.letv.upnpControl.entity.MouseData;

/**
 * @title: 
 * @description: 设备端的JNI 接口
 * @company: 乐视网信息技术（北京）股份有限公司 
 * @author 史学强
 * @version 1.0
 * @created 2012-12-05
 * @changeRecord
 */
public class jni_interface{
	
    /*控制命令处理*/
	public static void TvSendCtrAction (String actionName){

		LetvLog.d("jni_interface", "TvSendCtrAction = " + actionName);  
		if(Constants.CtrlType.MOUSE_PRESS.equals(actionName)) {
			KeyEventAction.touchScreen();
		}
		else if (Constants.CtrlType.UP.equals(actionName)) {
			KeyEventAction.simulateKeystroke(KeyEvent.KEYCODE_DPAD_UP);
			
		} else if (Constants.CtrlType.DOWN.equals(actionName)) {
			KeyEventAction.simulateKeystroke(KeyEvent.KEYCODE_DPAD_DOWN);
			
		} else if (Constants.CtrlType.LEFT.equals(actionName)) {
			KeyEventAction.simulateKeystroke(KeyEvent.KEYCODE_DPAD_LEFT);
			
		} else if (Constants.CtrlType.RIGHT.equals(actionName)) {
			KeyEventAction.simulateKeystroke(KeyEvent.KEYCODE_DPAD_RIGHT);
			
		} else if (Constants.CtrlType.OK.equals(actionName)) {
			if (LetvUtils.getTvManufacturer() == LetvUtils.XIAOMI) {
				KeyEventAction.simulateKeystroke(KeyEvent.KEYCODE_ENTER);
			}else{
				KeyEventAction.simulateKeystroke(KeyEvent.KEYCODE_DPAD_CENTER);
			}
			
		} else if (Constants.CtrlType.RETURN.equals(actionName)) {
			KeyEventAction.simulateKeystroke(KeyEvent.KEYCODE_BACK);
			
		} else if (Constants.CtrlType.HOME.equals(actionName)) {
			KeyEventAction.simulateKeystroke(KeyEvent.KEYCODE_HOME);
		} else if (Constants.CtrlType.POWER.equals(actionName)) {
			//KeyEventAction.simulateKeystroke(KeyEvent.KEYCODE_POWER);
			KeyEventAction.PowerOff(Engine.getInstance().getContext());
		} else if (Constants.CtrlType.MENU.equals(actionName)) {
			KeyEventAction.simulateKeystroke(KeyEvent.KEYCODE_MENU);
			
		}else if(Constants.CtrlType.WHEEL_DOWN.equals(actionName)){
			MouseData mMouseData = new MouseData();
			mMouseData.x = String.valueOf(0);
			mMouseData.y = String.valueOf(-1.0);
			KeyEventAction.MouseWheelEvent(Engine.getInstance().getContext(),mMouseData);
			
		}else if(Constants.CtrlType.WHEEL_UP.equals(actionName)){
			MouseData mMouseData = new MouseData();
			mMouseData.x = String.valueOf(0);
			mMouseData.y = String.valueOf(1.0);
			KeyEventAction.MouseWheelEvent(Engine.getInstance().getContext(),mMouseData);
			
		}else if(Constants.CtrlType.SETTING.equals(actionName)){	
			KeyEventAction.SettingEvent(Engine.getInstance().getContext());
			
		}else if (Constants.CtrlType.VOLUME_DOWN.equals(actionName)) {
			KeyEventAction.simulateKeystroke(KeyEvent.KEYCODE_VOLUME_DOWN);
			
		}else if (Constants.CtrlType.VOLUME_UP.equals(actionName)) {
			KeyEventAction.simulateKeystroke(KeyEvent.KEYCODE_VOLUME_UP);
			
		}else if (Constants.CtrlType.CHANNEL_DOWN.equals(actionName)) {
			KeyEventAction.simulateKeystroke(KeyEvent.KEYCODE_CHANNEL_DOWN);
			
		}else if (Constants.CtrlType.CHANNEL_UP.equals(actionName)) {
			KeyEventAction.simulateKeystroke(KeyEvent.KEYCODE_CHANNEL_UP);
			
		}else if (Constants.CtrlType.NUM_0.equals(actionName)) {
			KeyEventAction.simulateKeystroke(KeyEvent.KEYCODE_0);
			
		}else if (Constants.CtrlType.NUM_1.equals(actionName)) {
			KeyEventAction.simulateKeystroke(KeyEvent.KEYCODE_1);
			
		}else if (Constants.CtrlType.NUM_2.equals(actionName)) {
			KeyEventAction.simulateKeystroke(KeyEvent.KEYCODE_2);
			
		}else if (Constants.CtrlType.NUM_3.equals(actionName)) {
			KeyEventAction.simulateKeystroke(KeyEvent.KEYCODE_3);
			
		}else if (Constants.CtrlType.NUM_4.equals(actionName)) {
			KeyEventAction.simulateKeystroke(KeyEvent.KEYCODE_4);
			
		}else if (Constants.CtrlType.NUM_5.equals(actionName)) {
			KeyEventAction.simulateKeystroke(KeyEvent.KEYCODE_5);
			
		}else if (Constants.CtrlType.NUM_6.equals(actionName)) {
			KeyEventAction.simulateKeystroke(KeyEvent.KEYCODE_6);
			
		}else if (Constants.CtrlType.NUM_7.equals(actionName)) {
			KeyEventAction.simulateKeystroke(KeyEvent.KEYCODE_7);
			
		}else if (Constants.CtrlType.NUM_8.equals(actionName)) {
			KeyEventAction.simulateKeystroke(KeyEvent.KEYCODE_8);
			
		}else if (Constants.CtrlType.NUM_9.equals(actionName)) {
			KeyEventAction.simulateKeystroke(KeyEvent.KEYCODE_9);
	
		}else if (Constants.CtrlType.MUTE.equals(actionName)) {
			KeyEventAction.simulateKeystroke(KeyEvent.KEYCODE_VOLUME_MUTE);
		}else if(Constants.CtrlType.CLEAR_MEMORY.equals(actionName)){
			KeyEventAction.ClearMemory(Engine.getInstance().getContext());
		}else {
			KeyEventAction.sendControlActionToDmr(actionName);
		}
	}
	
	/*鼠标移动命令处理*/
	public static void TvSendMouseAction (String actionName,int x,int y){
		LetvLog.d("jni_interface", "TvSendMouseAction = " + x + y);  
		MouseData mMouseData = new MouseData();
		mMouseData.x = String.valueOf(x);
		mMouseData.y = String.valueOf(y);
		KeyEventAction.moveMouseEvent(Engine.getInstance().getContext(), mMouseData);
	
	}
	public static void TvSendMouseActionByUdp (int x,int y){
		LetvLog.d("jni_interface", "TvSendMouseActionByUdp = " + x + y);  
		MouseData mMouseData = new MouseData();
		mMouseData.x = String.valueOf(x);
		mMouseData.y = String.valueOf(y);
		KeyEventAction.moveMouseEvent(Engine.getInstance().getContext(), mMouseData);
	
	}
	
	/*输入文件命令处理*/ 
	public static void TvSendInputValueAction (String actionName,String value){
		LetvLog.d("jni_interface", "TvSendInputValueAction = " + actionName +"  value ="+ value);  
		KeyEventAction.addTextToEditText(Engine.getInstance().getContext(),value);	
	}

	/*播放网络视频*/ 
	public static int TvSendPlayUrl (String actionName,String url){
		LetvLog.d("jni_interface", "TvSendPlayUrl = " + actionName +"  url ="+ url);  
		return KeyEventAction.PlayVideoByUrl(url);	
	}
	
	/*播放推荐网络视频*/
	public static int TvSendRecommendedVideo (String actionName,String info){
		LetvLog.d("jni_interface", "TvSendRecommendedVideo = " + actionName +"  info ="+ info);  
		return KeyEventAction.SendRecommendedVideo(Engine.getInstance().getContext(),info);	
	}
	/*安装应用*/ 
	public static int TvInstallPackage(String actionName,String packageId){
		LetvLog.d("jni_interface", "TvInstallPackageUrl = " + actionName +"  packageId ="+ packageId);  
		return KeyEventAction.InstallPackage(Engine.getInstance().getContext(),packageId);	
	}
	public static void TvSendDmrInfo(String url,String mediaData){
	  LetvLog.d("jni_interface", "TvSendDmrInfo url= " + url);
		KeyEventAction.SetDmrInfo(url,mediaData);
	}
	
	/* 获取当前dlna是否开启 */
	public static boolean TvGetUpnpDeviceStatus() {
		Context context = Engine.getInstance().getContext();
		if(context == null){
			return true;
		}
		SharedPreferences sp = context.getSharedPreferences("DeviceStatus",
				Context.MODE_PRIVATE);
		boolean status = sp.getBoolean("isOn", true);
		LetvLog.d("TAG","TvGetUpnpDeviceStatus = " + status);
		return status;
	}
}
