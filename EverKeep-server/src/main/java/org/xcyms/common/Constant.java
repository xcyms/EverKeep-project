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
    }

    /**
     * 缓存常量
     */
    public interface Cache {
        String CONFIG = "config";
    }

    /**
     * 角色常量
     */
    public interface Role {
        Long ADMIN = 1L;
        Long USER = 2L;
    }
}