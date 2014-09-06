package com.letv.airplay;

import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

import com.letv.upnpControl.tools.LetvLog;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

/**
 * AirPlay --> C调用Java的JNI接口类
 * @author 韦念欣
 * @modify Jamin
 *
 */
public class JniInterface {
	
	public static final String TAG = JniInterface.class.getSimpleName();

	public static final int VIDEO_PLAYER_ID = 0;
	public static final int AUDIO_PLAYER_ID = 1;
	public static final int PICTURE_SHOW_ID = 2;
	public static final int MIRRORING_PLAYER_ID	   = 3;
	
	
	/*
	 * 网络指令+遥控器指令+内部指令
	 */
	private static final int TYPE_SET				= 10000;
	private static final int TYPE_SET_MEDIAPLAYER	= TYPE_SET + 1;	
	private static final int TYPE_SET_AUDIOPLAYER	= TYPE_SET + 2;		
	private static final int TYPE_SET_PIC			= TYPE_SET + 3;	
	private static final int TYPE_SET_SERVICE		= TYPE_SET + 4;
	private static final int TYPE_SET_MEDIA_ACTIVITY= TYPE_SET + 5;
	
	private static final int TYPE_PIC				= 20000;
	private static final int TYPE_PIC_SHOW			= TYPE_PIC + 1;
	private static final int TYPE_PIC_HIDE			= TYPE_PIC + 2;
	private static final int TYPE_PIC_ONCREATED		= TYPE_PIC + 3;
	private static final int TYPE_PIC_STATUS			= TYPE_PIC + 4;
	
	private static final int TYPE_VIDEO				= 30000;
	private static final int TYPE_VIDEO_START 		= TYPE_VIDEO + 1;
	private static final int TYPE_VIDEO_PLAY		= TYPE_VIDEO + 2;
	private static final int TYPE_VIDEO_PAUSE		= TYPE_VIDEO + 3;
	private static final int TYPE_VIDEO_STOP		= TYPE_VIDEO + 4;
	private static final int TYPE_VIDEO_SEEK_MD		= TYPE_VIDEO + 5;
	private static final int TYPE_VIDEO_SEEK_RC		= TYPE_VIDEO + 6;
	private static final int TYPE_VIDEO_BACK		= TYPE_VIDEO + 7;
	private static final int TYPE_VIDEO_HOME		= TYPE_VIDEO + 8;
	private static final int TYPE_VIDEO_VOL_L		= TYPE_VIDEO + 9;
	private static final int TYPE_VIDEO_VOL_R 		= TYPE_VIDEO + 10;
	private static final int TYPE_VIDEO_VOL_MUTE 	= TYPE_VIDEO + 11;
	private static final int TYPE_PLAY_URL			= TYPE_VIDEO + 12;	
	private static final int TYPE_VIDEO_PLAYORPAUSE	= TYPE_VIDEO + 13;
	private static final int TYPE_VIDEO_ERROR		= TYPE_VIDEO + 14;
	private static final int TYPE_VIDEO_SEEK_TS		= TYPE_VIDEO + 15;	
	private static final int TYPE_VIDEO_FINISH		= TYPE_VIDEO + 16;
	private static final int TYPE_VIDEO_PRO			= TYPE_VIDEO + 17;
	private static final int TYPE_VIDEO_PRO_VIEW	= TYPE_VIDEO + 18;
	private static final int TYPE_VIDEO_ONCREATED	= TYPE_VIDEO + 19;
	private static final int TYPE_VIDEO_STATUS		= TYPE_VIDEO + 20;
	
	private static final int TYPE_AUDIO				= 40000;
	private static final int TYPE_AUDIO_START 		= TYPE_AUDIO + 1;
	private static final int TYPE_AUDIO_PUSH_DATA 	= TYPE_AUDIO + 2;
	private static final int TYPE_AUDIO_LOGO 		= TYPE_AUDIO + 3;
	private static final int TYPE_AUDIO_ASAR 		= TYPE_AUDIO + 4;
	private static final int TYPE_AUDIO_MINM 		= TYPE_AUDIO + 5;
	private static final int TYPE_AUDIO_VOL			= TYPE_AUDIO + 6;
	private static final int TYPE_AUDIO_CLOSE 		= TYPE_AUDIO + 7;
	private static final int TYPE_AUDIO_BACK 		= TYPE_AUDIO + 8;
	private static final int TYPE_AUDIO_HOME		= TYPE_AUDIO + 9;
	private static final int TYPE_AUDIO_VOL_L		= TYPE_AUDIO + 10;
	private static final int TYPE_AUDIO_VOL_R		= TYPE_AUDIO + 11;
	private static final int TYPE_AUDIO_VOL_MUTE	= TYPE_AUDIO + 12;
	private static final int TYPE_AUDIO_ONCREATED   = TYPE_AUDIO + 13;
	private static final int TYPE_AUDIO_STATUS		= TYPE_AUDIO + 14;
	
	private static final int TYPE_MIRRORING					= 50000;
	private static final int TYPE_MIRRORING_START			= TYPE_MIRRORING + 1;
	private static final int TYPE_MIRRORING_PUSH_DATA			= TYPE_MIRRORING + 2;
	private static final int TYPE_MIRRORING_CLOSE			= TYPE_MIRRORING + 3;
	private static final int TYPE_MIRRORING_SET				= TYPE_MIRRORING + 4;
	private static final int TYPE_MIRRORING_STATUS			= TYPE_MIRRORING + 5;
	private static final int TYPE_MIRRORING_HOME				= TYPE_MIRRORING + 6;
	
	public int seekPos = 0;

	private AirplayService airplayService = null;
	private AirplayMediaPlayerActivity mediaPlayerActivity = null;
	private VideoPlayerManager videoPlayerManager = null;
	private AudioPlayerManager audioPlayerManager = null;
	private AirplayPictureShowActivity pictureActivity = null;
	private MirroringPlayerActivity mirroringPlayerActivity = null;
	
	private HandlerThread handlerThread;
	private MyHandler myHandler;
	
	public boolean playVideo = false;
	public boolean playMusic = false;
	public boolean showPicture = false;
	public boolean playMirroring = false;
	
	public byte[] Data = null;
	/*
	 * 确保startActivity串行
	 */
	Semaphore OnCreatedMedia;
	
	Semaphore OnCreatedPicture;
	
	Semaphore OnCreatedMirroring;
	
	private int VideoDuration;
	private int VideoPosition;
	
	public ConcurrentLinkedQueue<byte[]> mMirroringData;	// MirroringData
	/**
	 * JNI初始化方法
	 */
	public native void initJniInterface();

	
	public class StatusActivity {
		private AirplayMediaPlayerActivity beforeActivity;
		private AirplayMediaPlayerActivity afterActivity;
		
		StatusActivity(AirplayMediaPlayerActivity before, AirplayMediaPlayerActivity after){
			beforeActivity = before;
			afterActivity = after;
		}
	}
	
	/**
	 * mediaplayer arg
	 * @author Jamin
	 *
	 */
	public class MediaPlayerArg{
		private String mediaUrl;
		private int percent;
		private String videoId;
		
		MediaPlayerArg(String url, int pos, String id){
			mediaUrl = url;
			percent = pos;
			videoId = id;
		}
		
		public String getMediaUrl(){
			return mediaUrl;
		}
		public int getPercent(){
			return percent;
		}
		
		public String getVideoId(){
			return videoId;
		}
	}
	
	public class MediaPlayerState{
		private String videoId;
		private int state;
		private int stateType;
		
		MediaPlayerState(String id, int status, int st){
			videoId = id;
			state = status;
			stateType = st;
		}
		
		public String getVideoId(){
			return videoId;
		}
		
		public int getState(){
			return state;
		}
		public int getStateType(){
			return stateType;
		}
	}
	

	
	/**
	 * 单例
	 */
	private JniInterface() {
		
		handlerThread = new HandlerThread("jni_handler_thread");  
		handlerThread.start();
		myHandler = new MyHandler(handlerThread.getLooper());  
		
		OnCreatedMedia = new Semaphore(1);
		
		OnCreatedPicture = new Semaphore(1);
		
		OnCreatedMirroring = new Semaphore(1);
		
		mMirroringData = new ConcurrentLinkedQueue<byte[]>();
	}
	private static JniInterface singleton = new JniInterface();
	public static JniInterface getInstance() {
		return singleton;
	}

	class MyHandler extends Handler{  
        public MyHandler(){  
           
        }  
        public MyHandler(Looper looper){  
           super(looper);  
        }  
        /** 
         * 更改界面的方法 （其实handlemessage就是你想这个线程做的事情）
         */  
        @Override  
        public void handleMessage(Message msg) {
			//LetvLog.d(TAG, "handler_thread currentThread: " +Thread.currentThread().getId());
			super.handleMessage(msg);
			switch (msg.what){
			case TYPE_VIDEO_START:		
				SemaphoreMediaAcquire();
				
				setPlayVideo(true);
				setPlayMusic(false);
				setShowPicture(false);	
				setPlayMirroring(false);
				
				setPlayDuration(0);
				setPlayPosition(0);
				if (!TextUtils.isEmpty(((MediaPlayerArg)msg.obj).getMediaUrl())){
					if (mediaPlayerActivity == null) {
						airplayService.startActivity(VIDEO_PLAYER_ID, (MediaPlayerArg)msg.obj);
					}else {
						LetvLog.d(TAG, "TYPE_VIDEO_START "+ " mHandler: " + mediaPlayerActivity.mHandler);
						if (mediaPlayerActivity.mHandler != null) {
							audioPlayerManager = null;
							AirplayMediaPlayerActivity.CM cm = mediaPlayerActivity.new CM((MediaPlayerArg)msg.obj);
							mediaPlayerActivity.mHandler.post(cm);
						}
					}
				}
				break;
			case TYPE_VIDEO_PLAY:
				if(videoPlayerManager != null){
					videoPlayerManager.play();
				}
				break;
			case TYPE_VIDEO_PAUSE:
				if(videoPlayerManager != null){
					videoPlayerManager.pausePlay();
				}
				break;
			case TYPE_VIDEO_STOP:
				if(videoPlayerManager != null){
					String id = videoPlayerManager.getVideoId();
					String all = "all";
					LetvLog.d(TAG, "video stop  svid: " + id + " cvid: " + (String)msg.obj);
					if((id.equals((String)msg.obj)) || (id.equals(all))){
					setPlayVideo(false);
					videoPlayerManager.stopPlay();
					videoPlayerManager.finish();
					videoPlayerManager = null;
					}
				}
				break;
			case TYPE_VIDEO_SEEK_MD:
				LetvLog.d(TAG, "TYPE_VIDEO_SEEK_MD");
				if(videoPlayerManager != null){
					seekPos = (Integer)msg.obj * 1000;
					if(videoPlayerManager != null){
						videoPlayerManager.seekMediaPlayerUpdateUI(seekPos);
						videoPlayerManager.seekMediaPlayerMD(seekPos);
					}
				}
				break;
			case TYPE_PLAY_URL:
				if(videoPlayerManager != null){
					videoPlayerManager.play();
				}
				break;
			case TYPE_VIDEO_SEEK_RC:
				LetvLog.d(TAG, "TYPE_VIDEO_SEEK_RC");
				if(videoPlayerManager != null){
					videoPlayerManager.seekMediaPlayerRC((Integer)msg.obj);
				}
				break;
			case TYPE_VIDEO_SEEK_TS:
				LetvLog.d(TAG, "TYPE_VIDEO_SEEK_TS");
				if(videoPlayerManager != null){
					videoPlayerManager.seekMediaPlayerUpdateUI(seekPos);
					videoPlayerManager.seekMediaPlayerTS((Integer)msg.obj);
				}
				break;
			case TYPE_VIDEO_VOL_R:
				LetvLog.d(TAG, "TYPE_VIDEO_VOL_R");
				if(videoPlayerManager != null){
					videoPlayerManager.raiseVolume();
				}				
				break;
			case TYPE_VIDEO_VOL_L:
				LetvLog.d(TAG, "TYPE_VIDEO_VOL_L");
				if(videoPlayerManager != null){
					videoPlayerManager.reduceVolume();
				}
				break;
			case TYPE_VIDEO_VOL_MUTE:
				LetvLog.d(TAG, "TYPE_VIDEO_VOL_MUTE");
				if(videoPlayerManager != null){
					videoPlayerManager.muteVolume();
				}
				break;	
			case TYPE_VIDEO_BACK:
				if (videoPlayerManager != null) {
					setPlayVideo(false);
					videoPlayerManager.finish();
					videoPlayerManager = null;
				}
				break;
			case TYPE_VIDEO_HOME:
				if (videoPlayerManager != null) {
					setPlayVideo(false);
					videoPlayerManager.stopPlay();	
					videoPlayerManager.finish();
					videoPlayerManager = null;
				}
				break;
			case TYPE_VIDEO_PLAYORPAUSE:
				if(videoPlayerManager != null){
					videoPlayerManager.VideoPlayOrPause();
				}				
				break;
			case TYPE_VIDEO_FINISH:
				if(videoPlayerManager != null){
					setPlayVideo(false);
					videoPlayerManager.finish();
					videoPlayerManager = null;
				}
				break;
			case TYPE_VIDEO_ERROR:
				if (videoPlayerManager != null) {
					setPlayVideo(false);
					videoPlayerManager.finish();
					videoPlayerManager = null;
				}
				break;
			case TYPE_VIDEO_PRO:
				if (videoPlayerManager != null) {
					videoPlayerManager.progress();
				}
				break;
			case TYPE_VIDEO_PRO_VIEW:
				if (videoPlayerManager != null) {
					videoPlayerManager.progressView((Integer)msg.obj);
				}				
				break;
			case TYPE_SET_MEDIAPLAYER:
				LetvLog.d(TAG, "set VideoPlayerManager playVideo: " + playVideo);
				if(playVideo)
					videoPlayerManager = (VideoPlayerManager)msg.obj;
				break;
			case TYPE_SET_AUDIOPLAYER:
				LetvLog.d(TAG, "set AudioPlayerManager playMusic: " + playMusic);
				if(playMusic)
					audioPlayerManager = (AudioPlayerManager)msg.obj;
				break;
			case TYPE_SET_PIC:
				LetvLog.d(TAG, "set PictureShowActivity showPicture: " + showPicture);
				if(showPicture)
					pictureActivity = (AirplayPictureShowActivity)msg.obj;
				break;
			case TYPE_MIRRORING_SET:
				mirroringPlayerActivity = (MirroringPlayerActivity)msg.obj;
				break;
			case TYPE_PIC_SHOW:
				SemaphorePictureAcquire();
				
				setPlayMusic(false);
				setPlayVideo(false);
				setPlayMirroring(false);
				setShowPicture(true);
				
				String id = "all";
				AirplayService.setMediaPlayerState(id, id.length(), AirplayService.STOP, AirplayService.STOP_NORNAL);
				AirplayService.setAudioPlayerState(id, id.length(), AirplayService.STOP);
				AirplayService.setMirroringState(id, id.length(), AirplayService.STOP);
				
				if (mediaPlayerActivity != null){
					mediaPlayerActivity.finish();
					mediaPlayerActivity = null;
				}				
				if (pictureActivity == null){
					Data = null;
					Data = ((PicturePlayerArg)msg.obj).getPicData();
					airplayService.startActivity(PICTURE_SHOW_ID, new PicturePlayerArg(null, ((PicturePlayerArg)msg.obj).getPictureId(), ((PicturePlayerArg)msg.obj).getPictureType()));
				}else{
					pictureActivity.showPic((PicturePlayerArg)msg.obj);
				}
				break;
			case TYPE_PIC_HIDE:
				
				if (pictureActivity != null){
					if(pictureActivity.Finish((String)msg.obj))
					{
						pictureActivity = null;
						setShowPicture(false);						
					}
				}
				System.gc();
				break;
			case TYPE_SET_SERVICE:
				airplayService = (AirplayService)msg.obj;
				mediaPlayerActivity = null;
				videoPlayerManager = null;
				audioPlayerManager = null;
				pictureActivity = null;
				mirroringPlayerActivity = null;
				
				setPlayVideo(false);
				setPlayMusic(false);
				setShowPicture(false);
				setPlayMirroring(false);
				initJniInterface();
				break;
			case TYPE_SET_MEDIA_ACTIVITY:
				if(((StatusActivity)msg.obj).afterActivity == null){
					if (((StatusActivity)msg.obj).beforeActivity == mediaPlayerActivity){
						mediaPlayerActivity = ((StatusActivity)msg.obj).afterActivity;
						LetvLog.d(TAG, "mediaPlayerActivity: " + mediaPlayerActivity);
					}
				}else{
					mediaPlayerActivity = ((StatusActivity)msg.obj).afterActivity;
					LetvLog.d(TAG, "mediaPlayerActivity: " + mediaPlayerActivity);
				}
				break;
			case TYPE_VIDEO_STATUS:
				if(((MediaPlayerState)msg.obj).getVideoId() == null){
					AirplayService.setMediaPlayerState(null, 0, ((MediaPlayerState)msg.obj).getState(), ((MediaPlayerState)msg.obj).getStateType());
				}else{
					AirplayService.setMediaPlayerState(((MediaPlayerState)msg.obj).getVideoId(), ((MediaPlayerState)msg.obj).getVideoId().length(), ((MediaPlayerState)msg.obj).getState(), ((MediaPlayerState)msg.obj).getStateType());
				}
					break;
			case TYPE_AUDIO_STATUS:
				if(((MediaPlayerState)msg.obj).getVideoId() == null){
					AirplayService.setAudioPlayerState(null, 0, ((MediaPlayerState)msg.obj).getState());
				}else{
					AirplayService.setAudioPlayerState(((MediaPlayerState)msg.obj).getVideoId(), ((MediaPlayerState)msg.obj).getVideoId().length(), ((MediaPlayerState)msg.obj).getState());
				}
				break;
			case TYPE_PIC_STATUS:
				if(((MediaPlayerState)msg.obj).getVideoId() == null){
					AirplayService.setPictureState(null, 0, ((MediaPlayerState)msg.obj).getState());
				}else{
					AirplayService.setPictureState(((MediaPlayerState)msg.obj).getVideoId(), ((MediaPlayerState)msg.obj).getVideoId().length(), ((MediaPlayerState)msg.obj).getState());
				}
				break;
			case TYPE_MIRRORING_STATUS:
				if(((MediaPlayerState)msg.obj).getVideoId() == null){
					AirplayService.setMirroringState(null, 0, ((MediaPlayerState)msg.obj).getState());
				}else{
					AirplayService.setMirroringState(((MediaPlayerState)msg.obj).getVideoId(), ((MediaPlayerState)msg.obj).getVideoId().length(), ((MediaPlayerState)msg.obj).getState());
				}
				break;
			case TYPE_AUDIO_START:
				SemaphoreMediaAcquire();
				setPlayMusic(true);
				setPlayVideo(false);
				setShowPicture(false);
				setPlayMirroring(false);
				// 释放音频资源
				if (audioPlayerManager != null) {
					audioPlayerManager.stopPlay();
					audioPlayerManager = null;
				}
				
				if (mediaPlayerActivity == null) {
					airplayService.startActivity(AUDIO_PLAYER_ID, null);
				}else {
					if (mediaPlayerActivity.mHandler != null
							&& mediaPlayerActivity.ra != null) {
						videoPlayerManager = null;
						mediaPlayerActivity.mHandler.post(mediaPlayerActivity.ra);
					}
				}
				break;
			case TYPE_AUDIO_PUSH_DATA:
				if (audioPlayerManager != null) {
					audioPlayerManager.addDataSource((byte[])msg.obj, msg.arg1, msg.arg2);
				}
				break;
			case TYPE_AUDIO_LOGO:
				if(audioPlayerManager != null)
					audioPlayerManager.setMusicImage((byte[])msg.obj, 0, msg.arg1);
				else if(true == getPlayMusic()){
					LetvLog.d(TAG, "Again AudioPlayerLetvLogo");
					myHandler.sendMessageDelayed(myHandler.obtainMessage(TYPE_AUDIO_LOGO, msg.arg1, 0, (byte[])msg.obj), 1000);						
				}else{
					;
				}
				break;
			case TYPE_AUDIO_ASAR:
				if(audioPlayerManager != null)
					audioPlayerManager.setMusicAuthor((String)msg.obj);
				else if(true == getPlayMusic()){
					LetvLog.d(TAG, "Again AudioPlayerAsar");
					myHandler.sendMessageDelayed(myHandler.obtainMessage(TYPE_AUDIO_ASAR, msg.arg1, 0, (String)msg.obj), 1000);					
				}else{
					;
				}
				break;
			case TYPE_AUDIO_MINM:
				if(audioPlayerManager != null)
					audioPlayerManager.setMusicTitle((String)msg.obj);
				else if(true == getPlayMusic()){
					LetvLog.d(TAG, "Again AudioPlayerMinm");
					myHandler.sendMessageDelayed(myHandler.obtainMessage(TYPE_AUDIO_MINM, msg.arg1, 0, (String)msg.obj), 1000);					
				}else{
					;
				}
				break;
			case TYPE_AUDIO_VOL:
				if (audioPlayerManager != null) {
					audioPlayerManager.setStereoVolume(((Vol) msg.obj).getletfVol(), ((Vol) msg.obj).getrightVol());
				}
				break;
			case TYPE_AUDIO_VOL_L:
				if (audioPlayerManager != null) {
					audioPlayerManager.reduceVolume();
				}
				break;
			case TYPE_AUDIO_VOL_R:
				if (audioPlayerManager != null) {
					audioPlayerManager.raiseVolume();
				}
				break;
			case TYPE_AUDIO_VOL_MUTE:
				if (audioPlayerManager != null) {
					audioPlayerManager.muteVolume();
				}
				break;
			case TYPE_AUDIO_BACK:
				//AirplayService.setAudioPlayerState(AirplayService.STOP);
				if (audioPlayerManager != null) {
					setPlayMusic(false);
					audioPlayerManager.finish();
					audioPlayerManager = null;
				}
				break;
			case TYPE_AUDIO_HOME:
				//AirplayService.setAudioPlayerState(AirplayService.STOP);
				if (audioPlayerManager != null) {
					setPlayMusic(false);
					audioPlayerManager.finish();
					audioPlayerManager = null;
				}
				break;
			case TYPE_AUDIO_CLOSE:
				if (audioPlayerManager != null) {
					setPlayMusic(false);
					audioPlayerManager.finish();
					audioPlayerManager = null;
				}	
				//new Handler(mediaPlayerActivity.getMainLooper()).post(new Runnable(){
				//	@Override
				//	public void run() {
				//		if (audioPlayerManager != null && playVideo == false) {
				//			LetvLog.d(TAG, "closeAudioPlayer finish");
				//			audioPlayerManager.finish();
				//			audioPlayerManager = null;
				//		}
				//	}
				//});
				break;
			case TYPE_MIRRORING_START:
				SemaphoreMirroringAcquire();

				if (mirroringPlayerActivity != null)
				{
					final String type = ((MirroringPlayerArg)msg.obj).getType();
					final int width = ((MirroringPlayerArg)msg.obj).getWidth();
					final int height = ((MirroringPlayerArg)msg.obj).getHeight();
					final String mid = ((MirroringPlayerArg)msg.obj).getMirroringId();
					mirroringPlayerActivity.changeSurfaceView(type, width, height, mid);
				}
				else
				{
					setPlayMusic(false);
					setPlayVideo(false);
					setShowPicture(false);
					setPlayMirroring(true);
					mMirroringData.clear();
					airplayService.startActivity(MIRRORING_PLAYER_ID, (MirroringPlayerArg)msg.obj);
				}
				break;
			case TYPE_MIRRORING_PUSH_DATA:
					addDataSource((byte[])msg.obj, msg.arg1, msg.arg2);
				break;
			case TYPE_MIRRORING_CLOSE:
				mMirroringData.clear();
				if (mirroringPlayerActivity != null) {
					mirroringPlayerActivity.Finish();
					mirroringPlayerActivity = null;
					
				}
				break;
			case TYPE_MIRRORING_HOME:
				mMirroringData.clear();
				if (mirroringPlayerActivity != null) {
					setPlayMirroring(false);
					mirroringPlayerActivity.Finish();
					mirroringPlayerActivity = null;
					
				}
				break;
			default:
				break;
			}
		}  
   }  

	
	
	
	////////////////////////设置各个控制对象/////////////////////////
	public void setService(AirplayService airplayService) {
		myHandler.sendMessage(myHandler.obtainMessage(TYPE_SET_SERVICE, airplayService));
	}

	
	public void setMediaActivity(AirplayMediaPlayerActivity before, AirplayMediaPlayerActivity after) {
		LetvLog.d(TAG, "before: " + before + " after: " + after);
		myHandler.sendMessage(myHandler.obtainMessage(TYPE_SET_MEDIA_ACTIVITY, new StatusActivity(before, after)));
	}

	public void setMediaPlayer(VideoPlayerManager mediaPlayer) {
		LetvLog.d(TAG, "setMediaPlayer : " + mediaPlayer);
		myHandler.sendMessage(myHandler.obtainMessage(TYPE_SET_MEDIAPLAYER, mediaPlayer));
	}
	
	
	public void setAudioPlayer(AudioPlayerManager audioPlayer) {
		LetvLog.d(TAG, "setAudioPlayer : " + audioPlayer);
		myHandler.sendMessage(myHandler.obtainMessage(TYPE_SET_AUDIOPLAYER, audioPlayer));
	}

	
	
	public void setPictureShow(AirplayPictureShowActivity pictureShow) {
		myHandler.sendMessage(myHandler.obtainMessage(TYPE_SET_PIC, pictureShow));
	}

	
	public void setMirroringPlayer(MirroringPlayerActivity mpActivity) {
		myHandler.sendMessage(myHandler.obtainMessage(TYPE_MIRRORING_SET, mpActivity));
	}
	
	public void setPlayVideo(boolean Flag) {
		playVideo = Flag;
		LetvLog.d(TAG, "setPlayVideo: " + playVideo);
	}

	public boolean getPlayVideo() {
		LetvLog.d(TAG, "getPlayVideo: " + playVideo);
		return playVideo;
	}

	public void setPlayMusic(boolean Flag) {
		playMusic = Flag;
		LetvLog.d(TAG, "setPlayMusic: " + playMusic);
	}

	public boolean getPlayMusic(){
		LetvLog.d(TAG, "getPlayMusic: " + playMusic);	
		return playMusic;
	}
	
	public void setShowPicture(boolean Flag) {
		showPicture = Flag;
		LetvLog.d(TAG, "setShowPicture: " + showPicture);
	}

	public boolean getShowPicture() {
		LetvLog.d(TAG, "getShowPicture: " + showPicture);
		return showPicture;
	}
	
	public void setPlayMirroring(boolean Flag) {
		playMirroring = Flag;
		LetvLog.d(TAG, "setPlayMirroring: " + playMirroring);
	}

	public boolean getPlayMirroring() {
		LetvLog.d(TAG, "setPlayMirroring: " + playMirroring);
		return playMirroring;
	}
	
	public void setPlayDuration(int d)
	{
		if(d >= 0)
			VideoDuration = d;
		LetvLog.d(TAG, "setPlayDuration: " + VideoDuration);
	}
	
	public void setPlayPosition(int p){
		if(p >= 0)
			VideoPosition = p;
		LetvLog.d(TAG, "setPlayPositon: " + VideoPosition);
	}
	
	public void SemaphoreMediaAcquire(){
		LetvLog.d(TAG, "SemaphoreMediaAcquire start");
		try {
			OnCreatedMedia.acquire();
		} catch (InterruptedException e) {
			LetvLog.d(TAG, "SemaphoreMediaAcquire faild!");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		LetvLog.d(TAG, "SemaphoreMediaAcquire end");
	}

	public void SemaphoreMediaRelease(){
		int count;
		count = OnCreatedMedia.availablePermits();
		LetvLog.d(TAG, "SemaphoreMediaRelease start count: " + count);
		if(count == 0){
			OnCreatedMedia.release();
			LetvLog.d(TAG, "SemaphoreMediaReleasing");
		}
		else{
		}
		LetvLog.d(TAG, "SemaphoreMediaRelease end");
	}
	
	
	public void SemaphorePictureAcquire(){
		LetvLog.d(TAG, "SemaphorePictureAcquire start");
		try {
			OnCreatedPicture.acquire();
		} catch (InterruptedException e) {
			LetvLog.d(TAG, "SemaphorePictureAcquire faild!");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		LetvLog.d(TAG, "SemaphorePictureAcquire end");
	}

	public void SemaphorePictureRelease(){
		int count;
		count = OnCreatedPicture.availablePermits();
		LetvLog.d(TAG, "SemaphorePictureRelease start count: " + count);
		if(count == 0){
			OnCreatedPicture.release();
			LetvLog.d(TAG, "SemaphorePictureReleasing");
		}
		else{
		}
		LetvLog.d(TAG, "SemaphorePictureRelease end");
	}
	
	public void SemaphoreMirroringAcquire(){
		LetvLog.d(TAG, "SemaphoreMirroringAcquire start");
		try {
			OnCreatedMirroring.acquire();
		} catch (InterruptedException e) {
			LetvLog.d(TAG, "SemaphoreMirroringAcquire faild!");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		LetvLog.d(TAG, "SemaphoreMirroringAcquire end");
	}

	public void SemaphoreMirroringRelease(){
		int count;
		count = OnCreatedMirroring.availablePermits();
		LetvLog.d(TAG, "SemaphoreMirroringRelease start count: " + count);
		if(count == 0){
			OnCreatedMirroring.release();
			LetvLog.d(TAG, "SemaphoreMirroringReleasing");
		}
		else{
		}
		LetvLog.d(TAG, "SemaphoreMirroringRelease end");
	}
	
	public void startActivity(int appId) {
		LetvLog.d(TAG, "startActivity:" + appId);
		airplayService.startActivity(appId, null);
	}

	
	/**
	 * 遥控器的控制
	 */
	
	/*
	 * Audio
	 */
	public void AudioraiseVolume(){
		LetvLog.d(TAG, "raiseVolume");
		myHandler.sendEmptyMessage(TYPE_AUDIO_VOL_R);
	}
	
	public void AudioreduceVolume(){
		LetvLog.d(TAG, "reduceVolume");
		myHandler.sendEmptyMessage(TYPE_AUDIO_VOL_L);
	}
	
	public void AudiomuteVolume(){
		LetvLog.d(TAG, "muteVolume");
		myHandler.sendEmptyMessage(TYPE_AUDIO_VOL_MUTE);
	}
	
	public void AudioBack(){
		LetvLog.d(TAG, "AudioBack");
		myHandler.sendEmptyMessage(TYPE_AUDIO_BACK);
	}
	public void AudioHome(){
		LetvLog.d(TAG, "AudioHome");
		myHandler.sendEmptyMessage(TYPE_AUDIO_HOME);
	}
	
	/*
	 * Video
	 */
	public void VideoraiseVolume(){
		LetvLog.d(TAG, "raiseVolume");
		myHandler.sendEmptyMessage(TYPE_VIDEO_VOL_R);
	}
	
	public void VideoreduceVolume(){
		LetvLog.d(TAG, "reduceVolume");
		myHandler.sendEmptyMessage(TYPE_VIDEO_VOL_L);
	}
	
	public void VideomuteVolume(){
		LetvLog.d(TAG, "muteVolume");
		myHandler.sendEmptyMessage(TYPE_VIDEO_VOL_MUTE);
	}
	
	public void VideoSeekRC(int action){
		LetvLog.d(TAG, "VideoSeekRC " + "action: " + action);
		myHandler.removeMessages(TYPE_VIDEO_PLAYORPAUSE);
		myHandler.removeMessages(TYPE_VIDEO_SEEK_RC);
		myHandler.removeMessages(TYPE_VIDEO_SEEK_MD);
		myHandler.removeMessages(TYPE_VIDEO_SEEK_TS);
		myHandler.removeMessages(TYPE_VIDEO_PRO_VIEW);
		myHandler.sendMessage(myHandler.obtainMessage(TYPE_VIDEO_SEEK_RC, action));
	}

	public void VideoSeekTS(int pos){
		LetvLog.d(TAG, "VideoSeekTS " + "action: " + pos);
		myHandler.removeMessages(TYPE_VIDEO_PLAYORPAUSE);
		myHandler.removeMessages(TYPE_VIDEO_SEEK_RC);
		myHandler.removeMessages(TYPE_VIDEO_SEEK_MD);	
		myHandler.removeMessages(TYPE_VIDEO_SEEK_TS);
		myHandler.removeMessages(TYPE_VIDEO_PRO_VIEW);
		myHandler.sendMessage(myHandler.obtainMessage(TYPE_VIDEO_SEEK_TS, pos));
	}
	
	public void VideoBack(){
		LetvLog.d(TAG, "AudioBack");
		myHandler.sendEmptyMessage(TYPE_VIDEO_BACK);
	}
	
	public void VideoPlayOrPause(){
		LetvLog.d(TAG, "VideoPlayOrPause");
		myHandler.sendEmptyMessage(TYPE_VIDEO_PLAYORPAUSE);
	}
	
	public void VideoHome(){
		LetvLog.d(TAG, "VideoHome");
		myHandler.sendEmptyMessage(TYPE_VIDEO_HOME);
	}

	public void VideoFinish(){
		LetvLog.d(TAG, "VideoFinish");
		myHandler.sendEmptyMessage(TYPE_VIDEO_FINISH);
	}
	public void VideoOnError(){
		LetvLog.d(TAG, "VideoOnError");
		myHandler.sendEmptyMessage(TYPE_VIDEO_ERROR);
	}
	
	public void VideoPro(){
		//LetvLog.d(TAG, "VideoPro");
		myHandler.sendEmptyMessage(TYPE_VIDEO_PRO);		
	}
	
	public void VideoProView(int pos){
		//LetvLog.d(TAG, "VideoProView");
		myHandler.sendMessage(myHandler.obtainMessage(TYPE_VIDEO_PRO_VIEW, pos));		
	}
	
	/*
	 * Mirroring
	 * */
	public void MirroringHome(){
		LetvLog.d(TAG, "MirroringHome");
		myHandler.sendEmptyMessage(TYPE_MIRRORING_HOME);
	}
	
	
	public void setVideoState(String id, int status, int statusType){
		LetvLog.d(TAG, id + " setVideoState: " + status + "type: " + statusType);
		myHandler.sendMessage(myHandler.obtainMessage(TYPE_VIDEO_STATUS, 0, 0, new MediaPlayerState(id, status, statusType)));
	}
	
	public void setAudioState(String id, int status){
		LetvLog.d(TAG, id + " setAudioState: " + status);
		myHandler.sendMessage(myHandler.obtainMessage(TYPE_AUDIO_STATUS, 0, 0, new MediaPlayerState(id, status, 0)));
	}
	
	public void setPictureState(String id, int status){
		LetvLog.d(TAG, id + " setPictureState: " + status);
		myHandler.sendMessage(myHandler.obtainMessage(TYPE_PIC_STATUS, 0, 0, new MediaPlayerState(id, status, 0)));
	} 

	public void setMirroringState(String id, int status){
		LetvLog.d(TAG, id + " setMirroringState: " + status);
		myHandler.sendMessage(myHandler.obtainMessage(TYPE_MIRRORING_STATUS, 0, 0, new MediaPlayerState(id, status, 0)));
	} 	
	
	///////////////////////////推送视频//////////////////////////
	/**
	 * 开始视频推送
	 * @param mediaUrl
	 * @param percent 开始播放百分比
	 */
	public void startMediaPlayer(String mediaUrl, int percent, String id) {
		
		LetvLog.w(TAG, "startMediaPlayer "+ " mediaUrl: " + mediaUrl + " percent: " + percent + " id: " + id);
		myHandler.sendMessage(myHandler.obtainMessage(TYPE_VIDEO_START, 0, 0, new MediaPlayerArg(mediaUrl, percent, id)));
	}

	/**
	 * 获取视频总时间长度
	 * @return 单位毫秒
	 */
	public String mediaPlayerDurationGet() {
		
			LetvLog.d(TAG, "mediaPlayerDurationGet: " + VideoDuration);
			
			return String.valueOf(VideoDuration);
	}

	/**
	 * 获取当前播放的位置
	 * @return 单位毫秒
	 */
	public String mediaPlayerPositionGet() {
		
			LetvLog.d(TAG, "mediaPlayerPositionGet: " + VideoPosition);
			
			return String.valueOf(VideoPosition);
	}
	
	/**
	 * Seek操作
	 * @param pos 单位秒
	 */
	public void seekMediaPlayer(int pos, String id) {
		LetvLog.d(TAG, "seekMediaPlayer : " + pos);
		myHandler.removeMessages(TYPE_VIDEO_PLAYORPAUSE);
		myHandler.removeMessages(TYPE_VIDEO_SEEK_MD);
		myHandler.removeMessages(TYPE_VIDEO_SEEK_RC);
		myHandler.removeMessages(TYPE_VIDEO_SEEK_TS);
		myHandler.sendMessage(myHandler.obtainMessage(TYPE_VIDEO_SEEK_MD, pos));
	}

	/**
	 * 操作MediaPlayer
	 * @param operateType
	 */
	public void operateMediaPlayer(int operateType, String id) {
		//LetvLog.d(TAG, "operateMediaPlayer currentThread: " +Thread.currentThread().getId());		
		if (videoPlayerManager != null) {
			LetvLog.d(TAG, "operateMediaPlayer:" + operateType);
			switch (operateType){
			case VideoPlayerManager.PLAY:
				myHandler.sendEmptyMessage(TYPE_VIDEO_PLAY);
				break;
			case VideoPlayerManager.PAUSE:
				myHandler.sendEmptyMessage(TYPE_VIDEO_PAUSE);
				break;
			case VideoPlayerManager.STOP:
				myHandler.sendMessage(myHandler.obtainMessage(TYPE_VIDEO_STOP, id));
				break;
			case VideoPlayerManager.SEEK:
				myHandler.sendMessage(myHandler.obtainMessage(TYPE_VIDEO_SEEK_MD, seekPos));
				break;
			case VideoPlayerManager.PLAY_URL:
				myHandler.sendEmptyMessage(TYPE_PLAY_URL);
				break;
			default:
				break;
			}
		}
	}

	
	
	////////////////////////////推送音频////////////////////////////

	public static int gChannel;
	public static int gFrequency;
	public static int gSampBit;

	/**
	 * 开始音频推送
	 * @param mChannel
	 * @param mFrequency
	 * @param mSampBit
	 */
	public void startAudioPlayer(int mChannel, int mFrequency, int mSampBit) {
		LetvLog.d(TAG, "startAudioPlayer:" + mChannel + " " + mFrequency + " " + mSampBit);

		gChannel = mChannel;
		gFrequency = mFrequency;
		gSampBit = mSampBit;
		
		myHandler.sendEmptyMessage(TYPE_AUDIO_START);

	}

	/**
	 * 接收PCM数据
	 * @param audioData
	 * @param offsetInBytes
	 * @param sizeInBytes
	 */
	public void AudioPlayerPushData(byte[] audioData, int offsetInBytes, int sizeInBytes) {
		if(audioData != null)
			myHandler.sendMessage(myHandler.obtainMessage(TYPE_AUDIO_PUSH_DATA, offsetInBytes, sizeInBytes, audioData));
	}

	/**
	 * 关闭音频推送
	 */
	public void closeAudioPlayer() {
		LetvLog.d(TAG, "closeAudioPlayer:" + playVideo);
		
		myHandler.sendEmptyMessage(TYPE_AUDIO_CLOSE);
	}

	/**
	 * 设置音乐音量（0.0f - 1.0f）
	 * @param leftVolume
	 * @param rightVolume
	 */
	class Vol{
		Vol(float l, float r){leftVol = l; rightVol = r;};
		public float leftVol;
		public float rightVol;
		float getletfVol(){return leftVol;};
		float getrightVol(){return rightVol;};
	}
	
	public void setMediaVolume(float leftVolume, float rightVolume) {
		LetvLog.d(TAG, "setMediaVolume " + "leftVolume:" + leftVolume + "rightVolume:" + rightVolume);
		
		Vol vol = new Vol(leftVolume, rightVolume);
		myHandler.sendMessage(myHandler.obtainMessage(TYPE_AUDIO_VOL, 0, 0, vol));
	}

	/**
	 * 设置音乐的LetvLogO
	 * @param audioData
	 * @param sizeInBytes
	 */
	public void AudioPlayerLogo(byte[] audioData, int sizeInBytes) {
		if (sizeInBytes > 0 && audioData != null && audioData.length > 0) {
			LetvLog.d(TAG, "AudioPlayerLetvLogo");
			
			myHandler.sendMessage(myHandler.obtainMessage(TYPE_AUDIO_LOGO, sizeInBytes, 0, audioData));		
		}
	}

	/**
	 * 设置歌曲名
	 * @param audioData
	 * @param sizeInBytes
	 */
	public void AudioPlayerMinm(byte[] audioData, int sizeInBytes) {
		LetvLog.d(TAG, "AudioPlayerMinm");
		myHandler.sendMessage(myHandler.obtainMessage(TYPE_AUDIO_MINM, sizeInBytes, 0, new String(audioData)));	
	}

	/**
	 * 设置歌手
	 * @param audioData
	 * @param sizeInBytes
	 */
	public void AudioPlayerAsar(byte[] audioData, int sizeInBytes) {
		LetvLog.d(TAG, "AudioPlayerAsar");
		myHandler.sendMessage(myHandler.obtainMessage(TYPE_AUDIO_ASAR, sizeInBytes, 0, new String(audioData)));	

	}
	
	
	
	//////////////////////////////推送图片////////////////////////////////
	/**
	 * 显示推送的图片
	 * @param picData
	 * @param sizeInBytes
	 */
	public void startPictureShow(byte[] picData, int sizeInBytes, String iId, int iType) {
		LetvLog.d(TAG, "startPictureShow sizeInBytes: " + sizeInBytes);
		if(sizeInBytes > 4179){
			myHandler.sendMessage(myHandler.obtainMessage(TYPE_PIC_SHOW, 0, 0, new PicturePlayerArg(picData, iId, iType)));
		}
		else{
			LetvLog.d(TAG, "startPictureShow sizeInBytes is very less.");
		}
	}

	/**
	 * 停止推送图片
	 */
	public void stopPictureShow(String iId) {
		myHandler.sendMessage(myHandler.obtainMessage(TYPE_PIC_HIDE, 0, 0, iId));
	}	
	//////////////////////////////Mirroring//////////////////////////////////////////////////////
	
	public void startMirroringPlayer(String type, int width, int height, String id)
	{
		LetvLog.d(TAG, "startMirroringPlayer "+ " type: " + type + " width: " + width  + " height: " + height  + " id: " + id);
		myHandler.sendMessage(myHandler.obtainMessage(TYPE_MIRRORING_START, 0, 0, new MirroringPlayerArg(type, width, height, id)));
	}
	
	public void mirroringPlayerPushData(byte[] Data, int offset, int size, String id){
		
//		String result = "";
//		
//		 for (int i = 0; i < Data.length; i++) { 
//		     String hex = Integer.toHexString(Data[i] & 0xFF); 
//		     if (hex.length() == 1) { 
//		       hex = '0' + hex; 
//		     } 
//		     result += hex;
//		   }
//		 
//		 Log.d(TAG, "PushData: " + result); 
		if(Data != null)
			myHandler.sendMessage(myHandler.obtainMessage(TYPE_MIRRORING_PUSH_DATA, offset, size, Data));
		
	}
	public void closeMirroringPlayer(String id)
	{
		LetvLog.d(TAG, "closeMirroringPlayer "+ " id: " + id);
		
		myHandler.sendEmptyMessage(TYPE_MIRRORING_CLOSE);
	}

	public void addDataSource(byte[] audioData, int offsetInBytes, int sizeInBytes) {
		if (mMirroringData == null || audioData == null)
			return;
		
		if (offsetInBytes == 0 && sizeInBytes == audioData.length) {  
			mMirroringData.offer(audioData);
		} else {
			byte[] data = new byte[sizeInBytes];
			for (int i = 0; i < sizeInBytes; i++) {
				data[i] = audioData[offsetInBytes + i];
			}
			mMirroringData.offer(data);
		}
	}
	
	/**
	 * 加载库文件
	 */
    	static {
    		System.loadLibrary("gnustl_shared");
    		System.loadLibrary("glib-2.0");
    		System.loadLibrary("gmodule-2.0");
    		System.loadLibrary("gobject-2.0");
    		System.loadLibrary("gthread-2.0");
    		System.loadLibrary("iconv_airplay");
    		System.loadLibrary("xml2");
    		System.loadLibrary("debug_utils");
    		System.loadLibrary("porting_airplay");
    		System.loadLibrary("plist-master");
    		System.loadLibrary("airplayserver");
    		System.loadLibrary("airplayjni");
    	}
    	
}
