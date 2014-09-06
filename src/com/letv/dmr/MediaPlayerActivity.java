package com.letv.dmr;

import java.util.Timer;
import java.util.TimerTask;
import org.cybergarage.util.Debug;
import com.letv.smartControl.R;
import com.letv.upnpControl.tools.LetvUtils;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.os.PowerManager;

public class MediaPlayerActivity extends Activity implements OnKeyListener,
		MediaplayerConstants {
	private SurfaceView surfaceView;
	private static final String TAG = "MediaPlayerActivity";
	private RelativeLayout relativeLayout;
	private RelativeLayout play_now_loading;
	private RelativeLayout play_buffering_loading;
	private TextView text_position;
	private TextView text_duration;
	private TextView text_large;
	private ImageView image_play;
	private ImageView playing_state;
	private boolean mMuted = false;
	private Player player;
	private SeekBar seekBar;
	private int seekPos;
	public boolean isLiveTV = false;
	public boolean isSeeking = false;
	private Timer timerToHideSeeker;
	private boolean isStoppedByPhone;
	private UIhandler mUiHandler;
	private UPNPReceiver mUPNPReceiver = null;
	public static boolean running = false;
	private int state = STOP;
	private Drawable mButtonPlay;
	private Drawable mButtonPause;
	private Drawable mButtonFast;
	private Drawable mButtonRetreat;
	private Drawable mButtonCenterPause;
	private MediaHandlerThread mMediaHandlerThread;
	private Looper mMediaHandlerThreadLooper;
	private Workhandler mWorkHandler;
	boolean mPlayingOrPausing = false;
	private PowerManager.WakeLock mWakeLock = null;
    private AudioManager mAudioManager=null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Debug.d(TAG, "onCreate begin");
		if (LetvUtils.isLetvUi3Version()) {
			setContentView(R.layout.activity_main_letv);
		} else {
			setContentView(R.layout.activity_main);
		}

		image_play = (ImageView) findViewById(R.id.pause_iv);
		playing_state = (ImageView) findViewById(R.id.imageview_state);
		play_now_loading = (RelativeLayout) findViewById(R.id.play_now_loading);
		play_buffering_loading = (RelativeLayout) findViewById(R.id.buf_loading);
		mButtonPlay = getResources().getDrawable(
				R.drawable.ic_playbar_button_play);
		mButtonPause = getResources().getDrawable(
				R.drawable.ic_playbar_button_pause);
		mButtonFast = getResources().getDrawable(
				R.drawable.ic_playbar_button_fast_3);
		mButtonRetreat = getResources().getDrawable(
				R.drawable.ic_playbar_button_retreat_3);
		mButtonCenterPause = getResources().getDrawable(
				R.drawable.ic_playbar_stop);

		timerToHideSeeker = new Timer();

		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		mUiHandler = new UIhandler(Looper.getMainLooper());

		MediaplayerBase.getInstance().setMediaPlayer(this);

		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);    
		relativeLayout = (RelativeLayout) findViewById(R.id.cotroller_panel_rl);
		surfaceView = (SurfaceView) findViewById(R.id.surfaceview);
		seekBar = (SeekBar) findViewById(R.id.progress_sb);
		text_position = (TextView) findViewById(R.id.position_tv);
		text_duration = (TextView) findViewById(R.id.duration_tv);

		player = new Player(this, surfaceView, seekBar, text_position,
				text_duration, text_large);
		MediaplayerBase.getInstance().setPlayer(player);

		seekBar.setOnKeyListener(this);
		if (player != null) {
			int isMute = player.isStreamMute() ? 1 : 0;
			// notify client mute state control point
			Intent muteIntent = new Intent();
			muteIntent.setAction("com.letv.dlna.PLAY_SETMUTE");
			muteIntent.putExtra("MUTE", isMute);
			sendBroadcast(muteIntent);
		}

		isStoppedByPhone = false;

		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				Debug.d(TAG, "onProgressChanged :  " + progress + "fromUser="
						+ fromUser);
				if (fromUser) {
					Debug.d(TAG, "SeekBarChanged" + progress);
					player.playSeek(progress);
				}
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}
		});
		Debug.d(TAG, "onCreate savedInstanceState = " + savedInstanceState
				+ "mUPNPReceiver" + mUPNPReceiver);
		if (mUPNPReceiver != null) {

			unregisterReceiver(mUPNPReceiver);
			mUPNPReceiver = null;
		}
		mUPNPReceiver = new UPNPReceiver();
		IntentFilter localIntentFilter = new IntentFilter();
		localIntentFilter.addAction("com.letv.UPNP_STOP_ACTION");
		localIntentFilter.addAction("com.letv.UPNP_PLAY_ACTION");
		localIntentFilter.addAction("com.letv.UPNP_PAUSE_ACTION");
		localIntentFilter.addAction("com.letv.UPNP_SETVOLUME_ACTION");
		localIntentFilter.addAction("com.letv.UPNP_SETMUTE_ACTION");
		localIntentFilter.addAction("com.letv.UPNP_PLAY_SEEK");
		registerReceiver(mUPNPReceiver, localIntentFilter);
		running = true;
		state = PLAY;
		mMediaHandlerThread = new MediaHandlerThread();
		mMediaHandlerThread.start();

		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
				| PowerManager.ON_AFTER_RELEASE,
				MediaPlayerActivity.class.getName());
		mWakeLock.setReferenceCounted(false);
		mWakeLock.acquire();

		Debug.d(TAG, "onCreate end");
	}

	class UPNPReceiver extends BroadcastReceiver {
		UPNPReceiver() {
		}

		public void onReceive(Context paramContext, Intent paramIntent) {
			String action = paramIntent.getAction();
			Debug.d(TAG, "onReceive" + action);
			if (action.equals("com.letv.UPNP_PLAY_ACTION")) {
				String media_url = paramIntent.getStringExtra("media_uri");
				if (!MediaplayerBase.gDlnaMediaPlayerURL.equals(media_url)) {
					if (state != STOP) {
						Debug.d(TAG, "startMediaPlayer");
						finish();
					}
					MediaplayerBase.getInstance()
							.startMediaPlayerDelayed(media_url,
									MediaplayerBase.MEDIA_PLAYER_ID, 0, 3000);
				} else {

					Debug.d(TAG, "PLAY");
					operateMediaPlayer(MediaPlayerActivity.PLAY);
				}
				return;
			}
			if (action.equals("com.letv.UPNP_PAUSE_ACTION")) {
				operateMediaPlayer(MediaPlayerActivity.PAUSE);
				return;
			}
			if (action.equals("com.letv.UPNP_STOP_ACTION")) {
				operateMediaPlayer(MediaPlayerActivity.STOP);
				return;
			}
			if (action.equals("com.letv.UPNP_PLAY_SEEK")) {
				String rel_time = paramIntent.getStringExtra("REL_TIME");
				seekPos = parseTimeStringToMSecs(rel_time);
				Debug.d(TAG, "seekPos =" + seekPos);
				operateMediaPlayer(MediaPlayerActivity.SEEK);
			}
			if (action.equals("com.letv.UPNP_SETVOLUME_ACTION")) {
				int volume = paramIntent.getIntExtra("DesiredVolume", 50);

				Debug.d(TAG, "volume =" + volume);
				Message message = Message.obtain();
				message.arg1 = volume;
				message.what = SETVOLUME;
				mWorkHandler.sendMessage(message);
				return;
			}

			if (action.equals("com.letv.UPNP_SETMUTE_ACTION")) {
				Debug.d(TAG, "setMuteAction");
				Boolean isMute = (Boolean) paramIntent.getBooleanExtra(
						"DesiredMute", Boolean.valueOf(false));
				Debug.d(TAG, "setMuteAction isMute=" + isMute);
				Message message = Message.obtain();

				message.arg1 = isMute ? 1 : 0;
				message.what = SETMUTE;
				mWorkHandler.sendMessage(message);
				return;
			}
		}
	}

	public boolean isMuted() {
		return mMuted;
	}

	public boolean mute() {
		boolean bCommandSent = true;
		if (isMuted())
			return bCommandSent;
		if (player != null) {
			player.sendMuteKey();
			mMuted = true;
		}
		return bCommandSent;
	}

	/*
	 * UnMute FM Hardware (SoC)
	 * 
	 * @return true if set mute mode api was invoked successfully, false if the
	 * api failed.
	 */
	public boolean unMute() {
		boolean bCommandSent = true;
		if (!isMuted())
			return bCommandSent;
		if (player != null) {
			player.sendMuteKey();
			;
			mMuted = false;
		}
		return bCommandSent;
	}

	public static int parseTimeStringToMSecs(String paramString) {
		String[] arrayOfString = paramString.split(":|\\.");
		if (arrayOfString.length < 3)
			return 0;
		try {
			int i = Integer.parseInt(arrayOfString[0]);
			int j = Integer.parseInt(arrayOfString[1]);
			return 1000 * (Integer.parseInt(arrayOfString[2]) + (i * 3600 + j * 60));
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	public void onResume() {
		super.onResume();
		play_now_loading.setVisibility(View.VISIBLE);
		Debug.d(TAG, "onResume");
	}

	public void onStart() {
		super.onStart();
	}

	public void onPause() {
		super.onPause();
		Debug.d(TAG, "onPause");
		if (mUPNPReceiver != null) {
			unregisterReceiver(mUPNPReceiver);
			mUPNPReceiver = null;
		}
		if (mWakeLock != null){
			mWakeLock.release();
			mWakeLock = null;
		}
		if (!MediaPlayerActivity.this.isFinishing())
			MediaPlayerActivity.this.finish();
	}

	public void showLoading() {
		if (play_now_loading != null) {
			play_now_loading.setVisibility(View.VISIBLE);
		}
	}

	public void dismissLoading() {
		if (play_now_loading != null
				&& play_now_loading.getVisibility() == View.VISIBLE) {
			play_now_loading.setVisibility(View.INVISIBLE);
		}
	}

	public void showBufferLoading(String msg) {
		if (play_buffering_loading != null) {
			play_buffering_loading.setVisibility(View.VISIBLE);
		}
	}

	public void dismissBufferLoading() {
		if (play_buffering_loading != null) {
			play_buffering_loading.setVisibility(View.INVISIBLE);
		}
	}

	public void setSeekBarLayout(int isVisible) {
		if (isLiveTV) {
			isVisible = View.INVISIBLE;
		}
		relativeLayout.setVisibility(isVisible);
		if (isVisible == View.VISIBLE && player != null) {
			player.stopTimer();
			player.startTimer();
		} else if (player != null) {
			player.stopTimer();
		}
	}

	public class Workhandler extends Handler {
		public Workhandler(Looper looper) {
			super(looper);
		}

		public void handleMessage(Message msg) {

			Debug.d(TAG, "Workhandler msg.what begin= " + msg.what);
			switch (msg.what) {
			case PLAY:
				if (player != null && player.mediaPlayer != null) {
					state = PLAY;
					if (!player.isPlaying()) {
						player.play();
						sendPlayStateChangeBroadcast("com.letv.dlna.PLAY_PLAYING");
					}
				}
				mPlayingOrPausing = false;
				break;
			case PAUSE:
				if (player != null && player.mediaPlayer != null) {
					state = PAUSE;
					if (player.isPlaying()) {
						player.pause();
						sendPlayStateChangeBroadcast("com.letv.dlna.PLAY_PAUSED");
					}
				}
				mPlayingOrPausing = false;
				break;
			case STOP: {
				if (player != null) {
					if (mMuted) {
						player.sendMuteKey();
						mMuted = false;
					}
					player.stop();
					state = STOP;
					if (mMediaHandlerThreadLooper != null) {
						mMediaHandlerThreadLooper.quit();
						mMediaHandlerThreadLooper = null;
					}
					sendPlayStateChangeBroadcast("com.letv.dlna.PLAY_STOPPED");
				}
			}
				break;
			case SEEK:// dmr receive
				if (player != null)
					player.playProgressByTime(seekPos);
				break;
			case PROGRESS_SEEK:// press key
				int progress = msg.arg1;
				if (player != null)
					player.playSeek(progress);
				break;
			case SETVOLUME:
				int volume = msg.arg1;
				setVolume(volume);
				break;
			case SETMUTE:

				if (player != null) {
					player.sendMuteKey();
					int isMute = player.isStreamMute() ? 1 : 0;
					// notify client mute state control point
					Intent muteIntent = new Intent();
					muteIntent.setAction("com.letv.dlna.PLAY_SETMUTE");
					muteIntent.putExtra("MUTE", isMute);
					sendBroadcast(muteIntent);
				}
				break;
			default:
				break;
			}
			Debug.d(TAG, "Workhandler msg.what end = " + msg.what);

		}
	}

	private class MediaHandlerThread extends Thread {
		MediaHandlerThread() {
			super("MediaHandlerThread");
		}

		@Override
		public void run() {
			// Set this thread up so the handler will work on it
			Looper.prepare();
			mMediaHandlerThreadLooper = Looper.myLooper();
			mWorkHandler = new Workhandler(mMediaHandlerThreadLooper);
			Looper.loop();
		}
	}

	public class UIhandler extends Handler {
		public UIhandler(Looper looper) {
			super(looper);
		}

		public void handleMessage(Message msg) {

			Debug.d(TAG, "UIhandler msg.what begin= " + msg.what);
			if (player != null && player.mBuffing && player.mVideoLagging) {
				Debug.d(TAG, "buffing ,return");
				return;
			}
			switch (msg.what) {

			case PLAYORPAUSE:

				if (player != null && player.mediaPlayer != null) {
					Debug.d(TAG, "UIhandler state  = " + state);
					if (state != PLAY) {
						image_play.setVisibility(View.INVISIBLE);
						playing_state.setBackgroundDrawable(mButtonPlay);
						mWorkHandler.sendEmptyMessage(PLAY);
					} else {
						image_play.setBackgroundDrawable(mButtonCenterPause);
						image_play.setVisibility(View.VISIBLE);
						playing_state.setBackgroundDrawable(mButtonPause);
						mWorkHandler.sendEmptyMessage(PAUSE);
					}
				}
				break;
			case PLAY:
				if (player != null) {
					image_play.setVisibility(View.INVISIBLE);
					playing_state.setBackgroundDrawable(mButtonPlay);
					if (player != null && player.mediaPlayer != null) {
						if (!player.isPlaying()) {
							mWorkHandler.sendEmptyMessage(PLAY);
						}
					}

				}
				break;
			case PAUSE:
				if (player != null && player.mediaPlayer != null) {
					if (player.isPlaying()) {
						image_play.setBackgroundDrawable(mButtonCenterPause);
						image_play.setVisibility(View.VISIBLE);
						playing_state.setBackgroundDrawable(mButtonPause);
						mWorkHandler.sendEmptyMessage(PAUSE);
					}
				}
				break;
			case STOP: {
				if (isStoppedByPhone == true)
					return;
				isStoppedByPhone = true;
				mWorkHandler.sendEmptyMessage(STOP);
				if (!MediaPlayerActivity.this.isFinishing())
					MediaPlayerActivity.this.finish();
			}
				break;
			case SEEK: {
				if (isStoppedByPhone) {
					return;
				}
				if (player != null) {
					closeSeekBarAfter5s();
					int duration = player.durationGet();
					int pos = 0;
					if (duration != 0) {
						float seektime = (float) seekPos;
						float totaltime = (float) duration;
						float percent = seektime / totaltime;
						Debug.d(TAG, "progress = " + percent);
						pos = (int) (seekBar.getMax() * percent);
					}
					Debug.d(TAG, "########duration :  " + duration
							+ "Progress=" + pos + "seekPos=" + seekPos
							+ "seekbar max = " + seekBar.getMax());
					seekBar.setProgress(pos);
					setSeekBarLayout(View.VISIBLE);
				}
			}
				mWorkHandler.sendEmptyMessage(SEEK);
				break;
			case PROGRESS_SEEK:
				if (isStoppedByPhone) {
					return;
				}
				int action = msg.arg1;
				int prog = 0;
				
				int seekprog = seekBar.getMax() / 50;
				int seektime = 5 * 1000;
				if (player != null) {
					int durarion = player.durationGet();
					seektime = durarion / 30;
					if (seektime < 60 * 1000)
						seektime = 60 * 1000;
					if (durarion > 0)
						seekprog = seekBar.getMax() * seektime / durarion;
					Debug.d(TAG,
							"########seekBar.getProgress :  "
									+ seekBar.getProgress() + "seekBar.seekprog="
									+ seekprog + "seekBar.max =" + seekBar.getMax() +
									"durarion = "+ durarion) ;
					//float time = 4*60*60*1000*4*60*59;
					//int a = time/4*60*60;
				}
				
				if (action == 0) {

					prog = seekBar.getProgress() - seekprog;
					if (prog < 0) {
						prog = 0;
					}
					playing_state.setBackgroundDrawable(mButtonRetreat);
				} else {
					prog = seekBar.getProgress() + seekprog;
					if (prog > seekBar.getMax()) {
						prog = seekBar.getMax();
					}
					playing_state.setBackgroundDrawable(mButtonFast);
				}
				Message message = Message.obtain();
				message.arg1 = prog;
				message.what = PROGRESS_SEEK;
				mWorkHandler.sendMessage(message);
				break;
			case SETMUTE:
				// ui do nothing
				mWorkHandler.sendEmptyMessage(SETMUTE);
				break;
			default:
				break;
			}
			Debug.d(TAG, " UIhandler handleMessage msg.what end = " + msg.what);
		}
	}

	public void sendPlayStateChangeBroadcast(String paramString) {
		Intent localIntent = new Intent();
		localIntent.setAction(paramString);
		sendBroadcast(localIntent);
		Debug.d(TAG, "########sendPlayStateChangeBroadcast:  " + paramString);
	}

	public void operateMediaPlayer(int operateType) {
		mUiHandler.sendEmptyMessage(operateType);
	}

	public void closeSeekBarAfter5s() {

		timerToHideSeeker.cancel();
		timerToHideSeeker = new Timer();
		timerToHideSeeker.schedule(new TimerTask() {
			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (seekBar.isShown()/* &&(seekBar.isPressed()) */) {
							setSeekBarLayout(View.INVISIBLE);
						}
					}
				});
			}
		}, 5000);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		Debug.d(TAG, "onKeyUp keycode =  " + keyCode);
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_LEFT:
			seekBar.setPressed(false);
			if (player != null && player.mediaPlayer != null) {
				if (state == PLAY) {
					playing_state.setBackgroundDrawable(mButtonPlay);
				} else {
					playing_state.setBackgroundDrawable(mButtonPause);
				}
			}
			break;
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			seekBar.setPressed(false);
			if (player != null && player.mediaPlayer != null) {

				if (state == PLAY) {
					playing_state.setBackgroundDrawable(mButtonPlay);
				} else {
					playing_state.setBackgroundDrawable(mButtonPause);
				}
			}
			break;
		}
		return true;// super.onKeyUp(keyCode, event);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		Debug.d(TAG, "onKeyDown keycode =  " + keyCode);
	    int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);   
	    int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		switch (keyCode) {
		    
		case KeyEvent.KEYCODE_DPAD_UP:
			return false;
		case KeyEvent.KEYCODE_HOME:
		case KeyEvent.KEYCODE_BACK: {
			if (!MediaPlayerActivity.this.isFinishing())
				finish();
			Debug.d(TAG, "keyCode = back or home");
		}
			return true;
		case KeyEvent.KEYCODE_DPAD_DOWN:
			return false;

		case KeyEvent.KEYCODE_DPAD_CENTER: {
			mPlayingOrPausing = true;
			mUiHandler.sendEmptyMessage(PLAYORPAUSE);
		}
			break;
        case KeyEvent.KEYCODE_VOLUME_DOWN:
            
            currentVolume--;         
             if (currentVolume>=0)
            {
                 Debug.d(TAG, "(youbin)currentVolume_Down=" +  currentVolume*100/maxVolume );
                 int vule=currentVolume*100/maxVolume ;
                 setVolumeToPhone(vule);
            }            
                break;
                
             case KeyEvent.KEYCODE_VOLUME_UP:
                 
             currentVolume++;           
                 if (currentVolume<=maxVolume)
                {                                  
                     Debug.d(TAG, "((youbin))currentVolume_Up=" + currentVolume*100/maxVolume );
                     int vule= currentVolume*100/maxVolume ;
                     setVolumeToPhone(vule);
                }
          
             break;
		case KeyEvent.KEYCODE_DPAD_LEFT:

			if (!seekBar.isShown()) {
				Debug.d(TAG, "seekBar Shown ");
				setSeekBarLayout(View.VISIBLE);
			}
			seekBar.setPressed(true);
			closeSeekBarAfter5s();
			Message message = Message.obtain();
			message.arg1 = 0;
			message.what = PROGRESS_SEEK;
			mUiHandler.sendMessage(message);
			// }
			break;
		case KeyEvent.KEYCODE_DPAD_RIGHT:

			if (!seekBar.isShown()) {
				Debug.d(TAG, "seekBar Shown ");
				setSeekBarLayout(View.VISIBLE);
			}
			seekBar.setPressed(true);
			closeSeekBarAfter5s();
			Message message1 = Message.obtain();
			message1.arg1 = 1;
			message1.what = PROGRESS_SEEK;
			mUiHandler.sendMessage(message1);
			// }
			break;
		}

		return  super.onKeyDown(keyCode, event);
	}
    
    
    private void setVolumeToPhone(int volume){
        
        Intent volumeIntent = new Intent();
        volumeIntent.setAction("com.letv.dlna.PLAY_SETVOLUME");
        volumeIntent.putExtra("VOLUME", volume);
        sendBroadcast(volumeIntent);

        
    }
	public void setVolume(int volume) {

		// notify client control point
		Intent volumeIntent = new Intent();
		volumeIntent.setAction("com.letv.dlna.PLAY_SETVOLUME");
		volumeIntent.putExtra("VOLUME", volume);
		sendBroadcast(volumeIntent);

		int isMute = 0;
		if (volume <= 0) {

			isMute = 1;
		} else {
			isMute = 0;
		}
		// notify client mute state control point
		Intent muteIntent = new Intent();
		muteIntent.setAction("com.letv.dlna.PLAY_SETMUTE");
		muteIntent.putExtra("MUTE", isMute);
		sendBroadcast(muteIntent);
		if (player != null)
			player.setVolume(volume);
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		Debug.d(TAG, "onKey keycode =  " + keyCode);
		switch (keyCode) {
		case KeyEvent.KEYCODE_HOME:
		case KeyEvent.KEYCODE_BACK: {
			if (!MediaPlayerActivity.this.isFinishing())
				finish();
			Debug.d(TAG, "keyCode = back or home");
		}
			return true;
		default:
			break;
		}
		return false;
	}

	public Player getMediaPlayer() {
		return player;
	}

	protected void onDestroy() {
		super.onDestroy();
		running = false;
		MediaplayerBase.getInstance().setMediaPlayer(null);
		MediaplayerBase.getInstance().setPlayer(null);

		Debug.d(TAG, "onDestroy");
	}

	@Override
	protected void onStop() {
		super.onStop();
		mUiHandler.removeCallbacksAndMessages(null);
		mUiHandler.sendEmptyMessage(STOP);
		mWorkHandler.removeCallbacksAndMessages(null);
		System.gc();
		Debug.d(TAG, "onStop");
	}
}
