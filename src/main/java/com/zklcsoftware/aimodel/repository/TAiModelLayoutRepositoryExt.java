package com.zklcsoftware.aimodel.repository;

import java.util.List;

import com.zklcsoftware.aimodel.dto.TAiModelLayoutDTO;
import com.zklcsoftware.basic.repository.BaseRepositoryExt;

/**
 * Service Interface:TAiModelLayout
 * @author administrator
 * @date 22-三月-25
 */
public interface TAiModelLayoutRepositoryExt extends BaseRepositoryExt {

	List<TAiModelLayoutDTO> getUserModelLayoutInfoByUserTypeCode(String userTypeCode,String layoutId);
	
}
