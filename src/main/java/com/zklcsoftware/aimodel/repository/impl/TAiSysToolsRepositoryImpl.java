package com.zklcsoftware.aimodel.repository.impl;

import com.zklcsoftware.aimodel.domain.TAiSysTools;
import com.zklcsoftware.basic.repository.BaseRepositoryExtImpl;
import com.zklcsoftware.aimodel.repository.TAiSysToolsRepositoryExt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TAiSysToolsRepositoryImpl extends BaseRepositoryExtImpl implements TAiSysToolsRepositoryExt {

    @Override
    public List<TAiSysTools> queryAiSysTools(String sysPromptId) {
        Map params=new HashMap<>();
        params.put("sysPromptId",sysPromptId);
        return findListObj("queryAiSysTools",params,TAiSysTools.class);
    }
}