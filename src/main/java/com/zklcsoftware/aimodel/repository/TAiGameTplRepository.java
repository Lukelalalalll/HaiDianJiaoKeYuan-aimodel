package com.zklcsoftware.aimodel.repository;

import com.zklcsoftware.aimodel.service.TAiGameTplService;
import org.springframework.stereotype.Repository;
import com.zklcsoftware.basic.repository.BaseDao;
import com.zklcsoftware.aimodel.domain.TAiGameTpl;

/**
 * Service Interface:TAiGameTpl
 * @author zklcsoftware_projectarchitecture
 * @date Mar 22, 2025
 */
@Repository
public interface TAiGameTplRepository extends BaseDao<TAiGameTpl,String>, TAiGameTplRepositoryExt {
    TAiGameTpl findBySysPromptId(String sysPromptId);
}