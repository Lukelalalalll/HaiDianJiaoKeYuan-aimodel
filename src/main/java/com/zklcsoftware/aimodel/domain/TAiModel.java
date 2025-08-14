package com.zklcsoftware.aimodel.domain;


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
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="t_ai_model")
@ApiModel(value = "TAiModel", description = "模型列表")
public class TAiModel extends AbstractBaseDomain {
	
	@Id
	@GeneratedValue(generator = "paymentableGenerator")
    @GenericGenerator(name = "paymentableGenerator", strategy = "uuid")
	@Column(name="id")
	@Length(max=32,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	id;
	
	@Column(name="name")
	@ApiModelProperty(value = "模型名称")
	@Length(max=128,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	name;
	
	@Column(name="appkey")
	@ApiModelProperty(value = "模型appkey")
	@Length(max=128,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	appkey;
	
	@Column(name="appsecret")
	@ApiModelProperty(value = "模型appsecret")
	@Length(max=128,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	appsecret;
	
	@Column(name="status")
	@ApiModelProperty(value = "0禁用  1启用")
	@Length(max=11,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private Integer	status;
	
	@Column(name="impl_class")
	@ApiModelProperty(value = "代码实现类")
	@Length(max=512,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	implClass;
	
	@Column(name="explain")
	@ApiModelProperty(value = "备注说明")
	@Length(max=512,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	explain;
	
	@Column(name="ext_arg_json")
	@ApiModelProperty(value = "扩展参数")
	@JsonView(WithoutView.class)
	private String	extArgJson;
	
	@Column(name="icon_logo")
	@ApiModelProperty(value = "logo")
	@Length(max=512,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	iconLogo;
	
	@Column(name="pro_img")
	@ApiModelProperty(value = "产品图片")
	@Length(max=512,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	proImg;

	@Column(name="is_del")
	@ApiModelProperty(value = "删除标识(0正常  1删除)")
	@Length(max=11,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private Integer	isDel;

	@Transient
	private String agentId;//第三方智能体ID

	@Transient
	private Boolean isIgnoreQuestion;//是否忽略原始问题   智能应用设置true(统一提交"按要求输出")   用户智能体设置成false(按用户输入内容提交)

	@Transient
	private Map<String,Object>  agentInputArgs=new HashMap<>();
	
}
