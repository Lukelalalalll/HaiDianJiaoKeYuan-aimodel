package com.zklcsoftware.aimodel.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.zklcsoftware.aimodel.domain.TAiShuzirenXingxiang;
import com.zklcsoftware.aimodel.domain.TShuzirenService;
import com.zklcsoftware.aimodel.repository.TAiShuzirenRepository;
import com.zklcsoftware.aimodel.repository.TAiShuzirenXingxiangRepository;
import com.zklcsoftware.aimodel.repository.TShuzirenServiceRepository;
import com.zklcsoftware.aimodel.util.ConstantUtil;
import com.zklcsoftware.basic.util.DateUtil;
import com.zklcsoftware.common.dto.OperaResult;
import com.zklcsoftware.common.web.util.HttpClients;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.zklcsoftware.basic.service.impl.BaseServiceImpl;
import com.zklcsoftware.aimodel.domain.TAiShuziren;
import com.zklcsoftware.aimodel.service.TAiShuzirenService;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Transactional
public class TAiShuzirenServiceImpl extends BaseServiceImpl<TAiShuziren, String> implements TAiShuzirenService {

    @Autowired
    private TAiShuzirenRepository tAiShuzirenRepository;
    @Autowired
    private TShuzirenServiceRepository tShuzirenServiceRepository;
    @Autowired
    private TAiShuzirenXingxiangRepository tAiShuzirenXingxiangRepository;
    @Value("${uploadfiledir.uploadFilePath}")
    private String uploadFilePath;//服务器路径
    @Value("${uploadfiledir.uploadFileUrl}")
    private String uploadFileUrl;//文件封存的http地址

    @Override
    public List<TAiShuziren> findByZtAndIsValid(Integer zt, Integer isValid) {
        return tAiShuzirenRepository.findByZtAndIsValid(zt, isValid);
    }

    @Override
    public List<TAiShuziren> findByCreateUserIdAndIsValidOrderByCreateDateDesc(String userGuid, Integer isValid) {
        return tAiShuzirenRepository.findByCreateUserIdAndIsValidOrderByCreateDateDesc(userGuid, isValid);
    }

    @Override
    public OperaResult digitalPersonVideoUpload(String text, String fileUrl, String xingxiangId, String userGuid, String uName) {
        OperaResult operaResult=new OperaResult();
        String token = ConstantUtil.sysConfig.get("digital_person_token");//获取数字人token
        //查询已启用的服务器
        List<TShuzirenService> serviceList = tShuzirenServiceRepository.findByStatus(1);
        if(serviceList.size() == 0){
            return OperaResult.putFailResult("error","暂无服务器使用");
        }
        int minNum = 0;//服务器最小任务数
        TShuzirenService tShuzirenService = new TShuzirenService();
        for(int i=0; i<serviceList.size(); i++){

            int count = 0;//统计服务使用状态

            try {
                String url = "http://"+ serviceList.get(i).getServiceAddress() +":5000/v1/status";
                Map dataMap=new HashMap();
                dataMap.put("token", token);
                String result = HttpClients.get(url, dataMap);
                if(StringUtils.isNotEmpty(result)){
                    JSONObject jsonObject = JSONObject.parseObject(result);
                    //count = (int) jsonObject.get("queue");
                    count = (int) jsonObject.get("time");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            //将第一个ip使用数作为最小数
            if(i == 0){
                minNum = count;
                tShuzirenService = serviceList.get(i);
            }

            //使用数小于最小数，则替换
            if(count < minNum){
                minNum = count;
                tShuzirenService = serviceList.get(i);
            }

            //当队列数为0时直接使用该服务器，跳出循环
            if(count == 0){
                break;
            }
        }

        if(tShuzirenService.getServiceQuantity() > minNum){
            return OperaResult.putFailResult("error","服务器繁忙");
        }

        String ip = tShuzirenService.getServiceAddress();

        TAiShuziren tAiShuziren = new TAiShuziren();
        tAiShuziren.setContent(text);
        tAiShuziren.setServerIp(ip);
        tAiShuziren.setIsValid(1);
        tAiShuziren.setCreateUserId(userGuid);
        tAiShuziren.setCreateUserName(uName);
        tAiShuziren.setCreateDate(new Date());

        try {
            OkHttpClient client = new OkHttpClient();
            // 创建多部分请求体
            okhttp3.RequestBody requestBody = null;
            if(StringUtils.isNotBlank(fileUrl)){
                URI uri = new URI(fileUrl);
                String filePath = "ai/digitalPerson/" + DateUtil.formatDateByFormat(new Date(), "yyyy-MM-dd") + "/";
                // 获取文件的原始文件名
                String fileName = uri.getPath().substring(uri.getPath().lastIndexOf("/") + 1);
                // 定义文件保存的路径
                File dest = new File(uploadFilePath + filePath + fileName);
                // 创建上传目录
                if (!dest.getParentFile().exists()) {
                    dest.getParentFile().mkdirs();
                }
                // 将文件保存到指定路径
                FileUtils.copyURLToFile(uri.toURL(), dest);
                tAiShuziren.setTemplateFile(filePath + fileName);

                // 创建文件请求体
                okhttp3.RequestBody fileBody = okhttp3.RequestBody.create(MediaType.parse("application/octet-stream"), dest);
                // 编码文件名以避免乱码
                String encodedFileName =  URLEncoder.encode(dest.getPath(), "UTF-8");
                String type = dest.getName().substring(dest.getName().lastIndexOf(".") + 1);
                requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("videofile", encodedFileName + "." + type, fileBody)
                        .addFormDataPart("content", text)
                        .addFormDataPart("token", token)
                        .build();
            }else if(StringUtils.isNotBlank(xingxiangId)){
                tAiShuziren.setXingxiangId(xingxiangId);
                TAiShuzirenXingxiang xingxiang = tAiShuzirenXingxiangRepository.getOne(xingxiangId);
                //音频文件
                File file1 = new File(uploadFilePath + xingxiang.getReferenceAudio());
                String fileName1 =  URLEncoder.encode(file1.getPath(), "UTF-8");// 编码文件名以避免乱码
                String filetype1 = file1.getName().substring(file1.getName().lastIndexOf(".") + 1);

                //无声视频文件
                File file2 = new File(uploadFilePath + xingxiang.getSilentVideo());
                String fileName2 =  URLEncoder.encode(file2.getPath(), "UTF-8");// 编码文件名以避免乱码
                String filetype2 = file2.getName().substring(file2.getName().lastIndexOf(".") + 1);

                // 创建文件请求体
                okhttp3.RequestBody fileBody1 = okhttp3.RequestBody.create(MediaType.parse("application/octet-stream"), file1);
                okhttp3.RequestBody fileBody2 = okhttp3.RequestBody.create(MediaType.parse("application/octet-stream"), file2);

                // 创建多部分请求体
                requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("reference_audio", fileName1 + "." + filetype1, fileBody1)
                        .addFormDataPart("reference_text", xingxiang.getReferenceText())
                        .addFormDataPart("silent_video", fileName2 + "." + filetype2, fileBody2)
                        .addFormDataPart("content", text)
                        .addFormDataPart("token", token)
                        .build();
            }

            // 创建请求
            Request request = new Request.Builder()
                    .url("http://"+ip+":5000/v1/genvideo")
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
                    tAiShuziren.setCode((String) jsonObject.get("id"));
                    tAiShuziren.setZt(1);
                    tAiShuziren.setProgressDec(resultMsg);
                    operaResult.getData().put("result", jsonObject.toString());
                }else{
                    return OperaResult.putFailResult("error", resultMsg);
                }
            }
        }catch (Exception e){
            log.error("文件上传异常",e);
            return OperaResult.putFailResult("error","文件处理出错: " + e.getMessage());
        }
        //保存数据
        this.save(tAiShuziren);

        operaResult.setResultCode(com.zklcsoftware.common.web.util.ConstantUtil.OPT_RESULT_CODE_SUCCESS);
        operaResult.setResultDesc("上传成功");
        return operaResult;
    }
}
