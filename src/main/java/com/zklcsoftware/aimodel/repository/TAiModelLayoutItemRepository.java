package com.zklcsoftware.aimodel.repository;

import org.springframework.stereotype.Repository;
import com.zklcsoftware.basic.repository.BaseDao;
import com.zklcsoftware.aimodel.domain.TAiModelLayoutItem;

/**
 * Service Interface:TAiModelLayoutItem
 * @author administrator
 * @date 22-三月-25
 */
@Repository
public interface TAiModelLayoutItemRepository extends BaseDao<TAiModelLayoutItem,String>, TAiModelLayoutItemRepositoryExt {
	
}
