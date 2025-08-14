package com.zklcsoftware.aimodel.repository;

import org.springframework.stereotype.Repository;
import com.zklcsoftware.basic.repository.BaseDao;
import com.zklcsoftware.aimodel.domain.TAiShuzirenXingxiang;

import java.util.List;

/**
 * Service Interface:TAiShuzirenXingxiang
 * @author zklcsoftware_projectarchitecture
 * @date Apr 11, 2025
 */
@Repository
public interface TAiShuzirenXingxiangRepository extends BaseDao<TAiShuzirenXingxiang,String>, TAiShuzirenXingxiangRepositoryExt {

    /**
     * 根据视频状态和是否有效查询
     * @param videoZt
     * @param isdel
     * @return
     */
    List<TAiShuzirenXingxiang> findByVideoZtAndIsDel(Integer videoZt, Integer isdel);
}