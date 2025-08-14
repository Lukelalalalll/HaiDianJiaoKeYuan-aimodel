package com.zklcsoftware.aimodel.repository;

import org.springframework.stereotype.Repository;
import com.zklcsoftware.basic.repository.BaseDao;
import com.zklcsoftware.aimodel.domain.TAiShuzirenPpt;

import java.util.List;

/**
 * Service Interface:TAiShuzirenPpt
 * @author zklcsoftware_projectarchitecture
 * @date Apr 11, 2025
 */
@Repository
public interface TAiShuzirenPptRepository extends BaseDao<TAiShuzirenPpt,String>, TAiShuzirenPptRepositoryExt {

    /**
     * 根据微课id查询ppt列表
     * @param courseId
     * @param isDel
     * @return
     */
    List<TAiShuzirenPpt> findByCourseIdAndIsDelOrderByPptXh(String courseId, Integer isDel);

    /**
     * 根据微课状态查询ppt列表
     * @param videoZt
     * @param isDel
     * @return
     */
    List<TAiShuzirenPpt> findByVideoZtAndIsDel(Integer videoZt, Integer isDel);
}