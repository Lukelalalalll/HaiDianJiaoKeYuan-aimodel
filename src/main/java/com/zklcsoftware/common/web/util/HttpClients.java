package com.zklcsoftware.common.web.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.util.FileCopyUtils;

import javax.net.ssl.SSLContext;


/**
 * httpclient调用接口公用方法类
 * 
 * @author sj
 *
 */
@Slf4j
public class HttpClients {

	/**
	 * 公用方法httpclient发送请求post
	 * 
	 * @param url
	 * @param map
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("deprecation")
	public static String post(String url, Map<String, Object> map) throws Exception {
		return post(url,map,null);
	}

	/**
	 * 公用方法httpclient发送请求post
	 *
	 * @param url
	 * @param map
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("deprecation")
	public static String post(String url, Map<String, Object> map,Map<String, Object> headerMap) throws Exception {
		String result = "";
		// POST的URL
		HttpPost httppost = new HttpPost(url);
		// 建立HttpPost对象
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		if (map != null) {
			Set<Map.Entry<String, Object>> set = map.entrySet();
			for (Iterator<Map.Entry<String, Object>> it = set.iterator(); it.hasNext();) {
				Map.Entry<String, Object> entry = (Map.Entry<String, Object>) it.next();
				// 建立一个NameValuePair数组，用于存储欲传送的参数
				params.add(new BasicNameValuePair(entry.getKey(), String.valueOf(entry.getValue())));
			}
		}
		// 添加参数
		httppost.setEntity(new UrlEncodedFormEntity(params));
		if(headerMap!=null){
			Set<Map.Entry<String, Object>> set = headerMap.entrySet();
			for (Iterator<Map.Entry<String, Object>> it = set.iterator(); it.hasNext();) {
				Map.Entry<String, Object> entry = (Map.Entry<String, Object>) it.next();
				httppost.addHeader(entry.getKey(), String.valueOf(entry.getValue()));
			}
		}

		// 设置编码
		HttpClient httpClient = getNoSSLCheckHttpClient();
		HttpResponse response = httpClient.execute(httppost);
		// 发送Post,并返回一个HttpResponse对象
		if (response.getStatusLine().getStatusCode() == 200) {// 如果状态码为200,就是正常返回
			result = EntityUtils.toString(response.getEntity());
		}
		httpClient.getConnectionManager().shutdown();
		return result;
	}

	/**
	 * 公用方法httpclient发送请求get
	 * 
	 * @param url
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public static String get(String url, Map<String, Object> map) throws Exception {
		return get(url,map,null);
	}

	/**
	 * 公用方法httpclient发送请求get
	 *
	 * @param url
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public static String get(String url, Map<String, Object> map,Map<String,Object> headerMap) throws Exception {
		String result = "";
		String str = "";
		// 封装请求参数
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		if (map != null) {
			Set<Map.Entry<String, Object>> set = map.entrySet();
			for (Iterator<Map.Entry<String, Object>> it = set.iterator(); it.hasNext();) {
				Map.Entry<String, Object> entry = (Map.Entry<String, Object>) it.next();
				// 建立一个NameValuePair数组，用于存储欲传送的参数
				params.add(new BasicNameValuePair(entry.getKey(), String.valueOf(entry.getValue())));
			}
			str = EntityUtils.toString(new UrlEncodedFormEntity(params));
		}

		if (str != "") {// 如果参数不为空
			url = url + "?" + str;
		}
		// 根据地址获取请求
		HttpGet request = new HttpGet(url);// 这里发送get请求

		if(headerMap!=null){
			Set<Map.Entry<String, Object>> set = headerMap.entrySet();
			for (Iterator<Map.Entry<String, Object>> it = set.iterator(); it.hasNext();) {
				Map.Entry<String, Object> entry = (Map.Entry<String, Object>) it.next();
				request.addHeader(entry.getKey(), String.valueOf(entry.getValue()));
			}
		}

		// 获取当前客户端对象
		HttpClient httpClient = getNoSSLCheckHttpClient();
		// 通过请求对象获取响应对象
		HttpResponse response = httpClient.execute(request);

		// 判断网络连接状态码是否正常(0--200都数正常)
		if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			result = EntityUtils.toString(response.getEntity(), "utf-8");
		}
		httpClient.getConnectionManager().shutdown();
		return result;
	}
	/**
	 * @Description 获取一个不校验SSL的httpclient
	 * @Author zhushaog
	 * @UpdateTime 2025/4/9 14:49
	 * @return: org.apache.http.client.HttpClient
	 * @throws
	 */
	private static HttpClient getNoSSLCheckHttpClient(){
		CloseableHttpClient httpClient=null;
		try {
			// 创建一个不验证证书链的 SSLContext
			SSLContext sslContext = SSLContextBuilder.create()
					.loadTrustMaterial((chain, authType) -> true)
					.build();

			// 创建一个 SSLConnectionSocketFactory
			SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
					sslContext,
					NoopHostnameVerifier.INSTANCE
			);

			// 创建一个自定义的 HttpClient
			httpClient = org.apache.http.impl.client.HttpClients.custom()
					.setSSLSocketFactory(sslSocketFactory)
					.build();
		} catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
			throw new RuntimeException("Failed to create HttpClient with SSL context", e);
		}
		return httpClient;
	}
	
	/**
	 * 针对服务器发送请求get
	 * 超时时间设置 6s
	 * @param url
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public static String getForMachine(String url, Map<String, Object> map) throws Exception {
		String result = "";
		String str = "";
		// 封装请求参数
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		if (map != null) {
			Set<Map.Entry<String, Object>> set = map.entrySet();
			for (Iterator<Map.Entry<String, Object>> it = set.iterator(); it.hasNext();) {
				Map.Entry<String, Object> entry = (Map.Entry<String, Object>) it.next();
				// 建立一个NameValuePair数组，用于存储欲传送的参数
				params.add(new BasicNameValuePair(entry.getKey(), String.valueOf(entry.getValue())));
			}
			str = EntityUtils.toString(new UrlEncodedFormEntity(params));
		}
		if (str != "") {// 如果参数不为空
			url = url + "?" + str;
		}
		// 通过请求对象获取响应对象
		HttpResponse response;
		HttpClient httpClient = getNoSSLCheckHttpClient();
		try {
			// 根据地址获取请求
			HttpGet request = new HttpGet(url);// 这里发送get请求
			// 获取当前客户端对象
			httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 20000);//连接时间20s
			httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,  6000);//数据传输时间6s
			response = httpClient.execute(request);
			// 判断网络连接状态码是否正常(0--200都数正常)
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				result = EntityUtils.toString(response.getEntity(), "utf-8");
			}
		} catch (Exception e) {
			
			return null;
		}
		httpClient.getConnectionManager().shutdown();
		return result;
	}
	/**
	 * 
	 * <p>
	 * 功能 删除
	 * </p>
	 * @author zhushaog 时间 2017年5月28日 下午10:56:48
	 * @param url
	 * @return
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	public static String delete(String url) throws Exception{

		String result = "";

		// 获取当前客户端对象
		HttpClient httpClient = getNoSSLCheckHttpClient();
		// 根据地址获取请求
		HttpDelete request = new HttpDelete(url);// 这里发送delete请求
		// 通过请求对象获取响应对象
		HttpResponse response = httpClient.execute(request);

		// 判断网络连接状态码是否正常(0--200都数正常)
		if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			result = EntityUtils.toString(response.getEntity(), "utf-8");
		}
		httpClient.getConnectionManager().shutdown();
		return result;
	
	}

	/**
	 * 
	 * <p>
	 * 功能 发送json格式数据的post请求
	 * </p>
	 * 
	 * @author zhushaog 时间 2017年4月12日 上午10:34:45
	 * @param url
	 * @param jsonData
	 * @return
	 * @throws Exception
	 */
	public static String postJson(String url, String jsonData) throws Exception {
		return postJson(url, jsonData,null);
	}


	/**
	 *
	 * <p>
	 * 功能 发送json格式数据的post请求
	 * </p>
	 *
	 * @author zhushaog 时间 2017年4月12日 上午10:34:45
	 * @param url
	 * @param jsonData
	 * @return
	 * @throws Exception
	 */
	public static String postJson(String url, String jsonData, Map<String, Object> headerMap) throws Exception {
		String result = "";

		// 获取当前客户端对象
		HttpClient httpClient = getNoSSLCheckHttpClient();
		// 根据地址获取请求
		HttpPost request = new HttpPost(url);// 这里发送get请求

		// 设置json数据
		StringEntity entity = new StringEntity(jsonData, "utf-8");// 解决中文乱码问题
		//entity.setContentEncoding("UTF-8");
		//entity.setContentType("application/json");
		request.setEntity(entity);

		if(headerMap!=null){
			Set<Map.Entry<String, Object>> set = headerMap.entrySet();
			for (Iterator<Map.Entry<String, Object>> it = set.iterator(); it.hasNext();) {
				Map.Entry<String, Object> entry = (Map.Entry<String, Object>) it.next();
				request.addHeader(entry.getKey(), String.valueOf(entry.getValue()));
			}
		}

		// 通过请求对象获取响应对象
		HttpResponse response = httpClient.execute(request);

		// 判断网络连接状态码是否正常(0--200都数正常)
		if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			result = EntityUtils.toString(response.getEntity(), "utf-8");
		}
		httpClient.getConnectionManager().shutdown();
		return result;
	}

	// file1与file2在同一个文件夹下 filepath是该文件夹指定的路径
	public static String SubmitPost(String url, MultipartEntity reqEntity) {
		String result = "";
		HttpClient httpclient = getNoSSLCheckHttpClient();
		try {
			HttpPost httppost = new HttpPost(url);
			httppost.setEntity(reqEntity);
			HttpResponse response = httpclient.execute(httppost);
			int statusCode = response.getStatusLine().getStatusCode();
			// 判断网络连接状态码是否正常(0--200都数正常)
			if (statusCode == HttpStatus.SC_OK) {
				result = EntityUtils.toString(response.getEntity(), "utf-8");
			}
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		httpclient.getConnectionManager().shutdown();
		return result;
	}

	public static void getFile(String fileUrl,String filePath) throws Exception {
		// 通过请求对象获取响应对象
		HttpResponse response;
		HttpClient httpClient = new DefaultHttpClient();
		// 根据地址获取请求
		HttpGet request = new HttpGet(fileUrl);// 这里发送get请求
		// 获取当前客户端对象
		httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 20000);//连接时间20s
		httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,  6000);//数据传输时间6s
		response = httpClient.execute(request);
		// 判断网络连接状态码是否正常(0--200都数正常)
		if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			if(response.getEntity()!=null){
				InputStream inputStream=response.getEntity().getContent();
				File file=new File(filePath);
				file.getParentFile().mkdirs();
				FileCopyUtils.copy(inputStream, new FileOutputStream(filePath)); //将图片保存在本次磁盘D盘，命名为xxx.png
			}
		}else{
			throw new IOException("文件不存在");
		}
		httpClient.getConnectionManager().shutdown();
	}

	/*public static void main(String[] args) throws Exception {
		// 向消息中心服务注册
		Map messageServiceInfo=new HashMap(); messageServiceInfo.put("id",
		  "11"); messageServiceInfo.put("messageSercet","11");
		  messageServiceInfo.put("appName", "11");
		  messageServiceInfo.put("appType", ConstantUtil.MESSAGE_APPTYPE2);
		  messageServiceInfo.put("schoolId", ConstantUtil.MESSAGE_APPTYPE2);
		  messageServiceInfo.put("access_token",
		  "6fe1d592-54b1-490b-b577-82429d7e2d2c");
		  System.out.println(post(
		  "http://192.168.100.232:9000/messageservice/api/toSaveTCloudMessageApp"
		  ,messageServiceInfo));
		 
		// 向ZIS服务注册
		Map agentInfo = new HashMap();
		agentInfo.put("id", "zis_zcloudagentsim");
		agentInfo.put("passwd", "9521b303c8964c8fb52ad5bad8629e45");
		agentInfo.put("name", "sim管理");
		agentInfo.put("cert","LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tDQpNSUlCMlRDQ0FVS2dBd0lCQWdJREI1dHpNQTBHQ1NxR1NJYjNEUUVCQlFVQU1DNHhDekFKQmdOVkJBWVRBbU51TVJBd0RnWURWUVFLDQpFd2RHYkhseWFYTm9NUTB3Q3dZRFZRUURFd1J5YjI5ME1CNFhEVEV4TURnek1UQXlOREl5TUZvWERUSXhNRGd5T0RBeU5ESXlNRm93DQpOVEVMTUFrR0ExVUVCaE1DWTI0eEVEQU9CZ05WQkFvVEIyWnNlWEpwYzJneEZEQVNCZ05WQkFNVEMzTnBiWEJzWldGblpXNTBNSUdmDQpNQTBHQ1NxR1NJYjNEUUVCQVFVQUE0R05BRENCaVFLQmdRQ01YazBFUlZiL2VnSDdFVmFubVNHaldMY1JWMzRsa1ZpMHRLbHR6ZTQ0DQpKRE9aeGpYYjlNb3NmOXNZajBpUXo4ODVxQ0psaUcxWW1Vay94c1dEb1dBUnNvd3B6ejkzMjNFTWx5KzdOdWhtQlpSVXkyeVVKengzDQoyT2hOUU5VSGwydWJhTE12RmpsblJLK1dWSktyTjk3UHozNFFkRml5dDhvNTNMSzF5OUVHaXdJREFRQUJNQTBHQ1NxR1NJYjNEUUVCDQpCUVVBQTRHQkFIY1oyNDBPQjNlNWtTSjFsN25tOFlOQ05rV3p3dXJaUXRHdmhqUUJyMVE0eEhGRkQzcU5OdE5MVG1jU1hmN2xnREIvDQo1dXNHbDgwMWtRVWtDNTRRRFBWdjhLMFpOUHU2ZVRkWS9kc3NwQXlLMFpKQnhTaGZGVklBWmZwWjQ5RXByVlhVUG1zaC9qd01jRjNiDQpIQ3R3MzZBaEMxZVRuVVZkOWd0YzVPNkFNeTJKDQotLS0tLUVORCBDRVJUSUZJQ0FURS0tLS0tDQo=");
		agentInfo.put("providerObjects", StringUtils.split("学生基础信息,学生家庭成员基础信息,学生综合基础信息,教职工基础信息,职务信息,学校基础信息,学区基础信息,学部基础信息,年级基础信息,班级基础信息,组织机构信息", ","));
		agentInfo.put("usedObjects", StringUtils.split("学生基础信息,学生家庭成员基础信息,学生综合基础信息,教职工基础信息,职务信息,学校基础信息,学区基础信息,学部基础信息,年级基础信息,班级基础信息,组织机构信息", ","));
		//System.out.println(new Gson().toJson(agentInfo));
		System.out.println(postJson("http://192.168.100.232:9000/zis-8081/api/agents?access_token=921e40a7-d70d-4645-80b5-00e6a83984ef",new Gson().toJson(agentInfo)));
	}*/
}
