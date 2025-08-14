package com.zklcsoftware.basic.util.io;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

public class FileLoadUtil {
	
	/**
	 * 加载Properties文件 返回map数据
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public static Map returnMapByProperties(String fileName) throws IOException{
		 InputStream in = new BufferedInputStream(new FileInputStream(fileName));
		 Properties p = new Properties();
		 p.load(in);
		return p;
	}
}
