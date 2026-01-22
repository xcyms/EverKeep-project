package org.xcyms.service.storage.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.xcyms.common.Constant;
import org.xcyms.service.IConfigService;
import org.xcyms.service.storage.IStorageService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * 本地磁盘存储实现
 */
@Slf4j
@Service("LOCAL")
@RequiredArgsConstructor
public class LocalStorageServiceImpl implements IStorageService {

    private final IConfigService configService;

    @Override
    public String upload(File file, String relativePath) {
        String rootPath = configService.getConfigValue(null, Constant.ConfigKey.UPLOAD_PATH);
        File destFile = new File(rootPath, relativePath.replace("/", File.separator));

        if (!destFile.getParentFile().exists()) {
            destFile.getParentFile().mkdirs();
        }

        try {
            Files.copy(file.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return Constant.UPLOAD_ROOT_PATH + relativePath;
        } catch (IOException e) {
            log.error("本地存储上传失败", e);
            throw new RuntimeException("文件保存失败");
        }
    }

    @Override
    public void delete(String url) {
        String rootPath = configService.getConfigValue(null, Constant.ConfigKey.UPLOAD_PATH);
        // 只有以 UPLOAD_ROOT_PATH 开头的才是本地路径
        if (url.startsWith(Constant.UPLOAD_ROOT_PATH)) {
            String relativePath = url.replace(Constant.UPLOAD_ROOT_PATH, "");
            File file = new File(rootPath, relativePath.replace("/", File.separator));
            if (file.exists()) {
                file.delete();
            }
        }
    }
}