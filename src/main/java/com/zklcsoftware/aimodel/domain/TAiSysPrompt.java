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
import java.util.Date;

@SuppressWarnings("serial")
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="t_ai_sys_prompt")
@ApiModel(value = "TAiSysPrompt", description = "自定义智能体")
public class TAiSysPrompt extends AbstractBaseDomain {
	
	@Id
	@GeneratedValue(generator = "paymentableGenerator")
    @GenericGenerator(name = "paymentableGenerator", strategy = "uuid")
	@Column(name="id")
	@Length(max=32,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	id;
	
	@Column(name="name")
	@ApiModelProperty(value = "智能体名称")
	@Length(max=128,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	name;
	
	@Column(name="notes")
	@ApiModelProperty(value = "智能体描述")
	@Length(max=512,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	notes;

	@Column(name="bus_type")
	@ApiModelProperty(value = "业务分类(字典)")
	@Length(max=11,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private Integer	busType;

	@Column(name="jssd")
	@ApiModelProperty(value = "角色设定")
	@Length(max=11,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private Integer	jssd;
	
	@Column(name="prompt_template")
	@ApiModelProperty(value = "模板内容")
	@JsonView(WithoutView.class)
	private String	promptTemplate;
	
	@Column(name="pro_img")
	@ApiModelProperty(value = "图片")
	@Length(max=512,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	proImg;
	
	@Column(name="model_id")
	@ApiModelProperty(value = "引用模型标识")
	@Length(max=32,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	modelId;
	
	@Column(name="publish_status")
	@ApiModelProperty(value = "发布状态(0未发布  1已发布  2已停用)")
	@Length(max=11,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private Integer	publishStatus=1;
	
	@Column(name="publish_range")
	@ApiModelProperty(value = "发布范围(0个人可见  1全校可见)")
	@Length(max=11,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private Integer	publishRange=1;

	@Column(name="review_status")
	@ApiModelProperty(value = "审核状态(0待审核 1审核通过 2审核不通过)")
	@Length(max=11,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private Integer	reviewStatus;

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

	@Column(name="order_no")
	@ApiModelProperty(value = "排序字段 越小越靠前")
	@Length(max=11,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private Integer	orderNo;

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

	@Column(name="prologue")
	@ApiModelProperty(value = "开场白")
	@JsonView(WithoutView.class)
	private String	prologue;

	@Column(name="bakreply")
	@ApiModelProperty(value = "兜底回复")
	@JsonView(WithoutView.class)
	private String	bakreply;

	@Column(name="user_prompt_template")
	@ApiModelProperty(value = "用户提示词模板")
	@JsonView(WithoutView.class)
	private String	userPromptTemplate;

	@Column(name="source_flag")
	@ApiModelProperty(value = "来源  1系统  2用户 3外部")
	@JsonView(WithoutView.class)
	private Integer	sourceFlag;

	@Column(name="custom_impl_class")
	@ApiModelProperty(value = "自定义实现类")
	@JsonView(WithoutView.class)
	private String	customImplClass;

	@Column(name="use_context")
	@ApiModelProperty(value = "是否开启上下文(0否 1是),工具类的智能体一般不设置支持")
	@JsonView(WithoutView.class)
	private Integer	useContext;

	@Column(name="use_context_maxcount")
	@ApiModelProperty(value = "可提交的上下文最大数量")
	@JsonView(WithoutView.class)
	private Integer	useContextMaxcount;

	@Column(name="use_question_optimize")
	@ApiModelProperty(value = "是否启用问题优化 0否  1是  默认否")
	@JsonView(WithoutView.class)
	private Integer	useQuestionOptimize;

	@Column(name="function_type")
	@ApiModelProperty(value = "功能类型")
	@JsonView(WithoutView.class)
	private String	functionType;

	@Column(name="sys_prompt_type")
	@ApiModelProperty(value = "类型 1-智能体应用  2-用户智能体 3-工具智能体")
	@JsonView(WithoutView.class)
	private Integer	sysPromptType;

	@Column(name="knowledge_id")
	@ApiModelProperty(value = "知识库ID")
	@JsonView(WithoutView.class)
	private String	knowledgeId;

	@Column(name="is_use_szr")
	@ApiModelProperty(value = "是否启用数字人播报(1是 0否  为空否)")
	@JsonView(WithoutView.class)
	private Integer	isUseSzr;

	@Column(name="extJson")
	@ApiModelProperty(value = "智能体个性化配置json形式")
	@JsonView(WithoutView.class)
	private String	extJson;

	@Column(name="agent_id")
	@ApiModelProperty(value = "第三方agentId")
	@JsonView(WithoutView.class)
	private String	agentId;

	@Column(name="is_use_tools")
	@ApiModelProperty(value = "是否启用工具  1是 0否 默认否")
	@JsonView(WithoutView.class)
	private Integer	isUseTools;
}
