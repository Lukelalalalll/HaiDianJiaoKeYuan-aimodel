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
@Table(name="t_ai_user_session")
@ApiModel(value = "TAiUserSession", description = "用户提问会话")
public class TAiUserSession extends AbstractBaseDomain {
	
	@Id
	@GeneratedValue(generator = "paymentableGenerator")
    @GenericGenerator(name = "paymentableGenerator", strategy = "uuid")
	@Column(name="id")
	@Length(max=32,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	id;
	
	@Column(name="name")
	@ApiModelProperty(value = "会话名称")
	@Length(max=128,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	name;
	
	@Column(name="user_id")
	@ApiModelProperty(value = "用户标识")
	@Length(max=128,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	userId;
	
	@Column(name="user_type")
	@ApiModelProperty(value = "用户类型")
	@Length(max=11,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private Integer	userType;
	
	@Column(name="create_time")
	@ApiModelProperty(value = "创建时间")
	@JsonView(WithoutView.class)
	private Date	createTime;

	@Column(name="update_time")
	@ApiModelProperty(value = "更新时间")
	@JsonView(WithoutView.class)
	private Date	updateTime;
	
	@Column(name="sys_prompt_id")
	@ApiModelProperty(value = "智能体ID(默认为空表示对话对比功能的会话)")
	@Length(max=32,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	sysPromptId;

	@Column(name="user_prompt_id")
	@ApiModelProperty(value = "提示词(默认首个对话选用的提示词  可选)")
	@Length(max=32,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	userPromptId;

	@Column(name="is_del")
	@ApiModelProperty(value = "删除标识(0正常  1删除)")
	@Length(max=11,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private Integer	isDel;

	@Column(name="user_name")
	@ApiModelProperty(value = "用户名")
	@Length(max=128,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	userName;

	@Column(name="ch_name")
	@ApiModelProperty(value = "用户姓名")
	@Length(max=128,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	chName;

	@Column(name="ref_session_id")
	@ApiModelProperty(value = "关联会话标识")
	@Length(max=32,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	refSessionId;

	@Column(name="student_id")
	@ApiModelProperty(value = "用户标识")
	@Length(max=128,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	studentId;
	
}
