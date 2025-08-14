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

@ApiModel(value = "TAiModel", description = "模型列表")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class TAiUserSessionDTO implements Serializable {

	@ApiModelProperty(value = "会话名称(不填默认 提问问题内容)")
	private String	name;

	@ApiModelProperty(value = "选择的提示词标识 (可为空，实际内容可跟提示词内容不一致-允许修改)")
	private String	userPromptId;

	@ApiModelProperty(value = "智能体标识，默认为空表示对话对比功能的会话，会话选择模型列表必填")
	private String	sysPromptId;

	@ApiModelProperty(value = "会话选择模型列表,如果智能体标识不为空，则该值不予生效")
	private List<String> modelIds;

	private String refSessionId;//关联会话标识
	
}
