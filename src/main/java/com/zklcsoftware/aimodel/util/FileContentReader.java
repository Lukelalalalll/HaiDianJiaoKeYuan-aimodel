package com.zklcsoftware.aimodel.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.zklcsoftware.basic.util.ServletUtils;
import com.zklcsoftware.common.web.util.Base64Util;
import com.zklcsoftware.doubao.signer.DoubaoHttpClients;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.microsoft.OfficeParser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description 文本文件读取工具类
 * @Author zhushaog
 * @UpdateTime 2025/3/14 14:11
 * @throws
 */
@Slf4j
@Component
public class FileContentReader {
    @Autowired
    public static String readPdf(File file) throws IOException {
        String content = "";
        PDFTextStripper pdfStripper = new PDFTextStripper();
        try (PDDocument document = PDDocument.load(file)) {
            content=pdfStripper.getText(document);
        }catch (Exception e){
            log.error("pdf文本读取异常,尝试使用ocr智能文档解析功能处理{}",file.getPath());
            if("1".equals(ConstantUtil.sysConfig.get("is_use_ocrpdf"))){//判断是否开启ocr识别pdf功能 1是 0否 默认否
                content=readPdfByOCR(file);
            }
        }
        return content;
    }

    /**
     * @Description 使用ocr识别功能解析pdf,内网5M以下，外网100M以下
     * @Author zhushaog
     * @param: file
     * @UpdateTime 2025/3/14 14:10
     * @return: java.lang.String
     * @throws
     */
    public static String readPdfByOCR(File file) throws IOException {

        String wstAccessKeyID= "AKLTNDg0ZDI0MGM5NDViNGU1YWJlZDMyYzNhMThjYjYzYWM";//从全局配置中读取  火山引擎
        String wstSecretAccessKey= "TmpJNU5tWXdZalU1TURoa05HVXlORGhoT0RBME1EWmhaVFUxWW1RM1lqaw==";//从全局配置中读取  火山引擎

        String fileBody=ConstantUtil.sysConfig.get("uploadFileUrl")+file.getPath().replace(ConstantUtil.sysConfig.get("uploadFilePath"),"");//网络地址
        if(ServletUtils.isIntranetAddress(fileBody)){//内网地址
            if(file.length()<=5*1024*1024){//仅处理小于5MBpdf文件  使用base64编码方式提交
                fileBody= Base64Util.encodeFileToBase64(file.getPath());
            }else{
                return "";//内网地址文件，超过5MB不处理
            }
        }else{
            if(file.length()>100*1024*1024){
                return "";//外网地址文件，超过100MB不处理
            }
        }
        return readPdfByOCR(wstAccessKeyID,wstSecretAccessKey,fileBody);

    }
    /**
     * @Description 识别内容
     * @Author zhushaog
     * @param: fileBody
     * @param: ocrType
     * @UpdateTime 2025/4/14 21:22
     * @return: java.lang.String
     * @throws
     */
    public static String readPdfByOCR(String wstAccessKeyID,String wstSecretAccessKey,String fileBody) throws FileNotFoundException {
        String ocrApi = "http://visual.volcengineapi.com?Action=OCRPdf&Version=2021-08-23";
        //解析文档相关请求参数
        Map<String, Object> paramMap = new HashMap<>();
        if(fileBody.startsWith("http")){//判断是网络文件地址方式还是BASE64编码方式
            paramMap.put("image_url",fileBody);//网络地址
        }else{
            paramMap.put("image_base64",fileBody);//文件内容 base64编码
        }
        paramMap.put("file_type","pdf");
        paramMap.put("version","v3");
        paramMap.put("page_num",300);
        String content = "";//识别内容
        try {
            String result = DoubaoHttpClients.post(ocrApi, paramMap, null,wstAccessKeyID, wstSecretAccessKey,"cn-north-1","cv");
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(result, JsonObject.class);
            long code = jsonObject.get("code").getAsLong();
            if (code==10000) {// 成功
                JsonObject data = jsonObject.getAsJsonObject("data");
                log.info("pdf识别内容{}",data.get("markdown"));
                content=data.get("markdown").getAsString();
            }
        } catch (Exception e) {
            log.error("ocr识别pdf异常{}",fileBody);
        }
        return content;
    }

    public static String readWord(File file) throws IOException {
        if (file.getName().toLowerCase().endsWith(".docx")) {
            StringBuilder textBuilder = new StringBuilder();
            try (XWPFDocument doc = new XWPFDocument(new FileInputStream(file))) {
                // 获取所有段落
                List<XWPFParagraph> paragraphs = doc.getParagraphs();
                for (XWPFParagraph paragraph : paragraphs) {
                    textBuilder.append(paragraph.getText()).append("\n");
                }
                return textBuilder.toString().trim();
            }
        } else {
            // For .doc files, use Tika
            BodyContentHandler handler = new BodyContentHandler(-1);
            Metadata metadata = new Metadata();
            ParseContext parseContext = new ParseContext();
            OfficeParser officeParser = new OfficeParser();
            try (InputStream stream = new FileInputStream(file)) {
                officeParser.parse(stream, handler, metadata, parseContext);
                return handler.toString();
            } catch (TikaException e) {
                throw new RuntimeException(e);
            } catch (SAXException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static String readExcel(File file) throws IOException, InvalidFormatException {
        StringBuilder sb = new StringBuilder();
        Workbook workbook = null;
        if (file.getName().toLowerCase().endsWith(".xlsx")) {
            workbook = new XSSFWorkbook(file);
        } else if (file.getName().toLowerCase().endsWith(".xls")) {
            workbook = new HSSFWorkbook(new FileInputStream(file));
        }

        if (workbook != null) {
            try {
                for (Sheet sheet : workbook) {
                    for (Row row : sheet) {
                        for (Cell cell : row) {
                            switch (cell.getCellType()) {
                                case STRING:
                                    sb.append(cell.getStringCellValue()).append("\t");
                                    break;
                                case NUMERIC:
                                    sb.append(cell.getNumericCellValue()).append("\t");
                                    break;
                                case BOOLEAN:
                                    sb.append(cell.getBooleanCellValue()).append("\t");
                                    break;
                                default:
                                    sb.append("\t");
                            }
                        }
                        sb.append("\n");
                    }
                }
            } finally {
                workbook.close();
            }
        }
        return sb.toString();
    }

    public static String readTxt(File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }

    private static String readPpt(File file) throws FileNotFoundException {

        BodyContentHandler handler = new BodyContentHandler(-1);
        Metadata metadata = new Metadata();
        AutoDetectParser parser = new AutoDetectParser();

        String text = "";
        try (FileInputStream stream = new FileInputStream(file)) {
            parser.parse(stream, handler, metadata, new ParseContext());
            text = handler.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TikaException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
        return text;
    }

    public static String readFileContent(File file, Map extConfigMap) throws IOException, InvalidFormatException {
        String fileName = file.getName().toLowerCase();
        String documentText = "";
        if (fileName.endsWith(".pdf")) {
            if(extConfigMap!=null && Boolean.valueOf(String.valueOf(extConfigMap.get("pdf_ai")))){
                documentText= readPdfByOCR(file);//使用视觉理解识别pdf内容
            }else{
                documentText= readPdf(file);
            }
        } else if (fileName.endsWith(".doc") || fileName.endsWith(".docx")) {
            documentText= readWord(file);
        } else if (fileName.endsWith(".xls") || fileName.endsWith(".xlsx")) {
            documentText= readExcel(file);
        } else if (fileName.endsWith(".txt") || fileName.endsWith(".md")) {
            documentText= readTxt(file);
        } else if (fileName.endsWith(".ppt") || fileName.endsWith(".pptx")) {
            documentText= readPpt(file);
        } else {
            throw new IllegalArgumentException("Unsupported file type: " + fileName);
        }
        //先判断是否开启利用ai结构化文档内容成md，再判断 doc/pdf 内容字数，如果高于1.5w字，则使用pandoc转换md文档
        if((fileName.endsWith(".doc") || fileName.endsWith(".docx"))){
            if(extConfigMap!=null && Boolean.valueOf(String.valueOf(extConfigMap.get("md_ai")))){
                if(documentText.length()<15000){
                    documentText= WordToMdConverter.doc2mdByAi(documentText,String.valueOf(extConfigMap.get("deepseek_api")),String.valueOf(extConfigMap.get("deepseek_modelid")),String.valueOf(extConfigMap.get("deepseek_apikey")));//使用ai 整理word内容 转换成md
                }else{
                    String doc2md="";
                    if(fileName.endsWith(".docx")){
                        doc2md=WordToMdConverter.doc2mdByPythonUtil(file);//使用PythonUtil整理word内容 转换成md
                    }
                    if(StringUtils.isBlank(doc2md)){
                        doc2md=WordToMdConverter.doc2mdByPandoc(file);//使用Pandoc整理word内容 转换成md
                    }
                    if(StringUtils.isNotBlank(doc2md)){
                        documentText=doc2md;
                    }
                }
            }else{
                String doc2md="";
                if(fileName.endsWith(".docx")){
                    doc2md=WordToMdConverter.doc2mdByPythonUtil(file);//使用PythonUtil整理word内容 转换成md
                }
                if(StringUtils.isBlank(doc2md)){
                    doc2md=WordToMdConverter.doc2mdByPandoc(file);//使用Pandoc整理word内容 转换成md
                }
                if(StringUtils.isNotBlank(doc2md)){
                    documentText=doc2md;
                }
            }
        }
        return documentText;
    }

    public static void main(String[] args) {
        // 示例用法
        try {
            File file = new File("D:\\webapp\\files\\aimodel\\test.docx"); // 替换为你的文件路径

            String wstAccessKeyID= "AKLTNDg0ZDI0MGM5NDViNGU1YWJlZDMyYzNhMThjYjYzYWM";//从全局配置中读取  火山引擎
            String wstSecretAccessKey= "TmpJNU5tWXdZalU1TURoa05HVXlORGhoT0RBME1EWmhaVFUxWW1RM1lqaw==";//从全局配置中读取  火山引擎
            String content = readPdfByOCR(wstAccessKeyID,wstSecretAccessKey,"http://dev.xiaotunyun.com/apk/test.pdf");
            System.out.println(content);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
