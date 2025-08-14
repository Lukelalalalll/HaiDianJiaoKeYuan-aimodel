package com.zklcsoftware.aimodel.repository;

import org.springframework.stereotype.Repository;
import com.zklcsoftware.basic.repository.BaseDao;
import com.zklcsoftware.aimodel.domain.TAiShuziren;

import java.util.List;

/**
 * Service Interface:TAiShuziren
 * @author zklcsoftware_projectarchitecture
 * @date Apr 1, 2025
 */
@Repository
public interface TAiShuzirenRepository extends BaseDao<TAiShuziren,String>, TAiShuzirenRepositoryExt {

    /**
     * 根据状态查询
     * @param zt
     * @param isValid
     * @return
     */
    List<TAiShuziren> findByZtAndIsValid(Integer zt, Integer isValid);

    /**
     * 根据创建人查询
     * @param userGuid
     * @param isValid
     * @return
     */
    List<TAiShuziren> findByCreateUserIdAndIsValidOrderByCreateDateDesc(String userGuid, Integer isValid);
}