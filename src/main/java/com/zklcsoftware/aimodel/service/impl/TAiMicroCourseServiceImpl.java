package com.zklcsoftware.aimodel.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.zklcsoftware.aimodel.domain.TAiShuzirenPpt;
import com.zklcsoftware.aimodel.domain.TAiShuzirenXingxiang;
import com.zklcsoftware.aimodel.domain.TShuzirenService;
import com.zklcsoftware.aimodel.repository.TAiMicroCourseRepository;
import com.zklcsoftware.aimodel.repository.TAiShuzirenPptRepository;
import com.zklcsoftware.aimodel.repository.TAiShuzirenXingxiangRepository;
import com.zklcsoftware.aimodel.service.TAiShuzirenXingxiangService;
import com.zklcsoftware.aimodel.service.TShuzirenServiceService;
import com.zklcsoftware.aimodel.util.ConstantUtil;
import com.zklcsoftware.basic.util.DateUtil;
import com.zklcsoftware.common.dto.OperaResult;
import okhttp3.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.zklcsoftware.basic.service.impl.BaseServiceImpl;
import com.zklcsoftware.aimodel.domain.TAiMicroCourse;
import com.zklcsoftware.aimodel.service.TAiMicroCourseService;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TAiMicroCourseServiceImpl extends BaseServiceImpl<TAiMicroCourse, String> implements TAiMicroCourseService {

    @Autowired
    private TAiMicroCourseRepository tAiMicroCourseRepository;
    @Autowired
    private TAiShuzirenPptRepository tAiShuzirenPptRepository;
    @Autowired
    private TAiShuzirenXingxiangRepository tAiShuzirenXingxiangRepository;
    @Autowired
    private TShuzirenServiceService tShuzirenServiceService;
    @Autowired
    TAiShuzirenXingxiangService tAiShuzirenXingxiangService;
    @Value("${uploadfiledir.uploadFilePath}")
    private String uploadFilePath;//服务器路径
    @Value("${uploadfiledir.uploadFileUrl}")
    private String uploadFileUrl;//文件封存的http地址

    @Override
    public List<TAiMicroCourse> findByCreateUserIdAndIsDelOrderByCreateDateDesc(String userGuid, Integer isDel) {
        return tAiMicroCourseRepository.findByCreateUserIdAndIsDelOrderByCreateDateDesc(userGuid, isDel);
    }

    @Override
    public OperaResult microCourseSave(TAiMicroCourse course, String userGuid, String uName) {

        //校验ppt
        List<TAiShuzirenPpt> pptList = course.getPptList();
        if(pptList.size() == 0){
            return OperaResult.putFailResult("error", "请上传ppt");
        }else{
            for(TAiShuzirenPpt ppt: pptList){
                if(StringUtils.isBlank(ppt.getXingxiangId())){
                    return OperaResult.putFailResult("error", "请选择数字人形象");
                }
            }
        }

        TAiMicroCourse aiMicroCourse = null;
        //更新时删除视频文件
        if(course!=null && StringUtils.isNotBlank(course.getCourseId())){
            aiMicroCourse = this.findById(course.getCourseId());
            aiMicroCourse.setCourseName(course.getCourseName());
            aiMicroCourse.setUpdateUserId(userGuid);
            aiMicroCourse.setUpdateUserName(uName);
            aiMicroCourse.setUpdatedate(new Date());

            //删除视频文件
            if(aiMicroCourse.getVideoZt() == 2){
                new File(aiMicroCourse.getVideoUrl()).delete();
            }
            //删除已存在的ppt
            List<TAiShuzirenPpt> tAiShuzirenPptList = tAiShuzirenPptRepository.findByCourseIdAndIsDelOrderByPptXh(aiMicroCourse.getCourseId(), ConstantUtil.IS_DEL_0);
            if(tAiShuzirenPptList.size() > 0){
                tAiShuzirenPptList.forEach(ppt->{
                    ppt.setIsDel(ConstantUtil.IS_DEL_1);
                    ppt.setUpdateUserId(userGuid);
                    ppt.setUpdateUserName(uName);
                    ppt.setUpdatedate(new Date());

                    //删除视频文件
                    if(ppt.getVideoZt() == 2){
                        new File(ppt.getPptUrl()).delete();
                        new File(ppt.getPptImg()).delete();
                        if(ppt.getVideoId() != null){
                            new File(ppt.getVideoUrl()).delete();
                        }
                        new File(ppt.getVideoProceUrl()).delete();
                        new File(ppt.getCover()).delete();
                    }
                });
                tAiShuzirenPptRepository.saveAll(tAiShuzirenPptList);
            }
        }else{//新增
            aiMicroCourse = new TAiMicroCourse();
            BeanUtils.copyProperties(course,aiMicroCourse);
            aiMicroCourse.setIsDel(ConstantUtil.IS_DEL_0);
            aiMicroCourse.setCreateUserId(userGuid);
            aiMicroCourse.setCreateUserName(uName);
            aiMicroCourse.setCreateDate(new Date());
        }
        aiMicroCourse.setVideoZt(course.getVideoZt());//视频状态，0:未提交，1:生成中，2:已完成，3:生成失败
        //保存微课信息
        TAiMicroCourse course1 = this.save(aiMicroCourse);

        //获取数字人token
        String token =ConstantUtil.sysConfig.get("digital_person_token");

        int num = 0;//添加数字人的文本内容
        List<TAiShuzirenPpt> ppts = new ArrayList<>();
        for(TAiShuzirenPpt ppt: pptList){
            num += ppt.getContent().length();

            TAiShuzirenPpt tAiShuzirenPpt = new TAiShuzirenPpt();
            tAiShuzirenPpt.setCourseId(course1.getCourseId());
            tAiShuzirenPpt.setXingxiangId(ppt.getXingxiangId());
            tAiShuzirenPpt.setPptXh(ppt.getPptXh());
            tAiShuzirenPpt.setPptImg(ppt.getPptImg());
            tAiShuzirenPpt.setContent(ppt.getContent());
            tAiShuzirenPpt.setSzrWz(ppt.getSzrWz());
            tAiShuzirenPpt.setSzrDx(ppt.getSzrDx());
            tAiShuzirenPpt.setVideoZt(course1.getVideoZt());
            tAiShuzirenPpt.setIsDel(ConstantUtil.IS_DEL_0);
            tAiShuzirenPpt.setCreateUserId(userGuid);
            tAiShuzirenPpt.setCreateUserName(uName);
            tAiShuzirenPpt.setCreateDate(new Date());
            try {
                if(StringUtils.isNotBlank(ppt.getPptUrl())){
                    URI uri = new URI(ppt.getPptUrl());
                    String filePath = "ai/microCourse/" + DateUtil.formatDateByFormat(new Date(), "yyyy-MM-dd") + "/";
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
                    tAiShuzirenPpt.setPptUrl(filePath + fileName);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            TAiShuzirenXingxiang xingxiang = tAiShuzirenXingxiangService.findById(ppt.getXingxiangId());
            tAiShuzirenPpt.setCover(xingxiang.getCover());
            ppts.add(tAiShuzirenPpt);
        }

       /* //计算一个字符的时长
        BigDecimal scale = new BigDecimal("60").divide(new BigDecimal("30"), 2, BigDecimal.ROUND_HALF_UP);        //计算时长
        BigDecimal shichang = scale.multiply(new BigDecimal(num));
        //分钟
        BigDecimal min = shichang.divide(new BigDecimal("60"), 0, BigDecimal.ROUND_HALF_UP);
        // 剩余秒数
        BigDecimal sec = shichang.remainder(new BigDecimal("60")).setScale(0, BigDecimal.ROUND_HALF_UP);
        StringBuilder description = new StringBuilder();
        if (min.compareTo(BigDecimal.ZERO) > 0) {
            description.append(min).append("分");
        }
        description.append(sec).append("秒");

        course1.setDes("预计还需要" + description);
        this.save(course1);*/
        //保存微课图片信息
        tAiShuzirenPptRepository.saveAll(ppts);
        return OperaResult.putSuccessResult("courseId",course1.getCourseId());
    }

    @Override
    public List<TAiMicroCourse> findByVideoZtAndIsDel(Integer videoZt, Integer isDel) {
        return tAiMicroCourseRepository.findByVideoZtAndIsDel(videoZt,isDel);
    }

    @Override
    public OperaResult microCourseVideoConcatenation(String courseId, String userGuid, String uName) {
        String token =ConstantUtil.sysConfig.get("digital_person_token");//获取数字人token

        //查询微课图片信息
        List<TAiShuzirenPpt> pptList = tAiShuzirenPptRepository.findByCourseIdAndIsDelOrderByPptXh(courseId, ConstantUtil.IS_DEL_0);
        if(pptList.size() == 0){
            return OperaResult.putFailResult("errSb", "请上传ppt课件");
        }

        //查询微课信息
        TAiMicroCourse course = this.findById(courseId);
        course.setVideoZt(1);//设置微课状态为生成中，定时生成
        course.setUpdateUserId(userGuid);
        course.setUpdateUserName(uName);
        course.setUpdatedate(new Date());
        this.save(course);

        //循环生成每页ppt的数字人视频id,更新数字人视频状态为生成中，定时生成
        for(TAiShuzirenPpt ppt: pptList){
            if(StringUtils.isNotBlank(ppt.getXingxiangId()) && StringUtils.isNotBlank(ppt.getContent())){

                //获取数字人服务ip
                TShuzirenService serviceIp = tShuzirenServiceService.getMinServiceIp();
                if(serviceIp ==null){
                    continue;
                }
                ppt.setServerIp(serviceIp.getServiceAddress());
                TAiShuzirenXingxiang xingxiang = tAiShuzirenXingxiangRepository.getOne(ppt.getXingxiangId());
                try {
                    //音频文件
                    File file1 = new File(uploadFilePath + xingxiang.getReferenceAudio());
                    String fileName1 =  URLEncoder.encode(file1.getPath(), "UTF-8");// 编码文件名以避免乱码
                    String filetype1 = file1.getName().substring(file1.getName().lastIndexOf(".") + 1);

                    //无声视频文件
                    File file2 = new File(uploadFilePath + xingxiang.getSilentVideo());
                    String fileName2 =  URLEncoder.encode(file2.getPath(), "UTF-8");// 编码文件名以避免乱码
                    String filetype2 = file2.getName().substring(file2.getName().lastIndexOf(".") + 1);

                    OkHttpClient client = new OkHttpClient();
                    // 创建文件请求体
                    okhttp3.RequestBody fileBody1 = okhttp3.RequestBody.create(MediaType.parse("application/octet-stream"), file1);
                    okhttp3.RequestBody fileBody2 = okhttp3.RequestBody.create(MediaType.parse("application/octet-stream"), file2);

                    // 创建多部分请求体
                    okhttp3.RequestBody requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("reference_audio", fileName1 + "." + filetype1, fileBody1)
                            .addFormDataPart("reference_text", xingxiang.getReferenceText())
                            .addFormDataPart("silent_video", fileName2 + "." + filetype2, fileBody2)
                            .addFormDataPart("content", ppt.getContent())
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
                            ppt.setVideoId((String) jsonObject.get("id"));
                            ppt.setVideoZt(1);
                            ppt.setProgressDec(resultMsg);
                        }else{
                            ppt.setVideoZt(3);
                            ppt.setProgressDec(resultMsg);
                        }
                    }
                    ppt.setUpdateUserId(userGuid);
                    ppt.setUpdateUserName(uName);
                    ppt.setUpdatedate(new Date());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        tAiShuzirenPptRepository.saveAll(pptList);
        return OperaResult.putSuccessResult("success",true);
    }
}
