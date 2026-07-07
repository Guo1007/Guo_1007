package gcy.system.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "aliyun.oss")
public class AliYunOssConfig {

    private String endpoint;

    private String key;

    private String secret;

    private String bucket;

    private String url;

}
