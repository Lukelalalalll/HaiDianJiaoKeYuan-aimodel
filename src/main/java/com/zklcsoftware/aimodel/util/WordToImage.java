package com.zklcsoftware.aimodel.util;

import com.aspose.words.Document;
import com.aspose.words.SaveFormat;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class WordToImage {

    public static void main(String[] args) {

        String inputDir = "D:\\海淀教科院新闻资料\\教案";
        String outputDir = "D:\\海淀教科院新闻资料\\教案_提取_图片";

        processDirectory(new File(inputDir), inputDir, outputDir);
    }

    /**
     * 递归处理目录
     */
    private static void processDirectory(File dir, String inputBaseDir, String outputBaseDir) {
        if (!dir.exists()) return;

        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                processDirectory(file, inputBaseDir, outputBaseDir);
            } else {
                String fileName = file.getName();
                if (isSupportedFileType(fileName)) {
                    try {
                        // 构建输出子目录
                        String relativePath = getRelativePath(file.getParent(), inputBaseDir);
                        File outSubDir = new File(outputBaseDir + File.separator + relativePath);
                        if (!outSubDir.exists()) {
                            outSubDir.mkdirs();
                        }

                        // 提取并保存第一页图片
                        extractFirstPageAsImage(file, outSubDir.getAbsolutePath());
                    } catch (Exception e) {
                        System.err.println("处理文件失败: " + file.getAbsolutePath());
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 判断是否支持的文件类型
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
     * 提取文档第一页为图片
     */
    private static void extractFirstPageAsImage(File file, String outputDir) throws Exception {
        String fileName = file.getName();
        String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
        String outputImagePath = outputDir + File.separator + baseName + ".png";

        File tempPdfFile = null;
        boolean isTemp = false;

        // 如果是 Word 文件，先转成 PDF
        if (fileName.endsWith(".doc") || fileName.endsWith(".docx")) {
            tempPdfFile = File.createTempFile("doc2pdf-", ".pdf");
            tempPdfFile.deleteOnExit();
            Document doc = new Document(file.getAbsolutePath());
            doc.save(tempPdfFile.getAbsolutePath(), SaveFormat.PDF);
            isTemp = true;
        }

        // 加载 PDF 并渲染第一页为图片
        try (PDDocument document = PDDocument.load(isTemp ? tempPdfFile : file)) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            BufferedImage image = pdfRenderer.renderImageWithDPI(0, 300, ImageType.RGB); // 第一页，300 DPI
            ImageIO.write(image, "PNG", new File(outputImagePath));
        }


        // 如果是 Word 文件，先转成 PDF  处理完毕后 删掉pdf
        if (fileName.endsWith(".doc") || fileName.endsWith(".docx")) {
            tempPdfFile.delete();
        }

    }
}
