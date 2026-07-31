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
import java.util.Set;

/**
 * 阿里云OSS对象存储服务类，提供文件上传到阿里云OSS的功能。
 * 支持按文件夹分类存储，自动按日期组织文件路径，返回可访问的文件URL。
 * 上传前执行三重校验：扩展名白名单 → Content-Type 校验 → 二进制魔数校验。
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Slf4j
@Service
public class OssService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp");

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
     * 上传前执行三重安全校验，文件会以UUID重命名保留原始扩展名，按上传日期自动创建子目录。
     * 上传成功后返回文件的完整可访问URL。
     *
     * @param file   要上传的MultipartFile文件对象，不能为空
     * @param folder OSS存储桶中的目标文件夹名称，用于对文件进行分类存储
     * @return 上传成功后的文件完整访问URL，格式为 配置的URL前缀 + 对象存储路径
     * @throws BusinessException 当上传文件为空、校验不通过或读取文件流失败时抛出
     */
    public String upload(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }

        // ========== 步骤 1：扩展名白名单校验 ==========
        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        }
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new BusinessException("仅支持 jpg/jpeg/png/gif/webp 格式图片");
        }

        // ========== 步骤 2：Content-Type 校验 ==========
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessException("文件类型不合法");
        }

        // ========== 步骤 3：魔数（Magic Number）校验 ==========
        validateImageMagic(file);

        String fileName = IdUtil.simpleUUID() + "." + ext;
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
     * 通过读取文件前 12 字节的魔数（Magic Number）校验文件是否为真实图片。
     * 支持 JPEG、PNG、GIF、WEBP 四种格式。
     *
     * @param file 待校验的上传文件
     * @throws BusinessException 当文件魔数与声明的图片格式不匹配时抛出
     */
    private void validateImageMagic(MultipartFile file) {
        byte[] header = new byte[12];
        try (InputStream is = file.getInputStream()) {
            int read = is.read(header);
            if (read <= 0) {
                throw new BusinessException("文件为空");
            }
        } catch (IOException e) {
            throw new BusinessException("无法读取文件内容");
        }
        String hex = bytesToHex(header);
        boolean jpeg = hex.startsWith("ffd8ff");
        boolean png = hex.startsWith("89504e47");
        boolean gif = hex.startsWith("47494638");
        boolean webp = hex.startsWith("52494646") && hex.length() >= 24
                && hex.substring(16, 24).equals("57454250");
        if (!(jpeg || png || gif || webp)) {
            throw new BusinessException("文件内容与扩展名不符，请上传真实图片文件");
        }
    }

    /**
     * 将字节数组转换为小写十六进制字符串。
     *
     * @param bytes 字节数组
     * @return 小写十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
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
