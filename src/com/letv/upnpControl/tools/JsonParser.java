package com.letv.upnpControl.tools;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.letv.upnpControl.entity.MouseData;


/**
 * @title: 
 * @description: JSON解析工具类 
 * @company: 乐视网信息技术（北京）股份有限公司 
 * @author 于庭龙 
 * @version 1.0
 * @created 2012-2-8 下午10:56:02
 * @changeRecord
 */
public class JsonParser {
//	/**
//	 * 解析并生成InitServer2Client, refresh时
//	 * @param jsonData
//	 * @return
//	 */
//	public static InitServer2Client parseInitServer2Client_refresh(String jsonData) {
//		InitServer2Client data = new InitServer2Client();
//		JSONObject jsonObject = null;
//		JSONArray jsonArray = null;
//		List<DeviceData> list = new ArrayList<DeviceData>();
//		try {
//			jsonObject = new JSONObject(jsonData);
//			jsonArray = jsonObject.getJSONArray("members");
//		} catch (JSONException e1) {
//			e1.printStackTrace();
//		}
//
//		try {
//			data.setSeq(jsonObject.getString("seq"));
//			data.setAct(jsonObject.getString("act"));
//
//			for (int i = 0; i < jsonArray.length(); i++) {
//				JSONObject jsonObject2 = (JSONObject) jsonArray.opt(i);
//				DeviceData devicedata = new DeviceData();
//				devicedata.gip = (jsonObject2.getString("gip"));
//				devicedata.gport = (jsonObject2.getString("gport"));
//				devicedata.id = (jsonObject2.getString("id"));
//				devicedata.ip = (jsonObject2.getString("ip"));
//				devicedata.port = (jsonObject2.getString("port"));
//				devicedata.type = (jsonObject2.getString("type"));
//				devicedata.name = (jsonObject2.getString("name"));
//				list.add(devicedata);
//			}
//			data.setMembers(list);
//
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//		return data;
//	}
//	
//	/**
//	 * 解析并生成InitServer2Client
//	 * @param jsonData
//	 * @return
//	 */
//	public static InitServer2Client parseInitServer2Client(String jsonData) {
//		InitServer2Client data = new InitServer2Client();
//		JSONObject jsonObject = null;
//		JSONArray jsonArray = null;
//		List<DeviceData> list = new ArrayList<DeviceData>();
//		try {
//			jsonObject = new JSONObject(jsonData);
//			jsonArray = jsonObject.getJSONArray("members");
//		} catch (JSONException e1) {
//			e1.printStackTrace();
//		}
//
//		try {
//			data.setSeq(jsonObject.getString("seq"));
//			data.setAct(jsonObject.getString("act"));
//			data.setKey(jsonObject.getString("key"));
//
//			for (int i = 0; i < jsonArray.length(); i++) {
//				JSONObject jsonObject2 = (JSONObject) jsonArray.opt(i);
//				DeviceData devicedata = new DeviceData();
//				devicedata.gip = (jsonObject2.getString("gip"));
//				devicedata.gport = (jsonObject2.getString("gport"));
//				devicedata.id = (jsonObject2.getString("id"));
//				devicedata.ip = (jsonObject2.getString("ip"));
//				devicedata.port = (jsonObject2.getString("port"));
//				devicedata.type = (jsonObject2.getString("type"));
//				devicedata.name = (jsonObject2.getString("name"));
//				list.add(devicedata);
//			}
//			data.setMembers(list);
//
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//		return data;
//	}
//	
//	/**
//	 * 解析从服务器获取到标签的数据
//	 * @param jsonData
//	 * @return
//	 */
//	public static DownloadLabelData parseDownloadLabelData(String jsonData) {
//		DownloadLabelData data = new DownloadLabelData();
//		JSONObject jsonObject;
//		try {
//			jsonObject = new JSONObject(jsonData);
//			data.setSeq(jsonObject.getString("seq"));
//			data.setAct(jsonObject.getString("act"));
//			// data.setRoom(jsonObject.getString("room"));
//			data.setMembers(new ArrayList<VideoLabelData>());
//			JSONArray jsonArray = jsonObject.getJSONArray("list");
//			for (int i = 0; i < jsonArray.length(); i++) {
//				JSONObject jsonObject2 = (JSONObject) jsonArray.opt(i);
//				VideoLabelData videodata = new VideoLabelData();
//				videodata.setVideoid(jsonObject2.getString("videoid"));
//				videodata.setTitle(jsonObject2.getString("title"));
//				videodata.setTime(jsonObject2.getString("time"));
//				videodata.setType(jsonObject2.getString("type"));
////				videodata.setTag(jsonObject2.getString("tag"));
//				data.getMembers().add(videodata);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return data;
//	}
//	
//	/**
//	 * 解析设备数据
//	 * @param jsonData
//	 * @return
//	 */
//	public static RefreshDeviceData parseRefreshDeviceData(String jsonData) {
//		RefreshDeviceData data = new RefreshDeviceData();
//		JSONObject jsonObject = null;
//		JSONArray jsonArray = null;
//		List<DeviceData> list = new ArrayList<DeviceData>();
//		try {
//			jsonObject = new JSONObject(jsonData);
//			jsonArray = jsonObject.getJSONArray("members");
//		} catch (JSONException e1) {
//			e1.printStackTrace();
//		}
//
//		try {
//			data.setSeq(jsonObject.getString("seq"));
//			data.setAct(jsonObject.getString("act"));
//			for (int i = 0; i < jsonArray.length(); i++) {
//				JSONObject jsonObject2 = (JSONObject) jsonArray.opt(i);
//				DeviceData devicedata = new DeviceData();
//				devicedata.gip = (jsonObject2.getString("gip"));
//				devicedata.gport = (jsonObject2.getString("gport"));
//				devicedata.id = (jsonObject2.getString("id"));
//				devicedata.ip = (jsonObject2.getString("ip"));
//				devicedata.port = (jsonObject2.getString("port"));
//				devicedata.type = (jsonObject2.getString("type"));
//				devicedata.name = (jsonObject2.getString("name"));
//				list.add(devicedata);
//			}
//			data.setMembers(list);
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//		return data;
//	}
//	
//	/**
//	 * 解析成控制指令包
//	 * @param jsonData
//	 * @return 解析后生成的pojo类
//	 */
//	public static ControlData parseControlData(String jsonData) {
//		ControlData mControlData = new ControlData();
//		JSONObject jsonObject = null;
//		try {
//			jsonObject = new JSONObject(jsonData);
//			
//			mControlData.seq = jsonObject.getString("seq");
//			mControlData.act = jsonObject.getString("act");
//			mControlData.fromid = jsonObject.getString("fromid");
//			mControlData.toid = jsonObject.getString("toid");
//			mControlData.cmd = jsonObject.getString("cmd");
//			mControlData.value = jsonObject.getString("value");
//			
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//		
//		return mControlData;
//	}
//	
	/**
	 * 解析收到的鼠标坐标
	 * @param jsonData
	 * @return
	 */
	public static MouseData parseMouseData(String jsonData){
		MouseData mMouseData = new MouseData();
		JSONObject jsonObject = null;
		try {
			jsonObject = new JSONObject(jsonData);
			JSONObject vJSONObject = jsonObject.getJSONObject("value");
			mMouseData.x = vJSONObject.getString("x");
			mMouseData.y = vJSONObject.getString("y");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return mMouseData;
	}
}
