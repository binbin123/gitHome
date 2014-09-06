package com.letv.airplay;


import com.letv.airplay.JniInterface.MediaPlayerArg;
import com.letv.upnpControl.tools.LetvLog;
import com.letv.upnpControl.tools.LetvUtils;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.provider.Settings;
/**
 * 接收断网事件，从Java通过JNI调用C方法
 * @author 李振
 * @modify 韦念欣
 * @modify Jamin
 */
public class AirplayService extends Service {

	private final String TAG = AirplayService.class.getSimpleName();

	public static final String ACTION_CONNECTIVITY_CHANGE 		= "android.net.conn.CONNECTIVITY_CHANGE";
	public static final String ACTION_WIFI_AP_STATE_CHANGED 	= "android.net.wifi.WIFI_AP_STATE_CHANGED";
	public static final String ACTION_WIFI_STATE_CHANGED 	= "android.net.wifi.WIFI_STATE_CHANGED";
	public static final String ACTION_ETH_STATE_CHANGED		= "android.net.ethernet.ETH_STATE_CHANGED";
	public static final String ACTION_CHANGE_NAME_SC				= "com.smartControl.action.changeName";
	public static final String ACTION_CHANGE_NAME_SYS				= "com.letv.action.changeName";
	public static final String ACTION_MSS_CHANGE				= "com.letv.t2.globalsetting.multiscreenstatechange";
	
	public static final int IDLE		= -1;
	public static final int PLAY 	= 0;
	public static final int PAUSE	= 1;
	public static final int CACHE	= 2;
	public static final int STOP	  	= 3;
	
	public static final int STOP_NORNAL = 0;
	public static final int STOP_COMPLETE = 1;
	public static final int STOP_HOME = 2;
	public static final int STOP_BACK = 3;
	public static final int STOP_ERROR = 4;
	

	private boolean IsOn = false; //配置
	

	private LinkProxy proxy = null; 
	//private KillService ks = null;
	
	SharedPreferences sp = null;
	private final String ONOFF = "ONOFF_KEY"; 
	private final int ON = 1;
	private final int OFF = 0;
	private final int EMPTY = 2;
	private final int ERR = -1;
	
	private NetStateBroadCastReceiver netStateBroadCastReceiver;


	public void startActivity(int appId, Object arg) {
		LetvLog.d(TAG, "Airplay startActivity:" + appId);

		Intent intent = new Intent();
		Class<?> cls = null;
		if (appId == JniInterface.VIDEO_PLAYER_ID) {
			cls = AirplayMediaPlayerActivity.class;
			intent.putExtra("playerType", AirplayMediaPlayerActivity.VIDEO);
			intent.putExtra("mediaUrl", ((MediaPlayerArg)arg).getMediaUrl());
			intent.putExtra("percent", ((MediaPlayerArg)arg).getPercent());
			intent.putExtra("videoId", ((MediaPlayerArg)arg).getVideoId());
		} else if (appId == JniInterface.AUDIO_PLAYER_ID) {
			cls = AirplayMediaPlayerActivity.class;
			intent.putExtra("playerType", AirplayMediaPlayerActivity.MUSIC);
		} else if (appId == JniInterface.PICTURE_SHOW_ID) {
			cls = AirplayPictureShowActivity.class;
			intent.putExtra("pictureId", ((PicturePlayerArg)arg).getPictureId());
			intent.putExtra("pictureType", ((PicturePlayerArg)arg).getPictureType());
			//intent.putExtra("picData", data);
		}else if(appId == JniInterface.MIRRORING_PLAYER_ID){
			cls = MirroringPlayerActivity.class;
			intent.putExtra("type", ((MirroringPlayerArg)arg).getType());
			intent.putExtra("width", ((MirroringPlayerArg)arg).getWidth());
			intent.putExtra("height", ((MirroringPlayerArg)arg).getHeight());
			intent.putExtra("mId", ((MirroringPlayerArg)arg).getMirroringId());			
		}
		intent.setClass(AirplayService.this, cls);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

	@Override
	public void onCreate() {
		LetvLog.d(TAG, "Airplay Create start");
		 
		int value = 0;
		//SharedPreferences sp = initSP();
		
		//value = getValue(sp, ONOFF);
	
		value = Settings.System.getInt(this.getContentResolver(),"multi_screen", 1);
		/*shixq temporary for c1s 1.5 airplay conflict,first close,use system self*/
		if(LetvUtils.isCloseAirplay()){
			value = OFF;
		}
		LetvLog.d(TAG, "Airplay onCreate value: " + value);
		
		switch(value){
			case ON:
				IsOn = true;
				break;
			case OFF:
				IsOn = false;
				break;
			case EMPTY:
				IsOn = true;
				//setValue(sp, ONOFF, ON);
				break;
			case ERR:
				IsOn = true;
				break;
			default:
				IsOn = true;
				break;		
		}

		JniInterface.getInstance().setService(this);
		registerReceiver();
		proxy = new LinkProxy(this);
		
		//if(true == SystemApp()){
		//	ks = new KillService(this);
		//}
		
		super.onCreate();
		LetvLog.d(TAG, "Airplay Create end");
	}

	public boolean SystemApp(){
		
		PackageManager pm = this.getPackageManager();  
		
		LetvLog.d(TAG, "Airplay  getPackageName: " + this.getPackageName());
		
		PackageInfo info;
		try {
			info = pm.getPackageInfo(this.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LetvLog.d(TAG, "Airplay  getPackageInfo false");
			return false;
		}
		
		ApplicationInfo appInfo = info.applicationInfo;
		
		
		LetvLog.d(TAG, "Airplay  uid : " + appInfo.uid);	
		
		if (appInfo.uid == 1000 ){
			LetvLog.d(TAG, "Airplay is system app!");
			return true;
		}else{
			//do nothing!
		}
		
		LetvLog.d(TAG, "Airplay is not system app!");
		
		return false;
	}
	
	public SharedPreferences initSP(){
		
		LetvLog.d(TAG, "Airplay initSP");
		 Context ctx = AirplayService.this;
		 if(null != sp){
		   sp = null;
		 }
		 sp = ctx.getSharedPreferences("airplay", MODE_PRIVATE);
		 
		 return sp;
		 
	}
	
	public int getValue(SharedPreferences sp, String key){
		LetvLog.d(TAG, "Airplay SP: " + sp + " getValue key: " + key);
		int value = 0;
		if(null != sp){
			value = sp.getInt(key, 2);
			return value;
		}
		return -1;
	}

	public boolean setValue(SharedPreferences sp, String key, int value){
		
		LetvLog.d(TAG, "Airplay SP: " + sp + " setValue key: " + key + " value: " + value);
		if(null != sp){
			 Editor editor = sp.edit();
			 editor.putInt(key, value);
			 editor.commit();
		}else{
			return false;
		}
		
		return true;
	}
	
	public void registerReceiver() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		intentFilter.addAction(ACTION_WIFI_AP_STATE_CHANGED);
		if(LetvUtils.isHideNameFunc()){
			intentFilter.addAction(ACTION_CHANGE_NAME_SYS);
		}else{
			intentFilter.addAction(ACTION_CHANGE_NAME_SC);
		}
		intentFilter.addAction(ACTION_MSS_CHANGE);
		netStateBroadCastReceiver = new NetStateBroadCastReceiver();
		registerReceiver(netStateBroadCastReceiver, intentFilter);

	}
	     
	/**
	 * 接收断网事件广播接收者
	 * @author 韦念欣
	 * @modify Jamin
	 */
	public class NetStateBroadCastReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {

			LetvLog.d(TAG, "Airplay BroadcastReceiver intent:" + intent);
			
			if(intent.getAction().equals(ACTION_MSS_CHANGE)){
				boolean status = intent.getBooleanExtra("MultiScreenIsOn", true);
				LetvLog.d(TAG, "ACTION_MSS_CHANGE: " + status);
				Intent i = new Intent(context, AirplayService.class);
				if(true == status){
					i.setAction("MultiScreenUp");
				}else{
					i.setAction("MultiScreenDown");
				}
				context.startService(i);
			}
			if(intent.getAction().equals(ACTION_WIFI_AP_STATE_CHANGED)){
                Intent i = new Intent(context, AirplayService.class);
        		i.setAction(ACTION_WIFI_AP_STATE_CHANGED);
        		context.startService(i);	
			}
			if (intent.getAction().equals(ACTION_CONNECTIVITY_CHANGE)) {
                Intent i = new Intent(context, AirplayService.class);
        		i.setAction(ACTION_CONNECTIVITY_CHANGE);
        		context.startService(i);
			}
			if (intent.getAction().equals(ACTION_CHANGE_NAME_SYS)) {
				LetvLog.d(TAG, "Airplay BroadcastReceiver changeName");
				//Toast.makeText(context, "changeName", 500).show();
				Intent i = new Intent(context, AirplayService.class);
				i.setAction(ACTION_CHANGE_NAME_SYS);
				context.startService(i);
			}
			if (intent.getAction().equals(ACTION_CHANGE_NAME_SC)) {
				LetvLog.d(TAG, "Airplay BroadcastReceiver changeName");
				//Toast.makeText(context, "changeName", 500).show();
				Intent i = new Intent(context, AirplayService.class);
				i.setAction(ACTION_CHANGE_NAME_SC);
				context.startService(i);
			}
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		LetvLog.d(TAG, "Airplay onStartCommand intent: " + intent);
		
		String act = null;
		// TODO Auto-generated method stub
		if(null == intent){
			act = "startService";
		}else{
			act = intent.getAction();
		}
		LetvLog.d(TAG, "Airplay onStartCommand " + " IsOn: "+ IsOn + " act: "+ act);
		if("startService".equals(act)){
			LetvLog.d(TAG, "Airplay startService");
			if(true == IsOn){
				LetvLog.d(TAG, "Airplay startService startmCast");
				proxy.FiltOpt("MultiScreenUp");
			}else{
				LetvLog.d(TAG, "Airplay startService do nothings!");
			}
		}
		else if("MultiScreenUp".equals(act)){
			
			LetvLog.d(TAG, "Airplay MultiScreenUp IsOn: " + IsOn);
			if(IsOn == false){
				IsOn = true;
				//SharedPreferences sp = initSP();
				//setValue(sp, ONOFF, ON);
				proxy.FiltOpt("MultiScreenUp");
			}else{
				LetvLog.d(TAG, "Airplay already up!");
			}
		}else if("MultiScreenDown".equals(act)){
			LetvLog.d(TAG, "Airplay MultiScreenDown IsOn: " + IsOn);
			if(IsOn == true){
				IsOn = false;
				//SharedPreferences sp = initSP();
				//setValue(sp, ONOFF, OFF);
				proxy.FiltOpt("MultiScreenDown");
			}else{
				LetvLog.d(TAG, "Airplay already down!");
			}
		}else if (ACTION_CONNECTIVITY_CHANGE.equals(act) && (IsOn == true)) {
			proxy.FiltOpt(ACTION_CONNECTIVITY_CHANGE);
		} else if (ACTION_WIFI_AP_STATE_CHANGED.equals(act) && (IsOn == true)) {
			proxy.FiltOpt(ACTION_WIFI_AP_STATE_CHANGED);
		} else if (ACTION_CHANGE_NAME_SYS.equals(act) && (IsOn == true)) {
			proxy.FiltOpt(ACTION_CHANGE_NAME_SYS);
		}else if (ACTION_CHANGE_NAME_SC.equals(act) && (IsOn == true)) {
			proxy.FiltOpt(ACTION_CHANGE_NAME_SC);
		}else{
			LetvLog.d(TAG, "Airplay do nothing!");
		}

		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * 断网后，停止正在播放的音视频和图片
	 */
	public void releaseResource(){
		JniInterface jni = JniInterface.getInstance();
		String id = "all";
		if (jni.playVideo){
			setMediaPlayerState(id, id.length(), STOP, STOP_NORNAL);
			jni.operateMediaPlayer(VideoPlayerManager.STOP, id);
		}
		if (jni.playMusic){
			setAudioPlayerState(id, id.length(), STOP);
			jni.closeAudioPlayer();
		}
		if (jni.showPicture){
			jni.stopPictureShow(id);
		}
		if(jni.playMirroring){
			setMirroringState(id, id.length(), STOP);
			jni.closeMirroringPlayer(id);
		}
	}

	

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		LetvLog.d(TAG, "Airplay onDestroy ");
		
		releaseResource();
		if (netStateBroadCastReceiver != null) {
			this.unregisterReceiver(netStateBroadCastReceiver);
		}
		proxy.release();
		
		//if(ks != null){
		//	ks.Release();
		//}
		airplayStop();
	}
	
	
	public native String stringFromJNI();
	
	public static native int renderStart();
	
	public static native int renderTestFunc();
	
	public static native int dlnaRenderNetInfoSet(String ip, String mac);
	
	
	public static native int airplayStop();
	public static native int airplayStart();
	
	public static native void setMediaPlayerState(String id, int length, int state, int stateType);
	
	public static native void setAudioPlayerState(String id, int length, int state);
	
	public static native void setPictureState(String id, int length, int state);
	
	public static native void setMirroringState(String id, int length, int state);

}
