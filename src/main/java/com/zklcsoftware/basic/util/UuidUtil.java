package com.zklcsoftware.basic.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

public class UuidUtil {

	private static String localIP = StringUtil.getIp();
	private static String strs = "0123456789qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM";
	private static  SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
	public static String get32UUID() {
		String uuid = UUID.randomUUID().toString().trim().replaceAll("-", "");
		return uuid;
	}
	
	/**
	 * @param prebus   需要产生SNSUUID码的前缀业务代码   必须为6码    基本一个业务表对应一个前缀业务码
	 * 
	 * @return 返回SNSUUID   prebus+time+radom(2)+localIP
	 * */
	public static String getSnsUUID(String prebus){
		String suid = null;
		if(prebus.length()==6){
			suid = prebus+df.format(new Date())+get2RdmStr()+localIP;
		}
		return suid;
	}
	
	private static String get2RdmStr(){
		StringBuffer buf = new StringBuffer();
		Random random = new Random();
		int i=0;
		int index = 0;
		while(true){
			if(i>1)
				break;
			index = random.nextInt(strs.length()-1);
			buf.append(strs.charAt(index));
			i++;
		}
		return buf.toString();
	}

}

