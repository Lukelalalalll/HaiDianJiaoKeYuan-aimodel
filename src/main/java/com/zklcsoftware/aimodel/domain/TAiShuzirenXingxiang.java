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
@Table(name="t_ai_shuziren_xingxiang")
@ApiModel(value = "TAiShuzirenXingxiang", description = "数字人形象表")
public class TAiShuzirenXingxiang extends AbstractBaseDomain {
	
	@Id
	@GeneratedValue(generator = "paymentableGenerator")
    @GenericGenerator(name = "paymentableGenerator", strategy = "uuid")
	@Column(name="xingxiang_id")
	@ApiModelProperty(value = "主键id")
	@Length(max=32,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	xingxiangId;
	
	@Column(name="template_file")
	@ApiModelProperty(value = "模版源头文件")
	@Length(max=128,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	templateFile;
	
	@Column(name="video_id")
	@ApiModelProperty(value = "视频id")
	@Length(max=128,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	videoId;
	
	@Column(name="reference_audio")
	@ApiModelProperty(value = "音频文件")
	@Length(max=128,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	referenceAudio;
	
	@Column(name="reference_text")
	@ApiModelProperty(value = "音频文字")
	@Length(max=128,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	referenceText;
	
	@Column(name="silent_video")
	@ApiModelProperty(value = "静音视频")
	@Length(max=128,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	silentVideo;
	
	@Column(name="cover")
	@ApiModelProperty(value = "视频封面")
	@Length(max=128,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	cover;

	@Column(name="sfkt")
	@ApiModelProperty(value = "是否抠图")
	@Length(max=11,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private Integer	sfkt;

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
	@ApiModelProperty(value = "视频状态")
	@Length(max=11,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private Integer	videoZt;
	
	@Column(name="publish_range")
	@ApiModelProperty(value = "发布范围(1个人可见  ,0全校可见)")
	@Length(max=11,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private Integer	publishRange;
	
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
	
}
