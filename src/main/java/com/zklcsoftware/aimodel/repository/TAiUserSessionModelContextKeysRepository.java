package com.zklcsoftware.aimodel.repository;

import org.springframework.stereotype.Repository;
import com.zklcsoftware.basic.repository.BaseDao;
import com.zklcsoftware.aimodel.domain.TAiUserSessionModelContextKeys;

/**
 * Service Interface:TAiUserSessionModelContextKeys
 * @author zklcsoftware_projectarchitecture
 * @date Oct 21, 2024
 */
@Repository
public interface TAiUserSessionModelContextKeysRepository extends BaseDao<TAiUserSessionModelContextKeys,String>, TAiUserSessionModelContextKeysRepositoryExt {
	
}