package com.zklcsoftware.aimodel.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhushaog
 * @version 1.0
 * @ClassName ScqdImgDTO.java
 * @company zklcsoftware
 * @Description TODO
 * @createTime 2025/04/09 13:22
 */
@Data
public class ScqdDTO implements Serializable {
    private String qdmc;//素材位置
    private List<String> scqdList;
}
