package com.zklcsoftware.aimodel.dto;

import javax.persistence.Column;

import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonView;
import com.zklcsoftware.basic.model.AbstractBaseDomain;
import com.zklcsoftware.basic.model.AbstractBaseDomain.AddValid;
import com.zklcsoftware.basic.model.AbstractBaseDomain.UpdateValid;
import com.zklcsoftware.basic.model.AbstractBaseDomain.WithoutView;

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
@ApiModel(value = "TAiModelLayoutItem", description = "用户布局智能体dto")
public class TAiModelLayoutItemDTO extends AbstractBaseDomain {
	

	@ApiModelProperty(value = "布局id")
	private String	layoutId;
	
	@ApiModelProperty(value = "组件id")
	private String	componentId;
	
	@ApiModelProperty(value = "组件名称")
	private String	componentName;
	
	@ApiModelProperty(value = "排序号")
	private Integer	itemSort;
	
	@ApiModelProperty(value = "智能体占位符")
	private String	placeHolder;
	
	@ApiModelProperty(value = "智能体初始数据")
	private String	promptInitData;
	@ApiModelProperty(value = "智能体id")
	private String	sysPromptId;
	
	@ApiModelProperty(value = "智能体名称")
	private String	promptName;
	
	@ApiModelProperty(value = "组件状态(默认1：发布  0：未发布）")
	private Integer	componentStatus;

	@ApiModelProperty(value = "支持客户端类型： pc,app")
	private String clientTypes;
	
	@ApiModelProperty(value = "第三方应用url")
	private String	otherUrl;

	@ApiModelProperty(value = "组件说明")
	private String	componentDes;
	
	
}
