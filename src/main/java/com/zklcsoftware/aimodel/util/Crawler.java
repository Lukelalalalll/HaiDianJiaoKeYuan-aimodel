package com.zklcsoftware.aimodel.util;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Crawler {

    String outPut="";//输出目录
    static Map<Integer,String> xdMap=new HashMap<>();
    static Map<Integer,String> xkMap=new HashMap<>();
    static {
        xdMap.put(1,"小学");
        xdMap.put(20,"初中");
        xdMap.put(30,"高中");

        xkMap.put(1,"语文");
        xkMap.put(2,"数学");
        xkMap.put(3,"英语");
        xkMap.put(4,"物理");
        xkMap.put(5,"化学");
        xkMap.put(6,"生物");
        xkMap.put(7,"政治");
        xkMap.put(8,"历史");
        xkMap.put(9,"地理");
        xkMap.put(16,"科学");
    }




    public static void main(String[] args) {
        Integer[] param1Array = {1, 20,30};
        Integer[] param2Array = {1,2,3,4,5,6,7,8,9,16};
        String baseUrl = "https://zuoye.hdzypt.cn/zbtiku/tiku/tikupoint?grade={param1}&subject={param2}&token=6347391f9084699c9efbaa530577044f";
        String outputDirectory = "C:\\webapp\\files\\aimodel\\stzsd";

        File directory = new File(outputDirectory);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            for (Integer param1 : param1Array) {
                for (Integer param2 : param2Array) {
                    String url = baseUrl.replace("{param1}", String.valueOf(param1)).replace("{param2}", String.valueOf(param2));

                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    HttpGet request = new HttpGet(url);

                    try (CloseableHttpResponse response = httpClient.execute(request)) {
                        if (response.getStatusLine().getStatusCode() == 200) {
                            String responseBody = EntityUtils.toString(response.getEntity());
                            ObjectMapper objectMapper = new ObjectMapper();
                            JsonNode rootNode = objectMapper.readTree(responseBody);

                            if (rootNode.path("errNo").asInt() == 0) {
                                JsonNode dataNode = rootNode.path("data").path("tree");
                                List<String> results =new ArrayList<>();

                                //列出知识点
                                /*for (JsonNode jsonNode : dataNode) {
                                    results.addAll( extractLeafNodes(jsonNode, "",param1,param2));
                                }*/

                                String fileName = outputDirectory +File.separator+ xdMap.get(param1) + File.separator + xkMap.get(param2) +File.separator+ "stzsd.json";
                                File file=new File(fileName);
                                if(!file.getParentFile().exists()){
                                    file.getParentFile().mkdirs();
                                }

                                FileWriter fileWriter = new FileWriter(fileName);
                                fileWriter.write(dataNode.toString());
                                fileWriter.close();

                                if(results!=null && results.size()>0){
                                    for (String result : results) {
                                        System.out.println(result);
                                    }
                                }

                            } else {
                                System.out.println("Error: " + rootNode.path("errStr").asText());
                            }
                        } else {
                            System.out.println("Failed to fetch data for " + param1 + "-" + param2);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<String> extractLeafNodes(JsonNode node, String parentTitle,Integer param1,Integer param2) {
        List<String> results = new ArrayList<>();
        String currentTitle = parentTitle.isEmpty() ? node.path("title").asText() : parentTitle + "-" + node.path("title").asText();
        int pintId = node.path("pintId").asInt();

        if (node.path("children").isArray() && node.path("children").size() > 0) {
            for (JsonNode childNode : node.path("children")) {
                results.addAll(extractLeafNodes(childNode, currentTitle,param1,param2));
            }
        } else {
            results.add(String.format("｛\"学科\": \"%s\", \"学段\": %s,\"知识点名称\": \"%s\", \"知识点ID\": %d｝",xdMap.get(param1),xkMap.get(param2), currentTitle, pintId));
        }

        return results;
    }
}
