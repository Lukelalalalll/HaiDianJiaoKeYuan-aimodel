package com.zklcsoftware.common.dto;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import com.zklcsoftware.common.web.util.ConstantUtil;
import lombok.Data;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.beanutils.BeanUtils;

@Data
@SuppressWarnings("serial")
@ApiModel(value="API响应对象")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OperaResult implements Serializable {

	@ApiModelProperty(value="结果码   0-成功 -1-失败")
	private Integer resultCode = 0;
	
	@ApiModelProperty(value="结果描述(消息框的内容)")
	private String resultDesc;
	
	@ApiModelProperty(value="封装的对象")
	private Map<String,Object> data = new HashMap<String,Object>();

	public static OperaResult putSuccessResult(String key, Object value){
		OperaResult operaResult= new OperaResult();
		operaResult.getData().put(key, value);
		return operaResult;
	}

	public static OperaResult putFailResult(Map<String,Object> data){
		OperaResult operaResult= new OperaResult();
		operaResult.setResultCode(ConstantUtil.OPT_RESULT_CODE_FAIL);
		operaResult.setData(data);
		return operaResult;
	}

	public static OperaResult putFailResult(String key, Object value){
		OperaResult operaResult= new OperaResult();
		operaResult.setResultCode(ConstantUtil.OPT_RESULT_CODE_FAIL);
		operaResult.getData().put(key, value);
		return operaResult;
	}
}
