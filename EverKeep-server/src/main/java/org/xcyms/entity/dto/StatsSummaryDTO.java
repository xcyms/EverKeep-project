package org.xcyms.entity.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *     首页统计DTO
 * </p>
 *
 * @author liu-xu
 * @date 2026年01月16日 9:27
 */
@Data
public class StatsSummaryDTO {

    /** 图片总数 */
    private Long imageCount;
    /** 相册总数 */
    private Long albumCount;
    /** 系统用户数 (管理员可见总数，用户可见1) */
    private Long userCount;
    /** 已用存储空间 (字节) */
    private Long storageUsage;
    /** 最近7天上传趋势数据 (key: 日期, value: 数量) */
    private List<Map<String, Object>> uploadTrend;
}
