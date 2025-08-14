package com.zklcsoftware.aimodel.vo;


import com.zklcsoftware.basic.model.AbstractBaseDomain;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@SuppressWarnings("serial")

@ApiModel(value = "TAiModel", description = "模型列表")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class TAiUserSessionModelVO extends AbstractBaseDomain {

	@ApiModelProperty(value = "会话名称(不填默认 提问问题内容)")
	private String	id;
	@ApiModelProperty(value = "模型标识")
	private String	modelId;
	@ApiModelProperty(value = "模型名称")
	private String	modelName;
	@ApiModelProperty(value = "模型图片")
	private String	modelImg;
	@ApiModelProperty(value = "会话上下文列表")
	private List<TAiUserSessionModelContextVO> contextList;


	
}
