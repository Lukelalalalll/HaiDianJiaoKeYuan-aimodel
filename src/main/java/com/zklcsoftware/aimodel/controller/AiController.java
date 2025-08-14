package com.zklcsoftware.aimodel.controller;

import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.zklcsoftware.aimodel.domain.*;
import com.zklcsoftware.aimodel.dto.*;
import com.zklcsoftware.aimodel.service.*;
import com.zklcsoftware.aimodel.util.*;
import com.zklcsoftware.aimodel.vo.*;
import com.zklcsoftware.aimodel.websocket.dto.AiOutMsgDTO;
import com.zklcsoftware.basic.util.DateUtil;
import com.zklcsoftware.common.dto.OperaResult;
import com.zklcsoftware.common.web.ExtBaseController;
import com.zklcsoftware.common.web.util.HttpClients;
import com.zklcsoftware.common.web.util.MD5Util;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.http.client.methods.HttpGet;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * @author zhushaog
 * @version 1.0
 * @className HomeController
 * @description
 * @date 2024/8/5 14:10
 **/
@Slf4j
@Controller
@Api(tags = "AI回答逻辑处理")
@RequestMapping(path = {"", "/api"})
public class AiController extends ExtBaseController {
    @Autowired
    TAiModelService aiModelService;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    TAiUserSessionModelService tAiUserSessionModelService;
    @Autowired
    TAiUserSessionModelContextService tAiUserSessionModelContextService;
    @Autowired
    TAiUserSessionService tAiUserSessionService;
    @Autowired
    TAiUserPromptService tAiUserPromptService;
    @Autowired
    TAiSysPromptService tAiSysPromptService;
    @Autowired
    TSchoolcloudSysDictionaryService tSchoolcloudSysDictionaryService;
    @Autowired
    CommentGenerationService commentGenerationService;

    @Autowired DateQueryAndToEchartService echartService;
    @Autowired
    TAiModelService tAiModelService;
    @Value("${sys.netname}")
    private String netname;
    @Value("${security.oauth2.client.client-id}")
    private String clientId;
    @Value("${security.oauth2.client.user-authorization-uri}")
    private String userAuthorizationUri;
    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Autowired
    private TAiAttachmentService tAiAttachmentService;
    @Autowired
    private TAiUserSessionModelContextFileindexService tAiUserSessionModelContextFileindexService;
    @Autowired
    private OAuth2ClientContext oauth2Context;//单点登录业务系统里引入这个bean


    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    // corePoolSize 核心线程数，保留的线程池大小
    // maximumPoolSize 线程池的最大大小
    // keepAliveTime 空闲线程结束的超时时间
    // workQueue 存放任务的队列
    private static ThreadPoolExecutor executor = null;
    static {
        executor=new ThreadPoolExecutor(20, 50, 30, TimeUnit.MINUTES,new LinkedBlockingQueue<Runnable>(50));
    }
    //大模型任务列表
    private static Map<String,Future>  taskMap = new HashMap<>();

    @ApiOperation(value = "获取类型字典", notes = "获取类型字典")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "dictType", paramType = "query",required = true, value = "字典类型值  410智能体分类   411问题分类", dataType = "Integer"),
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String")
    })
    @PostMapping(value = {"/queryDictList"})
    @ResponseBody
    public OperaResult queryDictList(@RequestParam(required = true) Integer dictType) {
        return OperaResult.putSuccessResult("dictData", tSchoolcloudSysDictionaryService.queryDictList(dictType));
    }

    @ApiOperation(value = "获取用户权限", notes = "获取用户权限")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String")
    })
    @ResponseBody
    @PostMapping(value = {"/getUserAuth"})
    public OperaResult getUserAuth() {

        OperaResult result=OperaResult.putSuccessResult("isAdmin",false);
        result.getData().put("userId",this.getUserGuid());//用户ID
        result.getData().put("uname",this.getUName());//用户姓名
        //result.getData().put("sessionId",this.getRequest().getSession().getId());//当前页面会话ID
        result.getData().put("userTypeCode",this.getUserTypeCode());//用户类型  101002-教师 101003-家长  101004-学生  101007-平台管理员
        result.getData().put("is_view_znt",ConstantUtil.sysConfig.get("is_view_znt"));//是否显示智能体(0不显示  1显示  默认显示)
        result.getData().put("is_createppt",ConstantUtil.sysConfig.get("is_createppt"));//是否开启生成ppt功能
        if (getUsePost().indexOf("系统管理员") != -1 || (ConstantUtil.USER_TYPE_101007+"").equals(getUserTypeCode())) {//职务是系统管理员 或者用户类型是101007平台admin 则赋予管理权限
            result.getData().put("isAdmin",true);
        }
        result.getData().put("bus_app_names",ConstantUtil.sysConfig.get("bus_app_names"));//自定义系统名称
        result.getData().put("access_token",oauth2Context.getAccessToken().getValue());//当前登录用户的token

        if("教师".equals(this.getUserType())){
            result.getData().put("aiMenus",ConstantUtil.sysConfig.get("TEA_AI_MENU"));
        }else if("学生".equals(this.getUserType())){
            result.getData().put("aiMenus",ConstantUtil.sysConfig.get("STU_AI_MENU"));
        }else if("家长".equals(this.getUserType())){
            result.getData().put("aiMenus",ConstantUtil.sysConfig.get("PARENT_AI_MENU"));
        }

        return result;
    }

    @ApiOperation(value = "App获取用户权限", notes = "App获取用户权限")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String")
    })
    @ResponseBody
    @PostMapping(value = {"/appGetUserAuth"})
    public OperaResult appGetUserAuth() {

        OperaResult result=OperaResult.putSuccessResult("isAdmin",false);
        result.getData().put("userId",this.getUserGuid());//用户ID
        result.getData().put("uname",this.getUName());//用户姓名
        result.getData().put("userTypeCode",this.getUserTypeCode());//用户类型  101002-教师 101003-家长  101004-学生  101007-平台管理员
        result.getData().put("is_view_znt",ConstantUtil.sysConfig.get("is_view_znt"));//是否显示智能体(0不显示  1显示  默认显示)
        result.getData().put("is_createppt",ConstantUtil.sysConfig.get("is_createppt"));//是否开启生成ppt功能
        result.getData().put("bus_app_names",ConstantUtil.sysConfig.get("bus_app_names"));//自定义系统名称

        if (getUsePost().indexOf("系统管理员") != -1 || (ConstantUtil.USER_TYPE_101007+"").equals(getUserTypeCode())) {//职务是系统管理员 或者用户类型是101007平台admin 则赋予管理权限
            result.getData().put("isAdmin",true);
        }

        String confValue = "";
        if("教师".equals(this.getUserType())){
            confValue = ConstantUtil.sysConfig.get("APP_TEA_AI_MENU");
        }else if("学生".equals(this.getUserType())){
            confValue = ConstantUtil.sysConfig.get("APP_STU_AI_MENU");
        }else if("家长".equals(this.getUserType())){
            confValue = ConstantUtil.sysConfig.get("APP_PARENT_AI_MENU");
        }
        JSONArray jsonArray = JSONArray.parseArray(confValue);
        for(int i = 0; i < jsonArray.size(); i++){

            //生成随机字符串
            Random random = new Random();
            StringBuilder sb = new StringBuilder(4);
            for (int j = 0; j < 4; j++) {
                int index = random.nextInt(CHARACTERS.length());
                sb.append(CHARACTERS.charAt(index));
            }
            //img拼接处理
            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
            String imgClickLink = (String) jsonObject.get("imgClickLink");
            String imgUrlPrefix = (String) jsonObject.get("imgUrlPrefix");
            try {
                imgClickLink = userAuthorizationUri + "?client_id=" + clientId + "&redirect_uri=" + URLEncoder.encode(netname + contextPath + imgClickLink, "UTF-8") + "&response_type=code&state=" + sb.toString();
                imgUrlPrefix = netname + contextPath + imgUrlPrefix;
                jsonObject.put("imgClickLink", imgClickLink);
                jsonObject.put("imgUrlPrefix", imgUrlPrefix);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        result.getData().put("aiMenus",jsonArray);
        return result;
    }

    /*@ApiOperation(value = "跳转到AI体验页面", notes = "跳转到AI体验页面")
    @GetMapping(value = {"/","/home"})
    public String home(Model model, HttpServletRequest request) {
        model.addAttribute("userId",this.getUserGuid());
        model.addAttribute("modelSessionId","4028e4a292a419500192a41bd1330001");
        return "dphome";
    }*/

    @ApiOperation(value = "查询模型列表", notes = "查询模型列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String")
    })
    @PostMapping("/queryAiModelList")
    @ResponseBody
    public OperaResult queryAiModelList() {
        List<TAiModel> aiModelList=aiModelService.queryAiModelList();
        List<TAiModelVO> aiModelVOS=new ArrayList();
        for (TAiModel tAiModel : aiModelList) {
            aiModelVOS.add(TAiModelVO.builder().id(tAiModel.getId())
                    .name(tAiModel.getName()).explain(tAiModel.getExplain()).iconLogo(tAiModel.getIconLogo()).proImg(tAiModel.getProImg()).build());
        }
        return OperaResult.putSuccessResult("models",aiModelVOS);
    }

    @ApiOperation(value = "AI提问-提交敏感词校验", notes = "AI提问-提交敏感词校验")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "question", paramType = "query", value = "问题内容", dataType = "String"),
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String")
    })
    @ResponseBody
    @PostMapping("/checkAnswer")
    public OperaResult checkAnswer(String question) {
        OperaResult operaResult=new OperaResult();
        String checkResult=checkSensitiveWord(question);//敏感词校验结果
        if (StringUtils.isNotBlank(checkResult)) {
            return OperaResult.putFailResult("warn","提问中包含敏感词:"+checkResult);
        }
        return operaResult;
    }
    /**
     * @Description 敏感词校验
     * @Author zhushaog
     * @param: question
     * @UpdateTime 2024/11/6 18:31
     * @return: java.lang.String
     * @throws
     */
    private String checkSensitiveWord(String question){
        String result=null;
        /*if (StringUtils.isNotBlank(question)) {
            if (ConstantUtil.sensitiveWordBs.contains(question)) {
                // 替换敏感词
                log.info("敏感词替换，敏感词为："+ ConstantUtil.sensitiveWordBs.findFirst(question));
                result= ConstantUtil.sensitiveWordBs.findFirst(question);
            }
        }*/
        return result;
    }

    @ApiOperation(value = "AI提问", notes = "AI提问")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "modelSessionId", paramType = "query", value = "会话标识", dataType = "String"),
            @ApiImplicitParam(name = "question", paramType = "query", value = "问题内容", dataType = "String"),
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String")
    })
    @ResponseBody
    @PostMapping("/answer")
    public OperaResult answer(String modelSessionId,String question, HttpServletRequest request) {

        OperaResult operaResult=new OperaResult();
        String checkResult=checkSensitiveWord(question);//敏感词校验结果
        if (StringUtils.isNotBlank(checkResult)) {
            return OperaResult.putFailResult("warn","提问中包含敏感词:"+checkResult);
        }

        String dqrq=new SimpleDateFormat("yyyyMMdd").format(new Date());
        Object apis=redisTemplate.opsForValue().get(ConstantUtil.API_THRESHOLD_KEY+ dqrq+":" +this.getUserGuid());
        if(apis==null){
            redisTemplate.opsForValue().set(ConstantUtil.API_THRESHOLD_KEY+dqrq+":"+this.getUserGuid(),0, 24, TimeUnit.HOURS);
            redisTemplate.opsForValue().increment(ConstantUtil.API_THRESHOLD_KEY+dqrq+":"+this.getUserGuid(),1);
            apis="0";//初始为
        }
        //校验用户当日api调用次数
        if(ConstantUtil.sysConfig.containsKey(this.getUserTypeCode()+"_API_THRESHOLD")
                && Integer.parseInt(String.valueOf(apis))>Integer.parseInt(ConstantUtil.sysConfig.get(this.getUserTypeCode()+"_API_THRESHOLD"))){
            operaResult.setResultCode(com.zklcsoftware.common.web.util.ConstantUtil.OPT_RESULT_CODE_FAIL);
            operaResult.setResultDesc("今日次数已用完！");
            log.error("触发api次数阈值{}",this.getUserLoginName());
        }else{
            operaResult.setResultCode(com.zklcsoftware.common.web.util.ConstantUtil.OPT_RESULT_CODE_SUCCESS);
            //加入到后台安装的线程池中等待执行
            Future<?> future =executor.submit(new Runnable() {
                @Override
                public void run() {
                    aiModelService.answer(modelSessionId,question,ConstantUtil.CALL_TYPE_1);
                }
            });
            taskMap.put(modelSessionId,future);//放入到全局变量中，用于停止回答
            redisTemplate.opsForValue().increment(ConstantUtil.API_THRESHOLD_KEY+dqrq+":"+this.getUserGuid(),1);
        }

        return operaResult;
    }

    @ApiOperation(value = "终止会话", notes = "终止会话")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "modelSessionId", paramType = "query", value = "会话标识", dataType = "String"),
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String")
    })
    @ResponseBody
    @PostMapping("/stopAnswer")
    public OperaResult stopAnswer(String modelSessionId) {
        //终止正在执行中会话
        Future future=taskMap.get(modelSessionId);
        if(future!=null){
            future.cancel(true);
        }
        return OperaResult.putSuccessResult("success",true);
    }


    @ApiOperation(value = "保存终止会话内容", notes = "保存终止会话内容")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "modelSessionId", paramType = "query", value = "会话标识", dataType = "String"),
            @ApiImplicitParam(name = "answer", paramType = "query", value = "回复内容", dataType = "String"),
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String")
    })
    @ResponseBody
    @PostMapping("/saveStopAnswer")
    public OperaResult saveStopAnswer(String modelSessionId,String answer) {
        if(ConstantUtil.contextIdMap.containsKey(modelSessionId)){
            TAiUserSessionModelContext  tAiUserSessionModelContext=tAiUserSessionModelContextService
                    .findById(ConstantUtil.contextIdMap.get(modelSessionId));
            tAiUserSessionModelContext.setAssistantOut(answer);
            tAiUserSessionModelContextService.save(tAiUserSessionModelContext);
        }
        return OperaResult.putSuccessResult("success",true);
    }

    @ApiOperation(value = "编辑修改AI回复内容", notes = "编辑修改AI回复内容")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "contextId", paramType = "query", value = "上下文ID", dataType = "String"),
            @ApiImplicitParam(name = "answer", paramType = "query", value = "回复内容", dataType = "String"),
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String")
    })
    @ResponseBody
    @PostMapping("/editAiAnswer")
    public OperaResult editAiAnswer(String contextId,String answer) {
        TAiUserSessionModelContext  tAiUserSessionModelContext=tAiUserSessionModelContextService
                .findById(contextId);
        tAiUserSessionModelContext.setAssistantOut(answer);
        tAiUserSessionModelContextService.save(tAiUserSessionModelContext);
        return OperaResult.putSuccessResult("success",true);
    }

    @ApiOperation(value = "AI提问", notes = "AI提问")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "sysPromptId", paramType = "query", value = "智能体ID", dataType = "String"),
            @ApiImplicitParam(name = "modelSessionId", paramType = "query", value = "会话标识", dataType = "String"),
            @ApiImplicitParam(name = "question", paramType = "query", value = "问题内容", dataType = "String"),
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String")
    })
    @ResponseBody
    @PostMapping("/answerApi")
    public OperaResult answerApi(String sysPromptId,String question, HttpServletRequest request,String modelSessionId) {
        return OperaResult.putSuccessResult("aiOutMsg",aiModelService.answerApi(sysPromptId,modelSessionId,question, this.getUserGuid()));
    }

    @ApiOperation(value = "获取提示词列表", notes = "获取提示词列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tsc", paramType = "query", value = "搜索提示词名称", dataType = "String"),
            @ApiImplicitParam(name = "busType", paramType = "query", value = "提示词分类值", dataType = "Integer"),
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String")
    })
    @ResponseBody
    @PostMapping("/queryTscList")
    public OperaResult queryTscList(String tsc,Integer busType) {
        Pageable pageable =PageRequest.of(0,1000);//默认查询1000条
        return OperaResult.putSuccessResult("tscList",tAiUserPromptService.queryTscList(tsc,busType,pageable).getContent());
    }

    @ApiOperation(value = "获取智能体列表", notes = "获取智能体列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "znt", paramType = "query", value = "搜索智能体名称", dataType = "String"),
            @ApiImplicitParam(name = "busType", paramType = "query", value = "智能体分类值", dataType = "Integer"),
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String")
    })
    @ResponseBody
    @PostMapping("/queryZntList")
    public OperaResult queryZntList(String znt,Integer busType) {
        Pageable pageable =PageRequest.of(0,1000);//默认查询1000条

        List<TAiSysPromptVO> zntList = tAiSysPromptService.queryZntList(znt, busType, pageable, null, ConstantUtil.PUBLISH_STATUS_1, ConstantUtil.REVIEW_STATUS_1, this.getUserGuid()).getContent();
        for(TAiSysPromptVO tAiSysPromptVO : zntList){
            //生成随机字符串
            Random random = new Random();
            StringBuilder sb = new StringBuilder(4);
            for (int j = 0; j < 4; j++) {
                int index = random.nextInt(CHARACTERS.length());
                sb.append(CHARACTERS.charAt(index));
            }
            //组装地址
            String url = netname + contextPath + "/index.html#/appZntDialogDetail?zntId=" + tAiSysPromptVO.getId();
            String appUrl = null;
            try {
                appUrl = userAuthorizationUri + "?client_id=" + clientId + "&redirect_uri=" + URLEncoder.encode(url, "UTF-8") + "&response_type=code&state=" + sb.toString();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            tAiSysPromptVO.setAppUrl(appUrl);
        }
        return OperaResult.putSuccessResult("zntList", zntList);
    }

    @ApiOperation(value = "查询历史会话", notes = "查询历史会话")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String")
    })
    @ResponseBody
    @PostMapping("/queryAiUserSessions")
    public OperaResult queryAiUserSessions(@RequestBody(required = true) SessionlQueryDTO sessionlQueryVO) {
        String userId=this.getUserGuid();//当前用户标识
        List<TAiUserSessionVO> aiUserSessionVOList = tAiUserSessionService.queryAiUserSessions(sessionlQueryVO, userId);
        for(TAiUserSessionVO list:aiUserSessionVOList){
            //生成随机字符串
            Random random = new Random();
            StringBuilder sb = new StringBuilder(4);
            for (int j = 0; j < 4; j++) {
                int index = random.nextInt(CHARACTERS.length());
                sb.append(CHARACTERS.charAt(index));
            }
            //组装地址
            String url = netname + contextPath + "/index.html#/appZntDialogDetail?zntId=" + list.getSysPromptId();
            String appUrl = null;
            try {
                appUrl = userAuthorizationUri + "?client_id=" + clientId + "&redirect_uri=" + URLEncoder.encode(url, "UTF-8") + "&response_type=code&state=" + sb.toString();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            list.setAppUrl(appUrl);
        }
        return OperaResult.putSuccessResult("sessionList", aiUserSessionVOList);
    }

    @ApiOperation(value = "查询所有历史会话分页", notes = "查询所有历史会话分页")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String"),
            @ApiImplicitParam(name = "layoutId", paramType = "query", value = "布局id", dataType = "int"),
            @ApiImplicitParam(name = "page", paramType = "query", value = "当前页码", dataType = "int"),
            @ApiImplicitParam(name = "size", paramType = "query", value = "每页条数", dataType = "int")
    })
    @ResponseBody
    @PostMapping("/queryAllAiUserSessionsPage")
    public OperaResult queryAllAiUserSessionsPage(String layoutId, Pageable pageable) {

        String userId=this.getUserGuid();//当前用户标识
        Page<TAiUserSessionVO> page = tAiUserSessionService.queryAllAiUserSessionsPage(layoutId, userId, pageable);
        /*for(TAiUserSessionVO list:aiUserSessionVOList){
            //生成随机字符串
            Random random = new Random();
            StringBuilder sb = new StringBuilder(4);
            for (int j = 0; j < 4; j++) {
                int index = random.nextInt(CHARACTERS.length());
                sb.append(CHARACTERS.charAt(index));
            }
            //组装地址
            String url = netname + contextPath + "/index.html#/appZntDialogDetail?zntId=" + list.getSysPromptId();
            String appUrl = null;
            try {
                appUrl = userAuthorizationUri + "?client_id=" + clientId + "&redirect_uri=" + URLEncoder.encode(url, "UTF-8") + "&response_type=code&state=" + sb.toString();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            list.setAppUrl(appUrl);
        }*/
        return OperaResult.putSuccessResult("page", page);
    }

    @ApiOperation(value = "查询历史会话详情", notes = "查询历史会话详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "aiSessionId", paramType = "query", value = "会话标识", dataType = "String"),
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String")
    })
    @ResponseBody
    @PostMapping("/queryAiUserSessionDetails")
    public OperaResult queryAiUserSessionDetails(String aiSessionId) {
        String userId=this.getUserGuid();//当前用户标识
        List<TAiUserSessionModelVO> tAiUserSessionlModels =tAiUserSessionService.queryAiUserSessionDetails(aiSessionId);
        OperaResult operaResult=OperaResult.putSuccessResult("tAiUserSessionlModels",tAiUserSessionlModels);
        TAiUserSession tAiUserSession=tAiUserSessionService.findById(aiSessionId);
        if(StringUtils.isNotBlank(tAiUserSession.getSysPromptId())){//智能体会话 返回智能体信息
            TAiSysPrompt tAiSysPrompt=tAiSysPromptService.findById(tAiUserSession.getSysPromptId());
            operaResult.getData().put("sysPromptName",tAiSysPrompt.getName());
            operaResult.getData().put("sysPromptImg",tAiSysPrompt.getProImg());
            operaResult.getData().put("prologue",tAiSysPrompt.getPrologue());
            operaResult.getData().put("notes",tAiSysPrompt.getNotes());
            operaResult.getData().put("isUseSzr",tAiSysPrompt.getIsUseSzr());
            if(tAiSysPrompt.getPublishStatus()==ConstantUtil.PUBLISH_STATUS_0   //未发布
                    || tAiSysPrompt.getReviewStatus()==ConstantUtil.REVIEW_STATUS_2 //已停用
                    || (tAiSysPrompt.getPublishRange()==ConstantUtil.PUBLISH_RANGE_0 && !this.getUserGuid().equals(tAiSysPrompt.getCreateUser())) //个人发布但不是本人
            ){
                operaResult.getData().put("isAnswer",false);//未发布、已停用、个人发布但不是本人
            }else{
                operaResult.getData().put("isAnswer",true);
            }
        }
        return operaResult;
    }

    @ApiOperation(value = "创建新会话", notes = "创建新会话")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String")
    })
    @ResponseBody
    @PostMapping("/createAiSession")
    public OperaResult createAiSession(@RequestBody(required = true) TAiUserSessionDTO tAiUserSessionlDTO) {
        String userId=this.getUserGuid();//用户标识
        String userTypeCode=this.getUserTypeCode();
        TAiUserSessionVO tAiUserSessionlVO=tAiUserSessionService.createAiSession(tAiUserSessionlDTO,userId,userTypeCode,this.getUserLoginName(),this.getUName()
                ,((this.getUserTypeCode().equals(ConstantUtil.USER_TYPE_101003+""))?String.valueOf(this.getLoginUser().get("studentId")):null));
        return OperaResult.putSuccessResult("tAiUserSession",tAiUserSessionlVO);
    }

    @ApiOperation(value = "修改会话名称", notes = "修改会话名称")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String")
    })
    @ResponseBody
    @PostMapping("/updateAiSessionName")
    public OperaResult updateAiSessionName(@RequestParam(required = true) String aiSessionId,@RequestParam(required = true)  String newName) {
        TAiUserSession tAiUserSession=tAiUserSessionService.findById(aiSessionId);
        tAiUserSession.setName(newName);
        tAiUserSessionService.save(tAiUserSession);
        return OperaResult.putSuccessResult("success",true);
    }

    @ApiOperation(value = "删除会话", notes = "删除会话")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "aiSessionId", paramType = "query", value = "会话标识", dataType = "String"),
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String")
    })
    @ResponseBody
    @PostMapping("/deleteAiSession")
    public OperaResult deleteAiSession(@RequestParam(required = true) String aiSessionId) {
        TAiUserSession tAiUserSession=tAiUserSessionService.findById(aiSessionId);
        tAiUserSession.setIsDel(ConstantUtil.IS_DEL_1);
        tAiUserSessionService.save(tAiUserSession);
        return OperaResult.putSuccessResult("success",true);
    }

    @ApiOperation(value = "删除智能体", notes = "删除智能体")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "zntId", paramType = "query", value = "智能体ID", dataType = "String"),
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String")
    })
    @ResponseBody
    @PostMapping("/delZnt")
    public OperaResult delZnt(String zntId) {
        TAiSysPrompt tAiSysPrompt =tAiSysPromptService.findById(zntId);
        //判断是否本人查询本人智能体
        if(!this.getUserGuid().equals(tAiSysPrompt.getCreateUser())){
            return OperaResult.putFailResult("error","不合法的请求");
        }
        tAiSysPrompt.setIsDel(ConstantUtil.IS_DEL_1);//作废
        tAiSysPromptService.save(tAiSysPrompt);
        return OperaResult.putSuccessResult("sysPrompt",tAiSysPrompt);
    }

    @ApiOperation(value = "获取智能体列表-个人智能体列表", notes = "获取智能体列表-个人智能体列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "znt", paramType = "query", value = "搜索智能体名称", dataType = "Integer"),
            @ApiImplicitParam(name = "busType", paramType = "query", value = "分类字典值", dataType = "Integer"),
            @ApiImplicitParam(name = "pageable", paramType = "query", value = "分页对象", dataType = "Object"),
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String")
    })
    @ResponseBody
    @PostMapping("/queryUserZntList")
    public OperaResult queryUserZntList() {
        Pageable pageable =PageRequest.of(0,10000);//默认查询10000条
        return OperaResult.putSuccessResult("zntList",tAiSysPromptService.queryZntList(null,null,pageable,this.getUserGuid(),null,null,null).getContent());
    }

    @ApiOperation(value = "获取智能体详情-个人智能体", notes = "获取智能体列表-个人智能体")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "zntId", paramType = "query", value = "智能体ID", dataType = "String"),
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String")
    })
    @ResponseBody
    @PostMapping("/queryZntMes")
    public OperaResult queryZntMes(String zntId) {

        TAiSysPrompt tAiSysPrompt =tAiSysPromptService.findById(zntId);
        //判断是否本人查询本人智能体
        if(!this.getUserGuid().equals(tAiSysPrompt.getCreateUser())){
            return OperaResult.putFailResult("error","不合法的请求");
        }
        return OperaResult.putSuccessResult("sysPrompt",tAiSysPrompt);
    }

    @ApiOperation(value = "保存智能体", notes = "保存智能体")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String")
    })
    @ResponseBody
    @PostMapping("/saveZnt")
    public OperaResult saveZnt(@RequestBody TAiSysPromptDTO tAiSysPromptDTO) {
        OperaResult operaResult=new OperaResult();

        TAiSysPrompt tAiSysPrompt=null;
        //更新
        if(tAiSysPromptDTO!=null && StringUtils.isNotBlank(tAiSysPromptDTO.getId())){
            tAiSysPrompt=tAiSysPromptService.findById(tAiSysPromptDTO.getId());

            //判断是否本人查询本人智能体
            if(!this.getUserGuid().equals(tAiSysPrompt.getCreateUser())){
                return OperaResult.putFailResult("error","不合法的请求");
            }

            BeanUtils.copyProperties(tAiSysPromptDTO,tAiSysPrompt);

            tAiSysPrompt.setIsDel(ConstantUtil.IS_DEL_0);//删除状态 0正常 1删除


            if(!ConstantUtil.ZNT_PASS_STATUS_0.equals(ConstantUtil.sysConfig.get("is_znt_autopass")) || tAiSysPrompt.getPublishRange()==ConstantUtil.PUBLISH_RANGE_0){//是否自动审核通过个人提交的智能体  1是 0否
                tAiSysPrompt.setReviewStatus(ConstantUtil.REVIEW_STATUS_1);//直接审核通过
            }else{
                tAiSysPrompt.setReviewStatus(ConstantUtil.REVIEW_STATUS_0);//待审核
            }

            tAiSysPrompt.setPublishStatus(ConstantUtil.PUBLISH_STATUS_1);//发布状态 1已发布  0未发布 2已停用 直接发布
            tAiSysPrompt.setUpdateTime(new Date());
            tAiSysPrompt.setUpdateUser(this.getUserGuid());
            tAiSysPromptService.save(tAiSysPrompt);
            //更新所有已存在的会话的模型ID
            tAiUserSessionModelService.updateAllSessionModelId(tAiSysPrompt.getId(),tAiSysPrompt.getModelId());

        }else{//新增
            tAiSysPrompt=new TAiSysPrompt();
            BeanUtils.copyProperties(tAiSysPromptDTO,tAiSysPrompt);
            tAiSysPrompt.setCreateTime(new Date());
            tAiSysPrompt.setCreateUser(this.getUserGuid());
            tAiSysPrompt.setIsDel(ConstantUtil.IS_DEL_0);//删除状态 0正常 1删除
            //tAiSysPrompt.setReviewStatus(ConstantUtil.REVIEW_STATUS_0);//待审核

            if(!ConstantUtil.ZNT_PASS_STATUS_0.equals(ConstantUtil.sysConfig.get("is_znt_autopass")) || tAiSysPrompt.getPublishRange()==ConstantUtil.PUBLISH_RANGE_0){//是否自动审核通过个人提交的智能体  1是 0否
                tAiSysPrompt.setReviewStatus(ConstantUtil.REVIEW_STATUS_1);//直接审核通过
            }else{
                tAiSysPrompt.setReviewStatus(ConstantUtil.REVIEW_STATUS_0);//待审核
            }

            tAiSysPrompt.setSysPromptType(ConstantUtil.SYS_PROMPT_TYPE_2);//1-智能体应用  2-用户智能体 3-工具智能体
            //if(this.getUserTypeCode().equals(ConstantUtil.USER_TYPE_101007+"")){//如果是管理员创建的 直接发布,普通用户创建的手动发布
            //    tAiSysPrompt.setPublishStatus(ConstantUtil.PUBLISH_STATUS_1);//发布状态 1已发布  0未发布 2已停用
            //}else{
                tAiSysPrompt.setPublishStatus(ConstantUtil.PUBLISH_STATUS_1);//发布状态 1已发布  0未发布 2已停用 直接发布
            //}

            tAiSysPrompt.setChName(this.getUName());
            tAiSysPrompt.setUserName(this.getUserLoginName());
            tAiSysPromptService.save(tAiSysPrompt);
            //如果是本人创建智能体创建会话
            tAiUserSessionService.createAiSession(TAiUserSessionDTO.builder().name(tAiSysPrompt.getName()).sysPromptId(tAiSysPrompt.getId()).build()
                    ,this.getUserGuid(),this.getUserTypeCode(),this.getUserLoginName(),this.getUName()
                    ,((this.getUserTypeCode().equals(ConstantUtil.USER_TYPE_101003+""))?String.valueOf(this.getLoginUser().get("studentId")):null));
        }

        try {
            //生成随机字符串
            Random random = new Random();
            StringBuilder sb = new StringBuilder(4);
            for (int j = 0; j < 4; j++) {
                int index = random.nextInt(CHARACTERS.length());
                sb.append(CHARACTERS.charAt(index));
            }
            //组装地址
            String url = netname + contextPath + "/index.html#/appZntDialogDetail?zntId=" + tAiSysPrompt.getId();
            String appUrl = userAuthorizationUri + "?client_id=" + clientId + "&redirect_uri=" + URLEncoder.encode(url, "UTF-8") + "&response_type=code&state=" + sb.toString();
            operaResult.getData().put("appUrl", appUrl);
            operaResult.getData().put("zntId", tAiSysPrompt.getId());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        operaResult.putSuccessResult("success",true);
        return operaResult;
    }

    @ApiOperation(value = "智能体发布", notes = "智能体发布")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "zntId", paramType = "query", value = "智能体ID", dataType = "String"),
            @ApiImplicitParam(name = "publishStatus", paramType = "query", value = "发布状态(0未发布  1已发布  2已停用)", dataType = "Integer"),
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String")
    })
    @ResponseBody
    @PostMapping("/publishZnt")
    public OperaResult publishZnt(String zntId,Integer publishStatus ) {
        TAiSysPrompt tAiSysPrompt=tAiSysPromptService.findById(zntId);

        //判断是否本人查询本人智能体
        if(!this.getUserGuid().equals(tAiSysPrompt.getCreateUser())){
            return OperaResult.putFailResult("error","不合法的请求");
        }

        tAiSysPrompt.setPublishStatus(publishStatus);//发布状态(0未发布  1已发布  2已停用)
        tAiSysPromptService.save(tAiSysPrompt);
        return OperaResult.putSuccessResult("success",true);
    }

    @ApiOperation(value = "获取aiPPT code", notes = "获取aiPPT code")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String")
    })
    @ResponseBody
    @PostMapping("/getAiPPTCode")
    public OperaResult getAiPPTCode() throws Exception {

        Long dqsjc=System.currentTimeMillis()/1000L;

        String apiUrl=ConstantUtil.sysConfig.get("aippt_api");
        String ak=ConstantUtil.sysConfig.get("aippt_ak");//aippt ak值
        String sk = ConstantUtil.sysConfig.get("aippt_sk");//aippt sk值

        String data = "GET@/api/grant/code/@"+dqsjc;//获取ui sdk集成code接口
        String signature = genHmac(data, sk);

        Map<String,Object> headerMap=new HashMap<>();
        headerMap.put("x-api-key",ak);
        headerMap.put("x-timestamp",dqsjc);
        headerMap.put("x-signature",signature);
        try {
            HttpClients.get("https://co.aippt.cn/api/grant/code/?uid="+this.getUserGuid()+"&channel=aimodel",null,new HashMap<>());
        }catch (Exception e){
            log.error("尝试调用https://co.aippt.cn");
        }

        LinkedTreeMap linkedTreeMap=new Gson().fromJson(HttpClients.get("https://co.aippt.cn/api/grant/code/?uid="+this.getUserGuid()+"&channel=aimodel",null,headerMap),LinkedTreeMap.class);

        return OperaResult.putSuccessResult("apiCode",linkedTreeMap.get("data"));
    }

    @ApiOperation(value = "根据ppt内容 创建AIPPT任务", notes = "根据ppt内容 创建AIPPT任务")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pptNr", paramType = "query", value = "PPT内容", dataType = "String"),
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String")
    })
    @ResponseBody
    @PostMapping("/createAiPPTTask")
    public OperaResult createAiPPTTask(String pptNr) throws Exception {

        String uid=this.getUserGuid();
        String xChannel="aimodel";
        OperaResult xTokenObj=getAiPPTUserToken();
        String xtoken=String.valueOf(xTokenObj.getData().get("token"));
        String ak=ConstantUtil.sysConfig.get("aippt_ak");//aippt ak值
        AiOutMsgDTO pptDgResult=tAiModelService.answerApi("2c81808e93f79f9a0193fc58d9670010","{\"text\":\""+pptNr+"\"}",this.getUserGuid());//调用PPT大纲生成功能
        String content=String.valueOf(pptDgResult.getData());
        Integer type=7;//参考附件形式 生成大纲
        String createTaskUrl="https://co.aippt.cn/api/ai/chat/v2/task";//AIPPT任务接口

        File file= new File(uploadFilePath+"/aippt/"+new SimpleDateFormat("yyyyMMdd").format(new Date())+"/"+ java.util.UUID.randomUUID().toString().replaceAll("-","").toLowerCase()+".txt");
        if(!file.getParentFile().exists()){
            file.getParentFile().mkdirs();
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(pptNr);
        } catch (IOException e) {
            log.error("写入文件时发生错误",e);
        }
        OkHttpClient client = getUnsafeOkHttpClient();
        // 创建文件请求体
        okhttp3.RequestBody fileBody = okhttp3.RequestBody.create(MediaType.parse("application/octet-stream"), file);
        // 编码文件名以避免乱码
        String encodedFileName =  URLEncoder.encode(file.getPath(), "UTF-8");
        // 创建多部分请求体
        okhttp3.RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("content", content)
                .addFormDataPart("type",type+"")
                .build();
        // 创建请求
        Request request = new Request.Builder()
                .url(createTaskUrl)
                .addHeader("x-api-key", ak)
                .addHeader("x-channel", xChannel)
                .addHeader("x-token", xtoken)
                .post(requestBody)
                .build();
        // 打印请求头
        System.out.println("Request URL: " + request.url());
        System.out.println("Request Headers: " + request.headers());
        try {
            Request requestCheck = new Request.Builder()
                    .url(createTaskUrl)
                    .addHeader("x-api-key", ak)
                    .post(requestBody)
                    .build();
            client.newCall(requestCheck);
        }catch (Exception e){
            log.error("尝试调用https://co.aippt.cn");
        }

        JSONObject result= new JSONObject();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("上传文件失败：" + response);
            }
            // 将响应体内容读取到一个字符串变量中
            String responseBody = response.body() != null ? response.body().string() : "";
            // 打印响应体内容
            System.out.println(responseBody);

            // 检查响应体是否为空，并解析为 JSON 对象
            if (StringUtils.isNotEmpty(responseBody)) {
                result = JSON.parseObject(responseBody);
            }

        } catch (IOException e) {
            log.error("根据参考附件创建aippt生成任务失败",e);

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("上传文件失败：" + response);
                }
                // 将响应体内容读取到一个字符串变量中
                String responseBody = response.body() != null ? response.body().string() : "";
                // 打印响应体内容
                System.out.println(responseBody);

                // 检查响应体是否为空，并解析为 JSON 对象
                if (StringUtils.isNotEmpty(responseBody)) {
                    result = JSON.parseObject(responseBody);
                }

            } catch (IOException e1) {
                log.error("(重试请求)根据参考附件创建aippt生成任务失败",e);
                return OperaResult.putFailResult("error","根据参考附件创建aippt生成任务失败");
            }

        }

        /*JSONObject result=new JSONObject();
        JSONObject data=new JSONObject();
        data.put("id",32207340);
        result.put("data",data);*/
        return OperaResult.putSuccessResult("task",result);

    }
    /**
     * @Description 获取一个不校验SSL的OkHttpClient
     * @Author zhushaog
     * @UpdateTime 2025/4/9 14:48
     * @return: okhttp3.OkHttpClient
     * @throws
     */
    private static OkHttpClient getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*@ApiOperation(value = "根据ppt内容 创建AIPPT任务", notes = "根据ppt内容 创建AIPPT任务")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pptNr", paramType = "query", value = "PPT内容", dataType = "String"),
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String")
    })
    @ResponseBody
    @PostMapping("/createAiPPTTask")
    public OperaResult createAiPPTTask(String pptNr) throws Exception {

        String uid=this.getUserGuid();
        String xChannel="aimodel";
        OperaResult xTokenObj=getAiPPTUserToken();
        String xtoken=String.valueOf(xTokenObj.getData().get("token"));
        String ak=ConstantUtil.sysConfig.get("aippt_ak");//aippt ak值
        String title="请为下面教案生成PPT内容。包括封面页、内容页和封底页。，将教案改成用于上课向学生播放的PPT大纲。因为是给学生看的，所以只体现需要学生参与的环节内容，语气以第二人称为主。各项任务需要转写成能吸引学生参与的语气和描述。按照文本内容做，不要扩展。";
        Integer type=17;//参考附件形式 生成大纲
        String createTaskUrl="https://co.aippt.cn/api/ai/chat/v2/task";

        File file= new File(uploadFilePath+"/aippt/"+new SimpleDateFormat("yyyyMMdd").format(new Date())+"/"+ java.util.UUID.randomUUID().toString().replaceAll("-","").toLowerCase()+".txt");
        if(!file.getParentFile().exists()){
            file.getParentFile().mkdirs();
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(pptNr);
        } catch (IOException e) {
            log.error("写入文件时发生错误",e);
        }
        OkHttpClient client = new OkHttpClient();
        // 创建文件请求体
        okhttp3.RequestBody fileBody = okhttp3.RequestBody.create(MediaType.parse("application/octet-stream"), file);
        // 编码文件名以避免乱码
        String encodedFileName =  URLEncoder.encode(file.getPath(), "UTF-8");
        // 创建多部分请求体
        okhttp3.RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("files", file.getName(), fileBody)
                .addFormDataPart("title",title)
                .addFormDataPart("type",type+"")
                .build();
        // 创建请求
        Request request = new Request.Builder()
                .url(createTaskUrl)
                .addHeader("x-api-key", ak)
                .addHeader("x-channel", xChannel)
                .addHeader("x-token", xtoken)
                .post(requestBody)
                .build();
        // 打印请求头
        System.out.println("Request URL: " + request.url());
        System.out.println("Request Headers: " + request.headers());

        JSONObject result= new JSONObject();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("上传文件失败：" + response);
            }
            // 将响应体内容读取到一个字符串变量中
            String responseBody = response.body() != null ? response.body().string() : "";
            // 打印响应体内容
            System.out.println(responseBody);

            // 检查响应体是否为空，并解析为 JSON 对象
            if (StringUtils.isNotEmpty(responseBody)) {
                result = JSON.parseObject(responseBody);
            }

        } catch (IOException e) {
           log.error("根据参考附件创建aippt生成任务失败",e);
            return OperaResult.putFailResult("error","根据参考附件创建aippt生成任务失败");
        }

        *//*JSONObject result=new JSONObject();
        JSONObject data=new JSONObject();
        data.put("id",32207340);
        result.put("data",data);*//*
        return OperaResult.putSuccessResult("task",result);

    }*/

    @ApiOperation(value = "获取aiPPT 用户token", notes = "获取aiPPT 用户token")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String")
    })
    @ResponseBody
    @PostMapping("/getAiPPTUserToken")
    public OperaResult getAiPPTUserToken() throws Exception {

        Long dqsjc=System.currentTimeMillis()/1000L;
        String token=null;
        Object tokenObj=redisTemplate.opsForValue().get("api:uk:"+this.getUserGuid());
        if(tokenObj!=null){
            token=String.valueOf(tokenObj);
        }else{
            try {
                String ak=ConstantUtil.sysConfig.get("aippt_ak");//aippt ak值
                String sk = ConstantUtil.sysConfig.get("aippt_sk");//aippt sk值
                String data = "GET@/api/grant/token/@"+dqsjc;//获取ui sdk集成code接口
                String signature = genHmac(data, sk);
                Map<String,Object> headerMap=new HashMap<>();
                headerMap.put("x-api-key",ak);
                headerMap.put("x-timestamp",dqsjc);
                headerMap.put("x-signature",signature);
                try {
                    HttpClients.get("https://co.aippt.cn/api/grant/token?uid="+this.getUserGuid()+"&channel=aimodel",null,new HashMap<>());
                }catch (Exception e){
                    log.error("尝试调用https://co.aippt.cn");
                }

                JSONObject linkedTreeMap=JSON.parseObject(HttpClients.get("https://co.aippt.cn/api/grant/token?uid="+this.getUserGuid()+"&channel=aimodel",null,headerMap));
                token=linkedTreeMap.getJSONObject("data").getString("token");
                redisTemplate.opsForValue().set("api:uk:"+this.getUserGuid(),token,29,TimeUnit.DAYS);
            }catch (Exception e){
                log.error("AIPPT生成用户token失败",e);
                return OperaResult.putFailResult("error","AIPPT生成用户token失败");
            }
        }
        return OperaResult.putSuccessResult("token",token);

    }

    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
    private static String genHmac(String data, String key) throws Exception {
        SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), HMAC_SHA1_ALGORITHM);
        Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
        mac.init(signingKey);
        byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(rawHmac);
    }



    @ApiOperation(value = "AI文生图", notes = "AI文生图")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "aiSessionId", paramType = "query", value = "ai标识", dataType = "String"),
            @ApiImplicitParam(name = "content", paramType = "query", value = "内容", dataType = "String"),
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String")
    })
    @ResponseBody
    @PostMapping("/textToImage")
    public OperaResult textToImage(String aiSessionId,String content, HttpServletRequest request) {

        OperaResult operaResult=new OperaResult();
        String checkResult=checkSensitiveWord(content);//敏感词校验结果
        if (StringUtils.isNotBlank(checkResult)) {
            return OperaResult.putFailResult("warn","内容中包含敏感词:"+checkResult);
        }

        String dqrq=new SimpleDateFormat("yyyyMMdd").format(new Date());

        operaResult.setResultCode(com.zklcsoftware.common.web.util.ConstantUtil.OPT_RESULT_CODE_SUCCESS);

        AiOutMsgDTO aiOutMsgDTO = aiModelService.textToImage(aiSessionId,content);
        operaResult.getData().put("aiOutMsgDTO", aiOutMsgDTO);
        /*//加入到后台安装的线程池中等待执行
        executor.execute(new Runnable() {
            @Override
            public void run() {*/

            /*}
        });*/
        return operaResult;
    }

    @ApiOperation(value = "评语生成", notes = "评语生成")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "zntId", paramType = "query", value = "智能体id", dataType = "String"),
            @ApiImplicitParam(name = "fileUrl", paramType = "query", value = "文件路径", dataType = "String"),
            @ApiImplicitParam(name = "content", paramType = "query", value = "描述", dataType = "String"),
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String")
    })
    @ResponseBody
    @PostMapping("/CommentGeneration")
    public OperaResult CommentGeneration(String zntId, String fileUrl, String content, HttpServletRequest request, HttpServletResponse response) {

        OperaResult operaResult=new OperaResult();
        String checkResult=checkSensitiveWord(content);//敏感词校验结果
        if (StringUtils.isNotBlank(checkResult)) {
            return OperaResult.putFailResult("warn","内容中包含敏感词:"+checkResult);
        }

        operaResult.setResultCode(com.zklcsoftware.common.web.util.ConstantUtil.OPT_RESULT_CODE_SUCCESS);

        AiOutMsgDTO aiOutMsgDTO = commentGenerationService.CommentGeneration(zntId,fileUrl,content,this.getUserGuid());
        operaResult.getData().put("aiOutMsgDTO", aiOutMsgDTO);
        /*//加入到后台安装的线程池中等待执行
        executor.execute(new Runnable() {
            @Override
            public void run() {*/

            /*}
        });*/
        return operaResult;
    }

    @ApiOperation(value = "下载评语模板", notes = "下载评语模板")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String")
    })
    @ResponseBody
    @GetMapping("/downloadCommentTemplate")
    public void downloadCommentTemplate(HttpServletResponse response) {
        try {
            String filename = "template_pysc.xlsx";
            // path是指欲下载的文件的路径。
            String path = "/webapp/template/" + filename;
            //String path = uploadFilePath + filename;

            log.info("文件下载地址==========="+path);
            File file = new File(path);
            if (!file.exists()) {
                log.info("文件不存在");
                return;
            }
            // 以流的形式下载文件。
            InputStream fis = new BufferedInputStream(new FileInputStream(path));
            log.info("文件==========="+fis);
            byte[] buffer = new byte[fis.available()];
            log.info("文件大小==========="+buffer.length);
            fis.read(buffer);
            fis.close();
            // 清空response
            response.reset();
            // 设置response的Header
            String agent = (String)getRequest().getHeader("USER-AGENT").toUpperCase(); //获得浏览器信息并转换为大写
            String codedFileName = "评语生成模板.xlsx";
            if (agent.indexOf("MSIE") > 0 || (agent.indexOf("GECKO")>0 && agent.indexOf("RV:11")>0)) {  //IE浏览器和Edge浏览器
                codedFileName = URLEncoder.encode(codedFileName, "UTF-8");
            } else {  //其他浏览器
                codedFileName = new String(codedFileName.getBytes("UTF-8"), "iso-8859-1");
            }

            //response.addHeader("Content-Disposition", "attachment;filename="+new String(filename_z.getBytes("gb2312"), "ISO8859-1"));//+ new String(filename.getBytes())
            response.addHeader("Content-Disposition", "attachment;filename=" + codedFileName); //+ new String(filename.getBytes())
            response.addHeader("Content-Length", "" + file.length());
            OutputStream toClient = new BufferedOutputStream(response.getOutputStream());
            response.setContentType("application/octet-stream");
            toClient.write(buffer);
            toClient.flush();
            toClient.close();
        } catch (IOException ex) {
            log.info("文件==========="+ex.getMessage());
            ex.printStackTrace();
        }
    }

    @ApiOperation(value = "文字转音频", notes = "文字转音频")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "aiSessionId", paramType = "query", value = "ai标识", dataType = "String"),
            @ApiImplicitParam(name = "content", paramType = "query", value = "描述", dataType = "String"),
            @ApiImplicitParam(name = "voiceType", paramType = "query", value = "音色", dataType = "String"),
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String")
    })
    @ResponseBody
    @PostMapping("/textToAudio")
    public OperaResult textToAudio(String aiSessionId,String content, String voiceType) throws Exception {

        OperaResult operaResult=new OperaResult();
        String checkResult=checkSensitiveWord(content);//敏感词校验结果
        if (StringUtils.isNotBlank(checkResult)) {
            return OperaResult.putFailResult("warn","内容中包含敏感词:"+checkResult);
        }

        operaResult.setResultCode(com.zklcsoftware.common.web.util.ConstantUtil.OPT_RESULT_CODE_SUCCESS);

        //TTSWebsocketDemo.textToAudio(content,voiceType,uploadFilePath+"/test.mp3");
        //AiOutMsgDTO aiOutMsgDTO = aiModelService.textToAudio(content,voiceType);
        /*//加入到后台安装的线程池中等待执行
        executor.execute(new Runnable() {
            @Override
            public void run() {*/

            /*}
        });*/
        return operaResult;
    }

    /**
     * <p>
     * 功能 该业务统一异常处理方法
     * </p>
     * @author zhushaog 时间 2017年2月23日 上午11:30:06
     * @param req
     * @param e
     * @return
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    public OperaResult runtimeExceptionHandler(HttpServletRequest req, Exception e) {
        log.error("服务异常",e);
        return OperaResult.putFailResult("success",false);
    }

    /*public static void main(String[] args) {

        Map headerMap=new HashMap();
        headerMap.put("Authorization","HMAC-SHA256 Credential=AKLTNDg0ZDI0MGM5NDViNGU1YWJlZDMyYzNhMThjYjYzYWM/20250108/cn-beijing/cv/request, SignedHeaders=host;x-content-sha256;x-date, Signature=75b329fb90760078ef0c868e8c1ada565ed0f10ed20b4b346ecc5f65502753c4");
        headerMap.put("Host","visual.volcengineapi.com");
        headerMap.put("X-Content-Sha256","3c503817142f178ebf02ed1a30f561d68969c15fd14e7bf40cf956d72333c95f");
        headerMap.put("X-Date","20250108T105641Z");

        Map dataMap=new HashMap();

        dataMap.put("image_base64","JXFr2HqU3ED06U7gDr0pirODkIn/fZ/wpyidv4E/77P8AhUi1H8bAeKcMHpSAT4GET/vs/wCFG24/55p/32f8KA1TF49qQgMMAjp2pSs24jy0/wC+z/hSOJwM7E6Z++f8KNQ1GOSTtC84we9XbOVRaRA9RGvb2rOuEuwGZVjBUj+I9M/Sqf8AwuHwLpX/ABK7/SdSkntv3UzxRx7WdeCRl+mQa5cQ4xSbOrDrV3P/2Q==");


        try {
            String res=HttpClients.post("http://visual.volcengineapi.com/?Action=OCRNormal&Version=2020-08-26",dataMap,headerMap);
            System.out.println(res);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }*/

    @ApiOperation(value = "数据查询并生成报表", notes = "数据查询并生成报表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "sysPromptId", paramType = "query", value = "智能体id", dataType = "String"),
            @ApiImplicitParam(name = "question", paramType = "query", value = "问题内容", dataType = "String"),
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String")
    })
    @ResponseBody
    @PostMapping("/dateQueryAndToEchart")
    public OperaResult dateQueryAndToEchart(String sysPromptId,String question, HttpServletRequest request) {
        return OperaResult.putSuccessResult("aiOutMsg",echartService.DateQueryAndToEchart(sysPromptId,question,this.getUserGuid()));
    }


    @Value("${uploadfiledir.uploadFilePath}")
    private String uploadFilePath;//服务器路径
    @Value("${uploadfiledir.uploadFileUrl}")
    private String uploadFileUrl;//文件封存的http地址

    @ApiOperation(value = "获取上传的ppt转换后的图片列表", notes = "获取上传的ppt转换后的图片列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pptUrl", paramType = "query", value = "ppt地址", dataType = "String"),
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String")
    })
    @ResponseBody
    @PostMapping("/pptToImageList")
    public OperaResult pptToImageList(String pptUrl,HttpServletRequest request) {
        OperaResult operaResult = new OperaResult();

        TAiModel tAiModel=tAiModelService.findById("5");//腾讯混元  此次使用腾讯云对象存储-文档转换服务 将ppt转换成图片
        String tx_sass_secretId= JSON.parseObject(tAiModel.getExtArgJson()).getString("tx_sass_secretId");
        String tx_sass_secretkey= JSON.parseObject(tAiModel.getExtArgJson()).getString("tx_sass_secretkey");
        String tx_sass_cos_region= JSON.parseObject(tAiModel.getExtArgJson()).getString("tx_sass_cos_region");
        String tx_sass_cos_bucket_name= JSON.parseObject(tAiModel.getExtArgJson()).getString("tx_sass_cos_bucket_name");
        int lastPage=0;//尝试请求10页内容
        //返回生成的预览图片
        List<String> images=new ArrayList<>();
        List<PptContentDto> pptList=new ArrayList<>();
        try {
            String pptPath=uploadFilePath+pptUrl.replace(uploadFileUrl,"");
            TxencentUtil.uploadObject(tx_sass_secretId,tx_sass_secretkey,tx_sass_cos_region,tx_sass_cos_bucket_name,pptPath,pptUrl.replace(uploadFileUrl,""));
            String fileUrl="http://"+tx_sass_cos_bucket_name+".cos."+tx_sass_cos_region+".myqcloud.com/"+pptUrl.replace(uploadFileUrl,"");
            File imagePath=new File(uploadFilePath+File.separator+"/ai/output/"+new File(pptPath).getName());
            if(!imagePath.exists()){
                imagePath.mkdirs();
            }
            //Thread.sleep(5000);
            for (int i = 0; i < 100; i++) {
                //下载文件预览pdf信息
                try {
                    HttpClients.getFile(fileUrl+"?ci-process=doc-preview&imageDpi=144&sdstType=png&page="+(i+1),imagePath+File.separator+(i+1)+".png");
                }catch (IOException e){
                    break;
                }
                lastPage++;
            }

            for (int i = 0; i < lastPage; i++) {
                //imageDpi参数默认96：720p， 144：1080p
                images.add(fileUrl+"?ci-process=doc-preview&imageDpi=144&dstType=png&page="+(i+1));

                PptContentDto dto = new PptContentDto();
                dto.setPptUrl(fileUrl);
                dto.setPptImg(fileUrl+"?ci-process=doc-preview&imageDpi=144&dstType=png&page="+(i+1));
                if(pptPath.endsWith(".ppt")){
                    dto.setPptContent(PptNotesExtractUtil.onePagePptNotesExtract(new File(pptPath), i));
                }else if(pptPath.endsWith(".pptx")){
                    dto.setPptContent(PptNotesExtractUtil.onePagePptxNotesExtract(new File(pptPath), i));
                }
                pptList.add(dto);
            }

        }catch (Exception e){
            log.error("使用腾讯云COS失败",e);
        }
        operaResult.getData().put("images",images);
        operaResult.getData().put("pptList",pptList);
        //return OperaResult.putSuccessResult("images",images);
        return operaResult;
    }

    @ApiOperation(value = "ppt转图片异步处理", notes = "ppt转图片异步处理")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pptUrl", paramType = "query", value = "ppt地址", dataType = "String"),
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String")
    })
    @ResponseBody
    @PostMapping(value = {"/pptToImageListAsync"})
    public OperaResult pptToImageListAsync(String pptUrl, HttpServletRequest request) {
        OperaResult operaResult = new OperaResult();
        String taskId = UUID.randomUUID().toString().replace("-", "");

        // 提交异步任务
        executor.submit(() -> {
            OperaResult result = this.pptToImageList(pptUrl, request);
            // 存储结果到 Redis
            redisTemplate.opsForValue().set(taskId, JSON.toJSONString(result), 20, TimeUnit.MINUTES);
        });
        operaResult.getData().put("taskId",taskId);
        operaResult.getData().put("cjsj",new Date().getTime());
        try {
            String pptPath=uploadFilePath+pptUrl.replace(uploadFileUrl,"");
            operaResult.getData().put("pptPage", PptNotesExtractUtil.getSlideCount(pptPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return operaResult;
    }

    @ApiOperation(value = "获取ppt转图片进度", notes = "获取ppt转图片进度")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String"),
            @ApiImplicitParam(name = "taskId", paramType = "query", value = "任务ID", dataType = "String"),
            @ApiImplicitParam(name = "cjsj", paramType = "query", value = "创建时间", dataType = "String"),
            @ApiImplicitParam(name = "pptPage", paramType = "query", value = "ppt页数", dataType = "Integer"),
    })
    @ResponseBody
    @PostMapping(value = {"/getPptToImageProcess"})
    public OperaResult getPptToImageProcess(String taskId, String cjsj, Integer pptPage) {
        OperaResult operaResult = new OperaResult();
        if (cjsj == null || pptPage == null) {
            return OperaResult.putFailResult("error", "参数缺失");
        }

        long currentTime = System.currentTimeMillis();
        long estimatedTotalTime = pptPage * 4000L; // 4秒每页
        long elapsedTime = currentTime - Long.valueOf(cjsj);
        String result = (String) redisTemplate.opsForValue().get(taskId);
        if (result == null) {
            double progress = Math.min((double) elapsedTime / estimatedTotalTime * 100, 100);

            // 剩余时间
            long remainingTime = estimatedTotalTime - elapsedTime;
            remainingTime = Math.max(0, remainingTime); // 确保剩余时间不小于0
            long minutes = remainingTime / 60000;
            long seconds = (remainingTime % 60000) / 1000;

            operaResult.getData().put("status", "processing");
            operaResult.getData().put("progress", String.format("%.2f%%", progress));
            operaResult.getData().put("estimatedRemainingTime", String.format("%d分%d秒", minutes, seconds));
            return operaResult;
        }
        operaResult.getData().put("status", "success");
        operaResult.getData().put("pptList", JSON.parseObject(result));
        return operaResult;
    }

    @ResponseBody
    @PostMapping("/saveContextExtJson")
    public OperaResult saveContextExtJson(String contextId,String extJson) {
        TAiUserSessionModelContext tAiUserSessionModelContext=tAiUserSessionModelContextService.findById(contextId);
        tAiUserSessionModelContext.setExtJson(extJson);//保存用户下载aippt.cn生成的ppt文档
        tAiUserSessionModelContextService.save(tAiUserSessionModelContext);
        return OperaResult.putSuccessResult("success",true);
    }

    @ResponseBody
    @PostMapping("/saveContextExtJsonForKey")
    public OperaResult saveContextExtJsonForKey(String contextId,String extJson,String key) {
        TAiUserSessionModelContext tAiUserSessionModelContext=tAiUserSessionModelContextService.findById(contextId);
        String extJsonStr=tAiUserSessionModelContext.getExtJson();
        if(StringUtils.isNotBlank(extJsonStr)){
            Map extJsonMap=JSON.parseObject(extJsonStr);
            extJsonMap.put(key,extJson);
            tAiUserSessionModelContext.setExtJson(JSON.toJSONString(extJsonMap));//保存用户下载aippt.cn生成的ppt文档
        }else{
            Map extJsonMap=new HashMap();
            extJsonMap.put(key,extJson);
            tAiUserSessionModelContext.setExtJson(JSON.toJSONString(extJsonMap));//保存用户下载aippt.cn生成的ppt文档
        }
        tAiUserSessionModelContextService.save(tAiUserSessionModelContext);
        return OperaResult.putSuccessResult("success",true);
    }

    @ResponseBody
    @PostMapping("/savePPT")
    public OperaResult savePPT(String pptId,String pptUrl) {

        //{contextId: _this.lastContextId,pptId: data.id,pptUrl:data.url,pptName:data.filename}

        //下载pptUrl的原始文件到服务器目录--地址有效期5分钟， 持久化到自身服务器上用于历史查询
        String newPptName=pptId+".ppt";
        try {
            HttpClients.getFile(pptUrl.replace("https://","http://"),uploadFilePath+"/ai/jasc/"+newPptName);
        }catch (Exception e){
           log.error("下载ppt文件异常",e);
        }

        String fileUrl=uploadFileUrl+"/ai/jasc/"+newPptName;//文件地址
        //this.saveContextExtJson(contextId,extJson);//保存用户下载aippt.cn生成的ppt文档
        return OperaResult.putSuccessResult("pptUrl",fileUrl);

    }

    /*public static void main(String[] args) {
        //下载pptUrl的原始文件到服务器目录--地址有效期5分钟， 持久化到自身服务器上用于历史查询
        String newPptName="屈原PPT.ppt"+System.currentTimeMillis()+".ppt";
        String pptUrl="http://aippt-domestic.aippt.com/temporary/2025-01-17/7078404425z4sl2yu6.pptx?Expires=1737082008&OSSAccessKeyId=LTAI5tEC4LwH9eYcd5ANuTnS&Signature=VIO9sS6TorQD4EeLxL8gsmk9K9g%3D&response-content-disposition=attachment%3Bfilename%2A%3DUTF-8%27%27%25E3%2580%258A%25E8%25AE%25A4%25E8%25AF%2586%25E5%25B1%2588%25E5%258E%259F%25E4%25B8%258E%25E6%2584%259F%25E5%258F%2597%25E5%2585%25B6%25E7%2588%25B1%25E5%259B%25BD%25E7%25B2%25BE%25E7%25A5%259EPPT%25E3%2580%258B.pptx";
        try {
            HttpClients.getFile(pptUrl,"C:/webapp/files/aimodel/"+"/ai/jasc/"+newPptName);
        }catch (Exception e){
            log.error("下载ppt文件异常",e);
        }
    }*/

    @ApiOperation(value = "转文本上传", notes = "转文本上传")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String"),
            @ApiImplicitParam(name = "zntId", paramType = "query", value = "智能体id", dataType = "String")
    })
    @ResponseBody
    @PostMapping("/toTextUpload")
    public OperaResult toTextUpload(MultipartFile multipartFile, String zntId,HttpServletRequest request) {
        try {
            request.setCharacterEncoding("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return tAiAttachmentService.zskUpload(multipartFile, zntId, this.getUserGuid(), this.getUName());
    }

    @ApiOperation(value = "zip包转文本上传", notes = "zip包转文本上传")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String"),
            @ApiImplicitParam(name = "zntId", paramType = "query", value = "智能体id", dataType = "String")
    })
    @ResponseBody
    @PostMapping("/zipToTextUpload")
    public OperaResult zipToTextUpload(MultipartFile multipartFile, String zntId) {
        if (StringUtils.isEmpty(zntId)) {
            return OperaResult.putFailResult("error", "智能体id不能为空");
        }

        if (!multipartFile.getOriginalFilename().endsWith(".zip")) {
            return OperaResult.putFailResult("error", "请上传zip文件");
        }
        return tAiAttachmentService.zskZipUpload(multipartFile, zntId, this.getUserGuid(), this.getUName());
    }

    @ApiOperation(value = "获取知识库分页列表", notes = "获取知识库分页列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "zntId", paramType = "query", value = "智能体ID", dataType = "String"),
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String"),
            @ApiImplicitParam(name = "pageable", paramType = "query", value = "分页对象", dataType = "Object"),
    })
    @ResponseBody
    @PostMapping("/queryZskPage")
    public OperaResult queryZskPage(String zntId, Pageable pageable) {
        if(StringUtils.isEmpty(zntId)){
            return OperaResult.putFailResult("error","知识库ID不能为空");
        }
        return OperaResult.putSuccessResult("page", tAiAttachmentService.queryZskPage(zntId, pageable));
    }

    @ApiOperation(value = "删除知识库", notes = "删除知识库")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "attachmentId", paramType = "query", value = "附件id", dataType = "String"),
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String")
    })
    @ResponseBody
    @PostMapping("/delZsk")
    public OperaResult delZsk(String attachmentId) {
        TAiAttachment tAiAttachment =tAiAttachmentService.findById(attachmentId);
        //判断是否本人查询本人知识库
        if(!this.getUserGuid().equals(tAiAttachment.getCreateUser())){
            return OperaResult.putFailResult("error","不合法的请求");
        }
        return tAiAttachmentService.delZsk(attachmentId);
    }

    @ApiOperation(value = "知识库重命名", notes = "知识库重命名")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "attachmentId", paramType = "query", value = "附件id", dataType = "String"),
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String")
    })
    @ResponseBody
    @PostMapping("/reZskName")
    public OperaResult reZskName(String attachmentId,String newName) {
        TAiAttachment tAiAttachment =tAiAttachmentService.findById(attachmentId);
        //判断是否本人查询本人知识库
        if(!this.getUserGuid().equals(tAiAttachment.getCreateUser())){
            return OperaResult.putFailResult("error","不合法的请求");
        }
        tAiAttachment.setFileName(newName);
        tAiAttachmentService.save(tAiAttachment);
        return OperaResult.putSuccessResult("success",true);
    }

    @ApiOperation(value = "md转word下载", notes = "md转word下载")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String"),
    })
    @ResponseBody
    @PostMapping("/mdToWordDown")
    public OperaResult mdToWordDown(@RequestBody String obj) {
        Map map = new Gson().fromJson(obj, Map.class);
        String modelContextId = String.valueOf(map.get("modelContextId"));
        String htmlData = String.valueOf(map.get("htmlData"));

        TAiUserSessionModelContext context = tAiUserSessionModelContextService.findById(modelContextId);
        String url = uploadFilePath+"/ai/context/" + modelContextId + ".docx";
        File file = new File(url);

        if(!file.exists()){
            file.getParentFile().mkdirs();
        }

        if (StringUtils.isNotBlank(htmlData)) {
            // 前端提供html 转成 word
            MdToWordConverter.convertHtmlToWordByPandoc(htmlData,url);
        } else {
            // 取数据库的 md 数据，转word。表格等格式支持不好。
            String assistantOut = context.getAssistantOut().replaceAll("<div>.*?</div>", "");
            MdToWordConverter.convertMarkdownToWord(assistantOut,url);
        }

        String fileUrl=uploadFileUrl+"/ai/context/"+modelContextId + ".docx";//文件地址
        return OperaResult.putSuccessResult("url",fileUrl);

    }

    @ApiOperation(value = "转md文件并下载", notes = "转md文件并下载")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String"),
            @ApiImplicitParam(name = "modelContextId", paramType = "query", value = "模型上下文id", dataType = "String"),
    })
    @ResponseBody
    @PostMapping("/mdFileDown")
    public OperaResult mdFileDown(String modelContextId) {
        try {
            String filename = modelContextId + ".md";
            TAiUserSessionModelContext context = tAiUserSessionModelContextService.findById(modelContextId);
            String assistantOut = context.getAssistantOut().replaceAll("<div.*</div>", "");
            String path = uploadFilePath+"/ai/context/" + filename;
            File file = new File(path);
            if(!file.exists()){
                file.getParentFile().mkdirs();
            }
            FileWriter fileOutputStream = new FileWriter(file);
            fileOutputStream.write(assistantOut);
            fileOutputStream.close();

            String fileUrl=uploadFileUrl+"/ai/context/"+filename;//文件地址
            return OperaResult.putSuccessResult("url",fileUrl);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return OperaResult.putSuccessResult("url","");
    }

    /**
     * @Description 根据文本生成语音文件
     * @Author zhushaog
     * @param: text 传入文字
     * @UpdateTime 2025/3/22 10:37
     * @return: java.util.Map<java.lang.String,java.lang.Object>
     * @throws
     */
    @PostMapping(value = "/getSoundFileByText")
    @ResponseBody
    public OperaResult getSoundFileByText(String text){

        TAiModel tAiModel=tAiModelService.findById("7");//火山引擎配置；
        String tts_appid= JSON.parseObject(tAiModel.getExtArgJson()).getString("tts_appid");//语音合成appid
        String tts_token= JSON.parseObject(tAiModel.getExtArgJson()).getString("tts_token");//语音合成token
        String filePath="/ai/sound/"+ DateUtil.formatDateByFormat(new Date(),"yyyy-MM-dd")+"/"+ UUID.fastUUID().toString() +".mp3";
        String fileSavePath=uploadFilePath+ filePath;
        String fileUrl=uploadFileUrl+filePath;
        try {
            com.zklcsoftware.aimodel.util.TtsHttpDemo.textToAudio(tts_appid,tts_token,text, "zh_female_cancan_mars_bigtts", 1.0,fileSavePath);
        } catch (IOException e) {
            log.error("文字转语音异常",e);
            return OperaResult.putFailResult("error","转换异常");
        }
        if(new File(fileSavePath).exists()){
            return OperaResult.putSuccessResult("soundFile",fileUrl);
        }else{
            return OperaResult.putFailResult("error",true);
        }
    }

    /**
     * @Description 用户录音上传处理
     * @Author zhushaog
     * @UpdateTime 2024/7/19 13:37
     * @return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     * @throws
     */
    //@PostMapping(value = "/uploadAudioFile", produces = "application/json;charset=UTF-8")
    @PostMapping(value = "/uploadAudioFile")
    @ResponseBody
    public OperaResult uploadAudioFile(MultipartFile file, String fileUrl){
        OperaResult operaResult=new OperaResult();
        if(file == null && StringUtils.isBlank(fileUrl)){
            return OperaResult.putFailResult("error","文件不存在");
        }
        String uuid= java.util.UUID.randomUUID().toString().replaceAll("-","");
        String path=new SimpleDateFormat("yyyy-MM-dd").format(new Date()) +"/"+uuid+".mp3";//文件路径
        String savePath="";//实际服务器存放路径
        String sourceFile="";//访问地址
        try {
            savePath= uploadFilePath +"ai/uploadSound/"+path;
            sourceFile=uploadFileUrl+"/ai/uploadSound/"+path;
            //判断父目录是否存在 如果不存在则先创建
            File file1=new File(savePath);
            if(!file1.getParentFile().exists()){
                file1.getParentFile().mkdirs();
            }

            if(file == null){
                URI uri = new URI(fileUrl);
                FileUtils.copyURLToFile(uri.toURL(), new File(savePath));
            }else{
                file.transferTo(new File(savePath));
            }
            operaResult.getData().put("sourceFile",sourceFile);
        }catch (Exception e){
            log.error("语音上传保存异常",e);
            return OperaResult.putFailResult("error","语音上传保存异常");
        }
        //如果识别上传的语音文件
        String dataText= null;
        try {
            dataText = ZjAsrClientUtil.audio2text(savePath);
            operaResult.getData().put("soundText",dataText);
        } catch (Exception e) {
            log.error("语音转文字异常",e);
            return OperaResult.putFailResult("error","语音转文字异常");
        }
        return operaResult;
    }


    @ApiOperation(value = "回答完查看索引文件", notes = "回答完查看索引文件")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "sessionContextId", paramType = "query", value = "上下文回答id", dataType = "String"),
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String"),
    })
    @ResponseBody
    @PostMapping("/answeredCallFileindexList")
    public OperaResult answeredCallFileindexList(String sessionContextId) {
        String netdiskUrl=ConstantUtil.sysConfig.get("netdisk_api");//获取网盘地址

        List<TAiUserSessionModelContextFileindexVO> fileindexList = tAiUserSessionModelContextFileindexService.answeredCallFileindexList(sessionContextId);
        for(TAiUserSessionModelContextFileindexVO fileindexVo : fileindexList){
            fileindexVo.setNetdiskUrl(netdiskUrl + "/download?fileid=" + fileindexVo.getNetdiskId());
        }
        return OperaResult.putSuccessResult("fileindexList", fileindexList);
    }

    @ApiOperation(value = "教学生成 - 跨学科 - 目录选择列表", notes = "教学生成 - 跨学科 - 目录选择列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String"),
    })
    @ResponseBody
    @PostMapping("/jiaoxueshengchengKuaxuekePathList")
    public OperaResult jiaoxueshengchengKuaxuekePathList(@RequestBody Object obj) {

        Map objMap = new Gson().fromJson(new Gson().toJson(obj), Map.class);
        List<String> pathList = (List<String>) objMap.get("pathList");
        String search = (String) objMap.get("search");

        System.out.println(search);
        // 根据参数读取目录，查询目录下的文件。
        String rootPath = uploadFilePath;
        rootPath = rootPath.replace("/", File.separator).replace("\\", File.separator);
        if (!rootPath.endsWith(File.separator)) {
            rootPath += File.separator;
        }
        rootPath += "book" + File.separator;
        StringBuilder sb = new StringBuilder(rootPath);
        for (String path : pathList) {
            sb.append(path).append(File.separator);
        }
        File file = new File(String.valueOf(sb));  // 根据参数，取到对应文件夹。
        File jsonFile = new File(String.valueOf(sb), "book.json");  // 文件下的 book.json

        List<String> names = new ArrayList<>();  // 要返回的数据
        if (!jsonFile.exists()) {
            // 返回目录下的文件夹名称列表。
            File[] files = file.listFiles();
            if (files != null) {
                for (File file1 : files) {
                    names.add(file1.getName());
                }
            }

            if (pathList.size() == 1) {
                // 学段排序
                String[] order = {"小学", "初中", "高中"}; // 定义排序顺序数组
                Comparator<String> comparator = Comparator.comparingInt(s -> Arrays.binarySearch(order, s));
                names = names.stream().sorted(comparator).collect(Collectors.toList());
            } else {
                // 按年级排序。
                String[] strings = names.toArray(new String[0]);
                DataUtil.bubbleSort(strings);
                names = Arrays.asList(strings);
            }

        } else {
            // 解析json数据。取到科目，递归连接所有层级，返回一级列表。
            try {
                String dataStr = new String(Files.readAllBytes(Paths.get(jsonFile.getAbsolutePath())));
                Map map  =new Gson().fromJson(dataStr, Map.class);
                List<Object> data = (List<Object>) map.get("data");
                DataUtil.execJson(names, data, "");
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (StringUtils.isNotBlank(search)) {
                // 过滤搜索内容
                names = names.stream()
                        .filter(s -> s.contains(search))
                        .collect(Collectors.toList());
            }
        }
        return OperaResult.putSuccessResult("pathList", names);
    }

    @ApiOperation(value = "人工智能 - 目录选择列表", notes = "人工智能 - 目录选择列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String"),
    })
    @ResponseBody
    @PostMapping("/rengongzhinengPathList")
    public OperaResult rengongzhinengPathList(@RequestBody Object obj) {

        Map objMap = new Gson().fromJson(new Gson().toJson(obj), Map.class);
        List<String> pathList = (List<String>) objMap.get("pathList");

        // 根据参数读取目录，查询目录下的文件。
        String rootPath = uploadFilePath;
        rootPath = rootPath.replace("/", File.separator).replace("\\", File.separator);
        if (!rootPath.endsWith(File.separator)) {
            rootPath += File.separator;
        }
        StringBuilder sb = new StringBuilder("人工智能" + File.separator);
        for (String path : pathList) {
            sb.append(path).append(File.separator);
        }
        File file = new File(rootPath + sb);  // 根据参数，取到对应文件夹。
        List<String> names = new ArrayList<>();  // 要返回的名称
        List<String> urlList = new ArrayList<>();  // 要返回的文件对就的下载地址。
        List<Long> wordSize = new ArrayList<>();  // 要返回的word文件大小

        // 返回目录下的文件夹名称列表。
        File[] files = file.listFiles();
        if (files != null) {
            Arrays.sort(files);
            for (File file1 : files) {
                names.add(file1.getName());
            }
        }

        if (pathList.size() == 0) {
            // 学段排序
            String[] order = {"小学", "初中", "高中"}; // 定义排序顺序数组
            Comparator<String> comparator = Comparator.comparingInt(s -> Arrays.binarySearch(order, s));
            names = names.stream().sorted(comparator).collect(Collectors.toList());
        } else if (pathList.size() == 2 || pathList.size() == 3) {
            // 按年级排序。
            String[] strings = names.toArray(new String[0]);
            DataUtil.bubbleSort(strings);
            names = Arrays.asList(strings);
        }

        if (pathList.size() == 4) {
            // 文件大小和下载地址。
            for (String name : names) {
                wordSize.add(new File(file, name).length());
                urlList.add(uploadFileUrl + "/" + sb.toString().replace("\\", "/") + name);
            }
        }

        OperaResult operaResult = new OperaResult();
        operaResult.getData().put("pathList", names);
        operaResult.getData().put("wordSize", wordSize);
        operaResult.getData().put("urlList", urlList);
        return operaResult;
    }

    @ApiOperation(value = "出题助手，知识点目录", notes = "出题助手，知识点目录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "period", paramType = "query", value = "学段名", dataType = "String"),
            @ApiImplicitParam(name = "subject", paramType = "query", value = "学科名", dataType = "String"),
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String"),
    })
    @ResponseBody
    @PostMapping("/queryStZsdIndex")
    public OperaResult queryStZsdIndex() {
        // 根据参数读取目录，查询目录下的文件。
        String rootPath = uploadFilePath;
        rootPath = rootPath.replace("/", File.separator).replace("\\", File.separator);
        if (!rootPath.endsWith(File.separator)) {
            rootPath += File.separator;
        }
        rootPath += "stzsd" + File.separator;
        String zsdIdexPath = rootPath+File.separator+"stzsd_index.json";
        File jsonFile = new File(zsdIdexPath);
        String treeData = null;  // 要返回的数据
        if (jsonFile.exists()) {
            // 解析json数据。取到科目，递归连接所有层级，返回一级列表。
            try {
                treeData= new String(Files.readAllBytes(Paths.get(jsonFile.getAbsolutePath())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return OperaResult.putSuccessResult("treeData", JSONObject.parseArray(treeData));
    }
    @ApiOperation(value = "出题助手，知识点信息查询", notes = "出题助手，知识点信息查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "period", paramType = "query", value = "学段名", dataType = "String"),
            @ApiImplicitParam(name = "subject", paramType = "query", value = "学科名", dataType = "String"),
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String"),
    })
    @ResponseBody
    @PostMapping("/queryStZsdList")
    public OperaResult queryStZsdList(@RequestParam(required = true) String period,@RequestParam(required = true)  String subject) {
        // 根据参数读取目录，查询目录下的文件。
        String rootPath = uploadFilePath;
        rootPath = rootPath.replace("/", File.separator).replace("\\", File.separator);
        if (!rootPath.endsWith(File.separator)) {
            rootPath += File.separator;
        }
        rootPath += "stzsd" + File.separator;
        String zsdJsonPath = rootPath+period+File.separator+subject+File.separator+"stzsd.json";
        File jsonFile = new File(zsdJsonPath);
        String treeData = null;  // 要返回的数据
        if (jsonFile.exists()) {
            // 解析json数据。取到科目，递归连接所有层级，返回一级列表。
            try {
                treeData= new String(Files.readAllBytes(Paths.get(jsonFile.getAbsolutePath())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return OperaResult.putSuccessResult("treeData", JSONObject.parseArray(treeData));
    }

    @ApiOperation(value = "查询作业系统试题库", notes = "查询作业系统试题库")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "question", paramType = "query", value = "提问内容(json字符串)", dataType = "String"),
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String"),
    })
    @ResponseBody
    @PostMapping("/queyrZyQkxx")
    public OperaResult queyrZyQkxx(String question) {

        Map<String,Object> dataMap = new Gson().fromJson(question, HashMap.class);

        //"cjsl":初级数量    //类型字数字  0~9
        //"zjsl":中级数量    //类型字数字  0~9
        //"gjsl":高级数量    //类型字数字  0~9

        Integer cjsl=dataMap.containsKey("cjsl")&&StringUtils.isNotBlank(String.valueOf(dataMap.get("cjsl")))?Double.valueOf(String.valueOf(dataMap.get("cjsl"))).intValue():0;
        Integer zjsl=dataMap.containsKey("zjsl")&&StringUtils.isNotBlank(String.valueOf(dataMap.get("zjsl")))?Double.valueOf(String.valueOf(dataMap.get("zjsl"))).intValue():0;
        Integer gjsl=dataMap.containsKey("gjsl")&&StringUtils.isNotBlank(String.valueOf(dataMap.get("gjsl")))?Double.valueOf(String.valueOf(dataMap.get("gjsl"))).intValue():0;

        if(dataMap.containsKey("sjljImages")){//如果包含图片则调用知识点识别接口
            AiOutMsgDTO aiOutMsgDTO=tAiModelService.answerApi("yc91808e93f79f9a0193fc58d9670026",question,this.getUserGuid());
            dataMap.put("zsd",aiOutMsgDTO.getData());
        }
        //根据知识点内容查询结构化知识点内容
        Integer pointId=null;
        Integer xd=null;
        Integer subject=null;
        if(dataMap.containsKey("pointId")){//页面选择知识点
            pointId=Double.valueOf(String.valueOf(dataMap.get("pointId"))).intValue();//知识点ID
            xd=Double.valueOf(String.valueOf(dataMap.get("period"))).intValue();//学段
            subject=Double.valueOf(String.valueOf(dataMap.get("subject"))).intValue();//学科
        }else{//页面手动输入/上传试题图片后程序识别的知识点
            AiOutMsgDTO aiOutMsgDTO=tAiModelService.answerApi("xc91808e93f79f9a0193fc58d9670116","{\"text\":\""+dataMap.get("zsd")+"\"}",this.getUserGuid());
            Map<String,Object> zsdData=new Gson().fromJson(String.valueOf(aiOutMsgDTO.getData()),HashMap.class);
            xd=ConstantUtil.zyXdMap.get(String.valueOf(zsdData.get("xd")));//学段
            subject=ConstantUtil.zyXkMap.get(String.valueOf(zsdData.get("subject")));//学科
            pointId=zsdData.containsKey("pointId")?Double.valueOf(String.valueOf(zsdData.get("pointId"))).intValue():0;//知识点id
        }

        OperaResult operaResult=new OperaResult();
        if(cjsl>0){
            operaResult.getData().put("cjtList",this.queryZskStxx(pointId,subject,xd,1,cjsl));
        }
        if(zjsl>0){
            operaResult.getData().put("zjtList",this.queryZskStxx(pointId,subject,xd,3,zjsl));
        }
        if(gjsl>0){
            operaResult.getData().put("gjtList",this.queryZskStxx(pointId,subject,xd,5,gjsl));
        }

        return operaResult;
    }
    /**
     * @Description 查询作业系统试题库
     * @Author zhushaog
     * @param: pointId  知识点ID
     * @param: subject   学科ID
     * @param: xd  学段ID
     * @param: nd   难度  1~5
     * @param: sl   数量
     * @UpdateTime 2025/3/31 23:35
     * @return: java.util.List<java.lang.Object>
     * @throws
     */
    private JsonNode queryZskStxx(Integer pointId,Integer subject,Integer xd,Integer nd,Integer sl) {

        JsonNode dataNode = null;
        String apiURL = "https://zuoye.hdzypt.cn/zbtiku/tiku/searchquestion" +
                "?gradeId=%d" +
                "&subjectId=%d" +
                "&zbPointId[]=%d" +
                "&queryType=1&sourceId=2&picUrl=&tid=0&category=0" +
                "&zbDifficulty=%d" +
                "&zbSource=0&zbCreateTime=0&sortDifficulty=0&zbMadeTime=0&zbRealTime=0&zbAccuracy=0&difficultCnt=2&madeTime=0&realTime=0&accuracy=0&zbExamSourceType=0&removeDuplicate=1&pointType=2&comprehensiveSort=1&tabType=1&zbGrade=0&zbYear=0&zbProvince=0&zbExamSource=0&zbPower=0" +
                "&pn=1&rn=%d" +
                "&pointSort=0" +
                "&isLoading=false" +
                "&token=6347391f9084699c9efbaa530577044f";
        apiURL = String.format(apiURL, xd, subject, pointId, nd, sl);

        try (org.apache.http.impl.client.CloseableHttpClient httpClient = org.apache.http.impl.client.HttpClients.createDefault()) {

            HttpGet request = new HttpGet(apiURL);

            try (org.apache.http.client.methods.CloseableHttpResponse response = httpClient.execute(request)) {
                if (response.getStatusLine().getStatusCode() == 200) {
                    String responseBody = org.apache.http.util.EntityUtils.toString(response.getEntity());
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode rootNode = objectMapper.readTree(responseBody);

                    if (rootNode.path("errNo").asInt() == 0) {
                        dataNode = rootNode.path("data").path("list");
                    }
                }
            }

        } catch (Exception e) {
            log.error("请求作业试题库异常",e);
        }
        return dataNode;
    }

    @ApiOperation(value = "批量下载素材", notes = "批量下载素材")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String"),
            @ApiImplicitParam(name = "contextId", paramType = "query", value = "上下文ID", dataType = "String"),
    })
    @ResponseBody
    @PostMapping("/downLoadScqd")
    public OperaResult downLoadScqd(String contextId,String qdmc, HttpServletResponse response) throws Exception {
        // 获取资源地址列表和scmc属性
        TAiUserSessionModelContext tAiUserSessionModelContext=tAiUserSessionModelContextService.findById(contextId);
        String extJson=tAiUserSessionModelContext.getExtJson();
        // 创建临时目录存放下载的文件
        String zipFilePath=uploadFilePath+"/scqd/"+ contextId +".zip";
        // 创建压缩包
        File zipFile = new File(zipFilePath);
        if(extJson.indexOf("scqd")>-1){
            JSONArray scqdArray=JSON.parseArray(JSON.parseObject(extJson).getString("scqd").replaceAll("\\\\\\\"","\\\"").replaceAll("^\"|\"$", ""));

            if(zipFile.exists()){
                zipFile.delete();
            }
            if(!zipFile.getParentFile().exists()){
                zipFile.getParentFile().mkdirs();
            }
            JSONArray imgUrlArray=null;
            JSONObject imgJson=null;
            File zipChildFile =null;

            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
                for (int i = 0; i < scqdArray.size(); i++) {
                    if(StringUtils.isNotBlank(qdmc)){
                        if( qdmc.equals(scqdArray.getJSONObject(i).getString("qdmc")) && scqdArray.getJSONObject(i).containsKey("imgUrl")){
                            imgUrlArray=scqdArray.getJSONObject(i).getJSONArray("imgUrl");
                            for (int j = 0; j < imgUrlArray.size(); j++) {
                                zipChildFile=new File(uploadFilePath+imgUrlArray.getString(j).replace(uploadFileUrl,""));
                                addToZipFile(zipChildFile,scqdArray.getJSONObject(i).getString("qdmc")+"/"+"素材"+j+"_"+zipChildFile.getName(),zos);
                            }
                        }
                    }else{
                        if(scqdArray.getJSONObject(i).containsKey("imgUrl")){
                            imgUrlArray=scqdArray.getJSONObject(i).getJSONArray("imgUrl");
                            for (int j = 0; j < imgUrlArray.size(); j++) {
                                zipChildFile=new File(uploadFilePath+imgUrlArray.getString(j).replace(uploadFileUrl,""));
                                addToZipFile(zipChildFile,scqdArray.getJSONObject(i).getString("qdmc")+"/"+"素材"+j+"_"+zipChildFile.getName(),zos);
                            }
                        }
                    }

                }
            }catch (Exception e) {
                log.error("生成压缩文件失败",e);
            }
            // 返回压缩包给用户
            return OperaResult.putSuccessResult("fileUrl", uploadFileUrl+"/scqd/"+contextId+".zip");
        }else{
            return null;
        }

    }

    private void addToZipFile(File file, String fileName, ZipOutputStream zos) throws FileNotFoundException, IOException {
        FileInputStream fis = new FileInputStream(file);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zos.putNextEntry(zipEntry);

        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zos.write(bytes, 0, length);
        }
        fis.close();
    }


    @ApiOperation(value = "查看pdf内容", notes = "查看pdf内容")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String"),
            @ApiImplicitParam(name = "wjm", paramType = "query", value = "文件名", dataType = "String"),
    })
    @PostMapping("/getJcPdfContent")
    @ResponseBody
    public String getJcPdfContent(String wjm,String a) throws Exception {
        String jascFilesPath=uploadFilePath+"/ai/jasc/ckjafiles";//参考教案存放路径

        String wjmPdf=wjm;//pdf文件名
        String wjmDocx=wjm.replace(".pdf",".docx");//对应的docx文件名
        String wjmDoc=wjm.replace(".pdf",".doc");//对应的doc文件名

        File jcFile=null;

        //逐个校验文件是否存在
        if(new File(jascFilesPath+"/"+wjmPdf).exists()){
            jcFile=new File(jascFilesPath+"/"+wjmPdf);
            return FileContentReader.readPdf(jcFile);
        }else if(new File(jascFilesPath+"/"+wjmDocx).exists()){
            jcFile=new File(jascFilesPath+"/"+wjmDocx);
            return WordToMdConverter.doc2mdByPythonUtil(jcFile);
        }else if(new File(jascFilesPath+"/"+wjmDoc).exists()){
            jcFile=new File(jascFilesPath+"/"+wjmDoc);
            return FileContentReader.readWord(jcFile);
        }

        return null;
    }

}