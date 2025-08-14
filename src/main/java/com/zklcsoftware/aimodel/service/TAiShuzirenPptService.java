package com.zklcsoftware.aimodel.service;

import java.util.List;
import com.zklcsoftware.basic.service.BaseService;
import com.zklcsoftware.aimodel.domain.TAiShuzirenPpt;

public interface TAiShuzirenPptService extends BaseService<TAiShuzirenPpt,String> {

    /**
     * 根据微课id查询ppt列表
     * @param courseId
     * @param isDel
     * @return
     */
    List<TAiShuzirenPpt> findByCourseIdAndIsDelOrderByPptXh(String courseId, Integer isDel);

    /**
     *
     * @param videoZt
     * @param isDel
     * @return
     */
    List<TAiShuzirenPpt> findByVideoZtAndIsDel(Integer videoZt, Integer isDel);
}
