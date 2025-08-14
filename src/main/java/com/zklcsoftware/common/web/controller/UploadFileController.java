package com.zklcsoftware.common.web.controller;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.zklcsoftware.basic.util.StringUtil;
import com.zklcsoftware.basic.util.UuidUtil;
import com.zklcsoftware.common.web.util.SXSSFWorkBookUtil;
import com.zklcsoftware.common.dto.OperaResult;
import com.zklcsoftware.common.web.ExtBaseController;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Controller
@Api(tags = "上传接口")
@RequestMapping(path = {"", "/api"})
public class UploadFileController extends ExtBaseController {
    @Value("${uploadfiledir.uploadFilePath}")
    private String uploadFilePath;//服务器路径
    @Value("${uploadfiledir.uploadFileUrl}")
    private String uploadFileUrl;//文件封存的http地址

    private static final List<String> filePaths = new ArrayList<String>();

    static {
        filePaths.add("zntImg");
        filePaths.add("tscImg");
        filePaths.add("ai");
        filePaths.add("scqd");
        filePaths.add("人工智能");
    }
    private static final String suffixs="jpg,jpeg,png,JPG,JPEG,PNG,doc,docx,txt,pdf,md,xls,xlsx,mp3,mp4,mov,mkv,avi,wmv,wav,ppt,pptx";

    @ApiOperation(value = "上传文件功能", notes = "上传文件功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "file", value = "附件", required = true, dataType = "MultipartFile", paramType = "query"),
            @ApiImplicitParam(name = "moduleName", value = "智能体图片填写zntImg,提示词图片填写tscImg", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(paramType = "query", name = "access_token")})
    @RequestMapping(value = "/common/uploadFile", method = {RequestMethod.POST})
    @ResponseBody
    public OperaResult uploadFile(@RequestParam("file") MultipartFile file, @RequestParam(required = false) String moduleName) throws Exception {

        //文件地址
        StringBuffer filePath = new StringBuffer(uploadFilePath);
        //文件url
        StringBuffer fileUrl = new StringBuffer(uploadFileUrl);

        //验证moduleName是否属于白名单内的目录
        if (!filePaths.contains(moduleName)) {
            throw new MyException("非法上传目录！");
        }

        //判断是否需要验证文件类型
        if (StringUtils.isNotBlank(suffixs)) {
            //检验文件格式是否符合格式限制
            if (!StringUtil.checkFileType(file.getOriginalFilename(), suffixs)) {
                throw new MyException("不支持的文件类型！");
            }
        }

        //如果模块名称不为空，则追加模块到上传地址上
        if (StringUtils.isNotBlank(moduleName)) {
            filePath.append("/" + moduleName + "/");
        } else {
            filePath.append("/project_architecture/");
        }

        if (StringUtils.isNotBlank(moduleName)) {
            fileUrl.append("/" + moduleName + "/");
        } else {
            fileUrl.append("/project_architecture/");
        }

        //如果不存在则创建
        StringUtil.createDirectory(filePath.toString());
        //原名称
        String originalFilename = file.getOriginalFilename();
        //新名称(uuid随机数加上后缀名)
        String newfileName = UuidUtil.get32UUID() + originalFilename.substring(originalFilename.lastIndexOf("."));
        //新的图片
        File newfile = new File(filePath + newfileName);
        //把内存图片写入磁盘中
        file.transferTo(newfile);
        //返回url到客户端
        OperaResult operaResult=new OperaResult();
        operaResult.getData().put("fileName", originalFilename);
        operaResult.getData().put("fileUrl", fileUrl + newfileName);
        operaResult.getData().put("filePath", moduleName+"/"+newfileName);
        return operaResult;
    }

    @ApiOperation(value = "下载文件", notes = "下载文件功能")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "access_token")})
    @GetMapping(value = "/busFiles/**")
    @CrossOrigin(origins = "*") // 允许所有来源，也可以指定特定的域名
    @ResponseBody
    public FileSystemResource busFiles(HttpServletRequest request, HttpServletResponse response) throws Exception {

        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        int pos = path.lastIndexOf("/busFiles/");
        path = path.substring(pos + 10);
        String filePath = org.springframework.util.StringUtils.trimWhitespace(path);

        /*if(!filePath.toLowerCase().endsWith(".jpg") && !filePath.toLowerCase().endsWith(".png") && !filePath.endsWith(".png") && !filePath.toLowerCase().endsWith(".jpeg")){
            throw new MyException("非法的文件格式！");
        }*/

        // 规范化用户输入的路径
        Path filePathNormalized = Paths.get(uploadFilePath+filePath).normalize();
        String realFilePath="";//文件实际路径
        for (String spath : filePaths) {
            //判断是否是安全目录
            if(filePathNormalized.startsWith(uploadFilePath+spath+"/")){
                realFilePath=filePathNormalized.toString();
                break;
            }
        }
        File file=new File(realFilePath);
        if(StringUtils.isNotBlank(realFilePath) && file.exists()){

            // 在客户端显示的文件名，尽量避免中文文件名，如必须，则需要根据不同浏览器分别处理
            try {
                String fileName = file.getName();
                if(StringUtils.isNotBlank(request.getParameter("fileName"))){
                    fileName= URLEncoder.encode(request.getParameter("fileName"));
                }

                // 将附件名称转换成utf-8，后下载
                response.setHeader("Content-Disposition",
                        "attachment;filename=" + new String(fileName.getBytes("utf-8"), "iso-8859-1"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            return new FileSystemResource(realFilePath);
        }else {
            throw new MyException("文件不存在！");
        }
    }

    @ApiOperation(value="下载excel根据数据", notes="下载excel根据数据")
    @RequestMapping(value = "/common/downloadExcel", method = {RequestMethod.GET})
    public void downloadExcel(String heads, String data, String fileName, HttpServletRequest request, HttpServletResponse response){
        try {
            SXSSFWorkBookUtil.downloadExcel(heads, data, fileName, request,response);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public OperaResult runtimeExceptionHandler(HttpServletRequest req, Exception e) {
        log.error("服务异常", e);
        return OperaResult.putFailResult("error", e.getMessage());
    }
}
