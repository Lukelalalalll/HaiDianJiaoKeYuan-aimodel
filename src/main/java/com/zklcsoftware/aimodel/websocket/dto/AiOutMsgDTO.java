package com.zklcsoftware.aimodel.websocket.dto;

import com.zklcsoftware.common.dto.OperaResult;
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
public class AiOutMsgDTO implements Serializable {
    private String contextId;
    private Integer responseType=1; //1-流式 2-直调
    private String dataType="text"; //text-文档回答   json-其他类型回答(含ocr识别类型)
    private Object data;
    private Boolean done;

    public static AiOutMsgDTO wsSuccessText(Object value){
        return AiOutMsgDTO.builder().responseType(1).dataType("text").data(value).done(false).build();
    }
    public static AiOutMsgDTO wsSuccessDone(String contextId, Object value){
        return AiOutMsgDTO.builder().responseType(1).contextId(contextId).dataType("text").data(value).done(true).build();
    }

}