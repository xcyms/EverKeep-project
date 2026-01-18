package org.xcyms.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.xcyms.common.ApiResult;
import org.xcyms.service.IImageService;

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

    // 图片上传接口
    @PostMapping("/upload")
    public ApiResult<?> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "albumId", required = false) Long albumId,
            @RequestParam(value = "category", required = false, defaultValue = "image") String category) {
        return imageService.uploadImage(file, albumId, category);
    }
}