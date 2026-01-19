package org.xcyms.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.xcyms.common.ApiResult;
import org.xcyms.common.annotation.ApiDoc;
import org.xcyms.common.enums.YesNoEnum;
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
@ApiDoc("图片管理")
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
    @ApiDoc("获取我的图片分页列表")
    @PostMapping("/page")
    public ApiResult<IPage<ImageDTO>> page(@RequestParam(required = false) String column,
                                           @RequestParam(required = false, defaultValue = "true") boolean asc,
                                           Page<Image> page,
                                           @RequestBody ImageDTO imageDTO) {
        // 处理排序字段
        if (StringUtils.isNotBlank(column)) {
            page.addOrder(asc ? OrderItem.asc(column) : OrderItem.desc(column));
        }
        // 只能查看自己的图片
        imageDTO.setUserId(StpUtil.getLoginIdAsLong());
        return imageService.getPage(page, imageDTO);
    }

    /**
     * 获取公开图片列表 (画廊)
     */
    @ApiDoc("获取公开图片分页列表")
    @PostMapping("/public/page")
    public ApiResult<IPage<ImageDTO>> publicPage(@RequestParam(required = false) String column,
                                                 @RequestParam(required = false, defaultValue = "false") boolean asc,
                                                 Page<Image> page,
                                                 @RequestBody ImageDTO imageDTO) {
        if (StringUtils.isNotBlank(column)) {
            page.addOrder(asc ? OrderItem.asc(column) : OrderItem.desc(column));
        } else {
            // 默认按时间倒序
            page.addOrder(OrderItem.desc("create_time"));
        }
        // 强制查询公开状态的图片
        imageDTO.setStatus(YesNoEnum.YES);
        imageDTO.setUserId(null); // 查询所有用户的
        return imageService.getPage(page, imageDTO);
    }

    @ApiDoc("批量删除图片")
    @DeleteMapping("/delete")
    public ApiResult<String> delete(@RequestBody List<Long> idList) {
        return imageService.removeByIds(idList) ? ApiResult.success("删除成功") : ApiResult.error("删除失败");
    }

    @ApiDoc("更新图片公开状态")
    @PostMapping("/updateStatus")
    public ApiResult<String> updateStatus(@RequestBody ImageDTO imageDTO) {
        return imageService.updateStatus(imageDTO);
    }

    @ApiDoc("移动图片到相册")
    @PostMapping("/move")
    public ApiResult<String> move(Long imageId, Long albumId) {
        return imageService.move(imageId, albumId);
    }

    @ApiDoc("设置图片为相册封面")
    @PostMapping("/setCover")
    public ApiResult<String> setCover(Long imageId) {
        return imageService.setCover(imageId);
    }

    @ApiDoc("批量移动图片到相册")
    @PostMapping("/batchMove")
    public ApiResult<String> batchMove(@RequestBody ImageDTO imageDTO) {
        return imageService.batchMove(imageDTO);
    }
}