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
@Table(name="t_ai_game_tpl")
@ApiModel(value = "TAiGameTpl", description = "ai小游戏模板")
public class TAiGameTpl extends AbstractBaseDomain {
	
	@Id
	@GeneratedValue(generator = "paymentableGenerator")
    @GenericGenerator(name = "paymentableGenerator", strategy = "uuid")
	@Column(name="id")
	@ApiModelProperty(value = "主键")
	@Length(max=32,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	id;
	
	@Column(name="name")
	@ApiModelProperty(value = "游戏名")
	@Length(max=128,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	name;
	
	@Column(name="tpl_content")
	@ApiModelProperty(value = "模板内容")
	@JsonView(WithoutView.class)
	private String	tplContent;
	
	@Column(name="sys_prompt_id")
	@ApiModelProperty(value = "关联智能体ID")
	@Length(max=32,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	sysPromptId;
	
	@Column(name="tools_sys_prompt_id")
	@ApiModelProperty(value = "生成数据工具智能体")
	@Length(max=32,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	toolsSysPromptId;
	
	@Column(name="is_del")
	@ApiModelProperty(value = "删除标识(0正常  1删除)")
	@Length(max=1,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private Integer	isDel;
	
	@Column(name="create_time")
	@ApiModelProperty(value = "创建时间")
	@JsonView(WithoutView.class)
	private Date	createTime;
	
	@Column(name="create_user")
	@ApiModelProperty(value = "创建用户")
	@Length(max=32,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	createUser;
	
	@Column(name="update_time")
	@ApiModelProperty(value = "更新时间")
	@JsonView(WithoutView.class)
	private Date	updateTime;
	
	@Column(name="update_user")
	@ApiModelProperty(value = "更新用户")
	@Length(max=32,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	updateUser;
	
}
