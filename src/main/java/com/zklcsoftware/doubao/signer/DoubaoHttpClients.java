package com.zklcsoftware.doubao.signer;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.util.*;


/**
 * 豆包httpclient调用接口公用方法类
 * 
 * @author sj
 *
 */
public class DoubaoHttpClients {

	/**
	 * 公用方法httpclient发送请求post
	 *
	 * @param url
	 * @param paramMap
	 * @param headerMap
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("deprecation")
	public static String post(String url, Map<String, Object> paramMap, Map<String, Object> headerMap, String AccessKeyID, String SecretAccessKey, String region, String service) throws Exception {
		String result = "";

		Credentials credentials = new Credentials();
		credentials.setAccessKeyID(AccessKeyID);
		credentials.setSecretAccessKey(SecretAccessKey);
		credentials.setRegion(region);
		credentials.setService(service);

		/* create signer */
		Signer signer = new Signer();

		/* create http client */
		CloseableHttpClient httpClient = HttpClients.createDefault();

		/* prepare request */
		HttpPost request = new HttpPost();
		request.setURI(new URI(url));
		request.addHeader(HttpHeaders.USER_AGENT, "volc-sdk-java/v1.0.0");
		if(headerMap!=null){
			Set<Map.Entry<String, Object>> set = headerMap.entrySet();
			for (Iterator<Map.Entry<String, Object>> it = set.iterator(); it.hasNext();) {
				Map.Entry<String, Object> entry = (Map.Entry<String, Object>) it.next();
				request.addHeader(entry.getKey(), String.valueOf(entry.getValue()));
			}
		}

		// 建立HttpPost对象
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		if (paramMap != null) {
			Set<Map.Entry<String, Object>> set = paramMap.entrySet();
			for (Iterator<Map.Entry<String, Object>> it = set.iterator(); it.hasNext();) {
				Map.Entry<String, Object> entry = (Map.Entry<String, Object>) it.next();
				// 建立一个NameValuePair数组，用于存储欲传送的参数
				nvps.add(new BasicNameValuePair(entry.getKey(), String.valueOf(entry.getValue())));
			}
		}
		request.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

		signer.sign(request, credentials);

		/* launch request */
		CloseableHttpResponse response = httpClient.execute(request);

		/* status code */
		System.out.println(response.getStatusLine().getStatusCode());   // 200

		/* get response body */
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			result = EntityUtils.toString(entity);
			System.out.println(result);
		}
		/* close resources */
		response.close();
		httpClient.close();
		return result;
	}

	/**
	 * 
	 * <p>
	 * 功能 发送json格式数据的post请求
	 * </p>
	 * 
	 * @author chh
	 * @param url
	 * @param jsonData
	 * @return
	 * @throws Exception
	 */
	public static String postJson(String url, String jsonData, String ak, String sk, String region, String service) throws Exception {
		String result = "";

		Credentials credentials = new Credentials();
		credentials.setAccessKeyID(ak);
		credentials.setSecretAccessKey(sk);
		credentials.setRegion(region);
		credentials.setService(service);

		/* create signer */
		Signer signer = new Signer();

		/* create http client */
		CloseableHttpClient httpClient = HttpClients.createDefault();

		/* prepare request */
		HttpPost request = new HttpPost();
		request.setURI(new URI(url));
		request.addHeader(HttpHeaders.USER_AGENT, "volc-sdk-java/v1.0.0");

		// 设置json数据
		StringEntity strentity = new StringEntity(jsonData, "utf-8");// 解决中文乱码问题
		strentity.setContentEncoding("UTF-8");
		strentity.setContentType("application/json");
		request.setEntity(strentity);

		signer.sign(request, credentials);

		/* launch request */
		CloseableHttpResponse response = httpClient.execute(request);

		/* status code */
		System.out.println(response.getStatusLine().getStatusCode());   // 200

		/* get response body */
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			result = EntityUtils.toString(entity);
			System.out.println(result);
		}
		/* close resources */
		response.close();
		httpClient.close();
		return result;
	}
}
