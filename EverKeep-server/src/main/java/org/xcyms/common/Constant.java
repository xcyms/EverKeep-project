package org.xcyms.common;

import lombok.Getter;

/**
 * <p>
 *     公共常量类
 * </p>
 * @author liu-xu
 * @date 2026年01月11日 15:31
 */
@Getter
public class Constant {

    public static final String SALT = "everKeep_salt";

    public static final String UPLOAD_ROOT_PATH = "/uploads/";

    public static final String THUMBNAIL_SUFFIX = "_thumb";

    /**
     * 配置键常量
     */
    public interface ConfigKey {
        /** 用户最大存储空间 (bytes) */
        String MAX_STORAGE_SIZE = "max_storage_size";
        /** 单个文件最大限制 (bytes) */
        String MAX_FILE_SIZE = "max_file_size";
        /** 允许的文件后缀 */
        String ALLOWED_EXTENSIONS = "allowed_extensions";
        /** 上传根路径 */
        String UPLOAD_PATH = "upload_path";
        /** 用户上传目录 */
        String USER_UPLOAD_DIR = "user_upload_dir";

        /** 存储类型 (LOCAL/S3) */
        String STORAGE_TYPE = "storage_type";

        interface S3 {
            String ENDPOINT = "s3_endpoint";
            String ACCESS_KEY = "s3_access_key";
            String SECRET_KEY = "s3_secret_key";
            String BUCKET = "s3_bucket";
            String DOMAIN = "s3_domain";
        }

        interface TencentCOS {
            String SECRET_ID = "tencent_cos_secret_id";
            String SECRET_KEY = "tencent_cos_secret_key";
            String BUCKET = "tencent_cos_bucket";
            String REGION = "tencent_cos_region";
        }
    }

    /**
     * 缓存常量
     */
    public interface Cache {
        String CONFIG = "config";

        Long EXPIRE_7_DAYS = 60 * 60 * 24 * 7L;

        Long EXPIRE_1_HOUR = 60 * 60L;

        Long EXPIRE_30_MINUTES = 60 * 30L;

        Long EXPIRE_1_MINUTE = 60L;

        Long EXPIRE_5_SECONDS = 5L;

        Long EXPIRE_NONE = -1L;
    }

    /**
     * 角色常量
     */
    public interface Role {
        Long ADMIN = 1L;
        Long USER = 2L;
    }
}