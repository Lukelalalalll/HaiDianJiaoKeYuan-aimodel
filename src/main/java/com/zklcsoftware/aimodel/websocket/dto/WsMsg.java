package com.zklcsoftware.aimodel.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Websocket交互消息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WsMsg implements Serializable {
    /**
     * 消息ID，对于客户端而言，只要消息不重复即可
     */
    private String id;

    /**
     * 原始消息id（当消息类型是ack时）
     */
    private String origid;

    /**
     * 消息用户，这里使用客户端的userid
     */
    private String from;

    /**
     * 消息用户名称，这里使用客户端的userid
     */
    private String fromUserName;

    /**
     * 消息类型，ly/chat
     */
    private String type;

    /**
     * 消息数据
     */
    private String data;
}