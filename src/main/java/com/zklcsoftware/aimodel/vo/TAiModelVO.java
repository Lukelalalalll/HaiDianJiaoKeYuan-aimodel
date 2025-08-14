package com.zklcsoftware.aimodel.vo;


import com.fasterxml.jackson.annotation.JsonView;
import com.zklcsoftware.basic.model.AbstractBaseDomain;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;

@SuppressWarnings("serial")

@ApiModel(value = "TAiModel", description = "模型列表")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class TAiModelVO extends AbstractBaseDomain {
	private String	id;

	@ApiModelProperty(value = "模型名称")
	private String	name;

	@ApiModelProperty(value = "备注说明")
	private String	explain;

	@ApiModelProperty(value = "logo")
	private String	iconLogo;
	@ApiModelProperty(value = "产品图片")
	private String	proImg;
	
}
