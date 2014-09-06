package com.letv.dmr;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import org.cybergarage.util.Debug;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.os.ServiceManager;
import android.view.IWindowManager;
import android.os.RemoteException;
import android.view.KeyEvent;
import android.os.SystemProperties;
import com.letv.smartControl.R;
import com.letv.upnpControl.tools.LetvUtils;
import com.mstar.tv.service.interfaces.ITvServiceServer;

@SuppressLint("HandlerLeak")
public class Player implements OnBufferingUpdateListener, OnCompletionListener,
		OnErrorListener, OnInfoListener, OnSeekCompleteListener,
		MediaPlayer.OnPreparedListener, SurfaceHolder.Callback {
	private static final String TAG = "Player";
	private static final String TV_SERVICE = "tv_services";
	public MediaPlayer mediaPlayer;
	private SurfaceHolder surfaceHolder;
	private SeekBar skbProgress;
	private Timer mTimer = new Timer();
	TimerTask mTimerTask = null;
	private Activity activity;
	private AudioManager am;
	private TextView text_position;
	private TextView text_duration;
	private TextView text_large;
	private int position = 0;
	private int duration = 0;
	private int videoWidth;
	private int videoHeight;
	private boolean mediaPlayerStoping = false;
	private int maxVol = 0;
	private int time = 0;
	private boolean mSurfaceExist = false;
	private boolean prepared = false;
	public boolean mSeekComplete = true;
	//private Display currDisplay;
	//private SurfaceView mVideoSurface = null;
	public boolean mBuffing = false;
	public boolean mVideoLagging = false;

	@SuppressWarnings("deprecation")
	public Player(final Activity activity, SurfaceView surfaceView,
			SeekBar skbProgress, TextView text_position,
			TextView text_duration, TextView text_large) {
		this.activity = activity;
		this.skbProgress = skbProgress;
		this.text_duration = text_duration;
		this.text_position = text_position;
		this.text_large = text_large;
		if (LetvUtils.isLetvUi3Version()) {
			skbProgress.setThumb(getThumbDrawable("00:00:00"));
		}
		am = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
		String product = SystemProperties.get("ro.letv.product.name");

		if (product.contains("S50") || product.contains("S40")) {
			maxVol = 100;
		} else {
			maxVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		}
		int currentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
		// notify control point dmr mediaplayer current volume
		Intent volumeIntent = new Intent();
		volumeIntent.setAction("com.letv.dlna.PLAY_SETVOLUME");
		volumeIntent.putExtra("VOLUME", 100 * currentVolume / maxVol);
		activity.sendBroadcast(volumeIntent);
		//mVideoSurface = surfaceView;
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(this);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		//currDisplay = activity.getWindowManager().getDefaultDisplay();
		startTimer();
		mediaPlayerStoping = false;
		mSeekComplete = true;
		mBuffing = false;
		mVideoLagging = false;
	}

	private Drawable getThumbDrawable(String text) {
		int fontSize = (int) activity.getResources().getDimension(
				R.dimen.thumb_position_font_size);
		int fontX = (int) activity.getResources().getDimension(
				R.dimen.thumb_position_font_x);
		int fontY = (int) activity.getResources().getDimension(
				R.dimen.thumb_position_font_y);
		Bitmap bitmap = BitmapFactory.decodeResource(activity.getResources(),
				R.drawable.playbar_time_box)
				.copy(Bitmap.Config.ARGB_8888, true);
		Paint paint = new Paint();
		paint.setStyle(Style.FILL);
		paint.setFlags(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(activity.getResources().getColor(android.R.color.white));
		paint.setTextSize(fontSize);
		Canvas canvas = new Canvas(bitmap);
		canvas.drawText(text, fontX, fontY, paint);
		Drawable newThumb = new BitmapDrawable(activity.getResources(), bitmap);
		return newThumb;
	}

	Handler handleProgress = new Handler() {
		public void handleMessage(Message msg) {

			int progress = msg.arg1;
			if (activity instanceof MediaPlayerActivity) {
				if (((MediaPlayerActivity) activity).mPlayingOrPausing == true)
					return;
			}
			if (mediaPlayer == null || !isPrepared()
					|| mediaPlayerStoping == true || mSeekComplete == false
					|| mBuffing == true)
				return;

			if (progress >= 0) {
				if (progress == skbProgress.getMax()) {

					mediaPlayer
							.seekTo((int) duration * (skbProgress.getMax() - 1)
									/ skbProgress.getMax());
				} else {
					mediaPlayer.seekTo((int) duration * progress
							/ skbProgress.getMax());
				}
			}

			position = mediaPlayer.getCurrentPosition();

			if (duration <= 0)
				return;

			if (position > duration) {
				position = 0;
			}

			int pos = (int) (skbProgress.getMax() * ((float) position / (float) duration));

			Debug.d(TAG, "play setProgress =" + pos);
			int kp = (int) (position / 1000);
			int pminute = kp / 60;
			int phour = pminute / 60;
			int psecond = kp % 60;
			pminute %= 60;
			if (LetvUtils.isLetvUi3Version()) {
				skbProgress.setThumb(getThumbDrawable(String.format(
						"%02d:%02d:%02d", phour, pminute, psecond)));
			} else {

				text_position.setText(String.format("%02d:%02d:%02d", phour,
						pminute, psecond));
				if (text_large != null)
					text_large.setText(text_position.getText() + "/"
							+ text_duration.getText());

			}
			skbProgress.setProgress(pos);

		}
	};

	public void play() {
		Debug.d(TAG, "play");
		try {
			if (mediaPlayer != null) {
				mediaPlayer.start();
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}

	public void pause() {
		try {
			if (mediaPlayer != null) {
				mediaPlayer.pause();
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}

	public void stop() {

		if (mediaPlayer != null) {
			mediaPlayerStoping = true;
			stopTimer();
			try {
				mediaPlayer.stop();
				mediaPlayer.reset();
				mediaPlayer.release();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			}
			mediaPlayer = null;
		}
	}

	public void playUrl(String videoUrl) {

		try {
			if (videoUrl != null) {
				mediaPlayer.reset();
				mediaPlayer.setDisplay(surfaceHolder);
				mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
				mediaPlayer.setDataSource(videoUrl);
				if (activity instanceof AudioPlayerActivity) {
					((AudioPlayerActivity) activity).showLoading();
				}
				prepared = false;
				mediaPlayer.prepareAsync();
			}

		} catch (IllegalArgumentException e) {

			e.printStackTrace();
		} catch (IllegalStateException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	public void playUrl(String videoUrl, int time) {
		Debug.d(TAG, "playUrl");
		this.time = time;
	    if (activity instanceof AudioPlayerActivity) {
			((AudioPlayerActivity) activity).showLoading();
		}
		try {
			if (videoUrl != null) {
				mediaPlayer.reset();
				mediaPlayer.setDisplay(surfaceHolder);
				mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
				Debug.e("player", "video url = " + Uri.parse(videoUrl));
				mediaPlayer.setDataSource(activity, Uri.parse(videoUrl));
				prepared = false;
				mediaPlayer.prepareAsync();
			}

		} catch (IllegalArgumentException e) {
			Debug.d(TAG, "IllegalArgumentException" + e);
			e.printStackTrace();
		} catch (IllegalStateException e) {
			Debug.d(TAG, "IllegalStateException" + e);
			e.printStackTrace();
		} catch (IOException e) {
			Debug.d(TAG, "IOException" + e);
			e.printStackTrace();
			activity.finish();
		} catch (Exception e) {
			Debug.d(TAG, "Exception" + e);
		}
	}

	public void startTimer() {
		if (mTimer == null) {
			mTimer = new Timer();
		}

		if (mTimerTask == null) {
			mTimerTask = new TimerTask() {
				public void run() {
					if (mediaPlayer == null || !isPrepared()) {
						Debug.d(TAG, "mediaPlayer == null or prepared =  "
								+ prepared);
						return;

					}

					Debug.d(TAG, "TimerTask handle progress");
					Message message = Message.obtain();
					message.arg1 = -1;
					handleProgress.sendMessage(message);
				}
			};
		}

		if (mTimer != null && mTimerTask != null)
			mTimer.schedule(mTimerTask, 0, 500);

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

	@Override
	public void onBufferingUpdate(MediaPlayer arg0, int bufferingProgress) {
		Debug.d(TAG, "onBufferingUpdate= " + bufferingProgress);
		skbProgress.setSecondaryProgress(bufferingProgress);
	}

	public void onSeekComplete(MediaPlayer arg0) {
		Debug.d(TAG, "onSeekComplete= ");
		mSeekComplete = true;
	}

	@Override
	public void onCompletion(MediaPlayer arg0) {
		Debug.d(TAG, "finish playing");
		activity.finish();
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		Debug.d(TAG, "surface  Changed");
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {

		mSurfaceExist = true;
		try {
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setDisplay(surfaceHolder);
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setScreenOnWhilePlaying(true);
			mediaPlayer.setOnErrorListener(this);
			mediaPlayer.setOnBufferingUpdateListener(this);
			mediaPlayer.setOnPreparedListener(this);
			mediaPlayer.setOnCompletionListener(this);
			mediaPlayer.setOnSeekCompleteListener(this);
			mediaPlayer.setOnInfoListener(this);

			// mediaPlayer.setOnBufferingEventListener(this);
			/* modify for tencent video dlna */
			String newUrl = MediaplayerBase.gDlnaMediaPlayerURL.replace(
					"&amp;", "&");
			playUrl(newUrl, MediaplayerBase.gSeekPercent);


		} catch (Exception e) {
			Debug.d(TAG, "error" + e);
		}
		Debug.d(TAG, "surface created");
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		Debug.d(TAG, "surface Destroyed");
		mSurfaceExist = false;

	}

	@Override
	public void onPrepared(MediaPlayer player) {
		Debug.d(TAG, "on Prepared time = " + time);
		if (mediaPlayer == null) {
			Debug.d(TAG, "on Prepared mediaplayer = null ");
		}
		if (mediaPlayer != null) {
			videoWidth = mediaPlayer.getVideoWidth();
			videoHeight = mediaPlayer.getVideoHeight();
		}
		Debug.d(TAG, "on Prepared  video width = " + videoWidth
				+ "video height = " + videoHeight);
//		Debug.d(TAG,
//				"on Prepared  currDisplay.getWidth()  = "
//						+ currDisplay.getWidth()
//						+ "video currDisplay.getHeight() = "
//						+ currDisplay.getHeight());
		/*
		 * if(videoWidth < currDisplay.getWidth() && videoHeight <
		 * currDisplay.getHeight()){ /*float wRatio =
		 * (float)currDisplay.getWidth()/(float)videoWidth; float hRatio =
		 * (float)currDisplay.getHeight()/(float)videoHeight;
		 * 
		 * float ratio = Math.max(wRatio, hRatio);
		 * 
		 * videoWidth = (int)Math.ceil((float)currDisplay.getWidth()/ratio);
		 * videoHeight = (int)Math.ceil((float)currDisplay.getHeight()/ratio);
		 * Debug.d(TAG, "on Prepared  video width = " + videoWidth +
		 * "video height = " + videoHeight);
		 */

		/*
		 * android.widget.RelativeLayout.LayoutParams sufaceviewParams =
		 * (android.widget.RelativeLayout.LayoutParams)
		 * mVideoSurface.getLayoutParams(); sufaceviewParams.height=
		 * videoHeight; sufaceviewParams.width = videoWidth;
		 * mVideoSurface.setLayoutParams(sufaceviewParams); }
		 */
		// android.widget.RelativeLayout.LayoutParams sufaceviewParams =
		// (android.widget.RelativeLayout.LayoutParams)
		// mVideoSurface.getLayoutParams();
		//
		// int display_width = currDisplay.getWidth();
		// int display_height = currDisplay.getHeight();
		//
		// if ( videoWidth * display_height > display_width * videoHeight ) {
		// sufaceviewParams.height = display_width * videoHeight / videoWidth;
		//
		// } else if ( videoWidth * display_height < display_width * videoHeight
		// ) {
		// sufaceviewParams.width = display_height * videoWidth / videoHeight;
		//
		// }else{
		// sufaceviewParams.height=display_height;
		// sufaceviewParams.width = display_width;
		// }
		// mVideoSurface.setLayoutParams(sufaceviewParams);
		// start media player
		try {
			if (player != null)
				player.start();
		} catch (IllegalStateException e) {
			Debug.d(TAG, "onPrepared IllegalStateException" + e);
			e.printStackTrace();
		}
		Debug.d(TAG, "on Prepared done");

		prepared = true;
		if (mediaPlayer != null)
			duration = mediaPlayer.getDuration();
		if (duration == 0) {
			Debug.d(TAG, "on Prepared duration = 0 ");
		}
		if (LetvUtils.isLetvUi3Version()) {
			skbProgress.setMax(duration / 1000);
		}
		if (activity instanceof MediaPlayerActivity) {
			if (duration <= 0)// live TV
			{

				((MediaPlayerActivity) activity)
						.setSeekBarLayout(View.INVISIBLE);
				((MediaPlayerActivity) activity).isLiveTV = true;
			} else {
				((MediaPlayerActivity) activity).closeSeekBarAfter5s();
				((MediaPlayerActivity) activity).setSeekBarLayout(View.VISIBLE);
				((MediaPlayerActivity) activity).isLiveTV = false;
			}
		} else if (activity instanceof AudioPlayerActivity) {

			((AudioPlayerActivity) activity).setSeekBarLayout(View.VISIBLE);
		}

		if (time != 0 && mediaPlayer != null) {
			//mediaPlayer.seekTo(mediaPlayer.getDuration() / 100 * time);
			mediaPlayer.seekTo(time);//time unit millisecond
		}

		time = 0;

		int kd = duration / 1000;
		int dminute = kd / 60;
		int dhour = dminute / 60;
		int dsecond = kd % 60;

		dminute %= 60;
		text_duration.setText(String.format("%02d:%02d:%02d", dhour, dminute,
				dsecond));

		if (activity instanceof MediaPlayerActivity) {
			((MediaPlayerActivity) activity)
					.sendPlayStateChangeBroadcast("com.letv.dlna.PLAY_PLAYING");
			((MediaPlayerActivity) activity).dismissLoading();
		} else if (activity instanceof AudioPlayerActivity) {
			((AudioPlayerActivity) activity).dismissLoading();
			((AudioPlayerActivity) activity)
					.sendPlayStateChangeBroadcast("com.letv.dlna.PLAY_PLAYING");
		}
	}

	public void setVolume(int progress) {
		String product = SystemProperties.get("ro.letv.product.name");
		Debug.d(TAG, " version=" + product);

		progress = (int) (maxVol * ((float) progress / 100));

		if (progress > maxVol) {
			progress = maxVol;
		}
		Debug.d(TAG, "setVolume" + progress);
		if (product.contains("S50") || product.contains("S40")) {
			addVolume(progress);
		}else{
			am.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 1);
		}
	}

	private void addVolume(int volume) {
		ITvServiceServer server = ITvServiceServer.Stub
				.asInterface(ServiceManager.checkService(TV_SERVICE));
		if (server == null) {
			Debug.d(TAG, "Unable to find ITvService interface.");
		}
		if (server != null) {
			try {
				int curVolume = server.getVolume();
				int addVol = volume - curVolume;
				boolean isAdd = addVol > 0 ? true : false;
				addVol = addVol > 0 ? addVol : (-addVol);
				server.adjustVolume(isAdd, addVol);
			} catch (RemoteException ex) {
				Debug.d(TAG, "RemoteException from getTvService()");
			}
		}
	}

	private void sendKeyEvent(int keyCode) {
		try {

			Debug.d(TAG, "MediaPlayer  sendKeyEvent =" + keyCode);

			KeyEvent down = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
			KeyEvent up = new KeyEvent(KeyEvent.ACTION_UP, keyCode);
			IWindowManager windowManager = IWindowManager.Stub
					.asInterface(ServiceManager.getService("window"));
			windowManager.injectKeyEvent(down, true);
			windowManager.injectKeyEvent(up, true);
		} catch (RemoteException e) {
			Debug.d(TAG, "DeadOjbectException");
		}
	}

	public boolean isPlaying() {
		try {
			if (mediaPlayer != null) {
				return mediaPlayer.isPlaying();
			}
		} catch (IllegalStateException e) {
			Debug.d(TAG, "positionGet IllegalStateException" + e);
			e.printStackTrace();
		}
		return false;
	}

	public void setMute(boolean ismute) {
		Debug.d(TAG, "setMute" + ismute);
		am.setStreamMute(AudioManager.STREAM_MUSIC, ismute);
	}

	public void sendMuteKey() {
		sendKeyEvent(KeyEvent.KEYCODE_VOLUME_MUTE);
	}

	public boolean isStreamMute() {

		String version = SystemProperties.get("ro.letv.release.version");
		Debug.d(TAG, "isStreamMute version=" + version);
		if (version.contains("X60")) {
			return am.isMasterMute();
		}
		return am.isStreamMute(AudioManager.STREAM_MUSIC);
	}

	public int getVolume() {

		String product = SystemProperties.get("ro.letv.product.name");
		int volume = 0;
		if (product.contains("S50") || product.contains("S40")) {
			ITvServiceServer server = ITvServiceServer.Stub
					.asInterface(ServiceManager.checkService(TV_SERVICE));
			if (server == null) {
				Debug.d(TAG, "Unable to find ITvService interface.");
			}
			if (server != null) {
				try {
					volume = server.getVolume();
				} catch (RemoteException ex) {
					Debug.d(TAG, "RemoteException from getTvService()");
				}
			}

		} else {
			volume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
		}
		if (maxVol != 0) {
			return volume;// 10* volume/maxVol;
		}
		return 0;
	}

	public int getMaxVolume() {
		return maxVol;
	}

	public boolean isPrepared() {
		return prepared;
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		Debug.d(TAG, "onError extra=" + extra);
		if (mediaPlayer != null) {

			String errorType = "MEDIA_ERROR_UNKNOWN";
			if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
				errorType = "MEDIA_ERROR_SERVER_DIED";
			} else if (what == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
				errorType = "MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK";
			} else {
				Debug.d(TAG, "error finish activity "
						+ "MediaPlayer onError: what=" + what + " extra="
						+ extra + "errorType= " + errorType);
				if (!activity.isFinishing())
					activity.finish();
				return false;
			}

			Debug.d(TAG, "MediaPlayer onError: what=" + what + " extra="
					+ extra + "errorType= " + errorType);

			mediaPlayer.stop();
			mediaPlayer.release();
			mediaPlayer = null;
			if (surfaceHolder != null && mSurfaceExist) {
				mediaPlayer = new MediaPlayer();
				mediaPlayer.setDisplay(surfaceHolder);
				mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
				mediaPlayer.setOnErrorListener(this);
				mediaPlayer.setOnBufferingUpdateListener(this);
				mediaPlayer.setOnPreparedListener(this);
				mediaPlayer.setOnCompletionListener(this);
				mediaPlayer.setOnSeekCompleteListener(this);
				// mediaPlayer.setOnBufferingEventListener(this);
				playUrl(MediaplayerBase.gDlnaMediaPlayerURL,
						MediaplayerBase.gSeekPercent);
			}
		}
		return false;
	}

	public void playSeek(int progress) {

		if (mediaPlayer == null || !isPrepared() || mediaPlayerStoping == true
				|| mSeekComplete == false) {
			return;
		}

		mSeekComplete = false;
		if (skbProgress.getMax() <= 0) {
			return;
		}
		Debug.d(TAG, "playSeek progress=" + progress);
		if (progress >= 0) {
			if (progress == skbProgress.getMax()) {
               //haved end,no need seek
			} else {
				float persent = (float)progress/(float)skbProgress.getMax();
				int time = (int)(duration * persent);
				Debug.d(TAG, "playSeek time=" + time);
				mediaPlayer.seekTo(time);
			}
		}

	}

	public int playProgressByTime(int second) {
		if (second == 0 || duration == 0)
			return 1;
		if (mediaPlayer != null) {
			mediaPlayer.seekTo((int) second);
		}
		Debug.d(TAG, "seekTo" + second);
		return (1000 * 100) / (duration * second);
	}

	public int durationGet() {
		return duration;
	}

	public int positionGet() {
		if (duration <= 0)
			return 0;
		if (mSeekComplete == false || mBuffing == true
				|| mediaPlayerStoping == true)
			return position;
		try {
			if (mediaPlayer != null) {
				position = mediaPlayer.getCurrentPosition();
			}
		} catch (IllegalStateException e) {
			Debug.d(TAG, "positionGet IllegalStateException" + e);
			e.printStackTrace();
		}
		if (position > duration) {
			position = 0;
		}
		return position;
	}

	public boolean onInfo(MediaPlayer mp, int what, int extra) {
		Debug.d(TAG, "onInfo what = " + what + "extra = " + extra);
		if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
			mBuffing = true;
			if (activity instanceof MediaPlayerActivity)
				((MediaPlayerActivity) activity).showBufferLoading(null);

		} else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
			mBuffing = false;
			if (activity instanceof MediaPlayerActivity)
				((MediaPlayerActivity) activity).dismissBufferLoading();
		} else if (what == MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING) {
			mVideoLagging = true;
		}
		return true;
	}
	/*
	 * @Override public void onBufferingEvent(MediaPlayer arg0, float arg1,
	 * boolean arg2) {
	 * 
	 * float percent = arg1; boolean bufferingStarted = arg2;
	 * 
	 * if(bufferingStarted) { if(skbProgress.isPressed()==false) {
	 * ((MediaPlayerActivity)
	 * activity).showBufferLoading(String.valueOf((int)percent)); } } else {
	 * ((MediaPlayerActivity) activity).dismissBufferLoading(); }
	 * 
	 * }
	 */
}
