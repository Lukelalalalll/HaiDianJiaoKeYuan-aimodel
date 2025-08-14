package com.zklcsoftware.aimodel.service;

import java.util.List;
import com.zklcsoftware.basic.service.BaseService;
import com.zklcsoftware.aimodel.domain.TShuzirenService;

public interface TShuzirenServiceService extends BaseService<TShuzirenService,Integer> {


    /**
     * 根据状态查询
     * @param status
     * @return
     */
    List<TShuzirenService> findByStatus(Integer status);

    /**
     * 获取使用最少的ip
     * @return
     */
    TShuzirenService getMinServiceIp();
}
