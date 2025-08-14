/**
 * 
 */
package com.zklcsoftware.basic.repository;

/**
 * 动态SQL处理异常。
 * 
 * @author 陈飞飞
 * 2014年3月7日
 */
@SuppressWarnings("serial")
public class DynamicSqlException extends RuntimeException {

	/**
	 * 
	 */
	public DynamicSqlException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 */
	public DynamicSqlException(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 */
	public DynamicSqlException(Throwable arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public DynamicSqlException(String arg0, Throwable arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

}
