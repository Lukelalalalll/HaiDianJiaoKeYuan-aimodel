/**
 *
 */
package com.zklcsoftware.basic.repository;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import freemarker.template.Configuration;
import freemarker.template.ObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * 动态SQL处理工具类, 支持Freemarker语法。
 *
 * @author 陈飞飞
 */
@SuppressWarnings("deprecation")
public class DynamicSqlUtil {
	protected static Configuration freeMarkerEngine = new Configuration();
	private static ConcurrentMap<String, Template> templateCache = new ConcurrentHashMap<String, Template>();

	static {
		freeMarkerEngine.setObjectWrapper(ObjectWrapper.BEANS_WRAPPER);
	}

	/**
	 * 处理动态SQL语句。
	 *
	 * @param params 命名参数和值的键对。
	 * @param sqlTemplate 支持Freemarker语法的动态SQL语句。
	 * @return SQL元素。
	 * @throws DynamicSqlException 动态SQL处理异常。
	 */
	public static ISqlElement processSql(Map<String, Object> params, String sqlTemplate) throws DynamicSqlException {
		Map<String, Object> context = new HashMap<String, Object>();
		for (Map.Entry<String, Object> paramEntry : params.entrySet()) {
            context.put(paramEntry.getKey(), paramEntry.getValue());
        }
		StringWriter out = new StringWriter();
        Template tpl = templateCache.get(sqlTemplate);
        if (tpl == null) {
            try {
				tpl = new Template("tpl", new StringReader(sqlTemplate), freeMarkerEngine);
			} catch (IOException e) {
				throw new DynamicSqlException("动态SQL语句读取出错。", e);
			}
            templateCache.put(sqlTemplate, tpl);
        }

        try {
			tpl.process(context, out);
		} catch (TemplateException e) {
			throw new DynamicSqlException("动态SQL中的FreeMarker语法错误。", e);
		} catch (IOException e) {
			throw new DynamicSqlException("动态SQL语句输出出错。", e);
		}
        String sql = out.toString();

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

	/**
	 * 处理动态SQL语句。(不包含占位符)
	 *
	 * @param params 命名参数和值的键对。
	 * @param sqlTemplate 支持Freemarker语法的动态SQL语句。
	 * @return SQL元素。
	 * @throws DynamicSqlException 动态SQL处理异常。
	 */
	public static String processSqlNoPlaceholders(Map<String, Object> params, String sqlTemplate) throws DynamicSqlException {
		Map<String, Object> context = new HashMap<String, Object>();
		for (Map.Entry<String, Object> paramEntry : params.entrySet()) {
			context.put(paramEntry.getKey(), paramEntry.getValue());
		}
		StringWriter out = new StringWriter();
		Template tpl = templateCache.get(sqlTemplate);
		if (tpl == null) {
			try {
				tpl = new Template("tpl", new StringReader(sqlTemplate), freeMarkerEngine);
			} catch (IOException e) {
				throw new DynamicSqlException("动态SQL语句读取出错。", e);
			}
			templateCache.put(sqlTemplate, tpl);
		}

		try {
			tpl.process(context, out);
		} catch (TemplateException e) {
			throw new DynamicSqlException("动态SQL中的FreeMarker语法错误。", e);
		} catch (IOException e) {
			throw new DynamicSqlException("动态SQL语句输出出错。", e);
		}
		return out.toString();
	}
}
