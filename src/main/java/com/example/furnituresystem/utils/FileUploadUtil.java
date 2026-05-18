package com.example.furnituresystem.utils;

import com.example.furnituresystem.service.OssService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Component
public class FileUploadUtil {

    private static OssService ossService;

    public FileUploadUtil(OssService ossService) {
        FileUploadUtil.ossService = ossService;
    }

    public static String upload(MultipartFile file, String folder) {
        return ossService.upload(file, folder);
    }

    public static String uploadAvatar(MultipartFile file) {
        return ossService.uploadAvatar(file);
    }
}
