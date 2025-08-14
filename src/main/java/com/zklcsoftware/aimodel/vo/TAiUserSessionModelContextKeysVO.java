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
@ApiModel(value = "TAiUserSessionModelContextKeys", description = "用户提问会话中的关键词")
public class TAiUserSessionModelContextKeysVO extends AbstractBaseDomain {

	@ApiModelProperty(value = "关键词")
	private String	keyWord;
	@ApiModelProperty(value = "热度值")
	private Long questionNums;
	
}
