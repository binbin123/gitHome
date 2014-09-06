package com.letv.dmr.utils;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

public class NetUtil {

  private Context mContext;
  private static final String WIRE_TYPE = "wire_type";
  private static NetUtil netUtils = null;
  private int netType = 0; //0 wireless  1 wire
	public  boolean isCanConnected(Context ctx) {
		ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		return ni != null && ni.isConnected();
	}
	
	private NetUtil(Context mContext){
    this.mContext = mContext;
  
  }
	
	 public static NetUtil getInstance(Context mContext){
	    if(netUtils == null){
	      netUtils = new NetUtil(mContext);
	    }
	    return netUtils;
	  }
	
	 /**
   * 有线网络设置
   * @param type 0：DHCP  1：静态IP  3：ADSL
   */
  public void setWireType(int type){
    netType = type;
    //Settings.System.putInt(mContext.getContentResolver(), WIRE_TYPE, type);
  }
  
  /**
   * 获取有线网络设置
   * @return 0：DHCP 1：静态IP 2：ADSL  如果获取不到则返回DHCP
   */
  public int getWireType(){
     return netType;
  }
  
}
