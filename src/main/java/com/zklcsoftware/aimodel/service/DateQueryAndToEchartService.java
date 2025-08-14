package com.zklcsoftware.aimodel.service;

import com.zklcsoftware.aimodel.websocket.dto.AiOutMsgDTO;

/**
 * @author zcg
 * @ClassName DateQueryAndToEchartService.java
 * @Description 数据查询
 * @createTime 2025-01-12 11:30
 **/
public interface DateQueryAndToEchartService {

	/**
	 * 根据用户查询的数据，返回图标格式，前端直接渲染
	 * @param zntId
	 * @param content
	 * @param file
	 * @param request
	 * @param response
	 * @return
	 */
    AiOutMsgDTO DateQueryAndToEchart(String zntId, String content, String userId);
    //, HttpServletRequest request, HttpServletResponse response);
}
