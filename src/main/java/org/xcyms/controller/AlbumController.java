package org.xcyms.controller;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xcyms.common.ApiResult;
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
}
