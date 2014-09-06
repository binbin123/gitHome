package com.letv.upnpControl.tools;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Drawable;

/**
 * @title: 閸ュ墽澧栧銉ュ徔缁拷 * @description:
 * @company: 娑旀劘顬呯純鎴滀繆閹垱濡ч張顖ょ礄閸栨ぞ鍚敍澶庡亗娴犺姤婀侀梽鎰彆閸欙拷
 * @author 娴滃骸娑垫Λ锟� * @version 1.0
 * @created 2011-8-3 娑撳宕�:19:37
 * @changeRecord
 */
public class ImageUtil {

	/**
	 * 閺�儳銇囩紓鈺佺毈閸ュ墽澧� *
	 * 
	 * @param bitmap
	 * @param w
	 * @param h
	 * @return
	 */
	public static Bitmap zoomBitmap(Bitmap bitmap, int w, int h) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		Matrix matrix = new Matrix();
		float scaleWidht = ((float) w / width);
		float scaleHeight = ((float) h / height);
		matrix.postScale(scaleWidht, scaleHeight);
		Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, width, height,
				matrix, true);
		return newbmp;
	}

	/**
	 * 鐏忓挷rawable鏉烆剙瀵叉稉绡塱tmap
	 * 
	 * @param drawable
	 * @return
	 */
	public static Bitmap drawableToBitmap(Drawable drawable) {
		int width = drawable.getIntrinsicWidth();
		int height = drawable.getIntrinsicHeight();
		Bitmap bitmap = Bitmap.createBitmap(width, height, drawable
				.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
				: Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, width, height);
		drawable.draw(canvas);
		return bitmap;
	}

	/**
	 * 閼惧嘲绶遍幓鎰仛濞戝牊浼呴惃鍕箒娑撳﹨顬戦惃鍕禈閻楋拷 * @param bitmap
	 * 
	 * @param roundPx
	 * @return
	 */
	public static Bitmap getTipNumBitmap(Bitmap bitmap, String num) {
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		Paint paint = new Paint();
		paint.setAntiAlias(true);

		canvas.drawBitmap(bitmap, 0, 0, paint);

		paint.setColor(Color.WHITE);
		paint.setTextAlign(Paint.Align.CENTER);
		paint.setTextSize(20);
		canvas.drawText(num, bitmap.getWidth() / 2, bitmap.getHeight() / 2 + 5,
				paint);

		bitmap.recycle();
		return output;
	}

	/**
	 * 閼惧嘲绶遍崷鍡氼瀾閸ュ墽澧栭惃鍕煙濞夛拷 * @param bitmap
	 * 
	 * @param roundPx
	 * @return
	 */
	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx) {

		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		bitmap.recycle();

		return output;
	}

	/**
	 * 閼惧嘲绶辩敮锕�拷瑜拌京娈戦崶鍓у閺傝纭� *
	 * 
	 * @param bitmap
	 * @return
	 */
	public static Bitmap createReflectionImageWithOrigin(Bitmap bitmap) {
		final int reflectionGap = 0;// 娑撳秷顪呮稉顓㈡？閻ㄥ嫬鍨庨梾锟�
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();

		Matrix matrix = new Matrix();
		matrix.preScale(1, -1);

		Bitmap reflectionImage = Bitmap.createBitmap(bitmap, 0, height * 3 / 4,
				width, height / 4, matrix, false);
		Bitmap bitmapWithReflection = Bitmap.createBitmap(width,
				(height + height / 4), Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmapWithReflection);
		canvas.drawBitmap(bitmap, 0, 0, null);// 缂佹ê鍩楅崢鐔锋禈
		Paint deafalutPaint = new Paint();
		canvas.drawRect(0, height, width, height + reflectionGap, deafalutPaint);// 缂佹ê鍩楅崚鍡涙缁撅拷
																					// canvas.drawBitmap(reflectionImage,
																					// 0,
																					// height
																					// +
																					// reflectionGap
																					// +
																					// 3,
																					// null);

		Paint paint = new Paint();
		LinearGradient shader = new LinearGradient(0, bitmap.getHeight(), 0,
				bitmapWithReflection.getHeight() + reflectionGap, 0x60ffffff,
				0x00ffffff, TileMode.CLAMP);
		paint.setShader(shader);

		// Set the Transfer mode to be porter duff and destination in
		paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));

		// Draw a rectangle using the paint with our linear gradient
		canvas.drawRect(0, height, width, bitmapWithReflection.getHeight()
				+ reflectionGap + 3, paint);
		return bitmapWithReflection;
	}

	/**
	 * 鐏忓棗鍍甸崶鎯ь樀閻炲棔璐熼悘鏉垮閸ワ拷 * @param bitmap
	 * 
	 * @return
	 */
	public static Bitmap convertGrayImg(Bitmap bitmap) {

		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		int[] pix = new int[width * height];
		bitmap.getPixels(pix, 0, width, 0, 0, width, height);
		int alpha = 0xFF << 24;
		int color, red, green, blue;

		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				color = pix[width * i + j];
				red = ((color & 0x00FF0000) >> 16);
				green = ((color & 0x0000FF00) >> 8);
				blue = color & 0x000000FF;
				color = (red + green + blue) / 3;
				color = alpha | (color << 16) | (color << 8) | color;
				pix[width * i + j] = color;
			}
		}
		Bitmap result = Bitmap.createBitmap(width, height, Config.RGB_565);
		result.setPixels(pix, 0, width, 0, 0, width, height);
		return result;
	}

	/**
	 * 鐠佸墽鐤嗛崶鍓у濡紕纭� *
	 * 
	 * @param bmpSource
	 * @param Blur
	 * @return
	 */
	public static Bitmap SetBlur(Bitmap bmpSource, int Blur) { // 濠ф劒缍呴崶鎾呯礉濡紕纭﹀鍝勫
		int pixels[] = new int[bmpSource.getWidth() * bmpSource.getHeight()];
		// 妫版粏澹婇弫鎵矋閿涘奔绔存稉顏勫剼缁辩姴顕惔鏂剧娑擃亜鍘撶槐锟�
		int pixelsRawSource[] = new int[bmpSource.getWidth()
				* bmpSource.getHeight() * 3];
		// 娑撳甯懝鍙夋殶缂佸嫸绱濇担婊�礋閸忓啯鏆熼幑顕嗙礉閸︺劍鐦℃稉锟界湴濡紕纭﹀鍝勫閻ㄥ嫭妞傞崐娆庣瑝閸欘垱娲块弨锟�
		int pixelsRawNew[] = new int[bmpSource.getWidth()
				* bmpSource.getHeight() * 3];
		// 娑撳甯懝鍙夋殶缂佸嫸绱濋幒銉ュ綀鐠侊紕鐣绘潻鍥╂畱娑撳甯懝鎻掞拷
		bmpSource.getPixels(pixels, 0, bmpSource.getWidth(), 0, 0,
				bmpSource.getWidth(), bmpSource.getHeight());
		// 閼惧嘲褰囬崓蹇曠閻愶拷 // 濡紕纭﹀鍝勫閿涘本鐦″顏嗗箚娑擄拷顐煎鍝勫婢х偛濮炴稉锟筋偧
		for (int k = 1; k <= Blur; k++) {
			// 娴犲骸娴橀悧鍥﹁厬閼惧嘲褰囧В蹇庨嚋閸嶅繒绀屾稉澶婂斧閼硅尙娈戦崐锟�
			for (int i = 0; i < pixels.length; i++) {
				pixelsRawSource[i * 3 + 0] = Color.red(pixels[i]);
				pixelsRawSource[i * 3 + 1] = Color.green(pixels[i]);
				pixelsRawSource[i * 3 + 2] = Color.blue(pixels[i]);
			}
			// 閸欐牗鐦℃稉顏嗗仯娑撳﹣绗呭锕�礁閻愬湱娈戦獮鍐叉綆閸婇棿缍旈懛顏勭箒閻ㄥ嫬锟�
			int CurrentPixel = bmpSource.getWidth() * 3 + 3;
			// 瑜版挸澧犳径鍕倞閻ㄥ嫬鍎氱槐鐘靛仯閿涘奔绮犻悙锟�,2)瀵拷顬�
			for (int i = 0; i < bmpSource.getHeight() - 3; i++)
			// 妤傛ê瀹冲顏嗗箚
			{
				for (int j = 0; j < bmpSource.getWidth() * 3; j++)
				// 鐎硅棄瀹冲顏嗗箚
				{
					CurrentPixel += 1;
					// 閸欐牔绗傛稉瀣箯閸欑绱濋崣鏍ч挬閸у洤锟�
					int sumColor = 0; // 妫版粏澹婇崪锟�
					sumColor = pixelsRawSource[CurrentPixel
							- bmpSource.getWidth() * 3]; // 娑撳﹣绔撮悙锟�
					sumColor = sumColor + pixelsRawSource[CurrentPixel - 3]; // 瀹革缚绔撮悙锟�
					sumColor = sumColor + pixelsRawSource[CurrentPixel + 3]; // 閸欏厖绔撮悙锟�
					sumColor = sumColor
							+ pixelsRawSource[CurrentPixel
									+ bmpSource.getWidth() * 3]; // 娑撳绔撮悙锟�
					pixelsRawNew[CurrentPixel] = Math.round(sumColor / 4); // 鐠佸墽鐤嗛崓蹇曠閻愶拷
				}
			}
			// 鐏忓棙鏌婃稉澶婂斧閼硅尙绮嶉崥鍫熷灇閸嶅繒绀屾０婊嗗
			for (int i = 0; i < pixels.length; i++) {
				pixels[i] = Color.rgb(pixelsRawNew[i * 3 + 0],
						pixelsRawNew[i * 3 + 1], pixelsRawNew[i * 3 + 2]);
			}
		}
		// 鎼存梻鏁ら崚鏉挎禈閸嶏拷
		Bitmap bmpReturn = Bitmap.createBitmap(bmpSource.getWidth(),
				bmpSource.getHeight(), Config.ARGB_8888);
		bmpReturn.setPixels(pixels, 0, bmpSource.getWidth(), 0, 0,
				bmpSource.getWidth(), bmpSource.getHeight());
		// 韫囧懘銆忛弬鏉跨紦娴ｅ秴娴橀悞璺烘倵婵夘偄鍘栭敍灞肩瑝閼崇晫娲块幒銉ワ綖閸忓懏绨崶鎯у剼閿涘苯鎯侀崚娆忓敶鐎涙ɑ濮ら柨锟�
		return bmpReturn;
	}
}