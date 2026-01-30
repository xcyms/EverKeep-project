package org.xcyms.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.xcyms.common.ApiResult;
import org.xcyms.common.annotation.ApiDoc;
import org.xcyms.entity.Album;
import org.xcyms.entity.dto.AlbumDTO;
import org.xcyms.service.IAlbumService;

import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * 相册表 前端控制器
 * </p>
 *
 * @author liu-xu
 * @since 2026-01-11
 */
@Slf4j
@ApiDoc("相册管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/album")
public class AlbumController {

    private final IAlbumService albumService;

    @ApiDoc("获取我的相册列表")
    @GetMapping("/list")
    public ApiResult<List<AlbumDTO>> getMyAlbums(String name) {
        return albumService.getMyAlbums(name, StpUtil.getLoginIdAsLong());
    }

    @ApiDoc("分页获取相册列表")
    @PostMapping("/page")
    public ApiResult<IPage<AlbumDTO>> page(@RequestParam(required = false) String column,
                                           @RequestParam(required = false, defaultValue = "true") boolean asc,
                                           Page<Album> page,
                                           @RequestBody AlbumDTO albumDTO) {
        // 处理排序字段
        if (StringUtils.isNotBlank(column)) {
            // 如果前端传入的是 imageCount，则直接使用该别名排序
            // 定义允许排序的白名单
            List<String> allowColumns = Arrays.asList("a.create_time", "imageCount");
            if (allowColumns.contains(column)) {
                page.addOrder(asc ? OrderItem.asc(column) : OrderItem.desc(column));
            } else {
                // 如果传入非法字段，可以记录日志或默认不排序/抛出异常
                log.warn("检测到非法的排序字段请求: {}", column);
            }
        }
        // 增加 id 作为二级排序，防止分页重复
        page.addOrder(OrderItem.desc("id"));

        albumDTO.setUserId(StpUtil.getLoginIdAsLong());
        // 业务层根据 albumDTO.getName() 进行模糊查询
        return albumService.getPage(page, albumDTO);
    }

    @ApiDoc("创建相册")
    @PostMapping("/create")
    public ApiResult<String> create(@RequestBody Album album) {
        album.setUserId(StpUtil.getLoginIdAsLong());
        return albumService.save(album) ? ApiResult.success("创建成功") : ApiResult.error("创建失败");
    }

    @ApiDoc("更新相册")
    @PostMapping("/update")
    public ApiResult<String> update(@RequestBody Album album) {
        return albumService.updateById(album) ? ApiResult.success("更新成功") : ApiResult.error("更新失败");
    }

    @ApiDoc("删除相册")
    @DeleteMapping("/delete")
    public ApiResult<String> delete(Long id) {
        return albumService.removeById(id) ? ApiResult.success("删除成功") : ApiResult.error("删除失败");
    }
}