package com.tencent.cos.demo;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import com.tencent.cos.COS;
import com.tencent.cos.COSImpl;
import com.tencent.cos.bean.CosDirectory;
import com.tencent.cos.bean.CosFile;
import com.tencent.cos.bean.HttpHeader;
import com.tencent.cos.bean.Message;
import com.tencent.cos.constant.CosConst;
import com.tencent.cos.util.COSLog;
import com.tencent.dns.Tencent;
import com.tencent.stat.StatConfig;




import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

public class MainActivity extends Activity implements OnClickListener{
	
	private String TAG = "cosDemo";
	

	private EditText bucketId;

	private EditText path;
	
	private EditText expries;
	
	private EditText deleteObj;
	
	private EditText result;
	
	private EditText dpath;
	
	private COS tencentCos = null;
	
	private Context context = null;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		HttpHeader httpHeader = new HttpHeader();
		httpHeader.setHost("cosapi.myqcloud.com");
		httpHeader.setTimeOut(20000);
		super.onCreate(savedInstanceState);
		
		COSLog.Debug = true;
		
		context = this;
		try {
			String secretId = "AKID12312377987bkjhh213122312";
			tencentCos =  new COSImpl(1000128,"iqOLdwEPIQrDaJPbaBFUh3uf",httpHeader,context); //老用户只有accessId
			//tencentCos = new COSImpl(1000128,"",secretId,httpHeader,context); //新用户具有secretId
		} catch (Exception e) {
			e.printStackTrace();
		}
		setContentView(R.layout.activity_main);
		initViews();
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	private void initViews() {
	
		findViewById(R.id.rmdir).setOnClickListener(this);
		findViewById(R.id.mkdir).setOnClickListener(this);
		findViewById(R.id.setMeta).setOnClickListener(this);
		findViewById(R.id.getMeta).setOnClickListener(this);
		findViewById(R.id.delete).setOnClickListener(this);
		findViewById(R.id.rename).setOnClickListener(this);
		findViewById(R.id.bucket).setOnClickListener(this);
		findViewById(R.id.upload).setOnClickListener(this);
		findViewById(R.id.listFile).setOnClickListener(this);
		
		bucketId = ((EditText) findViewById(R.id.bucketIdText));
		path = ((EditText) findViewById(R.id.PathText));
		expries = ((EditText) findViewById(R.id.expriesText));
		deleteObj = ((EditText) findViewById(R.id.deleteText));
		dpath = ((EditText) findViewById(R.id.dpathText));
		result = ((EditText) findViewById(R.id.resultText));
		
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.mkdir:
			doMkdir();
			break;
		case R.id.rmdir:
			doRmdir();
			break;
		case R.id.rename:
			doRename();
			break;
		case R.id.getMeta:
			doGetmeta();
			break;
		case R.id.setMeta:
			doSetmeta();
			break;
		case R.id.delete:
			doDeleteFile();
			break;
		case R.id.upload:
			doUpload();
			break;
		case R.id.bucket:
			doBucket();
			break;
		case R.id.listFile:
			doList();
			break;
		default:
			break;
		}
	}

	private void doBucket() {
		Intent intent0 = new Intent(MainActivity.this, BucketMain.class);//页面跳转的关键在这儿，注意这儿输入的是.class。
		setTitle("bucketMain");
		startActivity(intent0);
	}

	private void doUpload() {
		/*Message message = new Message();
		
		Map map = new HashMap();
		String bucket_param = bucketId.getText().toString();
		String path_param = path.getText().toString();
		
		map.put("bucketId", bucket_param);
		map.put("path",path_param);
		map.put(CosConst.LOCAL_FILE_PATH,"/mnt/sdcard/c360_debug.txt");
		map.put("cosFile","c360_debug.txt");
		StringBuilder aa = new StringBuilder();
		tencentCos.COSUploadFile(map, message, aa);
		
		
		String resultParam = "";
		
		
		resultParam = "code : "+message.getCode();
		resultParam = resultParam + " message : "+message.getMessage();

		result.setText(resultParam);*/
		Intent intent0 = new Intent(MainActivity.this, UploadFileMain.class);//页面跳转的关键在这儿，注意这儿输入的是.class。
		setTitle("bucketMain");
		startActivity(intent0);
		
	}

	private void doList() {
		Message message = new Message();
		
		Map map = new HashMap();
		String bucket_param = bucketId.getText().toString();
		String path_param = path.getText().toString();
		
		map.put("bucketId", bucket_param);
		map.put("path",path_param);
		
		List<CosFile> fileList = new ArrayList<CosFile>();
		List<CosDirectory> dirList = new ArrayList<CosDirectory>();
		int fileTotalNum = 0;
		int floderTotalNum = 0;
		tencentCos.COSListFile(map, fileList, dirList, floderTotalNum, fileTotalNum, message,context);
		
		StringBuffer sbuffer = new StringBuffer();
		Log.i(TAG, "list : "+dirList.toString());
		
		if(message.getCode() == 0){
			for(int i=0;i<dirList.size();i++){
				sbuffer.append(" dir name : "+dirList.get(i).getName());
			}
			
			for(int i=0;i<fileList.size();i++){
				sbuffer.append(" file name : "+fileList.get(i).getName());
			}
		}
		
		String resultParam = "";
		resultParam = "code:"+message.getCode();
		resultParam = resultParam + "; message:"+message.getMessage();
		resultParam = resultParam + "; data:" + sbuffer.toString();
		
		result.setText(resultParam);
	}

	private void doDeleteFile() {
		Message message = new Message();
		
		Map map = new HashMap();
		String bucket_param = bucketId.getText().toString();
		String path_param = path.getText().toString();
		String deleteObj_param = deleteObj.getText().toString();
		
		map.put("bucketId",bucket_param);
		map.put("path", path_param);
		map.put("deleteObj",deleteObj_param);
		
		tencentCos.COSDeleteFile(map, message,context);
		
		String resultParam = "";
		
		
		resultParam = "code : "+message.getCode();
		resultParam = resultParam + " message : "+message.getMessage();

		result.setText(resultParam);
	}

	private void doSetmeta() {
		Message message = new Message();
		
		Map map = new HashMap();
		String bucket_param = bucketId.getText().toString();
		String path_param = path.getText().toString();
		String expries_param = expries.getText().toString();
		
		map.put("bucketId",bucket_param);
		map.put("path", path_param);
		map.put("expires",expries_param);
		
		
		tencentCos.COSSetMeta(map, message,context);
		
		String resultParam = "";
		
		
		resultParam = "code:"+message.getCode();
		resultParam = resultParam + "; message:"+message.getMessage();
		
		result.setText(resultParam);
	}

	private void doGetmeta() {
	Message message = new Message();
		
		Map map = new HashMap();
		String bucket_param = bucketId.getText().toString();
		String path_param = path.getText().toString();
		
		map.put("bucketId",bucket_param);
		map.put("path", path_param);
		
		CosDirectory dir = new CosDirectory();
		
		tencentCos.COSGetMeta(map, dir, message,context);
		
		String resultParam = "";
		
		
		resultParam = "code:"+message.getCode();
		resultParam = resultParam + "; message:"+message.getMessage();
		resultParam = resultParam + "; data:" + "expries=" + dir.getExpires();
		
		expries.setText(dir.getCacheControl());
		result.setText(resultParam);
	}

	private void doRename() {
		
		Message message = new Message();
		
		Map map = new HashMap();
		
		String bucket_param = bucketId.getText().toString();
		String path_param = path.getText().toString();
		String dpath_param = dpath.getText().toString();
		
		map.put("bucketId", bucket_param);
		map.put("spath",path_param);
		map.put("dpath", dpath_param);
		
		tencentCos.COSRename(map,message);
		String resultParam = "";
		
		
		resultParam = "code:"+message.getCode();
		resultParam = resultParam + "; message:"+message.getMessage();

		result.setText(resultParam);
		
	}

	private void doRmdir() {
		Message message = new Message();
		
		Map map = new HashMap();
		String bucket_param = bucketId.getText().toString();
		String path_param = path.getText().toString();
		
		map.put("bucketId",bucket_param);
		map.put("path", path_param);
		
		tencentCos.COSRmDir(map, message,context);
		
		String resultParam = "";
		
		
		resultParam = "code:"+message.getCode();
		resultParam = resultParam + "; message:"+message.getMessage();

		result.setText(resultParam);
	}

	private void doMkdir() {
		Message message = new Message();
		
		Map map = new HashMap();
		String bucket_param = bucketId.getText().toString();
		String path_param = path.getText().toString();
		String expries_param = expries.getText().toString();
		
		map.put("bucketId",bucket_param);
		map.put("path", path_param);
		map.put("expries",expries_param);
		
		tencentCos.COSMkDir(map, message,context);
		
		String resultParam = "";
		
		resultParam = "code:"+message.getCode();
		resultParam = resultParam + "; message:"+message.getMessage();
		
		result.setText(resultParam);
	}




}
