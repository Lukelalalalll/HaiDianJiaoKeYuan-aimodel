package com.zklcsoftware.aimodel.repository;

import java.util.List;

import com.zklcsoftware.aimodel.dto.TAiModelLayoutItemDTO;
import com.zklcsoftware.basic.repository.BaseRepositoryExt;

/**
 * Service Interface:TAiModelLayoutItem
 * @author administrator
 * @date 22-三月-25
 */
public interface TAiModelLayoutItemRepositoryExt extends BaseRepositoryExt {

	/**
	 * 根据布局id查询下的智能体信息
	 * @param layoutId
	 * @return
	 */
    public List<TAiModelLayoutItemDTO> getItemsByLayoutIdOrderbySort(String layoutId);
	
}
