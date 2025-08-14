package com.zklcsoftware.aimodel.repository.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zklcsoftware.aimodel.dto.TAiModelLayoutDTO;
import com.zklcsoftware.aimodel.repository.TAiModelLayoutRepositoryExt;
import com.zklcsoftware.basic.repository.BaseRepositoryExtImpl;

public class TAiModelLayoutRepositoryImpl extends BaseRepositoryExtImpl implements TAiModelLayoutRepositoryExt {

    @Override
    public List<TAiModelLayoutDTO> getUserModelLayoutInfoByUserTypeCode(String userTypeCode,String layoutId) {
        Map params = new HashMap();
        params.put("userTypeCode",userTypeCode);
        params.put("layoutId",layoutId);
        return findListObj("getUserModelLayoutInfoByUserTypeCodeOrderbySortAsc",params,TAiModelLayoutDTO.class);
    }
}
