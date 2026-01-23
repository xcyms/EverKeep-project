package org.xcyms.service.storage.impl;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.region.Region;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.xcyms.common.Constant;
import org.xcyms.service.IConfigService;
import org.xcyms.service.storage.IStorageService;

import java.io.File;

/**
 * <p>
 * 腾讯对象存储 实现
 * </p>
 *
 * @author liu-xu
 * @date 2026年01月23日 14:54
 */
@Slf4j
@Service("TENCENT")
@RequiredArgsConstructor
public class TencentCosStorageServiceImpl implements IStorageService {

    private final IConfigService configService;

    private COSClient createCOSClient() {
        // 设置用户身份信息。
        String secretId = configService.getConfigValue(null, Constant.ConfigKey.TencentCOS.SECRET_ID);//用户的 SecretId
        String secretKey = configService.getConfigValue(null, Constant.ConfigKey.TencentCOS.SECRET_KEY);//用户的 SecretKey
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);

        // ClientConfig 中包含了后续请求 COS 的客户端设置：
        ClientConfig clientConfig = new ClientConfig();

        // 设置 bucket 的地域
        // COS_REGION
        clientConfig.setRegion(new Region(configService.getConfigValue(null, Constant.ConfigKey.TencentCOS.REGION)));

        // 生成 cos 客户端。
        return new COSClient(cred, clientConfig);
    }

    @Override
    public String upload(File file, String relativePath) {
        // 存储桶的命名格式为 BucketName-APPID，此处填写的存储桶名称必须为此格式
        String bucketName = configService.getConfigValue(null, Constant.ConfigKey.TencentCOS.BUCKET);
        // 对象键(Key)是对象在存储桶中的唯一标识。
        COSClient cosClient = null;
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, relativePath, file);
        try {
            cosClient = createCOSClient();
            PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);
            log.info("腾讯云上传成功: {}", putObjectResult);
            return putObjectResult.getETag();
        } catch (Exception e) {
            log.error("腾讯云上传失败", e);
            throw e;
        } finally {
            if (cosClient != null) {
                cosClient.shutdown();
            }
        }
    }

    @Override
    public void delete(String url) {
        COSClient cosClient = null;
        try {
            cosClient = createCOSClient();
            cosClient.deleteObject(configService.getConfigValue(null, Constant.ConfigKey.TencentCOS.BUCKET), url);
            log.info("腾讯云删除成功: {}", url);
        } catch (Exception e) {
            log.error("腾讯云删除失败: {}", url, e);
        } finally {
            if (cosClient != null) {
                cosClient.shutdown();
            }
        }
    }
}
