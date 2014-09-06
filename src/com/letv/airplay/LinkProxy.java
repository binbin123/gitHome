package com.letv.airplay;

import java.lang.reflect.Method;

import com.letv.upnpControl.tools.LetvLog;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;


/**
 * The LinkProxy class serves as the proxy for the AirplayMDNS class.
 * The important property of class LinkProxy is that choose a approriate network interface and call AirplayMDNS function.  
 * @author Jamin
 */
class LinkProxy{
	private static final String TAG = "airplay.LinkProxy";
	private boolean IsOn = false; //configure
	private boolean SS = false;	//current
	
	private static final String WifiApName 	= "AP"; 
	private static final String WifiName 	= "WIFI";
	private static final String EthName		= "ETH";
	
	private static final String WLAN0		= "wlan0";
	private static final String ETH0		= "eth0";
	
	private AirplayService service = null;
	AirplayMDNS jmdns= null; 
	private String Name2Bind = null;
	
    public static final int WIFI_AP_STATE_DISABLING = 10;
    public static final int WIFI_AP_STATE_DISABLED = 11;
    public static final int WIFI_AP_STATE_ENABLING = 12;
    public static final int WIFI_AP_STATE_ENABLED = 13;
    public static final int WIFI_AP_STATE_FAILED = 14;  
    
	LinkProxy(AirplayService airplayService){
		service = airplayService;
		jmdns = AirplayMDNS.getInstance(service);
	}

	public void release(){
		if(jmdns != null){
			jmdns.clearInstance();
			jmdns = null;
		}
	}
	
	public void FiltOpt(String event){
		
		boolean AP = isApEnabled();
		boolean WIFI = isWifiEnabled();
		boolean ETH = isEthEnabled();
		
		LetvLog.d(TAG, "Airplay current binded " + Name2Bind + " " + event);
		
		if(event.contentEquals("MultiScreenUp")){
			if(null == Name2Bind){
				if(true == AP){
					Name2Bind = WifiApName;
					LetvLog.d(TAG, "Airplay bind " + Name2Bind);
					startmCast(WLAN0);
					return;
				}else if(true == ETH){
					Name2Bind = EthName;
					LetvLog.d(TAG, "Airplay bind " + Name2Bind);
					startmCast(ETH0);
					return;
				}else if(true == WIFI){
					Name2Bind = EthName;
					LetvLog.d(TAG, "Airplay bind " + Name2Bind);
					startmCast(WLAN0);
					return;
				}else{
					LetvLog.d(TAG, "Airplay do nothing!");
				}
			}
			
		}else if(event.contentEquals("MultiScreenDown")){
			
			LetvLog.d(TAG, "Airplay unbind " + Name2Bind);
			Name2Bind = null;
			stopmCast();
			return;
		}
		else if(event.contentEquals(AirplayService.ACTION_WIFI_AP_STATE_CHANGED)){
			if(true == AP){
				if(null == Name2Bind){
					Name2Bind = WifiApName;
					LetvLog.d(TAG, " Airplay Bind: " + Name2Bind);
					stopmCast();
					startmCast(WLAN0);
					return;
				}
				if(true == Name2Bind.equals(WifiApName)){
					LetvLog.d(TAG, " Airplay already bind !");
					return;
				}else{
					Name2Bind = WifiApName;
					LetvLog.d(TAG, " Airplay Bind: " + Name2Bind);
					stopmCast();
					startmCast(WLAN0);
				}
			}else{
				if(null == Name2Bind){
					LetvLog.d(TAG, " Airplay do nothing!");
					return;
				}
				if(true == Name2Bind.equals(WifiApName)){
					LetvLog.d(TAG, " Airplay Unbind " + Name2Bind);
					Name2Bind = null;
					stopmCast();
					if(true == ETH){
						Name2Bind = EthName;
						LetvLog.d(TAG, " Airplay Bind: " + Name2Bind);
						startmCast(ETH0);
						return;
					}
					if(true == WIFI){
						Name2Bind = WifiName;
						LetvLog.d(TAG, " Airplay Bind: " + Name2Bind);
						startmCast(WLAN0);
						return;
					}
				}
			}
			
		}else if(event.contentEquals(AirplayService.ACTION_CONNECTIVITY_CHANGE)){
			if(false == ETH && false == WIFI){
				LetvLog.d(TAG, "Airplay disconnect");
				
				if(null == Name2Bind){
					LetvLog.d(TAG, "Airplay do nothing!");
					return;
				}
				
				if(true == Name2Bind.contentEquals(WifiApName)){
					LetvLog.d(TAG, "Airplay WIFI-AP binded " + Name2Bind);
					return;
				}else{
					LetvLog.d(TAG, "Airplay Undind " + Name2Bind);
					Name2Bind = null;
					stopmCast();
				}
			}
			if(false == ETH && true == WIFI){
				
				if(null == Name2Bind){
					Name2Bind = WifiName;
					LetvLog.d(TAG, "Airplay bind " + Name2Bind);
					stopmCast();
					startmCast(WLAN0);
					return;
				}
				
				if(true == Name2Bind.contentEquals(WifiName)){
					LetvLog.d(TAG, "Airplay binded " + Name2Bind);
					return;
				}else{
					Name2Bind = WifiName;
					LetvLog.d(TAG, "Airplay bind " + Name2Bind);
					stopmCast();
					startmCast(WLAN0);
				}
			}
			if(true == ETH && false == WIFI){
				if(null == Name2Bind){
					Name2Bind = EthName;
					LetvLog.d(TAG, "Airplay bind " + Name2Bind);
					startmCast(ETH0);
					return;
				}
				if(true == Name2Bind.contentEquals(EthName) || true == Name2Bind.contentEquals(WifiApName)){
					LetvLog.d(TAG, "Airplay binded " + Name2Bind);
					return;
				}else if(true == Name2Bind.contentEquals(WifiName)){
					Name2Bind = EthName;
					LetvLog.d(TAG, "Airplay bind " + Name2Bind);
					stopmCast();
					startmCast(ETH0);
					return;
				}
			}
		}else if(event.contentEquals(AirplayService.ACTION_CHANGE_NAME_SYS) || event.contentEquals(AirplayService.ACTION_CHANGE_NAME_SC)){
			if(null == Name2Bind){
				LetvLog.d(TAG, "Airplay no bind!");
				return;
			}else{
				stopmCast();
				if(true == Name2Bind.contentEquals(WifiApName)){
					LetvLog.d(TAG, "Airplay bind " + Name2Bind);
					startmCast(WLAN0);
					return;
				}else if(true == Name2Bind.contentEquals(WifiName)){
					LetvLog.d(TAG, "Airplay bind " + Name2Bind);
					startmCast(WLAN0);
					return;
				}else{
					LetvLog.d(TAG, "Airplay bind " + Name2Bind);
					startmCast(ETH0);
					return;
				}
			}
			
		}else{
			
		}
	}
	
	 private int getWifiApState() {
		 WifiManager wifiManager = (WifiManager) service.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
	     try {
	    	Method method = wifiManager.getClass().getMethod("getWifiApState");
	     	int i = (Integer) method.invoke(wifiManager);
	     	LetvLog.d(TAG, "Airplay getWifiApState "+ i);
	        return i;
	     } catch (Exception e) {
	        LetvLog.d(TAG, "Airplay " + "Cannot get WiFi AP state "+ e);
	        return WIFI_AP_STATE_FAILED;
	        }
	    }
	     
	    private boolean isApEnabled() {
	        int state = getWifiApState();
	        
	        if(WIFI_AP_STATE_ENABLING == state || WIFI_AP_STATE_ENABLED == state){
	        	LetvLog.d(TAG, "Airplay isApEnabled");
	        	return true;
	        }
	       
	        LetvLog.d(TAG, "Airplay isApDisabled");
	        return false;
	    }
	    
	    private boolean isWifiEnabled(){
	    	
	    	ConnectivityManager manager = (ConnectivityManager) service
    				.getApplicationContext().getSystemService(
    						Context.CONNECTIVITY_SERVICE);
    		if (manager == null) {
    			LetvLog.d(TAG, "airplay manager null");
    			return false;
    		}
    		NetworkInfo networkinfo = manager.getActiveNetworkInfo();

    		if (networkinfo == null || !networkinfo.isAvailable()) {
    			LetvLog.d(TAG, "airplay networkinfo faild!");
    			return false;
    		}

    		String type = networkinfo.getTypeName();
	    	
			if (type.contains("WIFI") || type.contains("wifi")) {
				LetvLog.d(TAG, "airplay isWifiEnabled");
				return true;
			}
			
			LetvLog.d(TAG, "airplay isWifiDisabled");
			return false;
	    }
	    
	    private boolean isEthEnabled(){
	    	
	    	ConnectivityManager manager = (ConnectivityManager) service
    				.getApplicationContext().getSystemService(
    						Context.CONNECTIVITY_SERVICE);
    		if (manager == null) {
    			LetvLog.d(TAG, "airplay manager null");
    			return false;
    		}
    		NetworkInfo networkinfo = manager.getActiveNetworkInfo();

    		if (networkinfo == null || !networkinfo.isAvailable()) {
    			LetvLog.d(TAG, "airplay networkinfo faild!");
    			return false;
    		}

    		String type = networkinfo.getTypeName();
	    	
    		if (type.contains("ETHERNET") || type.contains("ethernet")) {
    			LetvLog.d(TAG, "airplay isEthEnabled");
    			return true;
			}
    		
    		LetvLog.d(TAG, "airplay isEthDisabled");
			return false;
	    }
	
	/**
	 * The function can be called when re
	 */
	public boolean alive(String name){
		
		LetvLog.d(TAG, "Airplay alive " + name);
		
		if(name.contentEquals(WifiApName)){
			return isApEnabled();
		}else if(name.contentEquals(WifiName)){
			return isWifiEnabled();
		}else if(name.contentEquals(EthName)){
			return isEthEnabled();
		}else{
			return false;
		}
		
	}
	
	private void startmCast(String name) {
		LetvLog.d(TAG, "Airplay  startmCast");
		if (jmdns != null) {
			jmdns.getMyHandler().post(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					try {
						LetvLog.d(TAG, "run startmCast");
						AirplayService.airplayStart();
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
			jmdns.getMyHandler().sendMessage(jmdns.getMyHandler().obtainMessage(AirplayMDNS.MDNS_START, name));	
		}
	}

	private void stopmCast() {
		LetvLog.d(TAG, "Airplay  stopmCast");
		if (jmdns != null) {
			jmdns.getMyHandler().sendEmptyMessage(AirplayMDNS.MDNS_STOP);
			jmdns.getMyHandler().post(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					try {
						LetvLog.d(TAG, "run stopmCast");
						service.releaseResource();
						AirplayService.airplayStop();
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
		}
	}
}