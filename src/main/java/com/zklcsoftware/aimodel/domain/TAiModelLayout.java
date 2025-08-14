package com.zklcsoftware.aimodel.domain;

import java.util.Date;

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
@Table(name="t_ai_model_layout")
@ApiModel(value = "TAiModelLayout", description = "AI组件布局")
public class TAiModelLayout extends AbstractBaseDomain {
	
	@Id
	@GeneratedValue(generator = "paymentableGenerator")
    @GenericGenerator(name = "paymentableGenerator", strategy = "uuid")
	@Column(name="layout_id")
	@ApiModelProperty(value = "布局id")
	@Length(max=32,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	layoutId;
	
	@Column(name="layout_name")
	@ApiModelProperty(value = "名称")
	@Length(max=32,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	layoutName;
	
	@Column(name="layout_des")
	@ApiModelProperty(value = "描述")
	@Length(max=100,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	layoutDes;
	
	@Column(name="layout_sort")
	@ApiModelProperty(value = "排序号")
	@Length(max=11,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private Integer	layoutSort;
	
	@Column(name="layout_icon")
	@ApiModelProperty(value = "布局图标")
	@Length(max=100,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	layoutIcon;
	
	@Column(name="layout_status")
	@ApiModelProperty(value = "状态(1：有效，0“：无效）")
	@Length(max=11,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private Integer	layoutStatus;
	
	@Column(name="create_user_id")
	@ApiModelProperty(value = "创建人编号")
	@Length(max=32,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	createUserId;
	
	@Column(name="create_user_name")
	@ApiModelProperty(value = "操作员名称")
	@Length(max=20,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	createUserName;
	
	@Column(name="create_date")
	@ApiModelProperty(value = "添加日期")
	@JsonView(WithoutView.class)
	private Date	createDate;
	
	@Column(name="is_valid")
	@ApiModelProperty(value = "有效性")
	@Length(max=11,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private Integer	isValid;
	
	@Column(name="update_user_id")
	@ApiModelProperty(value = "更新人编号")
	@Length(max=32,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	updateUserId;
	
	@Column(name="update_user_name")
	@ApiModelProperty(value = "更新人姓名")
	@Length(max=20,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	updateUserName;
	
	@Column(name="update_date")
	@ApiModelProperty(value = "更新日期")
	@JsonView(WithoutView.class)
	private Date	updateDate;
	
	@Column(name="user_type")
	@ApiModelProperty(value = "用户类型(教师，学生，家长)")
	@Length(max=100,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	userType;
	
}
