package com.zklcsoftware.aimodel.service;

import java.util.List;
import com.zklcsoftware.basic.service.BaseService;
import com.zklcsoftware.aimodel.domain.TAiShuziren;
import com.zklcsoftware.common.dto.OperaResult;

public interface TAiShuzirenService extends BaseService<TAiShuziren,String> {

    /**
     * 根据状态和是否有效查询
     * @param zt
     * @param isValid
     * @return
     */
    List<TAiShuziren> findByZtAndIsValid(Integer zt, Integer isValid);

    /**
     * 根据创建人id和是否有效查询
     * @param userGuid
     * @param isValid
     * @return
     */
    List<TAiShuziren> findByCreateUserIdAndIsValidOrderByCreateDateDesc(String userGuid, Integer isValid);

    /**
     * 数字人视频上传
     * @param text
     * @param fileUrl
     * @param xingxiangId
     * @param userGuid
     * @param uName
     * @return
     */
    OperaResult digitalPersonVideoUpload(String text, String fileUrl, String xingxiangId, String userGuid, String uName);
}
