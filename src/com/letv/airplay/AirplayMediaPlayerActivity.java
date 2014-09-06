package com.letv.airplay;

import com.letv.airplay.JniInterface.MediaPlayerArg;
import com.letv.upnpControl.tools.LetvLog;
import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.WindowManager;

/**
 * MediaPlayerActivity同时管理音频和视频的界面
 * 
 * @author 韦念欣
 * 
 */
public class AirplayMediaPlayerActivity extends Activity {

	public static final String TAG = AirplayMediaPlayerActivity.class.getSimpleName();

	public final static boolean VIDEO = true;
	public final static boolean MUSIC = false;

	private PlayerView playerView;
	private VideoPlayerManager videoPlayerManager;
	private AudioPlayerManager audioPlayerManager;

	private boolean playerType;
	private String url;
	private int percent;
	private String videoId;
	
	
	public Handler mHandler;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); 
		LetvLog.d(TAG, this + " onCreate: " +Thread.currentThread().getId());			
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		mHandler = new Handler();
		JniInterface.getInstance().setMediaActivity(this,this);
		
		playerView = new PlayerView(this);
		setContentView(playerView);
		
		Intent intent = getIntent();
		playerType = intent.getBooleanExtra("playerType", VIDEO);
		url = intent.getStringExtra("mediaUrl");
		percent = intent.getIntExtra("percent", 0);
		videoId = intent.getStringExtra("videoId");
		
		LetvLog.e(TAG, "playerType: " + playerType);
		if (playerType == VIDEO) {
			videoPlayerManager = new VideoPlayerManager(this, playerView, url, percent, videoId);
			videoPlayerManager.onCreate(savedInstanceState);
		} else if (playerType == MUSIC) {
			audioPlayerManager = new AudioPlayerManager(this, playerView);
			audioPlayerManager.onCreate(savedInstanceState);
		}
		
		JniInterface.getInstance().SemaphoreMediaRelease();
	}

	@Override
	protected void onStart() {
		super.onStart();

		LetvLog.e(TAG,  this + " onStart");
		
		if (playerType == VIDEO) {
			videoPlayerManager.onStart();
		} else if (playerType == MUSIC) {
			audioPlayerManager.onStart();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		LetvLog.e(TAG,  this + " onResume");
		
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (playerType == VIDEO) {
					videoPlayerManager.onResume();
				} else if (playerType == MUSIC) {
					audioPlayerManager.onResume();
				}
			}
		}, 3000);
	}

	@Override
	protected void onPause() {
		super.onPause();

		LetvLog.e(TAG, this + " onPause");
		JniInterface.getInstance().setMediaActivity(this,null);
		JniInterface.getInstance().SemaphoreMediaRelease();
		if (playerType == VIDEO) {
			videoPlayerManager.onPause();
		} else if (playerType == MUSIC) {
			audioPlayerManager.onPause();
		}
	}

	@Override
	protected void onStop() {

		LetvLog.e(TAG,  this + " onStop: " + playerType);
		super.onStop();
		JniInterface.getInstance().setMediaActivity(this,null);
		if (playerType == VIDEO) {
			videoPlayerManager.onStop();
		} else if (playerType == MUSIC) {
			audioPlayerManager.onStop();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		LetvLog.e(TAG,  this + " onDestroy");
		
		JniInterface.getInstance().setMediaActivity(this,null);
		if (playerType == VIDEO) {
			videoPlayerManager.onDestroy();
		} else if (playerType == MUSIC) {
			audioPlayerManager.onDestroy();
		}
		
		if(mHandler!=null){
			if(ra != null)
				mHandler.removeCallbacks(ra);
			
			mHandler = null;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (playerType == VIDEO) {
			return videoPlayerManager.onKeyDown(keyCode, event);
		} else if (playerType == MUSIC) {
			return audioPlayerManager.onKeyDown(keyCode, event);
		}
		return super.onKeyDown(keyCode, event);
	}

	public boolean getPlayerType() {
		return playerType;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (playerType == VIDEO) {
			return videoPlayerManager.onKeyUp(keyCode, event);
		}
		return super.onKeyUp(keyCode, event);
	}

	
	public class CM implements Runnable{
		String mediaUrl;
		int percent;
		String videoId;
		
		CM(MediaPlayerArg arg){
			mediaUrl = arg.getMediaUrl();
			percent = arg.getPercent();
			videoId = arg.getVideoId();
		};
		
		@Override
		public void run() {
			LetvLog.e(TAG, "Runnable changeVideo: " + " mediaUrl: " + mediaUrl + " percent: " + percent + "videoId: " + videoId);
			changeVideo(mediaUrl, percent, videoId);
		}
		
		
	}
	public Runnable ra = new Runnable(){
		public void run(){
			changeMusic();
		}
	};
	/**
	 * 从音频界面转换到视频界面
	 */
	public void changeVideo(String url, int per, String id) {
		LetvLog.e(TAG, "changeVideo: " + playerType);
		playerView.removePureBG();
		if (playerType != VIDEO) {
			//JniInterface.getInstance().setAudioPlayer(null);
			if (audioPlayerManager != null) {
				//audioPlayerManager.onPause();
				audioPlayerManager.stopPlay();
			}
			audioPlayerManager = null;
			playerType = VIDEO;

			videoPlayerManager = new VideoPlayerManager(this, playerView, url, per, id);
			videoPlayerManager.onCreate(null);
			videoPlayerManager.onStart();
			videoPlayerManager.onResume();
		}
		
		JniInterface.getInstance().SemaphoreMediaRelease();
	}

	/**
	 * 从视频界面转换到音频界面
	 */
	public void changeMusic() {
		LetvLog.e(TAG, "changeMusic: " + playerType);
		if (playerType != MUSIC) {
			//JniInterface.getInstance().setMediaPlayer(null);
			if (videoPlayerManager != null) {
				videoPlayerManager.stopPlay();
			}

			videoPlayerManager = null;
			playerType = MUSIC;

			audioPlayerManager = new AudioPlayerManager(this, playerView);
			audioPlayerManager.onCreate(null);
			audioPlayerManager.onStart();
			audioPlayerManager.onResume();
		}
		JniInterface.getInstance().SemaphoreMediaRelease();
	}
}
