package com.zklcsoftware.aimodel.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author lipanlong
 * @version 1.0
 * @className ZhiXiangDTO
 * @description 指向数据对象
 * @date 2024/8/12 13:33
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ZhiXiangDTO implements Serializable {

    //房间号
    private String roomNo;
    //房间名称
    private String roomName;
    //目标房间号
    private String targetRoomNo;
    //目标房间名称
    private String targetRoomName;
    //转向类型
    private String turnType;
    //描述
    private String description;

}
