package com.zklcsoftware.aimodel.repository;

import org.springframework.stereotype.Repository;
import com.zklcsoftware.basic.repository.BaseDao;
import com.zklcsoftware.aimodel.domain.TAiAttachment;

/**
 * Service Interface:TAiAttachment
 * @author zklcsoftware_projectarchitecture
 * @date Mar 19, 2025
 */
@Repository
public interface TAiAttachmentRepository extends BaseDao<TAiAttachment,String>, TAiAttachmentRepositoryExt {
	
}