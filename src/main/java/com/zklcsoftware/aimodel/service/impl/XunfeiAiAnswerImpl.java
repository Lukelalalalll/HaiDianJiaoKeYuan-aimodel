package com.zklcsoftware.aimodel.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.zklcsoftware.aimodel.domain.*;
import com.zklcsoftware.aimodel.dto.MessageDTO;
import com.zklcsoftware.aimodel.repository.TAiUserSessionModelContextFileindexRepository;
import com.zklcsoftware.aimodel.repository.TAiUserSessionModelContextKeysRepository;
import com.zklcsoftware.aimodel.repository.TAiUserSessionModelContextRepository;
import com.zklcsoftware.aimodel.service.AiAnswerService;
import com.zklcsoftware.aimodel.service.TAiUserSessionModelService;
import com.zklcsoftware.aimodel.util.ConstantUtil;
import com.zklcsoftware.aimodel.util.IKAnalyzerUtil;
import com.zklcsoftware.aimodel.vo.TAiUserSessionModelContextFileindexVO;
import com.zklcsoftware.aimodel.websocket.dto.AiOutMsgDTO;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.lang.String.format;

/**
 * @author zhushaog
 * @version 1.0
 * @ClassName XunfeiAiAnswerImpl.java
 * @company zklcsoftware
 * @Description 讯飞AI回答实现
 * @createTime 2024/08/23 17:04
 */
@Service("XunfeiAiAnswerImpl")
@Slf4j
@Scope("prototype")
public class XunfeiAiAnswerImpl extends WebSocketListener implements AiAnswerService {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    TAiUserSessionModelContextRepository tAiUserSessionModelContextRepository;

    @Autowired
    TAiUserSessionModelContextKeysRepository tAiUserSessionModelContextKeysRepository;

    @Autowired
    private TAiUserSessionModelContextFileindexRepository tAiUserSessionModelContextFileindexRepository;

    @Autowired
    private TAiUserSessionModelService tAiUserSessionModelService;

    public static Map<String,List<RoleContent>> historyListMap=new HashMap<>();//上下文问题记录

    private Boolean wsCloseFlag=false;

    private TAiModel tAiModel;

    private String sessionId;
    private String userId;

    private List<MessageDTO> userChatMessages;

    private String question;

    private String questionPromptFormat;

    private Integer callType;

    private String knowledgeDocumentUrl;

    public String getContextId() {
        return contextId;
    }

    public void setContextId(String contextId) {
        this.contextId = contextId;
    }

    private String contextId;

    public AiOutMsgDTO getAiOutMsgDTO() {
        return aiOutMsgDTO;
    }

    public void setAiOutMsgDTO(AiOutMsgDTO aiOutMsgDTO) {
        this.aiOutMsgDTO = aiOutMsgDTO;
    }

    private AiOutMsgDTO aiOutMsgDTO;

    public Integer getCallType() {
        return callType;
    }

    public void setCallType(Integer callType) {
        this.callType = callType;
    }

    public String getKnowledgeDocumentUrl() {
        return knowledgeDocumentUrl;
    }

    public void setKnowledgeDocumentUrl(String knowledgeDocumentUrl) {
        this.knowledgeDocumentUrl = knowledgeDocumentUrl;
    }

    protected static Gson gson=new Gson();

    public String getQuestionPromptFormat() {
        return questionPromptFormat;
    }

    public void setQuestionPromptFormat(String questionPromptFormat) {
        this.questionPromptFormat = questionPromptFormat;
    }

    private String totalAnswer="";

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

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

    public List<MessageDTO> getUserChatMessages() {
        return userChatMessages;
    }

    public void setUserChatMessages(List<MessageDTO> userChatMessages) {
        this.userChatMessages = userChatMessages;
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
        settAiModel(tAiModel);
        setUserChatMessages(userChatMessages);
        setSessionId(sessionId);
        setQuestion(question);
        setUserId(userId);
        setQuestionPromptFormat(questionPromptFormat);
        setCallType(callType);
        setKnowledgeDocumentUrl(knowledgeDocumentUrl);

        // 构建鉴权url
        String authUrl = null;
        WebSocket webSocket =null;
        try {
            authUrl = getAuthUrl(JSON.parseObject(tAiModel.getExtArgJson()).getString("host_url"), tAiModel.getAppkey(), tAiModel.getAppsecret());
            OkHttpClient client = new OkHttpClient.Builder().build();
            String url = authUrl.toString().replace("http://", "ws://").replace("https://", "wss://");
            Request request = new Request.Builder().url(url).build();
            webSocket = client.newWebSocket(request, this);
        } catch (Exception e) {
            wsCloseFlag=true;
            log.error("讯飞星火调用失败",e);
        }
        while (!wsCloseFlag){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                wsCloseFlag=true;
                log.error("讯飞星火AI回答线程休眠异常",e);
            }
        }
        return aiOutMsgDTO;
    }

    @Override
    public AiOutMsgDTO sendAnswerWithImages(TAiModel tAiModel, String question, String questionPromptFormat, String sessionId, List<MessageDTO> messageDTOS, String userId, List<String> images) {
        return null;
    }


    // 鉴权方法
    private  String getAuthUrl(String hostUrl, String apiKey, String apiSecret) throws Exception {
        URL url = new URL(hostUrl);
        // 时间
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        String date = format.format(new Date());
        // 拼接
        String preStr = "host: " + url.getHost() + "\n" +
                "date: " + date + "\n" +
                "GET " + url.getPath() + " HTTP/1.1";
        // System.err.println(preStr);
        // SHA256加密
        Mac mac = Mac.getInstance("hmacsha256");
        SecretKeySpec spec = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "hmacsha256");
        mac.init(spec);

        byte[] hexDigits = mac.doFinal(preStr.getBytes(StandardCharsets.UTF_8));
        // Base64加密
        String sha = Base64.getEncoder().encodeToString(hexDigits);
        // System.err.println(sha);
        // 拼接
        String authorization = String.format("api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"", apiKey, "hmac-sha256", "host date request-line", sha);
        // 拼接地址
        HttpUrl httpUrl = Objects.requireNonNull(HttpUrl.parse("https://" + url.getHost() + url.getPath())).newBuilder().//
                addQueryParameter("authorization", Base64.getEncoder().encodeToString(authorization.getBytes(StandardCharsets.UTF_8))).//
                addQueryParameter("date", date).//
                addQueryParameter("host", url.getHost()).//
                build();

        // System.err.println(httpUrl.toString());
        return httpUrl.toString();
    }

    public void onOpen(WebSocket webSocket, Response response) {
        super.onOpen(webSocket, response);
        log.info("讯飞请求开始");
        MyThread myThread = new MyThread(webSocket);
        myThread.start();
    }

    private TAiUserSessionModelContext answeredCallObject(String question,String questionPromptFormat, String answer, String sessionModelId, String knowledgeDocumentUrl) {
        TAiUserSessionModelContext tAiUserSessionModelContext=TAiUserSessionModelContext.builder()
                .createTime(new Date())
                .assistantOut(answer)
                .userIn(question)
                .userInPromptFormat(StringUtils.isNotBlank(questionPromptFormat)?questionPromptFormat:question)
                .sessionModelId(sessionModelId)
                .build();
        tAiUserSessionModelContextRepository.save(tAiUserSessionModelContext);

        if(StringUtils.isNotBlank(knowledgeDocumentUrl)){
            TAiUserSessionModel sessionModel = tAiUserSessionModelService.findById(sessionModelId);
            String[] split = knowledgeDocumentUrl.split(",");
            for (String url : split) {
                TAiUserSessionModelContextFileindex fileindex = new TAiUserSessionModelContextFileindex().builder()
                        .documentUrl(url)
                        .sessionId(sessionModel.getSessionId())
                        .sessionModelId(sessionModelId)
                        .sessionContextId(tAiUserSessionModelContext.getId())
                        .createTime(new Date())
                        .build();
                tAiUserSessionModelContextFileindexRepository.save(fileindex);
            }
        }

        //将提问信息分词处理 保存到数据库
        try {
            List<String> results= IKAnalyzerUtil.getIKAnalyzerResult(question);
            for (String result : results) {
                tAiUserSessionModelContextKeysRepository.save(TAiUserSessionModelContextKeys.builder()
                        .sessionContextId(tAiUserSessionModelContext.getId())
                        .keyWord(result)
                        .createTime(new Date())
                        .build());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return tAiUserSessionModelContext;

    }

    @Override
    public String answeredCall(String question, String questionPromptFormat, String answer, String sessionModelId) {
        return null;
    }

    @Override
    public void answeredCallUpdate(String contextId, String answer, String sessionModelId, String knowledgeDocumentUrl) {
        TAiUserSessionModelContext tAiUserSessionModelContext=tAiUserSessionModelContextRepository.getOne(contextId);
        tAiUserSessionModelContext.setAssistantOut(answer);
        tAiUserSessionModelContextRepository.save(tAiUserSessionModelContext);
    }


    // 线程来发送音频与参数
    class MyThread extends Thread {
        private WebSocket webSocket;

        public MyThread(WebSocket webSocket) {
            this.webSocket = webSocket;
        }

        public void run() {
            try {
                JSONObject requestJson = new JSONObject();
                JSONObject header = new JSONObject();  // header参数
                header.put("app_id",JSON.parseObject(tAiModel.getExtArgJson()).getString("app_id"));
                header.put("uid", UUID.randomUUID().toString().substring(0, 10));

                JSONObject parameter = new JSONObject(); // parameter参数
                JSONObject chat = new JSONObject();
                chat.put("domain", "generalv2");
                chat.put("temperature", 0.5);
                chat.put("max_tokens", 4096);
                parameter.put("chat", chat);

                JSONObject payload = new JSONObject(); // payload参数
                JSONObject message = new JSONObject();
                JSONArray text = new JSONArray();
                // 历史问题获取
                for (MessageDTO userChatMessage : userChatMessages) {
                    RoleContent roleContent = new RoleContent();
                    roleContent.role = userChatMessage.getRole();
                    roleContent.content = userChatMessage.getContent();
                    text.add(JSON.toJSON(roleContent));
                }

                // 最新问题
                RoleContent roleContent = new RoleContent();
                roleContent.role = "user";
                roleContent.content = StringUtils.isNotBlank(getQuestionPromptFormat())?getQuestionPromptFormat():getQuestion();
                text.add(JSON.toJSON(roleContent));

                message.put("text", text);
                payload.put("message", message);
                requestJson.put("header", header);
                requestJson.put("parameter", parameter);
                requestJson.put("payload", payload);
                // System.err.println(requestJson); // 可以打印看每次的传参明细

                //用户提问及回答结果保存 用于上下文回答
                TAiUserSessionModelContext tAiUserSessionModelContext=answeredCallObject(question,questionPromptFormat,null,sessionId, knowledgeDocumentUrl);
                setContextId(tAiUserSessionModelContext.getId());//设置上下文ID
                ConstantUtil.contextIdMap.put(sessionId,tAiUserSessionModelContext.getId());//存储上下文ID

                webSocket.send(requestJson.toString());
                // 等待服务端返回完毕后关闭
                while (true) {
                    // System.err.println(wsCloseFlag + "---");
                    Thread.sleep(200);
                    if (wsCloseFlag) {
                        break;
                    }
                }
                tAiUserSessionModelContext.setAssistantOut(getTotalAnswer());
                tAiUserSessionModelContextRepository.save(tAiUserSessionModelContext);
                webSocket.close(1000, "");
            } catch (Exception e) {
                webSocket.cancel();
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        //log.info("讯飞星火大模型返回:"+text);
        JsonParse myJsonParse = new Gson().fromJson(text, JsonParse.class);
        if (myJsonParse.header.code != 0) {
            webSocket.close(1000, "");
        }
        List<Text> textList = myJsonParse.payload.choices.text;
        String resultAnswer="";
        for (Text temp : textList) {
            resultAnswer = resultAnswer + temp.content;
        }
        setTotalAnswer(getTotalAnswer()+resultAnswer);

        if (callType == ConstantUtil.CALL_TYPE_1) {
            if(!wsCloseFlag){
                messagingTemplate.convertAndSendToUser(getUserId(), format("/chatmessage"+tAiModel.getId()+"/%s", sessionId), gson.toJson(AiOutMsgDTO.wsSuccessText(getTotalAnswer())));
            }
            if (myJsonParse.header.status == 2) {
                wsCloseFlag = true;
                //给用户返回结束回答结束动作
                messagingTemplate.convertAndSendToUser(userId, format("/chatmessage"+tAiModel.getId()+"/%s", sessionId), gson.toJson(AiOutMsgDTO.wsSuccessDone(contextId,getTotalAnswer())));

            }
        }

    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        super.onFailure(webSocket, t, response);
        try {
            if (null != response) {
                int code = response.code();
                System.out.println("onFailure code:" + code);
                System.out.println("onFailure body:" + response.body().string());
                if (101 != code) {

                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            log.error("讯飞onFailure异常",e);
        }finally {
            //给用户返回结束回答结束动作
            messagingTemplate.convertAndSendToUser(userId, format("/chatmessage" + tAiModel.getId() + "/%s", sessionId)
                    , gson.toJson(AiOutMsgDTO.wsSuccessText("<服务繁忙，请稍后再试>")));
            messagingTemplate.convertAndSendToUser(userId, format("/chatmessage" + tAiModel.getId() + "/%s", sessionId)
                    , gson.toJson(AiOutMsgDTO.wsSuccessDone(contextId,"<意外终止，请稍后重试>")));
            log.info("讯飞调用传输异常");
            wsCloseFlag=true;
        }
    }
    public void onClosed(WebSocket webSocket, int code, String reason) {
        super.onClosed(webSocket, code, reason);
        wsCloseFlag=true;
        log.info("讯飞请求结束");
    }


    //返回的json结果拆解
    class JsonParse {
        Header header;
        Payload payload;
    }

    class Header {
        int code;
        int status;
        String sid;
    }

    class Payload {
        Choices choices;
    }

    class Choices {
        List<Text> text;
    }

    class Text {
        String role;
        String content;
    }

    public class RoleContent {
        String role;
        String content;

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
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
