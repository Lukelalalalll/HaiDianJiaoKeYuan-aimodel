package com.zklcsoftware.aimodel.service;

import java.util.List;

import com.zklcsoftware.aimodel.domain.TAiModel;
import com.zklcsoftware.aimodel.domain.TAiSysPrompt;
import com.zklcsoftware.aimodel.dto.MessageDTO;
import com.zklcsoftware.basic.service.BaseService;
import com.zklcsoftware.aimodel.domain.TAiSysTools;

public interface TAiSysToolsService extends BaseService<TAiSysTools,String> {
    /**
     * @Description 大模型function call响应
     * @Author zhushaog
     * @param: tAiModel
     * @param: tAiSysPrompt
     * @param: questiond
     * @param: userId
     * @UpdateTime 2025/6/11 10:17
     * @return: java.util.List<com.zklcsoftware.aimodel.dto.MessageDTO>
     * @throws
     */
    List<MessageDTO> bigModelFunctionCall(TAiModel tAiModel,TAiSysPrompt tAiSysPrompt, String questiond,String userId,String studentId);
    /**
     * @Description  查询智能体工具集
     * @Author zhushaog
     * @param: sysPromptId
     * @UpdateTime 2025/6/11 10:17
     * @return: java.util.List<com.zklcsoftware.aimodel.domain.TAiSysTools>
     * @throws
     */
    public List<TAiSysTools> queryAiSysTools(String sysPromptId);
}
