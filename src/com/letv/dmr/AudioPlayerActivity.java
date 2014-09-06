package com.letv.dmr;

import org.cybergarage.util.Debug;
import com.letv.smartControl.R;
import com.letv.upnpControl.tools.LetvUtils;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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

public class AudioPlayerActivity extends Activity implements OnKeyListener,
		MediaplayerConstants {

	private static final String TAG = "AudioPlayerActivity";
	private SurfaceView surfaceView;
	private RelativeLayout relativeLayout;
	private RelativeLayout play_now_loading;
	private TextView text_position;
	private TextView text_duration;
	private TextView text_large;
	private ImageView image_play;
	private ImageView playing_state;
	private Player player;
	private SeekBar seekBar;
	private int seekPos;
	private int mMuteConut = 0;
	private boolean mMuted = false;
	private AnimationDrawable diskAnimation;
	private Workhandler mWorkHandler;
	private Looper mMediaHandlerThreadLooper;
	private MediaHandlerThread mMediaHandlerThread;
	private UIhandler mUiHandler;
	private UPNPReceiver mUPNPReceiver = null;
	public static boolean running = false;
	private int state = STOP;
	private TextView text_songName;
	private PowerManager.WakeLock mWakeLock = null;
    private AudioManager mAudioManager=null;
    private boolean isSeekState=false;//暂停状态下快进，还是播放中快进
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Debug.d(TAG, "onCreate begin");
		if (LetvUtils.isLetvUi3Version()) {
			setContentView(R.layout.audioactivity_main_letv);
		} else {
			setContentView(R.layout.audioactivity_main);
		}
		image_play = (ImageView) findViewById(R.id.pause_iv);
		playing_state = (ImageView) findViewById(R.id.imageview_state);
		text_songName = (TextView) findViewById(R.id.song_name_txt);
		String song_name = MediaplayerBase.mSongName;
		Debug.d(TAG, "onCreate song_name = " + song_name);
		if (song_name != null) {
			text_songName.setText(song_name);
		}
		// 初始化
		ImageView imageView = (ImageView) findViewById(R.id.player_view_music_image_disk);
		diskAnimation = (AnimationDrawable) imageView.getBackground();

		play_now_loading = (RelativeLayout) findViewById(R.id.play_now_loading);
		relativeLayout = (RelativeLayout) findViewById(R.id.cotroller_panel_rl);
		surfaceView = (SurfaceView) findViewById(R.id.surfaceview);
		seekBar = (SeekBar) findViewById(R.id.progress_sb);
		text_position = (TextView) findViewById(R.id.position_tv);
		text_duration = (TextView) findViewById(R.id.duration_tv);

		mUiHandler = new UIhandler(Looper.getMainLooper());
		MediaplayerBase.getInstance().setAudioPlayer(this);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);    
		player = new Player(this, surfaceView, seekBar, text_position,
				text_duration, text_large);
		MediaplayerBase.getInstance().setPlayer(player);

		seekBar.setOnKeyListener(this);
		Intent muteIntent = new Intent();
		muteIntent.setAction("com.letv.dlna.PLAY_SETMUTE");
		muteIntent.putExtra("MUTE", 0);
		sendBroadcast(muteIntent);

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
		mMediaHandlerThread = new MediaHandlerThread();
		mMediaHandlerThread.start();
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
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
				| PowerManager.ON_AFTER_RELEASE,
				AudioPlayerActivity.class.getName());
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
									MediaplayerBase.AUDIO_PLAYER_ID, 0, 3000);
				} else {
					if (state != PLAY) {
						Debug.d(TAG, "PLAY");
						MediaplayerBase.getInstance().operateMediaPlayer(
								AudioPlayerActivity.PLAY);
						state = PLAY;
					}
				}
				return;
			}
			if (action.equals("com.letv.UPNP_PAUSE_ACTION")) {
				MediaplayerBase.getInstance().operateMediaPlayer(
						AudioPlayerActivity.PAUSE);
				state = PAUSE;
				return;
			}
			if (action.equals("com.letv.UPNP_STOP_ACTION")) {
				MediaplayerBase.getInstance().operateMediaPlayer(
						AudioPlayerActivity.STOP);
				state = STOP;
				return;
			}
			if (action.equals("com.letv.UPNP_PLAY_SEEK")) {
				String rel_time = paramIntent.getStringExtra("REL_TIME");
				seekPos = parseTimeStringToMSecs(rel_time);
				Debug.d(TAG, "seekPos =" + seekPos);
				operateMediaPlayer(AudioPlayerActivity.SEEK);
			}
			if (action.equals("com.letv.UPNP_SETVOLUME_ACTION")) {
				int volume = paramIntent.getIntExtra("DesiredVolume", 50);
				Debug.d(TAG, "volume =" + volume);
				setVolume(volume);
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
		if (isMuted() && player.isStreamMute())
			return bCommandSent;
		player.setMute(true);
		mMuted = true;
		mMuteConut++;
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
		if (!isMuted() && !player.isStreamMute())
			return bCommandSent;
		player.setMute(false);
		mMuted = false;
		mMuteConut--;
		return bCommandSent;
	}

	public static int parseTimeStringToMSecs(String paramString) {
		String[] arrayOfString = paramString.split(":|\\.");
		if (arrayOfString.length < 3)
			return 0;
		int i = Integer.parseInt(arrayOfString[0]);
		int j = Integer.parseInt(arrayOfString[1]);
		return 1000 * (Integer.parseInt(arrayOfString[2]) + (i * 3600 + j * 60));
	}

	public void onResume() {
		super.onResume();
		// 开始动画
		if (diskAnimation != null && !diskAnimation.isRunning()) {
			diskAnimation.start();
		}
		Debug.d(TAG, "onResume");
	}

	public void onStart() {
		super.onStart();
		Debug.d(TAG, "onStart");
	}

	@Override
	public void onPause() {
		super.onPause();
		Debug.d(TAG, "onPause");
		// 停止动画
		if (diskAnimation != null && diskAnimation.isRunning()) {
			diskAnimation.stop();
		}
		if (mUPNPReceiver != null) {
			unregisterReceiver(mUPNPReceiver);
			mUPNPReceiver = null;
		}
		if (mWakeLock != null) {
			mWakeLock.release();
			mWakeLock = null;
		}
		if (!AudioPlayerActivity.this.isFinishing())
			AudioPlayerActivity.this.finish();
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

	public void setSeekBarLayout(int isVisible) {

		relativeLayout.setVisibility(isVisible);
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

	@SuppressLint("HandlerLeak")
	private class Workhandler extends Handler {

		public Workhandler(Looper looper) {
			super(looper);
		}

		@SuppressLint("HandlerLeak")
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case PLAY:
				if (player != null) {
					player.play();
					sendPlayStateChangeBroadcast("com.letv.dlna.PLAY_PLAYING");
				}
				break;
			case PAUSE:
				if (player != null && player.mediaPlayer != null) {
					if (player.isPlaying()) {
						player.pause();
						sendPlayStateChangeBroadcast("com.letv.dlna.PLAY_PAUSED");
					}
				}
				break;
			case STOP: {
				if (player != null) {
					if (mMuted) {
						player.setMute(false);
						mMuteConut--;
						mMuted = false;
					}
					Debug.d(TAG, "onstop mMuteConut = " + mMuteConut);
					player.stop();
					if (mMediaHandlerThreadLooper != null) {
						mMediaHandlerThreadLooper.quit();
						mMediaHandlerThreadLooper = null;
					}
					sendPlayStateChangeBroadcast("com.letv.dlna.PLAY_STOPPED");
				}
			}
				break;
			case SEEK:
				player.playProgressByTime(seekPos);
				break;
			case PROGRESS_SEEK:
				int progress = msg.arg1;
				player.playSeek(progress);
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
		}
	}

	@SuppressLint("HandlerLeak")
	class UIhandler extends Handler {
		public UIhandler(Looper looper) {
			super(looper);
		}

		public void handleMessage(Message msg) {

			switch (msg.what) {
			case PLAYORPAUSE:

				if (player != null && player.mediaPlayer != null) {
					Debug.d(TAG, "UIhandler state  = " + state);
					if (!player.isPlaying()) {
						image_play.setVisibility(View.INVISIBLE);
						playing_state
								.setBackgroundResource(R.drawable.ic_playbar_button_play);
						mWorkHandler.sendEmptyMessage(PLAY);
					} else {
						image_play.setVisibility(View.VISIBLE);
						playing_state
								.setBackgroundResource(R.drawable.ic_playbar_button_pause);
						mWorkHandler.sendEmptyMessage(PAUSE);
					}
				}
				break;
			case PLAY:
				if (player != null) {
					if (image_play.getVisibility() == View.VISIBLE) {
						image_play.setVisibility(View.GONE);
					}
					playing_state
							.setBackgroundResource(R.drawable.ic_playbar_button_play);
				}
				break;
			case PAUSE:
				if (player != null && player.mediaPlayer != null) {
					if (player.isPlaying()) {
						image_play.setVisibility(View.VISIBLE);
						playing_state
								.setBackgroundResource(R.drawable.ic_playbar_button_pause);
					}
				}
				break;
			case STOP: {
				if (!AudioPlayerActivity.this.isFinishing())
					AudioPlayerActivity.this.finish();
			}
				break;
			case SEEK: {
				if (player != null) {
					int duration = player.durationGet();
					long pos = 0;
					if (duration != 0) {
						float seektime = (float) seekPos;
						float totaltime = (float) duration;
						float percent = seektime / totaltime;
						Debug.d(TAG, "progress = " + percent);
						pos = (int) (seekBar.getMax() * percent);

					}
					Debug.d(TAG, "########duration :  " + duration
							+ "Progress=" + pos + "seekPos=" + seekPos);
					seekBar.setProgress((int) pos);

					setSeekBarLayout(View.VISIBLE);
				}
			}
				break;
			case PROGRESS_SEEK:

				int action = msg.arg1;
				int prog,
				seekprog = seekBar.getMax() / 25;
				int seektime = 5 * 1000;
				if (player != null) {
					int durarion = player.durationGet();
					seektime = durarion / 25;
					if (seektime < 5 * 1000)
						seektime = 5 * 1000;
					if (durarion > 0)
						seekprog = seekBar.getMax() * seektime / durarion;
				}
				Debug.d(TAG,
						"########seekBar.getProgress :  "
								+ seekBar.getProgress() + "seekprog="
								+ seekprog);
				if (action == 0) {

					prog = seekBar.getProgress() - seekprog;
					if (prog < 0) {
						prog = 0;
					}
					playing_state
							.setBackgroundResource(R.drawable.ic_playbar_button_retreat_3);
				} else {
					prog = seekBar.getProgress() + seekprog;
					if (prog > seekBar.getMax()) {
						prog = seekBar.getMax();
					}
					playing_state
							.setBackgroundResource(R.drawable.ic_playbar_button_fast_3);
				}
				Message message = Message.obtain();
				message.arg1 = prog;
				message.what = PROGRESS_SEEK;
				mWorkHandler.sendMessage(message);
				break;
			default:
				break;
			}
			mWorkHandler.sendEmptyMessage(msg.what);
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

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		Debug.d(TAG, "onKeyUp keycode =  " + keyCode);
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_LEFT:
			if (player != null && player.mediaPlayer != null) {
			    if (isSeekState==true)
                {
			        player.play();
			        isSeekState=false;
                }
			  
				if (player.isPlaying()) {
					playing_state
							.setBackgroundResource(R.drawable.ic_playbar_button_play);
				} else {
					playing_state
							.setBackgroundResource(R.drawable.ic_playbar_button_pause);
				}
			}
			break;
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			if (player != null && player.mediaPlayer != null) {
			     if (isSeekState==true)
	                {
	                    player.play();
	                    isSeekState=false;
	                }
				if (player.isPlaying()) {
					playing_state
							.setBackgroundResource(R.drawable.ic_playbar_button_play);
				} else {
					playing_state
							.setBackgroundResource(R.drawable.ic_playbar_button_pause);
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
		case KeyEvent.KEYCODE_BACK:
			if (!AudioPlayerActivity.this.isFinishing())
				finish();
			Debug.d(TAG, "keyCode = back or home");
			return true;
		case KeyEvent.KEYCODE_DPAD_DOWN:
			return false;
		case KeyEvent.KEYCODE_DPAD_CENTER:
			if (player.isPlaying()) {
				image_play.setVisibility(View.VISIBLE);
				playing_state
						.setBackgroundResource(R.drawable.ic_playbar_button_pause);
				mWorkHandler.sendEmptyMessage(PAUSE);
			} else {
				image_play.setVisibility(View.GONE);
				playing_state
						.setBackgroundResource(R.drawable.ic_playbar_button_play);
				mWorkHandler.sendEmptyMessage(PLAY);
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
		    if ( player.isPlaying())
            {
	            player.pause();
	            isSeekState=true;

            }
			if (!seekBar.isShown()) {
				Debug.d(TAG, "seekBar Shown ");
				setSeekBarLayout(View.VISIBLE);
			}
			seekBar.setPressed(true);
			Message message = Message.obtain();
			message.arg1 = 0;
			message.what = PROGRESS_SEEK;
			mUiHandler.sendMessage(message);
			break;
		case KeyEvent.KEYCODE_DPAD_RIGHT:
		      if ( player.isPlaying())
	            {
	                player.pause();
	                isSeekState=true;

	            }
			if (!seekBar.isShown()) {
				Debug.d(TAG, "seekBar Shown ");
				setSeekBarLayout(View.VISIBLE);
			}
			seekBar.setPressed(true);
			Message message1 = Message.obtain();
			message1.arg1 = 1;
			message1.what = PROGRESS_SEEK;
			mUiHandler.sendMessage(message1);
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
	/* volume value from 0- 100 */
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

		player.setVolume(volume);
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		Debug.d(TAG, "onKey keycode =  " + keyCode);
		switch (keyCode) {
		case KeyEvent.KEYCODE_HOME:
		case KeyEvent.KEYCODE_BACK: {
			if (!AudioPlayerActivity.this.isFinishing())
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
		System.gc();
		MediaplayerBase.getInstance().setAudioPlayer(null);
		MediaplayerBase.getInstance().setPlayer(null);
		Debug.d(TAG, "onDestroy");
	}

	@Override
	protected void onStop() {
		super.onStop();
		mUiHandler.removeCallbacksAndMessages(null);
		mWorkHandler.sendEmptyMessage(STOP);
		Debug.d(TAG, "onStop");
	}
}
