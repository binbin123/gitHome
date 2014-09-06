package com.letv.upnpControl.tools;

import java.io.IOException;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;


public class AccountUtils {
    public static final String LETV_ACCOUNT_TYPE = "com.letv";
    public static final String AUTH_TOKEN_TYPE = "AuthToken";
    public static final String LETVURL="content://com.letv.account.userinfo/com.letv";
    public static String loginName = null;
    public static String token = null;
    public static boolean isLetvAccountLogin(Context context){
        AccountManager am = AccountManager.get(context);
        boolean isLogin = false;
        final Account[] accountList = am.getAccountsByType(LETV_ACCOUNT_TYPE);
        if (accountList != null && accountList.length > 0 && accountList[0] != null) {
            isLogin = true;
        }
        return isLogin;
    }

    public static String getAccountName(Context context) {
        AccountManager am = AccountManager.get(context);
        final Account[] accountList = am.getAccountsByType(LETV_ACCOUNT_TYPE);
        String username = null;
        if (accountList != null && accountList.length > 0) {
            username = accountList[0].name;
        }
        return username;
    }

    public static String getAccountToken(Context context)
            throws OperationCanceledException, IOException,
            AuthenticatorException {
        AccountManager am = AccountManager.get(context);
        final Account[] accountList = am.getAccountsByType(LETV_ACCOUNT_TYPE);
        String authToken = null;
        if (accountList != null && accountList.length > 0) {
            authToken = am.blockingGetAuthToken(accountList[0],
                    LETV_ACCOUNT_TYPE, true);
        }
        return authToken;
    }

    public static String getAccountUID(Context context) {
        String userdata = null;
        Uri uri = Uri.parse(LETVURL);
        Cursor cursor = null;
        try{
        	cursor = context.getContentResolver().query(uri, null, null, null, null); 
        	if(cursor != null && cursor.moveToFirst()) { 
        		loginName = cursor.getString(cursor.getColumnIndex("login_name"));
        		userdata = cursor.getString(cursor.getColumnIndex("uid"));
        		token = cursor.getString(cursor.getColumnIndex("token"));
        	}
        }catch(Exception e){
        	e.printStackTrace();
        }finally{
        	if(cursor!=null){
        		cursor.close();
        		cursor = null;
        	}
        }
        return userdata;
    }

    public static void gotoAccountApp(Context context, Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
    
//    public static String getBean(Context context){
//    	AccountManager am = AccountManager.get(context);
//    	final Account[] accountList = am.getAccountsByType(LETV_ACCOUNT_TYPE);
//
//    	String bean = "";
//    	if (accountList != null && accountList.length > 0) {
//    	    bean = am.getUserData(accountList[0],"UserInfo");
//    	}
//    	return bean;
//    }
    
//    public static String getPasswordz(Context context){
//    	AccountManager am = AccountManager.get(context);
//    	final Account[] accountList = am.getAccountsByType(LETV_ACCOUNT_TYPE);
//
//    	String passwd = "";
//    	if (accountList != null && accountList.length > 0) {
//    	    String password = am.getPassword(accountList[0]);
//    	    passwd = new String(Base64.decode(password, Base64.DEFAULT));
//    	}
//    	return passwd;
//    }
}
