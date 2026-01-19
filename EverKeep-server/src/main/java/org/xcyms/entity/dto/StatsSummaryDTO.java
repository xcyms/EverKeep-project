package org.xcyms.entity.dto;

import lombok.Data;
import org.xcyms.common.annotation.ApiDocProperty;

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

    @ApiDocProperty("图片总数")
    private Long imageCount;

    @ApiDocProperty("相册总数")
    private Long albumCount;

    @ApiDocProperty("系统用户数")
    private Long userCount;

    @ApiDocProperty("已用存储空间(Byte)")
    private Long storageUsage;

    @ApiDocProperty("总存储空间(Byte)")
    private Long totalSize;

    @ApiDocProperty("上传趋势数据列表")
    private List<Map<String, Object>> uploadTrend;
}