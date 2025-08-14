package com.zklcsoftware.aimodel.service.impl;

import cn.hutool.poi.excel.WorkbookUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionContentPart;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import com.volcengine.ark.runtime.service.ArkService;
import com.zklcsoftware.aimodel.domain.*;
import com.zklcsoftware.aimodel.dto.MessageDTO;
import com.zklcsoftware.aimodel.repository.TAiGameTplRepository;
import com.zklcsoftware.aimodel.repository.TAiSysPromptRepository;
import com.zklcsoftware.aimodel.service.*;
import com.zklcsoftware.aimodel.util.ConstantUtil;
import com.zklcsoftware.aimodel.util.TemplateService;
import com.zklcsoftware.aimodel.websocket.dto.AiOutMsgDTO;
import com.zklcsoftware.basic.util.DateUtil;
import com.zklcsoftware.basic.util.ServletUtils;
import com.zklcsoftware.basic.util.StringUtil;
import com.zklcsoftware.basic.util.UuidUtil;
import com.zklcsoftware.common.web.util.Base64Util;
import com.zklcsoftware.common.web.util.HttpClients;
import com.zklcsoftware.common.web.util.ImageUtil;
import com.zklcsoftware.doubao.signer.DoubaoHttpClients;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.io.*;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

import static java.lang.String.format;

/**
 * @author zhushaog
 * @version 1.0
 * @ClassName TongyiAiAnswerImpl.java
 * @company zklcsoftware
 * @Description 字节豆包模型回答实现
 * @createTime 2024/12/30 17:04
 */
@Service("ZjDouBaoAiAnswerImpl")
@Slf4j
public class ZjDouBaoAiAnswerImpl extends AiAnswerCallServiceImpl implements AiAnswerService {

    @Resource
    private SimpMessagingTemplate messagingTemplate;

    @Value("${uploadfiledir.uploadFilePath}")
    private String uploadFilePath;//服务器路径
    @Value("${uploadfiledir.uploadFileUrl}")
    private String uploadFileUrl;//文件封存的http地址
    @Autowired
    private TAiSysPromptService tAiSysPromptService;
    @Autowired
    private TAiModelService tAiModelService;
    @Autowired
    TAiSysPromptRepository sysPromptRepository;
    @Autowired
    TAiGameTplRepository tAiGameTplRepository;
    @Autowired
    private TAiUserSessionModelService tAiUserSessionModelService;
    @Autowired
    private TAiUserSessionService tAiUserSessionService;
    @Autowired
    TemplateService templateService;
    @Autowired
    TAiTkJsqkService tAiTkJsqkService;
    @Autowired
    TAiTkXstkService tAiTkXstkService;

    protected static Map<String,String> voiceTypeMap=new HashMap<>();//音色库
    static {

        //美式 voiceType=1
        voiceTypeMap.put("男1-0","zh_male_wennuanahu_moon_bigtts");//男，中文、美式英语    --温暖阿虎/Alvin
        voiceTypeMap.put("女1-0","zh_female_shuangkuaisisi_moon_bigtts");//女，中文、美式英语  --爽快思思/Skye
        voiceTypeMap.put("男1-1","en_male_adam_mars_bigtts");//男，中文、美式英语    --Adam
        voiceTypeMap.put("女1-1","zh_female_linjia_mars_bigtts");//女，中文、美式英语  --邻家小妹/Lily

        //英式 voiceType=2
        voiceTypeMap.put("男2-0","en_male_smith_mars_bigtts");//男，英式英语   --Smith
        voiceTypeMap.put("女2-0","en_female_anna_mars_bigtts");//女，英式英语  --Anna
        voiceTypeMap.put("男2-1","en_male_smith_mars_bigtts");//男，英式英语   --Smith
        voiceTypeMap.put("女2-1","en_female_anna_mars_bigtts");//女，英式英语  --Anna

    }

    //static ConnectionPool connectionPool = new ConnectionPool(5, 1, TimeUnit.SECONDS);
    //static Dispatcher dispatcher = new Dispatcher();

    public AiOutMsgDTO sendAnswer(TAiModel tAiModel, String question,String questionPromptFormat, String sessionId, List<MessageDTO> userChatMessages,String userId,Integer callType, String knowledgeDocumentUrl) {

        AiOutMsgDTO aiOutMsgDTO=null;
        ArkService service = ArkService.builder().apiKey(tAiModel.getAppkey()).build();
        String endpointId= JSON.parseObject(tAiModel.getExtArgJson()).getString("endpoint_id");

        final List<ChatMessage> streamMessages = new ArrayList<>();

        for (MessageDTO userChatMessage : userChatMessages) {
            final ChatMessage chatMessage =
                    ChatMessage.builder()
                    //.role(ChatMessageRole.SYSTEM)
                    .content(userChatMessage.getContent()).build();
            if(ChatMessageRole.SYSTEM.value().equals(userChatMessage.getRole())){
                chatMessage.setRole(ChatMessageRole.SYSTEM);
            }else if(ChatMessageRole.USER.value().equals(userChatMessage.getRole())){
                chatMessage.setRole(ChatMessageRole.USER);
            }else if(ChatMessageRole.ASSISTANT.value().equals(userChatMessage.getRole())){
                chatMessage.setRole(ChatMessageRole.ASSISTANT);
            }else if(ChatMessageRole.TOOL.value().equals(userChatMessage.getRole())){
                chatMessage.setRole(ChatMessageRole.TOOL);
                chatMessage.setToolCallId(userChatMessage.getToolCallId());
                chatMessage.setName(userChatMessage.getToolCallFunctionName());
            }
            streamMessages.add(chatMessage);
        }

        final ChatMessage  chatMessage = ChatMessage.builder()
                .role(ChatMessageRole.USER)
                .content(StringUtils.isNotBlank(questionPromptFormat)?questionPromptFormat:question).build();
        streamMessages.add(chatMessage);

        StringBuilder fullContent = new StringBuilder();
        StringBuilder reasoningContent = new StringBuilder();

        //用户提问及回答结果保存 用于上下文回答
        String contextId=this.answeredCall(question,questionPromptFormat, null, sessionId);
        ConstantUtil.contextIdMap.put(sessionId,contextId);//记录当前会话的最后一次上下文ID

        try {
            ChatCompletionRequest streamChatCompletionRequest = ChatCompletionRequest.builder()
                    .model(endpointId)
                    .messages(streamMessages)
                    .build();
            log.info("豆包回答开始");
            service.streamChatCompletion(streamChatCompletionRequest)
                    .doOnError(Throwable::printStackTrace)
                    .blockingForEach(
                            choice -> {
                                if (choice.getChoices().size() > 0) {
                                    //log.info("字节豆包模型回答：{}", choice.getChoices().get(0).getMessage().getContent());
                                    ChatMessage message = choice.getChoices().get(0).getMessage();
                                    // 判断是否触发深度推理，触发则打印模型输出的思维链内容
                                    if (message.getReasoningContent()!= null &&!message.getReasoningContent().isEmpty()) {
                                        if("1".equals(ConstantUtil.sysConfig.get("is_view_reason"))){//是否显示思考过程
                                            reasoningContent.append(message.getReasoningContent());
                                            messagingTemplate.convertAndSendToUser(userId, format("/chatmessage"+tAiModel.getId()+"/%s", sessionId), gson.toJson(AiOutMsgDTO.wsSuccessText("<div class=\"thought-content\"><div class=\"thought-content thought-top-title\"><h3>思考：</h3></div>"+"\n"+reasoningContent.toString()+"</div>")));
                                        }
                                    }else{
                                        fullContent.append(choice.getChoices().get(0).getMessage().getContent());
                                        if(reasoningContent.length()>0 && "1".equals(ConstantUtil.sysConfig.get("is_view_reason"))){
                                            messagingTemplate.convertAndSendToUser(userId, format("/chatmessage"+tAiModel.getId()+"/%s", sessionId), gson.toJson(AiOutMsgDTO.wsSuccessText("<div class=\"thought-content\"><div class=\"thought-content thought-top-title\"><h3>思考：</h3></div>"+"\n"+reasoningContent.toString()+"</div>"+"\n"+fullContent.toString())));
                                        }else{
                                            messagingTemplate.convertAndSendToUser(userId, format("/chatmessage"+tAiModel.getId()+"/%s", sessionId), gson.toJson(AiOutMsgDTO.wsSuccessText(fullContent.toString())));
                                        }
                                    }
                                }
                            }
                    );

            // shutdown service
            service.shutdownExecutor();

        } catch (Exception e) {
            log.error("字节豆包模型回答API异常",e);
            // shutdown service
            service.shutdownExecutor();
        }
        //用户提问及回答结果保存 用于上下文回答
        String answerContent=fullContent.toString();
        if(reasoningContent.length()>0 && "1".equals(ConstantUtil.sysConfig.get("is_view_reason")) ){
            answerContent="<div class=\"thought-content\"><div class=\"thought-content thought-top-title\"><h3>思考：</h3></div>"+"\n"+reasoningContent.toString()+"</div>"+"\n"+fullContent.toString();
        }
        this.answeredCallUpdate(contextId,answerContent, sessionId, knowledgeDocumentUrl);

        if(callType== ConstantUtil.CALL_TYPE_1){
            //给用户返回结束回答结束动作
            messagingTemplate.convertAndSendToUser(userId, format("/chatmessage"+tAiModel.getId()+"/%s", sessionId), gson.toJson(AiOutMsgDTO.wsSuccessDone(contextId,fullContent.toString())));
        }else if(callType==ConstantUtil.CALL_TYPE_2){
            aiOutMsgDTO=AiOutMsgDTO.wsSuccessDone(contextId,fullContent.toString());
        }
        log.info("豆包回答结束");
        return aiOutMsgDTO;
    }

    @Override
    public AiOutMsgDTO sendAnswerWithImages(TAiModel tAiModel, String question, String questionPromptFormat, String sessionId, List<MessageDTO> userChatMessages, String userId, List<String> images) {
        AiOutMsgDTO aiOutMsgDTO=null;
        try {

            //ArkService service = ArkService.builder().dispatcher(dispatcher).connectionPool(connectionPool).baseUrl("https://ark.cn-beijing.volces.com/api/v3").apiKey(tAiModel.getAppkey()).build();
            ArkService service = ArkService.builder().apiKey(tAiModel.getAppkey()).build();
            String endpointId= JSON.parseObject(tAiModel.getExtArgJson()).getString("sjlj_endpoint_id");

            final List<ChatMessage> streamMessages = new ArrayList<>();

            for (MessageDTO userChatMessage : userChatMessages) {
                final ChatMessage chatMessage =
                        ChatMessage.builder()
                                //.role(ChatMessageRole.SYSTEM)
                                .content(userChatMessage.getContent()).build();
                if(ChatMessageRole.SYSTEM.value().equals(userChatMessage.getRole())){
                    chatMessage.setRole(ChatMessageRole.SYSTEM);
                }else if(ChatMessageRole.USER.value().equals(userChatMessage.getRole())){
                    chatMessage.setRole(ChatMessageRole.USER);
                }else if(ChatMessageRole.ASSISTANT.value().equals(userChatMessage.getRole())){
                    chatMessage.setRole(ChatMessageRole.ASSISTANT);
                }else if(ChatMessageRole.TOOL.value().equals(userChatMessage.getRole())){
                    chatMessage.setRole(ChatMessageRole.TOOL);
                }
                streamMessages.add(chatMessage);
            }

            List<ChatCompletionContentPart> multiContent=new ArrayList<>();
            multiContent.add(ChatCompletionContentPart.builder().type("text")
                    .text(StringUtils.isNotBlank(questionPromptFormat)?questionPromptFormat:question).build());
            if(images!=null && images.size()>0){
                for (int i = 0; i < images.size(); i++) {
                    String imgUrl=images.get(i);
                    //判断imgUrl地址是否是内网地址如果是则转成base64编码
                    if(ServletUtils.isIntranetAddress(imgUrl)){
                        imgUrl=imgUrl.replace(uploadFileUrl,uploadFilePath);
                        imgUrl=Base64Util.getImageStr(imgUrl);
                    }
                    multiContent.add(ChatCompletionContentPart.builder()
                            .type("image_url")
                            .imageUrl(new ChatCompletionContentPart.ChatCompletionContentPartImageURL(imgUrl))
                            .build());
                }
            }

            final ChatMessage  chatMessage = ChatMessage.builder()
                    .role(ChatMessageRole.USER)
                    .multiContent(multiContent)
                    .build();
            streamMessages.add(chatMessage);

            /*StringBuilder fullContent = new StringBuilder();
            ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                    .model(endpointId)
                    .messages(streamMessages)
                    .build();
            service.createChatCompletion(chatCompletionRequest).getChoices().forEach(choice -> {
                        log.info("字节豆包模型回答：{}", choice.getMessage().getContent());
                        fullContent.append(choice.getMessage().getContent());
                        messagingTemplate.convertAndSendToUser(userId, format("/chatmessage"+tAiModel.getId()+"/%s", sessionId), gson.toJson(AiOutMsgDTO.wsSuccessText(choice.getMessage().getContent())));
            });*/


            StringBuilder fullContent = new StringBuilder();
            ChatCompletionRequest streamChatCompletionRequest = ChatCompletionRequest.builder()
                    .model(endpointId)
                    .messages(streamMessages)
                    .build();

            //用户提问及回答结果保存 用于上下文回答
            String contextId=this.answeredCall(question,questionPromptFormat, null, sessionId);
            ConstantUtil.contextIdMap.put(sessionId,contextId);//记录当前会话的最后一次上下文ID

            try {
                service.streamChatCompletion(streamChatCompletionRequest)
                        .doOnError(Throwable::printStackTrace)
                        .blockingForEach(
                                choice -> {
                                    if (choice.getChoices().size() > 0) {
                                        //log.info("字节豆包模型回答：{}", choice.getChoices().get(0).getMessage().getContent());
                                        fullContent.append(choice.getChoices().get(0).getMessage().getContent());
                                        messagingTemplate.convertAndSendToUser(userId, format("/chatmessage"+tAiModel.getId()+"/%s", sessionId), gson.toJson(AiOutMsgDTO.wsSuccessText(fullContent.toString())));
                                    }
                                }
                        );
            } catch (Exception e) {
                log.error("字节豆包模型回答API异常",e);
            }

            // shutdown service
            service.shutdownExecutor();
            //用户提问及回答结果保存 用于上下文回答
            this.answeredCallUpdate(contextId, fullContent.toString(),sessionId, null);
            //给用户返回结束回答结束动作
            messagingTemplate.convertAndSendToUser(userId, format("/chatmessage"+tAiModel.getId()+"/%s", sessionId), gson.toJson(AiOutMsgDTO.wsSuccessDone(contextId,fullContent.toString())));
            aiOutMsgDTO= AiOutMsgDTO.wsSuccessDone(contextId, fullContent.toString());
        } catch (Exception e) {
            log.error("字节豆包模型回答API异常",e);
        }
        return aiOutMsgDTO;
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
        AiOutMsgDTO aiOutMsgDTO = new AiOutMsgDTO();

        //文件地址
        StringBuffer filePath = new StringBuffer(uploadFilePath);
        //文件url
        StringBuffer fileUrl = new StringBuffer(uploadFileUrl);

        String AccessKeyID= JSON.parseObject(tAiModel.getExtArgJson()).getString("wst_access_key_id");
        String SecretAccessKey= JSON.parseObject(tAiModel.getExtArgJson()).getString("wst_secret_access_key");
        String url = "http://visual.volcengineapi.com?Action=CVProcess&Version=2022-08-31";
        try {
            JSONObject contJson = JSON.parseObject(content);
            JSONObject dataJson = (JSONObject) contJson.get("data");
            String text = dataJson.get("text").toString();
            int imgNum = 1;
            if(dataJson.get("imgNum")!=null){
                //imgNum = Integer.parseInt(dataJson.get("imgNum").toString());
                // 使用 Double.parseDouble 解析字符串，并强制转换为整数
                imgNum = (int) Double.parseDouble(dataJson.get("imgNum").toString());
            }

            JSONObject req=new JSONObject();
            //请求Body(查看接口文档请求参数-请求示例，将请求参数内容复制到此)
            req.put("req_key","high_aes_general_v21_L");
            req.put("prompt",text);
            req.put("model_version","general_v2.1_L");
            req.put("req_schedule_conf","general_v20_9B_pe");
            req.put("llm_seed",-1);
            req.put("seed",-1);
            req.put("scale",3.5);
            req.put("ddim_steps",25);
            req.put("width",512);
            req.put("height",512);
            req.put("use_pre_llm",true);
            req.put("use_sr",true);
            req.put("sr_seed",-1);
            req.put("sr_strength",0.4);
            req.put("sr_scale",3.5);
            req.put("sr_steps",20);
            req.put("is_only_sr",false);
            req.put("return_url",true);

            List<JsonObject> dataList = new ArrayList<>();
            for(int i=0;i<imgNum;i++) {
                String result = DoubaoHttpClients.postJson(url, req.toString(), AccessKeyID, SecretAccessKey, "cn-north-1", "cv");
                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(result, JsonObject.class);
                long code = jsonObject.get("code").getAsLong();
                if (code == 10000) {// 成功
                    JsonObject data = jsonObject.getAsJsonObject("data");
                    dataList.add(data);

                    /*JsonObject dataObj = new JsonObject();
                    dataObj.add("image_urls", data.get("image_urls"));
                    dataObj.add("llm_result", data.get("llm_result"));
                    dataObj.add("pe_result", data.get("pe_result"));

                    aiOutMsgDTO.setResponseType(2);
                    aiOutMsgDTO.setDataType("json");
                    aiOutMsgDTO.setDone(false);
                    aiOutMsgDTO.setData(dataObj.toString());
                    messagingTemplate.convertAndSendToUser(userId, format("/chatmessage" + tAiModel.getId() + "/%s", sessionId), gson.toJson(aiOutMsgDTO));

                    //文生图结果保存 用于上下文回答
                    String contextId = this.answeredCall(content, "", gson.toJson(aiOutMsgDTO), sessionId);
                    //给用户返回结束回答结束动作
                    messagingTemplate.convertAndSendToUser(userId, format("/chatmessage" + tAiModel.getId() + "/%s", sessionId), gson.toJson(AiOutMsgDTO.wsSuccessDone(contextId, gson.toJson(aiOutMsgDTO))));*/
                }
            }

            filePath.append("/ai/textToImage/");
            fileUrl.append("/ai/textToImage/");
            //如果不存在则创建
            StringUtil.createDirectory(filePath.toString());

            if(!dataList.isEmpty()) {
                JsonObject dataObj = new JsonObject();
                JsonElement llm_result = null;
                JsonElement pe_result = null;
                JsonArray image_urls = new JsonArray();
                for (JsonObject data : dataList) {
                    JsonArray imageUrlArr = (JsonArray) data.get("image_urls");
                    for (JsonElement imageUrl : imageUrlArr) {
                        //image_urls.add(imageUrl);

                        //新名称(uuid随机数加上后缀名)
                        String newfileName = UuidUtil.get32UUID() + ".jpg";

                        ImageUtil.downloadImage(imageUrl.getAsString(), filePath + newfileName);
                        ImageUtil.downloadImage(imageUrl.getAsString(), filePath + newfileName);
                        //HttpClients.getFile(imageUrl.getAsString(), filePath + newfileName);

                        image_urls.add(fileUrl+newfileName);

                    }
                    llm_result = data.get("llm_result");
                    pe_result = data.get("pe_result");
                }

                dataObj.add("image_urls", image_urls);
                dataObj.add("llm_result", llm_result);
                dataObj.add("pe_result", pe_result);

                aiOutMsgDTO.setResponseType(2);
                aiOutMsgDTO.setDataType("json");
                aiOutMsgDTO.setDone(false);
                aiOutMsgDTO.setData(dataObj.toString());
                messagingTemplate.convertAndSendToUser(userId, format("/chatmessage" + tAiModel.getId() + "/%s", sessionId), gson.toJson(aiOutMsgDTO));

                //文生图结果保存 用于上下文回答
                String contextId = this.answeredCall(content, "", gson.toJson(aiOutMsgDTO), sessionId);
                //给用户返回结束回答结束动作
                messagingTemplate.convertAndSendToUser(userId, format("/chatmessage" + tAiModel.getId() + "/%s", sessionId), gson.toJson(AiOutMsgDTO.wsSuccessDone(contextId, gson.toJson(aiOutMsgDTO))));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return aiOutMsgDTO;
    }

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

        String AccessKeyID= JSON.parseObject(tAiModel.getExtArgJson()).getString("wst_access_key_id");
        String SecretAccessKey= JSON.parseObject(tAiModel.getExtArgJson()).getString("wst_secret_access_key");

        /*
        action = "OCRNormal";          // 通用识别OCR
        action = "MultiLanguageOCR";   // 多语种OCR
        action = "BankCard";           // 银行卡识别，有v1,v2版本
        action = "IDCard";             // 身份证识别，有v1,v2版本
        action = "DrivingLicense";     // 驾驶证识别
        action = "VehicleLicense";     // 行驶证识别
        action = "OcrTaibao";          // 台湾居民来往大陆通行证识别
        action = "OcrVatInvoice";      // 增值税发票识别
        action = "OcrTaxiInvoice";     // 出租车发票识别
        action = "OcrQuotaInvoice";    // 定额发票识别
        action = "OcrTrainTicket";     // 火车票识别
        action = "OcrFlightInvoice";   // 飞机行程单识别
        action = "OcrFinance";         // 混贴报销场景
        action = "OcrRollInvoice";     // 增值税卷票识别
        action = "OcrPassInvoice";     // 高速公路过路费票识别
        action = "OcrFoodProduction";  // 食品生产许可证识别
        action = "OcrFoodBusiness";    // 食品经营许可证识别
        action = "OcrClueLicense";     // 营业执照识别
        action = "OCRTrade";           // 商标证识别
        action = "OCRRuanzhu";         // 软件著作权识别
        action = "OCRCosmeticProduct"; // 化妆品生产许可证识别
        action = "OcrSeal";            // 印章识别
        action = "OcrTextAlignment";   // 合同校验
        action = "OCRTable";           // 表格识别
        */
        JSONObject contJson = JSON.parseObject(content);
        //JSONObject dataJson = (JSONObject) contJson.get("data");
        String userfilePath = contJson.get("filePath").toString();//用户上传文件列表
        String ocrType = contJson.get("ocrType").toString();
        Gson gson = new Gson();

        JsonArray mesStringBuffer = new JsonArray();//回答结果
        AiOutMsgDTO aiOutMsgDTO = new AiOutMsgDTO();//方法返回结果
        List<String> filePaths=new ArrayList<>();//文件列表
        if(userfilePath.startsWith("[") && userfilePath.endsWith("]")){//多图片
            filePaths=gson.fromJson(userfilePath,new TypeToken<List<String>>() {}.getType());
        }else{
            filePaths.add(userfilePath);//单个图片
        }

        for (String filePath : filePaths) {
            //文件地址
            String imageBase64= Base64Util.encodeFileToBase64(uploadFilePath+filePath);

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("image_base64", imageBase64);
            //paramMap.put("image_url", imageUrl);

            if(ocrType.equals("OCRCertificate")){//证书识别==>通用识别
                ocrType = "OCRNormal";
            }else if(ocrType.equals("OCRHandwriting")){//手写识别==>通用识别
                ocrType = "OCRNormal";
            }else if(ocrType.equals("OCRStudentIDCard")){//学籍卡识别==>通用识别
                ocrType = "OCRNormal";
            }else if(ocrType.equals("OCRHouseBook")){//户口本识别==>通用识别
                ocrType = "OCRNormal";
            }else if(ocrType.equals("OCRExamination")){//试卷识别==>通用识别
                ocrType = "OCRNormal";
            }

            String url = "";
            if(ocrType.equals("OCRNormal") || ocrType.isEmpty()) {
                url = "http://visual.volcengineapi.com?Action=OCRNormal&Version=2020-08-26";
            }else if(ocrType.equals("MultiLanguageOCR")){// 多语种OCR
                url = "http://visual.volcengineapi.com?Action=MultiLanguageOCR&Version=2022-08-31";
            }else if(ocrType.equals("IDCard")){//身份证识别
                url = "http://visual.volcengineapi.com?Action=IDCard&Version=2020-08-26";
            }else if(ocrType.equals("BankCard")){//银行卡识别
                url = "http://visual.volcengineapi.com?Action=BankCard&Version=2020-08-26";
                paramMap.put("version","v2");
            }else if(ocrType.equals("OcrVatInvoice")){//增值税发票识别
                url = "http://visual.volcengineapi.com?Action=OcrVatInvoice&Version=2020-08-26";
            }


            try {
                String result = DoubaoHttpClients.post(url, paramMap, null,AccessKeyID, SecretAccessKey,"cn-north-1","cv");
                JsonObject jsonObject = gson.fromJson(result, JsonObject.class);
                long code = jsonObject.get("code").getAsLong();
                if (code==10000) {// 成功
                    JsonObject data = jsonObject.getAsJsonObject("data");
                    JsonObject dataObj = new JsonObject();
                    if(ocrType.equals("IDCard")) {//身份证
                        //dataObj.add("card_front", data.get("card_front"));
                        JsonObject cardFront = (JsonObject)data.get("card_front");
                        dataObj.add("xm", cardFront.get("name"));
                        dataObj.add("xb", cardFront.get("gender"));
                        dataObj.add("mz", cardFront.get("ethnicity"));
                        dataObj.add("csrq", cardFront.get("data_of_birth"));
                        dataObj.add("zhuzhi", cardFront.get("domicile"));
                        dataObj.add("zfzh", cardFront.get("id_number"));
                    }else if(ocrType.equals("BankCard")){//银行卡
                        //dataObj.add("bank_front", data);
                        dataObj.add("bankName", data.get("bank_name"));
                        dataObj.add("cardName", data.get("card_name"));
                        dataObj.add("cardType", data.get("card_type"));
                        dataObj.add("expiredDate", data.get("expired_date"));
                        dataObj.add("number", data.get("number"));
                    }else if(ocrType.equals("OcrVatInvoice")){//增值税发票
                        //dataObj.add("vatInvoice_texts", data);
                        JsonObject licenseMain = (JsonObject) data.get("license_main");
                        dataObj.add("fpdm", licenseMain.get("invoice_code"));//发票代码
                        dataObj.add("fpmc", licenseMain.get("invoice_name"));//发票名称
                        dataObj.add("fphm", licenseMain.get("invoice_no"));//发票号码
                        dataObj.add("kprq", licenseMain.get("invoice_date"));//开票日期
                        dataObj.add("jym", licenseMain.get("check_code"));//校验码
                        dataObj.add("jqbh", licenseMain.get("machine_num"));//机器编号
                        dataObj.add("gmfmc", licenseMain.get("buyer_name"));//购买方名称
                        dataObj.add("gmfnsrsbh", licenseMain.get("buyer_taxpayer_no"));//购买方纳税人识别号
                        dataObj.add("entry", licenseMain.get("entry"));//应税条目1 货物或应税劳务、服务名称//应税条目1 金额//应税条目1 数量//应税条目1 税额//应税条目1 税率//应税条目1 单位// 应税条目1 单价(不含税)
                        dataObj.add("zje", licenseMain.get("total_price"));//金额
                        dataObj.add("se", licenseMain.get("total_tax"));//税额
                        dataObj.add("jshjdxje", licenseMain.get("big_total_price_and_tax"));//价税合计大写金额
                        dataObj.add("jshjxxje", licenseMain.get("total_price_and_tax"));//价税合计小写金额
                        dataObj.add("xsf", licenseMain.get("seller_name"));//销售方
                        dataObj.add("xsfsbh", licenseMain.get("seller_taxpayer_no"));//销售方识别号
                        dataObj.add("xsfdzhdh", licenseMain.get("seller_address_phone"));//销售方地址和电话
                        dataObj.add("xsfkhh", licenseMain.get("seller_account"));//销售方开户行和账号
                        dataObj.add("shr", licenseMain.get("payee"));//收款人
                        dataObj.add("fh", licenseMain.get("reviewer"));//复核
                        dataObj.add("kpr", licenseMain.get("drawer")); //开票人
                        dataObj.add("sfjgz", null);//是否加盖章
                        dataObj.add("zzsfplb", null);//增值税发票类别
                    }else if(ocrType.equals("MultiLanguageOCR")){//多语种OCR
                        //dataObj.add("ocr_infos", data.get("ocr_infos"));
                        JsonArray ocrInfos = (JsonArray)data.get("ocr_infos");
                        JsonArray ocrInfoArr = new JsonArray();
                        for (JsonElement ocrInfo : ocrInfos) {
                            JsonObject ocrInfoJson = (JsonObject) ocrInfo;
                            JsonObject obj = new JsonObject();
                            obj.add("lang", ocrInfoJson.get("lang"));
                            obj.add("text", ocrInfoJson.get("text"));
                            ocrInfoArr.add(obj);
                        }
                        dataObj.add("ocr_infos", ocrInfoArr);
                    }else {
                        if("OCRCertificate".equals(contJson.get("ocrType").toString())){//证书类的支持批量上传 批量解析
                            mesStringBuffer.add(data.get("line_texts"));
                            dataObj.add("line_texts", mesStringBuffer);
                        }else{
                            dataObj.add("line_texts", data.get("line_texts"));
                        }
                    }

                    aiOutMsgDTO.setDataType("json");
                    aiOutMsgDTO.setResponseType(2);
                    aiOutMsgDTO.setDone(false);
                    aiOutMsgDTO.setData(dataObj.toString());

                }
            } catch (Exception e) {
                log.error("图片识别异常",e);
            }
        }

        if(sessionId!=null){
            messagingTemplate.convertAndSendToUser(userId, format("/chatmessage"+tAiModel.getId()+"/%s", sessionId), gson.toJson(aiOutMsgDTO));
        }

        //文字识别结果保存 用于上下文回答
        String contextId=this.answeredCall(content,"", gson.toJson(aiOutMsgDTO), sessionId);
        //给用户返回结束回答结束动作
        if(sessionId!=null) {
            messagingTemplate.convertAndSendToUser(userId, format("/chatmessage" + tAiModel.getId() + "/%s", sessionId), gson.toJson(AiOutMsgDTO.wsSuccessDone(contextId, gson.toJson(aiOutMsgDTO))));
        }

        return aiOutMsgDTO;
    }

    @Override
    public AiOutMsgDTO commentGeneration(TAiModel tAiModel, String question, String sysPromptId, String sessionId,String userId) {
        AiOutMsgDTO aiOutMsgDTO = new AiOutMsgDTO();

        JSONObject contJson = JSON.parseObject(question);
        String fileUrl = contJson.get("fileUrl").toString();
        String content = contJson.get("content").toString();

        try {
            //文件路径，替换http地址
            String filePath = uploadFilePath + fileUrl.replace(uploadFileUrl + "/","");
            File file = new File(filePath);

            //文件名称
            String strFileName = file.getName();
            if (StringUtils.isBlank(strFileName) || !(strFileName.endsWith(".xls") || strFileName.endsWith(".xlsx"))){
                System.out.println("文件格式有误，请重新选择，仅支持excel文件(*.xls,*.xlsx)");
            }

            //查询智能体信息
            TAiSysPrompt prompt = tAiSysPromptService.findById(sysPromptId);
            //用户提示词模板
            String userPromptTemplate = prompt.getUserPromptTemplate();

            //创建一个导出工作簿
            Workbook workbook;
            if (strFileName.endsWith(".xls")) {
                workbook = new HSSFWorkbook();
            } else {
                workbook = new XSSFWorkbook();
            }

            //获取文件流
            FileInputStream inputStream = new FileInputStream(file);
            //InputStream inputStream = file.getInputStream();
            Workbook book = WorkbookUtil.createBook(inputStream);
            //循环遍历工作簿中的每个工作表
            for (int i = 0; i < book.getNumberOfSheets(); i++) {
                //获取工作表
                Sheet sheetAt = book.getSheetAt(i);
                Row row = sheetAt.getRow(0);//第一行标题

                if (row == null) {
                    continue;
                }
                //导出sheet
                Sheet sheet = workbook.createSheet(String.valueOf(i));
                //创建一个新的样式
                CellStyle newStyle = workbook.createCellStyle();
                //将标题赋值到导出sheet中
                Row sheetRow = sheet.createRow(0);
                for (int j = 0; j < row.getPhysicalNumberOfCells(); j++) {
                    int columnWidth = row.getSheet().getColumnWidth(j);
                    sheet.setColumnWidth(j, columnWidth);
                    // 复制样式属性
                    newStyle.cloneStyleFrom(row.getCell(j).getCellStyle());
                    Cell cell = sheetRow.createCell(j);
                    cell.setCellValue(row.getCell(j).getStringCellValue());
                    cell.setCellStyle(newStyle);
                }
                //在标题后添加一列评语
                sheet.setColumnWidth(row.getPhysicalNumberOfCells() + 1, 256 * 20);
                sheetRow.createCell(row.getPhysicalNumberOfCells()).setCellValue("评语");

                //循环遍历行（从第二行开始）
                for (int j = 1; j < sheetAt.getPhysicalNumberOfRows(); j++) {
                    /*if(j > 11){
                        break;
                    }*/
                    Row nextRwo = sheetAt.getRow(j);//获取行
                    //导出行数据
                    Row sheetNextRow = sheet.createRow(j);
                    String rowContent = "";//行内容
                    //循环遍历列
                    for (int k = 0; k < nextRwo.getPhysicalNumberOfCells(); k++) {
                        //对应标题的单元格数据
                        String rowValue = row.getCell(k).getStringCellValue();
                        //单元格数据
                        String nextCellValue = nextRwo.getCell(k).getStringCellValue();
                        rowContent += rowContent == "" ? rowValue + ":" + nextCellValue : "," + rowValue + ":" + nextCellValue;

                        //导出的单元格数据
                        Cell cell = sheetNextRow.createCell(k);
                        cell.setCellValue(nextCellValue);
                    }
                    //将提示词中的内容替换
                    /*String promptContent = userPromptTemplate
                            .replace("${content}", rowContent == "" ? "" : rowContent)
                            .replace("${ckbz}", content == null ? "" : content);*/
                    //直调文档回答接口
                    AiOutMsgDTO answerApi = tAiModelService.answerApi(sysPromptId, "{\"content\": \""+rowContent+"\",\"ckbz\": \""+content+"\"}", userId);
                    Object data = answerApi.getData();
                    //导出的单元格数据
                    Cell cell = sheetNextRow.createCell(nextRwo.getPhysicalNumberOfCells());
                    cell.setCellValue(data.toString().replace("\"", ""));
                }
            }
            inputStream.close();//关闭输入流

            // 将新的Excel文件写入输出流
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            byte[] bytes = outputStream.toByteArray();

            // 创建目录（如果目录不存在）
            File dir = new File(uploadFilePath);
            if (!dir.exists()) {
                dir.mkdirs(); // 创建多级目录
            }

            //没有文件去创建文件
            String url = uploadFilePath + "ai/" + System.currentTimeMillis() + (strFileName.endsWith(".xls") ? ".xls" : ".xlsx");
            File file1 = new File(url);
            //File file1 = new File(filePath);
            // 检查文件是否存在，如果不存在则创建文件
            if (!file1.exists()) {
                file1.createNewFile(); // 创建文件
            }

            // 上传Excel文件到服务器
            FileOutputStream fos = new FileOutputStream(file1);
            fos.write(bytes);
            fos.close();

            //JsonObject dataObj = new JsonObject();
            //dataObj.add("fileUrl", new JsonPrimitive(uploadFilePath + strFileName.split("\\.")[0] + ".xls"));

            aiOutMsgDTO.setResponseType(2);
            aiOutMsgDTO.setDataType("json");
            aiOutMsgDTO.setDone(false);
            aiOutMsgDTO.setData(url.replace(uploadFilePath, uploadFileUrl + "/"));

            if(sessionId!=null){
                messagingTemplate.convertAndSendToUser(userId, format("/chatmessage"+tAiModel.getId()+"/%s", sessionId), gson.toJson(aiOutMsgDTO));
            }

            //文字识别结果保存 用于上下文回答
            String contextId=this.answeredCall(question,"", gson.toJson(aiOutMsgDTO), sessionId);
            //给用户返回结束回答结束动作
            if(sessionId!=null) {
                messagingTemplate.convertAndSendToUser(userId, format("/chatmessage" + tAiModel.getId() + "/%s", sessionId), gson.toJson(AiOutMsgDTO.wsSuccessDone(contextId, gson.toJson(aiOutMsgDTO))));
            }
            return AiOutMsgDTO.wsSuccessDone(contextId, gson.toJson(aiOutMsgDTO));
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public AiOutMsgDTO genGameHtml(TAiModel tAiModel, String question, String sessionModelId, String userId) {
        //查询游戏模板信息
        TAiUserSessionModel tAiUserSessionModel=tAiUserSessionModelService.findById(sessionModelId);//会话模型信息
        TAiUserSession tAiUserSession=tAiUserSessionService.findById(tAiUserSessionModel.getSessionId());//会话信息
        TAiGameTpl tAiGameTpl=tAiGameTplRepository.findBySysPromptId(tAiUserSession.getSysPromptId());//游戏模板信息
        //直调文档回答接口
        AiOutMsgDTO answerApi = tAiModelService.answerApi(tAiGameTpl.getToolsSysPromptId(), question, userId);
        JSONArray gameData = JSON.parseArray(String.valueOf(answerApi.getData()));
        Map dataMap=new HashMap();
        dataMap.put("gamedata",gameData);
        String html =templateService.generateText(tAiGameTpl.getTplContent(),dataMap);
        // 创建新的txt文件路径
        String filePath="/ai/gamehtml/"+ DateUtil.formatDateByFormat(new Date(),"yyyy-MM-dd");
        String htmlFile=sessionModelId+"_"+System.currentTimeMillis()/1000 + ".html";
        Path txtFilePath = Paths.get(uploadFilePath+filePath, htmlFile);
        try {
            if(!new File(uploadFilePath+filePath).exists()){
                new File(uploadFilePath+filePath).mkdirs();
            }
            Files.write(txtFilePath, html.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            log.error("生成html异常",e);
        }
        String htmlUrl=uploadFileUrl + "/"+filePath+"/"+htmlFile;
        Map gameDataMap=new HashMap();
        gameDataMap.put("htmlDownloadUrl",htmlUrl);
        gameDataMap.put("htmlValue",html);

        answerApi.setData(gameDataMap);
        //文字识别结果保存 用于上下文回答
        String contextId=this.answeredCall(question,"", gson.toJson(answerApi), sessionModelId);
        return AiOutMsgDTO.wsSuccessDone(contextId,gameDataMap);
    }

    @Override
    public AiOutMsgDTO dateQueryAndToEchart(TAiModel tAiModel, String question, String sysPromptId, String sessionId,String userId) {
        AiOutMsgDTO aiOutMsgDTO = new AiOutMsgDTO();
        //查询智能体信息
        TAiSysPrompt prompt = tAiSysPromptService.findById(sysPromptId);
        //用户提示词模板
        String userPromptTemplate = prompt.getUserPromptTemplate();

        //测试ddl
        String ddlTestStr =""
        		/*
        		+"CREATE TABLE `js_jichuxinxi` ("
        		+"`id` int NOT NULL AUTO_INCREMENT COMMENT 'id',"
        		+"`js_xm` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '教师姓名',"
        		+"`js_no` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '教师编号',"
        		+"`js_cjgzny` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '教师参加工作时间(2022-01)',"
        		+"`js_mz` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '教师民族(汉族、满族、回族等)',"
        		+"`js_zzmm` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '教师政治面貌(中共党员、群众、共青团员等)',"
        		+"`js_xb` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '教师性别(男、女)',"
        		+"`xx_mc` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '教师所在学校名称',"
        		+"PRIMARY KEY (`id`) USING BTREE"
        		+") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb3 COMMENT='教师基础信息表';"
        		*/
                //新+ hd_xueshengxinxi
                +"CREATE TABLE `hd_xueshengxinxi` ("
                +"`id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',"
                +"`student_global_id` varchar(32) NOT NULL COMMENT '学生标识号',"
                +"`student_no` varchar(32) DEFAULT NULL COMMENT '学生编号',"
                +"`student_name` varchar(100) DEFAULT NULL COMMENT '学生姓名',"
                +"`input_time` datetime DEFAULT NULL COMMENT '数据录入时间(2022-01-01 11:11:11)',"
                +"`update_time` datetime DEFAULT NULL COMMENT '数据更新时间(2022-01-01 11:11:11)',"
                +"`del_flag` int(11) DEFAULT NULL COMMENT '删除标记(1:启用 0:作废)',"
                +"`class_global_id` varchar(32) DEFAULT NULL COMMENT '学生所在班级标识号',"
                +"`class_name` varchar(20) DEFAULT NULL COMMENT '学生所在班级名称',"
                +"`grade_global_id` varchar(32) DEFAULT NULL COMMENT '学生所在年级标识号',"
                +"`grade_name` varchar(20) DEFAULT NULL COMMENT '学生所在年级名称',"
                +"`grade_enrollment_date` date DEFAULT NULL COMMENT '学生入学时间(2022-01)',"
                +"`grade_graduation_date` date DEFAULT NULL COMMENT '学生毕业时间(2022-01)',"
                +"`faculty_global_id` varchar(32) DEFAULT NULL COMMENT '学生所在学段标识号',"
                +"`faculty_name` varchar(64) DEFAULT NULL COMMENT '学生所在学段名称',"
                +"`faculty_time` int(11) DEFAULT NULL COMMENT '学生所在学段长度',"
                +"`district_global_id` varchar(32) DEFAULT NULL COMMENT '学生所在校区标识号',"
                +"`district_name` varchar(32) DEFAULT NULL COMMENT '学生所在校区名称',"
                +"`school_global_id` varchar(32) DEFAULT NULL COMMENT '学生所在学校标识号',"
                +"`school_name` varchar(50) DEFAULT NULL COMMENT '学生所在学校名称',"
                +"PRIMARY KEY (`id`),"
                +"KEY `datastore_collect_30_index_school_global_id` (`school_global_id`) USING BTREE"
                +") ENGINE=InnoDB AUTO_INCREMENT=393211 DEFAULT CHARSET=utf8 COMMENT='学生信息表';"
                //新+ hd_xueshengxinxi
                +"CREATE TABLE `hd_jiaoshixinxi` ("
                +"`id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',"
                +"`teacher_global_id` varchar(32) NOT NULL COMMENT '教师标识号',"
                +"`teacher_no` varchar(32) DEFAULT NULL COMMENT '教师编号',"
                +"`teacher_name` varchar(100) DEFAULT NULL COMMENT '教师姓名',"
                +"`input_time` datetime DEFAULT NULL COMMENT '数据录入时间(2022-01-01 11:11:11)',"
                +"`update_time` datetime DEFAULT NULL COMMENT '数据更新时间(2022-01-01 11:11:11)',"
                +"`del_flag` int(11) DEFAULT NULL COMMENT '删除标记(1:启用 0:作废)',"
                +"`district_global_id` varchar(32) DEFAULT NULL COMMENT '教师所在校区标识号',"
                +"`district_name` varchar(32) DEFAULT NULL COMMENT '教师所在校区名称',"
                +"`school_global_id` varchar(32) DEFAULT NULL COMMENT '教师所在学校标识号',"
                +"`school_name` varchar(50) DEFAULT NULL COMMENT '教师所在学校名称',"
                +"PRIMARY KEY (`id`),"
                +"KEY `datastore_collect_31_index_school_global_id` (`school_global_id`) USING BTREE"
                +") ENGINE=InnoDB AUTO_INCREMENT=65536 DEFAULT CHARSET=utf8 COMMENT='教师信息表';";

        try {
            //将提示词中的内容替换
            //String promptContent = userPromptTemplate
            //        .replace("${ddl_commont}", content == null ? "" : ddlTestStr)
            //        .replace("${text}", content == null ? "" : content);
            //直调文档回答接口
            AiOutMsgDTO answerApi = tAiModelService.answerApi("2c91808e93f79f9a0193fc58d96700a1", "{\"ddl_commont\": \""+ddlTestStr+"\",\"text\": \""+question+"\"}",userId);
            Object data = answerApi.getData();
            JsonObject jsonObject = new Gson().fromJson(data.toString(), JsonObject.class);

            String sqlStr = jsonObject.get("sql").toString();
            String head = jsonObject.get("head").toString();//表头
            String code = jsonObject.get("code").toString();
            if("0".equals(code)){
                //去掉字符串前后的 引号  ”
                sqlStr = sqlStr.substring(1, sqlStr.length()-1);
                List datalist = sysPromptRepository.findBySql(sqlStr, null);

                //如果是二维数组结果 且sql语句为分组统计时则使用图表展示
                if(datalist.size()>0 && datalist.get(0) instanceof Object[]){

                    //组装查询结果数据
                    StringBuffer datasets = new StringBuffer();
                    List<Object[]> list = (List<Object[]>) datalist;
                    datasets.append(head+"\n");
                    for (Object[] object : list) {
                        String[] stringArray = new String[object.length];
                        for (int i = 0; i < object.length; i++) {
                            stringArray[i] = String.valueOf(object[i]);
                        }
                        datasets.append(String.join("|", stringArray)+"\n");
                    }

                    //如果包含分组查询 则使用图表展示
                    if(sqlStr.toLowerCase().indexOf("group by") >-1){
                        //将slq提交给图表智能体制作图表html
                        AiOutMsgDTO answerApi2 = tAiModelService.answerApi("2c91808e93f79f9a0193fc58d96700b1", "{\"datasets\": \""+datasets+"\",\"text\": \""+question+"\"}",userId);
                        aiOutMsgDTO.setResponseType(2);
                        aiOutMsgDTO.setDataType("html");
                        aiOutMsgDTO.setDone(false);
                        aiOutMsgDTO.setData(answerApi2.getData());

                        if(sessionId!=null){
                            messagingTemplate.convertAndSendToUser(userId, format("/chatmessage"+tAiModel.getId()+"/%s", sessionId), gson.toJson(aiOutMsgDTO));
                        }
                        //文字识别结果保存 用于上下文回答
                        String contextId=this.answeredCall(question,"", gson.toJson(aiOutMsgDTO), sessionId);
                        //给用户返回结束回答结束动作
                        if(sessionId!=null) {
                            messagingTemplate.convertAndSendToUser(userId, format("/chatmessage" + tAiModel.getId() + "/%s", sessionId), gson.toJson(AiOutMsgDTO.wsSuccessDone(contextId, gson.toJson(aiOutMsgDTO))));
                        }
                    }else{//不包含时 则使用普通回答
                        AiOutMsgDTO answerApi2 = tAiModelService.answerApi("2c91808e9416a12101941a64e34b014", "根据用户问题："+question+",结合查询数据结果:\n "+
                                datasets
                                +"。\n以上是查询结果,整理总结出一段回答，直接输出不要重复问题本身",userId);

                        aiOutMsgDTO.setResponseType(2);
                        aiOutMsgDTO.setDataType("text");
                        aiOutMsgDTO.setDone(false);
                        aiOutMsgDTO.setData(answerApi2.getData());

                        if(sessionId!=null){
                            messagingTemplate.convertAndSendToUser(userId, format("/chatmessage"+tAiModel.getId()+"/%s", sessionId), gson.toJson(aiOutMsgDTO));
                        }

                        //文字识别结果保存 用于上下文回答
                        String contextId=this.answeredCall(question,"", gson.toJson(aiOutMsgDTO), sessionId);
                        //给用户返回结束回答结束动作
                        if(sessionId!=null) {
                            messagingTemplate.convertAndSendToUser(userId, format("/chatmessage" + tAiModel.getId() + "/%s", sessionId), gson.toJson(AiOutMsgDTO.wsSuccessDone(contextId, gson.toJson(aiOutMsgDTO))));
                        }
                    }
                }else{
                    if((datalist.size()==1 && datalist.get(0) instanceof Object)){
                        //通用总结
                        AiOutMsgDTO answerApi2 = tAiModelService.answerApi("2c91808e9416a12101941a64e34b014", "根据用户问题："+question+",结合查询数据结果:"+String.valueOf(datalist.get(0))+"。\n整理总结出一段回答，直接输出不要重复问题本身",userId);
                        aiOutMsgDTO.setResponseType(2);
                        aiOutMsgDTO.setDataType("text");
                        aiOutMsgDTO.setDone(false);
                        aiOutMsgDTO.setData(answerApi2.getData());
                    }

                    if(sessionId!=null){
                        messagingTemplate.convertAndSendToUser(userId, format("/chatmessage"+tAiModel.getId()+"/%s", sessionId), gson.toJson(aiOutMsgDTO));
                    }

                    //文字识别结果保存 用于上下文回答
                    String contextId=this.answeredCall(question,"", gson.toJson(aiOutMsgDTO), sessionId);
                    //给用户返回结束回答结束动作
                    if(sessionId!=null) {
                        messagingTemplate.convertAndSendToUser(userId, format("/chatmessage" + tAiModel.getId() + "/%s", sessionId), gson.toJson(AiOutMsgDTO.wsSuccessDone(contextId, gson.toJson(aiOutMsgDTO))));
                    }
                }
            }else{

                aiOutMsgDTO.setResponseType(2);
                aiOutMsgDTO.setDataType("text");
                aiOutMsgDTO.setDone(false);
                aiOutMsgDTO.setData("抱歉，无法回答该问题");
                if(sessionId!=null){
                    messagingTemplate.convertAndSendToUser(userId, format("/chatmessage"+tAiModel.getId()+"/%s", sessionId), gson.toJson(aiOutMsgDTO));
                }

                //文字识别结果保存 用于上下文回答
                String contextId=this.answeredCall(question,"", gson.toJson(aiOutMsgDTO), sessionId);
                //给用户返回结束回答结束动作
                if(sessionId!=null) {
                    messagingTemplate.convertAndSendToUser(userId, format("/chatmessage" + tAiModel.getId() + "/%s", sessionId), gson.toJson(AiOutMsgDTO.wsSuccessDone(contextId, gson.toJson(aiOutMsgDTO))));
                }
            }

        }catch (Exception ex){
            ex.printStackTrace();
        }
        return aiOutMsgDTO;
    }

    @Override
    public AiOutMsgDTO textToAudio(TAiModel tAiModel, String question, String sysPromptId, String sessionModelId, String userId) {

        String tts_appid= JSON.parseObject(tAiModel.getExtArgJson()).getString("tts_appid");//语音合成appid
        String tts_token= JSON.parseObject(tAiModel.getExtArgJson()).getString("tts_token");//语音合成token

        JSONObject contJson = JSON.parseObject(question);
        String text = contJson.get("text").toString();//用户提交的对话内容
        String voiceType = String.valueOf(contJson.get("voiceType"));//音色  voiceType=1 美式 voiceType=2 英式
        String voiceSpeed = String.valueOf(contJson.get("voiceSpeed"));//语速
        AiOutMsgDTO aiOutMsgDTO=tAiModelService.answerApi(sysPromptId, "{\"text\": \""+text+"\"}", userId);
        JSONArray jsonArray = JSON.parseArray(String.valueOf(aiOutMsgDTO.getData()));
        JSONObject jsonObject =null;
        List<String> outFiles=new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            jsonObject =jsonArray.getJSONObject(i);
            String sex=jsonObject.getString("sex");//性别
            String talk=jsonObject.getString("talk");//talk
            String user=jsonObject.getString("user");//用户
            String inputVoiceType=voiceTypeMap.get(sex+voiceType+"-"+user.length()%2);//选择音色库
            try {
                com.zklcsoftware.aimodel.util.TtsHttpDemo.textToAudio(tts_appid,tts_token,talk, inputVoiceType, Double.valueOf(voiceSpeed),uploadFilePath+"/ai/"+sessionModelId+"-"+i+".mp3");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            outFiles.add(uploadFilePath+"/ai/"+sessionModelId+"-"+i+".mp3");

        }
        //合并成一个mp3文件
        String filePath="/ai/"+sessionModelId+"-"+System.currentTimeMillis()+".mp3";
        String mergeFiles=uploadFilePath+filePath;
        try {
            com.zklcsoftware.aimodel.util.TtsHttpDemo.mergeFiles(outFiles,mergeFiles);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String outMp3file=uploadFileUrl+filePath;
        aiOutMsgDTO.setResponseType(2);
        aiOutMsgDTO.setDataType("text");
        aiOutMsgDTO.setDone(false);
        aiOutMsgDTO.setData(outMp3file);

        if(sessionModelId!=null){
            messagingTemplate.convertAndSendToUser(userId, format("/chatmessage"+tAiModel.getId()+"/%s", sessionModelId), gson.toJson(aiOutMsgDTO));
        }

        //文字识别结果保存 用于上下文回答
        String contextId=this.answeredCall(question,"", gson.toJson(aiOutMsgDTO), sessionModelId);
        //给用户返回结束回答结束动作
        if(sessionModelId!=null) {
            messagingTemplate.convertAndSendToUser(userId, format("/chatmessage" + tAiModel.getId() + "/%s", sessionModelId), gson.toJson(AiOutMsgDTO.wsSuccessDone(contextId, gson.toJson(aiOutMsgDTO))));
        }
        return AiOutMsgDTO.wsSuccessDone(contextId, gson.toJson(aiOutMsgDTO));

    }

    @Override
    public AiOutMsgDTO teaBaiwen(TAiModel tAiModel, String question, String sessionModelId, String userId) {

        AiOutMsgDTO returnAiOutMsgDTO=null;
        //根据用户问题匹配最合适的题号，并返回回答结果
        List<TAiTkJstk> tkJsqks=tAiTkJsqkService.findAll();
        StringBuffer tkxx=new StringBuffer();
        for (TAiTkJstk tkJsqk : tkJsqks) {
            tkxx.append(tkJsqk.getId()+". "+tkJsqk.getWt());
        }
        String xh="0";//题号
        try {
            AiOutMsgDTO aiOutMsgDTO=tAiModelService.answerApi("yc91808e93f79f9a0193fc58d1170125","{\"text\":\""+question+"\",\"tkxx:\""+tkxx.toString()+"\"}",userId);
            xh=aiOutMsgDTO.getData().toString();
        }catch (Exception e){
            log.error("提问语义解析异常，未找到匹配题号",e);
        }

        if("0".equals(xh)){//找不到对应题号 提交给大模型回答
            returnAiOutMsgDTO=this.sendAnswer( tAiModel,question,  question,  sessionModelId,new ArrayList<>(),  userId,ConstantUtil.CALL_TYPE_1,null);
        }else{//找到对应题号  直接输出回答内容
            //给用户返回结束回答结束动作
            /*returnAiOutMsgDTO=AiOutMsgDTO.wsSuccessDone("",tAiTkJsqkService.findById(Integer.parseInt(xh)).getHd());
            //文字识别结果保存 用于上下文回答
            String contextId=this.answeredCall(question,"", String.valueOf(returnAiOutMsgDTO.getData()), sessionModelId, null);
            if(sessionModelId!=null) {
                messagingTemplate.convertAndSendToUser(userId, format("/chatmessage" + tAiModel.getId() + "/%s", sessionModelId), gson.toJson(AiOutMsgDTO.wsSuccessText(String.valueOf(returnAiOutMsgDTO.getData()))));
                messagingTemplate.convertAndSendToUser(userId, format("/chatmessage" + tAiModel.getId() + "/%s", sessionModelId), gson.toJson(AiOutMsgDTO.wsSuccessDone(contextId, String.valueOf(returnAiOutMsgDTO.getData()))));
            }*/
            String answerNr="对【回答内容】进行润色总结，然后直接输出结果\n ## 回答内容\n"+tAiTkJsqkService.findById(Integer.parseInt(xh)).getHd();//总结内容
            returnAiOutMsgDTO=this.sendAnswer( tAiModel,question,  answerNr,  sessionModelId,new ArrayList<>(),  userId,ConstantUtil.CALL_TYPE_1,null);

        }
        return returnAiOutMsgDTO;
    }

    @Override
    public AiOutMsgDTO stuBaiwen(TAiModel tAiModel, String question, String sessionModelId, String userId) {
        AiOutMsgDTO returnAiOutMsgDTO=null;
        //根据用户问题匹配最合适的题号，并返回回答结果
        List<TAiTkXstk> tkJsqks=tAiTkXstkService.findAll();
        StringBuffer tkxx=new StringBuffer();
        for (TAiTkXstk tAiTkXstk : tkJsqks) {
            tkxx.append(tAiTkXstk.getId()+". "+tAiTkXstk.getWt());
        }
        String xh="0";//题号
        try {
            AiOutMsgDTO aiOutMsgDTO=tAiModelService.answerApi("yc91808e93f79f9a0193fc58d1170125","{\"text\":\""+question+"\",\"tkxx:\""+tkxx.toString()+"\"}",userId);
            xh=aiOutMsgDTO.getData().toString();
        }catch (Exception e){
            log.error("提问语义解析异常，未找到匹配题号",e);
        }

        if("0".equals(xh)){//找到对应题号  直接输出回答内容
            returnAiOutMsgDTO=this.sendAnswer( tAiModel,question,  question,  sessionModelId,new ArrayList<>(),  userId,ConstantUtil.CALL_TYPE_1,null);
        }else{
            /*//给用户返回结束回答结束动作
            returnAiOutMsgDTO=AiOutMsgDTO.wsSuccessDone("",tAiTkXstkService.findById(Integer.parseInt(xh)).getHd());
            //文字识别结果保存 用于上下文回答
            String contextId=this.answeredCall(question,"", String.valueOf(returnAiOutMsgDTO.getData()), sessionModelId, null);
            if(sessionModelId!=null) {
                messagingTemplate.convertAndSendToUser(userId, format("/chatmessage" + tAiModel.getId() + "/%s", sessionModelId), gson.toJson(AiOutMsgDTO.wsSuccessText(String.valueOf(returnAiOutMsgDTO.getData()))));
                messagingTemplate.convertAndSendToUser(userId, format("/chatmessage" + tAiModel.getId() + "/%s", sessionModelId), gson.toJson(AiOutMsgDTO.wsSuccessDone(contextId, String.valueOf(returnAiOutMsgDTO.getData()))));
            }*/
            String answerNr="对【回答内容】进行润色总结，然后直接输出结果\n ## 回答内容\n"+tAiTkJsqkService.findById(Integer.parseInt(xh)).getHd();//总结内容
            returnAiOutMsgDTO=this.sendAnswer( tAiModel,question,  answerNr,  sessionModelId,new ArrayList<>(),  userId,ConstantUtil.CALL_TYPE_1,null);
        }
        return returnAiOutMsgDTO;
    }

    /*@Override
    public String sendAnswer(TAiModel tAiModel, String question, List<MessageDTO> userChatMessages) {

        StringBuilder fullContent = new StringBuilder();
        try {
            ArkService service = ArkService.builder().apiKey(tAiModel.getAppkey()).build();
            String endpointId= JSON.parseObject(tAiModel.getExtArgJson()).getString("endpoint_id");
            final List<ChatMessage> streamMessages = new ArrayList<>();
            for (MessageDTO userChatMessage : userChatMessages) {
                final ChatMessage chatMessage =ChatMessage.builder().content(userChatMessage.getContent()).build();
                if(ChatMessageRole.SYSTEM.value().equals(userChatMessage.getRole())){
                    chatMessage.setRole(ChatMessageRole.SYSTEM);
                }else if(ChatMessageRole.USER.value().equals(userChatMessage.getRole())){
                    chatMessage.setRole(ChatMessageRole.USER);
                }else if(ChatMessageRole.ASSISTANT.value().equals(userChatMessage.getRole())){
                    chatMessage.setRole(ChatMessageRole.ASSISTANT);
                }else if(ChatMessageRole.TOOL.value().equals(userChatMessage.getRole())){
                    chatMessage.setRole(ChatMessageRole.TOOL);
                }
                streamMessages.add(chatMessage);
            }
            final ChatMessage  chatMessage = ChatMessage.builder()
                    .role(ChatMessageRole.USER)
                    .content(question).build();
            streamMessages.add(chatMessage);

            ChatCompletionRequest streamChatCompletionRequest = ChatCompletionRequest.builder()
                    .model(endpointId)
                    .messages(streamMessages)
                    .build();
            service.createChatCompletion(streamChatCompletionRequest).getChoices().forEach(choice -> fullContent.append(choice.getMessage().getContent()));
            // shutdown service
            service.shutdownExecutor();

        } catch (Exception e) {
            log.error("字节豆包模型回答API异常",e);
        }
        return fullContent.toString();
    }*/

}
