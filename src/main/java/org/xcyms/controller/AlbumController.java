package org.xcyms.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.xcyms.common.ApiResult;
import org.xcyms.entity.Album;
import org.xcyms.entity.dto.AlbumDTO;
import org.xcyms.service.IAlbumService;

import java.util.List;

/**
 * <p>
 * 相册表 前端控制器
 * </p>
 *
 * @author liu-xu
 * @since 2026-01-11
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/album")
public class AlbumController {

    private final IAlbumService albumService;

    @GetMapping("/list")
    public ApiResult<List<AlbumDTO>> getMyAlbums(String name) {
        return albumService.getMyAlbums(name, StpUtil.getLoginIdAsLong());
    }

    @PostMapping("/page")
    public ApiResult<?> page(@RequestParam(required = false) String column,
                             @RequestParam(required = false, defaultValue = "true") boolean asc,
                             Page<Album> page,
                             @RequestBody AlbumDTO albumDTO) {

        // 处理排序字段
        if (StringUtils.isNotBlank(column)) {
            page.addOrder(asc ? OrderItem.asc(column) : OrderItem.desc(column));
        }

        // 业务层根据 albumDTO.getName() 进行模糊查询
        return albumService.getPage(page, albumDTO);
    }

    /**
     * 新增相册
     *
     * @param album 相册参数
     * @return org.xcyms.common.ApiResult<?>
     * @author liu-xu
     * @date 2026/1/13 11:57
     */
    @PostMapping("/create")
    public ApiResult<?> create(@RequestBody Album album) {
        album.setUserId(StpUtil.getLoginIdAsLong());
        return albumService.save(album) ? ApiResult.success() : ApiResult.error("创建失败");
    }

    /**
     * 修改相册
     *
     * @param album 相册参数
     * @return org.xcyms.common.ApiResult<?>
     * @author liu-xu
     * @date 2026/1/13 11:57
     */
    @PostMapping("/update")
    public ApiResult<?> update(@RequestBody Album album) {
        return albumService.updateById(album) ? ApiResult.success() : ApiResult.error("更新失败");
    }

    /**
     * 删除相册
     *
     * @param id 相册ID
     * @return org.xcyms.common.ApiResult<?>
     * @author liu-xu
     * @date 2026/1/13 11:57
     */
    @DeleteMapping("/delete")
    public ApiResult<?> delete(Long id) {
        return albumService.removeById(id) ? ApiResult.success() : ApiResult.error("删除失败");
    }
}
