package com.letv.airplay;

import java.util.concurrent.ConcurrentLinkedQueue;

import com.letv.upnpControl.tools.LetvLog;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Message;

/**
 * AudioPlayer PCM流播放器
 * @author 韦念欣
 * @time	2013-03-07
 */
public class AudioPlayer {

	private final static String TAG = AudioPlayer.class.getSimpleName();

	private Handler mHandler;
	private AudioTrack mAudioTrack; 			// AudioTrack对象
	private AudioParam mAudioParam; 			// 音频参数
	private PlayAudioThread mPlayAudioThread;	// 播放线程
	private ConcurrentLinkedQueue<byte[]> mAudioData;	// 音频数据（线程安全队列）
	
	private boolean mIsReady;					// 播放源是否就绪
	private boolean mThreadrRunningFlag; 		// 线程运行标志
	private int mMinBufSize;					// 缓冲区大小
	private int mPlayIndex;					// 当前播放位置

	private PlayState mPlayState = PlayState.MPS_UNINIT; // 当前播放状态(默认未就绪)

	public static final int MESSAGE_ID = 8888;// Handler消息ID
	
	private OnStartPlayMusicListener listener;
	
	public interface OnStartPlayMusicListener{
		public void onStartPlayMusic();
	}
	
	public void setOnStartPlayMusic(OnStartPlayMusicListener listener){
		this.listener = listener;
	}
	
	public AudioPlayer(Handler handler) {
		mHandler = handler;
		mAudioData = new ConcurrentLinkedQueue<byte[]>();
	}

	public AudioPlayer(AudioParam audioParam){
		mAudioData = new ConcurrentLinkedQueue<byte[]>();
		setAudioParam(audioParam);
	}
	
	public AudioPlayer(Handler handler, AudioParam audioParam) {
		this(handler);
		setAudioParam(audioParam);
	}

	/**
	 * 设置音频参数
	 */
	public void setAudioParam(AudioParam audioParam) {
		mAudioParam = audioParam;
	}

	/**
	 * 添加音频源
	 */
	public void addDataSource(byte[] audioData) {
		if (audioData != null){
			mAudioData.offer(audioData);
		}
	}

	/**
	 * 播放前的准备
	 */
	public boolean prepare() {
		if (mAudioParam == null) {
			return false;
		}

		if (mIsReady == true) {
			return true;
		}

		// 创建AudioTrack
		try {
			createAudioTrack();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		setPlayState(PlayState.MPS_PREPARE);
		mIsReady = true;

		return true;
	}

	/**
	 * 播放
	 */
	public boolean play() {
		if (mIsReady == false) {
			return false;
		}

		switch (mPlayState) {
			case MPS_PREPARE:
				mPlayIndex = 0;
				setPlayState(PlayState.MPS_PLAYING);
				startThread();
				break;
			case MPS_PAUSE:
				setPlayState(PlayState.MPS_PLAYING);
				startThread();
				break;
			default:
				break;
		}
		return true;
	}

	/**
	 * 暂停
	 */
	public boolean pause() {

		if (mIsReady == false) {
			return false;
		}

		if (mPlayState == PlayState.MPS_PLAYING) {
			setPlayState(PlayState.MPS_PAUSE);
			stopThread();
		}

		return true;
	}

	/**
	 * 停止
	 */
	public boolean stop() {

		if (mIsReady == false) {
			return false;
		}

		setPlayState(PlayState.MPS_PREPARE);
		stopThread();

		return true;
	}
	
	/**
	 * 设置音量
	 * @param leftVolume
	 * @param rightVolume
	 */
	public void setStereoVolume(float leftVolume, float rightVolume){
		if (mAudioTrack != null){
			leftVolume = leftVolume<0.0f? 0.0f:leftVolume;
			leftVolume = leftVolume>1.0f? 1.0f:leftVolume;
			rightVolume = rightVolume<0.0f? 0.0f:rightVolume; 
			rightVolume = rightVolume>1.0f? 1.0f:rightVolume; 
			mAudioTrack.setStereoVolume(leftVolume, rightVolume);
		}
	}
	
	
	/**
	 * 释放播放源
	 */
	private boolean release() {
		//stop();
		LetvLog.d(TAG, "release"); 
		releaseAudioTrack();

		mIsReady = false;

		setPlayState(PlayState.MPS_UNINIT);

		return true;
	}

	/**
	 * 设置播放状态
	 * @param state
	 */
	private void setPlayState(PlayState state) {
		mPlayState = state;
		if (mHandler != null) {
			Message msg = new Message();
			msg.what = MESSAGE_ID;
			msg.obj = mPlayState;
			mHandler.sendMessage(msg);
		}
	}

	/**
	 * 创建AudioTrack
	 * @throws Exception
	 */
	private void createAudioTrack() throws Exception {

		if (mAudioParam == null){
			throw new IllegalArgumentException("没有设置音频参数");
		}
		// 获得构建对象的最小缓冲区大小
		mMinBufSize = AudioTrack.getMinBufferSize(mAudioParam.mFrequency, mAudioParam.mChannel, mAudioParam.mSampBit);

		LetvLog.d(TAG, "mMinBufSize:" + mMinBufSize);
		mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
				mAudioParam.mFrequency, mAudioParam.mChannel,
				mAudioParam.mSampBit, mMinBufSize, AudioTrack.MODE_STREAM);
	}

	/**
	 * 释放资源
	 */
	private void releaseAudioTrack() {
		if (mAudioTrack != null) {
			mAudioTrack.stop();
			mAudioTrack.release();
			mAudioTrack = null;
		}
	}

	/**
	 * 开启播放的音频
	 */
	private void startThread() {
		if (mPlayAudioThread == null) {
			mThreadrRunningFlag = true;
			mPlayAudioThread = new PlayAudioThread();
			mPlayAudioThread.start();
		}
	}

	/**
	 * 停止线程
	 */
	private void stopThread() {
		if (mPlayAudioThread != null) {
			mThreadrRunningFlag = false;
			mPlayAudioThread = null;
		}
	}

	/**
	 * 播放音频的线程
	 */
	class PlayAudioThread extends Thread {

		@Override
		public void run() {
			
			// 缓冲数据
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}

			if (!mThreadrRunningFlag && mAudioTrack != null) {
				mAudioTrack.release();
				mAudioTrack = null;
				return;
			}

			mAudioTrack.play();
			
			if (listener != null){
				listener.onStartPlayMusic();
			}
			
			// 获取PCM音频流，并播放
			while (mThreadrRunningFlag) {
				
				byte[] data = mAudioData.poll();
				try {
					// 网络慢时的等待2秒缓冲问题
					while (data == null && mThreadrRunningFlag && mAudioTrack != null){
						mAudioTrack.pause();
						sleep(2000);
						data = mAudioData.poll();
						if (data != null && mThreadrRunningFlag)
							mAudioTrack.play();
					}

					// 播放PCM数据
					if (data != null){
						int size = mAudioTrack.write(data, 0, data.length);
						mPlayIndex += size;
					}
				} catch (Exception e) {
					e.printStackTrace();
					onPlayComplete();
					mThreadrRunningFlag = false;
				}
			}
			// 释放资源
			release();
		}
	}

	/**
	 * 播放完毕时转换状态
	 */
	public void onPlayComplete() {
		mPlayAudioThread = null;
		if (mPlayState != PlayState.MPS_PAUSE) {
			setPlayState(PlayState.MPS_PREPARE);
		}
	}

	/**
	 * 播放状态
	 * 
	 * @author 韦念欣
	 * @time 2013-03-07
	 */
	public enum PlayState {
		/**
		 * 未就绪
		 */
		MPS_UNINIT,

		/**
		 * 准备就绪(停止)
		 */
		MPS_PREPARE,

		/**
		 * 正在播放
		 */
		MPS_PLAYING,

		/**
		 * 暂停播放
		 */
		MPS_PAUSE
	}

	
	/**
	 * 音频参数
	 * 
	 * @author 韦念欣
	 * @time 2013-03-07
	 */
	public static class AudioParam {
		/**
		 * 采样率
		 */
		private int mFrequency;

		/**
		 * 声道
		 */
		private int mChannel;

		/**
		 * 采样精度
		 */
		private int mSampBit;
		
		public AudioParam(){}
		public AudioParam(int frequency, int channel, int sampBit){
			setFrequency(frequency);
			setChannel(channel);
			setSampBit(sampBit);
		}
		
		public void setFrequency(int frequency){
			this.mFrequency = frequency;
		}
		
		public void setChannel(int channel){
			switch (channel){
			case 2:
				this.mChannel = AudioFormat.CHANNEL_OUT_STEREO ;
				break;
			case 1:
				this.mChannel = AudioFormat.CHANNEL_OUT_MONO;
				break;
			default:
				this.mChannel = AudioFormat.CHANNEL_OUT_DEFAULT;
				break;
			}
		}
		
		public void setSampBit(int sampBit){
			switch (sampBit){
			case 16:
				this.mSampBit = AudioFormat.ENCODING_PCM_16BIT;
				break;
			case 8:
				this.mSampBit = AudioFormat.ENCODING_PCM_8BIT;
				break;
			default:
				this.mSampBit = AudioFormat.ENCODING_DEFAULT;
				break;
			}
		}
	}
}
