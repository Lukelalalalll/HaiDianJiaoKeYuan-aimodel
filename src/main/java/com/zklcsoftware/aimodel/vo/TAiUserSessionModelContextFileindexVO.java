package com.zklcsoftware.aimodel.vo;

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
@ApiModel(value = "TAiUserSessionModelContextFileindexVO", description = "用户提问会话中的知识库文件索引")
public class TAiUserSessionModelContextFileindexVO extends AbstractBaseDomain {

	@ApiModelProperty(value = "文件索引id")
	private String	fileindexId;
	@ApiModelProperty(value = "上下文id")
	private String	sessionContextId;
	@ApiModelProperty(value = "网盘id")
	private String	netdiskId;
	@ApiModelProperty(value = "网盘路径")
	private String	netdiskUrl;
	@ApiModelProperty(value = "文件名称")
	private String fileName;
	@ApiModelProperty(value = "文件类型")
	private String fileType;

}
