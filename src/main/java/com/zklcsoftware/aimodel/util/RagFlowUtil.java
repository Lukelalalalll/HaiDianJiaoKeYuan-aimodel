package com.zklcsoftware.aimodel.util;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.zklcsoftware.basic.util.UuidUtil;
import com.zklcsoftware.common.web.util.HttpClients;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 杨洁
 * @ClassName RagFlowUtil
 * @Description
 * @createTime 2025-03-17 13:22
 **/
public class RagFlowUtil {

    private static final String zskKey = ConstantUtil.sysConfig.get("knowledge_apikey_ragflow");//知识库API key
//    private static final String zskKey = "ragflow-M4YTI0YjgyMjBiOTExZjA4ZmVhMTJkZG";//知识库API key
    private static final String zskUrl = ConstantUtil.sysConfig.get("knowledge_apiurl_ragflow");//知识库API地址
    private static final String zskThreshold = ConstantUtil.sysConfig.get("knowledge_score_threshold");//知识库检索相似度阈值
    private static final String zsktopN = ConstantUtil.sysConfig.get("knowledge_topN");//知识库检索topN条数据

    /**
     * 创建数据集（知识库）
     * @param name
     * @return
     */
    public static JSONObject createDatasets(String name){
        JSONObject jsonObject = new JSONObject();

        try {
            Map headerMap=new HashMap();
            headerMap.put("Authorization","Bearer " + zskKey);

            JSONObject dataObject =  new JSONObject();
            dataObject.put("name",name);//名称
            dataObject.put("description",name);//描述
            dataObject.put("permission","team");//谁可以访问数据集 me:自己 team: 团队
            JSONObject chunkParserConfig = new JSONObject();
            chunkParserConfig.put("chunk_token_num",1024);
            dataObject.put("parser_config",chunkParserConfig);
            String post = HttpClients.postJson(zskUrl + "/api/v1/datasets", dataObject.toString(), headerMap);
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
     * @param datasetId 数据集ID
     * @return
     */
    public static JSONObject uploadDocument(File file, String datasetId) throws IOException {

        JSONObject jsonObject = new JSONObject();

        OkHttpClient client = new OkHttpClient();

        // 编码文件名以避免乱码
        String encodedFileName =  URLEncoder.encode(file.getPath(), "UTF-8");

        String type = file.getName().substring(file.getName().lastIndexOf("."));

        //判断文件名过长时进行截取处理  ragflow上传文件限制
        String fileName=file.getName();
        if(fileName.length()>30){
            fileName=fileName.substring(0,30)+type;
            file=FileUtil.rename(file,fileName,true);
        }

        // 创建文件请求体
        RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), file);
        // 创建多部分请求体
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", fileName, fileBody)
                .build();
        System.out.println(file.getName());

        // 创建请求
        Request request = new Request.Builder()
                .url(zskUrl + "/api/v1/datasets/"+datasetId+"/documents")
                .addHeader("Authorization", "Bearer " + zskKey)
                .addHeader("Content-Type", "multipart/form-data")
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
        jsonObject.put("filePath",file.getAbsolutePath());//返回截取名称后 重命名文件
        return jsonObject;
    }

    /**
     * 更新文档配置
     * @param datasetId
     * @param documentId
     * @param data
     * @return
     * @throws IOException
     */
    public static JSONObject updateDocument(String datasetId, String documentId, String data) throws IOException {
        JSONObject jsonObject = new JSONObject();
        try {
            HttpPut httpPut = new HttpPut(zskUrl + "/api/v1/datasets/" + datasetId + "/documents/" + documentId);
            httpPut.setHeader("Authorization","Bearer " + zskKey);
            httpPut.setHeader("Content-Type","application/json");
            httpPut.setEntity(new StringEntity(data,"UTF-8"));
            // 创建 HttpClient 实例
            HttpClient httpClient = org.apache.http.impl.client.HttpClients.createDefault();
            // 执行请求
            HttpResponse response = httpClient.execute(httpPut);
            // 获取响应状态码
            int statusCode = response.getStatusLine().getStatusCode();
            System.out.println("Status Code: " + statusCode);
            // 获取响应实体内容
            String responseBody = EntityUtils.toString(response.getEntity());
            System.out.println("Response Body: " + responseBody);
            if (responseBody != null) {
                jsonObject = JSONObject.parseObject(responseBody);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    /**
     * 解析文档
     * @param datasetId
     * @param documentIds
     * @return
     */
    public static JSONObject analyzeDocument(String datasetId, List<String> documentIds){
        JSONObject jsonObject = new JSONObject();

        try {
            Map headerMap=new HashMap();
            headerMap.put("Authorization","Bearer " + zskKey);
            headerMap.put("Content-Type","application/json");

            JSONObject dataObject =  new JSONObject();
            if(documentIds !=null){
                dataObject.put("document_ids", documentIds);
            }

            String post = HttpClients.postJson(zskUrl + "/api/v1/datasets/"+datasetId+"/chunks", dataObject.toString(), headerMap);
            if(StringUtils.isNotEmpty(post)){
                jsonObject = JSONObject.parseObject(post);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    /**
     * 删除文档
     * @param datasetId 数据集ID
     * @param documentIds
     * @return
     */
    public static JSONObject removeDocuments(String datasetId, List<String> documentIds){
        JSONObject jsonObject = new JSONObject();

        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");
        //String data = "{\"ids\": "+documentIds.toString()+"}";
        // 使用 Gson 构建 JSON 数据
        Gson gson = new Gson();
        String data = "{\"ids\": " + gson.toJson(documentIds) + "}";
        RequestBody body = RequestBody.create(mediaType, data);
        Request request = new Request.Builder()
                .url(zskUrl + "/api/v1/datasets/"+datasetId+"/documents")
                .delete(body)
                .addHeader("Authorization", "Bearer " + zskKey)
                .addHeader("Content-Type", "application/json")
                .build();
        try (Response response = client.newCall(request).execute()) {
            int statusCode = response.code();
            String responseBody = response.body().string();
            System.out.println("状态码: " + statusCode);
            System.out.println("响应内容: " + responseBody);
            if (responseBody != null) {
                jsonObject = JSONObject.parseObject(responseBody);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    /**
     * @Description 创建agent的session会话
     * @Author zhushaog
     * @param: agentId  agentId
     * @param: sessionId  指定的会话ID
     * @UpdateTime 2025/4/29 14:17
     * @return: com.alibaba.fastjson.JSONObject
     * @throws
     */
    public static JSONObject createAgentSession(String ragFlowHostUrl,String ragFlowApiKey,String agentId,String sessionId,Map  params){
        JSONObject jsonObject = new JSONObject();
        try {
            Map headerMap=new HashMap();
            headerMap.put("Authorization","Bearer " + ragFlowApiKey);
            headerMap.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

            if (params != null) {
                Map<String, String> encodedParams = encodeParams(params);
                params.putAll(encodedParams);
            }

            String post = HttpClients.post(ragFlowHostUrl + "/api/v1/agents/"+agentId+"/sessions?user_id="+sessionId, params, headerMap);
            if(StringUtils.isNotEmpty(post)){
                jsonObject = JSONObject.parseObject(post);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
    public static Map<String, String> encodeParams(Map<String, String> params) throws Exception {
        Map<String, String> encodedParams = new HashMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            encodedParams.put(entry.getKey(), new String(entry.getValue().getBytes(StandardCharsets.UTF_8), Charset.forName("ISO-8859-1")));
        }
        return encodedParams;
    }

}
