package com.zklcsoftware.aimodel.service;

import java.util.List;
import com.zklcsoftware.basic.service.BaseService;
import com.zklcsoftware.aimodel.domain.TAiWarnWords;

public interface TAiWarnWordsService extends BaseService<TAiWarnWords,Integer> {

    public void addTAiWarnWords(TAiWarnWords tAiWarnWords);

    public void deleteTAiWarnWords(TAiWarnWords tAiWarnWords);
	
}
