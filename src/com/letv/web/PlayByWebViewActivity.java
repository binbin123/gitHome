package com.letv.web;

import java.lang.reflect.InvocationTargetException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import com.letv.smartControl.R;
import com.letv.upnpControl.tools.LetvLog;

public class PlayByWebViewActivity extends Activity {
	private String url = null;
	private CustomWebView mWebView;
	private int loadingProgress = 0;
	
	@SuppressLint("NewApi")
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		LetvLog.i("web","#######  on pause  ########");
		if (mWebView == null) {
			return;
		}
		try {
			mWebView.getClass().getMethod("onPause").invoke(mWebView,(Object[])null);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		mWebView.doOnPause();
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		if (mWebView.isLoading()) {
			mWebView.stopLoading();
			mWebView.clearHistory();
			mWebView.clearCache(false);
			mWebView.destroy();
		}
	}

	@SuppressLint("NewApi")
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		LetvLog.i("web","#######  on resume  ########");
		try {
			mWebView.getClass().getMethod("onResume").invoke(mWebView,(Object[])null);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mWebView.doOnResume();           
	}
	
	private View loadingView;
	private TextView loadingText;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.webview_play);
		// getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
		// ViewGroup.LayoutParams.MATCH_PARENT);
		loadingProgress = 0;
		loadingView = findViewById(R.id.loading_layout1);
		loadingText = (TextView) findViewById(R.id.loading_text1);
		mWebView = (CustomWebView) findViewById(R.id.webview_play);
		
		Intent intent = getIntent();
		if (intent != null) {
			url  = intent.getData().toString();
			 LetvLog.i("web","url:"+ url);
		}
		if(mWebView.isLoading()){
			mWebView.stopLoading();
		}
		mWebView.loadUrl(url);
		
		mWebView.setWebViewClient(new WebViewClient() {
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}
		});
		mWebView.setWebChromeClient(new LetvWebChromeClient());
		mWebView.setBackgroundColor(Color.parseColor("#FBFBFB"));
	}
	
	/**
	 * 设置LetvWebChromeClient
	 * 
	 */
	public class LetvWebChromeClient extends WebChromeClient {
		@Override
		public void onRequestFocus(WebView view) {

		}

		/**
		 * 处理JavaScript弹出的对话框
		 */
		@Override
		public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
			Builder builder = new AlertDialog.Builder(PlayByWebViewActivity.this);
			builder.setTitle("提示");
			builder.setMessage(message);
			builder.setPositiveButton(android.R.string.ok, new AlertDialog.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					result.confirm();
				}
			});
			builder.setCancelable(false);
			builder.create().show();
			return true;
		}

		@Override
		public void onExceededDatabaseQuota(String url, String databaseIdentifier, long currentQuota, long estimatedSize,
				long totalUsedQuota, WebStorage.QuotaUpdater quotaUpdater) {
			quotaUpdater.updateQuota(204801);
		}

		@Override
		public void onReachedMaxAppCacheSize(long spaceNeeded, long totalUsedQuota, WebStorage.QuotaUpdater quotaUpdater) {
			quotaUpdater.updateQuota(spaceNeeded * 2);
		}

		@Override
		public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
			callback.invoke(origin, true, false);
			super.onGeolocationPermissionsShowPrompt(origin, callback);
		}

		@Override
		public void onGeolocationPermissionsHidePrompt() {
			super.onGeolocationPermissionsHidePrompt();
		}

		@Override
		public Bitmap getDefaultVideoPoster() {
			//return BitmapFactory.decodeResource(getResources(), R.drawable.browser_default_video_poster);
			return null;
		}

		@Override
		public View getVideoLoadingProgressView() {
			LayoutInflater inflater = LayoutInflater.from(PlayByWebViewActivity.this);
			return inflater.inflate(R.layout.browser_video_loading_progress, null);
		}

		
		/**
		 * 显示网页获取加载进度
		 */
		@Override
		public void onProgressChanged(WebView view, int newProgress) {
//			loadingProgress = newProgress;
			if (newProgress > loadingProgress) {
				loadingProgress = newProgress;
			}
			if (newProgress < 100) {
				loadingView.setVisibility(View.VISIBLE);
				loadingText.setText(getString(R.string.buf_loading_txt, newProgress + "%"));
				loadingView.bringToFront();
			} else {
				loadingView.setVisibility(View.GONE);
			}
			
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
