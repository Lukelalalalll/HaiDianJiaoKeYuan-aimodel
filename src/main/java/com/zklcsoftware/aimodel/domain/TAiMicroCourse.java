package com.zklcsoftware.aimodel.domain;

import java.util.Date;

import java.io.Serializable;
import java.util.List;
import javax.persistence.*;

import org.hibernate.annotations.GenericGenerator;
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
@Table(name="t_ai_micro_course")
@ApiModel(value = "TAiMicroCourse", description = "微课信息表")
public class TAiMicroCourse extends AbstractBaseDomain {
	
	@Id
	@GeneratedValue(generator = "paymentableGenerator")
    @GenericGenerator(name = "paymentableGenerator", strategy = "uuid")
	@Column(name="course_id")
	@ApiModelProperty(value = "主键id")
	@Length(max=32,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	courseId;
	
	@Column(name="course_name")
	@ApiModelProperty(value = "课程名称")
	@Length(max=50,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	courseName;
	
	@Column(name="video_url")
	@ApiModelProperty(value = "视频路径")
	@Length(max=128,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	videoUrl;

	@Column(name="video_zt")
	@ApiModelProperty(value = "视频状态(0:未提交，1:生成中，2:已完成，3:生成失败)")
	@Length(max=11,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private Integer	videoZt;

	@Column(name="des")
	@ApiModelProperty(value = "描述")
	@Length(max=255,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	des;
	
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

	@Transient
	private List<TAiShuzirenPpt> pptList;

	@Transient
	@ApiModelProperty(value = "视频查看路径")
	private String	videoUrlView;
	
}
