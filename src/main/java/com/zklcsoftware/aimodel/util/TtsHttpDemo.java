package com.zklcsoftware.aimodel.util;

import com.alibaba.fastjson.JSON;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

public class TtsHttpDemo {
    private static final Logger log = LoggerFactory.getLogger(TtsHttpDemo.class);
    public static final String API_URL = "https://openspeech.bytedance.com/api/v1/tts";

    public static void textToAudio(String appId,String token,String talk, String inputVoiceType,Double voiceSpeed, String filePath) throws IOException {
        // set your appid and access_token
        //final String appId = "1034542277";
        //final String token = "-k2EQonrX5WcS0Or2slpegAS47Wmx4vb";

        TtsRequest ttsRequest = TtsRequest.builder()
                .app(TtsRequest.App.builder()
                        .appid(appId)
                        .cluster("volcano_tts")
                        .build())
                .user(TtsRequest.User.builder()
                        .uid("uid")
                        .build())
                .audio(TtsRequest.Audio.builder()
                        .encoding("mp3")
                        .voiceType(inputVoiceType)
                        .speedRatio(voiceSpeed)
                        .build())
                .request(TtsRequest.Request.builder()
                        .reqID(UUID.randomUUID().toString())
                        .operation("query")
                        .text(talk)
                        .build())
                .build();


        String reqBody = JSON.toJSONString(ttsRequest);
        log.info("request: {}", reqBody);

        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(MediaType.get("application/json; charset=utf-8"), reqBody);
        Request request = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer;" + token)
                .post(body)
                .build();

        Response response = client.newCall(request).execute();
        if (response.code() == 200) {
            // 解码 base64 字符串并写入文件
            String base64String = JSON.parseObject(response.body().string()).getString("data");
            byte[] decodedBytes = Base64.getDecoder().decode(base64String);
            try {
                //判断父目录是否存在 不存在则创建
                ensureParentDirectoryExists(filePath);

                // 将解码后的字节写入文件
                try (FileOutputStream fos = new FileOutputStream(filePath)) {
                    fos.write(decodedBytes);
                }
            } catch (IOException e) {
                log.error("写入文件时发生错误: " + e.getMessage());
            }
        }
    }

    public static void ensureParentDirectoryExists(String filePath) {
        Path path = Paths.get(filePath);
        Path parentDir = path.getParent();

        if (parentDir != null) {
            try {
                Files.createDirectories(parentDir);
            } catch (IOException e) {
                System.err.println("Failed to create directories: " + e.getMessage());
            }
        }
    }

    public static void mergeFiles(List<String> inputFiles, String outputFile) throws IOException {
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputFile))) {
            for (String inputFile : inputFiles) {
                if(new File(inputFile).exists()){
                    try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(inputFile))) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = bis.read(buffer)) != -1) {
                            bos.write(buffer, 0, bytesRead);
                        }
                    }
                }
            }
        }
    }

    /*public static void main(String[] args) throws IOException {
        List<String> mp3List= new ArrayList<>();
        mp3List.add("C:\\webapp\\files\\aimodel\\ai\\4028e4a2948c910a01948c912e9e0001-0.mp3");
        mp3List.add("C:\\webapp\\files\\aimodel\\ai\\4028e4a2948c910a01948c912e9e0001-1.mp3");
        mp3List.add("C:\\webapp\\files\\aimodel\\ai\\4028e4a2948c910a01948c912e9e0001-2.mp3");
        mp3List.add("C:\\webapp\\files\\aimodel\\ai\\4028e4a2948c910a01948c912e9e0001-3.mp3");
        mergeFiles(mp3List,"C:\\webapp\\files\\aimodel\\ai\\4028e4a2948c910a01948c912e9e0001.mp3");
    }*/
}
