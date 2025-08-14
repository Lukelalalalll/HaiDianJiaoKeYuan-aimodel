package com.zklcsoftware.aimodel.service.impl;

import com.alibaba.fastjson.JSON;
import com.volcengine.ark.runtime.model.completion.chat.*;
import com.volcengine.ark.runtime.service.ArkService;
import com.zklcsoftware.aimodel.domain.TAiModel;
import com.zklcsoftware.aimodel.domain.TAiSysPrompt;
import com.zklcsoftware.aimodel.domain.TAiSysTools;
import com.zklcsoftware.aimodel.dto.MessageDTO;
import com.zklcsoftware.aimodel.repository.TAiSysToolsRepository;
import com.zklcsoftware.aimodel.service.TAiSysToolsService;
import com.zklcsoftware.aimodel.util.ConstantUtil;
import com.zklcsoftware.basic.service.impl.BaseServiceImpl;
import com.zklcsoftware.basic.util.DateUtil;
import com.zklcsoftware.common.web.util.HttpClients;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
@Slf4j
public class TAiSysToolsServiceImpl extends BaseServiceImpl<TAiSysTools, String> implements TAiSysToolsService {

    @Autowired
    TAiSysToolsRepository tAiSysToolsRepository;
    @Override
    public List<MessageDTO> bigModelFunctionCall(TAiModel tAiModel, TAiSysPrompt tAiSysPrompt, String userQuestion,String userId,String studentId) {

        List<MessageDTO>  toolsMessageDTOs=new ArrayList<>();//工具响应结果
        String apiKey = tAiModel.getAppkey();//apiKey
        ArkService service = ArkService.builder().apiKey(apiKey).build();//创建服务
        String endpointId= JSON.parseObject(tAiModel.getExtArgJson()).getString("endpoint_id");//字节推理点

        log.info("\n----- function call request -----");
        final List<ChatMessage> messages = new ArrayList<>();
        String systemPrompt = tAiSysPrompt.getPromptTemplate();//系统提示词

        final ChatMessage sysMessage = ChatMessage.builder()
                .role(ChatMessageRole.SYSTEM).content(systemPrompt)
                .build();
        final ChatMessage userMessage = ChatMessage.builder()
                .role(ChatMessageRole.USER)
                .content("## 用户问题:\n" +userQuestion+"## 当前时间:\n" + DateUtil.formatDateByFormat(new Date(), "yyyy-MM-dd HH:mm:ss"))
                .build();
        messages.add(sysMessage);
        messages.add(userMessage);

        List<TAiSysTools> tAiSysTools=this.queryAiSysTools(tAiSysPrompt.getId());
        List<ChatTool> tools = new ArrayList<>();
        for (TAiSysTools tAiSysTool : tAiSysTools) {
            tools.add(new ChatTool(
                    "function",
                    new ChatFunction.Builder()
                            .name(tAiSysTool.getServerUrl()+tAiSysTool.getMethodName())
                            .description(tAiSysTool.getToolDescription())
                            .parameters(new Questtion(
                                    "object",
                                    /*new HashMap<String, Object>() {{
                                        put("userId", new HashMap<String, String>() {{
                                            put("type", "string");
                                            put("description", "当前用户在数字校园中的标识");
                                        }});
                                    }}*/
                                    null,
                                    Collections.singletonList("th")
                            ))
                            .build()
            ));
        }
        //工具调用
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(endpointId)
                .messages(messages)
                .tools(tools)
                .build();
        service.createChatCompletion(chatCompletionRequest).getChoices().forEach(
                choicex -> {
                    if(choicex.getMessage().getToolCalls()!=null){
                        choicex.getMessage().getToolCalls().forEach(
                                toolCall -> {
                                    String content = execFunctionCall(toolCall, userId,studentId);//工具调用结果
                                    if(StringUtils.isNotBlank(content)){
                                        //工具调用结果封装到回答上下文中
                                        toolsMessageDTOs.add(MessageDTO.builder().role("tool")
                                                .content(content)
                                                .toolCallId(toolCall.getId())
                                                .toolCallFunctionName(toolCall.getFunction().getName())
                                                .build());
                                    }
                                });
                    }
                }
        );
        service.shutdownExecutor();
        return toolsMessageDTOs;
    }

    @Override
    public List<TAiSysTools> queryAiSysTools(String sysPromptId) {
        return tAiSysToolsRepository.queryAiSysTools(sysPromptId);
    }

    //远程函数调用
    private String execFunctionCall(ChatToolCall toolCall,String userId,String studentId) {
        Map params=new HashMap();
        params.put("userId", userId);//约定传入用户ID
        params.put("studentId", studentId);//约定传入学生ID
        params.put("access_token", ConstantUtil.sysConfig.get("clientAccessToken"));//业务系统统一使用认证中心
        String result = null;
        try {
            result = HttpClients.post(toolCall.getFunction().getName(), params);
        } catch (Exception e) {
           log.error(toolCall.getFunction().getName()+"  fc调用异常",e);
        }
        return result;
    }

    public static class Questtion {
        public String type;
        public Map<String, Object> properties;
        public List<String> required;

        public Questtion(String type, Map<String, Object> properties, List<String> required) {
            this.type = type;
            this.properties = properties;
            this.required = required;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Map<String, Object> getProperties() {
            return properties;
        }

        public void setProperties(Map<String, Object> properties) {
            this.properties = properties;
        }

        public List<String> getRequired() {
            return required;
        }

        public void setRequired(List<String> required) {
            this.required = required;
        }

        public String queryAnswer(Integer th,String wt){
            return "答案";
        }

    }
}
