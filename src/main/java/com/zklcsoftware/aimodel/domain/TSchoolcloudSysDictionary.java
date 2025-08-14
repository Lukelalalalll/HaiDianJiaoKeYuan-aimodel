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
@Table(name="t_schoolcloud_sys_dictionary")
@ApiModel(value = "TSchoolcloudSysDictionary", description = "字典信息表")
public class TSchoolcloudSysDictionary extends AbstractBaseDomain {
	
	@Id
	@GeneratedValue
	@Column(name="id")
	@ApiModelProperty(value = "ID")
	@Length(max=11,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private Integer	id;
	
	@Column(name="pid")
	@ApiModelProperty(value = "PID")
	@Length(max=11,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private Integer	pid;
	
	@Column(name="dictionaryname")
	@ApiModelProperty(value = "值")
	@Length(max=32,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	dictionaryname;

	@Column(name="is_del")
	@ApiModelProperty(value = "删除标记 0正常 1删除")
	@Length(max=11,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private Integer	isDel;

	@Column(name="order_no")
	@ApiModelProperty(value = "排序字段(越小越靠前)")
	@Length(max=11,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private Integer	orderNo;
	
}
