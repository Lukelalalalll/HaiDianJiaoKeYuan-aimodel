package com.zklcsoftware.aimodel.vo;


import com.zklcsoftware.basic.model.AbstractBaseDomain;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.beans.Transient;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SuppressWarnings("serial")

@ApiModel(value = "TAiModel", description = "模型列表")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class TAiUserSessionVO extends AbstractBaseDomain {

	@ApiModelProperty(value = "会话名称(不填默认 提问问题内容)")
	private String	id;

	@ApiModelProperty(value = "会话名称(不填默认 提问问题内容)")
	private String	name;

	@ApiModelProperty(value = "引用的智能体标识")
	private String	sysPromptId;

	@ApiModelProperty(value = "引用的提示词标识")
	private String	userPromptId;

	@ApiModelProperty(value = "生成的具体的会话集合")
	List<TAiUserSessionModelVO> tAiUserSessionlModels=new ArrayList<>();

	@ApiModelProperty(value = "审核状态(0待审核 1审核通过 2审核不通过)")
	private String	reviewStatus;

	private Date createTime;

	private Date updateTime;

	@ApiModelProperty(value = "智能体图片")
	private String	proImg;

	@ApiModelProperty(value = "appUrl")
	private String	appUrl;

	private Long twcs;//提问次数

	@ApiModelProperty(value = "是否启用数字人播报(1是 0否  为空否)")
	private Integer	isUseSzr;

	@ApiModelProperty(value = "组件id")
	private String	componentId;

	@ApiModelProperty(value = "组件名称")
	private String	componentName;

	@ApiModelProperty(value = "组件状态(默认1：发布  0：未发布）")
	private String	componentStatus;

	@ApiModelProperty(value = "关联会话ID")
	private String	refSessionId;
}
