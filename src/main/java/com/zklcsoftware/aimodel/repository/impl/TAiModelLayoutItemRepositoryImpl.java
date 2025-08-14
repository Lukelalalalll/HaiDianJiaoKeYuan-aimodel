package com.zklcsoftware.aimodel.repository.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zklcsoftware.aimodel.dto.TAiModelLayoutItemDTO;
import com.zklcsoftware.aimodel.repository.TAiModelLayoutItemRepositoryExt;
import com.zklcsoftware.basic.repository.BaseRepositoryExtImpl;

public class TAiModelLayoutItemRepositoryImpl extends BaseRepositoryExtImpl implements TAiModelLayoutItemRepositoryExt {
	
    @Override
    public List<TAiModelLayoutItemDTO> getItemsByLayoutIdOrderbySort(String layoutId) {
        Map params = new HashMap();
        params.put("layoutId",layoutId);
        return findListObj("getItemsByLayoutIdOrderbySort",params,TAiModelLayoutItemDTO.class);
    }
}
