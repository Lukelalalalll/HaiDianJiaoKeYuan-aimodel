package com.zklcsoftware.aimodel.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.zklcsoftware.aimodel.domain.TShuzirenService;
import com.zklcsoftware.aimodel.repository.TAiShuzirenXingxiangRepository;
import com.zklcsoftware.aimodel.service.TShuzirenServiceService;
import com.zklcsoftware.aimodel.util.ConstantUtil;
import com.zklcsoftware.aimodel.util.FfmpegVideoMattingUtil;
import com.zklcsoftware.basic.util.DateUtil;
import com.zklcsoftware.common.dto.OperaResult;
import okhttp3.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.zklcsoftware.basic.service.impl.BaseServiceImpl;
import com.zklcsoftware.aimodel.domain.TAiShuzirenXingxiang;
import com.zklcsoftware.aimodel.service.TAiShuzirenXingxiangService;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.*;

@Service
@Transactional
public class TAiShuzirenXingxiangServiceImpl extends BaseServiceImpl<TAiShuzirenXingxiang, String> implements TAiShuzirenXingxiangService {

    @Autowired
    private TAiShuzirenXingxiangRepository tAiShuzirenXingxiangRepository;
    @Autowired
    private TShuzirenServiceService tShuzirenServiceService;
    @Value("${uploadfiledir.uploadFilePath}")
    private String uploadFilePath;//服务器路径
    @Value("${uploadfiledir.uploadFileUrl}")
    private String uploadFileUrl;//文件封存的http地址

    @Override
    public List<TAiShuzirenXingxiang> findByVideoZtAndIsDel(Integer videoZt, Integer isdel) {
        return tAiShuzirenXingxiangRepository.findByVideoZtAndIsDel(videoZt, isdel);
    }

    @Override
    public List<TAiShuzirenXingxiang> queryUserShuzirenXingxiangList(String userGuid) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userGuid);
        List<TAiShuzirenXingxiang> list = tAiShuzirenXingxiangRepository.findListObj("queryUserShuzirenXingxiangList", params, TAiShuzirenXingxiang.class);
        for (TAiShuzirenXingxiang xingxiang : list) {
            xingxiang.setReferenceAudio(uploadFileUrl + "/" + xingxiang.getReferenceAudio());
            xingxiang.setReferenceText(uploadFileUrl + "/" + xingxiang.getReferenceText());
            xingxiang.setSilentVideo(uploadFileUrl + "/" + xingxiang.getSilentVideo());
            xingxiang.setCover(uploadFileUrl + "/" + xingxiang.getCover());
        }
        return list;
    }

    @Override
    public OperaResult shuzirenXingxiangSave(String fileUrl, Integer sfkt, String userGuid, String uName) {

        //获取数字人token
        String token = ConstantUtil.sysConfig.get("digital_person_token");

        //获取数字人服务ip
        TShuzirenService serviceIp = tShuzirenServiceService.getMinServiceIp();
        if(serviceIp ==null){
            return OperaResult.putFailResult("error", "获取数字人服务ip失败");
        }

        TAiShuzirenXingxiang xingxiang = new TAiShuzirenXingxiang();
        try {
            URI uri = new URI(fileUrl);
            String filePath = "ai/digitalPerson/" + DateUtil.formatDateByFormat(new Date(), "yyyy-MM-dd") + "/";
            String suffix = uri.getPath().substring(uri.getPath().lastIndexOf(".") + 1);
            String fileName = UUID.randomUUID().toString().replaceAll("-", "") + "." + suffix;
            // 定义文件保存的路径
            File file = new File(uploadFilePath + filePath + fileName);
            // 创建上传目录
            if (!file.getParentFile().exists()) {
               file.getParentFile().mkdirs();
            }
            // 将文件保存到指定路径
            FileUtils.copyURLToFile(uri.toURL(), file);
            xingxiang.setTemplateFile(filePath + fileName);

            //截取视频一帧作为封面,如要抠图需要进行抠图处理
            String fileName2 = UUID.randomUUID().toString().replaceAll("-", "") + ".png";
            File file2 = new File(uploadFilePath + filePath + fileName2);
            file2.getParentFile().mkdirs();// 创建父目录（如果不存在）
            FfmpegVideoMattingUtil.extractBackgroundFrame(file.getPath(), file2.getPath());
            if(sfkt !=null && sfkt == 0){//是否抠图 0：否 1：是
                xingxiang.setCover(filePath + fileName2);
            }else{
                String fileName3 = UUID.randomUUID().toString().replaceAll("-", "") + ".png";
                File file3 = new File(uploadFilePath + filePath + fileName3);
                file3.getParentFile().mkdirs();// 创建父目录（如果不存在）
                // 分析背景帧颜色
                String bgColor = FfmpegVideoMattingUtil.analyzeBackgroundColor(file2.getPath());
                // 基于背景色进行抠图
                boolean b = FfmpegVideoMattingUtil.imgMatting(file2.getPath(), file3.getPath(), bgColor);
                if(b){
                    xingxiang.setCover(filePath + file3.getName());
                }
            }

            OkHttpClient client = new OkHttpClient();
            // 创建文件请求体
            okhttp3.RequestBody fileBody1 = okhttp3.RequestBody.create(MediaType.parse("application/octet-stream"), file);
            String fileName1 =  URLEncoder.encode(file.getPath(), "UTF-8");// 编码文件名以避免乱码
            String filetype1 = file.getName().substring(file.getName().lastIndexOf(".") + 1);

            // 创建多部分请求体
            okhttp3.RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("videofile", fileName1 + "." + filetype1, fileBody1)
                    .addFormDataPart("token", token)
                    .build();

            // 创建请求
            Request request = new Request.Builder()
                    .url("http://"+ serviceIp.getServiceAddress() +":5000/v1/genvideo")
                    .post(requestBody)
                    .build();
            // 打印请求头
            System.out.println("Request URL: " + request.url());
            System.out.println("Request Headers: " + request.headers());

            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new IOException("上传文件失败：" + response);
            }
            // 将响应体内容读取到一个字符串变量中
            String responseBody = response.body() != null ? response.body().string() : "";
            // 打印响应体内容
            System.out.println(responseBody);
            // 检查响应体是否为空，并解析为 JSON 对象
            JSONObject jsonObject = new JSONObject();
            if (StringUtils.isNotEmpty(responseBody)) {
                jsonObject = JSONObject.parseObject(responseBody);
                String resultMsg = (String) jsonObject.get("result_msg");
                if(jsonObject.get("result").equals("success")){
                    xingxiang.setVideoId((String) jsonObject.get("id"));
                    xingxiang.setVideoZt(1);
                    xingxiang.setProgressDec(resultMsg);
                }else{
                    xingxiang.setVideoZt(3);
                    xingxiang.setProgressDec(resultMsg);
                }
            }
        }catch (Exception e){
           e.printStackTrace();
           return OperaResult.putFailResult("error", "上传文件失败");
        }
        xingxiang.setSfkt(sfkt);
        xingxiang.setPublishRange(1);
        xingxiang.setServerIp(serviceIp.getServiceAddress());
        xingxiang.setIsDel(ConstantUtil.IS_DEL_0);
        xingxiang.setCreateUserId(userGuid);
        xingxiang.setCreateUserName(uName);
        xingxiang.setCreateDate(new Date());
        this.save(xingxiang);
        return OperaResult.putSuccessResult("success",true);
    }
}
