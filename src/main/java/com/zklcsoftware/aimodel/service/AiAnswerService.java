package com.zklcsoftware.aimodel.service;

import com.zklcsoftware.aimodel.domain.TAiModel;
import com.zklcsoftware.aimodel.dto.MessageDTO;
import com.zklcsoftware.aimodel.websocket.dto.AiOutMsgDTO;

import java.util.List;

/**
 * @author zhushaog
 * @version 1.0
 * @ClassName AiAnswerService.java
 * @company zklcsoftware
 * @Description AI文档回答
 * @createTime 2024/08/23 16:56
 */
public interface AiAnswerService extends AiAnswerCallService{
    /***
     * @Description 模型调用接口(流式)
     * @Author zhushaog
     * @param: modelId 模型id
     * @param: messageDTOS  上下文列表
     * @param: sessionId  会话标识
     * @param: question   问题
     * @param: knowledgeDocumentUrl   知识库文档路径
     * @UpdateTime 2024/8/23 17:03
     * @throws
     */
    public AiOutMsgDTO sendAnswer(TAiModel tAiModel, String question,String questionPromptFormat, String sessionId, List<MessageDTO> messageDTOS,String userId,Integer callType, String knowledgeDocumentUrl);

    /***
     * @Description 视觉理解接口(流式  提问+图片集合)
     * @Author zhushaog
     * @param: modelId 模型id
     * @param: messageDTOS  上下文列表
     * @param: sessionId  会话标识
     * @param: question   问题
     * @UpdateTime 2024/8/23 17:03
     * @throws
     */
    public AiOutMsgDTO sendAnswerWithImages(TAiModel tAiModel, String question,String questionPromptFormat, String sessionId, List<MessageDTO> messageDTOS,String userId,List<String> images);

    /**
     * 文本转图片
     * @param tAiModel
     * @param content
     * @param sessionModelId
     * @param userId
     * @return
     */
    public AiOutMsgDTO textToImage(TAiModel tAiModel, String content, String sessionModelId, String userId);


    /***
     * @Description 模型调用接口(直调)
     * @Author zhushaog
     * @param: modelId 模型id
     * @param: messageDTOS  上下文列表
     * @param: sessionId  会话标识
     * @param: question   问题
     * @UpdateTime 2024/8/23 17:03
     * @throws
     */
    //public String sendAnswer(TAiModel tAiModel, String question, List<MessageDTO> messageDTOS);

    /**
     * 文本识别
     * @param tAiModel
     * @param content
     * @param sessionModelId
     * @param userId
     * @return
     */
    public AiOutMsgDTO textRecognition(TAiModel tAiModel, String content, String sessionModelId, String userId);

    /**
     * 评语生成
     * @param tAiModel
     * @param question
     * @param sysPromptId
     * @param sessionModelId
     * @param userId
     */
    AiOutMsgDTO commentGeneration(TAiModel tAiModel, String question, String sysPromptId, String sessionModelId, String userId);

    /**
     * 游戏页面生成
     * @param tAiModel
     * @param question
     * @param sessionModelId
     * @param userId
     */
    AiOutMsgDTO genGameHtml(TAiModel tAiModel, String question, String sessionModelId, String userId);



    /**
     * 数据查询转换为chart
     * @param tAiModel
     * @param question
     * @param sysPromptId
     * @param sessionModelId
     * @param userId
     * @return
     */
    AiOutMsgDTO dateQueryAndToEchart(TAiModel tAiModel, String question, String sysPromptId, String sessionModelId, String userId);

    AiOutMsgDTO textToAudio(TAiModel tAiModel, String question, String sysPromptId, String sessionModelId, String userId);
    /***
     * @Description 教师百文
     * @Author zhushaog
     * @param: tAiModel
     * @param: s
     * @param: sessionModelId
     * @param: userId
     * @UpdateTime 2025/4/19 8:30
     * @return: com.zklcsoftware.aimodel.websocket.dto.AiOutMsgDTO
     * @throws
     */
    AiOutMsgDTO teaBaiwen(TAiModel tAiModel, String question, String sessionModelId, String userId);

    /***
     * @Description 学生百文
     * @Author zhushaog
     * @param: tAiModel
     * @param: s
     * @param: sessionModelId
     * @param: userId
     * @UpdateTime 2025/4/19 8:30
     * @return: com.zklcsoftware.aimodel.websocket.dto.AiOutMsgDTO
     * @throws
     */
    AiOutMsgDTO stuBaiwen(TAiModel tAiModel, String question, String sessionModelId, String userId);
}
