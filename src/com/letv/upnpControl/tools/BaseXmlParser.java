package com.letv.upnpControl.tools;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * @title: 
 * @description: 
 * @company: 乐视网信息技术（北京）股份有限公司 
 * @author 于庭龙 
 * @version 1.0
 * @created 2012-2-26 上午3:32:00
 * @changeRecord
 */
public class BaseXmlParser {
	/**
	 * 将字符串转换成流
	 * @param response 需要转换的字符串
	 * @return InputStream数据流对象 
	 */
	protected static InputStream parseString2Stream(String response){
		ByteArrayInputStream in = new ByteArrayInputStream(response.getBytes());
		return in;
	}
}
