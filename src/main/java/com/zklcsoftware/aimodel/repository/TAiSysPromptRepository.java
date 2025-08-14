package com.zklcsoftware.aimodel.repository;

import com.zklcsoftware.aimodel.domain.TAiUserPrompt;
import org.springframework.stereotype.Repository;
import com.zklcsoftware.basic.repository.BaseDao;
import com.zklcsoftware.aimodel.domain.TAiSysPrompt;

import java.util.List;

/**
 * Service Interface:TAiSysPrompt
 * @author zklcsoftware_projectarchitecture
 * @date Oct 18, 2024
 */
@Repository
public interface TAiSysPromptRepository extends BaseDao<TAiSysPrompt,String>, TAiSysPromptRepositoryExt {
    public List<TAiSysPrompt> findByIsDelOrderByCreateTimeDesc(Integer isDel);
}