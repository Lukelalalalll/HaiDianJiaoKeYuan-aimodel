package com.zklcsoftware.aimodel.service;

import java.util.List;
import com.zklcsoftware.basic.service.BaseService;
import com.zklcsoftware.aimodel.domain.TAiMicroCourse;
import com.zklcsoftware.common.dto.OperaResult;

public interface TAiMicroCourseService extends BaseService<TAiMicroCourse,String> {


    /**
     * 根据用户ID查询微课列表
     * @param userGuid
     * @param isDel
     * @return
     */
    List<TAiMicroCourse> findByCreateUserIdAndIsDelOrderByCreateDateDesc(String userGuid, Integer isDel);

    /**
     * 保存微课
     * @param course
     * @param userGuid
     * @param uName
     * @return
     */
    OperaResult microCourseSave(TAiMicroCourse course, String userGuid, String uName);

    /**
     * 根据视频状态查询微课
     * @param videoZt
     * @param isDel
     * @return
     */
    List<TAiMicroCourse> findByVideoZtAndIsDel(Integer videoZt, Integer isDel);

    /**
     * 合成微课视频
     * @param courseId
     * @param userGuid
     * @param uName
     * @return
     */
    OperaResult microCourseVideoConcatenation(String courseId, String userGuid, String uName);
}
