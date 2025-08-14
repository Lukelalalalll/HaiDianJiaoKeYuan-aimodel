package com.zklcsoftware.aimodel.repository.impl;

import com.zklcsoftware.aimodel.domain.TAiUserSession;
import com.zklcsoftware.aimodel.dto.SessionlQueryDTO;
import com.zklcsoftware.basic.repository.BaseRepositoryExtImpl;
import com.zklcsoftware.aimodel.repository.TAiUserSessionRepositoryExt;
import com.zklcsoftware.basic.repository.ObjectRowMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TAiUserSessionRepositoryImpl extends BaseRepositoryExtImpl implements TAiUserSessionRepositoryExt {

    @Override
    public List<TAiUserSession> queryUserAiSession(String userId, Integer isDel, SessionlQueryDTO sessionlQueryVO) {
        Map params=new HashMap();
        params.put("userId",userId);
        params.put("isDel",isDel);
        if(sessionlQueryVO.getSysPromptIds()!=null && sessionlQueryVO.getSysPromptIds().size()>0){
            params.put("sysPromptIds",sessionlQueryVO.getSysPromptIds());
        }else{
            params.put("sysPromptId",sessionlQueryVO.getSysPromptId());
        }
        params.put("lastNum",sessionlQueryVO.getLastNum());
        return findListWithIn("queryUserAiSession",params, new ObjectRowMapper(TAiUserSession.class));
    }
}