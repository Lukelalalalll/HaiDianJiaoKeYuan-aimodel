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
@Table(name="t_ai_attachment")
@ApiModel(value = "TAiAttachment", description = "ai附件表")
public class TAiAttachment extends AbstractBaseDomain {
	
	@Id
	@GeneratedValue(generator = "paymentableGenerator")
	@GenericGenerator(name = "paymentableGenerator", strategy = "uuid")
	@Column(name="attachment_id")
	@ApiModelProperty(value = "编号")
	@Length(max=32,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	attachmentId;
	
	@Column(name="attachment_type")
	@ApiModelProperty(value = "类型")
	@Length(max=50,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	attachmentType;
	
	@Column(name="attachment_size")
	@ApiModelProperty(value = "大小")
	@JsonView(WithoutView.class)
	private Double	attachmentSize;
	
	@Column(name="file_name")
	@ApiModelProperty(value = "文件名称")
	@Length(max=255,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	fileName;
	
	@Column(name="real_name")
	@ApiModelProperty(value = "真实文件名称")
	@Length(max=255,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	realName;
	
	@Column(name="content_type")
	@ApiModelProperty(value = "附件类型")
	@Length(max=255,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	contentType;
	
	@Column(name="znt_id")
	@ApiModelProperty(value = "智能体id")
	@Length(max=255,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	zntId;
	
	@Column(name="netdisk_id")
	@ApiModelProperty(value = "网盘id")
	@Length(max=255,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	netdiskId;

	@Column(name="zsk_slug")
	@ApiModelProperty(value = "知识库slug")
	@Length(max=255,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	zskSlug;

	@Column(name="zsk_location")
	@ApiModelProperty(value = "知识库文件location")
	@Length(max=255,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	zskLocation;

	@Column(name="zsk_url")
	@ApiModelProperty(value = "知识库Url")
	@Length(max=255,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	zskUrl;

	@Column(name="zsk_zt")
	@ApiModelProperty(value = "知识库状态(0:未学习 1:学习中 2:已学习)")
	@Length(max=11,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private Integer	zskZt;
	
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

	@Column(name="standby_zsk_location")
	@ApiModelProperty(value = "备用知识库文件location")
	@Length(max=255,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	standbyZskLocation;

	@Column(name="standby_zsk_url")
	@ApiModelProperty(value = "备用知识库Url")
	@Length(max=255,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	standbyZskUrl;

}
