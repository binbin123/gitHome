package com.letv.screenui.activity.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;

import android.util.Log;

public class HttpUtil {

	private static int connectTimeOut = 8000;

	private static int readTimeOut = 8000;

	private static String requestEncoding = "utf-8";

	private static final AllowAllHostnameVerifier HOSTNAME_VERIFIER = new AllowAllHostnameVerifier();
	private static X509TrustManager xtm = new X509TrustManager() {
		public void checkClientTrusted(X509Certificate[] chain, String authType) {
		}

		public void checkServerTrusted(X509Certificate[] chain, String authType) {
		}

		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}
	};
	private static X509TrustManager[] xtmArray = new X509TrustManager[] { xtm };

	public static int getConnectTimeOut() {
		return HttpUtil.connectTimeOut;
	}

	public static int getReadTimeOut() {
		return HttpUtil.readTimeOut;
	}

	public static String getRequestEncoding() {
		return requestEncoding;
	}

	/**
	 * @param connectTimeOut
	 *            杩炴帴瓒呮椂(姣�?
	 * @see com.hengpeng.common.web.HttpRequestProxy#connectTimeOut
	 */
	public static void setConnectTimeOut(int connectTimeOut) {
		HttpUtil.connectTimeOut = connectTimeOut;
	}

	/**
	 * @param readTimeOut
	 * 
	 * @see com.hengpeng.common.web.HttpRequestProxy#readTimeOut
	 */
	public static void setReadTimeOut(int readTimeOut) {
		HttpUtil.readTimeOut = readTimeOut;
	}

	/**
	 * @param requestEncoding
	 * 
	 * @see com.hengpeng.common.web.HttpRequestProxy#requestEncoding
	 */
	public static void setRequestEncoding(String requestEncoding) {
		HttpUtil.requestEncoding = requestEncoding;
	}

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
			httpRequest
					.setEntity(new UrlEncodedFormEntity(params, recvEncoding));

			DefaultHttpClient client = new DefaultHttpClient();//

			client.getParams().setParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT, 8000);
			client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
					8000);

			HttpResponse httpResponse = client.execute(httpRequest);
			// 200 ok
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				//
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
		Log.i("TAG", "give HTTP data is锛� " + responseContent);
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

			httpRequest
					.setEntity(new UrlEncodedFormEntity(params, recvEncoding));
			Log.d("TAG", "doGet2 url is : " + httpRequest.getURI());

			HttpResponse httpResponse = new DefaultHttpClient()
					.execute(httpRequest);
			Log.d("TAG", "httpResponse.getStatusLine().getStatusCode()  = "
					+ httpResponse.getStatusLine().getStatusCode());

			if (httpResponse.getStatusLine().getStatusCode() == 200) {

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
		Log.i("TAG", "give http data is : " + responseContent);
		return responseContent;
	}

	public static String doGet(String reqUrl, String recvEncoding, int timeOut) {

		HttpURLConnection url_con = null;
		String responseContent = null;
		try {
			String queryUrl = reqUrl;
			Log.d("TAG", "doGet url is: " + reqUrl);
			URL url = new URL(queryUrl);

			url_con = (HttpURLConnection) url.openConnection();

			url_con.setRequestMethod("GET");

			if (timeOut == 0) {
				url_con.setConnectTimeout(HttpUtil.connectTimeOut);//

				url_con.setReadTimeout(HttpUtil.readTimeOut);
			} else {
				url_con.setConnectTimeout(timeOut);
				url_con.setReadTimeout(timeOut);
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
		} finally {
			if (url_con != null) {
				url_con.disconnect();
			}
		}

		return responseContent;
	}

	public static boolean doPost2(String pathUrl, String requestString,
			String recvEncoding) {
		boolean flag = false;
		try {

			URL url = new URL(pathUrl);
			HttpURLConnection httpConn = (HttpURLConnection) url
					.openConnection();

			httpConn.setDoOutput(true);
			httpConn.setDoInput(true);
			httpConn.setUseCaches(false);
			httpConn.setRequestMethod("POST");

			byte[] requestStringBytes = requestString
					.getBytes(Constants.CHARSET);
			httpConn.setRequestProperty("Content-length", ""
					+ requestStringBytes.length);
			httpConn.setRequestProperty("Content-Type",
					"application/octet-stream");
			httpConn.setRequestProperty("Charset", "UTF-8");
			httpConn.setConnectTimeout(connectTimeOut);
			httpConn.setReadTimeout(readTimeOut);

			OutputStream outputStream = httpConn.getOutputStream();
			outputStream.write(requestStringBytes);
			outputStream.close();

			int responseCode = httpConn.getResponseCode();
			Log.e("TAG", "responseCode = " + responseCode);
			if (HttpURLConnection.HTTP_OK == responseCode) {
				flag = true;

				StringBuffer sb = new StringBuffer();
				String readLine;
				BufferedReader responseReader;

				responseReader = new BufferedReader(new InputStreamReader(
						httpConn.getInputStream(), Constants.CHARSET));
				while ((readLine = responseReader.readLine()) != null) {
					sb.append(readLine).append("\n");
				}
				Log.d("TAG", "HttpPost response = " + sb.toString());
				responseReader.close();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return flag;
	}

	public static String doPost(String reqUrl, String parameters,
			String recvEncoding) {
		HttpURLConnection url_con = null;
		String responseContent = null;

		try {
			URL url = new URL(reqUrl);
			url_con = (HttpURLConnection) url.openConnection();
			url_con.setRequestMethod("POST");
			url_con.setConnectTimeout(connectTimeOut);
			url_con.setReadTimeout(readTimeOut);
			url_con.setDoOutput(true);

			byte[] b = parameters.getBytes();
			url_con.getOutputStream().write(b, 0, b.length);
			url_con.getOutputStream().flush();
			url_con.getOutputStream().close();
			// url_con.connect();
			InputStream in = url_con.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(in,
					recvEncoding));
			Log.e("TAG",
					"url_con.getResponseCode() = " + url_con.getResponseCode());

			String tempLine = rd.readLine();

			StringBuffer tempStr = new StringBuffer();
			String crlf = System.getProperty("line.separator");
			while (tempLine != null) {
				tempStr.append(tempLine);
				tempStr.append(crlf);
				tempLine = rd.readLine();
			}
			responseContent = tempStr.toString();
			rd.close();
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (url_con != null) {
				url_con.disconnect();
			}
		}
		return responseContent;
	}

	// test do Https post
	public static String doPost(String reqUrl, String recvEncoding, int timeOut) {
		HttpsURLConnection url_con = null;
		String responseContent = null;
		String uri = null;
		int paramIndex = reqUrl.indexOf("?");
		StringBuffer params = new StringBuffer();
		if (paramIndex > 0) {
			uri = reqUrl.substring(0, paramIndex);
			String parameters = reqUrl.substring(paramIndex + 1,
					reqUrl.length());
			String[] paramArray = parameters.split("&");
			for (int i = 0; i < paramArray.length; i++) {
				String strPara = paramArray[i];
				int index = strPara.indexOf("=");
				if (index > 0) {
					String parameter = strPara.substring(0, index);
					String value = strPara.substring(index + 1,
							strPara.length());
					params.append(parameter);
					params.append("=");
					try {
						params.append(URLEncoder.encode(value,
								Constants.CHARSET));
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					params.append("&");
				}
			}
			params = params.deleteCharAt(params.length() - 1);
		}
		try {
			String queryUrl = uri;
			Log.d("TAG", "doPost url is: " + reqUrl);
			URL url = new URL(queryUrl);
			url_con = (HttpsURLConnection) url.openConnection();
			SSLContext context = SSLContext.getInstance("TLS");
			context.init(new KeyManager[0], xtmArray, new SecureRandom());
			SSLSocketFactory socketFactory = context.getSocketFactory();
			((HttpsURLConnection) url_con).setSSLSocketFactory(socketFactory);
			((HttpsURLConnection) url_con)
					.setHostnameVerifier(HOSTNAME_VERIFIER);
			if (url_con != null) {
				Log.d("LPF", "--connection sucessful--");
			}
			url_con.setRequestMethod("POST");
			if (timeOut == 0) {
				url_con.setConnectTimeout(HttpUtil.connectTimeOut);
				url_con.setReadTimeout(HttpUtil.readTimeOut);
			} else {
				url_con.setConnectTimeout(timeOut);
				url_con.setReadTimeout(timeOut);
			}
			url_con.setDoOutput(true);
			byte[] b = params.toString().getBytes();
			url_con.getOutputStream().write(b);
			url_con.getOutputStream().flush();
			url_con.getOutputStream().close();
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
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (url_con != null) {
				url_con.disconnect();
			}
		}
		Log.d("LPF", "--https--responseContent--" + responseContent);
		return responseContent;
	}
}
