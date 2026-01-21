package org.xcyms.service.impl;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
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
     */
    @Async("ioExecutor")
    public void processExifAsync(File file, Long imageId) {
        log.info("开始异步解析图片 EXIF: {}, ID: {}", file.getName(), imageId);
        try {
            // 确保文件已完全写入
            if (!file.exists()) {
                log.warn("文件不存在，跳过解析: {}", file.getAbsolutePath());
                return;
            }

            Metadata metadata = ImageMetadataReader.readMetadata(file);
            Image updateImage = new Image();
            updateImage.setId(imageId);
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

            if (hasExif) {
                imageMapper.updateById(updateImage);
                log.info("图片 EXIF 异步解析并更新完成: {}", imageId);
            } else {
                log.info("图片不包含 EXIF 信息: {}", imageId);
            }

        } catch (Exception e) {
            log.error("EXIF 异步解析失败: {}", file.getName(), e);
        }
    }
}
