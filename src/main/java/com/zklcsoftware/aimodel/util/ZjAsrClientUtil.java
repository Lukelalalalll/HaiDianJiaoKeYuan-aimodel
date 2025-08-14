package com.zklcsoftware.aimodel.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
/**
 * @Description 火山引擎语音识别API
 * @Author zhushaog
 * @UpdateTime 2025/1/10 17:17
 * @throws
 */
public class ZjAsrClientUtil {
    private static final Logger logger = LoggerFactory.getLogger(ZjAsrClientUtil.class);

    public static String audio2text(String audio_path) throws URISyntaxException, JsonProcessingException, FileNotFoundException {
        String appid = ConstantUtil.sysConfig.get("tts_appid");  // 项目的 appid
        String token = ConstantUtil.sysConfig.get("tts_token");  // 项目的 token
        String cluster = "volcengine_streaming_common";  // 请求的集群
        AsrClient asr_client = null;

        String audio_format="mp3";
        if(audio_path.toLowerCase().endsWith(".wav")){
            audio_format="wav";
        }

        StringBuilder stringBuilder=new StringBuilder();
        FileInputStream fp = null;
        try {
            asr_client = AsrClient.build();
            asr_client.setAppid(appid);
            asr_client.setToken(token);
            asr_client.setCluster(cluster);
            asr_client.setFormat(audio_format);
            asr_client.setShow_utterances(true);
            asr_client.asr_sync_connect();

            File file = new File(audio_path);
            fp = new FileInputStream(file);
            byte[] b = new byte[16000];
            int len = 0;
            int count = 0;
            AsrResponse asr_response = new AsrResponse();
            while ((len = fp.read(b)) > 0) {
                count += 1;
                logger.info("send data pack length: {}, count {}, is_last {}", len, count, fp.available() == 0);
                asr_response = asr_client.asr_send(Arrays.copyOfRange(b, 0, len), fp.available() == 0);
            }

            // get asr text
//            AsrResponse response = asr_client.getAsrResponse();
            for (AsrResponse.Result result: asr_response.getResult()) {
                stringBuilder.append(result.getText());
                logger.info(result.getText());
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        } finally {
            if (asr_client != null) {
                asr_client.asr_close();
            }
            if(fp!=null){
                try {
                    fp.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return stringBuilder.toString();
    }

    /*public static void main(String[] args) throws URISyntaxException, JsonProcessingException, FileNotFoundException {
        String appid = "1034542277";  // 项目的 appid
        String token = "-k2EQonrX5WcS0Or2slpegAS47Wmx4vb";  // 项目的 token
        String cluster = "volcengine_streaming_common";  // 请求的集群
        String audio_path = "D:\\webapp\\files\\aimodel\\tts-55115c410bf44d2e95d276878d0b8fca.mp3";  // 本地音频文件路径；
        String audio_format = "mp3";  // wav 或者 mp3, 根据音频类型设置

        AsrClient asr_client = null;
        try {
            asr_client = AsrClient.build();
            asr_client.setAppid(appid);
            asr_client.setToken(token);
            asr_client.setCluster(cluster);
            asr_client.setFormat(audio_format);
            asr_client.setShow_utterances(true);
            asr_client.asr_sync_connect();

            File file = new File(audio_path);
            FileInputStream fp = new FileInputStream(file);
            byte[] b = new byte[16000];
            int len = 0;
            int count = 0;
            AsrResponse asr_response = new AsrResponse();
            while ((len = fp.read(b)) > 0) {
                count += 1;
                logger.info("send data pack length: {}, count {}, is_last {}", len, count, fp.available() == 0);
                asr_response = asr_client.asr_send(Arrays.copyOfRange(b, 0, len), fp.available() == 0);
            }

            // get asr text
//            AsrResponse response = asr_client.getAsrResponse();
            for (AsrResponse.Result result: asr_response.getResult()) {
                logger.info(result.getText());
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        } finally {
            if (asr_client != null) {
                asr_client.asr_close();
            }
        }
    }*/
}
