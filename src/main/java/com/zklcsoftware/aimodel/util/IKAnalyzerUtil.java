package com.zklcsoftware.aimodel.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

/**
 * @Description  分词器工具类
 * @Author zhushaog
 * @UpdateTime 2022/10/23 14:23
 * @throws
 */
public class IKAnalyzerUtil {

	/**
	 * @Description  返回文本的分词结果(去除停止词)
	 * @Author zhushaog
	 * @param: text
	 * @UpdateTime 2022/10/23 14:23
	 * @return: java.util.List<java.lang.String>
	 * @throws
	 */
	public static List<String> getIKAnalyzerResult(String text) throws IOException {

		StringReader sr=new StringReader(text);
		List<String> reulsts=new ArrayList<>();

		IKSegmenter ik=new IKSegmenter(sr, true);
		Lexeme lex=null;
		while((lex=ik.next())!=null){
			if(lex.getLexemeText()!=null){
				reulsts.add(lex.getLexemeText());
			}
		}
		return reulsts;
	}

	//测试
	public static void main(String[] args) throws IOException {
		String text="介绍下学校信息";
		List<String> results=IKAnalyzerUtil.getIKAnalyzerResult(text);
		System.out.println(results);
	}

}

