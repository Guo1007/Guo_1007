package gcy.system.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectResult;
import gcy.system.config.AliYunOssConfig;
import gcy.system.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * 阿里云OSS对象存储服务类，提供文件上传到阿里云OSS的功能。
 * 支持按文件夹分类存储，自动按日期组织文件路径，返回可访问的文件URL。
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Slf4j
@Service
public class OssService {

    private final AliYunOssConfig ossConfig;

    private OSS ossClient;

    /**
     * 构造函数，通过依赖注入接收阿里云OSS配置信息。
     *
     * @param ossConfig 阿里云OSS配置对象，包含endpoint、accessKey、secret、bucket和访问URL等配置项
     */
    public OssService(AliYunOssConfig ossConfig) {
        this.ossConfig = ossConfig;
    }

    /**
     * 初始化OSS客户端连接。
     * 在Spring Bean属性设置完成后自动调用，使用配置中的endpoint、accessKey和secret
     * 构建OSS客户端实例，建立与阿里云OSS服务的连接。
     */
    @PostConstruct
    public void init() {
        ossClient = new OSSClientBuilder().build(
                ossConfig.getEndpoint(),
                ossConfig.getKey(),
                ossConfig.getSecret()
        );
        log.info("OSS client initialized, endpoint={}, bucket={}", ossConfig.getEndpoint(), ossConfig.getBucket());
    }

    /**
     * 将文件上传到阿里云OSS指定文件夹中。
     * 文件会以UUID重命名保留原始扩展名，按上传日期自动创建子目录，生成唯一的存储路径。
     * 上传成功后返回文件的完整可访问URL。
     *
     * @param file   要上传的MultipartFile文件对象，不能为空
     * @param folder OSS存储桶中的目标文件夹名称，用于对文件进行分类存储
     * @return 上传成功后的文件完整访问URL，格式为 配置的URL前缀 + 对象存储路径
     * @throws BusinessException 当上传文件为空或读取文件流失败时抛出，提示用户上传失败
     */
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

    /**
     * 上传用户头像文件到OSS的头像专用文件夹中。
     * 该方法是对 {@link #upload(MultipartFile, String)} 的便捷封装，
     * 固定使用 "avatar" 作为存储文件夹，简化头像上传的调用。
     *
     * @param file 要上传的头像文件，不能为空
     * @return 上传成功后的头像文件完整访问URL
     * @throws BusinessException 当上传文件为空或读取文件流失败时抛出，提示用户上传失败
     */
    public String uploadAvatar(MultipartFile file) {
        return upload(file, "avatar");
    }
}
