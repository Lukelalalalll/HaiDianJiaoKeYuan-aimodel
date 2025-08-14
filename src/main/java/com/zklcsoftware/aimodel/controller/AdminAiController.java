package com.zklcsoftware.aimodel.controller;

import com.zklcsoftware.aimodel.domain.TAiModel;
import com.zklcsoftware.aimodel.domain.TAiSysPrompt;
import com.zklcsoftware.aimodel.domain.TAiUserPrompt;
import com.zklcsoftware.aimodel.domain.TAiUserSession;
import com.zklcsoftware.aimodel.dto.SessionlQueryDTO;
import com.zklcsoftware.aimodel.dto.TAiSysPromptDTO;
import com.zklcsoftware.aimodel.dto.TAiUserPromptDTO;
import com.zklcsoftware.aimodel.dto.TAiUserSessionDTO;
import com.zklcsoftware.aimodel.service.*;
import com.zklcsoftware.aimodel.util.ConstantUtil;
import com.zklcsoftware.aimodel.vo.TAiModelVO;
import com.zklcsoftware.aimodel.vo.TAiUserSessionModelVO;
import com.zklcsoftware.aimodel.vo.TAiUserSessionVO;
import com.zklcsoftware.aimodel.vo.TJxxVO;
import com.zklcsoftware.basic.util.In;
import com.zklcsoftware.common.dto.OperaResult;
import com.zklcsoftware.common.web.ExtBaseController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author zhushaog
 * @version 1.0
 * @className HomeController
 * @description
 * @date 2024/8/5 14:10
 **/
@Slf4j
@Controller
@Api(tags = "AI后台管理接口")
@RequestMapping(path = {"/admin", "/api/admin"})
public class AdminAiController extends ExtBaseController {
    @Autowired
    TAiModelService aiModelService;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    TAiUserSessionModelService tAiUserSessionModelService;
    @Autowired
    TAiUserSessionService tAiUserSessionService;
    @Autowired
    TAiUserPromptService tAiUserPromptService;
    @Autowired
    TAiSysPromptService tAiSysPromptService;
    @Autowired
    TAiWarnWordsContextRefService tAiWarnWordsContextRefService;


    @ApiOperation(value = "获取提示词列表", notes = "获取提示词列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tsc", paramType = "query", value = "搜索提示词名称", dataType = "Integer"),
            @ApiImplicitParam(name = "busType", paramType = "query", value = "分类字典值", dataType = "Integer"),
            @ApiImplicitParam(name = "pageable", paramType = "query", value = "分页对象", dataType = "Object"),
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String")
    })
    @ResponseBody
    @PostMapping("/queryTscList")
    public OperaResult queryTscList(String tsc,Integer busType,Pageable pageable) {
        return OperaResult.putSuccessResult("page",tAiUserPromptService.queryTscList(tsc,busType,pageable));
    }

    @ApiOperation(value = "获取智能体列表", notes = "获取智能体列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "znt", paramType = "query", value = "搜索智能体名称", dataType = "Integer"),
            @ApiImplicitParam(name = "busType", paramType = "query", value = "分类字典值", dataType = "Integer"),
            @ApiImplicitParam(name = "pageable", paramType = "query", value = "分页对象", dataType = "Object"),
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String")
    })
    @ResponseBody
    @PostMapping("/queryZntList")
    public OperaResult queryZntList(String znt, Integer busType, Pageable pageable) {
        return OperaResult.putSuccessResult("page",tAiSysPromptService.queryZntList(znt,busType,pageable,null,null,null,null));
    }

    @ApiOperation(value = "获取智能体详情", notes = "获取智能体列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "zntId", paramType = "query", value = "智能体ID", dataType = "String"),
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String")
    })
    @ResponseBody
    @PostMapping("/queryZntMes")
    public OperaResult queryZntMes(String zntId) {
        return OperaResult.putSuccessResult("sysPrompt",tAiSysPromptService.findById(zntId));
    }

    @ApiOperation(value = "保存智能体", notes = "保存智能体")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String")
    })
    @ResponseBody
    @PostMapping("/saveZnt")
    public OperaResult saveZnt(@RequestBody TAiSysPromptDTO tAiSysPromptDTO) {

        TAiSysPrompt tAiSysPrompt=null;
        //更新
        if(tAiSysPromptDTO!=null && StringUtils.isNotBlank(tAiSysPromptDTO.getId())){
            tAiSysPrompt=tAiSysPromptService.findById(tAiSysPromptDTO.getId());
            BeanUtils.copyProperties(tAiSysPromptDTO,tAiSysPrompt);
            tAiSysPrompt.setUpdateTime(new Date());
            tAiSysPrompt.setUpdateUser(this.getUserGuid());
        }else{//新增
            tAiSysPrompt=new TAiSysPrompt();
            BeanUtils.copyProperties(tAiSysPromptDTO,tAiSysPrompt);
            tAiSysPrompt.setCreateTime(new Date());
            tAiSysPrompt.setCreateUser(this.getUserGuid());
            tAiSysPrompt.setIsDel(ConstantUtil.IS_DEL_0);
            tAiSysPrompt.setReviewStatus(ConstantUtil.REVIEW_STATUS_1);//管理员添加的智能体 直接审核通过
            tAiSysPrompt.setPublishStatus(ConstantUtil.PUBLISH_STATUS_1);//发布状态 1已发布  0未发布 2已停用
            tAiSysPrompt.setPublishRange(1);//发布范围  全校
            tAiSysPrompt.setSysPromptType(ConstantUtil.SYS_PROMPT_TYPE_2);//1-智能体应用  2-用户智能体 3-工具智能体
            tAiSysPrompt.setChName(this.getUName());
            tAiSysPrompt.setUserName(this.getUserLoginName());
        }
        tAiSysPromptService.save(tAiSysPrompt);
        return OperaResult.putSuccessResult("success",true);
    }

    @ApiOperation(value = "智能体审核", notes = "智能体审核")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "zntId", paramType = "query", value = "智能体ID", dataType = "String"),
            @ApiImplicitParam(name = "reviewStatus", paramType = "query", value = "审核状态(0待审核 1审核通过 2审核不通过)", dataType = "Integer"),
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String")
    })
    @ResponseBody
    @PostMapping("/reviewZnt")
    public OperaResult reviewZnt(String zntId,Integer reviewStatus ) {
        TAiSysPrompt tAiSysPrompt=tAiSysPromptService.findById(zntId);
        tAiSysPrompt.setReviewStatus(reviewStatus);
        if(reviewStatus==ConstantUtil.REVIEW_STATUS_1){
            tAiSysPrompt.setPublishStatus(ConstantUtil.PUBLISH_STATUS_1);//发布
        }
        tAiSysPromptService.save(tAiSysPrompt);
        return OperaResult.putSuccessResult("success",true);
    }

    @ApiOperation(value = "智能体审核", notes = "智能体审核")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "zntId", paramType = "query", value = "智能体ID", dataType = "String"),
            @ApiImplicitParam(name = "reviewStatus", paramType = "query", value = "审核状态(0待审核 1审核通过 2审核不通过)", dataType = "Integer"),
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String")
    })
    @ResponseBody
    @PostMapping("/publishZnt")
    public OperaResult publishZnt(String zntId,Integer publishStatus ) {
        TAiSysPrompt tAiSysPrompt=tAiSysPromptService.findById(zntId);
        tAiSysPrompt.setPublishStatus(publishStatus);
        tAiSysPromptService.save(tAiSysPrompt);
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
        TAiSysPrompt tAiSysPrompt=tAiSysPromptService.findById(zntId);
        tAiSysPrompt.setIsDel(ConstantUtil.IS_DEL_1);
        tAiSysPromptService.save(tAiSysPrompt);
        return OperaResult.putSuccessResult("success",true);
    }

    @ApiOperation(value = "保存提示词", notes = "保存提示词")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String")
    })
    @ResponseBody
    @PostMapping("/saveTsc")
    public OperaResult saveTsc(@RequestBody TAiUserPromptDTO tAiUserPromptDTO) {

        TAiUserPrompt tAiUserPrompt=null;
        //更新
        if(tAiUserPromptDTO!=null && StringUtils.isNotBlank(tAiUserPromptDTO.getId())){
            tAiUserPrompt=tAiUserPromptService.findById(tAiUserPromptDTO.getId());
            BeanUtils.copyProperties(tAiUserPromptDTO,tAiUserPrompt);
            tAiUserPrompt.setUpdateTime(new Date());
            tAiUserPrompt.setUpdateUser(this.getUserGuid());
            tAiUserPrompt.setPublishStatus(ConstantUtil.PUBLISH_STATUS_1);//发布状态 1已发布 0未发布
        }else{//新增
            tAiUserPrompt=new TAiUserPrompt();
            BeanUtils.copyProperties(tAiUserPromptDTO,tAiUserPrompt);
            tAiUserPrompt.setCreateTime(new Date());
            tAiUserPrompt.setCreateUser(this.getUserGuid());
            tAiUserPrompt.setIsDel(ConstantUtil.IS_DEL_0);
            tAiUserPrompt.setPublishStatus(ConstantUtil.PUBLISH_STATUS_1);//发布状态 1已发布 0未发布
        }
        tAiUserPromptService.save(tAiUserPrompt);
        return OperaResult.putSuccessResult("success",true);
    }

    @ApiOperation(value = "删除提示词", notes = "删除提示词")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tscId", paramType = "query", value = "提示词ID", dataType = "String"),
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String")
    })
    @ResponseBody
    @PostMapping("/delTsc")
    public OperaResult delTsc(String tscId) {
        TAiUserPrompt tAiUserPrompt=tAiUserPromptService.findById(tscId);
        tAiUserPrompt.setIsDel(ConstantUtil.IS_DEL_1);
        tAiUserPromptService.save(tAiUserPrompt);
        return OperaResult.putSuccessResult("success",true);
    }

    @ApiOperation(value = "页面统计信息", notes = "页面统计信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "year", paramType = "query", value = "年份", dataType = "Integer"),
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String")
    })
    @ResponseBody
    @PostMapping("/queryTjxxList")
    public OperaResult queryTjxxList(Integer year) {

        OperaResult operaResult=new OperaResult();
        operaResult.getData().put("userTwcs",tAiUserSessionService.queryUsrTypeTjxxList(null,year));//按用户类型统计全部次数
        operaResult.getData().put("qxt_101004",tAiUserSessionService.queryUsrTypeTjxxList(ConstantUtil.USER_TYPE_101004,year));//曲线图 学生
        operaResult.getData().put("qxt_101002",tAiUserSessionService.queryUsrTypeTjxxList(ConstantUtil.USER_TYPE_101002,year));//曲线图 教师
        return operaResult;

    }



    @ApiOperation(value = "页面统计详情信息", notes = "页面统计详情信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "startDate", paramType = "query", value = "查询开始日期", dataType = "String"),
            @ApiImplicitParam(name = "endDate", paramType = "query", value = "查询结束日期", dataType = "String"),
            @ApiImplicitParam(name = "userType", paramType = "query", value = "用户类型", dataType = "Integer"),
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String"),
            @ApiImplicitParam(name = "pageable", paramType = "query", value = "分页对象", dataType = "Object"),
    })
    @ResponseBody
    @PostMapping("/queryTwxqxxList")
    public OperaResult queryTwxqxxList(String startDate,String endDate,@RequestParam(required = true) Integer userType,Pageable pageable) {
        OperaResult operaResult=new OperaResult();
        operaResult.getData().put("page",tAiUserSessionService.queryTwxqxxList(startDate,endDate,userType,pageable));//按用户类型统计全部次数
        return operaResult;
    }

    @ApiOperation(value = "页面统计-热度排行榜", notes = "页面统计-热度排行榜")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "topNum", paramType = "query", value = "显示的top值", dataType = "Integer"),
            @ApiImplicitParam(name = "userType", paramType = "query", value = "用户类型  101002-教师  101004-学生", dataType = "Integer"),
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String")
    })
    @ResponseBody
    @PostMapping("/queryHotTopList")
    public OperaResult queryHotTopList(Integer topNum,Integer userType) {
        OperaResult operaResult=new OperaResult();
        operaResult.getData().put("zntHotList",tAiUserSessionService.queryHotTopListForZnt(topNum==null?10:topNum,userType));//智能体热度排行
        operaResult.getData().put("questionHotList",tAiUserSessionService.queryHotTopListForQuestion(topNum==null?10:topNum,userType));//智能体热度排行
        return operaResult;
    }

    @ApiOperation(value = "提问预警记录查询", notes = "提问预警记录查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "startTime", paramType = "query", value = "开始时间", dataType = "String"),
            @ApiImplicitParam(name = "endTime", paramType = "query", value = "结束时间", dataType = "String"),
            @ApiImplicitParam(name = "queryStr", paramType = "query", value = "搜索关键词", dataType = "queryStr"),
            @ApiImplicitParam(name = "pageable", paramType = "query", value = "分页对象", dataType = "Object"),
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String")
    })
    @ResponseBody
    @PostMapping("/queryWarnWordsContextList")
    public OperaResult queryWarnWordsContextList(String startTime, String endTime, String queryStr, Pageable pageable) {
        return OperaResult.putSuccessResult("page",tAiWarnWordsContextRefService.queryWarnWordsContextList(startTime,endTime,queryStr,pageable));
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


}