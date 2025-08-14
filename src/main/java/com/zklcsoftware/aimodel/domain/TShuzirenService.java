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
@Table(name="t_shuziren_service")
@ApiModel(value = "TShuzirenService", description = "数字人服务")
public class TShuzirenService extends AbstractBaseDomain {
	
	@Id
	@GeneratedValue
	@Column(name="id")
	@ApiModelProperty(value = "id")
	@Length(max=11,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private Integer	id;
	
	@Column(name="service_address")
	@ApiModelProperty(value = "服务地址或ip")
	@Length(max=32,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	serviceAddress;
	
	@Column(name="service_quantity")
	@ApiModelProperty(value = "最大服务数")
	@Length(max=11,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private Integer	serviceQuantity;
	
	@Column(name="status")
	@ApiModelProperty(value = "状态(1启用，0禁用)")
	@Length(max=11,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private Integer	status;
	
}
