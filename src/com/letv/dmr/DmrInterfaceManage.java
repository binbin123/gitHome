package com.letv.dmr;

import org.cybergarage.upnp.Device;
import org.cybergarage.util.Debug;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Intent;
import com.letv.dmr.upnp.DMRService;
import android.net.Uri;
import android.os.RemoteException;
import android.os.ServiceManager;
import com.letv.dmr.upnp.MediaRendererDevice;
import com.letv.upnpControl.http.HttpUtil;

public class DmrInterfaceManage extends IDmrService.Stub {

	/** The singleton instance. */
	private static DmrInterfaceManage sInstance = new DmrInterfaceManage();
	private static final String TAG = "DmrInterfaceManage";

	private DmrInterfaceManage() {
		ServiceManager.addService("dmr", this);
	}

	public static DmrInterfaceManage getInstance() {

		return sInstance;
	}

	public String getCurrentTransportState() {
		if (DMRService.dmrDev != null) {
			return DMRService.dmrDev.getTransportState();
		}
		return "NO_MEDIA_PRESENT";
	}

	public int getCurrentPosition() {
		return MediaplayerBase.getInstance().mediaPlayerPositionGet();
	}

	public int getTotalTime() {
		return MediaplayerBase.getInstance().mediaPlayerDurationGet();
	}

	public void setAccountLoginReceivingPicture() {
		if (MediaplayerBase.gPictureActivity != null) {
			Intent intent = new Intent();
			intent.setAction("com.letv.accountLogin.receiveImage");
			intent.putExtra("media_type", "image/*");
			if (DMRService.dmrDev != null) {
				DMRService.dmrDev.notifyDMR(intent);
			}
		}
	}

	public int setUrl(String url, int media_type, int is_play) {

		Debug.d("DmrInterfaceManage", "url = " + url + "media_type= "
				+ media_type);
		if (is_play == 1) {
			Intent intent = new Intent();
			intent.setAction("android.intent.action.VIEW");
			Uri content_url = Uri.parse(url);
			intent.setData(content_url);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			if (DMRService.dmrDev != null) {
				DMRService.dmrDev.notifyDMR(intent);
				return 1;
			}
		}

		if (media_type == 0)// 0 video;1 image
		{
			Intent videoIntent = new Intent("com.letv.UPNP_PLAY_ACTION");
			videoIntent.putExtra("media_uri", url);
			videoIntent.putExtra("media_type", "video/*");
			if (DMRService.dmrDev != null) {
				DMRService.dmrDev.notifyDMR(videoIntent);
				return 1;
			}
		} else if (media_type == 1) {
			Intent imageIntent = new Intent("com.letv.UPNP_PLAY_ACTION");
			imageIntent.putExtra("media_uri", url);
			imageIntent.putExtra("media_type", "image/*");
			imageIntent.putExtra("download_type", 0);

			if (DMRService.dmrDev != null) {
				DMRService.dmrDev.notifyDMR(imageIntent);
				return 1;
			}
		}
		return 0;

	}

	public void setSeek(String seek_position) {
		Intent seekIntent = new Intent("com.letv.UPNP_PLAY_SEEK");
		seekIntent.putExtra("REL_TIME", seek_position);
		Debug.d("DmrInterfaceManage", "seek_position = " + seek_position);
		if (DMRService.dmrDev != null) {
			DMRService.dmrDev.notifyDMR(seekIntent);
		}
	}

	public void setVolume(int volume) {
		Debug.d(TAG, "setVolume = " + volume);
		MediaplayerBase.getInstance().mediaPlayerVolumeSet(volume);
	}

	public int getVolume() {
		return MediaplayerBase.getInstance().mediaPlayerVolumeGet();
	}

	public void Mute() {
		MediaplayerBase.getInstance().mediaPlayerMute();
	}

	public boolean getMute() {
		return MediaplayerBase.getInstance().mediaPlayerIsMute();
	}

	public void setAction(int action_type) {
		Debug.d(TAG, "setAction = " + action_type);
		MediaplayerBase.getInstance().operateMediaPlayer(action_type);
	}

	public boolean actionControlReceivedByUDP(String url, String mediaData) {
		Debug.d(TAG, "actionControlReceivedByUDP url= " + url);
		if (DMRService.dmrDev != null) {
			return DMRService.dmrDev.actionControlReceivedByUDP(
					MediaRendererDevice.SETURL, url, mediaData);
		}
		return false;
	}

	public void SendIntentToPhone(final Intent intent) {
		// TODO Auto-generated method stub
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					if (DMRService.dmrDev != null
							&& Device.mPhoneServerIp.length() > 0) {
						JSONObject js = new JSONObject();
						String uri = intent != null ? intent.toUri(0) : "";
						js.put("send_intent", uri);
						js.put("device_id", Device.mUuid);

						// for ANDROID
						String response = HttpUtil.doPost("http://"
								+ Device.mPhoneServerIp + ":"
								+ Device.mPhoneServerPort + "/inputvalues",
								js.toString(), "utf-8");
						if (response == null
								|| "connect timeout".equals(response)) {
                              
						}

					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();

				}
			}
		}).start();
	}

	@Override
	public int SendMessageToPhone(final String message) throws RemoteException {
		int ret = 0;
		// TODO Auto-generated method stub
		// new Thread(new Runnable() {
		// @Override
		// public void run() {
		// TODO Auto-generated method stub
		try {
			if (DMRService.dmrDev != null && Device.mPhoneServerIp.length() > 0) {
				JSONObject js = new JSONObject();
				js.put("send_text", message);
				js.put("device_id", Device.mUuid);

				// for ANDROID
				String response = HttpUtil.doPost("http://"
						+ Device.mPhoneServerIp + ":" + Device.mPhoneServerPort
						+ "/inputvalues", js.toString(), "utf-8");
				if (response == null || "connect timeout".equals(response)) {
					ret = -1;
				}

			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ret = -1;
		}
		// }
		// }).start();
		return ret;
	}

}
