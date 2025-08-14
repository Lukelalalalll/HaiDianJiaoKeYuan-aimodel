package com.zklcsoftware.aimodel.service.impl;

import com.zklcsoftware.aimodel.repository.TSchoolcloudSysDictionaryRepository;
import com.zklcsoftware.aimodel.util.ConstantUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.zklcsoftware.basic.service.impl.BaseServiceImpl;
import com.zklcsoftware.aimodel.domain.TSchoolcloudSysDictionary;
import com.zklcsoftware.aimodel.service.TSchoolcloudSysDictionaryService;

import javax.xml.ws.Action;
import java.util.List;

@Service
@Transactional
public class TSchoolcloudSysDictionaryServiceImpl extends BaseServiceImpl<TSchoolcloudSysDictionary, Integer> implements TSchoolcloudSysDictionaryService {

    @Autowired
    TSchoolcloudSysDictionaryRepository tSchoolcloudSysDictionaryRepository;

    @Override
    public List<TSchoolcloudSysDictionary> queryDictList(Integer dictType) {
        return tSchoolcloudSysDictionaryRepository.findByPidAndIsDelOrderByOrderNoAsc(dictType, ConstantUtil.IS_DEL_0);
    }
}
