package com.tencent.cos.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import android.content.Context;

import com.tencent.cos.bean.HttpHeader;
import com.tencent.cos.bean.HttpResponse;
import com.tencent.cos.constant.CosConst;
import com.tencent.cos.util.BaseFun;
import com.tencent.cos.util.COSLog;
import com.tencent.stat.StatService;

public class HttpGetThread implements Runnable {
	
	private static final String Tag = HttpGetThread.class.getName();

	private String url;
	private Map params;
	private HttpHeader httpHeader;
	private Context context;
	private HttpResponse httpResponse;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Map getParams() {
		return params;
	}

	public void setParams(Map params) {
		this.params = params;
	}

	public HttpHeader getHttpHeader() {
		return httpHeader;
	}

	public void setHttpHeader(HttpHeader httpHeader) {
		this.httpHeader = httpHeader;
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public HttpResponse getHttpResponse() {
		return httpResponse;
	}

	public void setHttpResponse(HttpResponse httpResponse) {
		this.httpResponse = httpResponse;
	}
	
	/**
	 * 由于android 4.0版本网络请求只能采用线程方式来访问，即在http请求方面采用统一方式
	 */
	@Override
	public void run() {
		String paramStr = "";
		BufferedReader reader = null;
		Iterator iter = this.params.entrySet().iterator();
		
		CosConst.httpResult = "";
		
		if(httpResponse == null){
			CosConst.httpResult = null;
			return ;
		}
		
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			Object key = entry.getKey();
			Object val = entry.getValue();
			paramStr = paramStr + "&" + key + "="
					+ BaseFun.RawurlEncode(String.valueOf(val));
		}
		
		if (!paramStr.equals("")) {
			paramStr = paramStr.replaceFirst("&", "?");
			this.url = this.url + paramStr;
		}
		
		COSLog.i(Tag, "url : " + this.url);
		
		HttpURLConnection urlConn = null;
		try {
			URL url = new URL(this.url);
			urlConn = (HttpURLConnection) url.openConnection();
			urlConn.setRequestMethod("GET");

			if (this.httpHeader == null) {
				urlConn.setConnectTimeout(CosConst.TIMEOUT);
				urlConn.setRequestProperty("Host", CosConst.HOST);
			} else {
				if (this.httpHeader.getHost() == null || this.httpHeader.getHost() == "") {
					urlConn.setRequestProperty("Host", CosConst.HOST);
				} else {
					urlConn.setRequestProperty("Host", this.httpHeader.getHost());
				}

				if (this.httpHeader.getTimeOut() == 0) {
					urlConn.setConnectTimeout(CosConst.TIMEOUT);
				} else {
					urlConn.setConnectTimeout(this.httpHeader.getTimeOut());
				}
			}

			urlConn.setDoInput(true);
			urlConn.setDoOutput(true);
			urlConn.setUseCaches(false);
			urlConn.setInstanceFollowRedirects(true);
			urlConn.setRequestProperty("Charset", "UTF-8");
			urlConn.connect();
			
			String requestId = urlConn.getHeaderField(CosConst.HTTPID);
			
			httpResponse.setResponseId(requestId);
			
			StatService.trackCustomEvent(context, CosConst.HTTPID, requestId);
			
			reader = new BufferedReader(new InputStreamReader(
					urlConn.getInputStream(), "utf-8"));
			String line = "";

			while ((line = reader.readLine()) != null) {
				CosConst.httpResult = CosConst.httpResult + line;
			}
			
			COSLog.i(Tag, "get result : " + CosConst.httpResult);

			urlConn.disconnect();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			urlConn.disconnect();
			CosConst.httpResult = null;
			return ;
		} catch (IOException e) {
			e.printStackTrace();
			CosConst.httpResult = null;
			return ;
		}
		return ;
		
	}

}
