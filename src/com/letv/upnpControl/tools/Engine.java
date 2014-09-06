package com.letv.upnpControl.tools;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Enumeration;
import org.apache.http.conn.util.InetAddressUtils;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.TextUtils;
import android.widget.Toast;
import com.letv.smartControl.R;

//import android.os.SystemProperties;

//import android.os.SystemProperties;
/**
 * @title:
 * @description:
 * @company: 娑旀劘顬呯純鎴滀繆閹垱濡ч張顖ょ礄閸栨ぞ鍚敍澶庡亗娴犺姤婀侀梽鎰彆閸欙拷
 * @author 娴滃骸娑垫Λ锟� * @version 1.0
 * @created 2012-2-7 娑撳宕�:19:01
 * @changeRecord
 */
public class Engine {
	private static Engine instance;
	// 閸欘亜鐤勬笟瀣娑擄拷顐�
	
	
	
	
	static {
		instance = new Engine();
	}
	private String clientId; // 鐎广垺鍩涚粩顖滄畱ID閿涘苯鎮囩粩顖滄畱閸烆垯绔撮弽鍥槕 濮濄倖妞傛担璺ㄦ暏閻ㄥ嫭妲搁幍瀣簚娑撴彃褰�	
	private String initURL = ""; // 娴犲孩婀囬崝鈥虫珤閼惧嘲褰囬崚鎵畱鐠嬪啫瀹抽崷鏉挎絻
	private DatagramSocket UDPsocket; // UDP娴溿倓绨伴惃鍑穙cket
	private Socket TCPsocket; // TCP娴溿倓绨伴惃鍑穙cket
	private String localIP; // 閺堫剙婀撮惃鍑閸︽澘娼�	
	private String userName = ""; // 閻劍鍩涢崥锟�	
	private String userPwd; // 鐎靛棛鐖�
	private String deviceName; // 鐠佹儳顦搁崥宥囆�	




private String room; // 閹村潡妫块敍灞界秼閸撳秳濞囬悽銊ф畱閺勭枹D5閸旂姴鐦戦惃鍓坰erName
	private int clientUDPPort; // 鐎广垺鍩涚粩顖濆殰闊偆娈戠粩顖氬經

	private String from;
	private static String to;

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public static String getTo() {
		return to;
	}

	public static void setTo(String To) {
		to = To;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	private String token;
	private String signature;
	private boolean isLogin = false; // 娑撳鐫嗛弰顖氭儊瀹歌尙绮￠惂璇茬秿
	private boolean isStart = false; // 娑撳鐫嗛崥搴″酱缂冩垹绮剁痪璺ㄢ柤閺勵垰鎯佸鑼病閸氼垰濮�	
	private boolean isExit = false; // 娑撳鐫嗛弰顖氭儊闁拷鍤�
	private static int count = 0;// 闁插秷绻涢惃鍕偧閺侊拷
	private long seq = 0; //
							// 閸欐垿锟藉☉鍫熶紖閻ㄥ嫮绱崣锟�	
	private Integer lock = 0; //
								// 閸氬本顒為柨锟�	
	private Object mInitServer2Clientlock = new Object();

	// public RemoteCallbackList<IThreeCallBack> remoteCallback = new
	// RemoteCallbackList<IThreeCallBack>();

	public Context context;

	/**
	 * 閸掓繂顬婇崠鏍︾娴滄稑鐔�張顑夸繆閹拷 * @param context
	 */
	public void init(Context context, String userName, String userPwd,
			String deviceName) {
		// 閻㈢喐鍨氱�銏″煕缁旂枠D
		// @SuppressWarnings("static-access")
		// TelephonyManager mTelephonyManager = (TelephonyManager)
		// context.getSystemService(context.TELEPHONY_SERVICE);
		// this.clientId =
		// mTelephonyManager.getDeviceId();//閼惧嘲褰囬幍瀣簚娑撴彃褰块敍瀛廠M閹靛婧�惃鍑EI閸滃瓔DMA閹靛婧�惃鍑狤ID

		// this.clientId = SystemProperties.get(Constants.DEVEICE_MAC);

		this.context = context;
		// 閼惧嘲褰囬張顒�勾閻ㄥ嚘P閸︽澘娼�		
		this.localIP = LetvUtils.getLocalIpAddress();

		this.userName = userName; // 閻劍鍩涢崥锟�
		this.userPwd = userPwd; // 鐎靛棛鐖�	
		this.deviceName = deviceName; // 鐠佹儳顦搁崥宥囆�	
		this.room = MD5.toMd5(userName);// 閹村潡妫块崣鍑ょ礉娴ｈ法鏁serName鏉╂稖顢慚D5閸旂姴鐦戦悽鐔稿灇
		/* 鐠佸墽鐤嗙拋鎯ь樃閸氬秴鎷癕AC鐎涙ぞ瑕嗛崚鎷岊啎婢跺洦寮挎潻鐗堟瀮娴犳湹鑵�*/
		// String name_mac = deviceName + this.clientId;
		// InitActivity.setDeviceNameAndMac(name_mac,userName);
	}

	public void initNettyData(String from, String token, String sig) {
		this.from = from;
		this.token = token;
		this.signature = sig;
	}

	/*
	 * 婵″倹鐏夐悽銊﹀煕濞屸剝婀佺化鑽ょ埠閻у妾版潻鍥风礉鐠佸墽鐤哻ontext閿涘矂绱堕弽鍥┬╅崝銊╂付鐟�	 */
	public void setContext(Context context) {
		this.context = context;
	}

	/**
	 * 閸掓繂顬婇崠鏈P缂冩垹绮禨ocket
	 * 
	 * @return
	 */
	public synchronized int initUDPSocket() {
		int result = Constants.ERRORTYPE.SUCCESS;
		if (UDPsocket == null) {
			try {
				UDPsocket = new DatagramSocket();

				// 鐠佹澘缍嶈ぐ鎾冲閻ㄥ垊DPSocket閻ㄥ嫮顏崣锟�this.clientUDPPort =
				// UDPsocket.getLocalPort();
			} catch (SocketException e) {
				result = Constants.ERRORTYPE.NET_ERROR;
				e.printStackTrace();
			}
		} else {
			result = Constants.ERRORTYPE.NET_ERROR;
		}
		return result;
	}

	/**
	 * 閸掓繂顬婇崠鏈P缂冩垹绮禨ocket
	 * 
	 * @return
	 */
	public synchronized int initTCPSocket() {
		int result = Constants.ERRORTYPE.SUCCESS;
		if (this.initURL == null || this.initURL.trim().equals("")) {
			result = Constants.ERRORTYPE.INITURL_NULL;
			return result;
		}

		if (TCPsocket == null) {
			TCPsocket = new Socket();
			SocketAddress remoteAddr = new InetSocketAddress(this.initURL,
					Constants.serviceTCPPort);
			try {
				TCPsocket.connect(remoteAddr, Constants.SOCKET_TIME_OUT);// 鐡掑懏妞傞弮鍫曟？
			} catch (IllegalArgumentException e) {// 鐡掑懏妞�				e.printStackTrace();
				result = Constants.ERRORTYPE.TIME_OUT;
			} catch (IOException e) {
				e.printStackTrace();
				result = Constants.ERRORTYPE.SERVER_ERROR;
			} catch (Exception e) {
				e.printStackTrace();
				result = Constants.ERRORTYPE.OTHER_ERROR;
			}
		} else {
			LetvLog.e("TAG", "initTCPSocket閺傝纭舵稉锟� TCPsocket != null");
			result = Constants.ERRORTYPE.OTHER_ERROR;
		}

		return result;
	}

	public synchronized void closeUDPSocket() {
		if (UDPsocket != null) {
			UDPsocket.close();
			UDPsocket = null;
		}
	}

	public synchronized void closeTCPSocket() {
		if (TCPsocket != null) {
			try {
				TCPsocket.close();
				TCPsocket = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 閸氼垰濮╅崥搴″酱閹恒儲鏁圭痪璺ㄢ柤
	 */
	public boolean startReceiveThread(Context context) {
		boolean result = false;
		if (!this.isStart()) {
			// 閸氬骸褰寸純鎴犵捕缁捐法鈻兼潻妯荤梾閺堝鎯庨崝锟�LetvLog.d("TAG",
			// "start background two receive thread (UDP and TCP).");

			if (this.TCPsocket == null) {
				this.initTCPSocket(); // 閸掓繂顬婇崠鏈PSocket
			}

			if (this.UDPsocket == null) {
				this.initUDPSocket(); // 閸掓繂顬婇崠鏈PSocket
			}

			// // TCP閹恒儲鏁圭痪璺ㄢ柤
			// this.tcpReceiveThread = new TCPReceiveThread(context);
			// this.tcpReceiveThread.start();
			//
			// // UDP閹恒儲鏁圭痪璺ㄢ柤
			// this.udpReceiveThread = new UDPReceiveThread(context);
			// this.udpReceiveThread.start();
			//
			// // 韫囧啳鐑︾痪璺ㄢ柤
			// this.checkThread = new CheckThread();
			// this.checkThread.start();
			//
			this.setStart(true);
			result = true;
		} else {
			LetvLog.d("TAG", "background thread start OK.");
		}

		return result;
	}

	/**
	 * 閸嬫粍顒涢幒澶婃倵閸欑増甯撮弨鍓佸殠缁嬶拷
	 */
	public void stopReceiveThread() {
		// // 閸嬫粍顒涢崥搴″酱閻ㄥ嚲CP閹恒儲鏁圭痪璺ㄢ柤
		// if(this.tcpReceiveThread != null){
		// this.tcpReceiveThread.stopTCPReceiveThread();
		// }
		//
		// // 閸嬫粍顒涢崥搴″酱閻ㄥ垊DP閹恒儲鏁圭痪璺ㄢ柤
		// if(this.udpReceiveThread != null){
		// this.udpReceiveThread.stopUDPReceiveThread();
		// }
		//
		// // 閸嬫粍顒涢崥搴″酱閻ㄥ嫬绺剧捄宕囧殠缁嬶拷// if(this.checkThread != null){
		// this.checkThread.stopCheckThread();
		// }

		this.setStart(false);
		this.setLogin(false);

	}

	/**
	 * 闁拷鍤稉澶婄潌閺堝秴濮�	 */
	public void exitThreeScreen() {
		// Engine.getInstance().setLogin(false);
		// Engine.getInstance().setStart(false);
		// Engine.getInstance().stopReceiveThread(); // 閸嬫粍顒涢崥搴″酱TCP閵嗕箒DP閵嗕礁绺剧捄宕囧殠缁嬶拷//
		// Engine.getInstance().closeTCPSocket(); // 閸忔娊妫碩CP Socket
		// Engine.getInstance().closeUDPSocket(); // 閸忔娊妫碪DP Socket
		// SendUtils.getInstance().stopBackgroundThread(); // 閸嬫粍顒涢崥搴″酱閺佺増宓佹径鍕倞缁捐法鈻�	
		}

	/**
	 * 閸楁洖鍨Ο鈥崇础
	 * 
	 * @return
	 */
	public static synchronized Engine getInstance() {
		if (instance == null) {
			instance = new Engine();
		}
		return instance;
	}

	public String getInitURL() {
		return initURL;
	}

	public void setInitURL(String initURL) {
		this.initURL = initURL;
	}

	public DatagramSocket getUDPsocket() {
		return UDPsocket;
	}

	public void setUDPsocket(DatagramSocket uDPsocket) {
		UDPsocket = uDPsocket;
	}

	public Socket getTCPsocket() {
		return TCPsocket;
	}

	public void setTCPsocket(Socket tCPsocket) {
		TCPsocket = tCPsocket;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getLocalIP() {
		return localIP;
	}

	public void setLocalIP(String localIP) {
		this.localIP = localIP;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public String getUserPwd() {
		return userPwd;
	}

	public void setUserPwd(String userPwd) {
		this.userPwd = userPwd;
	}

	public String getRoom() {
		return room;
	}

	public void setRoom(String room) {
		this.room = room;
	}

	public boolean isLogin() {
		return isLogin;
	}

	public void setLogin(boolean isLogin) {
		this.isLogin = isLogin;
	}

	public boolean isStart() {
		return isStart;
	}

	public void setStart(boolean isStart) {
		this.isStart = isStart;
	}

	public int getClientUDPPort() {
		return clientUDPPort;
	}

	public void setClientUDPPort(int clientUDPPort) {
		this.clientUDPPort = clientUDPPort;
	}

	// public DownloadLabelData getLablelist() {
	// return lablelist;
	// }
	//
	// public void setLablelist(DownloadLabelData lablelist) {
	// this.lablelist = lablelist;
	// }
	//
	// public InitServer2Client getmInitServer2Client() {
	// synchronized (mInitServer2Clientlock) {
	// return mInitServer2Client;
	// }
	// }
	//
	// public void setmInitServer2Client(InitServer2Client mInitServer2Client) {
	// synchronized (mInitServer2Clientlock) {
	// this.mInitServer2Client = mInitServer2Client;
	// }
	// if(mInitServer2Client.getMembers() != null){
	// LetvLog.d("TAG", "all members count: " +
	// mInitServer2Client.getMembers().size() + "");
	// }
	// }

	public boolean isExit() {
		return isExit;
	}

	public void setExit(boolean isExit) {
		this.isExit = isExit;
	}

	public int createCount() {
		synchronized (lock) {
			count++;
			return count;
		}
	}

	public void setCountZ() {
		synchronized (lock) {
			count = 0;
		}
	}

	/**
	 * 閻㈢喐鍨歴eq, 鐎广垺鍩涚粩顖涚槨濞嗏�鎮滈張宥呭閸ｃ劌褰傞柅浣圭Х閹垳娈戦崬顖欑閺嶅洩鐦�	 * 
	 * @return seq
	 */
	public String createSeq() {
		synchronized (lock) {
			seq++;
			return seq + "";
		}
	}

	public Context getContext() {

		return context;
	}
	public String getFilePath() {
		String path = "/data/data/com.letv.smartControl/files/";
        if(context != null){
        	String packName = context.getPackageName();
        	path = "/data/data/" + packName + "/files/";
        }
		return path;
	}

    
    /**
     * 
     * @param context
     * @return
     */
    public static String getLocalIpAddress(Context context)
    {
        
        try
        {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();)
            {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();)
                {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()
                        && InetAddressUtils.isIPv4Address(inetAddress.getHostAddress()))
                    {
                        
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        }
        catch (SocketException e)
        {
            // TODO: handle exception 
               e.printStackTrace();
        }
        
        Toast.makeText(context, context.getString(R.string.not_connected_network), Toast.LENGTH_LONG).show();
  /*      if (DMRService.dmrDev != null && !DMRService.dmrDev.isDMRStart) {
            Debug.d("DMRService", "******DMRStart thread start ");
            DMRService.dmrDev.start();
            DMRService.dmrDev.clearConnectedPhoneNumber();
        }*/
        return null;
    }
  /**
   * 
   * @param context
   * @return
   */
    public static String getAppVersion(Context context)
    {
        String versionCode ="";
        try
        {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            
            // 当前版本的版本号
            versionCode = info.versionName;
            
          /*  String strversionCode =String.format(context.getResources().getString(R.string.app_viersion_name), versionCode);
            strVersion.setText(strversionCode);*/
            
        }
        catch (NameNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return versionCode;
    }
    /**
     * 
     * @param context
     */
 
    public static String getDeviceInfo(Context context) {
        try {
            org.json.JSONObject json = new org.json.JSONObject();
            android.telephony.TelephonyManager tm = (android.telephony.TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            String device_id = tm.getDeviceId();

            android.net.wifi.WifiManager wifi = (android.net.wifi.WifiManager) context
                    .getSystemService(Context.WIFI_SERVICE);

            String mac = wifi.getConnectionInfo().getMacAddress();
            json.put("mac", mac);

            if (TextUtils.isEmpty(device_id)) {
                device_id = mac;
            }

            if (TextUtils.isEmpty(device_id)) {
                device_id = android.provider.Settings.Secure.getString(
                        context.getContentResolver(),
                        android.provider.Settings.Secure.ANDROID_ID);
            }

            json.put("device_id", device_id);

            return json.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

	
}
