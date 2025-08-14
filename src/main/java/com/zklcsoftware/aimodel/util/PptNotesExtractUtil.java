package com.zklcsoftware.aimodel.util;

import org.apache.poi.hslf.usermodel.*;
import org.apache.poi.xslf.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author 杨洁
 * @ClassName PptNotesExtractUtil.java
 * @Description ppt备注提取Util
 * @createTime 2025-04-14 10:42
 **/
public class PptNotesExtractUtil {

    /**
     * 提取所有幻灯片的备注
     * @param file
     */
    public static void PptNotesExtract(File file) {

        try (FileInputStream fis = new FileInputStream(file);
             HSLFSlideShow ppt = new HSLFSlideShow(fis)) {

            // 遍历所有幻灯片
            for (HSLFSlide slide : ppt.getSlides()) {
                // 获取当前幻灯片的备注页
                HSLFNotes notes = slide.getNotes();
                if (notes == null) continue;

                // 提取备注页中的所有文本
                StringBuilder noteText = new StringBuilder();
                for (HSLFShape shape : notes.getShapes()) {
                    if (shape instanceof HSLFTextShape) {
                        HSLFTextShape textShape = (HSLFTextShape) shape;
                        noteText.append(textShape.getText()).append("\n");
                    }
                }

                System.out.println("Slide " + slide.getSlideNumber() + " 备注:");
                System.out.println(noteText.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 提取所有幻灯片的备注
     * @param file
     * @return
     */
    public static void PptxNotesExtract(File file) {

        try (FileInputStream fis = new FileInputStream(file);
             XMLSlideShow pptx = new XMLSlideShow(fis)) {

            // 遍历所有幻灯片
            for (XSLFSlide slide : pptx.getSlides()) {
                // 获取当前幻灯片的备注页
                XSLFNotes notes = slide.getNotes();
                if (notes == null) continue;

                // 提取备注页中的所有文本
                StringBuilder noteText = new StringBuilder();
                for (XSLFShape shape : notes) {
                    if (shape instanceof XSLFTextShape) {
                        XSLFTextShape textShape = (XSLFTextShape) shape;
                        //noteText.append(textShape.getText()).append("\n");
                        noteText.append(textShape.getText());
                    }
                }

                System.out.println("Slide " + slide.getSlideNumber() + " 备注:");
                System.out.println(noteText.toString());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 提取单个幻灯片的备注
     * @param file
     * @param slideNumber
     * @return
     */
    public static String onePagePptNotesExtract(File file, Integer slideNumber) {

        // 提取备注页中的所有文本
        StringBuilder noteText = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(file);
             HSLFSlideShow ppt = new HSLFSlideShow(fis)) {
            HSLFSlide slide = ppt.getSlides().get(slideNumber);
            // 获取当前幻灯片的备注页
            HSLFNotes notes = slide.getNotes();
            if (notes == null) return noteText.toString();
            for (HSLFShape shape : notes.getShapes()) {
                if (shape instanceof HSLFTextShape) {
                    HSLFTextShape textShape = (HSLFTextShape) shape;
                    noteText.append(textShape.getText()).append("\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return noteText.toString();
    }

    /**
     * 提取单个幻灯片的备注
     * @param file
     * @param slideNumber
     * @return
     */
    public static String onePagePptxNotesExtract(File file, Integer slideNumber) {

        // 提取备注页中的所有文本
        StringBuilder noteText = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(file);
             XMLSlideShow pptx = new XMLSlideShow(fis)) {

            XSLFSlide slide = pptx.getSlides().get(slideNumber);
            // 获取当前幻灯片的备注页
            XSLFNotes notes = slide.getNotes();
            if (notes == null) return noteText.toString();
            for (XSLFShape shape : notes) {
                if (shape instanceof XSLFTextShape) {
                    XSLFTextShape textShape = (XSLFTextShape) shape;
//                    noteText.append(textShape.getText()).append("\n");
                    noteText.append(textShape.getText());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return noteText.toString();
    }

    /**
     * 获取PPT或PPTX文件的幻灯片数量
     * @param filePath
     * @return
     * @throws IOException
     */
    public static int getSlideCount(String filePath) throws IOException {
        FileInputStream fis = new FileInputStream(filePath);
        int slideCount = 0;

        try {
            if (filePath.toLowerCase().endsWith(".ppt")) {
                // 处理PPT文件（二进制格式）
                HSLFSlideShow ppt = new HSLFSlideShow(fis);
                slideCount = ppt.getSlides().size();
            } else if (filePath.toLowerCase().endsWith(".pptx")) {
                // 处理PPTX文件（XML格式）
                XMLSlideShow pptx = new XMLSlideShow(fis);
                slideCount = pptx.getSlides().size();
            } else {
                throw new IllegalArgumentException("不支持的文件格式，仅支持 .ppt 和 .pptx");
            }
        } finally {
            fis.close();
        }

        return slideCount;
    }
}
