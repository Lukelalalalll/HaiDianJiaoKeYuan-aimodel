package com.zklcsoftware.basic.util;

import java.io.*;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import com.zklcsoftware.common.web.controller.MyException;

public class StringUtil {

	public static double pers = 1048576; //1024*1024

    /**
     * 获取本机ip
     * @return
     */
    public static String getIp(){
        String ip = "";
        try {
            InetAddress inet = InetAddress.getLocalHost();
            ip = inet.getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return ip;
    }

    public static String returnPOColName(String dbColName){
        
        String [] strs=dbColName.split("_");
        String poColName="";
        for(int i=0;i<strs.length;i++){
            poColName =poColName+(strs[i].replaceFirst(strs[i].charAt(0) + "",
                    new String(strs[i].charAt(0) + "").toUpperCase()));
        }
        return poColName;
        
    }
    
    /**
     * 转换sql参数   aaa,bbbb,bbb==>  "aaa","bbb","ccc"
     */
    public static String replaceString(String param){
        StringBuilder sb = new StringBuilder();
        String para[] =param.split(",");
        for(int i =0;i<para.length;i++){
            sb.append("'"+para[i]+"',");
        }
        String aa = sb.toString();
        return aa.substring(0, aa.lastIndexOf(","));
    }
    
    /**
     * 转换sql参数   aaa,bbbb,bbb==>  "aaa","bbb","ccc"
     */
    public static String replaceString(List param){
        StringBuilder sb = new StringBuilder();
        
        for(int i =0;i<param.size();i++){
            sb.append("'"+param.get(i).toString()+"',");
        }
        String aa = sb.toString();
        if(!"".equals(aa)){
            return aa.substring(0, aa.lastIndexOf(","));
        }else{
            return "";
        }
        
    }
    
    /**
     * 转换科学计数法
     * @param obj
     * @return
     */
    public static String convertBig(String obj){
    	BigDecimal db = new BigDecimal(obj);
    	String ii = db.toPlainString();
    	
    	return ii;
    }
    
   /**
     * 
     * <p>
     * MB，KB转字节功能
     * </p>
     * @author sunjian 时间 2018年12月14日 上午9:38:48
     * @param str
     * @return
     */
    public static long sizeFormat(String str){
        long size = 0;
        if(StringUtils.isNotBlank(str)){
            if(str.toUpperCase().endsWith("KB")){
                size = (long)(Double.parseDouble(str.substring(0,str.length()-2))*1024);
            }else if(str.toUpperCase().endsWith("MB")){
                size = (long)(Double.parseDouble(str.substring(0,str.length()-2))*pers);
            }else if(str.toUpperCase().endsWith("M")){
                size = (long)(Double.parseDouble(str.substring(0,str.length()-2))*pers);
            }else if(str.toUpperCase().endsWith("GB")){
                size = (long)(Double.parseDouble(str.substring(0,str.length()-2))*1024*pers);
            }else if(str.toUpperCase().endsWith("G")){
                size = (long)(Double.parseDouble(str.substring(0,str.length()-2))*1024*pers);
            }
        }
        return size;
    }
    
    /**
     * 
     * <p>
     * 判断是否为允许的上传文件类型,true表示允许
     * </p>
     * @author sunjian 时间 2018年12月14日 上午9:39:05
     * @param fileName 文件名称
     * @param suffixs 限制的类型字符
     * @return
     */
    public static boolean checkFileType(String fileName, String suffixs) {
        // 转换类型为List
        List<String> suffixList = Arrays.asList(suffixs.split(","));
        for(String str : suffixList)
        {
            if(StringUtils.isNotBlank(str)){
                if (fileName.trim().toLowerCase().endsWith(str.toLowerCase())) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * 
     * <p>
     * 创建文件路径功能
     * </p>
     * @author sunjian 时间 2018年12月14日 上午10:21:01
     * @param path
     * @return
     * @throws MyException 
     */
    public static void createDirectory(String path) throws MyException {
        try {
            File wf = new File(path);
            if (!wf.exists()) {
                wf.mkdirs();
            }
        } catch (Exception e) {
            throw new MyException("系统找不到指定的文件！");
        }
    }

    /**
     * 利用BufferedReader实现Inputstream转换成String <功能详细描述>
     *
     * @param in
     * @return String
     */

    public static String readStreamString(InputStream in, String encode) {
        String str = "";
        try {
            if (StringUtils.isEmpty(encode)) {
                // 默认以utf-8形式
                encode = "utf-8";
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, encode));
            StringBuffer sb = new StringBuffer();
            while ((str = reader.readLine()) != null) {
                sb.append(str).append("\n");
            }
            return sb.toString();
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return str;
    }

    public static boolean isJavaClass(Class<?> clz) {
        return clz != null && clz.getClassLoader() == null;
    }

    /**
     * 转换${}类型的值替换
     * @param content
     * @param kvs
     * @return
     */
    public static String parseData(String content, Map<String,String> kvs){
        Pattern p = Pattern.compile("(\\$\\{)([\\w]+)(\\})");
        Matcher m = p.matcher(content);
        StringBuffer sr = new StringBuffer();
        while(m.find()){
            String group = m.group();
            m.appendReplacement(sr, kvs.get(group));
        }
        m.appendTail(sr);
        return sr.toString();
    }

}
