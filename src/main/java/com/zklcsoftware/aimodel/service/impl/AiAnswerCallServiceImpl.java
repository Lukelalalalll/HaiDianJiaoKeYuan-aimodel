package com.zklcsoftware.aimodel.service.impl;

import com.fasterxml.jackson.databind.node.BooleanNode;
import com.github.houbb.sensitive.word.api.IWordDeny;
import com.google.gson.Gson;
import com.zklcsoftware.aimodel.domain.*;
import com.zklcsoftware.aimodel.dto.MessageDTO;
import com.zklcsoftware.aimodel.repository.TAiUserSessionModelContextFileindexRepository;
import com.zklcsoftware.aimodel.repository.TAiUserSessionModelContextKeysRepository;
import com.zklcsoftware.aimodel.repository.TAiUserSessionModelContextRepository;
import com.zklcsoftware.aimodel.service.AiAnswerCallService;
import com.zklcsoftware.aimodel.service.AiAnswerService;
import com.zklcsoftware.aimodel.service.TAiUserSessionModelService;
import com.zklcsoftware.aimodel.service.TAiWarnWordsContextRefService;
import com.zklcsoftware.aimodel.util.ConstantUtil;
import com.zklcsoftware.aimodel.util.IKAnalyzerUtil;
import com.zklcsoftware.aimodel.vo.TAiUserSessionModelContextFileindexVO;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zhushaog
 * @version 1.0
 * @ClassName AiAnswerService.java
 * @company zklcsoftware
 * @Description AI文档回答
 * @createTime 2024/08/23 16:56
 */
@Service
public class AiAnswerCallServiceImpl implements AiAnswerCallService {

    @Autowired
    TAiUserSessionModelContextRepository tAiUserSessionModelContextRepository;
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    TAiUserSessionModelContextKeysRepository tAiUserSessionModelContextKeysRepository;
    @Autowired
    TAiWarnWordsContextRefService tAiWarnWordsContextRefService;
    @Autowired
    private TAiUserSessionModelContextFileindexRepository tAiUserSessionModelContextFileindexRepository;
    @Autowired
    private TAiUserSessionModelService tAiUserSessionModelService;

    protected static Gson gson=new Gson();

    /**
     * @Description 大模型回答完毕后 记录上下文信息到数据库
     * @Author zhushaog
     * @param: question
     * @param: answer
     * @param: sessionModelId
     * @param: knowledgeDocumentUrl
     * @UpdateTime 2024/10/19 11:53
     * @throws
     */

    @Override
    public String answeredCall(String question, String questionPromptFormat, String answer, String sessionModelId) {

        TAiUserSessionModelContext tAiUserSessionModelContext=TAiUserSessionModelContext.builder()
                .createTime(new Date())
                .assistantOut(answer)
                .userIn(question)
                .userInPromptFormat(StringUtils.isNotBlank(questionPromptFormat)?questionPromptFormat:question)
                .sessionModelId(sessionModelId)
                .build();
        tAiUserSessionModelContextRepository.save(tAiUserSessionModelContext);

        //将提问信息分词处理 保存到数据库
        try {
            List<String> results= IKAnalyzerUtil.getIKAnalyzerResult(question);
            for (String result : results) {
                tAiUserSessionModelContextKeysRepository.save(TAiUserSessionModelContextKeys.builder()
                        .sessionContextId(tAiUserSessionModelContext.getId())
                        .keyWord(result)
                        .createTime(new Date())
                        .build());

                //检测是否触发预警词
                Boolean checkWarnWordResult=checkWarnWord(result);
                if(checkWarnWordResult){
                    tAiWarnWordsContextRefService.save(TAiWarnWordsContextRef.builder()
                            .words(result)
                            .contextId(tAiUserSessionModelContext.getId())
                            .createTime(new Date())
                            .build());
                }

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return tAiUserSessionModelContext.getId();


    }

    @Override
    public void answeredCallUpdate(String contextId, String answer, String sessionModelId, String knowledgeDocumentUrl) {
        TAiUserSessionModelContext tAiUserSessionModelContext=tAiUserSessionModelContextRepository.getOne(contextId);
        tAiUserSessionModelContext.setAssistantOut(answer);
        tAiUserSessionModelContextRepository.save(tAiUserSessionModelContext);


        if(StringUtils.isNotBlank(knowledgeDocumentUrl) && answer.indexOf("<引用片段>")>-1 && answer.indexOf("</引用片段>")>-1){
            List<TAiUserSessionModelContextFileindex> fileindexList=new ArrayList<>();
            TAiUserSessionModel sessionModel = tAiUserSessionModelService.findById(sessionModelId);

            // 定义正则表达式
            String regex = "<引用片段>(.*?)</引用片段>";

            // 编译正则表达式
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(answer);
            String findText = "";
            // 查找匹配的内容
            while (matcher.find()) {
                findText=findText+","+matcher.group(1);
            }
            if(StringUtils.isNotBlank(findText)){
                List<String> findTextArr=Arrays.asList(findText.split(","));
                String[] split = knowledgeDocumentUrl.split(",");
                for (int i = 0; i < split.length; i++) {
                    if(findTextArr.contains("文本块"+i)){//包含该文本块
                        TAiUserSessionModelContextFileindex fileindex = new TAiUserSessionModelContextFileindex().builder()
                                .documentUrl(split[i])
                                .sessionId(sessionModel.getSessionId())
                                .sessionModelId(sessionModelId)
                                .sessionContextId(tAiUserSessionModelContext.getId())
                                .createTime(new Date())
                                .build();
                        tAiUserSessionModelContextFileindexRepository.save(fileindex);
                    }
                }
            }

        }

    }

    /**
     * @Description 预警词校验
     * @Author zhushaog
     * @param: question
     * @UpdateTime 2024/11/6 18:31
     * @return: java.lang.String
     * @throws
     */
    private Boolean checkWarnWord(String cutWord){
        return ConstantUtil.warnWords.contains(cutWord);
    }
}
