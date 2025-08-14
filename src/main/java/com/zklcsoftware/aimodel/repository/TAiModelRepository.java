package com.zklcsoftware.aimodel.repository;

import com.zklcsoftware.basic.util.In;
import org.springframework.stereotype.Repository;
import com.zklcsoftware.basic.repository.BaseDao;
import com.zklcsoftware.aimodel.domain.TAiModel;

import java.util.List;

/**
 * Service Interface:TAiModel
 * @author zklcsoftware_projectarchitecture
 * @date Aug 23, 2024
 */
@Repository
public interface TAiModelRepository extends BaseDao<TAiModel,String>, TAiModelRepositoryExt {
	/**
	 * @Description 查询有效大模型
	 * @Author zhushaog
	 * @UpdateTime 2024/8/23 17:14
	 * @return: java.util.List<com.zklcsoftware.aimodel.domain.TAiModel>
	 * @throws
	 */
    public List<TAiModel> findByStatus(Integer status);
}