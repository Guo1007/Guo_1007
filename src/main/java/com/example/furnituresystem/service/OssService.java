package com.example.furnituresystem.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectResult;
import com.example.furnituresystem.config.AliYunOssConfig;
import com.example.furnituresystem.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Service
public class OssService {

    private final AliYunOssConfig ossConfig;

    private OSS ossClient;

    public OssService(AliYunOssConfig ossConfig) {
        this.ossConfig = ossConfig;
    }

    @PostConstruct
    public void init() {
        ossClient = new OSSClientBuilder().build(
                ossConfig.getEndpoint(),
                ossConfig.getKey(),
                ossConfig.getSecret()
        );
        log.info("OSS client initialized, endpoint={}, bucket={}", ossConfig.getEndpoint(), ossConfig.getBucket());
    }

    public String upload(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = IdUtil.simpleUUID() + ext;
        String datePath = DateUtil.today().replace("-", "/");
        String objectName = folder + "/" + datePath + "/" + fileName;
        try (InputStream inputStream = file.getInputStream()) {
            PutObjectResult result = ossClient.putObject(ossConfig.getBucket(), objectName, inputStream);
            log.info("OSS上传成功, bucket={}, objectName={}, etag={}",
                    ossConfig.getBucket(), objectName, result.getETag());
        } catch (IOException e) {
            log.error("OSS上传失败", e);
            throw new BusinessException("文件上传失败，请重试");
        }
        return ossConfig.getUrl() + "/" + objectName;
    }

    public String uploadAvatar(MultipartFile file) {
        return upload(file, "avatar");
    }
}
