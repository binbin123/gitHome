package com.letv.dmr;

import org.cybergarage.util.Debug;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ImageView;

public class VolumeProgressBar extends ImageView {
  private static final String TAG = "VolumeProgressBar";
	private Paint mProgressPaint;
	private Paint mDefaultPaint;
	private int progressWidth = 0;	

	private int max = 10;
	private int progress = 5;	

	public void setVolumeColor(int a, int r, int g, int b) {
		mProgressPaint.setARGB(a, r, g, b);
	}
  public void setMaxVolume(int max) {
    this.max = max;
  }
	public void setVolumeBgColor(int a, int r, int g, int b) {
		mDefaultPaint.setARGB(a, r, g, b);
	}

	public VolumeProgressBar(Context context) {
		super(context);
		init();
	}

	public VolumeProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs, 0);
		init();
	}

	public VolumeProgressBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		mProgressPaint = new Paint();
		mProgressPaint.setAntiAlias(true);
		mProgressPaint.setARGB(255, 0, 160, 233);

		mDefaultPaint = new Paint();
		mDefaultPaint.setAntiAlias(true);
		mDefaultPaint.setARGB(255, 102, 102, 102);
	}

	@Override
	protected void onDraw(Canvas canvas) {
	  Debug.d(TAG, "onDraw  progress =" + progress);
		super.onDraw(canvas);
		if (progressWidth <= 0) {
			progressWidth = getWidth() / (2 * max - 1);
		}
		for (int i = 0; i < max; i++) {
			Rect rect = new Rect(i * 2 * progressWidth, 0, i * 2 * progressWidth + progressWidth, getHeight());
			if (i < progress) {
				canvas.drawRect(rect, mProgressPaint);
			} else {
				canvas.drawRect(rect, mDefaultPaint);
			}
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
	}

	private int measureWidth(int measureSpec) {
		int result = 0;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);

		if (specMode == MeasureSpec.EXACTLY) {
			result = specSize;
		} else {
			if (specMode == MeasureSpec.AT_MOST) {
				result = Math.min(result, specSize);
			}
		}

		return result;
	}

	private int measureHeight(int measureSpec) {
		int result = 0;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);

		if (specMode == MeasureSpec.EXACTLY) {
			result = specSize;
		} else {
			if (specMode == MeasureSpec.AT_MOST) {
				result = Math.min(result, specSize);
			}
		}
		return result;
	}

	public int getMax() {
		return max;
	}

	public int getProgress() {
		return progress;
	}
	
	public void setProgressAtStart(int progress){	  
	  this.progress = progress;
	}

	public void setProgress(int progress) {
  
		if (progress >= 0 && progress <= max) {
			this.progress = progress;
			invalidate();
		}
	}
}
