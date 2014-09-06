package com.letv.dmr.utils;

import android.util.Log;

/**
 * 
 *
 */
public class LogUtil{
	public static final String S_LOGTAG = "lyc_jni";	
	public static final boolean B_DEBUG = true;		

	public static void v(String msg) {
		if (B_DEBUG){
			android.util.Log.v(S_LOGTAG, msg);			
		}
	}
	public static void v(String TAG ,String msg) {
		if (B_DEBUG){
			android.util.Log.v(TAG, msg);
		}
	}
	
	public static void e(String msg) {
		if (B_DEBUG){
			android.util.Log.e(S_LOGTAG, msg);
		}
	}
	public static void e(String TAG ,String msg) {
		if (B_DEBUG){
			android.util.Log.e(TAG, msg);
		}
	}

	public static void i(String msg) {
		if (B_DEBUG){
			android.util.Log.i(S_LOGTAG, msg);
		}
	}
	
	public static void i(String TAG ,String msg) {
		if (B_DEBUG){
			android.util.Log.i(TAG, msg);
		}
	}
	
	public static void d(String msg) {
		if (B_DEBUG){
			android.util.Log.d(S_LOGTAG, msg);
		}
	}
	
	public static void d(String TAG ,String msg) {
		if (B_DEBUG){
			if(msg == null){
				Log.d(TAG, "null");
			}
			else{
				Log.d(TAG, msg);
			}
		}
	}
}