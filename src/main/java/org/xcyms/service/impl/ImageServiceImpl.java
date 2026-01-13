package org.xcyms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
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
import org.xcyms.entity.dto.ImageDTO;
import org.xcyms.mapper.AlbumMapper;
import org.xcyms.mapper.ImageMapper;
import org.xcyms.service.IImageService;

/**
 * <p>
 * 图片表 服务实现类
 * </p>
 *
 * @author liu-xu
 * @since 2026-01-11
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageServiceImpl extends ServiceImpl<ImageMapper, Image> implements IImageService {

    private final ModelMapper mapper;
    private final AlbumMapper albumMapper;

    /**
     * 获取图片列表
     *
     * @param page     分页参数
     * @param imageDTO 查询参数
     * @return org.xcyms.common.ApiResult<?>
     * @author liu-xu
     * @date 2026/1/12 9:24
     */
    @Override
    public ApiResult<?> getPage(Page<Image> page, ImageDTO imageDTO) {
        IPage<Image> iPage = this.page(page, new QueryWrapper<Image>()
                .lambda()
                .like(StringUtils.isNotBlank(imageDTO.getName()), Image::getName, imageDTO.getName())
                .eq(null != imageDTO.getStatus(), Image::getStatus, imageDTO.getStatus())
                .eq(null != imageDTO.getAlbumId(), Image::getAlbumId, imageDTO.getAlbumId())
                .eq(null != imageDTO.getUserId(), Image::getUserId, imageDTO.getUserId()));
        IPage<ImageDTO> iPageDTO = iPage.convert(image -> mapper.map(image, ImageDTO.class));
        return ApiResult.success(iPageDTO);
    }

    /**
     * 修改图片状态
     *
     * @param imageDTO 图片参数
     * @return org.xcyms.common.ApiResult<?>
     * @author liu-xu
     * @date 2026/1/12 9:24
     */
    @Override
    public ApiResult<?> updateStatus(ImageDTO imageDTO) {
        LambdaQueryWrapper<Image> queryWrapper = new LambdaQueryWrapper<Image>()
                .in(Image::getId, imageDTO.getIds());
        Image image = new Image();
        image.setStatus(imageDTO.getStatus());
        this.update(image, queryWrapper);
        return ApiResult.success();
    }

    /**
     * 移动图片
     *
     * @param imageId 图片id
     * @param albumId 相册id
     * @return org.xcyms.common.ApiResult<?>
     * @author liu-xu
     * @date 2026/1/12 9:24
     */
    @Override
    public ApiResult<?> move(Long imageId, Long albumId) {
        Image image = new Image();
        image.setAlbumId(albumId);
        this.update(image, new LambdaQueryWrapper<Image>().eq(Image::getId, imageId));
        return ApiResult.success();
    }

    @Override
    public ApiResult<?> setCover(Long imageId) {
        if (imageId == null || imageId <= 0) {
            return ApiResult.error("图片ID不能为空");
        }

        Image image = getById(imageId);
        if (image == null) {
            return ApiResult.error("图片不存在");
        }

        Long albumId = image.getAlbumId();
        if (albumId == null) {
            return ApiResult.error("图片未关联相册");
        }

        Album album = albumMapper.selectById(albumId);
        if (album == null) {
            return ApiResult.error("相册不存在");
        }

        // 验证图片确实属于该相册
        if (!album.getId().equals(image.getAlbumId())) {
            return ApiResult.error("图片不属于指定相册");
        }

        Album updateAlbum = new Album();
        updateAlbum.setCover(image.getUrl());
        albumMapper.update(updateAlbum, new LambdaQueryWrapper<Album>().eq(Album::getId, album.getId()));

        return ApiResult.success();
    }

}
