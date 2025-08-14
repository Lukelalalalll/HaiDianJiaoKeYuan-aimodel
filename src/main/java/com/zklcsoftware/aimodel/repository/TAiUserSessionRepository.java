package com.zklcsoftware.aimodel.repository;

import org.springframework.stereotype.Repository;
import com.zklcsoftware.basic.repository.BaseDao;
import com.zklcsoftware.aimodel.domain.TAiUserSession;

import java.util.List;

/**
 * Service Interface:TAiUserSession
 * @author zklcsoftware_projectarchitecture
 * @date Oct 18, 2024
 */
@Repository
public interface TAiUserSessionRepository extends BaseDao<TAiUserSession,String>, TAiUserSessionRepositoryExt {
	public List<TAiUserSession> findByUserIdAndIsDelAndSysPromptIdIsNotNullOrderByCreateTimeDesc(String userId,Integer isDel);

    public List<TAiUserSession> findByUserIdAndIsDelAndSysPromptIdIsNullOrderByUpdateTimeDesc(String userId,Integer isDel);

    public TAiUserSession findByUserIdAndSysPromptIdAndIsDel(String userId,String sysPromptId,Integer isDel);

    List<TAiUserSession> findByUserIdAndIsDelAndSysPromptIdOrderByUpdateTimeDesc(String userId, Integer isDel,String sysPromptId);

    List<TAiUserSession> findFirst3ByUserIdAndIsDelAndSysPromptIdOrderByUpdateTimeDesc(String userId, Integer isDel,String sysPromptId);


    List<TAiUserSession> findFirst3ByUserIdAndIsDelAndSysPromptIdIsNullOrderByUpdateTimeDesc(String userId, Integer isDel);
}