package com.zklcsoftware.aimodel.vo;

import com.fasterxml.jackson.annotation.JsonView;
import com.zklcsoftware.basic.model.AbstractBaseDomain;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import java.util.Date;

@SuppressWarnings("serial")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel(value = "TAiWarnWordsContextVO", description = "AI提问触发预警词提问信息")
public class TAiWarnWordsContextVO extends AbstractBaseDomain {
	

	private Integer	id;

	@ApiModelProperty(value = "用户名")
	private String	userName;

	@ApiModelProperty(value = "姓名")
	private String	chName;

	@ApiModelProperty(value = "手机号")
	private String	mobile;

	@ApiModelProperty(value = "触发预警词")
	private String	warnWords;

	@ApiModelProperty(value = "提问内容")
	private String	twnr;

	@ApiModelProperty(value = "提问时间")
	private String	twsj;
	
}
