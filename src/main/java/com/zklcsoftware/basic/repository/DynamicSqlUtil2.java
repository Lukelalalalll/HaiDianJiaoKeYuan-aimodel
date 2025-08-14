/**
 * 
 */
package com.zklcsoftware.basic.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 动态SQL处理工具类。范例：<br/>
 * select * from user where 1=1 {{lastName?and last_name like %:lastName%}} {{firstName ? and first_name like :firstName%}} {{ageLt?and age<=:ageLt}} {{ageGt?and age>=:ageGt}} order by id
 * <br/>
 * {{参数名?附加语句}}<br/>
 * 当参数名对应的值不为<code>null</code>，并且当值类型为字符串是不是空字符串时，附加语句有效。
 * 
 * @author 陈飞飞
 */
public class DynamicSqlUtil2 {
	/**
	 * 处理动态SQL语句。
	 * 
	 * @param params 命名参数和值的键对。
	 * @param sqlTemplate 支持Freemarker语法的动态SQL语句。
	 * @return SQL元素。
	 * @throws DynamicSqlException 动态SQL处理异常。
	 */
	public static ISqlElement processSql(Map<String, Object> params, String sqlTemplate) throws DynamicSqlException {
        Pattern p1 = Pattern.compile ("\\{\\{(.*?)\\}\\}");
        int offset = 0;
        Matcher m1 = p1.matcher(sqlTemplate);
        StringBuffer buff=new StringBuffer();
        while (! m1.hitEnd()) {
        	if (m1.find(offset)) {
        		int fromIndex=m1.start();
        		buff.append(sqlTemplate.substring(offset,fromIndex));
        		String subs = m1.group(1);
        		int indexOfQ = subs.indexOf("?");
        		String key = subs.substring(0, indexOfQ).trim();
        		Object value = params.get(key);
        		if (value != null) {
        			boolean hasContent = true;
        			if (value instanceof String) {
        				hasContent = !((String) value).trim().equals("");
        			}
        			if (hasContent) {
        				buff.append(subs.substring(indexOfQ+1, subs.length()).trim());
        			}
        		}
        		offset=m1.end();
        	}
        }
        String sql = (buff.toString()+sqlTemplate.substring(offset)).trim();
        sql = sql.replaceAll("[oO][rR][dD][eE][rR]\\s+[bB][yY]$", "");
        
        // holder for avaliable parameters.
        Map<String, Object> paramsMap = new HashMap<String, Object>();
        List<Object> paramNamesList = new ArrayList<Object>();
        
        Pattern p = Pattern.compile ("(:([a-zA-Z0-9_]+))");
        Matcher m = p.matcher (sql);
        while (m.find()) {
        	String name = m.group(2);
        	paramsMap.put(name, params.get(name));
        	//paramValuesList.add(params.get(name));
        	paramNamesList.add(name);
        }
          Object[] paramValues = new Object[paramNamesList.size()];
        // handle like
        // handle like %:username
        p = Pattern.compile ("(%:([a-zA-Z0-9_]+))");
        m = p.matcher (sql);
        while (m.find()) {
        	String key = m.group(2);
        	Object v = paramsMap.get(key);
        	if (v instanceof String) {
        		String v1 = (String) v;
        		if (! v1.startsWith("%")) {
        			paramsMap.put(key, "%" + v1);
        		}
        	}
        }
        // handle :username%
        p = Pattern.compile ("(:([a-zA-Z0-9_]+)%)");
        m = p.matcher (sql);
        while (m.find()) {
        	String key = m.group(2);
        	Object v = paramsMap.get(key);
        	if (v instanceof String) {
        		String v1 = (String) v;
        		if (! v1.endsWith("%")) {
        			paramsMap.put(key, v1 + "%" );
        		}
        	}
        }
        for(int i=0; i<paramNamesList.size(); i++)
        {
        	paramValues[i] = paramsMap.get(paramNamesList.get(i));
        }
        // remove all '%'.
//        sql = sql.replaceAll("%", "");
        sql = sql.replaceAll("%:([a-zA-Z0-9_]+)%", "?");
        sql = sql.replaceAll("%:([a-zA-Z0-9_]+)", "?");
        sql = sql.replaceAll(":([a-zA-Z0-9_]+)%", "?");
        // end handle like
        
        // replace all named params with ?
        sql = sql.replaceAll(":([a-zA-Z0-9_]+)", "?");
        
        
		SqlElementImpl s = new SqlElementImpl();
		s.setParams(paramValues);
		s.setParamsMap(paramsMap);
		s.setSql(sql);
		
		return s;
	}
	
	/*public static void main(String[] args) {
		Map<String, Object> params = new HashMap<>();
		params.put("lastName", "chen");
		params.put("firstName", "a");
		params.put("ageLt", 30);
		params.put("ageGt", 10);
		params.put("orderby_last_name", "desc");
		ISqlElement sql = DynamicSqlUtil2.processSql(params, "select * from user where 1=1 {{lastName?and last_name like %:lastName%}} {{firstName ? and first_name like :firstName%}} {{ageLt?and age<=:ageLt}} {{ageGt?and age>=:ageGt}} order by {{orderby_id?id $orderby_id$}} {{orderby_last_name?last_name $orderby_last_name$}}");
		System.out.println(sql.getSql());
		Object[] params2 = sql.getParams();
		for (Object object : params2) {
			System.out.println(object);
		}
	}*/
}
