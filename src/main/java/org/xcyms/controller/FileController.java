package org.xcyms.controller;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.xcyms.common.ApiResult;
import org.xcyms.entity.Image;
import org.xcyms.service.IImageService;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;

/**
 * @author liu-xu
 * @date 2026年01月11日 15:35
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/file")
public class FileController {

    private final IImageService imageService;

    // 图片上传接口
    @PostMapping("/upload")
    public ApiResult<?> upload(@RequestPart("file") MultipartFile file, @RequestParam(value = "albumId", required = false) Long albumId) {
        if (file.isEmpty()) {
            return ApiResult.error("文件不能为空");
        }

        // 验证文件大小 (例如限制为10MB)
        long maxSize = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxSize) {
            return ApiResult.error("文件大小不能超过10MB");
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

        // 允许的图片格式白名单
        Set<String> allowedExtensions = Set.of(".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp");
        if (!allowedExtensions.contains(suffix)) {
            return ApiResult.error("不支持的文件格式，仅支持: JPG, PNG, GIF, BMP, WEBP");
        }

        // 1. 定义存储路径 (建议从配置中读取)
        String filePath = System.getProperty("user.dir") + "/uploads/"; // 使用相对路径更安全
        File destDir = new File(filePath);
        if (!destDir.exists()) {
            // 检查目录创建结果
            if (!destDir.mkdirs()) {
                return ApiResult.error("文件上传目录创建失败，请检查权限");
            }
        }

        // 2. 生成新文件名
        String newFileName = UUID.randomUUID() + suffix;

        try {
            // 3. 保存文件到本地
            file.transferTo(new File(filePath + newFileName));

            // 4. 将图片元数据存入数据库
            Image image = new Image();
            image.setUserId(StpUtil.getLoginIdAsLong());
            image.setAlbumId(albumId);
            image.setName(originalFilename);
            image.setUrl("/uploads/" + newFileName); // 返回相对路径
            image.setSize(file.getSize());
            image.setType(suffix.replace(".", ""));

            imageService.save(image);

            return ApiResult.success(image);
        } catch (IOException e) {
            return ApiResult.error("文件上传失败: " + e.getMessage());
        }
    }
}
