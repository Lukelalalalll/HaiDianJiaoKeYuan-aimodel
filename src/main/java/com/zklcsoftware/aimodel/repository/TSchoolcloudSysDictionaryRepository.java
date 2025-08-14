package com.zklcsoftware.aimodel.repository;

import org.springframework.stereotype.Repository;
import com.zklcsoftware.basic.repository.BaseDao;
import com.zklcsoftware.aimodel.domain.TSchoolcloudSysDictionary;

import java.util.List;

/**
 * Service Interface:TSchoolcloudSysDictionary
 * @author zklcsoftware_projectarchitecture
 * @date Oct 18, 2024
 */
@Repository
public interface TSchoolcloudSysDictionaryRepository extends BaseDao<TSchoolcloudSysDictionary,Integer>, TSchoolcloudSysDictionaryRepositoryExt {
	List<TSchoolcloudSysDictionary>  findByPidAndIsDelOrderByOrderNoAsc(Integer pid, Integer isDel);
}