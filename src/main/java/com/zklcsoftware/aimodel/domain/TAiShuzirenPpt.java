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
@Table(name="t_ai_shuziren_ppt")
@ApiModel(value = "TAiShuzirenPpt", description = "数字人ppt表")
public class TAiShuzirenPpt extends AbstractBaseDomain {
	
	@Id
	@GeneratedValue(generator = "paymentableGenerator")
    @GenericGenerator(name = "paymentableGenerator", strategy = "uuid")
	@Column(name="id")
	@ApiModelProperty(value = "主键id")
	@Length(max=32,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	id;
	
	@Column(name="course_id")
	@ApiModelProperty(value = "微课id")
	@Length(max=32,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	courseId;

	@Column(name="xingxiang_id")
	@ApiModelProperty(value = "形象id")
	@Length(max=32,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	xingxiangId;

	@Column(name="ppt_url")
	@ApiModelProperty(value = "ppt路径")
	@Length(max=128,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	pptUrl;
	
	@Column(name="ppt_xh")
	@ApiModelProperty(value = "ppt序号")
	@Length(max=11,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private Integer	pptXh;

	@Column(name="ppt_img")
	@ApiModelProperty(value = "ppt图片")
	@Length(max=256,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	pptImg;

	@Column(name="content")
	@ApiModelProperty(value = "文本内容")
	@Length(max=500,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	content;
	
	@Column(name="szr_wz")
	@ApiModelProperty(value = "数字人位置")
	@Length(max=128,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	szrWz;

	@Column(name="szr_dx")
	@ApiModelProperty(value = "数字人缩放大小")
	@Length(max=128,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	szrDx;

	@Column(name="video_id")
	@ApiModelProperty(value = "视频id")
	@Length(max=32,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	videoId;
	
	@Column(name="video_url")
	@ApiModelProperty(value = "视频路径")
	@Length(max=128,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	videoUrl;

	@Column(name="video_proce_url")
	@ApiModelProperty(value = "视频处理路径")
	@Length(max=128,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	videoProceUrl;

	@Column(name="cover")
	@ApiModelProperty(value = "视频封面")
	@Length(max=128,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	cover;
	
	@Column(name="progress")
	@ApiModelProperty(value = "进度")
	@Length(max=11,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private Integer	progress;
	
	@Column(name="progress_dec")
	@ApiModelProperty(value = "进度描述")
	@Length(max=500,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	progressDec;
	
	@Column(name="server_ip")
	@ApiModelProperty(value = "服务器IP")
	@Length(max=128,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	serverIp;
	
	@Column(name="video_zt")
	@ApiModelProperty(value = "视频状态(1:生成中，2:已完成，3:失败)")
	@Length(max=11,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private Integer	videoZt;
	
	@Column(name="is_del")
	@ApiModelProperty(value = "删除标识(0正常  1删除)")
	@Length(max=11,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private Integer	isDel;
	
	@Column(name="create_user_id")
	@ApiModelProperty(value = "创建人编号")
	@Length(max=32,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	createUserId;
	
	@Column(name="create_user_name")
	@ApiModelProperty(value = "创建人名称")
	@Length(max=32,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	createUserName;
	
	@Column(name="create_date")
	@ApiModelProperty(value = "创建时间")
	@JsonView(WithoutView.class)
	private Date	createDate;

	@Column(name="update_user_id")
	@ApiModelProperty(value = "更新人编号")
	@Length(max=32,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	updateUserId;

	@Column(name="update_user_name")
	@ApiModelProperty(value = "更新人名称")
	@Length(max=32,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	updateUserName;

	@Column(name="update_date")
	@ApiModelProperty(value = "更新时间")
	@JsonView(WithoutView.class)
	private Date	updatedate;
	
}
