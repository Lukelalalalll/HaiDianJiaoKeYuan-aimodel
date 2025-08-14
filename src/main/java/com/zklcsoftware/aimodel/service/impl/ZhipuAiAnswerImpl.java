package com.zklcsoftware.aimodel.service.impl;

import com.alibaba.fastjson.JSON;
import com.zhipu.oapi.ClientV4;
import com.zhipu.oapi.service.v4.model.*;
import com.zklcsoftware.aimodel.domain.TAiModel;
import com.zklcsoftware.aimodel.dto.MessageDTO;
import com.zklcsoftware.aimodel.service.AiAnswerService;
import com.zklcsoftware.aimodel.util.ConstantUtil;
import com.zklcsoftware.aimodel.websocket.dto.AiOutMsgDTO;
import io.reactivex.Flowable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.String.format;

/**
 * @author zhushaog
 * @version 1.0
 * @ClassName TongyiAiAnswerImpl.java
 * @company zklcsoftware
 * @Description 智谱文档回答实现
 * @createTime 2024/08/23 17:04
 */
@Service("ZhipuAiAnswerImpl")
@Slf4j
public class ZhipuAiAnswerImpl extends AiAnswerCallServiceImpl implements AiAnswerService {
    public static Map<String, List<ChatMessage>> userChatMessages = new HashMap<>();//上下文问题记录
    @Resource
    private SimpMessagingTemplate messagingTemplate;

    public AiOutMsgDTO sendAnswer(TAiModel tAiModel, String question, String questionPromptFormat, String sessionId, List<MessageDTO> userChatMessages, String userId, Integer callType, String knowledgeDocumentUrl) {
        AiOutMsgDTO aiOutMsgDTO = null;
        String contextId="";
        try {
            final String[] totalAnswer = {""};
            ClientV4 client = new ClientV4.Builder(tAiModel.getAppkey()).build();
            List<ChatMessage> messages = new ArrayList<>();
            //用户上下文处理
            for (MessageDTO userChatMessage : userChatMessages) {
                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setRole(userChatMessage.getRole());
                chatMessage.setContent(userChatMessage.getContent());
                messages.add(chatMessage);
            }
            ChatMessage chatMessage = new ChatMessage("user", StringUtils.isNotBlank(questionPromptFormat) ? questionPromptFormat : question);
            messages.add(chatMessage);
            ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                    .model("GLM-4-0520")//使用最新官方大模型
                    .stream(Boolean.TRUE)
                    .messages(messages)
                    .requestId(UUID.randomUUID().toString().replaceAll("-", ""))
                    .build();

            //用户提问及回答结果保存 用于上下文回答
            contextId=this.answeredCall(question,questionPromptFormat, null, sessionId);
            ConstantUtil.contextIdMap.put(sessionId,contextId);//记录当前会话的最后一次上下文ID
            log.info("智普回答开始");
            ModelApiResponse sseModelApiResp = client.invokeModelApi(chatCompletionRequest);
            if (sseModelApiResp.isSuccess()) {
                AtomicBoolean isFirst = new AtomicBoolean(true);

                ChatMessageAccumulator chatMessageAccumulator = mapStreamToAccumulator(sseModelApiResp.getFlowable())
                        .doOnNext(accumulator -> {
                            {
                                    /*if (isFirst.getAndSet(false)) {

                                    }*/
                                if (accumulator.getDelta() != null && accumulator.getDelta().getTool_calls() != null) {
                                    String jsonString = JSON.toJSONString(accumulator.getDelta().getTool_calls());
                                    System.out.println("tool_calls: " + jsonString);
                                }
                                if (accumulator.getDelta() != null && accumulator.getDelta().getContent() != null) {
                                    totalAnswer[0] = totalAnswer[0] + accumulator.getDelta().getContent();
                                    try {
                                        messagingTemplate.convertAndSendToUser(userId, format("/chatmessage" + tAiModel.getId() + "/%s", sessionId), gson.toJson(AiOutMsgDTO.wsSuccessText(totalAnswer[0])));
                                        Thread.sleep(100);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        return;
                                    }

                                }
                            }
                        })
                        .lastElement()
                        .blockingGet();

                //用户提问及回答结果保存 用于上下文回答
                this.answeredCallUpdate(contextId, totalAnswer[0],sessionId,null);

                if (callType == ConstantUtil.CALL_TYPE_1) {
                    //给用户返回结束回答结束动作
                    messagingTemplate.convertAndSendToUser(userId, format("/chatmessage" + tAiModel.getId() + "/%s", sessionId), gson.toJson(AiOutMsgDTO.wsSuccessDone(contextId, totalAnswer[0])));
                } else if (callType == ConstantUtil.CALL_TYPE_2) {
                    aiOutMsgDTO = AiOutMsgDTO.wsSuccessDone(contextId, totalAnswer[0]);
                }

                /*Choice choice = new Choice(chatMessageAccumulator.getChoice().getFinishReason(), 0L, chatMessageAccumulator.getDelta());
                List<Choice> choices = new ArrayList<>();
                choices.add(choice);
                ModelData data = new ModelData();
                data.setChoices(choices);
                data.setUsage(chatMessageAccumulator.getUsage());
                data.setId(chatMessageAccumulator.getId());
                data.setCreated(chatMessageAccumulator.getCreated());
                data.setRequestId(chatCompletionRequest.getRequestId());
                sseModelApiResp.setFlowable(null);
                sseModelApiResp.setData(data);*/
            }
        } catch (Exception e) {
            //给用户返回结束回答结束动作
            messagingTemplate.convertAndSendToUser(userId, format("/chatmessage" + tAiModel.getId() + "/%s", sessionId)
                    , gson.toJson(AiOutMsgDTO.wsSuccessText("<服务繁忙，请稍后再试>")));
            messagingTemplate.convertAndSendToUser(userId, format("/chatmessage" + tAiModel.getId() + "/%s", sessionId)
                    , gson.toJson(AiOutMsgDTO.wsSuccessDone(contextId,"<服务繁忙，请稍后再试>")));
            log.error("智谱文档回答API异常", e);
        }
        log.info("智普回答结束");
        return aiOutMsgDTO;
    }

    @Override
    public AiOutMsgDTO sendAnswerWithImages(TAiModel tAiModel, String question, String questionPromptFormat, String sessionId, List<MessageDTO> messageDTOS, String userId, List<String> images) {
        return null;
    }

    public static Flowable<ChatMessageAccumulator> mapStreamToAccumulator(Flowable<ModelData> flowable) {
        return flowable.map((chunk) -> {
            return new ChatMessageAccumulator(((Choice) chunk.getChoices().get(0)).getDelta(), (ChatMessage) null, (Choice) chunk.getChoices().get(0), chunk.getUsage(), chunk.getCreated(), chunk.getId());
        });
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
