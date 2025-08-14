package com.zklcsoftware.common.web.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Base64Utils;

public class Base64Util {
	
	/**
	 * 根据图片地址转换为base64编码字符串
	 * @param url
	 * @return
	 */
	public static String getImageStr(String url) {
		//判断url类型
		if(!StringUtils.isBlank(url)){
			try {
				if(url.contains("http://") || url.contains("https://")){
					//网络图片暂时不做处理
					return url;
//					//下载网络文件
//		    		URL curl = new URL(url);    
//		    	    HttpURLConnection conn = (HttpURLConnection)curl.openConnection();    
//		    	    //设置超时间为3秒  
//		    	    conn.setConnectTimeout(3*1000);  
//		    	    //防止屏蔽程序抓取而返回403错误  
//		    	    conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");  
//	    	        //得到输入流  
//	    	        InputStream in = conn.getInputStream();    
//	    	        //获取自己数组  
//	    	        byte[] getData = readInputStream(in);
//	    	        in.read(getData);
//	    	        in.close();
//	    	        // 加密
//				    return "data:image/jpeg;base64,"+Base64Utils.encodeToString(getData);
				}else{
					InputStream inputStream = null;
					byte[] data = null;
			        inputStream = new FileInputStream(url);
			        data = new byte[inputStream.available()];
			        inputStream.read(data);
			        inputStream.close();
			        // 加密
					if(url.toLowerCase().endsWith(".png")){
						return "data:image/png;base64,"+Base64Utils.encodeToString(data);
					}else{
						return "data:image/jpeg;base64,"+Base64Utils.encodeToString(data);
					}

				}
			} catch (IOException e) {
		        return null;
		    }
		}else{
			return null;
		}
	}
	
	/**
     * 从输入流中获取字节数组
     * @param inputStream
     * @return
     * @throws IOException
     */
    private static  byte[] readInputStream(InputStream inputStream) throws IOException {  
        byte[] buffer = new byte[1024];  
        int len = 0;  
        ByteArrayOutputStream bos = new ByteArrayOutputStream();  
        while((len = inputStream.read(buffer)) != -1) {  
            bos.write(buffer, 0, len);  
        }  
        bos.close();  
        return bos.toByteArray();  
    }

	/**
	 * 对指定路径的文件进行 Base64 编码
	 *
	 * @param filePath 文件路径
	 * @return Base64 编码后的字符串
	 */
	public static String encodeFileToBase64(String filePath) {
		try {
			// 读取文件内容为字节数组
			byte[] fileContent = Files.readAllBytes(Paths.get(filePath));

			// 使用 Base64 编码器对字节数组进行编码
			String encodedString = Base64.getEncoder().encodeToString(fileContent);

			return encodedString;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
