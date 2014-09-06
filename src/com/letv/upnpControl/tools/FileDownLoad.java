package com.letv.upnpControl.tools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import java.io.FileOutputStream;


public class FileDownLoad extends AsyncTask<String, Integer, Void> {

	private static final String TAG = "FileDownLoad";
	private Context mContext = null;
	public HttpURLConnection conn = null;
	private InputStream is = null;
	private String mMimeType = null;
	private String mFileName = null;
    public FileDownLoad(Context context){
    	mContext = context;
    }
    public FileDownLoad(){

    }
	protected void onPreExecute() {
		LetvLog.d(TAG, "onPreExecute");
	}

	@Override
	protected Void doInBackground(String... params) {
		// TODO Auto-generated method stub

		URL myFileUrl = null;
		int count = params.length;
		LetvLog.d(TAG, "count= " + count);
         
		String url = params[0];
		mFileName = params[1];
        if(params.length == 3){
        	mMimeType = params[2];
        }
		LetvLog.d(TAG, "doInBackground url=" + url + "filename =" + mFileName + 
				"mMimeType =" + mMimeType);
		try {
			myFileUrl = new URL(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		// myFileUrl may be null
		if (myFileUrl == null) {
			LetvLog.e(TAG, "myFileUrl =" + myFileUrl);
			return null;
		}

		try {
			conn = (HttpURLConnection) myFileUrl.openConnection();
			LetvLog.d(TAG, "HttpURLConnection");
			conn.setDoInput(true);
			conn.setConnectTimeout(10 * 1000);
			// conn.setReadTimeout(10*1000);
			conn.connect();

			if (conn.getResponseCode() == 200) {
				LetvLog.d(TAG, "connect");
				int fileSize = conn.getContentLength();
				is = conn.getInputStream();
				if (is == null) {

					LetvLog.d(TAG, "is == null");
				}

				writeFile(mFileName, is, fileSize);

				if (is != null)
					is.close();
			}
			conn.disconnect();
			conn = null;

		} catch (IOException e) {
			LetvLog.d(TAG, "IOException = " + e);
			e.printStackTrace();
		}
		return null;
	}

	public void writeFile(String fileName, InputStream is, int fileSize) {
		try {
			String path = LetvUtils.getSDPath() + fileName;
			LetvLog.d(TAG, "writeFile begin file path = " + path);
			File file = new File(path);
			
			FileOutputStream fout = new FileOutputStream(file);

			byte[] buffer = new byte[1024];
			int len = -1;
			//int readDataLength = 0;
			while ((len = is.read(buffer)) != -1) {
				fout.write(buffer, 0, len);
//				readDataLength += len;
//				if(readDataLength > 1*1024*1024 && !mIsPLay){
//					mIsPLay = true;
//					LetvLog.d(TAG, "writeFile readDataLength = " + readDataLength);
//					Intent installIntent = new Intent();
//					installIntent
//							.setAction("ACTION_PLAY_AUDIO");
//					installIntent.putExtra("FILENAME", fileName);
//					mContext.sendBroadcast(installIntent);
//				}
				
			}
		//	LetvLog.d(TAG, "currentTime = " + System.currentTimeMillis());
			//publishProgress(100);
			fout.close();
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void onProgressUpdate(Integer... progress) {

	}

	protected void onPostExecute(Void result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		if(mMimeType.contains("apk")){
			
			String fileName = LetvUtils.getSDPath() + mFileName;
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(new File(fileName)),
					"application/vnd.android.package-archive");
			mContext.startActivity(intent);
		}
	}
}
