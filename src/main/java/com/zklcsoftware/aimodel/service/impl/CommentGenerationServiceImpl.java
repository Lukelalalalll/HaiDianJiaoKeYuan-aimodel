package com.zklcsoftware.aimodel.service.impl;

import cn.hutool.poi.excel.WorkbookUtil;
import com.zklcsoftware.aimodel.domain.TAiSysPrompt;
import com.zklcsoftware.aimodel.service.CommentGenerationService;
import com.zklcsoftware.aimodel.service.TAiSysPromptService;
import com.zklcsoftware.aimodel.websocket.dto.AiOutMsgDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * @author 杨洁
 * @ClassName CommentGenerationServiceImpl.java
 * @Description 评语生成
 * @createTime 2025-01-10 14:34
 **/
@Service("CommentGenerationServiceImpl")
@Slf4j
public class CommentGenerationServiceImpl implements CommentGenerationService {

    @Autowired
    TAiSysPromptService tAiSysPromptService;
    @Autowired
    TAiModelServiceImpl tAiModelService;
    @Value("${uploadfiledir.uploadFilePath}")
    private String uploadFilePath;//服务器路径
    @Value("${uploadfiledir.uploadFileUrl}")
    private String uploadFileUrl;//文件封存的http地址

    @Override
    public AiOutMsgDTO CommentGeneration(String zntId, String fileUrl, String content,String userId) {

        AiOutMsgDTO aiOutMsgDTO = new AiOutMsgDTO();

        try {
            //文件路径，替换http地址
            String filePath=uploadFilePath+fileUrl.replace(uploadFileUrl + "/","");
            File file = new File(filePath);

            //文件名称
            String strFileName = file.getName();
            if (StringUtils.isBlank(strFileName) || !(strFileName.endsWith(".xls") || strFileName.endsWith(".xlsx"))){
                System.out.println("文件格式有误，请重新选择，仅支持excel文件(*.xls,*.xlsx)");
            }

            //查询智能体信息
            TAiSysPrompt prompt = tAiSysPromptService.findById(zntId);
            //用户提示词模板
            String userPromptTemplate = prompt.getUserPromptTemplate();

            //创建一个导出工作簿
            Workbook workbook;
            if (strFileName.endsWith(".xls")) {
                workbook = new HSSFWorkbook();
            } else {
                workbook = new XSSFWorkbook();
            }

            //获取文件流
            FileInputStream inputStream = new FileInputStream(file);
            //InputStream inputStream = file.getInputStream();
            Workbook book = WorkbookUtil.createBook(inputStream);
            //循环遍历工作簿中的每个工作表
            for (int i = 0; i < book.getNumberOfSheets(); i++) {
                //获取工作表
                Sheet sheetAt = book.getSheetAt(i);
                Row row = sheetAt.getRow(0);//第一行标题

                if (row == null) {
                    continue;
                }
                //导出sheet
                Sheet sheet = workbook.createSheet(String.valueOf(i));
                //创建一个新的样式
                CellStyle newStyle = workbook.createCellStyle();
                //将标题赋值到导出sheet中
                Row sheetRow = sheet.createRow(0);
                for (int j = 0; j < row.getPhysicalNumberOfCells(); j++) {
                    int columnWidth = row.getSheet().getColumnWidth(j);
                    sheet.setColumnWidth(j, columnWidth);
                    // 复制样式属性
                    newStyle.cloneStyleFrom(row.getCell(j).getCellStyle());
                    Cell cell = sheetRow.createCell(j);
                    cell.setCellValue(row.getCell(j).getStringCellValue());
                    cell.setCellStyle(newStyle);
                }
                //在标题后添加一列评语
                sheet.setColumnWidth(row.getPhysicalNumberOfCells() + 1, 256 * 20);
                sheetRow.createCell(row.getPhysicalNumberOfCells()).setCellValue("评语");

                //循环遍历行（从第二行开始）
                for (int j = 1; j < sheetAt.getPhysicalNumberOfRows(); j++) {
                    if(j > 11){
                        break;
                    }
                    Row nextRwo = sheetAt.getRow(j);//获取行
                    //导出行数据
                    Row sheetNextRow = sheet.createRow(j);
                    String rowContent = "";//行内容
                    //循环遍历列
                    for (int k = 0; k < nextRwo.getPhysicalNumberOfCells(); k++) {
                        //对应标题的单元格数据
                        String rowValue = row.getCell(k).getStringCellValue();
                        //单元格数据
                        String nextCellValue = nextRwo.getCell(k).getStringCellValue();
                        rowContent += rowContent == "" ? rowValue + ":" + nextCellValue : "," + rowValue + ":" + nextCellValue;

                        //导出的单元格数据
                        Cell cell = sheetNextRow.createCell(k);
                        cell.setCellValue(nextCellValue);
                    }
                    /*//将提示词中的内容替换
                    String promptContent = userPromptTemplate
                            .replace("${content}", rowContent == "" ? "" : rowContent)
                            .replace("${ckbz}", content == null ? "" : content);*/
                    //直调文档回答接口
                    AiOutMsgDTO answerApi = tAiModelService.answerApi(zntId, "{\"content\": \""+rowContent+"\",\"ckbz\": \""+content+"\"}",userId);
                    Object data = answerApi.getData();
                    //JsonObject jsonObject = new Gson().fromJson(data.toString(), JsonObject.class);
                    //导出的单元格数据
                    Cell cell = sheetNextRow.createCell(sheetAt.getPhysicalNumberOfRows() + 1);
                    cell.setCellValue(data.toString().replace("\"", ""));
                }
            }
            inputStream.close();//关闭输入流

            // 将新的Excel文件写入输出流
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            byte[] bytes = outputStream.toByteArray();

            // 创建目录（如果目录不存在）
            File dir = new File(uploadFilePath);
            if (!dir.exists()) {
                dir.mkdirs(); // 创建多级目录
            }

            //没有文件去创建文件
            //File file1 = new File(uploadFilePath + strFileName.split("\\.")[0] + System.currentTimeMillis() + (strFileName.endsWith(".xls") ?".xls": ".xlsx"));
            File file1 = new File(filePath);
            // 检查文件是否存在，如果不存在则创建文件
            if (!file1.exists()) {
                file1.createNewFile(); // 创建文件
            }

            // 上传Excel文件到服务器
            FileOutputStream fos = new FileOutputStream(file1);
            fos.write(bytes);
            fos.close();

            //JsonObject dataObj = new JsonObject();
            //dataObj.add("fileUrl", new JsonPrimitive(uploadFilePath + strFileName.split("\\.")[0] + ".xls"));

            aiOutMsgDTO.setResponseType(2);
            aiOutMsgDTO.setDataType("json");
            aiOutMsgDTO.setDone(false);
            aiOutMsgDTO.setData(filePath.replace(uploadFilePath, uploadFileUrl + "/"));

        }catch (Exception ex){
            ex.printStackTrace();
        }
        return aiOutMsgDTO;
    }
}
