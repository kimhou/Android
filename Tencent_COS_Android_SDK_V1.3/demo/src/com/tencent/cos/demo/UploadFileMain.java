package com.tencent.cos.demo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.util.EncodingUtils;

import com.tencent.cos.COS;
import com.tencent.cos.COSImpl;
import com.tencent.cos.bean.HttpHeader;
import com.tencent.cos.bean.Message;
import com.tencent.cos.constant.CosConst;
import com.tencent.cos.util.BaseFun;
import com.tencent.cos.util.COSLog;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

public class UploadFileMain extends Activity implements OnClickListener {

	private COS tencentCos = null;

	private TextView selectFileText = null;
	
	private EditText readText = null;
	
	private EditText bucketText = null;
	
	private EditText pathText = null;
	
	private EditText result = null;
	
	private final int IMAGE_CODE = 0; 
	private final String IMAGE_TYPE = "*/*";
	
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
			//用户上报cos调用信息
			String secretId = "AKID12312377987bkjhh213122312";
			tencentCos =  new COSImpl(1000128,"iqOLdwEPIQrDaJPbaBFUh3uf",httpHeader,context); //老用户只有accessId
			//tencentCos = new COSImpl(1000128,"",secretId,httpHeader,context); //新用户具有secretId
		} catch (Exception e) {
			e.printStackTrace();
		}
		setContentView(R.layout.upload_main);
		initViews();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	private void initViews() {
		findViewById(R.id.bucketShow).setOnClickListener(this);
		findViewById(R.id.selectFile).setOnClickListener(this);
		findViewById(R.id.doUpload).setOnClickListener(this);
		findViewById(R.id.fileShow).setOnClickListener(this);
		
		selectFileText = ((TextView) findViewById(R.id.selectFileView));
		
		
		
		bucketText = ((EditText)findViewById(R.id.bucketIdUploadText));
		pathText = ((EditText)findViewById(R.id.pathUploadText));
		result = ((EditText)findViewById(R.id.uploadResultText));
	}

	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.selectFile:
			doSelectFile();
			break;
		case R.id.doUpload:
			doUploadFile();
			break;
		case R.id.bucketShow:
			doBucket();
			break;
		case R.id.fileShow:
			doFile();
			break;
		default:
			break;
		}
	}

	private void doSelectFile() {
		Intent getAlbum = new Intent(Intent.ACTION_GET_CONTENT);
		getAlbum.setType(IMAGE_TYPE);
		startActivityForResult(getAlbum, IMAGE_CODE);
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		  if (resultCode != RESULT_OK) {    
			  return;
		  }
		  
		  Bitmap bm = null;
		  
		  ContentResolver resolver = getContentResolver();
		  
		  if (requestCode == IMAGE_CODE) {

                   Uri originalUri = data.getData();        //获得图片的uri 
                   String[] proj = {MediaStore.Images.Media.DATA};

                   Cursor cursor = managedQuery(originalUri, proj, null, null, null); 

                   int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

                   cursor.moveToFirst();

                   String path = cursor.getString(column_index);
                   selectFileText.setText(path);		   
		  } 
	}

	private void doFile() {
		Intent intent0 = new Intent(UploadFileMain.this, MainActivity.class);// 页面跳转的关键在这儿，注意这儿输入的是.class。
		setTitle("MainActivity");
		startActivity(intent0);
	}

	private void doBucket() {
		Intent intent0 = new Intent(UploadFileMain.this, BucketMain.class);// 页面跳转的关键在这儿，注意这儿输入的是.class。
		setTitle("BucketMain");
		startActivity(intent0);
	}

	private void doUploadFile() {
		Message message = new Message();
		
		Map map = new HashMap();
		String bucket_param = bucketText.getText().toString();
		String path_param = pathText.getText().toString();
		
		map.put("bucketId", bucket_param);
		map.put("path",path_param);
		map.put(CosConst.LOCAL_FILE_PATH,selectFileText.getText().toString());
		map.put("cosFile",BaseFun.GetFileName(selectFileText.getText().toString()));
		
		tencentCos.COSUploadFile(map, message,context);
		
		
		String resultParam = "";
		
		
		resultParam = "code : "+message.getCode();
		resultParam = resultParam + " message : "+message.getMessage();

		result.setText(resultParam);
	}


}
