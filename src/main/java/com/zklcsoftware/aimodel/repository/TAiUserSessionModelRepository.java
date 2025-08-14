package com.zklcsoftware.aimodel.repository;

import org.springframework.stereotype.Repository;
import com.zklcsoftware.basic.repository.BaseDao;
import com.zklcsoftware.aimodel.domain.TAiUserSessionModel;

import java.util.List;

/**
 * Service Interface:TAiUserSessionModel
 * @author zklcsoftware_projectarchitecture
 * @date Oct 18, 2024
 */
@Repository
public interface TAiUserSessionModelRepository extends BaseDao<TAiUserSessionModel,String>, TAiUserSessionModelRepositoryExt {
	public List<TAiUserSessionModel> findBySessionId(String sessionId);
}