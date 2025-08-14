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
@Table(name="t_ai_user_session_model_context")
@ApiModel(value = "TAiUserSessionModelContext", description = "用户提问会话模型对应上下文")
public class TAiUserSessionModelContext extends AbstractBaseDomain {
	
	@Id
	@GeneratedValue(generator = "paymentableGenerator")
    @GenericGenerator(name = "paymentableGenerator", strategy = "uuid")
	@Column(name="id")
	@Length(max=32,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	id;
	
	@Column(name="session_model_id")
	@ApiModelProperty(value = "会话标识")
	@Length(max=32,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	sessionModelId;
	
	@Column(name="order_no")
	@ApiModelProperty(value = "排序号")
	@Length(max=11,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private Integer	orderNo;
	
	@Column(name="user_in")
	@ApiModelProperty(value = "用户提问内容")
	@JsonView(WithoutView.class)
	private String	userIn;

	@Column(name="user_in_prompt_format")
	@ApiModelProperty(value = "根据user提示词优化后提交个大模型的内容")
	@JsonView(WithoutView.class)
	private String	userInPromptFormat;
	
	@Column(name="assistant_out")
	@ApiModelProperty(value = "模型回复内容")
	@JsonView(WithoutView.class)
	private String	assistantOut;

	@Column(name="ext_json")
	@ApiModelProperty(value = "问答扩展信息  用于存储第三方工具返回信息  展示历史时使用")
	@JsonView(WithoutView.class)
	private String	extJson;
	
	@Column(name="create_time")
	@ApiModelProperty(value = "创建时间")
	@JsonView(WithoutView.class)
	private Date	createTime;
	
	@Column(name="user_prompt_id")
	@ApiModelProperty(value = "提示词ID")
	@Length(max=32,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	userPromptId;
	
}
