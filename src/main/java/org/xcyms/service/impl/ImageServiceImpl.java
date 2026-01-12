package org.xcyms.service.impl;

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
import org.xcyms.entity.Image;
import org.xcyms.entity.dto.ImageDTO;
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
}
