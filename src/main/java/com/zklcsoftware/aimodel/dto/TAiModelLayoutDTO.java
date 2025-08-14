package com.zklcsoftware.aimodel.dto;

import java.util.List;

import com.zklcsoftware.basic.model.AbstractBaseDomain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@SuppressWarnings("serial")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel(value = "TAiModelLayout", description = "用户布局dto")
public class TAiModelLayoutDTO extends AbstractBaseDomain {
	

	@ApiModelProperty(value = "布局id")
	private String	layoutId;
	
	@ApiModelProperty(value = "名称")
	private String	layoutName;
	
	@ApiModelProperty(value = "描述")
	private String	layoutDes;
	
	@ApiModelProperty(value = "排序号")
	private Integer	layoutSort;
	
	@ApiModelProperty(value = "布局图标")
	private String	layoutIcon;
	
	@ApiModelProperty(value = "状态(1：启用，0：禁用)")
	private Integer	layoutStatus;
	
	@ApiModelProperty(value = "用户类型(教师，学生，家长)")
	private String	userType;
	
	@ApiModelProperty(value = "智能体集合")
	private List<TAiModelLayoutItemDTO> itemList;
	
}
