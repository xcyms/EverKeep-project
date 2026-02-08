package org.xcyms.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.xcyms.entity.Video;
import org.xcyms.mapper.VideoMapper;
import org.xcyms.service.storage.StorageFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * <p>
 * 视频处理类（提取封面图等）
 * </p>
 *
 * @author liu-xu
 * @date 2026年02月08日
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VideoProcessor {

    private final VideoMapper videoMapper;
    private final StorageFactory storageFactory;

    /**
     * 异步提取视频封面
     *
     * @param videoFile   本地临时视频文件
     * @param videoId     视频记录ID
     * @param relativeDir 封面图保存的相对目录
     */
    @Async("ioExecutor")
    public void processVideoAsync(File videoFile, Long videoId, String relativeDir) {
        log.info("开始处理视频封面: {}, ID: {}", videoFile.getName(), videoId);
        File coverFile = null;
        FFmpegFrameGrabber grabber = null;
        try {
            if (!videoFile.exists()) {
                log.warn("视频文件不存在，跳过处理: {}", videoFile.getAbsolutePath());
                return;
            }

            grabber = new FFmpegFrameGrabber(videoFile);
            grabber.start();

            // 获取视频时长（微秒转秒）
            long duration = grabber.getLengthInTime() / 1000000;

            // 获取视频第一帧（非空帧）
            Frame frame = null;
            int count = 0;
            // 尝试读取前 50 帧，直到找到一个有图像的帧（避免开头黑屏）
            while (count < 50 && (frame = grabber.grabImage()) != null) {
                if (frame.image != null) {
                    break;
                }
                count++;
            }

            if (frame != null && frame.image != null) {
                // 转换帧为图片
                Java2DFrameConverter converter = new Java2DFrameConverter();
                BufferedImage bi = converter.getBufferedImage(frame);

                // 保存临时封面图
                String coverName = videoFile.getName().substring(0, videoFile.getName().lastIndexOf(".")) + ".jpg";
                coverFile = File.createTempFile("cover_", ".jpg");
                ImageIO.write(bi, "jpg", coverFile);

                // 上传封面图
                String coverRelativePath = relativeDir + coverName;
                String coverUrl = storageFactory.getService().upload(coverFile, coverRelativePath);

                // 更新数据库
                Video updateVideo = new Video();
                updateVideo.setId(videoId);
                updateVideo.setCoverUrl(coverUrl);
                updateVideo.setDuration(duration);
                videoMapper.updateById(updateVideo);

                log.info("视频封面提取并上传成功: {}", coverUrl);
            }

        } catch (Exception e) {
            log.error("视频处理失败: {}", videoFile.getName(), e);
        } finally {
            try {
                if (grabber != null) {
                    grabber.stop();
                    grabber.release();
                }
            } catch (Exception e) {
                log.error("释放 grabber 失败", e);
            }
            // 清理临时封面文件
            if (coverFile != null && coverFile.exists()) {
                coverFile.delete();
            }
            // 清理视频临时文件
            if (videoFile != null && videoFile.exists()) {
                videoFile.delete();
            }
        }
    }
}