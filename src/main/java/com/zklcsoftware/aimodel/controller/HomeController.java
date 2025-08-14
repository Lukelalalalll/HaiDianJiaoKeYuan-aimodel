package com.zklcsoftware.aimodel.controller;

import com.zklcsoftware.aimodel.util.ConstantUtil;
import com.zklcsoftware.common.dto.OperaResult;
import com.zklcsoftware.common.web.ExtBaseController;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author zhushaog
 * @version 1.0
 * @className HomeController
 * @description
 * @date 2024/8/5 14:10
 **/
@Slf4j
@Controller
@Api(tags = "测试流鉴权")
@RequestMapping(path = {""})
public class HomeController extends ExtBaseController {
    @GetMapping(value = {"/getM3u8/{nodeId}/{streamId}.m3u8"})
    public void getM3u8(HttpServletResponse response,@PathVariable("nodeId") String nodeId,@PathVariable("streamId") String streamId){
        response.setHeader("X-Accel-Redirect", "/cloudfile/"+nodeId+"/m3u8/" +streamId+".m3u8");
    }

    @GetMapping(value = {"/getTs/{nodeId}/{streamTsIndex}.ts"})
    public void getTs(HttpServletResponse response,@PathVariable("nodeId") String nodeId,@PathVariable("streamTsIndex") String streamTsIndex){
        response.setHeader("X-Accel-Redirect", "/cloudfile/"+nodeId+"/ts/" +streamTsIndex+".ts");
    }

    @GetMapping(value = {"/getMp4/{nodeId}/{streamId}.mp4"})
    public void getMp4(HttpServletResponse response,@PathVariable("nodeId") String nodeId,@PathVariable("streamId") String streamId){
        response.setHeader("X-Accel-Redirect", "/cloudfile/"+nodeId+"/mp4/" +streamId+".mp4");
    }

    @GetMapping(value = {"/test/cloudfile/{nodeId}/{type}/{streamId}"})
    public void test(HttpServletResponse response,@PathVariable("nodeId") String nodeId,@PathVariable("type") String type,@PathVariable("streamId") String streamId){
        log.info("请求参数 nodeId:{},type:{},streamId:{}",nodeId,type,streamId);
    }

    @Value("${sys.netname}")
    String targetUrl;//目标系统的URL
    @GetMapping("/")
    public void index(HttpServletResponse response) throws IOException {
        String userTypeCode=this.getUserTypeCode();
        if((ConstantUtil.USER_TYPE_101007+"").equals(userTypeCode)){
            response.sendRedirect(targetUrl+"/schoolcloud-webdesktop/");
        }else{
            response.sendRedirect("index.html");
        }
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