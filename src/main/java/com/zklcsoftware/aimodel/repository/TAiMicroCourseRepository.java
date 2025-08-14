package com.zklcsoftware.aimodel.repository;

import org.springframework.stereotype.Repository;
import com.zklcsoftware.basic.repository.BaseDao;
import com.zklcsoftware.aimodel.domain.TAiMicroCourse;

import java.util.List;

/**
 * Service Interface:TAiMicroCourse
 * @author zklcsoftware_projectarchitecture
 * @date Apr 11, 2025
 */
@Repository
public interface TAiMicroCourseRepository extends BaseDao<TAiMicroCourse,String>, TAiMicroCourseRepositoryExt {

    /**
     * 根据用户ID查询微课列表
     * @param userGuid
     * @param isDel
     * @return
     */
    List<TAiMicroCourse> findByCreateUserIdAndIsDelOrderByCreateDateDesc(String userGuid, Integer isDel);

    /**
     * 根据视频状态查询微课
     * @param videoZt
     * @param isDel
     * @return
     */
    List<TAiMicroCourse> findByVideoZtAndIsDel(Integer videoZt, Integer isDel);
}