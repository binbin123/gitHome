package com.letv.upnpControl.tools;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import java.io.FileOutputStream;
import java.io.File;
import com.letv.smartControl.R;
import com.letv.upnpControl.receiver.InstallQuietReceiver;
import com.letv.upnpControl.ui.ShowApkInstallProgress;

/**
 *
 *   
 */
public class DownloadUtil extends AsyncTask<String, Integer, Void> {
	private static final String TAG = "DownloadUtil";

	private String mUrl;
	private Context mContext = null;
	public HttpURLConnection conn = null;
	private boolean isConnected = false;
	private InputStream is = null;
	private SeekBar mDownloadProgressBar = null;
	private String mAppName = "";
	private RelativeLayout mDownloadView = null;
	private TextView mDownloadProgressTitleView = null;
	private TextView mDownloadTitleView = null;
	public RelativeLayout mInstallView = null;
	public RelativeLayout mInstalledView = null;
	public TextView mInstalledTextView = null;


	public DownloadUtil(Context context, String appName) {
		super();
		mContext = context;
		mAppName = appName;
		initView();

	}

	private void initView() {
		Activity activity = (ShowApkInstallProgress) mContext;
		mDownloadView = (RelativeLayout) activity.findViewById(R.id.download);
		mDownloadProgressBar = (SeekBar) activity
				.findViewById(R.id.progressbar_download);
		mDownloadTitleView = (TextView) activity.findViewById(R.id.title);
		mDownloadProgressTitleView = (TextView) activity
				.findViewById(R.id.progress_download);
		mInstallView = (RelativeLayout) activity.findViewById(R.id.install);
		mInstalledView = (RelativeLayout) activity.findViewById(R.id.installed);
		mInstalledTextView = (TextView) activity
				.findViewById(R.id.title_installed);
	}

	protected void onPreExecute() {
		mDownloadTitleView.setText(mContext.getString(R.string.download) + " "+ mAppName);
		LetvLog.d(TAG, "onPreExecute");
	}

	@Override
	protected Void doInBackground(String... params) {
		// TODO Auto-generated method stub

		URL myFileUrl = null;
		int count = params.length;
		LetvLog.d(TAG, "count= " + count);
		if (count < 3) {
			return null;
		}
		String url = params[0];
		String appName = params[1];
		String fileName = params[1] + ".apk";
		String packageName = params[2];
		LetvLog.d(TAG, "doInBackground url=" + url + "filename =" + fileName);
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
				isConnected = true;
				int fileSize = conn.getContentLength();
				is = conn.getInputStream();
				if (is == null) {

					LetvLog.d(TAG, "is == null");
				}
			
				writeFile(fileName, is, fileSize);

				if (is != null)
					is.close();
			}
			conn.disconnect();
			conn = null;
			/* install apk */
			String fileUrl = Engine.getInstance().getFilePath() + fileName;
			File file = new File(fileUrl);
			if (file.exists()) {
				Intent installIntent = new Intent();
				installIntent
						.setAction(InstallQuietReceiver.INSTALL_PACKAGE_QUIET);
				installIntent.setData(Uri.fromFile(file));
				installIntent.putExtra("INSTALL_PACKAGE_NAME", packageName);
				installIntent.putExtra("INSTALL_APP_NAME", appName);
				
				LetvLog.d(TAG, "begin install apk = " + fileName);
				mContext.sendBroadcast(installIntent);
			}

		} catch (IOException e) {
			LetvLog.d(TAG, "IOException = " + e);
			e.printStackTrace();
			isConnected = false;
		}
		return null;
	}

	public void writeFile(String fileName, InputStream is, int fileSize) {
		try {
            
			LetvLog.d(TAG, "writeFile begin file name = " + fileName);
			FileOutputStream fout = mContext.openFileOutput(fileName,
					mContext.MODE_WORLD_READABLE
							| mContext.MODE_WORLD_WRITEABLE);

			byte[] buffer = new byte[1024];
			int len = -1;
			int readDataLength = 0;
			long oldTime = System.currentTimeMillis();
			LetvLog.d(TAG, "oldTime = " + oldTime);
			while ((len = is.read(buffer)) != -1) {
				fout.write(buffer, 0, len);
				readDataLength += len;
				long currentTime = System.currentTimeMillis();
				if (currentTime - oldTime >= 1000) {
					LetvLog.d(TAG, "readDataLength = " + readDataLength);
					LetvLog.d(TAG, "fileSize = " + fileSize);
					int prog = ((int) ((readDataLength / (float) fileSize) * 100));
					publishProgress(prog);
					LetvLog.d(TAG, "publish progress = " + prog);
					oldTime = currentTime;
				}
			}
			LetvLog.d(TAG, "currentTime = " + System.currentTimeMillis());
			publishProgress(100);
			fout.close();
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void onProgressUpdate(Integer... progress) {
		mDownloadProgressBar.setProgress(progress[0]);
		mDownloadProgressTitleView.setText(mContext
				.getString(R.string.downloaded) + progress[0] + "%");
	}

	protected void onPostExecute(Void result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		{
			if (!isConnected) {
				Toast.makeText(mContext,
						mContext.getString(R.string.install_failed),
						Toast.LENGTH_SHORT).show();
				((ShowApkInstallProgress) mContext).finish();
			} else {
				mDownloadView.setVisibility(View.INVISIBLE);
				mInstallView.setVisibility(View.VISIBLE);

			}
		}
	}
}
