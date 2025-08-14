package com.zklcsoftware.aimodel.service.impl;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.ResultCallback;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.utils.Constants;
import com.zklcsoftware.aimodel.domain.TAiModel;
import com.zklcsoftware.aimodel.dto.MessageDTO;
import com.zklcsoftware.aimodel.service.AiAnswerService;
import com.zklcsoftware.aimodel.util.ConstantUtil;
import com.zklcsoftware.aimodel.websocket.dto.AiOutMsgDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import static java.lang.String.format;

/**
 * @author zhushaog
 * @version 1.0
 * @ClassName TongyiAiAnswerImpl.java
 * @company zklcsoftware
 * @Description 通义千问文档回答实现
 * @createTime 2024/08/23 17:04
 */
@Service("TongyiAiAnswerImpl")
@Slf4j
public class TongyiAiAnswerImpl extends AiAnswerCallServiceImpl implements AiAnswerService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public static Map<String,List<Message>> userChatMessages=new HashMap<>();//上下文问题记录

    @Override
    public AiOutMsgDTO sendAnswer(TAiModel tAiModel, String question,String questionPromptFormat, String sessionId, List<MessageDTO> userChatMessages,String userId,Integer callType, String knowledgeDocumentUrl) {

        Constants.apiKey=tAiModel.getAppsecret();
        AiOutMsgDTO aiOutMsgDTO = null;
        //用户提问及回答结果保存 用于上下文回答
        String contextId="";
        try {

            List<Message> messages = new ArrayList<>();
            for (MessageDTO userChatMessage : userChatMessages) {
                messages.add(Message.builder().role(userChatMessage.getRole()).content(userChatMessage.getContent()).build());
            }
            Message chatMessage = Message.builder().role(Role.USER.getValue()).content(StringUtils.isNotBlank(questionPromptFormat)?questionPromptFormat:question).build();
            messages.add(chatMessage);

            Generation gen = new Generation();
            GenerationParam param =
                    GenerationParam.builder().model("qwen-max")
                            .messages(messages)
                            .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                            .topP(0.8)
                            .incrementalOutput(true) // get streaming output incrementally
                            .build();
            Semaphore semaphore = new Semaphore(0);
            StringBuilder fullContent = new StringBuilder();

            //用户提问及回答结果保存 用于上下文回答
            contextId=this.answeredCall(question,questionPromptFormat, null, sessionId);
            ConstantUtil.contextIdMap.put(sessionId,contextId);//记录当前会话的最后一次上下文ID
            final Boolean[] outStr = {true};//持续输出
            log.info("通义回答开始");
            gen.streamCall(param, new ResultCallback<GenerationResult>() {
                @Override
                public void onEvent(GenerationResult message) {
                    //log.info("通义千问文档回答:"+message);
                    fullContent.append(message.getOutput().getChoices().get(0).getMessage().getContent());
                    if(outStr[0]){
                        messagingTemplate.convertAndSendToUser(userId, format("/chatmessage"+tAiModel.getId()+"/%s", sessionId), gson.toJson(AiOutMsgDTO.wsSuccessText(fullContent.toString())));
                    }else{
                        throw new RuntimeException();
                    }
                }
                @Override
                public void onError(Exception ex){
                    log.error("通义千问文档回答API异常",ex);
                    //给用户返回结束回答结束动作
                    messagingTemplate.convertAndSendToUser(userId, format("/chatmessage" + tAiModel.getId() + "/%s", sessionId)
                            , gson.toJson(AiOutMsgDTO.wsSuccessText("<服务繁忙，请稍后再试>")));

                    semaphore.release();
                }
                @Override
                public void onComplete(){
                    semaphore.release();
                }

            });
            try {
                semaphore.acquire();
            }catch (InterruptedException e){
                outStr[0] =false;
                semaphore.release();
            }

            //用户提问及回答结果保存 用于上下文回答
            this.answeredCallUpdate(contextId, fullContent.toString(),sessionId, knowledgeDocumentUrl);

            if (callType == ConstantUtil.CALL_TYPE_1) {
                //给用户返回结束回答结束动作
                messagingTemplate.convertAndSendToUser(userId, format("/chatmessage"+tAiModel.getId()+"/%s", sessionId), gson.toJson(AiOutMsgDTO.wsSuccessDone(contextId,fullContent.toString())));
            } else if (callType == ConstantUtil.CALL_TYPE_2) {
                aiOutMsgDTO = AiOutMsgDTO.wsSuccessDone(contextId, fullContent.toString());
            }

        } catch (Exception e) {
            log.error("通义千问文档回答API异常",e);
        }
        log.info("通义回答结束");
        return aiOutMsgDTO;
    }

    @Override
    public AiOutMsgDTO sendAnswerWithImages(TAiModel tAiModel, String question, String questionPromptFormat, String sessionId, List<MessageDTO> messageDTOS, String userId, List<String> images) {
        return null;
    }

    /**
     * 文本转图片
     *
     * @param content
     * @param sessionId
     * @param userId
     * @return
     */
    @Override
    public AiOutMsgDTO textToImage(TAiModel tAiModel, String content, String sessionId, String userId) {
        return null;
    }

    /*@Override
    public String sendAnswer(TAiModel tAiModel, String question, List<MessageDTO> messageDTOS) {
        return null;
    }*/

    /**
     * 文本识别
     *
     * @param tAiModel
     * @param content
     * @param sessionId
     * @param userId
     * @return
     */
    @Override
    public AiOutMsgDTO textRecognition(TAiModel tAiModel, String content, String sessionId, String userId) {
        return null;
    }

    @Override
    public AiOutMsgDTO commentGeneration(TAiModel tAiModel, String question, String sysPromptId, String sessionModelId,String userId) {
        return null;
    }

    @Override
    public AiOutMsgDTO genGameHtml(TAiModel tAiModel, String question, String sessionModelId, String userId) {
        return null;
    }

    @Override
    public AiOutMsgDTO dateQueryAndToEchart(TAiModel tAiModel, String question, String sysPromptId, String sessionModelId,String userId) {
        return null;
    }

    @Override
    public AiOutMsgDTO textToAudio(TAiModel tAiModel, String question, String sysPromptId, String sessionModelId, String userId) {
        return null;
    }

    @Override
    public AiOutMsgDTO teaBaiwen(TAiModel tAiModel, String question, String sessionModelId, String userId) {
        return null;
    }

    @Override
    public AiOutMsgDTO stuBaiwen(TAiModel tAiModel, String question, String sessionModelId, String userId) {
        return null;
    }
}
