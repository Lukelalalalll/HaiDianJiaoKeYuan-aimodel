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
@Table(name="t_ai_model_layout_item")
@ApiModel(value = "TAiModelLayoutItem", description = "AI组件布局组件集")
public class TAiModelLayoutItem extends AbstractBaseDomain {
	
	@Id
	@GeneratedValue(generator = "paymentableGenerator")
    @GenericGenerator(name = "paymentableGenerator", strategy = "uuid")
	@Column(name="id")
	@ApiModelProperty(value = "id")
	@Length(max=32,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	id;
	
	@Column(name="layout_id")
	@ApiModelProperty(value = "布局id")
	@Length(max=32,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	layoutId;
	
	@Column(name="item_sort")
	@ApiModelProperty(value = "排序号")
	@Length(max=11,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private Integer	itemSort;

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
	
	@Column(name="other_url")
	@ApiModelProperty(value = "第三方应用url")
	@Length(max=512,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	otherUrl;
	
}
