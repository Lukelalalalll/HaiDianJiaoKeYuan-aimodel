package com.zklcsoftware.aimodel.service.impl;

import com.zklcsoftware.aimodel.repository.TAiWarnWordsContextRefRepository;
import com.zklcsoftware.aimodel.vo.TAiSysPromptVO;
import com.zklcsoftware.aimodel.vo.TAiWarnWordsContextVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.zklcsoftware.basic.service.impl.BaseServiceImpl;
import com.zklcsoftware.aimodel.domain.TAiWarnWordsContextRef;
import com.zklcsoftware.aimodel.service.TAiWarnWordsContextRefService;

import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class TAiWarnWordsContextRefServiceImpl extends BaseServiceImpl<TAiWarnWordsContextRef, Integer> implements TAiWarnWordsContextRefService {
    @Autowired
    TAiWarnWordsContextRefRepository tAiWarnWordsContextRefRepository;
    @Override
    public Page<TAiWarnWordsContextVO> queryWarnWordsContextList(String startTime, String endTime, String queryStr, Pageable pageable) {
        Map params=new HashMap();
        params.put("startTime", startTime);
        params.put("endTime",endTime);
        params.put("queryStr",queryStr);
        params.put("queryStr2",queryStr);
        Page<TAiWarnWordsContextVO>  page=tAiWarnWordsContextRefRepository
                .findPage("queryWarnWordsContextList","queryWarnWordsContextList_count",params,pageable,TAiWarnWordsContextVO.class);
        return page;
    }
}
