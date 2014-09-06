package com.letv.upnpControl.http;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.letv.upnpControl.tools.AndroidXmlParser;
import com.letv.upnpControl.tools.Constants;
import com.letv.upnpControl.tools.LetvLog;


/**
 * @title:
 * @description: 从服务器中获得json数据
 * @company: 乐视网信息技术（北京）股份有限公司
 * @author 于庭龙
 * @version 1.0
 * @created 2012-2-6 下午6:59:38
 * @changeRecord
 */
public class JsonGet {
	public static final String TAG = JsonGet.class.getSimpleName();
	/**
	 * 获取调度地址,方法内部会尝试从三个地址获取调度
	 * @return 调度地址
	 */
	public static String getInitAdd() {
		String result = "";
		boolean isOK = false;
		String str = HttpUtil.doGet(Constants.INITURL, Constants.CHARSET, 5000);
		if (str != null && !str.trim().equals("")) {
			try {
				JSONObject jsonObject = new JSONObject(str);
				result = jsonObject.getString("ip");
				isOK = true;
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		if (!isOK) {
			str = HttpUtil.doGet(Constants.INITURL2, Constants.CHARSET, 5000);
			if (str != null && !str.trim().equals("")) {
				try {
					JSONObject jsonObject = new JSONObject(str);
					result = jsonObject.getString("ip");
					isOK = true;
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}

		if (!isOK) {
			str = HttpUtil.doGet(Constants.INITURL3, Constants.CHARSET, 5000);
			if (str != null && !str.trim().equals("")) {
				try {
					JSONObject jsonObject = new JSONObject(str);
					result = jsonObject.getString("ip");
					isOK = true;
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}

		return result;
	}

	/**
	 * 登录用户中心
	 * 
	 * @return 是否登录成功
	 */
	public static boolean loginUserCenter(String username, String password) {
		boolean result = false;// 测试使用
		// 尝试登录三次
		for (int i = 0; i < 3; i++) {
			String jsonString = "";
			if (Constants.interface_flag) {
				jsonString = HttpUtil.doGet(Constants.GET_USER_CENTER_L
						+ "?username=" + username + "&password=" + password,
						Constants.CHARSET, 0);
			} else {
				jsonString = HttpUtil.doGet(Constants.GET_USER_CENTER
						+ "?username=" + username + "&password=" + password,
						Constants.CHARSET, 0);
			}
			if (jsonString != null && jsonString.trim().length() != 0) {
				try {
					JSONObject jsonObject = new JSONObject(jsonString);
					JSONObject jsonBean = jsonObject.getJSONObject("bean");
					boolean jsonResult = jsonBean.getBoolean("result");
					if (jsonResult) {// 登录用户中心成功
						result = true;
						break;
					}
				} catch (JSONException e) {
					result = false;
					e.printStackTrace();
					break;
				}
			}
		}

		return result;
	}

	/**
	 * 翻译接口
	 * @param mid   媒资ID
	 * @param key 值有 350K，1000K，1300K，720P，1080P
	 * @return
	 */
	public static String Translation(String mid) {
		String jsonString = "";
		for (int i = 0; i < 3; i++) {
			if (Constants.interface_flag) {
				jsonString = HttpUtil.doGet2(
						Constants.GET_TRANSLATION_URL_STRING_L + "?mmsid="
								+ mid, Constants.CHARSET);
			} else {
				jsonString = HttpUtil.doGet2(
						Constants.GET_TRANSLATION_URL_STRING + "?mmsid=" + mid,
						Constants.CHARSET);
			}

			if (jsonString != null && !jsonString.equals("")) {
				break;
			}
		}
		return jsonString;
	}

	/**
	 * 根据媒资ID 获得MP4播放地址
	 * 
	 * @param mid
	 *            媒资ID
	 * @return
	 */
	public static String GetMp4(String mid) {
		if (mid == null || mid.trim().length() == 0) {
			return null;
		}

		String url = "";
		if (Constants.interface_flag) {
			url = Constants.GET_MP4_STRING_L.replace("{$mmsid}", mid);
		} else {
			url = Constants.GET_MP4_STRING.replace("{$mmsid}", mid);
		}
		if (url == null || url.trim().equals("")) {
			return null;
		}
		System.out.println("-----------GetMp4 mp4 url:" + url);
		String json = HttpUtil.doGet2(url, Constants.CHARSET);
		System.out.println("-----------GetMp4 json: " + json);
		if (CheckJson(json)) {
			try {
				if (Constants.interface_flag) {
					JSONObject jObject = new JSONObject(json);
					String bean = jObject.getString("bean");
					if (CheckJson(bean)) {
						JSONObject beanObject = new JSONObject(bean);
						JSONArray jsonArray = beanObject.getJSONArray("video");
						if (jsonArray != null && jsonArray.length() > 0) {
							JSONObject videoObject = (JSONObject) jsonArray
									.get(0);
							String urlStr = videoObject.getString("url");

							if (urlStr == null || urlStr.trim().length() == 0) {
								return null;
							} else {
								LetvLog.d(TAG, "---------获取的播放地址：" + urlStr);
								return urlStr;
							}
						}
					}
				} else {
					JSONObject jObject = new JSONObject(json);
					String bean = jObject.getString("bean");
					if (CheckJson(bean)) {
						JSONObject beanObject = new JSONObject(bean);
						String preUrl = beanObject.getString("mp4");
						if (preUrl != null) {
							String urlStr1 = getUrlValue(preUrl, "df");
							String urlStr2 = getUrlValue(preUrl, "br");
							if (urlStr1 == null || urlStr1.trim().length() == 0
									|| urlStr2 == null
									|| urlStr2.trim().length() == 0) {
								return null;
							} else {
								String urlString = "http://g3.letv.cn/"
										+ urlStr1 + "?b=" + urlStr2;
								LetvLog.d(TAG, "---------拼接的播放地址：" + urlString);
								return urlString;
							}
						}
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	/**
	 * 获取URL中的KEY的值
	 * @param url
	 * @param key
	 * @return
	 */
	private static String getUrlValue(String url, String key) {
		String value = null;
		if (url != null && key != null) {
			if (url.contains("?") && url.contains(key + "=")) {
				String keyEnd = url.substring(url.indexOf(key));
				if (keyEnd != null) {
					if (keyEnd.contains("&")) {
						value = keyEnd.substring(key.length() + 1,keyEnd.indexOf("&"));
					} else {
						value = keyEnd.substring(key.length() + 1);
					}
				}
			}
		}
		return value;
	}

	/**
	 * 检查字符串是否是json 数据
	 * 
	 * @param json
	 * @return
	 */
	public static boolean CheckJson(String json) {
		LetvLog.d(TAG, "check json : " + json);
		if (json == null || json.trim().length() == 0) {
			return false;
		}
		try {
			new JSONObject(json);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * 翻译接口，只或的720P的视频，没有返回NULL
	 * 
	 * @param mid
	 * @return
	 */
	public static String translate(String mid, String device_type) {
		// return mid;
		String result = "";
		System.out.println("translate mid: " + mid + "device_type: "
				+ device_type);
		String jsonStr = JsonGet.Translation(mid);

		if (jsonStr != null && jsonStr.trim().length() != 0) {
			try {
				JSONObject jsonObject = new JSONObject(jsonStr);
				if (device_type.equals(Constants.DEVICE_TYPE_STB)) {// TV
					if (jsonObject.has("720p")) {
						result = jsonObject.getString("720p");
					} else if (jsonObject.has("1300k")) {
						result = jsonObject.getString("1300k");
					} else if (jsonObject.has("1000k")) {
						result = jsonObject.getString("1000k");
					} else if (jsonObject.has("800k")) {
						result = jsonObject.getString("800k");
					} else {
						result = jsonObject.getString("350k");
					}
				} else {// PC
					result = jsonObject.getString("350k");
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	/**
	 * 根据媒资ID 获得flv播放地址
	 * @param mid 媒资ID
	 * @return
	 */
	public static String getLFV(String mid) {
		if (mid==null || mid.trim().length()==0) {
			return null;
		}
		String url = Constants.GET_FLV_STRING.replace("{$mmsid}", mid);
		String json = HttpUtil.doGet2(url, Constants.CHARSET);
		if(CheckJson(json)){
			try {
				JSONObject jObject = new JSONObject(json);
				String bean = jObject.getString("bean");
				if (CheckJson(bean)) {
					JSONObject beanObject = new JSONObject(bean);
					String flv = beanObject.getString("flv");
					JSONObject flvObject = new JSONObject(flv);
					JSONArray blockJSONArray = flvObject.getJSONArray("block");
					JSONObject mJSONObject = blockJSONArray.getJSONObject(0);
					
					String urlStr = mJSONObject.getString("url");
					if (urlStr != null) {
						String df = getUrlValue(urlStr, "df");
						String br = getUrlValue(urlStr, "br");
						if (df==null || df.trim().length()==0 || br==null || br.trim().length()==0 ) {
							return null;
						}
						else {
							String urlString="http://g3.letv.cn/"+df+"?expect=6&amp;br="+br+"&amp;tag=box";
							String xmlStr = HttpUtil.doGet(urlString, Constants.CHARSET, 5000);
							try {
								String playURLStr = AndroidXmlParser.readXMLPlayURL(xmlStr);
								return playURLStr;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		// 测试，随时可以删除
//		String urlString = "http://g3.letv.cn/13/52/85/2131580282.0.flv?expect=6&amp;br=1782&amp;tag=box";
//		String xmlStr = HttpUtil.doGet(urlString, "GBK", 5000);
		return null;
	}
}
