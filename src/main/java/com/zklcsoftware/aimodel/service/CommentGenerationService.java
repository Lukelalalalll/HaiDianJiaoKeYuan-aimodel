package com.zklcsoftware.aimodel.service;

import com.zklcsoftware.aimodel.websocket.dto.AiOutMsgDTO;

/**
 * @author yj
 * @ClassName CommentGenerationService.java
 * @Description 评语生成
 * @createTime 2025-01-10 14:30
 **/
public interface CommentGenerationService {

    AiOutMsgDTO CommentGeneration(String zntId, String fileUrl, String content, String userId);
}
