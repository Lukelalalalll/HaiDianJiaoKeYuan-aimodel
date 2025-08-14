package com.zklcsoftware.aimodel.controller;

import com.zklcsoftware.common.web.ExtBaseController;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

/**
 * @author zhushaog
 * @version 1.0
 * @ClassName McpServerTestApi.java
 * @company zklcsoftware
 * @Description 测试使用的接口
 * @createTime 2025/06/10 16:10
 */
@Slf4j
@Controller
@Api(tags = "AI回答逻辑处理")
@RequestMapping(path = {"/mcptest/v1/", "/api/mcptest/v1/"})
public class McpServerTestApi  extends ExtBaseController {
    @PostMapping(value = {"/queryKbxx"})
    @ResponseBody
    public String queryKbxx(HttpServletResponse response, String userId){
        String result="<table width=\"792\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style='width:396.00pt;border-collapse:collapse;table-layout:fixed;'>\n" +
                "   <col width=\"99\" span=\"8\" style='width:49.50pt;'/>\n" +
                "   <tr height=\"29\" style='height:14.50pt;'>\n" +
                "    <td class=\"xl65\" height=\"29\" width=\"99\" style='height:14.50pt;width:49.50pt;' x:str>节次</td>\n" +
                "    <td class=\"xl65\" width=\"99\" style='width:49.50pt;' x:str>星期一</td>\n" +
                "    <td class=\"xl65\" width=\"99\" style='width:49.50pt;' x:str>星期二</td>\n" +
                "    <td class=\"xl65\" width=\"99\" style='width:49.50pt;' x:str>星期三</td>\n" +
                "    <td class=\"xl65\" width=\"99\" style='width:49.50pt;' x:str>星期四</td>\n" +
                "    <td class=\"xl65\" width=\"99\" style='width:49.50pt;' x:str>星期五</td>\n" +
                "    <td class=\"xl65\" width=\"99\" style='width:49.50pt;' x:str>星期六</td>\n" +
                "    <td class=\"xl65\" width=\"99\" style='width:49.50pt;' x:str>星期日</td>\n" +
                "   </tr>\n" +
                "   <tr height=\"28\" style='height:14.00pt;'>\n" +
                "    <td class=\"xl66\" height=\"84\" rowspan=\"3\" style='height:42.00pt;border-right:.5pt solid windowtext;border-bottom:.5pt solid windowtext;' x:num>1</td>\n" +
                "    <td class=\"xl67\" x:str>生物</td>\n" +
                "    <td class=\"xl67\" x:str>语文</td>\n" +
                "    <td class=\"xl67\" x:str>数学</td>\n" +
                "    <td class=\"xl67\" x:str>英语</td>\n" +
                "    <td class=\"xl67\" x:str>数学</td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "   </tr>\n" +
                "   <tr height=\"28\" style='height:14.00pt;'>\n" +
                "    <td class=\"xl67\" x:str>杨昳</td>\n" +
                "    <td class=\"xl67\" x:str>许迎迎</td>\n" +
                "    <td class=\"xl67\" x:str>李迎春</td>\n" +
                "    <td class=\"xl67\" x:str>陈俊玲</td>\n" +
                "    <td class=\"xl67\" x:str>李迎春</td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "   </tr>\n" +
                "   <tr height=\"28\" style='height:14.00pt;'>\n" +
                "    <td class=\"xl67\" x:str>NJ216 初<span style='display:none;'>一1班</span></td>\n" +
                "    <td class=\"xl67\" x:str>NJ216 初<span style='display:none;'>一1班</span></td>\n" +
                "    <td class=\"xl67\" x:str>NJ216 初<span style='display:none;'>一1班</span></td>\n" +
                "    <td class=\"xl67\" x:str>NJ216 初<span style='display:none;'>一1班</span></td>\n" +
                "    <td class=\"xl67\" x:str>NJ216 初<span style='display:none;'>一1班</span></td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "   </tr>\n" +
                "   <tr height=\"28\" style='height:14.00pt;'>\n" +
                "    <td class=\"xl66\" height=\"84\" rowspan=\"3\" style='height:42.00pt;border-right:.5pt solid windowtext;border-bottom:.5pt solid windowtext;' x:num>2</td>\n" +
                "    <td class=\"xl67\" x:str>数学</td>\n" +
                "    <td class=\"xl67\" x:str>语文</td>\n" +
                "    <td class=\"xl67\" x:str>英语</td>\n" +
                "    <td class=\"xl67\" x:str>语文</td>\n" +
                "    <td class=\"xl67\" x:str>英语</td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "   </tr>\n" +
                "   <tr height=\"28\" style='height:14.00pt;'>\n" +
                "    <td class=\"xl67\" x:str>李迎春</td>\n" +
                "    <td class=\"xl67\" x:str>许迎迎</td>\n" +
                "    <td class=\"xl67\" x:str>陈俊玲</td>\n" +
                "    <td class=\"xl67\" x:str>许迎迎</td>\n" +
                "    <td class=\"xl67\" x:str>陈俊玲</td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "   </tr>\n" +
                "   <tr height=\"28\" style='height:14.00pt;'>\n" +
                "    <td class=\"xl67\" x:str>NJ216 初<span style='display:none;'>一1班</span></td>\n" +
                "    <td class=\"xl67\" x:str>NJ216 初<span style='display:none;'>一1班</span></td>\n" +
                "    <td class=\"xl67\" x:str>NJ216 初<span style='display:none;'>一1班</span></td>\n" +
                "    <td class=\"xl67\" x:str>NJ216 初<span style='display:none;'>一1班</span></td>\n" +
                "    <td class=\"xl67\" x:str>NJ216 初<span style='display:none;'>一1班</span></td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "   </tr>\n" +
                "   <tr height=\"28\" style='height:14.00pt;'>\n" +
                "    <td class=\"xl66\" height=\"84\" rowspan=\"3\" style='height:42.00pt;border-right:.5pt solid windowtext;border-bottom:.5pt solid windowtext;' x:num>3</td>\n" +
                "    <td class=\"xl67\" x:str>信息技术</td>\n" +
                "    <td class=\"xl67\" x:str>英语</td>\n" +
                "    <td class=\"xl67\" x:str>体育</td>\n" +
                "    <td class=\"xl67\" x:str>体育</td>\n" +
                "    <td class=\"xl67\" x:str>语文</td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "   </tr>\n" +
                "   <tr height=\"28\" style='height:14.00pt;'>\n" +
                "    <td class=\"xl67\" x:str>周洪万</td>\n" +
                "    <td class=\"xl67\" x:str>陈俊玲</td>\n" +
                "    <td class=\"xl67\" x:str>孟雄</td>\n" +
                "    <td class=\"xl67\" x:str>景慧娟</td>\n" +
                "    <td class=\"xl67\" x:str>许迎迎</td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "   </tr>\n" +
                "   <tr height=\"28\" style='height:14.00pt;'>\n" +
                "    <td class=\"xl67\" x:str>NJ216 初<span style='display:none;'>一1班</span></td>\n" +
                "    <td class=\"xl67\" x:str>NJ216 初<span style='display:none;'>一1班</span></td>\n" +
                "    <td class=\"xl67\" x:str>NJ216 初<span style='display:none;'>一1班</span></td>\n" +
                "    <td class=\"xl67\" x:str>NJ216 初<span style='display:none;'>一1班</span></td>\n" +
                "    <td class=\"xl67\" x:str>NJ216 初<span style='display:none;'>一1班</span></td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "   </tr>\n" +
                "   <tr height=\"28\" style='height:14.00pt;'>\n" +
                "    <td class=\"xl66\" height=\"84\" rowspan=\"3\" style='height:42.00pt;border-right:.5pt solid windowtext;border-bottom:.5pt solid windowtext;' x:num>4</td>\n" +
                "    <td class=\"xl67\" x:str>体育</td>\n" +
                "    <td class=\"xl67\" x:str>英语</td>\n" +
                "    <td class=\"xl67\" x:str>思想政治</td>\n" +
                "    <td class=\"xl67\" x:str>数学</td>\n" +
                "    <td class=\"xl67\" x:str>思想政治</td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "   </tr>\n" +
                "   <tr height=\"28\" style='height:14.00pt;'>\n" +
                "    <td class=\"xl67\" x:str>景慧娟</td>\n" +
                "    <td class=\"xl67\" x:str>陈俊玲</td>\n" +
                "    <td class=\"xl67\" x:str>宋盈盈</td>\n" +
                "    <td class=\"xl67\" x:str>李迎春</td>\n" +
                "    <td class=\"xl67\" x:str>宋盈盈</td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "   </tr>\n" +
                "   <tr height=\"28\" style='height:14.00pt;'>\n" +
                "    <td class=\"xl67\" x:str>NJ216 初<span style='display:none;'>一1班</span></td>\n" +
                "    <td class=\"xl67\" x:str>NJ216 初<span style='display:none;'>一1班</span></td>\n" +
                "    <td class=\"xl67\" x:str>NJ216 初<span style='display:none;'>一1班</span></td>\n" +
                "    <td class=\"xl67\" x:str>NJ216 初<span style='display:none;'>一1班</span></td>\n" +
                "    <td class=\"xl67\" x:str>NJ216 初<span style='display:none;'>一1班</span></td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "   </tr>\n" +
                "   <tr height=\"28\" style='height:14.00pt;'>\n" +
                "    <td class=\"xl66\" height=\"84\" rowspan=\"3\" style='height:42.00pt;border-right:.5pt solid windowtext;border-bottom:.5pt solid windowtext;' x:num>5</td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "   </tr>\n" +
                "   <tr height=\"28\" style='height:14.00pt;'>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "   </tr>\n" +
                "   <tr height=\"28\" style='height:14.00pt;'>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "   </tr>\n" +
                "   <tr height=\"28\" style='height:14.00pt;'>\n" +
                "    <td class=\"xl66\" height=\"84\" rowspan=\"3\" style='height:42.00pt;border-right:.5pt solid windowtext;border-bottom:.5pt solid windowtext;' x:num>6</td>\n" +
                "    <td class=\"xl67\" x:str>英语</td>\n" +
                "    <td class=\"xl67\" x:str>体育</td>\n" +
                "    <td class=\"xl67\" x:str>语文</td>\n" +
                "    <td class=\"xl67\" x:str>班会</td>\n" +
                "    <td class=\"xl67\" x:str>美术</td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "   </tr>\n" +
                "   <tr height=\"28\" style='height:14.00pt;'>\n" +
                "    <td class=\"xl67\" x:str>陈俊玲</td>\n" +
                "    <td class=\"xl67\" x:str>景慧娟</td>\n" +
                "    <td class=\"xl67\" x:str>许迎迎</td>\n" +
                "    <td class=\"xl67\" x:str>马立军</td>\n" +
                "    <td class=\"xl67\" x:str>李惠燕</td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "   </tr>\n" +
                "   <tr height=\"28\" style='height:14.00pt;'>\n" +
                "    <td class=\"xl67\" x:str>NJ216 初<span style='display:none;'>一1班</span></td>\n" +
                "    <td class=\"xl67\" x:str>NJ216 初<span style='display:none;'>一1班</span></td>\n" +
                "    <td class=\"xl67\" x:str>NJ216 初<span style='display:none;'>一1班</span></td>\n" +
                "    <td class=\"xl67\" x:str>NJ216 初<span style='display:none;'>一1班</span></td>\n" +
                "    <td class=\"xl67\" x:str>NJ216 初<span style='display:none;'>一1班</span></td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "   </tr>\n" +
                "   <tr height=\"28\" style='height:14.00pt;'>\n" +
                "    <td class=\"xl66\" height=\"84\" rowspan=\"3\" style='height:42.00pt;border-right:.5pt solid windowtext;border-bottom:.5pt solid windowtext;' x:num>7</td>\n" +
                "    <td class=\"xl67\" x:str>地理</td>\n" +
                "    <td class=\"xl67\" x:str>数学</td>\n" +
                "    <td class=\"xl67\" x:str>生物</td>\n" +
                "    <td class=\"xl67\" x:str>心理健康</td>\n" +
                "    <td class=\"xl67\" x:str>地理</td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "   </tr>\n" +
                "   <tr height=\"28\" style='height:14.00pt;'>\n" +
                "    <td class=\"xl67\" x:str>姚云</td>\n" +
                "    <td class=\"xl67\" x:str>李迎春</td>\n" +
                "    <td class=\"xl67\" x:str>杨昳</td>\n" +
                "    <td class=\"xl67\" x:str>陈晨心理</td>\n" +
                "    <td class=\"xl67\" x:str>姚云</td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "   </tr>\n" +
                "   <tr height=\"28\" style='height:14.00pt;'>\n" +
                "    <td class=\"xl67\" x:str>NJ216 初<span style='display:none;'>一1班</span></td>\n" +
                "    <td class=\"xl67\" x:str>NJ216 初<span style='display:none;'>一1班</span></td>\n" +
                "    <td class=\"xl67\" x:str>NJ216 初<span style='display:none;'>一1班</span></td>\n" +
                "    <td class=\"xl67\" x:str>NJ216 初<span style='display:none;'>一1班</span></td>\n" +
                "    <td class=\"xl67\" x:str>NJ216 初<span style='display:none;'>一1班</span></td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "   </tr>\n" +
                "   <tr height=\"28\" style='height:14.00pt;'>\n" +
                "    <td class=\"xl66\" height=\"84\" rowspan=\"3\" style='height:42.00pt;border-right:.5pt solid windowtext;border-bottom:.5pt solid windowtext;' x:num>8</td>\n" +
                "    <td class=\"xl67\" x:str>语文</td>\n" +
                "    <td class=\"xl67\" x:str>历史</td>\n" +
                "    <td class=\"xl67\" x:str>音乐</td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "    <td class=\"xl67\" x:str>历史</td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "   </tr>\n" +
                "   <tr height=\"28\" style='height:14.00pt;'>\n" +
                "    <td class=\"xl67\" x:str>许迎迎</td>\n" +
                "    <td class=\"xl67\" x:str>马立军</td>\n" +
                "    <td class=\"xl67\" x:str>周辰</td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "    <td class=\"xl67\" x:str>马立军</td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "   </tr>\n" +
                "   <tr height=\"28\" style='height:14.00pt;'>\n" +
                "    <td class=\"xl67\" x:str>NJ216 初<span style='display:none;'>一1班</span></td>\n" +
                "    <td class=\"xl67\" x:str>NJ216 初<span style='display:none;'>一1班</span></td>\n" +
                "    <td class=\"xl67\" x:str>NJ216 初<span style='display:none;'>一1班</span></td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "    <td class=\"xl67\" x:str>NJ216 初<span style='display:none;'>一1班</span></td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "    <td class=\"xl67\"></td>\n" +
                "   </tr>\n" +
                "   <![if supportMisalignedColumns]>\n" +
                "    <tr width=\"0\" style='display:none;'/>\n" +
                "   <![endif]>\n" +
                "  </table>";
        return result;
    }

    @PostMapping(value = {"/queryXxkxx"})
    @ResponseBody
    public String queryXxkxx(HttpServletResponse response, String userId){
        String result="<table class='table_list' id='table1'>\n" +
                "\t\t\t\t\t\t\t<tr class='thead'>\n" +
                "\t\t\t\t\t\t\t\t<th width='20%'>课程名称</th>\n" +
                "\t\t\t\t\t\t\t\t<th width='15%'>所属活动</th>\n" +
                "\t\t\t\t\t\t\t\t<th width='10%'>授课教师</th>\n" +
                "\t\t\t\t\t\t\t\t<th width='5%'>最大人数</th>\n" +
                "\t\t\t\t\t\t\t\t<th width='10%'>上课时间</th>\n" +
                "\t\t\t\t\t\t\t\t<th width='10%'>操作</th>\n" +
                "\t\t\t\t\t\t\t</tr>\n" +
                "\t\t\t\t\t\t  \t \n" +
                "\t\t\t\t\t\t  \t \t <tr>\n" +
                "\t\t\t\t\t\t  \t \t \t \n" +
                "\t\t\t\t\t\t  \t \t \t <td>诗韵中国</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>【中学段】周三脊梁选修课</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>孙琳</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td><span class='red'>24</span></td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>2025/02/17 - 2025/06/06</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>\n" +
                "\t\t\t\t\t\t\t  \t \t \t<!--  <div class='blueBtnBox blueBtnBox2'> -->\n" +
                "\t\t\t\t\t \t\t\t\t\t\t\t<a href=\"/ytelective/scmAction!courseDetail.dhtml?syllabusid=2ce480f793cf407a0194fdbd93ec00c8&flag=0\" class='blue_btn'>详情</a>\n" +
                "\t\t\t\t\t\t\t  \t \t \t<!--  </div> -->\n" +
                "\t\t\t\t\t\t  \t \t \t </td>\n" +
                "\t\t\t\t\t\t  \t \t</tr>\n" +
                "\t\t\t\t\t\t  \t \n" +
                "\t\t\t\t\t\t  \t \t <tr>\n" +
                "\t\t\t\t\t\t  \t \t \t \n" +
                "\t\t\t\t\t\t  \t \t \t <td>康思谜题</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>【中学段】周一脊梁选修课</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>刘文超</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td><span class='red'>25</span></td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>2025/02/17 - 2025/06/06</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>\n" +
                "\t\t\t\t\t\t\t  \t \t \t<!--  <div class='blueBtnBox blueBtnBox2'> -->\n" +
                "\t\t\t\t\t \t\t\t\t\t\t\t<a href=\"/ytelective/scmAction!courseDetail.dhtml?syllabusid=2ce480f793cf407a0194fd2cac41000f&flag=0\" class='blue_btn'>详情</a>\n" +
                "\t\t\t\t\t\t\t  \t \t \t<!--  </div> -->\n" +
                "\t\t\t\t\t\t  \t \t \t </td>\n" +
                "\t\t\t\t\t\t  \t \t</tr>\n" +
                "\t\t\t\t\t\t  \t \n" +
                "\t\t\t\t\t\t  \t \t <tr>\n" +
                "\t\t\t\t\t\t  \t \t \t \n" +
                "\t\t\t\t\t\t  \t \t \t <td>排球社团（中段）周三</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>【中学段】周三脊梁选修课</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>冯亚宁</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td><span class='red'>30</span></td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>2024/09/02 - 2024/12/12</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>\n" +
                "\t\t\t\t\t\t\t  \t \t \t<!--  <div class='blueBtnBox blueBtnBox2'> -->\n" +
                "\t\t\t\t\t \t\t\t\t\t\t\t<a href=\"/ytelective/scmAction!courseDetail.dhtml?syllabusid=2ce480f790d27ef1019192313d77029e&flag=0\" class='blue_btn'>详情</a>\n" +
                "\t\t\t\t\t\t\t  \t \t \t<!--  </div> -->\n" +
                "\t\t\t\t\t\t  \t \t \t </td>\n" +
                "\t\t\t\t\t\t  \t \t</tr>\n" +
                "\t\t\t\t\t\t  \t \n" +
                "\t\t\t\t\t\t  \t \t <tr>\n" +
                "\t\t\t\t\t\t  \t \t \t \n" +
                "\t\t\t\t\t\t  \t \t \t <td>数智思维课</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>【中学段】周一脊梁选修课</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>康路</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td><span class='red'>25</span></td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>2024/09/02 - 2024/12/12</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>\n" +
                "\t\t\t\t\t\t\t  \t \t \t<!--  <div class='blueBtnBox blueBtnBox2'> -->\n" +
                "\t\t\t\t\t \t\t\t\t\t\t\t<a href=\"/ytelective/scmAction!courseDetail.dhtml?syllabusid=2ce480f790d27ef101918ced3da3000c&flag=0\" class='blue_btn'>详情</a>\n" +
                "\t\t\t\t\t\t\t  \t \t \t<!--  </div> -->\n" +
                "\t\t\t\t\t\t  \t \t \t </td>\n" +
                "\t\t\t\t\t\t  \t \t</tr>\n" +
                "\t\t\t\t\t\t  \t \n" +
                "\t\t\t\t\t\t  \t \t <tr>\n" +
                "\t\t\t\t\t\t  \t \t \t \n" +
                "\t\t\t\t\t\t  \t \t \t <td>跆拳道</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>【中学段】周三脊梁课</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>李丽廷</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td><span class='red'>11</span></td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>2024/02/26 - 2024/06/14</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>\n" +
                "\t\t\t\t\t\t\t  \t \t \t<!--  <div class='blueBtnBox blueBtnBox2'> -->\n" +
                "\t\t\t\t\t \t\t\t\t\t\t\t<a href=\"/ytelective/scmAction!courseDetail.dhtml?syllabusid=2ce480ba8d544652018dcb40746e02df&flag=0\" class='blue_btn'>详情</a>\n" +
                "\t\t\t\t\t\t\t  \t \t \t<!--  </div> -->\n" +
                "\t\t\t\t\t\t  \t \t \t </td>\n" +
                "\t\t\t\t\t\t  \t \t</tr>\n" +
                "\t\t\t\t\t\t  \t \n" +
                "\t\t\t\t\t\t  \t \t <tr>\n" +
                "\t\t\t\t\t\t  \t \t \t \n" +
                "\t\t\t\t\t\t  \t \t \t <td>英语小剧场</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>【中学段】周一脊梁课</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>冯江连</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td><span class='red'>23</span></td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>2024/02/26 - 2024/06/14</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>\n" +
                "\t\t\t\t\t\t\t  \t \t \t<!--  <div class='blueBtnBox blueBtnBox2'> -->\n" +
                "\t\t\t\t\t \t\t\t\t\t\t\t<a href=\"/ytelective/scmAction!courseDetail.dhtml?syllabusid=2ce480ba8d544652018dca7446a90156&flag=0\" class='blue_btn'>详情</a>\n" +
                "\t\t\t\t\t\t\t  \t \t \t<!--  </div> -->\n" +
                "\t\t\t\t\t\t  \t \t \t </td>\n" +
                "\t\t\t\t\t\t  \t \t</tr>\n" +
                "\t\t\t\t\t\t  \t \n" +
                "\t\t\t\t\t\t  \t \t <tr>\n" +
                "\t\t\t\t\t\t  \t \t \t \n" +
                "\t\t\t\t\t\t  \t \t \t <td>排球社团</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>体育社团选课</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>冯亚宁</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td><span class='red'>30</span></td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>2023/09/06 - 2024/07/01</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>\n" +
                "\t\t\t\t\t\t\t  \t \t \t<!--  <div class='blueBtnBox blueBtnBox2'> -->\n" +
                "\t\t\t\t\t \t\t\t\t\t\t\t<a href=\"/ytelective/scmAction!courseDetail.dhtml?syllabusid=2ce480ac8a5e34a9018a740ddce7001c&flag=0\" class='blue_btn'>详情</a>\n" +
                "\t\t\t\t\t\t\t  \t \t \t<!--  </div> -->\n" +
                "\t\t\t\t\t\t  \t \t \t </td>\n" +
                "\t\t\t\t\t\t  \t \t</tr>\n" +
                "\t\t\t\t\t\t  \t \n" +
                "\t\t\t\t\t\t  \t \t <tr>\n" +
                "\t\t\t\t\t\t  \t \t \t \n" +
                "\t\t\t\t\t\t  \t \t \t <td>诗韵中国</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>【中学段】周一脊梁课</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>孙琳</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td><span class='red'>25</span></td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>2023/09/04 - 2023/12/22</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>\n" +
                "\t\t\t\t\t\t\t  \t \t \t<!--  <div class='blueBtnBox blueBtnBox2'> -->\n" +
                "\t\t\t\t\t \t\t\t\t\t\t\t<a href=\"/ytelective/scmAction!courseDetail.dhtml?syllabusid=2ce4808e899657dd018a3fa8e3950039&flag=0\" class='blue_btn'>详情</a>\n" +
                "\t\t\t\t\t\t\t  \t \t \t<!--  </div> -->\n" +
                "\t\t\t\t\t\t  \t \t \t </td>\n" +
                "\t\t\t\t\t\t  \t \t</tr>\n" +
                "\t\t\t\t\t\t  \t \n" +
                "\t\t\t\t\t\t  \t \t <tr>\n" +
                "\t\t\t\t\t\t  \t \t \t \n" +
                "\t\t\t\t\t\t  \t \t \t <td>走近百老汇</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>【中学段】周三脊梁课</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>陈阳</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td><span class='red'>28</span></td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>2023/09/04 - 2023/12/22</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>\n" +
                "\t\t\t\t\t\t\t  \t \t \t<!--  <div class='blueBtnBox blueBtnBox2'> -->\n" +
                "\t\t\t\t\t \t\t\t\t\t\t\t<a href=\"/ytelective/scmAction!courseDetail.dhtml?syllabusid=2ce4808e899657dd018a409150dc008e&flag=0\" class='blue_btn'>详情</a>\n" +
                "\t\t\t\t\t\t\t  \t \t \t<!--  </div> -->\n" +
                "\t\t\t\t\t\t  \t \t \t </td>\n" +
                "\t\t\t\t\t\t  \t \t</tr>\n" +
                "\t\t\t\t\t\t  \t \n" +
                "\t\t\t\t\t\t  \t \t <tr>\n" +
                "\t\t\t\t\t\t  \t \t \t \n" +
                "\t\t\t\t\t\t  \t \t \t <td>7.4日低学段冰上体验  12：20-12：50</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>2023冰上体验课程（低学段）</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>罗霄</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td><span class='red'>70</span></td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>2023/07/04 - 2023/07/04</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>\n" +
                "\t\t\t\t\t\t\t  \t \t \t<!--  <div class='blueBtnBox blueBtnBox2'> -->\n" +
                "\t\t\t\t\t \t\t\t\t\t\t\t<a href=\"/ytelective/scmAction!courseDetail.dhtml?syllabusid=2c91883a88c44ee90188d74d6c790298&flag=0\" class='blue_btn'>详情</a>\n" +
                "\t\t\t\t\t\t\t  \t \t \t<!--  </div> -->\n" +
                "\t\t\t\t\t\t  \t \t \t </td>\n" +
                "\t\t\t\t\t\t  \t \t</tr>\n" +
                "\t\t\t\t\t\t  \t \n" +
                "\t\t\t\t\t\t  \t \t <tr>\n" +
                "\t\t\t\t\t\t  \t \t \t \n" +
                "\t\t\t\t\t\t  \t \t \t <td>编结课程（非遗）</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>2023科技节体验课程（低学段）</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>李老师</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td><span class='red'>30</span></td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>2023/07/03 - 2023/07/03</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>\n" +
                "\t\t\t\t\t\t\t  \t \t \t<!--  <div class='blueBtnBox blueBtnBox2'> -->\n" +
                "\t\t\t\t\t \t\t\t\t\t\t\t<a href=\"/ytelective/scmAction!courseDetail.dhtml?syllabusid=2c91883a88c44ee90188cecab0b60003&flag=0\" class='blue_btn'>详情</a>\n" +
                "\t\t\t\t\t\t\t  \t \t \t<!--  </div> -->\n" +
                "\t\t\t\t\t\t  \t \t \t </td>\n" +
                "\t\t\t\t\t\t  \t \t</tr>\n" +
                "\t\t\t\t\t\t  \t \n" +
                "\t\t\t\t\t\t  \t \t <tr>\n" +
                "\t\t\t\t\t\t  \t \t \t \n" +
                "\t\t\t\t\t\t  \t \t \t <td>少儿体适能</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>【低学段】脊梁课（周二）</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>夏浩轩</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td><span class='red'>26</span></td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>2023/02/14 - 2023/06/09</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>\n" +
                "\t\t\t\t\t\t\t  \t \t \t<!--  <div class='blueBtnBox blueBtnBox2'> -->\n" +
                "\t\t\t\t\t \t\t\t\t\t\t\t<a href=\"/ytelective/scmAction!courseDetail.dhtml?syllabusid=2ce480c4850613bb01863050e1fc0019&flag=0\" class='blue_btn'>详情</a>\n" +
                "\t\t\t\t\t\t\t  \t \t \t<!--  </div> -->\n" +
                "\t\t\t\t\t\t  \t \t \t </td>\n" +
                "\t\t\t\t\t\t  \t \t</tr>\n" +
                "\t\t\t\t\t\t  \t \n" +
                "\t\t\t\t\t\t  \t \t <tr>\n" +
                "\t\t\t\t\t\t  \t \t \t \n" +
                "\t\t\t\t\t\t  \t \t \t <td>《围棋》</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>【低学段】脊梁课（周四）</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>任一鸣</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td><span class='red'>25</span></td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>2023/02/14 - 2023/06/09</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>\n" +
                "\t\t\t\t\t\t\t  \t \t \t<!--  <div class='blueBtnBox blueBtnBox2'> -->\n" +
                "\t\t\t\t\t \t\t\t\t\t\t\t<a href=\"/ytelective/scmAction!courseDetail.dhtml?syllabusid=2ce480c48633b0de0186352f31d700c5&flag=0\" class='blue_btn'>详情</a>\n" +
                "\t\t\t\t\t\t\t  \t \t \t<!--  </div> -->\n" +
                "\t\t\t\t\t\t  \t \t \t </td>\n" +
                "\t\t\t\t\t\t  \t \t</tr>\n" +
                "\t\t\t\t\t\t  \t \n" +
                "\t\t\t\t\t\t  \t \t <tr>\n" +
                "\t\t\t\t\t\t  \t \t \t \n" +
                "\t\t\t\t\t\t  \t \t \t <td>小篮球基础</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>【低学段】脊梁课（周四）</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>夏浩轩</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td><span class='red'>23</span></td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>2022/09/05 - 2022/12/09</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>\n" +
                "\t\t\t\t\t\t\t  \t \t \t<!--  <div class='blueBtnBox blueBtnBox2'> -->\n" +
                "\t\t\t\t\t \t\t\t\t\t\t\t<a href=\"/ytelective/scmAction!courseDetail.dhtml?syllabusid=2ce480c1817a3bdf0182edea51f0007d&flag=0\" class='blue_btn'>详情</a>\n" +
                "\t\t\t\t\t\t\t  \t \t \t<!--  </div> -->\n" +
                "\t\t\t\t\t\t  \t \t \t </td>\n" +
                "\t\t\t\t\t\t  \t \t</tr>\n" +
                "\t\t\t\t\t\t  \t \n" +
                "\t\t\t\t\t\t  \t \t <tr>\n" +
                "\t\t\t\t\t\t  \t \t \t \n" +
                "\t\t\t\t\t\t  \t \t \t <td>英语小剧场</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>【低学段】脊梁课（周二）</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>冯江连</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td><span class='red'>22</span></td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>2022/09/05 - 2022/12/09</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>\n" +
                "\t\t\t\t\t\t\t  \t \t \t<!--  <div class='blueBtnBox blueBtnBox2'> -->\n" +
                "\t\t\t\t\t \t\t\t\t\t\t\t<a href=\"/ytelective/scmAction!courseDetail.dhtml?syllabusid=2ce480c1817a3bdf0182ed1183460034&flag=0\" class='blue_btn'>详情</a>\n" +
                "\t\t\t\t\t\t\t  \t \t \t<!--  </div> -->\n" +
                "\t\t\t\t\t\t  \t \t \t </td>\n" +
                "\t\t\t\t\t\t  \t \t</tr>\n" +
                "\t\t\t\t\t\t  \t \n" +
                "\t\t\t\t\t\t  \t \t <tr>\n" +
                "\t\t\t\t\t\t  \t \t \t \n" +
                "\t\t\t\t\t\t  \t \t \t <td>小篮球基础</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>【低学段】周四脊梁课</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>夏浩轩</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td><span class='red'>25</span></td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>2022/02/28 - 2022/06/17</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>\n" +
                "\t\t\t\t\t\t\t  \t \t \t<!--  <div class='blueBtnBox blueBtnBox2'> -->\n" +
                "\t\t\t\t\t \t\t\t\t\t\t\t<a href=\"/ytelective/scmAction!courseDetail.dhtml?syllabusid=2ce480df7e277c20017f159e782b0036&flag=0\" class='blue_btn'>详情</a>\n" +
                "\t\t\t\t\t\t\t  \t \t \t<!--  </div> -->\n" +
                "\t\t\t\t\t\t  \t \t \t </td>\n" +
                "\t\t\t\t\t\t  \t \t</tr>\n" +
                "\t\t\t\t\t\t  \t \n" +
                "\t\t\t\t\t\t  \t \t <tr>\n" +
                "\t\t\t\t\t\t  \t \t \t \n" +
                "\t\t\t\t\t\t  \t \t \t <td>小篮球基础</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>【低学段】周二脊梁课</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>夏浩轩</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td><span class='red'>25</span></td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>2022/02/28 - 2022/06/17</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>\n" +
                "\t\t\t\t\t\t\t  \t \t \t<!--  <div class='blueBtnBox blueBtnBox2'> -->\n" +
                "\t\t\t\t\t \t\t\t\t\t\t\t<a href=\"/ytelective/scmAction!courseDetail.dhtml?syllabusid=2ce480df7e277c20017f159c1c1b0033&flag=0\" class='blue_btn'>详情</a>\n" +
                "\t\t\t\t\t\t\t  \t \t \t<!--  </div> -->\n" +
                "\t\t\t\t\t\t  \t \t \t </td>\n" +
                "\t\t\t\t\t\t  \t \t</tr>\n" +
                "\t\t\t\t\t\t  \t \n" +
                "\t\t\t\t\t\t  \t \t <tr>\n" +
                "\t\t\t\t\t\t  \t \t \t \n" +
                "\t\t\t\t\t\t  \t \t \t <td>科学小发现</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>【低学段】周二脊梁选修课</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>冯迪</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td><span class='red'>30</span></td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>2021/09/07 - 2021/12/24</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>\n" +
                "\t\t\t\t\t\t\t  \t \t \t<!--  <div class='blueBtnBox blueBtnBox2'> -->\n" +
                "\t\t\t\t\t \t\t\t\t\t\t\t<a href=\"/ytelective/scmAction!courseDetail.dhtml?syllabusid=2ce480fe7b1fc13f017b96754b7b0034&flag=0\" class='blue_btn'>详情</a>\n" +
                "\t\t\t\t\t\t\t  \t \t \t<!--  </div> -->\n" +
                "\t\t\t\t\t\t  \t \t \t </td>\n" +
                "\t\t\t\t\t\t  \t \t</tr>\n" +
                "\t\t\t\t\t\t  \t \n" +
                "\t\t\t\t\t\t  \t \t <tr>\n" +
                "\t\t\t\t\t\t  \t \t \t \n" +
                "\t\t\t\t\t\t  \t \t \t <td>趣味数学思维</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>【低学段】周四脊梁选修课</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>刘茜</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td><span class='red'>30</span></td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>2021/09/07 - 2021/12/24</td>\n" +
                "\t\t\t\t\t\t  \t \t \t <td>\n" +
                "\t\t\t\t\t\t\t  \t \t \t<!--  <div class='blueBtnBox blueBtnBox2'> -->\n" +
                "\t\t\t\t\t \t\t\t\t\t\t\t<a href=\"/ytelective/scmAction!courseDetail.dhtml?syllabusid=2ce480de7b9a3e86017b9b2af525006f&flag=0\" class='blue_btn'>详情</a>\n" +
                "\t\t\t\t\t\t\t  \t \t \t<!--  </div> -->\n" +
                "\t\t\t\t\t\t  \t \t \t </td>\n" +
                "\t\t\t\t\t\t  \t \t</tr>\n" +
                "\t\t\t\t\t\t  \t \n" +
                "\t\t\t\t\t  \t </table>";
        return result;
    }

}
