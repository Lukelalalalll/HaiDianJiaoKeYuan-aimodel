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
@Table(name="t_ai_sys_tools")
@ApiModel(value = "TAiSysTools", description = "工具集定义信息")
public class TAiSysTools extends AbstractBaseDomain {
	
	@Id
	@GeneratedValue(generator = "paymentableGenerator")
    @GenericGenerator(name = "paymentableGenerator", strategy = "uuid")
	@Column(name="id")
	@Length(max=32,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	id;
	
	@Column(name="name")
	@ApiModelProperty(value = "工具名称")
	@Length(max=128,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	name;
	
	@Column(name="notes")
	@ApiModelProperty(value = "显示描述")
	@JsonView(WithoutView.class)
	private String	notes;
	
	@Column(name="tool_description")
	@ApiModelProperty(value = "注册描述")
	@JsonView(WithoutView.class)
	private String	toolDescription;
	
	@Column(name="method_name")
	@ApiModelProperty(value = "方法名")
	@Length(max=512,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	methodName;
	
	@Column(name="server_url")
	@ApiModelProperty(value = "调用地址")
	@Length(max=512,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	serverUrl;

	@Column(name="respone_description")
	@ApiModelProperty(value = "接口响应内容说明")
	@Length(max=512,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	responeDescription;

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
	
	@Column(name="is_del")
	@ApiModelProperty(value = "删除标识(0正常  1删除)")
	@Length(max=1,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private Integer	isDel;
	
	@Column(name="ext_json")
	@ApiModelProperty(value = "个性化配置json形式")
	@JsonView(WithoutView.class)
	private String	extJson;
	
	@Column(name="tool_type")
	@ApiModelProperty(value = "工具类型1 function、2 mcpserver")
	@Length(max=11,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private Integer	toolsType;

	@Column(name="sys_prompt_id")
	@ApiModelProperty(value = "专属智能体ID,为空表示全局共享")
	@JsonView(WithoutView.class)
	private String	sysPromptId;
	
}
