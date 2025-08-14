package com.zklcsoftware.aimodel.controller;

import com.alibaba.fastjson.JSONObject;
import com.zklcsoftware.aimodel.domain.*;
import com.zklcsoftware.aimodel.service.*;
import com.zklcsoftware.aimodel.util.ConstantUtil;
import com.zklcsoftware.aimodel.util.FfmpegVideoConcatenationUtils;
import com.zklcsoftware.basic.util.DateUtil;
import com.zklcsoftware.common.dto.OperaResult;
import com.zklcsoftware.common.web.ExtBaseController;
import com.zklcsoftware.common.web.util.HttpClients;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Slf4j
@Controller
@Api(tags = "AI数字人管理器")
@RequestMapping(path = {"", "/api"})
public class AiShuzirenController extends ExtBaseController {

    @Autowired
    TAiMicroCourseService aiMicroCourseService;
    @Autowired
    TAiShuzirenPptService tAiShuzirenPptService;
    @Autowired
    TAiShuzirenXingxiangService tAiShuzirenXingxiangService;
    @Autowired
    private TAiShuzirenService tAiShuzirenService;
    @Autowired
    TShuzirenServiceService tShuzirenServiceService;
    @Value("${uploadfiledir.uploadFilePath}")
    private String uploadFilePath;//服务器路径
    @Value("${uploadfiledir.uploadFileUrl}")
    private String uploadFileUrl;//文件封存的http地址

    @ApiOperation(value = "获取用户微课列表", notes = "获取用户微课列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String"),
    })
    @ResponseBody
    @PostMapping(value = {"/queryUserMicroCourseList"})
    public OperaResult queryUserMicroCourseList() {
        String userGuid = this.getUserGuid();
        List<TAiMicroCourse> list = aiMicroCourseService.findByCreateUserIdAndIsDelOrderByCreateDateDesc(userGuid, 0);
        for (TAiMicroCourse course : list) {
            if(course.getVideoZt() == 2){
//                course.setVideoUrl(uploadFileUrl + "/" + course.getVideoUrl());
                course.setVideoUrlView(uploadFileUrl + "/" + course.getVideoUrl());
            }else if(course.getVideoZt() == 1){
                List<TAiShuzirenPpt> pptList = tAiShuzirenPptService.findByCourseIdAndIsDelOrderByPptXh(course.getCourseId(), ConstantUtil.IS_DEL_0);
                int textNum = 0;
                for (TAiShuzirenPpt ppt : pptList) {
                    if(ppt.getContent() != null && ppt.getContent().length() > 0){
                        textNum += ppt.getContent().length();
                    }
                }
                // 根据每页 30 个字的标准计算所需总页数，使用 Math.ceil 向上取整
                int totalPages = (int) Math.ceil((double) textNum / 30);
                // 每页耗时为 60 毫秒，计算总预估时间（单位：毫秒）
                long totalEstimatedTime = totalPages * 60_000L; // 单位毫秒

                // 计算已耗时
                long elapsedTime;
                if(course.getUpdatedate() == null){
                    elapsedTime = System.currentTimeMillis() - course.getCreateDate().getTime();
                }else{
                    elapsedTime = System.currentTimeMillis() - course.getUpdatedate().getTime();
                }
                // 剩余时间 = 总预估时间 - 已耗时
                long remainingTime = totalEstimatedTime - elapsedTime;

                // 将剩余时间转换为“秒”单位，并进一步拆分为分钟和秒数
                long totalSeconds = remainingTime / 1000;
                long minutes = totalSeconds / 60;
                long seconds = totalSeconds % 60;
                String formattedRemainingTime = String.format("%d分%d秒", minutes, seconds);
                course.setDes("预计还需" + formattedRemainingTime);
                aiMicroCourseService.save(course);
            }
        }
        return OperaResult.putSuccessResult("list", list);
    }

    @ApiOperation(value = "微课详情", notes = "获取微课详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String"),
            @ApiImplicitParam(name = "courseId", paramType = "query", value = "微课id", dataType = "String"),
    })
    @ResponseBody
    @PostMapping(value = {"/microCourseView"})
    public OperaResult microCourseView(String courseId) {
        //查询微课信息
        TAiMicroCourse course = aiMicroCourseService.findById(courseId);
        //查询微课图片信息
        List<TAiShuzirenPpt> pptList = tAiShuzirenPptService.findByCourseIdAndIsDelOrderByPptXh(courseId, ConstantUtil.IS_DEL_0);
        for (TAiShuzirenPpt ppt : pptList) {
            ppt.setPptUrl(uploadFileUrl + "/" + ppt.getPptUrl());
            ppt.setVideoUrl(uploadFileUrl + "/" + ppt.getVideoUrl());
            ppt.setCover(uploadFileUrl + "/" + ppt.getCover());
        }
        course.setPptList(pptList);
        return OperaResult.putSuccessResult("course", course);
    }

    @ApiOperation(value = "微课保存", notes = "微课保存")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String"),
    })
    @ResponseBody
    @PostMapping(value = {"/microCourseSave"})
    public OperaResult microCourseSave(@RequestBody TAiMicroCourse course) {
        return aiMicroCourseService.microCourseSave(course, this.getUserGuid(), this.getUName());
    }

    @ApiOperation(value = "微课视频合成", notes = "微课视频合成")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String"),
            @ApiImplicitParam(name = "courseId", paramType = "query", value = "微课id", dataType = "String"),
    })
    @ResponseBody
    @PostMapping(value = {"/microCourseVideoConcatenation"})
    public OperaResult microCourseVideoConcatenation(String courseId) {
        String userGuid = this.getUserGuid();
        String uName = this.getUName();
        return aiMicroCourseService.microCourseVideoConcatenation(courseId, userGuid, uName);
    }

    @ApiOperation(value = "微课删除", notes = "微课删除")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String"),
            @ApiImplicitParam(name = "courseId", paramType = "query", value = "主键id", dataType = "String"),
    })
    @ResponseBody
    @PostMapping(value = {"/microCourseDel"})
    public OperaResult microCourseDel(String courseId) {
        TAiMicroCourse microCourse = aiMicroCourseService.findById(courseId);
        microCourse.setIsDel(ConstantUtil.IS_DEL_1);
        microCourse.setUpdateUserId(this.getUserGuid());
        microCourse.setUpdateUserName(this.getUName());
        microCourse.setUpdatedate(new Date());
        aiMicroCourseService.save(microCourse);
        return OperaResult.putSuccessResult("success",true);
    }

    @ApiOperation(value = "获取用户数字人形象列表", notes = "获取用户数字人形象列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String"),
    })
    @ResponseBody
    @PostMapping(value = {"/queryUserShuzirenXingxiangList"})
    public OperaResult queryUserShuzirenXingxiangList() {
        String userGuid = this.getUserGuid();
        return OperaResult.putSuccessResult("list", tAiShuzirenXingxiangService.queryUserShuzirenXingxiangList(userGuid));
    }

    @ApiOperation(value = "数字人形象保存", notes = "数字人形象保存")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String"),
            @ApiImplicitParam(name = "fileUrl", paramType = "query", value = "文件地址", dataType = "String"),
            @ApiImplicitParam(name = "sfkt", paramType = "query", value = "是否抠图", dataType = "int"),
    })
    @ResponseBody
    @PostMapping(value = {"/shuzirenXingxiangSave"})
    public OperaResult shuzirenXingxiangSave(String fileUrl, Integer sfkt) {
        String userGuid = this.getUserGuid();
        String uName = this.getUName();
        return tAiShuzirenXingxiangService.shuzirenXingxiangSave(fileUrl, sfkt, userGuid, uName);
    }

    @ApiOperation(value = "数字人形象删除", notes = "数字人形象删除")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String"),
            @ApiImplicitParam(name = "xingxiangId", paramType = "query", value = "形象id", dataType = "String"),
    })
    @ResponseBody
    @PostMapping(value = {"/shuzirenXingxiangDel"})
    public OperaResult shuzirenXingxiangDel(String xingxiangId) {
        TAiShuzirenXingxiang xingxiang = tAiShuzirenXingxiangService.findById(xingxiangId);
        xingxiang.setIsDel(ConstantUtil.IS_DEL_1);
        tAiShuzirenXingxiangService.save(xingxiang);
        return OperaResult.putSuccessResult("success",true);
    }

    @ApiOperation(value = "数字人查询", notes = "数字人查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String"),
    })
    @ResponseBody
    @PostMapping("/digitalPersonList")
    public OperaResult digitalPersonList() {
        List<TAiShuziren> szrList = tAiShuzirenService.findByCreateUserIdAndIsValidOrderByCreateDateDesc(this.getUserGuid(), 1);
        for (TAiShuziren szr : szrList) {
            szr.setTemplateFile(uploadFileUrl + "/" + szr.getTemplateFile());
            szr.setTemplateFileVideo(uploadFileUrl + "/" + szr.getTemplateFileVideo());
            szr.setTemplateFileAudio(szr.getTemplateFileAudio());
            szr.setAsrFormatAudioUrl(uploadFileUrl + "/" + szr.getAsrFormatAudioUrl());
            szr.setShuzirenFile(uploadFileUrl + "/" + szr.getShuzirenFile());
        }
        return OperaResult.putSuccessResult("szrList", szrList);
    }

    @ApiOperation(value = "数字人删除", notes = "数字人删除")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String"),
            @ApiImplicitParam(name = "id", paramType = "query", value = "主键id", dataType = "String"),
    })
    @ResponseBody
    @PostMapping("/digitalPersonDel")
    public OperaResult digitalPersonDel(String id) {
        TAiShuziren shuziren = tAiShuzirenService.findById(id);
        shuziren.setIsValid(0);
        tAiShuzirenService.save(shuziren);
        return OperaResult.putSuccessResult("success",true);
    }

    /*@ApiOperation(value = "数字人视频上传", notes = "数字人视频上传")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String"),
            @ApiImplicitParam(name = "text", paramType = "query", value = "视频描述", dataType = "String"),
    })
    @ResponseBody
    @PostMapping("/digitalPersonVideoUpload")
    public OperaResult digitalPersonVideoPpload(@RequestPart("file") MultipartFile file, String text){
        log.info("数字人视频上传");
        OperaResult operaResult=new OperaResult();
        if (file == null) {
            operaResult.setResultCode(1);
            operaResult.setResultDesc("上传文件为空");
            return operaResult;
        }

        if (StringUtils.isEmpty(text)) {
            operaResult.setResultCode(1);
            operaResult.setResultDesc("文本不能为空");
            return operaResult;
        }

        if (!file.getOriginalFilename().endsWith(".mp4")) {
            return OperaResult.putFailResult("error", "请上传mp4文件");
        }

        TAiShuziren tAiShuziren = new TAiShuziren();
        try {
            String filePath = "ai/digitalPerson/" + DateUtil.formatDateByFormat(new Date(), "yyyy-MM-dd") + "/";
            // 获取文件的原始文件名
            String originalFilename = file.getOriginalFilename();
            // 定义文件保存的路径
            File dest = new File(uploadFilePath + filePath + originalFilename);
            // 创建上传目录
            if (!dest.getParentFile().exists()) {
                dest.getParentFile().mkdirs();
            }
            // 将文件保存到指定路径
            file.transferTo(dest);
            tAiShuziren.setTemplateFile(filePath + originalFilename);

            log.info("提取文件开始");
            //提取无声视频
            String silentVideo = UUID.randomUUID().toString().replaceAll("-", "") + ".mp4";
            Process start = new ProcessBuilder("ffmpeg", "-i", dest.getAbsolutePath(), "-c:v", "copy", "-an", uploadFilePath + filePath + silentVideo).start();
            start.waitFor();//等待命令执行完成
            //提取音频为 MP3
            String audio = UUID.randomUUID().toString().replaceAll("-", "") + ".mp3";
            Process start1 = new ProcessBuilder("ffmpeg", "-i", dest.getAbsolutePath(), "-q:a", "0", "-map", "a", uploadFilePath + filePath + audio).start();
            start1.waitFor();//等待命令执行完成
            log.info("提取文件结束");
            log.info("silentVideo：" + uploadFilePath + filePath + silentVideo);
            log.info("audio：" + uploadFilePath + filePath + audio);

            tAiShuziren.setTemplateFileVideo(filePath + silentVideo);
            tAiShuziren.setTemplateFileAudio(filePath + audio);

            log.info("模型训练开始");
            org.apache.http.impl.client.CloseableHttpClient mxHttpClient = org.apache.http.impl.client.HttpClients.createDefault();
            org.apache.http.client.methods.HttpPost mxPost = new org.apache.http.client.methods.HttpPost("http://117.50.176.155:18180/v1/preprocess_and_tran");

            log.info("reference_audio:" + uploadFileUrl + tAiShuziren.getTemplateFileAudio());
            JSONObject mxObject = new JSONObject();
            mxObject.put("format", "mp3");
            mxObject.put("reference_audio", uploadFileUrl + "/" + tAiShuziren.getTemplateFileAudio());
            mxObject.put("lang", "zh");

            StringEntity mxEntity = new StringEntity(mxObject.toString());
            mxPost.setEntity(mxEntity);
            mxPost.setHeader("Content-Type", "application/json");

            // 执行请求 模型训练
            org.apache.http.client.methods.CloseableHttpResponse mxResponse = mxHttpClient.execute(mxPost);
            String mxResult = org.apache.http.util.EntityUtils.toString(mxResponse.getEntity());
            log.info("模型训练结束" + mxResult);
            if(StringUtils.isNotEmpty(mxResult)){
                JSONObject mxObject1 = JSONObject.parseObject(mxResult);
                if(mxObject1.get("code").equals(-1)){
                    return OperaResult.putFailResult("error", mxObject1.get("msg"));
                }
                String asrFormatAudioUrl = (String) mxObject1.get("asr_format_audio_url");//语音文件
                String referenceAudioText = (String) mxObject1.get("reference_audio_text");//语音文件对应的文字

                tAiShuziren.setAsrFormatAudioUrl(asrFormatAudioUrl);
                tAiShuziren.setReferenceAudioText(referenceAudioText);

                log.info("音频合成开始");
                JSONObject audioObject = new JSONObject();
                audioObject.put("speaker", UUID.randomUUID().toString().replaceAll("-", ""));// 提供一个 UUID
                audioObject.put("text", text);//用户输入的内容
                audioObject.put("format", "wav");
                audioObject.put("topP", 0.7);
                audioObject.put("max_new_tokens", 1024);
                audioObject.put("chunk_length", 100);
                audioObject.put("repetition_penalty", 1.2);
                audioObject.put("temperature", 0.7);
                audioObject.put("need_asr", false);
                audioObject.put("streaming", false);
                audioObject.put("is_fixed_seed", 0);
                audioObject.put("is_norm", 0);
                audioObject.put("reference_audio", asrFormatAudioUrl); // 模型训练返回值的asr_format_audio_url
                audioObject.put("reference_text", referenceAudioText); // 模型训练返回值的reference_audio_text
                //音频合成
                org.apache.http.impl.client.CloseableHttpClient audioHttpClient = org.apache.http.impl.client.HttpClients.createDefault();
                org.apache.http.client.methods.HttpPost audioPost = new org.apache.http.client.methods.HttpPost("http://117.50.176.155:18180/v1/invoke");

                StringEntity audioEntity = new StringEntity(audioObject.toString());
                audioPost.setEntity(audioEntity);
                audioPost.setHeader("Content-Type", "application/json");

                // 执行请求 模型训练
                org.apache.http.client.methods.CloseableHttpResponse audioResponse = audioHttpClient.execute(audioPost);
                HttpEntity audioEntity1 = audioResponse.getEntity();//返回流文件
                //保存流文件
                String audioFile = uploadFilePath + filePath + UUID.randomUUID().toString().replaceAll("-", "") + ".wav";
                try (FileOutputStream fos = new FileOutputStream(audioFile);
                     InputStream is = audioEntity1.getContent()) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                }
                log.info("音频合成结束" + audioFile);

                log.info("视频合成开始");
                //提供一个UUID，方面后面查询合成进度
                String videoCode = UUID.randomUUID().toString().replaceAll("-", "");
                JSONObject videoObject = new JSONObject();
                videoObject.put("audio_url", audioFile);// 上一步保存下来的音频文件(文件名最好是UUID)
                videoObject.put("video_url", uploadFileUrl + "/" + filePath + audio);// 准备工作中的静音视频文件(文件名最好是UUID)
                videoObject.put("code", videoCode);// 提供一个UUID，方面后面查询合成进度
                videoObject.put("chaofen", 0);
                videoObject.put("watermark_switch", 0);
                videoObject.put("pn", 1);
                //视频合成
                org.apache.http.impl.client.CloseableHttpClient videoHttpClient = org.apache.http.impl.client.HttpClients.createDefault();
                org.apache.http.client.methods.HttpPost videoPost = new org.apache.http.client.methods.HttpPost("http://117.50.176.155:8383/easy/submit");

                StringEntity videoEntity = new StringEntity(videoObject.toString());
                videoPost.setEntity(videoEntity);
                videoPost.setHeader("Content-Type", "application/json");

                // 执行请求 模型训练
                org.apache.http.client.methods.CloseableHttpResponse videoResponse = videoHttpClient.execute(videoPost);
                String videoResult = org.apache.http.util.EntityUtils.toString(videoResponse.getEntity());
                log.info("视频合成结束" + videoResult);
                if(StringUtils.isNotEmpty(videoResult)){
                    JSONObject videoObject1 = JSONObject.parseObject(videoResult);
                    Boolean success = (Boolean) videoObject1.get("success");
                    if(success){
                        tAiShuziren.setCode(videoCode);
                        tAiShuziren.setZt(1);//生成中
                    }
                }
            }else{
                return OperaResult.putFailResult("error","模型训练失败");
            }
        }catch (Exception e){
            log.error("文件上传异常",e);
            return OperaResult.putFailResult("error","文件处理出错: " + e.getMessage());
        }
        tAiShuziren.setIsValid(1);
        tAiShuziren.setCreateUserId(this.getUserGuid());
        tAiShuziren.setCreateUserName(this.getUName());
        tAiShuziren.setCreateDate(new Date());
        tAiShuzirenService.save(tAiShuziren);

        OperaResult.putSuccessResult("success",true);
        return operaResult;
    }*/

    @ApiOperation(value = "数字人视频上传", notes = "数字人视频上传")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "access_token", paramType = "query", value = "访问令牌", dataType = "String"),
            @ApiImplicitParam(name = "text", paramType = "query", value = "视频描述", dataType = "String"),
            @ApiImplicitParam(name = "fileUrl", paramType = "query", value = "文件地址", dataType = "String"),
            @ApiImplicitParam(name = "xingxiangId", paramType = "query", value = "形象id", dataType = "String")
    })
    @ResponseBody
    @PostMapping("/digitalPersonVideoUpload")
    public OperaResult digitalPersonVideoPpload(String text, String fileUrl, String xingxiangId){
        log.info("数字人视频上传");
        if (StringUtils.isEmpty(text)) {
            return OperaResult.putFailResult("error","文本不能为空");
        }
        String userGuid = this.getUserGuid();
        String uName = this.getUName();
        return tAiShuzirenService.digitalPersonVideoUpload(text, fileUrl, xingxiangId, userGuid, uName);
    }

}