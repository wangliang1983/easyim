package com.easyim.biz.api.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * MD5的工具类
 * @author wl
 *
 */
public abstract class MD5Util {


	/**
	 * 做md5签名
	 * @param keyMap
	 * @param secure
	 * @return
	 */
	public static String md5(Map<String,String> keyMap,String secure){
		List<String> keyList =  new ArrayList<String>();
		
		Set<String> keys = keyMap.keySet();
		for(String key:keys){
			keyList.add(key);
		}
		Collections.sort(keyList);
		
		StringBuilder sb = new StringBuilder();
		for(String key:keyList){
			sb.append(key);
			sb.append("=");
			if(keyMap.get(key)!=null){
				sb.append(keyMap.get(key));
			}
			sb.append("&");
		}
		sb.append("key=").append(secure);
			
		return md5(sb.toString());
	}
	
	
	
	public static String md5(String sourceStr){
        String result = "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            try {
				md.update(sourceStr.getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
            byte b[] = md.digest();
            int i;
            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
            result = buf.toString().toUpperCase();
        } catch (NoSuchAlgorithmException e) {
        	 throw new RuntimeException(e);
        }
        return result;
    }
	

}
