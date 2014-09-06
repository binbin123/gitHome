package com.letv.upnpControl.tools;

import android.util.Log;

/**
 * @title: 
 * @description:用于Log的打印与调试 
 * @company: 乐视网信息技术（北京）股份有限公司 
 * @author shixq
 * @version 1.0
 * @created 2012-2-7 下午2:46:01
 * @changeRecord
 */
public class LetvLog{
	public static final String S_LOGTAG = "YTL_LETV";	//打印Log的默认TAG
	public static final boolean B_DEBUG = false;		//是否打印Log的开关

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
	
	public static void w(String TAG ,String msg) {
		//if (B_DEBUG){
			if(msg == null){
				Log.d(TAG, "null");
			}
			else{
				Log.d(TAG, msg);
			}
		//}
	}
}