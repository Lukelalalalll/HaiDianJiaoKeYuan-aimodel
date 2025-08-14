package com.zklcsoftware.aimodel.repository;

import org.springframework.stereotype.Repository;
import com.zklcsoftware.basic.repository.BaseDao;
import com.zklcsoftware.aimodel.domain.TAiModelComponent;

/**
 * Service Interface:TAiModelComponent
 * @author administrator
 * @date 24-三月-25
 */
@Repository
public interface TAiModelComponentRepository extends BaseDao<TAiModelComponent,String>, TAiModelComponentRepositoryExt {
	
}
