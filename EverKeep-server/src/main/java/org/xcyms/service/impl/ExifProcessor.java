package org.xcyms.service.impl;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.xcyms.common.Constant;
import org.xcyms.entity.Image;
import org.xcyms.mapper.ImageMapper;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * <p>
 * 图片exif信息处理
 * </p>
 *
 * @author liu-xu
 * @date 2026年01月21日 17:18
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExifProcessor {

    private final ImageMapper imageMapper;

    /**
     * 异步提取并更新图片元数据
     * @param file 物理文件
     * @param imageId 数据库记录ID
     * @param originalUrl 原始图片Web访问路径
     */
    @Async("ioExecutor")
    public void processExifAsync(File file, Long imageId, String originalUrl) {
        log.info("开始异步处理图片: {}, ID: {}, URL: {}", file.getName(), imageId, originalUrl);
        try {
            if (!file.exists()) {
                log.warn("文件不存在，跳过处理: {}", file.getAbsolutePath());
                return;
            }

            Image updateImage = new Image();
            updateImage.setId(imageId);

            // 1. 生成缩略图
            try {
                String originalPath = file.getAbsolutePath();
                String thumbnailPath = insertBeforeExtension(originalPath, Constant.THUMBNAIL_SUFFIX);
                File thumbFile = new File(thumbnailPath);

                log.debug("准备生成缩略图: {} -> {}", originalPath, thumbnailPath);

                Thumbnails.of(file)
                        .size(400, 400) // 限制最大宽高
                        .outputQuality(0.8) // 质量
                        .toFile(thumbFile);

                // 设置缩略图 URL
                if (originalUrl != null) {
                    String thumbUrl = insertBeforeExtension(originalUrl, Constant.THUMBNAIL_SUFFIX);
                    updateImage.setThumbnailUrl(thumbUrl);
                }
                log.info("缩略图生成成功: {}", thumbnailPath);
            } catch (Exception e) {
                log.error("缩略图生成失败 (ID: {}): {}", imageId, e.getMessage());
            }

            // 2. 解析 EXIF 信息
            Metadata metadata = ImageMetadataReader.readMetadata(file);
            boolean hasExif = false;

            // 1. 机身信息
            ExifIFD0Directory ifd0Dir = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            if (ifd0Dir != null) {
                updateImage.setMake(ifd0Dir.getString(ExifIFD0Directory.TAG_MAKE));
                updateImage.setModel(ifd0Dir.getString(ExifIFD0Directory.TAG_MODEL));
                hasExif = true;
            }

            // 2. 拍摄参数
            ExifSubIFDDirectory subIfdDir = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            if (subIfdDir != null) {
                updateImage.setExposureTime(subIfdDir.getString(ExifSubIFDDirectory.TAG_EXPOSURE_TIME));
                updateImage.setFNumber(subIfdDir.getString(ExifSubIFDDirectory.TAG_FNUMBER));
                updateImage.setIso(subIfdDir.getString(ExifSubIFDDirectory.TAG_ISO_EQUIVALENT));
                updateImage.setFocalLength(subIfdDir.getString(ExifSubIFDDirectory.TAG_FOCAL_LENGTH));
                updateImage.setLensModel(subIfdDir.getString(ExifSubIFDDirectory.TAG_LENS_MODEL));

                Date date = subIfdDir.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
                if (date != null) {
                    updateImage.setTakeTime(LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()));
                }
                hasExif = true;
            }

            // 3. GPS信息
            GpsDirectory gpsDir = metadata.getFirstDirectoryOfType(GpsDirectory.class);
            if (gpsDir != null && gpsDir.getGeoLocation() != null) {
                updateImage.setLat(String.valueOf(gpsDir.getGeoLocation().getLatitude()));
                updateImage.setLng(String.valueOf(gpsDir.getGeoLocation().getLongitude()));
                hasExif = true;
            }

            // 无论是否有 EXIF，只要生成了缩略图也需要更新
            if (hasExif || updateImage.getThumbnailUrl() != null) {
                imageMapper.updateById(updateImage);
                log.info("图片处理完成（EXIF/缩略图）: {}", imageId);
            }

        } catch (Exception e) {
            log.error("EXIF 异步解析失败: {}", file.getName(), e);
        }
    }
        private String insertBeforeExtension(String path, String suffix) {
            if (path == null) return null;
            int lastDotIndex = path.lastIndexOf(".");
            if (lastDotIndex == -1) {
                return path + suffix;
            }
            return path.substring(0, lastDotIndex) + suffix + path.substring(lastDotIndex);
        }
    }