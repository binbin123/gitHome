package com.letv.upnpControl.tools;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @title: 
 * @description: 
 * @company: 涔愯缃戜俊鎭妧鏈紙鍖椾含锛夎偂浠芥湁闄愬叕鍙�
 * @author 浜庡涵榫�
 * @version 1.0
 * @created 2012-2-26 涓婂崍3:32:29
 * @changeRecord
 */
public class AndroidXmlParser extends BaseXmlParser{
	/**
	 *	鍛藉悕绌洪棿
	 */
	public static final String NAMESPACE = null;
	
	/**
	 * 瑙ｆ瀽XML涓殑鎾斁鍦板潃
	 * @param resultStr
	 * @return
	 * @throws Exception
	 */
	public static String readXMLPlayURL(String resultStr) throws Exception {
		InputStream inputStream = parseString2Stream(resultStr);// 鑾峰彇瀛楃娴�		
		Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream);
		
		Element root = document.getDocumentElement();
		NodeList nodes = root.getElementsByTagName("node");
		Node node = nodes.item(0);
		String url = node.getFirstChild().getNodeValue();
		
		return url;
		
		
//		LetvLog.e("TAG", "url : " + url);
//		String url2 =node.getTextContent(); 
//		LetvLog.e("TAG", "url2 : " + url2);
//		NodeList nodelist = dom.getElementsByTagName("nodelist");
//		Node n =  nodelist.item(0);//nodelist
//		String url = n.getFirstChild().getTextContent();
		
		
		
//		String result = "";
//		InputStream inputStream = parseString2Stream(resultStr);// 鑾峰彇瀛楃娴�//		// 鍙栧緱xml瑙ｆ瀽瀹炰緥
//		XmlPullParser pullParser = Xml.newPullParser();
//		// 璁剧疆瑙ｆ瀽鐨勬枃妗ｆ祦
//		pullParser.setInput(inputStream, "UTF-8");
//
//		// 浜х敓绗竴涓簨浠�//		int eventType = pullParser.getEventType();
//		while (eventType != XmlPullParser.END_DOCUMENT) {
//			switch (eventType) {
//			// 寮�鏂囨。浜嬩欢
//			case XmlPullParser.START_DOCUMENT:
//				break;
//			// 寮�鍏冪礌浜嬩欢
//			case XmlPullParser.START_TAG:
//				// 鑾峰彇褰撳墠鎸囧畾鍏冪礌鐨勫悕绉�//				String nodeName = pullParser.getName();
//				if (nodeName.equals("nodelist")) {
//					
//					
//					
//					result = LetvUtils.getTranscodingString(pullParser.getAttributeValue(NAMESPACE, "node"));
//					
//					LetvLog.e("TAG", "result = " + result);
//				}
//				break;
//			}
//			// 杩涘叆涓嬩竴涓厓绱犲苟瑙﹀彂鐩稿簲鐨勪簨浠�//			eventType = pullParser.next();
//		}
//
//		inputStream.close();
//		return url;
	}
}
