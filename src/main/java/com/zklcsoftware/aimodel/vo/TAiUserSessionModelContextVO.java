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
public class TAiUserSessionModelContextVO extends AbstractBaseDomain {

	@ApiModelProperty(value = "用户输入内容")
	private String	userIn;

	@ApiModelProperty(value = "模型回复内容")
	private String	assistantOut;

	@ApiModelProperty(value = "排序号")
	private Integer orderNo;
	@ApiModelProperty(value = "当前问答标识")
	private String id;

	@ApiModelProperty(value = "当前文档扩展信息")
	private String extJson;

	@ApiModelProperty(value = "知识库文档（可下载）")
	private List<TAiUserSessionModelContextFileindexVO> fileindexList;


	
}
