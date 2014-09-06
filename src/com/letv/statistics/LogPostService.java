package com.letv.statistics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.letv.upnpControl.tools.LetvLog;
import com.letv.upnpControl.tools.LetvUtils;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;


public class LogPostService extends Service {

	private String Url = "http://log.hdtv.letv.com/api/log/mobile/tvzs";
	private Thread logPostThread;
	private static final String TBL_STARTUPS = "startupsTbl";
	private static final String TBL_LOGINS = "loginsTbl";
	private static final String TBL_NAVCLICKS = "navclicksTbl";
	private static final String TBL_PUSHES = "pushesTbl";
	private static final String TBL_VSHOWS = "vshowsTbl";
	private static final String TBL_VPLAYS = "vplaysTbl";
	private static final String TBL_APPSHOWS = "appshowsTbl";
	private static final String TBL_APPINSTALLS = "appinstallsTbl";
	private static final String TBL_OPENDURS = "opendursTbl";
	private static final String TAG = "LogPostService";
	private int INTERVAL;
	private long lastPostTime;
	private String mac;
	private String model;
	private String aver;
	private String os;
	private String osVersion;
	private String udid;

	private DBHelper helper;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		
		PhoneInfo phoneInfo = new PhoneInfo(this); 
	
		mac = LetvUtils.getMac(this);
		udid = phoneInfo.getUdid();
		model = phoneInfo.getDeviceType();
		os = phoneInfo.getDeviceOS();
		osVersion = phoneInfo.getDeviceOSVersion();
		aver = phoneInfo.getVersionName();
		INTERVAL = 180;
		lastPostTime = System.currentTimeMillis();
		helper = new DBHelper(this);
	}

	private Boolean isUpdateDataBase() {
		Boolean flag = false;
		if(helper==null){
			return false;
		}
		if (helper.isEmpty(TBL_STARTUPS) == false) {
			flag = true;
		}
		if (helper.isEmpty(TBL_LOGINS) == false) {
			flag = true;
		}
		if (helper.isEmpty(TBL_NAVCLICKS) == false) {
			flag = true;
		}
		if (helper.isEmpty(TBL_PUSHES) == false) {
			flag = true;
		}
		if (helper.isEmpty(TBL_VSHOWS) == false) {
			flag = true;
		}
		if (helper.isEmpty(TBL_VPLAYS) == false) {
			flag = true;
		}
		if (helper.isEmpty(TBL_APPSHOWS) == false) {
			flag = true;
		}
		if (helper.isEmpty(TBL_APPINSTALLS) == false) {
			flag = true;
		}
		if (helper.isEmpty(TBL_OPENDURS) == false) {
			flag = true;
		}
		return flag;
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		onStart(intent,startId);
		return START_STICKY;
	}
	@Override
	public void onStart(Intent intent, int startId) {
		logPostThread = new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
			
					/* 判断有无需要上报的数据和是否连接网络*/
					if (isUpdateDataBase() == true && LetvUtils.isCanConnected(LogPostService.this)) {
						ContentValues values = new ContentValues();
						values.put("at",
								Long.toString(System.currentTimeMillis()));
						int dur = 0;

						dur = (int) (System.currentTimeMillis() - lastPostTime);
						if(helper==null){
							return;
						}
						if (dur > 60*1000) {
							int pdur = dur / 1000;
							values.put("dur", pdur);
							values.put("fdur", pdur);
							values.put("bdur", 0);
							try{
								helper.delAll(TBL_OPENDURS);
								helper.insert(values, TBL_OPENDURS);
							}catch(Exception e){
								e.printStackTrace();
							}
						}

						post();
					} else {
//						Log.i("logpost", "data base is no update");
					}

					try {
						Thread.sleep(INTERVAL * 1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		logPostThread.start();
		
	}

	@Override
	public void onDestroy() {
		if (logPostThread != null && logPostThread.isAlive()) {
			try {
				logPostThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			logPostThread = null;
		}
		super.onDestroy();
	}

	private void buildJson(JSONObject device, String table, String strPnt,
			String[] str, int size) {
		JSONArray js = new JSONArray();
		try {
			Cursor c = helper.query(table);
			// Log.i("logpost", "table:" + table);
			if (c.moveToFirst()) {

				do {
					JSONObject b = new JSONObject();
					for (int i = 0; i < size; i++) {
						if (str[i].equals("tp") == true
								|| str[i].equals("dur") == true
								|| str[i].equals("fdur") == true
								|| str[i].equals("bdur") == true) {
							b.put(str[i], c.getInt(i + 1));
						} else {
							b.put(str[i], c.getString(i + 1));
						}
					}
					// Log.i("logpost", "b:" + b);
					js.put(b);

				} while (c.moveToNext());
				// Log.i("logpost", "js:" + js);
				device.put(strPnt, js);

			} else {
				// Log.i("logpost", "table:null");
			}
			c.close();
			c = null;
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	private void clearTableData() {
		helper.delAll(TBL_STARTUPS);
		helper.delAll(TBL_LOGINS);
		helper.delAll(TBL_NAVCLICKS);
		helper.delAll(TBL_PUSHES);
		helper.delAll(TBL_VSHOWS);
		helper.delAll(TBL_VPLAYS);
		helper.delAll(TBL_APPSHOWS);
		helper.delAll(TBL_APPINSTALLS);
		helper.delAll(TBL_OPENDURS);
	}

	private void post() {
		try {
			// 首先最外层是{}，是创建一个对象
			JSONObject device = new JSONObject();

			/* 手机基本信息 */
			device.put("mac", mac);
			device.put("udid", udid);
			device.put("model", model);
			device.put("os", os);
			device.put("osver", osVersion);
			device.put("aver", aver);
			device.put("hb", INTERVAL);

			/* 批量上报信息 */
			String table = null;
			String strPnt = null;
			String str[] = new String[5];
			int size = 0;

			table = TBL_STARTUPS;
			strPnt = "startups";
			str[0] = "at";
			size = 1;
			buildJson(device, table, strPnt, str, size);

			table = TBL_LOGINS;
			strPnt = "logins";
			str[0] = "at";
			str[1] = "tp";
			str[2] = "dur";
			size = 3;
			buildJson(device, table, strPnt, str, size);

			table = TBL_NAVCLICKS;
			strPnt = "navclicks";
			str[0] = "at";
			str[1] = "navid";
			size = 2;
			buildJson(device, table, strPnt, str, size);

			table = TBL_PUSHES;
			strPnt = "pushes";
			str[0] = "at";
			str[1] = "tp";
			size = 2;
			buildJson(device, table, strPnt, str, size);

			table = TBL_VSHOWS;
			strPnt = "vshows";
			str[0] = "at";
			str[1] = "vid";
			str[2] = "vn";
			size = 3;
			buildJson(device, table, strPnt, str, size);

			table = TBL_VPLAYS;
			strPnt = "vplays";
			str[0] = "at";
			str[1] = "vid";
			str[2] = "vn";
			size = 3;
			buildJson(device, table, strPnt, str, size);

			table = TBL_APPSHOWS;
			strPnt = "appshows";
			str[0] = "at";
			str[1] = "apn";
			str[2] = "an";
			size = 3;
			buildJson(device, table, strPnt, str, size);

			table = TBL_APPINSTALLS;
			strPnt = "appinstalls";
			str[0] = "at";
			str[1] = "apn";
			str[2] = "an";
			size = 3;
			buildJson(device, table, strPnt, str, size);

			table = TBL_OPENDURS;
			strPnt = "opendurs";
			str[0] = "at";
			str[1] = "dur";
			str[2] = "fdur";
			str[3] = "bdur";
			size = 4;
			buildJson(device, table, strPnt, str, size);

			String json = device.toString();
			if (json != null) {
				LetvLog.i(TAG, "json:" + json);
				String reData = LogPost.reqForPost(Url, json);
				if (reData != null) {
					if (isLogPostSuccess(reData) == true) {
						/* 上报成功 */
						LetvLog.i(TAG, "post log is success!");
						clearTableData();
						lastPostTime = System.currentTimeMillis();
					}
				}
			}
		} catch (JSONException ex) {
			// 键为null或使用json不支持的数字格式(NaN, infinities)
			throw new RuntimeException(ex);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private Boolean isLogPostSuccess(String result) {
		JSONObject jo;
		Boolean flag = true;
		try {
			jo = new JSONObject(result);

			if (jo.has("httpStatusCode") == true) {
				if (jo.getInt("httpStatusCode") != 200) {
					flag = false;
				}
			}

			if (jo.has("hb") == true) {

				INTERVAL = jo.getInt("hb");
				LetvLog.i(TAG, "hb=" + INTERVAL);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return flag;
	}
}
