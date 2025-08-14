package com.zklcsoftware.aimodel.repository;

import org.springframework.stereotype.Repository;
import com.zklcsoftware.basic.repository.BaseDao;
import com.zklcsoftware.aimodel.domain.TAiUserPrompt;

import java.util.List;

/**
 * Service Interface:TAiUserPrompt
 * @author zklcsoftware_projectarchitecture
 * @date Oct 18, 2024
 */
@Repository
public interface TAiUserPromptRepository extends BaseDao<TAiUserPrompt,String>, TAiUserPromptRepositoryExt {

    public List<TAiUserPrompt> findByIsDelOrderByCreateTimeDesc(Integer isDel);
	
}