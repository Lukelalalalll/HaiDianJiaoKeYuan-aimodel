package com.zklcsoftware.aimodel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author zcg
 * @version 1.0
 * @ClassName TongjiDTO.java
 * @company zklcsoftware
 * @Description TODO
 * @createTime 2025/01/12 14:00
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TongjiDTO implements Serializable {

        String title;//标题
        String value;//值

}
