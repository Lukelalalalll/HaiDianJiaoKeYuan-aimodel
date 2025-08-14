package com.zklcsoftware.aimodel.repository;

import com.zklcsoftware.aimodel.vo.TAiUserSessionModelContextFileindexVO;
import com.zklcsoftware.basic.repository.BaseRepositoryExt;

import java.util.List;
import java.util.Map;

/**
 * Service Interface:TAiUserSessionModelContextFileindex
 * @author zklcsoftware_projectarchitecture
 * @date Mar 21, 2025
 */
public interface TAiUserSessionModelContextFileindexRepositoryExt extends BaseRepositoryExt {

    List<TAiUserSessionModelContextFileindexVO> queryFileIndexBySessionModelId(Map<String,Object> params);
}