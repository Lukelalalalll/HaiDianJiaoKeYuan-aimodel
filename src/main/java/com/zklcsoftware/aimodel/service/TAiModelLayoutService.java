package com.zklcsoftware.aimodel.service;

import java.util.List;

import com.zklcsoftware.aimodel.domain.TAiModelLayout;
import com.zklcsoftware.aimodel.dto.TAiModelLayoutDTO;
import com.zklcsoftware.basic.service.BaseService;

public interface TAiModelLayoutService extends BaseService<TAiModelLayout,String> {
	
	/**
	 * 根据用户类型获取用户布局信息
	 * @param userTypeCode
	 * @return
	 */
    List<TAiModelLayoutDTO> getUserModelLayoutInfoByUserTypeCode(String userTypeCode,String layoutId);
    
}
