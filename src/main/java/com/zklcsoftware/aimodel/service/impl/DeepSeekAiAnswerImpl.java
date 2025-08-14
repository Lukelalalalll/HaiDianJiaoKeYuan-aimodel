package com.zklcsoftware.aimodel.service.impl;

import com.alibaba.dashscope.common.Message;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zklcsoftware.aimodel.domain.TAiModel;
import com.zklcsoftware.aimodel.dto.MessageDTO;
import com.zklcsoftware.aimodel.service.AiAnswerService;
import com.zklcsoftware.aimodel.util.ConstantUtil;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

/**
 * @author zhushaog
 * @version 1.0
 * @ClassName DeepSeekAiAnswerImpl.java
 * @company zklcsoftware
 * @Description deepseek智能回答实现  https://platform.baichuan-ai.com/docs/api#16
 * @createTime 2024/08/23 17:04
 */
@Service("DeepSeekAiAnswerImpl")
@Slf4j
@Scope("prototype")
public class DeepSeekAiAnswerImpl extends AiAnswerCallServiceImpl implements AiAnswerService {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    public static Map<String, List<Message>> userChatMessages = new HashMap<>();//上下文问题记录

    private Boolean wsCloseFlag = false;

    private TAiModel tAiModel;

    private String sessionId;

    private String userId;

    private String question;

    private String totalAnswer = "";

    public String getTotalAnswer() {
        return totalAnswer;
    }

    public void setTotalAnswer(String totalAnswer) {
        this.totalAnswer = totalAnswer;
    }

    public Boolean getWsCloseFlag() {
        return wsCloseFlag;
    }

    public void setWsCloseFlag(Boolean wsCloseFlag) {
        this.wsCloseFlag = wsCloseFlag;
    }

    public TAiModel gettAiModel() {
        return tAiModel;
    }

    public void settAiModel(TAiModel tAiModel) {
        this.tAiModel = tAiModel;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    @Override
    public AiOutMsgDTO sendAnswer(TAiModel tAiModel, String question,String questionPromptFormat, String sessionId, List<MessageDTO> userChatMessages,String userId,Integer callType, String knowledgeDocumentUrl) {

        AiOutMsgDTO aiOutMsgDTO=null;
        StringBuilder fullContent = new StringBuilder();
        final String[] stopContent = {""};//终止原因
        StringBuilder reasoningContent = new StringBuilder();
        // 创建消息列表
        userChatMessages.add(MessageDTO.builder()
                        .role("user")
                        .content(StringUtils.isNotBlank(questionPromptFormat)?questionPromptFormat:question)
                .build());

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("model", JSON.parseObject(tAiModel.getExtArgJson()).getString("model_id"));
        jsonObject.put("stream", true);
        jsonObject.put("messages", userChatMessages);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json;chartset=uft-8"),jsonObject.toString());
        // 创建请求对象
        Request request = new Request.Builder()
                .url(JSON.parseObject(tAiModel.getExtArgJson()).getString("host_url"))
                .post(requestBody) // 请求体
                .addHeader("Authorization", "Bearer " + tAiModel.getAppsecret())
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .build();

        // 开启 Http 客户端
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)   // 建立连接的超时时间
                .readTimeout(10, TimeUnit.MINUTES)  // 建立连接后读取数据的超时时间
                .build();

        // 创建一个 CountDownLatch 对象，其初始计数为1，表示需要等待一个事件发生后才能继续执行。
        CountDownLatch eventLatch = new CountDownLatch(1);

        // 实例化EventSource，注册EventSource监听器 -- 创建一个用于处理服务器发送事件的实例，并定义处理事件的回调逻辑
        RealEventSource realEventSource = new RealEventSource(request, new EventSourceListener() {
            @Override
            public void onEvent(EventSource eventSource, String id, String type, String data) {

                JSONObject message=JSONObject.parseObject(data);
                String finish_reason=message.getJSONArray("choices").getJSONObject(0).getString("finish_reason");

                if(message.getJSONArray("choices")!=null
                        && message.getJSONArray("choices").getJSONObject(0)!=null
                        && message.getJSONArray("choices").getJSONObject(0).getJSONObject("delta")!=null){

                    if(message.getJSONArray("choices").getJSONObject(0).getJSONObject("delta").containsKey("reasoning_content")
                            && message.getJSONArray("choices").getJSONObject(0).getJSONObject("delta").getString("content")==null){//思考内容
                        if("1".equals(ConstantUtil.sysConfig.get("is_view_reason"))){//是否显示思考过程
                            reasoningContent.append(message.getJSONArray("choices").getJSONObject(0).getJSONObject("delta").getString("reasoning_content"));
                            //log.info("DeepSeek智能文档回答:{}", message.getJSONArray("choices").getJSONObject(0).getJSONObject("delta").getString("reasoning_content"));
                            messagingTemplate.convertAndSendToUser(userId, format("/chatmessage"+tAiModel.getId()+"/%s", sessionId), gson.toJson(AiOutMsgDTO.wsSuccessText("<div class=\"thought-content\"><div class=\"thought-content thought-top-title\"><h3>思考：</h3></div>"+"\n"+reasoningContent.toString()+"</div>")));
                        }

                    }else{
                        fullContent.append(message.getJSONArray("choices").getJSONObject(0).getJSONObject("delta").getString("content"));
                        //log.info("DeepSeek智能文档回答:{}", message.getJSONArray("choices").getJSONObject(0).getJSONObject("delta").getString("content"));
                        if(reasoningContent.length()>0 && "1".equals(ConstantUtil.sysConfig.get("is_view_reason"))){
                            messagingTemplate.convertAndSendToUser(userId, format("/chatmessage"+tAiModel.getId()+"/%s", sessionId), gson.toJson(AiOutMsgDTO.wsSuccessText("<div class=\"thought-content\"><div class=\"thought-content thought-top-title\"><h3>思考：</h3></div>"+"\n"+reasoningContent.toString()+"</div>"+"\n"+fullContent.toString())));
                        }else{
                            String fullContentStr=fullContent.toString().replace("<think>","<div class=\"thought-content\"><div class=\"thought-content thought-top-title\"><h3>思考：</h3></div>").replace("</think>","</div>");
                            messagingTemplate.convertAndSendToUser(userId, format("/chatmessage"+tAiModel.getId()+"/%s", sessionId), gson.toJson(AiOutMsgDTO.wsSuccessText(fullContentStr)));
                        }
                    }
                }

                /*stop：模型自然停止生成，或遇到 stop 序列中列出的字符串。

                length ：输出长度达到了模型上下文长度限制，或达到了 max_tokens 的限制。

                content_filter：输出内容因触发过滤策略而被过滤。

                insufficient_system_resource：系统推理资源不足，生成被打断。*/

                if ("stop".equals(finish_reason)
                        || "length".equals(finish_reason)
                        || "content_filter".equals(finish_reason)
                        || "insufficient_system_resource".equals(finish_reason)
                        || "tool_calls".equals(finish_reason) ) {

                    if(!"stop".equals(finish_reason)){//非正常结束
                        stopContent[0] ="<意外终止，请稍后重试>";
                    }

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
                log.error("DeepSeekAI异常结束：{}",response);
            }

        });

        //用户提问及回答结果保存 用于上下文回答
        String contextId=this.answeredCall(question,questionPromptFormat, null, sessionId);
        ConstantUtil.contextIdMap.put(sessionId,contextId);//记录当前会话的最后一次上下文ID
        log.info("DeepSeek回答开始");
// 与服务器建立连接
        realEventSource.connect(okHttpClient);

// await() 方法被调用来阻塞当前线程，直到 CountDownLatch 的计数变为0。
        try {
            eventLatch.await();
        } catch (InterruptedException e) {
            eventLatch.countDown(); // 重新设置中断状态
            realEventSource.cancel();
        }

        //用户提问及回答结果保存 用于上下文回答
        String answerContent=fullContent.toString();
        if(reasoningContent.length()>0 && "1".equals(ConstantUtil.sysConfig.get("is_view_reason")) ){
            answerContent="<div class=\"thought-content\"><div class=\"thought-content thought-top-title\"><h3>思考：</h3></div>"+"\n"+reasoningContent.toString()+"</div>"+"\n"+fullContent.toString();
        }
        this.answeredCallUpdate(contextId,answerContent,sessionId,knowledgeDocumentUrl);

        if (callType == ConstantUtil.CALL_TYPE_1) {
            //给用户返回结束回答结束动作
            messagingTemplate.convertAndSendToUser(userId, format("/chatmessage"+tAiModel.getId()+"/%s", sessionId), gson.toJson(AiOutMsgDTO.wsSuccessDone(contextId,fullContent.toString()+stopContent[0])));
        } else if (callType == ConstantUtil.CALL_TYPE_2) {
            aiOutMsgDTO = AiOutMsgDTO.wsSuccessDone(contextId, fullContent.toString());
        }
        log.info("DeepSeek回答结束");
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
