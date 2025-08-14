package com.zklcsoftware.aimodel.service.impl;

import com.zklcsoftware.aimodel.repository.TAiUserSessionModelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.zklcsoftware.basic.service.impl.BaseServiceImpl;
import com.zklcsoftware.aimodel.domain.TAiUserSessionModel;
import com.zklcsoftware.aimodel.service.TAiUserSessionModelService;

import java.util.Collections;
import java.util.List;

@Service
@Transactional
public class TAiUserSessionModelServiceImpl extends BaseServiceImpl<TAiUserSessionModel, String> implements TAiUserSessionModelService {

    @Autowired
    private TAiUserSessionModelRepository tAiUserSessionModelRepository;
    @Autowired
    JdbcTemplate jdbcTemplate;
    /**
     * 根据会话id查询
     *
     * @param aiSessionId
     * @return
     */
    @Override
    public List<TAiUserSessionModel> findBySessionId(String aiSessionId) {
        return tAiUserSessionModelRepository.findBySessionId(aiSessionId);
    }

    @Override
    public void updateAllSessionModelId(String systemPromptId, String modelId) {
        jdbcTemplate.update("UPDATE t_ai_user_session_model t INNER JOIN t_ai_user_session t1 ON t.session_id = t1.id SET t.model_id = ? WHERE t1.sys_prompt_id = ?;",modelId,systemPromptId);
    }
}
