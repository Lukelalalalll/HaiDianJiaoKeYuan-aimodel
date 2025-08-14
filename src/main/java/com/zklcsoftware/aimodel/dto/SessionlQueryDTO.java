package com.zklcsoftware.aimodel.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("serial")

@ApiModel(value = "查询会话数据体", description = "查询会话数据体")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SessionlQueryDTO implements Serializable {

	@ApiModelProperty(value = "查询类型(1-常规模型对比会话  2-智能体会话-使用用户  3-智能体会话-智能体创建用户  4-智能应用会话)")
	Integer sessionType;

	@ApiModelProperty(value = "查询最近数量 0或者空则查全部")
	Integer lastNum;

	@ApiModelProperty(value = "智能体id")
	String sysPromptId;

	@ApiModelProperty(value = "智能体id集合")
	List<String> sysPromptIds;
}
