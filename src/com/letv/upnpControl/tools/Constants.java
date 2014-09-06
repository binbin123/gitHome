package com.letv.upnpControl.tools;

/**
 * @title:
 * @description:
 * @company: 涔愯缃戜俊鎭妧鏈紙鍖椾含锛夎偂浠芥湁闄愬叕鍙�
 * @author 浜庡涵榫�
 * @version 1.0
 * @created 2012-2-24 涓婂崍12:22:07
 * @changeRecord
 */
public class Constants
{
    public final static String defaultDeviceName = "鏅鸿兘鐢佃";// "Smart TV";
    
    public final static String goto_player = "com.letv.play.pushplayvideo";
    
    public final static String goto_player_url = "goto_player_url";
    
    public final static String goto_player_title = "goto_player_title";
    
    public final static String goto_player_time = "goto_player_time";
    
    public final static String DEFAULT_HOST = "mscf.hdtv.letv.com";
    
    public static final String DEVEICE_MAC = "net.local.mac";// 鐩掑瓙鐨凪AC鍦板潃
    
    public static final String DEVEICE_NAME = "persist.sys.dlna.name";// 鐩掑瓙鐨勫悕瀛�
    
    // public final static boolean isUserCenter = true;//
    // 鐧诲綍鏃舵槸鍚﹂渶瑕佸厛璁块棶鐢ㄦ埛涓績鎺ュ彛
    public final static int serviceTCPPort = 8000;
    
    public final static int servicePort = 21;
    
    public final static String deviceType = "tv";
    
    public final static int SOCKET_TIME_OUT = 10000;
    
    public final static int CHANNEL_ID = 0;// 娓犻亾
    
    /** 涓庢湇鍔″櫒閫氫俊鐨勭紪鐮佹牸寮� */
    public static final String CHARSET = "utf-8";
    
    public static final String DEVICE_TYPE_PC = "pc";// 璁惧鐨勭被鍨�
    
    public static final String DEVICE_TYPE_STB = "tv";// 璁惧鐨勭被鍨�
    
    public static final String DEVICE_TYPE_CONTROL = "controlPhone";// 璁惧鐨勭被鍨�
                                                                    // 鎵嬫満閬ユ帶鍣�
    
    public static final String INTENT_UPDATE_DOWNLOAD = "com.letv.updateDOWNLOAD";
    
    public static final String INTENT_UPDATE_DEVICE = "com.letv.updateDevice";
    
    public static final String INTENT_UPDATE_TAG = "com.letv.updateTag";
    
    public static final String INTENT_DOWNLOAD_OK = "com.letv.download_ok";// 涓嬭浇鎺у埗鍒楄〃鑾峰彇瀹屾垚
    
    public static final String INTENT_DOWNLOAD_R = "com.letv.r_download";// 鎺ㄩ€佷笅杞界殑鎻愮ず
    
    public static final boolean IS_SHAKE = true;
    
    // public static final String LOGURL =
    // "http://60.28.199.181/post_statistic";//Log鎻愪氦鐨勫湴鍧€
    // public static final String INITURL =
    // "http://220.181.117.43:180/getserver?dev=1";//璋冨害鍦板潃
    
    // 鑾峰彇flv璋冨害鎾斁鍦板潃鐨刄RL
    // public static final String GET_FLV_STRING =
    // "http://mms.letv.com/MMS/getVideo/response_type/json/vid/{$mmsid}/vtype/flv_vip";
    public static final String GET_FLV_STRING =
        "http://mms.letv.com/MMS/getUrlsForIptv/response_type/json/vid/{$mmsid}/vtype/flv_vip";
    
    public static final String LOGURL = "http://count.3p.letv.com/post_statistic";// Log鎻愪氦鐨勫湴鍧€
    
    public static final String INITURL = "http://ser.3p.letv.com/getserver";// 璋冨害鍦板潃1
    
    public static final String INITURL2 = "http://sercu.3p.letv.com/getserver";// 璋冨害鍦板潃2
    
    public static final String INITURL3 = "http://serct.3p.letv.com/getserver";// 璋冨害鍦板潃3
    
    public static final class ERRORTYPE
    {
        public static final int BASE_TYPE = 0;
        
        public static final int SUCCESS = BASE_TYPE + 1; // 澶勭悊鎴愬姛
        
        public static final int NET_ERROR = BASE_TYPE + 2; // 缃戠粶寮傚父
        
        public static final int TIME_OUT = BASE_TYPE + 3; // 瓒呮椂
        
        public static final int OTHER_ERROR = BASE_TYPE + 4; // 鍏朵粬閿欒
        
        public static final int INITURL_NULL = BASE_TYPE + 5; // 璋冨害鍦板潃涓虹┖
        
        public static final int SERVER_ERROR = BASE_TYPE + 6; // 鏈嶅姟鍣ㄥ紓甯�
    }
    
    // 鐢ㄦ埛涓績
    // private static final String GET_USER_CENTER =
    // "http://passport.letv.com/cas/loginCheck.do";//?username=%s&password=%s&service=www
    
    // 鍓嶄笁涓帴鍙�
    // 鐧诲綍鎺ュ彛
    public static final String GET_USER_CENTER = "http://ser.3p.letv.com/login";// ?username=%s&password=%s
    
    // 缈昏瘧鎺ュ彛
    public static final String GET_TRANSLATION_URL_STRING = "http://mms.letv.com/album/api/mid_switch_for_tricreen.php";
    
    // 鑾峰彇mp4鍦板潃
    // public static final String GET_MP4_STRING =
    // "http://mms.letv.com/MMS/getMp4Url/response_type/json/mid/{$mmsid}/vtype/mp4/redirect/1";
    public static final String GET_MP4_STRING =
        "http://api.mms.letv.com/MMS/getMp4Url/response_type/json/mid/{$mmsid}/vtype/mp4/redirect/1/pro/tricreen";
    
    // 鍚庝笁涓帴鍙�
    // 鍒樺箍姘戠殑鐧诲綍
    public static final String GET_USER_CENTER_L = "http://119.57.33.168:8282/ISG/android/login";// ?username=%s&password=%s
    
    // 鍒樺箍姘戠殑缈昏瘧鎺ュ彛
    public static final String GET_TRANSLATION_URL_STRING_L = "http://119.57.33.168:8282/ISG/iptvJSON/SwitchScreen";// ?mmsid=1321365
    
    // 鍒樺箍姘戠殑鑾峰彇mp4鍦板潃鎺ュ彛
    public static final String GET_MP4_STRING_L =
        "http://119.57.33.168:8282/ISG/iptvJSON/GetVideoMP?vid={$mmsid}&vtype=flv_350";
    
    public static boolean interface_flag = false;// 鎺ュ彛鐨勯€夋嫨锛宼rue鏃讹紝鎵嶆湁鍚庝笁涓帴鍙ｏ紝false鏃讹紝閲囩敤鍓嶄笁涓帴鍙�
    
    public static boolean hasMouse = false;// 榧犳爣鍔熻兘锛宼rue鏃讹紝鍚湁榧犳爣鍔熻兘锛宖alse鏃讹紝鏃犻紶鏍囧姛鑳姐€�
    
    public static final class LoginStatus
    {
        public static final String OPERATION_ERROR = "operatioin_error"; // 璋冨害鍦板潃鑾峰彇澶辫触
        
        public static final String USER_CENTER_ERROR = "user_center_error"; // 鐧诲綍鐢ㄦ埛涓績澶辫触
        
        public static final String NET_ERROR = "net_error"; // 缃戠粶閿欒
        
        public static final String LOGIN_SUCCESS = "login_success"; // 鐧诲綍鎴愬姛
        
        public static final String TIME_OUT = "time_out"; // 瓒呮椂
        
        public static final String INITURL_NULL = "INITURL_NULL"; // 璋冨害鍦板潃涓簄ull
        
        public static final String OTHER_ERROR = "OTHER_ERROR"; // 鍏朵粬閿欒
        
        public static final String SERVER_ERROR = "server_error"; // 鏈嶅姟鍣ㄥ紓甯�
        
    }
    
    public static final class CtrlType
    {
        public static final String CONTROL = "control";
        
        public static final String PHONE_ONLINE = "phone_online";
        
        public static final String UP = "up";
        
        public static final String DOWN = "down";
        
        public static final String RIGHT = "right";
        
        public static final String LEFT = "left";
        
        public static final String OK = "ok";
        
        public static final String RETURN = "return";
        
        public static final String HOME = "home";
        
        public static final String MENU = "menu";
        
        public static final String INPUT_TEXT = "input_text";
        
        public static final String MOUSE_MOVE = "mouse_move";
        
        public static final String MOUSE_PRESS = "mouse_press";
        
        public static final String POWER = "power";
        
        public static final String SPEECH = "speech";// 璇煶 add by zengld
        
        public static final String PAGE_UP = "page_up";// 涓婁竴椤�
        
        public static final String PAGE_DOWN = "page_down";// 涓嬩竴椤�
        
        public static final String SOUND_CONTROL = "sound_control";// 璇煶鎺у埗
                                                                   // 鏁版嵁缁撴瀯涓巌nput_text鐩稿悓
        
        public static final String WHEEL_UP = "mouse_wheel_up"; // 婊氳疆涓婃粴
        
        public static final String WHEEL_DOWN = "mouse_wheel_down";// 婊氳疆涓嬫粴
        
        public static final String SETTING = "setting";// 璁剧疆
        
        public static final String VOLUME_DOWN = "volume_down";// 闊抽噺-
        
        public static final String VOLUME_UP = "volume_up";// 闊抽噺+
        
        public static final String CHANNEL_DOWN = "channel_down";// 棰戦亾-
        
        public static final String CHANNEL_UP = "channel_up";// 棰戦亾-
        
        public static final String NUM_0 = "num_0";
        
        public static final String NUM_1 = "num_1";
        
        public static final String NUM_2 = "num_2";
        
        public static final String NUM_3 = "num_3";
        
        public static final String NUM_4 = "num_4";
        
        public static final String NUM_5 = "num_5";
        
        public static final String NUM_6 = "num_6";
        
        public static final String NUM_7 = "num_7";
        
        public static final String NUM_8 = "num_8";
        
        public static final String NUM_9 = "num_9";
        
        public static final String MUTE = "mute";
        
        public static final String CLEAR_MEMORY = "clear_memory";
        
    }
    
    public static final String SPEECH_ACTION = "speech_action";// 璇煶骞挎挱Action
    
    public static final String SPEECH_CONTENT = "speech_content";
    
    public static final String NET_VIDEO_PUSH_START = "netty_url_push_start";
    
    public static final String NET_VIDEO_PUSH_STOP = "netty_url_push_stop";
    
    public static final String NET_VIDEO_PUSH_SEEK = "netty_url_push_seek";
    
    public static final String NET_VIDEO_PUSH_SEEK30S = "netty_url_push_30s_seek";
    
    public static final String NET_VIDEO_PUSH_PAUSE_OR_PLAY = "netty_url_push_pause_or_play";
    
    public static final String NET_VIDEO_PUSH_REPLAY = "netty_url_push_replay";
    
    public static final String NET_VIDEO_PUSH_GET_DMR_STATE = "netty_url_push_get_dmr_state";
    
    public static final String NET_VIDEO_PUSH_SET_VOLUME = "netty_url_push_set_volume";
    
    public static final String NET_VIDEO_PUSH_SET_MUTE = "netty_url_push_set_mute";
    
    /* TV for video search,recommend */
    public static final String NET_VIDEO_SEARCH = "netty_video_search";
    
    public static final String NET_VIDEO_RECOMMEND = "netty_video_recommend";
    
    /* TV for applation recommend */
    public static final String NET_APP_RECOMMEND = "netty_app_recommend";
    
    public static final int ONLINE_DATA_TYPE = 3000;
    
    public static final int PING_DATA_TYPE = 3001;
    
    public static final int ONLINE_ACK_DATA_TYPE = 3002;
    
    public static final int OFFLINE_DATA_TYPE = 3003;
    
    public static final int OFFLINE_ACK_DATA_TYPE = 3004;
    
    public static final int SEND_DATA_TYPE = 3005;
    
    public static final class CtrlKeyCode
    {
        public static final int UP = 100;
        
        public static final int DOWN = 101;
        
        public static final int RIGHT = 102;
        
        public static final int LEFT = 103;
        
        public static final int OK = 104;
        
        public static final int PAGE_UP = 105;
        
        public static final int PAGE_DOWN = 106;
        
        public static final int RETURN = 107;
        
        public static final int HOME = 108;
        
        public static final int SETTING = 109;
        
        public static final int INPUT_TEXT = 110;
        
        public static final int MOUSE_MOVE = 111;
        
        public static final int MOUSE_PRESS = 112;
        
        public static final int POWER = 113;
        
        public static final int MENU = 114;
        
        public static final int SPEECH = 115;// 璇煶 add by zengld
        
        public static final int SOUND_CONTROL = 116;// 璇煶鎺у埗
                                                    // 鏁版嵁缁撴瀯涓巌nput_text鐩稿悓
        
        public static final int WHEEL_UP = 116; // 婊氳疆涓婃粴
        
        public static final int WHEEL_DOWN = 117;// 婊氳疆涓嬫粴
        
        public static final int NUM_0 = 118;
        
        public static final int NUM_1 = 119;
        
        public static final int NUM_2 = 120;
        
        public static final int NUM_3 = 121;
        
        public static final int NUM_4 = 122;
        
        public static final int NUM_5 = 123;
        
        public static final int NUM_6 = 124;
        
        public static final int NUM_7 = 125;
        
        public static final int NUM_8 = 126;
        
        public static final int NUM_9 = 127;
        
        public static final int VOLUME_DOWN = 128;
        
        public static final int VOLUME_UP = 129;
        
        public static final int CHANNEL_DOWN = 130;
        
        public static final int CHANNEL_UP = 131;
        
        public static final int MUTE = 132;
        
        public static final int MEMORY_CLEAR = 133;
        
        public static final int PHONE_ONLINE = 999;
    }
}