package com.letv.web;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.letv.upnpControl.tools.LetvLog;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;

/**
 * @author xujie_w
 * 
 */
public class CustomWebView extends WebView {

	private Context mContext;

	private int mProgress = 100;

	private boolean mIsLoading = false;

	private String mLoadedUrl;

	private TouchAndScrollingListener listener;

	private static boolean mBoMethodsLoaded = false;

	private static Method mOnPauseMethod = null;
	private static Method mOnResumeMethod = null;
	private static Method mSetFindIsUp = null;
	private static Method mNotifyFindDialogDismissed = null;

	public CustomWebView(Context context) {
		super(context);
		mContext = context;
		initializeOptions();
		loadMethods();
	}

	public CustomWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		initializeOptions();
		loadMethods();
	}

	@Override
	public boolean canGoForward() {
		// TODO Auto-generated method stub

		return super.canGoForward();

	}

	@Override
	public void addView(View child) {
		// TODO Auto-generated method stub
		super.addView(child);

	}

	@SuppressWarnings("deprecation")
	public void initializeOptions() {

		WebSettings settings = getSettings();
		// settings.setUserAgentString("Mozilla/5.0 (LETVSMART; U; iPad; zh-cn;) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Safari/533.18.5");
		//设置使用大视窗模式
		settings.setUseWideViewPort(true);
		settings.setDefaultTextEncodingName("GBK");
		settings.setLightTouchEnabled(true);
	 
		requestFocus();
		settings.setJavaScriptCanOpenWindowsAutomatically(true);
		settings.setPluginState(PluginState.ON);
		//settings.setPluginsEnabled(true);
		settings.setAllowFileAccess(true);
		settings.setLoadWithOverviewMode(true);

		settings.setUseWideViewPort(true);
		settings.setLoadWithOverviewMode(true);
		
		settings.setJavaScriptEnabled(true);
		settings.setJavaScriptCanOpenWindowsAutomatically(true);

		settings.setSupportZoom(false);
		// Technical settings
		settings.setSupportMultipleWindows(true);
		setLongClickable(true);
		setScrollbarFadingEnabled(true);
		setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		setDrawingCacheEnabled(true);

		settings.setAppCacheEnabled(true);
		settings.setDatabaseEnabled(true);
		settings.setDomStorageEnabled(true);

		settings.setCacheMode(WebSettings.LOAD_DEFAULT);
		String dirapp = mContext.getApplicationContext().getDir("cache", Context.MODE_PRIVATE).getPath();
		settings.setAppCacheMaxSize(Long.MAX_VALUE);
		settings.setAppCachePath(dirapp);
		String dir = mContext.getApplicationContext().getDir("databases", Context.MODE_PRIVATE).getPath();
		settings.setDatabasePath(dir);
		dir = mContext.getApplicationContext().getDir("geolocation", Context.MODE_PRIVATE).getPath();
		settings.setGeolocationDatabasePath(dir);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {

		final int action = ev.getAction();

		// Enable / disable zoom support in case of multiple pointer, e.g.
		// enable zoom when we have two down pointers, disable with one pointer
		// or when pointer up.
		// We do this to prevent the display of zoom controls, which are not
		// useful and override over the right bubble.
		if ((action == MotionEvent.ACTION_DOWN) || (action == MotionEvent.ACTION_POINTER_DOWN)
				|| (action == MotionEvent.ACTION_POINTER_1_DOWN) || (action == MotionEvent.ACTION_POINTER_2_DOWN)
				|| (action == MotionEvent.ACTION_POINTER_3_DOWN)) {
			if (ev.getPointerCount() > 1) {
				this.getSettings().setBuiltInZoomControls(true);
				this.getSettings().setSupportZoom(true);
			} else {
				this.getSettings().setBuiltInZoomControls(false);
				this.getSettings().setSupportZoom(false);
				if (listener != null) {
					listener.onTouchDown(ev);
				}
			}
		} else if ((action == MotionEvent.ACTION_UP) || (action == MotionEvent.ACTION_POINTER_UP)
				|| (action == MotionEvent.ACTION_POINTER_1_UP) || (action == MotionEvent.ACTION_POINTER_2_UP)
				|| (action == MotionEvent.ACTION_POINTER_3_UP)) {
			this.getSettings().setBuiltInZoomControls(false);
			this.getSettings().setSupportZoom(false);
			if (listener != null) {
				listener.onTouchUp(ev);
			}
		}

		return super.onTouchEvent(ev);
	}

	@Override
	public void loadUrl(String url) {
		mLoadedUrl = url;
		super.loadUrl(url);
	}

	public void setProgress(int progress) {
		mProgress = progress;
	}

	public int getProgress() {
		return mProgress;
	}

	public void notifyPageStarted() {
		mIsLoading = true;
	}

	public void notifyPageFinished() {
		mProgress = 100;
		mIsLoading = false;
	}

	public boolean isLoading() {
		return mIsLoading;
	}

	public String getLoadedUrl() {
		return mLoadedUrl;
	}

	public void resetLoadedUrl() {
		mLoadedUrl = null;
	}

	public boolean isSameUrl(String url) {
		if (url != null) {
			return url.equalsIgnoreCase(this.getUrl());
		}

		return false;
	}

	public void doOnPause() {
		if (mOnPauseMethod != null) {
			try {

				mOnPauseMethod.invoke(this);

			} catch (IllegalArgumentException e) {
				LetvLog.e("CustomWebView", "doOnPause(): " + e.getMessage());
			} catch (IllegalAccessException e) {
				LetvLog.e("CustomWebView", "doOnPause(): " + e.getMessage());
			} catch (InvocationTargetException e) {
				LetvLog.e("CustomWebView", "doOnPause(): " + e.getMessage());
			}
		}
	}

	public void doOnResume() {
		if (mOnResumeMethod != null) {
			try {

				mOnResumeMethod.invoke(this);

			} catch (IllegalArgumentException e) {
				LetvLog.e("CustomWebView", "doOnResume(): " + e.getMessage());
			} catch (IllegalAccessException e) {
				LetvLog.e("CustomWebView", "doOnResume(): " + e.getMessage());
			} catch (InvocationTargetException e) {
				LetvLog.e("CustomWebView", "doOnResume(): " + e.getMessage());
			}
		}
	}

	public void doSetFindIsUp(boolean value) {
		if (mSetFindIsUp != null) {
			try {

				mSetFindIsUp.invoke(this, value);

			} catch (IllegalArgumentException e) {
				LetvLog.e("CustomWebView", "doSetFindIsUp(): " + e.getMessage());
			} catch (IllegalAccessException e) {
				LetvLog.e("CustomWebView", "doSetFindIsUp(): " + e.getMessage());
			} catch (InvocationTargetException e) {
				LetvLog.e("CustomWebView", "doSetFindIsUp(): " + e.getMessage());
			}
		}
	}

	public void doNotifyFindDialogDismissed() {
		if (mNotifyFindDialogDismissed != null) {
			try {

				mNotifyFindDialogDismissed.invoke(this);

			} catch (IllegalArgumentException e) {
				LetvLog.e("CustomWebView", "doNotifyFindDialogDismissed(): " + e.getMessage());
			} catch (IllegalAccessException e) {
				LetvLog.e("CustomWebView", "doNotifyFindDialogDismissed(): " + e.getMessage());
			} catch (InvocationTargetException e) {
				LetvLog.e("CustomWebView", "doNotifyFindDialogDismissed(): " + e.getMessage());
			}
		}
	}

	private void loadMethods() {

		if (!mBoMethodsLoaded) {

			try {

				mOnPauseMethod = WebView.class.getMethod("onPause");
				mOnResumeMethod = WebView.class.getMethod("onResume");

			} catch (SecurityException e) {
				LetvLog.e("CustomWebView", "loadMethods(): " + e.getMessage());
				mOnPauseMethod = null;
				mOnResumeMethod = null;
			} catch (NoSuchMethodException e) {
				LetvLog.e("CustomWebView", "loadMethods(): " + e.getMessage());
				mOnPauseMethod = null;
				mOnResumeMethod = null;
			}

			try {

				mSetFindIsUp = WebView.class.getMethod("setFindIsUp", Boolean.TYPE);
				mNotifyFindDialogDismissed = WebView.class.getMethod("notifyFindDialogDismissed");

			} catch (SecurityException e) {
				LetvLog.e("CustomWebView", "loadMethods(): " + e.getMessage());
				mSetFindIsUp = null;
				mNotifyFindDialogDismissed = null;
			} catch (NoSuchMethodException e) {
				LetvLog.e("CustomWebView", "loadMethods(): " + e.getMessage());
				mSetFindIsUp = null;
				mNotifyFindDialogDismissed = null;
			}

			mBoMethodsLoaded = true;
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		postInvalidate();
	}

	/**
	 * p抛出接口
	 * 
	 * @param listener
	 */
	public void setTouchAndScrollingListener(TouchAndScrollingListener listener) {
		this.listener = listener;
	}

	public interface TouchAndScrollingListener {

		// 手指按下
		void onTouchDown(MotionEvent ev);

		// 手指离开的瞬间
		void onTouchUp(MotionEvent ev);

		// 滑动
		void onScrolling();
	}

}
