package com.tencent.cos;

import java.util.List;
import java.util.Map;

import android.R.integer;
import android.content.Context;

import com.tencent.cos.bean.CosBucket;
import com.tencent.cos.bean.CosDirectory;
import com.tencent.cos.bean.CosFile;
import com.tencent.cos.bean.Message;

public interface COS {

	public boolean COSMkDir(Map param, Message message, Context context);
	
	public boolean COSMkDir(Map param, Message message);

	public boolean COSRmDir(Map param, Message message, Context context);
	
	public boolean COSRmDir(Map param, Message message);

	public boolean COSListFile(Map param, List<CosFile> fileList, List<CosDirectory> dirList,
                                 int foldertotalnum, int filetotalnum, Message message,Context context);
	
	public boolean COSListFile(Map param, List<CosFile> fileList, List<CosDirectory> dirList,
            int foldertotalnum, int filetotalnum, Message message);

	public boolean COSGetMeta(Map param, CosDirectory dir, Message message,Context context);
	
	public boolean COSGetMeta(Map param, CosDirectory dir, Message message);

	public boolean COSSetMeta(Map param, Message message,Context context);
	
	public boolean COSSetMeta(Map param, Message message);

	public boolean COSDeleteFile(Map param, Message message,Context context);
	
	public boolean COSDeleteFile(Map param, Message message);

	public boolean COSRename(Map param, Message message,Context context);
	
	public boolean COSRename(Map param, Message message);

	public boolean COSCreateBucket(Map param, Message message,Context context);
	
	public boolean COSCreateBucket(Map param, Message message);
	
	public boolean COSDeleteBucket(Map param, Message message,Context context);
	
	public boolean COSDeleteBucket(Map param, Message message);

	public boolean COSSetBucket(Map param, Message message,Context context);
	
	public boolean COSSetBucket(Map param, Message message);

	public boolean COSGetBucket(Map param, CosBucket bucket, Message message,Context context);
	
	public boolean COSGetBucket(Map param, CosBucket bucket, Message message);

	public boolean COSListBucket(Map param, List<CosBucket> bucketList, int foldertotalnum,
			                       Message message,Context context);
	
	public boolean COSListBucket(Map param, List<CosBucket> bucketList, int foldertotalnum,
            Message message);
	
	public boolean COSUploadFileByContent(Map param,byte[] content, Message message,Context context);

	public boolean COSUploadFileByContent(Map param, byte[] content, Message message);

	public boolean COSUploadFile(Map param, Message message,Context context);
	
	public boolean COSUploadFile(Map param, Message message);
	
	public boolean COSUploadFileByContentWithCompress(Map param,byte[] content, Message message, Context context);
	
	public boolean COSUploadFileByContentWithCompress(Map param, byte[] content, Message message);
	
	public boolean COSUploadFileWithCompress(Map param, Message message,Context context);
	
	public boolean COSUploadFileWithCompress(Map param, Message message);
	
	public boolean COSCompressOnlineFile(Map param, Message message,Context context);
	
	public boolean COSCompressOnlineFile(Map param, Message message);
	

}
