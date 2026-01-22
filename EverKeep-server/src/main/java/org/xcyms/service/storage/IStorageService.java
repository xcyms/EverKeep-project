package org.xcyms.service.storage;

import java.io.File;

/**
 * <p>
 *     存储服务接口
 * </p>
 *
 * @author liu-xu
 * @date 2026年01月22日 9:38
 */
public interface IStorageService {

    /**
     * 上传文件
     * @param file 本地临时文件
     * @param relativePath 相对路径 (如 user/1/image/2026/01/22/xxx.jpg)
     * @return 最终可访问的 Web URL 或路径
     */
    String upload(File file, String relativePath);

    /**
     * 删除文件
     * @param url 数据库存储的 URL
     */
    void delete(String url);
}
