package org.xcyms.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
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
import org.xcyms.entity.Video;
import org.xcyms.entity.dto.VideoDTO;
import org.xcyms.service.IVideoService;

import java.util.List;

/**
 * <p>
 * 视频表 前端控制器
 * </p>
 *
 * @author liu-xu
 * @since 2026-02-08
 */
@ApiDoc("视频管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/video")
public class VideoController {

    private final IVideoService videoService;

    @ApiDoc("获取我的视频分页列表")
    @PostMapping("/page")
    public ApiResult<IPage<VideoDTO>> page(@RequestParam(required = false) String column,
                                           @RequestParam(required = false, defaultValue = "true") boolean asc,
                                           Page<Video> page,
                                           @RequestBody VideoDTO videoDTO) {
        if (StringUtils.isNotBlank(column)) {
            page.addOrder(asc ? OrderItem.asc(column) : OrderItem.desc(column));
        }
        page.addOrder(OrderItem.desc("id"));
        videoDTO.setUserId(StpUtil.getLoginIdAsLong());
        return videoService.getPage(page, videoDTO);
    }

    @ApiDoc("获取公开视频分页列表")
    @PostMapping("/public/page")
    public ApiResult<IPage<VideoDTO>> publicPage(@RequestParam(required = false) String column,
                                                 @RequestParam(required = false, defaultValue = "false") boolean asc,
                                                 Page<Video> page,
                                                 @RequestBody VideoDTO videoDTO) {
        if (StringUtils.isNotBlank(column)) {
            page.addOrder(asc ? OrderItem.asc(column) : OrderItem.desc(column));
        } else {
            page.addOrder(OrderItem.desc("create_time"));
        }
        page.addOrder(OrderItem.desc("id"));
        videoDTO.setStatus(YesNoEnum.YES);
        videoDTO.setUserId(null);
        return videoService.getPage(page, videoDTO);
    }

    @ApiDoc("批量删除视频")
    @DeleteMapping("/delete")
    public ApiResult<String> delete(@RequestBody List<Long> idList) {
        return videoService.removeByIds(idList) ? ApiResult.success("删除成功") : ApiResult.error("删除失败");
    }

    @ApiDoc("更新视频公开状态")
    @PostMapping("/updateStatus")
    public ApiResult<String> updateStatus(@RequestBody VideoDTO videoDTO) {
        return videoService.updateStatus(videoDTO);
    }

    @ApiDoc("移动视频到相册")
    @PostMapping("/move")
    public ApiResult<String> move(Long videoId, Long albumId) {
        return videoService.move(videoId, albumId);
    }

    @ApiDoc("获取回收站视频列表")
    @GetMapping("/recycle/page")
    public ApiResult<IPage<VideoDTO>> recyclePage(Page<Video> page) {
        return videoService.getRecyclePage(page, StpUtil.getLoginIdAsLong());
    }

    @ApiDoc("从回收站恢复视频")
    @PostMapping("/restore")
    public ApiResult<String> restore(@RequestBody List<Long> idList) {
        return videoService.restore(idList);
    }

    @ApiDoc("永久删除视频")
    @DeleteMapping("/deletePermanently")
    public ApiResult<String> deletePermanently(@RequestBody List<Long> idList) {
        return videoService.deletePermanently(idList);
    }

    @ApiDoc("批量移动视频到相册")
    @PostMapping("/batchMove")
    public ApiResult<String> batchMove(@RequestBody VideoDTO videoDTO) {
        return videoService.batchMove(videoDTO);
    }

    @ApiDoc("获取所有视频分页列表(管理员)")
    @SaCheckRole("ADMIN")
    @PostMapping("/admin/page")
    public ApiResult<IPage<VideoDTO>> adminPage(@RequestParam(required = false) String column,
                                                @RequestParam(required = false, defaultValue = "false") boolean asc,
                                                Page<Video> page,
                                                @RequestBody VideoDTO videoDTO) {
        if (StringUtils.isNotBlank(column)) {
            page.addOrder(asc ? OrderItem.asc(column) : OrderItem.desc(column));
        } else {
            page.addOrder(OrderItem.desc("create_time"));
        }
        page.addOrder(OrderItem.desc("id"));
        return videoService.getPage(page, videoDTO);
    }

    @ApiDoc("设置视频封面")
    @PostMapping("/setCover")
    public ApiResult<String> setCover(@RequestParam Long videoId) {
        return videoService.setCover(videoId);
    }
}