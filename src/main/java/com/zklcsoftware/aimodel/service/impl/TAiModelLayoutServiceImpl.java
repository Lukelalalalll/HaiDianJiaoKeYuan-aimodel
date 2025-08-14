package com.zklcsoftware.aimodel.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zklcsoftware.aimodel.domain.TAiModelLayout;
import com.zklcsoftware.aimodel.dto.TAiModelLayoutDTO;
import com.zklcsoftware.aimodel.dto.TAiModelLayoutItemDTO;
import com.zklcsoftware.aimodel.repository.TAiModelLayoutItemRepository;
import com.zklcsoftware.aimodel.repository.TAiModelLayoutRepository;
import com.zklcsoftware.aimodel.service.TAiModelLayoutService;
import com.zklcsoftware.basic.service.impl.BaseServiceImpl;

@Service
@Transactional
public class TAiModelLayoutServiceImpl extends BaseServiceImpl<TAiModelLayout, String> implements TAiModelLayoutService {
	
    @Autowired TAiModelLayoutRepository aiModelLayoutRepository;
    @Autowired TAiModelLayoutItemRepository aiModelLayoutItemRepository;
    
	@Override
	public List<TAiModelLayoutDTO> getUserModelLayoutInfoByUserTypeCode(String userTypeCode,String layoutId) {
		//查询一级布局
		List<TAiModelLayoutDTO> list = aiModelLayoutRepository.getUserModelLayoutInfoByUserTypeCode(userTypeCode,layoutId);
		//补充布局内的智能体信息
		for(TAiModelLayoutDTO l : list){
			List<TAiModelLayoutItemDTO> itemList = aiModelLayoutItemRepository.getItemsByLayoutIdOrderbySort(l.getLayoutId());
			l.setItemList(itemList);
		}
		return list;
	}
	
}
