package com.zklcsoftware.aimodel.repository;

import org.springframework.stereotype.Repository;
import com.zklcsoftware.basic.repository.BaseDao;
import com.zklcsoftware.aimodel.domain.TAiTkXstk;

/**
 * Service Interface:TAiTkXstk
 * @author zklcsoftware_projectarchitecture
 * @date Apr 19, 2025
 */
@Repository
public interface TAiTkXstkRepository extends BaseDao<TAiTkXstk,Integer>, TAiTkXstkRepositoryExt {
	
}