package com.zklcsoftware.aimodel.service.impl;

import com.zklcsoftware.aimodel.repository.TAiShuzirenPptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.zklcsoftware.basic.service.impl.BaseServiceImpl;
import com.zklcsoftware.aimodel.domain.TAiShuzirenPpt;
import com.zklcsoftware.aimodel.service.TAiShuzirenPptService;

import java.util.List;

@Service
@Transactional
public class TAiShuzirenPptServiceImpl extends BaseServiceImpl<TAiShuzirenPpt, String> implements TAiShuzirenPptService {

    @Autowired
    private TAiShuzirenPptRepository tAiShuzirenPptRepository;

    @Override
    public List<TAiShuzirenPpt> findByCourseIdAndIsDelOrderByPptXh(String courseId, Integer isDel) {
        return tAiShuzirenPptRepository.findByCourseIdAndIsDelOrderByPptXh(courseId, isDel);
    }

    @Override
    public List<TAiShuzirenPpt> findByVideoZtAndIsDel(Integer videoZt, Integer isDel) {
        return tAiShuzirenPptRepository.findByVideoZtAndIsDel(videoZt, isDel);
    }
}
