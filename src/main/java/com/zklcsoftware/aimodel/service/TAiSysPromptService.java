package com.zklcsoftware.aimodel.service;

import java.util.List;

import com.google.gson.JsonObject;
import com.zklcsoftware.aimodel.vo.TAiSysPromptVO;
import com.zklcsoftware.basic.service.BaseService;
import com.zklcsoftware.aimodel.domain.TAiSysPrompt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TAiSysPromptService extends BaseService<TAiSysPrompt,String> {

    /**
     * @Description 查询智能体列表
     * @Author zhushaog
     * @param: znt
     * @param: busType  业务类型
     * @param: pageable 分页对象
     * @param: userId   创建用户ID
     * @param: reviewStatus  审核状态
     * @UpdateTime 2024/12/16 10:08
     * @return: org.springframework.data.domain.Page<com.zklcsoftware.aimodel.vo.TAiSysPromptVO>
     * @throws
     */
    public Page<TAiSysPromptVO> queryZntList(String znt, Integer busType, Pageable pageable,String createUser,Integer publishStatus,Integer reviewStatus,String accessUser);

    /**
     * @Description 直调方式使用智能体获取响应结果
     * @Author zhushaog
     * @param: sysPromptId
     * @param: question
     * @param: sessionId
     * @UpdateTime 2025/1/8 11:49
     * @return: com.google.gson.JsonObject
     * @throws
     */
    public JsonObject andsendAnswer(String sysPromptId, String question, String sessionId);
}
