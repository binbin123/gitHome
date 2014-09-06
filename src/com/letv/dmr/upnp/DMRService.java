package com.letv.dmr.upnp;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.widget.Toast;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;
import com.letv.dmr.AudioPlayerActivity;
import com.letv.dmr.MediaPlayerActivity;
import com.letv.dmr.MediaplayerBase;
import com.letv.dmr.PictureShowActivity;
import com.letv.dmr.upnp.MediaRendererDevice;
import com.letv.dmr.DmrInterfaceManage;
import com.letv.pp.service.LeService;
import com.letv.smartControl.R;
import com.letv.upnpControl.http.HttpUtil;
import com.letv.upnpControl.tools.LetvLog;
import com.letv.upnpControl.tools.LetvUtils;
import com.letv.web.WebViewActivity;

import org.cybergarage.upnp.Device;
import org.cybergarage.upnp.UPnP;
import org.cybergarage.upnp.device.ConnectPhoneNumChangeListener;
import org.cybergarage.upnp.device.InvalidDescriptionException;
import org.cybergarage.util.Debug;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.wifi.WifiManager.MulticastLock;
import android.content.SharedPreferences;

@SuppressLint("HandlerLeak")
public class DMRService extends Service implements
		ConnectPhoneNumChangeListener {

	public static DMRDevice dmrDev = null;

	private static String mDescriptionFileName;

	private static String mServiceAVTFileName;

	private static String mServiceCMFileName;

	private static String mServiceRCFileName;
	public static LeService mUTP = null;
	public static long UTP_INIT_VAL = -1;

	private String mAbsPath = null;

	private MulticastLock multicastLock = null;

	private static Object mDmrDeviceSync = new Object();

	private DMRStart mStartThread = null;

	enum MediaSource {
		PICTURE, AUDIO, VIDEO
	}

	public static final int DISPLAY_WARNING = 0;

	private UIhandler mUiHander;

	private ServiceHandler mServiceHandler = null;

	private Looper mLooper = null;

	public static Context mContext = null;

	public static final boolean debugMode = true;

	public void startActivity(int appId) {
		mServiceHandler.sendEmptyMessageDelayed(appId, 500);
	}

	public void startActivityDelayed(int appId, int time) {
		mServiceHandler.sendEmptyMessageDelayed(appId, time);
	}

	private class ServiceHandler extends Handler {

		public ServiceHandler(Looper looper) {
			super(looper);
		}

		public boolean MediaPlayerIsStarting() {

			ActivityManager localActivityManager = (ActivityManager) getSystemService("activity");
			ComponentName topComponentName = ((ActivityManager.RunningTaskInfo) localActivityManager
					.getRunningTasks(1).get(0)).topActivity;
			String topActivityClass = topComponentName.getClassName();
			if (topActivityClass == null)
				return false;
			if (topActivityClass.equals("com.letv.dmr.MediaPlayerActivity")) {
				Debug.d("DMRService", "MediaPlayerIsStarting =  "
						+ topComponentName.getPackageName());
				return true;
			}
			return false;
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (MediaPlayerIsStarting()
					&& msg.what != MediaplayerBase.PICTURE_SHOW_ID) {
				Debug.d("DMRService", "MediaPlayerIsStarting return ");
				return;
			}
			// stopTvMediaPlayer();
			Class<?> cls = MediaPlayerActivity.class;

			if (msg.what == MediaplayerBase.PICTURE_SHOW_ID) {
				cls = PictureShowActivity.class;
			} else if (msg.what == MediaplayerBase.AUDIO_PLAYER_ID) {
				cls = AudioPlayerActivity.class;
			}

			Intent in = new Intent().setClass(DMRService.this, cls);
			in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(in);

		}
	}

	void sendMessageToPhone(final Intent intent) {

		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					if (DMRService.dmrDev == null)
						return;
					int size = DMRService.dmrDev.ConnectedDeviceList.size();
					LetvLog.d("DMRService",
							"ConnectedDeviceList size= " + size);
					if (size > 0) {
						JSONObject message = new JSONObject();
						message.put("intent_text",
								intent != null ? intent.toUri(0) : "");
						JSONObject js = new JSONObject();
						js.put("input_text", message.toString());
						js.put("device_id", Device.mUuid);
						// for ANDROID
						synchronized (Device.mDeviceSync) {
							for (int i = 0; i < size; i++) {
								if (DMRService.dmrDev == null)
									return;

								String url = "http://"
										+ DMRService.dmrDev.ConnectedDeviceList
												.get(i).get("IP")
										+ ":"
										+ DMRService.dmrDev.ConnectedDeviceList
												.get(i).get("PORT")
										+ "/inputintent";

								LetvLog.d("DMRService",
										"sendMessageToPhone url= " + url);
								HttpUtil.doPost(url, js.toString(), "utf-8");
							}
						}
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}).start();
	}

	private BroadcastReceiver mDMRServiceListener = new BroadcastReceiver() {
		public void onReceive(Context paramContext, Intent paramIntent) {
			if ((DMRService.dmrDev == null) || (!DMRService.dmrDev.isDMRStart))
				return;
			if (paramIntent.getAction().equals(
					"android.intent.action.PACKAGE_ADDED")
					|| paramIntent.getAction().equals(
							"android.intent.action.PACKAGE_REMOVED")
					|| paramIntent.getAction().equals(
							"android.intent.action.PACKAGE_REPLACED")) {
				sendMessageToPhone(paramIntent);
				return;
			}
			DMRService.dmrDev.handleMessage(paramIntent);
		}
	};

	private BroadcastReceiver mDeviceNameChangeListener = new BroadcastReceiver() {
		public void onReceive(Context paramContext, Intent paramIntent) {
			DMRService.dmrDev.setDmrDeviceName(true);
		}
	};
	static {
		mDescriptionFileName = "DMR-Intel/description.xml";
		mServiceAVTFileName = "DMR-Intel/AVTransport.scpd.xml";
		mServiceRCFileName = "DMR-Intel/RenderingControl.scpd.xml";
		mServiceCMFileName = "DMR-Intel/ConnectionManager.scpd.xml";
	}

	String copyAsset(String paramString1, String paramString2) {
		File localFile = new File(paramString1 + "/" + paramString2);

		InputStream localInputStream = null;
		FileOutputStream localFileOutputStream = null;
		try {
			localInputStream = getAssets().open(paramString2);

			localFileOutputStream = openFileOutput(localFile.getName(),
					Context.MODE_PRIVATE);
			while (localInputStream.available() > 0) {
				byte[] b = new byte[1024];
				int bytesread = localInputStream.read(b);
				localFileOutputStream.write(b, 0, bytesread);
			}

			localFileOutputStream.close();
			localInputStream.close();

		} catch (FileNotFoundException localFileNotFoundException) {
			localFileNotFoundException.printStackTrace();
			Debug.d("DMRService", "FileNotFoundException ");
			return null;
		} catch (IOException localIOException) {
			localIOException.printStackTrace();
			Debug.d("DMRService", "IOException ");
			return null;
		}

		return localFile.getName();
	}

	public IBinder onBind(Intent paramIntent) {
		return null;
	}

	public void onCreate() {
		super.onCreate();
		Debug.d("DMRService", ">>>>>>onCreate");
		// StrictMode.ThreadPolicy localThreadPolicy = StrictMode
		// .getThreadPolicy();
		// StrictMode.ThreadPolicy.Builder localBuilder = new
		// StrictMode.ThreadPolicy.Builder(
		// localThreadPolicy);
		// StrictMode.setThreadPolicy(localBuilder.permitNetwork().build());
		/*
		 * if(debugMode == true){ StrictMode.setThreadPolicy(new
		 * StrictMode.ThreadPolicy.Builder() .detectDiskReads()
		 * .detectDiskWrites() .detectNetwork() .penaltyLog() .build());
		 * StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
		 * .detectLeakedSqlLiteObjects() .penaltyLog() .penaltyDeath()
		 * .build()); }
		 */

		// mUTP = new LeService();
		// try {
		// UTP_INIT_VAL = mUTP.startService(this,6990,Constants.UTP_PARAMITER);
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		DmrInterfaceManage.getInstance();
		WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		multicastLock = wifiManager.createMulticastLock("multicast.dlna");
		multicastLock.acquire();
		this.mAbsPath = getFilesDir().getAbsolutePath();
		Debug.d("DMRService", "this.mAbsPath = " + this.mAbsPath);

		copyAsset(this.mAbsPath, mServiceAVTFileName);
		copyAsset(this.mAbsPath, mServiceRCFileName);
		copyAsset(this.mAbsPath, mServiceCMFileName);
		if (dmrDev == null) {
			try {
				Debug.d("DMRService", ">>>>Create DMRDevice....");
				UPnP.setEnable(UPnP.USE_ONLY_IPV4_ADDR);
				// get mac
				String deviceMac = LetvUtils.getProductNameMac(this);

				Debug.d("DMRService", ">>>>Create DMRDevice mac = " + deviceMac);
				// get device description.xml
				String desFileName = copyAsset(this.mAbsPath,
						mDescriptionFileName);
				if (desFileName == null) {
					Debug.e("DMRService",
							">>>>Create DMRDevice desFileName == null");
					return;
				}
				// get device name
				String deviceName = "KanKan";
				if (LetvUtils.isHideNameFunc()) {
					deviceName = SystemProperties.get("net.hostname");
					dmrDev = new DMRDevice(this.mAbsPath + "/" + desFileName);
					if (dmrDev != null) {
						dmrDev.addConnectedPhoneListener(this);
						dmrDev.clearConnectedPhoneNumber();
						dmrDev.setUDN(dmrDev.getUDN() + deviceMac);
						dmrDev.setFriendlyName(deviceName);
						dmrDev.setFriendlyNameId(deviceName);
					}
				} else {

					SharedPreferences sp = getSharedPreferences("DeviceName",
							MODE_PRIVATE);

					deviceName = sp.getString("device_name", "KanKan");

					Debug.d("DMRService", ">>>>Create DMRDevice name = "
							+ deviceName);

					int id = getSharedPreferences("DeviceNameId", MODE_PRIVATE)
							.getInt("position", 0);
					dmrDev = new DMRDevice(this.mAbsPath + "/" + desFileName);
					if (dmrDev != null) {
						dmrDev.addConnectedPhoneListener(this);
						dmrDev.setUDN(dmrDev.getUDN() + deviceMac);
						dmrDev.setFriendlyName(deviceName);
						dmrDev.setFriendlyNameId(String.valueOf(id));
					}
				}

			} catch (InvalidDescriptionException localInvalidDescriptionException) {
				Debug.d("DMRService",
						">>>>Create DMRDevice.InvalidDescriptionException");
			}
		}
		IntentFilter playIntentFilter = new IntentFilter();
		playIntentFilter.addAction("com.letv.dlna.PLAY_POSTION_REFRESH");
		playIntentFilter.addAction("com.letv.dlna.PLAY_PAUSED");
		playIntentFilter.addAction("com.letv.dlna.PLAY_PLAYING");
		playIntentFilter.addAction("com.letv.dlna.PLAY_STOPPED");
		playIntentFilter.addAction("com.letv.dlna.PLAY_SETVOLUME");
		playIntentFilter.addAction("com.letv.dlna.PLAY_SETMUTE");
		registerReceiver(mDMRServiceListener, playIntentFilter);

		IntentFilter packageIntentFilter = new IntentFilter();
		packageIntentFilter.addAction("android.intent.action.PACKAGE_ADDED");
		packageIntentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
		packageIntentFilter.addAction("android.intent.action.PACKAGE_REPLACED");
		packageIntentFilter.addDataScheme("package");
		registerReceiver(mDMRServiceListener, packageIntentFilter);

		IntentFilter deviceName = new IntentFilter();
		deviceName.addAction("com.letv.action.changeName");

		registerReceiver(mDeviceNameChangeListener, deviceName);

		mUiHander = new UIhandler(Looper.getMainLooper());
		mLooper = Looper.myLooper();
		if (mLooper != null) {
			mServiceHandler = new ServiceHandler(mLooper);
		}
		mContext = DMRService.this;
	}

	public void onDestroy() {
		super.onDestroy();
		if (multicastLock != null)
			multicastLock.release();
		if (this.mDMRServiceListener != null && dmrDev != null) {
			try {
				unregisterReceiver(this.mDMRServiceListener);
				unregisterReceiver(this.mDeviceNameChangeListener);
			} catch (IllegalArgumentException e) {
				Debug.d("DMRService",
						">>>>>>unregisterReceiver is not register");
			}
		}
		Debug.d("DMRService", ">>>>>>onDestroy");
		if (dmrDev != null && dmrDev.isDMRStart) {
			Debug.d("DMRService", "******DMRStop thread start ");
			new DMRStop("DMRStop").start();
		}
		if (dmrDev != null) {
			dmrDev.removeConnectedPhoneListener(this);
			dmrDev.clearConnectedPhoneNumber();
		}

	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		Debug.d("DMRService", "******onStartCommand ");
		super.onStart(intent, startId);

		mStartThread = new DMRStart("DMRStart");
		mStartThread.start();

		return START_STICKY;
	}

	public class UIhandler extends Handler {
		public UIhandler(Looper looper) {
			super(looper);
		}

		public void handleMessage(Message msg) {

			switch (msg.what) {
			case DISPLAY_WARNING:
				Toast.makeText(DMRService.this,
						getString(R.string.installIexplore), Toast.LENGTH_LONG)
						.show();
				break;
			default:
				break;
			}

		}
	}

	public class DMRDevice extends MediaRendererDevice {
		public boolean isDMRStart = false;

		private Timer mTimer = null;

		private TimerTask mTimerTask = null;

		public DMRDevice(String arg2) throws InvalidDescriptionException {
			super(arg2);
		}

		public void stopTimer() {

			if (mTimer != null) {
				mTimer.cancel();
				mTimer = null;
			}

			if (mTimerTask != null) {
				mTimerTask.cancel();
				mTimerTask = null;
			}

		}

		public void setDmrDeviceStatus(boolean isOn) {
			LetvLog.d("TAG", "setDmrDeviceStatus = " + isOn);
			SharedPreferences sp = mContext.getSharedPreferences(
					"DeviceStatus", MODE_PRIVATE);

			SharedPreferences.Editor editor = sp.edit();
			editor.putBoolean("isOn", isOn);
			editor.commit();
		}

		public boolean getDmrDeviceStatus() {
			SharedPreferences sp = mContext.getSharedPreferences(
					"DeviceStatus", MODE_PRIVATE);
			boolean status = sp.getBoolean("isOn", true);
			LetvLog.w("TAG", "getDmrDeviceStatus = " + status);
			return status;
		}

		public void setDmrDeviceName(String deviceName, String id) {
			if (dmrDev != null) {
				dmrDev.setFriendlyName(deviceName);
				dmrDev.setFriendlyNameId(id);
			}

		}

		public void setDmrDeviceName(boolean immediate) {
			if (LetvUtils.isHideNameFunc() == false) {
				return;
			}
			if (immediate) {

				String deviceName = SystemProperties.get("net.hostname");
				if (deviceName == null
						|| (deviceName != null && deviceName.length() == 0)) {
					deviceName = SystemProperties.get("persist.sys.dlna.name",
							"LetvDmr");
				}
				Debug.d("DMRService", ">>>>immediate setDmrDeviceName name = "
						+ deviceName);
				if (dmrDev != null) {
					dmrDev.setFriendlyName(deviceName);
					dmrDev.setFriendlyNameId(deviceName);
				}

			} else {

				if (mTimer == null) {
					mTimer = new Timer();
				}

				if (mTimerTask == null) {
					mTimerTask = new TimerTask() {
						public void run() {
							String deviceName = SystemProperties
									.get("net.hostname");
							if (deviceName == null
									|| (deviceName != null && deviceName
											.length() == 0)) {
								deviceName = SystemProperties.get(
										"persist.sys.dlna.name", "LetvDmr");
							}
							Debug.d("DMRService",
									">>>>timer setDmrDeviceName name = "
											+ deviceName);
							if (dmrDev != null) {
								dmrDev.setFriendlyName(deviceName);
								dmrDev.setFriendlyNameId(deviceName);
							}
							stopTimer();

						}
					};
				}

				if (mTimer != null && mTimerTask != null)
					mTimer.schedule(mTimerTask, 0, 5 * 1000);
			}
		}

		public void notifyDMR(Intent paramIntent) {
			Debug.d("DMRService", "notifyDMR");
			if (getDmrDeviceStatus() == false) {
				return;
			}
			if ("com.letv.accountLogin.receiveImage".equals(paramIntent
					.getAction())) {
				DMRService.this.sendBroadcast(paramIntent);
				return;
			}
			if ("android.intent.action.VIEW".equals(paramIntent.getAction())) {
				ActivityManager localActivityManager = (ActivityManager) getSystemService("activity");
				ComponentName topComponentName = ((ActivityManager.RunningTaskInfo) localActivityManager
						.getRunningTasks(1).get(0)).topActivity;
				String topActivityClass = topComponentName.getClassName();

				if (topActivityClass != null
						&& topActivityClass
								.equals("com.letv.web.WebViewActivity")) {
				}
				Debug.d("DMRService", "topActivityClass =" + topActivityClass);
				try {
					startActivity(paramIntent);
				} catch (ActivityNotFoundException ie) {
					if (LetvUtils.getTvManufacturer() == LetvUtils.LETV) {
						paramIntent.setClass(DMRService.this,
								WebViewActivity.class);
						startActivity(paramIntent);
					} else {
						mUiHander.sendEmptyMessage(DISPLAY_WARNING);
					}
				}
				return;
			}

			if ("InstallApk".equals(paramIntent.getAction())) {

				String filename = paramIntent.getStringExtra("filename");
				String filecontent = paramIntent.getStringExtra("filecontent");
				Debug.d("DMRService", "install apk  filename =" + filename);
				try {

					FileOutputStream fout = openFileOutput(filename,
							Context.MODE_PRIVATE);

					byte[] bytes = filecontent.getBytes();
					Debug.d("DMRService", "bytes length = " + bytes.length);
					fout.write(bytes);

					fout.close();

				}

				catch (Exception e) {

					e.printStackTrace();
				}
			}

			if ("com.letv.UPNP_PLAY_ACTION".equals(paramIntent.getAction())) {
				String str = paramIntent.getStringExtra("media_type");
				if ("audio/*".equals(str)) {
					String file_name = paramIntent.getStringExtra("file_name");
					Debug.d("MediaRendererDevice", "audio file_name  = "
							+ file_name);
					MediaplayerBase.getInstance().setSongName(file_name);
				}

				if ("audio/*".equals(str)) {
					String url = paramIntent.getStringExtra("media_uri");
					String position = paramIntent
							.getStringExtra("start_position");
					int startPosition = 0;
					if (position != null) {
						startPosition = Integer.valueOf(position);
					}
					if (!AudioPlayerActivity.running) {

						Debug.d("MediaRendererDevice", "audio url  = "
								+ paramIntent.getStringExtra("media_uri"));
						if (MediaPlayerActivity.running) {
							MediaplayerBase.getInstance().stopAduioOrVideo();
							MediaplayerBase.getInstance()
									.startAudioPlayerDelayed(url,
											startPosition, DMRService.this,
											3000);
						} else {
							MediaplayerBase.getInstance().startAudioPlayer(url,
									startPosition, DMRService.this);
						}
						return;

					} else if (!MediaplayerBase.gDlnaMediaPlayerURL.equals(url)) {

						MediaplayerBase.getInstance().stopAduioOrVideo();
						MediaplayerBase.getInstance().startAudioPlayerDelayed(
								url, startPosition, DMRService.this, 3000);
						return;
					}

				}
				if ("video/*".equals(str)) {
					if (AudioPlayerActivity.running) {
						MediaplayerBase.getInstance().stopAduioOrVideo();
					}
					String url = paramIntent.getStringExtra("media_uri");
					String position = paramIntent
							.getStringExtra("start_position");
					int startPosition = 0;
					if (position != null) {
						startPosition = Integer.valueOf(position);
					}

					if (!MediaPlayerActivity.running) {

						if (isUTP == false) {

							MediaplayerBase.getInstance().startMediaPlayer(url,
									startPosition, DMRService.this);
						} else {

							MediaplayerBase.getInstance().startUTPMediaPlayer(
									url, startPosition, DMRService.this);
						}

						return;
					} else if (!MediaplayerBase.gDlnaMediaPlayerURL.equals(url)) {
						MediaplayerBase.getInstance().stopAduioOrVideo();
						MediaplayerBase.getInstance().startMediaPlayerDelayed(
								url, MediaplayerBase.MEDIA_PLAYER_ID,
								startPosition, 3000);
						return;
					}

				}

				if ("image/*".equals(str)) {
					String url = paramIntent.getStringExtra("media_uri");
					int download_type = paramIntent.getIntExtra(
							"download_type", 1);

					Debug.d("MediaRendererDevice", "image url  = "
							+ paramIntent.getStringExtra("media_uri"));

					MediaplayerBase.getInstance().startPictureShow(url,
							download_type, DMRService.this);
					return;
				}

			}

			DMRService.this.sendBroadcast(paramIntent);
		}

		public boolean start() {

			this.isDMRStart = super.start();
			Debug.d("DMRService", "*******DMRDevice start=" + this.isDMRStart);
			if (this.isDMRStart) {
				LetvUtils.saveFileToSDCar("kankan_log",
						String.valueOf(this.isDMRStart) + "\n");
			}
			return this.isDMRStart;

		}

		public boolean stop() {
			this.isDMRStart = false;
			boolean ret = super.stop();
			super.stopMediaStateThread();
			Debug.d("DMRService", "*******DMRDevice stop = " + ret);
			return ret;
		}
	}

	class DMRStart extends Thread {
		public DMRStart(String arg2) {
			super(arg2);
		}

		public void run() {
			Debug.d("DMRService", "******DMRStart thread run ");
			synchronized (mDmrDeviceSync) {
				Debug.d("DMRService", "******DMRStart DMRService.dmrDev =  "
						+ DMRService.dmrDev);
				if (DMRService.dmrDev == null) {
					Debug.d("DMRService",
							"******DMRStart thread DMRService.dmrDev = null ");

					try {
						Debug.d("DMRService", ">>>>Create DMRDevice....");
						UPnP.setEnable(UPnP.USE_ONLY_IPV4_ADDR);
						// get mac
						String deviceMac = LetvUtils
								.getProductNameMac(DMRService.this);

						Debug.d("DMRService", ">>>>Create DMRDevice mac = "
								+ deviceMac);
						// get device description.xml
						String desFileName = copyAsset(mAbsPath,
								mDescriptionFileName);
						// get device name
						if (desFileName == null) {
							Debug.e("DMRService",
									">>>>Create DMRDevice desFileName == null");
							return;
						}
						String deviceName = "kankan";
						if (LetvUtils.isHideNameFunc()) {
							deviceName = SystemProperties.get("net.hostname");
							DMRService.dmrDev = new DMRDevice(mAbsPath + "/"
									+ desFileName);
							if (DMRService.dmrDev != null) {
								DMRService.dmrDev
										.addConnectedPhoneListener(DMRService.this);
								DMRService.dmrDev.setUDN(dmrDev.getUDN()
										+ (String) deviceMac);
								DMRService.dmrDev.setFriendlyName(deviceName);
								dmrDev.setFriendlyNameId(deviceName);
							}
						} else {
							SharedPreferences sp = getSharedPreferences(
									"DeviceName", MODE_PRIVATE);

							deviceName = sp.getString("device_name", "KanKan");

							Debug.d("DMRService",
									">>>>Create DMRDevice name = " + deviceName);

							DMRService.dmrDev = new DMRDevice(mAbsPath + "/"
									+ desFileName);
							if (DMRService.dmrDev != null) {
								DMRService.dmrDev
										.addConnectedPhoneListener(DMRService.this);
								DMRService.dmrDev.setUDN(dmrDev.getUDN()
										+ (String) deviceMac);
								DMRService.dmrDev.setFriendlyName(deviceName);
								int id = getSharedPreferences("DeviceNameId",
										MODE_PRIVATE).getInt("position", 0);
								dmrDev.setFriendlyNameId(String.valueOf(id));
							}
						}

					} catch (InvalidDescriptionException localInvalidDescriptionException1) {
						Debug.d("DMRService",
								">>>>Create DMRDevice.InvalidDescriptionException");
					}
				}
				Debug.d("DMRService",
						"******DMRStart DMRService.dmrDev.isDMRStart =  "
								+ DMRService.dmrDev.isDMRStart);
				if (DMRService.dmrDev != null && !DMRService.dmrDev.isDMRStart) {
					Debug.d("DMRService", "******DMRStart thread start ");
					DMRService.dmrDev.start();
				}
			}

		}
	}

	class DMRStop extends Thread {
		public DMRStop(String arg2) {
			super(arg2);
		}

		public void run() {
			Debug.d("DMRService", "******DMRStopt thread run ");
			synchronized (mDmrDeviceSync) {
				if (DMRService.dmrDev == null) {
					Debug.d("DMRService",
							"******DMRStopt thread DMRService.dmrDev = null,return ");
					return;
				}
				if (DMRService.dmrDev.isDMRStart) {
					Debug.d("DMRService", "******DMRStopt thread start ");
					DMRService.dmrDev.stop();

					DMRService.dmrDev = null;
				}
			}
		}
	}

	public static int getControlPointNumber() {
		int num = 0;
		if (DMRService.dmrDev != null) {
			num = DMRService.dmrDev.getConnectedPhoneNumber();
		}
		return num;
	}

	@Override
	public void NotifyControlPointNumberChanged() {
		// TODO Auto-generated method stub
		Intent intent = new Intent();
		intent.setAction("com.letv.action.displayPhoneNum");
		intent.putExtra("PHONENUM", getControlPointNumber());
		sendBroadcast(intent);
	}

}
