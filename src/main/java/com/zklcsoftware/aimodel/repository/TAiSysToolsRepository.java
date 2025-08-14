package com.zklcsoftware.aimodel.repository;

import org.springframework.stereotype.Repository;
import com.zklcsoftware.basic.repository.BaseDao;
import com.zklcsoftware.aimodel.domain.TAiSysTools;

/**
 * Service Interface:TAiSysTools
 * @author zklcsoftware_projectarchitecture
 * @date Jun 10, 2025
 */
@Repository
public interface TAiSysToolsRepository extends BaseDao<TAiSysTools,String>, TAiSysToolsRepositoryExt {
	
}