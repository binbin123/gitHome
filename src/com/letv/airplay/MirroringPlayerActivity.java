package com.letv.airplay;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.letv.airplay.JniInterface.MyHandler;
import com.letv.smartControl.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

/**
 * Mirroring
 * @author Jamin
 *
 */
public class MirroringPlayerActivity extends Activity implements SurfaceHolder.Callback{  
	
	public static final String TAG = MirroringPlayerActivity.class.getSimpleName();
	private final int MIRRORING_DATA = 20140521;

	private Handler mHandler;

	private MirroringThread mirroringThread;	// play thread
	
	private boolean mThreadrRunningFlag; 		// thread running flag
	
	private String type;
	private int width;
	private int height;
	private String mId;
	
	private Object lock = new Object(); // 同步锁
	
	private int winWidth;
	private int winHeight;
	
	private SurfaceView sv;
	private ImageView iv;
	private MediaCodec decoder;
	
	private byte[] data;
	
//	File file;
//	FileOutputStream in;
	
	RelativeLayout rlayout;
	RelativeLayout.LayoutParams ivlp;
	
	private HandlerThread handlerThread;
	private MyHandler myHandler;	
	//private RelativeLayout view;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); 
		
		Log.d(TAG, "MirroringPlayerActivity onCreate");
		
		Intent intent = getIntent();
		type = intent.getStringExtra("type");
		width = intent.getIntExtra("width", 0);
		height = intent.getIntExtra("height", 0);
		mId = intent.getStringExtra("mId");
		
		data = null;
		
		DisplayMetrics dm = new DisplayMetrics();
		WindowManager windowManager = (WindowManager) this
				.getSystemService(this.WINDOW_SERVICE);
		windowManager.getDefaultDisplay().getMetrics(dm);
		winWidth = dm.widthPixels;
		winHeight = dm.heightPixels;

//		file =  new File("/mnt/sda/sda1/h264-mirroring");
//		
//
//		
//		try {
//			file.createNewFile();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			
//			Log.d(TAG, "Mirroring createNewFile failed!");
//			e.printStackTrace();
//		}
//		
//		try {
//			in = new FileOutputStream(file);
//		} catch (FileNotFoundException e1) {
//			
//			Log.d(TAG, "Mirroring FileOutputStream failed!");
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		
		rlayout = new RelativeLayout(this);
		
		sv = new SurfaceView(this);

		 LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
		 
		 RelativeLayout view = (RelativeLayout)inflater.inflate(R.layout.mirroring, null);
		 
		 iv = (ImageView)view.findViewById(R.id.mirroring_logo);

		 view.removeView(iv);
		
		 //lp3.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
		 
		 ivlp = new RelativeLayout.LayoutParams(winWidth, winHeight);	 
		 
		 addSurfaceView();
		
		 addImageView();
		 
		 setContentView(rlayout);
		 sv.getHolder().addCallback(this);
		 
		JniInterface.getInstance().setMirroringPlayer(this);
		
		JniInterface.getInstance().SemaphoreMirroringRelease();
	}
 
	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}




	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		// TODO Auto-generated method stub
		Log.d(TAG, "Mirroring surfaceCreated!");
		
		if(false == isMediaCodecExist()){
			Log.d(TAG, "Mirroring android.media.MediaCodec is not exist!");
			String info =  getApplicationContext().getString(com.letv.smartControl.R.string.mirroring_error);
			Toast.makeText(this, info, 500).show();
			JniInterface.getInstance().setMirroringState(this.mId, AirplayService.STOP);
			return;
		}else{
			Log.d(TAG, "Mirroring android.media.MediaCodec exist!");
		}
		
		try {
			decoder = MediaCodec.createDecoderByType(type); 
			
			if(null == decoder){
				Log.d(TAG, "Mirroring decoder create faild!");
				JniInterface.getInstance().setMirroringState(this.mId, AirplayService.STOP);
				return;
			}
		    MediaFormat mediaFormat = MediaFormat.createVideoFormat(type, width, height);  
		    decoder.configure(mediaFormat, arg0.getSurface(), null, 0);  
		    decoder.start();
		    
		    mirroringThread = null;
			mThreadrRunningFlag = true;
			mirroringThread = new MirroringThread();
			mirroringThread.start();
		} catch (Exception e) {
			Log.d(TAG, "Mirroring surfaceCreated catch:" + e);
			JniInterface.getInstance().setMirroringState(this.mId, AirplayService.STOP);
		}
		

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		// TODO Auto-generated method stub
		mThreadrRunningFlag = false;
		Log.d(TAG, "Mirroring surfaceDestroyed!");
//		synchronized (lock) {
//			if(decoder != null){
//				decoder.stop();
//				decoder.release();
//				decoder = null;
//			}
//		}
	}

	
	@Override
	protected void onPause() {
		super.onPause();
		
		Log.d(TAG, "MirroringPlayerActivity onPause");
		JniInterface.getInstance().SemaphoreMirroringRelease();
		
		JniInterface.getInstance().MirroringHome();
	}

	@Override
	protected void onStop() {
		Log.d(TAG, "MirroringPlayerActivity onStop");
		super.onStop();
		
		JniInterface.getInstance().setMirroringState(this.mId, AirplayService.STOP);
		stopPlay();	
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "MirroringPlayerActivity onDestroy");
		JniInterface.getInstance().setMirroringState(this.mId, AirplayService.STOP);
	}
	
	class MyHandler extends Handler{  
        public MyHandler(){  
           
        }  
        public MyHandler(Looper looper){  
           super(looper);  
        }  
        /** 
         * handlerThread view
         */  
        @Override  
        public void handleMessage(Message msg) {

			super.handleMessage(msg);
			switch (msg.what){
			case MIRRORING_DATA:
				break;
			default:
				break;
			}
        }
        
	}
	
	/**
	 * 播放镜像的线程
	 */
	class MirroringThread extends Thread {

		@Override
		public void run() {
			
			long timestamp = 0l;
			
			// get h264 data and play
			while (mThreadrRunningFlag) {
				
				synchronized (lock) {
					if(decoder != null)
					{
						if(data == null)
						{
							data = JniInterface.getInstance().mMirroringData.poll();
						}			
						try {
							if (data != null){
								 ByteBuffer[] inputBuffers = decoder.getInputBuffers();  
						            int inputBufferIndex = decoder.dequeueInputBuffer(0);  
						        if (inputBufferIndex >= 0) {  
						            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];  
						            inputBuffer.clear();  
						            inputBuffer.put(data, 0, data.length); 
						         
//						            in.write(data);

						            decoder.queueInputBuffer(inputBufferIndex, 0, data.length, timestamp, 0); 
						            data = null;
						        }
						       MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();  
						       int outputBufferIndex = decoder.dequeueOutputBuffer(bufferInfo,0);  
						
						       while (outputBufferIndex >= 0) {  
						    	   decoder.releaseOutputBuffer(outputBufferIndex, true);  
						    	   outputBufferIndex = decoder.dequeueOutputBuffer(bufferInfo, 0);  
						       }  
							}
						} catch (Exception e) {
							e.printStackTrace();
							//Looper.prepare();
							//String info =  getApplicationContext().getString(com.letv.smartControl.R.string.mirroring_error);
							//Toast.makeText(getApplicationContext(), info, 500).show();	
							//Looper.loop();  
							mThreadrRunningFlag = false;
							
							Handler handler;
							handler = new Handler(getApplicationContext().getMainLooper());
							handler.post(new Runnable() {
								@Override
								public void run() {
									String info =  getApplicationContext().getString(com.letv.smartControl.R.string.mirroring_error);
									Toast.makeText(getApplicationContext(), info, 500).show();
									}
								}
							);
						}					
					}
				}
			}
			// 释放资源
			release();
		}
	}

//	public void addDataSource(byte[] audioData, int offsetInBytes, int sizeInBytes) {
//		if (mAudioData == null || audioData == null)
//			return;
//		
//		if (offsetInBytes == 0 && sizeInBytes == audioData.length) {  
//			mAudioData.offer(audioData);
//		} else {
//			byte[] data = new byte[sizeInBytes];
//			for (int i = 0; i < sizeInBytes; i++) {
//				data[i] = audioData[offsetInBytes + i];
//			}
//			mAudioData.offer(data);
//		}
//	}
	
	public void changeSurfaceView(String iType, int iWidth, int iHeight, String iId)
	{
		width = iWidth;
		height = iHeight;
		mId = iId;
		Log.d(TAG, "Mirroring changeSurfaceView: width" + width + " height:" + height); 
		
		Handler handler;
		handler = new Handler(this.getMainLooper());

		handler.post(new Runnable() {
			@Override
			public void run() {
				removeSurfaceView();
				removeImageView();
				addSurfaceView();
				addImageView();
				JniInterface.getInstance().SemaphoreMirroringRelease();
				}
			}
		);
	}
	/**
	 * release decoder
	 */
	private boolean release() {
		Log.d(TAG, "Mirroring thread release!"); 
		
//		  try {
//			in.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			
//			Log.d(TAG, "Mirroring close release!"); 
//			e.printStackTrace();
//		}
		
		synchronized (lock) {
			if(decoder != null){
				Log.d(TAG, "Mirroring decoder release!"); 
				decoder.stop();
				decoder.release();
				decoder = null;
			}
		}
		
		return true;
	}
	
	private void addSurfaceView()
	{

		float scale = (float) width / height;
		int tmpWidth = Math.round(scale * winHeight);
		int tmpHeight = Math.round(winWidth / scale);

		int dHeight = winHeight;
		int dWidth = winWidth;
			
		if (tmpWidth > dWidth) {
			dHeight = tmpHeight;
		}
		if (tmpHeight > dHeight) {
			dWidth = tmpWidth;
		}
		
		 RelativeLayout.LayoutParams svlp = new RelativeLayout.LayoutParams(dWidth, dHeight);
		 svlp.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
		 
		 rlayout.addView(sv, svlp );  
		
	}
	
	private  void removeSurfaceView()
	{
		stopPlay();
		rlayout.removeView(sv);
	}
	
	private void removeImageView()
	{
		rlayout.removeView(iv);
	}
	
	private void addImageView()
	{		
		 rlayout.addView(iv, ivlp ); 
	}
	
	private boolean stopPlay() {
		Log.d(TAG, "Mirroring stopPlay"); 
		mThreadrRunningFlag = false;
		return true;
	}	
	
	public void Finish(){
		if(!this.isFinishing())
		this.finish();
	}

	private boolean isMediaCodecExist() {
	    boolean isLibExist = true;
        try {
            Class.forName("android.media.MediaCodec");
        } catch (Exception ex) {
            isLibExist = false;
            ex.printStackTrace();
        }
        
        return isLibExist;
	}	
	
}
