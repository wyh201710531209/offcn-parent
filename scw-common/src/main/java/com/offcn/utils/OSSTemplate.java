package com.offcn.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.Data;
import lombok.ToString;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@ToString
@Data
public class OSSTemplate {
    private String endpoint;
    private String bucketDomain;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

    public String upload(InputStream inputStream, String fileName){

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String folderName = simpleDateFormat.format(new Date());
         fileName = UUID.randomUUID().toString().replace("-", "")+"_"+fileName;
         //创建oss实例
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        //上传
        ossClient.putObject(bucketName, "pic/"+folderName+"/"+fileName, inputStream);
        // 关闭OSSClient。
        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ossClient.shutdown();
        System.out.println("上传成功");
        System.out.println("上传文件:"+"http://" + bucketDomain + "/pic/" + folderName + "/" + fileName);
        return "http://"+bucketName+"/pic/"+folderName+"/"+fileName;

    }
}
