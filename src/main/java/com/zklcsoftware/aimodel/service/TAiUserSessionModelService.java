package com.zklcsoftware.aimodel.service;

import java.util.List;
import com.zklcsoftware.basic.service.BaseService;
import com.zklcsoftware.aimodel.domain.TAiUserSessionModel;

public interface TAiUserSessionModelService extends BaseService<TAiUserSessionModel,String> {


    /**
     * 根据会话id查询
     * @param aiSessionId
     * @return
     */
    List<TAiUserSessionModel> findBySessionId(String aiSessionId);

    void updateAllSessionModelId(String id, String modelId);
}
