package com.zklcsoftware.aimodel.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zklcsoftware.aimodel.domain.TAiModel;
import com.zklcsoftware.aimodel.dto.MessageDTO;
import com.zklcsoftware.aimodel.service.AiAnswerService;
import com.zklcsoftware.aimodel.util.ConstantUtil;
import com.zklcsoftware.aimodel.util.RagFlowUtil;
import com.zklcsoftware.aimodel.websocket.dto.AiOutMsgDTO;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.internal.sse.RealEventSource;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

/**
 * @author zhushaog
 * @version 1.0
 * @ClassName RagflowAgentAnswerImpl.java
 * @company zklcsoftware
 * @Description ragflowAgent回答调用
 * @createTime 2024/08/23 17:04
 */
@Service("RagflowAgentAnswerImpl")
@Slf4j
@Scope("prototype")
public class RagflowAgentAnswerImpl extends AiAnswerCallServiceImpl implements AiAnswerService {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    public AiOutMsgDTO sendAnswer(TAiModel tAiModel, String question,String questionPromptFormat, String sessionId, List<MessageDTO> userChatMessages,String userId,Integer callType, String knowledgeDocumentUrl) {

        AiOutMsgDTO aiOutMsgDTO=null;
        // 创建消息列表
        userChatMessages.add(MessageDTO.builder()
                .role("user")
                .content(StringUtils.isNotBlank(questionPromptFormat)?questionPromptFormat:question)
                .build());

        String ragFlowHostUrl=JSON.parseObject(tAiModel.getExtArgJson()).getString("host_url");//第三方agent服务地址
        String ragFlowApiKey= tAiModel.getAppsecret();//第三方agent服务apiKey

        JSONObject jsonObject = new JSONObject();
        if(tAiModel.getIsIgnoreQuestion()){
            jsonObject.put("question", "按要求输出");
        }else{
            jsonObject.put("question", question);
        }

        jsonObject.put("stream", true);
        //jsonObject.putAll(tAiModel.getAgentInputArgs());//扩展参数

        //创建agent的会话
        JSONObject sessionJSONObject=RagFlowUtil.createAgentSession(ragFlowHostUrl,ragFlowApiKey,tAiModel.getAgentId(),sessionId,tAiModel.getAgentInputArgs());
        if(sessionJSONObject.getInteger("code")==0){
            jsonObject.put("session_id",sessionJSONObject.getJSONObject("data").getString("id"));
            jsonObject.put("sync_dsl",true);
        }

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json;chartset=uft-8"),jsonObject.toString());
        // 创建请求对象
        Request request = new Request.Builder()
                .url(ragFlowHostUrl+"/api/v1/agents/"+tAiModel.getAgentId()+"/completions")
                .post(requestBody) // 请求体
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + ragFlowApiKey)
                .build();

        // 开启 Http 客户端
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)   // 建立连接的超时时间
                .readTimeout(10, TimeUnit.MINUTES)  // 建立连接后读取数据的超时时间
                .build();

        // 创建一个 CountDownLatch 对象，其初始计数为1，表示需要等待一个事件发生后才能继续执行。
        CountDownLatch eventLatch = new CountDownLatch(1);
        final String[] fullContent = {null};

        // 实例化EventSource，注册EventSource监听器 -- 创建一个用于处理服务器发送事件的实例，并定义处理事件的回调逻辑
        RealEventSource realEventSource = new RealEventSource(request, new EventSourceListener() {
            @Override
            public void onEvent(EventSource eventSource, String id, String type, String data) {
                //log.info("百川智能文档回答:" + data);
                JSONObject message=JSONObject.parseObject(data);
                Boolean done=false;
                if(message.get("data") instanceof Boolean){
                    done=true;
                }else{
                    fullContent[0] =message.getJSONObject("data").getString("answer");
                    messagingTemplate.convertAndSendToUser(userId, format("/chatmessage"+tAiModel.getId()+"/%s", sessionId), gson.toJson(AiOutMsgDTO.wsSuccessText(fullContent[0])));
                }
                if (done) {    // 消息类型，add 增量，finish 结束，error 错误，interrupted 中断
                    eventLatch.countDown();
                }
            }
            @Override
            public void onClosed(EventSource eventSource) {
                eventLatch.countDown(); // 重新设置中断状态
            }
            @Override
            public void onFailure(EventSource eventSource, @Nullable Throwable t, @Nullable Response response) {
                eventLatch.countDown(); // 重新设置中断状态
                log.error("RagFlowAgent-AI异常结束：{}",t.toString(),response);
            }

        }

        );

        //用户提问及回答结果保存 用于上下文回答
        String contextId=this.answeredCall(question,questionPromptFormat, null, sessionId);
        ConstantUtil.contextIdMap.put(sessionId,contextId);//记录当前会话的最后一次上下文ID
        // 与服务器建立连接
        realEventSource.connect(okHttpClient);

        try {
            eventLatch.await();
        } catch (InterruptedException e) {
            eventLatch.countDown(); // 重新设置中断状态
            realEventSource.cancel();
        }

        //用户提问及回答结果保存 用于上下文回答
        this.answeredCallUpdate(contextId, fullContent[0],sessionId,knowledgeDocumentUrl);

        if (callType == ConstantUtil.CALL_TYPE_1) {
            //给用户返回结束回答结束动作
            messagingTemplate.convertAndSendToUser(userId, format("/chatmessage"+tAiModel.getId()+"/%s", sessionId), gson.toJson(AiOutMsgDTO.wsSuccessDone(contextId, fullContent[0])));
        } else if (callType == ConstantUtil.CALL_TYPE_2) {
            aiOutMsgDTO = AiOutMsgDTO.wsSuccessDone(contextId, fullContent[0]);
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
