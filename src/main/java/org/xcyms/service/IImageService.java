package org.xcyms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.xcyms.common.ApiResult;
import org.xcyms.entity.Image;
import org.xcyms.entity.dto.ImageDTO;

/**
 * <p>
 * 图片表 服务类
 * </p>
 *
 * @author liu-xu
 * @since 2026-01-11
 */
public interface IImageService extends IService<Image> {

    ApiResult<?> getPage(Page<Image> page, ImageDTO imageDTO);
}
