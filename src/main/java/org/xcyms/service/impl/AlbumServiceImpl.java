package org.xcyms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.xcyms.common.ApiResult;
import org.xcyms.entity.Album;
import org.xcyms.entity.dto.AlbumDTO;
import org.xcyms.mapper.AlbumMapper;
import org.xcyms.service.IAlbumService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 相册表 服务实现类
 * </p>
 *
 * @author liu-xu
 * @since 2026-01-11
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlbumServiceImpl extends ServiceImpl<AlbumMapper, Album> implements IAlbumService {

    private final ModelMapper modelMapper;

    @Override
    public ApiResult<List<AlbumDTO>> getMyAlbums() {
        return ApiResult.success(this.list(new QueryWrapper<Album>()
                        .lambda().orderByDesc(Album::getCreateTime))
                .stream().map(album -> modelMapper.map(album, AlbumDTO.class))
                .collect(Collectors.toList()));
    }

    @Override
    public boolean createAlbum(AlbumDTO albumDTO) {
        return baseMapper.insert(modelMapper.map(albumDTO, Album.class)) > 0;
    }

    @Override
    public ApiResult<List<AlbumDTO>> getMyAlbums(String name, Long userId) {
        return ApiResult.success(this.list(new QueryWrapper<Album>()
                        .lambda()
                        .select(Album::getId, Album::getName)
                        .eq(null != userId, Album::getUserId, userId)
                        .like(StringUtils.isNotBlank(name), Album::getName, name)
                        .orderByDesc(Album::getCreateTime))
                .stream().map(album -> modelMapper.map(album, AlbumDTO.class))
                .collect(Collectors.toList()));
    }
}
