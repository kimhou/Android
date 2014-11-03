package com.tencent.cos.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;


/**
 * 基础字符处理函数
 * 
 * @author mucdullge
 * 
 */
public class BaseFun {

	/**
	 * 通过http://xxxx.xxxx.xxx或https://xxxx.xxxx.xxx提取出xxxx.xxxx.xxx
	 * 
	 * @param url
	 * @return
	 */
	public static String getHostName(String url) {
		if (url == null)
			return "";

		url = url.trim();
		String host = url.toLowerCase();
		int end;

		if (host.startsWith("http://")) {
			end = url.indexOf("/", 8);
			if (end > 7) {
				host = url.substring(7, end);
			} else {
				host = url.substring(7);
			}

		} else if (host.startsWith("https://")) {
			end = url.indexOf("/", 9);
			if (end > 8) {
				host = url.substring(7, end);
			} else {
				host = url.substring(8);
			}
		} else {
			end = url.indexOf("/", 1);
			if (end > 1) {
				host = url.substring(0, url.indexOf("/", 1));
			} else {
				host = url;
			}
		}

		return host;
	}

	/**
	 * ip转换为http://ip
	 * 
	 * @param host
	 * @return
	 */
	public static String getUrlHost(String host) {
		if (host == null)
			return "";

		host = host.trim();

		String host_lower = host.toLowerCase();

		String url_host = "http://" + host_lower;
		return url_host;
	}

	/**
	 * 去除字符首或者尾第一个指定字符
	 * 
	 * @param source
	 * @param param
	 *            为空时，指定删除字符首或者尾字符
	 * @param head
	 *            ture 为剔除字符首，false 为剔除字符尾
	 * @return
	 */
	public static String SubString(String source, String param, boolean head) {

		if (source == null) {
			return null;
		}

		int size = source.length();

		if (param == null || param == "") {
			if (head) {
				return source.substring(0, size - 1);
			} else {
				return source.substring(1, size);
			}
		} else {
			int offset = 0;

			offset = source.indexOf(param);
			if (offset == -1)
				return source;

			if (head) {
				return source.substring(offset + 1, size);
			} else {
				offset = source.lastIndexOf(param);
				return source.substring(0, offset);
			}

		}

	}

	/**
	 * 针对字符实现rawurlencode转换
	 * 
	 * @param source
	 * @return
	 */
	public static String RawurlEncode(String source) {

		if (source == null) {
			return null;
		}

		try {
			source = java.net.URLEncoder.encode(source, "utf-8");
			source = source.replace("+", "%20");
			return source;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 判断值是否为空
	 * 
	 * @param value
	 * @return
	 */
	public static boolean CheckValueNull(Object value) {
		if (value == null)
			return true;

		String valueString = String.valueOf(value);

		if (valueString.equals(""))
			return true;

		return false;
	}

	/**
	 * 判断值是否为数字
	 * 
	 * @param value
	 * @return
	 */
	public static boolean CheckValueNum(String value) {
		if (value == null)
			return false;

		Pattern pattern = Pattern.compile("[0-9]*");
		return pattern.matcher(value).matches();
	}
	
	/**
	 * 把文件中的数据按照字节读出
	 * @param FilePath 文件绝对路径
	 * @return
	 */
	public static byte[] readFile(String FilePath) {
		byte[] buffer = null;
		FileInputStream fin = null;
		try {
			fin = new FileInputStream(FilePath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		int length = 0;
		try {
			length = fin.available();
		    buffer = new byte[length];
			fin.read(buffer);
			fin.close();
			return buffer;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 获取文件名
	 * @param filePath 文件绝对路径
	 * @return
	 */
	public static String GetFileName(String filePath) {
		int offset = filePath.lastIndexOf("/");

		return filePath.substring(offset + 1);
	}
}
