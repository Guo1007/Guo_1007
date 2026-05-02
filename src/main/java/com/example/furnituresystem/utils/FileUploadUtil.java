package com.example.furnituresystem.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.example.furnituresystem.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Component
@Slf4j
public class FileUploadUtil {

    @Value("${upload.path}")
    private String uploadPath;

    @Value("${upload.url-prefix}")
    private String urlPrefix;

    private static String PATH;

    private static String URL_PREFIX;

    @PostConstruct
    public void init() {
        PATH = uploadPath;
        URL_PREFIX = urlPrefix;
    }

    public static String upload(MultipartFile file, String folder) {
        if (file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        String ext = FileUtil.extName(file.getOriginalFilename());
        String fileName = IdUtil.simpleUUID() + "." + ext;
        String datePath = DateUtil.today().replace("-", "/");
        String relativePath = folder + "/" + datePath + "/" + fileName;
        String fullPath = PATH + relativePath;
        FileUtil.mkdir(FileUtil.getParent(fullPath, 1));
        try {
            file.transferTo(new File(fullPath));
            log.info("上传成功: {}", fullPath);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException("文件上传失败，请重试");
        }
        return URL_PREFIX + relativePath;
    }

    public static String uploadAvatar(MultipartFile file) {
        return upload(file, "avatar");
    }

}
