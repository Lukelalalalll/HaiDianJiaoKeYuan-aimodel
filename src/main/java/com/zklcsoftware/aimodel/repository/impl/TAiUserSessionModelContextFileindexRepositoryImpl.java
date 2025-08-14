package com.zklcsoftware.aimodel.repository.impl;

import com.zklcsoftware.aimodel.vo.TAiUserSessionModelContextFileindexVO;
import com.zklcsoftware.basic.repository.BaseRepositoryExtImpl;
import com.zklcsoftware.aimodel.repository.TAiUserSessionModelContextFileindexRepositoryExt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TAiUserSessionModelContextFileindexRepositoryImpl extends BaseRepositoryExtImpl implements TAiUserSessionModelContextFileindexRepositoryExt {

    @Override
    public List<TAiUserSessionModelContextFileindexVO> queryFileIndexBySessionModelId(Map<String,Object> params) {
        return this.findListObj("queryFileIndexBySessionModelId",params,TAiUserSessionModelContextFileindexVO.class);
    }
}