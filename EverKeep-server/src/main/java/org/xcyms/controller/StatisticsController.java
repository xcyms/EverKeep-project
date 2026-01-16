package org.xcyms.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xcyms.common.ApiResult;
import org.xcyms.entity.Album;
import org.xcyms.entity.Image;
import org.xcyms.entity.dto.StatsSummaryDTO;
import org.xcyms.service.IAlbumService;
import org.xcyms.service.IImageService;
import org.xcyms.service.IUserService;

import java.util.List;
import java.util.Map;

/**
 * 统计信息控制器
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/stats")
public class StatisticsController {

    private final IImageService imageService;
    private final IAlbumService albumService;
    private final IUserService userService;

    @GetMapping("/summary")
    public ApiResult<StatsSummaryDTO> getSummary() {
        Long userId = StpUtil.getLoginIdAsLong();
        boolean isAdmin = StpUtil.hasRole("ADMIN");

        StatsSummaryDTO stats = new StatsSummaryDTO();

        // 1. 基础统计数据
        if (isAdmin) {
            stats.setImageCount(imageService.count());
            stats.setAlbumCount(albumService.count());
            stats.setUserCount(userService.count());
            Map<String, Object> map = imageService.getMap(new QueryWrapper<Image>()
                    .select("sum(size) as totalSize")
                    .eq("deleted", 0));
            stats.setStorageUsage(map != null && map.get("totalSize") != null ?
                    Long.parseLong(map.get("totalSize").toString()) : 0L);
        } else {
            stats.setImageCount(imageService.count(new QueryWrapper<Image>().eq("user_id", userId)));
            stats.setAlbumCount(albumService.count(new QueryWrapper<Album>().eq("user_id", userId)));
            stats.setUserCount(1L);
            Map<String, Object> map = imageService.getMap(new QueryWrapper<Image>()
                    .select("sum(size) as totalSize")
                    .eq("user_id", userId)
                    .eq("deleted", 0));
            stats.setStorageUsage(map != null && map.get("totalSize") != null ?
                    Long.parseLong(map.get("totalSize").toString()) : 0L);
        }

        // 2. 最近7天趋势统计 (MySQL 语法)
        String sql = "SELECT DATE_FORMAT(create_time, '%m-%d') as date, COUNT(*) as count " +
                "FROM biz_image " +
                "WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) " +
                (isAdmin ? "" : "AND user_id = " + userId + " ") +
                "AND deleted = 0 " +
                "GROUP BY date " +
                "ORDER BY date ASC";

        List<Map<String, Object>> trendData = imageService.listMaps(new QueryWrapper<Image>()
                .select("DATE_FORMAT(create_time, '%m-%d') as date", "COUNT(*) as count")
                .apply("create_time >= DATE_SUB(CURDATE(), INTERVAL 6 DAY)")
                .apply(!isAdmin, "user_id = {0}", userId)
                .eq("deleted", 0)
                .groupBy("date")
                .orderByAsc("date"));

        stats.setUploadTrend(trendData);

        return ApiResult.success(stats);
    }
}