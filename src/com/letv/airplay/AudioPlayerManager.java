package com.letv.airplay;


import org.cybergarage.util.Debug;

import com.letv.upnpControl.tools.LetvLog;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.SystemProperties;
import android.util.Log;
import android.view.KeyEvent;

/**
 * AirPlay音频播放界面控制管理
 * @author 韦念欣
 *
 */
public class AudioPlayerManager{

	private static final String TAG = AudioPlayerManager.class.getSimpleName();
	
	private Activity activity;
	private PlayerView playerView;
	
	private AudioPlayer audioPlayer;
	private AudioManager audioManager;
	private int mChannel;
	private int mFrequency;
	private int mSampBit;
	//private boolean first3s = true;
	
	private SharedPreferences settings;
	private int DEFAULT_VOLUME = 10;
	private int MAX_VOLUME = 15;
	private int volume = DEFAULT_VOLUME;
	private static final String PREFS_NAME = "volume";
	private static final String SETTING_NAME = "volume_setting";
	
	public AudioPlayerManager(Activity activity, PlayerView playerView){
		this.activity = activity;
		this.playerView = playerView;
	}
	
	public void onCreate(Bundle bundle) {
		LetvLog.e(TAG, "onCreate");
		JniInterface.getInstance().setAudioPlayer(this);
		settings = activity.getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE);
		volume = settings.getInt(SETTING_NAME, DEFAULT_VOLUME);
		audioManager = (AudioManager) activity.getSystemService(Activity.AUDIO_SERVICE);	
		
		setAudioParamForJNI();

		startPlay();
		
		// 播放开始3秒后，才能调节音量
		//first3s = true;
		//new Handler(activity.getMainLooper()).postDelayed(new Runnable() {
		//	@Override
		//	public void run() {
		//		first3s = false;
		//	}
		//}, 3000);
		playerView.switchToPureBG();
	}

	public void onStart(){
		playerView.startDiskAnimatinon();
	}
	
	public void onResume(){
	}
	
	public void onPause(){
		JniInterface.getInstance().AudioHome();
	}
	
	public void onStop() {
		JniInterface.getInstance().setAudioState(null, AirplayService.STOP);
		//playerView.stopDiskAnimation();
		//stopPlay();
	}
	
	public void onDestroy(){
		JniInterface.getInstance().setAudioState(null, AirplayService.STOP);
	}

	/**
	 * 从JNI加载音频参数
	 */
	public void setAudioParamForJNI() {
		this.mChannel = JniInterface.getInstance().gChannel;
		this.mFrequency = JniInterface.getInstance().gFrequency;
		this.mSampBit = JniInterface.getInstance().gSampBit;
	}

	/**
	 * 手动加载音频参数
	 * @param channel
	 * @param frequency
	 * @param sampBit
	 */
	public void setAudioParam(int channel, int frequency, int sampBit) {
		this.mChannel = channel;
		this.mFrequency = frequency;
		this.mSampBit = sampBit;
	}

	/**
	 * 开始播放音频
	 */
	public void startPlay() {
		stopPlay();
		//JniInterface.getInstance().setPlayMusic(true);
		audioPlayer = new AudioPlayer(new AudioPlayer.AudioParam(mFrequency, mChannel, mSampBit));
		audioPlayer.prepare();
		audioPlayer.play();
	}

	/**
	 * 停止播放音频
	 */
	public void stopPlay() {
		LetvLog.e(TAG, "audioPlayer stopPlay");
		playerView.stopDiskAnimation();
		//JniInterface.getInstance().setPlayMusic(false);
		if (audioPlayer != null) {
			LetvLog.e(TAG, "audioPlayer stop");
			audioPlayer.stop();
			audioPlayer = null;
		}
	}

	/**
	 * 接收音频PCM数据
	 * @param audioData
	 * @param offsetInBytes
	 * @param sizeInBytes
	 */
	public void addDataSource(byte[] audioData, int offsetInBytes, int sizeInBytes) {
		if (audioData == null || audioPlayer == null)
			return;

		if (offsetInBytes == 0 && sizeInBytes == audioData.length) {
			audioPlayer.addDataSource(audioData);
		} else {
			byte[] data = new byte[sizeInBytes];
			for (int i = 0; i < sizeInBytes; i++) {
				data[i] = audioData[offsetInBytes + i];
			}
			audioPlayer.addDataSource(data);
		}
	}
	
	/**
	 * 设置音乐图片
	 * @param data
	 * @param offset
	 * @param length
	 */
	public void setMusicImage(byte[] data, int offset, int length){ 
		playerView.setMusicLogo(data);
	}
	
	/**
	 * 设置音乐标题
	 * @param title
	 */
	public void setMusicTitle(String title){
		if (title.startsWith("http")){
			return;
		}else{
			playerView.setMusicTitle(title);
		}
	}
	
	/**
	 * 设置音乐演唱者
	 * @param singer
	 */
	public void setMusicAuthor(String singer){
		if (singer.startsWith("http")){
			return;
		}else{
			playerView.setMusicSinger(singer);
		}
		
	}
	
	/**
	 * 设置音频
	 * @param leftVolume	左声道：最小0.0，最大1.0
	 * @param rightVolume	右声道：最小0.0，最大1.0
	 */
	public void setStereoVolume(float leftVolume, float rightVolume) {
		
		//Log.d(TAG, "setStereoVolume: " +  "audioPlayer: " + audioPlayer + " first3s: " + first3s + " leftVolume: " + leftVolume + " rightVolume: " + rightVolume);
		
		//if (first3s){
		//	return;
		//}
		
		if (audioPlayer != null && audioManager != null){
			
			if(false == isStreamMute()){
				audioPlayer.setStereoVolume(leftVolume, rightVolume);
				volume = Math.round((leftVolume + rightVolume) / 2 * MAX_VOLUME);
				setVolume(volume, true);
			}else{
				Log.d(TAG, "audioPlayer isStreamMute!");
			}
		}
	}

	public boolean isStreamMute() {
//		String version = SystemProperties.get("ro.letv.release.version");
//		Log.d(TAG, "isStreamMute version=" + version);
//		if (version.contains("X60")) {
//			return audioManager.isMasterMute();
//		}
		return audioManager.isMasterMute();
	}
	
	/**
	 * 相应遥控器按键操作
	 */
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			switch (keyCode){
			case KeyEvent.KEYCODE_BACK:				// 返回键
				JniInterface.getInstance().AudioBack();
				//AirplayService.setAudioPlayerState(AirplayService.STOP);	
				//finish();
				return true;
			case KeyEvent.KEYCODE_VOLUME_UP:		// 提高全局音量
			case KeyEvent.KEYCODE_DPAD_UP:
				//JniInterface.getInstance().AudioraiseVolume();
				//setVolume(volume+1, true);
				return true;
			case KeyEvent.KEYCODE_VOLUME_DOWN:		// 降低全局音量
			case KeyEvent.KEYCODE_DPAD_DOWN:
				//JniInterface.getInstance().AudioreduceVolume();
				//setVolume(volume-1, true);
				return true;
			case KeyEvent.KEYCODE_VOLUME_MUTE:		// 静音
				//JniInterface.getInstance().AudiomuteVolume();
				//setVolume(0, true);
				return true;
			case KeyEvent.KEYCODE_HOME:				// HOME键
			case KeyEvent.KEYCODE_ESCAPE:
			case KeyEvent.KEYCODE_POWER:
				return true;
			case KeyEvent.KEYCODE_MENU:				// MENU键
			case KeyEvent.KEYCODE_DPAD_CENTER:		// 方向键-中
				break;
			default:
				break;
			}
		}
		return false;
	}
	
	/**
	 * 设置音量
	 * @param volume
	 * @param showUI
	 */
	private void setVolume(int volume, boolean showUI){
		
		LetvLog.d(TAG, "setVolume:" +  " volume: " + volume + " showUI: " + showUI);
		
		if (volume < 0){
			volume = 0;
		}else if (volume > MAX_VOLUME){
			volume = MAX_VOLUME;
		}
		this.volume = volume;
		
		if (showUI){
			playerView.setSound(volume);
		}
		
		int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
		int current = Math.round((float)volume/MAX_VOLUME*max);
		audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, current, 0);

		Editor editor = settings.edit();
		editor.putInt(SETTING_NAME, volume);
		editor.commit();
	}
	
	/**
	 * 增大音量
	 */
	
	public void raiseVolume(){
		setVolume(volume+1, true);
	}
	
	/**
	 * 减小音量
	 */
	
	public void reduceVolume(){
		setVolume(volume-1, true);
	}
	
	/**
	 * 静音
	 */
	public void muteVolume(){
		setVolume(0, true);
	}
	
	/**
	 * Activity结束
	 */
	public void finish(){
		stopPlay();	
		//JniInterface.getInstance().setMediaPlayer(null);
		//JniInterface.getInstance().setAudioPlayer(null);
		//JniInterface.getInstance().setMediaActivity(null);
		if(!activity.isFinishing())
		activity.finish();
	}
}
