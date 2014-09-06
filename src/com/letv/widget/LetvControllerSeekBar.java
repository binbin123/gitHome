package com.letv.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.widget.SeekBar;

/**
 * {@hide}
 */
public class LetvControllerSeekBar extends SeekBar {

    public LetvControllerSeekBar(Context context) {
        this(context, null);
    }

    public LetvControllerSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, com.android.internal.R.attr.controllerSeekBarStyleLetv);
    }

    public LetvControllerSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

	@Override
		protected synchronized void onDraw(Canvas canvas) {
		int progress = getProgress();
		int sendcondProgress = getSecondaryProgress();
		if (getProgressDrawable() != null) {
			if (getProgressDrawable() instanceof LayerDrawable) {
				LayerDrawable layerDrawable = (LayerDrawable) getProgressDrawable();
				if (layerDrawable.findDrawableByLayerId(android.R.id.progress) instanceof GradientDrawable) {
					GradientDrawable gradientDrawable = (GradientDrawable) layerDrawable
							.findDrawableByLayerId(android.R.id.progress);
					Rect rect = gradientDrawable.getBounds();
					if (progress == 0) {
						rect.right = 0;
					} else {
						// float n = getHeight() -
						// getThumb().getIntrinsicHeight()
						// - getPaddingTop() - getPaddingBottom();
						float n = layerDrawable.getIntrinsicHeight();
						float m = getWidth() - getPaddingLeft()
								- getPaddingRight();
						rect.right = (int) ((m * (progress - 1) / (getMax() - 1)));
					}
					gradientDrawable.setBounds(rect);
				}
				if (layerDrawable
						.findDrawableByLayerId(android.R.id.secondaryProgress) instanceof GradientDrawable) {
					GradientDrawable gradientDrawable = (GradientDrawable) layerDrawable
							.findDrawableByLayerId(android.R.id.secondaryProgress);
					Rect rect = gradientDrawable.getBounds();
					if (sendcondProgress == 0) {
						rect.right = 0;
					} else {
						float n = layerDrawable.getIntrinsicHeight();
						float m = getWidth() - getPaddingLeft()
								- getPaddingRight();
						rect.right = (int) ((m * (sendcondProgress - 1) / (getMax() - 1)));
					}
					gradientDrawable.setBounds(rect);
				}
			}
		}
		super.onDraw(canvas);
	}
}
