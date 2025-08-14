package com.zklcsoftware.aimodel.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Ai相关Websocket交互消息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AiInMsgDTO implements Serializable {
    private String modelId;//选填  大模型PK时必填
    private String sysPromptId; //选填  智能体提问必填
    private String dataType;//text-文档回答   json-其他类型回答(含ocr识别类型)
    private Object data;//由dataType决定
    private Integer responseType; //期望响应方式   1-流式 2-直调   (智能体需支持两种调用方式，否则该参数不予生效)
}