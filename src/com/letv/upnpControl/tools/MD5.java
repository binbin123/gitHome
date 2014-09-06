package com.letv.upnpControl.tools;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @title: 
 * @description: MD5加密工具类 
 * @company: 乐视网信息技术（北京）股份有限公司 
 * @author 于庭龙 
 * 
 1.0
 * @created 2012-2-7 下午2:35:29
 * @changeRecord
 */
public class MD5 {
	/**
	 * 将字符串变换程MD5加密字符串
	 * @param md5Str
	 * @return
	 */
	public static String toMd5(String md5Str) {
		String result = "";
		try {
			MessageDigest algorithm = MessageDigest.getInstance("MD5");
			algorithm.reset();
			algorithm.update(md5Str.getBytes("utf-8"));
			result = toHexString(algorithm.digest());
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}

	private static String toHexString(byte[] bytes) {
		StringBuilder hexString = new StringBuilder();

		for (int b : bytes) {
			if (b < 0){
				b += 256;
			}
			if (b < 16){
				hexString.append("0");				
			}
			hexString.append(Integer.toHexString(b));
		}
		return hexString.toString();
	}
}
