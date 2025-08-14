package com.zklcsoftware.aimodel.repository;

import org.springframework.stereotype.Repository;
import com.zklcsoftware.basic.repository.BaseDao;
import com.zklcsoftware.aimodel.domain.TSchoolcloudSysConf;

/**
 * Service Interface:TSchoolcloudSysConf
 * @author zklcsoftware_projectarchitecture
 * @date Aug 23, 2024
 */
@Repository
public interface TSchoolcloudSysConfRepository extends BaseDao<TSchoolcloudSysConf,String>, TSchoolcloudSysConfRepositoryExt {
	
}