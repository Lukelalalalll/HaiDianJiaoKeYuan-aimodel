package com.zklcsoftware.aimodel.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class FfmpegVideoMattingUtil {

    /*public static void main(String[] args) {
        String inputVideo = "\"C:\\Users\\ASUS\\Desktop\\QQ录屏20250427170227.mp4\"";
        String outputVideo = "C:\\Users\\ASUS\\Desktop\\video.mov";
        String outputImage = "C:\\Users\\ASUS\\Desktop\\img.png";
        try {
            extractBackgroundFrame(inputVideo,outputImage);
            String s = analyzeBackgroundColor(outputImage);
            System.out.println("图片颜色：" + s);
            //performMatting(inputVideo, outputVideo, s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/
    // 截取视频背景帧
    public static boolean extractBackgroundFrame(String inputVideo, String outputImage) throws IOException {
        String[] ffmpegCommand = {
                "ffmpeg",
                "-i", inputVideo,
                "-vframes", "1",
                outputImage
        };
        return executeFFmpegCommand(ffmpegCommand);
    }

    // 基于图片进行抠图
    public static boolean imgMatting(String inputImg, String outputImg, String bgColor) throws IOException {
        // 构建 FFmpeg 命令
        String[] ffmpegCommand = {
                "ffmpeg",
                "-i", inputImg,
                "-vf", "chromakey=" + bgColor + ":0.1:0.1",
                "-pix_fmt", "rgba",
                outputImg
        };
        return executeFFmpegCommand(ffmpegCommand);
    }

    // 基于背景色进行抠图（输出.mov支持透明通道）
    public static boolean performMatting(String inputVideo, String outputVideo, String bgColor) throws IOException {
        //相似度容差（similarity）：值越大，与抠图颜色相近的颜色都会被抠掉。
        //边缘融合度（blend）：值越大，边缘过渡越自然。
        String[] ffmpegCommand = {
                "ffmpeg",
                "-i", inputVideo,
                "-vf", "chromakey="+bgColor+":0.1:0.1",
                "-c:v", "qtrle",
                outputVideo
        };
        return executeFFmpegCommand(ffmpegCommand);
    }

    // 执行 FFmpeg 命令
    public static boolean executeFFmpegCommand(String[] command) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        Process process = processBuilder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }

        try {
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 分析背景帧颜色
    public static String analyzeBackgroundColor(String imagePath) throws IOException {

        File imageFile = new File(imagePath);
        BufferedImage image = ImageIO.read(imageFile);
        int width = image.getWidth();
        int height = image.getHeight();
        long redSum = 0;
        long greenSum = 0;
        long blueSum = 0;
        long pixelCount = width * height;

        // 遍历所有像素并累加 RGB 值
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = new Color(image.getRGB(x, y));
                redSum += color.getRed();
                greenSum += color.getGreen();
                blueSum += color.getBlue();
            }
        }

        // 计算平均 RGB 值
        int avgRed = (int) (redSum / pixelCount);
        int avgGreen = (int) (greenSum / pixelCount);
        int avgBlue = (int) (blueSum / pixelCount);

        // 将 RGB 值转换为十六进制颜色代码
        return String.format("#%02x%02x%02x", avgRed, avgGreen, avgBlue);
    }

}