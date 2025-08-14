package com.zklcsoftware.aimodel.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * @Description 解析目录 将下面的文档转成txt 存放到原始文件目录
 * @Author zhushaog
 * @UpdateTime 2025/2/26 15:59
 * @throws
 */
@Slf4j
public class FileConverter {

    // 假设这是已经存在的转换解析方法
    public static String convertFileToString(File file){
        return convertFileToString(file,null);
    }
    public static String convertFileToString(File file, Map extConfigMap){
        // 这里应该是具体的文件转换逻辑
        try {
            return FileContentReader.readFileContent(file,  extConfigMap);
        } catch (Exception e) {
            log.error("转换文件时出错: " + file.getAbsolutePath());
            return "";
        }
    }

    // 将目录下的所有文件转换为txt文件
    public static void convertDirectoryToTxt(String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.exists() || !directory.isDirectory()) {
            log.info("指定的路径不是一个有效的目录: " + directoryPath);
            return;
        }

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    // 递归处理子目录
                    convertDirectoryToTxt(file.getAbsolutePath());
                } else {
                    try {
                        if(!file.getAbsolutePath().toLowerCase().endsWith(".txt")){
                            // 调用转换方法
                            String content = convertFileToString(file);
                            if(StringUtils.isEmpty(content)){
                                log.info("未识别出内容的文件:{}", file.getAbsolutePath());
                            }else{
                                // 创建新的txt文件路径
                                Path txtFilePath = Paths.get(file.getParent(), file.getName() + ".txt");
                                // 写入内容到新的txt文件
                                Files.write(txtFilePath, content.getBytes());
                                log.error("已转换文件: " + file.getAbsolutePath() + " 到 " + txtFilePath);
                            }
                        }

                    } catch (IOException e) {
                        log.error("转换文件时出错:{}" ,file.getAbsolutePath());
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static void listDirectoryContents(String directoryPath, int level) {
        File directory = new File(directoryPath);
        if (!directory.exists() || !directory.isDirectory()) {
            log.info("指定的路径不是一个有效的目录: " + directoryPath);
            return;
        }
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                // 输出当前文件或目录的路径，根据层级缩进
                StringBuilder indent = new StringBuilder();
                for (int i = 0; i < level; i++) {
                    indent.append("  "); // 使用两个空格作为缩进
                }
                System.out.println(indent.toString() + file.getName());

                if (file.isDirectory()) {
                    // 递归处理子目录，层级加1
                    listDirectoryContents(file.getAbsolutePath(), level + 1);
                }
            }
        }
    }

    public static void main(String[] args) {
        // 示例调用
        String directoryPath = "D:\\海淀教科院新闻资料\\同步教科院\\小海灵\\教科院新闻资料";
        convertDirectoryToTxt(directoryPath);
        // 调用新方法列出目录内容
        //listDirectoryContents(directoryPath, 0);
    }
}
