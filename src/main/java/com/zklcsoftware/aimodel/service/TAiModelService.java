package com.zklcsoftware.aimodel.service;

import com.zklcsoftware.aimodel.domain.TAiModel;
import com.zklcsoftware.aimodel.websocket.dto.AiOutMsgDTO;
import com.zklcsoftware.basic.service.BaseService;

import java.util.List;

public interface TAiModelService extends BaseService<TAiModel,String> {
    List<TAiModel> queryAiModelList();
    AiOutMsgDTO answer(String sessionModelId, String question,Integer callType);
    AiOutMsgDTO textToImage(String aiSessionId, String content);
    AiOutMsgDTO textRecognition(String aiSessionId, String content);

    /**
     * @Description 直调智能体接口(保存提问信息)
     * @Author zhushaog
     * @param: sysPromptId
     * @param: question
     * @param: userId
     * @UpdateTime 2025/1/20 9:28
     * @return: com.zklcsoftware.aimodel.websocket.dto.AiOutMsgDTO
     * @throws
     */
    AiOutMsgDTO answerApi(String sysPromptId, String question, String userId);

    /**
     * @Description 直调智能体接口(保存提问信息)
     * @Author zhushaog
     * @param: sysPromptId
     * @param: question
     * @param: userId
     * @UpdateTime 2025/1/20 9:28
     * @return: com.zklcsoftware.aimodel.websocket.dto.AiOutMsgDTO
     * @throws
     */
    AiOutMsgDTO answerApi(String sysPromptId,String sessionModelId, String question, String userId);

    /**
     * @Description 直调智能体接口
     * @Author zhushaog
     * @param: sysPromptId
     * @param: question
     * @param: userId
     * @UpdateTime 2025/1/20 9:28
     * @return: com.zklcsoftware.aimodel.websocket.dto.AiOutMsgDTO
     * @throws
     */
    AiOutMsgDTO textToAudio(String content, String voiceType);
}
