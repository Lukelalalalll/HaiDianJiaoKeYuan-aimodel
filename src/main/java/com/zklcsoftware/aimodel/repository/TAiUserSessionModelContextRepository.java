package com.zklcsoftware.aimodel.repository;

import org.springframework.stereotype.Repository;
import com.zklcsoftware.basic.repository.BaseDao;
import com.zklcsoftware.aimodel.domain.TAiUserSessionModelContext;

import java.util.List;

/**
 * Service Interface:TAiUserSessionModelContext
 * @author zklcsoftware_projectarchitecture
 * @date Oct 18, 2024
 */
@Repository
public interface TAiUserSessionModelContextRepository extends BaseDao<TAiUserSessionModelContext,String>, TAiUserSessionModelContextRepositoryExt {
	public List<TAiUserSessionModelContext> findFirst40BySessionModelIdOrderByCreateTimeDesc(String sessionModelId);

	public List<TAiUserSessionModelContext> findFirst10BySessionModelIdOrderByCreateTimeDesc(String sessionModelId);


}