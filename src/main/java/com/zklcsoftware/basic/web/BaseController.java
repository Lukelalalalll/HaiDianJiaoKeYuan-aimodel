package com.zklcsoftware.basic.web;

import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.zklcsoftware.basic.model.PageData;
import com.zklcsoftware.basic.util.UuidUtil;

public class BaseController {
	
	/**
	 * 获取页面元素信息
	 * @return
	 */
	protected PageData getPageData() {
		return new PageData(this.getRequest());
	}
	
	
	/**
	 * 得到request对象
	 */
	public HttpServletRequest getRequest() {
		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
		
		return request;
	}
	
	/**
	 * 得到32位的uuid
	 * @return
	 */
	public String get32UUID(){
		
		return UuidUtil.get32UUID();
	}
}
