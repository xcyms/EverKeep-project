package org.xcyms.controller;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.xcyms.common.ApiResult;
import org.xcyms.entity.Image;
import org.xcyms.service.IConfigService;
import org.xcyms.service.IImageService;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;

/**
 * <p>
 * 文件上传控制器
 * <p/>
 * @author liu-xu
 * @date 2026年01月11日 15:35
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/file")
public class FileController {

    private final IImageService imageService;
    private final IConfigService configService;

    // 图片上传接口
    @PostMapping("/upload")
    public ApiResult<?> upload(@RequestPart("file") MultipartFile file, @RequestParam(value = "albumId", required = false) Long albumId) {
        if (file.isEmpty()) {
            return ApiResult.error("文件不能为空");
        }

        Long userId = StpUtil.getLoginIdAsLong();

        // 验证文件大小 (从配置读取)
        String maxSizeStr = configService.getConfigValue(userId, "max_file_size");
        long maxSize = maxSizeStr != null ? Long.parseLong(maxSizeStr) : 10 * 1024 * 1024; // 默认10MB
        if (file.getSize() > maxSize) {
            return ApiResult.error("文件大小超过限制，最大允许: " + (maxSize / 1024 / 1024) + "MB");
        }

        // 验证文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename.isEmpty()) {
            return ApiResult.error("文件名不能为空");
        }

        // 防止路径遍历攻击
        if (originalFilename.contains("..") || originalFilename.contains("/")) {
            return ApiResult.error("文件名包含非法字符");
        }

        // 获取文件扩展名并验证
        String suffix;
        int lastDotIndex = originalFilename.lastIndexOf(".");
        if (lastDotIndex > 0) {
            suffix = originalFilename.substring(lastDotIndex).toLowerCase();
        } else {
            return ApiResult.error("文件必须包含扩展名");
        }

        // 允许的图片格式 (从配置读取)
        String allowedExtStr = configService.getConfigValue(userId, "allowed_extensions");
        Set<String> allowedExtensions;
        if (allowedExtStr != null) {
            allowedExtensions = Set.of(allowedExtStr.toLowerCase().split(","));
        } else {
            allowedExtensions = Set.of("jpg", "jpeg", "png", "gif", "webp");
        }

        // 注意：后缀校验逻辑要匹配配置中的格式（带点或不带点）
        String ext = suffix.startsWith(".") ? suffix.substring(1) : suffix;
        if (!allowedExtensions.contains(ext)) {
            return ApiResult.error("不支持的文件格式，仅支持: " + String.join(", ", allowedExtensions).toUpperCase());
        }

        // 1. 定义存储路径 (建议从配置中读取)
        String filePath = configService.getConfigValue(userId, "upload_path");
        if (filePath == null || filePath.isEmpty()) {
            return ApiResult.error("未配置上传路径，请联系管理员");
        }

        // 确保路径以分隔符结尾
        if (!filePath.endsWith(File.separator)) {
            filePath += File.separator;
        }

        // 2. 生成新文件名
        String newFileName = UUID.randomUUID() + suffix;

        File destFile = new File(filePath + newFileName);
        File destDir = destFile.getParentFile();
        if (destDir != null && !destDir.exists()) {
            if (!destDir.mkdirs()) {
                return ApiResult.error("文件上传目录创建失败，请检查权限: " + destDir.getAbsolutePath());
            }
        }

        try {
            // 3. 保存文件到本地
            file.transferTo(destFile);

            // 4. 将图片元数据存入数据库
            Image image = new Image();
            image.setUserId(userId);
            image.setAlbumId(albumId);
            image.setName(originalFilename);
            image.setUrl("/uploads/" + newFileName); // 返回 Web 访问路径，而非磁盘物理路径
            image.setSize(file.getSize());
            image.setType(ext);

            imageService.save(image);

            return ApiResult.success(image);
        } catch (IOException e) {
            return ApiResult.error("文件上传失败: " + e.getMessage());
        }
    }
}
