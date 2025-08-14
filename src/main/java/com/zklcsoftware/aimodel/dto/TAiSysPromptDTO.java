package com.zklcsoftware.aimodel.dto;

import com.fasterxml.jackson.annotation.JsonView;
import com.zklcsoftware.basic.model.AbstractBaseDomain;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.persistence.Column;

@SuppressWarnings("serial")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel(value = "TAiSysPrompt", description = "自定义智能体")
public class TAiSysPromptDTO extends AbstractBaseDomain {
	

	private String	id;

	@ApiModelProperty(value = "智能体名称")
	private String	name;

	@ApiModelProperty(value = "智能体描述")
	private String	notes;

	@ApiModelProperty(value = "业务分类(字典)")
	private Integer	busType;

	@ApiModelProperty(value = "图片")
	private String	proImg;

	@ApiModelProperty(value = "引用模型标识")
	private String	modelId;

	@ApiModelProperty(value = "排序字段 越小越靠前")
	private Integer	orderNo;

	@ApiModelProperty(value = "角色设定")
	private Integer	jssd;


	@ApiModelProperty(value = "模板内容")
	private String	promptTemplate;

	@ApiModelProperty(value = "发布状态(0未发布  1已发布  2已停用)")
	private Integer	publishStatus;

	@ApiModelProperty(value = "发布范围(个人可见  全校可见)")
	private Integer	publishRange;

	@ApiModelProperty(value = "开场白")
	private String	prologue;

	@ApiModelProperty(value = "兜底回复")
	private String	bakreply;
	@ApiModelProperty(value = "第三方agentId")
	private String	agentId;
}
