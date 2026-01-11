package org.xcyms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.xcyms.entity.Image;
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
@Service
public class ImageServiceImpl extends ServiceImpl<ImageMapper, Image> implements IImageService {

}
