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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
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
    public ApiResult<?> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "albumId", required = false) Long albumId,
            @RequestParam(value = "category", required = false, defaultValue = "image") String category) {
        if (file.isEmpty()) {
            return ApiResult.error("文件不能为空");
        }

        Long userId = StpUtil.getLoginIdAsLong();

        // 验证 category 合法性，防止路径穿越
        if (category.contains("..") || category.contains("/") || category.contains("\\")) {
            return ApiResult.error("非法的文件类型参数");
        }

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

        // 1. 定义存储路径 (从配置读取根路径)
        String rootPath = configService.getConfigValue(userId, "upload_path");
        if (rootPath == null || rootPath.isEmpty()) {
            return ApiResult.error("未配置上传路径，请联系管理员");
        }

        // 确保根路径以分隔符结尾
        if (!rootPath.endsWith(File.separator)) {
            rootPath += File.separator;
        }

        // 2. 构建年月日路径 (如: 2024/01/16)
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        // 构建分类和日期文件夹路径 (如: avatar/2024/01/16/)
        // 注意：Web 访问路径统一用 /，物理路径用 File.separator
        String subPath = category + "/" + datePath + "/";
        String physicalSubPath = subPath.replace("/", File.separator);
        String fullPath = rootPath + physicalSubPath;

        // 3. 生成新文件名
        String newFileName = UUID.randomUUID() + suffix;

        File destFile = new File(fullPath + newFileName);
        File destDir = destFile.getParentFile();
        if (destDir != null && !destDir.exists()) {
            if (!destDir.mkdirs()) {
                return ApiResult.error("文件上传目录创建失败，请检查权限: " + destDir.getAbsolutePath());
            }
        }

        try {
            // 4. 保存文件到本地
            file.transferTo(destFile);

            // 5. 返回 Web 访问路径
            String webUrl = "/uploads/" + subPath + newFileName;

            // 6. 如果是普通图片，则存入数据库 biz_image 表
            // 头像和封面通常由具体业务接口保存 URL，不作为“相册图片”管理
            if ("image".equals(category)) {
                Image image = new Image();
                image.setUserId(userId);
                image.setAlbumId(albumId);
                image.setName(originalFilename);
                image.setUrl(webUrl);
                image.setSize(file.getSize());
                image.setType(ext);
                imageService.save(image);
                return ApiResult.success(image);
            }

            // 其他类型（头像、封面等）直接返回 URL 供前端调用业务接口保存
            Map<String, String> result = new HashMap<>();
            result.put("url", webUrl);
            result.put("name", originalFilename);
            return ApiResult.success(result);
        } catch (IOException e) {
            return ApiResult.error("文件上传失败: " + e.getMessage());
        }
    }
}