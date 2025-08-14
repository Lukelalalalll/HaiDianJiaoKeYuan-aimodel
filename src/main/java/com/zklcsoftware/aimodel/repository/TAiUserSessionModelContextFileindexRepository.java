package com.zklcsoftware.aimodel.repository;

import org.springframework.stereotype.Repository;
import com.zklcsoftware.basic.repository.BaseDao;
import com.zklcsoftware.aimodel.domain.TAiUserSessionModelContextFileindex;

/**
 * Service Interface:TAiUserSessionModelContextFileindex
 * @author zklcsoftware_projectarchitecture
 * @date Mar 21, 2025
 */
@Repository
public interface TAiUserSessionModelContextFileindexRepository extends BaseDao<TAiUserSessionModelContextFileindex,String>, TAiUserSessionModelContextFileindexRepositoryExt {
	
}