package com.zklcsoftware.aimodel.util;

import com.vladsch.flexmark.ast.*;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.options.MutableDataSet;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
@Slf4j
public class MdToWordConverter {

    public static final String HTML_TEMPLATE_PATH = "/webapp/template/md2docx_template.docx";
    /**
     * html 转 word
     */
    public static void convertHtmlToWordByPandoc(String htmlData, String outputFilePath) {

        try {
            // 创建临时 HTML 文件
            Path tempHtmlFile = Files.createTempFile("temp", ".html");
            Files.write(tempHtmlFile, htmlData.getBytes("UTF-8"));

            String pandocCmd="pandoc";
            String templatePath=HTML_TEMPLATE_PATH;
            String os = System.getProperty("os.name").toLowerCase();
            if(os.contains("win")){//如果是windows环境 本地开发
                templatePath="C:/webapp/files/aimodel/md2docx_template.docx";
            }

            // 构建 Pandoc 命令
            List<String> command = new ArrayList<>();
            command.add(pandocCmd);
            command.add(tempHtmlFile.toString());
            command.add("-o");
            command.add(outputFilePath);
            command.add("--reference-doc");
            command.add(templatePath);

            // 执行 Pandoc 命令
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new RuntimeException("Pandoc conversion failed with exit code " + exitCode);
            }

            // 删除临时文件
            Files.deleteIfExists(tempHtmlFile);
        } catch (Exception e) {
            log.error("html转word异常",e);
        }
    }


    public static void convertHtmlToWord(String htmlData, String outputFilePath) {
//        markdown = "";
//        Parser parser = Parser.builder().build();
//        HtmlRenderer renderer = HtmlRenderer.builder().build();
//        String html = renderer.render(parser.parse(markdown));

        try {
            //word内容
            byte[] b = htmlData.getBytes(StandardCharsets.UTF_8);  //这里是必须要设置编码的，不然导出中文就会乱码。
            ByteArrayInputStream bais = new ByteArrayInputStream(b);//将字节数组包装到流中
            // 生成word格式
            POIFSFileSystem poifs = new POIFSFileSystem();
            DirectoryEntry directory = poifs.getRoot();
            DocumentEntry documentEntry = directory.createDocument("WordDocument", bais);

            FileOutputStream fileOutputStream = new FileOutputStream(outputFilePath);
            poifs.writeFilesystem(fileOutputStream);
            //输出文件
            fileOutputStream.close();
            poifs.close();
        } catch (Exception e) {
        }
    }

    /**
     * md 转word
     */
    public static void convertMarkdownToWord(String markdown, String outputFilePath) {
        // 解析 Markdown 内容
        MutableDataSet options = new MutableDataSet();
        Parser parser = Parser.builder(options).build();
        Node document = parser.parse(markdown);

        // 创建 Word 文档
        XWPFDocument doc = new XWPFDocument();

        // 遍历 Markdown 节点并添加到 Word 文档中
        processNode(doc, document);

        // 保存 Word 文档
        try (FileOutputStream out = new FileOutputStream(outputFilePath)) {
            doc.write(out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void processNode(XWPFDocument doc, Node node) {
        if (node instanceof Heading) {
            processHeading(doc, (Heading) node);
        } else if (node instanceof Paragraph) {
            processParagraph(doc, (Paragraph) node);
        } else if (node instanceof BulletList) {
            processBulletList(doc, (BulletList) node);
        } else if (node instanceof OrderedList) {
            processOrderedList(doc, (OrderedList) node);
        }

        // 递归处理子节点
        for (Node child : node.getChildren()) {
            processNode(doc, child);
        }
    }

    private static void processHeading(XWPFDocument doc, Heading heading) {
        int level = heading.getLevel();
        XWPFParagraph paragraph = doc.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setText(heading.getText().toString());
        run.setBold(true);
        switch (level) {
            case 1:
                run.setFontSize(24); // 标题1样式
                break;
            case 2:
                run.setFontSize(20); // 标题2样式
                break;
            case 3:
                run.setFontSize(16); // 标题3样式
                break;
            default:
                run.setFontSize(14); // 默认标题样式
        }
    }

    private static void processParagraph(XWPFDocument doc, Paragraph paragraph) {
        XWPFParagraph p = doc.createParagraph();
        XWPFRun run = p.createRun();
        for (Node child : paragraph.getChildren()) {
            if (child instanceof Text) {
                run.setText(((Text) child).getChars().toString());
            } else if (child instanceof Emphasis) {
                processEmphasis(run, (Emphasis) child);
            } else if (child instanceof StrongEmphasis) {
                processStrongEmphasis(run, (StrongEmphasis) child);
            }
        }
    }

    private static void processEmphasis(XWPFRun run, Emphasis emphasis) {
        XWPFRun italicRun = run.getParagraph().createRun();
        italicRun.setItalic(true);
        for (Node child : emphasis.getChildren()) {
            if (child instanceof Text) {
                italicRun.setText(((Text) child).getChars().toString());
            }
        }
    }

    private static void processStrongEmphasis(XWPFRun run, StrongEmphasis strongEmphasis) {
        XWPFRun boldRun = run.getParagraph().createRun();
        boldRun.setBold(true);
        for (Node child : strongEmphasis.getChildren()) {
            if (child instanceof Text) {
                boldRun.setText(((Text) child).getChars().toString());
            }
        }
    }

    private static void processBulletList(XWPFDocument doc, BulletList bulletList) {
        for (Node listItem : bulletList.getChildren()) {
            if (listItem instanceof ListItem) {
                XWPFParagraph paragraph = doc.createParagraph();
                paragraph.setIndentationLeft(500); // 设置缩进
                XWPFRun run = paragraph.createRun();
                run.setText(((ListItem) listItem).getChars().toString());
                run.setFontSize(12);
            }
        }
    }

    private static void processOrderedList(XWPFDocument doc, OrderedList orderedList) {
        for (Node listItem : orderedList.getChildren()) {
            if (listItem instanceof ListItem) {
                XWPFParagraph paragraph = doc.createParagraph();
                paragraph.setIndentationLeft(500); // 设置缩进
                XWPFRun run = paragraph.createRun();
                run.setText(((ListItem) listItem).getChars().toString());
                run.setFontSize(12);
            }
        }
    }
}
