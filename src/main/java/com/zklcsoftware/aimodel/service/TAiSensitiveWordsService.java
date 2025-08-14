package com.zklcsoftware.aimodel.service;

import java.util.List;
import com.zklcsoftware.basic.service.BaseService;
import com.zklcsoftware.aimodel.domain.TAiSensitiveWords;

public interface TAiSensitiveWordsService extends BaseService<TAiSensitiveWords,Integer> {

    public void addTAiSensitiveWords(TAiSensitiveWords tAiSensitiveWords);

    public void deleteTAiSensitiveWords(TAiSensitiveWords tAiSensitiveWords);
}
