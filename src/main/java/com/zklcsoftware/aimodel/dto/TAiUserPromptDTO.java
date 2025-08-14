package com.zklcsoftware.aimodel.dto;

import com.zklcsoftware.basic.model.AbstractBaseDomain;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@SuppressWarnings("serial")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel(value = "TAiUserPrompt", description = "用户提示词(提示词广场)")
public class TAiUserPromptDTO extends AbstractBaseDomain {

	private String	id;

	@ApiModelProperty(value = "提示词名称")
	private String	name;

	@ApiModelProperty(value = "提示词描述")
	private String	notes;

	@ApiModelProperty(value = "模板内容")
	private String	promptTemplate;

	@ApiModelProperty(value = "图片")
	private String	proImg;

	@ApiModelProperty(value = "业务分类(字典)")
	private Integer	busType;

	@ApiModelProperty(value = "排序字段 越小越靠前")
	private Integer	orderNo;

	@ApiModelProperty(value = "发布状态(0未发布  1已发布  2已停用)")
	private Integer	publishStatus;

	@ApiModelProperty(value = "发布范围(个人可见  全校可见)")
	private Integer	publishRange;

}
