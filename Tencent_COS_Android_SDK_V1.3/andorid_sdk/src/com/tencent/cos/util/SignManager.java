package com.tencent.cos.util;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.util.Base64;
import android.util.Log;


/**
 * SDK签名
 * 
 * @author mucdullge
 * 
 */
public class SignManager {
	
	/**
	 * 生成用户访问签名
	 * @param <K>
	 * @param <V>
	 * @param sourceMap
	 * @param accessKey
	 * @param methodName
	 * @return
	 */
	public static <K, V> String MakeSign(Map sourceMap,
			String accessKey, String methodName) {

		Map<K,V> sort_sourceMap = new SignManager()
				.sortMap(sourceMap);
		
		String path_url = "";
		for (Map.Entry entry : sort_sourceMap.entrySet()) {
			String param = entry.getKey()+"="+entry.getValue();
			path_url = path_url+param+"&";
		}
		path_url = BaseFun.SubString(path_url,"&",false);
		
		path_url = methodName + "&"+path_url;
		
		Log.i("cos_sdk", "url : "+path_url);
		
		path_url = BaseFun.RawurlEncode(path_url);
		
		byte[] shaBytes = HMACSHA1.getHmacSHA1(path_url, accessKey);
     
		String sign = Base64.encodeToString(shaBytes, Base64.DEFAULT);
	
		return sign;
	}
	
	
	/**
	 * 针对map中key进行排序
	 * @param map
	 * @return
	 */
	private <K, V> Map<K, V> sortMap(Map<K, V> map) {
		class MyMap<M, N> {
			private M key;
			private N value;

			private M getKey() {
				return key;
			}

			private void setKey(M key) {
				this.key = key;
			}

			private N getValue() {
				return value;
			}

			private void setValue(N value) {
				this.value = value;
			}
		}

		List<MyMap<K, V>> list = new ArrayList<MyMap<K, V>>();
		for (Iterator<K> i = map.keySet().iterator(); i.hasNext();) {
			MyMap<K, V> my = new MyMap<K, V>();
			String key = (String) i.next();
			my.setKey((K)key);
			my.setValue(map.get(key));
			list.add(my);
		}

		Collections.sort(list, new Comparator<MyMap<K, V>>() {
			public int compare(MyMap<K, V> o1, MyMap<K, V> o2) {
				return ((String) o1.getKey()).compareTo((String)o2.getKey());
			}
		});

		Map<K, V> sortMap = new LinkedHashMap<K, V>();
		for (int i = 0, k = list.size(); i < k; i++) {
			MyMap<K, V> my = (MyMap)list.get(i);
			sortMap.put(my.getKey(), my.getValue());
		}
		return sortMap;
	}

}
