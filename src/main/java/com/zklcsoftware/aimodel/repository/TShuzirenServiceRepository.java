package com.zklcsoftware.aimodel.repository;

import org.springframework.stereotype.Repository;
import com.zklcsoftware.basic.repository.BaseDao;
import com.zklcsoftware.aimodel.domain.TShuzirenService;

import java.util.List;

/**
 * Service Interface:TShuzirenService
 * @author zklcsoftware_projectarchitecture
 * @date Apr 3, 2025
 */
@Repository
public interface TShuzirenServiceRepository extends BaseDao<TShuzirenService,Integer>, TShuzirenServiceRepositoryExt {

    /**
     * 根据状态查询
     * @param status
     * @return
     */
    List<TShuzirenService> findByStatus(Integer status);
}