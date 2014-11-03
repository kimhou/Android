package com.tencent.cos.constant;

import java.net.InetAddress;

/**
 * SDK静态变量
 * 
 * @author mucdullge
 * 
 */
public class CosConst {

	public final static String version = "1.0.0";

	// 编码类型 UTF-8
	public static final String ENCODE_CODE = "UTF-8";
	
	public static final int FILE_MAX_LENGTH = 2147483647; // 2G

	/*
	 * 接入网类型
	 */
	public static final class APN {
		public final static int unknow = 0;
		public final static int cmnet = 1;
		public final static int cmwap = 2;
		public final static int _3gnet = 3;
		public final static int _3gwap = 4;
		public final static int uninet = 5;
		public final static int uniwap = 6;
		public final static int wifi = 7;
		public final static int ctwap = 8;
		public final static int ctnet = 9;
		public final static int cmcc = 10;
		public final static int unicom = 11;
		public final static int cmct = 12;
	}

	public static final class APNName {
		public final static String NAME_UNKNOWN = "unknown";
		public final static String NAME_CMNET = "cmnet";
		public final static String NAME_CMWAP = "cmwap";
		public final static String NAME_3GNET = "3gnet";
		public final static String NAME_3GWAP = "3gwap";
		public final static String NAME_UNINET = "uninet";
		public final static String NAME_UNIWAP = "uniwap";
		public final static String NAME_WIFI = "wifi";
		public final static String NAME_CTWAP = "ctwap";
		public final static String NAME_CTNET = "ctnet";
		public final static String NAME_CMCC = "cmcc";
		public final static String NAME_UNICOM = "unicom";
		public final static String NAME_CMCT = "cmct";
	}

	public static final int Operator_Unknown = 0;
	public static final int Operator_CMCC = 1;
	public static final int Operator_Unicom = 2;
	public static final int Operator_CMCT = 3;
	public static final int Operator_WIFI = 4;

	/**
	 * 终端平台类型
	 */
	public static final class PLAFORM {
		public static final byte IPHONE = 1;
		public static final byte ANDROID = 2;
	}
	
	// 服务器回包字段
	public static final String COS_SERVER_RET_CODE = "code";
	public static final String COS_SERVER_RET_MSG = "msg";
	public static final String COS_SERVER_RET_DATA = "data";
	
	// HOST
	public static final String COS_HOST = "http://cosapi.myqcloud.com";
	
	// API
	public static final String MKDIR_URL = "/api/cos_mkdir";

	public static final String RMDIR_URL = "/api/cos_rmdir";

	public static final String LISTFILE_URL = "/api/cos_list_file";

	public static final String GETMETA_URL = "/api/cos_get_meta";

	public static final String SETMETA_URL = "/api/cos_set_meta";

	public static final String DELETEFILE_URL = "/api/cos_delete_file";

	public static final String RENAME_URL = "/api/cos_rename";

	public static final String CREATEBUCKET_URL = "/api/cos_create_bucket";

	public static final String SETBUCKET_URL = "/api/cos_set_bucket";

	public static final String GETBUCKET_URL = "/api/cos_get_bucket";

	public static final String LISTBUCKET_URL = "/api/cos_list_bucket";

	public static final String DELETEBUCKET_URL = "/api/cos_delete_bucket";

	public static final String UPLOAD_URL = "/api/cos_upload";
	
	public static final String UPLOAD_WITHCOMPRESS_URL = "/api/cos_upload_with_compress";
	
	public static final String COMPRESS_FILE_URL = "/api/cos_compress_file";
	// api 参数	
	
	public static final String BUCKET_ID = "bucketId";
	
	// 压缩后文件存放的bucket
	public static final String COMPRESS_BUCKET_ID = "compressBucketId";
	
	// 原始文件上传后存放的bucket
	public static final String UPLOAD_BUCKET_ID = "uploadBucketId";
	
	// 在线压缩时，待压缩文件所在的bucket
	public static final String SRC_BUCKET_ID = "srcBucketId";
	
	// 在线压缩时，压缩后文件存放的bucket
	public static final String DST_BUCKET_ID = "dstBucketId";
	
	public static final String PATH = "path";
	
	// 压缩后文件的存放路径
	public static final String COMPRESS_FILE_PATH = "compressFilePath";
	
	// 原始文件上传后存放的完整路径
	public static final String UPLOAD_FILE_PATH = "uploadFilePath";
	
	// 在线压缩时，待压缩文件所在路径
	public static final String SRC_FILE_PATH = "srcFilePath";	
	
	// 在线压缩时，压缩后文件的存放路径
	public static final String DST_FILE_PATH = "dstFilePath";
	
	public static final String OFFSET = "offset";
	
	public static final String COUNT = "count";
	
	public static final String PREFIX = "prefix";
	
	public static final String SPATH = "spath";
	
	public static final String DPATH = "dpath";
	
	public static final String ACL = "acl";
	
	public static final String REFERER = "referer";
	
	public static final String CRTTIME = "crttime";
	
	public static final String UPTIME = "uptime";
	
	public static final String FINISHFLAG = "finish_flag";
	
	public static final String MD5 = "md5";
	
	public static final String NAME = "name";
	
	public static final String TYPE = "type";
	
	public static final String FSIZE = "fsize";
	
	public static final String CACHE_CONTROL = "cacheControl";
	
	public static final String CONTENT_LANGUAGE = "contentLanguage";
	
	public static final String CONTENT_ENCODING = "contentEncoding";
	
	public static final String CONTENT_DISPOSITION = "contentDisposition";
	
	public static final String EXPIRES = "expires";
	
	public static final String FOLDER_TOTAL_NUM = "foldertotalnum";
	
	public static final String FILE_TOTAL_NUM = "filetotalnum";
	
	public static final String DIRENT_LST = "direntlst";
	
	public static final String DELETE_OBJ = "deleteObj";
	
	public static final String FILES = "files";
	
	public static final String COSFILE = "cosFile";
	
	public static final String LOCAL_FILE_PATH = "localFilePath";
	
	public static final int TIMEOUT = 20000;
	
	public static final String HOST = "cosapi.myqcloud.com";
	
	public static final int retryTimes = 3;

	public static final int sendMinutes = 10;
	
	public static final String APPKEY = "A4CJC52Z2QZ3";
	
	public static final String HTTPID = "Cos-Header-Rid";
	
	public static InetAddress[] inetAdd = null;
	
	public static int getDNStimes =0;
	
	public static String httpResult = "";
	

}
