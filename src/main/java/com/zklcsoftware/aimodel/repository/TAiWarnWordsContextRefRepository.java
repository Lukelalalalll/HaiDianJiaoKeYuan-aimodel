package com.zklcsoftware.aimodel.repository;

import org.springframework.stereotype.Repository;
import com.zklcsoftware.basic.repository.BaseDao;
import com.zklcsoftware.aimodel.domain.TAiWarnWordsContextRef;

/**
 * Service Interface:TAiWarnWordsContextRef
 * @author zklcsoftware_projectarchitecture
 * @date Nov 6, 2024
 */
@Repository
public interface TAiWarnWordsContextRefRepository extends BaseDao<TAiWarnWordsContextRef,Integer>, TAiWarnWordsContextRefRepositoryExt {
	
}