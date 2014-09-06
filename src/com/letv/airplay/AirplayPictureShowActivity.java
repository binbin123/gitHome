package com.letv.airplay;

import com.letv.smartControl.R;
import com.letv.upnpControl.tools.LetvLog;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.WindowManager;
import android.widget.ImageView;

/**
 * 图片显示
 * @author 韦念欣
 *
 */
public class AirplayPictureShowActivity extends Activity {  
	
	public static final String TAG = AirplayPictureShowActivity.class.getSimpleName();
	
	private ImageView imageView;
	private final int PICTURE_SHOW = 12399838;
	private Handler handler;
	private String _mPictureId;
	private int _mType;

	public byte[] pictureData;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); 
		super.onCreate(savedInstanceState);
		LetvLog.e(TAG, "PictureShowActivity onCreate");
		setContentView(R.layout.airplay_activity_picture);
		imageView = (ImageView)findViewById(R.id.imageView);
		JniInterface.getInstance().setPictureShow(this);
		
		Intent intent = getIntent();
		
		handler = new Handler(getMainLooper()){
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				System.gc();
				byte[] data = ((PicturePlayerArg) msg.obj).getPicData();
				_mPictureId = ((PicturePlayerArg) msg.obj).getPictureId();
				LetvLog.d(TAG, "airplay pic show id:" + _mPictureId);
				_mType		= ((PicturePlayerArg) msg.obj).getPictureType();
				
				Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
				imageView.setImageBitmap(bitmap);
			}
		};
		//pictureData = intent.getByteArrayExtra("picData");
		showPic(new PicturePlayerArg(JniInterface.getInstance().Data, intent.getStringExtra("pictureId"), intent.getIntExtra("pictureType", 0)));
		
		
	}
 
	@Override
	protected void onPause() {
		super.onPause();
		
		LetvLog.e(TAG, "PictureShowActivity onPause");
		JniInterface.getInstance().setPictureShow(null);
		JniInterface.getInstance().SemaphorePictureRelease();
		
		this.finish();
	}

	@Override
	protected void onStop() {
		LetvLog.e(TAG, "PictureShowActivity onStop");
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		JniInterface.getInstance().setPictureShow(null);
		JniInterface.getInstance().SemaphorePictureRelease();
		JniInterface.getInstance().setPictureState(_mPictureId, _mType);
		LetvLog.e(TAG, "PictureShowActivity onDestroy");
	}
	
	public boolean Finish(String iId)
	{
		LetvLog.d(TAG, "airplay pic stop id:" + iId);
		
		if(_mPictureId == null)
		{
			this.finish();
			return true;			
		}
		
		if(iId == null)
		{
			return false;
		}
		if(iId.equals("all"))
		{
			this.finish();
			return true;			
		}
		if(_mPictureId.equals(iId))
		{
			this.finish();
			return true;
		}
		
		return true;
	}
	/**
	 * 从主线程显示图片
	 */
	public void showPic(PicturePlayerArg arg){
		handler.sendMessage(handler.obtainMessage(PICTURE_SHOW, 0, 0, arg));
		JniInterface.getInstance().SemaphorePictureRelease();
	}
}
