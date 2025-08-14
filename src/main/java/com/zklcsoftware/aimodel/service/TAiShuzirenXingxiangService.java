package com.zklcsoftware.aimodel.service;

import java.util.List;
import com.zklcsoftware.basic.service.BaseService;
import com.zklcsoftware.aimodel.domain.TAiShuzirenXingxiang;
import com.zklcsoftware.common.dto.OperaResult;

public interface TAiShuzirenXingxiangService extends BaseService<TAiShuzirenXingxiang,String> {

    /**
     * 根据视频状态和是否有效查询
     * @param videoZt
     * @param isdel
     * @return
     */
    List<TAiShuzirenXingxiang> findByVideoZtAndIsDel(Integer videoZt, Integer isdel);

    /**
     * 查询用户数字人形象
     * @param userGuid
     * @return
     */
    List<TAiShuzirenXingxiang> queryUserShuzirenXingxiangList(String userGuid);

    /**
     * 数字人形象保存
     * @param fileUrl
     * @param sfkt
     * @param userGuid
     * @param uName
     * @return
     */
    OperaResult shuzirenXingxiangSave(String fileUrl, Integer sfkt, String userGuid, String uName);
}
