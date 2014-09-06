package com.letv.screenui.activity.tools;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import android.graphics.BitmapFactory;

public class DesUtils {
	public static final String[] music_extensions;
	public static final String[] photo_extensions;
	public static final String[] video_extensions = { ".3gp", ".divx", ".h264",
			".avi", ".m2ts", ".mkv", ".mov", ".mp2", ".mp4", ".mpg", ".mpeg",
			".rm", ".rmvb", ".wmv", ".ts", ".tp", ".dat", ".vob", ".flv",
			".vc1", ".m4v", ".f4v", ".asf", ".lst", ".m2v", ".mts", ".3g2",
			".dv", ".trp" };
	private Cipher decryptCipher = null;
	private Cipher encryptCipher = null;

	static {
		music_extensions = new String[] { ".amr", ".mp3", ".wma", ".m4a",
				".aac", ".ape", ".ogg", ".flac", ".alac", ".wav", ".mid",
				".xmf", ".mka", ".pcm", ".adpcm" };
		photo_extensions = new String[] { ".jpg", ".jpeg", ".bmp", ".tif",
				".tiff", ".png", ".gif", ".giff", ".jfi", ".jpe", ".jif",
				".jfif", ".jps" };
	}

	public DesUtils() throws Exception {
		this("gjaoun");
	}

	public DesUtils(String paramString) throws Exception {
		Key localKey = getKey(paramString.getBytes());
		this.encryptCipher = Cipher.getInstance("DES");
		this.encryptCipher.init(1, localKey);
		this.decryptCipher = Cipher.getInstance("DES");
		this.decryptCipher.init(2, localKey);
	}

	public static String CheckMediaType(String paramString) {
		String str = "";// str = "application/vnd.android.package-archive";
		if (isVideo(paramString))
			str = "video/*";
		if (isMusic(paramString))
			str = "audio/*";
		if (isPhoto(paramString))
			str = "image/*";
		if (isApk(paramString))
			str = "application/*";
		return str;
	}

	public static String getFileName(String pathandname) {
		int start = pathandname.lastIndexOf("/");
		int end = pathandname.lastIndexOf(".");
		if (start != -1 && end != -1) {
			try {
				return URLDecoder.decode(pathandname.substring(start + 1, end),
						"UTF-8");
			} catch (UnsupportedEncodingException e) {
				return null;
			}
		} else {
			return null;
		}
	}

	public static String GetCpuInfo(DesUtils paramDesUtils) {
		Object localObject = null;
		CommandRun localCommandRun = new CommandRun();
		try {
			localObject = localCommandRun.run(new String[] { "/system/bin/cat",
					"/proc/cpuinfo" }, "/system/bin/");
			String str1 = ((String) localObject).toLowerCase();
			localObject = str1;
			try {
				String str2 = paramDesUtils.encrypt((String) localObject);
				return str2;
			} catch (Exception localException) {
				// return (String)localObject;
			}
		} catch (IOException localIOException) {
		}
		return (String) localObject;
	}

	public static String byteArr2HexStr(byte[] paramArrayOfByte)
			throws Exception {
		int i = paramArrayOfByte.length;
		StringBuffer localStringBuffer = new StringBuffer(i * 2);
		for (int j = 0; j < i; ++j) {
			for (int k = paramArrayOfByte[j]; k < 0; k += 256) {
				if (k < 16)
					localStringBuffer.append("0");
				localStringBuffer.append(Integer.toString(k, 16));
			}
		}
		return localStringBuffer.toString();
	}

	private Key getKey(byte[] paramArrayOfByte) throws Exception {
		byte[] arrayOfByte = new byte[8];
		for (int i = 0; (i < paramArrayOfByte.length)
				&& (i < arrayOfByte.length); ++i)
			arrayOfByte[i] = paramArrayOfByte[i];
		return new SecretKeySpec(arrayOfByte, "DES");
	}

	public static byte[] hexStr2ByteArr(String paramString) throws Exception {
		byte[] arrayOfByte1 = paramString.getBytes();
		int i = arrayOfByte1.length;
		byte[] arrayOfByte2 = new byte[i / 2];
		for (int j = 0; j < i; j += 2) {
			String str = new String(arrayOfByte1, j, 2);
			arrayOfByte2[(j / 2)] = (byte) Integer.parseInt(str, 16);
		}
		return arrayOfByte2;
	}

	public static boolean isAmlogicChip() {
		try {
			DesUtils localDesUtils = new DesUtils("gjaoun");
			int i = localDesUtils.decrypt(GetCpuInfo(localDesUtils)).indexOf(
					localDesUtils.decrypt("7c0f13b6d5986e65"));
			int j = 0;
			if (i != -1)
				j = 1;
			return j > 0 ? true : false;
		} catch (Exception localException) {
		}
		return false;
	}

	public static boolean isApk(String paramString) {
		return paramString.toLowerCase().endsWith(".apk");
	}

	public static boolean isMusic(String paramString) {
		String str = paramString.toLowerCase();
		String[] arrayOfString = music_extensions;
		int i = arrayOfString.length;
		for (int j = 0; j < i; ++j)
			if (str.endsWith(arrayOfString[j]))
				return true;
		return false;
	}

	public static boolean isPhoto(String paramString) {
		String str = paramString.toLowerCase();
		String[] arrayOfString = photo_extensions;
		int i = arrayOfString.length;
		for (int j = 0; j < i; ++j)
			if (str.endsWith(arrayOfString[j]))
				return true;
		return false;
	}

	public static boolean isVideo(String paramString) {
		String str = paramString.toLowerCase();
		String[] arrayOfString = video_extensions;
		int i = arrayOfString.length;
		for (int j = 0; j < i; ++j)
			if (str.endsWith(arrayOfString[j]))
				return true;
		return false;
	}

	public static String pathTransferForJB(String paramString) {
		String str = paramString;
		if (paramString.startsWith("/storage/sd")) {
			if (paramString.contains("/storage/sdcard0"))
				str = paramString.replaceFirst("/storage/sdcard0",
						"/mnt/sdcard");
			else
				str = paramString.replaceFirst("/storage/sd", "/mnt/sd");
		}
		return str;
	}

	public static String timeFormatToString(int paramInt) {
		StringBuffer localStringBuffer = new StringBuffer();
		int i = paramInt / 1000;
		int j = i / 3600;
		int l;
		int i1;
		if (j >= 10) {
			localStringBuffer.append(j);
		} else {
			localStringBuffer.append("0").append(j);
		}
		int k = i % 3600;
		l = k / 60;
		if (l < 10) {
			localStringBuffer.append(":0").append(l);
		} else {
			localStringBuffer.append(":").append(l);
		}
		i1 = k % 60;
		if (i1 < 10) {
			localStringBuffer.append(":0").append(i1);
		} else {
			localStringBuffer.append(":").append(i1);
		}
		return localStringBuffer.toString();
	}

	public String decrypt(String paramString) throws Exception {
		return new String(decrypt(hexStr2ByteArr(paramString)));
	}

	public byte[] decrypt(byte[] paramArrayOfByte) throws Exception {
		return this.decryptCipher.doFinal(paramArrayOfByte);
	}

	public String encrypt(String paramString) throws Exception {
		return byteArr2HexStr(encrypt(paramString.getBytes()));
	}

	public byte[] encrypt(byte[] paramArrayOfByte) throws Exception {
		return this.encryptCipher.doFinal(paramArrayOfByte);
	}

	public static int computeSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		int initialSize = computeInitialSampleSize(options, minSideLength,
				maxNumOfPixels);

		int roundedSize;
		if (initialSize <= 8) {
			roundedSize = 1;
			while (roundedSize < initialSize) {
				roundedSize <<= 1;
			}
		} else {
			roundedSize = (initialSize + 7) / 8 * 8;
		}

		return roundedSize;
	}

	private static int computeInitialSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		double w = options.outWidth;
		double h = options.outHeight;

		int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
				.sqrt(w * h / maxNumOfPixels));
		int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(
				Math.floor(w / minSideLength), Math.floor(h / minSideLength));

		if (upperBound < lowerBound) {
			// return the larger one when there is no overlapping zone.
			return lowerBound;
		}

		if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
			return 1;
		} else if (minSideLength == -1) {
			return lowerBound;
		} else {
			return upperBound;
		}
	}

	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			if (width > height) {
				inSampleSize = Math.round((float) height / (float) reqHeight);
			} else {
				inSampleSize = Math.round((float) width / (float) reqWidth);
			}

			// This offers some additional logic in case the image has a strange
			// aspect ratio. For example, a panorama may have a much larger
			// width than height. In these cases the total pixels might still
			// end up being too large to fit comfortably in memory, so we should
			// be more aggressive with sample down the image (=larger
			// inSampleSize).

			final float totalPixels = width * height;

			// Anything more than 2x the requested pixels we'll sample down
			// further.
			final float totalReqPixelsCap = reqWidth * reqHeight * 2;

			while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
				inSampleSize++;
			}
		}

		return inSampleSize;
	}

	static class CommandRun {
		public String run(String[] paramArrayOfString, String paramString)
				throws IOException {
			return "";
		}
	}
}
