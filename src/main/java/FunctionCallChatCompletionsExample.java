import com.volcengine.ark.runtime.model.completion.chat.*;
import com.volcengine.ark.runtime.service.ArkService;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
@Slf4j
public class FunctionCallChatCompletionsExample {
    public static void main(String[] args) {
        String apiKey = "841d51ce-7564-432a-b6bc-4d7301468b80";
        ArkService service = ArkService.builder().apiKey(apiKey).build();
        String endpointId = "ep-20250228184935-9zrbx";

        System.out.println("\n----- function call request -----");
        final List<ChatMessage> messages = new ArrayList<>();


        //预算15%出自哪一篇文章
        //吴院长2023年发表过多少篇文章
        //新建信息化项目要求网络安全预算不低于项目总预算的多少？
        //谁主持了北京大学校访日？
        String userQuestion = "下午几点钟放学？";
        String systemPrompt = "# 系统角色\n" +
                "你是一个智慧校园助手，依托学校知识库，为师生提供服务。\n" +
                "\n" +
                "# 任务要求\n" +
                "## 功能使用\n" +
                "- 当师生询问课表信息、选修课上课情况、上课时间、放学时间时，使用提供的工具集（function call）进行查询并准确反馈结果。\n" +
                "- 尽可能的使用语义贴近的多个工具集获取结果，交由大模型整合输出\n" +
                "- 对于其他与校园相关的问题，依据学校知识库进行解答。\n" +
                "\n" +
                "## 交流风格\n" +
                "- 语言简洁明了，避免复杂生僻的词汇和表述，确保师生能轻松理解。\n" +
                "- 态度友好热情，展现出积极的服务态度。\n" +
                "\n" +
                "## 回复准确性\n" +
                "- 回复内容要基于学校知识库和工具集查询结果，保证信息准确无误。\n" +
                "- 若遇到知识库中没有的信息，诚实地告知师生暂无法提供相关内容。\n" +
                "\n" +
                "# 工作流程\n" +
                "- 首先，倾听师生的问题。\n" +
                "- 其次，判断问题类型，若为课表信息、选修课上课查询，调用工具集查询；若为其他校园相关问题，从学校知识库查找答案。\n" +
                "- 最后，将查询或解答结果清晰准确地反馈给师生。 ";

        final ChatMessage sysMessage = ChatMessage.builder()
                .role(ChatMessageRole.SYSTEM).content(systemPrompt)
                .build();
        final ChatMessage userMessage = ChatMessage.builder()
                .role(ChatMessageRole.USER)
                .content("## 用户问题:\n" +userQuestion)
                .build();
        messages.add(sysMessage);
        messages.add(userMessage);

        final List<ChatTool> tools = Arrays.asList(
                new ChatTool(
                        "function",
                        new ChatFunction.Builder()
                                .name("查询课表")
                                .description("获取学生课表信息(不包含选修课信息)，返回内容包含上课科目(示例语文、数学、英语、物理、道德与法治、化学、生物、地理、历史、思想政治等)" +
                                        "，上课时间（示例 13:40-14:20、16:00-16:40 ）、上课老师(教师)姓名、上课地点(示例 阶梯教室、三年级五班教室、NJ301)、" +
                                        "上课内容、上课节次(示例  第1节、第2节等)、上课周次（示例  第1周、第2周等）、周内天（示例  周一、周二、周三等）、学年学期等信息(示例 2023学年秋季学期、2024学年春季学期等)")
                                .parameters(new Questtion(
                                        "object",
                                        new HashMap<String, Object>() {{
                                            put("userId", new HashMap<String, String>() {{
                                                put("type", "string");
                                                put("description", "当前用户在数字校园中的标识");
                                            }});
                                        }},
                                        Collections.singletonList("th")
                                ))
                                .build()
                ),
                new ChatTool(
                        "function",
                        new ChatFunction.Builder()
                                .name("查询选修课")
                                .description("获取选修课的上课信息，返回内容包含选修课的上课时间（示例 13:40-14:20、16:00-16:40 ）" +
                                        "、上课老师(教师)姓名、上课地点(示例 阶梯教室、三年级五班教室、NJ301)" +
                                        "、选修课内容（示例  手工制作、国画写生、机器人课程等）、上课节次(示例  第1节、第2节等)、上课周次（示例  第1周、第2周等）、周内天（示例  周一、周二、周三等）、学年学期等信息(示例 2023学年秋季学期、2024学年春季学期等)")
                                .parameters(new Questtion(
                                        "object",
                                        new HashMap<String, Object>() {{
                                            put("userId", new HashMap<String, String>() {{
                                                put("type", "string");
                                                put("description", "当前用户在数字校园中的标识");
                                            }});
                                        }},
                                        Collections.singletonList("th")
                                ))
                                .build()
                )
        );

        //回答用户提问
        final List<ChatMessage> messages2 = new ArrayList<>();
        messages2.add(sysMessage);
        final ChatMessage userMessage2 = ChatMessage.builder()
                .role(ChatMessageRole.USER)
                .content("## 用户问题:\n" +userQuestion)
                .build();
        messages2.add(userMessage2);

        //工具调用
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(endpointId)
                .messages(messages)
                .tools(tools)
                .build();
        service.createChatCompletion(chatCompletionRequest).getChoices().forEach(
                choicex -> {
                    if(choicex.getMessage().getToolCalls()!=null){
                        choicex.getMessage().getToolCalls().forEach(
                                toolCall -> {
                                    //工具调用结果封装到回答上下文中
                                    messages2.add(ChatMessage.builder().role(ChatMessageRole.TOOL)
                                            .toolCallId(toolCall.getId())
                                            .content(toolCall.getFunction().getName()+"----业务查询结果")
                                            .name(toolCall.getFunction().getName())
                                            .build());
                                });
                    }
                }
        );

        log.info("上下文信息：");
        for (ChatMessage chatMessage : messages2) {
            log.info("角色"+chatMessage.getRole().value()+","+"内容----"+chatMessage.getContent());
        }

        //回答用户提问
        ChatCompletionRequest chatCompletionRequest2 = ChatCompletionRequest.builder()
                .model(endpointId)
                .messages(messages2)
                .build();
        /*service.createChatCompletion(chatCompletionRequest2).getChoices().forEach(
                choicex -> {
                    if (choicex.getMessage().getContent() != null) {
                        System.out.println(choicex.getMessage().getContent());
                    }
                }
        );*/

        // shutdown service
        service.shutdownExecutor();
    }

    public static class Questtion {
        public String type;
        public Map<String, Object> properties;
        public List<String> required;

        public Questtion(String type, Map<String, Object> properties, List<String> required) {
            this.type = type;
            this.properties = properties;
            this.required = required;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Map<String, Object> getProperties() {
            return properties;
        }

        public void setProperties(Map<String, Object> properties) {
            this.properties = properties;
        }

        public List<String> getRequired() {
            return required;
        }

        public void setRequired(List<String> required) {
            this.required = required;
        }

        public String queryAnswer(Integer th,String wt){
            return "答案";
        }

    }

}