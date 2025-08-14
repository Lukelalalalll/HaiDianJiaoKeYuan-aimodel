package com.zklcsoftware.aimodel.service;

import java.util.List;

import com.zklcsoftware.aimodel.vo.TAiWarnWordsContextVO;
import com.zklcsoftware.basic.service.BaseService;
import com.zklcsoftware.aimodel.domain.TAiWarnWordsContextRef;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TAiWarnWordsContextRefService extends BaseService<TAiWarnWordsContextRef,Integer> {
    public Page<TAiWarnWordsContextVO> queryWarnWordsContextList(String startTime, String endTime, String queryStr, Pageable pageable);
	
}
