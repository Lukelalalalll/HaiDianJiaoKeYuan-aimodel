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
@Table(name="t_ai_shuziren")
@ApiModel(value = "TAiShuziren", description = "ai数字人管理表")
public class TAiShuziren extends AbstractBaseDomain {
	
	@Id
	@GeneratedValue(generator = "paymentableGenerator")
    @GenericGenerator(name = "paymentableGenerator", strategy = "uuid")
	@Column(name="id")
	@ApiModelProperty(value = "id")
	@Length(max=32,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	id;
	
	@Column(name="template_file")
	@ApiModelProperty(value = "模版源头文件")
	@Length(max=128,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	templateFile;

	@Column(name="xingxiang_id")
	@ApiModelProperty(value = "形象id")
	@Length(max=32,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	xingxiangId;

	@Column(name="template_file_video")
	@ApiModelProperty(value = "视频模版文件")
	@Length(max=128,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	templateFileVideo;
	
	@Column(name="template_file_audio")
	@ApiModelProperty(value = "音频模版文件")
	@Length(max=128,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	templateFileAudio;
	
	@Column(name="asr_format_audio_url")
	@Length(max=128,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	asrFormatAudioUrl;
	
	@Column(name="reference_audio_text")
	@Length(max=128,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	referenceAudioText;
	
	@Column(name="code")
	@Length(max=128,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	code;
	
	@Column(name="shuziren_file")
	@ApiModelProperty(value = "数字人url")
	@Length(max=128,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	shuzirenFile;
	
	@Column(name="zt")
	@ApiModelProperty(value = "状态")
	@Length(max=11,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private Integer	zt;
	
	@Column(name="create_user_id")
	@ApiModelProperty(value = "创建人编号")
	@Length(max=32,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	createUserId;
	
	@Column(name="create_user_name")
	@ApiModelProperty(value = "操作员名称")
	@Length(max=20,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	createUserName;
	
	@Column(name="create_date")
	@ApiModelProperty(value = "添加日期")
	@JsonView(WithoutView.class)
	private Date	createDate;
	
	@Column(name="is_valid")
	@ApiModelProperty(value = "有效性")
	@Length(max=11,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private Integer	isValid;

	@Column(name="content")
	@ApiModelProperty(value = "文本内容")
	@JsonView(WithoutView.class)
	private String	content;

	@Column(name="cover")
	@ApiModelProperty(value = "封面")
	@Length(max=128,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	cover;

	@Column(name="progress")
	@ApiModelProperty(value = "进度")
	@Length(max=11,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private Integer	progress;

	@Column(name="progressDec")
	@ApiModelProperty(value = "进度描述")
	@Length(max=128,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	progressDec;

	@Column(name="server_ip")
	@ApiModelProperty(value = "服务器IP")
	@Length(max=128,groups= {UpdateValid.class, AddValid.class})
	@JsonView(WithoutView.class)
	private String	serverIp;

}
