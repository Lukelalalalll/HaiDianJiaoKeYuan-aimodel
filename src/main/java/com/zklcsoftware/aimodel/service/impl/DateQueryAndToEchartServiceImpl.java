package com.zklcsoftware.aimodel.service.impl;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.zklcsoftware.aimodel.domain.TAiSysPrompt;
import com.zklcsoftware.aimodel.repository.TAiSysPromptRepository;
import com.zklcsoftware.aimodel.service.DateQueryAndToEchartService;
import com.zklcsoftware.aimodel.service.TAiSysPromptService;
import com.zklcsoftware.aimodel.websocket.dto.AiOutMsgDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 赵成刚
 * @ClassName DateQueryAndToEchartServiceImpl.java
 * @Description 数据查询并生成报表
 * @createTime 2025-01-12 11:30
 **/
@Service("DateQueryAndToEchartServiceImpl")
@Slf4j
public class DateQueryAndToEchartServiceImpl implements DateQueryAndToEchartService {

    @Autowired TAiSysPromptService tAiSysPromptService;
    @Autowired TAiModelServiceImpl tAiModelService;
    //@Autowired DateQueryAndToEchartRepository echartRepository;
    @Autowired TAiSysPromptRepository sysPromptRepository;
    
    
    /*
    @Value("${uploadfiledir.uploadFilePath}")
    private String uploadFilePath;//服务器路径
*/
    @Override
    public AiOutMsgDTO DateQueryAndToEchart(String zntId, String content,String userId) {
    		//, HttpServletRequest request1, HttpServletResponse response) {

        AiOutMsgDTO aiOutMsgDTO = new AiOutMsgDTO();
        //查询智能体信息
        TAiSysPrompt prompt = tAiSysPromptService.findById(zntId);
        //用户提示词模板
        String userPromptTemplate = prompt.getUserPromptTemplate();
        
        //测试ddl
        String ddlTestStr =""
        		/*
        		+"CREATE TABLE `js_jichuxinxi` ("
        		+"`id` int NOT NULL AUTO_INCREMENT COMMENT 'id',"
        		+"`js_xm` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '教师姓名',"
        		+"`js_no` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '教师编号',"
        		+"`js_cjgzny` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '教师参加工作时间(2022-01)',"
        		+"`js_mz` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '教师民族(汉族、满族、回族等)',"
        		+"`js_zzmm` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '教师政治面貌(中共党员、群众、共青团员等)',"
        		+"`js_xb` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '教师性别(男、女)',"
        		+"`xx_mc` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '教师所在学校名称',"
        		+"PRIMARY KEY (`id`) USING BTREE"
        		+") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb3 COMMENT='教师基础信息表';"
        		*/
        		//新+ hd_xueshengxinxi
        		+"CREATE TABLE `hd_xueshengxinxi` ("
        		+"`id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',"
        		+"`student_global_id` varchar(32) NOT NULL COMMENT '学生标识号',"
        		+"`student_no` varchar(32) DEFAULT NULL COMMENT '学生编号',"
        		+"`student_name` varchar(100) DEFAULT NULL COMMENT '学生姓名',"
        		+"`input_time` datetime DEFAULT NULL COMMENT '数据录入时间(2022-01-01 11:11:11)',"
        		+"`update_time` datetime DEFAULT NULL COMMENT '数据更新时间(2022-01-01 11:11:11)',"
        		+"`del_flag` int(11) DEFAULT NULL COMMENT '删除标记(1:启用 0:作废)',"
        		+"`class_global_id` varchar(32) DEFAULT NULL COMMENT '学生所在班级标识号',"
        		+"`class_name` varchar(20) DEFAULT NULL COMMENT '学生所在班级名称',"
        		+"`grade_global_id` varchar(32) DEFAULT NULL COMMENT '学生所在年级标识号',"
        		+"`grade_name` varchar(20) DEFAULT NULL COMMENT '学生所在年级名称',"
        		+"`grade_enrollment_date` date DEFAULT NULL COMMENT '学生入学时间(2022-01)',"
        		+"`grade_graduation_date` date DEFAULT NULL COMMENT '学生毕业时间(2022-01)',"
        		+"`faculty_global_id` varchar(32) DEFAULT NULL COMMENT '学生所在学段标识号',"
        		+"`faculty_name` varchar(64) DEFAULT NULL COMMENT '学生所在学段名称',"
        		+"`faculty_time` int(11) DEFAULT NULL COMMENT '学生所在学段长度',"
        		+"`district_global_id` varchar(32) DEFAULT NULL COMMENT '学生所在校区标识号',"
        		+"`district_name` varchar(32) DEFAULT NULL COMMENT '学生所在校区名称',"
        		+"`school_global_id` varchar(32) DEFAULT NULL COMMENT '学生所在学校标识号',"
        		+"`school_name` varchar(50) DEFAULT NULL COMMENT '学生所在学校名称',"
        		+"PRIMARY KEY (`id`),"
        		+"KEY `datastore_collect_30_index_school_global_id` (`school_global_id`) USING BTREE"
        		+") ENGINE=InnoDB AUTO_INCREMENT=393211 DEFAULT CHARSET=utf8 COMMENT='学生信息表';"
		//新+ hd_xueshengxinxi
        		+"CREATE TABLE `hd_jiaoshixinxi` ("
        		+"`id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',"
        		+"`teacher_global_id` varchar(32) NOT NULL COMMENT '教师标识号',"
        		+"`teacher_no` varchar(32) DEFAULT NULL COMMENT '教师编号',"
        		+"`teacher_name` varchar(100) DEFAULT NULL COMMENT '教师姓名',"
        		+"`input_time` datetime DEFAULT NULL COMMENT '数据录入时间(2022-01-01 11:11:11)',"
        		+"`update_time` datetime DEFAULT NULL COMMENT '数据更新时间(2022-01-01 11:11:11)',"
        		+"`del_flag` int(11) DEFAULT NULL COMMENT '删除标记(1:启用 0:作废)',"
        		+"`district_global_id` varchar(32) DEFAULT NULL COMMENT '教师所在校区标识号',"
        		+"`district_name` varchar(32) DEFAULT NULL COMMENT '教师所在校区名称',"
        		+"`school_global_id` varchar(32) DEFAULT NULL COMMENT '教师所在学校标识号',"
        		+"`school_name` varchar(50) DEFAULT NULL COMMENT '教师所在学校名称',"
        		+"PRIMARY KEY (`id`),"
        		+"KEY `datastore_collect_31_index_school_global_id` (`school_global_id`) USING BTREE"
        		+") ENGINE=InnoDB AUTO_INCREMENT=65536 DEFAULT CHARSET=utf8 COMMENT='教师信息表';";
        
        try {
            //将提示词中的内容替换
            //String promptContent = userPromptTemplate
            //        .replace("${ddl_commont}", content == null ? "" : ddlTestStr)
            //        .replace("${text}", content == null ? "" : content);
            //直调文档回答接口
            AiOutMsgDTO answerApi = tAiModelService.answerApi(zntId, "{\"ddl_commont\": \""+ddlTestStr+"\",\"text\": \""+content+"\"}",userId);
            Object data = answerApi.getData();
            JsonObject jsonObject = new Gson().fromJson(data.toString(), JsonObject.class);
            String sqlStr = jsonObject.get("sql").toString();
            //去掉字符串前后的 引号  ”
            sqlStr = sqlStr.substring(1, sqlStr.length()-1);
            List<Object[]> list = sysPromptRepository.findBySql(sqlStr, null);
            //将slq的执行结果输出


            //xAxisData  样例：   data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
            JsonArray xlist = new JsonArray();
            //yAxisData  样例：  data: [120, 200, 150, 80, 70, 110, 130],
            JsonArray ylist = new JsonArray();
            
            /**
             * 饼图 对象
            data: [
                   { value: 1048, name: 'Search Engine' },
                   { value: 735, name: 'Direct' },
                   { value: 580, name: 'Email' },
                   { value: 484, name: 'Union Ads' },
                   { value: 300, name: 'Video Ads' }
                 ]
             */

            //渲染饼图数据
            List<Map<String, Object>> dataPie = new ArrayList<>();
            
            for(int i =0;i<list.size();i++){
            	xlist.add((String)list.get(i)[0]);
            	ylist.add((BigInteger)list.get(i)[1]);
            	//ylist.add(100);
            	//柱状图、折线图 --- end
            	
            	//下面的组装饼图数据
                Map<String, Object> item1 = new HashMap<>();
                item1.put("value", list.get(i)[1]);
                item1.put("name", list.get(i)[0]);
                dataPie.add(item1);
            }
            
            JsonObject jsonObjectChart = new JsonObject();
            jsonObjectChart.addProperty("chartType", "基础柱状图");

            //赋值到原始对象--柱状图、折线图
            jsonObjectChart.add("xAxisData", xlist);
            jsonObjectChart.add("yAxisData",  ylist);
            		
            //赋值到原始对象--饼图
            /*
            Gson gson = new Gson();
            String listJson = gson.toJson(dataPie);
            */
            jsonObjectChart.addProperty("pieData",dataPie.toString());
            /*
                              注意：显示目前不支持title.text、series.name 显示；
            option = {
			  title: {
			    //text: 'Referer of a Website',  《《注释掉
			    left: 'center'
			  },
			  series: [
			    {
			      //name: 'Access From', 《《注释掉
			      type: 'pie',
			      radius: '50%',
			      
      		//下是json内容，取 pieData 内容
            	{
            	"code": 0,
            	"sql": "SELECT js_xb AS '教师性别', COUNT(*) AS '人数', COUNT(*) / (SELECT COUNT(*) FROM js_jichuxinxi) AS '比例' FROM js_jichuxinxi GROUP BY js_xb",
            	"chart": "{"chartType":"基础柱状图","xAxisData":["女","男"],"yAxisData":[9,1],"pieData":"[{name=女, value=9}, {name=男, value=1}]"}",
            	"summary": "",
            	"metadata": "该SQL语句用于从教师基础信息表中查询出不同性别的教师人数及所占比例，以便直观了解教师男女比例情况。"
            }
            */
            
            jsonObject.addProperty("chart", jsonObjectChart.toString());
            jsonObjectChart.addProperty("chart_show", "Y");
            
            aiOutMsgDTO.setResponseType(2);
            aiOutMsgDTO.setDataType("json");
            aiOutMsgDTO.setDone(false);
            aiOutMsgDTO.setData(jsonObject.toString());

        }catch (Exception ex){
            ex.printStackTrace();
        }
        return aiOutMsgDTO;
    }
    //old
/*
    @Override
    public AiOutMsgDTO DateQueryAndToEchart(String zntId, String content,String userId) {
    		//, HttpServletRequest request1, HttpServletResponse response) {

        AiOutMsgDTO aiOutMsgDTO = new AiOutMsgDTO();
        //查询智能体信息
        TAiSysPrompt prompt = tAiSysPromptService.findById(zntId);
        //用户提示词模板
        String userPromptTemplate = prompt.getUserPromptTemplate();
        
        //测试ddl
        String ddlTestStr ="CREATE TABLE `js_jichuxinxi` ("
        		+"`id` int NOT NULL AUTO_INCREMENT COMMENT 'id',"
        		+"`js_xm` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '教师姓名',"
        		+"`js_no` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '教师编号',"
        		+"`js_cjgzny` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '教师参加工作时间(2022-01)',"
        		+"`js_mz` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '教师民族(汉族、满族、回族等)',"
        		+"`js_zzmm` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '教师政治面貌(中共党员、群众、共青团员等)',"
        		+"`js_xb` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '教师性别(男、女)',"
        		+"`xx_mc` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '教师所在学校名称',"
        		+"PRIMARY KEY (`id`) USING BTREE"
        		+") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb3 COMMENT='教师基础信息表';";

        try {
            //将提示词中的内容替换
            //String promptContent = userPromptTemplate
            //        .replace("${ddl_commont}", content == null ? "" : ddlTestStr)
            //        .replace("${text}", content == null ? "" : content);
            //直调文档回答接口
            AiOutMsgDTO answerApi = tAiModelService.answerApi(zntId, "{\"ddl_commont\": \""+ddlTestStr+"\",\"text\": \""+content+"\"}",userId);
            Object data = answerApi.getData();
            JsonObject jsonObject = new Gson().fromJson(data.toString(), JsonObject.class);
            String sqlStr = jsonObject.get("sql").toString();
            //去掉字符串前后的 引号  ”
            sqlStr = sqlStr.substring(1, sqlStr.length()-1);
            List<Object[]> list = sysPromptRepository.findBySql(sqlStr, null);

            //xAxisData  样例：   data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
            JsonArray xlist = new JsonArray();
            //yAxisData  样例：  data: [120, 200, 150, 80, 70, 110, 130],
            JsonArray ylist = new JsonArray();
            
            *//**
             * 饼图 对象
            data: [
                   { value: 1048, name: 'Search Engine' },
                   { value: 735, name: 'Direct' },
                   { value: 580, name: 'Email' },
                   { value: 484, name: 'Union Ads' },
                   { value: 300, name: 'Video Ads' }
                 ]
             *//*

            //渲染饼图数据
            List<Map<String, Object>> dataPie = new ArrayList<>();
            
            for(int i =0;i<list.size();i++){
            	xlist.add((String)list.get(i)[0]);
            	ylist.add((BigInteger)list.get(i)[1]);
            	//ylist.add(100);
            	//柱状图、折线图 --- end
            	
            	//下面的组装饼图数据
                Map<String, Object> item1 = new HashMap<>();
                item1.put("value", list.get(i)[1]);
                item1.put("name", list.get(i)[0]);
                dataPie.add(item1);
            }
            
            JsonObject jsonObjectChart = new JsonObject();
            jsonObjectChart.addProperty("chartType", "基础柱状图");

            //赋值到原始对象--柱状图、折线图
            jsonObjectChart.add("xAxisData", xlist);
            jsonObjectChart.add("yAxisData",  ylist);
            		
            //赋值到原始对象--饼图
            
            Gson gson = new Gson();
            String listJson = gson.toJson(dataPie);
            
            jsonObjectChart.addProperty("pieData",dataPie.toString());
            
                              注意：显示目前不支持title.text、series.name 显示；
            option = {
			  title: {
			    //text: 'Referer of a Website',  《《注释掉
			    left: 'center'
			  },
			  series: [
			    {
			      //name: 'Access From', 《《注释掉
			      type: 'pie',
			      radius: '50%',
			      
      		//下是json内容，取 pieData 内容
            	{
            	"code": 0,
            	"sql": "SELECT js_xb AS '教师性别', COUNT(*) AS '人数', COUNT(*) / (SELECT COUNT(*) FROM js_jichuxinxi) AS '比例' FROM js_jichuxinxi GROUP BY js_xb",
            	"chart": "{"chartType":"基础柱状图","xAxisData":["女","男"],"yAxisData":[9,1],"pieData":"[{name=女, value=9}, {name=男, value=1}]"}",
            	"summary": "",
            	"metadata": "该SQL语句用于从教师基础信息表中查询出不同性别的教师人数及所占比例，以便直观了解教师男女比例情况。"
            }
            
            
            jsonObject.addProperty("chart", jsonObjectChart.toString());
            jsonObjectChart.addProperty("chart_show", "Y");
            
            aiOutMsgDTO.setResponseType(2);
            aiOutMsgDTO.setDataType("json");
            aiOutMsgDTO.setDone(false);
            aiOutMsgDTO.setData(jsonObject.toString());

        }catch (Exception ex){
            ex.printStackTrace();
        }
        return aiOutMsgDTO;
    }
    */
}
