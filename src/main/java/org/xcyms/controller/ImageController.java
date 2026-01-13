package org.xcyms.controller;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.xcyms.common.ApiResult;
import org.xcyms.entity.Image;
import org.xcyms.entity.dto.ImageDTO;
import org.xcyms.service.IImageService;

import java.util.List;

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
    public ApiResult<?> page(@RequestParam(required = false) String column,
                             @RequestParam(required = false, defaultValue = "true") boolean asc,
                             Page<Image> page,
                             @RequestBody ImageDTO imageDTO) {
        // 处理排序字段
        if (StringUtils.isNotBlank(column)) {
            page.addOrder(asc ? OrderItem.asc(column) : OrderItem.desc(column));
        }
        return imageService.getPage(page, imageDTO);
    }

    @DeleteMapping("/delete")
    public ApiResult<?> delete(@RequestBody List<Long> idList) {
        return imageService.removeByIds(idList) ? ApiResult.success() : ApiResult.error("删除失败");
    }

    @PostMapping("/updateStatus")
    public ApiResult<?> updateStatus(@RequestBody ImageDTO imageDTO) {
        return imageService.updateStatus(imageDTO);
    }

    @PostMapping("/move")
    public ApiResult<?> move(Long imageId, Long albumId) {
        return imageService.move(imageId, albumId);
    }

    @PostMapping("/setCover")
    public ApiResult<?> setCover(Long imageId) {
        return imageService.setCover(imageId);
    }
}
