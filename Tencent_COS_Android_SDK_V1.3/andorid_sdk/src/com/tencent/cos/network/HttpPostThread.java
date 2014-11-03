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

import android.content.Context;

import com.tencent.cos.bean.HttpHeader;
import com.tencent.cos.bean.HttpResponse;
import com.tencent.cos.constant.CosConst;
import com.tencent.cos.util.BaseFun;
import com.tencent.cos.util.COSLog;

public class HttpPostThread implements Runnable{
	
	private static final String Tag = HttpPostThread.class.getName();

	private String url;

	private Map params;

	private byte[] fileData;

	private String filename;

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

	public byte[] getFileData() {
		return fileData;
	}

	public void setFileData(byte[] fileData) {
		this.fileData = fileData;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
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



	@Override
	public void run() {
		String paramStr = "";
		Iterator iter = this.params.entrySet().iterator();
		
		CosConst.httpResult = "";

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
		
		BufferedReader reader=null;
		HttpURLConnection httpURLConnection = null;
		try {
			String end = "\r\n";
	        String twoHyphens = "--";
	        String boundary = "*****";
			URL url = new URL(this.url);
			httpURLConnection = (HttpURLConnection) url
					.openConnection();
			
			if(this.httpHeader == null){
				httpURLConnection.setRequestProperty("Host", CosConst.HOST);
			}else{
				if(this.httpHeader.getHost() == null || this.httpHeader.getHost() == ""){
					httpURLConnection.setRequestProperty("Host", CosConst.HOST);
				}else{
					httpURLConnection.setRequestProperty("Host", this.httpHeader.getHost());
				}
			}
			
			httpURLConnection.setDoInput(true);
			httpURLConnection.setDoOutput(true);
			httpURLConnection.setUseCaches(false);
			// 使用POST方法
			httpURLConnection.setRequestMethod("POST");
			httpURLConnection.setInstanceFollowRedirects(true);
			httpURLConnection.setRequestProperty("Charset", "UTF-8");
			httpURLConnection.setRequestProperty("Content-Type",
                     "multipart/form-data;boundary="+boundary);
		
			httpURLConnection.connect();
			DataOutputStream dos = new DataOutputStream(
					httpURLConnection.getOutputStream());
			dos.writeBytes(twoHyphens + boundary + end);
			
			dos.writeBytes("Content-Disposition: form-data; name=\"cosFile\"; filename=\""+filename+"\""+end);
			dos.writeBytes("Content-Type: image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword "+end);
			
			dos.writeBytes(end);
			dos.write(this.fileData);
			
			
			String requestId = httpURLConnection.getHeaderField(CosConst.HTTPID);
			this.httpResponse.setResponseId(requestId);
			
			reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "utf-8"));
			String line="";
			while ((line = reader.readLine()) != null){
				CosConst.httpResult = CosConst.httpResult + line;
			}
			COSLog.i(Tag, "get result : "+CosConst.httpResult);
			dos.flush();
			dos.close();
			httpURLConnection.disconnect();
			return ;
		} catch (MalformedURLException e) {
			e.printStackTrace();
			httpURLConnection.disconnect();
			CosConst.httpResult = null;
			return ;
		} catch (IOException e) {
			e.printStackTrace();
			httpURLConnection.disconnect();
			CosConst.httpResult = null;
			return ;
		}
	}

}
