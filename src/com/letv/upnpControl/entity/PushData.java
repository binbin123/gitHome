package com.letv.upnpControl.entity;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @title: 
 * @description: 推送协议的封装类
 * @company: 乐视网信息技术（北京）股份有限公司 
 * @author 于庭龙 
 * @version 1.0
 * @created 2012-2-14 上午10:22:33
 * @changeRecord
 */
public class PushData {
	public String seq;
	public String act;
	public String fromid;
	public String toid;
	public String videoid;
	public String title;
	public String time;

	public String jsonString() {
		JSONObject object = new JSONObject();

		try {
			object.put("seq", this.seq);
			object.put("act", this.act);
			object.put("fromid", this.fromid);
			object.put("toid", this.toid);
			object.put("videoid", this.videoid);
			object.put("title", this.title);
			object.put("time", this.time);

			return object.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return "";
	}
}
