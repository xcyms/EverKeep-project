package org.xcyms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.xcyms.common.ApiResult;
import org.xcyms.entity.Album;
import org.xcyms.entity.Image;
import org.xcyms.entity.dto.AlbumDTO;
import org.xcyms.mapper.AlbumMapper;
import org.xcyms.mapper.ImageMapper;
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
    private final ImageMapper imageMapper;

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

    @Override
    public ApiResult<?> getPage(Page<Album> page, AlbumDTO albumDTO) {
        LambdaQueryWrapper<Album> wrapper = new LambdaQueryWrapper<>();
        if (albumDTO != null) {
            wrapper.like(StringUtils.isNotBlank(albumDTO.getName()), Album::getName, albumDTO.getName());
            wrapper.eq(null != albumDTO.getUserId(), Album::getUserId, albumDTO.getUserId());
        }
        Page<Album> userPage = this.page(page, wrapper);

        List<AlbumDTO> dtos = userPage.getRecords().stream().map(album -> {
            AlbumDTO dto = modelMapper.map(album, AlbumDTO.class);
            dto.setImageCount(imageMapper.selectCount(new QueryWrapper<Image>()
                    .lambda().eq(Image::getAlbumId, album.getId())));
            return dto;
        }).collect(Collectors.toList());

        Page<AlbumDTO> resultPage = new Page<>(page.getCurrent(), page.getSize(), userPage.getTotal());
        resultPage.setRecords(dtos);

        return ApiResult.success(resultPage);
    }
}
