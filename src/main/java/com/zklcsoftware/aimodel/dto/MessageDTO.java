package com.zklcsoftware.aimodel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author zhushaog
 * @version 1.0
 * @ClassName MessageDTO.java
 * @company zklcsoftware
 * @Description TODO
 * @createTime 2024/10/19 11:26
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageDTO implements Serializable {

        String role;//角色
        String content;//上下文内容
        String toolCallId;//工具调用id
        String toolCallFunctionName;//工具调用函数名

        public MessageDTO(String role,String content){
                this.role=role;
                this.content=content;
        }

}
