package com.zklcsoftware.aimodel.repository;

import org.springframework.stereotype.Repository;
import com.zklcsoftware.basic.repository.BaseDao;
import com.zklcsoftware.aimodel.domain.TAiSensitiveWords;

/**
 * Service Interface:TAiSensitiveWords
 * @author zklcsoftware_projectarchitecture
 * @date Oct 18, 2024
 */
@Repository
public interface TAiSensitiveWordsRepository extends BaseDao<TAiSensitiveWords,Integer>, TAiSensitiveWordsRepositoryExt {
	
}