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
@Table(name="t_schoolcloud_sys_conf")
@ApiModel(value = "TSchoolcloudSysConf", description = "云平台配置表")
public class TSchoolcloudSysConf extends AbstractBaseDomain {
	
	@Id
	@GeneratedValue(generator = "paymentableGenerator")
    @GenericGenerator(name = "paymentableGenerator", strategy = "uuid")
	@Column(name="conf_key")
	@ApiModelProperty(value = "配置key")
	@Length(max=32,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	confKey;
	
	@Column(name="conf_value")
	@ApiModelProperty(value = "配置value")
	@Length(max=2048,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	confValue;
	
	@Column(name="conf_name")
	@ApiModelProperty(value = "配置名")
	@Length(max=128,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	confName;
	
}
