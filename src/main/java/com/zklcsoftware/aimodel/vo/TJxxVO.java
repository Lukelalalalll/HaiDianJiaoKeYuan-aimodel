package com.zklcsoftware.aimodel.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author zhushaog
 * @version 1.0
 * @ClassName TJxxVO.java
 * @company zklcsoftware
 * @Description TODO
 * @createTime 2024/10/21 14:52
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel(value = "TJxxVO", description = "统计信息")
public class TJxxVO implements Serializable {
    @ApiModelProperty(value = "用户类型  101004学生  101002教师")
    private Integer userType;
    @ApiModelProperty(value = "统计日期   年-月")
    private String tjrq;
    @ApiModelProperty(value = "次数")
    private Long questionNums;
    @ApiModelProperty(value = "用户姓名")
    private String chName;

}
