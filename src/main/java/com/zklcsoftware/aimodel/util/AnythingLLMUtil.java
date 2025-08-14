package com.zklcsoftware.aimodel.util;

import com.alibaba.fastjson.JSONObject;
import com.zklcsoftware.basic.util.UuidUtil;
import com.zklcsoftware.common.web.util.HttpClients;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 杨洁
 * @ClassName AnythingLLMUtil.java
 * @Description
 * @createTime 2025-03-17 13:22
 **/
public class AnythingLLMUtil {

    private static final String zskKey = ConstantUtil.sysConfig.get("knowledge_apikey");//知识库API key
    private static final String zskUrl = ConstantUtil.sysConfig.get("knowledge_apiurl");//知识库API地址
    private static final String zskThreshold = ConstantUtil.sysConfig.get("knowledge_score_threshold");//知识库检索相似度阈值
    private static final String zsktopN = ConstantUtil.sysConfig.get("knowledge_topN");//知识库检索topN条数据

    /**
     * 创建工作空间
     * @param name
     * @return
     */
    public static JSONObject createWorkspace(String name){
        JSONObject jsonObject = new JSONObject();

        try {
            Map headerMap=new HashMap();
            headerMap.put("Authorization","Bearer " + zskKey);

            Map dataMap=new HashMap();
            dataMap.put("name",name);
            dataMap.put("similarityThreshold",zskThreshold);
            dataMap.put("openAiTemp",0.7);
            dataMap.put("openAiHistory",20);
            dataMap.put("openAiPrompt","Custom prompt for responses");
            dataMap.put("queryRefusalResponse","Custom refusal message");
            dataMap.put("chatMode","chat");
            dataMap.put("topN",zsktopN);
            String post = HttpClients.post(zskUrl + "/api/v1/workspace/new", dataMap, headerMap);
            if(StringUtils.isNotEmpty(post)){
                jsonObject = JSONObject.parseObject(post);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    /**
     * 上传文档
     * @param file
     * @return
     */
    public static JSONObject uploadDocument(File file) throws IOException {

        JSONObject jsonObject = new JSONObject();
        /*try {
            // 执行网盘上传
            CloseableHttpClient httpClient = org.apache.hc.client5.http.impl.classic.HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(zskUrl + "/api/v1/document/upload");

            MultipartEntityBuilder builder = MultipartEntityBuilder.create()
                    .addBinaryBody("file", file, ContentType.APPLICATION_JSON, file.getName());

            httpPost.setEntity(builder.build());
            httpPost.addHeader("Authorization", "Bearer " + zskKey);
            CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
            String uploadResult = EntityUtils.toString(httpResponse.getEntity(), "utf-8");
            System.out.println("网盘回调：" + uploadResult);
            if(!StringUtils.isEmpty(uploadResult)){
                jsonObject = JSONObject.parseObject(uploadResult);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }*/

        OkHttpClient client = new OkHttpClient();

        // 创建文件请求体
        RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), file);
        // 编码文件名以避免乱码
        String encodedFileName =  URLEncoder.encode(file.getPath(), "UTF-8");

        String type = file.getName().substring(file.getName().lastIndexOf(".") + 1);
        // 创建多部分请求体
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", UuidUtil.get32UUID() + "." + type, fileBody)
                .build();
        System.out.println(file.getName());

        // 创建请求
        Request request = new Request.Builder()
                .url(zskUrl + "/api/v1/document/upload")
                .addHeader("Authorization", "Bearer " + zskKey)
                .post(requestBody)
                .build();
        // 打印请求头
        System.out.println("Request URL: " + request.url());
        System.out.println("Request Headers: " + request.headers());

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
                jsonObject = JSONObject.parseObject(responseBody);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    /**
     * 上传文档链接
     * @param linkUrl
     * @return
     */
    public static JSONObject uploadDocumentLink(String linkUrl){
        JSONObject jsonObject = new JSONObject();

        try {
            Map headerMap=new HashMap();
            headerMap.put("Authorization","Bearer " + zskKey);

            Map dataMap=new HashMap();
            dataMap.put("link",linkUrl);
            String post = HttpClients.post(zskUrl + "/api/v1/document/upload-link", dataMap, headerMap);
            if(StringUtils.isNotEmpty(post)){
                jsonObject = JSONObject.parseObject(post);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    /**
     * 文件跟智能体工作空间(知识库）关联并做向量化处理
     * @param slug
     * @return
     */
    public static JSONObject updateEmbeddings(String slug, List<String> adds, List<String> deletes){
        JSONObject jsonObject = new JSONObject();

        try {
            Map headerMap=new HashMap();
            headerMap.put("Authorization","Bearer " + zskKey);
            headerMap.put("Content-Type","application/json");

            JSONObject dataObject =  new JSONObject();
            if(adds !=null){
                dataObject.put("adds", adds);
            }
            if(deletes !=null){
                dataObject.put("deletes", deletes);
            }

            String post = HttpClients.postJson(zskUrl + "/api/v1/workspace/"+slug+"/update-embeddings", dataObject.toString(), headerMap);
            if(StringUtils.isNotEmpty(post)){
                jsonObject = JSONObject.parseObject(post);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    /**
     * 文件检索生效
     * @param slug
     * @return
     */
    public static JSONObject updatePin(String slug, String docPath, Boolean pinStatus){
        JSONObject jsonObject = new JSONObject();

        try {
            Map headerMap=new HashMap();
            headerMap.put("Authorization","Bearer " + zskKey);

            JSONObject dataObject =  new JSONObject();
            dataObject.put("docPath",docPath);
            dataObject.put("pinStatus",pinStatus);

            String post = HttpClients.postJson(zskUrl + "/api/v1/workspace/"+slug+"/update-pin", dataObject.toString(), headerMap);
            if(StringUtils.isNotEmpty(post)){
                jsonObject = JSONObject.parseObject(post);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    /**
     * 删除文件
     * @param locations
     * @return
     */
    public static JSONObject removeDocuments(List<String> locations){
        JSONObject jsonObject = new JSONObject();

        try {
            Map headerMap=new HashMap();
            headerMap.put("Authorization","Bearer " + zskKey);
            headerMap.put("Content-Type","application/json");

            JSONObject dataObject =  new JSONObject();
            if(locations !=null){
                dataObject.put("locations", locations);
            }

            String post = HttpClients.postJson(zskUrl + "/api/v1/system/remove-documents", dataObject.toString(), headerMap);
            if(StringUtils.isNotEmpty(post)){
                jsonObject = JSONObject.parseObject(post);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
}
