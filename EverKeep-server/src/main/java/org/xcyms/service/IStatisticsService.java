package org.xcyms.service;

import org.xcyms.common.ApiResult;
import org.xcyms.entity.dto.StatsSummaryDTO;

/**
 * 统计服务接口
 */
public interface IStatisticsService {
    /**
     * 获取系统统计摘要数据
     */
    ApiResult<StatsSummaryDTO> getSummary(Long userId);
}