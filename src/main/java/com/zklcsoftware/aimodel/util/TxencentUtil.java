package com.zklcsoftware.aimodel.util;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.region.Region;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * 腾讯SDK工具类
 */
public class TxencentUtil {

    /**
     * 上传文件至腾讯对象存储
     */
    public static void uploadObject(String secretId, String secretKey, String cosRegion,String bucketName,String localFilePath,String fileId) {

        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        // 2 设置 bucket 的区域, COS 地域的简称请参照 https://cloud.tencent.com/document/product/436/6224
        // clientConfig 中包含了设置 region, https(默认 http), 超时, 代理等 set 方法, 使用可参见源码或者常见问题 Java SDK 部分。
        Region region = new Region(cosRegion);
        ClientConfig clientConfig = new ClientConfig(region);
        // 3 生成 cos 客户端。
        COSClient cosClient = new COSClient(cred, clientConfig);
        // 指定要上传的文件
        File localFile = new File(localFilePath);
        // 指定要上传到 COS 上对象键
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fileId, localFile);
        PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);
        // 关闭客户端(关闭后台线程)
        cosClient.shutdown();

    }


    /**
     * 获取文档预览响应头信息
     * @param fileUrl
     * @param key
     * @return
     */
    public static String getResponseHeaders(String fileUrl,String key){
        URL url;
        URLConnection conn=null;
        try {
            url = new URL(fileUrl+"?ci-process=doc-preview");
            conn = url.openConnection();

        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String val = conn.getHeaderField(key);
        System.out.println(key+"    "+val);
        System.out.println( conn.getLastModified() );
        return val;
    }

    /*public static void main(String[] args) {


        String tx_sass_secretId ="AKIDCDKAZqHvPjv5ygnVbFcuGQaWz71dZdlo";
        String tx_sass_secretkey = "DNGMOCnAFarqbIzQjz5KWRcV6QWJ2oHQ";
        String tx_sass_cos_region = "ap-beijing";
        String tx_sass_cos_bucket_name = "xtydemo-netdisk-1301227471";
        String uploadpath="D:\\test";
        String filepath="/doc/bb.docx";

        try {
            TxencentUtil.uploadObject(tx_sass_secretId,tx_sass_secretkey,tx_sass_cos_region,tx_sass_cos_bucket_name,uploadpath+File.separator+filepath,filepath);
            String fileUrl="https://"+tx_sass_cos_bucket_name+".cos."+tx_sass_cos_region+".myqcloud.com/"+filepath;
            File imagePath=new File(uploadpath+File.separator+"/image"+filepath);
            if(!imagePath.exists()){
                imagePath.mkdirs();
            }
            //获取预览结果
            String totalPage = getResponseHeaders(fileUrl,"X-Total-Page");
            if(StringUtils.isNotEmpty(totalPage)){
                for (int i = 0; i < Integer.parseInt(totalPage); i++) {
                    //下载图片信息
                    HttpClients.getImage(fileUrl+"?ci-process=doc-preview&page="+(i+1),imagePath+File.separator+i+".jpg");
                }
            }
            //将图片信息合并成一个pdf文件
            PrintToPdfUtil.toPdf(uploadpath+File.separator+"/image"+filepath,uploadpath+File.separator+filepath+".pdf");


        }catch (Exception e){

        }


    }*/

}
