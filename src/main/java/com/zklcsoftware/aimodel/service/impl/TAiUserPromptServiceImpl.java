package com.zklcsoftware.aimodel.service.impl;

import com.zklcsoftware.aimodel.repository.TAiUserPromptRepository;
import com.zklcsoftware.aimodel.util.ConstantUtil;
import com.zklcsoftware.aimodel.vo.TAiSysPromptVO;
import com.zklcsoftware.aimodel.vo.TAiUserPromptVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.zklcsoftware.basic.service.impl.BaseServiceImpl;
import com.zklcsoftware.aimodel.domain.TAiUserPrompt;
import com.zklcsoftware.aimodel.service.TAiUserPromptService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class TAiUserPromptServiceImpl extends BaseServiceImpl<TAiUserPrompt, String> implements TAiUserPromptService {

    @Autowired
    TAiUserPromptRepository tAiUserPromptRepository;
    @Override
    public Page<TAiUserPromptVO> queryTscList(String tsc, Integer busType, Pageable pageable) {
        Map params=new HashMap();
        params.put("tsc", tsc);
        params.put("busType",busType);
        Page<TAiUserPromptVO>  page=tAiUserPromptRepository.findPage("queryTscList","queryTscList_count",params,pageable,TAiUserPromptVO.class);
        return page;
    }
}
