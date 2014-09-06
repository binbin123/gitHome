package com.letv.screenui.activity.tools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import com.letv.upnpControl.tools.LetvLog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

/**
 * 
 *	
 */
public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
	private static final String TAG = "DownloadImageTask";
	public static final int MAX_REQUEST_WIDTH = 1024;
	public static final int MAX_REQUEST_HEIGHT = 768;
	public static boolean downloadSuccessful = false;
	private static boolean save_iphone, save_android;
	private ImageView iv;
	private String imageUrl;
	public Bitmap bitmap = null;
	private Context mContext = null;
	public HttpURLConnection conn = null;
	private InputStream is = null;
	private String flag;

	public DownloadImageTask(ImageView iv, String imageUrl, Context context,
			String flag) {
		super();
		this.iv = iv;
		this.imageUrl = imageUrl;
		this.mContext = context;
		this.flag = flag;
		LetvLog.e("DownloadImageTask", "imageUrl =" + imageUrl);
	}

	protected void onPreExecute() {
	}

	@Override
	protected Bitmap doInBackground(String... params) {
		URL myFileUrl = null;
		LetvLog.e("DownloadImageTask", "do inbackground imageUrl =" + imageUrl);
		try {
			myFileUrl = new URL(imageUrl);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		if (myFileUrl == null) {
			LetvLog.e("DownloadImageTask", "imageUrl =" + imageUrl);
			return null;
		}

		try {
			conn = (HttpURLConnection) myFileUrl.openConnection();
			conn.setDoInput(true);
			conn.setConnectTimeout(10 * 1000);
			conn.connect();
			if (conn.getResponseCode() == 200) {
				is = conn.getInputStream();
				if (is == null) {

				}
				getLoadingImage(is, MAX_REQUEST_WIDTH, MAX_REQUEST_HEIGHT);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				if (bitmap != null) {
					bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
				}
				InputStream sbs = new ByteArrayInputStream(baos.toByteArray());
				if (flag.equals("iphone")) {
					saveIphoneFile(sbs);
				} else {
					saveAndroidFile(sbs);
				}
				if (save_iphone && save_android) {
					downloadSuccessful = true;
					LetvLog.d(TAG, "Save images successful!");
				}
				if (is != null) {
					is.close();
					is = null;
				}
				if (sbs != null) {
					sbs.close();
					sbs = null;
				}
			}
			conn.disconnect();
			conn = null;

		} catch (IOException e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e1) {
			System.gc();
			getLoadingImage(is, 128, 128);
		}
		return bitmap;
	}

	public void getLoadingImage(InputStream is, int width, int height) {
		BitmapFactory.Options opt = new BitmapFactory.Options();
		byte[] arrayOfByte = null;
		try {
			arrayOfByte = readStream(is);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (arrayOfByte != null) {
			opt.inJustDecodeBounds = true;
			opt.inSampleSize = 1;
			BitmapFactory.decodeByteArray(arrayOfByte, 0, arrayOfByte.length,
					opt);
			if (opt.outWidth == -1 || opt.outHeight == -1) {

			} else {
				opt.inSampleSize = DesUtils.computeSampleSize(opt, -1, width
						* height);
			}
			opt.inJustDecodeBounds = false;
			opt.inDither = false;

			bitmap = BitmapFactory.decodeByteArray(arrayOfByte, 0,
					arrayOfByte.length, opt);
		} else {
			bitmap = null;
		}
	}

	public static byte[] readStream(InputStream in) throws Exception {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = -1;
		while ((len = in.read(buffer)) != -1) {
			outputStream.write(buffer, 0, len);
		}
		outputStream.close();
		return outputStream.toByteArray();
	}

	protected void onPostExecute(Bitmap result) {
		super.onPostExecute(result);
		if (result != null) {
			if (iv != null)
				iv.setImageBitmap(result);
		} else {
			// if (!isConnected) {
			// Toast.makeText(mContext, "connect server failed",
			// Toast.LENGTH_SHORT).show();
			// } else {
			// // Toast.makeText(mContext, "Memory is lower",
			// // Toast.LENGTH_SHORT).show();
			// }
		}
	}

	public void saveIphoneFile(InputStream in) {
		try {
			FileOutputStream fOutputStream = mContext.openFileOutput(
					"iphone.jpg", Context.MODE_PRIVATE);
			byte[] buffer = new byte[1024];
			int len = -1;
			while ((len = in.read(buffer)) != -1) {
				fOutputStream.write(buffer, 0, len);
			}
			fOutputStream.flush();
			fOutputStream.close();
			save_iphone = true;
		} catch (IOException e) {
			e.printStackTrace();
			save_iphone = false;
		}

	}

	public void saveAndroidFile(InputStream in) {
		try {
			FileOutputStream fOutputStream = mContext.openFileOutput(
					"android.jpg", Context.MODE_PRIVATE);
			byte[] buffer = new byte[1024];
			int len = -1;
			while ((len = in.read(buffer)) != -1) {
				fOutputStream.write(buffer, 0, len);
			}
			fOutputStream.flush();
			fOutputStream.close();
			save_android = true;
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
			save_android = false;
		}

	}

}