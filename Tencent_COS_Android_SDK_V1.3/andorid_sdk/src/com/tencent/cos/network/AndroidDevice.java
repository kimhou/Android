package com.tencent.cos.network;

import java.util.HashMap;
import java.util.Map;

import com.tencent.cos.constant.CosConst;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiManager.WifiLock;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class AndroidDevice {

	private static final String TAG = AndroidDevice.class.getName();
	private static Uri PREFERRED_APN_URI = Uri
			.parse("content://telephony/carriers/preferapn");
	private static AndroidDevice sDevice = new AndroidDevice();
	private static final int CON_TYPE_NONE = -1;
	private volatile boolean mInited = false;

	private TelephonyManager mTelephonyMgr = null;
	private ConnectivityManager mConnectivityMgr = null;
	private Context mContext = null;
	//private NetworkChangeReceiver mNetworkReveiver = null;
	private WifiLock mWifiLock = null;
	private volatile int mNetworkType = CON_TYPE_NONE;
	private String mCurrentAPN = null;
	private String mDeviceInfo = null;
	private String mSimpleDeviceInfo = null;
	private Map<String, Integer> mApnMap = new HashMap<String, Integer>();
	private int JELLY_BEAN_NEW = 17;

	private int currentASU = Integer.MIN_VALUE;
	private boolean asuChanged = true;

	private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
		public void onSignalStrengthsChanged(
				android.telephony.SignalStrength signalStrength) {
			int asu = signalStrength.getGsmSignalStrength();

			currentASU = asu;
			asuChanged = true;

			super.onSignalStrengthsChanged(signalStrength);
		};
	};

	private String storageInfo = "";

	public static AndroidDevice Instance() {
		return sDevice;
	}

	private AndroidDevice() {
		mApnMap.put(CosConst.APNName.NAME_UNKNOWN, CosConst.APN.unknow);
		mApnMap.put(CosConst.APNName.NAME_CMNET, CosConst.APN.cmnet);
		mApnMap.put(CosConst.APNName.NAME_CMWAP, CosConst.APN.cmwap);
		mApnMap.put(CosConst.APNName.NAME_3GNET, CosConst.APN._3gnet);
		mApnMap.put(CosConst.APNName.NAME_3GWAP, CosConst.APN._3gwap);
		mApnMap.put(CosConst.APNName.NAME_UNINET, CosConst.APN.uninet);
		mApnMap.put(CosConst.APNName.NAME_UNIWAP, CosConst.APN.uniwap);
		mApnMap.put(CosConst.APNName.NAME_WIFI, CosConst.APN.wifi);
		mApnMap.put(CosConst.APNName.NAME_CTWAP, CosConst.APN.ctwap);
		mApnMap.put(CosConst.APNName.NAME_CTNET, CosConst.APN.ctnet);
		mApnMap.put(CosConst.APNName.NAME_CMCC, CosConst.APN.cmcc);
		mApnMap.put(CosConst.APNName.NAME_UNICOM, CosConst.APN.unicom);
		mApnMap.put(CosConst.APNName.NAME_CMCT, CosConst.APN.cmct);
	}
	
	/*public synchronized void release()
	{
		if (mContext != null)
		{
			IntentFilter upIntentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
			mContext.registerReceiver(mNetworkReveiver, upIntentFilter);
		}
	}
	
	

	public int getApnByName(String apnName)
	{
		return mApnMap.get(apnName);
	}

	public int getNetworkType()
	{
		return mNetworkType;
	}

	public boolean isNetworkAvailable()
	{
		boolean available = false;

		NetworkInfo info = mConnectivityMgr.getActiveNetworkInfo();

		if (info == null)
		{
			return false;
		}

		available = info.isConnected();

		if (available)
		{
			// 网络有效时，填充当前接入点
			mCurrentAPN = info.getExtraInfo();
		}
		else
		{
			// 网络无效时输出当前网络类型
			Log.i(TAG, "isNetworkEnable() : FALSE with TYPE = " + info.getType());
		}

		return available;
	}


	public String getAPN()
	{
		if (isViaWIFI())
		{
			return CosConst.APNName.NAME_WIFI;
		}

		if (mCurrentAPN == null)
		{
			ApnNode node = getDefaultAPN();
			if (node != null)
				mCurrentAPN = node.getApn();
			else
				mCurrentAPN = CosConst.APNName.NAME_UNKNOWN;
		}

		return mCurrentAPN;
	}

	public int getOperatorByAPN(int apn)
	{
		int operator = CosConst.Operator_Unknown;

		switch (apn)
		{
		case CosConst.APN.cmnet:
		case CosConst.APN.cmwap:
		case CosConst.APN.cmcc:
			operator = CosConst.Operator_CMCC;
			break;

		case CosConst.APN.uninet:
		case CosConst.APN.uniwap:
		case CosConst.APN.unicom:
		case CosConst.APN._3gnet:
		case CosConst.APN._3gwap:
			operator = CosConst.Operator_Unicom;
			break;

		case CosConst.APN.ctwap:
		case CosConst.APN.ctnet:
		case CosConst.APN.cmct:
			operator = CosConst.Operator_CMCT;
			break;

		case CosConst.APN.wifi:
			operator = CosConst.Operator_WIFI;
			break;

		default:
			operator = CosConst.Operator_Unknown;
			break;
		}

		return operator;
	}

	public int getAPNValue()
	{
		Integer apn = null;
		String apnName = getAPN();
		if (apnName != null)
		{
			apn = mApnMap.get(apnName);
		}

		// return (null != apn ? getOperator(apn) : WnsConst.APN.unknow);
		return (null != apn ? apn : CosConst.APN.unknow);
	}

	public boolean isViaWIFI()
	{

		if (mConnectivityMgr == null)
		{
			mConnectivityMgr = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		}

		NetworkInfo activeNetInfo = mConnectivityMgr.getActiveNetworkInfo();
		if (activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI)
		{
			return true;
		}

		return false;
	}
	
	public ApnNode getDefaultAPN()
	{
		String apn = "";
		String name = "";

		ApnNode apnNode = new ApnNode();

		if (android.os.Build.VERSION.SDK_INT < JELLY_BEAN_NEW)
		{
			Cursor mCursor = mContext.getContentResolver().query(PREFERRED_APN_URI, null, null, null, null);

			if (mCursor == null)
			{
				return null;
			}

			while (mCursor != null && mCursor.moveToNext())
			{
				name = mCursor.getString(mCursor.getColumnIndex("name"));
				apn = mCursor.getString(mCursor.getColumnIndex("apn")).toLowerCase();
			}
		}
		else
		{
			NetworkInfo activeNetInfo = mConnectivityMgr.getActiveNetworkInfo();
			if (activeNetInfo != null)
			{
				if (activeNetInfo.getType() == ConnectivityManager.TYPE_MOBILE)
				{
					apn = activeNetInfo.getExtraInfo().toLowerCase();
				}
				else if (activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI)
				{
					apn = CosConst.APNName.NAME_WIFI;
				}
			}
		}

		apnNode.setName(name);
		apnNode.setApn(apn);
		// WNSLog.i(TAG, "getDefaultAPN = " + apnNode);

		return apnNode;
	}
	
	
	public class NetworkChangeReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			Log.e(TAG, "NetworkChangeReceiver onReceive()"
					+ (context == null ? " with Context" : " without Context"));
			
			AndroidDevice.this.initialize(context);

			if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction()))
			{
				NetworkInfo ni = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
				if (ni == null)
				{
					Log.e(TAG, "onReceive NetworkInfo ni == null");
					return;
				}
				Log.i(TAG, ni.toString());
				if (ni.getState() == State.CONNECTED && ni.getType() == ConnectivityManager.TYPE_WIFI)
				{
					mCurrentAPN = ni.getExtraInfo();
					Log.i(TAG, "wifi connected,mCurrentAPN = " + mCurrentAPN);
					if (mNetworkType == ConnectivityManager.TYPE_MOBILE || mNetworkType == CON_TYPE_NONE)
					{
						mNetworkType = ConnectivityManager.TYPE_WIFI;
						ServiceManager.Instance().onWifiNetChanged(true);
						lockWifi();
					}
				}
				else if (ni.getState() == State.DISCONNECTED && ni.getType() == ConnectivityManager.TYPE_WIFI)
				{
					Log.e(TAG, "wifi disconnected");
					if (mNetworkType == ConnectivityManager.TYPE_WIFI)
					{
						mNetworkType = CON_TYPE_NONE;
					}
				}

				if (ni.getState() == State.CONNECTED && ni.getType() == ConnectivityManager.TYPE_MOBILE)
				{
					mCurrentAPN = ni.getExtraInfo();
					Log.i(TAG, "mobile connected,mCurrentAPN = " + mCurrentAPN);
					if (mNetworkType == CON_TYPE_NONE)
					{
						mNetworkType = ConnectivityManager.TYPE_MOBILE;
						ServiceManager.Instance().onMobileNetChanged(true);
					}
				}
				else if (ni.getState() == State.DISCONNECTED && ni.getType() == ConnectivityManager.TYPE_MOBILE)
				{
					// 在wifi正常连接的情况下，关闭APN会导致连接关闭
					if (mNetworkType == ConnectivityManager.TYPE_MOBILE)
					{
						mNetworkType = CON_TYPE_NONE;
					}
				}

				// XXX 当网络变化的时候，刷新设备信息
				getDeviceInfo(true);
				getSimpleDeviceInfo(true);
				System.out.println(mDeviceInfo);
				WnsCollector.Instance().onNetworkChanged();
			}
		}
	}*/

}
