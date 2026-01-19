package org.xcyms.service.impl;

import cn.dev33.satoken.stp.StpUtil;
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
import org.springframework.web.multipart.MultipartFile;
import org.xcyms.common.ApiResult;
import org.xcyms.entity.Album;
import org.xcyms.entity.Image;
import org.xcyms.entity.dto.ImageDTO;
import org.xcyms.mapper.AlbumMapper;
import org.xcyms.mapper.ImageMapper;
import org.xcyms.service.IConfigService;
import org.xcyms.service.IImageService;
import org.xcyms.utils.IdGenerator;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;

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
    private final IConfigService configService;

    @Override
    public ApiResult<ImageDTO> uploadImage(MultipartFile file, Long albumId, String category) {
        if (file == null || file.isEmpty()) {
            return ApiResult.error("文件不能为空");
        }

        Long userId = StpUtil.getLoginIdAsLong();
        String originalFilename = file.getOriginalFilename();
        if (StringUtils.isBlank(originalFilename)) {
            return ApiResult.error("文件名不能为空");
        }

        // 1. 验证 (大小、格式)
        ApiResult<ImageDTO> validateResult = validateFile(file, userId);
        if (validateResult.getCode() != 200) {
            return validateResult;
        }

        // 2. 路径处理
        String suffix = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        String relativePath = getRelativePath(userId, category);
        String rootPath = configService.getConfigValue(null, "upload_path");
        if (StringUtils.isBlank(rootPath)) {
            return ApiResult.error("未配置系统上传根路径");
        }

        String fullDirPath = rootPath.endsWith(File.separator) ? rootPath + relativePath : rootPath + File.separator + relativePath;
        String newFileName = IdGenerator.nextIdStr() + suffix;
        File destFile = new File(fullDirPath.replace("/", File.separator), newFileName);

        // 3. 保存文件
        try {
            if (!destFile.getParentFile().exists() && !destFile.getParentFile().mkdirs()) {
                return ApiResult.error("目录创建失败");
            }
            file.transferTo(destFile);
        } catch (IOException e) {
            log.error("文件保存失败", e);
            return ApiResult.error("文件保存失败");
        }

        // 4. 数据入库
        String webUrl = "/uploads/" + relativePath + newFileName;
        if ("image".equals(category)) {
            Image image = new Image();
            image.setUserId(userId);
            image.setAlbumId(albumId);
            image.setName(originalFilename);
            image.setUrl(webUrl);
            image.setSize(file.getSize());
            image.setType(suffix.substring(1));
            this.save(image);
            return ApiResult.success(mapper.map(image, ImageDTO.class));
        }

        ImageDTO imageDTO = new ImageDTO();
        imageDTO.setName(originalFilename);
        imageDTO.setUrl(webUrl);
        return ApiResult.success(imageDTO);
    }

    private ApiResult<ImageDTO> validateFile(MultipartFile file, Long userId) {
        // 大小校验
        String maxSizeStr = configService.getConfigValue(userId, "max_file_size");
        long maxSize = StringUtils.isNotBlank(maxSizeStr) ? Long.parseLong(maxSizeStr) : 10 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            return ApiResult.error("文件大小超限");
        }
        // 格式校验
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        String allowedExtStr = configService.getConfigValue(userId, "allowed_extensions");
        Set<String> allowedExtensions = StringUtils.isNotBlank(allowedExtStr)
                ? Set.of(allowedExtStr.toLowerCase().split(","))
                : Set.of("jpg", "jpeg", "png", "gif", "webp");
        String ext = suffix.startsWith(".") ? suffix.substring(1) : suffix;
        if (!allowedExtensions.contains(ext)) {
            return ApiResult.error("不支持的文件格式");
        }
        return ApiResult.success();
    }

    private String getRelativePath(Long userId, String category) {
        String userSubDir = configService.getConfigValue(userId, "user_upload_dir");
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotBlank(userSubDir)) {
            sb.append(userSubDir).append("/");
        }
        sb.append(category).append("/").append(datePath).append("/");
        return sb.toString();
    }

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
    public ApiResult<IPage<ImageDTO>> getPage(Page<Image> page, ImageDTO imageDTO) {
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
    public ApiResult<String> updateStatus(ImageDTO imageDTO) {
        LambdaQueryWrapper<Image> queryWrapper = new LambdaQueryWrapper<Image>()
                .in(Image::getId, imageDTO.getIds());
        Image image = new Image();
        image.setStatus(imageDTO.getStatus());
        this.update(image, queryWrapper);
        return ApiResult.success("状态更新成功");
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
    public ApiResult<String> move(Long imageId, Long albumId) {
        Image image = new Image();
        image.setAlbumId(albumId);
        this.update(image, new LambdaQueryWrapper<Image>().eq(Image::getId, imageId));
        return ApiResult.success("移动成功");
    }

    @Override
    public ApiResult<String> setCover(Long imageId) {
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

        return ApiResult.success("封面设置成功");
    }

    @Override
    public ApiResult<String> batchMove(ImageDTO imageDTO) {
        if (imageDTO.getIds() == null || imageDTO.getIds().isEmpty()) {
            return ApiResult.error("未选择图片");
        }
        if (imageDTO.getAlbumId() == null) {
            return ApiResult.error("未选择目标相册");
        }

        Image image = new Image();
        image.setAlbumId(imageDTO.getAlbumId());
        this.update(image, new LambdaQueryWrapper<Image>().in(Image::getId, imageDTO.getIds()));
        return ApiResult.success("批量移动成功");
    }
}