package com.letv.web;

import java.lang.reflect.InvocationTargetException;

import com.letv.smartControl.R;
import com.letv.upnpControl.tools.LetvLog;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

public class WebViewActivity extends Activity {

	public static final String TAG = "WebViewActivity";
	public static final String IPHONE_USERAGENT = "Mozilla/5.0 (iPhone; U; "
			+ "CPU iPhone OS 4_0 like Mac OS X; en-us) AppleWebKit/532.9 "
			+ "(KHTML, like Gecko) Version/4.0.5 Mobile/8A293 Safari/6531.22.7";
//	public static final String IPAD_USERAGENT = "Mozilla/5.0 (LETVX60;iPad; "
//			+ "CPU OS 5_0 like Mac OS X) AppleWebKit/535.35 (KHTML, like Gecko)";

	public static final String APP_USERAGENT = "Mozilla/5.0 (webApp_60TV;iPad; "
			+ "CPU OS 5_0 like Mac OS X) AppleWebKit/535.35 (KHTML, like Gecko)";
	
	public static final String IPAD_USERAGENT = "Mozilla/5.0 (LETVC1;iPad; " +
	        "CPU OS 5_0 like Mac OS X) AppleWebKit/535.35 (KHTML, like Gecko)";
	
	private View loadingView;
	private TextView loadingText;
	private View myView = null;
	private WebView mWebView;
	private int loadingProgress = 0;
	private WebViewClient mWebViewClient = new WebViewClient() {
		// 处理页面导航
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			
			LetvLog.d(TAG, "shouldOverrideUrlLoading url = " + url);
			return true;
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			view.getSettings().setUserAgentString(IPAD_USERAGENT);
			LetvLog.d(TAG, "onPageFinished");

		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			if (url.contains("youku.com")) {
				view.getSettings().setUserAgentString(IPHONE_USERAGENT);
			}
			super.onPageStarted(view, url, favicon);
	
			LetvLog.d(TAG, "onPageStarted url = " + url);
		}
	};

	// 浏览网页历史记录
	// goBack()和goForward()
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			mChromeClient.onHideCustomView();
		}
		LetvLog.d(TAG, "onKeyDown keyCode =  " + keyCode);
		return super.onKeyDown(keyCode, event);
	}

	private WebChromeClient mChromeClient = new WebChromeClient() {

		private CustomViewCallback myCallback = null;

		// 配置权限 （在WebChromeClinet中实现）
//		@Override
//		public void onGeolocationPermissionsShowPrompt(String origin,
//				GeolocationPermissions.Callback callback) {
//			callback.invoke(origin, true, false);
//			super.onGeolocationPermissionsShowPrompt(origin, callback);
//		}
//
//		// 扩充数据库的容量（在WebChromeClinet中实现）
//		@Override
//		public void onExceededDatabaseQuota(String url,
//				String databaseIdentifier, long currentQuota,
//				long estimatedSize, long totalUsedQuota,
//				WebStorage.QuotaUpdater quotaUpdater) {
//
//			quotaUpdater.updateQuota(estimatedSize * 2);
//		}
//
//		// 扩充缓存的容量
//		@Override
//		public void onReachedMaxAppCacheSize(long spaceNeeded,
//				long totalUsedQuota, WebStorage.QuotaUpdater quotaUpdater) {
//
//			quotaUpdater.updateQuota(spaceNeeded * 2);
//		}

		// Android 使WebView支持HTML5 Video（全屏）播放的方法
		@Override
		public void onShowCustomView(View view, CustomViewCallback callback) {
			if (myCallback != null) {
				myCallback.onCustomViewHidden();
				myCallback = null;
				return;
			}
			LetvLog.d(TAG, "onShowCustomView ");
			ViewGroup parent = (ViewGroup) mWebView.getParent();
			parent.removeView(mWebView);
			parent.addView(view);
			myView = view;
			myCallback = callback;
			mChromeClient = this;
		}

		@Override
		public void onHideCustomView() {
			if (myView != null) {
				if (myCallback != null) {
					myCallback.onCustomViewHidden();
					myCallback = null;
				}

				ViewGroup parent = (ViewGroup) myView.getParent();
				parent.removeView(myView);
				parent.addView(mWebView);
				myView = null;
			}
		}

		/**
		 * 显示网页获取加载进度
		 */
		@Override
		public void onProgressChanged(WebView view, int newProgress) {
		
			LetvLog.d(TAG, "Progress = " + newProgress);
			if (newProgress > loadingProgress) {
				loadingProgress = newProgress;
			}
			if (newProgress < 100) {
				loadingView.setVisibility(View.VISIBLE);
				loadingText.setText(getString(R.string.buf_loading_txt,
						newProgress + "%"));
				loadingView.bringToFront();
			} else {
				loadingView.setVisibility(View.GONE);
			}

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webview);

		loadingView = findViewById(R.id.loading_layout1);
		loadingText = (TextView) findViewById(R.id.loading_text1);
		mWebView = (WebView) findViewById(R.id.webview_play);
		mWebView.setWebChromeClient(mChromeClient);
		mWebView.setWebViewClient(mWebViewClient);
		mWebView.setBackgroundColor(Color.BLACK);

		 mWebView.getSettings().setJavaScriptEnabled(true);
		 mWebView.getSettings().setPluginState(PluginState.ON);
		 mWebView.getSettings().setPluginsEnabled(true);//可以使用插件
		 mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		 mWebView.getSettings().setAllowFileAccess(true);
	//	 mWebView.getSettings().setDefaultTextEncodingName("UTF-8");
		 mWebView.getSettings().setLoadWithOverviewMode(true);
		 mWebView.getSettings().setUseWideViewPort(true);
		 //mWebView.setVisibility(View.VISIBLE);

		//initializeOptions();
		loadingProgress = 0;
		// 加载需要显示的网页
		Intent intent = getIntent();
		Uri content_url = intent.getData();
		LetvLog.d(TAG, "url = " + content_url.toString());

		try {
			mWebView.loadUrl(content_url.toString());

		} catch (Exception e) {

			finish();
		}

	}

	private void initializeOptions() {

		WebSettings settings = mWebView.getSettings();
		// settings.setUserAgentString("Mozilla/5.0 (LETVSMART; U; iPad; zh-cn;) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Safari/533.18.5");
		// 设置使用大视窗模式
	    settings.setUseWideViewPort(true);
		settings.setDefaultTextEncodingName("GBK");
		settings.setLightTouchEnabled(true);

		mWebView.requestFocus();
		settings.setJavaScriptCanOpenWindowsAutomatically(true);
		settings.setPluginState(PluginState.ON);
		// settings.setPluginsEnabled(true);
		settings.setAllowFileAccess(true);
		settings.setLoadWithOverviewMode(true);
		settings.setJavaScriptEnabled(true);
		// Technical settings
		settings.setSupportMultipleWindows(true);
		mWebView.setLongClickable(true);
		mWebView.setScrollbarFadingEnabled(true);
		mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		mWebView.setDrawingCacheEnabled(true);

		settings.setAppCacheEnabled(true);
		settings.setDatabaseEnabled(true);
		settings.setDomStorageEnabled(true);

		settings.setCacheMode(WebSettings.LOAD_DEFAULT);
		String dirapp = getApplicationContext().getDir("cache",
				Context.MODE_PRIVATE).getPath();
		settings.setAppCacheMaxSize(Long.MAX_VALUE);
		settings.setAppCachePath(dirapp);
		String dir = getApplicationContext().getDir("databases",
				Context.MODE_PRIVATE).getPath();
		settings.setDatabasePath(dir);
		dir = getApplicationContext().getDir("geolocation",
				Context.MODE_PRIVATE).getPath();
		settings.setGeolocationDatabasePath(dir);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		try {
			mWebView.getClass().getMethod("onPause")
					.invoke(mWebView, (Object[]) null);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		mChromeClient.onHideCustomView();
		mWebView.onPause();
		mWebView.pauseTimers();
		finish();

	}

	@Override
	protected void onResume() {
		super.onResume();
		try {
			mWebView.getClass().getMethod("onResume")
					.invoke(mWebView, (Object[]) null);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		mWebView.onResume();
		mWebView.resumeTimers();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mWebView.stopLoading();
		mWebView.destroy();
	}

}
