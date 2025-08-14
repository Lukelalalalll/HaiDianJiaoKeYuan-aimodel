package com.zklcsoftware.aimodel.service;

import com.zklcsoftware.aimodel.domain.TAiModel;
import com.zklcsoftware.aimodel.dto.MessageDTO;
import com.zklcsoftware.aimodel.vo.TAiUserSessionModelContextFileindexVO;

import java.util.List;
import java.util.Map;

/**
 * @author zhushaog
 * @version 1.0
 * @ClassName AiAnswerService.java
 * @company zklcsoftware
 * @Description AI文档回答
 * @createTime 2024/08/23 16:56
 */
public interface AiAnswerCallService {
    /**
     * @Description 大模型回答完毕后 记录上下文信息到数据库
     * @Author zhushaog
     * @param: question
     * @param: answer
     * @param: sessionModelId
     * @param: knowledgeDocumentUrl
     * @UpdateTime 2024/10/19 11:53
     * @throws
     */
    String answeredCall(String question, String questionPromptFormat, String answer, String sessionModelId);

    /**
     * @Description 大模型回答完毕后 记录上下文信息到数据库
     * @Author zhushaog
     * @param: contextId 上下文ID
     * @param: answer
     * @UpdateTime 2024/10/19 11:53
     * @throws
     */
    void answeredCallUpdate(String contextId,String answer, String sessionModelId, String knowledgeDocumentUrl);
}
