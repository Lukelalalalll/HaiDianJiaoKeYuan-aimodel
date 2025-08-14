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
@Table(name="t_ai_sensitive_words")
@ApiModel(value = "TAiSensitiveWords", description = "AI提问敏感词")
public class TAiSensitiveWords extends AbstractBaseDomain {
	
	@Id
	@GeneratedValue
	@Column(name="id")
	@Length(max=11,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private Integer	id;
	
	@Column(name="words")
	@Length(max=128,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	words;
	
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
