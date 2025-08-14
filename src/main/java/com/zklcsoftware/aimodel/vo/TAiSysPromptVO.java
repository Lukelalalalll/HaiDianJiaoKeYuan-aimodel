package com.zklcsoftware.aimodel.vo;

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
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel(value = "TAiSysPrompt", description = "自定义智能体")
public class TAiSysPromptVO extends AbstractBaseDomain {
	

	private String	id;

	@ApiModelProperty(value = "智能体名称")
	private String	name;

	@ApiModelProperty(value = "智能体描述")
	private String	notes;

	@ApiModelProperty(value = "业务分类(字典)")
	private Integer	busType;

	@ApiModelProperty(value = "角色设定")
	private Integer	jssd;

	@ApiModelProperty(value = "图片")
	private String	proImg;

	@ApiModelProperty(value = "引用模型标识")
	private String	modelId;

	@ApiModelProperty(value = "排序字段 越小越靠前")
	private Integer	orderNo;

	@ApiModelProperty(value = "热度值")
	private Long questionNums;

	@ApiModelProperty(value = "创建时间")
	private Date createTime;

	@ApiModelProperty(value = "发布状态(0未发布  1已发布  2已停用)")
	private String	publishStatus;

	@ApiModelProperty(value = "发布范围(0个人可见  1全校可见)")
	private String	publishRange;

	@ApiModelProperty(value = "审核状态(0待审核 1审核通过 2审核不通过)")
	private String	reviewStatus;

	@ApiModelProperty(value = "发布状态(0未发布  1已发布  2已停用)")
	private String	publishStatusName;

	@ApiModelProperty(value = "发布范围(0个人可见  1全校可见)")
	private String	publishRangeName;

	@ApiModelProperty(value = "审核状态(0待审核 1审核通过 2审核不通过)")
	private String	reviewStatusName;

	@ApiModelProperty(value = "创建人")
	private String	chName;

	@ApiModelProperty(value = "开场白")
	private String	prologue;

	@ApiModelProperty(value = "兜底回复")
	private String	bakreply;

	@ApiModelProperty(value = "appUrl")
	private String	appUrl;
	private Long twcs;//提问次数

	@ApiModelProperty(value = "是否启用数字人播报(1是 0否  为空否)")
	private Integer	isUseSzr;

}
