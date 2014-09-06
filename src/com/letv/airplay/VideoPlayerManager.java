package com.letv.airplay;

import java.io.IOException;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;

import com.letv.upnpControl.tools.LetvLog;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.Toast;
import android.os.HandlerThread;

/**
 * AirPlay视频播放界面控制管理
 * 
 * @author 韦念欣
 * @modify Jamin
 */
public class VideoPlayerManager implements OnCompletionListener,
		OnErrorListener, OnInfoListener, OnPreparedListener,
		OnSeekCompleteListener, OnBufferingUpdateListener, OnVideoSizeChangedListener {
	private String TAG = VideoPlayerManager.class.getSimpleName();

	private Activity activity;
	private PlayerView playerView;
	private String mediaUrl;
	private int percent;
	private String videoId;
	
	//store mediaplayer stop status
	private int stopType;

	private Object lock = new Object(); // 同步锁

	public static final int PLAY = 0;
	public static final int PAUSE = 1;
	public static final int CACHE = 2;
	public static final int STOP = 3;
	public static final int SEEK = 10;
	public static final int PLAY_URL = 4;
	public static final int PROGRESS_SEEK_RC = 5;
	public static final int PLAYORPAUSE = 6;
	public static final int PROGRESS_SEEK_MD = 7;
	public static final int PROGRESS_POS = 8;
	public static final int PROGRESS_POS_UI = 9;

	private MediaPlayer mediaPlayer;
	private SharedPreferences settings;
	private AudioManager audioManager;
	private SurfaceHolder holder;
	private Timer timer;
	private TimerTask task;

	private Timer pTimer;
	private TimerTask pTimerTask;

	private boolean isPlaying;
	private boolean isVideo;
	private boolean prepared; // 用于MediaPlay是否准备完毕
	private boolean first3s = true; // 用于防止前三秒客户端概率性自动发送暂停的问题
	private int mTotalTime = 0;
	private int mCurrentPos = 0;
	private int DEFAULT_VOLUME = 10;
	private int MAX_VOLUME = 15;
	private int volume = DEFAULT_VOLUME;
	private boolean mSeekComplete = true;
	private boolean mBuffing = false;
	private boolean mStoping = false;
	private boolean mVideoLagging = false;
	private HandlerThread mHandlerThread;
	private static final String PREFS_NAME = "volume";
	private static final String SETTING_NAME = "volume_setting";
	

	public VideoPlayerManager(Activity activity, PlayerView playerView,
			String url, int per, String id) {
		this.activity = activity;
		this.playerView = playerView;
		this.mediaUrl = url;
		this.percent = per;
		this.videoId = id;
		
		this.stopType = AirplayService.STOP_NORNAL;
	}
	
	public String getVideoId(){
		return videoId;
	}

	public void onCreate(Bundle savedInstanceState) {
		// LetvLog.e(TAG, "onCreate currentThread:" + Thread.currentThread().getId()
		// );
		JniInterface.getInstance().setMediaPlayer(this);
		settings = activity.getSharedPreferences(PREFS_NAME,
				Activity.MODE_PRIVATE);
		volume = settings.getInt(SETTING_NAME, DEFAULT_VOLUME);
		audioManager = (AudioManager) activity
				.getSystemService(Activity.AUDIO_SERVICE);
		setVolume(volume, false);
		isPlaying = false;
		first3s = true;
		mSeekComplete = true;
		mVideoLagging = false;
		mStoping = false;

		playerView.setOnSeekBarChangeListener(new PlayerView.OnSeekBarChangeListener() {

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {

					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {

					}

					@Override
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						if (fromUser) {
							// 注意：seekBar内部最大值为100
							if (mTotalTime != 0) {
								progress = Math.round(progress / 100f
										* mTotalTime);
								seekMediaPlayerTS(progress);
							}
						}
					}
				});

		if (mediaUrl == null) {
			LetvLog.e(TAG, "mediaPlayer URL is null");
			playerView.switchToPlayingError();
			return;
		}

		synchronized (lock) {
			// 设置MediaPlayer
			try {

				mediaPlayer = new MediaPlayer();

				synchronized (lock) {
					if (mediaPlayer != null) {
						mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
						mediaPlayer.setDataSource(mediaUrl);
						mediaPlayer.setOnCompletionListener(this);
						mediaPlayer.setOnErrorListener(this);
						mediaPlayer.setOnBufferingUpdateListener(this);
						mediaPlayer.setOnInfoListener(this);
						mediaPlayer.setOnPreparedListener(this);
						mediaPlayer.setOnSeekCompleteListener(this);
						mediaPlayer.setOnVideoSizeChangedListener(this);
					}
					prepared = false;
				}
			} catch (IOException e) {
				e.printStackTrace();
				LetvLog.e(TAG, "MEDIA_INIT_ERROR");
				playerView.switchToPlayingError();
				JniInterface.getInstance().VideoFinish();
				return;
			} catch (NullPointerException ne) {
				ne.printStackTrace();
				LetvLog.e(TAG, "NullPointerException");
				playerView.switchToPlayingError();
				JniInterface.getInstance().VideoFinish();
				return;

			}
		}
		// 设置Holder
		holder = playerView.getSurfaceView().getHolder();
		holder.setFormat(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		holder.addCallback(new Callback() {
			public void surfaceCreated(SurfaceHolder holder) {
				LetvLog.d(TAG, "surfaceCreated");
				synchronized (lock) {
					if (mediaPlayer != null) {
						mediaPlayer.setDisplay(holder);
						try {
							mediaPlayer.prepareAsync();
						} catch (IllegalStateException e) {
							LetvLog.d(TAG, "IllegalStateException" + e);
							e.printStackTrace();
							JniInterface.getInstance().VideoFinish();
						} catch (Exception e) {
							LetvLog.d(TAG, "Exception" + e);
							JniInterface.getInstance().VideoFinish();
						}
					}
				}
			}

			public void surfaceChanged(SurfaceHolder holder, int format,
					int width, int height) {

			}

			public void surfaceDestroyed(SurfaceHolder holder) {
				LetvLog.d(TAG, "surfaceDestroyed");
				// stopPlay();
				JniInterface.getInstance().VideoFinish();
			}
		});

		// 判断客户端开启是否来推送音乐界面，切换相应界面
		if (isMusic(mediaUrl)) {
			playerView.switchToPlayingVideoMusic();
			mediaPlayer.prepareAsync();
		} else {
			playerView.switchToPlayingVideo();
			playerView.setCenterProgressBarShow(true);
		}
		pTimer = new Timer();
		pTimerTask = new TimerTask() {
			@Override
			public void run() {
				JniInterface.getInstance().VideoPro();
			}
		};
		pTimer.schedule(pTimerTask, 0, 1000);
	}

	public void onStart() {

	}

	public void onResume() {

	}

	public void onPause() {
		LetvLog.d(TAG, "onPause");
		JniInterface.getInstance().VideoHome();
	}

	public void onStop() {
		LetvLog.d(TAG, "onStop");
		//AirplayService.setMediaPlayerState(this.videoId, AirplayService.STOP);
		JniInterface.getInstance().setVideoState(this.videoId, AirplayService.STOP, stopType);
	}

	public void onDestroy() {
		LetvLog.d(TAG, "onDestroy");
		//AirplayService.setMediaPlayerState(this.videoId, AirplayService.STOP);
		JniInterface.getInstance().setVideoState(this.videoId, AirplayService.STOP, stopType);
	}



	/**
	 * 相应遥控器按键操作
	 */
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		// LetvLog.d(TAG, "onKeyDown currentThread: "
		// +Thread.currentThread().getId());
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			switch (keyCode) {
			case KeyEvent.KEYCODE_BACK: // 返回键
				JniInterface.getInstance().VideoBack();
				return true;
			case KeyEvent.KEYCODE_VOLUME_UP: // 提高音量
			case KeyEvent.KEYCODE_DPAD_UP:
				//JniInterface.getInstance().VideoraiseVolume();
				return false;
			case KeyEvent.KEYCODE_VOLUME_DOWN: // 降低音量
			case KeyEvent.KEYCODE_DPAD_DOWN:
				//JniInterface.getInstance().VideoreduceVolume();
				return false;
			case KeyEvent.KEYCODE_VOLUME_MUTE: // 静音
				//JniInterface.getInstance().VideomuteVolume();
				return true;
			case KeyEvent.KEYCODE_DPAD_CENTER: // 按中键
				JniInterface.getInstance().VideoPlayOrPause();
				return true;
			case KeyEvent.KEYCODE_DPAD_LEFT: // 按左键
				JniInterface.getInstance().VideoSeekRC(0);
				return true;
			case KeyEvent.KEYCODE_DPAD_RIGHT: // 按右键
				JniInterface.getInstance().VideoSeekRC(1);
				return true;
			case KeyEvent.KEYCODE_HOME: // HOME键
			case KeyEvent.KEYCODE_ESCAPE:
			case KeyEvent.KEYCODE_POWER:
				// stopPlay();
				Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.addCategory(Intent.CATEGORY_HOME);
				activity.startActivity(intent);
				JniInterface.getInstance().VideoFinish();
				return true;
			case KeyEvent.KEYCODE_MENU: // MENU键
				break;
			default:
				break;
			}
		}
		return false;
	}

	/**
	 * 相应遥控器按键操作
	 */
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_UP) {
			switch (keyCode) {
			case KeyEvent.KEYCODE_DPAD_LEFT: // 按左键
			case KeyEvent.KEYCODE_DPAD_RIGHT: // 按右键
				if (isPlaying) {
					playerView.setProgressBarImage(PlayerView.PROGRESS_TYPE_PLAY);
				} else {
					playerView.setProgressBarImage(PlayerView.PROGRESS_TYPE_PAUSE);
				}
				return true;
			default:
				break;
			}
		}
		return false;
	}

	/**
	 * 设置音量
	 * 
	 * @param volume
	 * @param showUI
	 */
	private void setVolume(int volume, boolean showUI) {
		if (first3s)
			return;
		if (volume < 0) {
			volume = 0;
		} else if (volume > MAX_VOLUME) {
			volume = MAX_VOLUME;
		}
		this.volume = volume;
		if (showUI) {
			playerView.setSound(volume);
		}

		int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
		int current = Math.round((float) volume / MAX_VOLUME * max);
		audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, current, 0);
		Editor editor = settings.edit();
		editor.putInt(SETTING_NAME, volume);
		editor.commit();
	}

	public void raiseVolume() {
		setVolume(volume + 1, true);
	}

	/**
	 * 减小音量
	 */

	public void reduceVolume() {
		setVolume(volume - 1, true);
	}

	/**
	 * 静音
	 */
	public void muteVolume() {
		setVolume(0, true);
	}

	public void onSeekComplete(MediaPlayer arg0) {
		Log.d(TAG, "onSeekComplete");
		synchronized (lock) {
			mSeekComplete = true;
			Log.d(TAG, "onSeekComplete:" + mSeekComplete);
		}
		playerView.setCenterProgressBarShow(false);
	}

	/**
	 * 资源准备完毕
	 */
	@Override
	public void onPrepared(MediaPlayer mediaPlayer) {
		
		playerView.setTotalTime(mediaPlayer.getDuration());

		synchronized (lock) {
			try {
				mTotalTime = mediaPlayer.getDuration();
				JniInterface.getInstance().setPlayDuration(mTotalTime);
				int position = Math.round((percent / 100f * mTotalTime));
				mediaPlayer.seekTo(position);
				mediaPlayer.start();
				JniInterface.getInstance().setVideoState(this.videoId, AirplayService.PLAY, 0);
	
				// 播放开始3秒后，才能执行暂停操作（启动时客户端概率性发送暂停指令）
				first3s = true;
				new Handler(activity.getMainLooper()).postDelayed(new Runnable() {
					@Override
					public void run() {
						first3s = false;
					}
				}, 3000);
				
				isPlaying = true;
				prepared = true;
				playerView.setCenterProgressBarShow(false);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}



	}

	/**
	 * 开始播放
	 */
	public void play() {
		LetvLog.d(TAG, "play" + " mSeekComplete: " + mSeekComplete + " mBuffing:" + mBuffing + " mStoping:" + mStoping);
		
		synchronized (lock) {
			if ((mediaPlayer != null) && prepared) {
				
				try {
					if(mediaPlayer.isPlaying()){
						return;
					}
				}catch(Exception e){
					e.printStackTrace();
					return;
				}
				
				
				
				try{
					mediaPlayer.start();
					isPlaying = true;
					JniInterface.getInstance().setVideoState(this.videoId, AirplayService.PLAY, 0);
					playerView.setProgressBarImage(PlayerView.PROGRESS_TYPE_PLAY);
					if (isVideo) {
						playerView.setCenterButtonShow(false);
						playerView.ShowControlSeekBar8s();
					} else {
						playerView.startDiskAnimatinon();
					}
				}catch(Exception e){
					e.printStackTrace();
					return;
				}
			}
		}
	}

	/**
	 * 暂停播放
	 */
	public void pausePlay() {
		
		LetvLog.d(TAG, "pausePlay" + " mSeekComplete: " + mSeekComplete + " mBuffing:" + mBuffing + " mStoping:" + mStoping);
		
		synchronized (lock) {
			if ((mediaPlayer != null)&& prepared) {
				
				boolean status = true;
				try{
					status = mediaPlayer.isPlaying();
				}catch(Exception e){
					e.printStackTrace();
					return;
				}
				
				if(status){
					try{
						LetvLog.d(TAG, "pause " + "isVideo: " + isVideo);
						mediaPlayer.pause();
						isPlaying = false;
						JniInterface.getInstance().setVideoState(this.videoId, AirplayService.PAUSE, 0);
						playerView.setProgressBarImage(PlayerView.PROGRESS_TYPE_PAUSE);
						if (isVideo) {
							playerView.setCenterButtonShow(true);
							playerView.setControlSeekBarVisibility(View.VISIBLE);
						} else {
							playerView.stopDiskAnimation();
						}
					}catch(Exception e){
						e.printStackTrace();
						return;
					}
					
				}else{
					try{
						LetvLog.d(TAG, "play " + "prepared: " + prepared);
						mediaPlayer.start();
						isPlaying = true;
						JniInterface.getInstance().setVideoState(this.videoId, AirplayService.PLAY, 0);
						playerView.setProgressBarImage(PlayerView.PROGRESS_TYPE_PLAY);
						if (isVideo) {
							playerView.setCenterButtonShow(false);
							playerView.ShowControlSeekBar8s();
						} else {
							playerView.startDiskAnimatinon();
						}
					}catch(Exception e){
						e.printStackTrace();
						return;
					}
				}
			} else {
				LetvLog.d(TAG, "pausePlay miss!");
			}
		}
	}

	/**
	 * 停止播放
	 */
	public void stopPlay() {
		LetvLog.d(TAG, "stopPlay");
		stopUpdateProgress();

		synchronized (lock) {
			if (mStoping == false) {
				try {
					if (mediaPlayer != null) {
						LetvLog.d(TAG, "mediaPlayer stop");
						mediaPlayer.stop();
						mediaPlayer.release();
						mediaPlayer = null;
						mStoping = true;
					}
				} catch (Exception e) {
					LetvLog.d(TAG, "Exception");
					mediaPlayer = null;
				}
			}
		}
	}

	public void finish() {
		LetvLog.d(TAG, "finish");
		synchronized (lock) {
			if (mStoping == false) {
				try {
					LetvLog.d(TAG, "mediaPlayer = " + mediaPlayer);
					if (mediaPlayer != null) {
						LetvLog.d(TAG, "mediaPlayer stop");
						mediaPlayer.stop();
						mediaPlayer.release();
						mediaPlayer = null;
						mStoping = true;
						if (pTimer != null) {
							pTimer.cancel();
							pTimer = null;
						}
						if (pTimerTask != null) {
							pTimerTask.cancel();
							pTimerTask = null;
						}
					}
				} catch (Exception e) {
					LetvLog.d(TAG, "Exception");
					mediaPlayer = null;
				}
			}
		}
		playerView.handler.removeCallbacksAndMessages(null);
		if (!activity.isFinishing())
			activity.finish();
	}
	/**
	 * 向前快进
	 */
	public void leapForward() {
		if (prepared && mediaPlayer != null) {

			// int total = mediaPlayer.getDuration();
			int step = mTotalTime / 50;
			LetvLog.d(TAG, "mediaPlayer total = " + mTotalTime);
			LetvLog.d(TAG, "mediaPlayer step = " + step);
			int current = mCurrentPos + step;
			LetvLog.d(TAG, "mediaPlayer current = " + current);
			if (current >= mTotalTime) {
				current = mTotalTime - 1;
			}
			// seekMediaPlayer(current);
			seekMediaPlayerUpdateUI(current);
		}
	}

	/**
	 * 向后快退
	 */
	public void bounceBack() {
		if (prepared && mediaPlayer != null) {
			// int total = mediaPlayer.getDuration();
			int step = mTotalTime / 50;
			int current = mCurrentPos - step;
			if (current < 0) {
				current = 0;
			}
			// seekMediaPlayer(current);
			seekMediaPlayerUpdateUI(current);
		}
	}

	/**
	 * 显示位置
	 */
	public void seekMediaPlayerUpdateUI(int pos) {

		LetvLog.d(TAG, "seekMediaPlayerUpdateUI : " + "pos : " + pos
				+ " prepared : " + prepared + " mediaPlayer : " + mediaPlayer);

		if (prepared && mediaPlayer != null) {
			// mSeekComplete = false;
			//playerView.setCurrentTime(pos);
			// mediaPlayer.seekTo(pos);
			playerView.ShowControlSeekBar8s();
		}
	}

	/**
	 * 播放完毕
	 */
	@Override
	public void onCompletion(MediaPlayer mediaPlayer) {
		LetvLog.d(TAG, "onCompletion");
		stopType = AirplayService.STOP_COMPLETE;
		JniInterface.getInstance().VideoFinish();
	}

	/**
	 * 接收MediaPlayer信息
	 */
	@Override
	public boolean onInfo(MediaPlayer mediaPlayer, int what, int extra) {
		LetvLog.d(TAG, "onInfo " + what + "");
		// LetvLog.d(TAG, "onInfo currentThread: " +Thread.currentThread().getId());
		synchronized (lock) {
			if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
				mBuffing = true;
				playerView.setCenterProgressBarShow(true);
			} else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
				playerView.setCenterProgressBarShow(false);
				mBuffing = false;
			} else if (what == MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING) {
				mVideoLagging = true;
			}
		}
		return false;
	}

	/**
	 * 播放发生错误
	 */
	@Override
	public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {

		// LetvLog.d(TAG, "onError currentThread: "
		// +Thread.currentThread().getId());
		
		String error =  this.activity.getString(com.letv.smartControl.R.string.play_error) + what;
		
		if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
			LetvLog.e(TAG, "MEDIA_ERROR_SERVER_DIED");
			onCreate(null);
			return false;
		} else if (what == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
			LetvLog.e(TAG, "MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK");
		} else if (what == MediaPlayer.MEDIA_ERROR_UNKNOWN) {
			LetvLog.e(TAG, "MEDIA_ERROR_UNKNOWN");
		} else {
			LetvLog.e(TAG, "MEDIA_ERROR");
		}
		
		Toast.makeText(this.activity, error, 500).show();
		
		JniInterface.getInstance().VideoOnError();
		return false;
	}

	/**
	 * 缓冲区进度显示
	 */
	@Override
	public void onBufferingUpdate(MediaPlayer mediaPlayer, int progress) {

		// LetvLog.e(TAG, "onBufferingUpdate currentThread:" +
		// Thread.currentThread().getId() );
		playerView.setControlBuffereProgress(progress);
	}

	/**
	 * 获取视频总时间长度
	 * 
	 * @return
	 */
	public int getMediaPlayerDuration() {


		synchronized (lock) {
			if (mediaPlayer != null) {
				if (mTotalTime != 0)
					return mTotalTime;
				
				if (!prepared)
					return 0;
				
				if (mSeekComplete == false || mBuffing == true
						|| mStoping == true)
					return 0;
				
				try{
					return mediaPlayer.getDuration();
				}catch(Exception e){
					e.printStackTrace();
					return 0;
				}
				
			} else {
				return 0;
			}
		}
	}

	/**
	 * 获取当前播放进度
	 * 
	 * @return
	 */
	public int getMediaPlayerPosition() {

		synchronized (lock) {
			if (mediaPlayer != null) {
				if (mCurrentPos != 0)
					return mCurrentPos;
				
				if (!prepared)
					return 0;
				
				if (mSeekComplete == false || mBuffing == true
						|| mStoping == true)
					return 0;
				try{
					return mediaPlayer.getCurrentPosition();
				}catch(Exception e){
					e.printStackTrace();
					return 0;
				}
			} else {
				return 0;
			}
		}
	}

	/**
	 * Seek操作
	 * 
	 * @param pos
	 *            单位豪秒
	 * @return
	 */
	public void seekMediaPlayerMD(int pos) {

		LetvLog.d(TAG, "seekMediaPlayerMD" + " mSeekComplete: " + mSeekComplete + " mBuffing:" + mBuffing + " mStoping:" + mStoping);
		
		synchronized (lock) {
			if (prepared && mediaPlayer != null) {
				LetvLog.d(TAG, "seekMediaPlayerMD" + " pos: " + pos);
				
				if (mSeekComplete == false || mBuffing == true
						|| mStoping == true)
					return;
				
				try {
					mediaPlayer.seekTo(pos);
				} catch (Exception e) {
					e.printStackTrace();
					mSeekComplete = false;
				}
				mSeekComplete = false;
			}
		}
	}

	public void seekMediaPlayerTS(int pos) {

		LetvLog.d(TAG, "seekMediaPlayerTS" + " mSeekComplete: " + mSeekComplete + " mBuffing:" + mBuffing + " mStoping:" + mStoping);
		
		synchronized (lock) {
			if (prepared && mediaPlayer != null) {
				LetvLog.d(TAG, "seekMediaPlayerTS" + " pos: " + pos);

				if (mSeekComplete == false || mBuffing == true
						|| mStoping == true)
					return;
				
				try {
					mediaPlayer.seekTo(pos);
				} catch (IllegalStateException e) {
					e.printStackTrace();
					mSeekComplete = false;
				}
				mSeekComplete = false;
			}
		}
	}

	public void seekMediaPlayerRC(int action) {

		LetvLog.d(TAG, "seekMediaPlayerRC" + " action: " + action + " prepared: "
				+ prepared);
		LetvLog.d(TAG, "seekMediaPlayerRC" + " mSeekComplete: " + mSeekComplete + " mBuffing:" + mBuffing + " mStoping:" + mStoping);
		synchronized (lock) {
			if (prepared && mediaPlayer != null) {
				
				if (mSeekComplete == false || mBuffing == true
						|| mStoping == true)
					return;
				
				if (action == 1) {
					leapForward();
					playerView.setProgressBarImage(PlayerView.PROGRESS_TYPE_SPEED);
				} else {
					bounceBack();
					playerView.setProgressBarImage(PlayerView.PROGRESS_TYPE_REVERSE);
				}
				
				try{
					mTotalTime = mediaPlayer.getDuration();
				}catch(Exception e){
					e.printStackTrace();
				}
				
				int step = mTotalTime / 50;
				if (step < 7000) {
					step = 7000;
				}
				int current;
				if (action == 0) {
					current = mCurrentPos - step;
					if (current < 0) {
						current = 0;
					}
				} else {
					//JniInterface.getInstance().setVideoState(this.videoId, AirplayService.PAUSE, 0);
					current = mCurrentPos + step;
					if (current >= mTotalTime) {
						current = mTotalTime - 1;
					}
				}
				
				try {
					LetvLog.d(TAG, "seekTo: " + current);
					mediaPlayer.seekTo(current);
				} catch (Exception e) {
					e.printStackTrace();
					mSeekComplete = false;
				}
				mSeekComplete = false;
				
			}
		}
	}

	public void VideoPlayOrPause() {

		LetvLog.d(TAG, "VideoPlayOrPause" + " mSeekComplete: " + mSeekComplete + " mBuffing:" + mBuffing + " mStoping:" + mStoping);
		
		synchronized (lock) {
			if (prepared && mediaPlayer != null) {
				
				if(true == mStoping)
					return;

				boolean status = true;
				try{
					status = mediaPlayer.isPlaying();
				}catch(Exception e){
					e.printStackTrace();
					return;
				}
				
				if(status){
					LetvLog.d(TAG, "pause begin");
					try{
						mediaPlayer.pause();
					}catch(Exception e){
						LetvLog.d(TAG, "pause end");
						return;
					}
					LetvLog.d(TAG, "pause end");
					isPlaying = false;
					JniInterface.getInstance().setVideoState(this.videoId, AirplayService.PAUSE, 0);
					playerView.setProgressBarImage(PlayerView.PROGRESS_TYPE_PAUSE);
					if (isVideo) {
						playerView.setCenterButtonShow(true);
						playerView.setControlSeekBarVisibility(View.VISIBLE);
					} else {
						playerView.stopDiskAnimation();
					}
				}else{
					LetvLog.d(TAG, "play begin");
					try{
						mediaPlayer.start();
					}catch(Exception e){
						LetvLog.d(TAG, "play end");
						return;
					}
					LetvLog.d(TAG, "play end");
					isPlaying = true;
					JniInterface.getInstance().setVideoState(this.videoId, AirplayService.PLAY, 0);
					playerView.setProgressBarImage(PlayerView.PROGRESS_TYPE_PLAY);
					if (isVideo) {
						playerView.setCenterButtonShow(false);
						playerView.ShowControlSeekBar8s();
					} else {
						playerView.startDiskAnimatinon();
					}
					
				}
			}
		}
	}

	public void progress() {
		synchronized (lock) {
			if (prepared && mediaPlayer != null) {
				
				if (mSeekComplete == false || mBuffing == true
						|| mStoping == true)
					return;
				
				try {
					mCurrentPos = mediaPlayer.getCurrentPosition();
					LetvLog.d(TAG, "mCurrentPos: " + mCurrentPos);
				} catch (IllegalStateException e) {
					e.printStackTrace();
					return;
				}
				if (mTotalTime <= 0)
					return;
				if (mCurrentPos > mTotalTime) {
					mCurrentPos = mTotalTime;
				}
				
				JniInterface.getInstance().VideoProView(mCurrentPos);
				JniInterface.getInstance().setPlayPosition(mCurrentPos);
				// seekMediaPlayerUpdateUI(mCurrentPos);
			}
		}

	}

	public void progressView(int pos) {
		playerView.setCurrentTime(pos);
	}

	/**
	 * 停止更新播放进度条
	 */
	private void stopUpdateProgress() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		if (task != null) {
			task.cancel();
			task = null;
		}

		if (pTimer != null) {
			pTimer.cancel();
			pTimer = null;
		}
		if (pTimerTask != null) {
			pTimerTask.cancel();
			pTimerTask = null;
		}
	}

	/**
	 * 保持视频比例播放
	 * 
	 * @param videoWidth
	 * @param videoHeight
	 * @return
	 */
	private boolean changeDisplayScale(int videoWidth, int videoHeight) {
		if (videoHeight > 0 && videoWidth > 0) {

			DisplayMetrics dm = new DisplayMetrics();
			WindowManager windowManager = (WindowManager) activity
					.getSystemService(activity.WINDOW_SERVICE);
			windowManager.getDefaultDisplay().getMetrics(dm);
			int diaplayWidth = dm.widthPixels;
			int diaplayHeight = dm.heightPixels;

			// WindowManager wm =
			// (WindowManager)activity.getSystemService(Activity.WINDOW_SERVICE);
			// Display display = wm.getDefaultDisplay();
			// int diaplayWidth = display.getRawWidth();
			// int diaplayHeight = display.getRawHeight();

			SurfaceView surfaceView = playerView.getSurfaceView();
			LayoutParams params = surfaceView.getLayoutParams();
			float scale = (float) videoWidth / videoHeight;
			int tmpWidth = Math.round(scale * diaplayHeight);
			int tmpHeight = Math.round(diaplayWidth / scale);

			if (tmpWidth > diaplayWidth) {
				diaplayHeight = tmpHeight;
			}
			if (tmpHeight > diaplayHeight) {
				diaplayWidth = tmpWidth;
			}
			params.width = diaplayWidth;
			params.height = diaplayHeight;

			surfaceView.setLayoutParams(params);
			return true;
		}
		return false;
	}

	@Override
	public void onVideoSizeChanged(MediaPlayer arg0, int arg1, int arg2) {

		// TODO Auto-generated method stub
		// 保持视频原先的比例播放
		isVideo = changeDisplayScale(arg1,arg2);
		// 再次判断客户端开启视频界面来播放音频的情况
		if (isVideo) {
			playerView.ShowControlSeekBar8s();
		} else {
			isVideo = !isMusic(mediaUrl);
			if (!isVideo) {
				playerView.setControlSeekBarVisibility(View.VISIBLE);
			} else {
				playerView.ShowControlSeekBar8s();
			}
		}
	}
	
	/**
	 * 检查URL是否为音乐
	 * 
	 * @param url
	 * @return
	 */
	private boolean isMusic(String url) {
		if (TextUtils.isEmpty(url))
			return false;

		int index = url.trim().lastIndexOf('.');
		if (index == -1)
			return false;

		String extension = url.substring(index).trim();
		return supportMusicFormat.contains(extension.toLowerCase());
	}

	/**
	 * 音乐文件后缀名
	 */
	private HashSet<String> supportMusicFormat;
	{
		supportMusicFormat = new HashSet<String>();
		supportMusicFormat.add(".mp3");
		supportMusicFormat.add(".wav");
		supportMusicFormat.add(".ogg");
		supportMusicFormat.add(".flac");
		supportMusicFormat.add(".aac");
		supportMusicFormat.add(".wma");
		supportMusicFormat.add(".mid");
		supportMusicFormat.add(".xmf");
		supportMusicFormat.add(".mxmf");
		supportMusicFormat.add(".rtttl");
		supportMusicFormat.add(".rtx");
		supportMusicFormat.add(".ota");
		supportMusicFormat.add(".imy");
		supportMusicFormat.add(".ac3");
		supportMusicFormat.add(".ape");
		supportMusicFormat.add(".m4a");
		supportMusicFormat.add(".amr");
		supportMusicFormat.add(".pcm");
		supportMusicFormat.add(".midi");
		supportMusicFormat.add(".mka");
		supportMusicFormat.add(".mpc");
		supportMusicFormat.add(".mp+");
		supportMusicFormat.add(".cda");
		supportMusicFormat.add(".voc");
		supportMusicFormat.add(".aif");
		supportMusicFormat.add(".svx");
		supportMusicFormat.add(".snd");
		supportMusicFormat.add(".vqf");
		supportMusicFormat.add(".mmf");
		supportMusicFormat.add(".m4r");
		supportMusicFormat.add(".wv");
	}


}
