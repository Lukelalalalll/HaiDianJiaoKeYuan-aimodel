package com.zklcsoftware.aimodel.service.impl;

import com.baidubce.qianfan.Qianfan;
import com.baidubce.qianfan.model.chat.Message;
import com.google.gson.Gson;
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

import static java.lang.String.format;

/**
 * @author zhushaog
 * @version 1.0
 * @ClassName TongyiAiAnswerImpl.java
 * @company zklcsoftware
 * @Description 文心一言文档回答实现
 * @createTime 2024/08/23 17:04
 */
@Service("WenxinyiyanAiAnswerImpl")
@Slf4j
public class WenxinyiyanAiAnswerImpl extends AiAnswerCallServiceImpl implements AiAnswerService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public static Map<String, List<Message>> userChatMessages=new HashMap<>();//上下文问题记录

    @Override
    public AiOutMsgDTO sendAnswer(TAiModel tAiModel, String question,String questionPromptFormat, String sessionId, List<MessageDTO> userChatMessages,String userId,Integer callType, String knowledgeDocumentUrl) {

        AiOutMsgDTO aiOutMsgDTO=null;
        StringBuilder fullContent=new StringBuilder();
        Gson gson = new Gson();
        List<Message> messages = new ArrayList<>();
        String systemPrompt=null;
        for (MessageDTO userChatMessage : userChatMessages) {
            if("system".equals(userChatMessage.getRole())){
                systemPrompt=userChatMessage.getContent();
            }else{
                Message chatMessage = new Message();
                chatMessage.setRole(userChatMessage.getRole());
                chatMessage.setContent(userChatMessage.getContent());
                messages.add(chatMessage);
            }
        }

        Message chatMessage = new Message();
        chatMessage.setRole("user");
        chatMessage.setContent(StringUtils.isNotBlank(questionPromptFormat)?questionPromptFormat:question);
        messages.add(chatMessage);

        //用户提问及回答结果保存 用于上下文回答
        String contextId=this.answeredCall(question,questionPromptFormat, null, sessionId);
        ConstantUtil.contextIdMap.put(sessionId,contextId);//记录当前会话的最后一次上下文ID
        log.info("文心一言回答开始");
        // 使用安全认证AK/SK鉴权，替换下列示例中参数，安全认证Access Key替换your_iam_ak，Secret Key替换your_iam_sk
        Qianfan qianfan = new Qianfan(tAiModel.getAppkey(), tAiModel.getAppsecret());
        try {
            qianfan.chatCompletion()
                    .model("ERNIE-4.0-8K")
                    //.model("Yi-34B-Chat")
                    .messages(messages)
                    .system(systemPrompt)//设置systemprompt
                    // 启用流式返回
                    .executeStream()
                    .forEachRemaining(chunk ->{
                                fullContent.append(chunk.getResult());
                                messagingTemplate.convertAndSendToUser(userId, format("/chatmessage"+tAiModel.getId()+"/%s", sessionId), gson.toJson(AiOutMsgDTO.wsSuccessText(fullContent.toString())));
                            }
                    );
            //用户提问及回答结果保存 用于上下文回答
            this.answeredCallUpdate(contextId, fullContent.toString(),sessionId, knowledgeDocumentUrl);
            if (callType == ConstantUtil.CALL_TYPE_1) {
                //给用户返回结束回答结束动作
                messagingTemplate.convertAndSendToUser(userId, format("/chatmessage"+tAiModel.getId()+"/%s", sessionId), gson.toJson(AiOutMsgDTO.wsSuccessDone(contextId,fullContent.toString())));
            } else if (callType == ConstantUtil.CALL_TYPE_2) {
                aiOutMsgDTO = AiOutMsgDTO.wsSuccessDone(contextId, fullContent.toString());
            }
            log.info("文心一言回答结束");
        }catch (Exception e){
            //给用户返回结束回答结束动作
            messagingTemplate.convertAndSendToUser(userId, format("/chatmessage" + tAiModel.getId() + "/%s", sessionId)
                    , gson.toJson(AiOutMsgDTO.wsSuccessText("<服务繁忙，请稍后再试>")));
            messagingTemplate.convertAndSendToUser(userId, format("/chatmessage" + tAiModel.getId() + "/%s", sessionId)
                    , gson.toJson(AiOutMsgDTO.wsSuccessDone(contextId,"<服务繁忙，请稍后再试>")));
            log.error("文心一言回答API异常", e);
        }
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
