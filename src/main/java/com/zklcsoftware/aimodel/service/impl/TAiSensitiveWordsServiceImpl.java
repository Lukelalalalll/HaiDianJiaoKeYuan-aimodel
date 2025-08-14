package com.zklcsoftware.aimodel.service.impl;

import com.zklcsoftware.aimodel.domain.TAiWarnWords;
import com.zklcsoftware.aimodel.util.ConstantUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.zklcsoftware.basic.service.impl.BaseServiceImpl;
import com.zklcsoftware.aimodel.domain.TAiSensitiveWords;
import com.zklcsoftware.aimodel.service.TAiSensitiveWordsService;

@Service
@Transactional
public class TAiSensitiveWordsServiceImpl extends BaseServiceImpl<TAiSensitiveWords, Integer> implements TAiSensitiveWordsService {

    @Override
    public void addTAiSensitiveWords(TAiSensitiveWords tAiSensitiveWords) {
        this.save(tAiSensitiveWords);
        ConstantUtil.sensitiveWordBs.addWord(tAiSensitiveWords.getWords());
    }

    @Override
    public void deleteTAiSensitiveWords(TAiSensitiveWords tAiSensitiveWords) {
        this.delete(tAiSensitiveWords);
        ConstantUtil.sensitiveWordBs.removeWord(tAiSensitiveWords.getWords());
    }

}
