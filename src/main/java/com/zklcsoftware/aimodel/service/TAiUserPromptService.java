package com.zklcsoftware.aimodel.service;

import java.util.List;

import com.zklcsoftware.aimodel.vo.TAiSysPromptVO;
import com.zklcsoftware.aimodel.vo.TAiUserPromptVO;
import com.zklcsoftware.basic.service.BaseService;
import com.zklcsoftware.aimodel.domain.TAiUserPrompt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TAiUserPromptService extends BaseService<TAiUserPrompt,String> {

    public Page<TAiUserPromptVO> queryTscList(String tsc, Integer busType, Pageable pageable);

}
