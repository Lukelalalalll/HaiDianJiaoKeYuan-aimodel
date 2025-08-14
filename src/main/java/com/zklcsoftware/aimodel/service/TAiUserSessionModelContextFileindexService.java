package com.zklcsoftware.aimodel.service;

import java.util.List;

import com.zklcsoftware.aimodel.vo.TAiUserSessionModelContextFileindexVO;
import com.zklcsoftware.basic.service.BaseService;
import com.zklcsoftware.aimodel.domain.TAiUserSessionModelContextFileindex;

public interface TAiUserSessionModelContextFileindexService extends BaseService<TAiUserSessionModelContextFileindex,String> {


    /**
     * 根据会话上下文id查询已回答过的文件索引
     * @param sessionContextId
     * @return
     */
    List<TAiUserSessionModelContextFileindexVO> answeredCallFileindexList(String sessionContextId);
}
