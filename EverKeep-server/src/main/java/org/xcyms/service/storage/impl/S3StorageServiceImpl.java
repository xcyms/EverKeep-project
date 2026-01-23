package org.xcyms.service.storage.impl;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.xcyms.common.Constant;
import org.xcyms.service.IConfigService;
import org.xcyms.service.storage.IStorageService;

import java.io.File;
import java.io.FileInputStream;

/**
 * S3 兼容云存储实现 (支持 MinIO, AWS S3, Cloudflare R2 等)
 */
@Slf4j
@Service("S3")
@RequiredArgsConstructor
public class S3StorageServiceImpl implements IStorageService {

    private final IConfigService configService;

    private MinioClient getClient() {
        return MinioClient.builder()
                .endpoint(configService.getConfigValue(null, Constant.ConfigKey.S3.ENDPOINT))
                .credentials(
                        configService.getConfigValue(null, Constant.ConfigKey.S3.ACCESS_KEY),
                        configService.getConfigValue(null, Constant.ConfigKey.S3.SECRET_KEY)
                )
                .build();
    }

    @Override
    public String upload(File file, String relativePath) {
        String bucket = configService.getConfigValue(null, Constant.ConfigKey.S3.BUCKET);
        String domain = configService.getConfigValue(null, Constant.ConfigKey.S3.DOMAIN);

        try (FileInputStream fis = new FileInputStream(file)) {
            getClient().putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(relativePath)
                            .stream(fis, file.length(), -1)
                            .build()
            );
            // 返回完整的 Web 访问路径
            return domain.endsWith("/") ? domain + relativePath : domain + "/" + relativePath;
        } catch (Exception e) {
            log.error("S3上传失败", e);
            throw new RuntimeException("云存储上传失败");
        }
    }

    @Override
    public void delete(String url) {
        String domain = configService.getConfigValue(null, Constant.ConfigKey.S3.DOMAIN);
        String bucket = configService.getConfigValue(null, Constant.ConfigKey.S3.BUCKET);

        // 只有不是本地路径的才尝试从 S3 删除
        if (!url.startsWith(Constant.UPLOAD_ROOT_PATH)) {
            String objectName = url.replace(domain, "").replaceFirst("^/", "");
            try {
                getClient().removeObject(
                        RemoveObjectArgs.builder().bucket(bucket).object(objectName).build()
                );
            } catch (Exception e) {
                log.error("S3删除失败: {}", url, e);
            }
        }
    }
}