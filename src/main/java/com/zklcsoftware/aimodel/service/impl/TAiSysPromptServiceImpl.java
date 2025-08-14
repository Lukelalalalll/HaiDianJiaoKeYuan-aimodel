package com.zklcsoftware.aimodel.service.impl;

import com.google.gson.JsonObject;
import com.zklcsoftware.aimodel.repository.TAiSysPromptRepository;
import com.zklcsoftware.aimodel.repository.TAiUserSessionModelContextRepository;
import com.zklcsoftware.aimodel.repository.TAiUserSessionModelContextRepositoryExt;
import com.zklcsoftware.aimodel.util.ConstantUtil;
import com.zklcsoftware.aimodel.vo.TAiSysPromptVO;
import com.zklcsoftware.basic.util.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.zklcsoftware.basic.service.impl.BaseServiceImpl;
import com.zklcsoftware.aimodel.domain.TAiSysPrompt;
import com.zklcsoftware.aimodel.service.TAiSysPromptService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class TAiSysPromptServiceImpl extends BaseServiceImpl<TAiSysPrompt, String> implements TAiSysPromptService {

    @Autowired
    TAiSysPromptRepository tAiSysPromptRepository;
    @Autowired
    TAiUserSessionModelContextRepository tAiUserSessionModelContextRepository;
    @Override
    public Page<TAiSysPromptVO> queryZntList(String znt, Integer busType, Pageable pageable,String createUser,Integer publishStatus,Integer reviewStatus,String accessUser){
        Map params=new HashMap();
        params.put("znt", znt);
        params.put("busType",busType);
        params.put("createUser",createUser);
        params.put("publishStatus",publishStatus);
        params.put("reviewStatus",reviewStatus);
        params.put("accessUser",accessUser);
        Page<TAiSysPromptVO>  page=tAiSysPromptRepository.findPage("queryZntList","queryZntList_count",params,pageable,TAiSysPromptVO.class);
        for (TAiSysPromptVO tAiSysPromptVO : page.getContent()) {
            tAiSysPromptVO.setTwcs(tAiUserSessionModelContextRepository.countContextBySysPromptId(tAiSysPromptVO.getId()));
        }
        return page;
    }

    @Override
    public JsonObject andsendAnswer(String sysPromptId, String question, String sessionId) {
        //判断智能体选型、解析question内容,查询智能体用户提示词模板，拼接prompt，调用智能体接口返回结果，根据需求判断是否保留上下文


        return null;
    }
}
