package com.tencent.cos.network;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;



import com.tencent.cos.bean.HttpHeader;
import com.tencent.cos.bean.HttpResponse;
import com.tencent.cos.constant.CosConst;
import com.tencent.cos.util.BaseFun;
import com.tencent.cos.util.COSLog;
import com.tencent.stat.StatService;

import android.content.Context;
import android.util.Log;

/**
 * httpconnect 连接及其get/post处理
 * 
 * @author mucdullge
 * 
 */
public class HttpConnect {

	private static final String Tag = HttpConnect.class.getName();

	public static HttpConnect httpConnect = null;
	
	private Context content ;

	public static HttpConnect getInitialize() {
		httpConnect = new HttpConnect();
		return httpConnect;
	}
	
	/**
	 * 通过http中的GET请求来获取数据
	 * @param Url  get请求url
	 * @param params get请求参数
	 * @param httpHeader httpconnection的head设置，暂时只支持timeout,host设置
	 * @param Context context 页面设备上下文
	 * @param HttpResponse http response返回信息
	 * @return
	 */
	public String doGet(String Url, Map params, HttpHeader httpHeader, Context context, HttpResponse httpResponse) {
		
		HttpGetThread httpGetThread = new HttpGetThread();
		
		httpGetThread.setUrl(Url);
		httpGetThread.setParams(params);
		httpGetThread.setHttpHeader(httpHeader);
		httpGetThread.setContext(context);
		httpGetThread.setHttpResponse(httpResponse);
		
		Thread thread = new Thread(httpGetThread);
		thread.start();
		try {
			thread.join();
		} catch (InterruptedException e) {
			COSLog.e(Tag, e.getMessage());
			e.printStackTrace();
			return null;
		}
		
		return CosConst.httpResult;
	}

	
	/**
	 * 上传文件
	 * @param Url 上传文件http url
	 * @param params 上传文件URL 参数 (基本有cosFile,bucketId,path,time,sign)
	 * @param fileData 文件内容
	 * @param filename 文件名称
	 * @param filename httpconnection的head设置，暂时只支持timeout,host设置
	 * @param Context context 页面设备上下文
	 * @param HttpResponse http response返回信息
	 * @return
	 */
	public String doUpload(String Url, Map params, byte[] fileData,
			String filename, HttpHeader httpHeader,Context context,HttpResponse httpResponse) {
		
		HttpPostThread httpPostThread = new HttpPostThread();
		
		httpPostThread.setUrl(Url);
		httpPostThread.setHttpHeader(httpHeader);
		httpPostThread.setParams(params);
		httpPostThread.setFileData(fileData);
		httpPostThread.setFilename(filename);
		httpPostThread.setContext(context);
		httpPostThread.setHttpResponse(httpResponse);
		
		Thread thread = new Thread(httpPostThread);
		thread.start();
		
		try {
			thread.join();
		} catch (InterruptedException e) {
			COSLog.e(Tag, e.getMessage());
			e.printStackTrace();
			return null;
		}
		
		return CosConst.httpResult;

	}

}
