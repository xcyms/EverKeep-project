package org.xcyms.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.xcyms.common.ApiResult;
import org.xcyms.common.annotation.ApiDoc;
import org.xcyms.common.annotation.UploadLimit;
import org.xcyms.service.IImageService;
import org.xcyms.service.IVideoService;

/**
 * <p>
 * 文件上传控制器
 * <p/>
 * @author liu-xu
 * @date 2026年01月11日 15:35
 */
@ApiDoc("文件上传")
@RestController
@RequiredArgsConstructor
@RequestMapping("/file")
public class FileController {

    private final IImageService imageService;
    private final IVideoService videoService;

    @ApiDoc("通用文件上传接口")
    @PostMapping("/upload")
    @UploadLimit()
    public ApiResult<?> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "albumId", required = false) Long albumId,
            @RequestParam(value = "category", required = false, defaultValue = "image") String category) {
        if ("video".equals(category)) {
            return videoService.uploadVideo(file, albumId, category);
        }
        return imageService.uploadImage(file, albumId, category);
    }
}