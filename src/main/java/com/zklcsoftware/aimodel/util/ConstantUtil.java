package com.zklcsoftware.aimodel.util;

import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import com.github.houbb.sensitive.word.support.allow.WordAllows;
import com.github.houbb.sensitive.word.support.deny.WordDenys;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConstantUtil {

	public final static String API_THRESHOLD_KEY="AT:U:";//用户回答API阈值前缀
	/**
	 * 是否启用-起用
	 */
	public final static Integer STATE_1 = 1;

	public static final Map<String, String> sysConfig=new HashMap<>();//系统配置信息

	public static List<String> warnWords=new ArrayList<>();//预警词列表

	public static final Map<String, String> contextIdMap=new HashMap<>();//记录会话ID最后一次上下文ID (用于停止回答时 记录保存到数据库中)

	/**
	 * 删除标识
	 */
	public final static Integer IS_DEL_1 = 1;//删除
	public final static Integer IS_DEL_0 = 0;//正常

	public final static Integer SESSION_TYPE_1=1;//常规模型对比会话

	public final static Integer SESSION_TYPE_2=2;//智能体会话-智能体使用人群查询

	public final static Integer SESSION_TYPE_3=3;//智能体会话-智能体创建人查询

	public final static Integer SESSION_TYPE_4=4;//智能体应用会话查询

	public final static Integer USER_TYPE_101002=101002;//教师类型

	public final static Integer USER_TYPE_101004=101004;//学生类型

	public final static Integer USER_TYPE_101003=101003;//家长类型

	public final static Integer USER_TYPE_101007=101007;//管理员类型

	public final static Integer REVIEW_STATUS_0=0;//待审核
	public final static Integer REVIEW_STATUS_1=1;//审核通过
	public final static Integer REVIEW_STATUS_2=2;//审核不通过

	public final static String ZNT_PASS_STATUS_0="0";//是否自动审核通过个人提交的智能体  1是 0否  默认是

	public final static Integer PUBLISH_STATUS_0=0;//未发布
	public final static Integer PUBLISH_STATUS_1=1;//已发布

	public final static Integer PUBLISH_RANGE_0=0;//个人
	public final static Integer PUBLISH_RANGE_1=1;//全校

	public final static Integer PUBLISH_STATUS_2=2;//已停用

	public final static Integer DICT_TYPE_410=410;//智能体分类
	public final static Integer DICT_TYPE_411=411;//问题分类

	public final static Integer CALL_TYPE_1=1;// 1流式调用  2直调  默认流式
	public final static Integer CALL_TYPE_2=2;// 1流式调用  2直调  默认流式

	public final static Integer SYS_PROMPT_TYPE_1=1;//1-智能体应用  2-用户智能体 3-工具智能体
	public final static Integer SYS_PROMPT_TYPE_2=2;//1-智能体应用  2-用户智能体 3-工具智能体
	public final static Integer SYS_PROMPT_TYPE_3=3;//1-智能体应用  2-用户智能体 3-工具智能体
	public final static Integer SYS_PROMPT_TYPE_4=4;//1-智能体应用  2-用户智能体 3-工具智能体

	public final static Integer DEFAULT_TOPN=5;//默认上下文条数

	public static SensitiveWordBs sensitiveWordBs=null;

	static {
		sensitiveWordBs=SensitiveWordBs.newInstance()
				.wordAllow(WordAllows.chains(WordAllows.defaults(), null))
				.wordDeny(WordDenys.chains(WordDenys.defaults(), null))
				.init();
	}

	public static Map<String,Integer> zyXdMap=new HashMap<>();
	public static Map<String,Integer> zyXkMap=new HashMap<>();
	static {
		zyXdMap.put("小学",1);
		zyXdMap.put("初中",20);
		zyXdMap.put("高中",30);

		zyXkMap.put("语文",1);
		zyXkMap.put("数学",2);
		zyXkMap.put("英语",3);
		zyXkMap.put("物理",4);
		zyXkMap.put("化学",5);
		zyXkMap.put("生物",6);
		zyXkMap.put("政治",7);
		zyXkMap.put("历史",8);
		zyXkMap.put("地理",9);
		zyXkMap.put("科学",16);
	}

	public static final Integer use_question_optimize_0=0;//不启用问题优化
	public static final Integer use_question_optimize_1=1;//启用问题优化

	public final static Integer USE_AI_TOOLS_1=1;//是否启用工具  1是 0否 默认否
}