package com.zklcsoftware.aimodel.repository;

import com.zklcsoftware.aimodel.domain.TAiUserSession;
import com.zklcsoftware.aimodel.dto.SessionlQueryDTO;
import com.zklcsoftware.basic.repository.BaseRepositoryExt;

import java.util.List;

/**
 * Service Interface:TAiUserSession
 * @author zklcsoftware_projectarchitecture
 * @date Oct 18, 2024
 */
public interface TAiUserSessionRepositoryExt extends BaseRepositoryExt {
    /**
     * @Description 查询用户session会话
     * @Author zhushaog
     * @param: userId
     * @param: isDel0
     * @param: sessionlQueryVO
     * @UpdateTime 2025/3/21 10:38
     * @return: java.util.List<com.zklcsoftware.aimodel.domain.TAiUserSession>
     * @throws
     */
    List<TAiUserSession> queryUserAiSession(String userId, Integer isDel, SessionlQueryDTO sessionlQueryVO);
}