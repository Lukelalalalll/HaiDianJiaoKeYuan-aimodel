package com.zklcsoftware.aimodel.service.impl;

import com.zklcsoftware.aimodel.repository.TAiUserSessionModelContextFileindexRepository;
import com.zklcsoftware.aimodel.vo.TAiUserSessionModelContextFileindexVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.zklcsoftware.basic.service.impl.BaseServiceImpl;
import com.zklcsoftware.aimodel.domain.TAiUserSessionModelContextFileindex;
import com.zklcsoftware.aimodel.service.TAiUserSessionModelContextFileindexService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class TAiUserSessionModelContextFileindexServiceImpl extends BaseServiceImpl<TAiUserSessionModelContextFileindex, String> implements TAiUserSessionModelContextFileindexService {

    @Autowired
    private TAiUserSessionModelContextFileindexRepository tAiUserSessionModelContextFileindexRepository;

    @Override
    public List<TAiUserSessionModelContextFileindexVO> answeredCallFileindexList(String sessionContextId) {
        Map<String,Object> params = new HashMap<>();
        params.put("sessionContextId",sessionContextId);
        return tAiUserSessionModelContextFileindexRepository.queryFileIndexBySessionModelId(params);
    }
}
