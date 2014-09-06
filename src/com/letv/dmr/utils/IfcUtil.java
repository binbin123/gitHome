package com.letv.dmr.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.conn.util.InetAddressUtils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.PppoeManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class IfcUtil {
  public static final String IFNAME_ETH0 = "eth0";
  public static final String IFNAME_PPP0 = "ppp0";
  public static final String IFNAME_WLAN0 = "wlan0";
  private static final int PING_TIMEOUT = 1000;

  /*
   * 获取ifconfig的第一个HWaddr（有线网卡）的地址。
   */
  public static String getLocalMacAddress() {
    
      String macAddress = getMACAddress("eth0");
      if(macAddress.equals(""))
      {
         macAddress = getMACAddress("wlan0");
      }    
      return macAddress;         

  }
  
  public static String getMACAddress(String interfaceName) {
    try {
        List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
        for (NetworkInterface intf : interfaces) {
            if (interfaceName != null) {
                if (!intf.getName().equalsIgnoreCase(interfaceName)) continue;
            }
            byte[] mac = intf.getHardwareAddress();
            if (mac==null) return "";
            StringBuilder buf = new StringBuilder();
            for (int idx=0; idx<mac.length; idx++)
                buf.append(String.format("%02X:", mac[idx]));       
            if (buf.length()>0) buf.deleteCharAt(buf.length()-1);
            return buf.toString();
        }
    } catch (Exception ex) { } 
    return "";

}

 
  static public boolean checkReachableByIP(String strIP) {
    boolean result = false;
    try {
      result = InetAddress.getByName(strIP).isReachable(PING_TIMEOUT);
    } catch (UnknownHostException e1) {
      e1.printStackTrace();
    } catch (IOException e2) {
      e2.printStackTrace();
    }
    return result;
  }

  static public void killPro(String pName) {
    try {
        
      new ProcessBuilder().command("/system/busybox/bin/kill", pName).start();
      
    } catch (IOException e1) {
      e1.printStackTrace();
    }
  }

  /**
   * 获取4为随机数
   * 
   * @return
   */
  public static String getSerNum() {
    Double d = Math.random();
    if (d >= 0.1 && d < 1) {
      return ((int) (d * 10000)) + "";
    } else {
      return getSerNum();
    }
  }

 
  /**
   * 获取本地的IP地址
   * @return
   */
  
  public static String getIpAddress(Context context) {
	   String ip = null;
	  ip = getLocalWifiIpAddress(context);
	  if(ip.length()< 1){
		  ip = getLocalIpAddress();
	  }
	  return ip;
  }
  public static String getLocalIpAddress() {
  //  Log.e("TAG", "getLocalIpAddress new:");
    try {
      for (Enumeration<NetworkInterface> en = NetworkInterface
          .getNetworkInterfaces(); en.hasMoreElements();) {
        NetworkInterface intf = en.nextElement();
        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
          InetAddress inetAddress = enumIpAddr.nextElement();
          if (!inetAddress.isLoopbackAddress() &&  InetAddressUtils.isIPv4Address(inetAddress.getHostAddress())) {

            String ipAddress =  inetAddress.getHostAddress().toString();

            if(checkIpAddress(ipAddress))
            {
              Log.e("TAG", inetAddress.getHostAddress().toString());
              return ipAddress;
            }       
          }
        }
      }
    } catch (SocketException ex) {
      ex.printStackTrace();
      Log.e("TAG", ex.toString());
    }
    return "";
  }
  public  static String getLocalWifiIpAddress(Context context) {
      String ip = "";
      WifiInfo localWifiInfo = ((WifiManager)context.getSystemService("wifi"))
              .getConnectionInfo();

      if (localWifiInfo == null)
          return "";

      int ipAddress =  localWifiInfo.getIpAddress();
      
      if (ipAddress != 0) {  
          ip = ((ipAddress & 0xff) + "." + (ipAddress >> 8 & 0xff) + "."   
              + (ipAddress >> 16 & 0xff) + "." + (ipAddress >> 24 & 0xff));  
      }  
      return ip;
  }
  
  public static boolean checkIpAddress(String ipAddress) { 
    String regex = "[1-9](\\d{1,2})?\\.(0|([1-9](\\d{1,2})?))\\.(0|([1-9](\\d{1,2})?))\\.(0|([1-9](\\d{1,2})?))"; 
    return Pattern.matches(regex, ipAddress); 
  } 

  public static String intToIp(int i) {

    return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "."
        + ((i >> 16) & 0xFF) + "." + (i >> 24 & 0xFF);
  }

  static public String getCurrentMacAddress(Context context) {
    String macAddr = null;
    ConnectivityManager connMan = (ConnectivityManager) context
        .getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo info = connMan.getActiveNetworkInfo();
    if (info != null) {
      switch (info.getType()) {
      case ConnectivityManager.TYPE_WIFI: {
        WifiManager wifiMan = (WifiManager) context
            .getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMan.getConnectionInfo();
        if (wifiInfo != null) {
          macAddr = wifiInfo.getMacAddress();
        }
      }
        break;

      default:
        macAddr = getLocalMacAddress();
      }
    }
    return macAddr;
  }

  static public WifiInfo getCurrentWifiInfo(Context context) {
    WifiInfo wifiInfo = null;

    ConnectivityManager connMan = (ConnectivityManager) context
        .getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo info = connMan.getActiveNetworkInfo();
    if (info != null && (info.getType() == ConnectivityManager.TYPE_WIFI)) {
      WifiManager wifiMan = (WifiManager) context
          .getSystemService(Context.WIFI_SERVICE);
      wifiInfo = wifiMan.getConnectionInfo();
    }
    return wifiInfo;
  }

  public static String StateToString(int state) {
    switch (state) {
    case WifiManager.WIFI_STATE_UNKNOWN:
      return "WIFI_STATE_UNKNOWN";

    case WifiManager.WIFI_STATE_ENABLING:
      return "WIFI_STATE_ENABLING";

    case WifiManager.WIFI_STATE_ENABLED:
      return "WIFI_STATE_ENABLED";

    case WifiManager.WIFI_STATE_DISABLING:
      return "WIFI_STATE_DISABLING";

    case WifiManager.WIFI_STATE_DISABLED:
      return "WIFI_STATE_DISABLED";
    }

    return "Exception";
  }
}
