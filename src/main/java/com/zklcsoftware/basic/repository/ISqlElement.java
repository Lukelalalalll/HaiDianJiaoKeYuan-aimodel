package com.zklcsoftware.basic.repository;

import java.util.Map;

/**
 * SQL元素。
 * 
 * @author 陈飞飞
 *
 */
public interface ISqlElement {
	/**
	 * 获取所有有效的参数值。
	 * 
	 * @return 有效的参数值数组。
	 */
	public Object[] getParams();
	
	/**
	 * 获取所有有效的命名参数键值对。
	 * 
	 * @return 有效的参数键值对。
	 */
	public Map<String, Object> getParamsMap();
	
	/**
	 * 获取处理后的真实SQL语句。
	 * 
	 * @return SQL语句。
	 */
	public String getSql();
}