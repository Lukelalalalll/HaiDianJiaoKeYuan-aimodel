package com.zklcsoftware.aimodel.repository;

import org.springframework.stereotype.Repository;
import com.zklcsoftware.basic.repository.BaseDao;
import com.zklcsoftware.aimodel.domain.TAiModelLayout;

/**
 * Service Interface:TAiModelLayout
 * @author administrator
 * @date 22-三月-25
 */
@Repository
public interface TAiModelLayoutRepository extends BaseDao<TAiModelLayout,String>, TAiModelLayoutRepositoryExt {
	
}
