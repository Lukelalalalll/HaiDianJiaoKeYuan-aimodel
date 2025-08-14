package com.zklcsoftware.aimodel.util;

public class EchartsUtil {
	
	//基础柱状图
	public final static String category = new StringBuffer().append("{")
															//.append("option = {")
															.append("xAxis: {")
															.append("type: 'category',")
															//.append("data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']")
															.append("data: [xAxisData]")
															.append("},")
															.append("yAxis: {")
															.append("type: 'value'")
															.append("},")
															.append("series: [")
															.append("{")
															//.append("data: [120, 200, 150, 80, 70, 110, 130],")
															.append("data: [yAxisData],")
															.append("type: 'bar'")
															.append("}")
															.append("]")
															.append("};").toString();
	
}