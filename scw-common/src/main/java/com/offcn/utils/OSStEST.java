package com.offcn.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class OSStEST {
    public static void main(String[] args) throws IOException {
        // Endpoint以杭州为例，其它Region请按实际情况填写。
        String endpoint = "oss-cn-beijing.aliyuncs.com";
// 云账号AccessKey有所有API访问权限，建议遵循阿里云安全最佳实践，创建并使用RAM子账号进行API访问或日常运维，请登录 https://ram.console.aliyun.com 创建。
        String accessKeyId = "LTAI4Fz8yLvFUyfPMzvhBJvL";
        String accessKeySecret = "PasmTPBt9m7eGN1j4raEDS2e0YnRhS";

// 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

// 上传文件流。
        InputStream inputStream = new FileInputStream(new File("C:\\Users\\Public\\Pictures\\Sample Pictures\\1.jpg"));
        ossClient.putObject("offcn202103031102", "pic/james.jpg", inputStream);

// 关闭OSSClient。
        ossClient.shutdown();
        System.out.println("上传成功");
    }
}
