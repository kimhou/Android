/**
 * 针对android4.0以上版本，通过dns解析获取IP
 */
package com.tencent.cos.network;

import java.net.InetAddress;

import android.content.Context;

import com.tencent.cos.constant.CosConst;
import com.tencent.cos.util.BaseFun;
import com.tencent.dns.Tencent;


public class GetDNS implements Runnable {
	
	private Context context;
	
	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	@Override
	public void run() {
		CosConst.inetAdd = null;
		
		String dnsHost = BaseFun.getHostName(CosConst.COS_HOST);
		Tencent httpTencent = new Tencent();

		CosConst.inetAdd = httpTencent.getHostByNameFrom114DNS(this.context,
				dnsHost);
	}

}
