package com.letv.upnpControl.service;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
//import android.os.SystemProperties;
import com.letv.accountLogin.netty.ImageReceiver;
import com.letv.accountLogin.netty.OperationCtrlData;
import com.letv.accountLogin.netty.TvMessageClient;
import com.letv.upnpControl.tools.AccountUtils;
import com.letv.upnpControl.tools.Constants;
import com.letv.upnpControl.tools.Engine;
import com.letv.upnpControl.tools.LetvLog;
import com.letv.upnpControl.tools.LetvUtils;
import com.letv.upnpControl.tools.MD5;

public class ListenNetWorkService extends Service {

	private ConnectivityManager connectivityManager;
	public static final String ACTION_LOGIN_EVENT = "com.letv.action.login_event";
	public static final String ACTION_LOGOUT_EVENT = "com.letv.action.logout_event";
	private NetworkInfo info;
	private boolean isLogin;
	public static HandlerThread mThread = null;
	public static InitHandler mHandler = null;
	private String msgInfo = null;
	private String from = null;
	private String uID = null;
	private String token = null;
	private String signature = null;
	public static final int SEND_ONLINE = 1001;
	public static final int CREATE_MD5 = 1002;
	public static final int INIT_DATA = 1004;
	public static final int IMAGE_RECEIVE = 1005;
	public static final int SEND_OFFLINE = 1006;
	private static final String TVSTR = "@letv.com/tv-";
	
	private Timer time ;
	private MyTimerTask mtt;

	public class InitHandler extends Handler {
		public InitHandler(Looper looper) {
			super(looper);
		}

		public void handleMessage(Message msg) {
			LetvLog.d("LPF", "msg.what begin = " + msg.what);
			switch (msg.what) {
			case SEND_ONLINE:
				LetvLog.d("LPF", "--send online--");
				if (from != null && msgInfo != null && token != null
						&& signature != null) {
					TvMessageClient.sendDataByNetty(Constants.ONLINE_DATA_TYPE,
							from, token, msgInfo, ListenNetWorkService.this,
							signature);
					Engine.getInstance().initNettyData(from, token, signature);
				}
				break;
			case CREATE_MD5:
				LetvLog.d("LPF", "--create MD5--");
				LetvLog.d("LPF", "token is--"+token);
				signature = MD5.toMd5(from + token);
				LetvLog.d("LPF", "md5 is--"+signature);
				break;
			case INIT_DATA:
				LetvLog.d("LPF", "--init data--");
				try {
					initData();
				} catch (OperationCanceledException e) {
					e.printStackTrace();
				} catch (AuthenticatorException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case IMAGE_RECEIVE:
				LetvLog.d("LPF", "--init image receiver--");
				if (from != null) {
					ImageReceiver.sendOnline(from, ListenNetWorkService.this,OperationCtrlData.ip);
				}
				break;
			case SEND_OFFLINE:
				LetvLog.d("LPF", "--send offline--");
				if (Engine.getInstance().getFrom() != null
						&& Engine.getInstance().getToken() != null
						&& Engine.getInstance().getSignature() != null) {
					TvMessageClient.sendDataByNetty(
							Constants.OFFLINE_DATA_TYPE, Engine.getInstance()
									.getFrom(),
							Engine.getInstance().getToken(), "LETV",
							getApplicationContext(), Engine.getInstance()
									.getSignature());
				}
				break;
			}
			LetvLog.d("LPF", "msg.what end = " + msg.what);
		}
	}

	private void initData() throws OperationCanceledException,
			AuthenticatorException, IOException {
		uID = AccountUtils.getAccountUID(getApplicationContext());
		if (uID != null) {
			LetvLog.d("LPF", "--uid = " + uID + "--");
		}
		msgInfo = SystemProperties.get("net.hostname");
		
		from = uID + TVSTR + SystemProperties.get("net.local.mac");
		token = AccountUtils.token;
		if (token != null) {
			LetvLog.d("LPF", "--token = " + token + "--");
		}
	}

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
				LetvLog.d("LPF", "网络状态已经改变");
				connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				info = connectivityManager.getActiveNetworkInfo();
				if (info != null && info.isConnected()) {
					if(time != null){
						if(mtt != null){
							mtt.cancel();
							mtt = null;
						}
						time.cancel();
						time = null;
					}
					time = new Timer();
					mtt = new MyTimerTask();
					time.schedule(mtt,5000);
					
				}
			}
		}
	};
	private void registerReceiver() {
		//注册网络切换receiver
		IntentFilter mFilter = new IntentFilter();
		mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(mReceiver, mFilter);
	}

	@Override
	public void onCreate() {
		registerReceiver();
		mThread = new HandlerThread(ListenNetWorkService.class.getSimpleName()
				+ "$InitHandler");
		mThread.start();
		mHandler = new InitHandler(mThread.getLooper());
		if(LetvUtils.isCanConnected(this)){
			sendOnline();
		}
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		LetvLog.d("LPF", "listonnetwork Destroy");
		if (mHandler != null) {
			mHandler.removeMessages(CREATE_MD5);
			mHandler.removeMessages(SEND_ONLINE);
			mHandler.removeMessages(INIT_DATA);
			mHandler.removeMessages(IMAGE_RECEIVE);
			mHandler = null;
		}
		if (mThread != null) {
			mThread.getLooper().quit();
			try {
				mThread.join(500);
			} catch (Exception e) {
				e.printStackTrace();
			}
			mThread = null;
		}
		if (mtt != null) {
			mtt.cancel();
			mtt = null;
			if (time != null) {
				time.cancel();
				time = null;
			}
		}
		unregisterReceiver(mReceiver);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	private class MyTimerTask extends TimerTask{
		@Override
		public void run() {
			isLogin = AccountUtils
					.isLetvAccountLogin(getApplicationContext());
			if (isLogin) {
				sendOnline();
			}
			if (mtt != null) {
				mtt.cancel();
				mtt = null;
				if (time != null) {
					time.cancel();
					time = null;
				}
			}
		}
		
	}
	
	public static void sendOnline(){
		LetvLog.d("LPF", "--method online--");
		mHandler.removeMessages(INIT_DATA);
		mHandler.sendEmptyMessage(INIT_DATA);
		mHandler.removeMessages(CREATE_MD5);
		mHandler.sendEmptyMessageDelayed(CREATE_MD5, 500);
		mHandler.removeMessages(SEND_ONLINE);
		mHandler.sendEmptyMessageDelayed(SEND_ONLINE, 500);
	}
	public static void sendOffline(){
		LetvLog.d("LPF", "--method offline--");
		mHandler.removeMessages(SEND_OFFLINE);
		mHandler.sendEmptyMessage(SEND_OFFLINE);
	}
	
	public static void sendImageOnline(){
		LetvLog.d("LPF", "send image online message!");
		mHandler.removeMessages(IMAGE_RECEIVE);
		mHandler.sendEmptyMessage(IMAGE_RECEIVE);
	}

}
