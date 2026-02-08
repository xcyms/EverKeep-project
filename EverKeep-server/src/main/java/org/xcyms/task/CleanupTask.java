package org.xcyms.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.xcyms.entity.Image;
import org.xcyms.entity.Video;
import org.xcyms.mapper.ImageMapper;
import org.xcyms.mapper.VideoMapper;
import org.xcyms.service.IImageService;
import org.xcyms.service.IVideoService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *     文件清理定时任务
 * </p>
 *
 * @author liu-xu
 * @date 2026年01月21日 16:44
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CleanupTask {

    private final ImageMapper imageMapper;
    private final IImageService imageService;
    private final VideoMapper videoMapper;
    private final IVideoService videoService;

    /**
     * 每天凌晨2点执行，清理回收站中超过30天的图片和视频
     * cron: 秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupRecycleBin() {
        log.info("开始执行回收站清理任务...");

        // 1. 计算30天前的时间
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        // 2. 清理图片
        List<Image> expiredImages = imageMapper.selectExpiredRecycleImages(thirtyDaysAgo);
        if (!expiredImages.isEmpty()) {
            log.info("发现 {} 张过期图片，准备清理...", expiredImages.size());
            List<Long> imageIds = expiredImages.stream().map(Image::getId).collect(Collectors.toList());
            try {
                imageService.deletePermanently(imageIds);
                log.info("成功清理 {} 张过期图片", imageIds.size());
            } catch (Exception e) {
                log.error("清理过期图片时发生错误", e);
            }
        }

        // 3. 清理视频
        List<Video> expiredVideos = videoMapper.selectExpiredRecycleVideos(thirtyDaysAgo);
        if (!expiredVideos.isEmpty()) {
            log.info("发现 {} 个过期视频，准备清理...", expiredVideos.size());
            List<Long> videoIds = expiredVideos.stream().map(Video::getId).collect(Collectors.toList());
            try {
                videoService.deletePermanently(videoIds);
                log.info("成功清理 {} 个过期视频", videoIds.size());
            } catch (Exception e) {
                log.error("清理过期视频时发生错误", e);
            }
        }

        log.info("回收站清理任务执行完毕");
    }
}
