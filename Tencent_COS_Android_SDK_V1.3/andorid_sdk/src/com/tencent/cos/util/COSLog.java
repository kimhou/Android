package com.tencent.cos.util;

import android.util.Log;

public class COSLog {
	
	public static boolean Debug = true;
	
	public static void v(String tag,String msg){
        if(Debug){
            Log.v(tag, msg);
        }
    }
     
    public static void d(String tag,String msg){
        if(Debug){
            Log.d(tag, msg);
        }
    }
    
    public static void i(String tag,String msg){
        if(Debug){
            Log.i(tag, msg);
        }
    }
    
    public static void w(String tag,String msg){
        if(Debug){
            Log.w(tag, msg);
        }
    }
    public static void e(String tag,String msg){
        if(Debug){
            Log.e(tag, msg);
        }
    }    
}
