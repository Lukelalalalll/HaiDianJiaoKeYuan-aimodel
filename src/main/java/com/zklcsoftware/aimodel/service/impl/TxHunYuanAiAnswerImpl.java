package com.zklcsoftware.aimodel.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.SSEResponseModel;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.hunyuan.v20230901.HunyuanClient;
import com.tencentcloudapi.hunyuan.v20230901.models.ChatCompletionsRequest;
import com.tencentcloudapi.hunyuan.v20230901.models.ChatCompletionsResponse;
import com.tencentcloudapi.hunyuan.v20230901.models.Message;
import com.zhipu.oapi.service.v4.model.ChatMessage;
import com.zhipu.oapi.service.v4.model.ChatMessageAccumulator;
import com.zhipu.oapi.service.v4.model.Choice;
import com.zhipu.oapi.service.v4.model.ModelData;
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
 * @Description 腾讯混元回答实现
 * @createTime 2024/08/23 17:04
 */
@Service("TxHunYuanAiAnswerImpl")
@Slf4j
public class TxHunYuanAiAnswerImpl extends AiAnswerCallServiceImpl implements AiAnswerService {
    public static Map<String, List<Message>> userChatMessages = new HashMap<>();//上下文问题记录
    @Resource
    private SimpMessagingTemplate messagingTemplate;

    public AiOutMsgDTO sendAnswer(TAiModel tAiModel, String question, String questionPromptFormat, String sessionId, List<MessageDTO> userChatMessages, String userId, Integer callType, String knowledgeDocumentUrl) {
        AiOutMsgDTO aiOutMsgDTO = null;
        //用户提问及回答结果保存 用于上下文回答
        String contextId=null;
        try {
            String totalAnswer = "";
            List<Message> messages = new ArrayList<>();
            for (MessageDTO userChatMessage : userChatMessages) {
                Message chatMessage = new Message();
                chatMessage.setRole(userChatMessage.getRole());
                chatMessage.setContent(userChatMessage.getContent());
                messages.add(chatMessage);
            }

            Message chatMessage = new Message();
            chatMessage.setRole("user");
            chatMessage.setContent(StringUtils.isNotBlank(questionPromptFormat) ? questionPromptFormat : question);
            messages.add(chatMessage);//添加本次问题

            // 实例化一个认证对象，入参需要传入腾讯云账户 SecretId 和 SecretKey，此处还需注意密钥对的保密
            // 代码泄露可能会导致 SecretId 和 SecretKey 泄露，并威胁账号下所有资源的安全性。以下代码示例仅供参考，建议采用更安全的方式来使用密钥，请参见：https://cloud.tencent.com/document/product/1278/85305
            // 密钥可前往官网控制台 https://console.cloud.tencent.com/cam/capi 进行获取
            Credential cred = new Credential(tAiModel.getAppkey(), tAiModel.getAppsecret());
            // 实例化一个http选项，可选的，没有特殊需求可以跳过
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint("hunyuan.tencentcloudapi.com");
            // 实例化一个client选项，可选的，没有特殊需求可以跳过
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            // 实例化要请求产品的client对象,clientProfile是可选的
            HunyuanClient client = new HunyuanClient(cred, "", clientProfile);
            // 实例化一个请求对象,每个接口都会对应一个request对象
            ChatCompletionsRequest req = new ChatCompletionsRequest();
            req.setModel("hunyuan-pro");
            req.setMessages(messages.toArray(new Message[messages.size()]));
            req.setStream(true);

            //用户提问及回答结果保存 用于上下文回答
            contextId=this.answeredCall(question,questionPromptFormat, null, sessionId);
            ConstantUtil.contextIdMap.put(sessionId,contextId);//记录当前会话的最后一次上下文ID
            log.info("腾讯混元回答开始");
            // 返回的resp是一个ChatCompletionsResponse的实例，与请求对象对应
            ChatCompletionsResponse resp = client.ChatCompletions(req);
            // 输出json格式的字符串回包
            JSONArray jsonArray = null;
            if (resp.isStream()) { // 流式响应
                for (SSEResponseModel.SSE e : resp) {
                    jsonArray = JSONObject.parseObject(e.Data).getJSONArray("Choices");
                    for (int i = 0; i < jsonArray.size(); i++) {
                        totalAnswer = totalAnswer + jsonArray.getJSONObject(i).getJSONObject("Delta").getString("Content");
                    }
                    messagingTemplate.convertAndSendToUser(userId, format("/chatmessage" + tAiModel.getId() + "/%s", sessionId), gson.toJson(AiOutMsgDTO.wsSuccessText(totalAnswer)));
                }
            }

            //用户提问及回答结果保存 用于上下文回答
            this.answeredCallUpdate(contextId, totalAnswer,sessionId,knowledgeDocumentUrl);

            if (callType == ConstantUtil.CALL_TYPE_1) {
                //给用户返回结束回答结束动作
                messagingTemplate.convertAndSendToUser(userId, format("/chatmessage" + tAiModel.getId() + "/%s", sessionId), gson.toJson(AiOutMsgDTO.wsSuccessDone(contextId, totalAnswer)));
            } else if (callType == ConstantUtil.CALL_TYPE_2) {
                aiOutMsgDTO = AiOutMsgDTO.wsSuccessDone(contextId, totalAnswer);
            }

        } catch (Exception e) {
            //给用户返回结束回答结束动作
            messagingTemplate.convertAndSendToUser(userId, format("/chatmessage" + tAiModel.getId() + "/%s", sessionId)
                    , gson.toJson(AiOutMsgDTO.wsSuccessText("<服务繁忙，请稍后再试>")));
            messagingTemplate.convertAndSendToUser(userId, format("/chatmessage" + tAiModel.getId() + "/%s", sessionId)
                    , gson.toJson(AiOutMsgDTO.wsSuccessDone(contextId,"<服务繁忙，请稍后再试>")));
            log.error("腾讯混元回答API异常", e);
        }
        log.info("腾讯混元回答结束");
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
