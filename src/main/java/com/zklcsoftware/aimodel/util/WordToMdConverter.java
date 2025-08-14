package com.zklcsoftware.aimodel.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionContentPart;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import com.volcengine.ark.runtime.service.ArkService;
import com.zklcsoftware.aimodel.dto.MessageDTO;
import com.zklcsoftware.aimodel.websocket.dto.AiOutMsgDTO;
import com.zklcsoftware.basic.util.ServletUtils;
import com.zklcsoftware.common.web.util.Base64Util;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.internal.sse.RealEventSource;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

@Slf4j
public class WordToMdConverter {
    /**
     * @Description 根据pandoc工具处理doc2md
     * @Author zhushaog
     * @param: file
     * @UpdateTime 2025/4/25 10:25
     * @return: java.lang.String
     * @throws
     */
    public static String doc2mdByPandoc(File file) {

        String mdContent="";
        try {
            String pandocCmd="pandoc";
            // 构建 Pandoc 命令
            List<String> command = new ArrayList<>();
            command.add(pandocCmd);
            command.add(file.getAbsolutePath());
            command.add("-o");
            command.add(file.getAbsolutePath()+".md");

            // 执行 Pandoc 命令
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();
            int extCode=process.waitFor();
            if(extCode==0){
                mdContent=new FileContentReader().readFileContent(new File(file.getAbsolutePath()+".md"),null);
                //清理掉临时文件
                Files.deleteIfExists(new File(file.getAbsolutePath()+".md").toPath());
            }
        } catch (Exception e) {
            log.error("doc转word异常---pandoc工具",e);
        }
        return mdContent;
    }

    /**
     * @Description 根据Python工具包处理doc2md,
     * curl -X POST -F "file=@/webapp/schoolcloud-datas/aimodel/webapp/files/aimodel/ai/context/作文七年级.docx" -F "detect_titles=false" http://39.105.192.32:18080/docx2mdservice/convert --output 作文七年级.docx.md
     * @Author zhushaog
     * @param: file
     * @UpdateTime 2025/4/25 10:25
     * @return: java.lang.String
     * @throws
     */
    public static String doc2mdByPythonUtil(File file) {
        String mdContent="";
        try {
            // 构建 curl 命令
            List<String> command = new ArrayList<>();
            command.add("curl");
            command.add("-X");
            command.add("POST");
            command.add("-F");
            command.add("file=@"+file.getAbsolutePath());
            command.add("-F");
            command.add("detect_titles=true");
            command.add(ConstantUtil.sysConfig.get("docx2mdservice")+"/docx2mdservice/convert");
            command.add("--output");
            command.add(file.getAbsolutePath()+".md");
            // 执行 Pandoc 命令
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();
            int extCode=process.waitFor();
            if(extCode==0){
                mdContent=new FileContentReader().readFileContent(new File(file.getAbsolutePath()+".md"),null);
                //清理掉临时文件
                Files.deleteIfExists(new File(file.getAbsolutePath()+".md").toPath());
            }
        } catch (Exception e) {
            log.error("doc转word异常---python工具",e);
        }
        return mdContent;

    }

    /*public static void main(String[] args) {
        ConstantUtil.sysConfig.put("docx2mdservice","http://39.105.192.32:18080");
        System.out.println(WordToMdConverter.doc2mdByPythonUtil(new File("D:\\海淀教科院新闻资料\\同步教科院\\小海灵\\资料清单.docx")));
    }*/

    /**
     * @Description 使用AI处理doc2md
     * @Author zhushaog
     * @param: wordContent
     * @UpdateTime 2025/4/25 10:43
     * @return: java.lang.String
     * @throws
     */
    public static String doc2mdByAi(String wordContent,String deepseek_api,String deepseek_modelid,String deepseek_apikey) {
        String mdContent="";

        AiOutMsgDTO aiOutMsgDTO=null;
        StringBuilder fullContent = new StringBuilder();
        // 创建消息列表
        List<MessageDTO> userChatMessages=new ArrayList<>();
        userChatMessages.add(MessageDTO.builder()
                .role("user")
                .content("对下面的【文档内容】进行处理 整理成合理的层级结构，按md形式输出，请保持原文内容只做整理，不要对文本进行总结润色。## 输出要求 \n 不要以'```markdown'开头、不要以```结尾; \n## 文档内容\\n"+wordContent)
                .build());

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("model", deepseek_modelid);
        jsonObject.put("stream", true);
        jsonObject.put("messages", userChatMessages);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json;chartset=uft-8"),jsonObject.toString());
        // 创建请求对象
        Request request = new Request.Builder()
                .url(deepseek_api)
                .post(requestBody) // 请求体
                .addHeader("Authorization", "Bearer " + deepseek_apikey)
                .addHeader("Content-Type", "application/json")
                .build();

        // 开启 Http 客户端
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)   // 建立连接的超时时间
                .readTimeout(10, TimeUnit.MINUTES)  // 建立连接后读取数据的超时时间
                .build();

        // 创建一个 CountDownLatch 对象，其初始计数为1，表示需要等待一个事件发生后才能继续执行。
        CountDownLatch eventLatch = new CountDownLatch(1);

        // 实例化EventSource，注册EventSource监听器 -- 创建一个用于处理服务器发送事件的实例，并定义处理事件的回调逻辑
        RealEventSource realEventSource = new RealEventSource(request, new EventSourceListener() {
                @Override
                public void onEvent(EventSource eventSource, String id, String type, String data) {
                    //log.info("百川智能文档回答:" + data);
                    JSONObject message=JSONObject.parseObject(data);
                    String finish_reason=message.getJSONArray("choices").getJSONObject(0).getString("finish_reason");
                    fullContent.append(message.getJSONArray("choices").getJSONObject(0).getJSONObject("delta").getString("content"));
                    if ("stop".equals(finish_reason)) {    // 消息类型，add 增量，finish 结束，error 错误，interrupted 中断
                        eventLatch.countDown();
                    }
                }
                @Override
                public void onClosed(EventSource eventSource) {
                    eventLatch.countDown(); // 重新设置中断状态
                }
                @Override
                public void onFailure(EventSource eventSource, @Nullable Throwable t, @Nullable Response response) {
                    eventLatch.countDown(); // 重新设置中断状态
                    log.error("doc转md，使用AI整理异常：{}",response);
                }
            }
        );
        // 与服务器建立连接
        realEventSource.connect(okHttpClient);
        try {
            eventLatch.await();
        } catch (InterruptedException e) {
            eventLatch.countDown(); // 重新设置中断状态
            realEventSource.cancel();
        }
        mdContent= fullContent.toString();
        return mdContent;
    }


    public static void main(String[] args) {
        String filePath="D:\\\\海淀教科院新闻资料\\\\教案_提取_图片\\\\2024京教杯上传教学设计（海淀区各科）\\\\海淀区“风采杯”中学教师教学成果展示活动\\\\语文\\\\七年级语文\\\\";//原文件目录
        String outfilePath="D:\\\\海淀教科院新闻资料\\\\教案_提取_图片\\\\2024京教杯上传教学设计（海淀区各科）\\\\海淀区“风采杯”中学教师教学成果展示活动\\\\语文\\\\七年级语文\\\\out_put";//原文件目录
        AiOutMsgDTO aiOutMsgDTO=null;
        try {

            //ArkService service = ArkService.builder().dispatcher(dispatcher).connectionPool(connectionPool).baseUrl("https://ark.cn-beijing.volces.com/api/v3").apiKey(tAiModel.getAppkey()).build();
            ArkService service = ArkService.builder().apiKey("841d51ce-7564-432a-b6bc-4d7301468b80").build();
            String endpointId= "ep-20241230180032-98b74";

            StringBuilder fullContent = new StringBuilder();
            File file = new File(filePath);
            for (File listFile : file.listFiles()) {
                final List<ChatMessage> streamMessages = new ArrayList<>();
                List<ChatCompletionContentPart> multiContent=new ArrayList<>();
                multiContent.add(ChatCompletionContentPart.builder().type("text")
                        .text("##需求描述\n从图片中提取主题或单元名称、" +
                                "学段(小学/初中)、" +
                                "教材版本(人教版/北师大版/人教版(2024)/北师大版(2024)/北京版/北京版(2024))、" +
                                "教材书本(一年级上册/一年级下册/二年级上册/二年级下册/三年级上册/三年级下册/四年级上册/四年级下册/五年级上册/五年级下册/六年级上册/六年级下册/七年级上册/七年级下册/八年级上册/八年级下册/九年级上册/九年级下册)、" +
                                "学科(语文/数学/英语/物理/化学/生物学/历史/地理/道德与法治/音乐/美术/科学/信息科技)、" +
                                "所属领域、所属模块等信息，\n " +
                                "##输出要求\n" +
                                "1、请根据语义并参考给定字典列表设置值不要有多余字符，忽略掉个人敏感数据信息，键值可使用中文。\n" +
                                "2、以json对象形式输出").build());
                String fileAbsolutePath=listFile.getAbsolutePath();
                String imgUrl=Base64Util.getImageStr(fileAbsolutePath);//转换base64编码
                multiContent.add(ChatCompletionContentPart.builder()
                        .type("image_url")
                        .imageUrl(new ChatCompletionContentPart.ChatCompletionContentPartImageURL(imgUrl))//识别内容
                        .build());
                final ChatMessage  chatMessage = ChatMessage.builder()
                        .role(ChatMessageRole.USER)
                        .multiContent(multiContent)
                        .build();
                streamMessages.add(chatMessage);
                ChatCompletionRequest streamChatCompletionRequest = ChatCompletionRequest.builder()
                        .model(endpointId)
                        .messages(streamMessages)
                        .build();
                StringBuffer aiContent = new StringBuffer();
                try {
                    service.streamChatCompletion(streamChatCompletionRequest)
                            .doOnError(Throwable::printStackTrace)
                            .blockingForEach(
                                    choice -> {
                                        if (choice.getChoices().size() > 0) {
                                            //log.info("字节豆包模型回答：{}", choice.getChoices().get(0).getMessage().getContent());
                                            aiContent.append(choice.getChoices().get(0).getMessage().getContent());
                                        }
                                    }
                            );
                    JSONObject jsonObject=JSON.parseObject(aiContent.toString().replaceAll("```",""));
                    jsonObject.put("文件名",fileAbsolutePath.replaceAll(filePath,""));
                    writeToFileWithUTF8(jsonObject.toJSONString(),outfilePath+File.separator+fileAbsolutePath.replaceAll(filePath,"")+".json",false);
                    //输出sql
                    System.out.println(convertJsonToInsertSql(jsonObject.toJSONString()));;

                    //fullContent.append(jsonObject.toJSONString()+"\n");
                } catch (Exception e) {
                    log.error("字节豆包模型回答API异常",e);
                }
            }

            // shutdown service
            service.shutdownExecutor();
            //用户提问及回答结果保存 用于上下文回答
            //log.info(fullContent.toString());
        } catch (Exception e) {
            log.error("字节豆包模型回答API异常",e);
        }
    }

    /**
     * 将文本内容以 UTF-8 编码写入指定文件
     *
     * @param content 要写入的文本内容
     * @param filePath 目标文件路径
     * @param append   是否追加写入
     */
    public static void writeToFileWithUTF8(String content, String filePath, boolean append) {
        File file = new File(filePath);

        // 创建父目录
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.err.println("创建文件失败: " + filePath);
                e.printStackTrace();
                return;
            }
        }

        // 使用 UTF-8 编码写入
        try (OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(file, append), StandardCharsets.UTF_8)) {
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static String convertJsonToInsertSql(String jsonContent) {
        JSONObject jsonObject = JSONObject.parseObject(jsonContent);

        // 生成 UUID 主键
        String id = UUID.randomUUID().toString().toLowerCase().replace("-", "");

        String xkmc = getStringValue(jsonObject, "学科");
        String ssly = getStringValue(jsonObject, "所属领域");
        String jcsb = getStringValue(jsonObject, "教材书本");
        String jcbb = getStringValue(jsonObject, "教材版本");
        String xd = getStringValue(jsonObject, "学段");
        String ztdym = getStringValue(jsonObject, "主题单元名");
        String ssmk = getStringValue(jsonObject, "所属模块");
        String wjm = getStringValue(jsonObject, "文件名");

        // 构建 SQL 语句（忽略 create 和 update 字段）
        return String.format(
                "INSERT INTO `t_ai_ja_example` (`id`, `xkmc`, `ssly`, `jcsb`, `xd`, `ztdym`, `ssmk`, `wjm`,`jcbb`) VALUES ('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s');",
                id, escapeSingleQuotes(xkmc), escapeSingleQuotes(ssly), escapeSingleQuotes(jcsb),
                escapeSingleQuotes(xd), escapeSingleQuotes(ztdym), escapeSingleQuotes(ssmk), escapeSingleQuotes(wjm),escapeSingleQuotes(jcbb)
        );
    }

    private static String getStringValue(JSONObject obj, String key) {
        return obj.containsKey(key) ? obj.getString(key) : "";
    }

    private static String escapeSingleQuotes(String value) {
        return value == null ? "" : value.replace("'", "''");
    }

}
