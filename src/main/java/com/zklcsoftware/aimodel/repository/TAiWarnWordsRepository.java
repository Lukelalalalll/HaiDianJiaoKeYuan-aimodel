package com.zklcsoftware.aimodel.repository;

import org.springframework.stereotype.Repository;
import com.zklcsoftware.basic.repository.BaseDao;
import com.zklcsoftware.aimodel.domain.TAiWarnWords;

/**
 * Service Interface:TAiWarnWords
 * @author zklcsoftware_projectarchitecture
 * @date Nov 6, 2024
 */
@Repository
public interface TAiWarnWordsRepository extends BaseDao<TAiWarnWords,Integer>, TAiWarnWordsRepositoryExt {
	
}