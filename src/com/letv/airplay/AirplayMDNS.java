package com.letv.airplay;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import com.letv.upnpControl.tools.LetvLog;
import com.letv.upnpControl.tools.LetvUtils;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;

class AirplayMDNS {
	public static AirplayMDNS _instance;
	private HandlerThread handlerThread;
	private MyHandler myHandler;

	public MyHandler getMyHandler() {
		return myHandler;
	}

	private AirplayService service = null;
	/**
	 * bind interface only one.
	 */
	private JmDNS jmdns = null;
	
	/**
	 * bind interface
	 */
	private String connected_device = null;

	private static final int MDNS = 1000;
	public static final int MDNS_START = MDNS + 1;
	public static final int MDNS_STOP = MDNS + 2;
	private static final int MDNS_REFRESH = MDNS + 3;
	private static final int MDNS_CHANGE = MDNS + 4; // devicename

	private static final String AIRPLAY = "_airplay._tcp.local";
	private static final String AIRTUNES = "_raop._tcp.local";
	private static final int PORT1 = 7000; // airplay
	private static final int PORT2 = 5000; // airtune

	private ServiceInfo serviceInfoAirplay = null;
	private ServiceInfo serviceInfoAirtunes = null;
	private static final String TAG = AirplayMDNS.class.getSimpleName();


	boolean first = true;
	
	@SuppressLint("HandlerLeak")
	class MyHandler extends Handler {
		public MyHandler(Looper looper) {
			super(looper);
		}

		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			LetvLog.d(TAG, "handleMessage: " + msg.what);
			switch (msg.what) {
			case MDNS_START:
			case MDNS_REFRESH:
				if(true == first){
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					stopJmdns();
					BindInterface((String)msg.obj);
					first = false;
				}else{
					stopJmdns();
					BindInterface((String)msg.obj);	
				}
				break;
			case MDNS_CHANGE:
				break;
			case MDNS_STOP:
				stopJmdns();
				break;

			}
		}
	}

	public AirplayMDNS(AirplayService airplayService) {
		service = airplayService;
		handlerThread = new HandlerThread("jmdns_handler_thread");
		handlerThread.start();
		myHandler = new MyHandler(handlerThread.getLooper());
	}

	public static AirplayMDNS getInstance(AirplayService airplayService) {
		if (_instance == null) {
			_instance = new AirplayMDNS(airplayService);
		}
		return _instance;

	}

	public void clearInstance() {

		if (myHandler != null) {
			myHandler.removeMessages(MDNS_START);
			myHandler.removeMessages(MDNS_REFRESH);
			myHandler.sendEmptyMessage(MDNS_STOP);
		}

		if (_instance != null)
			_instance = null;

		release();
		myHandler = null;
		handlerThread.quit();
	}

	private boolean stopJmdns() {
		LetvLog.d(TAG, "stopJmdns start");
		if (jmdns != null) {
			try {
				jmdns.close();
				jmdns = null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				jmdns = null;
			}
		}
		LetvLog.d(TAG, "stopJmdns end");
		return true;
	}

	private boolean release() {
		LetvLog.d(TAG, "release start");
		if (jmdns != null) {
			jmdns.unregisterAllServices();
			try {
				jmdns.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				LetvLog.d(TAG, "release end close false");
				return false;
			}
			jmdns = null;
		}

		LetvLog.d(TAG, "release end true");
		return true;
	}

	@SuppressWarnings("rawtypes")
	private boolean BindInterface(String name) {

		InetAddress ip = null;
		NetworkInterface inet = null;
		String deviceName = "Letv-";
		
		LetvLog.d(TAG, "getByName  " + name);

		try {
			inet = NetworkInterface.getByName(name);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LetvLog.d(TAG, "BindInterface end getByName " + name + " false");
			return false;
		}

		if(inet == null){
			LetvLog.d(TAG, "inet = null!");
			return false;
		}
		
		Enumeration addresses = inet.getInetAddresses();

		if(addresses == null){
			LetvLog.d(TAG, "addresses = null!");
			return false;
		}
		
		while (addresses.hasMoreElements()) {
			ip = (InetAddress) addresses.nextElement();

			if (ip != null && ip instanceof Inet4Address) {
				System.out.println("bind IP = " + ip.getHostAddress());
				try {

					SimpleDateFormat formatter = new SimpleDateFormat("HHmmss");
					Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
					String str = formatter.format(curDate);

					String tmp1, tmp2 = null;
					tmp1 = getMacAddress(inet, "");
					tmp2 = deviceName + tmp1 + str;

					LetvLog.d(TAG, "BindInterface target: " + tmp2);

					jmdns = JmDNS.create(ip, tmp2);
					
					if(LetvUtils.isHideNameFunc()){
						deviceName = "Letv";
						deviceName = SystemProperties.get("net.hostname");
					}else{
						SharedPreferences sp = service.getSharedPreferences("DeviceName",
								service.MODE_PRIVATE);

						deviceName = sp.getString("device_name", "KanKan");
					}
					
					LetvLog.d(TAG, "BindInterface deviceName: " + deviceName);
					
						//deviceName = "Letv";
						//deviceName = SystemProperties.get("net.hostname");
						//SharedPreferences sp = service.getSharedPreferences("DeviceName",
						//		service.MODE_WORLD_READABLE);

						//deviceName = sp.getString("device_name", "KanKan");
					tmp1 = getMacAddress(inet, ":");
					final HashMap<String, String> values1 = new HashMap<String, String>();
					/*
					values1.put("deviceid", tmp1);
					values1.put("features", "0x11B");
					values1.put("model", "Letv,1");
					values1.put("srcvers", "130.14");
					*/
					
					values1.put("deviceid", tmp1);
					values1.put("features", "0x100029ff");
					values1.put("model", "AppleTV3,1");
					values1.put("srcvers", "150.33");
					values1.put("flags", "0x4");
					values1.put("vv", "1");

					
					serviceInfoAirplay = ServiceInfo.create(AIRPLAY,
							deviceName, PORT1, 0, 0, values1);

					tmp1 = getMacAddress(inet, "");
					tmp2 = tmp1 + "@" + deviceName;
					final HashMap<String, String> values2 = new HashMap<String, String>();

					
/*					values2.put("txtvers", "1");
					values2.put("cn", "0,1");
					values2.put("ch", "2");
					values2.put("ek", "1");
					values2.put("et", "0,1");
					values2.put("sv", "false");
					values2.put("tp", "UDP");
					values2.put("sm", "false");
					values2.put("ss", "16");
					values2.put("sr", "44100");
					values2.put("pw", "false");
					values2.put("vn", "3");
					values2.put("da", "true");
					values2.put("vs", "130.14");
					values2.put("md", "0,1,2");
					values2.put("am", "AppleTV3,1");
					*/
						
					values2.put("txtvers", "1");
					values2.put("cn", "0,1");
					values2.put("ch", "2");
					values2.put("et", "0,3,5");
					values2.put("sv", "false");
					values2.put("tp", "UDP");
					values2.put("ss", "16");
					values2.put("sr", "44100");
					values2.put("pw", "false");
					values2.put("da", "true");
					values2.put("vs", "150.33");
					
					values2.put("md", "0,1,2");
							
					values2.put("ft", "0x100029FF");
					values2.put("rhd", "4.1.3");
					values2.put("vv", "1");
					values2.put("vn", "65537");
					values2.put("am", "AppleTV3,1");	
					values2.put("sf", "0x4");

					
					serviceInfoAirtunes = ServiceInfo.create(AIRTUNES, tmp2,
							PORT2, 0, 0, values2);

					jmdns.registerService(serviceInfoAirplay);
					jmdns.registerService(serviceInfoAirtunes);

					//jmdns.registerFlushAn();

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					LetvLog.d(TAG, "BindInterface end jmdns create false");
					return false;
				}
			}
		}
		return true;

	}

	private void StoreCurrent(String name) {
		LetvLog.d(TAG, "StoreCurrent start " + name);
		connected_device = name;
		LetvLog.d(TAG, "StoreCurrent end " + connected_device);
	}

	private String getMacAddress(NetworkInterface ni, String insert) {
		String mac = "";
		StringBuffer sb = new StringBuffer();

		byte[] macs;
		try {
			macs = ni.getHardwareAddress();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		
		for (int i = 0; i < macs.length; i++) {
			mac = Integer.toHexString(macs[i] & 0xFF);

			if (mac.length() == 1) {
				mac = '0' + mac;
			}

			sb.append(mac + insert);
		}

		mac = sb.toString();
		
		if(insert == ":")
			mac = mac.substring(0, mac.length() - 1);
		
		return mac;
	}

}
