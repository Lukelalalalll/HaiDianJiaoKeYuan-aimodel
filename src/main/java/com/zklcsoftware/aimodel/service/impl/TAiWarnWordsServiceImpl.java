package com.zklcsoftware.aimodel.service.impl;

import com.zklcsoftware.aimodel.util.ConstantUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.zklcsoftware.basic.service.impl.BaseServiceImpl;
import com.zklcsoftware.aimodel.domain.TAiWarnWords;
import com.zklcsoftware.aimodel.service.TAiWarnWordsService;

@Service
@Transactional
public class TAiWarnWordsServiceImpl extends BaseServiceImpl<TAiWarnWords, Integer> implements TAiWarnWordsService {
    @Override
    public void addTAiWarnWords(TAiWarnWords tAiWarnWords) {
        this.save(tAiWarnWords);
        ConstantUtil.warnWords.add(tAiWarnWords.getWords());
    }

    @Override
    public void deleteTAiWarnWords(TAiWarnWords tAiWarnWords) {
        this.delete(tAiWarnWords);
        ConstantUtil.warnWords.remove(tAiWarnWords.getWords());
    }
}
