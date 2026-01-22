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
import org.xcyms.service.storage.StorageFactory;

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
    private final StorageFactory storageFactory;

    /**
     * 异步提取并更新图片元数据
     * @param file 物理临时文件
     * @param imageId 数据库记录ID
     * @param originalUrl 原始图片Web访问路径
     */
    @Async("ioExecutor")
    public void processExifAsync(File file, Long imageId, String originalUrl) {
        log.info("开始异步处理图片: {}, ID: {}, URL: {}", file.getName(), imageId, originalUrl);
        File thumbFile = null;
        try {
            if (!file.exists()) {
                log.warn("文件不存在，跳过处理: {}", file.getAbsolutePath());
                return;
            }

            Image updateImage = new Image();
            updateImage.setId(imageId);

            // 1. 生成并上传缩略图
            try {
                String originalPath = file.getAbsolutePath();
                String thumbnailPath = insertBeforeExtension(originalPath);
                thumbFile = new File(thumbnailPath);

                log.debug("准备生成缩略图: {} -> {}", originalPath, thumbnailPath);

                Thumbnails.of(file)
                        .size(400, 400)
                        .outputQuality(0.8)
                        .toFile(thumbFile);

                // 上传缩略图 (使用与原图相同的相对路径逻辑，但在文件名后加后缀)
                // 注意：这里需要从 originalUrl 中提取出相对路径
                // 简化处理：我们直接从存储服务获取上传路径逻辑，或者在 uploadImage 时就把 relativePath 传过来
                // 为了简单，我们这里假设 relativePath 可以从 originalUrl 推导，或者干脆重新生成

                // 改进：我们这里需要 thumbnail 的相对路径
                // 暂时简单处理，从 originalUrl 提取相对路径部分
                String relativeUrl = extractRelativePath(originalUrl);
                String thumbRelativePath = insertBeforeExtension(relativeUrl);

                String thumbUrl = storageFactory.getService().upload(thumbFile, thumbRelativePath);
                updateImage.setThumbnailUrl(thumbUrl);

                log.info("缩略图生成并上传成功: {}", thumbUrl);
            } catch (Exception e) {
                log.error("缩略图处理失败 (ID: {}): {}", imageId, e.getMessage());
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
        } finally {
            // 清理临时文件
            if (file.exists()) {
                file.delete();
            }
            if (thumbFile != null && thumbFile.exists()) {
                thumbFile.delete();
            }
        }
    }

    private String extractRelativePath(String url) {
        if (url.startsWith(Constant.UPLOAD_ROOT_PATH)) {
            return url.replace(Constant.UPLOAD_ROOT_PATH, "");
        }
        // 对于 S3，使用 URI 解析路径
        try {
            java.net.URI uri = new java.net.URI(url);
            String path = uri.getPath();
            return path.startsWith("/") ? path.substring(1) : path;
        } catch (Exception e) {
            return url;
        }
    }
    private String insertBeforeExtension(String path) {
        if (path == null) return null;
        int lastDotIndex = path.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return path + Constant.THUMBNAIL_SUFFIX;
        }
        return path.substring(0, lastDotIndex) + Constant.THUMBNAIL_SUFFIX + path.substring(lastDotIndex);
    }
}