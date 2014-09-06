package com.letv.dmr;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import org.cybergarage.util.Debug;
import com.letv.dmr.upnp.DMRService;
import com.letv.pp.url.PlayUrl;
import android.text.TextUtils;
import android.util.Log;

public class MediaplayerBase {

	private static final String TAG = "MediaplayerBase";
	public static final int MEDIA_PLAYER_ID = 0;
	public static final int PICTURE_SHOW_ID = 1;
	public static final int AUDIO_PLAYER_ID = 2;
	public static String gDlnaMediaPlayerURL = "";
	public static String gDlnaPictureShowURL = "";
	public static String mSongName = "";
	public static int gDlnaPictureUrlType = PictureShowActivity.URL_HTTP;
	public static int gSeekPos = 0;
	public static int gSeekPercent = 0;
	public static boolean gLiveTV = false;
	private static DMRService gMainService = null;
	private static MediaPlayerActivity gMediaPlayerActivity = null;
	private static AudioPlayerActivity gAudioPlayerActivity = null;
	public static PictureShowActivity gPictureActivity = null;
	private static MediaplayerBase singleton = null;
	private Player player = null;

	private MediaplayerBase() {

	}

	public static MediaplayerBase getInstance() {
		if (singleton == null) {
			synchronized (MediaplayerBase.class) {
				if (singleton == null) {
					singleton = new MediaplayerBase();
				}
			}
		}
		return singleton;
	}

	public void setService(DMRService dlnaService) {
		if (gMainService == null)
			gMainService = dlnaService;
	}

	public void setMediaPlayer(MediaPlayerActivity mediaPlayer) {
		gMediaPlayerActivity = mediaPlayer;
	}

	public void setAudioPlayer(AudioPlayerActivity AudioPlayer) {
		gAudioPlayerActivity = AudioPlayer;
	}

	public void setPictureShow(PictureShowActivity pictureShow) {
		gPictureActivity = pictureShow;
	}

	public void stopAduioOrVideo() {
		if (gMediaPlayerActivity != null && !gMediaPlayerActivity.isFinishing()) {
			gMediaPlayerActivity.finish();
		} else if (gAudioPlayerActivity != null
				&& !gAudioPlayerActivity.isFinishing()) {
			gAudioPlayerActivity.finish();
		}
	}

	public void operateMediaPlayer(int operateType) {
		if (gMediaPlayerActivity != null) {
			gMediaPlayerActivity.operateMediaPlayer(operateType);
		} else if (gAudioPlayerActivity != null) {
			gAudioPlayerActivity.operateMediaPlayer(operateType);
		} else if (gPictureActivity != null) {
			if (operateType == PictureShowActivity.STOP) {
				stopPictureShow();
			}
		}

	}

	public void startPictureShow(String picUrl, int uType, DMRService DmrService) {
		gDlnaPictureShowURL = picUrl;
		gDlnaPictureUrlType = uType;
		Debug.d(TAG, "startPictureShow");
		setService(DmrService);
		if (gPictureActivity == null) {
			Debug.d(TAG, "gPictureActivity == null");
			DmrService.startActivity(PICTURE_SHOW_ID);
		} else {
			if (gPictureActivity.isStopped == true
					|| gPictureActivity.isFinishing()) {
				Debug.d(TAG, "gPictureActivity.isStopped ="
						+ gPictureActivity.isStopped);
				DmrService.startActivity(PICTURE_SHOW_ID);
			} else {
				Debug.d(TAG, "gPictureActivity.initoperate");
				gPictureActivity.initoperate(true);

			}
		}
	}

	public void stopPictureShow() {
		if ((gPictureActivity != null) && (!gPictureActivity.isFinishing())) {
			Debug.d(TAG, "stopPictureShow  finish");
			gPictureActivity.finish();
			gPictureActivity = null;
		}
	}

	public void startUTPMediaPlayer(String mediaUrl, int pos,
			DMRService DmrService) {

		String mLocation = "";
		try {
			mLocation = URLDecoder.decode(mediaUrl, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		setService(DmrService);
		gDlnaMediaPlayerURL = playUrlUtp(mLocation);
		gLiveTV = mediaUrl.trim().startsWith("http://live.");
		gSeekPercent = pos;
		DmrService.startActivity(MEDIA_PLAYER_ID);
	}

	public void startMediaPlayer(String mediaUrl, int pos, DMRService DmrService) {
		Debug.d(TAG, "startMediaPlayer");
		setService(DmrService);
		gDlnaMediaPlayerURL = mediaUrl;
		gLiveTV = mediaUrl.trim().startsWith("http://live.");
		gSeekPercent = pos;
		DmrService.startActivity(MEDIA_PLAYER_ID);
	}

	public void startAudioPlayer(String mediaUrl, int pos, DMRService DmrService) {
		Debug.d(TAG, "startAudioPlayer");
		gDlnaMediaPlayerURL = mediaUrl;
		gSeekPercent = pos;
		setService(DmrService);
		DmrService.startActivity(AUDIO_PLAYER_ID);
	}

	public void startAudioPlayerDelayed(String mediaUrl, int pos,
			DMRService DmrService, int time) {
		Debug.d(TAG, "startAudioPlayerDelayed");
		gDlnaMediaPlayerURL = mediaUrl;
		gSeekPercent = pos;
		setService(DmrService);
		DmrService.startActivityDelayed(AUDIO_PLAYER_ID, time);
	}

	public void startAudioPlayer(String mediaUrl, int pos) {
		Debug.d(TAG, "startMediaPlayer");
		gDlnaMediaPlayerURL = mediaUrl;
		gSeekPercent = pos;
		if (gMainService != null)
			gMainService.startActivity(AUDIO_PLAYER_ID);
	}

	public void setSongName(String name) {
		mSongName = name;
	}

	public void startMediaPlayer(String mediaUrl, int pos) {
		Debug.d(TAG, "startMediaPlayer");
		gDlnaMediaPlayerURL = mediaUrl;
		gLiveTV = mediaUrl.trim().startsWith("http://live.");
		gSeekPercent = pos;
		gMainService.startActivity(MEDIA_PLAYER_ID);
	}

	public void startMediaPlayerDelayed(String mediaUrl, int media_type,
			int pos, int time) {
		Debug.d(TAG, "startMediaPlayer");
		gDlnaMediaPlayerURL = mediaUrl;
		gLiveTV = mediaUrl.trim().startsWith("http://live.");
		gSeekPercent = pos;
		gMainService.startActivityDelayed(media_type, time);
	}

	private String playUrlUtp(String url) {
		Log.e("into", "utp=====play" + url);
		if (TextUtils.isEmpty(url)) {
			return null;
		}
		String result = "";
		if (DMRService.UTP_INIT_VAL < 0) {
			return result;
		}

		PlayUrl cc = new PlayUrl(DMRService.mUTP.getServicePort(), url, "", "");
		result = cc.getPlay();
		return result;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public String mediaPlayerMaxVolumeGet() {

		if (player != null) {
			int value = player.getMaxVolume();
			Debug.d(TAG, "mediaPlayerMaxVolumeGet" + value);
			return String.valueOf(value);
		}
		return "0";
	}

	public int mediaPlayerDurationGet() {

		if (player != null) {
			int value = player.durationGet();
			Debug.d(TAG, "mediaPlayerDurationGet" + value);
			return value;// String.valueOf(value);
		}
		return 0;// "0";
	}

	public int mediaPlayerPositionGet() {
		if (player != null) {
			int value = player.positionGet();
			Debug.d(TAG, "mediaPlayerPositionGet" + value);
			return value;
		}

		return 0;
	}

	public int mediaPlayerVolumeGet() {

		if (player != null) {
			int value = player.getVolume();
			Debug.d(TAG, "mediaPlayerVolumeGet" + value);
			// return String.valueOf(value);
			return value;
		}

		return 0; // String.valueOf(0);
	}

	public void mediaPlayerVolumeSet(int progress) {

		if (player != null) {
			player.setVolume(progress);
			Debug.d(TAG, "mediaPlayerVolumeSet" + progress);
			return;
		}

		return;
	}

	public void mediaPlayerMute() {

		if (player != null) {
			player.sendMuteKey();
		}
	}

	public boolean mediaPlayerIsMute() {

		if (player != null) {
			return player.isStreamMute();
		}
		return false;
	}
}
