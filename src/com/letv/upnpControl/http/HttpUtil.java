package com.letv.upnpControl.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import com.letv.upnpControl.tools.Constants;
import com.letv.upnpControl.tools.LetvLog;

/**
 * @title: HTTP连接工具类
 * @description: HTTP连接工具类
 * @company: 乐视网信息技术（北京）股份有限公司 
 * @author 于庭龙 
 * @version 1.0
 * @created 2012-2-6 下午7:00:39
 * @changeRecord
 */
public class HttpUtil {
	/**
	 * 连接超时
	 */
	private static int connectTimeOut = 15*1000;

	/**
	 * 读取数据超时
	 */
	private static int readTimeOut = 8000;

	/**
	 * 请求编码
	 */
	private static String requestEncoding = "utf-8";
	
	private static String TAG = HttpUtil.class.getSimpleName();

	/**
	 * @return 连接超时(毫秒)
	 * @see com.hengpeng.common.web.HttpRequestProxy#connectTimeOut
	 */
	public static int getConnectTimeOut() {
		return HttpUtil.connectTimeOut;
	}

	/**
	 * @return 读取数据超时(毫秒)
	 * @see com.hengpeng.common.web.HttpRequestProxy#readTimeOut
	 */
	public static int getReadTimeOut() {
		return HttpUtil.readTimeOut;
	}

	/**
	 * @return 请求编码
	 * @see com.hengpeng.common.web.HttpRequestProxy#requestEncoding
	 */
	public static String getRequestEncoding() {
		return requestEncoding;
	}

	/**
	 * @param connectTimeOut
	 *            连接超时(毫秒)
	 * @see com.hengpeng.common.web.HttpRequestProxy#connectTimeOut
	 */
	public static void setConnectTimeOut(int connectTimeOut) {
		HttpUtil.connectTimeOut = connectTimeOut;
	}

	/**
	 * @param readTimeOut
	 *            读取数据超时(毫秒)
	 * @see com.hengpeng.common.web.HttpRequestProxy#readTimeOut
	 */
	public static void setReadTimeOut(int readTimeOut) {
		HttpUtil.readTimeOut = readTimeOut;
	}

	/**
	 * @param requestEncoding
	 *            请求编码
	 * @see com.hengpeng.common.web.HttpRequestProxy#requestEncoding
	 */
	public static void setRequestEncoding(String requestEncoding) {
		HttpUtil.requestEncoding = requestEncoding;
	}

	/**
	 * <pre>
	 * 发送带参数的GET的HTTP请求
	 * </pre>
	 * 
	 * @param reqUrl
	 *            HTTP请求URL
	 * @param parameters
	 *            参数映射表
	 * @return HTTP响应的字符串
	 * 
	 *         基本可用，支持重定向URL访问， 但没有做深度测试
	 */
	public static String doGet(String reqUrl, Map<String, String> parameters,
			String recvEncoding) {
		String responseContent = null;
		HttpPost httpRequest = new HttpPost(reqUrl);

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		for (Iterator<Entry<String, String>> iter = parameters.entrySet()
				.iterator(); iter.hasNext();) {
			Entry<String, String> element = (Entry<String, String>) iter.next();
			params.add(new BasicNameValuePair(element.getKey().toString(),
					element.getValue().toString()));
		}

		try {
			// 发出HTTP request
			httpRequest
					.setEntity(new UrlEncodedFormEntity(params, recvEncoding));
			// 取得HTTP response
			DefaultHttpClient client = new DefaultHttpClient();// 浏览器

			client.getParams().setParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT, 8000);
			client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
					8000);

			HttpResponse httpResponse = client.execute(httpRequest);
			// 若状态码为200 ok
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				// 取出回应字串
				responseContent = EntityUtils
						.toString(httpResponse.getEntity());
			} else {

			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return responseContent;
	}

	/**
	 * 
	 * @param reqUrl
	 * @param recvEncoding
	 * @return
	 */
	public static String doGet2(String reqUrl, String recvEncoding) {
		String responseContent = null;
		HttpPost httpRequest = new HttpPost(reqUrl);

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		try {
			// 发出HTTP request
			httpRequest
					.setEntity(new UrlEncodedFormEntity(params, recvEncoding));
			// 取得HTTP response
			HttpResponse httpResponse = new DefaultHttpClient()
					.execute(httpRequest);

			// 若状态码为200 ok
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				// 取出回应字串
				responseContent = EntityUtils
						.toString(httpResponse.getEntity());
			} else {

			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return responseContent;
	}

	/**
	 * <pre>
	 * 发送不带参数的GET的HTTP请求
	 * </pre>
	 * 
	 * @param reqUrl
	 *            HTTP请求URL
	 * @return HTTP响应的字符串
	 * 
	 *         未调，不确定是否可用
	 */
	public static String doGet(String reqUrl, String recvEncoding, int timeOut) {

		HttpURLConnection url_con = null;
		String responseContent = null;
		try {
			String queryUrl = reqUrl;
			URL url = new URL(queryUrl);

			url_con = (HttpURLConnection) url.openConnection();

			url_con.setRequestMethod("GET");
			// System.setProperty("sun.net.client.defaultConnectTimeout",
			// String.valueOf(HttpUtil.connectTimeOut));//
			// （单位：毫秒）jdk1.4换成这个,连接超时
			// System.setProperty("sun.net.client.defaultReadTimeout",
			// String.valueOf(HttpUtil.readTimeOut)); // （单位：毫秒）jdk1.4换成这个,读操作超时
			if (timeOut == 0) {
				url_con.setConnectTimeout(HttpUtil.connectTimeOut);// （单位：毫秒）jdk
																	// 1.5换成这个,连接超时
				url_con.setReadTimeout(HttpUtil.readTimeOut);// （单位：毫秒）jdk
																// 1.5换成这个,读操作超时
			} else {
				url_con.setConnectTimeout(timeOut);// （单位：毫秒）jdk 1.5换成这个,连接超时
				url_con.setReadTimeout(timeOut);// （单位：毫秒）jdk 1.5换成这个,读操作超时
			}
			url_con.setDoOutput(false);
			url_con.setDoInput(true);

			InputStream in = url_con.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(in,
					recvEncoding));
			String tempLine = rd.readLine();
			StringBuffer temp = new StringBuffer();
			String crlf = System.getProperty("line.separator");
			while (tempLine != null) {
				temp.append(tempLine);
				temp.append(crlf);
				tempLine = rd.readLine();
			}
			responseContent = temp.toString();
			rd.close();
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (url_con != null) {
				url_con.disconnect();
			}
		}

		return responseContent;
	}

	/**
	 * <pre>
	 * 发送带参数的POST的HTTP请求
	 * </pre>
	 * 
	 * @param reqUrl
	 *            HTTP请求URL
	 * @param parameters
	 *            参数映射表
	 * @return HTTP响应的字符串
	 * 
	 */
	public static boolean doPost2(String pathUrl, String requestString,
			String recvEncoding) {
		boolean flag = false;
		try {
			// 建立连接
			URL url = new URL(pathUrl);
			HttpURLConnection httpConn = (HttpURLConnection) url
					.openConnection();

			// 设置连接属性
			httpConn.setDoOutput(true);// 使用 URL 连接进行输出
			httpConn.setDoInput(true);// 使用 URL 连接进行输入
			httpConn.setUseCaches(false);// 忽略缓存
			httpConn.setRequestMethod("POST");// 设置URL请求方法

			// 设置请求属性
			// 获得数据字节数据，请求数据流的编码，必须和下面服务器端处理请求流的编码一致
			byte[] requestStringBytes = requestString
					.getBytes(Constants.CHARSET);
			httpConn.setRequestProperty("Content-length", ""
					+ requestStringBytes.length);
			httpConn.setRequestProperty("Content-Type",
					"application/octet-stream");
			// httpConn.setRequestProperty("Connection", "Keep-Alive");// 维持长连接
			httpConn.setRequestProperty("Charset", "UTF-8");
			httpConn.setConnectTimeout(connectTimeOut);
			httpConn.setReadTimeout(readTimeOut);// （单位：毫秒）jdk 1.5换成这个,读操作超时

			// String name = URLEncoder.encode("黄武艺", Constants.CHARSET);
			// httpConn.setRequestProperty("NAME", name);

			// 建立输出流，并写入数据
			OutputStream outputStream = httpConn.getOutputStream();
			outputStream.write(requestStringBytes);
			outputStream.close();
			// 获得响应状态
			int responseCode = httpConn.getResponseCode();
			if (HttpURLConnection.HTTP_OK == responseCode) {// 连接成功
				flag = true;
				// 当正确响应时处理数据
				StringBuffer sb = new StringBuffer();
				String readLine;
				BufferedReader responseReader;
				// 处理响应流，必须与服务器响应流输出的编码一致
				responseReader = new BufferedReader(new InputStreamReader(
						httpConn.getInputStream(), Constants.CHARSET));
				while ((readLine = responseReader.readLine()) != null) {
					sb.append(readLine).append("\n");
				}
				responseReader.close();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return flag;
	}

	/**
	 * <pre>
	 * 发送带参数的POST的HTTP请求
	 * </pre>
	 * 
	 * @param reqUrl
	 *            HTTP请求URL
	 * @param parameters
	 *            参数映射表
	 * @return HTTP响应的字符串
	 * 
	 *         已调，可用 于庭龙
	 */
	public static String doPost(String reqUrl, String parameters,String recvEncoding) {
		HttpURLConnection url_con = null;
		String responseContent = null;

		try {
			URL url = new URL(reqUrl);
			url_con = (HttpURLConnection) url.openConnection();
			url_con.setRequestMethod("POST");
			url_con.setConnectTimeout(connectTimeOut);// （单位：毫秒）jdk
			// 1.5换成这个,连接超时
			url_con.setReadTimeout(readTimeOut);// （单位：毫秒）jdk 1.5换成这个,读操作超时
			url_con.setDoOutput(true);

			byte[] b = parameters.getBytes();

			url_con.getOutputStream().write(b, 0, b.length);
			url_con.getOutputStream().flush();
			url_con.getOutputStream().close();
			// url_con.connect();
			InputStream in = url_con.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(in,
					recvEncoding));

			String tempLine = rd.readLine();

			StringBuffer tempStr = new StringBuffer();
			String crlf = System.getProperty("line.separator");// 改平台下行与行的分隔符
			while (tempLine != null) {// 循环获取数据
				tempStr.append(tempLine);
				tempStr.append(crlf);
				tempLine = rd.readLine();
			}
			responseContent = tempStr.toString();
			rd.close();
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
			responseContent =  "connect timeout";
		}
		finally {
			if (url_con != null) {
				url_con.disconnect();
			}
		}
		LetvLog.d(TAG,"responseContent = " + responseContent);
		return responseContent;
	}
}
