package com.zklcsoftware.aimodel.websocket.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 互动房间消息
 */
@Data
@Builder
public class RoomMessageDTO implements Serializable {
    private String roomId;//房间ID
    private Long id;//发送人ID
    private String data;//发送消息体
}
