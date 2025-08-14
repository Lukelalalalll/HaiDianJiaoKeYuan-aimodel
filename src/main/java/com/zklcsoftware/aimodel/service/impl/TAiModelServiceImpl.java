package com.zklcsoftware.aimodel.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baidubce.qianfan.Qianfan;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.zhipu.oapi.ClientV4;
import com.zhipu.oapi.Constants;
import com.zhipu.oapi.service.v4.model.ChatCompletionRequest;
import com.zhipu.oapi.service.v4.model.ChatMessage;
import com.zhipu.oapi.service.v4.model.ChatMessageRole;
import com.zhipu.oapi.service.v4.model.ModelApiResponse;
import com.zklcsoftware.aimodel.domain.*;
import com.zklcsoftware.aimodel.dto.MessageDTO;
import com.zklcsoftware.aimodel.dto.TAiUserSessionDTO;
import com.zklcsoftware.aimodel.repository.TAiModelRepository;
import com.zklcsoftware.aimodel.repository.TAiUserSessionModelContextRepository;
import com.zklcsoftware.aimodel.service.*;
import com.zklcsoftware.aimodel.util.ConstantUtil;
import com.zklcsoftware.aimodel.util.FileContentReader;
import com.zklcsoftware.aimodel.util.TemplateService;
import com.zklcsoftware.aimodel.util.ZjAsrClientUtil;
import com.zklcsoftware.aimodel.vo.TAiUserSessionVO;
import com.zklcsoftware.aimodel.websocket.dto.AiInMsgDTO;
import com.zklcsoftware.aimodel.websocket.dto.AiOutMsgDTO;
import com.zklcsoftware.basic.service.impl.BaseServiceImpl;
import com.zklcsoftware.basic.util.DateUtil;
import com.zklcsoftware.common.dto.OperaResult;
import com.zklcsoftware.doubao.signer.Signer;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

import static java.lang.String.format;

@Service
@Slf4j
@Transactional
public class TAiModelServiceImpl extends BaseServiceImpl<TAiModel, String> implements TAiModelService {

    @Autowired
    TSchoolcloudSysConfService tSchoolcloudSysConfService;
    @Autowired
    TAiModelRepository aiModelRepository;
    @Autowired
    TAiUserSessionModelService tAiUserSessionModelService;
    @Autowired
    TAiUserSessionService tAiUserSessionService;
    @Autowired
    TAiSysPromptService tAiSysPromptService;
    @Autowired
    TAiUserSessionModelContextRepository tAiUserSessionModelContextRepository;
    @Autowired
    TAiWarnWordsService tAiWarnWordsService;
    @Autowired
    TAiSensitiveWordsService tAiSensitiveWordsService;
    @Autowired
    TAiModelService tAiModelService;
    private static final Gson gson = new Gson();
   @Resource
   ApplicationContext applicationContext;

   @Autowired
   TemplateService templateService;

   @Autowired
   TAiSysToolsService tAiSysToolsService;

    @Value("${uploadfiledir.uploadFilePath}")
    private String uploadFilePath;//服务器路径
    @Value("${uploadfiledir.uploadFileUrl}")
    private String uploadFileUrl;//文件封存的http地址

    @Value("${security.oauth2.client.client-id}")
    private String clientId;
    @Value("${security.oauth2.client.client-secret}")
    private String clientSecret;
    @Value("${security.oauth2.client.access-token-uri}")
    private String accessTokenUri;


    @PostConstruct
    void initSensitiveWord(){
        long startTime=System.currentTimeMillis();
        log.info("加载敏感词开始");
        //查询维护的最新的敏感词库 追加到第三方包中
        List <TAiSensitiveWords> sensitiveWords=tAiSensitiveWordsService.findAll();//查询维护的最新的敏感词库 追加到第三方包中
        List<String> sensitiveStr=new ArrayList<>();
        for (TAiSensitiveWords sensitiveWord : sensitiveWords) {
            ConstantUtil.sensitiveWordBs.addWord(sensitiveWord.getWords());
        }
        log.info("加载敏感词结束 耗时{}毫秒", System.currentTimeMillis() - startTime);

        log.info("加载预警词开始");
        //预警词刷新
        List<TAiWarnWords> warnWords=tAiWarnWordsService.findAll();
        for (TAiWarnWords warnWord : warnWords) {
            ConstantUtil.warnWords.add(warnWord.getWords());
        }
        log.info("加载预警词结束 耗时{}毫秒", System.currentTimeMillis() - startTime);
    }
    @PostConstruct
    @Scheduled(cron = "0 0/3 * * * ?")
    void init(){
        long startTime=System.currentTimeMillis();
        log.info("初始化全局数据开始");
        //初始化配置信息
        List<TSchoolcloudSysConf> tSchoolcloudSysConfs=tSchoolcloudSysConfService.findAll();
        for (TSchoolcloudSysConf tSchoolcloudSysConf : tSchoolcloudSysConfs) {
            ConstantUtil.sysConfig.put(tSchoolcloudSysConf.getConfKey(),tSchoolcloudSysConf.getConfValue());
        }
        //读取火山引擎配置中的ocr识别凭证
        TAiModel tAiModel=tAiModelService.findById("7");//读取火山引擎配置
        String wst_access_key_id= JSON.parseObject(tAiModel.getExtArgJson()).getString("wst_access_key_id");
        String wst_secret_access_key= JSON.parseObject(tAiModel.getExtArgJson()).getString("wst_secret_access_key");
        String tts_appid= JSON.parseObject(tAiModel.getExtArgJson()).getString("tts_appid");
        String tts_token= JSON.parseObject(tAiModel.getExtArgJson()).getString("tts_token");
        ConstantUtil.sysConfig.put("wst_access_key_id",wst_access_key_id);//火山引擎配置
        ConstantUtil.sysConfig.put("wst_secret_access_key",wst_secret_access_key);//火山引擎配置
        ConstantUtil.sysConfig.put("tts_appid",tts_appid);//火山引擎配置
        ConstantUtil.sysConfig.put("tts_token",tts_token);//火山引擎配置

        ConstantUtil.sysConfig.put("uploadFilePath",uploadFilePath);
        ConstantUtil.sysConfig.put("uploadFileUrl",uploadFileUrl);
        log.info("初始化全局数据结束 耗时{}毫秒", System.currentTimeMillis() - startTime);

        //定时请求智谱/文心/讯飞模型
        try {
            List<ChatMessage> messages = new ArrayList<>();
            ChatMessage chatMessage = new ChatMessage(ChatMessageRole.USER.value(), "定时请求");
            messages.add(chatMessage);
            ClientV4 client = new ClientV4.Builder("4d6a32222290fca1c89ab4259de064f9.Js7N11119ZC3DXAw")
                    .build();
            ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                    .model(Constants.ModelChatGLM4)
                    .stream(Boolean.FALSE)
                    .invokeMethod(Constants.invokeMethod)
                    .messages(messages)
                    .build();
            client.invokeModelApi(chatCompletionRequest);
        } catch (Exception e) {
            log.error("智谱地址定时请求");
        }

        try {
            // 使用安全认证AK/SK鉴权，替换下列示例中参数，安全认证Access Key替换your_iam_ak，Secret Key替换your_iam_sk
            Qianfan qianfan = new Qianfan("test", "test");
            qianfan.chatCompletion()
                    .model("ERNIE-4.0-8K").execute();
        } catch (Exception e) {
            log.error("文心定时请求");
        }

        try {
            String xfUrl="wss://spark-api.xf-yun.com";
            OkHttpClient client = new OkHttpClient.Builder().build();
            Request request = new Request.Builder().url(xfUrl).build();
            client.newCall(request);
        } catch (Exception e) {
            log.error("讯飞模型地址定时请求");
        }

        //定时获取客户端调用凭证
        //获取网盘上传token
        Map dataMap=new HashMap();
        dataMap.put("client_id", clientId);
        dataMap.put("client_secret", clientSecret);
        dataMap.put("grant_type", "client_credentials");
        String clientAccessToken = null;
        try {
            clientAccessToken = com.zklcsoftware.common.web.util.HttpClients.post(accessTokenUri, dataMap, null);
            JSONObject tokenJson = JSONObject.parseObject(clientAccessToken);
            String token = (String) tokenJson.get("access_token");
            ConstantUtil.sysConfig.put("clientAccessToken",token);
        } catch (Exception e) {
            log.error("定时获取厂商认证token异常");
        }

    }

    @Override
    public List<TAiModel> queryAiModelList() {
        List<TAiModel> aiModelList = aiModelRepository.findByStatus(1);//查询有效的模型列表
        return aiModelList;
    }

    private static Map<String,Object> fromJson(String jsonString) {
        Map<String,Object> resultMap =null;
        try {
            resultMap = gson.fromJson(jsonString, HashMap.class);
        } catch (JsonSyntaxException e) {
            log.error("解析失败");
        }
        return resultMap;
    }

    @Override
    public AiOutMsgDTO answer(String sessionModelId, String question,Integer callType) {
        TAiUserSessionModel tAiUserSessionModel=tAiUserSessionModelService.findById(sessionModelId);
        TAiUserSession tAiUserSession=tAiUserSessionService.findById(tAiUserSessionModel.getSessionId());
        //更新会话使用时间
        tAiUserSession.setUpdateTime(new Date());

        TAiModel tAiModel=this.findById(tAiUserSessionModel.getModelId());
        List<MessageDTO> messageDTOS=new ArrayList<>();
        MessageDTO systemMessage=null;
        String questionPromptFormat=null;//实际提交给大模型的用户内容
        String knowledgeDocumentUrl = null;//知识库文档路径
        /*List<TAiUserSessionModelContext> tAiUserSessionModelContexts=
                tAiUserSessionModelContextRepository.findFirst10BySessionModelIdOrderByCreateTimeDesc(sessionModelId);//查询会话的最后10条上下文列表*/
        //判断question请求格式
        AiInMsgDTO aiInMsgDTO=null;
        AiOutMsgDTO aiOutMsgDTO=null;
        try {
            aiInMsgDTO=gson.fromJson(question,AiInMsgDTO.class);
            if(aiInMsgDTO!=null && aiInMsgDTO.getData()!=null){
                question=aiInMsgDTO.getData().toString();
            }
        }catch (Exception e){
            log.error("question请求格式错误");
        }

        //自定义智能体 查询systemPrompt
        Map<String,Object> resultMap=null;
        if(StringUtils.isNotBlank(tAiUserSession.getSysPromptId())){
            TAiSysPrompt tAiSysPrompt=tAiSysPromptService.findById(tAiUserSession.getSysPromptId());

            resultMap=this.fromJson(question);

            //如果智能体有用户提示词模板内容且用户问题是文本格式(大模型pk、智能体问答功能请求) 则构造一个map解析
            if(tAiSysPrompt.getSysPromptType()==ConstantUtil.SYS_PROMPT_TYPE_2 && resultMap==null){
                resultMap=new HashMap<>();
                resultMap.put("text",question);
            }

            //循环判断属性值类型
            if(resultMap!=null){
                //判断是否存在知识库引用,如果不存在且智能体后台指定了知识库ID 则使用后台指定的知识库引用
                if(!resultMap.containsKey("knowledge_ref") && StringUtils.isNotBlank(tAiSysPrompt.getKnowledgeId())){
                    resultMap.put("knowledge_ref",tAiSysPrompt.getKnowledgeId());

                    if(tAiSysPrompt.getUseQuestionOptimize()==ConstantUtil.use_question_optimize_1){
                        //使用问题优化智能体 对用户提问优化
                        AiOutMsgDTO answerApiMsg=this.answerApi
                                ("yc91808e93f79f9a0193fc58d1170122","{\"text\":\""+resultMap.get("text")+"\",\"mbzskjj\":\""+tAiSysPrompt.getNotes()+"\"}",tAiUserSession.getUserId());
                        resultMap.put("queryOptimize",answerApiMsg.getData());//提问优化结果
                    }
                    //知识库检索 是否存在个性化配置 针对ragflow
                    if(StringUtils.isNotBlank(tAiSysPrompt.getExtJson())){
                        JSONObject configJSONObject= JSON.parseObject(tAiSysPrompt.getExtJson());
                        if(configJSONObject.getJSONObject("zsk_retrieval_config")!=null){
                            resultMap.put("zsk_retrieval_config",configJSONObject.getJSONObject("zsk_retrieval_config").toJSONString());
                        }

                        if(configJSONObject.getJSONObject("standby_zsk_retrieval_config")!=null){
                            resultMap.put("standby_zsk_retrieval_config",configJSONObject.getJSONObject("standby_zsk_retrieval_config").toJSONString());
                        }

                        if(configJSONObject.getJSONObject("file_handle_config")!=null){
                            resultMap.put("file_handle_config",configJSONObject.getJSONObject("file_handle_config").toJSONString());
                        }
                    }

                }
                resultMap=this.handelResultMap(resultMap,tAiUserSession.getUserId());
            }
            //入参不为空且存在用户提示词模板则根据模板 生成提交内容
            if( resultMap!=null  && StringUtils.isNotBlank(tAiSysPrompt.getUserPromptTemplate())){
                questionPromptFormat=templateService.generateText(tAiSysPrompt.getUserPromptTemplate(),resultMap);//根据模板内容设置 用户提示词
            }else{
                //判断是否是用户智能体且存在知识库，如果未用户提示词 使用知识库解答 ---智能体广场功能
                if(tAiSysPrompt.getKnowledgeId()!=null && tAiSysPrompt.getUserPromptTemplate()==null){
                    //如果有启用提问优化，则提交给大模型的也使用优化后的问题
                    if(resultMap.containsKey("queryOptimize")){
                        resultMap.put("text",resultMap.get("queryOptimize"));
                    }
                    questionPromptFormat=templateService.generateText(
                            "## 指引说明\n" +
                            "    1 回答时请注意保持礼貌和专业，避免给出不确定或误导性的信息。\n" +
                            "## 输出限制\n" +
                            "    1. **优先使用下面提供的【上下文信息】**来回答【用户问题】。请确保答案基于这些信息，并且尽可能详细和准确。\n" +
                            "    2. 如果提供的上下文信息不足以回答问题，请根据你自己的知识库提供一个合理的推测或解释，并告知用户这是基于现有信息的最佳猜测。\n" +
                            "    3. 列出总结回答时 使用的文本内容所引用到<文本块>的标签名，使用<div style='display:none'><引用片段></引用片段></div>包裹，" +
                                    "示例：<div style='display:none'><引用片段>文本块1,文本块2</引用片段></div>、<div style='display:none'><引用片段>无</引用片段></div> \n" +
                            "    4. <引用片段>标签放到回答结果最后显示,回答的文本段落后不显示参考的<文本块>信息，回答的结果里不要包含<文本块0>、<文本块1>这种标签内容 \n"+
                            "## 上下文信息：\n" +
                            " ${knowledge_context}\n" +
                            "## 用户问题：\n" +
                            " ${text}",resultMap);//根据模板内容设置 用户提示词
                }
            }

            systemMessage=new MessageDTO("system",tAiSysPrompt.getPromptTemplate());
            if(resultMap!=null && StringUtils.isNotBlank(tAiSysPrompt.getPromptTemplate())){
                systemMessage.setContent(templateService.generateText(tAiSysPrompt.getPromptTemplate(),resultMap));//根据模板内容 设置系统提示词
            }
            messageDTOS.add(systemMessage);
        }

        //自定义智能体 查询systemPrompt
        if(StringUtils.isNotBlank(tAiUserSession.getSysPromptId())){
            TAiSysPrompt tAiSysPrompt=tAiSysPromptService.findById(tAiUserSession.getSysPromptId());
            tAiModel=this.findById(tAiSysPrompt.getModelId());//如果是自定义智能体，则使用自定义智能体的模型

            if(StringUtils.isNotBlank(tAiSysPrompt.getCustomImplClass())){
                AiAnswerService aiAnswerService=applicationContext.getBean(tAiSysPrompt.getCustomImplClass(),AiAnswerService.class);
                String functionType = tAiSysPrompt.getFunctionType();
                if("textToImage".equals(functionType)) {//文生图
                    aiOutMsgDTO=aiAnswerService.textToImage(tAiModel, aiInMsgDTO!=null?gson.toJson(aiInMsgDTO):question, sessionModelId, tAiUserSession.getUserId());
                }else if("textRecognition".equals(functionType)){//文字识别
                    aiOutMsgDTO=aiAnswerService.textRecognition(tAiModel, question, sessionModelId, tAiUserSession.getUserId());
                }else if("sendAnswerWithImages".equals(functionType)){//文字识别
                    List<String> imageUrls=new ArrayList<>();
                    if(resultMap!=null && resultMap.containsKey("sjljImages")){
                        imageUrls=gson.fromJson(gson.toJson(resultMap.get("sjljImages")),new TypeToken<List<String>>(){}.getType());
                    }
                    aiOutMsgDTO=aiAnswerService.sendAnswerWithImages(tAiModel, question,questionPromptFormat, sessionModelId,
                            new ArrayList<MessageDTO>(){{add(new MessageDTO("system",tAiSysPrompt.getPromptTemplate()));}}, tAiUserSession.getUserId(),imageUrls);
                }else if("commentGeneration".equals(functionType)){//评语生成
                    aiOutMsgDTO=aiAnswerService.commentGeneration(tAiModel, question, "2c91808e93f79f9a0193fc58d9670012", sessionModelId, tAiUserSession.getUserId());
                }else if("dateQueryAndToEchart".equals(functionType)){
                    aiOutMsgDTO=aiAnswerService.dateQueryAndToEchart(tAiModel, question,"2c91808e93f79f9a0193fc58d96700a1", sessionModelId, tAiUserSession.getUserId());
                }else if("textToAudio".equals(functionType)){
                    aiOutMsgDTO=aiAnswerService.textToAudio(tAiModel, resultMap!=null?gson.toJson(resultMap):question,"2c91808e93f79f9a0193fc57904d00aa", sessionModelId, tAiUserSession.getUserId());
                }else if("genGameHtml".equals(functionType)){
                    aiOutMsgDTO=aiAnswerService.genGameHtml(tAiModel, resultMap!=null?gson.toJson(resultMap):question, sessionModelId, tAiUserSession.getUserId());
                }else if("teaBaiwen".equals(functionType)){
                    aiOutMsgDTO=aiAnswerService.teaBaiwen(tAiModel, question, sessionModelId, tAiUserSession.getUserId());
                }else if("stuBaiwen".equals(functionType)){
                    aiOutMsgDTO=aiAnswerService.stuBaiwen(tAiModel, question, sessionModelId, tAiUserSession.getUserId());
                }
            }else{

                //倒序放入到messageDTOS
                if(ConstantUtil.CALL_TYPE_2!=callType && tAiSysPrompt.getUseContext()!=null && tAiSysPrompt.getUseContext()==1){

                    List<TAiUserSessionModelContext> tAiUserSessionModelContexts=
                            tAiUserSessionModelContextRepository.queryBySessionModelId(sessionModelId,tAiSysPrompt.getUseContextMaxcount()==null?ConstantUtil.DEFAULT_TOPN:tAiSysPrompt.getUseContextMaxcount());//查询会话的最后5条上下文列表

                    for (int i = tAiUserSessionModelContexts.size() - 1; i >= 0; i--) {
                        messageDTOS.add(MessageDTO.builder()
                                .role("user")
                                .content(StringUtils.isNotBlank(tAiUserSessionModelContexts.get(i).getUserInPromptFormat())?tAiUserSessionModelContexts.get(i).getUserInPromptFormat():tAiUserSessionModelContexts.get(i).getUserIn())
                                .build());
                        messageDTOS.add(MessageDTO.builder()
                                .role("assistant")
                                .content(StringUtils.isNotBlank(tAiUserSessionModelContexts.get(i).getUserInPromptFormat())?tAiUserSessionModelContexts.get(i).getUserInPromptFormat():tAiUserSessionModelContexts.get(i).getUserIn())
                                .build());
                    }
                }

                if(questionPromptFormat==null){
                    questionPromptFormat=question;
                }
                //判断是否存在引用的附件信息作为参考资料，如果存在 则调整提示词追加参考资料
                if(StringUtils.isNotBlank(tAiSysPrompt.getBakreply())){//存在知识库 且存在兜底回复是触发
                    questionPromptFormat=questionPromptFormat+"\n## 兜底回复(现有知识无法解答用户问题时输出一下语句)\n" +tAiSysPrompt.getBakreply();
                }

                if(tAiSysPrompt.getIsUseSzr()==ConstantUtil.STATE_1 && tAiSysPrompt.getKnowledgeId()!=null){//存在知识库 且启用数字人的话  则追加生成概述提示词
                    questionPromptFormat=questionPromptFormat+"\n## 输出要求\n 最先输出一个不超过100字的总结，然后再输出正文,总结内容由特定div包裹，格式如下<div class='gs' style='display:none'>总结内容</div>";
                }

                if(resultMap!=null && resultMap.containsKey("knowledge_document_url")){
                    knowledgeDocumentUrl = String.valueOf(resultMap.get("knowledge_document_url"));
                }

                AiAnswerService aiAnswerService=applicationContext.getBean(tAiModel.getImplClass(),AiAnswerService.class);
                if("RagflowAgentAnswerImpl".equals(tAiModel.getImplClass())){//如果是ragflow 则调用ragflow的agent服务，则设置下agentId
                    tAiModel.setAgentId(tAiSysPrompt.getAgentId());
                    tAiModel.setAgentInputArgs(resultMap);//用户页面输入参数 传入agent中
                    if(tAiSysPrompt.getSysPromptType()==ConstantUtil.SYS_PROMPT_TYPE_2){
                        tAiModel.setIsIgnoreQuestion(false);
                    }else {
                        tAiModel.setIsIgnoreQuestion(true);
                    }

                }

                //判断是否启用了function_call功能，如果启用则先调用function_call功能
                List<MessageDTO> toolsCallResult=new ArrayList<>();//function call调用结果
                if(tAiSysPrompt.getIsUseTools()!=null && tAiSysPrompt.getIsUseTools()==ConstantUtil.USE_AI_TOOLS_1){
                    toolsCallResult=tAiSysToolsService.bigModelFunctionCall(tAiModel,tAiSysPrompt,question, tAiUserSession.getUserId(),tAiUserSession.getStudentId());
                }
                if(toolsCallResult!=null && toolsCallResult.size()>0){
                    messageDTOS.addAll(toolsCallResult);//合并工具调用结果，根据工具结果回复用户
                    aiOutMsgDTO=aiAnswerService.sendAnswer(tAiModel, question, question+"\n## 当前时间:\n" + DateUtil.formatDateByFormat(new Date(), "yyyy-MM-dd HH:mm:ss"), sessionModelId, messageDTOS, tAiUserSession.getUserId(),callType, null);
                }else{
                    aiOutMsgDTO=aiAnswerService.sendAnswer(tAiModel, question, questionPromptFormat+"\n## 当前时间:\n" + DateUtil.formatDateByFormat(new Date(), "yyyy-MM-dd HH:mm:ss"), sessionModelId, messageDTOS, tAiUserSession.getUserId(),callType, knowledgeDocumentUrl);
                }

            }

        }else {
            List<TAiUserSessionModelContext> tAiUserSessionModelContexts= tAiUserSessionModelContextRepository.queryBySessionModelId(sessionModelId,ConstantUtil.DEFAULT_TOPN);//查询会话的最后5条上下文列表
            //倒序放入到messageDTOS
            if(ConstantUtil.CALL_TYPE_2!=callType){
                for (int i = tAiUserSessionModelContexts.size() - 1; i >= 0; i--) {
                    messageDTOS.add(MessageDTO.builder()
                            .role("user")
                            .content(StringUtils.isNotBlank(tAiUserSessionModelContexts.get(i).getUserInPromptFormat())?tAiUserSessionModelContexts.get(i).getUserInPromptFormat():tAiUserSessionModelContexts.get(i).getUserIn())
                            .build());
                    messageDTOS.add(MessageDTO.builder()
                            .role("assistant")
                            .content(StringUtils.isNotBlank(tAiUserSessionModelContexts.get(i).getUserInPromptFormat())?tAiUserSessionModelContexts.get(i).getUserInPromptFormat():tAiUserSessionModelContexts.get(i).getUserIn())
                            .build());
                }
            }
            //大模型PK是消息体判断
            resultMap=this.fromJson(question);
            //循环判断属性值类型
            if(resultMap!=null){
                resultMap=this.handelResultMap(resultMap,tAiUserSession.getUserId());
            }

            //判断是否存在引用的附件信息作为参考资料，如果存在 则调整提示词追加参考资料
            if(resultMap!=null && resultMap.containsKey("knowledge_context")){//针对大模型pk场景 上传资料问答问题
                questionPromptFormat=String.valueOf(resultMap.get("text"))+"\n## 参考资料：\n" +resultMap.get("knowledge_context");
            }else{
                questionPromptFormat=String.valueOf(resultMap.get("text"));
            }
            if(resultMap!=null && resultMap.containsKey("knowledge_document_url")){
                knowledgeDocumentUrl = String.valueOf(resultMap.get("knowledge_document_url"));
            }

            AiAnswerService aiAnswerService=applicationContext.getBean(tAiModel.getImplClass(),AiAnswerService.class);
            aiOutMsgDTO=aiAnswerService.sendAnswer(tAiModel, question, questionPromptFormat, sessionModelId, messageDTOS, tAiUserSession.getUserId(),callType, knowledgeDocumentUrl);
        }
        return aiOutMsgDTO;
    }
    /**
     * @Description 处理map内容
     * @Author zhushaog
     * @param: resultMap
     * @UpdateTime 2025/1/10 18:03
     * @throws
     */
    private Map handelResultMap(Map<String, Object> resultMap,String userId) {
        Map<String, Object> resultMapNew=new HashMap(resultMap);

        // 使用forEach方法
        resultMap.forEach((key, value) -> {
            //判断key是否存在类型定义，如果存在按类型定义转换成文本，如果不存在则跳过处理
            if(resultMap.containsKey(key+"_type")){
                String filePath=uploadFilePath+value.toString().replace(uploadFileUrl,"");
                if("audio".equals(resultMap.get(key+"_type"))){//音频文件
                    try {
                        resultMapNew.put(key, ZjAsrClientUtil.audio2text(filePath));
                    } catch (Exception e) {
                        log.error("请求内容音频文件读取异常",e);
                    }
                }else if("word".equals(resultMap.get(key+"_type"))){//文本文件
                    try {
                        File file=new File(filePath);
                        Map hanleFileConfigMap=null;
                        if(resultMap.containsKey("zsk_retrieval_config")){
                            hanleFileConfigMap.putAll(JSONObject.parseObject(String.valueOf(resultMap.get("zsk_retrieval_config")), Map.class));
                        }
                        String reusltText=FileContentReader.readFileContent(file,hanleFileConfigMap);
                        resultMapNew.put(key, reusltText);//解析结果
                    } catch (Exception e) {
                        log.error("请求内容文本文件读取异常",e);
                    }
                }else if("image".equals(resultMap.get(key+"_type"))){//图片识别
                    try {
                        filePath=value.toString().replace(uploadFileUrl,"");//图片解析
                        String imageOcrJson="{\"filePath\":\""+filePath+"\",\"ocrType\":\"OCRNormal\"}";
                        AiOutMsgDTO aiOutMsgDTO=this.answerApi("2c91808e9416a12101941a64e34b01ax",imageOcrJson,userId);//使用图片识别智能体  获取图片文字内容
                        Map dataMap=gson.fromJson(aiOutMsgDTO.getData().toString(), Map.class);
                        resultMapNew.put(key, dataMap.get("line_texts").toString());
                    } catch (Exception e) {
                        log.error("请求内容文本文件读取异常",e);
                    }
                }else if("fileList".equals(resultMap.get(key+"_type"))){//附件集合识别
                    try {
                        JSONArray jsonArrays=JSON.parseArray(String.valueOf(value));//附件集合解析
                        StringBuffer stringBuffer=new StringBuffer();
                        for (int i = 0; i < jsonArrays.size(); i++) {
                            JSONObject jsonObject=jsonArrays.getJSONObject(i);
                            if(jsonObject!=null && jsonObject.containsKey("file") && jsonObject.containsKey("file_type")){
                                String fileType=jsonObject.getString("file_type");//获取文件分类，根据分类做不同处理
                                String file=jsonObject.getString("file").replace(uploadFileUrl,"");//获取文件存储路径
                                if("word".equals(fileType)){//文本文件
                                    try {
                                        Map hanleFileConfigMap=null;
                                        if(resultMap.containsKey("zsk_retrieval_config")){
                                            hanleFileConfigMap.putAll(JSONObject.parseObject(String.valueOf(resultMap.get("zsk_retrieval_config")), Map.class));
                                        }
                                        stringBuffer.append(FileContentReader.readFileContent(new File(file),hanleFileConfigMap)+"\n");
                                    } catch (Exception e) {
                                        log.error("请求内容文本文件读取异常",e);
                                    }
                                }else if("image".equals(fileType)){//图片识别
                                    try {
                                        String imageOcrJson="{\"filePath\":\""+file+"\",\"ocrType\":\"OCRNormal\"}";
                                        AiOutMsgDTO aiOutMsgDTO=this.answerApi("2c91808e9416a12101941a64e34b01ax",imageOcrJson,userId);//使用图片识别智能体  获取图片文字内容
                                        Map dataMap=gson.fromJson(aiOutMsgDTO.getData().toString(), Map.class);
                                        stringBuffer.append(dataMap.get("line_texts").toString()+"\n");
                                    } catch (Exception e) {
                                        log.error("请求内容文本文件读取异常",e);
                                    }
                                }

                            }
                        }
                        if(StringUtils.isBlank(stringBuffer)){
                            resultMapNew.remove(key);
                        }else{
                            resultMapNew.put(key, stringBuffer.toString());//参考资料
                        }
                    } catch (Exception e) {
                        log.error("请求内容文本文件读取异常",e);
                    }
                }
            }
            //处理知识库引用信息（根据引用查询相关知识库片段）
            //http://192.168.100.34:3001/api/v1/workspace/yt_school_zsk/vector-search
            if("knowledge_ref".equals(key)){
                if("ragflow".equals(ConstantUtil.sysConfig.get("knowledge_type"))){
                    String knowledgeRef=String.valueOf(resultMap.get("knowledge_ref"));
                    String knowledgeApi=ConstantUtil.sysConfig.get("knowledge_apiurl_ragflow")+"/api/v1/retrieval";
                    Map queryMap=new HashMap();
                    if(resultMap.containsKey("queryOptimize")){
                        queryMap.put("question",resultMap.get("queryOptimize"));
                    }else{
                        queryMap.put("question",resultMap.get("text"));
                    }
                    if(resultMap.containsKey("zsk_retrieval_config")){//按该智能体独立配置的知识库检索
                        queryMap.putAll(JSONObject.parseObject(String.valueOf(resultMap.get("zsk_retrieval_config")), Map.class));
                    }else {//按全局配置的相似度阈值、topN进行检索
                        queryMap.put("similarity_threshold",ConstantUtil.sysConfig.get("knowledge_score_threshold"));
                        queryMap.put("vector_similarity_weight",ConstantUtil.sysConfig.get("knowledge_score_threshold"));
                        queryMap.put("page",1);
                        queryMap.put("page_size",ConstantUtil.sysConfig.get("knowledge_topN"));
                    }

                    queryMap.put("dataset_ids",Arrays.asList(new String[]{knowledgeRef}));//知识库ID

                    Map headerMap=new HashMap();
                    headerMap.put("Authorization","Bearer "+ConstantUtil.sysConfig.get("knowledge_apikey_ragflow"));
                    headerMap.put("Content-Type","application/json");
                    StringBuffer stringBuffer=new StringBuffer();
                    String documentUrls ="";
                    try {
                        String res= com.zklcsoftware.common.web.util.HttpClients.postJson(knowledgeApi,JSON.toJSONString(queryMap),headerMap);
                        if(StringUtils.isNotBlank(res)){
                            List<Object> set = new ArrayList<>();
                            JSONObject resJsonObject=JSON.parseObject(res);//检索结果
                            if(resJsonObject!=null && resJsonObject.containsKey("code") && 0==resJsonObject.getInteger("code") && resJsonObject.getJSONObject("data")!=null){
                                for (int i = 0; i < resJsonObject.getJSONObject("data").getJSONArray("chunks").size(); i++) {
                                    JSONObject jsonObject=resJsonObject.getJSONObject("data").getJSONArray("chunks").getJSONObject(i);
                                    stringBuffer.append("<文本块"+i+">\n"+jsonObject.getString("content")+"\n</文本块"+i+">"+"\n");//引用片段内容
                                    set.add(jsonObject.getString("document_id"));//引用文档ID
                                }
                                for (Object o : set){
                                    documentUrls += o +",";
                                }
                            }

                        }
                        //判断是否存在备用检索(主检索未搜到情况的),如果存在且主检索搜索结果为空的情况下启用备用检索
                        if(StringUtils.isBlank(stringBuffer) && resultMap.containsKey("standby_zsk_retrieval_config")){
                            //备用检索
                            queryMap.putAll(JSONObject.parseObject(String.valueOf(resultMap.get("standby_zsk_retrieval_config")), Map.class));//
                            String standbyRes= com.zklcsoftware.common.web.util.HttpClients.postJson(knowledgeApi,JSON.toJSONString(queryMap),headerMap);
                            if(StringUtils.isNotBlank(res)){
                                List<Object> set = new ArrayList<>();
                                JSONObject resJsonObject=JSON.parseObject(standbyRes);//检索结果
                                if(resJsonObject!=null && resJsonObject.containsKey("code") && 0==resJsonObject.getInteger("code") && resJsonObject.getJSONObject("data")!=null){
                                    for (int i = 0; i < resJsonObject.getJSONObject("data").getJSONArray("chunks").size(); i++) {
                                        JSONObject jsonObject=resJsonObject.getJSONObject("data").getJSONArray("chunks").getJSONObject(i);
                                        stringBuffer.append("<文本块"+i+">\n"+jsonObject.getString("content")+"\n</文本块"+i+">"+"\n");//引用片段内容
                                        set.add(jsonObject.getString("document_id"));//引用文档ID
                                    }
                                    for (Object o : set){
                                        documentUrls += o +",";
                                    }
                                }
                            }
                        }

                        resultMapNew.put("knowledge_context", stringBuffer.toString());
                        resultMapNew.put("knowledge_document_url", documentUrls);

                    } catch (Exception e) {
                        log.error("知识库检索异常",e);
                        resultMapNew.put("websearch_context", stringBuffer.toString());
                        resultMapNew.put("knowledge_document_url", documentUrls);
                    }
                }else{
                    String knowledgeRef=String.valueOf(resultMap.get("knowledge_ref"));
                    String knowledgeApi=ConstantUtil.sysConfig.get("knowledge_apiurl")+"/api/v1/workspace/"+knowledgeRef+"/vector-search";
                    Map queryMap=new HashMap();
                    if(resultMap.containsKey("queryOptimize")){
                        queryMap.put("query",resultMap.get("queryOptimize"));
                    }else{
                        queryMap.put("query",resultMap.get("text"));
                    }
                    queryMap.put("topN",ConstantUtil.sysConfig.get("knowledge_topN"));
                    queryMap.put("scoreThreshold",ConstantUtil.sysConfig.get("knowledge_score_threshold"));

                    Map headerMap=new HashMap();
                    headerMap.put("Authorization","Bearer "+ConstantUtil.sysConfig.get("knowledge_apikey"));
                    headerMap.put("Content-Type","application/json");
                    StringBuffer stringBuffer=new StringBuffer();
                    String documentUrls ="";
                    try {
                        String res= com.zklcsoftware.common.web.util.HttpClients.postJson(knowledgeApi,JSON.toJSONString(queryMap),headerMap);
                        if(StringUtils.isNotBlank(res)){
                            List<Object> set = new ArrayList<>();
                            for (int i = 0; i < JSON.parseObject(res).getJSONArray("results").size(); i++) {
                                JSONObject jsonObject=JSON.parseObject(res).getJSONArray("results").getJSONObject(i);
                                JSONObject metadataJson = (JSONObject) jsonObject.get("metadata");
                                if(metadataJson !=null){
                                    //documentUrls += metadataJson.getString("url")+",";
                                    set.add(metadataJson.getString("url"));
                                }
                                stringBuffer.append("<文本块"+i+">\n"+jsonObject.getString("text")+"\n</文本块"+i+">"+"\n");//引用片段内容
                            }
                            for (Object o : set){
                                documentUrls += o +",";
                            }
                        }
                        resultMapNew.put("knowledge_context", stringBuffer.toString());
                        resultMapNew.put("knowledge_document_url", documentUrls);
                    } catch (Exception e) {
                        log.error("知识库检索异常",e);
                        resultMapNew.put("websearch_context", stringBuffer.toString());
                        resultMapNew.put("knowledge_document_url", documentUrls);
                    }
                }

            }

            //处理联网搜索信息（根据联网搜索信息查询相关知识库片段）
            //https://api.bochaai.com/v1/web-search
            if("isNetSearch".equals(key) && Boolean.valueOf(String.valueOf(resultMap.get("isNetSearch")))){//联网搜索
                StringBuffer stringBuffer=new StringBuffer();
                try {
                    Map queryMap=new HashMap();
                    queryMap.put("query",resultMap.get("text"));
                    queryMap.put("summary",true);
                    String res= this.websearchApi(queryMap);
                    /*String webSearchApi=ConstantUtil.sysConfig.get("bocha-web-search_apiurl")+"/v1/web-search";
                    Map headerMap=new HashMap();
                    headerMap.put("Authorization","Bearer "+ConstantUtil.sysConfig.get("knowledge_apikey"));
                    headerMap.put("Content-Type","application/json");
                    headerMap.put("Host", "api.bochaai.com");
                    String res= com.zklcsoftware.common.web.util.HttpClients.postJson(webSearchApi,JSON.toJSONString(queryMap),headerMap);*/

                    if(StringUtils.isNotBlank(res) && JSON.parseObject(res)!=null
                            && JSON.parseObject(res).getJSONObject("data")!=null
                            && JSON.parseObject(res).getJSONObject("data").getJSONObject("webPages")!=null
                            && JSON.parseObject(res).getJSONObject("data").getJSONObject("webPages").getJSONArray("value")!=null){
                       JSONArray jsonArray= JSON.parseObject(res).getJSONObject("data").getJSONObject("webPages").getJSONArray("value");
                        for (int i = 0; i < jsonArray.size(); i++) {
                            JSONObject jsonObject=jsonArray.getJSONObject(i);
                            stringBuffer.append(jsonObject.getString("summary"));
                        }
                    }
                    resultMapNew.put("websearch_context", stringBuffer.toString());
                } catch (Exception e) {
                    log.error("联网搜索异常",e);
                    resultMapNew.put("websearch_context", stringBuffer.toString());
                }
            }
        });
        return resultMapNew;
    }
    /**
     * @Description 博查联网搜索内容接口调用
     * @Author zhushaog
     * @param: message
     * @UpdateTime 2025/2/28 11:34
     * @return: java.lang.String
     * @throws
     */
    private String websearchApi(Map message) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        String webSearchApi=ConstantUtil.sysConfig.get("bocha-web-search_apiurl")+"/v1/web-search";
        // 构建请求头
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", String.format("Bearer %s", ConstantUtil.sysConfig.get("bocha-web-search_apikey")));
        // 创建HttpEntity包装请求头和请求体
        org.springframework.http.HttpEntity<Map<String, Object>> requestEntity = new org.springframework.http.HttpEntity<>(message, headers);
        // 设置请求的URL
        URL url = new URL(webSearchApi);
        // 发送POST请求
        ResponseEntity<String> response = restTemplate.postForEntity(url.toString(), requestEntity, String.class);
        String responseBody =null;
        // 检查响应状态码
        if (response.getStatusCode() == HttpStatus.OK) {
            responseBody = response.getBody();
        }
        return responseBody;
    }

    @Override
    public AiOutMsgDTO textToImage(String aiSessionId, String content) {
        List<TAiUserSessionModel> tAiUserSessionModels=tAiUserSessionModelService.findBySessionId(aiSessionId);
        AiOutMsgDTO aiOutMsgDTO = new AiOutMsgDTO();
        if(!tAiUserSessionModels.isEmpty()) {
            TAiUserSessionModel tAiUserSessionModel = tAiUserSessionModels.get(0);
            TAiUserSession tAiUserSession = tAiUserSessionService.findById(tAiUserSessionModel.getSessionId());
            TAiModel tAiModel = this.findById(tAiUserSessionModel.getModelId());

            AiAnswerService aiAnswerService = applicationContext.getBean(tAiModel.getImplClass(), AiAnswerService.class);
            aiOutMsgDTO = aiAnswerService.textToImage(tAiModel, content, aiSessionId, tAiUserSession.getUserId());
        }
        return aiOutMsgDTO;
    }

    @Override
    public AiOutMsgDTO textRecognition(String aiSessionId, String content) {
        List<TAiUserSessionModel> tAiUserSessionModels=tAiUserSessionModelService.findBySessionId(aiSessionId);
        AiOutMsgDTO aiOutMsgDTO = new AiOutMsgDTO();
        if(!tAiUserSessionModels.isEmpty()) {
            TAiUserSessionModel tAiUserSessionModel = tAiUserSessionModels.get(0);
            TAiUserSession tAiUserSession = tAiUserSessionService.findById(tAiUserSessionModel.getSessionId());
            TAiModel tAiModel = this.findById(tAiUserSessionModel.getModelId());

            AiAnswerService aiAnswerService = applicationContext.getBean(tAiModel.getImplClass(), AiAnswerService.class);
            aiOutMsgDTO = aiAnswerService.textRecognition(tAiModel, content, aiSessionId, tAiUserSession.getUserId());
        }
        return aiOutMsgDTO;
    }

    @Override
    public AiOutMsgDTO answerApi(String sysPromptId, String question,String userId) {
        return this.answerApi( sysPromptId,null,  question, userId);
    }

    public AiOutMsgDTO answerApi(String sysPromptId,String sessionModelId, String question,String userId) {
        TAiUserSessionDTO tAiUserSessionDTO=TAiUserSessionDTO.builder().sysPromptId(sysPromptId).name(question.length()>30?question.substring(0,30):question).build();
        if(StringUtils.isNotBlank(sessionModelId)){
            return this.answer(sessionModelId,question,ConstantUtil.CALL_TYPE_2);
        }else{
            TAiUserSessionVO tAiUserSessionVO=tAiUserSessionService.createAiSession(tAiUserSessionDTO,userId,null,null,null,null);
            return this.answer(tAiUserSessionVO.getTAiUserSessionlModels().get(0).getId(),question,ConstantUtil.CALL_TYPE_2);
        }
    }

    @Override
    public AiOutMsgDTO textToAudio(String content, String voiceType) {
        AiOutMsgDTO aiOutMsgDTO = new AiOutMsgDTO();

        /*String fileUrl = uploadFilePath + System.currentTimeMillis() + ".mp3";
        try {
            TTSWebsocketDemo.textToAudio(content,voiceType,fileUrl);
        } catch (Exception e) {
            log.error("调用火山引擎音频生成API异常",e);
        }*/

        try {
            // 调用火山引擎音频生成API
            String AccessKeyID= "-k2EQonrX5WcS0Or2slpegAS47Wmx4vb";
            String SecretAccessKey= "GSJEToREPh453Z6vjEknCwp9KpiVZ9it";
            String url = "https://openspeech.bytedance.com/api/v1/tts_async/submit";

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("appid","1034542277");
            jsonObject.put("text",content);
            jsonObject.put("format","mp3");
            jsonObject.put("voice_type",voiceType);
            jsonObject.put("sample_rate",24000);
            jsonObject.put("volume",1.2);
            jsonObject.put("speed",0.9);
            jsonObject.put("pitch",1.1);
            jsonObject.put("enable_subtitle",1);

            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost request = new HttpPost();
            request.setURI(new URI(url));
            request.addHeader(HttpHeaders.USER_AGENT, "curl/7.54.00");
            request.addHeader("Resource-Id", "volc.tts_async.default");
            request.addHeader("Authorization", "Bearer;" + AccessKeyID);

            Signer signer = new Signer();
            // 设置json数据
            StringEntity strentity = new StringEntity(jsonObject.toString(), "utf-8");// 解决中文乱码问题
            strentity.setContentEncoding("UTF-8");
            strentity.setContentType("application/json");
            request.setEntity(strentity);

            /* launch request */
            CloseableHttpResponse response = httpClient.execute(request);

            /* status code */
            System.out.println(response.getStatusLine().getStatusCode());   // 200

            /* get response body */
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String result = EntityUtils.toString(entity);
                System.out.println(result);
            }
            /* close resources */
            response.close();
            httpClient.close();
            //312b0a8b-d306-4703-8e61-3c2e64994ec2
        }catch (Exception e){
            e.printStackTrace();
        }


        /*aiOutMsgDTO.setResponseType(2);
        aiOutMsgDTO.setDataType("json");
        aiOutMsgDTO.setDone(false);
        aiOutMsgDTO.setData(fileUrl.replace(uploadFilePath, uploadFileUrl + "/"));*/
        return null;
    }

}
