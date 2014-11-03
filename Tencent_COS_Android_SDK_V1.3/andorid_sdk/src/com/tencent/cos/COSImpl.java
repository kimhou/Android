package com.tencent.cos;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.io.File;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.content.Context;
import android.util.Log;

import com.tencent.cos.COS;
import com.tencent.cos.network.GetDNS;
import com.tencent.cos.network.HttpConnect;
import com.tencent.cos.util.BaseFun;
import com.tencent.cos.util.COSLog;
import com.tencent.cos.util.SignManager;
import com.tencent.cos.bean.CosBucket;
import com.tencent.cos.bean.CosDirectory;
import com.tencent.cos.bean.CosFile;
import com.tencent.cos.bean.HttpHeader;
import com.tencent.cos.bean.HttpResponse;
import com.tencent.cos.bean.Message;
import com.tencent.cos.constant.CosConst;
import com.tencent.dns.Tencent;
import com.tencent.stat.MtaSDkException;
import com.tencent.stat.StatAppMonitor;
import com.tencent.stat.StatConfig;
import com.tencent.stat.StatReportStrategy;
import com.tencent.stat.StatService;
import com.tencent.stat.common.StatConstants;

public class COSImpl implements COS {
	private static final String Tag = COSImpl.class.getName();

	private int _accessId;
	private String _accessKey;
	private String _secretId;

	private static HttpConnect httpConnect = null;

	private HttpHeader httpHeader = null;

	public COSImpl(int accessId, String accessKey, HttpHeader httpHeader,
			Context context) throws Exception {

		if (accessId <= 0) {
			throw new Exception("Invalid accessId!");
		}

		if (accessKey == "") {
			throw new Exception("Invalid accessKey!");
		}

		_accessId = accessId;
		_accessKey = accessKey;
		this.httpHeader = httpHeader;

		StatConfig.setDebugEnable(false);
		StatConfig.setAutoExceptionCaught(true);
		StatConfig.setStatSendStrategy(StatReportStrategy.APP_LAUNCH);

		if (httpConnect == null) {
			httpConnect = HttpConnect.getInitialize();
		}

		try {
			StatService.startStatService(context, CosConst.APPKEY,
					StatConstants.VERSION);
		} catch (MtaSDkException e) {
			e.printStackTrace();
			throw new Exception("Invalid mtasdk!");
		}
	}

	
	public COSImpl(int accessId, String accessKey, String secretId,HttpHeader httpHeader,
			Context context) throws Exception {

		if (accessId <= 0) {
			throw new Exception("Invalid accessId!");
		}

		if (accessKey == "") {
			throw new Exception("Invalid accessKey!");
		}
		
		if(secretId == ""){
			throw new Exception("Invalid sercetId");
		}

		_accessId = accessId;
		_accessKey = accessKey;
		_secretId = secretId;
		this.httpHeader = httpHeader;

		StatConfig.setDebugEnable(false);
		StatConfig.setAutoExceptionCaught(true);
		StatConfig.setStatSendStrategy(StatReportStrategy.APP_LAUNCH);

		if (httpConnect == null) {
			httpConnect = HttpConnect.getInitialize();
		}

		try {
			StatService.startStatService(context, CosConst.APPKEY,
					StatConstants.VERSION);
		} catch (MtaSDkException e) {
			e.printStackTrace();
			throw new Exception("Invalid mtasdk!");
		}
	}

	/**
	 * 解析server传回的json串，获取其中的三个字段："code" "msg" "data"
	 * 
	 * @param String
	 *            jsonStr server传回的json串
	 * @param Message
	 *            message 保存解析后的"code" "msg"字段的值
	 * @param Context
	 *            移动终端页面上下文
	 * 
	 * @return JSONObject dataJsonObj 保存解析后的"data"字段的值 Message message
	 *         保存解析后的"code" "msg"字段的值
	 * 
	 */
	private JSONObject parseJson(String jsonStr, Message message,
			Context context) {
		if ((jsonStr == null) || jsonStr == "" || (message == null)) {
			StatService.reportError(context, "jsonStr message is null");
			return null;
		}

		try {
			JSONObject serverRetJsonObj = new JSONObject(jsonStr);
			Object tempValue = null;
			boolean isParseOk = true;

			// get "code"
			if (!serverRetJsonObj.isNull(CosConst.COS_SERVER_RET_CODE)) {
				tempValue = serverRetJsonObj.get(CosConst.COS_SERVER_RET_CODE);
				int retCode = Integer.parseInt(String.valueOf(tempValue));
				message.setCode(retCode);
			} else {
				message.setCode(-28997);
				message.setMessage("json decode error");
				return null;
			}

			// get "msg"
			if (!serverRetJsonObj.isNull(CosConst.COS_SERVER_RET_MSG)) {
				tempValue = serverRetJsonObj.get(CosConst.COS_SERVER_RET_MSG);
				String retMsg = String.valueOf(tempValue);
				message.setMessage(retMsg);
			} else {
				message.setCode(-28997);
				message.setMessage("json decode error");
				return null;
			}

			// get "data"
			if (!serverRetJsonObj.isNull(CosConst.COS_SERVER_RET_DATA)) {
				tempValue = serverRetJsonObj.get(CosConst.COS_SERVER_RET_DATA);
				String dataString = String.valueOf(tempValue);
				JSONObject dataJsonObj = new JSONObject(dataString);
				return dataJsonObj;
			}

			return null;
		} catch (JSONException e) {
			message.setCode(-28997);
			message.setMessage("json decode error");
			e.printStackTrace();
			StatService.reportException(context, e);
			return null;
		}

	}

	/**
	 * 从server返回的json串中的data段解析 bucket meta信息
	 * 
	 * @param JSONObject
	 *            dataJsonObj server返回的json串中的data段
	 * @param CosBucket
	 *            bucket 返回解析后的bucket meta信息
	 * @param Context
	 *            context 移动终端页面上下文
	 * @return boolean true/false 成功/失败 Bucket bucket 返回解析后的bucket meta信息
	 */
	private boolean parseBucketMeta(JSONObject dataJsonObj, CosBucket bucket,
			Context context) {
		if ((dataJsonObj == null) || (bucket == null)) {
			StatService.reportError(context, "dataJsonObj bucket is null");
			return false;
		}

		try {
			boolean isParseOK = true;

			Object tempValue = null;
			// get "acl"
			if (!dataJsonObj.isNull(CosConst.ACL)) {
				tempValue = dataJsonObj.get(CosConst.ACL);
				int acl = Integer.parseInt(String.valueOf(tempValue));
				bucket.setAcl(acl);
			} else {
				isParseOK = false;
			}

			// get "referer"
			if (!dataJsonObj.isNull(CosConst.REFERER)) {
				tempValue = dataJsonObj.get(CosConst.REFERER);
				String referer = String.valueOf(tempValue);
				bucket.setReferer(referer);
			} else {
				bucket.setReferer("");
			}

			// get "crttime"
			if (!dataJsonObj.isNull(CosConst.CRTTIME)) {
				tempValue = dataJsonObj.get(CosConst.CRTTIME);
				Long crttime = Long.parseLong(String.valueOf(tempValue));
				bucket.setBuildtime(crttime);
			} else {
				isParseOK = false;
			}

			// get "uptime"
			if (!dataJsonObj.isNull(CosConst.UPTIME)) {
				tempValue = dataJsonObj.get(CosConst.UPTIME);
				Long uptime = Long.parseLong(String.valueOf(tempValue));
				bucket.setUpdatetime(uptime);
			} else {
				isParseOK = false;
			}

			// get "type"
			if (!dataJsonObj.isNull(CosConst.TYPE)) {
				tempValue = dataJsonObj.get(CosConst.TYPE);
				int type = Integer.valueOf(String.valueOf(tempValue));
				bucket.setType(type);
			}

			// get "name"
			if (!dataJsonObj.isNull(CosConst.NAME)) {
				tempValue = dataJsonObj.get(CosConst.NAME);
				String name = String.valueOf(tempValue);
				bucket.setName(name);
			}

			return isParseOK;
		} catch (JSONException e) {
			e.printStackTrace();
			StatService.reportException(context, e);
			return false;
		}
	}

	/**
	 * 从server返回的json串中的data段解析 dir meta信息
	 * 
	 * @param JSONObject
	 *            dataJsonObj server返回的json串中的data段
	 * @param CosDirectory
	 *            dir 返回解析后的dir meta信息
	 * @param Context
	 *            context为移动终端页面上下文
	 * @return boolean true/false 成功/失败 CosDirectory dir 返回解析后的dir meta信息
	 */
	private boolean parseDirectoryMeta(JSONObject dataJsonObj,
			CosDirectory dir, Context context) {
		if ((dataJsonObj == null) || (dir == null)) {
			StatService.reportError(context, "dataJsonObj dir is null");
			return false;
		}

		try {
			boolean isParseOK = true;

			Object tempValue = null;
			// get "Cache-Control"
			if (!dataJsonObj.isNull("Cache-Control")) {
				tempValue = dataJsonObj.get("Cache-Control");
				String cacheControl = String.valueOf(tempValue);
				dir.setCacheControl(cacheControl);
			}

			// get "Content-Language"
			if (!dataJsonObj.isNull("Content-Language")) {
				tempValue = dataJsonObj.get("Content-Language");
				String contentLanguage = String.valueOf(tempValue);
				dir.setContentLanguage(contentLanguage);
			}

			// get "Content-Encoding"
			if (!dataJsonObj.isNull("Content-Encoding")) {
				tempValue = dataJsonObj.get("Content-Encoding");
				String contentEncoding = String.valueOf(tempValue);
				dir.setContentEncoding(contentEncoding);
			}

			// get "Content-Disposition"
			if (!dataJsonObj.isNull("Content-Disposition")) {
				tempValue = dataJsonObj.get("Content-Disposition");
				String contentDisposition = String.valueOf(tempValue);
				dir.setContentDisposition(contentDisposition);
			}

			// get "Expires"
			if (!dataJsonObj.isNull("Expires")) {
				tempValue = dataJsonObj.get("Expires");
				String expires = String.valueOf(tempValue);
				dir.setExpires(expires);
			}

			// get "uptime"
			if (!dataJsonObj.isNull(CosConst.UPTIME)) {
				tempValue = dataJsonObj.get(CosConst.UPTIME);
				Long uptime = Long.parseLong(String.valueOf(tempValue));
				dir.setUptime(uptime);
			} else {
				isParseOK = false;
			}

			// get "crttime"
			if (!dataJsonObj.isNull(CosConst.CRTTIME)) {
				tempValue = dataJsonObj.get(CosConst.CRTTIME);
				Long crttime = Long.parseLong(String.valueOf(tempValue));
				dir.setCrtime(crttime);
			} else {
				isParseOK = false;
			}

			// get "name"
			if (!dataJsonObj.isNull(CosConst.NAME)) {
				tempValue = dataJsonObj.get(CosConst.NAME);
				String name = String.valueOf(tempValue);
				dir.setName(name);
			}

			// get "type"
			if (!dataJsonObj.isNull(CosConst.TYPE)) {
				tempValue = dataJsonObj.get(CosConst.TYPE);
				int type = Integer.valueOf(String.valueOf(tempValue));
				dir.setType(type);
			}
			return isParseOK;
		} catch (JSONException e) {
			StatService.reportException(context, e);
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 从server返回的json串中的data段解析 file meta信息
	 * 
	 * @param JSONObject
	 *            dataJsonObj server返回的json串中的data段
	 * @param CosFile
	 *            file 返回解析后的file meta信息
	 * 
	 * @param Context
	 *            context 为移动终端页面上下文
	 * @return boolean true/false 成功/失败 CosFile file 返回解析后的file meta信息
	 */
	private boolean parseFileMeta(JSONObject dataJsonObj, CosFile file,
			Context context) {
		if ((dataJsonObj == null) || (file == null)) {
			StatService.reportError(context, "dataJsonObj file is null");
			return false;
		}

		try {
			boolean isParseOK = true;

			Object tempValue = null;
			// get "finish flag"
			if (!dataJsonObj.isNull(CosConst.FINISHFLAG)) {
				tempValue = dataJsonObj.get(CosConst.FINISHFLAG);
				boolean finish_flag = Boolean
						.valueOf(String.valueOf(tempValue));
				file.setFinishFlag(finish_flag);
			} else

			// get "md5"
			if (!dataJsonObj.isNull(CosConst.MD5)) {
				tempValue = dataJsonObj.get(CosConst.MD5);
				String md5 = String.valueOf(tempValue);
				file.setMd5(md5);
			}

			// get "name"
			if (!dataJsonObj.isNull(CosConst.NAME)) {
				tempValue = dataJsonObj.get(CosConst.NAME);
				String name = String.valueOf(tempValue);
				file.setName(name);
			}

			// get "type"
			if (!dataJsonObj.isNull(CosConst.TYPE)) {
				tempValue = dataJsonObj.get(CosConst.TYPE);
				int type = Integer.valueOf(String.valueOf(tempValue));
				file.setType(type);
			}

			// get "fsize"
			if (!dataJsonObj.isNull(CosConst.FSIZE)) {
				tempValue = dataJsonObj.get(CosConst.FSIZE);
				long fsize = Long.valueOf(String.valueOf(tempValue));
				file.setFszie(fsize);
			}

			// get "crtime"
			if (!dataJsonObj.isNull(CosConst.CRTTIME)) {
				tempValue = dataJsonObj.get(CosConst.CRTTIME);
				long crtime = Long.valueOf(String.valueOf(tempValue));
				file.setCrttime(crtime);
			} else {
				isParseOK = false;
			}

			// get "uptime"
			if (!dataJsonObj.isNull(CosConst.UPTIME)) {
				tempValue = dataJsonObj.get(CosConst.UPTIME);
				long uptime = Long.valueOf(String.valueOf(tempValue));
				file.setUptime(uptime);
			} else {
				isParseOK = false;
			}

			return isParseOK;
		} catch (JSONException e) {
			StatService.reportException(context, e);
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 从json信息中获取对象的属性是文件还是目录
	 * 
	 * @param JSONObject
	 *            dataJsonObj jsonObj
	 * @param Context
	 *            context 为移动终端页面上下文
	 * @return int 1:文件 2：目录 -1：失败
	 */
	private int parseObjType(JSONObject dataJsonObj, Context context) {
		try {
			Object tempValue = null;
			// get "type"
			if (!dataJsonObj.isNull(CosConst.TYPE)) {
				tempValue = dataJsonObj.get(CosConst.TYPE);
				return Integer.valueOf(String.valueOf(tempValue));
			}
			StatService.reportError(context, "get file type error");
			return -1;
		} catch (JSONException e) {
			StatService.reportException(context, e);
			e.printStackTrace();
			return -1;
		}

	}

	/**
	 * http 通过get获取数据
	 * 
	 * @param Map
	 *            param
	 * @param Message
	 *            message
	 * @param String
	 *            methodName 调用的api，如：/api/cos_mkdir
	 * @param Context
	 *            context 为移动终端页面上下文
	 * @return JSONObject dataJsonObj 保存解析后的"data"字段的值 Message message
	 *         保存解析后的"code" "msg"字段的值
	 */
	private JSONObject getHttpData(Map param, Message message,
			String methodName, Context context) {

		if ((message == null) || (methodName == null)) {
			StatService.reportError(context, "message is null");
			return null;
		}

		if (param == null) {
			param = new HashMap();
		}

		Date nowDate = new Date();
		long time = nowDate.getTime() / 1000;

		param.put("time", time);
		param.put("accessId", _accessId);
		
		if(this._secretId!=null && this._secretId != "" && !this._secretId.equals("")){
			param.put("secretId", this._secretId);
		}
		
		String sign = SignManager.MakeSign(param, _accessKey, methodName);
		sign = sign.trim();
		param.put("sign", sign);
		
		GetDNS getDns = new GetDNS();
		getDns.setContext(context);
		Thread getDnsThread = new Thread(getDns);

		getDnsThread.start();

		try {
			getDnsThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if(CosConst.inetAdd ==null || CosConst.inetAdd.length==0){
			StatService.reportError(context, "http server error");
			message.setCode(-28997);
			message.setMessage("http server error");
			return null;
		}
		
		
		String hostAddress=CosConst.inetAdd[0].getHostAddress();
		
		if(hostAddress == null){
			StatService.reportError(context, "http server error");
			message.setCode(-28997);
			message.setMessage("http server error");
			return null;
		}
		String url = BaseFun.getUrlHost(hostAddress);
		url = url + methodName;

		HttpResponse httpResponse = new HttpResponse();

		String json = httpConnect.doGet(url, param, this.httpHeader, context,
				httpResponse);

		if (json == null) {
			StatService.reportError(context, "http server error");
			message.setCode(-28997);
			message.setMessage("http server error");
			return null;
		}

		// json decode
		JSONObject jsonObject = parseJson(json, message, context);

		long timestampe = System.currentTimeMillis();

		StatService.trackCustomEvent(
				context,
				CosConst.HTTPID,
				message.getCode() + "-" + timestampe + "-"
						+ httpResponse.getResponseId());

		return jsonObject;
	}

	/**
	 * http 上传文件数据
	 * 
	 * @param Map
	 *            param
	 * @param Message
	 *            message
	 * @param String
	 *            methodName 调用的api，如：/api/cos_upload
	 * @param String
	 *            filename 文件名
	 * @param byte[] fileData 文件内容
	 * @param Context
	 *            context 移动终端页面上下文
	 * @return JSONObject dataJsonObj 保存解析后的"data"字段的值 Message message
	 *         保存解析后的"code" "msg"字段的值
	 */
	private JSONObject UploadHttpData(Map param, Message message,
			String methodName, String filename, byte[] fileData, Context context) {

		if ((message == null) || (methodName == null)) {
			StatService.reportError(context, "message is null");
			return null;
		}

		if (param == null) {
			param = new HashMap();
		}

		Date nowDate = new Date();
		long time = nowDate.getTime() / 1000;

		param.put("time", time);
		param.put("accessId", _accessId);
		
		if(this._secretId!=null && this._secretId != "" && !this._secretId.equals("")){
			param.put("secretId", this._secretId);
		}
		
		String sign = SignManager.MakeSign(param, _accessKey, methodName);
		sign = sign.trim();
		param.put("sign", sign);

		GetDNS getDns = new GetDNS();
		getDns.setContext(context);
		Thread getDnsThread = new Thread(getDns);

		getDnsThread.start();

		try {
			getDnsThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if(CosConst.inetAdd ==null || CosConst.inetAdd.length==0){
			StatService.reportError(context, "http server error");
			message.setCode(-28997);
			message.setMessage("http server error");
			return null;
		}
		
		
		String hostAddress = CosConst.inetAdd[0].getHostAddress();
		
		if(hostAddress == null){
			message.setCode(-28997);
			message.setMessage("http server error");
			StatService.reportError(context, "http server error");
			return null;
		}
		String url = BaseFun.getUrlHost(hostAddress);

		url = url + methodName;

		HttpResponse httpResponse = new HttpResponse();

		String json = httpConnect.doUpload(url, param, fileData, filename,
				this.httpHeader, context, httpResponse);

		if (json == null) {
			message.setCode(-28997);
			message.setMessage("http server error");
			StatService.reportError(context, "http server error");
			return null;
		}

		// json decode
		JSONObject jsonObject = parseJson(json, message, context);

		long timestampe = System.currentTimeMillis();
		
		
		StatService.trackCustomEvent(
				context,
				CosConst.HTTPID,
				message.getCode() + "-" + timestampe + "-"
						+ httpResponse.getResponseId());

		return jsonObject;
	}

	/**
	 * 从传入的参数中提取设置bucket信息的可选参数
	 * 
	 * @param Map
	 *            inputParam 输入参数
	 * @param Map
	 *            outputParam 输出参数
	 * @param Context
	 *            context 移动终端页面上下文
	 * @return boolean true/false Map outputParam 输出参数
	 */
	private boolean getOptionalParamForBucket(Map inputParam, Map outputParam,
			Context context) {
		if ((inputParam == null) || (outputParam == null)) {
			StatService.reportError(context, "inputParam outputParam is null");
			return false;

		}

		// get acl
		Object tempValue = inputParam.get(CosConst.ACL);
		if (tempValue != null) {
			int acl = Integer.parseInt(String.valueOf(tempValue));
			outputParam.put("acl", acl);
		}

		// get referer
		tempValue = inputParam.get(CosConst.REFERER);
		if (tempValue != null) {
			String referer = String.valueOf(tempValue);
			outputParam.put("referer", referer);
		}

		return true;
	}

	/**
	 * 从传入的参数中提取设置dir信息的可选参数
	 * 
	 * @param Map
	 *            inputParam 输入参数
	 * @param Map
	 *            outputParam 输出参数
	 * @param Context
	 *            context 移动终端页面上下文
	 * @return boolean true/false Map outputParam 输出参数
	 */
	private boolean getOptionalParamForDir(Map inputParam, Map outputParam,
			Context context) {
		if ((inputParam == null) || (outputParam == null)) {
			StatService.reportError(context, "inputParam outputParam is null");
			return false;
		}

		// get expires
		Object tempValue = inputParam.get(CosConst.EXPIRES);
		if (tempValue != null) {
			int expires = Integer.parseInt(String.valueOf(tempValue));
			outputParam.put("expires", expires);
		}

		// get cacheControl
		tempValue = inputParam.get(CosConst.CACHE_CONTROL);
		if (tempValue != null) {
			String cacheControl = String.valueOf(tempValue);
			outputParam.put("cacheControl", cacheControl);
		}

		// get contentEncoding
		tempValue = inputParam.get(CosConst.CONTENT_ENCODING);
		if (tempValue != null) {
			String contentEncoding = String.valueOf(tempValue);
			outputParam.put("contentEncoding", contentEncoding);
		}

		// get contentDisposition
		tempValue = inputParam.get(CosConst.CONTENT_DISPOSITION);
		if (tempValue != null) {
			String contentDisposition = String.valueOf(tempValue);
			outputParam.put("contentDisposition", contentDisposition);
		}

		// get contentLanguage
		tempValue = inputParam.get(CosConst.CONTENT_LANGUAGE);
		if (tempValue != null) {
			String contentLanguage = String.valueOf(tempValue);
			outputParam.put("contentLanguage", contentLanguage);
		}

		return true;
	}

	/**
	 * 从传入的参数中提取list操作的可选参数
	 * 
	 * @param Map
	 *            inputParam 输入参数
	 * @param Map
	 *            outputParam 输出参数
	 * @param Context
	 *            context 移动终端页面上下文
	 * @return boolean true/false Map outputParam 输出参数
	 */
	private boolean getOptionalParamForList(Map inputParam, Map outputParam,
			Context context) {
		if ((inputParam == null) || (outputParam == null)) {
			StatService.reportError(context, "inputParam outputParam is null");
			return false;
		}

		// get offset
		Object tempValue = inputParam.get(CosConst.OFFSET);
		if (tempValue != null) {
			int acl = Integer.parseInt(String.valueOf(tempValue));
			outputParam.put("acl", acl);
		}

		// get count
		tempValue = inputParam.get(CosConst.COUNT);
		if (tempValue != null) {
			int count = Integer.parseInt(String.valueOf(tempValue));
			outputParam.put("count", count);
		}

		// get prefix
		tempValue = inputParam.get(CosConst.PREFIX);
		if (tempValue != null) {
			String prefix = String.valueOf(tempValue);
			outputParam.put("prefix", prefix);
		}

		return true;
	}

	/**
	 * 从传入的参数中提取compress操作的可选参数
	 * 
	 * @param Map
	 *            inputParam 输入参数
	 * @param Map
	 *            outputParam 输出参数
	 * @param Context
	 *            context 移动终端页面上下文
	 * @return boolean true/false Map outputParam 输出参数
	 */
	private boolean getOptionalParamForCompress(Map inputParam,
			Map outputParam, Context context) {
		if ((inputParam == null) || (outputParam == null)) {
			StatService.reportError(context, "inputparam outputparam is null");
			return false;
		}

		// get uploadBucketId
		Object tempValue = inputParam.get(CosConst.UPLOAD_BUCKET_ID);
		if (tempValue != null) {
			String uploadBucketId = String.valueOf(tempValue);
			outputParam.put("uploadBucketId", uploadBucketId);
		}

		// get uploadFilePath
		tempValue = inputParam.get(CosConst.UPLOAD_FILE_PATH);
		if (tempValue != null) {
			String uploadFilePath = String.valueOf(tempValue);
			if (uploadFilePath != ""
					&& uploadFilePath.substring(0, 1).compareTo("/") != 0) {
				uploadFilePath = "/" + uploadFilePath;
			}
			outputParam.put("uploadFilePath", uploadFilePath);
		}

		return true;
	}

	/**
	 * 从传入的参数中提取bucketId 和path 这两个必选参数
	 * 
	 * @param Map
	 *            inputParam 输入参数
	 * @param Map
	 *            outputParam 输出参数
	 * @param Message
	 *            message 返回的code和msg
	 * @param Context
	 *            context 移动终端页面上下文
	 * @return boolean true/false Map outputParam 输出参数 Message message
	 *         返回的code和msg
	 */
	private boolean getPartialRequiredParam(Map inputParam, Map outputParam,
			Message message, Context context) {
		// get bucketId
		Object tempValue = inputParam.get(CosConst.BUCKET_ID);
		if (tempValue == null) {
			StatService.reportError(context, "bucketId is null");
			message.setCode(-29494);
			message.setMessage("bukcetId is null");
			return false;
		}
		String bucketId = String.valueOf(tempValue);
		outputParam.put("bucketId", bucketId);

		// get path
		tempValue = inputParam.get(CosConst.PATH);
		if (tempValue == null) {
			StatService.reportError(context, "path is null");
			message.setCode(-29491);
			message.setMessage("path is null");
			return false;
		}
		String path = String.valueOf(tempValue);
		outputParam.put("path", path);

		return true;
	}

	/**
	 * 从传入的参数中提取compressBucketId 和compressFilePath 这两个必选参数
	 * 
	 * @param Map
	 *            inputParam 输入参数
	 * @param Map
	 *            outputParam 输出参数
	 * @param Message
	 *            message 返回的code和msg
	 * @param Context
	 *            context 移动终端页面上下文
	 * @return boolean true/false Map outputParam 输出参数 Message message
	 *         返回的code和msg
	 */
	private boolean getPartialRequiredParamForCompress(Map inputParam,
			Map outputParam, Message message, Context context) {
		// get compressBucketId
		Object tempValue = inputParam.get(CosConst.COMPRESS_BUCKET_ID);
		if (tempValue == null) {
			StatService.reportError(context, "compressBucketId is null");
			message.setCode(-29494);
			message.setMessage("compressBucketId is null");
			return false;
		}
		String compressBucketId = String.valueOf(tempValue);
		outputParam.put("compressBucketId", compressBucketId);

		// get compressFilePath
		tempValue = inputParam.get(CosConst.COMPRESS_FILE_PATH);
		if (tempValue == null) {
			StatService.reportError(context, "compressFilePath is null");
			message.setCode(-29491);
			message.setMessage("compressFilePath is null");
			return false;
		}
		String compressFilePath = String.valueOf(tempValue);
		if (compressFilePath != ""
				&& compressFilePath.substring(0, 1).compareTo("/") != 0) {
			compressFilePath = "/" + compressFilePath;
		}
		outputParam.put("compressFilePath", compressFilePath);

		return true;
	}

	/**
	 * 检查本地文件是否能够读取，并检查是否超过单次上传文件的最大值
	 * 
	 * @param String
	 *            localFilePath 本地文件全路径
	 * @param Message
	 *            message 输出参数 返回的code和msg
	 * @param Context
	 *            context 终端移动页面上下文
	 * @return boolean true/false 输出参数 Message message 返回的code和msg
	 */
	private boolean checkLocalFile(String localFilePath, Message message,
			Context context) {
		File localfile = new File(localFilePath);

		if (!localfile.canRead()) {
			if (!localfile.isFile()) {
				StatService.reportError(context, "localFilePath can not read");
				message.setCode(-29489);
				message.setMessage("localFilePath can not read");
				return false;
			} else {
				StatService.reportError(context, "localFilePath is not File");
				message.setCode(-29489);
				message.setMessage("localFilePath is not File");
				return false;
			}
		}
		long length = localfile.length();
		if (length > CosConst.FILE_MAX_LENGTH) {
			StatService.reportError(context,
					"invalid file size ,file is too large");
			message.setCode(-24968);
			message.setMessage("invalid file size, file is too large");
			return false;
		}
		return true;
	}

	/**
	 * 创建 目录
	 * 
	 * @param Map
	 *            param 可包含的key-value字段为： String bucketId
	 *            桶Id（长度小于等于64、字符（123456789 and A~Z and a~z and _ - .）） String
	 *            path 目录路径（不支持递归创建目录）.不允许ascII字符(0-31, 92, 127)；允许中文； int
	 *            expires （可选,默认为空）例如：7200.该目录下的”直接”文件(一级文件)，下载时的Expires头 String
	 *            cacheControl （可选，默认为空） 例如：max-age=200 .文件被下载时的cache-control头
	 *            String contentEncoding （可选，默认为空）例如：utf-8
	 *            .文件被下载时的Content-Encoding头 String
	 *            contentDisposition（可选，默认为空）例如：attachment; filename=\文件名+文件类型
	 *            .文件被下载时的contentDisposition头 String contentLanguage （可选，默认为空）
	 *            例如：zh .文件被下载时的contentLanguage头
	 * 
	 * @param Context
	 *            context 移动终端页面上下文
	 * 
	 * @return boolean true/false 成功/失败 Message message server返回的code和msg
	 */
	@Override
	public boolean COSMkDir(Map param, Message message, Context context) {
        System.out.println("--------------------yes-----------");

		StatService.trackCustomEvent(context, CosConst.MKDIR_URL, "true");
		StatAppMonitor monitor = new StatAppMonitor("mkdir");
		long starttime = System.currentTimeMillis();
		if (message == null) {
			return false;
		}

		if (param == null || param.size() == 0) {
			message.setCode(-29498);
			message.setMessage("param is null");
			return false;
		}

		Map paramMap = new HashMap();
		// 提取必选参数 bucketId & path
		if (false == getPartialRequiredParam(param, paramMap, message, context)) {
			return false;
		}

		// 提取可选参数
		getOptionalParamForDir(param, paramMap, context);

		JSONObject dataJsonObj = getHttpData(paramMap, message,
				CosConst.MKDIR_URL, context);

		long difftime = System.currentTimeMillis() - starttime;

		Log.d(Tag, "mkdir use times : " + difftime + " ms");
		monitor.setMillisecondsConsume(difftime);
		monitor.setReturnCode(message.getCode());
		if (message.getCode() == 0) {
			monitor.setResultType(StatAppMonitor.SUCCESS_RESULT_TYPE);
			StatService.reportAppMonitorStat(context, monitor);
			return true;
		}

		monitor.setResultType(StatAppMonitor.LOGIC_FAILURE_RESULT_TYPE);
		StatService.reportAppMonitorStat(context, monitor);

		return false;
	}

	/**
	 * 删除目录
	 * 
	 * @param Map
	 *            param 可包含的key-value字段为： String bucketId
	 *            桶Id（长度小于等于64、字符（123456789 and A~Z and a~z and _ - .）） String
	 *            path 目录路径（不支持递归创建目录）.不允许ascII字符(0-31, 92, 127)；允许中文；
	 * @param Context
	 *            context 移动终端页面上下文
	 * @return boolean true/false 成功/失败 Message message server返回的code和msg
	 */
	@Override
	public boolean COSRmDir(Map param, Message message, Context context) {
		StatService.trackCustomEvent(context, CosConst.RMDIR_URL, "true");
		StatAppMonitor monitor = new StatAppMonitor("rmdir");
		long starttime = System.currentTimeMillis();
		if (message == null) {
			return false;
		}

		if (param == null || param.size() == 0) {
			message.setCode(-29498);
			message.setMessage("param is null");
			return false;
		}

		Map paramMap = new HashMap();
		// 提取必选参数 bucketId & path
		if (false == getPartialRequiredParam(param, paramMap, message, context)) {
			return false;
		}

		JSONObject dataJsonObj = getHttpData(paramMap, message,
				CosConst.RMDIR_URL, context);

		long difftime = System.currentTimeMillis() - starttime;
		COSLog.i(Tag, "rmdir use times : " + difftime + " ms");
		monitor.setMillisecondsConsume(difftime);

		if (message.getCode() == 0) {
			monitor.setResultType(StatAppMonitor.SUCCESS_RESULT_TYPE);
			StatService.reportAppMonitorStat(context, monitor);
			return true;
		}

		monitor.setResultType(StatAppMonitor.LOGIC_FAILURE_RESULT_TYPE);
		StatService.reportAppMonitorStat(context, monitor);
		return false;
	}

	/**
	 * 获取 文件/目录 列表 按起始位置获取指定目录下的目录、文件列表，一次性最多获取100个，
	 * 默认按字符串排序，可以通过指定prefix参数列出以特定字符串起始的目录/文件
	 * 
	 * @param Map
	 *            param 可包含的key-value字段为: String bucketId
	 *            桶Id（长度小于等于64、字符（123456789 and A~Z and a~z and _ - .）） String
	 *            path 文件的上层全路径。不允许ascII字符(0-31, 92, 127)；允许中文； int offset
	 *            （可选，默认为0） 大于等于0 int count （可选，默认为100） 大于等于1，目前单次显示的最大个数为1000个
	 *            String prefix （可选，默认为空） 匹配的前缀, 只允许传入长度小于等于255、字符（123456789 and
	 *            A~Z and a~z and _ - . ）
	 * 
	 * @param Context
	 *            context 移动终端页面上下文
	 * @return boolean true/false 成功/失败 List<File> fileList 文件列表 int
	 *         foldertotalnum 已有的目录的个数 int filetotalnum 已有的文件的个数 Message message
	 *         server返回的code和msg
	 */
	@Override
	public boolean COSListFile(Map param, List<CosFile> fileList,
			List<CosDirectory> dirList, int foldertotalnum, int filetotalnum,
			Message message, Context context) {
		StatService.trackCustomEvent(context, CosConst.LISTFILE_URL, "true");
		StatAppMonitor monitor = new StatAppMonitor("listfile");
		long starttime = System.currentTimeMillis();
		if (message == null) {
			return false;
		}

		if (param == null || param.size() == 0) {
			message.setCode(-29498);
			message.setMessage("param is null");
			return false;
		}

		if (fileList == null) {
			message.setCode(-29498);
			message.setMessage("fileList is null");
			return false;
		}

		if (dirList == null) {
			message.setCode(-29498);
			message.setMessage("dirList is null");
			return false;
		}

		Map paramMap = new HashMap();
		// 提取必选参数 bucketId & path
		if (false == getPartialRequiredParam(param, paramMap, message, context)) {
			return false;
		}

		// 提取可选参数
		getOptionalParamForList(param, paramMap, context);

		JSONObject dataJsonObj = getHttpData(paramMap, message,
				CosConst.LISTFILE_URL, context);

		long difftime = System.currentTimeMillis() - starttime;
		COSLog.i(Tag, "listFile use times : " + difftime + " ms");

		monitor.setMillisecondsConsume(difftime);

		if ((dataJsonObj == null) || (message.getCode() != 0)) {
			return false;
		}

		// parse bucket list
		try {
			boolean isParseOK = true;
			Object tempValue = null;

			// get "foldertotalnum"
			if (!dataJsonObj.isNull(CosConst.FOLDER_TOTAL_NUM)) {
				tempValue = dataJsonObj.get(CosConst.FOLDER_TOTAL_NUM);
				foldertotalnum = Integer.parseInt(String.valueOf(tempValue));
			} else {
				isParseOK = false;
			}

			// get "filetotalnum"
			if (!dataJsonObj.isNull(CosConst.FILE_TOTAL_NUM)) {
				tempValue = dataJsonObj.get(CosConst.FILE_TOTAL_NUM);
				filetotalnum = Integer.parseInt(String.valueOf(tempValue));
			} else {
				isParseOK = false;
			}

			// get "files"
			if (!dataJsonObj.isNull(CosConst.FILES)) {
				JSONArray direntlst = dataJsonObj.getJSONArray(CosConst.FILES);
				JSONObject dirOrFileObject = null;
				for (int i = 0; i < direntlst.length(); i++) {
					dirOrFileObject = (JSONObject) direntlst.get(i);
					int objType = -1;
					objType = parseObjType(dirOrFileObject, context);
					if (objType == 1) { // file
						CosFile file = new CosFile();
						isParseOK &= parseFileMeta(dirOrFileObject, file,
								context);
						fileList.add(file);
					} else if (objType == 2) { // dir
						CosDirectory dir = new CosDirectory();
						isParseOK &= parseDirectoryMeta(dirOrFileObject, dir,
								context);
						dirList.add(dir);
					} else {
						isParseOK &= false;
					}
				}
			}

			if (isParseOK) {
				monitor.setResultType(StatAppMonitor.SUCCESS_RESULT_TYPE);
			} else {
				monitor.setResultType(StatAppMonitor.FAILURE_RESULT_TYPE);
			}
			StatService.reportAppMonitorStat(context, monitor);
			return isParseOK;
		} catch (JSONException e) {
			message.setCode(-28997);
			message.setMessage("json decode error");
			e.printStackTrace();
			monitor.setResultType(StatAppMonitor.FAILURE_RESULT_TYPE);
			StatService.reportAppMonitorStat(context, monitor);
			return false;
		}

	}

	/**
	 * 获取目录属性
	 * 
	 * @param Map
	 *            param 可包含的key-value字段为： String bucketId
	 *            桶Id（长度小于等于64、字符（123456789 and A~Z and a~z and _ - .）） String
	 *            path 目录路径（不支持递归创建目录）
	 * 
	 * @param Context
	 *            context 移动终端页面上下文
	 * @return boolean true/false 成功/失败 Message message server返回的code和msg
	 *         Directory dir server返回的dir元信息数据
	 */
	@Override
	public boolean COSGetMeta(Map param, CosDirectory dir, Message message,
			Context context) {
		StatService.trackCustomEvent(context, CosConst.GETMETA_URL, "true");
		StatAppMonitor monitor = new StatAppMonitor("getmeta");
		long starttime = System.currentTimeMillis();
		if (message == null) {
			StatService.reportError(context, "message is null");
			return false;
		}

		if (param == null || param.size() == 0) {
			StatService.reportError(context, "param is null");
			message.setCode(-29498);
			message.setMessage("param is null");
			return false;
		}

		if (dir == null) {
			StatService.reportError(context, "dir is null");
			message.setCode(-29498);
			message.setMessage("dir is null");
			return false;
		}

		Map paramMap = new HashMap();
		// 提取必选参数 bucketId & path
		if (false == getPartialRequiredParam(param, paramMap, message, context)) {
			return false;
		}

		JSONObject dataJsonObj = getHttpData(paramMap, message,
				CosConst.GETMETA_URL, context);

		long difftime = System.currentTimeMillis() - starttime;

		COSLog.i("Tag", "getmeta use time : " + difftime + " ms");

		monitor.setMillisecondsConsume(difftime);
		if ((dataJsonObj == null) || (message.getCode() != 0)) {
			StatService.reportError(context, "http server error");
			monitor.setResultType(StatAppMonitor.FAILURE_RESULT_TYPE);
			StatService.reportAppMonitorStat(context, monitor);
			return false;
		}

		// parse meta info
		if (!parseDirectoryMeta(dataJsonObj, dir, context)) {
			StatService.reportError(context, "json decode error");
			monitor.setResultType(StatAppMonitor.SUCCESS_RESULT_TYPE);
			message.setCode(-28997);
			message.setMessage("json decode error");
			StatService.reportAppMonitorStat(context, monitor);
			return false;
		}

		monitor.setResultType(StatAppMonitor.SUCCESS_RESULT_TYPE);
		StatService.reportAppMonitorStat(context, monitor);
		return true;
	}

	/**
	 * 设置目录属性
	 * 
	 * @param Map
	 *            param 可包含的key-value字段为： String bucketId
	 *            桶Id（长度小于等于64、字符（123456789 and A~Z and a~z and _ - .）） String
	 *            path 目录路径（不支持递归创建目录）.不允许ascII字符(0-31, 92, 127)；允许中文； int
	 *            expires （可选,默认为空）例如：7200.该目录下的”直接”文件(一级文件)，下载时的Expires头 String
	 *            cacheControl （可选，默认为空） 例如：max-age=200 .文件被下载时的cache-control头
	 *            String contentEncoding （可选，默认为空）例如：utf-8
	 *            .文件被下载时的Content-Encoding头 String
	 *            contentDisposition（可选，默认为空）例如：attachment; filename=\文件名+文件类型
	 *            .文件被下载时的contentDisposition头 String contentLanguage （可选，默认为空）
	 *            例如：zh .文件被下载时的contentLanguage头
	 * 
	 * @param Context
	 *            context 移动终端页面上下文
	 * @return boolean true/false 成功/失败 Message message server返回的code和msg
	 */
	@Override
	public boolean COSSetMeta(Map param, Message message, Context context) {

		StatService.trackCustomEvent(context, CosConst.SETMETA_URL, "true");
		StatAppMonitor monitor = new StatAppMonitor("setmeta");
		long starttime = System.currentTimeMillis();

		if (message == null) {
			StatService.reportError(context, "message is null");
			return false;
		}

		if (param == null || param.size() == 0) {
			StatService.reportError(context, "param is null");
			message.setCode(-29498);
			message.setMessage("param is null");
			return false;
		}

		Map paramMap = new HashMap();
		// 提取必选参数 bucketId & path
		if (false == getPartialRequiredParam(param, paramMap, message, context)) {
			return false;
		}

		// 提取可选参数
		getOptionalParamForDir(param, paramMap, context);

		JSONObject dataJsonObj = getHttpData(paramMap, message,
				CosConst.SETMETA_URL, context);

		long difftime = System.currentTimeMillis() - starttime;

		COSLog.i(Tag, "set meta use time : " + difftime + " ms");

		monitor.setMillisecondsConsume(difftime);

		if (message.getCode() == 0) {
			monitor.setResultType(StatAppMonitor.SUCCESS_RESULT_TYPE);
			StatService.reportAppMonitorStat(context, monitor);
			return true;
		}

		monitor.setResultType(StatAppMonitor.FAILURE_RESULT_TYPE);
		StatService.reportAppMonitorStat(context, monitor);
		return false;
	}

	/**
	 * 删除文件
	 * 
	 * @param Map
	 *            param 可包含的key-value字段为： String bucketId
	 *            桶Id（长度小于等于64、字符（123456789 and A~Z and a~z and _ - .）） String
	 *            path 文件的上层全路径。不允许ascII字符(0-31, 92, 127)；允许中文； String deleteObj
	 *            需要删除的文件/空目录对象列表, deleteObj为用’|’隔开的obj列表.
	 *            每个文件名只允许传入长度小于等于255字符，不允许ascII字符(0-31,47,92, 127)；允许中文；
	 * 
	 * 
	 * @param Context
	 *            context 移动终端页面上下文
	 * @return boolean true/false 成功/失败 Message message server返回的code和msg
	 */
	@Override
	public boolean COSDeleteFile(Map param, Message message, Context context) {

		StatService.trackCustomEvent(context, CosConst.DELETEFILE_URL, "true");

		StatAppMonitor monitor = new StatAppMonitor("deletefile");
		long starttime = System.currentTimeMillis();
		if (message == null) {
			StatService.reportError(context, "message is null");
			return false;
		}

		if (param == null || param.size() == 0) {
			StatService.reportError(context, "param is null");
			message.setCode(-29498);
			message.setMessage("param is null");
			return false;
		}

		Map paramMap = new HashMap();
		// 提取必选参数 bucketId & path
		if (false == getPartialRequiredParam(param, paramMap, message, context)) {
			return false;
		}

		// get deleteObj
		Object tempValue = param.get(CosConst.DELETE_OBJ);
		if (tempValue == null) {
			StatService.reportError(context, "deleteObj is null");
			message.setCode(-29489);
			message.setMessage("deleteObj is null");
			return false;
		}
		String deleteObj = String.valueOf(tempValue);
		paramMap.put("deleteObj", deleteObj);

		JSONObject dataJsonObj = getHttpData(paramMap, message,
				CosConst.DELETEFILE_URL, context);

		long difftime = System.currentTimeMillis() - starttime;

		COSLog.i(Tag, "delete file use time : " + difftime + " ms");

		monitor.setMillisecondsConsume(difftime);
		if (message.getCode() == 0) {
			monitor.setResultType(StatAppMonitor.SUCCESS_RESULT_TYPE);
			StatService.reportAppMonitorStat(context, monitor);
			return true;
		}

		monitor.setResultType(StatAppMonitor.FAILURE_RESULT_TYPE);
		StatService.reportAppMonitorStat(context, monitor);
		return false;
	}

	/**
	 * 修改目录文件名
	 * 
	 * @param Map
	 *            param 可包含的key-value字段为： String bucketId
	 *            桶Id（长度小于等于64、字符（123456789 and A~Z and a~z and _ - .）） String
	 *            spath source路径。不允许ascII字符(0-31, 92, 127)；允许中文； String dpath
	 *            dest路径。不允许ascII字符(0-31, 92, 127)；允许中文；
	 * 
	 * @param Context
	 *            context 移动终端页面上下文
	 * @return boolean true/false 成功/失败 Message message server返回的code和msg
	 */
	@Override
	public boolean COSRename(Map param, Message message, Context context) {

		StatService.trackCustomEvent(context, CosConst.RENAME_URL, "true");

		StatAppMonitor monitor = new StatAppMonitor("rename");

		long starttime = System.currentTimeMillis();

		if (message == null) {
			StatService.reportError(context, "message is null");
			return false;
		}

		if (param == null || param.size() == 0) {
			StatService.reportError(context, "param is null");
			message.setCode(-29498);
			message.setMessage("param is null");
			return false;
		}

		Object tempValue = null;
		// get bucketId
		tempValue = param.get(CosConst.BUCKET_ID);
		if (tempValue == null) {
			StatService.reportError(context, "bucketId is null");
			message.setCode(-29494);
			message.setMessage("bukcetId is null");
			return false;
		}
		String bucketId = String.valueOf(tempValue);
		Map paramMap = new HashMap();
		paramMap.put("bucketId", bucketId);

		// get spath
		tempValue = param.get(CosConst.SPATH);
		if (tempValue == null) {
			StatService.reportError(context, "spath is null");
			message.setCode(-29491);
			message.setMessage("spath is null");
			return false;
		}
		String spath = String.valueOf(tempValue);
		paramMap.put("spath", spath);

		// get dpath
		tempValue = param.get(CosConst.DPATH);
		if (tempValue == null) {
			StatService.reportError(context, "dpath is null");
			message.setCode(-29491);
			message.setMessage("dpath is null");
			return false;
		}
		String dpath = String.valueOf(tempValue);
		paramMap.put("dpath", dpath);

		JSONObject dataJsonObj = getHttpData(paramMap, message,
				CosConst.RENAME_URL, context);

		long difftime = System.currentTimeMillis() - starttime;

		COSLog.i(Tag, "rename use time : " + difftime + " ms");

		monitor.setMillisecondsConsume(difftime);
		if (message.getCode() == 0) {
			monitor.setResultType(StatAppMonitor.SUCCESS_RESULT_TYPE);
			StatService.reportAppMonitorStat(context, monitor);
			return true;
		}

		monitor.setResultType(StatAppMonitor.FAILURE_RESULT_TYPE);
		StatService.reportAppMonitorStat(context, monitor);
		return false;
	}

	/**
	 * 创建 bucket
	 * 
	 * @param Map
	 *            param 可包含的key-value字段为： String bucketId
	 *            桶Id（长度小于等于64、字符（123456789 and A~Z and a~z and _ - .）） int acl
	 *            （可选，默认为0） acl取值 0: bucket访问权限为私有读 1: bucket访问权限为公开读 String
	 *            referer （可选，默认为空） 允许访问 bucket的referer，允许访问 bucket的referer，如
	 *            "http://qq.com"
	 * 
	 * @param Context
	 *            context 移动终端页面上下文
	 * @return boolean true/false 成功/失败 Message message server返回的code和msg
	 */
	@Override
	public boolean COSCreateBucket(Map param, Message message, Context context) {
        System.out.println("cos create bucket method called-------------------------");

		StatService
				.trackCustomEvent(context, CosConst.CREATEBUCKET_URL, "true");

		StatAppMonitor monitor = new StatAppMonitor("createbucket");
		long starttime = System.currentTimeMillis();

		if (message == null) {
			StatService.reportError(context, "message is null");
			return false;
		}

		if ((param == null) || (param.size() == 0)) {
			StatService.reportError(context, "param is null");
			message.setCode(-29498);
			message.setMessage("param is null");
			return false;
		}

		Object tempValue = null;
		// get bucketId
		tempValue = param.get(CosConst.BUCKET_ID);
		if (tempValue == null) {
			StatService.reportError(context, "bucketId is null");
			message.setCode(-29494);
			message.setMessage("bukcetId is null");
			return false;
		}

		String bucketId = String.valueOf(tempValue);

		Map paramMap = new HashMap();
		paramMap.put("bucketId", bucketId);

		// 获取可选参数
		getOptionalParamForBucket(param, paramMap, context);

		JSONObject dataJsonObj = getHttpData(paramMap, message,
				CosConst.CREATEBUCKET_URL, context);

		long difftime = System.currentTimeMillis() - starttime;

		COSLog.i(Tag, "create bucket use time : " + difftime + " ms");

		monitor.setMillisecondsConsume(difftime);

		if (message.getCode() == 0) {
			monitor.setResultType(StatAppMonitor.SUCCESS_RESULT_TYPE);
			StatService.reportAppMonitorStat(context, monitor);
			return true;
		}

		monitor.setResultType(StatAppMonitor.FAILURE_RESULT_TYPE);
		StatService.reportAppMonitorStat(context, monitor);
		return false;
	}

	/**
	 * 删除 bucket
	 * 
	 * @param Map
	 *            param 可包含的key-value字段为： String bucketId
	 *            桶Id（长度小于等于64、字符（123456789 and A~Z and a~z and _ - .））
	 * 
	 * @param Context
	 *            context 移动终端页面上下文
	 * @return boolean true/false 成功/失败 Message message server返回的code和msg
	 */
	@Override
	public boolean COSDeleteBucket(Map param, Message message, Context context) {

		StatService
				.trackCustomEvent(context, CosConst.DELETEBUCKET_URL, "true");
		StatAppMonitor monitor = new StatAppMonitor("deletebucket");

		long starttime = System.currentTimeMillis();
		if (message == null) {
			StatService.reportError(context, "message is null");
			return false;
		}

		if ((param == null) || (param.size() == 0)) {
			StatService.reportError(context, "path is null");
			message.setCode(-29498);
			message.setMessage("param is null");
			return false;
		}

		Object tempValue = null;

		// get bucketId
		tempValue = param.get(CosConst.BUCKET_ID);
		if (tempValue == null) {
			StatService.reportError(context, "bucketId is null");
			message.setCode(-29494);
			message.setMessage("bukcetId is null");
			return false;
		}

		String bucketId = String.valueOf(tempValue);

		Map paramMap = new HashMap();
		paramMap.put("bucketId", bucketId);

		JSONObject dataJsonObj = getHttpData(paramMap, message,
				CosConst.DELETEBUCKET_URL, context);

		long difftime = System.currentTimeMillis() - starttime;

		COSLog.i(Tag, "deleteBucket use time : " + difftime + "ms");

		monitor.setMillisecondsConsume(difftime);
		if (message.getCode() == 0) {
			monitor.setResultType(StatAppMonitor.SUCCESS_RESULT_TYPE);
			StatService.reportAppMonitorStat(context, monitor);
			return true;
		}

		monitor.setResultType(StatAppMonitor.FAILURE_RESULT_TYPE);
		StatService.reportAppMonitorStat(context, monitor);
		return false;
	}

	/**
	 * 设置 bucket meta信息
	 * 
	 * @param Map
	 *            param 可包含的key-value字段为： String bucketId
	 *            桶Id（长度小于等于64、字符（123456789 and A~Z and a~z and _ - .）） int acl
	 *            （可选） acl取值 0: bucket访问权限为私有读 1: bucket访问权限为公开读
	 *            acl不传或acl=""则保持原设置不变 String referer （可选） 允许访问
	 *            bucket的referer，允许访问 bucket的referer，如 "http://qq.com"
	 *            referer不传则保持原设置不变，referer=""时会清空原有配置
	 * 
	 * @param Context
	 *            context 移动终端页面上下文
	 * @return boolean true/false 成功/失败 Message message server返回的code和msg
	 */
	@Override
	public boolean COSSetBucket(Map param, Message message, Context context) {

		StatService.trackCustomEvent(context, CosConst.SETBUCKET_URL, "true");
		StatAppMonitor monitor = new StatAppMonitor("setbucket");

		long starttime = System.currentTimeMillis();

		if (message == null) {
			StatService.reportError(context, "message is null");
			return false;
		}

		if (param == null || param.size() == 0) {
			StatService.reportError(context, "param is null");
			message.setCode(-29498);
			message.setMessage("param is null");
			return false;
		}

		Object tempValue = null;

		// get bucketId
		tempValue = param.get(CosConst.BUCKET_ID);
		if (tempValue == null) {
			StatService.reportError(context, "bucketId is null");
			message.setCode(-29494);
			message.setMessage("bukcetId is null");
			return false;
		}

		String bucketId = String.valueOf(tempValue);

		Map paramMap = new HashMap();
		paramMap.put("bucketId", bucketId);

		// 获取可选参数
		getOptionalParamForBucket(param, paramMap, context);

		JSONObject dataJsonObj = getHttpData(paramMap, message,
				CosConst.SETBUCKET_URL, context);

		long difftime = System.currentTimeMillis() - starttime;

		COSLog.i(Tag, "COSSetBucket use time : " + difftime + "ms");

		monitor.setMillisecondsConsume(difftime);

		if (message.getCode() == 0) {
			monitor.setResultType(StatAppMonitor.SUCCESS_RESULT_TYPE);
			StatService.reportAppMonitorStat(context, monitor);
			return true;
		}

		monitor.setResultType(StatAppMonitor.FAILURE_RESULT_TYPE);
		StatService.reportAppMonitorStat(context, monitor);
		return false;
	}

	/**
	 * 获取 bucket meta信息
	 * 
	 * @param Map
	 *            param 可包含的key-value字段为： String bucketId
	 *            桶Id（长度小于等于64、字符（123456789 and A~Z and a~z and _ - .））
	 * 
	 * @param Context
	 *            context 移动终端页面上下文
	 * @return boolean true/false 成功/失败 Message message server返回的code和msg Bucket
	 *         bucket bucket的meta信息
	 */
	@Override
	public boolean COSGetBucket(Map param, CosBucket bucket, Message message,
			Context context) {

		StatService.trackCustomEvent(context, CosConst.GETMETA_URL, "true");
		StatAppMonitor monitor = new StatAppMonitor("getbucket");

		long starttime = System.currentTimeMillis();
		if (message == null) {
			StatService.reportError(context, "message is null");
			return false;
		}

		if (param == null || param.size() == 0) {
			StatService.reportError(context, "param is null");
			message.setCode(-29498);
			message.setMessage("param is null");
			return false;
		}

		Object tempValue = null;

		// get bucketId
		tempValue = param.get("bucketId");
		if (tempValue == null) {
			StatService.reportError(context, "bucketId is null");
			message.setCode(-29494);
			message.setMessage("bukcetId is null");
			return false;
		}

		String bucketId = String.valueOf(tempValue);

		Map paramMap = new HashMap();
		paramMap.put("bucketId", bucketId);

		JSONObject dataJsonObj = getHttpData(paramMap, message,
				CosConst.GETBUCKET_URL, context);

		long difftime = System.currentTimeMillis() - starttime;
		monitor.setMillisecondsConsume(difftime);

		if ((dataJsonObj == null) || (message.getCode() != 0)) {
			monitor.setResultType(StatAppMonitor.FAILURE_RESULT_TYPE);
			StatService.reportAppMonitorStat(context, monitor);
			return false;
		}

		COSLog.i(Tag, "get bucket use time : " + difftime + " ms");

		// parse meta info
		if (!parseBucketMeta(dataJsonObj, bucket, context)) {
			monitor.setResultType(StatAppMonitor.FAILURE_RESULT_TYPE);
			StatService.reportAppMonitorStat(context, monitor);
			StatService.reportError(context, "json decode error");
			message.setCode(-28997);
			message.setMessage("json decode error");
			return false;
		}

		monitor.setResultType(StatAppMonitor.SUCCESS_RESULT_TYPE);
		StatService.reportAppMonitorStat(context, monitor);
		return true;
	}

	/**
	 * 获取 bucket 列表
	 * 
	 * @param Map
	 *            param 可包含的key-value字段为： int offset （可选，默认为0） 大于等于0 int count
	 *            （可选，默认为100） 大于等于1，目前单次显示的最大个数为1000个 String prefix （可选，默认为空）
	 *            匹配的前缀, 只允许传入长度小于等于255、字符（123456789 and A~Z and a~z and _ - . ）
	 * 
	 * @param Context
	 *            context 移动终端页面上下文
	 * @return boolean true/false 成功/失败 List<Bucket> bucketList bucket 列表 int
	 *         foldertotalnum 已有的bucket的个数 Message message server返回的code和msg
	 */
	@Override
	public boolean COSListBucket(Map param, List<CosBucket> bucketList,
			int foldertotalnum, Message message, Context context) {
		StatService.trackCustomEvent(context, CosConst.LISTBUCKET_URL, "true");

		StatAppMonitor monitor = new StatAppMonitor("listbucket");
		long starttime = System.currentTimeMillis();

		if (message == null) {
			StatService.reportError(context, "message is null");
			return false;
		}

		if (bucketList == null) {
			StatService.reportError(context, "bucketList is null");
			message.setCode(-29498);
			message.setMessage("bucketList is null");
			return false;
		}

		Map paramMap = new HashMap();
		Object tempValue = null;
		// 提取可选参数
		if ((param != null) && (param.size() != 0)) {
			getOptionalParamForList(param, paramMap, context);
		}

		JSONObject dataJsonObj = getHttpData(paramMap, message,
				CosConst.LISTBUCKET_URL, context);

		long difftime = System.currentTimeMillis() - starttime;
		COSLog.i(Tag, "list bucket use time : " + difftime + " ms");
		if ((dataJsonObj == null) || (message.getCode() != 0)) {
			monitor.setResultType(StatAppMonitor.FAILURE_RESULT_TYPE);
			StatService.reportAppMonitorStat(context, monitor);
			return false;
		}

		// parse bucket list
		try {
			boolean isParseOK = true;
			tempValue = null;

			// get "foldertotalnum"
			if (!dataJsonObj.isNull(CosConst.FOLDER_TOTAL_NUM)) {
				tempValue = dataJsonObj.get(CosConst.FOLDER_TOTAL_NUM);
				foldertotalnum = Integer.parseInt(String.valueOf(tempValue));
			} else {
				isParseOK = false;
			}

			// get "direntlst"
			if (!dataJsonObj.isNull(CosConst.DIRENT_LST)) {
				JSONArray direntlst = dataJsonObj
						.getJSONArray(CosConst.DIRENT_LST);
				JSONObject bucketObject = null;
				for (int i = 0; i < direntlst.length(); i++) {
					bucketObject = (JSONObject) direntlst.get(i);
					CosBucket bucket = new CosBucket();
					isParseOK &= parseBucketMeta(bucketObject, bucket, context);
					bucketList.add(bucket);
				}
			}

			if (isParseOK) {
				monitor.setResultType(StatAppMonitor.SUCCESS_RESULT_TYPE);
				StatService.reportAppMonitorStat(context, monitor);
			} else {
				monitor.setResultType(StatAppMonitor.FAILURE_RESULT_TYPE);
				StatService.reportAppMonitorStat(context, monitor);
			}
			return isParseOK;
		} catch (JSONException e) {
			message.setCode(-28997);
			message.setMessage("json decode error");
			StatService.reportException(context, e);
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 直接上传文件内容
	 * 
	 * @param Map
	 *            param 可包含的key-value字段为： String bucketId
	 *            桶Id（长度小于等于64、字符（123456789 and A~Z and a~z and _ - .）） String
	 *            path path 目录路径. 不允许ascII字符(0-31, 92, 127)；允许中文； String cosFile
	 *            存储到cos之后的对象名 String content 文件的内容. 最大长度为2G
	 * 
	 * @param Context
	 *            context 移动终端页面上下文
	 * @return boolean true/false 成功/失败 Message message server返回的code和msg
	 */
	@Override
	public boolean COSUploadFileByContent(Map param, byte[] content,
			Message message, Context context) {

		StatService.trackCustomEvent(context, CosConst.UPLOAD_URL, "true");
		StatAppMonitor monitor = new StatAppMonitor("uploadfile");
		long starttime = System.currentTimeMillis();

		if (message == null) {
			StatService.reportError(context, "message is null");
			return false;
		}

		if (param == null || param.size() == 0) {
			StatService.reportError(context, "param is null");
			message.setCode(-29498);
			message.setMessage("param is null");
			return false;
		}

		Map paramMap = new HashMap();
		// 提取必选参数 bucketId & path
		if (false == getPartialRequiredParam(param, paramMap, message, context)) {
			return false;
		}

		// get cosFile
		Object tempValue = param.get(CosConst.COSFILE);
		if (tempValue == null) {
			message.setCode(-29489);
			message.setMessage("cosFile is null");
			return false;
		}
		String cosFile = String.valueOf(tempValue);
		paramMap.put("cosFile", cosFile);

		JSONObject dataJsonObj = UploadHttpData(paramMap, message,
				CosConst.UPLOAD_URL, cosFile, content, context);

		long difftime = System.currentTimeMillis() - starttime;

		COSLog.i(Tag, "uploadFile use time : " + difftime + " ms");

		monitor.setMillisecondsConsume(difftime);
		if ((dataJsonObj == null) || (message.getCode() != 0)) {
			monitor.setResultType(StatAppMonitor.FAILURE_RESULT_TYPE);
			StatService.reportAppMonitorStat(context, monitor);
			return false;
		}

		monitor.setResultType(StatAppMonitor.SUCCESS_RESULT_TYPE);
		StatService.reportAppMonitorStat(context, monitor);
		return true;

	}

	/**
	 * 上传文件
	 * 
	 * @param Map
	 *            param 可包含的key-value字段为： String bucketId
	 *            桶Id（长度小于等于64、字符（123456789 and A~Z and a~z and _ - .）） String
	 *            path cos path 目录路径. 不允许ascII字符(0-31, 92, 127)；允许中文； String
	 *            cosFile 存储到cos之后的对象名 String localFilePath 本地文件的路径
	 * 
	 * @param Context
	 *            context 移动终端页面上下文
	 * @return boolean true/false 成功/失败 Message message server返回的code和msg
	 */
	@Override
	public boolean COSUploadFile(Map param, Message message, Context context) {

		StatService.trackCustomEvent(context, CosConst.UPLOAD_URL, "true");

		StatAppMonitor monitor = new StatAppMonitor("uploadfile");
		long starttime = System.currentTimeMillis();
		if (message == null) {
			StatService.reportError(context, "message is null");
			return false;
		}

		if (param == null || param.size() == 0) {
			StatService.reportError(context, "param is null");
			message.setCode(-29498);
			message.setMessage("param is null");
			return false;
		}

		Map paramMap = new HashMap();
		// 提取必选参数 bucketId & path
		if (false == getPartialRequiredParam(param, paramMap, message, context)) {
			return false;
		}

		// get cosFile
		Object tempValue = param.get(CosConst.COSFILE);
		if (tempValue == null) {
			StatService.reportError(context, "cosFile is null");
			message.setCode(-29489);
			message.setMessage("cosFile is null");
			return false;
		}
		String cosFile = String.valueOf(tempValue);
		paramMap.put("cosFile", cosFile);

		// get localFilePath
		tempValue = param.get(CosConst.LOCAL_FILE_PATH);
		if (tempValue == null) {
			StatService.reportError(context, "localFilePath is null");
			message.setCode(-29489);
			message.setMessage("localFilePath is null");
			return false;
		}
		String localFilePath = String.valueOf(tempValue);

		// 检查本地文件是否能够读取，并检查是否超过单次上传文件的最大值
		if (!checkLocalFile(localFilePath, message, context)) {
			return false;
		}

		byte[] content = BaseFun.readFile(localFilePath);

		if (content == null) {
			StatService.reportError(context, "read file memo fail");
			message.setCode(-29489);
			message.setMessage("read file memo fail");
			return false;
		}

		JSONObject dataJsonObj = UploadHttpData(paramMap, message,
				CosConst.UPLOAD_URL, cosFile, content, context);

		long difftime = System.currentTimeMillis() - starttime;
		monitor.setMillisecondsConsume(difftime);
		COSLog.i(Tag, "upload File use time : " + difftime + " ms");
		if ((dataJsonObj == null) || (message.getCode() != 0)) {
			monitor.setResultType(StatAppMonitor.FAILURE_RESULT_TYPE);
			StatService.reportAppMonitorStat(context, monitor);
			return false;
		}

		monitor.setResultType(StatAppMonitor.SUCCESS_RESULT_TYPE);
		StatService.reportAppMonitorStat(context, monitor);
		return true;
	}

	/**
	 * 上传文件内容并进行压缩 （目前支持不超过50MB的JPG、PNG格式的图片，注：上传内容一定要符合图片格式）
	 * 
	 * @param Map
	 *            param 输入参数, 包含: String compressBucketId 压缩后文件存放的bucket,
	 *            长度<=64、字符（123456789 and A~Z and a~z and _-.） String
	 *            compressFilePath 压缩后文件的存放路径, ,以文件名结尾的全路径， 长度小于等于4096,
	 *            字符（123456789 and A~Z and a~z and _ - . /和utf8编码的中文), 以"/"开头
	 *            String uploadBucketId 原始文件上传后存放的bucket（可选参数，如不保存源文件，则不传此参数）,
	 *            长度<=64、字符（123456789 and A~Z and a~z and _ - .） String
	 *            uploadFilePath
	 *            原始文件上传后存放的完整路径,以文件名结尾的全路径，（可选参数，如不保存源文件，则不传此参数）长度小于等于4096、
	 *            字符（123456789 and A~Z and a~z and _ - . / 和中文）, 以"/"开头
	 * @param byte[] content 输入参数, 文件内容
	 * @param Message
	 *            message 返回参数, 包含错误码和错误消息
	 * @param Context
	 *            context 移动终端页面上下文
	 * 
	 * @return true 成功, false 失败
	 */
	@Override
	public boolean COSUploadFileByContentWithCompress(Map param,
			byte[] content, Message message, Context context) {
		StatService.trackCustomEvent(context, CosConst.COMPRESS_FILE_PATH,
				"true");

		StatAppMonitor monitor = new StatAppMonitor("compressPath");
		long starttime = System.currentTimeMillis();
		if (message == null) {
			StatService.reportError(context, "message is null");
			return false;
		}

		if (param == null || param.size() == 0) {
			StatService.reportError(context, "param is null");
			message.setCode(-29498);
			message.setMessage("param is null");
			return false;
		}

		Map paramMap = new HashMap();
		// 提取必选参数 compressBucketId和compressFilePath
		if (false == getPartialRequiredParamForCompress(param, paramMap,
				message, context)) {
			return false;
		}

		// 提取可选参数 uploadBucketId和uploadFilePath
		getOptionalParamForCompress(param, paramMap, context);

		String fileName = BaseFun.GetFileName(String.valueOf(paramMap
				.get(CosConst.COMPRESS_FILE_PATH)));

		JSONObject dataJsonObj = UploadHttpData(paramMap, message,
				CosConst.UPLOAD_WITHCOMPRESS_URL, fileName, content, context);

		long difftime = System.currentTimeMillis() - starttime;

		COSLog.i(Tag, "uploadFile with compress use time : " + difftime + " ms");

		monitor.setMillisecondsConsume(difftime);

		if ((dataJsonObj == null) || (message.getCode() != 0)) {
			monitor.setResultType(StatAppMonitor.FAILURE_RESULT_TYPE);
			StatService.reportAppMonitorStat(context, monitor);
			return false;
		}

		monitor.setResultType(StatAppMonitor.SUCCESS_RESULT_TYPE);
		StatService.reportAppMonitorStat(context, monitor);
		return true;
	}

	/**
	 * 上传文件并压缩 （目前支持不超过50MB的JPG、PNG格式的图片，注：上传内容一定要符合图片格式）
	 * 
	 * @param Map
	 *            inParams 输入参数, 包含: String compressBucketId 压缩后文件存放的bucket,
	 *            长度<=64、字符（123456789 and A~Z and a~z and _-.） String
	 *            compressFilePath 压缩后文件的存放路径, ,以文件名结尾的全路径, 长度小于等于4096,
	 *            字符（123456789 and A~Z and a~z and _ - . /和utf8编码的中文), 以"/"开头
	 *            String localFilePath 本地文件的路径 String uploadBucketId
	 *            原始文件上传后存放的bucket（可选参数，如不保存源文件，则不传此参数）, 长度<=64、字符（123456789 and
	 *            A~Z and a~z and _ - .） String uploadFilePath
	 *            原始文件上传后存放的完整路径,以文件名结尾的全路径, （可选参数，如不保存源文件，则不传此参数）长度小于等于4096、
	 *            字符（123456789 and A~Z and a~z and _ - . / 和中文）, 以"/"开头
	 * @param Message
	 *            message 返回参数, 包含错误码和错误消息
	 * @param Context
	 *            context 移动终端页面上下文
	 * 
	 * @return true 成功, false 失败
	 */
	public boolean COSUploadFileWithCompress(Map param, Message message,
			Context context) {
		StatService.trackCustomEvent(context, CosConst.UPLOAD_WITHCOMPRESS_URL,
				"true");
		StatAppMonitor monitor = new StatAppMonitor("cos_upload_with_compress");
		long starttime = System.currentTimeMillis();
		if (message == null) {
			StatService.reportError(context, "message is null");
			return false;
		}

		if (param == null || param.size() == 0) {
			StatService.reportError(context, "param is null");
			message.setCode(-29498);
			message.setMessage("param is null");
			return false;
		}

		Map paramMap = new HashMap();
		// 提取必选参数 compressBucketId和compressFilePath
		if (false == getPartialRequiredParamForCompress(param, paramMap,
				message, context)) {
			return false;
		}

		// 提取可选参数 uploadBucketId和uploadFilePath
		getOptionalParamForCompress(param, paramMap, context);

		// get localFilePath
		Object tempValue = param.get(CosConst.LOCAL_FILE_PATH);
		if (tempValue == null) {
			StatService.reportError(context, "localFilePath is null");
			message.setCode(-29489);
			message.setMessage("localFilePath is null");
			return false;
		}
		String localFilePath = String.valueOf(tempValue);
		// 检查本地文件是否能够读取，并检查是否超过单次上传文件的最大值
		if (!checkLocalFile(localFilePath, message, context)) {
			return false;
		}

		byte[] content = BaseFun.readFile(localFilePath);

		if (content == null) {
			StatService.reportError(context, "read file memo fail");
			message.setCode(-29489);
			message.setMessage("read file memo fail");
			return false;
		}

		String fileName = BaseFun.GetFileName(String.valueOf(paramMap
				.get(CosConst.COMPRESS_FILE_PATH)));

		JSONObject dataJsonObj = UploadHttpData(paramMap, message,
				CosConst.UPLOAD_WITHCOMPRESS_URL, fileName, content, context);

		long difftime = System.currentTimeMillis() - starttime;

		COSLog.i(Tag, "uploadFile with compress use time : " + difftime + " ms");

		monitor.setMillisecondsConsume(difftime);
		if ((dataJsonObj == null) || (message.getCode() != 0)) {
			monitor.setResultType(StatAppMonitor.FAILURE_RESULT_TYPE);
			StatService.reportAppMonitorStat(context, monitor);
			return false;
		}

		monitor.setResultType(StatAppMonitor.SUCCESS_RESULT_TYPE);
		StatService.reportAppMonitorStat(context, monitor);
		return true;
	}

	/**
	 * 对线上文件进行压缩 （目前支持不超过50MB的JPG、PNG格式的图片）
	 * 
	 * @param Map
	 *            params 输入参数, 包含: String srcBucketId 待压缩文件所在的bucket,
	 *            长度<=64、字符（123456789 and A~Z and a~z and _ - .） String
	 *            dstBucketId 压缩后文件存放的bucket, 长度<=64、字符（123456789 and A~Z and
	 *            a~z and _ - .） String srcFilePath 待压缩文件所在路径,
	 *            以文件名结尾的全路径，长度小于等于4096, 字符（123456789 and A~Z and a~z and _ - .
	 *            / 和中文), 以"/"开头 String dstFilePath 压缩后文件的存放路径,
	 *            以文件名结尾的全路径，长度小于等于4096, 字符（123456789 and A~Z and a~z and _ - .
	 *            / 和中文), 以"/"开头
	 * @param Message
	 *            message 返回参数，包含错误码和错误消息
	 * 
	 * @param Context
	 *            context 移动终端页面上下文
	 * 
	 * @return true 成功, false 失败
	 */
	public boolean COSCompressOnlineFile(Map param, Message message,
			Context context) {
		StatService.trackCustomEvent(context, CosConst.UPLOAD_WITHCOMPRESS_URL,
				"true");

		StatAppMonitor monitor = new StatAppMonitor("cos_upload_with_compress");

		long starttime = System.currentTimeMillis();

		if (message == null) {
			StatService.reportError(context, "message is null");
			return false;
		}

		if (param == null || param.size() == 0) {
			StatService.reportError(context, "param is null");
			message.setCode(-29498);
			message.setMessage("param is null");
			return false;
		}

		Map paramMap = new HashMap();
		Object tempValue = null;

		// get srcBucketId
		tempValue = param.get(CosConst.SRC_BUCKET_ID);
		if (tempValue == null) {
			StatService.reportError(context, "srcBucketId is null");
			message.setCode(-29494);
			message.setMessage("srcBucketId is null");
			return false;
		}
		String srcBucketId = String.valueOf(tempValue);
		paramMap.put("srcBucketId", srcBucketId);

		// get dstBucketId
		tempValue = param.get(CosConst.DST_BUCKET_ID);
		if (tempValue == null) {
			StatService.reportError(context, "dstBucketId is null");
			message.setCode(-29494);
			message.setMessage("dstBucketId is null");
			return false;
		}
		String dstBucketId = String.valueOf(tempValue);
		paramMap.put("dstBucketId", dstBucketId);

		// get srcFilePath
		tempValue = param.get(CosConst.SRC_FILE_PATH);
		if (tempValue == null) {
			StatService.reportError(context, "srcFilePath is null");
			message.setCode(-29494);
			message.setMessage("srcFilePath is null");
			return false;
		}
		String srcFilePath = String.valueOf(tempValue);
		if (srcFilePath != ""
				&& srcFilePath.substring(0, 1).compareTo("/") != 0) {
			srcFilePath = "/" + srcFilePath;
		}
		paramMap.put("srcPath", srcFilePath);

		// get dstFilePath
		tempValue = param.get(CosConst.DST_FILE_PATH);
		if (tempValue == null) {
			StatService.reportError(context, "dstFilePath is null");
			message.setCode(-29494);
			message.setMessage("dstFilePath is null");
			return false;
		}
		String dstFilePath = String.valueOf(tempValue);
		if (dstFilePath != ""
				&& dstFilePath.substring(0, 1).compareTo("/") != 0) {
			dstFilePath = "/" + dstFilePath;
		}
		paramMap.put("dstFilePath", dstFilePath);

		JSONObject dataJsonObj = getHttpData(paramMap, message,
				CosConst.COMPRESS_FILE_URL, context);
		long difftime = System.currentTimeMillis() - starttime;

		COSLog.i(Tag, "COSCompressOnlineFile use time : " + difftime + " ms");

		monitor.setMillisecondsConsume(difftime);
		if (message.getCode() == 0) {
			monitor.setResultType(StatAppMonitor.SUCCESS_RESULT_TYPE);
			StatService.reportAppMonitorStat(context, monitor);
			return true;
		}

		monitor.setResultType(StatAppMonitor.FAILURE_RESULT_TYPE);
		StatService.reportAppMonitorStat(context, monitor);
		return false;
	}

	/**
	 * 创建 目录
	 * 
	 * @param Map
	 *            param 可包含的key-value字段为： String bucketId
	 *            桶Id（长度小于等于64、字符（123456789 and A~Z and a~z and _ - .）） String
	 *            path 目录路径（不支持递归创建目录）.不允许ascII字符(0-31, 92, 127)；允许中文； int
	 *            expires （可选,默认为空）例如：7200.该目录下的”直接”文件(一级文件)，下载时的Expires头 String
	 *            cacheControl （可选，默认为空） 例如：max-age=200 .文件被下载时的cache-control头
	 *            String contentEncoding （可选，默认为空）例如：utf-8
	 *            .文件被下载时的Content-Encoding头 String
	 *            contentDisposition（可选，默认为空）例如：attachment; filename=\文件名+文件类型
	 *            .文件被下载时的contentDisposition头 String contentLanguage （可选，默认为空）
	 *            例如：zh .文件被下载时的contentLanguage头
	 * 
	 * @return boolean true/false 成功/失败 Message message server返回的code和msg
	 */
	@Override
	public boolean COSMkDir(Map param, Message message) {
		Context context = null;
		return COSMkDir(param, message, context);
	}

	/**
	 * 删除目录
	 * 
	 * @param Map
	 *            param 可包含的key-value字段为： String bucketId
	 *            桶Id（长度小于等于64、字符（123456789 and A~Z and a~z and _ - .）） String
	 *            path 目录路径（不支持递归创建目录）.不允许ascII字符(0-31, 92, 127)；允许中文；
	 * @return boolean true/false 成功/失败 Message message server返回的code和msg
	 */
	@Override
	public boolean COSRmDir(Map param, Message message) {
		// TODO Auto-generated method stub
		return COSRmDir(param, message, null);
	}

	/**
	 * 获取 文件/目录 列表 按起始位置获取指定目录下的目录、文件列表，一次性最多获取100个，
	 * 默认按字符串排序，可以通过指定prefix参数列出以特定字符串起始的目录/文件
	 * 
	 * @param Map
	 *            param 可包含的key-value字段为: String bucketId
	 *            桶Id（长度小于等于64、字符（123456789 and A~Z and a~z and _ - .）） String
	 *            path 文件的上层全路径。不允许ascII字符(0-31, 92, 127)；允许中文； int offset
	 *            （可选，默认为0） 大于等于0 int count （可选，默认为100） 大于等于1，目前单次显示的最大个数为1000个
	 *            String prefix （可选，默认为空） 匹配的前缀, 只允许传入长度小于等于255、字符（123456789 and
	 *            A~Z and a~z and _ - . ）
	 * 
	 * @return boolean true/false 成功/失败 List<File> fileList 文件列表 int
	 *         foldertotalnum 已有的目录的个数 int filetotalnum 已有的文件的个数 Message message
	 *         server返回的code和msg
	 */
	@Override
	public boolean COSListFile(Map param, List<CosFile> fileList,
			List<CosDirectory> dirList, int foldertotalnum, int filetotalnum,
			Message message) {
		return COSListFile(param, fileList, dirList, foldertotalnum,
				filetotalnum, null);
	}

	/**
	 * 获取目录属性
	 * 
	 * @param Map
	 *            param 可包含的key-value字段为： String bucketId
	 *            桶Id（长度小于等于64、字符（123456789 and A~Z and a~z and _ - .）） String
	 *            path 目录路径（不支持递归创建目录）
	 * 
	 * @return boolean true/false 成功/失败 Message message server返回的code和msg
	 *         Directory dir server返回的dir元信息数据
	 */
	@Override
	public boolean COSGetMeta(Map param, CosDirectory dir, Message message) {
		return COSGetMeta(param, dir, message, null);
	}

	/**
	 * 设置目录属性
	 * 
	 * @param Map
	 *            param 可包含的key-value字段为： String bucketId
	 *            桶Id（长度小于等于64、字符（123456789 and A~Z and a~z and _ - .）） String
	 *            path 目录路径（不支持递归创建目录）.不允许ascII字符(0-31, 92, 127)；允许中文； int
	 *            expires （可选,默认为空）例如：7200.该目录下的”直接”文件(一级文件)，下载时的Expires头 String
	 *            cacheControl （可选，默认为空） 例如：max-age=200 .文件被下载时的cache-control头
	 *            String contentEncoding （可选，默认为空）例如：utf-8
	 *            .文件被下载时的Content-Encoding头 String
	 *            contentDisposition（可选，默认为空）例如：attachment; filename=\文件名+文件类型
	 *            .文件被下载时的contentDisposition头 String contentLanguage （可选，默认为空）
	 *            例如：zh .文件被下载时的contentLanguage头
	 * 
	 * @return boolean true/false 成功/失败 Message message server返回的code和msg
	 */
	@Override
	public boolean COSSetMeta(Map param, Message message) {
		return COSSetMeta(param, message, null);
	}

	/**
	 * 删除文件
	 * 
	 * @param Map
	 *            param 可包含的key-value字段为： String bucketId
	 *            桶Id（长度小于等于64、字符（123456789 and A~Z and a~z and _ - .）） String
	 *            path 文件的上层全路径。不允许ascII字符(0-31, 92, 127)；允许中文； String deleteObj
	 *            需要删除的文件/空目录对象列表, deleteObj为用’|’隔开的obj列表.
	 *            每个文件名只允许传入长度小于等于255字符，不允许ascII字符(0-31,47,92, 127)；允许中文；
	 * 
	 * 
	 * @return boolean true/false 成功/失败 Message message server返回的code和msg
	 */
	@Override
	public boolean COSDeleteFile(Map param, Message message) {
		return COSDeleteFile(param, message, null);
	}

	/**
	 * 修改目录文件名
	 * 
	 * @param Map
	 *            param 可包含的key-value字段为： String bucketId
	 *            桶Id（长度小于等于64、字符（123456789 and A~Z and a~z and _ - .）） String
	 *            spath source路径。不允许ascII字符(0-31, 92, 127)；允许中文； String dpath
	 *            dest路径。不允许ascII字符(0-31, 92, 127)；允许中文；
	 * 
	 * @return boolean true/false 成功/失败 Message message server返回的code和msg
	 */
	@Override
	public boolean COSRename(Map param, Message message) {
		return COSRename(param, message, null);
	}

	/**
	 * 创建 bucket
	 * 
	 * @param Map
	 *            param 可包含的key-value字段为： String bucketId
	 *            桶Id（长度小于等于64、字符（123456789 and A~Z and a~z and _ - .）） int acl
	 *            （可选，默认为0） acl取值 0: bucket访问权限为私有读 1: bucket访问权限为公开读 String
	 *            referer （可选，默认为空） 允许访问 bucket的referer，允许访问 bucket的referer，如
	 *            "http://qq.com"
	 * @return boolean true/false 成功/失败 Message message server返回的code和msg
	 */
	@Override
	public boolean COSCreateBucket(Map param, Message message) {
		return COSCreateBucket(param, message, null);
	}

	/**
	 * 删除 bucket
	 * 
	 * @param Map
	 *            param 可包含的key-value字段为： String bucketId
	 *            桶Id（长度小于等于64、字符（123456789 and A~Z and a~z and _ - .））
	 * @return boolean true/false 成功/失败 Message message server返回的code和msg
	 */
	@Override
	public boolean COSDeleteBucket(Map param, Message message) {
		return COSDeleteBucket(param, message, null);
	}

	/**
	 * 设置 bucket meta信息
	 * 
	 * @param Map
	 *            param 可包含的key-value字段为： String bucketId
	 *            桶Id（长度小于等于64、字符（123456789 and A~Z and a~z and _ - .）） int acl
	 *            （可选） acl取值 0: bucket访问权限为私有读 1: bucket访问权限为公开读
	 *            acl不传或acl=""则保持原设置不变 String referer （可选） 允许访问
	 *            bucket的referer，允许访问 bucket的referer，如 "http://qq.com"
	 *            referer不传则保持原设置不变，referer=""时会清空原有配置
	 * 
	 * @return boolean true/false 成功/失败 Message message server返回的code和msg
	 */
	@Override
	public boolean COSSetBucket(Map param, Message message) {
		return COSSetBucket(param, message, null);
	}

	/**
	 * 获取 bucket meta信息
	 * 
	 * @param Map
	 *            param 可包含的key-value字段为： String bucketId
	 *            桶Id（长度小于等于64、字符（123456789 and A~Z and a~z and _ - .））
	 * 
	 * @return boolean true/false 成功/失败 Message message server返回的code和msg Bucket
	 *         bucket bucket的meta信息
	 */
	@Override
	public boolean COSGetBucket(Map param, CosBucket bucket, Message message) {
		return COSGetBucket(param, bucket, message, null);
	}

	/**
	 * 获取 bucket 列表
	 * 
	 * @param Map
	 *            param 可包含的key-value字段为： int offset （可选，默认为0） 大于等于0 int count
	 *            （可选，默认为100） 大于等于1，目前单次显示的最大个数为1000个 String prefix （可选，默认为空）
	 *            匹配的前缀, 只允许传入长度小于等于255、字符（123456789 and A~Z and a~z and _ - . ）
	 * 
	 * @return boolean true/false 成功/失败 List<Bucket> bucketList bucket 列表 int
	 *         foldertotalnum 已有的bucket的个数 Message message server返回的code和msg
	 */
	@Override
	public boolean COSListBucket(Map param, List<CosBucket> bucketList,
			int foldertotalnum, Message message) {
		return COSListBucket(param, bucketList, foldertotalnum, message, null);
	}

	/**
	 * 直接上传文件内容
	 * 
	 * @param Map
	 *            param 可包含的key-value字段为： String bucketId
	 *            桶Id（长度小于等于64、字符（123456789 and A~Z and a~z and _ - .）） String
	 *            path path 目录路径. 不允许ascII字符(0-31, 92, 127)；允许中文； String cosFile
	 *            存储到cos之后的对象名 String content 文件的内容. 最大长度为2G
	 * 
	 * @return boolean true/false 成功/失败 Message message server返回的code和msg
	 */
	@Override
	public boolean COSUploadFileByContent(Map param, byte[] content,
			Message message) {
		return COSUploadFileByContent(param, content, message, null);
	}

	/**
	 * 上传文件
	 * 
	 * @param Map
	 *            param 可包含的key-value字段为： String bucketId
	 *            桶Id（长度小于等于64、字符（123456789 and A~Z and a~z and _ - .）） String
	 *            path cos path 目录路径. 不允许ascII字符(0-31, 92, 127)；允许中文； String
	 *            cosFile 存储到cos之后的对象名 String localFilePath 本地文件的路径
	 * 
	 * @return boolean true/false 成功/失败 Message message server返回的code和msg
	 */
	@Override
	public boolean COSUploadFile(Map param, Message message) {
		return COSUploadFile(param, message, null);
	}

	/**
	 * 上传文件内容并进行压缩 （目前支持不超过50MB的JPG、PNG格式的图片，注：上传内容一定要符合图片格式）
	 * 
	 * @param Map
	 *            param 输入参数, 包含: String compressBucketId 压缩后文件存放的bucket,
	 *            长度<=64、字符（123456789 and A~Z and a~z and _-.） String
	 *            compressPath 压缩后文件的存放路径, 长度小于等于4096, 字符（123456789 and A~Z and
	 *            a~z and _ - . /和utf8编码的中文), 以"/"开头 String uploadBucketId
	 *            原始文件上传后存放的bucket（可选参数，如不保存源文件，则不传此参数）, 长度<=64、字符（123456789 and
	 *            A~Z and a~z and _ - .） String uploadPath
	 *            原始文件上传后存放的完整路径（可选参数，如不保存源文件，则不传此参数）长度小于等于4096、 字符（123456789
	 *            and A~Z and a~z and _ - . / 和中文）, 以"/"开头
	 * @param byte[] content 输入参数, 文件内容
	 * @param Message
	 *            message 返回参数, 包含错误码和错误消息
	 * 
	 * @return true 成功, false 失败
	 */
	@Override
	public boolean COSUploadFileByContentWithCompress(Map param,
			byte[] content, Message message) {
		return COSUploadFileByContentWithCompress(param, content, message, null);
	}

	/**
	 * 上传文件并压缩 （目前支持不超过50MB的JPG、PNG格式的图片，注：上传内容一定要符合图片格式）
	 * 
	 * @param Map
	 *            inParams 输入参数, 包含: String compressBucketId 压缩后文件存放的bucket,
	 *            长度<=64、字符（123456789 and A~Z and a~z and _-.） String
	 *            compressPath 压缩后文件的存放路径, 长度小于等于4096, 字符（123456789 and A~Z and
	 *            a~z and _ - . /和utf8编码的中文), 以"/"开头 String localFilePath
	 *            本地文件的路径 String uploadBucketId
	 *            原始文件上传后存放的bucket（可选参数，如不保存源文件，则不传此参数）, 长度<=64、字符（123456789 and
	 *            A~Z and a~z and _ - .） String uploadPath
	 *            原始文件上传后存放的完整路径（可选参数，如不保存源文件，则不传此参数）长度小于等于4096、 字符（123456789
	 *            and A~Z and a~z and _ - . / 和中文）, 以"/"开头
	 * @param Message
	 *            message 返回参数, 包含错误码和错误消息
	 * 
	 * @return true 成功, false 失败
	 */
	@Override
	public boolean COSUploadFileWithCompress(Map param, Message message) {
		return COSUploadFileWithCompress(param, message, null);
	}

	/**
	 * 对线上文件进行压缩 （目前支持不超过50MB的JPG、PNG格式的图片）
	 * 
	 * @param Map
	 *            params 输入参数, 包含: String srcBucketId 待压缩文件所在的bucket,
	 *            长度<=64、字符（123456789 and A~Z and a~z and _ - .） String
	 *            dstBucketId 压缩后文件存放的bucket, 长度<=64、字符（123456789 and A~Z and
	 *            a~z and _ - .） String srcPath 待压缩文件所在路径, 长度小于等于4096,
	 *            字符（123456789 and A~Z and a~z and _ - . / 和中文), 以"/"开头 String
	 *            dstPath 压缩后文件的存放路径, 长度小于等于4096, 字符（123456789 and A~Z and a~z
	 *            and _ - . / 和中文), 以"/"开头
	 * @param Message
	 *            message 返回参数，包含错误码和错误消息
	 * 
	 * @return true 成功, false 失败
	 */
	@Override
	public boolean COSCompressOnlineFile(Map param, Message message) {
		return COSCompressOnlineFile(param, message, null);
	}

}
