package org.xcyms.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.xcyms.entity.Image;
import org.xcyms.mapper.ImageMapper;
import org.xcyms.service.IImageService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *     图片清理定时任务
 * </p>
 *
 * @author liu-xu
 * @date 2026年01月21日 16:44
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ImageCleanupTask {

    private final ImageMapper imageMapper;
    private final IImageService imageService;

    /**
     * 每天凌晨2点执行，清理回收站中超过30天的图片
     * cron: 秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupRecycleBin() {
        log.info("开始执行回收站清理任务...");

        // 1. 计算30天前的时间
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        // 2. 查询已删除且更新时间（即删除时间）早于30天前的记录
        List<Image> expiredImages = imageMapper.selectExpiredRecycleImages(thirtyDaysAgo);

        if (expiredImages.isEmpty()) {
            log.info("没有需要清理的过期图片");
            return;
        }

        log.info("发现 {} 张过期图片，准备清理...", expiredImages.size());

        // 3. 提取ID列表
        List<Long> idList = expiredImages.stream()
                .map(Image::getId)
                .collect(Collectors.toList());

        // 4. 调用 service 的永久删除方法（处理物理文件和数据库记录）
        try {
            imageService.deletePermanently(idList);
            log.info("成功清理 {} 张过期图片", idList.size());
        } catch (Exception e) {
            log.error("清理过期图片时发生错误", e);
        }

        log.info("回收站清理任务执行完毕");
    }
}
