package com.zklcsoftware.aimodel.repository;

import com.zklcsoftware.aimodel.domain.TAiUserSessionModelContext;
import com.zklcsoftware.aimodel.vo.TAiUserSessionModelContextVO;
import com.zklcsoftware.basic.repository.BaseRepositoryExt;

import java.util.List;

/**
 * Service Interface:TAiUserSessionModelContext
 * @author zklcsoftware_projectarchitecture
 * @date Oct 18, 2024
 */
public interface TAiUserSessionModelContextRepositoryExt extends BaseRepositoryExt {
    public List<TAiUserSessionModelContextVO> querySessionModelContextBySessionModelId(String sessionModelId);

    /**
     * @Description 智能体提问次数统计
     * @Author zhushaog
     * @param: id
     * @UpdateTime 2025/3/14 9:45
     * @return: java.lang.Long
     * @throws
     */
    Long countContextBySysPromptId(String id);

    public List<TAiUserSessionModelContext> queryBySessionModelId(String sessionModelId,Integer topN);
}