package org.xcyms.controller;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xcyms.common.ApiResult;
import org.xcyms.common.annotation.ApiDoc;
import org.xcyms.entity.dto.StatsSummaryDTO;
import org.xcyms.service.IStatisticsService;

/**
 * 统计信息控制器
 */
@ApiDoc("数据统计")
@RestController
@RequiredArgsConstructor
@RequestMapping("/stats")
public class StatisticsController {

    private final IStatisticsService statisticsService;

    @ApiDoc("获取系统统计摘要数据")
    @GetMapping("/summary")
    public ApiResult<StatsSummaryDTO> getSummary() {
        return statisticsService.getSummary(StpUtil.getLoginIdAsLong());
    }
}