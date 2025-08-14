package com.zklcsoftware;

import com.alibaba.fastjson.JSONObject;
import com.zklcsoftware.aimodel.domain.*;
import com.zklcsoftware.aimodel.service.*;
import com.zklcsoftware.aimodel.util.ConstantUtil;
import com.zklcsoftware.aimodel.util.FfmpegVideoConcatenationUtils;
import com.zklcsoftware.aimodel.util.FfmpegVideoMattingUtil;
import com.zklcsoftware.basic.util.DateUtil;
import com.zklcsoftware.common.dto.OperaResult;
import com.zklcsoftware.common.web.ExtBaseController;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;


@Component
@Slf4j
public class ScheduleService extends ExtBaseController{

	@Autowired
	private TAiShuzirenService tAiShuzirenService;
	@Autowired
	TShuzirenServiceService tShuzirenServiceService;
	@Autowired
	TAiShuzirenXingxiangService tAiShuzirenXingxiangService;
	@Autowired
	private TAiShuzirenPptService tAiShuzirenPptService;
	@Autowired
	TAiMicroCourseService aiMicroCourseService;
	@Value("${uploadfiledir.uploadFilePath}")
	private String uploadFilePath;//服务器路径
	@Value("${uploadfiledir.uploadFileUrl}")
	private String uploadFileUrl;//文件封存的http地址

	@Autowired
	@Qualifier("jdbcTemplate")
	private JdbcTemplate jdbcTemplate;

	@Value("${sys.scheduleServiceFlag}")
	private Boolean scheduleServiceFlag;//是否执行定时任务类

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private Integer count0 = 1;
    private Integer count1 = 1;
    private Integer count2 = 1;
    
    @Scheduled(fixedRateString = "${timer.rate}")
    public void reportCurrentTime() throws InterruptedException {
        System.out.println(String.format("rate第%s次执行，当前时间为：%s", count0++, dateFormat.format(new Date())));
    }
    
    /*@Scheduled(fixedDelayString = "${timer.delay}")
    public void reportCurrentTimeAfterSleep() throws InterruptedException {
        System.out.println(String.format("delay第%s次执行，当前时间为：%s", count1++, dateFormat.format(new Date())));
    }*/

	/**
	 * 定时更新数字人状态
	 * @throws InterruptedException
	 * @throws ParseException
	 */
	@SuppressWarnings("deprecation")
	@Scheduled(cron = "${timer.editShuziren}")
	public void editShuziren(){

		//scheduleServiceFlag不为空且==false时，不执行定时任务
		if(scheduleServiceFlag!=null && !scheduleServiceFlag){
			return;
		}

		log.info("定时器: 修改更新数字人状态  ---- start----");
		String token = ConstantUtil.sysConfig.get("digital_person_token");//获取数字人token
		//查询活动学校的状态为发布中
		List<TAiShuziren> szrList = tAiShuzirenService.findByZtAndIsValid(1, 1);
		//查询已启用的服务器
		List<TShuzirenService> serviceList = tShuzirenServiceService.findByStatus(1);
		if(szrList.size() > 0){
			for (TAiShuziren szr : szrList) {
				String ip = szr.getServerIp();
				if(StringUtils.isBlank(ip)){
					continue;
				}
				//已启用的服务器是否包含该服务器，如果不包含，则跳过该服务器
				boolean match = serviceList.stream().anyMatch(service -> service.getServiceAddress().equals(ip));
				if(!match){
					continue;
				}
				try {
					CloseableHttpClient queryHttpClient = HttpClients.createDefault();
					// 使用 URIBuilder 构建带有查询参数的 URL
					URIBuilder uriBuilder = new URIBuilder("http://"+ip+":5000/v1/query");
					uriBuilder.addParameter("token", token);
					uriBuilder.addParameter("id", szr.getCode());
					HttpGet queryGet = new HttpGet(uriBuilder.build());
					// 执行查询请求
					CloseableHttpResponse queryResponse = queryHttpClient.execute(queryGet);
					// 获取查询结果
					String queryResult = EntityUtils.toString(queryResponse.getEntity());
					// 解析查询结果
					JSONObject jsonObject = JSONObject.parseObject(queryResult);
					String result = (String) jsonObject.get("result");

					String filePath = "ai/digitalPerson/" + DateUtil.formatDateByFormat(new Date(), "yyyy-MM-dd") + "/";
					if(result.equals("finished")){
						szr.setZt(2);

						//返回信息
						String resultMsg = (String) jsonObject.get("result_msg");
						szr.setProgressDec(resultMsg);

						//进度
						Integer progress = (Integer) jsonObject.get("progress");
						szr.setProgress(progress);

						//合成结果
						String videoUrl = (String) jsonObject.get("video_url");
						URI uri = new URI(videoUrl);
						String videoName = uri.getPath().substring(uri.getPath().lastIndexOf("/") + 1);
						File file1 = new File(uploadFilePath + filePath + videoName);
						file1.getParentFile().mkdirs();// 创建父目录（如果不存在）
						FileUtils.copyURLToFile(uri.toURL(), file1);
						szr.setShuzirenFile(filePath + videoName);

					}else if(result.equals("process")){
						String resultMsg = (String) jsonObject.get("result_msg");
						szr.setProgressDec(resultMsg);
					}else if(result.equals("failed")){
						String resultMsg = (String) jsonObject.get("result_msg");
						szr.setProgressDec(resultMsg);
						szr.setZt(3);
					}
				}catch (Exception e){
					// 设置失败状态
					szr.setZt(3);
					//szr.setProgressDec("处理失败: " + e.getMessage()); // 记录异常信息
					//e.printStackTrace(); // 打印堆栈信息以便调试
					szr.setProgressDec("处理失败"); // 记录异常信息
				}
			}
		}
		tAiShuzirenService.save(szrList);
		log.info("定时器: 修改更新数字人状态  ---- end----  ");
	}

	/**
	 * 定时更新数字人形象状态
	 * @throws InterruptedException
	 * @throws ParseException
	 */
	@SuppressWarnings("deprecation")
	@Scheduled(cron = "${timer.editShuzirenXingxiang}")
	public void editShuzirenXingxiang(){

		//scheduleServiceFlag不为空且==false时，不执行定时任务
		if(scheduleServiceFlag!=null && !scheduleServiceFlag){
			return;
		}

		log.info("定时器: 修改更新数字人形象状态  ---- start----");

		String token = ConstantUtil.sysConfig.get("digital_person_token");//获取数字人token
		//查询已启用的服务器
		List<TShuzirenService> serviceList = tShuzirenServiceService.findByStatus(1);
		//查询生成中的形象
		List<TAiShuzirenXingxiang> xxList = tAiShuzirenXingxiangService.findByVideoZtAndIsDel(1, ConstantUtil.IS_DEL_0);
		if(xxList.size() > 0){
			for (TAiShuzirenXingxiang xx : xxList) {
				String ip = xx.getServerIp();
				if(StringUtils.isBlank(ip)){
					continue;
				}
				//已启用的服务器是否包含该服务器，如果不包含，则跳过该服务器
				boolean match = serviceList.stream().anyMatch(service -> service.getServiceAddress().equals(ip));
				if(!match){
					continue;
				}
				try {
					CloseableHttpClient queryHttpClient = HttpClients.createDefault();
					// 使用 URIBuilder 构建带有查询参数的 URL
					URIBuilder uriBuilder = new URIBuilder("http://"+ip+":5000/v1/query");
					uriBuilder.addParameter("token", token);
					uriBuilder.addParameter("id", xx.getVideoId());
					HttpGet queryGet = new HttpGet(uriBuilder.build());
					// 执行查询请求
					CloseableHttpResponse queryResponse = queryHttpClient.execute(queryGet);
					// 获取查询结果
					String queryResult = EntityUtils.toString(queryResponse.getEntity());
					// 解析查询结果
					JSONObject jsonObject = JSONObject.parseObject(queryResult);
					String result = (String) jsonObject.get("result");

					String filePath = "ai/digitalPerson/" + DateUtil.formatDateByFormat(new Date(), "yyyy-MM-dd") + "/";
					if(result.equals("finished")){
						xx.setVideoZt(2);

						//返回信息
						String resultMsg = (String) jsonObject.get("result_msg");
						xx.setProgressDec(resultMsg);

						//进度
						Integer progress = (Integer) jsonObject.get("progress");
						xx.setProgress(progress);

						//训练路径
						String referenceAudio = (String) jsonObject.get("reference_audio");
						URI uri1 = new URI(referenceAudio);
						String referenceAudioName = uri1.getPath().substring(uri1.getPath().lastIndexOf("/") + 1);
						File file2 = new File(uploadFilePath + filePath + referenceAudioName);
						file2.getParentFile().mkdirs();// 创建父目录（如果不存在）
						FileUtils.copyURLToFile(uri1.toURL(), file2);
						xx.setReferenceAudio(filePath+ file2.getName());

						//训练文本
						String referenceText = (String) jsonObject.get("reference_text");
						xx.setReferenceText(referenceText);

						//静音视频
						String silentVideo = (String) jsonObject.get("silent_video");
						URI uri2 = new URI(silentVideo);
						String silentVideoName = uri2.getPath().substring(uri2.getPath().lastIndexOf("/") + 1);
						File file3 = new File(uploadFilePath + filePath + silentVideoName);
						file3.getParentFile().mkdirs();// 创建父目录（如果不存在）
						FileUtils.copyURLToFile(uri2.toURL(), file3);
						xx.setSilentVideo(filePath + file3.getName());

					}else if(result.equals("process")){
						String resultMsg = (String) jsonObject.get("result_msg");
						xx.setProgressDec(resultMsg);
					}else if(result.equals("failed")){
						String resultMsg = (String) jsonObject.get("result_msg");
						xx.setProgressDec(resultMsg);
						xx.setVideoZt(3);
					}
				}catch (Exception e){
					// 设置失败状态
					xx.setVideoZt(3);
					//szr.setProgressDec("处理失败: " + e.getMessage()); // 记录异常信息
					e.printStackTrace(); // 打印堆栈信息以便调试
					xx.setProgressDec("处理失败"); // 记录异常信息
				}
			}
		}
		tAiShuzirenXingxiangService.save(xxList);
		log.info("定时器: 修改更新数字人形象状态  ---- end----  ");
	}

	/**
	 * 定时更新数字人ppt状态
	 * @throws InterruptedException
	 * @throws ParseException
	 */
	@SuppressWarnings("deprecation")
	@Scheduled(cron = "${timer.editShuzirenPpt}")
	public void editShuzirenPpt(){

		//scheduleServiceFlag不为空且==false时，不执行定时任务
		if(scheduleServiceFlag!=null && !scheduleServiceFlag){
			return;
		}

		log.info("定时器: 修改更新数字人ppt状态  ---- start----");

		String token = ConstantUtil.sysConfig.get("digital_person_token");//数字人token
		String videoResolution = ConstantUtil.sysConfig.get("video_resolution");//视频分辨率
		String filePath = "ai/microCourse/" + DateUtil.formatDateByFormat(new Date(), "yyyy-MM-dd") + "/";
		//查询已启用的服务器
		List<TShuzirenService> serviceList = tShuzirenServiceService.findByStatus(1);
		//查询生成中的形象
		List<TAiShuzirenPpt> pptList = tAiShuzirenPptService.findByVideoZtAndIsDel(1, ConstantUtil.IS_DEL_0);
		if(pptList.size() > 0){
			for (TAiShuzirenPpt ppt : pptList) {
				String ip = ppt.getServerIp();

				TAiShuzirenXingxiang xingxiang = tAiShuzirenXingxiangService.findById(ppt.getXingxiangId());
				if(xingxiang.getVideoZt() != 2){
					continue;
				}
				//ip为空调取生成视频接口获取视频id,不为空则根据视频id调取查询接口获取视频信息
				if(StringUtils.isNotBlank(ppt.getContent()) && StringUtils.isBlank(ip)){

					//获取数字人服务ip
					TShuzirenService serviceIp = tShuzirenServiceService.getMinServiceIp();
					if(serviceIp ==null){
						continue;
					}
					ppt.setServerIp(serviceIp.getServiceAddress());

					try {
						//音频文件
						File file1 = new File(uploadFilePath + xingxiang.getReferenceAudio());
						String fileName1 =  URLEncoder.encode(file1.getPath(), "UTF-8");// 编码文件名以避免乱码
						String filetype1 = file1.getName().substring(file1.getName().lastIndexOf(".") + 1);

						//无声视频文件
						File file2 = new File(uploadFilePath + xingxiang.getSilentVideo());
						String fileName2 =  URLEncoder.encode(file2.getPath(), "UTF-8");// 编码文件名以避免乱码
						String filetype2 = file2.getName().substring(file2.getName().lastIndexOf(".") + 1);

						OkHttpClient client = new OkHttpClient();
						// 创建文件请求体
						okhttp3.RequestBody fileBody1 = okhttp3.RequestBody.create(MediaType.parse("application/octet-stream"), file1);
						okhttp3.RequestBody fileBody2 = okhttp3.RequestBody.create(MediaType.parse("application/octet-stream"), file2);

						// 创建多部分请求体
						okhttp3.RequestBody requestBody = new MultipartBody.Builder()
								.setType(MultipartBody.FORM)
								.addFormDataPart("reference_audio", fileName1 + "." + filetype1, fileBody1)
								.addFormDataPart("reference_text", xingxiang.getReferenceText())
								.addFormDataPart("silent_video", fileName2 + "." + filetype2, fileBody2)
								.addFormDataPart("content", ppt.getContent())
								.addFormDataPart("token", token)
								.build();

						// 创建请求
						Request request = new Request.Builder()
								.url("http://"+ serviceIp.getServiceAddress() +":5000/v1/genvideo")
								.post(requestBody)
								.build();
						// 打印请求头
						System.out.println("Request URL: " + request.url());
						System.out.println("Request Headers: " + request.headers());

						Response response = client.newCall(request).execute();
						if (!response.isSuccessful()) {
							throw new IOException("上传文件失败：" + response);
						}
						// 将响应体内容读取到一个字符串变量中
						String responseBody = response.body() != null ? response.body().string() : "";
						// 打印响应体内容
						System.out.println(responseBody);
						// 检查响应体是否为空，并解析为 JSON 对象
						JSONObject jsonObject = new JSONObject();
						if (StringUtils.isNotEmpty(responseBody)) {
							jsonObject = JSONObject.parseObject(responseBody);
							String resultMsg = (String) jsonObject.get("result_msg");
							if(jsonObject.get("result").equals("success")){
								ppt.setVideoId((String) jsonObject.get("id"));
								ppt.setProgressDec(resultMsg);
							}else{
								ppt.setVideoZt(3);
								ppt.setProgressDec(resultMsg);
							}
						}
						tAiShuzirenPptService.save(ppt);
					} catch (Exception e) {
						e.printStackTrace();
						ppt.setVideoZt(3);
						tAiShuzirenPptService.save(ppt);
					}
				}else{
					//内容为空生成两秒静音视频
					if(StringUtils.isBlank(ppt.getContent()) && StringUtils.isNotBlank(ppt.getXingxiangId())){
						try {
							//最终合成视频.mp4
							String fileName4 = UUID.randomUUID().toString().replaceAll("-", "") + ".mp4";
							File file4 = new File(uploadFilePath + filePath + fileName4);
							file4.getParentFile().mkdirs();

							//将ppt图片转为背景图片处理-1080p
							String fileName5 = UUID.randomUUID().toString().replaceAll("-", "") + ".jpg";
							File file5 = new File(uploadFilePath + filePath + fileName5);
							FfmpegVideoConcatenationUtils.transcodeImage(ppt.getPptImg(), file5.getPath(), videoResolution);

							//原始视频抠图.mov
							String fileName6 = UUID.randomUUID().toString().replaceAll("-", "") + ".mov";
							File file6 = new File(uploadFilePath + filePath + fileName6);
							file6.getParentFile().mkdirs();
							//分析背景祯颜色
							String bgColor = FfmpegVideoMattingUtil.analyzeBackgroundColor(uploadFilePath + xingxiang.getCover());

							//视频转换1080p
							String fileName7 = UUID.randomUUID().toString().replaceAll("-", "") + ".mp4";
							File file7 = new File(uploadFilePath + filePath + fileName7);
							file7.getParentFile().mkdirs();
							FfmpegVideoConcatenationUtils.transcodeVideo(uploadFilePath + xingxiang.getSilentVideo(),file7.getPath(), videoResolution);
							//视频抠图
							FfmpegVideoMattingUtil.performMatting(file7.getPath(), file6.getPath(), bgColor);

							String[] str = ppt.getSzrWz().split(",");//数字人位置信息
							String[] str1 = ppt.getSzrDx().split(",");//数字人缩放信息

							//视频拼接
							FfmpegVideoConcatenationUtils.overlaySilentVideo(file6.getPath(), file4.getPath(), file5.getPath(), str[0], str[1],str1[0], str1[1], "2");
							ppt.setVideoProceUrl(filePath + fileName4);
							ppt.setVideoZt(2);
							ppt.setProgressDec("视频合成完成");
							tAiShuzirenPptService.save(ppt);
							file5.delete();
							file6.delete();
							file7.delete();
						}catch (Exception e){
							e.printStackTrace();
							ppt.setVideoZt(3);
							tAiShuzirenPptService.save(ppt);
						}
					}else{
						//已启用的服务器是否包含该服务器，如果不包含，则跳过该服务器
						boolean match = serviceList.stream().anyMatch(service -> service.getServiceAddress().equals(ip));
						if(!match){
							continue;
						}

						int retryCount = 0;//重试次数
						boolean success = false;//是否成功
						while (retryCount < 5 && !success) {
							retryCount++;
							try {
								CloseableHttpClient queryHttpClient = HttpClients.createDefault();
								// 使用 URIBuilder 构建带有查询参数的 URL
								URIBuilder uriBuilder = new URIBuilder("http://"+ip+":5000/v1/query");
								uriBuilder.addParameter("token", token);
								uriBuilder.addParameter("id", ppt.getVideoId());
								HttpGet queryGet = new HttpGet(uriBuilder.build());
								// 执行查询请求
								CloseableHttpResponse queryResponse = queryHttpClient.execute(queryGet);
								// 获取查询结果
								String queryResult = EntityUtils.toString(queryResponse.getEntity());
								// 解析查询结果
								JSONObject jsonObject = JSONObject.parseObject(queryResult);
								String result = (String) jsonObject.get("result");

								if(result.equals("finished")){
									ppt.setVideoZt(2);

									//返回信息
									String resultMsg = (String) jsonObject.get("result_msg");
									ppt.setProgressDec(resultMsg);

									//进度
									Integer progress = (Integer) jsonObject.get("progress");
									ppt.setProgress(progress);

									//合成视频
									String videoUrl = (String) jsonObject.get("video_url");
									URI uri = new URI(videoUrl);
									String videoName = uri.getPath().substring(uri.getPath().lastIndexOf("/") + 1);
									File file1 = new File(uploadFilePath + filePath + videoName);
									file1.getParentFile().mkdirs();// 创建父目录（如果不存在）
									FileUtils.copyURLToFile(uri.toURL(), file1);
									ppt.setVideoUrl(filePath + videoName);

									//封面
									String fileName2 = UUID.randomUUID().toString().replaceAll("-", "") + ".png";
									File file2 = new File(uploadFilePath + filePath + fileName2);
									// 提取视频背景帧
									FfmpegVideoMattingUtil.extractBackgroundFrame(file1.getPath(), file2.getPath());

									//抠图后保存的图片.png
									String fileName3 = UUID.randomUUID().toString().replaceAll("-", "") + ".png";
									File file3 = new File(uploadFilePath + filePath + fileName3);
									file3.getParentFile().mkdirs();// 创建父目录（如果不存在）
									// 分析背景帧颜色
									String bgColor = FfmpegVideoMattingUtil.analyzeBackgroundColor(file2.getPath());
									// 基于背景色进行抠图
									boolean b = FfmpegVideoMattingUtil.imgMatting(file2.getPath(), file3.getPath(), bgColor);
									if(b){
										ppt.setCover(filePath + file3.getName());
										file2.delete();
									}

									//将ppt图片转为背景图片处理-720p
									String fileName4 = UUID.randomUUID().toString().replaceAll("-", "") + ".jpg";
									File file4 = new File(uploadFilePath + filePath + fileName4);
									FfmpegVideoConcatenationUtils.transcodeImage(ppt.getPptImg(), file4.getPath(), videoResolution);


									//视频转换1080p
									String fileName8 = UUID.randomUUID().toString().replaceAll("-", "") + ".mp4";
									File file8 = new File(uploadFilePath + filePath + fileName8);
									file8.getParentFile().mkdirs();
									FfmpegVideoConcatenationUtils.transcodeVideo(file1.getPath(),file8.getPath(), videoResolution);
									//原始视频抠图.mov
									String fileName5 = UUID.randomUUID().toString().replaceAll("-", "") + ".mov";
									File file5 = new File(uploadFilePath + filePath + fileName5);
									file5.getParentFile().mkdirs();
									FfmpegVideoMattingUtil.performMatting(file8.getPath(), file5.getPath(), bgColor);

									String[] str = ppt.getSzrWz().split(",");//数字人位置信息
									String[] str1 = ppt.getSzrDx().split(",");//数字人缩放信息

									//视频拼接,生成一秒静音视频
									String fileName6 = UUID.randomUUID().toString().replaceAll("-", "") + ".mp4";
									File file6 = new File(uploadFilePath + filePath + fileName6);
									file6.getParentFile().mkdirs();
									FfmpegVideoConcatenationUtils.overlaySilentVideo(file5.getPath(), file6.getPath(), file4.getPath(), str[0], str[1],str1[0], str1[1], "0.5");
									//视频拼接,原视频拼接背景图片
									String fileName7 = UUID.randomUUID().toString().replaceAll("-", "") + ".mp4";
									File file7 = new File(uploadFilePath + filePath + fileName7);
									file7.getParentFile().mkdirs();
									FfmpegVideoConcatenationUtils.combineVideoAndImage(file5.getPath(), file7.getPath(), file4.getPath(), str[0], str[1],str1[0], str1[1]);
									//合成新的视频，前后两秒静音
									List<String> inputVideoList = new ArrayList<>();
									inputVideoList.add(file6.getPath());
									inputVideoList.add(file7.getPath());
									inputVideoList.add(file6.getPath());
									log.info("inputVideoList:"+inputVideoList);
									//最终处理后合成的视频
									String fileName = UUID.randomUUID().toString().replaceAll("-", "") + ".mp4";
									File file = new File(uploadFilePath + filePath + fileName);
									file.getParentFile().mkdirs();
									FfmpegVideoConcatenationUtils.concatenateVideos(inputVideoList, file.getPath(), false);

									ppt.setVideoProceUrl(filePath + fileName);
									file4.delete();
									file5.delete();
									file6.delete();
									file7.delete();
									file8.delete();
								}else if(result.equals("process")){
									String resultMsg = (String) jsonObject.get("result_msg");
									ppt.setProgressDec(resultMsg);
								}else if(result.equals("failed")){
									String resultMsg = (String) jsonObject.get("result_msg");
									ppt.setProgressDec(resultMsg);
									ppt.setVideoZt(3);
								}
								tAiShuzirenPptService.save(ppt);
								success = true; // 如果成功，跳出循环
							}catch (Exception e){
								e.printStackTrace(); // 打印堆栈信息以便调试
								// 设置失败状态
								ppt.setVideoZt(3);
								ppt.setProgressDec("处理失败");
								if (retryCount == 5) {
									//ppt.setVideoZt(3);
									tAiShuzirenPptService.save(ppt);
								}
							}
						}
					}
				}
			}
		}
		log.info("定时器: 修改更新数字人ppt状态  ---- end----  ");
	}
	/**
	 * 定时更新微课视频合成
	 * @throws InterruptedException
	 * @throws ParseException
	 */
	@SuppressWarnings("deprecation")
	@Scheduled(cron = "${timer.microCourseVideoConcatenation}")
	public void microCourseVideoConcatenation(){

		//scheduleServiceFlag不为空且==false时，不执行定时任务
		if(scheduleServiceFlag!=null && !scheduleServiceFlag){
			return;
		}

		log.info("定时器: 更新微课视频合成  ---- start----");

		//查询视频生成中微课信息
		List<TAiMicroCourse> courseList = aiMicroCourseService.findByVideoZtAndIsDel(1, ConstantUtil.IS_DEL_0);
		if(courseList.size() > 0){
			for (TAiMicroCourse course : courseList) {

				//查询微课图片信息
				List<TAiShuzirenPpt> pptList = tAiShuzirenPptService.findByCourseIdAndIsDelOrderByPptXh(course.getCourseId(), ConstantUtil.IS_DEL_0);
				if(pptList.size() > 0){
					List<String> inputVideoList = new ArrayList<>();
					Boolean flag = true;//是否全部合成
					Boolean errFlag = false;//是否有失败的
					for (TAiShuzirenPpt ppt : pptList) {
						if(StringUtils.isNotEmpty(ppt.getXingxiangId())){
							if(ppt.getVideoZt() == 2){
								inputVideoList.add(uploadFileUrl + "/" + ppt.getVideoProceUrl());
							}else{
								if(ppt.getVideoZt() == 3){
									errFlag = true;
								}
								flag = false;
								continue;
							}
						}
					}
					if(flag){
						String filePath = "ai/microCourse/" + DateUtil.formatDateByFormat(new Date(), "yyyy-MM-dd") + "/";
						String fileName = UUID.randomUUID().toString().replaceAll("-", "") + ".mp4";
						File file = new File(uploadFilePath + filePath + fileName);
						file.getParentFile().mkdirs();
						try {
							log.info("inputVideoList:"+inputVideoList);
							FfmpegVideoConcatenationUtils.concatenateVideos(inputVideoList, file.getPath(), false);
							course.setVideoUrl(filePath + fileName);
							course.setVideoZt(2);
							course.setDes("视频合成成功");
							aiMicroCourseService.save(course);
						} catch (Exception e) {
							e.printStackTrace();
							course.setVideoZt(3);
							course.setDes("视频合成失败");
							aiMicroCourseService.save(course);
						}
					}else{
						if(errFlag){
							course.setVideoZt(3);
							course.setDes("视频合成失败");
							aiMicroCourseService.save(course);
						}
					}

				}
			}
		}
		log.info("定时器: 更新微课视频合成  ---- end----  ");
	}

}
