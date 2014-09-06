package com.letv.upnpControl.tools;

import com.letv.upnpControl.http.JsonGet;

public class PlayUtils {
	/**
	 * 获取盒端到URL播放地址
	 * @param mid
	 * @return
	 */
	public static String getFlvUrl(String mid) {
		String mp4Url = null;
		for(int i=0; i<3; i++){//尝试获取三次
			mp4Url = JsonGet.getLFV(mid);
			if(mp4Url != null){
				break;
			}
		}
//		System.out.println("id:"+mid+ "   MP4:"+mp4Url);
		
		return mp4Url;
	}
}
