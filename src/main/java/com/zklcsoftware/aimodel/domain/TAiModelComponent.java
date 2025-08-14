package com.zklcsoftware.aimodel.domain;


import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import org.hibernate.annotations.GenericGenerator;
import javax.persistence.Id;
import javax.persistence.Table;
import com.fasterxml.jackson.annotation.JsonView;
import org.hibernate.validator.constraints.Length;
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
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="t_ai_model_component")
@ApiModel(value = "TAiModelComponent", description = "系统组件库")
public class TAiModelComponent extends AbstractBaseDomain {
	
	@Id
	@GeneratedValue(generator = "paymentableGenerator")
    @GenericGenerator(name = "paymentableGenerator", strategy = "uuid")
	@Column(name="component_id")
	@ApiModelProperty(value = "组件id")
	@Length(max=32,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	componentId;
	
	@Column(name="component_name")
	@ApiModelProperty(value = "组件名称")
	@Length(max=128,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	componentName;
	
	@Column(name="place_holder")
	@ApiModelProperty(value = "智能体占位符")
	@Length(max=200,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	placeHolder;
	
	@Column(name="sys_prompt_id")
	@ApiModelProperty(value = "智能体id")
	@Length(max=32,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	sysPromptId;

	@Column(name="component_status")
	@ApiModelProperty(value = "组件状态(默认1：发布  0：未发布）")
	@Length(max=11,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private Integer	componentStatus;
	
	@Column(name="prompt_init_data")
	@ApiModelProperty(value = "智能体数据集")
	@Length(max=1000,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	promptInitData;
	
	@Column(name="client_types")
	@ApiModelProperty(value = "支持客户端类型（pc,app）")
	@Length(max=128,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	clientTypes;
	
	@Column(name="component_des")
	@ApiModelProperty(value = "组件说明")
	@Length(max=128,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	componentDes;
	
}
