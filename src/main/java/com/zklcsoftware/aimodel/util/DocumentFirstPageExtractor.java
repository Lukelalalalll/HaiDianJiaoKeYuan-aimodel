package com.zklcsoftware.aimodel.util;

import com.aspose.words.*;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.util.List;

public class DocumentFirstPageExtractor {

    public static void extractFirstPages(String inputDir, String outputDir) {
        File rootOutputDir = new File(outputDir);
        if (!rootOutputDir.exists()) {
            rootOutputDir.mkdirs();
        }

        // 开始递归处理
        processDirectory(new File(inputDir), inputDir, outputDir);
    }

    /**
     * 递归处理目录
     */
    private static void processDirectory(File dir, String inputBaseDir, String outputBaseDir) {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                // 递归处理子目录
                processDirectory(file, inputBaseDir, outputBaseDir);
            } else {
                String fileName = file.getName();
                if (isSupportedFileType(fileName)) {
                    try {
                        // 获取相对路径并构造输出路径
                        String relativePath = getRelativePath(file.getParent(), inputBaseDir);
                        File outputSubDir = new File(outputBaseDir + File.separator + relativePath);
                        if (!outputSubDir.exists()) {
                            outputSubDir.mkdirs(); // 创建对应目录结构
                        }

                        // 提取并保存第一页
                        if (fileName.endsWith(".doc") || fileName.endsWith(".docx")) {
                            //extractWordFirstPage(file, outputSubDir.getAbsolutePath());
                            convertWordToPdfAndExtractFirstPage(file, outputSubDir.getAbsolutePath()); // 新增逻辑
                        } else if (fileName.endsWith(".pdf")) {
                            //extractPdfFirstPage(file, outputSubDir.getAbsolutePath());
                        }
                    } catch (Exception e) {
                        System.err.println("处理文件 " + file.getAbsolutePath() + " 出错: " + e.getMessage());
                    }
                }
            }
        }
    }


    /**
     * 将 Word 文档转换为 PDF 并提取第一页内容
     */
    private static void convertWordToPdfAndExtractFirstPage(File file, String outputDir) throws Exception {
        String fileName = file.getName();
        String baseName = fileName.substring(0, fileName.lastIndexOf('.'));

        // 转换后的 PDF 文件路径
        String pdfFilePath = outputDir + File.separator + baseName + ".pdf";
        String firstPagePath = outputDir + File.separator + fileName + ".pdf";

        // 使用 Aspose.Words 将 Word 转换为 PDF
        Document doc = new Document(file.getAbsolutePath());
        doc.save(pdfFilePath, SaveFormat.PDF);

        // 使用 PDFBox 加载 PDF 并提取第一页
        try (PDDocument document = PDDocument.load(new File(pdfFilePath))) {
            Splitter splitter = new Splitter();
            splitter.setSplitAtPage(1);
            List<PDDocument> splitDocuments = splitter.split(document);

            if (!splitDocuments.isEmpty()) {
                PDDocument firstPageDoc = splitDocuments.get(0);
                firstPageDoc.save(firstPagePath);
                firstPageDoc.close();
            }
        }
        //删掉pdf文件
        new File(pdfFilePath).delete();

    }

    /**
     * 判断是否是支持的文件类型
     */
    private static boolean isSupportedFileType(String fileName) {
        return fileName.endsWith(".doc") || fileName.endsWith(".docx") || fileName.endsWith(".pdf");
    }

    /**
     * 获取相对于输入根目录的路径
     */
    private static String getRelativePath(String currentPath, String baseDir) {
        return currentPath.replace(baseDir, "").replace("\\", "/");
    }

    /**
     * 提取 Word 文档第一页内容
     */
    private static void extractWordFirstPage(File file, String outputDir) throws Exception {
        String fileName = file.getName();
        String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
        String extension = getFileExtension(fileName).toLowerCase();
        String outputPath = outputDir + File.separator + baseName  + extension;

        Document doc = new Document(file.getAbsolutePath());

        // 删除除第一页外的所有节
        while (doc.getSections().getCount() > 1) {
            doc.getSections().removeAt(1);
        }

        // 保存为原格式
        if (extension.equals(".doc")) {
            doc.save(outputPath, SaveFormat.DOC);
        } else if (extension.equals(".docx")) {
            doc.save(outputPath, SaveFormat.DOCX);
        }
    }



    /**
     * 提取 PDF 第一页内容
     */
    private static void extractPdfFirstPage(File file, String outputDir) throws Exception {
        String fileName = file.getName();
        String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
        String outputPath = outputDir + File.separator + baseName + ".pdf";

        try (PDDocument document = PDDocument.load(file)) {
            Splitter splitter = new Splitter();
            splitter.setSplitAtPage(1); // 只提取第一页
            List<PDDocument> splitDocuments = splitter.split(document);

            if (!splitDocuments.isEmpty()) {
                PDDocument firstPageDoc = splitDocuments.get(0);
                firstPageDoc.save(outputPath);
                firstPageDoc.close();
            }
        }
    }

    /**
     * 获取文件扩展名
     */
    private static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex);
    }

    /**
     * 测试入口
     */
    public static void main(String[] args) {
        String inputDirectory = "D:\\海淀教科院新闻资料\\教案";
        String outputDirectory = "D:\\海淀教科院新闻资料\\教案_提取";
        extractFirstPages(inputDirectory, outputDirectory);
    }
}
