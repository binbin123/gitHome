package com.letv.update;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.http.HttpStatus;
import com.letv.smartControl.R;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


public class UpdateDialogActivity extends Activity implements OnClickListener {

	private UpdateData updateData;
	private TextView textUpdateVersion;
	private TextView textUpdateNote;
	private Button buttonUpdate;
	private Button buttonCancelUpdate;
	private Button buttonStopUpdate;
	private ProgressBar progressBar;
	private TextView textPercentage;	
	private File apkFile;
	private boolean stopDownload;
	private final static int CONNECTION_TIMEOUT = 30000;
	private final static int READ_TIMEOUT = CONNECTION_TIMEOUT;
	
	private final static int PROGRESS_SET_MAX = 0;
	private final static int PROGRESS_SET_PROGRESS = 1;
	private final static int UPDATE_FAIL_SERVER = 2;
	private final static int UPDATE_FAIL_CLIENT = 3;
	
	private static final int UPDATE_NOT = 0;
	private static final int UPDATE_OPTIONAL = 1;
	private static final int UPDATE_FORCE = 2;
	
	private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what){
			case PROGRESS_SET_MAX:
				progressBar.setMax(msg.arg2);
				break;
			case PROGRESS_SET_PROGRESS:
				progressBar.setProgress(msg.arg1);
				int percentage = (int) (100.0f * msg.arg1 / msg.arg2);
				textPercentage.setText(percentage + "%");
				break;
			case UPDATE_FAIL_SERVER:
				Toast.makeText(UpdateDialogActivity.this, R.string.update_fail_server, Toast.LENGTH_SHORT).show();
				break;
			case UPDATE_FAIL_CLIENT:
				Toast.makeText(UpdateDialogActivity.this, R.string.update_fail_client, Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		updateData = (UpdateData) getIntent().getExtras().get("updateInfo");


		requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏


		showNodeLayout();
	}
	
	@Override
	protected void onDestroy() {
		
		// 关闭服务
		Intent intent = new Intent(this, UpdateService.class);
		stopService(intent);
		
		super.onDestroy();
	}

	/**
	 * 显示更新日志对话框
	 */
	public void showNodeLayout() {
	    
		setContentView(R.layout.control_activity_update_note);
		textUpdateVersion = (TextView) findViewById(R.id.text_update_version);
		textUpdateNote = (TextView) findViewById(R.id.text_update_note);
		buttonUpdate = (Button) findViewById(R.id.button_update_now);
		buttonCancelUpdate = (Button) findViewById(R.id.button_update_not);
		textUpdateVersion.setText(getResources().getText(R.string.update_version)+ updateData.getVersion());
		textUpdateNote.setText(getResources().getText(R.string.update_info)+ updateData.getNote());		
		buttonUpdate.setOnClickListener(this);
		buttonCancelUpdate.setOnClickListener(this);
		buttonUpdate.setFocusable(true);
		buttonUpdate.setFocusableInTouchMode(true);
		buttonUpdate.requestFocus();
		buttonUpdate.requestFocusFromTouch();
		if (updateData.getCommand() == UPDATE_FORCE){
			buttonCancelUpdate.setEnabled(false);
			buttonCancelUpdate.setTextColor(Color.rgb(139, 139, 139));
		}

	}

	/**
	 * 显示更新进度条对话框
	 */
	private void showUpdateLayout() {
		setContentView(R.layout.control_activity_update_download);

		progressBar = (ProgressBar) findViewById(R.id.progressbar_download);
		textPercentage = (TextView) findViewById(R.id.text_percentage);
		buttonStopUpdate = (Button) findViewById(R.id.button_update_stop);

		buttonStopUpdate.setOnClickListener(this);
		if (updateData.getCommand() == UPDATE_FORCE){
			buttonStopUpdate.setEnabled(false);
			buttonStopUpdate.setTextColor(Color.rgb(139, 139, 139));
		}

		downloadApk();
	}
	
	@Override
	public void onClick(View v) {
		if (v == buttonUpdate) {				// 升级按钮
			showUpdateLayout();
			stopDownload = false;
		} else if (v == buttonCancelUpdate) {	// 取消升级按钮
			stopDownload = true;
//			ControlActivity.showUpdateSup = true;
			finish();
		} else if (v == buttonStopUpdate){		// 停止下载按钮
			stopDownload = true;
//			ControlActivity.showUpdateSup = true;
			finish();
		}
	}

	/**
	 * 启动新线程下载APK
	 */
	private void downloadApk() {
		new downloadApkThread().start();
	}

	/**
	 * 下载文件线程
	 */
	private class downloadApkThread extends Thread {
		@Override
		public void run() {
			File file = null;
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				String path = Environment.getExternalStorageDirectory()+ File.separator + "letv" + File.separator;
				file = new File(path);
				if (!file.exists()) {
					file.mkdirs();
				}
			}else{
				handler.sendEmptyMessage(UPDATE_FAIL_CLIENT);
				finish();
				return;
			}
			
			HttpURLConnection connection = null;
			try {
				URL url = new URL(updateData.getUrl());
				connection = (HttpURLConnection) url.openConnection();
				connection.setConnectTimeout(CONNECTION_TIMEOUT);
				connection.setReadTimeout(READ_TIMEOUT);
				InputStream is = connection.getInputStream();
				if (connection.getResponseCode() ==  HttpStatus.SC_OK){
					int length = connection.getContentLength();
					handler.sendMessage(handler.obtainMessage(PROGRESS_SET_MAX, 0, length));
					apkFile = new File(file, updateData.getApkFileName());
					FileOutputStream fos = new FileOutputStream(apkFile);
					byte buffer[] = new byte[2048];
					int len = 0, count = 0;
					while ((len = is.read(buffer, 0, buffer.length)) != -1 && !stopDownload){
						fos.write(buffer, 0, len);
						handler.sendMessage(handler.obtainMessage(PROGRESS_SET_PROGRESS, count+=len, length));
					}
					is.close();
					fos.close();
					
					if (stopDownload){
						file.delete();
					}else{
						installApk();
					}
				}else{
					handler.sendEmptyMessage(UPDATE_FAIL_SERVER);
				}
			}catch (IOException e) {
				e.printStackTrace();
				handler.sendEmptyMessage(UPDATE_FAIL_CLIENT);
			}finally{
				if (connection != null)
				connection.disconnect();
			}
			finish();
		}
	};

	/**
	 * 安装APK文件
	 */
	private void installApk() {

		if (apkFile == null || !apkFile.exists()) {
			return;
		}
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setDataAndType(Uri.parse("file://" + apkFile.getAbsolutePath()), "application/vnd.android.package-archive");
		startActivityForResult(intent, 0);	// 处理用户在安装应用的界面点击“取消安装”的操作
//		ControlActivity.showUpdateSup = true;
	}
	
	/**
	 * 处理用户在安装应用的界面点击“取消安装”的操作
	 * 效果为：如果为强制升级指令，则让SmartControlActivity重新执行其声明周期的方法
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		ControlActivity.showUpdateSup = true;
		if (updateData.getCommand() == UPDATE_FORCE){
			android.os.Process.killProcess(android.os.Process.myPid());
		}
	}
	
/*	*//**
	 * 防止应用按返回键退出升级操作
	 *//*
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return true;
	}*/
}
