package com.tencent.cos.demo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tencent.cos.COS;
import com.tencent.cos.COSImpl;
import com.tencent.cos.bean.CosBucket;
import com.tencent.cos.bean.HttpHeader;
import com.tencent.cos.bean.Message;
import com.tencent.cos.util.COSLog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

public class BucketMain extends Activity implements OnClickListener{
	
	private COS tencentCos = null;
	
	private EditText bukcetIdText = null;
	
	private EditText acl = null;
	
	private EditText referer = null;
	
	private EditText result = null;
	
	private Context context = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		HttpHeader httpHeader = new HttpHeader();
		httpHeader.setHost("cosapi.myqcloud.com");
		httpHeader.setTimeOut(20000);
		
		COSLog.Debug = true;
		super.onCreate(savedInstanceState);
		context = this;
		try {
			String secretId = "AKID12312377987bkjhh213122312";
			tencentCos =  new COSImpl(1000128,"iqOLdwEPIQrDaJPbaBFUh3uf",httpHeader,context); //老用户只有accessId
			//tencentCos = new COSImpl(1000128,"",secretId,httpHeader,context); //新用户具有secretId
		} catch (Exception e) {
			e.printStackTrace();
		}
		setContentView(R.layout.bucket_main);
		initViews();
	}

	private void initViews() {
		findViewById(R.id.createBucket).setOnClickListener(this);
		findViewById(R.id.RmBucket).setOnClickListener(this);
		findViewById(R.id.setBucket).setOnClickListener(this);
		findViewById(R.id.getBucket).setOnClickListener(this);
		findViewById(R.id.listBucket).setOnClickListener(this);
		findViewById(R.id.file).setOnClickListener(this);
		findViewById(R.id.uploadFile).setOnClickListener(this);
		
		bukcetIdText = ((EditText) findViewById(R.id.bucketIdMainText));
		acl = ((EditText) findViewById(R.id.aclText));
		referer = ((EditText) findViewById(R.id.refererText));
		result = ((EditText) findViewById(R.id.bucketResultText));	
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.createBucket:
			doCreateBucket();
			break;
		case R.id.RmBucket:
			doRmBucket();
			break;
		case R.id.setBucket:
			doSetBucket();
			break;
		case R.id.getBucket:
			doGetBucket();
			break;
		case R.id.listBucket:
			doListBucket();
			break;
		case R.id.file:
			doFile();
			break;
		case R.id.uploadFile:
			doUpload();
			break;
		default:
			break;
		}
	}

	private void doUpload() {
		Intent intent0 = new Intent(BucketMain.this, UploadFileMain.class);//页面跳转的关键在这儿，注意这儿输入的是.class。
		setTitle("UploadFileMain");
		startActivity(intent0);
	}

	private void doFile() {
		Intent intent0 = new Intent(BucketMain.this, MainActivity.class);//页面跳转的关键在这儿，注意这儿输入的是.class。
		setTitle("MainActivity");
		startActivity(intent0);
	}

	private void doListBucket() {
		Message message = new Message();
		
		int bucketTotalNum = 0;
		
		Map map = new HashMap();
		
		List<CosBucket> bucketList = new ArrayList<CosBucket>();
		
		tencentCos.COSListBucket(map, bucketList, bucketTotalNum, message,context);
		String resultParam = "";		
		
		resultParam = "code:"+message.getCode();
		resultParam = resultParam + "; message:"+message.getMessage();
		
		StringBuffer bucketString = new StringBuffer();
		for(int i=0;i<bucketList.size();i++){
			bucketString.append(bucketList.get(i).getName() +"  ");
		}
		resultParam = resultParam + "; data:" + bucketString.toString();
		result.setText(resultParam);
		
	}

	private void doGetBucket() {
		Message message = new Message();
		
		Map map = new HashMap();
		String bucket_param = bukcetIdText.getText().toString();
		
		map.put("bucketId",bucket_param);
		
		CosBucket bucket = new CosBucket();
		
		tencentCos.COSGetBucket(map, bucket, message,context);
		
		acl.setText(String.valueOf(bucket.getAcl()));
		referer.setText(bucket.getReferer());
		
		
		String resultParam = "";
		
		
		resultParam = "code:"+message.getCode();
		resultParam = resultParam + "; message:"+message.getMessage();
		resultParam = resultParam + "; data:" + "acl=" + Integer.toString(bucket.getAcl()) + " referer=" + bucket.getReferer();

		result.setText(resultParam);
		
	}

	private void doSetBucket() {
		Message message = new Message();
		
		Map map = new HashMap();
		String bucket_param = bukcetIdText.getText().toString();
		String acl_text = acl.getText().toString();
		String referer_text = referer.getText().toString();
		map.put("bucketId",bucket_param);
		map.put("acl",acl_text);
		map.put("referer", referer_text);
		
		tencentCos.COSSetBucket(map, message,context);
		
		String resultParam = "";
		
		
		resultParam = "code:"+message.getCode();
		resultParam = resultParam + "; message:"+message.getMessage();

		result.setText(resultParam);
		
		
	}

	private void doRmBucket() {
		
		Message message = new Message();
		
		Map map = new HashMap();
		String bucket_param = bukcetIdText.getText().toString();
		
		map.put("bucketId", bucket_param);
		
		tencentCos.COSDeleteBucket(map, message,context);
		
		String resultParam = "";
		
		
		resultParam = "code:"+message.getCode();
		resultParam = resultParam + "; message:"+message.getMessage();

		result.setText(resultParam);
		
	}

	private void doCreateBucket() {
        System.out.println("-------------------------------test--------------------");
		Message message = new Message();
		
		Map map = new HashMap();
		String bucket_param = bukcetIdText.getText().toString();
		
		map.put("bucketId", bucket_param);
		

		String acl_text = acl.getText().toString();
		String referer_text = referer.getText().toString();
		if(acl_text != null && !acl_text.equals("")){
			map.put("acl", acl_text);
		}
		map.put("referer", referer_text);
		tencentCos.COSCreateBucket(map, message,context);
		
		String resultParam = "";
		
		
		resultParam = "code:"+message.getCode();
		resultParam = resultParam + "; message:"+message.getMessage();

		result.setText(resultParam);
	}

}
