package com.zklcsoftware.aimodel.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zklcsoftware.aimodel.domain.TAiSysPrompt;
import com.zklcsoftware.aimodel.repository.TAiAttachmentRepository;
import com.zklcsoftware.aimodel.service.TAiSysPromptService;
import com.zklcsoftware.aimodel.util.*;
import com.zklcsoftware.basic.util.DateUtil;
import com.zklcsoftware.common.dto.OperaResult;
import com.zklcsoftware.common.web.util.HttpClients;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.zklcsoftware.basic.service.impl.BaseServiceImpl;
import com.zklcsoftware.aimodel.domain.TAiAttachment;
import com.zklcsoftware.aimodel.service.TAiAttachmentService;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
@Service
@Transactional
public class TAiAttachmentServiceImpl extends BaseServiceImpl<TAiAttachment, String> implements TAiAttachmentService {

    @Autowired
    private TAiAttachmentRepository maiAttachmentRepository;
    @Value("${uploadfiledir.uploadFilePath}")
    private String uploadFilePath;//服务器路径
    @Value("${uploadfiledir.uploadFileUrl}")
    private String uploadFileUrl;//文件封存的http地址
    @Autowired
    private OAuth2ClientContext oauth2Context;//单点登录业务系统里引入这个bean
    @Autowired
    TAiSysPromptService tAiSysPromptService;

    @Override
    public Page<TAiAttachment> queryZskPage(String zntId, Pageable pageable) {
        Map params=new HashMap();
        params.put("zntId", zntId);
        return maiAttachmentRepository.findPage("queryZskList", "queryZskList_count", params, pageable, TAiAttachment.class);
    }

    @Override
    public List<TAiAttachment> queryZskList(String zntId) {
        Map params=new HashMap();
        params.put("zntId", zntId);
        return maiAttachmentRepository.findListObj("queryZskList", params, TAiAttachment.class);
    }

    @Override
    public OperaResult delZsk(String attachmentId) {
        String knowledgeType = ConstantUtil.sysConfig.get("knowledge_type");//使用知识库类型anythingllm、ragflow，默认anythingllm
        TAiAttachment tAiAttachment =this.findById(attachmentId);
        String slug = tAiAttachment.getZskSlug();
        String location = tAiAttachment.getZskLocation();

        if(StringUtils.isNotBlank(slug)){
            if(knowledgeType==null || knowledgeType.equals("anythingllm")){
                //取消检索
                JSONObject updatePin = AnythingLLMUtil.updatePin(slug, location, false);
                if(updatePin !=null){
                    String message = (String) updatePin.get("message");
                    if(StringUtils.isEmpty(message)){
                        return OperaResult.putFailResult("error","取消检索失败");
                    }else{
                        if(message.equals("Pin status updated successfully")){
                            tAiAttachment.setZskZt(0);
                        }
                    }

                }
                //取消向量化
                List<String> deletes = new ArrayList<>();
                deletes.add(location);
                JSONObject updateEmbeddings = AnythingLLMUtil.updateEmbeddings(slug, null, deletes);
                if(updateEmbeddings !=null){
                    JSONObject workspace = (JSONObject) updateEmbeddings.get("workspace");
                    if(workspace.size() > 0){
                        JSONArray docArray = (JSONArray)workspace.get("documents");
                        if(docArray.size() > 0){
                            Boolean flag = false;
                            for (int i = 0; i < docArray.size(); i++) {
                                JSONObject document = (JSONObject)docArray.get(i);
                                String docpath = (String)document.get("docpath");
                                if(docpath.equals(location)){
                                    flag = true;
                                }
                            }
                            if(flag){
                                tAiAttachment.setZskZt(0);
                            }
                        }
                    }
                }
            }else if(knowledgeType.equals("ragflow")){
                JSONObject removeObject = RagFlowUtil.removeDocuments(slug, Arrays.asList(tAiAttachment.getZskLocation(),tAiAttachment.getStandbyZskLocation()));
                if(removeObject !=null){
                    Integer code1 = (Integer) removeObject.get("code");
                    if(code1 == 0){
                        tAiAttachment.setZskZt(0);
                    }
                }
            }
        }

        tAiAttachment.setIsDel(ConstantUtil.IS_DEL_1);//作废
        this.save(tAiAttachment);

        return OperaResult.putSuccessResult("success",true);
    }

    @Override
    public OperaResult zskUpload(MultipartFile multipartFile, String zntId, String userGuid, String uName) {
        String accessToken=oauth2Context.getAccessToken().getValue();//当前登录用户的token
        String netdiskUrl= ConstantUtil.sysConfig.get("netdisk_api");//获取网盘地址
        String netdiskClientId = ConstantUtil.sysConfig.get("netdisk-client-id");//获取网盘clientId
        String knowledgeType = ConstantUtil.sysConfig.get("knowledge_type");//使用知识库类型anythingllm、ragflow，默认anythingllm

        //写入附件信息
        TAiAttachment attachment = new TAiAttachment();
        attachment.setAttachmentType(multipartFile.getOriginalFilename().substring(multipartFile.getOriginalFilename().lastIndexOf(".") + 1));
        attachment.setAttachmentSize((double) multipartFile.getSize());
        attachment.setFileName(multipartFile.getOriginalFilename());
        attachment.setRealName(multipartFile.getOriginalFilename());
        attachment.setContentType(multipartFile.getContentType());
        attachment.setIsDel(0);
        attachment.setCreateTime(new Date());
        attachment.setCreateUser(userGuid);
        try {

            //将上传的文件从临时存储位置转移到目标文件路径
            File file = new File(uploadFilePath + "/ai/context/" + DateUtil.formatDateByFormat(new Date(), "yyyyMMdd") + "/" + multipartFile.getOriginalFilename());
            if(!file.exists()){
                file.getParentFile().mkdirs();
            }
            multipartFile.transferTo(file);

            //获取网盘上传token
            Map dataMap=new HashMap();
            dataMap.put("clientId", netdiskClientId);
            dataMap.put("access_token", accessToken);
            dataMap.put("systemname", URLEncoder.encode("AI文件", "UTF-8"));
            String uploadToken = HttpClients.post(netdiskUrl+"/api/createUploadToken", dataMap, null);
            if(StringUtils.isEmpty(uploadToken)) {
                return OperaResult.putFailResult("error", "获取网盘上传token失败");
            }
            JSONObject tokenJson = JSONObject.parseObject(uploadToken);
            String token = (String) tokenJson.get("uploadToken");
            if(StringUtils.isEmpty(token)) {
                return OperaResult.putFailResult("error", tokenJson.get("message"));
            }

            // 执行网盘上传
            CloseableHttpClient httpClient = org.apache.hc.client5.http.impl.classic.HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(netdiskUrl + "/upload");

            //上传源文件
            MultipartEntityBuilder builder = MultipartEntityBuilder.create()
                    .addBinaryBody("file", file, ContentType.APPLICATION_OCTET_STREAM, URLEncoder.encode(file.getName()));

            httpPost.setEntity(builder.build());
            httpPost.addHeader("uploadtoken", token);
            CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
            String uploadResult = EntityUtils.toString(httpResponse.getEntity(), "utf-8");
            System.out.println("网盘回调：" + uploadResult);
            if(!StringUtils.isEmpty(uploadResult)){
                JSONObject netdiskJson = JSONObject.parseObject(uploadResult);
                String fileId = (String) netdiskJson.get("fileId");
                attachment.setNetdiskId(fileId);//网盘id

                //投喂资料需要用到知识库id
                String slug = "";
                TAiSysPrompt tAiSysPrompt = null;
                if(StringUtils.isNotEmpty(zntId)){
                    attachment.setZntId(zntId);
                    tAiSysPrompt = tAiSysPromptService.findById(zntId);
                    //已有知识库id直接使用，没有去创建并在自定义智能体中存储到该知识库id
                    if(StringUtils.isNotEmpty(tAiSysPrompt.getKnowledgeId())){
                        slug = tAiSysPrompt.getKnowledgeId();
                    }else{
                        //字符串转拼音
                        String pinyin = StringToPinyin.convertToPinyin(tAiSysPrompt.getName());
                        //不同知识库类型使用不同的知识库创建方式
                        if(knowledgeType==null || knowledgeType.equals("anythingllm")){
                            //创建知识库
                            JSONObject cs_znt_zsk = AnythingLLMUtil.createWorkspace(pinyin);
                            if(cs_znt_zsk !=null) {
                                JSONObject workspace = (JSONObject) cs_znt_zsk.get("workspace");
                                slug = (String) workspace.get("slug");//知识库唯一标识
                                //在智能体中写入知识库id
                                tAiSysPrompt.setKnowledgeId(slug);
                                tAiSysPrompt.setUpdateUser(userGuid);
                                tAiSysPrompt.setUpdateTime(new Date());
                                tAiSysPromptService.save(tAiSysPrompt);
                            }else{
                                return OperaResult.putFailResult("error", "创建知识库失败");
                            }
                        }else if(knowledgeType.equals("ragflow")){
                            JSONObject datasets = RagFlowUtil.createDatasets(pinyin);
                            if(datasets !=null){
                                Integer code = (Integer) datasets.get("code");
                                if(code == 0){
                                    JSONObject data = (JSONObject) datasets.get("data");
                                    slug = (String) data.get("id");//知识库唯一标识
                                    //在智能体中写入知识库id
                                    tAiSysPrompt.setKnowledgeId(slug);
                                    tAiSysPrompt.setUpdateUser(userGuid);
                                    tAiSysPrompt.setUpdateTime(new Date());
                                    tAiSysPromptService.save(tAiSysPrompt);
                                }else{
                                    log.info((String) datasets.get("message"));
                                    return OperaResult.putFailResult("error", "创建知识库失败");
                                }
                            }
                        }
                    }
                }
                if(StringUtils.isEmpty(slug)){
                    return OperaResult.putFailResult("error", "创建知识库失败");
                }
                attachment.setZskSlug(slug);//知识库唯一标识

                if(knowledgeType==null || knowledgeType.equals("anythingllm")){
                    //将文件转为文本
                    String text = FileConverter.convertFileToString(file);
                    if(StringUtils.isEmpty(text)){
                        return OperaResult.putFailResult("error", "文件转换失败");
                    }
                    // 创建新的txt文件路径
                    Path txtFilePath = Paths.get(file.getParent(), file.getName().substring(0, file.getName().lastIndexOf(".")) + ".txt");
                    // 写入内容到新的txt文件
                    Files.write(txtFilePath, text.getBytes());
                    log.error("已转换文件: " + file.getAbsolutePath() + " 到 " + txtFilePath);
                    //txt文件
                    File txtFile = new File(txtFilePath.toString());

                    //上传文档
                    JSONObject uploadDocument = AnythingLLMUtil.uploadDocument(txtFile);
                    if(uploadDocument !=null){
                        Boolean success = (Boolean) uploadDocument.get("success");
                        if(!success){
                            return OperaResult.putFailResult("error", "上传知识库失败");
                        }

                        List<String> adds = new ArrayList<>();
                        List<String> deletes = new ArrayList<>();
                        JSONArray documents = (JSONArray)uploadDocument.get("documents");
                        if(documents.size() > 0){
                            JSONObject document = (JSONObject)documents.get(0);
                            String location = (String)document.get("location");
                            String url = (String)document.get("url");
                            attachment.setZskLocation(location);
                            attachment.setZskUrl(url);
                            adds.add(location);
                        }

                        if(StringUtils.isNotEmpty(slug)){
                            //向量化
                            JSONObject updateEmbeddings = AnythingLLMUtil.updateEmbeddings(slug, adds, deletes);
                            if(updateEmbeddings !=null){
                                JSONObject workspace = (JSONObject) updateEmbeddings.get("workspace");
                                JSONArray docArray = (JSONArray)workspace.get("documents");
                                if(docArray.size() > 0){
                                    Boolean flag = false;
                                    for (int i = 0; i < docArray.size(); i++) {
                                        JSONObject document = (JSONObject)docArray.get(i);
                                        String docpath = (String)document.get("docpath");
                                        if(docpath.equals(attachment.getZskLocation())){
                                            flag = true;
                                        }
                                    }
                                    if(flag){
                                        attachment.setZskZt(0);
                                    }
                                }
                            }

                            //检索
                            JSONObject updatePin = AnythingLLMUtil.updatePin(slug, attachment.getZskLocation(), true);
                            if(updatePin !=null && updatePin.size() > 0){
                                String message = (String) updatePin.get("message");
                                if(!message.equals("Pin status updated successfully")){
                                    attachment.setZskZt(1);
                                }else{
                                    attachment.setZskZt(2);
                                }
                            }

                        }
                    }
                }else if(knowledgeType.equals("ragflow")){

                    Map hanleFileConfigMap=null;
                    //知识库检索 是否存在个性化配置 针对ragflow
                    if(StringUtils.isNotBlank(tAiSysPrompt.getExtJson())){
                        JSONObject configJSONObject= JSON.parseObject(tAiSysPrompt.getExtJson());
                        if(configJSONObject.getJSONObject("file_handle_config")!=null){
                             hanleFileConfigMap=configJSONObject.getJSONObject("file_handle_config").getInnerMap();
                        }
                    }

                    //将文件转为md文本
                    String text = FileConverter.convertFileToString(file,hanleFileConfigMap);
                    if(StringUtils.isEmpty(text)){
                        return OperaResult.putFailResult("error", "文件转换失败");
                    }
                    // 创建新的md文件路径
                    Path mdFilePath = Paths.get(file.getParent(), file.getName().substring(0, file.getName().lastIndexOf(".")) + ".md");
                    // 写入内容到新的md文件
                    Files.write(mdFilePath, text.getBytes());
                    log.error("已转换文件: " + file.getAbsolutePath() + " 到 " + mdFilePath);
                    //md文件
                    File mdFile = new File(mdFilePath.toString());

                    //上传文档
                    String uploadSourceFileFlag=ConstantUtil.sysConfig.get("uploadsourcefile_flag");//是否给知识库上传原始文件   1是  0或null为空
                    JSONObject jsonObject =null;
                    if("1".equals(uploadSourceFileFlag)){
                        jsonObject = RagFlowUtil.uploadDocument(file, slug);
                    }else{
                        jsonObject = RagFlowUtil.uploadDocument(mdFile, slug);
                    }
                    if(jsonObject !=null) {
                        Integer code = (Integer) jsonObject.get("code");
                        if (code == 0) {
                            JSONArray dataArray = (JSONArray) jsonObject.get("data");
                            JSONObject dataObject = (JSONObject) dataArray.get(0);
                            String id = (String) dataObject.get("id");
                            attachment.setZskLocation(id);
                            attachment.setZskUrl(id);

                            if(StringUtils.isNotEmpty(tAiSysPrompt.getExtJson())){
                                JSONObject extJson = JSON.parseObject(tAiSysPrompt.getExtJson());
                                JSONObject fileStudyConfig = (JSONObject) extJson.get("file_study_config");
                                if(fileStudyConfig != null){
                                    if(text.length()>15000 && "one".equals(fileStudyConfig.getString("chunk_method"))){//内容小于15000字
                                        fileStudyConfig.remove("chunk_method");//大于15000字的  不能按one方式处理
                                    }
                                    //更新文档配置
                                    RagFlowUtil.updateDocument(slug, id, fileStudyConfig.toJSONString());
                                }
                            }

                            //解析文档
                            JSONObject analyzeObject = RagFlowUtil.analyzeDocument(slug, Arrays.asList(id));
                            if(analyzeObject !=null){
                                Integer code1 = (Integer) analyzeObject.get("code");
                                if(code1 == 0){
                                    attachment.setZskZt(2);
                                }else{
                                    attachment.setZskZt(3);
                                }
                            }
                        }else{
                            log.error((String) jsonObject.get("message"));
                            return OperaResult.putFailResult("error", "上传知识库失败");
                        }

                        //判断备用知识文档细化分块配置是否启用
                        try {
                            if(StringUtils.isNotEmpty(tAiSysPrompt.getExtJson())){
                                JSONObject extJson = JSON.parseObject(tAiSysPrompt.getExtJson());
                                JSONObject fileStudyConfig = (JSONObject) extJson.get("standby_file_study_config");
                                if(fileStudyConfig != null){
                                    //上传文档
                                    File xhMdFile = new File(jsonObject.getString("filePath"));//需要上传的知识库文件(首次upload时 可能会因为名称过长截取重命名，再次上传就使用截取处理后的文件)
                                    //上传文档
                                    JSONObject jsonObjectS =null;//上传备用文件用于细化分块
                                    if("1".equals(uploadSourceFileFlag)){
                                        jsonObjectS = RagFlowUtil.uploadDocument(file, slug);
                                    }else{
                                        jsonObjectS = RagFlowUtil.uploadDocument(xhMdFile, slug);
                                    }

                                    JSONArray dataArray = (JSONArray) jsonObjectS.get("data");
                                    JSONObject dataObject = (JSONObject) dataArray.get(0);
                                    String id = (String) dataObject.get("id");//备用知识文件上传ID
                                    attachment.setStandbyZskLocation(id);//备用知识文件分块ID
                                    attachment.setStandbyZskUrl(id);//备用知识文件分块ID
                                    //更新文档配置
                                    RagFlowUtil.updateDocument(slug, id, fileStudyConfig.toJSONString());
                                    //解析文档
                                    RagFlowUtil.analyzeDocument(slug, Arrays.asList(id));
                                }
                            }
                        }catch (Exception e){
                            log.error("备用知识附件分块异常--程序继续执行不影响主分块",e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return OperaResult.putFailResult("error", e.getMessage());
        }
        this.save(attachment);
        return OperaResult.putSuccessResult("success",true);
    }

    @Override
    public OperaResult zskZipUpload(MultipartFile multipartFile, String zntId, String userGuid, String uName) {
        String accessToken=oauth2Context.getAccessToken().getValue();//当前登录用户的token
        String netdiskUrl=ConstantUtil.sysConfig.get("netdisk_api");//获取网盘地址
        String netdiskClientId = ConstantUtil.sysConfig.get("netdisk-client-id");//获取网盘clientId
        String knowledgeType = ConstantUtil.sysConfig.get("knowledge_type");//使用知识库类型anythingllm、ragflow，默认anythingllm

        List<File> fileList = new ArrayList<>();
        try {//解析zip文件
            File destDir = new File(uploadFilePath + "ai/context/");
            if (!destDir.exists()) {
                destDir.mkdir();
            }
            InputStream inputStream = multipartFile.getInputStream();
            ZipInputStream zipInputStream = new ZipInputStream(inputStream, Charset.forName("GBK"));
            ZipEntry entry = zipInputStream.getNextEntry();
            while (entry != null) {
                String filePath = uploadFilePath + "ai/context/" + File.separator + entry.getName();
                if (!entry.isDirectory()) {
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
                    byte[] bytesIn = new byte[4096];
                    int read = 0;
                    while ((read = zipInputStream.read(bytesIn)) != -1) {
                        bos.write(bytesIn, 0, read);
                    }
                    bos.close();
                    File dir = new File(filePath);
                    fileList.add(dir);
                }else{
                    File dir = new File(filePath);
                    dir.mkdirs();
                }
                zipInputStream.closeEntry();
                entry = zipInputStream.getNextEntry();
            }
        }catch (Exception ex){
            return OperaResult.putFailResult("error", "解析zip失败");
        }

        if(fileList.size() == 0){
            return OperaResult.putFailResult("error", "zip中文件不存在");
        }

        List<TAiAttachment> attachments = new ArrayList<>();
        List<String> sbErr = new ArrayList<>();
        for(File file : fileList){
            if(file.getName().endsWith(".png") || file.getName().endsWith(".ofd") || file.getName().endsWith(".db") || file.getName().endsWith(".dot")
                    || file.getName().endsWith(".rar")){
                continue;
            }
            StringBuilder sb = new StringBuilder();

            //写入附件信息
            TAiAttachment attachment = new TAiAttachment();
            attachment.setAttachmentType(file.getName().substring(file.getName().lastIndexOf(".") + 1));
            attachment.setAttachmentSize((double) file.length());
            attachment.setFileName(file.getName());
            attachment.setRealName(file.getName());
            // 获取文件类型
            String fileName = file.getName();
            int lastDotIndex = fileName.lastIndexOf('.');
            if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
                String fileExtension = fileName.substring(lastDotIndex + 1).toLowerCase();
                attachment.setContentType(fileExtension);
            }
            attachment.setIsDel(0);
            attachment.setCreateTime(new Date());
            attachment.setCreateUser(userGuid);
            try {
                //获取网盘上传token
                Map dataMap=new HashMap();
                dataMap.put("clientId", netdiskClientId);
                dataMap.put("access_token", accessToken);
                dataMap.put("systemname", URLEncoder.encode("AI文件", "UTF-8"));
                String uploadToken = HttpClients.post(netdiskUrl+"/api/createUploadToken", dataMap, null);
                if(StringUtils.isNotEmpty(uploadToken)) {
                    JSONObject tokenJson = JSONObject.parseObject(uploadToken);
                    String token = (String) tokenJson.get("uploadToken");

                    if(StringUtils.isNotEmpty(token)) {
                        // 执行网盘上传
                        CloseableHttpClient httpClient = org.apache.hc.client5.http.impl.classic.HttpClients.createDefault();
                        HttpPost httpPost = new HttpPost(netdiskUrl + "/upload");

                        //上传源文件
                        MultipartEntityBuilder builder = MultipartEntityBuilder.create()
                                .addBinaryBody("file", file, ContentType.APPLICATION_OCTET_STREAM, URLEncoder.encode(file.getName()));

                        httpPost.setEntity(builder.build());
                        httpPost.addHeader("uploadtoken", token);
                        CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
                        String uploadResult = EntityUtils.toString(httpResponse.getEntity(), "utf-8");
                        System.out.println("网盘回调：" + uploadResult);

                        if(StringUtils.isNotEmpty(uploadResult)){
                            JSONObject netdiskJson = JSONObject.parseObject(uploadResult);
                            String fileId = (String) netdiskJson.get("fileId");
                            attachment.setNetdiskId(fileId);//网盘id

                            //投喂资料需要用到知识库id
                            String slug = "";
                            TAiSysPrompt tAiSysPrompt = null;
                            if(StringUtils.isNotEmpty(zntId)){
                                attachment.setZntId(zntId);
                                tAiSysPrompt = tAiSysPromptService.findById(zntId);
                                //已有知识库id直接使用，没有去创建并在自定义智能体中存储到该知识库id
                                if(StringUtils.isNotEmpty(tAiSysPrompt.getKnowledgeId())){
                                    slug = tAiSysPrompt.getKnowledgeId();
                                }else{
                                    //字符串转拼音
                                    String pinyin = StringToPinyin.convertToPinyin(tAiSysPrompt.getName());
                                    if(knowledgeType==null || knowledgeType.equals("anythingllm")){
                                        //创建知识库
                                        JSONObject cs_znt_zsk = AnythingLLMUtil.createWorkspace(pinyin);
                                        if(cs_znt_zsk !=null) {
                                            JSONObject workspace = (JSONObject) cs_znt_zsk.get("workspace");
                                            slug = (String) workspace.get("slug");//知识库唯一标识
                                            //在智能体中写入知识库id
                                            tAiSysPrompt.setKnowledgeId(slug);
                                            tAiSysPrompt.setUpdateUser(userGuid);
                                            tAiSysPrompt.setUpdateTime(new Date());
                                            tAiSysPromptService.save(tAiSysPrompt);
                                        }else{
                                            sb.append(sb.length() > 0 ? fileName + ",创建知识库失败" : fileName + "创建知识库失败");
                                        }
                                    }else if(knowledgeType.equals("ragflow")){
                                        //创建知识库
                                        JSONObject datasets = RagFlowUtil.createDatasets(pinyin);
                                        if(datasets !=null){
                                            Integer code = (Integer) datasets.get("code");
                                            if(code == 0){
                                                JSONObject data = (JSONObject) datasets.get("data");
                                                slug = (String) data.get("id");//知识库唯一标识
                                                //在智能体中写入知识库id
                                                tAiSysPrompt.setKnowledgeId(slug);
                                                tAiSysPrompt.setUpdateUser(userGuid);
                                                tAiSysPrompt.setUpdateTime(new Date());
                                                tAiSysPromptService.save(tAiSysPrompt);
                                            }else{
                                                log.info((String) datasets.get("message"));
                                                return OperaResult.putFailResult("error", "创建知识库失败");
                                            }
                                        }else{
                                            sb.append(sb.length() > 0 ? fileName + ",创建知识库失败" : fileName + "创建知识库失败");
                                        }
                                    }
                                }
                            }

                            if(StringUtils.isNotEmpty(slug)){
                                attachment.setZskSlug(slug);

                                if(knowledgeType==null || knowledgeType.equals("anythingllm")){
                                    //将文件转为文本
                                    String text = FileConverter.convertFileToString(file);
                                    if(StringUtils.isNotEmpty(text)){
                                        // 创建新的txt文件路径
                                        Path txtFilePath = Paths.get(file.getParent(), file.getName().substring(0, file.getName().lastIndexOf(".")) + ".txt");
                                        Files.write(txtFilePath, text.getBytes());// 写入内容到新的txt文件
                                        File txtFile = new File(txtFilePath.toString());
                                        //上传文档
                                        JSONObject uploadDocument = AnythingLLMUtil.uploadDocument(txtFile);
                                        if(uploadDocument !=null){
                                            Boolean success = (Boolean) uploadDocument.get("success");
                                            if(!success){
                                                sb.append(sb.length() > 0 ? fileName + ",上传知识库失败" : fileName + "上传知识库失败");
                                            }else{
                                                List<String> adds = new ArrayList<>();
                                                List<String> deletes = new ArrayList<>();
                                                JSONArray documents = (JSONArray)uploadDocument.get("documents");
                                                if(documents.size() > 0){
                                                    JSONObject document = (JSONObject)documents.get(0);
                                                    String location = (String)document.get("location");
                                                    String url = (String)document.get("url");
                                                    attachment.setZskLocation(location);
                                                    attachment.setZskUrl(url);
                                                    adds.add(location);
                                                }

                                                //向量化
                                                JSONObject updateEmbeddings = AnythingLLMUtil.updateEmbeddings(slug, adds, deletes);
                                                if(updateEmbeddings !=null){
                                                    JSONObject workspace = (JSONObject) updateEmbeddings.get("workspace");
                                                    JSONArray docArray = (JSONArray)workspace.get("documents");
                                                    if(docArray.size() > 0){
                                                        Boolean flag = false;
                                                        for (int i = 0; i < docArray.size(); i++) {
                                                            JSONObject document = (JSONObject)docArray.get(i);
                                                            String docpath = (String)document.get("docpath");
                                                            if(docpath.equals(attachment.getZskLocation())){
                                                                flag = true;
                                                            }
                                                        }
                                                        if(flag){
                                                            attachment.setZskZt(0);
                                                        }
                                                    }
                                                }

                                                //检索
                                                JSONObject updatePin = AnythingLLMUtil.updatePin(slug, attachment.getZskLocation(), true);
                                                if(updatePin !=null && updatePin.size() > 0){
                                                    String message = (String) updatePin.get("message");
                                                    if(!message.equals("Pin status updated successfully")){
                                                        attachment.setZskZt(1);
                                                    }else{
                                                        attachment.setZskZt(2);
                                                    }
                                                }
                                            }
                                        }
                                    }else{
                                        sb.append(sb.length() > 0 ? fileName + ",文件转换失败" : fileName + "文件转换失败");
                                    }

                                }else if(knowledgeType.equals("ragflow")){

                                    Map hanleFileConfigMap=null;
                                    //知识库检索 是否存在个性化配置 针对ragflow
                                    if(StringUtils.isNotBlank(tAiSysPrompt.getExtJson())){
                                        JSONObject configJSONObject= JSON.parseObject(tAiSysPrompt.getExtJson());
                                        if(configJSONObject.getJSONObject("file_handle_config")!=null){
                                            hanleFileConfigMap=configJSONObject.getJSONObject("file_handle_config").getInnerMap();
                                        }
                                    }

                                    //将文件转为md文本
                                    String text = FileConverter.convertFileToString(file,hanleFileConfigMap);
                                    if(!StringUtils.isEmpty(text)){
//                                        return OperaResult.putFailResult("error", "文件转换失败");
                                        // 创建新的md文件路径
                                        Path mdFilePath = Paths.get(file.getParent(), file.getName().substring(0, file.getName().lastIndexOf(".")) + ".md");
                                        // 写入内容到新的md文件
                                        Files.write(mdFilePath, text.getBytes());
                                        log.error("已转换文件: " + file.getAbsolutePath() + " 到 " + mdFilePath);
                                        //md文件
                                        File mdFile = new File(mdFilePath.toString());

                                        //上传文档
                                        String uploadSourceFileFlag=ConstantUtil.sysConfig.get("uploadsourcefile_flag");//是否给知识库上传原始文件   1是  0或null为空
                                        //上传文档
                                        JSONObject jsonObject =null;
                                        if("1".equals(uploadSourceFileFlag)){
                                            jsonObject = RagFlowUtil.uploadDocument(file, slug);
                                        }else{
                                            jsonObject = RagFlowUtil.uploadDocument(mdFile, slug);
                                        }
                                        if(jsonObject !=null) {
                                            Integer code = (Integer) jsonObject.get("code");
                                            if (code == 0) {
                                                JSONArray dataArray = (JSONArray) jsonObject.get("data");
                                                JSONObject dataObject = (JSONObject) dataArray.get(0);
                                                String id = (String) dataObject.get("id");
                                                attachment.setZskLocation(id);
                                                attachment.setZskUrl(id);

                                                //更新文档配置
                                                if(StringUtils.isNotEmpty(tAiSysPrompt.getExtJson())){
                                                    JSONObject extJson = JSON.parseObject(tAiSysPrompt.getExtJson());
                                                    JSONObject fileStudyConfig = (JSONObject) extJson.get("file_study_config");
                                                    if(fileStudyConfig != null){
                                                        if(text.length()>15000 && "one".equals(fileStudyConfig.getString("chunk_method"))){//内容小于15000字
                                                            fileStudyConfig.remove("chunk_method");//大于15000字的  不能按one方式处理
                                                        }
                                                        //更新文档配置
                                                        RagFlowUtil.updateDocument(slug, id, fileStudyConfig.toJSONString());
                                                    }
                                                }

                                                //解析文档
                                                JSONObject analyzeObject = RagFlowUtil.analyzeDocument(slug, Arrays.asList(id));
                                                if(analyzeObject !=null){
                                                    Integer code1 = (Integer) analyzeObject.get("code");
                                                    if(code1 == 0){
                                                        attachment.setZskZt(2);
                                                    }else{
                                                        attachment.setZskZt(3);
                                                    }
                                                }
                                            }else{
                                                log.error((String) jsonObject.get("message"));
                                                sb.append(sb.length() > 0 ? fileName + ",上传知识库失败" : fileName + "上传知识库失败");
                                            }

                                            //判断备用知识文档细化分块配置是否启用
                                            try {
                                                if(StringUtils.isNotEmpty(tAiSysPrompt.getExtJson())){
                                                    JSONObject extJson = JSON.parseObject(tAiSysPrompt.getExtJson());
                                                    JSONObject fileStudyConfig = (JSONObject) extJson.get("standby_file_study_config");//细化分块配置
                                                    if(fileStudyConfig != null){
                                                        File xhMdFile = new File(jsonObject.getString("filePath"));//需要上传的知识库文件(首次upload时 可能会因为名称过长截取重命名，再次上传就使用截取处理后的文件)
                                                        JSONObject jsonObjectS = null;//上传备用文件用于细化分块
                                                        if("1".equals(uploadSourceFileFlag)){
                                                            jsonObjectS = RagFlowUtil.uploadDocument(file, slug);
                                                        }else{
                                                            jsonObjectS = RagFlowUtil.uploadDocument(xhMdFile, slug);
                                                        }
                                                        JSONArray dataArray = (JSONArray) jsonObjectS.get("data");
                                                        JSONObject dataObject = (JSONObject) dataArray.get(0);
                                                        String id = (String) dataObject.get("id");//备用知识文件上传ID
                                                        attachment.setStandbyZskLocation(id);//备用知识文件分块ID
                                                        attachment.setStandbyZskUrl(id);//备用知识文件分块ID
                                                        //更新文档配置
                                                        RagFlowUtil.updateDocument(slug, id, fileStudyConfig.toJSONString());
                                                        //解析文档
                                                        RagFlowUtil.analyzeDocument(slug, Arrays.asList(id));
                                                    }
                                                }
                                            }catch (Exception e){
                                                log.error("备用知识附件分块异常--程序继续执行不影响主分块",e);
                                            }
                                        }
                                    }else{
                                        sb.append(sb.length() > 0 ? fileName + ",文件转换失败" : fileName + "文件转换失败");
                                    }
                                }
                            }else{
                                sb.append(sb.length() > 0 ? fileName + ",创建知识库失败" : fileName + "获取网盘上传token失败");
                            }
                        }else{
                            sb.append(sb.length() > 0 ? fileName + ",网盘上传失败" : fileName + "网盘上传失败");
                        }
                    }else{
                        sb.append(sb.length() > 0 ? fileName + "," + tokenJson.get("message") : fileName + tokenJson.get("message"));
                    }
                }else{
                    sb.append(sb.length() > 0 ? fileName + ",获取网盘上传token失败" : fileName + "获取网盘上传token失败");
                }
            } catch (Exception e) {
                e.printStackTrace();
                sb.append(sb.length() > 0 ? fileName + "," + e.getMessage() : fileName + e.getMessage());
            }
            sbErr.add(sb.toString());
            attachments.add(attachment);
        }
        log.info("fileErr:"+sbErr);
        this.save(attachments);
        return OperaResult.putSuccessResult("success",true);
    }
}
