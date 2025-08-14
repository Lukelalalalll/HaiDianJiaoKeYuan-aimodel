package com.zklcsoftware.aimodel.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * 视频拼接工具类
 */
public class FfmpegVideoConcatenationUtils {
    /*public static void main(String[] args) {
        // 要拼接的视频文件列表
        List<String> inputVideos = new ArrayList<>();
        inputVideos.add("\"C:\\Users\\ASUS\\Desktop\\video2.mp4\"");
        inputVideos.add("\"C:\\Users\\ASUS\\Desktop\\video3.mp4\"");
        String outputVideo = "\"C:\\Users\\ASUS\\Desktop\\video4.mp4\"";
        // 指定转码文件存放的目录
        String transcodedDirectory = "transcoded_videos/";
        // 背景图片文件
        String backgroundImage = "\"C:\\Users\\ASUS\\Desktop\\img3.jpg\"";
        // 调整视频大小的参数
        int videoWidth = 1280;
        int videoHeight = 720;

        try {
//            concatenateVideos(inputVideos, outputVideo);
//            System.out.println("视频拼接完成，输出文件: " + outputVideo);
            // 转码输入视频
            //List<String> transcodedVideos = transcodeVideos(inputVideos, transcodedDirectory);
            // 拼接转码后的视频并叠加到背景图片上
            concatenateVideos(inputVideos, outputVideo, false);
            System.out.println("视频拼接并叠加到背景图片完成，输出文件: " + outputVideo);

            // 示例：不拼接视频，直接叠加单个视频到背景图片
//            String singleVideo = transcodedVideos.get(0);
//            String singleVideo = "C:\\Users\\ASUS\\Desktop\\video.mov";
//            String singleOutputVideo = "C:\\Users\\ASUS\\Desktop\\video3.mp4";
//            overlaySilentVideo(singleVideo, singleOutputVideo, backgroundImage, "342.1", "99.2", "715.3", "500.6", "2");
//            System.out.println("单个视频叠加到背景图片完成，输出文件: " + singleOutputVideo);
        } catch (IOException | InterruptedException e) {
            System.err.println("视频处理过程中出现错误: " + e.getMessage());
        }
    }*/

    /**
     * 转码单个图片（图片分辨率转为720p,不更改原图）
     * @param inputImage
     * @param outputImage
     * @param videoResolution 视频分辨率
     * @throws IOException
     * @throws InterruptedException
     */
    public static void transcodeImage(String inputImage, String outputImage, String videoResolution) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add("ffmpeg");
        command.add("-i");
        command.add(inputImage);
        command.add("-vf");
//        command.add("scale=-2:1080:force_original_aspect_ratio=decrease,pad=ceil(iw/2)*2:ceil(ih/2)*2:(ow-iw)/2:(oh-ih)/2:color=black");
        command.add("scale=-2:"+videoResolution+":force_original_aspect_ratio=decrease,pad=ceil(iw/2)*2:ceil(ih/2)*2:(ow-iw)/2:(oh-ih)/2:color=black,hqdn3d=2:2:1.5:1.5,unsharp=5:5:0.8:3:3:0.4");
        command.add(outputImage);

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        Process process = processBuilder.start();

        // 读取 FFmpeg 的输出信息
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }

        // 等待 FFmpeg 进程结束
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("FFmpeg 进程以非零退出码结束: " + exitCode);
        }
    }

    /**
     * 转码视频
     * @param inputVideos
     * @param transcodedDirectory
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public static List<String> transcodeVideos(List<String> inputVideos, String transcodedDirectory) throws IOException, InterruptedException {
        List<String> transcodedVideos = new ArrayList<>();
        for (int i = 0; i < inputVideos.size(); i++) {
            String inputVideo = inputVideos.get(i);
            // 生成包含指定目录的转码后文件名
            String transcodedVideo = transcodedDirectory + "transcoded_" + i + ".mp4";
            transcodeVideo(inputVideo, transcodedVideo, "1080");
            transcodedVideos.add(transcodedVideo);
        }
        return transcodedVideos;
    }

    /**
     * 转码单个视频
     * @param inputVideo
     * @param outputVideo
     * @throws IOException
     * @throws InterruptedException
     */
    public static void transcodeVideo(String inputVideo, String outputVideo, String videoResolution) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add("ffmpeg");
        command.add("-i");
        command.add(inputVideo);
        // 使用新的滤镜命令
        command.add("-vf");
//        command.add("scale=-2:1080:force_original_aspect_ratio=decrease,pad=ceil(iw/2)*2:ceil(ih/2)*2:(ow-iw)/2:(oh-ih)/2");
        command.add("scale=-2:"+videoResolution+":flags=lanczos:force_original_aspect_ratio=decrease,pad=ceil(iw/2)*2:ceil(ih/2)*2:(ow-iw)/2:(oh-ih)/2,unsharp=5:5:0.8:3:3:0.4,format=yuv420p");
        command.add("-c:v");
        command.add("libx264");
        command.add("-crf");
        command.add("17");
        command.add("-preset");
        command.add("medium");
        // 复制音频流
        command.add("-c:a");
        command.add("copy");
        command.add(outputVideo);

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        Process process = processBuilder.start();

        // 读取 FFmpeg 的输出信息
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }

        // 等待 FFmpeg 进程结束
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("FFmpeg 进程以非零退出码结束: " + exitCode);
        }
    }

    /**
     * 拼接视频带背景
     * @param inputVideos
     * @param outputVideo
     * @param backgroundVideo
     * @throws IOException
     * @throws InterruptedException
     */
    public static void concatenateVideos(List<String> inputVideos, String outputVideo, String backgroundVideo) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add("ffmpeg");
        command.add("-i");
        command.add(backgroundVideo);
        for (String video : inputVideos) {
            command.add("-i");
            command.add(video);
        }
        command.add("-filter_complex");
        StringBuilder filterComplex = new StringBuilder();
        for (int i = 0; i < inputVideos.size(); i++) {
            // 修正后的字符串拼接
            filterComplex.append("[").append(i + 1).append(":v]").append("[").append(i + 1).append(":a]        }");

            filterComplex.append("concat=n=").append(inputVideos.size()).append(":v=1:a=1 [concatenated_v] [concatenated_a];");
            // 添加 overlay 滤镜，将拼接后的视频叠加到背景视频的 (100, 100) 位置
            filterComplex.append("[0:v][concatenated_v]overlay=100:100 [final_v];");
            filterComplex.append("[0:a][concatenated_a]amix=inputs=2 [final_a]");
            command.add(filterComplex.toString());
            command.add("-map");
            command.add("[final_v]");
            command.add("-map");
            command.add("[final_a]");
            command.add(outputVideo);

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();

            // 读取 FFmpeg 的输出信息
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            // 等待 FFmpeg 进程结束
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("FFmpeg 进程以非零退出码结束: " + exitCode);
            }
        }
    }

    /**
     * 叠加单个视频(图片中叠加视频，可以处理图片位置，大小)
     * 两秒静音视频
     * @param inputVideo
     * @param outputVideo
     * @param backgroundImage
     * @param x
     * @param y
     * @param width
     * @param height
     * @param second
     * @throws IOException
     * @throws InterruptedException
     */
    public static void overlaySilentVideo(String inputVideo, String outputVideo, String backgroundImage, String x, String y, String width, String height, String second) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add("ffmpeg");
        command.add("-loop");
        command.add("1");
        command.add("-i");
        command.add(backgroundImage);
        command.add("-i");
        command.add(inputVideo);
        command.add("-filter_complex");
        StringBuilder filterComplex = new StringBuilder();
        // 调整视频大小
        filterComplex.append("[1:v]scale=" + width + ":" + height + " [scaled_v];");
        // 添加 overlay 滤镜，将单个视频叠加到背景图片的指定位置
        filterComplex.append("[0:v][scaled_v]overlay=").append(x).append(":").append(y).append(" [final_v];");
        // 生成静音音频流
        filterComplex.append("anullsrc=channel_layout=stereo:sample_rate=44100 [silent_a];");
        // 设置静音音频流的时长为2秒
        filterComplex.append("[silent_a]atrim=duration="+second+" [final_a]");
        command.add(filterComplex.toString());
        command.add("-map");
        command.add("[final_v]");
        command.add("-map");
        command.add("[final_a]");
        command.add("-t");
        command.add(second);
        command.add(outputVideo);

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        Process process = processBuilder.start();

        // 读取 FFmpeg 的输出信息
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }

        // 等待 FFmpeg 进程结束
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("FFmpeg 进程以非零退出码结束: " + exitCode);
        }
    }

    /**
     * 叠加单个视频(图片中叠加视频，可以处理图片位置，大小)
     * 当前方法输出的视频中文字可能会减少
     * @param inputVideo
     * @param outputVideo
     * @param backgroundImage
     * @param x
     * @param y
     * @param width
     * @param height
     * @throws IOException
     * @throws InterruptedException
     */
    public static void overlaySingleVideo(String inputVideo, String outputVideo, String backgroundImage, String x, String y, String width, String height) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add("ffmpeg");
        command.add("-loop");
        command.add("1");
        command.add("-i");
        command.add(backgroundImage);
        command.add("-i");
        command.add(inputVideo);
        command.add("-filter_complex");
        StringBuilder filterComplex = new StringBuilder();
        // 调整背景图片大小
//        filterComplex.append("[0:v]scale=" + width + ":" + height + " [scaled_bg];");
        // 调整视频大小
        filterComplex.append("[1:v]scale=" + width + ":" + height + " [scaled_v];");
        // 添加 overlay 滤镜，将单个视频叠加到背景图片的指定位置
//        filterComplex.append("[scaled_bg][scaled_v]overlay=").append(x).append(":").append(y).append(" [final_v];");
        filterComplex.append("[0:v][scaled_v]overlay=").append(x).append(":").append(y).append(" [final_v];");
        // 假设背景图片无音频流，只使用视频的音频流
        filterComplex.append("[1:a]anull [final_a]");
        command.add(filterComplex.toString());
        command.add("-map");
        command.add("[final_v]");
        command.add("-map");
        command.add("[final_a]");
        command.add("-t");
        // 获取单个视频的时长
        long duration = getVideoDuration(inputVideo);
        command.add(String.valueOf(duration));
        command.add(outputVideo);

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        Process process = processBuilder.start();

        // 读取 FFmpeg 的输出信息
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }

        // 等待 FFmpeg 进程结束
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("FFmpeg 进程以非零退出码结束: " + exitCode);
        }
    }

    private static long getVideoDuration(String video) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add("ffmpeg");
        command.add("-i");
        command.add(video);
        command.add("-f");
        command.add("null");
        command.add("-");

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        Process process = processBuilder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        String line;
        long duration = 0;
        while ((line = reader.readLine()) != null) {
            if (line.contains("Duration:")) {
                int startIndex = line.indexOf("Duration: ") + 10;
                int endIndex = line.indexOf(",", startIndex);
                String durationStr = line.substring(startIndex, endIndex);
                String[] parts = durationStr.split(":");
                duration = (long) (Double.parseDouble(parts[0]) * 3600 + Double.parseDouble(parts[1]) * 60 + Double.parseDouble(parts[2]));
                break;
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("FFmpeg 进程以非零退出码结束: " + exitCode);
        }

        return duration;
    }

    /**
     * 将视频和图片进行拼接(图片中叠加视频，可以处理图片位置，大小)
     * @param inputVideoPath
     * @param outputPath
     * @param inputImagePath
     * @param x
     * @param y
     * @param width
     * @param height
     * @throws IOException
     * @throws InterruptedException
     */
    public static void combineVideoAndImage(String inputVideoPath, String outputPath, String inputImagePath, String x, String y, String width, String height) throws IOException, InterruptedException {
        // 构建 FFmpeg 命令
        ProcessBuilder processBuilder = new ProcessBuilder(
                "ffmpeg",
                "-i", inputVideoPath,
                "-i", inputImagePath,
                "-filter_complex",
                "[0:v]scale=" + width + ":" + height + "[scaled];[1:v][scaled]overlay=" + x + ":" + y,
                "-c:a", "copy",
                outputPath
        );

        // 启动进程
        Process process = processBuilder.start();

        // 读取 FFmpeg 输出
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }

        // 等待进程结束
        int exitCode = process.waitFor();
        if (exitCode == 0) {
            System.out.println("视频和图片合成成功！");
        } else {
            System.out.println("合成失败，退出码: " + exitCode);
        }
    }

    /**
     * 拼接视频（将多个片段视频合成一个完整视频）
     * @param inputVideos
     * @param outputVideo
     * @param addFadeEffect
     * @throws IOException
     * @throws InterruptedException
     */
    public static void concatenateVideos(List<String> inputVideos, String outputVideo, Boolean addFadeEffect) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add("ffmpeg");
        command.add("-threads");
        command.add("1");

        if(!addFadeEffect){//不添加淡入淡出效果
            for (String video : inputVideos) {
                command.add("-i");
                command.add(video);
            }
            command.add("-filter_complex");
            StringBuilder filterComplex = new StringBuilder();
            for (int i = 0; i < inputVideos.size(); i++) {
                // 修正后的字符串拼接
                filterComplex.append("[").append(i).append(":v]").append("[").append(i).append(":a]");
            }
            filterComplex.append("concat=n=").append(inputVideos.size()).append(":v=1:a=1 [v] [a]");
            command.add(filterComplex.toString());
            command.add("-map");
            command.add("[v]");
            command.add("-map");
            command.add("[a]");
            command.add(outputVideo);
        }else{
            // 为每个视频添加渐入渐出效果
            StringBuilder filter = new StringBuilder();
            for (int i = 0; i < inputVideos.size(); i++) {
                command.add("-i");
                command.add(inputVideos.get(i));
                double duration = getVideoDuration(inputVideos.get(i));
                filter.append("[")
                        .append(i)
                        .append(":v]fade=in:st=0:d=0.5,fade=out:st=")
                        .append(duration)
                        .append(":d=2[v")
                        .append(i)
                        .append("];[")
                        .append(i)
                        .append(":a]afade=in:st=0:d=0.5,afade=out:st=")
                        .append(duration)
                        .append(":d=2[a")
                        .append(i)
                        .append("];");
            }

            // 拼接处理后的视频和音频
            for (int i = 0; i < inputVideos.size(); i++) {
                filter.append("[v").append(i).append("][a").append(i).append("]");
            }
            filter.append("concat=n=").append(inputVideos.size()).append(":v=1:a=1 [v] [a]");

            command.add("-filter_complex");
            command.add(filter.toString());
            command.add("-map");
            command.add("[v]");
            command.add("-map");
            command.add("[a]");
            command.add(outputVideo);
        }

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        Process process = processBuilder.start();

        // 读取 FFmpeg 的输出信息
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }

        // 等待 FFmpeg 进程结束
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("FFmpeg 进程以非零退出码结束: " + exitCode);
        }
    }
}    