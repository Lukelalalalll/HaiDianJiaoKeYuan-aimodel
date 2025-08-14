package com.zklcsoftware.aimodel.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.zklcsoftware.aimodel.dto.TAiModelLayoutDTO;
import com.zklcsoftware.aimodel.service.TAiModelLayoutService;
import com.zklcsoftware.common.dto.OperaResult;
import com.zklcsoftware.common.web.ExtBaseController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/**
 * @author zcg
 * @version 1.0
 * @className HomeController
 * @description
 * @date 2025/3/22 10:46
 **/
@Slf4j
@Controller
@Api(tags = "AI布局管理器")
@RequestMapping(path = {"", "/api"})
public class AlLayoutiController extends ExtBaseController {
    @Autowired TAiModelLayoutService aiModelLayoutService;
    
    @ApiOperation(value = "获取用户布局信息", notes = "获取用户布局信息")
    @ApiImplicitParams({            
    	@ApiImplicitParam(name = "layoutId", paramType = "query", value = "布局id", dataType = "String"),
        @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String")
    })
    @ResponseBody
    @PostMapping(value = {"/getUserModelLayoutInfo"})
    public OperaResult getUserModelLayoutInfo(String layoutId) {
    	//用户类型code  101002-教师  101004-学生  其他用户类型，未开放 
    	String userTypeCode = this.getUserTypeCode();
        if("101002".equals(this.getUserTypeCode()) || "101003".equals(this.getUserTypeCode()) || "101004".equals(this.getUserTypeCode())){
        	List<TAiModelLayoutDTO> layoutInfo = aiModelLayoutService.getUserModelLayoutInfoByUserTypeCode(userTypeCode,layoutId);
        	return  OperaResult.putSuccessResult("layoutInfo", layoutInfo);
        }else{
        	return OperaResult.putFailResult("error", this.getUserType()+"用户类型，暂无权限");
        }
    }
}