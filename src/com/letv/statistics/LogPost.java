package com.letv.statistics;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.Properties;
import com.letv.upnpControl.tools.LetvLog;


public class LogPost {

	public static String reqForPost(String urlString, String requestData)
			throws Exception {
		Properties requestProperties = new Properties();
		requestProperties.setProperty("Content-Type",
				"application/json; charset=utf-8");

		return requestPost(urlString, requestData, requestProperties);
	}

	public static String requestPost(String postURL, String context,
			Properties requestProperties) throws Exception {
		HttpURLConnection httpUrlConn = null;
		StringBuffer sb = null;
		try {

			URL url = new URL(postURL);

			URLConnection urlConn = url.openConnection();

			httpUrlConn = (HttpURLConnection) urlConn;

			if ((requestProperties != null) && (requestProperties.size() > 0)) {
				for (Map.Entry<Object, Object> entry : requestProperties
						.entrySet()) {
					String key = String.valueOf(entry.getKey());
					String value = String.valueOf(entry.getValue());
					httpUrlConn.setRequestProperty(key, value);
				}
			}

			/*
			 * 设置是否向HttpURLConnection输出，因为这个是post请求，
			 * 参数要放在http正文内，因此需要设为true，默认是false
			 */
			httpUrlConn.setDoOutput(true);
			/* 设置是否从HttpURLConnection读入，默认是true */
			httpUrlConn.setDoInput(true);
			/* post请求不能使用缓存 */
			httpUrlConn.setUseCaches(false);
			/* 设置请求方法为"POST",默认为"GET" */
			httpUrlConn.setRequestMethod("POST");

			OutputStreamWriter wr = new OutputStreamWriter(
					httpUrlConn.getOutputStream());

			wr.write(context);

			wr.flush();

			BufferedReader in = new BufferedReader(new InputStreamReader(
					httpUrlConn.getInputStream()));

			String line;

			sb = new StringBuffer();

			while ((line = in.readLine()) != null) {

				sb.append(line);

			}

			wr.close();

			in.close();
			if (sb.toString() != null) {
				LetvLog.i("logpost", "response:" + sb.toString());
			}

		} catch (Exception e) {

			throw e;

		} finally {
			if (httpUrlConn != null) {
				httpUrlConn.disconnect();
				httpUrlConn = null;
			}
		}

		return sb.toString();

	}
}
