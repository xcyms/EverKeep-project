package org.xcyms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xcyms.common.ApiResult;
import org.xcyms.entity.Image;
import org.xcyms.entity.dto.ImageDTO;
import org.xcyms.service.IImageService;

/**
 * <p>
 * 图片表 前端控制器
 * </p>
 *
 * @author liu-xu
 * @since 2026-01-11
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/image")
public class ImageController {

    private final IImageService imageService;

    /**
     * 获取图片列表
     *
     * @param page     分页参数
     * @param imageDTO 查询参数
     * @return org.xcyms.common.ApiResult<?>
     * @author liu-xu
     * @date 2026/1/12 9:24
     */
    @PostMapping("/page")
    public ApiResult<?> page(Page<Image> page, @RequestBody ImageDTO imageDTO) {
        return imageService.getPage(page, imageDTO);
    }
}
