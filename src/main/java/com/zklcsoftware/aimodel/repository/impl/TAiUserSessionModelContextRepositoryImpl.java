package com.zklcsoftware.aimodel.repository.impl;

import com.zklcsoftware.aimodel.domain.TAiUserSessionModelContext;
import com.zklcsoftware.aimodel.vo.TAiUserSessionModelContextVO;
import com.zklcsoftware.basic.repository.BaseRepositoryExtImpl;
import com.zklcsoftware.aimodel.repository.TAiUserSessionModelContextRepositoryExt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TAiUserSessionModelContextRepositoryImpl extends BaseRepositoryExtImpl implements TAiUserSessionModelContextRepositoryExt {

    @Override
    public List<TAiUserSessionModelContextVO> querySessionModelContextBySessionModelId(String sessionModelId) {
        Map params = new HashMap();
        params.put("sessionModelId",sessionModelId);
        return findListObj("querySessionModelContextBySessionModelId",params,TAiUserSessionModelContextVO.class);
    }

    @Override
    public Long countContextBySysPromptId(String sysPromptId) {
        Map params = new HashMap();
        params.put("sysPromptId",sysPromptId);
        Map<String,Long> resultMap=findOneMap("countContextBySysPromptId",params);
        return resultMap.get("twcs");
    }

    @Override
    public List<TAiUserSessionModelContext> queryBySessionModelId(String sessionModelId, Integer topN) {
        Map params = new HashMap();
        params.put("topN",topN);
        params.put("sessionModelId",sessionModelId);
        return findListObj("queryBySessionModelId",params,TAiUserSessionModelContext.class);
    }
}