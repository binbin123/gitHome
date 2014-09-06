package com.letv.dmr.asynctask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import org.cybergarage.util.Debug;
import com.letv.dmr.upnp.DesUtils;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.letv.dmr.PictureShowActivity;
import java.io.FileOutputStream;
import java.io.File;
import com.letv.smartControl.R;
import com.letv.upnpControl.tools.Engine;
import com.letv.upnpControl.tools.LetvLog;

/**
 * 
 *	
 */
public class DownloadImageTask extends AsyncTask<String, Integer, Bitmap> {
	private static final String TAG = "DownloadImageTask";
	public static final int MAX_REQUEST_WIDTH = 1024;
	public static final int MAX_REQUEST_HEIGHT = 768;
	private String imageUrl;
	public Bitmap bitmap = null;
	private Context mContext = null;
	public HttpURLConnection conn = null;
	private boolean isConnected = false;
	private InputStream is = null;
	private RelativeLayout image_loading = null;
	private boolean isContinue;
	private int imageSize = 0;
	private TextView loading_title = null;

	public DownloadImageTask(Boolean isContinue, ImageView iv,
			RelativeLayout image_loading, TextView loading_title,
			String imageUrl, Context context) {
		super();
		this.isContinue = isContinue;
		this.imageUrl = imageUrl;
		this.mContext = context;
		this.image_loading = image_loading;
		this.loading_title = loading_title;
		Debug.e("DownloadImageTask", "imageUrl =" + imageUrl);
	}

	protected void onPreExecute() {
		Debug.d(TAG, "onPreExecute");

	}

	@Override
	protected Bitmap doInBackground(String... params) {
		// TODO Auto-generated method stub

		URL myFileUrl = null;
		Debug.d(TAG, "doInBackground");
		Debug.e("DownloadImageTask", "imageUrl =" + imageUrl);
		
		try {
			myFileUrl = new URL(imageUrl);
		
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		// myFileUrl may be nullgPictureActivity
		if (myFileUrl == null) {
			Debug.e("DownloadImageTask", "imageUrl =" + imageUrl);
			return null;
		}

		try {
			conn = (HttpURLConnection) myFileUrl.openConnection();
			Debug.d(TAG, "HttpURLConnection");
			conn.setDoInput(true);
			conn.setConnectTimeout(10 * 1000);
			// conn.setReadTimeout(10*1000);
			conn.connect();
			Debug.d(TAG, "connect");
			if (conn.getResponseCode() == 200) {
				isConnected = true;
				imageSize = conn.getContentLength();
				is = conn.getInputStream();
				if (is == null) {

					Debug.d(TAG, "is == null");
				}
				if (imageSize > 20 * 1024 * 1024) {
					writeFile("temp_image", is);
					String url = Engine.getInstance().getFilePath()
							+ "temp_image";
					File file = new File(url);
					if (file.exists()) {
						BitmapFactory.Options opt = new BitmapFactory.Options();

						opt.inJustDecodeBounds = true;
						opt.inSampleSize = 1;
						BitmapFactory.decodeFile(url, opt);

						Debug.d(TAG, "opt.outHeight = " + opt.outHeight
								+ "outWidth" + opt.outWidth);
						if (opt.outWidth == -1 || opt.outHeight == -1) {

						} else {
							opt.inSampleSize = DesUtils.computeSampleSize(opt,
									-1, MAX_REQUEST_WIDTH * MAX_REQUEST_HEIGHT);
						}
						Debug.d(TAG, "opt.inSampleSize = " + opt.inSampleSize);
						opt.inJustDecodeBounds = false;
						opt.inDither = false;

						bitmap = BitmapFactory.decodeFile(url, opt);
						Debug.d(TAG, "bitmap = " + bitmap);

						file.delete();
					}
					// return bitmap;
				} else {
					getLoadingImage(is, MAX_REQUEST_WIDTH, MAX_REQUEST_HEIGHT);
				}
				// bitmap = BitmapFactory.decodeStream(is);
				Debug.d(TAG, "bitmap = " + bitmap);
				if (is != null)
					is.close();
			}
			conn.disconnect();
			conn = null;

		} catch (IOException e) {
			Debug.d(TAG, "IOException = " + e);
			e.printStackTrace();
			isConnected = false;
		} catch (OutOfMemoryError e1) {
			Debug.d(TAG, "OutOfMemoryError = " + e1);
			System.gc();
			getLoadingImage(is, 128, 128);
			Debug.d(TAG, "OutOfMemoryError bitmap= " + bitmap);
		}
		return bitmap;
	}

	public void writeFile(String fileName, InputStream is) {
		try {

			FileOutputStream fout = mContext.openFileOutput(fileName,
					Context.MODE_PRIVATE);
			byte[] buffer = new byte[1024];
			int len = -1;
			int readDataLength = 0;
			long oldTime = System.currentTimeMillis();
			while ((len = is.read(buffer)) != -1) {
				fout.write(buffer, 0, len);
				readDataLength += len;
				long currentTime = System.currentTimeMillis();
				if (currentTime - oldTime >= 1000 && imageSize > 0) {
					LetvLog.d(TAG, "readDataLength = " + readDataLength);
					int prog = ((int) ((readDataLength / (float) imageSize) * 100));
					publishProgress(prog);
					LetvLog.d(TAG, "publish progress = " + prog);
					oldTime = currentTime;
				}
			}
			fout.close();
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void getLoadingImage(InputStream is, int width, int height) {
		BitmapFactory.Options opt = new BitmapFactory.Options();
		byte[] arrayOfByte = null;
		try {
			arrayOfByte = readStream(is);
		} catch (Exception e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			if (bitmap != null) {
				bitmap.recycle();
				bitmap = null;
			}
			Debug.d(TAG, "readStream failed " + opt.inSampleSize);
			return;
		}
		if (arrayOfByte != null) {
			Debug.d(TAG, "arrayOfByte length = " + arrayOfByte.length);

			opt.inJustDecodeBounds = true;
			opt.inSampleSize = 1;
			BitmapFactory.decodeByteArray(arrayOfByte, 0, arrayOfByte.length,
					opt);
			// BitmapFactory.decodeStream(is,null,opt);

			Debug.d(TAG, "opt.outHeight = " + opt.outHeight + "outWidth"
					+ opt.outWidth);
			if (opt.outWidth == -1 || opt.outHeight == -1) {

			} else {
				opt.inSampleSize = DesUtils.computeSampleSize(opt, -1, width
						* height);
			}
			Debug.d(TAG, "opt.inSampleSize = " + opt.inSampleSize);
			opt.inJustDecodeBounds = false;
			opt.inDither = false;
			if (bitmap != null) {
				bitmap.recycle();
				bitmap = null;
			}
			bitmap = BitmapFactory.decodeByteArray(arrayOfByte, 0,
					arrayOfByte.length, opt); // BitmapFactory.decodeStream(is,null,opt);
			Debug.d(TAG, "getLoadingImage bitmap = " + bitmap);
		} else {
			bitmap = null;
		}
	}

	public byte[] readStream(InputStream in) throws Exception, OutOfMemoryError {
		Debug.d(TAG, "readStream begin");
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = -1;
		int readDataLength = 0;
		long oldTime = System.currentTimeMillis();
		while ((len = in.read(buffer)) != -1) {
			outputStream.write(buffer, 0, len);

			readDataLength += len;
			long currentTime = System.currentTimeMillis();
			if (currentTime - oldTime >= 1000 && imageSize > 0) {
				LetvLog.d(TAG, "readDataLength = " + readDataLength);
				int prog = ((int) ((readDataLength / (float) imageSize) * 100));
				publishProgress(prog);
				LetvLog.d(TAG, "publish progress = " + prog);
				oldTime = currentTime;
			}
		}
		outputStream.close();
		in.close();

		Debug.d(TAG, "readStream end");
		return outputStream.toByteArray();
	}

	protected void onProgressUpdate(Integer... progress) {

		loading_title.setText(mContext.getString(R.string.pic_loading_txt)
				+ progress[0] + "%");

	}

	protected void onPostExecute(Bitmap result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		Debug.d(TAG, "onPostExecute result =" + result);
		if (image_loading != null)
			image_loading.setVisibility(View.INVISIBLE);
		if (result != null) {

			if (((PictureShowActivity) mContext).mUiHandler != null
					&& isContinue == false)
				((PictureShowActivity) mContext).mUiHandler
						.sendEmptyMessage(PictureShowActivity.SHOW_IMAGE);
			else {
				((PictureShowActivity) mContext).mUiHandler
						.sendEmptyMessage(PictureShowActivity.SHOW_IMAGE_TWO);
			}

			Debug.d(TAG, "iv.setImageBitmap success !");
		} else {
			if (!isConnected) {
				Toast.makeText(mContext,
						mContext.getString(R.string.connect_server_failed),
						Toast.LENGTH_SHORT).show();
			} else {
				Debug.d(TAG, "out of memory ,finish !");
				Toast.makeText(mContext,
						mContext.getString(R.string.download_image_failed),
						Toast.LENGTH_SHORT).show();
			}
			((PictureShowActivity) mContext).finish();
		}
	}

}
